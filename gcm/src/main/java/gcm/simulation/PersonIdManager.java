package gcm.simulation;

import java.util.List;

import gcm.scenario.PersonId;
import gcm.util.annotations.Source;

/**
 * Manager for the set of {@link PersonId} values for the simulation. It serves
 * to ensure that PersonId values used as keys are unique and for helping other
 * managers with optimizing their own data structures.
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface PersonIdManager extends Element {

	/**
	 * Returns true if and only if the person index exists. This is not to say
	 * the person exists, only that the index has been allocated.
	 */
	public boolean personIndexExists(int personId);

	/**
	 * Returns the first unused personId value
	 */
	public int getPersonIdLimit();

	/**
	 * Returns the boxed int value of a particular person, if that person
	 * exists. Throws a ModelException otherwise.
	 *
	 * @throws RuntimeException
	 *
	 *             <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if the
	 *             person id does not correspond to a known person
	 */
	public PersonId getBoxedPersonId(int personId);

	/**
	 * Returns a new PersonId
	 */
	public PersonId addPersonId();

	/**
	 * Returns the original PersonId instance that will equal the given
	 * instance. This cuts down on the size of key collections when a modeler
	 * chooses to create new PersonId instance rather than using the
	 * {@link EnvironmentImpl#addPerson(gcm.scenario.RegionId, gcm.scenario.CompartmentId)}
	 * method.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null
	 *             <li>if the person does not exist
	 * 
	 */
	public PersonId getCleanedPersonId(final PersonId personId);

	/**
	 * Returns true if and only if the person exits
	 */
	public boolean personExists(final PersonId personId);

	/**
	 * Returns a list of all people in the simulation
	 */
	public List<PersonId> getPeople();

	/**
	 * Removes the person from the simulation
	 * 
	 * @throws RuntimeException
	 *             <li>if the person does not exist
	 */
	public void removePerson(PersonId personId);

}
