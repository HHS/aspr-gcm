package gcm.output.simstate;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.experiment.progress.ExperimentProgressLog;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.ThreadSafe;
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public class ConsoleLogItemHandler implements OutputItemHandler {

	@Override
	public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing

	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
		// do nothing

	}

	@Override
	public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing

	}

	@Override
	public void closeExperiment() {
		// do nothing

	}

	@Override
	public void handle(OutputItem outputItem) {
		LogItem logItem = (LogItem) outputItem;
		StringBuilder sb = new StringBuilder();
		if(logItem.getScenarioId().getValue()!=0||logItem.getReplicationId().getValue()!=0) {
			sb.append("[scenario = ");
			sb.append(logItem.getScenarioId().getValue());
			sb.append(", replication = ");
			sb.append(logItem.getReplicationId().getValue());
			sb.append("] ");
		}
		sb.append(logItem.getLogStatus());
		sb.append(": ");
		sb.append(logItem.getMessage());
		String message = sb.toString();
		
		if (logItem.getLogStatus() == LogStatus.ERROR) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(LogItem.class);
		return result;
	}

}
