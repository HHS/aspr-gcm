package gcm.simulation;

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
public interface IndexedPropertyManager {

	/**
	 * Returns the property value stored for the given id. Does not return null.
	 * Note that this does not imply that the id exists in the simulation.
	 * 
	 */
	public <T> T getPropertyValue(int id);

	/**
	 * Returns the assignment time when the id's property was last set. Note that
	 * this does not imply that the id exists in the simulation.
	 * 
	 * @throws RuntimeException if time tracking is not turned on for this property
	 *                          via the policies established in the scenario.
	 * 
	 */
	public double getPropertyTime(int id);

	/**
	 * Sets the property value stored for the given person. Note that this does not
	 * imply that the person exists in the simulation. The environment must guard
	 * against access to removed people.
	 * 
	 * @throws RuntimeException
	 *                          <li>if the person or property value are null
	 * @param personId
	 * @param propertyValue
	 */
	public void setPropertyValue(int id, Object propertyValue);

}
