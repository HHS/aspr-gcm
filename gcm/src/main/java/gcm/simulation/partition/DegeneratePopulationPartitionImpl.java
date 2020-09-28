package gcm.simulation.partition;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.ResourceId;
import gcm.simulation.Context;
import gcm.simulation.Environment;
import gcm.simulation.ObservationManager;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.StochasticsManager;
import gcm.util.containers.BasePeopleContainer;
import gcm.util.containers.PeopleContainer;

/**
 * Implementation of PopulationPartition for degenerate partitions having only a
 * filter.
 */
public class DegeneratePopulationPartitionImpl implements PopulationPartition {
	private Object key;

	private final ComponentId componentId;

	private final StochasticsManager stochasticsManager;

	private final PeopleContainer peopleContainer;

	private final FilterInfo filterInfo;

	private final PartitionInfo partitionInfo;

	private final FilterEvaluator filterEvaluator;

	private final Environment environment;

	private final ObservationManager observationManager;

	@Override
	public ComponentId getOwningComponentId() {
		return componentId;
	}

	@Override
	public void handleAddPerson(long transactionId, PersonId personId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handleRemovePerson(long transactionId, PersonId personId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handleRegionChange(long transactionId, PersonId personId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handlePersonPropertyChange(long transactionId, PersonId personId, PersonPropertyId personPropertyId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handlePersonResourceChange(long transactionId, PersonId personId, ResourceId resourceId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handleCompartmentChange(long transactionId, PersonId personId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public void handleGroupMembershipChange(long transactionId, PersonId personId) {
		if (acceptTransactionId(transactionId)) {
			evaluate(personId);
		}
	}

	@Override
	public boolean validateLabelSetInfo(LabelSet labelSet) {
		return labelSet.isEmpty();
	}

	@Override
	public int getPeopleCount() {
		return peopleContainer.size();

	}

	@Override
	public int getPeopleCount(LabelSet labelSet) {
		return peopleContainer.size();
	}

	@Override
	public boolean contains(PersonId personId) {
		return peopleContainer.contains(personId);
	}

	@Override
	public boolean contains(PersonId personId, LabelSet labelSet) {
		return peopleContainer.contains(personId);
	}

	@Override
	public List<PersonId> getPeople(LabelSet labelSet) {
		return peopleContainer.getPeople();
	}

	/**
	 * Returns the people identifiers of this index
	 */
	@Override
	public List<PersonId> getPeople() {
		return peopleContainer.getPeople();
	}

	@Override
	public void init() {
		/*
		 * The obvious thing to do here is to invoke evaluate() against every person in
		 * the simulation causing the filter to evaluate each person.
		 * 
		 * However, the FilterPopulationMatcher usually does a much better job by
		 * analyzing the filter and the environment's ability to support the rapid
		 * assessment of individual sub-filters within the filter.
		 * 
		 * As a simple example, consider a filter such as:
		 * 
		 * (people having property value X) AND (people in Region R)
		 * 
		 * If the number of people having property value X is known (due to map option
		 * choices in the property's definition) it could limit the search for people
		 * who match the filter to just those having property value X.
		 */
		FilterPopulationMatcher.getMatchingPeople(filterInfo, environment)//
				.forEach(personId -> peopleContainer.add(personId));//
	}

	@Override
	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	@Override
	public PartitionInfo getPartitionInfo() {
		return partitionInfo;
	}

	private boolean remove(final PersonId personId) {
		return peopleContainer.remove(personId);
	}

	/**
	 * Constructs an IndexedPopulation
	 * 
	 * @param environment
	 * @param ownerKey
	 * @param filter
	 * 
	 * @throws RuntimeException
	 *                          <li>if context is null
	 *                          <li>if owner key is null
	 *                          <li>if filter is null
	 */
	public DegeneratePopulationPartitionImpl(final Object identifierKey, final Context context,
			final PartitionInfo partitionInfo, final ComponentId owningComponentId) {
		this.key = identifierKey;
		this.componentId = owningComponentId;
		this.observationManager = context.getObservationManager();
		this.stochasticsManager = context.getStochasticsManager();
		this.partitionInfo = partitionInfo;
		this.filterInfo = partitionInfo.getFilterInfo();
		this.filterEvaluator = FilterEvaluator.build(filterInfo);
		peopleContainer = new BasePeopleContainer(context);
		environment = context.getEnvironment();
	}

	private long lastTransactionId = -1;

	/*
	 * Returns true if and only if the given transaction id is not the most recent
	 * transaction id. This prevents duplicate updates when a filtered partition
	 * meets more than one of the trigger conditions maintained by the filtered
	 * partition manager.
	 */
	private boolean acceptTransactionId(long transactionId) {
		if (transactionId == lastTransactionId) {
			return false;
		}
		lastTransactionId = transactionId;
		return true;
	}

	/**
	 * Forces the index to evaluate a person's membership in this index.
	 */

	private void evaluate(final PersonId personId) {

		if (filterEvaluator.evaluate(environment, personId)) {
			boolean added = peopleContainer.add(personId);
			if (added) {
				observationManager.handlePopulationIndexPersonAddition(key, personId);
			}
		} else {
			boolean removed = remove(personId);
			if (removed) {
				observationManager.handlePopulationIndexPersonRemoval(key, personId);
			}
		}
	}

	@Override
	public StochasticPersonSelection samplePartition(PartitionSampler partitionSampler) {
		RandomGenerator randomGenerator;
		RandomNumberGeneratorId randomNumberGeneratorId = partitionSampler.getRandomNumberGeneratorId().orElse(null);
		if (randomNumberGeneratorId != null) {
			randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		} else {
			randomGenerator = stochasticsManager.getRandomGenerator();
		}

		PersonId excludedPersonId = partitionSampler.getExcludedPerson().orElse(null);

		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			while (true) {
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}		
		return new StochasticPersonSelection(result,false);		
	}

}