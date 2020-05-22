package gcm.experiment.progress;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.output.simstate.SimulationStatusItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An {@link OutputItemHandler} implementor that handles
 * {@link SimulationStatusItem} objects and records successful simulation
 * executions into a tab delimited, text file by writing one line per successful
 * execution. The line is composed of the numerical scenario id value followed
 * by the numerical replication id value.
 *
 * The {@link ExperimentProgressLog} passed during openExperiment() is used to
 * write the initial entries in the file, discarding any previous content.
 *
 * @author Shawn Hatch
 *
 */

@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class NIOExperimentProgressLogWriter implements OutputItemHandler {

	private static final String lineSeparator = System.getProperty("line.separator");

	/*
	 * The writer for lines in the file.
	 */
	@GuardedBy("this")
	private BufferedWriter writer;

	/*
	 * The file to which successful simulation execution entries are written
	 */
	@GuardedBy("this")
	private final Path progressLogFile;

	public NIOExperimentProgressLogWriter(final Path progressLogFile) {
		this.progressLogFile = progressLogFile;
	}

	@Override
	public synchronized void closeExperiment() {
		try {
			writer.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void closeSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		// do nothing
	}

	@Override
	public synchronized Set<Class<? extends OutputItem>> getHandledClasses() {
		final Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(SimulationStatusItem.class);
		return result;
	}

	@Override
	public synchronized void handle(final OutputItem outputItem) {
		final SimulationStatusItem simulationStatusItem = (SimulationStatusItem) outputItem;
		if (simulationStatusItem.successful()) {
			try {
				writer.write(simulationStatusItem.getScenarioId() + "\t" + simulationStatusItem.getReplicationId() + lineSeparator);
				writer.flush();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public synchronized void openExperiment(final ExperimentProgressLog experimentProgressLog) {
		/*
		 * We will clear out the old content from the file and replace it with
		 * the items in the experiment progress log.
		 */
		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		OutputStream out;
		try {
			out = Files.newOutputStream(progressLogFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

		try {
			for (final ScenarioId scenarioId : experimentProgressLog.getScenarioIds()) {
				for (final ReplicationId replicationId : experimentProgressLog.getReplicationIds(scenarioId)) {
					writer.write(scenarioId.getValue() + "\t" + replicationId.getValue() + lineSeparator);
				}
			}
			writer.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void openSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		// do nothing

	}
}
