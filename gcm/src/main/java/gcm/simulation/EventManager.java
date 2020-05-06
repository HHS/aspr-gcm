package gcm.simulation;

import java.util.List;
import java.util.Optional;

import gcm.scenario.GlobalComponentId;
import gcm.util.annotations.Source;

/**
 * The manager for controlling the flow of time for the simulation. Time is
 * measured as a double precision value and is initialized to zero. Time moves
 * forward as plans are executed. Thus this manager is also the planning manager
 * for the simulation.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface EventManager extends Element {

	/**
	 * Starts the flow of time. Components have their init() methods invoked.
	 * Component order is based on the order the Component Manager places the
	 * components. After initialization, the remainder of the action is in
	 * processing plans from the planning queue, and collecting observations
	 * from the Observation Manager and conducting those observations to the
	 * components. The simulation may only have one component in focus and the
	 * setting of the active component on the Component Manager is the
	 * responsibility of the Event Manager.
	 */
	public void execute();

	/**
	 * Halts the flow of time. This is a graceful halt and some plans and
	 * observations may still be processed after the halt() is invoked. Once
	 * whatever few events are processed, the execute() method will complete and
	 * the simulation will continue with its normal shutdown of reports.
	 */
	public void halt();

	/**
	 * Returns the current time. Time is set by the removal of a non-canceled
	 * plan.
	 */
	public double getTime();

	/**
	 * Returns the plan associated with the given key. Returns null if no plan
	 * is found.
	 */
	public <T extends Plan> T getPlan(final Object key);

	/**
	 * Returns the plan time for the plan associated with the key. Returns -1 if
	 * no plan is found.
	 */
	public double getPlanTime(final Object key);

	/**
	 * Removes the plan associated with the given key and returns it. The
	 * Optional will reflect if the plan is not found.
	 */
	public <T> Optional<T> removePlan(final Object key);

	/**
	 * Adds the plan for the give planTime and key. Null key values are
	 * acceptable, but the corresponding plans are not removable. Plan times are
	 * intended to be used for future events and should generally be greater
	 * than the current time.
	 */
	public void addPlan(final Plan plan, final double planTime, final Object key);

	/**
	 * Returns true if and only if this EventManager has not had halt() invoked
	 * and has at least one plan in the planning queue. The intended use is for
	 * components that are of type ComponentType.INTERNAL that need to schedule
	 * a periodic plan for only as long as other components are also active.
	 */
	public boolean isActive();
	
	/**
	 * Returns the set of plan keys for the current Component as a list. Items
	 * on the list are unique.
	 */
	public List<Object> getPlanKeys();
	
	public void initGlobalComponent(GlobalComponentId globalComponentId);
}
