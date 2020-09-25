package gcm.simulation.partition;

import java.util.List;

import gcm.scenario.CompartmentId;
import gcm.scenario.ComponentId;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.Element;
import gcm.simulation.ModelException;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.StochasticPersonSelection;
import gcm.util.annotations.Source;

/**
 * A manager for IndexedPopulations. The Environment uses a single instance of
 * this manager to manage its IndexedPopulations. Components are not exposed to
 * IndexedPopulations or the IndexedPopulationManager but instead work with the
 * Environment as a proxy.
 *
 * @author Shawn Hatch
 *
 */

@Source
public interface PartitionManager extends Element {

	/**
	 * Adds a population index for the given key. The key must not duplicate an
	 * existing key.
	 *
	 * @throws ModelException
	 *             <li>{@link SimulationErrorType#DUPLICATE_INDEXED_POPULATION}
	 *             if the key is already associated with a population index
	 */

	public void addPartition(final ComponentId componentId, final Partition partition, final Object key);

	/**
	 * Returns the list of person identifiers in the index for the given key.
	 *
	 * @throws ModelException
	 *             <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *             if the keys are not associated with a population index
	 */
	public List<PersonId> getPeople(final Object key);
	
	/**
	 * Returns the list of person identifiers in the index for the given key.
	 *
	 * @throws ModelException
	 *             <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *             if the keys are not associated with a population index
	 */
	public List<PersonId> getPeople(final Object key, LabelSet labelSet);

	/**
	 * Returns the number of people in the index for the given key.
	 *
	 * @throws ModelException
	 *             <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *             if the keys are not associated with a population index
	 */
	public int getPersonCount(final Object key);
	
	public int getPersonCount(final Object key,LabelSet labelSet);
	
	/**
	 * Returns a randomly selected person from the given index using the given PartitionSampler.
	 *
	 */
	public StochasticPersonSelection samplePartition(Object key, PartitionSampler partitionSampler);


	
	/**
	 * Returns true if and only if the person is contained in the population
	 * corresponding to the key. The key must correspond to an existing indexed
	 * population.
	 */
	
	public boolean contains(final PersonId personId, Object key);
	
	public boolean contains(PersonId personId ,LabelSet labelSet, Object key);


	/**
	 * Adds a person into all population indices that apply to that person. This
	 * must be invoked when a person is first added to the simulation. Repeated
	 * invocations are harmless, but wasteful. This manager will track and
	 * update the person's membership in the managed indices for each invocation
	 * of the handleXXX() methods of the manager. Tracking continues until the
	 * person is removed by invocation of the removePerson() method.
	 *
	 *
	 */
	public void handlePersonAddition(final PersonId personId);

	/**
	 * Updates all indexed population relative to the compartment change for the
	 * given person.
	 */
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId, final CompartmentId newCompartmentId);

	/**
	 * Updates a person's region association in all relevant indices and
	 * tracking structures.
	 */
	public void handlePersonRegionChange(final PersonId personId, final RegionId oldRegionId, final RegionId newRegionId);

	/**
	 * Updates a person's group association in all relevant indices and tracking
	 * structures.
	 */
	public void handlePersonGroupAddition(GroupId groupId, PersonId personId);

	/**
	 * Updates a person's group association in all relevant indices and tracking
	 * structures.
	 */
	public void handlePersonGroupRemoval(GroupId groupId, PersonId personId);

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
	 * population index associated with the given key. The key must correspond
	 * to an existing indexed population.
	 * 
	 */
	public boolean partitionExists(final Object key);

	/**
	 * Removes the population index for the given keys. The key must correspond
	 * to an existing indexed population.
	 *
	 * 
	 */
	public void removePartition(final Object key);

	/**
	 * Updates a person's property value association in all relevant indices and
	 * tracking structures. The key must correspond to an existing indexed
	 * population.
	 */
	public void handlePersonPropertyValueChange(final PersonId personId, final PersonPropertyId personPropertyId, final Object oldValue, final Object newValue);

	/**
	 * Returns the ComponentId of the component that added the indexed
	 * population. The key must correspond to an existing indexed population.
	 */
	public ComponentId getOwningComponent(final Object key);
	
	/**
	 * Returns true if and only if the given population labelSet is compatible with
	 * the PopulationPartitionDefinition associated with the key
	 */
	public boolean validateLabelSet(Object key, LabelSet labelSet);

}