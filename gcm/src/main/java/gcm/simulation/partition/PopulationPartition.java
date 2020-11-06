package gcm.simulation.partition;

import java.util.List;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.StochasticPersonSelection;


public interface PopulationPartition {


	public ComponentId getOwningComponentId();
	


	/**
	 * Precondition : the person id is not null.
	 */
	public void handleAddPerson(long tranactionId, PersonId personId);


	

	/**
	 * Precondition: Person must exist
	 *
	 */
	public void handleRemovePerson(long transactionId, PersonId personId);
	

	public void handleRegionChange(long transactionId, PersonId personId);

	public void handlePersonPropertyChange(long transactionId, PersonId personId, PersonPropertyId personPropertyId);

	public void handlePersonResourceChange(long transactionId, PersonId personId, ResourceId resourceId);

	public void handleCompartmentChange(long transactionId, PersonId personId);

	public void handleGroupMembershipChange(long transactionId, PersonId personId);



	public boolean validateLabelSetInfo(LabelSet labelSet);


	public int getPeopleCount();

	public int getPeopleCount(LabelSet labelSet);


	public boolean contains(PersonId personId);

	public boolean contains(PersonId personId, LabelSet labelSet);

	/**
	 * 
	 * 
	 * Precondition: the population partition query must match the population
	 * partition definition
	 * 
	 */
	public List<PersonId> getPeople(LabelSet labelSet);

	public List<PersonId> getPeople();

	public void init();


	/**
	 * Returns a randomly chosen person identifier from the partition consistent
	 * with the partition sampler info. Note that the sampler must be consistent
	 * with the partition definition used to create this population partition. No
	 * precondition tests will be performed.
	 */
	public StochasticPersonSelection samplePartition(final PartitionSampler partitionSampler); 

	public FilterInfo getFilterInfo();

	public PartitionInfo getPartitionInfo();
	
//	public void report();
}
