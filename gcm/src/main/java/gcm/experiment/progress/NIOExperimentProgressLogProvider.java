package gcm.experiment.progress;

import java.nio.file.Path;

import gcm.output.OutputItemHandler;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class NIOExperimentProgressLogProvider implements ExperimentProgressLogProvider{
	
	private final ExperimentProgressLog experimentProgressLog;
	private final NIOExperimentProgressLogWriter nioExperimentProgressLogWriter;
	
	public NIOExperimentProgressLogProvider(Path experimentProgressLogPath) {
		experimentProgressLog = NIOExperimentProgressLogReader.read(experimentProgressLogPath);
		nioExperimentProgressLogWriter = new NIOExperimentProgressLogWriter(experimentProgressLogPath);
	}
	
	@Override
	public ExperimentProgressLog getExperimentProgressLog() {
		return experimentProgressLog;
	}
	
	@Override
	public OutputItemHandler getSimulationStatusItemHandler() {
		return nioExperimentProgressLogWriter;
	} 
	
}
