package gcm.experiment.progress;

import java.nio.file.Path;

import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class NIOExperimentProgressLogProvider implements ExperimentProgressLogProvider{
	
	private final ExperimentProgressLog experimentProgressLog;
	private final NIOExperimentProgressLogWriter nioExperimentProgressLogWriter;
	
	
	/**
	 * Constructs this provider with the given path for experiment progress log.
	 * 
	 * 
	 * 
	 */

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
