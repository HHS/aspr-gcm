package gcm.output.simstate;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.NIOHeaderedOutputItemHandler;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.stats.Stat;
import net.jcip.annotations.ThreadSafe;

/**
 * An {@link OutputItemHandler} implementor that handles {@link ProfileItem}
 * objects. Each ProfileItem is written into a headered, tab-delimited, text
 * file passed to this {@link NIOProfileItemHandler}.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class NIOProfileItemHandler extends NIOHeaderedOutputItemHandler {

	public NIOProfileItemHandler(Path path) {
		super(path);
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(ProfileItem.class);
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
		sb.append("Depth");
		sb.append("\t");
		sb.append("Component");
		sb.append("\t");
		sb.append("Class");
		sb.append("\t");
		sb.append("Method");
		sb.append("\t");
		sb.append("Total Time");
		sb.append("\t");
		sb.append("Count");
		sb.append("\t");
		sb.append("Min");
		sb.append("\t");
		sb.append("Max");
		sb.append("\t");
		sb.append("Mean");
		sb.append("\t");
		sb.append("Standard Deviation");
		return sb.toString();
	}

	@Override
	protected String getOutputLine(OutputItem outputItem) {
		ProfileItem profileItem = (ProfileItem) outputItem;

		final StringBuilder sb = new StringBuilder();
		sb.append(profileItem.getScenarioId());
		sb.append("\t");
		sb.append(profileItem.getReplicationId());
		sb.append("\t");
		sb.append(profileItem.getId());
		sb.append("\t");
		sb.append(profileItem.getParentId());
		sb.append("\t");
		sb.append(profileItem.getDepth());
		sb.append("\t");
		sb.append(profileItem.getComponentId());
		sb.append("\t");
		sb.append(profileItem.getClassName());
		sb.append("\t");
		sb.append(profileItem.getMethodName());

		Stat stat = profileItem.getStat();
		double totalTime = stat.getMean().get() * stat.size();

		sb.append("\t");
		sb.append(totalTime);
		sb.append("\t");
		sb.append(stat.size());
		sb.append("\t");
		sb.append(stat.getMin().get());
		sb.append("\t");
		sb.append(stat.getMax().get());
		sb.append("\t");
		sb.append(stat.getMean().get());
		sb.append("\t");
		sb.append(stat.getStandardDeviation().get());
		return sb.toString();

	}

}
