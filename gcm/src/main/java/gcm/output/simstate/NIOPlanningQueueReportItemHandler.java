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
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An {@link OutputItemHandler} implementor that handles
 * {@link PlanningQueueReportItem} objects. Each PlanningQueueReportItem is
 * written into a headered, tab-delimited, text file passed to this
 * {@link NIOPlanningQueueReportItemHandler}. It is constructed with a planning
 * queue report threshold that indicates to each simulation instance how many
 * additions/removals to the planning queue are needed to generate
 * PlanningQueueReportItems.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class NIOPlanningQueueReportItemHandler extends NIOHeaderedOutputItemHandler {

	@GuardedBy("this")
	private final long planningQueueReportThreshold;

	/**
	 * Returns the number of additions/removals to trigger the bulk generation
	 * of PlanningQueueReportItems by each simulation.
	 */
	public synchronized long getPlanningQueueReportThreshold() {
		return planningQueueReportThreshold;
	}

	public NIOPlanningQueueReportItemHandler(Path path, long planningQueueReportThreshold) {
		super(path);
		this.planningQueueReportThreshold = planningQueueReportThreshold;
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(PlanningQueueReportItem.class);
		return result;
	}

	@Override
	protected String getHeader(OutputItem outputItem) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Scenario");
		sb.append("\t");
		sb.append("Replication");
		sb.append("\t");
		sb.append("Component");
		sb.append("\t");
		sb.append("Class");
		sb.append("\t");
		sb.append("Keyed");
		sb.append("\t");
		sb.append("Start Time");
		sb.append("\t");
		sb.append("End Time");
		sb.append("\t");
		sb.append("Additions");
		sb.append("\t");
		sb.append("Removals");
		sb.append("\t");
		sb.append("Cancellations");
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
		PlanningQueueReportItem planningQueueReportItem = (PlanningQueueReportItem) outputItem;

		final StringBuilder sb = new StringBuilder();
		sb.append(planningQueueReportItem.getScenarioId());
		sb.append("\t");
		sb.append(planningQueueReportItem.getReplicationId());
		sb.append("\t");
		sb.append(planningQueueReportItem.getComponentId());
		sb.append("\t");
		sb.append(planningQueueReportItem.getPlanningClass().getName());
		sb.append("\t");
		sb.append(planningQueueReportItem.isKeyed());
		sb.append("\t");
		sb.append(planningQueueReportItem.getStartTime());
		sb.append("\t");
		sb.append(planningQueueReportItem.getEndTime());
		sb.append("\t");
		sb.append(planningQueueReportItem.getAdditionCount());
		sb.append("\t");
		sb.append(planningQueueReportItem.getRemovalCount());
		sb.append("\t");
		sb.append(planningQueueReportItem.getCancellationCount());

		
		Stat stat = planningQueueReportItem.getStat();

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
