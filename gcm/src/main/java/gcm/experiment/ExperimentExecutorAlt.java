package gcm.experiment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import gcm.experiment.progress.ExperimentProgressLog;
import gcm.experiment.progress.ExperimentProgressLogProvider;
import gcm.output.OutputItemHandler;
import gcm.replication.Replication;
import gcm.scenario.ReplicationId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioId;
import gcm.simulation.Simulation;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Multi-threaded executor of an experiment using replications, reports and
 * various settings that influence how the experiment is executed.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
public final class ExperimentExecutorAlt {
	private static class ScenarioCacheBlock {
		private final Scenario scenario;
		private int replicationCount;

		public ScenarioCacheBlock(Scenario scenario) {
			this.scenario = scenario;
		}
	}

	/*
	 * A cache for scenarios to cut down on scenario generation costs.
	 */
	private static class ScenarioCache {
		private final int replicationCount;
		private final Experiment experiment;
		private Map<Integer, ScenarioCacheBlock> cache = new LinkedHashMap<>();

		public ScenarioCache(int replicationCount, Experiment experiment) {
			this.replicationCount = replicationCount;
			this.experiment = experiment;
		}

		public Scenario getScenario(int scenarioIndex) {
			ScenarioCacheBlock scenarioCacheBlock = cache.get(scenarioIndex);
			if (scenarioCacheBlock == null) {
				Scenario scenario = experiment.getScenario(scenarioIndex);
				scenarioCacheBlock = new ScenarioCacheBlock(scenario);
				cache.put(scenarioIndex, scenarioCacheBlock);
			}
			scenarioCacheBlock.replicationCount++;
			if (scenarioCacheBlock.replicationCount >= replicationCount) {
				cache.remove(scenarioIndex);
			}
			return scenarioCacheBlock.scenario;
		}

	}

	/*
	 * Utility class used for executing the experiment in a multi-threaded mode.
	 * Represents the scenario/replication pair. Allows for sorting where
	 * scenarios are executed with a random order so that long running scenarios
	 * are less likely to occupy all threads at the same time.
	 *
	 */
	private static class Job implements Comparable<Job> {
		int scenarioIndex;
		int replicationIndex;
		double randomizedValue;

		@Override
		public int compareTo(Job job) {
			int result = Integer.compare(replicationIndex, job.replicationIndex);
			if (result == 0) {
				result = Double.compare(randomizedValue, job.randomizedValue);
			}
			return result;
		}
	}

	private final Scaffold scaffold;

