package gcm.simulation;

import gcm.components.AbstractComponent;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A component of type ComponentType.INTERNAL. The MemoryReportComponent exists
 * to trigger the {@link MemoryReportManager} at regular intervals (in
 * simulation time) until the simulation ends. This is done so that the
 * {@link EventManager} does not have to repetitively check for memory report
 * construction.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final  class MemoryReportComponent extends AbstractComponent {

	/*
	 * A empty Plan implementor
	 */
	private static class MemoryReportPlan implements Plan {

	}

	/*
	 * A plan that we will reuse
	 */
	private MemoryReportPlan memoryReportPlan = new MemoryReportPlan();

	/*
	 * The time between memory reports
	 */
	private final double memoryReportInterval;

	/*
	 * The MemoryReportManager to stimulate each memoryReportInterval
	 */
	private MemoryReportManager memoryReportManager;

	private final EventManager eventManager;

	public MemoryReportComponent(Context context) {
		/*
		 * Initialize state
		 */
		eventManager = context.getEventManager();
		memoryReportInterval = context.getMemoryReportInterval();
		memoryReportManager = context.getMemoryReportManager();
	}

	@Override
	public void init(Environment environment) {
		/*
		 * Kick off the first plan
		 */
		eventManager.addPlan(memoryReportPlan, 0, null);
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		/*
		 * Generate the report
		 */
		memoryReportManager.generateMemoryReport();
		/*
		 * Ask the event manager if the simulation is out of future events. We
		 * only add the plan if there are other future events remaining.
		 */
		if (eventManager.isActive()) {
			eventManager.addPlan(memoryReportPlan, eventManager.getTime() + memoryReportInterval, null);
		}
	}

}
