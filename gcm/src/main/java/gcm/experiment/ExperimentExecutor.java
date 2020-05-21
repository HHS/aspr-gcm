package gcm.experiment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gcm.experiment.progress.ExperimentProgressLog;
import gcm.experiment.progress.NIOExperimentProgressLogReader;
import gcm.experiment.progress.NIOExperimentProgressLogWriter;
import gcm.output.OutputItemHandler;
import gcm.output.reports.NIOReportItemHandlerImpl.NIOReportItemHandlerBuilder;
import gcm.output.reports.Report;
import gcm.output.reports.ReportPeriod;
import gcm.output.reports.commonreports.BatchStatusReport;
import gcm.output.reports.commonreports.CompartmentPopulationReport;
import gcm.output.reports.commonreports.CompartmentPropertyReport;
import gcm.output.reports.commonreports.CompartmentTransferReport;
import gcm.output.reports.commonreports.GlobalPropertyReport;
import gcm.output.reports.commonreports.GroupPopulationReport;
import gcm.output.reports.commonreports.GroupPropertyReport;
import gcm.output.reports.commonreports.GroupPropertyReport.GroupPropertyReportSettings;
import gcm.output.reports.commonreports.MaterialsProducerPropertyReport;
import gcm.output.reports.commonreports.MaterialsProducerResourceReport;
import gcm.output.reports.commonreports.PersonPropertyInteractionReport;
import gcm.output.reports.commonreports.PersonPropertyReport;
import gcm.output.reports.commonreports.PersonResourceReport;
import gcm.output.reports.commonreports.PersonResourceReport.PersonResourceReportOption;
import gcm.output.reports.commonreports.RegionPropertyReport;
import gcm.output.reports.commonreports.RegionTransferReport;
import gcm.output.reports.commonreports.ResourcePropertyReport;
import gcm.output.reports.commonreports.ResourceReport;
import gcm.output.reports.commonreports.StageReport;
import gcm.output.simstate.ConsoleLogItemHandler;
import gcm.output.simstate.LogItem;
import gcm.output.simstate.NIOMemoryReportItemHandler;
import gcm.output.simstate.NIOPlanningQueueReportItemHandler;
import gcm.output.simstate.NIOProfileItemHandler;
import gcm.output.simstate.SimulationStatusItemHandler;
import gcm.replication.Replication;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
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
public final class ExperimentExecutor {

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Scaffold {
		private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
		private Experiment experiment;
		private int replicationCount = 1;
		private long seed;
		private int threadCount;
		private boolean produceSimulationStatusOutput;
		private OutputItemHandler logItemHandler;
		private Path profileReportPath;
		private Path experimentProgressLogPath;
		private ExperimentProgressLog experimentProgressLog = ExperimentProgressLog.builder().build();
		private Path memoryReportPath;
		private double memoryReportInterval;
		private Path planningQueueReportPath;
		private long planningQueueReportThreshold;
		private NIOReportItemHandlerBuilder nioReportItemHandlerBuilder = new NIOReportItemHandlerBuilder();
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

	private Scaffold scaffold = new Scaffold();

	/**
	 * Adds the given scenarios to the experiment
	 *
	 * @param experiment
	 *            the experiment to be executed
	 */
	public void setExperiment(final Experiment experiment) {
		if (experiment == null) {
			throw new RuntimeException("null experiment");
		}
		scaffold.experiment = experiment;
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

		if (scaffold.logItemHandler == null) {
			scaffold.logItemHandler = new ConsoleLogItemHandler();
		}
		addOutputItemHandler(scaffold.logItemHandler);
		
		if (scaffold.produceSimulationStatusOutput) {			
			addOutputItemHandler(new SimulationStatusItemHandler(scaffold.experiment.getScenarioCount(), scaffold.replicationCount, scaffold.logItemHandler));
		}

		if (scaffold.profileReportPath != null) {
			addOutputItemHandler(new NIOProfileItemHandler(scaffold.profileReportPath));
		}

		if (scaffold.experimentProgressLogPath != null) {
			scaffold.experimentProgressLog = NIOExperimentProgressLogReader.read(scaffold.experimentProgressLogPath);
			addOutputItemHandler(new NIOExperimentProgressLogWriter(scaffold.experimentProgressLogPath));
		}

		if (scaffold.memoryReportPath != null) {
			addOutputItemHandler(new NIOMemoryReportItemHandler(scaffold.memoryReportPath, scaffold.memoryReportInterval));
		}

		if (scaffold.planningQueueReportPath != null) {
			addOutputItemHandler(new NIOPlanningQueueReportItemHandler(scaffold.planningQueueReportPath, scaffold.planningQueueReportThreshold));
		}

		scaffold.nioReportItemHandlerBuilder.setRegularExperiment(scaffold.experiment);

		addOutputItemHandler(scaffold.nioReportItemHandlerBuilder.build());

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
	 * Executes the experiment utilizing multiple threads. If the simulation
	 * throws an exception it is caught and handled by reporting to standard
	 * error that the failure occured as well as printing a stack trace.
	 */
	private void executeMultiThreaded() {
		try {
			/*
			 * Let all the output item handlers know that the experiment is
			 * starting
			 */
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.openExperiment(scaffold.experimentProgressLog);
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
					if (!scaffold.experimentProgressLog.contains(scenarioId, replicationId)) {
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
				 * Start the initial threads. Don't exceed the thread count or
				 * the job count. Each time a thread is cleared, a new
				 * simulation will be processed through the CompletionService
				 * until we run out of simulations to run.
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
				 * While there are still jobs to be assigned to a thread, or
				 * jobs that have not yet completed processing, we check to see
				 * if a new job needs processing and see if a previous job has
				 * completed.
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
					 * This call is blocking and waits for a job to complete and
					 * a thread to clear.
					 */
					try {
						completionService.take().get();
					} catch (final InterruptedException | ExecutionException e) {
						// Note that this is the completion service failing and
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
		} finally {
			scaffold = new Scaffold();
		}
	}

	/*
	 * Executes the experiment using the main thread. If the simulation throws
	 * an exception it is caught and handled by reporting to standard error that
	 * the failure occurred as well as printing a stack trace.
	 */
	private void executeSingleThreaded() {
		try {

			/*
			 * Let all the output item handlers know that the experiment is
			 * starting
			 */
			for (OutputItemHandler outputItemHandler : scaffold.outputItemHandlers) {
				outputItemHandler.openExperiment(scaffold.experimentProgressLog);
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
			 * Execute each scenario/replication pair that is not contained in
			 * the experiment progress log.
			 */

			for (int i = 0; i < scaffold.experiment.getScenarioCount(); i++) {
				Scenario scenario = scaffold.experiment.getScenario(i);
				for (final Replication replication : replications) {
					if (!scaffold.experimentProgressLog.contains(scenario.getScenarioId(), replication.getId())) {
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
		} finally {
			scaffold = new Scaffold();
		}
	}

	/**
	 * Sets the number of replications that will be performed for each scenario.
	 *
	 * @param replicationCount
	 *            the number of replications
	 * 
	 * @throws RuntimeException
	 *             if the replication count < 0
	 */
	public void setReplicationCount(final int replicationCount) {
		if (replicationCount < 1) {
			throw new RuntimeException("non-positive replication count");
		}
		scaffold.replicationCount = replicationCount;
	}

	/**
	 * Add the output item handler to the experiment run.
	 * 
	 * @param outputItemHandler
	 *            the {@link OutputItemHandler} to add
	 * 
	 * @throws RuntimeException
	 *             if the output item handler is null
	 */
	public void addOutputItemHandler(final OutputItemHandler outputItemHandler) {
		if (outputItemHandler == null) {
			throw new RuntimeException("null output item handler");
		}
		scaffold.outputItemHandlers.add(outputItemHandler);
	}

	/**
	 * Sets the seed value that will be used to generate the seeds for each
	 * replication
	 *
	 * @param seed
	 *            the seed value that is used to generate the seed values in the
	 *            replications
	 */
	public void setSeed(final long seed) {
		scaffold.seed = seed;
	}

	/**
	 * Sets the number of scenarios that may run concurrently. Generally this
	 * should be set to one less than the number of virtual processors on the
	 * machine that is running the experiment. Setting the thread count to zero
	 * causes the simulations to execute in the calling thread that invokes
	 * execute() on this ExperimentExecutor.
	 *
	 * @param threadCount
	 *            -- The number of threads to use to run the experiment.
	 * 
	 * @throws RuntimeException
	 *             if the thread count is negative
	 * 
	 */
	public void setThreadCount(final int threadCount) {
		if (threadCount < 0) {
			throw new RuntimeException("negative thread count");
		}
		scaffold.threadCount = threadCount;
	}

	/**
	 * Turns on or off the logging of experiment progress to standard out.
	 * Default value is false.
	 * 
	 * @param produceConsoleOutput
	 *            turns on/off production of the experiment progress reporting
	 */
	public void setProduceSimulationStatusOutput(boolean produceSimulationStatusOutput) {
		scaffold.produceSimulationStatusOutput = produceSimulationStatusOutput;
	}

	/**
	 * Sets the {@link LogItem} handler for the experiment. Defaulted to null --
	 * no logging.
	 */
	public void setLogItemHandler(OutputItemHandler logItemHandler) {
		scaffold.logItemHandler = logItemHandler;
	}

	/**
	 * Sets the path for profile reporting. A null path turns off profile
	 * reporting. Default value is null.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 */
	public void setProfileReport(Path path) {
		scaffold.profileReportPath = path;
	}

	/**
	 * Sets the path for experiment progress log. A null path turns off logging
	 * and run resumption. Default value is null.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 */
	public void setExperimentProgressLog(Path path) {
		scaffold.experimentProgressLogPath = path;
	}

	/**
	 * Sets the path for memory reporting. A null path turns off memory
	 * reporting. Default value is null.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param memoryReportInterval
	 *            the number of days between memory report executions.
	 * 
	 * @throws RuntimeException
	 *             if the path is non-null and the memory report interval is
	 *             non-positive
	 */
	public void setMemoryReport(Path path, double memoryReportInterval) {

		if (path != null) {
			if (memoryReportInterval <= 0) {
				throw new RuntimeException("non-positive memory report interval");
			}
			scaffold.memoryReportPath = path;
			scaffold.memoryReportInterval = memoryReportInterval;
		} else {
			scaffold.memoryReportPath = path;
			scaffold.memoryReportInterval = 0;
		}
	}

	/**
	 * Sets the path for planning queue reporting. A null path turns off
	 * planning queue reporting. Default value is null.
	 *
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param planningQueueReportThreshold
	 *            the number of additions & removals from the planning queue
	 *            that triggers report generation
	 * 
	 * @throws RuntimeException
	 *             if the path is non-null and the planning queue report
	 *             threshold is non-positive
	 */
	public void setPlanningQueueReport(Path path, long planningQueueReportThreshold) {

		if (path != null) {
			if (planningQueueReportThreshold < 1) {
				throw new RuntimeException("non-positive planning queue report threshold");
			}
			scaffold.planningQueueReportPath = path;
			scaffold.planningQueueReportThreshold = planningQueueReportThreshold;
		} else {
			scaffold.planningQueueReportPath = path;
			scaffold.planningQueueReportThreshold = 0;
		}
	}

	/**
	 * Turns on or off the display of experiment columns in all reports. Default
	 * is true.
	 * 
	 * @param displayExperimentColumnsInReports
	 *            turns on/off the display of the experiment columns in all
	 *            reports
	 */
	public void setDisplayExperimentColumnsInReports(boolean displayExperimentColumnsInReports) {
		scaffold.nioReportItemHandlerBuilder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports);
	}

	/**
	 * Adds a custom report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportClass
	 *            the class reference to a Report implementor that has an empty
	 *            constructor.
	 * 
	 * @param initializationData
	 *            the array of non-null,thread-safe objects that will be used to
	 *            initialize the report
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report class is null
	 *             <li>if the initialization data is null
	 *             <li>if any object in the initialization data is null
	 */
	public void addCustomReport(Path path, Class<? extends Report> reportClass, Object... initializationData) {
		Set<Object> initialData = new LinkedHashSet<>();
		for (Object initialDatum : initializationData) {
			initialData.add(initialDatum);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, reportClass, initialData);
	}

	/**
	 * Adds a custom report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportClass
	 *            the class reference to a Report implementor that has an empty
	 *            constructor.
	 * 
	 * @param initializationData
	 *            the collection of non-null,thread-safe objects that will be
	 *            used to initialize the report
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report class is null
	 *             <li>if the initialization data is null
	 *             <li>if any object in the initialization data is null
	 */
	public void addCustomReport(Path path, Class<? extends Report> reportClass, Collection<? extends Object> initializationData) {
		Set<Object> initialData = new LinkedHashSet<>(initializationData);
		scaffold.nioReportItemHandlerBuilder.addReport(path, reportClass, initialData);
	}

	/**
	 * Adds an Experiment Column Report. This report is independent of the
	 * choice to include experiment columns in all other reports. A null path
	 * turns off the report. Default value is null.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 */
	public void addExperimentColumnReport(Path path) {
		scaffold.nioReportItemHandlerBuilder.setExperimentColumnReport(path);
	}

	/**
	 * Adds a Batch Status Report.
	 *
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addBatchStatusReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, BatchStatusReport.class, initialData);
	}

	/**
	 * Adds a Compartment Population Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 */
	public void addCompartmentPopulationReport(Path path, ReportPeriod reportPeriod) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentPopulationReport.class, initialData);
	}

	/**
	 * Adds a Compartment Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addCompartmentPropertyReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentPropertyReport.class, initialData);
	}

	/**
	 * Adds a Compartment Transfer Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 */
	public void addCompartmentTransferReport(Path path, ReportPeriod reportPeriod) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentTransferReport.class, initialData);
	}

	/**
	 * Adds a Global Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param globalPropertyIds
	 *            the array of {@link GlobalPropertyId} that will be reported
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null *
	 *             <li>if the globalPropertyIds is null
	 *             <li>if member of the globalPropertyIds is null
	 * 
	 */
	public void addGlobalPropertyReport(Path path, GlobalPropertyId... globalPropertyIds) {
		Set<Object> initialData = new LinkedHashSet<>();

		if (globalPropertyIds == null) {
			throw new RuntimeException("null person Property Ids");
		}
		for (GlobalPropertyId globalPropertyId : globalPropertyIds) {
			if (globalPropertyId == null) {
				throw new RuntimeException("null global property");
			}
			initialData.add(globalPropertyId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, GlobalPropertyReport.class, initialData);
	}

	/**
	 * Adds a Global Population Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 */
	public void addGroupPopulationReport(Path path, ReportPeriod reportPeriod) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		scaffold.nioReportItemHandlerBuilder.addReport(path, GroupPopulationReport.class, initialData);
	}

	/**
	 * Adds a Group Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @param groupPropertyReportSettings
	 *            the {@link GroupPropertyReportSettings} for this report.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 *             <li>if the groupPropertyReportSettings is null
	 */
	public void addGroupPropertyReport(Path path, ReportPeriod reportPeriod, GroupPropertyReportSettings groupPropertyReportSettings) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		initialData.add(groupPropertyReportSettings);
		scaffold.nioReportItemHandlerBuilder.addReport(path, GroupPropertyReport.class, initialData);
	}

	/**
	 * Adds a Materials Producer Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addMaterialsProducerPropertyReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, MaterialsProducerPropertyReport.class, initialData);
	}

	/**
	 * Adds a Materials Producer Resource Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addMaterialsProducerResourceReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, MaterialsProducerResourceReport.class, initialData);
	}

	/**
	 * Adds a Person Property Interaction Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @param personPropertyIds
	 *            the array of {@link PersonPropertyId} that will be reported
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 *             <li>if the personPropertyIds is null
	 *             <li>if member of the personPropertyIds is null
	 */
	public void addPersonPropertyInteractionReport(Path path, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		if (personPropertyIds == null) {
			throw new RuntimeException("null person Property Ids");
		}
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			initialData.add(personPropertyId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, PersonPropertyInteractionReport.class, initialData);
	}

	/**
	 * Adds a Person Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @param personPropertyIds
	 *            the collection of {@link PersonPropertyId} that will be
	 *            reported. If none are specified, all person properties are
	 *            reported.
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 *             <li>if the personPropertyIds is null
	 *             <li>if member of the personPropertyIds is null
	 */
	public void addPersonPropertyReport(Path path, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		if (personPropertyIds == null) {
			throw new RuntimeException("null person Property Ids");
		}
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			if (personPropertyId == null) {
				throw new RuntimeException("null person property");
			}
			initialData.add(personPropertyId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, PersonPropertyReport.class, initialData);
	}

	/**
	 * Adds a Person Resource Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @param reportPeopleWithoutResources
	 *            turns on/off the reporting of people without a resource
	 * 
	 * @param reportZeroPopulations
	 *            turns on/off the reporting when the number of people having a
	 *            resource is zero
	 * 
	 * @param resourceIds
	 *            the array of {@link ResourceId} that will be reported. If no
	 *            resources are added, then all resources are assumed active.
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 *             <li>if the resourceIds is null
	 *             <li>if member of the resourceIds is null
	 */
	public void addPersonResourceReport(Path path, ReportPeriod reportPeriod, boolean reportPeopleWithoutResources, boolean reportZeroPopulations, ResourceId... resourceIds) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		if (reportPeopleWithoutResources) {
			initialData.add(PersonResourceReportOption.REPORT_PEOPLE_WITHOUT_RESOURCES);
		}
		if (reportZeroPopulations) {
			initialData.add(PersonResourceReportOption.REPORT_ZERO_POPULATIONS);
		}
		if (resourceIds == null) {
			throw new RuntimeException("null resouce ids");
		}
		for (ResourceId resourceId : resourceIds) {
			initialData.add(resourceId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, PersonResourceReport.class, initialData);
	}

	/**
	 * Adds a Region Property Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param regionPropertyIds
	 *            the array of {@link RegionPropertyId} that will be reported
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null *
	 *             <li>if the regionPropertyIds is null
	 *             <li>if member of the regionPropertyIds is null
	 * 
	 */
	public void addRegionPropertyReport(Path path, RegionPropertyId... regionPropertyIds) {
		Set<Object> initialData = new LinkedHashSet<>();

		if (regionPropertyIds == null) {
			throw new RuntimeException("null region Property Ids");
		}
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			if (regionPropertyId == null) {
				throw new RuntimeException("null region property");
			}
			initialData.add(regionPropertyId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, RegionPropertyReport.class, initialData);
	}

	/**
	 * Adds a Region Transfer Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 */
	public void addRegionTransferReport(Path path, ReportPeriod reportPeriod) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		scaffold.nioReportItemHandlerBuilder.addReport(path, RegionTransferReport.class, initialData);
	}

	/**
	 * Adds a Resource Property Report.
	 * 
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addResourcePropertyReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, ResourcePropertyReport.class, initialData);
	}

	/**
	 * Adds a Resource Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @param reportPeriod
	 *            the reporting interval for this report
	 * 
	 * 
	 * @param resourceIds
	 *            the array of {@link ResourceId} that will be reported
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 *             <li>if the report period is null
	 *             <li>if the resourceIds is null
	 *             <li>if member of the resourceIds is null
	 */
	public void addResourceReport(Path path, ReportPeriod reportPeriod, ResourceId... resourceIds) {
		Set<Object> initialData = new LinkedHashSet<>();
		initialData.add(reportPeriod);
		for (ResourceId resourceId : resourceIds) {
			initialData.add(resourceId);
		}
		scaffold.nioReportItemHandlerBuilder.addReport(path, ResourceReport.class, initialData);
	}

	/**
	 * Adds a Stage Report.
	 * 
	 * @param path
	 *            the {@link Path} where the report will be recorded
	 * 
	 * @throws RuntimeException
	 *             <li>if the path is null
	 */
	public void addStageReport(Path path) {
		Set<Object> initialData = new LinkedHashSet<>();
		scaffold.nioReportItemHandlerBuilder.addReport(path, StageReport.class, initialData);
	}

}
