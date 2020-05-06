package gcm.simulation;

import java.util.Set;

import gcm.components.Component;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalComponentId;
import gcm.util.annotations.Source;

/**
 * The ComponentManager manages component construction and the focus of the
 * simulation on a single component. The focus is used to drive event validation
 * and observation generation.
 * 
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface ComponentManager extends Element {
	/**
	 * Instructs this component manager to wrap its components in proxies for
	 * profiling.
	 */
	public void wrapComponentsInProxies();

	/**
	 * Sets to focus back to GCM -- i.e. no Component has focus
	 */
	public void clearFocus();

	/**
	 * Sets the focus on the given component
	 * 
	 * @throws RuntimeException
	 *             if the component id is unknown
	 */
	public void setFocus(ComponentId componentId);

	/**
	 * Returns the Component that is currently in focus. Returns null if no
	 * component has focus.
	 */
	public Component getFocalComponent();

	/**
	 * Returns the id of the Component that is currently in focus. Returns the
	 * GCM id if no component has focus.
	 */
	public <T extends ComponentId> T getFocalComponentId();

	/**
	 * Returns the id of the Component that is currently in focus. Returns
	 * ComponentType.SIM if no component has focus.
	 */
	public ComponentType getFocalComponentType();

	/**
	 * Returns the ids all Components
	 */
	public Set<ComponentId> getComponentIds();
	
	/**
	 * Adds the global component
	 */
	public void addGlobalComponent(GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass);

}
