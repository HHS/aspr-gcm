package gcm.simulation;

import java.util.Arrays;

import gcm.scenario.ComponentId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * As the active component changes the state of the simulation, those changes
 * will often result in an observation on the part of other components. These
 * non-focal components cannot immediately act upon the simulation's state since
 * that would violate the reasonable assumption of state invariance on the part
 * of the current component. Thus we must delay these observations and so record
 * them as ObservationRecords. These are kept on a queue in the Observation
 * Manager and that queue is processed immediately after each component
 * activation. Time does not flow and the appropriate observation methods are
 * then invoked on the observing components who in turn become the focal
 * component.
 *
 * The observation record is composed of 1)the ComponentId which is simply the
 * focal identifier for the observing component and 2) the array of arguments
 * that make up the observation. The first key value (an ObservationType)
 * represents the method that will need to be invoked on the component with the
 * remaining keys containing the ordered set of arguments that are needed for
 * that observation method.
 */
@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public final class ObservationRecord {
	private final ComponentId componentId;
	private final Object[] arguments;

	@SuppressWarnings("unchecked")
	public <T> T getArgument(int index) {
		return (T) arguments[index];
	}

	public ComponentId getComponentId() {
		return componentId;
	}

	public ObservationRecord(final ComponentId componentId, final Object[] arguments) {
		super();
		this.componentId = componentId;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(componentId.toString());
		sb.append(Arrays.toString(arguments));
		return sb.toString();
	}
}