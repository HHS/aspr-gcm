package gcm.util.containers;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.PersonId;

/**
 * Interface for abstracting the details of how people ids are stored as either
 * a Set or a Boolean container.
 * 
 * @author Shawn Hatch
 */
public interface PeopleContainer {

	/*
	 * Returns a list of the people in the set with no duplicates
	 */
	public List<PersonId> getPeople();

	/*
	 * Returns true if and only if the person was successfully added
	 */
	public boolean add(PersonId personId);

	/*
	 * Returns true if and only if the person was successfully removed
	 */
	public boolean remove(PersonId personId);

	/*
	 * Returns the number of people in this container
	 */
	public int size();

	/*
	 * Adds the given collection of people to this container
	 */
	public void addAll(Collection<PersonId> collection);

	/*
	 * Returns true if and only if the person is contained.
	 */
	public boolean contains(PersonId personId);

	/*
	 * Returns a randomly selected person if this container has any people. Returns
	 * null otherwise.
	 */
	public PersonId getRandomPersonId(RandomGenerator randomGenerator);
}