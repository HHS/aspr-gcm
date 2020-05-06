package gcm.output.simstate;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.NIOHeaderedOutputItemHandler;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An {@link OutputItemHandler} that handles {@link MemoryReportItem} objects.
 * Each MemoryReportItem is written into a headered, tab-delimited, text file
 * passed to this {@link NIOMemoryReportItemHandler}. It is constructed with a
 * memory report interval(measured in days) that indicates to each simulation
 * instance how often (in simulation time) to generate MemoryReportItems.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class NIOMemoryReportItemHandler extends NIOHeaderedOutputItemHandler {

	@GuardedBy("this")
	private final double memoryReportInterval;

	/**
	 * Returns the amount of time in days between bulk generation of
	 * MemoryReportItems by each simulation.
	 */
	public synchronized double getMemoryReportInterval() {
		return memoryReportInterval;
	}

	public NIOMemoryReportItemHandler(Path path, double memoryReportInterval) {
		super(path);
		this.memoryReportInterval = memoryReportInterval;
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(MemoryReportItem.class);
		return result;
	}

	@Override
	protected String getHeader(OutputItem outputItem) {

		final StringBuilder sb = new StringBuilder();
		sb.append("Scenario");
		sb.append("\t");
		sb.append("Replication");
		sb.append("\t");
		sb.append("Id");
		sb.append("\t");
		sb.append("Parent Id");
		sb.append("\t");
		sb.append("Class");
		sb.append("\t");
		sb.append("Descriptor");
		sb.append("\t");
		sb.append("Time");
		sb.append("\t");
		sb.append("Self Byte Count");
		sb.append("\t");
		sb.append("Child Byte Count");
		sb.append("\t");
		sb.append("Total Byte Count");
		return sb.toString();
	}

	@Override
	protected String getOutputLine(OutputItem outputItem) {
		MemoryReportItem memoryReportItem = (MemoryReportItem) outputItem;

		final StringBuilder sb = new StringBuilder();
		sb.append(memoryReportItem.getScenarioId());
		sb.append("\t");
		sb.append(memoryReportItem.getReplicationId());
		sb.append("\t");
		sb.append(memoryReportItem.getId());
		sb.append("\t");
		sb.append(memoryReportItem.getParentId());
		sb.append("\t");
		sb.append(memoryReportItem.getItemClass().getSimpleName());
		sb.append("\t");
		sb.append(memoryReportItem.getDescriptor());
		sb.append("\t");
		sb.append(memoryReportItem.getTime());
		sb.append("\t");
		sb.append(memoryReportItem.getSelfByteCount());
		sb.append("\t");
		sb.append(memoryReportItem.getChildByteCount());
		long totalByteCount = memoryReportItem.getSelfByteCount() + memoryReportItem.getChildByteCount();
		sb.append("\t");
		sb.append(totalByteCount);
		return (sb.toString());

	}

}
