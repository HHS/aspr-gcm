package gcm.simulation;

import gcm.output.simstate.NIOPlanningQueueReportItemHandler;
import gcm.output.simstate.PlanningQueueReportItem;
import gcm.scenario.ComponentId;
import gcm.util.annotations.Source;

/**
 * 
 * Manager for the production of {@link PlanningQueueReportItem}. The planning
 * queue report is active when the
 * {@link NIOPlanningQueueReportItemHandler#getPlanningQueueReportThreshold()}
 * method returns a positive value.
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface PlanningQueueReportItemManager extends Element {

	/**
	 * Returns true if and only if the planning queue report is active, i.e. the
	 * simulation is producing {@link PlanningQueueReportItem} report items
	 */
	public boolean isActive();

	/**
	 * Records that a plan was added to the planning queue
	 */
	public void reportPlanningQueueAddition(ComponentId componentId, Plan plan, Object key);

	/**
	 * 
	 * Records that a plan was removed from the planning queue die to execution
	 * 
	 * @throws RuntimeException
	 *             <li>if the calculated queue depth from previous additions and
	 *             removals is non-positive.
	 */
	public void reportPlanningQueueRemoval(ComponentId componentId, Plan plan, Object key);

	/**
	 * 
	 * Records that a plan was removed from the planning queue due to cancellation
	 * 
	 * @throws RuntimeException
	 *             <li>if the calculated queue depth from previous additions and
	 *             removals is non-positive.
	 */
	public void reportPlanningQueueCancellation(ComponentId componentId, Plan plan, Object key);

	/**
	 * Should be called at the end of the simulation to signal that any
	 * remaining {@link PlanningQueueReportItem} items should be sent to the
	 * {@link OutputItemManager}.
	 */
	public void close();

}
