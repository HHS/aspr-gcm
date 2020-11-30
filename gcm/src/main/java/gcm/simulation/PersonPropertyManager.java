package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.util.annotations.Source;

/**
 * Common interface to all person property managers. A person property manager
 * manages all the property values for people for a particular person property
 * identifier.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface PersonPropertyManager {

	/**
	 * Returns the property value stored for the given person. Does not return
	 * null. Note that this does not imply that the person exists in the
	 * simulation. The environment must guard against access to removed people.
	 * 
	 * @throws RuntimeException
	 *             if the person is null
	 * 
	 * @param personId
	 */
	public <T> T getPropertyValue(PersonId personId);

	/**
	 * Returns the assignment time when the person's property was last set. Note
	 * that this does not imply that the person exists in the simulation. The
	 * environment must guard against access to removed people.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person is null
	 * 
	 *             <li>{@link SimulationErrorType#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED} if
	 *             time tracking is not turned on for this property via the
	 *             policies established in the scenario.
	 * 
	 * 
	 * @param personId
	 */
	public double getPropertyTime(PersonId personId);

	/**
	 * Sets the property value stored for the given person. Note that this does
	 * not imply that the person exists in the simulation. The environment must
	 * guard against access to removed people.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person or property value are null
	 * @param personId
	 * @param propertyValue
	 */
	public void setPropertyValue(PersonId personId, Object personPropertyValue);

	/**
	 * Returns the List of people who have a particular property value. The
	 * returned list will only include people who currently exist in the
	 * simulation given that handlePersonRemoval() has been executed for each
	 * person removal from the simulation. The environment must guard against
	 * access to removed people.
	 * 
	 * @param personPropertyValue
	 */
	//public List<PersonId> getPeopleWithPropertyValue(final Object personPropertyValue);

	/**
	 * Returns the number of people who have a particular property value.
	 * 
	 * @param personPropertyValue
	 */
	//public int getPersonCountForPropertyValue(final Object personPropertyValue);

	/**
	 * Integrate the person into this PropertyManager with the default value
	 * associated with property's definition. Note that this does not imply that
	 * the person exists in the simulation. The environment must guard against
	 * access to removed people.
	 * 
	 * @param personId
	 */
	public void handlePersonAddition(final PersonId personId);

	/**
	 * Removes the person such that the method getPeopleWithPropertyValue()
	 * functions properly. However, this does not guarantee that the person does
	 * not have a property value since there is no way to effectively remove
	 * people from most of the implementors of PropertyManager due to default
	 * values, array structures and use of primitives. Instead, the environment
	 * must guard against access to removed people.
	 * 
	 * @param personId
	 */
	public void handlePersonRemoval(final PersonId personId);
}
