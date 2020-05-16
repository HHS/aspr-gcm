package gcm.output.simstate;

import gcm.output.OutputItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * An {@link OutputItem} implementor used to pseudo-logging of GCM items.  
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
@Source(status = TestStatus.UNEXPECTED)
public class LogItem implements OutputItem{
	
	private final ScenarioId scenarioId;
	private final ReplicationId replicationId;
	private final LogStatus logStatus;
	private final String message;
	
	@Override
	public ScenarioId getScenarioId() {
		return scenarioId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return replicationId;
	}
	
	public LogStatus getLogStatus() {
		return logStatus;
	}
	
	public String getMessage() {
		return message;
	}

	public LogItem(ScenarioId scenarioId, ReplicationId replicationId, LogStatus logStatus, String message) {
		super();
		this.scenarioId = scenarioId;
		this.replicationId = replicationId;
		this.logStatus = logStatus;
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LogItem [scenarioId=");
		builder.append(scenarioId);
		builder.append(", replicationId=");
		builder.append(replicationId);
		builder.append(", logStatus=");
		builder.append(logStatus);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}

}
