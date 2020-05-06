package gcm.simulation;

import java.util.List;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.util.annotations.Source;

/**
 * Manager for all region and compartment membership for the simulation. This
 * includes arrival time tracking, population counts and mapping of compartment
 * and regions to their associated people.
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface PersonLocationManger extends Element {

	/**
	 * Returns the number of people current in the given compartment.
	 * 
	 * @param compartmentId
	 *            should not be null
	 * @return
	 */
	public int getCompartmentPopulationCount(final CompartmentId compartmentId);

	/**
	 * Returns the time when the current population of the given compartment was
	 * established.
	 * 
	 * @param compartmentId
	 *            should not be null
	 * @return
	 */

	public double getCompartmentPopulationTime(final CompartmentId compartmentId);

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * compartment. List elements are unique.
	 * 
	 * @param compartmentId
	 *            should not be null
	 * @return
	 */
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId);

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * region. List elements are unique.
	 * 
	 * @param regionId
	 *            should not be null
	 * @return
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId);

	/**
	 * Returns the compartment associated with the given person id.
	 * 
	 * @param personId
	 *            should not be null
	 * @return
	 */
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId);

	/**
	 * Returns the time when then person arrived at their current compartment.
	 * 
	 * @param personId
	 *            should not be null
	 * @return
	 */
	public double getPersonCompartmentArrivalTime(final PersonId personId);

	/**
	 * Returns the region associated with the given person id.
	 * 
	 * @param personId
	 *            should not be null
	 * @return
	 */
	public <T extends RegionId> T getPersonRegion(final PersonId personId);

	/**
	 * Returns the time when then person arrived at their current region.
	 * 
	 * @param personId
	 *            should not be null
	 * @return
	 */
	public double getPersonRegionArrivalTime(final PersonId personId);

	/**
	 * Returns the number of active people in the simulation.
	 * 
	 * @return
	 */
	public int getPopulationCount();

	/**
	 * Returns the time when the current number of active people in the
	 * simulation was established.
	 * 
	 * @return
	 */
	public double getPopulationTime();

	/**
	 * Returns the number of people current in the given region.
	 * 
	 * @param regionId
	 *            should not be null
	 * @return
	 */
	public int getRegionPopulationCount(final RegionId regionId);

	/**
	 * Returns the time when the current population of the given region was
	 * established.
	 * 
	 * @param regionId
	 *            should not be null
	 */
	public double getRegionPopulationTime(final RegionId regionId);

	/**
	 * Removes the person from all compartment and region tracking.
	 * 
	 * @param personId
	 */
	public void removePerson(final PersonId personId);

	/**
	 * Adds the person to compartment and region tracking.
	 * 
	 * @param personId
	 */
	public PersonId addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId);

	/**
	 * Updates the compartment associated with the given person.
	 * 
	 * @param personId
	 * @param compartmentId
	 */
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId);

	/**
	 * Updates the region associated with the given person.
	 * 
	 * @param personId
	 * @param regionId
	 */
	public void setPersonRegion(final PersonId personId, final RegionId regionId);
}
