package gcm.output.simstate;

import gcm.output.OutputItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An {@link OutputItem} that records the execution time and success of a
 * simulation execution.
 * 
 * Instances are constructed through the included builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.UNEXPECTED)
public final class SimulationStatusItem implements SimulationOutputItem {

	/*
	 * Container class for SimulationStatusItem
	 */
	private static class Scaffold {

		private ScenarioId scenarioId;

		private ReplicationId replicationId;

		private double duration;

		private boolean successful;
	}

	@NotThreadSafe
	public static class SimulationStatusItemBuilder {

		private Scaffold scaffold = new Scaffold();

		/**
		 * @throws RuntimeException
		 *             <li>if the scenario id is null
		 *             <li>if the replication id is null
		 * 
		 */
		private void validateScaffold() {
			if (scaffold.scenarioId == null) {
				throw new RuntimeException("null scenario id");
			}

			if (scaffold.replicationId == null) {
				throw new RuntimeException("null replication id");
			}

		}

		/**
		 * Builds the SimulationStausItem from the collected data
		 * 
		 * @throws RuntimeException
		 *             <li>if the scenario id has not been set
		 *             <li>if the replication id has not been set
		 */
		public SimulationStatusItem build() {
			try {
				validateScaffold();
				return new SimulationStatusItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the scenario id
		 * 
		 * @throws RuntimeException
		 *             if the scenario id is null
		 */
		public void setScenarioId(ScenarioId scenarioId) {
			if (scenarioId == null) {
				throw new RuntimeException("null scenario id");
			}

			scaffold.scenarioId = scenarioId;
		}

		/**
		 * Set the replication id
		 * 
		 * @throws RuntimeException
		 *             if the replication id is null
		 */
		public void setReplicationId(ReplicationId replicationId) {
			if (replicationId == null) {
				throw new RuntimeException("null replication id");
			}
			scaffold.replicationId = replicationId;
		}

		/**
		 * Sets the duration
		 * 
		 * @throws RuntimeException
		 *             if the duration is negative
		 */
		public void setDurartion(double duration) {
			if (duration < 0) {
				throw new RuntimeException("negative duration");
			}
			scaffold.duration = duration;
		}

		/**
		 * Sets the success state
		 */
		public void setSuccessful(boolean successful) {
			scaffold.successful = successful;
		}
	}

	/*
	 * private constructor used only by the builder
	 */
	private SimulationStatusItem(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	private final Scaffold scaffold;

	@Override
	public ScenarioId getScenarioId() {
		return scaffold.scenarioId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return scaffold.replicationId;
	}

	/**
	 * Returns the duration of the simulation's execution
	 */
	public double getDuration() {
		return scaffold.duration;
	}

	/**
	 * Returns true if and only if the simulation completed successfully
	 */
	public boolean successful() {
		return scaffold.successful;
	}

	/**
	 * Returns a boilerplate string representation in the form:
	 * 
	 * SimulationStatusItem [scenarioId=1, replicationId=2,id=3, duration=4.0,
	 * successful=true]
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimulationStatusItem [scenarioId=");
		builder.append(scaffold.scenarioId);
		builder.append(", replicationId=");
		builder.append(scaffold.replicationId);
		builder.append(", duration=");
		builder.append(scaffold.duration);
		builder.append(", successful=");
		builder.append(scaffold.successful);
		builder.append("]");
		return builder.toString();
	}

}
