package gcm.output.simstate;

import gcm.scenario.ReplicationId;

import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

@Immutable
@Source(status = TestStatus.UNEXPECTED)
public class SimulationWarningItem implements SimulationOutputItem {

	private final String warning;
	private final ScenarioId scenarioId;
	private final ReplicationId replicationId;

	private static class Scaffold {
		private String warning;
		private ScenarioId scenarioId;
		private ReplicationId replicationId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		public SimulationWarningItem build() {
			try {
				return new SimulationWarningItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		public Builder setScenarioId(ScenarioId scenarioId) {
			scaffold.scenarioId = scenarioId;
			return this;
		}

		public Builder setReplicationId(ReplicationId replicationId) {
			scaffold.replicationId = replicationId;
			return this;
		}

		public Builder setWarning(String warning) {
			scaffold.warning = warning;
			return this;
		}

	}

	private SimulationWarningItem(Scaffold scaffold) {
		this.warning = scaffold.warning;
		this.scenarioId = scaffold.scenarioId;
		this.replicationId = scaffold.replicationId;
	}

	@Override
	public ScenarioId getScenarioId() {
		return scenarioId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return replicationId;
	}

	public String getWarning() {
		return warning;
	}

}