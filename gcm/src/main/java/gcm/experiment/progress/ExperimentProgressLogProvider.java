package gcm.experiment.progress;

import gcm.output.OutputItemHandler;

public interface ExperimentProgressLogProvider {

	public ExperimentProgressLog getExperimentProgressLog();
	
	public OutputItemHandler getSimulationStatusItemHandler();
	
}