	private ExperimentExecutorAlt(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Scaffold {
		private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
		private Experiment experiment;
		private int replicationCount = 1;
		private long seed;
		private int threadCount;		
		private ExperimentProgressLogProvider experimentProgressLogProvider;
	}

	/*
	 * Class representing the return type of SimulationCallable. This is largely
	 * a placeholder since there has to be a return type for a callable.
	 * SimResult is never utilized.
	 */
	@Immutable
	private static class SimResult {
		private final boolean success;
		private final ScenarioId scenarioId;
		private final ReplicationId replicationId;

		public SimResult(final ScenarioId scenarioId, final ReplicationId replicationId, final boolean success) {
			this.scenarioId = scenarioId;
			this.replicationId = replicationId;
			this.success = success;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("Simulation Run for");
			builder.append(" Scenario ");
			builder.append(scenarioId);
			builder.append(" Replication ");
			builder.append(replicationId);
			if (success) {
				builder.append(" succeeded");
			} else {
				builder.append(" failed");
			}
			return builder.toString();
		}
	}

	/*
	 * A Callable implementor that runs the simulation in a thread from the
	 * completion service. The simulation instance, its Components and
	 * StateChangeListeners are created in the child thread by this
	 * SimulationCallable via the call() invocation. In this manner, they are
	 * thread-contained and thus thread-safe. It is important to note that no
	 * part of any of these objects should leak outside of the child thread.
	 *
	 * The parent and child thread share the following:
	 *
	 * <li>the Scenario <li>the Replication <li> the list of Output Item
	 * Handlers
	 *
	 * Thread safety is maintained by adherence to the following policies:
	 *
	 * The Scenario is a thread safe immutable class.
	 *
	 * The Replication is a thread safe immutable class.
	 * 
	 * The List of OutputItemHandler is a thread safe, immutable list of
	 * OutputItemHandler that are also thread safe.
	 *
	 * The SimResult is a thread safe, immutable class.
	 *
	 * The ReportItems are thread safe, immutable classes.
	 * 
	 */
	private static class SimulationCallable implements Callable<SimResult> {

		private final Scenario scenario;

		private final Replication replication;

		private final List<OutputItemHandler> outputItemHandlers;

		/*
		 * All construction arguments are thread safe implementations.
		 */
		private SimulationCallable(final Scenario scenario, final Replication replication, final List<OutputItemHandler> outputItemHandlers) {
			this.replication = replication;
			this.scenario = scenario;
			this.outputItemHandlers = new ArrayList<>(outputItemHandlers);
		}

		/**
		 * Executes the simulation using the scenario, replication and output
		 * item handlers. Returns a SimResult which will indicated
		 * success/failure. If the simulation throws an exception it is caught
		 * and handled by reporting to standard error that the failure occured
		 * as well as printing a stack trace.
		 */
		@Override
		public SimResult call() throws Exception {
			final Simulation simulation = new Simulation();
			simulation.setScenario(scenario);
			simulation.setReplication(replication);
			for (OutputItemHandler outputItemHandler : outputItemHandlers) {
				simulation.addOutputItemHandler(outputItemHandler);
			}

			// execute the simulation
			boolean success;
			try {
				simulation.execute();
				success = true;
			} catch (final Exception e) {
				success = false;
				System.err.println("Simulation failure for scenario " + scenario.getScenarioId() + " and replication " + replication.getId());
				e.printStackTrace();
			}
			return new SimResult(scenario.getScenarioId(), replication.getId(), success);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {

		}

		public ExperimentExecutorAlt build() {
			try {
				
				if (scaffold.experiment == null) {
					throw new RuntimeException("null experiment");
				}

				return new ExperimentExecutorAlt(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Adds the given scenarios to the experiment
		 *
		 */
		public Builder setExperiment(final Experiment experiment) {
			if (experiment == null) {
				throw new RuntimeException("null experiment");
			}
			scaffold.experiment = experiment;
			return this;
		}

		/**
		 * Sets the number of replications that will be performed for each
		 * scenario.
		 *
		 * 
		 * @throws RuntimeException
		 *             if the replication count < 0
		 */
		public Builder setReplicationCount(final int replicationCount) {
			if (replicationCount < 1) {
				throw new RuntimeException("non-positive replication count");
			}
			scaffold.replicationCount = replicationCount;
			return this;
		}

		/**
		 * Sets the seed value that will be used to generate the seeds for each
		 * replication
		 *
		 */
		public Builder setSeed(final long seed) {
			scaffold.seed = seed;
			return this;
		}

		/**
		 * Sets the number of scenarios that may run concurrently. Generally
		 * this should be set to one less than the number of virtual processors
		 * on the machine that is running the experiment. Setting the thread
		 * count to zero causes the simulations to execute in the calling thread
		 * that invokes execute() on this ExperimentExecutor.
		 * 
		 * 
		 * @throws RuntimeException
		 *             if the thread count is negative
		 * 
		 */
		public Builder setThreadCount(final int threadCount) {
			if (threadCount < 0) {
				throw new RuntimeException("negative thread count");
			}
			scaffold.threadCount = threadCount;
			return this;
		}

		/**
		 * Adds the {@link OutputItemHandler} objects from the given supplier.
		 */
		public Builder addOuputItemSupplier(final Supplier<List<OutputItemHandler>> outputItemHandlerSupplier) {
			scaffold.outputItemHandlers.addAll(outputItemHandlerSupplier.get());
			return this;
		}
		
		
		public Builder setExperimentProgressLogProvider(ExperimentProgressLogProvider experimentProgressLogProvider) {
			scaffold.experimentProgressLogProvider = experimentProgressLogProvider;
			return this;
		}
	}

	/**
	 * Executes the experiment using the information supplied via the various
	 * mutation methods. Clears all collected data upon completion. Thus this
	 * ExperimentExecutor returns to an empty and idle state.
	 * 
	 * @throws RuntimeException
	 *             if the experiment was not set
	 */
	public void execute() {
		

		if (scaffold.experiment == null) {
			throw new RuntimeException("null experiment");
		}

		if (scaffold.threadCount > 0) {
			executeMultiThreaded();
		} else {
			executeSingleThreaded();
		}
	}

	/*
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occured as well as printing a stack trace.
	 */
	private void executeMultiThreaded() {

		/*
		 * Let all the output item handlers know that the experiment is starting
		 */
		ExperimentProgressLog experimentProgressLog;
		if(scaffold.experimentProgressLogProvider != null) {
			experimentProgressLog = scaffold.experimentProgressLogProvider.getExperimentProgressLog();
			scaffold.outputItemHandlers.add(scaffold.experimentProgressLogProvider.getSimulationStatusItemHandler());
		}else {
			experimentProgressLog = ExperimentProgressLog.builder().build();
		}
		
		for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
			outputItemHandler.openExperiment(experimentProgressLog);
		}

		/*
		 * Get the replications
		 */

		ScenarioCache scenarioCache = new ScenarioCache(scaffold.replicationCount, scaffold.experiment);

		final List<Replication> replications = Replication.getReplications(scaffold.replicationCount, scaffold.seed);

		// Create the jobs and sort them to help avoid long running
		// scenarios from bunching up in the queue. Execute only the
		// scenario/replication pairs that are not contained in the
		// experiment progress log.
		List<Job> jobs = new ArrayList<>();
		Random random = new Random(scaffold.seed);
		for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
			for (int j = 0; j < replications.size(); j++) {
				ScenarioId scenarioId = scaffold.experiment.getScenarioId(i);
				ReplicationId replicationId = replications.get(j).getId();
				if (!experimentProgressLog.contains(scenarioId, replicationId)) {
					Job job = new Job();
					job.scenarioIndex = i;
					job.replicationIndex = j;
					job.randomizedValue = random.nextDouble();
					jobs.add(job);
				}
			}
		}

		// Collections.sort(jobs);

		/*
		 * If there is nothing to do, then do not engage.
		 */
		if (!jobs.isEmpty()) {

			int jobIndex = 0;

			// Create the Completion Service using the suggested thread
			// count
			final ExecutorService executorService = Executors.newFixedThreadPool(scaffold.threadCount);
			final CompletionService<SimResult> completionService = new ExecutorCompletionService<>(executorService);

			/*
			 * Start the initial threads. Don't exceed the thread count or the
			 * job count. Each time a thread is cleared, a new simulation will
			 * be processed through the CompletionService until we run out of
			 * simulations to run.
			 */
			while (jobIndex < Math.min(scaffold.threadCount, jobs.size()) - 1) {
				Job job = jobs.get(jobIndex);
				// Scenario scenario =
				// scaffold.experiment.getScenario(job.scenarioIndex);
				Scenario scenario = scenarioCache.getScenario(job.scenarioIndex);
				Replication replication = replications.get(job.replicationIndex);
				completionService.submit(new SimulationCallable(scenario, replication, scaffold.outputItemHandlers));
				jobIndex++;
			}

			/*
			 * While there are still jobs to be assigned to a thread, or jobs
			 * that have not yet completed processing, we check to see if a new
			 * job needs processing and see if a previous job has completed.
			 */
			int jobCompletionCount = 0;
			while (jobCompletionCount < jobs.size()) {
				if (jobIndex < jobs.size()) {
					Job job = jobs.get(jobIndex);
					// Scenario scenario =
					// scaffold.experiment.getScenario(job.scenarioIndex);
					Scenario scenario = scenarioCache.getScenario(job.scenarioIndex);
					Replication replication = replications.get(job.replicationIndex);
					completionService.submit(new SimulationCallable(scenario, replication, scaffold.outputItemHandlers));
					jobIndex++;
				}

				/*
				 * This call is blocking and waits for a job to complete and a
				 * thread to clear.
				 */
				try {
					completionService.take().get();
				} catch (final InterruptedException | ExecutionException e) {
					// Note that this is the completion service failing
					// and
					// not the simulation
					throw new RuntimeException(e);
				}

				/*
				 * Once the blocking call returns, we increment the
				 * jobCompletionCount
				 */
				jobCompletionCount++;
			}

			/*
			 * Since all jobs are done, the CompletionService is no longer
			 * needed so we shut down the executorService that backs it.
			 */
			executorService.shutdown();
		}
		/*
		 * We let the output items handlers know that the experiment is
		 * finished.
		 */
		for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
			outputItemHandler.closeExperiment();
		}

	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that
	 * the failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() {

		/*
		 * Let all the output item handlers know that the experiment is starting
		 */
		
		ExperimentProgressLog experimentProgressLog;
		if(scaffold.experimentProgressLogProvider != null) {
			experimentProgressLog = scaffold.experimentProgressLogProvider.getExperimentProgressLog();
			scaffold.outputItemHandlers.add(scaffold.experimentProgressLogProvider.getSimulationStatusItemHandler());
		}else {
			experimentProgressLog = ExperimentProgressLog.builder().build();
		}

		/*
		 * Retrieve the replications.
		 */
		final List<Replication> replications = Replication.getReplications(scaffold.replicationCount, scaffold.seed);

		/*
		 * The number of simulation runs is the product of the number of
		 * scenarios and the number of replications
		 */
		final int jobCount = replications.size() * scaffold.experiment.getScenarioCount();
		/*
		 * If there is nothing to do, then do not engage.
		 */
		if (jobCount == 0) {
			return;
		}

		/*
		 * Execute each scenario/replication pair that is not contained in the
		 * experiment progress log.
		 */

		for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
			Scenario scenario = scaffold.experiment.getScenario(i);
			for (final Replication replication : replications) {
				if (!experimentProgressLog.contains(scenario.getScenarioId(), replication.getId())) {
					final Simulation simulation = new Simulation();
					simulation.setReplication(replication);
					simulation.setScenario(scenario);
					for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
						simulation.addOutputItemHandler(outputItemHandler);
					}
					try {
						simulation.execute();

					} catch (final Exception e) {
						System.err.println("Simulation failure for scenario " + scenario.getScenarioId() + " and replication " + replication.getId());
						e.printStackTrace();
					}
				}
			}
		}

		for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
			outputItemHandler.closeExperiment();
		}

	}
}