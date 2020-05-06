package gcm.simulation;

import java.util.List;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;

/**
 * Note: IndexedPopulation is not exposed to the Components by GCM. The relevant
 * methods below are instead accessed by Components via the Environment which
 * acts to validate inputs and match keys to IndexedPopulations.
 *
 * An indexed population is a sub-population of the people contained in the
 * simulation who meet various criteria. The criteria are 1) person property
 * values, 2) compartment membership 3) region membership 4)resource values and
 * 5)group membership.
 *
 * GCM actively maintains indexed populations but does not alert components of
 * changes to the people contained in the population. Rather, components should
 * register to observe changes to the properties and region/compartment
 * assignments of people through the main observation capability of GCM.
 *
 * Indexed populations are regarded as being owned by the component that created
 * the index. Only the owning component can remove the index.
 * 
 * @author Shawn Hatch
 *
 */
public interface IndexedPopulation {
	
	/**
	 * Forces the index to evaluate a person's membership in this index.
	 */
	public void evaluate(final PersonId personId);
	
	/**
	 * Returns the focal key for the owning component of this index.
	 * 
	 * @return
	 */
	public ComponentId getOwningComponentId();
	/**
	 * Returns the people identifiers of this index
	 */
	public List<PersonId> getPeople();

	/**
	 * Returns a randomly chosen person identifier from the index, excluding the
	 * person identifier given. When the excludedPersonId is null it indicates
	 * that no person is being excluded. Returns null if the index is either
	 * empty or only contains the excluded person.
	 */
	public PersonId getRandomPersonId(final PersonId excludedPersonId);
	/**
	 * Returns a randomly chosen person identifier from the index, excluding the
	 * person identifier given. When the excludedPersonId is null it indicates
	 * that no person is being excluded. Returns null if the index is either
	 * empty or only contains the excluded person.
	 */
	public PersonId getRandomPersonFromGenerator(final PersonId excludedPersonId, RandomNumberGeneratorId randomNumberGeneratorId);
	
	/**
	 * Returns true if and only if the person is contained in the population
	 * index
	 */
	public boolean personInPopulationIndex(final PersonId personId);
	
	/**
	 * Initializes this population index.
	 */
	public void init();
	/**
	 * Removes the person from the index if they are present and does so without
	 * regard to the criteria of the index. Generally, this is used when a
	 * person is being removed from the simulation.
	 */
	public boolean remove(final PersonId personId);
	/**
	 * Returns the number of people in the index
	 */
	public int size();

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString();

	/**
	 * Returns the filter.
	 */
	public FilterInfo getFilterInfo();

}