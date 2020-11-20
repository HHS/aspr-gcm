package gcm.simulation;

import gcm.output.OutputItemHandler;
import gcm.output.reports.ReportItemHandler;
import gcm.output.simstate.SimulationStatusItem;
import gcm.replication.Replication;
import gcm.scenario.Scenario;
import gcm.util.TimeElapser;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * Simulation is the top level structure for the General Compartment Model. A
 * simulation is formed from )a Scenario, 2) a Replication, and 3) Output Item
 * Handlers. The Scenario provides the identifiers and properties of components,
 * people, resources, batches stages and materials. The Replication supplies the
 * random seed for stochastics. The Components (global components, regions,
 * compartments and materials producers) execute modeler-derived business rules
 * and are derived from the scenario. Reports are provided via the
 * {@link ReportItemHandler} and act as passive listeners to various events
 * and translate those events into Report Items which are processed by the
 * handler.
 *
 * The simulation is executed by the execute() method and will throw a
 * {@link RuntimeException} if this method is invoked more than once. Upon
 * execution, the simulation loads all scenario data and initializes components.
 * People and resources can be loaded from the Scenario, but typically are
 * created dynamically by components. Global components are initialized first,
 * followed by regions, compartments and materials producers. Within each type
 * of component initialization order follows the order that the corresponding
 * component identifiers were added to the scenario.
 *
 * Components are the active part of the simulation and are contributed by the
 * Modeler. People, resources and materials are passive and are acted upon by
 * the components via their interaction with the Environment. The Environment is
 * the source of all data state that is not private to the components. The
 * Environment takes no actions on its own and simply reacts to the requests
 * from the components.
 *
 * Time is measured in days and starts at value 0. Time flows as a function of
 * future planning on the part of components. Components make changes to the
 * state of the simulation such as moving people, altering resource levels or
 * changing property values. When those changes need to take place in the
 * future, the component schedules a plan with the Environment. When the plan
 * time is reached, the Environment sends the plan back to the Component for
 * execution. The Component then executes the plan subject to its own discretion
 * at that time. Note that this is not a future event system where events are
 * scheduled and execute without any further action on the part of the
 * component. There are no events to terminate should circumstances change.
 * Instead, plans can be cancelled by the component as needed and if a plan
 * comes due, the component is free to act in a way that is appropriate to the
 * current circumstances.
 *
 * Components can actively perceive information about people, resources and
 * materials from the Environment. They may also register to passively perceive
 * changes to people, resources and materials and will be stimulated by the
 * Environment when the registered changes have occurred.
 *
 * The Environment has at most one active component at a time. While a component
 * is active it may change the state of the Environment. The Environment manages
 * the passive observations that result from these changes and passes those
 * observations to the registered observing components only after the currently
 * active component has finished its actions. Each observer will become the
 * active component in turn. This prevents state changes in the Environment that
 * are invisible to the current component while it is executing and simplifies
 * reasoning about invariant conditions.
 *
 * Time halts when there are no plans and no observations left to execute.
 * Reports are closed and the simulation terminates.
 *
 * Reports register for passive observation of the Environment's state and have
 * access to same information as components.
 *
 * Reports map these observations, often in an aggregate manner, to ReportItems
 * which are thread safe immutable objects. The report items are in turn sent to
 * a {@link ReportItemHandler} which ushers the report items into files
 * chosen by the Modeler.
 *
 * @author Shawn Hatch
 *
 */

@NotThreadSafe
@Source
public final class Simulation {

	/*
	 * Builder for the Context. The Context executes the bulk of the simulation
	 * and exists so that no modeler provided class has direct access to the
	 * internal portions of the simulation.
	 */
	private Context.Builder builder = Context.builder();

	/**
	 * Executes the simulation from the collected data.
	 * 
	 * @throws RuntimeException
	 *             <li>if no scenario was set
	 *             <li>if no replication was set
	 *             
	 * 
	 */
	public void execute() {

		Context context = builder.build();

		/*
		 * We manage the production of SimulationStatusItems outside of the
		 * context and interpret the completion of the Context's execute method
		 * as a successful simulation execution.
		 */

		/*
		 * We find at least one output item handler that handles simulation
		 * status items
		 */
		boolean produceSimulationStatusItems = false;
		for (OutputItemHandler outputItemHandler : context.getOutputItemHandlers()) {
			produceSimulationStatusItems |= outputItemHandler.getHandledClasses().contains(SimulationStatusItem.class);
		}

		/*
		 * If we are producing simulation status items then we must invoke the
		 * context.execute() method in a try finally block.
		 */
		if (produceSimulationStatusItems) {
			SimulationStatusItem.Builder builder = SimulationStatusItem.builder();
			builder.setScenarioId(context.getScenario().getScenarioId());
			builder.setReplicationId(context.getReplication().getId());
			TimeElapser timeElapser = new TimeElapser();
			try {
				context.execute();
				builder.setSuccessful(true);
			} finally {
				builder.setDurartion(timeElapser.getElapsedMilliSeconds());
				SimulationStatusItem simulationStatusItem = builder.build();
				context.getOutputItemManager().releaseOutputItem(simulationStatusItem);
			}
		} else {
			context.execute();
		}
	}

	/**
	 * Sets the {@link Replication} for the simulation instance
	 * 
	 * @throws RuntimeException
	 *             if the replication is null
	 */
	public void setReplication(final Replication replication) {
		builder.setReplication(replication);
	}

	/**
	 * Add an {@link OutputItemHandler} for the simulation instance
	 * 
	 * @throws RuntimeException
	 *             if the outputItemHandler is null
	 */
	public void addOutputItemHandler(final OutputItemHandler outputItemHandler) {
		builder.addOutputItemHandler(outputItemHandler);
	}

	/**
	 * Sets the {@link Scenario} for the simulation instance
	 * 
	 * @throws RuntimeException
	 *             if the scenario is null
	 */
	public void setScenario(final Scenario scenario) {
		builder.setScenario(scenario);
	}

}
