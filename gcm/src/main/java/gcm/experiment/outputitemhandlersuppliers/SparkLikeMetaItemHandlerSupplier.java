package gcm.experiment.outputitemhandlersuppliers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import gcm.output.OutputItemHandler;
import gcm.output.simstate.ConsoleLogItemHandler;
import gcm.output.simstate.LogItem;
import gcm.output.simstate.SimulationStatusItemHandler;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/** 
 * Demonstration implementor of meta level reporting for GCM
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
public final class SparkLikeMetaItemHandlerSupplier implements Supplier<List<OutputItemHandler>>{
	

	private final List<OutputItemHandler> outputItemHandlers;

	/*
	 * Hidden constructor
	 */
	private SparkLikeMetaItemHandlerSupplier(Scaffold scaffold) {
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
		public SparkLikeMetaItemHandlerSupplier build() {

			try {

				if (scaffold.logItemHandler == null) {
					scaffold.logItemHandler = new ConsoleLogItemHandler();
				}
				addOutputItemHandler(scaffold.logItemHandler);

				if (scaffold.produceSimulationStatusOutput) {
					addOutputItemHandler(new SimulationStatusItemHandler(scaffold.scenarioCount, scaffold.replicationCount, scaffold.logItemHandler));
				}

				return new SparkLikeMetaItemHandlerSupplier(scaffold);

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
