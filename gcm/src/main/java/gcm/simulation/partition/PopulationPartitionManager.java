package gcm.simulation.partition;

import java.util.List;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.ResourceId;
import gcm.simulation.Element;
import gcm.simulation.ModelException;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.StochasticPersonSelection;
import gcm.util.annotations.Source;

/**
 * A manager for Population Partitions. The Environment uses a single instance
 * of this manager to manage its PopulationPartitions. Components are not
 * exposed to PopulationPartitions or the PopulationPartitionManager but instead
 * work with the Environment as a proxy.
 *
 * @author Shawn Hatch
 *
 */

@Source
public interface PopulationPartitionManager extends Element {

	/**
	 * Adds a population partition for the given key. The key must not duplicate an
	 * existing key.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#DUPLICATE_INDEXED_POPULATION}
	 *                        if the key is already associated with a population
	 *                        index
	 */

	public void addPopulationPartition(final ComponentId componentId, final Partition partition, final Object key);

	/**
	 * Returns the list of person identifiers in the partition for the given key.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the keys are not associated with a population index
	 */
	public List<PersonId> getPartitionPeople(final Object key, LabelSet labelSet);

	/**
	 * Returns the number of people in the index for the given key.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the keys are not associated with a population index
	 */
	public int getPartitionSize(final Object key, LabelSet labelSet);

	/**
	 * Returns a randomly selected person from the given index excluding the given
	 * person identifier. A null value for the excluded person is allowed. Returns
	 * null if there are no people in the index or the only person in the index is
	 * the excluded person.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the keys are not associated with a population index
	 */
	public PersonId getRandomPartitionedPerson(final PersonId excludedPersonId, final Object key, LabelSet labelSet);

	/**
	 * Returns a randomly selected person from the given index excluding the given
	 * person identifier. A null value for the excluded person is allowed. Returns
	 * null if there are no people in the index or the only person in the index is
	 * the excluded person. Random selection is from the RandomGenerator instance
	 * associated with the RandomNumberGeneratorId.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the keys are not associated with a population index
	 */
	public PersonId getRandomPartionedPersonFromGenerator(final PersonId excludedPersonId, final Object key,
			LabelSet labelSet, RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns true if and only if the person is contained in the population
	 * corresponding to the key. The key must correspond to an existing indexed
	 * population.
	 */
	public boolean personInPartition(final PersonId personId, final Object key, LabelSet labelSet);

	/**
	 * Adds a person into all population indices that apply to that person. This
	 * must be invoked when a person is first added to the simulation. Repeated
	 * invocations are harmless, but wasteful. This manager will track and update
	 * the person's membership in the managed indices for each invocation of the
	 * handleXXX() methods of the manager. Tracking continues until the person is
	 * removed by invocation of the removePerson() method.
	 *
	 *
	 */
	public void handlePersonAddition(final PersonId personId);

	/**
	 * Updates all indexed population relative to the compartment change for the
	 * given person.
	 */
	public void handlePersonCompartmentChange(final PersonId personId);

	/**
	 * Updates a person's region association in all relevant indices and tracking
	 * structures.
	 */
	public void handlePersonRegionChange(final PersonId personId);

	/**
	 * Updates a person's group association in all relevant indices and tracking
	 * structures.
	 */
	public void handleGroupMembershipChange(PersonId personId);

	/**
	 * Removes the person from all indices and related search mechanisms. This
	 * should only be called when a person is removed from the simulation.
	 */
	public void handlePersonRemoval(final PersonId personId);

	/**
	 * Updates a person's resource level association in all relevant indices and
	 * tracking structures.
	 */
	public void handlePersonResourceLevelChange(final PersonId personId, final ResourceId resourceId);

	/**
	 * Returns true if and only if this IndexedPopulationManager contains a
	 * population index associated with the given key. The key must correspond to an
	 * existing indexed population.
	 * 
	 */
	public boolean populationPartitionExists(final Object key);

	/**
	 * Removes the population index for the given keys. The key must correspond to
	 * an existing indexed population.
	 *
	 * 
	 */
	public void removePartition(final Object key);

	/**
	 * Updates a person's property value association in all relevant indices and
	 * tracking structures. The key must correspond to an existing indexed
	 * population.
	 */
	public void handlePersonPropertyValueChange(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Returns the ComponentId of the component that added the indexed population.
	 * The key must correspond to an existing indexed population.
	 */
	public ComponentId getOwningComponent(final Object key);

	/**
	 * Returns true if and only if the given population labelSet is compatible with
	 * the PopulationPartitionDefinition associated with the key
	 */
	public boolean validateLabelSet(Object key, LabelSet labelSet);

	/**
	 * Returns a contacted person. The LabelSetWeightingFunction must not be null.
	 */
	public StochasticPersonSelection getPersonIdFromWeight(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction);

	/**
	 * Returns a contacted person. The LabelSetWeightingFunction must not be null.
	 * Uses the random generator associated with the RandomNumberGeneratorId.
	 */
	public StochasticPersonSelection getPersonIdFromWeightAndGenerator(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId);

}