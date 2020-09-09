package gcm.simulation.group;

import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;

/**
 * A functional interface for selecting people from a group based on assigning a
 * weighting value to a person.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface GroupWeightingFunction {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * Repeated invocations with the same arguments should return the same value
	 * while no mutations to simulation state have taken place. The person will
	 * be a member of the group.
	 */
	public double getWeight(ObservableEnvironment observableEnvironment, PersonId personId, GroupId groupId);
}
