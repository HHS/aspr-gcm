package gcm.output.simstate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gcm.experiment.ExperimentProgressLog;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.TimeElapser;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.ThreadSafe;

/**
 * An {@link OutputItemHandler} implementor that handles
 * {@link SimulationStatusItem} and {@link SimulationWarningItem} objects and
 * converts them into {@link LogItem} objects in an aggregated manner to log the
 * completion status for the experiment.
 * 
 * @author Shawn Hatch
 *
 */

@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class SimulationOutuptItemHandler implements OutputItemHandler {
	/*
	 * Internal state guarded by this
	 */

	/*
	 * The size of the experiment = scenario count times replication count.
	 */
	private int experimentCount;

	private OutputItemHandler logItemHandler;

	/*
	 * The number of successful simulation executions accumulated in the current
	 * execution of the experiment.
	 */
	private int successCount;

	/*
	 * The number of successfully executed simulation runs prior to the current
	 * experiment execution. Derived from the ExperimentProgressLog. Needed to
	 * help differentiate progress achieved during the current execution from
	 * progress achieved in previous attempts to execute the experiment.
	 */
	private int previousProgressCount;

	/*
	 * The last reported percentage completion value that includes credit from
	 * previous executions of the experiment.
	 */
	private int lastReportPercentage = -1;

	/*
	 * Measures elapsed time from the beginning of the experiment and used to
	 * estimate the expected remaining time needed to execute the experiment
	 */
	private TimeElapser timeElapser = new TimeElapser();

	/*
	 * The collected simulation failures used at the close of the experiment to
	 * document which runs failed
	 */
	private List<SimulationStatusItem> failedSimulationStatusItems = new ArrayList<>();

	/**
	 * Constructor for {@link SimulationOutuptItemHandler}
	 * 
	 * @param scenarioCount
	 *            the number of scenarios in the experiment
	 * @param replicationCount
	 *            the number of replications in the experiment
	 */
	public SimulationOutuptItemHandler(int scenarioCount, int replicationCount, OutputItemHandler logItemHandler) {
		experimentCount = scenarioCount * replicationCount;
		this.logItemHandler = logItemHandler;
	}

	@Override
	public synchronized void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing
	}

	/*
	 * Returns the string representation of the value with a prepended "0" for
	 * numbers less than 10
	 */
	private String getBase60String(int value) {
		if (value < 10) {
			return "0" + Integer.toString(value);
		}
		return Integer.toString(value);
	}

	/*
	 * Returns a colon delimited string representation for the number of seconds
	 * in the form HH:MM:SS
	 */
	private String getTimeExpression(double seconds) {
		int n = (int) Math.round(seconds);
		int h = n / 3600;
		n = n % 3600;
		int m = n / 60;
		int s = n % 60;

		return h + ":" + getBase60String(m) + ":" + getBase60String(s);
	}
	
	private OutputItem sourceOutputItem;
	private final ScenarioId defaultScenarioId = new ScenarioId(0);
	private final ReplicationId defaultReplicationId = new ReplicationId(0); 

	private void log(LogStatus logStatus,String message) {
		ReplicationId replicationId;
		ScenarioId scenarioId;
		if(sourceOutputItem != null) {
			scenarioId = sourceOutputItem.getScenarioId();
			replicationId = sourceOutputItem.getReplicationId();
		}else {
			scenarioId = defaultScenarioId;
			replicationId = defaultReplicationId;
		}
		logItemHandler.handle(new LogItem(scenarioId, replicationId, logStatus, message));
	}	
	
	private void logInfo(String message) {		
		log(LogStatus.INFO, message);		
	}

	private void logError(String message) {
		log(LogStatus.ERROR, message);		
	}

	private void logWarning(String message) {
		log(LogStatus.WARNING, message);
	}

	@Override
	public synchronized void closeExperiment() {
		String timeExpression = getTimeExpression(timeElapser.getElapsedSeconds());

		int totalSuccessCount = previousProgressCount + successCount;
		String experimentCompletionMessage;
		if (previousProgressCount == 0) {
			experimentCompletionMessage = "Experiment finished with " + totalSuccessCount + " of " + experimentCount + " scenario replications successfully completed in " + timeExpression;
		} else {
			experimentCompletionMessage = "Experiment finished with " + totalSuccessCount + "(" + previousProgressCount + " from previous run(s))" + " of " + experimentCount
					+ " scenario replications successfully completed in " + timeExpression;
		}
		if (totalSuccessCount != experimentCount) {
			logError(experimentCompletionMessage);
		} else {
			logInfo(experimentCompletionMessage);
		}
		if (failedSimulationStatusItems.size() > 0) {
			logError("Failed simulations");
			int count = Math.min(100, failedSimulationStatusItems.size());
			for (int i = 0; i < count; i++) {
				SimulationStatusItem simulationStatusItem = failedSimulationStatusItems.get(i);
				logError("\t" + "Scenario " + simulationStatusItem.getScenarioId() + " Replication " + simulationStatusItem.getReplicationId());
			}
			if (failedSimulationStatusItems.size() > 100) {
				logError("\t...");
			}
		}
	}

	@Override
	public synchronized void handle(OutputItem outputItem) {
		sourceOutputItem = outputItem;
		if (outputItem instanceof SimulationStatusItem) {
			handleSimulationStatusItem((SimulationStatusItem) outputItem);
		} else {
			handleSimulationWarningItem((SimulationWarningItem) outputItem);
		}
		sourceOutputItem = null;
	}

	private final static int maxWarningCount = 20;
	private int warningCount;

	private void handleSimulationWarningItem(SimulationWarningItem simulationWarningItem) {
		if (warningCount < maxWarningCount) {
			warningCount++;			
			logWarning(simulationWarningItem.getWarning());
		}
	}

	private void handleSimulationStatusItem(SimulationStatusItem simulationStatusItem) {

		if (simulationStatusItem.successful()) {
			successCount++;
		} else {
			failedSimulationStatusItems.add(simulationStatusItem);
		}
		int completionCount = successCount + previousProgressCount + failedSimulationStatusItems.size();
		double completionProportion = completionCount;
		completionProportion /= experimentCount;
		completionProportion *= 100;
		int percentComplete = (int) completionProportion;
		if (percentComplete > lastReportPercentage) {
			lastReportPercentage = percentComplete;
			double averageTimePerExecution = timeElapser.getElapsedSeconds() / (successCount + failedSimulationStatusItems.size());
			int remainingExecutions = experimentCount - successCount - previousProgressCount - failedSimulationStatusItems.size();
			double expectedRemainingTime = Math.round(averageTimePerExecution * remainingExecutions);
			String timeExpression = getTimeExpression(expectedRemainingTime);
			logInfo(completionCount + " of " + experimentCount + " scenario replications, " + percentComplete + "% complete. Expected experiment completion in " + timeExpression);
		}

	}

	@Override
	public synchronized Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(SimulationStatusItem.class);
		result.add(SimulationWarningItem.class);
		return result;
	}

	@Override
	public synchronized void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing
	}

	@Override
	public synchronized void openExperiment(ExperimentProgressLog experimentProgressLog) {
		previousProgressCount = experimentProgressLog.size();
	}

}
