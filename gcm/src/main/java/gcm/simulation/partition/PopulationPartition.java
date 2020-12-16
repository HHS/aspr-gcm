package gcm.simulation.partition;

import java.util.List;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.StochasticPersonSelection;

/**
 * 
 * A {@link PopulationPartition} is the interface for implementors of
 * partitions, maintaining the people associated with the partition's cells by
 * handling individual mutation events.
 * 
 * Transaction id values are passed via the various mutation handling methods
 * and should be unique per change per person. These allow the
 * {@link PopulationPartition} to handle some potential idempotencies.
 * 
 * @author Shawn Hatch
 *
 */
public interface PopulationPartition {

	/**
	 * Returns the {@link ComponentId} of the component that added the
	 * {@link Partition} to the simulation.
	 */
	public ComponentId getOwningComponentId();

	/**
	 * Handles the addition of a person to the simulation
	 * 
	 * Precondition : the person id is not null.
	 */
	public void handleAddPerson(long transactionId, PersonId personId);

	/**
	 * Handles the removal of a person from the simulation
	 * 
	 * Precondition: Person must exist
	 *
	 */
	public void handleRemovePerson(long transactionId, PersonId personId);

	/**
	 * Handles the change of a person's region.
	 */
	public void handleRegionChange(long transactionId, PersonId personId);

	/**
	 * Handles the change of a person's property value.
	 */
	public void handlePersonPropertyChange(long transactionId, PersonId personId, PersonPropertyId personPropertyId);

	/**
	 * Handles the change of a person's resource level.
	 */
	public void handlePersonResourceChange(long transactionId, PersonId personId, ResourceId resourceId);

	/**
	 * Handles the change of a person's compartment
	 */
	public void handleCompartmentChange(long transactionId, PersonId personId);

	/**
	 * Handles the change of a person's group membership
	 */
	public void handleGroupMembershipChange(long transactionId, PersonId personId);

	/**
	 * Returns true if and only if the given {@link LabelSet} is compatible with
	 * this {@link PopulationPartition}. To be consistent, the {@link LabelSet}
	 * must not contain label values for label dimensions not contained in this
	 * partition.
	 */
	public boolean validateLabelSetInfo(LabelSet labelSet);

	/**
	 * Returns the number of people contained in this
	 * {@link PopulationPartition}
	 */

	public int getPeopleCount();

	/**
	 * Returns the number of people contained in this
	 * {@link PopulationPartition} that are contained in cells that match the
	 * given {@link LabelSet}
	 */
	public int getPeopleCount(LabelSet labelSet);

	/**
	 * Returns true if and only if the person is contained in this
	 * {@link PopulationPartition}
	 */
	public boolean contains(PersonId personId);

	/**
	 * Returns true if and only if the person is contained in this
	 * {@link PopulationPartition} under the cells consistent with the given
	 * {@link LabelSet}
	 */
	public boolean contains(PersonId personId, LabelSet labelSet);

	/**
	 * Returns the people contained in this {@link PopulationPartition} that are
	 * contained in cells that match the given {@link LabelSet}
	 */
	public List<PersonId> getPeople(LabelSet labelSet);

	/**
	 * Returns the people contained in this {@link PopulationPartition}
	 */
	public List<PersonId> getPeople();

	/**
	 * Invoked exactly once after construction, giving the
	 * {@link PopulationPartition} a chance to initialize and probe the
	 * population.
	 */
	public void init();

	/**
	 * Returns a randomly chosen person identifier from the partition consistent
	 * with the partition sampler info. Note that the sampler must be consistent
	 * with the partition definition used to create this population partition.
	 * No precondition tests will be performed.
	 */
	public StochasticPersonSelection samplePartition(final PartitionSampler partitionSampler);

	/**
	 * Returns the {@link FilterInfo} that corresponds to the {@link Partition}
	 * used to form this {@link PopulationPartition}
	 */
	public FilterInfo getFilterInfo();

	/**
	 * Returns the {@link Partition} used to form this
	 * {@link PopulationPartition}
	 */
	public Partition getPartition();
}
