package gcm.experiment.outputitemhandlersuppliers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import gcm.output.OutputItemHandler;
import gcm.output.simstate.ConsoleLogItemHandler;
import gcm.output.simstate.LogItem;
import gcm.output.simstate.NIOMemoryReportItemHandler;
import gcm.output.simstate.NIOPlanningQueueReportItemHandler;
import gcm.output.simstate.NIOProfileItemHandler;
import gcm.output.simstate.SimulationStatusItemHandler;
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
public final class NIOMetaItemHandlerSupplier implements Supplier<List<OutputItemHandler>>{
	private final List<OutputItemHandler> outputItemHandlers;

	/*
	 * Hidden constructor
	 */
	private NIOMetaItemHandlerSupplier(Scaffold scaffold) {
		this.outputItemHandlers = new ArrayList<>(scaffold.outputItemHandlers);
	}

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Scaffold {
		private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();

		private int replicationCount;
		private int scenarioCount;
		private OutputItemHandler logItemHandler;
		private Path profileReportPath;
		private Path memoryReportPath;
		private double memoryReportInterval;
		private Path planningQueueReportPath;
		private long planningQueueReportThreshold;
		private boolean produceSimulationStatusOutput;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Add the output item handler to the experiment run.
		 * 
		 * @param outputItemHandler
		 *            the {@link OutputItemHandler} to add
		 * 
		 * @throws RuntimeException
		 *             if the output item handler is null
		 */
		private void addOutputItemHandler(final OutputItemHandler outputItemHandler) {
			scaffold.outputItemHandlers.add(outputItemHandler);
		}

		/**
		 * Builds the supplier
		 */
		public NIOMetaItemHandlerSupplier build() {

			try {

				if (scaffold.logItemHandler == null) {
					scaffold.logItemHandler = new ConsoleLogItemHandler();
				}
				addOutputItemHandler(scaffold.logItemHandler);

				if (scaffold.produceSimulationStatusOutput) {
					addOutputItemHandler(new SimulationStatusItemHandler(scaffold.scenarioCount, scaffold.replicationCount, scaffold.logItemHandler));
				}

				if (scaffold.profileReportPath != null) {
					addOutputItemHandler(new NIOProfileItemHandler(scaffold.profileReportPath));
				}

				if (scaffold.memoryReportPath != null) {
					addOutputItemHandler(new NIOMemoryReportItemHandler(scaffold.memoryReportPath, scaffold.memoryReportInterval));
				}

				if (scaffold.planningQueueReportPath != null) {
					addOutputItemHandler(new NIOPlanningQueueReportItemHandler(scaffold.planningQueueReportPath, scaffold.planningQueueReportThreshold));
				}
				return new NIOMetaItemHandlerSupplier(scaffold);

			} finally {
				scaffold = new Scaffold();

			}

		}

		/**
		 * Turns on or off the logging of experiment progress to standard out.
		 * Default value is false.
		 * 
		 * @param produceConsoleOutput
		 *            turns on/off production of the experiment progress
		 *            reporting
		 */
		public Builder setProduceSimulationStatusOutput(boolean produceSimulationStatusOutput, int scenarioCount, int replicationCount) {
			scaffold.produceSimulationStatusOutput = produceSimulationStatusOutput;
			scaffold.replicationCount = replicationCount;
			scaffold.scenarioCount = scenarioCount;
			return this;
		}
		

		/**
		 * Sets the {@link LogItem} handler for the experiment. Defaulted to
		 * null -- no logging.
		 */
		public Builder setLogItemHandler(OutputItemHandler logItemHandler) {
			scaffold.logItemHandler = logItemHandler;
			return this;
		}

		/**
		 * Sets the path for profile reporting. A null path turns off profile
		 * reporting. Default value is null.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 */
		public Builder setProfileReport(Path path) {
			scaffold.profileReportPath = path;
			return this;
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
		public Builder setMemoryReport(Path path, double memoryReportInterval) {

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
			return this;
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
		public Builder setPlanningQueueReport(Path path, long planningQueueReportThreshold) {

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
			return this;
		}
	}
	
	/**
	 * Supplies the {@link OutputItemHandler}s.
	 * 
	 */
	@Override
	public List<OutputItemHandler> get() {
		return new ArrayList<>(outputItemHandlers);
	}

}
