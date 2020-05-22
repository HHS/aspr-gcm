package gcm.experiment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

import org.apache.commons.math3.util.FastMath;

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

/**
 * Multi-threaded executor of an experiment using replications, reports and
 * various settings that influence how the experiment is executed.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
public final class ExperimentExecutorSparkLike {
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
	private static class Job {
		private final int scenarioIndex;
		private final int replicationIndex;
		private final List<OutputItemHandler> outputItemHandlers;

		public Job(int scenarioIndex, int replicationIndex, List<OutputItemHandler> outputItemHandlers) {
			super();
			this.scenarioIndex = scenarioIndex;
			this.replicationIndex = replicationIndex;
			this.outputItemHandlers = outputItemHandlers;
		}

	}

	private final Scaffold scaffold;

	private ExperimentExecutorSparkLike(Scaffold scaffold) {
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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {

		}

		public ExperimentExecutorSparkLike build() {
			try {

				if (scaffold.experiment == null) {
					throw new RuntimeException("null experiment");
				}

				return new ExperimentExecutorSparkLike(scaffold);
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
		 * Sets the number of scenarios that may run concurrently.
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

	/*
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occured as well as printing a stack trace.
	 */

	/**
	 * Executes the experiment using the information supplied via the various
	 * mutation methods. Clears all collected data upon completion. Thus this
	 * ExperimentExecutor returns to an empty and idle state.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * 
	 * @throws RuntimeException
	 *             if the experiment was not set
	 */
	public void execute()  {
		/*
		 * Initialize the experiment progress log. If a provider was
		 * contributed, get the output item handler that will write the new
		 * entries into the log
		 */
		ExperimentProgressLog experimentProgressLog;
		if (scaffold.experimentProgressLogProvider != null) {
			experimentProgressLog = scaffold.experimentProgressLogProvider.getExperimentProgressLog();
			scaffold.outputItemHandlers.add(scaffold.experimentProgressLogProvider.getSimulationStatusItemHandler());
		} else {
			experimentProgressLog = ExperimentProgressLog.builder().build();
		}

		/*
		 * Let all the output item handlers know that the experiment is starting
		 * and give them a chance to update existing output to be consistent
		 * with the experiment progress log.
		 */
		for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
			outputItemHandler.openExperiment(experimentProgressLog);
		}

		/*
		 * Build a scenario cache. This will help hold down on excessive
		 * scenario constructions.
		 */
		ScenarioCache scenarioCache = new ScenarioCache(scaffold.replicationCount, scaffold.experiment);

		/*
		 * Establish the set of replications
		 */
		final List<Replication> replications = Replication.getReplications(scaffold.replicationCount, scaffold.seed);

		/*
		 * Create the jobs. Only create the jobs that are not covered in the
		 * experiment progress log.
		 */
			
		List<Job> jobs = new ArrayList<>();
		for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
			for (int j = 0; j < replications.size(); j++) {
				ScenarioId scenarioId = scaffold.experiment.getScenarioId(i);
				ReplicationId replicationId = replications.get(j).getId();
				if (!experimentProgressLog.contains(scenarioId, replicationId)) {
					Job job = new Job(i, j, scaffold.outputItemHandlers);
					jobs.add(job);
				}
			}
		}

		/*
		 * Execute the jobs.
		 */
		
		//Determine the number of threads to allocate to a ForkJoinPool.
		int threadCount = scaffold.threadCount;
		threadCount = FastMath.max(threadCount,1);
		threadCount = FastMath.min(Runtime.getRuntime().availableProcessors(),threadCount);
		
		try {
			new ForkJoinPool(threadCount).submit(() -> {			
				jobs.parallelStream().forEach(job -> {
					Scenario scenario = scenarioCache.getScenario(job.scenarioIndex);
					Replication replication = replications.get(job.replicationIndex);

					final Simulation simulation = new Simulation();
					simulation.setScenario(scenario);
					simulation.setReplication(replication);
					for (OutputItemHandler outputItemHandler : job.outputItemHandlers) {
						simulation.addOutputItemHandler(outputItemHandler);
					}
					try {					
						simulation.execute();
					} catch (final Exception e) {
						System.err.println("Simulation failure for scenario " + scenario.getScenarioId() + " and replication " + replication.getId());
						e.printStackTrace();
					}
				});
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		/*
		 * Let the output items handlers know that the experiment is
		 * finished.
		 */
		scaffold.outputItemHandlers.stream().forEach(handler -> handler.closeExperiment());
	}
}