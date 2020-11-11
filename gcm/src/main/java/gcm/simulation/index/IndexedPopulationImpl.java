package gcm.simulation.index;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.simulation.Context;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.ObservationManager;
import gcm.simulation.StochasticsManager;
import gcm.simulation.partition.FilterDisplay;
import gcm.simulation.partition.FilterEvaluator;
import gcm.simulation.partition.FilterInfo;
import gcm.simulation.partition.FilterPopulationMatcher;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.people.BasePeopleContainer;
import gcm.util.containers.people.PeopleContainer;

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
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class IndexedPopulationImpl implements IndexedPopulation{

	/*
	 * IndexedPopulations are managed by a single instance of the
	 * IndexedPopulationManager which is in turn managed by the Context.
	 * Components are not exposed to IndexedPopulations or the
	 * IndexedPopulationManager but instead work with the Environment as a
	 * proxy.
	 *
	 * Besides holding all of the obvious filtering variables specified by the
	 * client, this implementor has two core structures for containing people.
	 * The first is the PeopleContainer that holds the values of the people who
	 * are currently in the index (they pass all the filters). The second is a
	 * list used to select people at random from the set. This list is allowed
	 * to contain people who are not in the set and when selecting random people
	 * from the list we will have to check that the person so selected is in the
	 * set as well. Updating the contents of the list to sync up with the set is
	 * too expensive to be done each time a person leaves in the index. Instead,
	 * we only refresh the list when it has twice as many people as the set. In
	 * practice this is a performant compromise.
	 */

	private Object key;

	private final ComponentId componentId;

	private final StochasticsManager stochasticsManager;

	private final PeopleContainer peopleContainer;

	private final FilterInfo filterInfo;

	private final FilterEvaluator filterEvaluator;

	private final Environment environment;

	private final ObservationManager observationManager;

	/**
	 * Constructs an IndexedPopulation
	 * 
	 * @param environment
	 * @param ownerKey
	 * @param filter
	 * 
	 * @throws RuntimeException
	 *             <li>if context is null
	 *             <li>if owner key is null
	 *             <li>if filter is null
	 */
	public IndexedPopulationImpl(final Context context, final ComponentId componentId, Object key, final FilterInfo filterInfo) {
		if (context == null) {
			throw new RuntimeException("null context");
		}
		if (componentId == null) {
			throw new RuntimeException("null ownerKey");
		}
		if (filterInfo == null) {
			throw new RuntimeException("null filter");
		}
		this.key = key;
		this.componentId = componentId;
		this.observationManager = context.getObservationManager();
		this.stochasticsManager = context.getStochasticsManager();
		this.filterInfo = filterInfo;
		this.filterEvaluator = FilterEvaluator.build(filterInfo);
		peopleContainer = new BasePeopleContainer(context);
		environment = context.getEnvironment();
	}

	/**
	 * Forces the index to evaluate a person's membership in this index.
	 */
	@Override
	public void evaluate(final PersonId personId) {

		if (filterEvaluator.evaluate(environment, personId)) {
			boolean added = peopleContainer.safeAdd(personId);
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

	/**
	 * Returns the focal key for the owning component of this index.
	 * 
	 * @return
	 */
	@Override
	public ComponentId getOwningComponentId() {
		return componentId;
	}

	/**
	 * Returns the people identifiers of this index
	 */
	@Override
	public List<PersonId> getPeople() {
		return peopleContainer.getPeople();
	}

	/**
	 * Returns a randomly chosen person identifier from the index, excluding the
	 * person identifier given. When the excludedPersonId is null it indicates
	 * that no person is being excluded. Returns null if the index is either
	 * empty or only contains the excluded person.
	 */
	@Override
	public PersonId sampleIndex(final PersonId excludedPersonId) {
		/*
		 * Since we are potentially excluding a person, we need to determine how
		 * many candidates are available. To avoid an infinite loop, we must not
		 * have zero candidates.
		 */
		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			while (true) {
				RandomGenerator randomGenerator = stochasticsManager.getRandomGenerator();
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns a randomly chosen person identifier from the index, excluding the
	 * person identifier given. When the excludedPersonId is null it indicates
	 * that no person is being excluded. Returns null if the index is either
	 * empty or only contains the excluded person.
	 */
	@Override
	public PersonId sampleIndex(final PersonId excludedPersonId, RandomNumberGeneratorId randomNumberGeneratorId) {
		/*
		 * Since we are potentially excluding a person, we need to determine how
		 * many candidates are available. To avoid an infinite loop, we must not
		 * have zero candidates.
		 */
		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			while (true) {
				RandomGenerator randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if and only if the person is contained in the population
	 * index
	 */
	@Override
	public boolean personInPopulationIndex(final PersonId personId) {
		return peopleContainer.contains(personId);
	}

	/**
	 * Initializes this population index.
	 */
	@Override
	public void init() {
		/*
		 * The obvious thing to do here is to invoke evaluate() against every
		 * person in the simulation causing the filter to evaluate each person.
		 * 
		 * However, the FilterPopulationMatcher usually does a much better job
		 * by analyzing the filter and the environment's ability to support the
		 * rapid assessment of individual sub-filters within the filter.
		 * 
		 * As a simple example, consider a filter such as:
		 * 
		 * (people having property value X) AND (people in Region R)
		 * 
		 * If the number of people having property value X is known (due to map
		 * option choices in the property's definition) it could limit the
		 * search for people who match the filter to just those having property
		 * value X.
		 */
		FilterPopulationMatcher	.getMatchingPeople(filterInfo, environment)//
								.forEach(personId -> peopleContainer.unsafeAdd(personId));//
	}

	/**
	 * Removes the person from the index if they are present and does so without
	 * regard to the criteria of the index. Generally, this is used when a
	 * person is being removed from the simulation.
	 */
	@Override
	public boolean remove(final PersonId personId) {
		return peopleContainer.remove(personId);
	}

	/**
	 * Returns the number of people in the index
	 */
	@Override
	public int size() {
		return peopleContainer.size();
	}

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IndexedPopulation [componentId=");
		builder.append(componentId);
		builder.append(", filter=");
		builder.append(FilterDisplay.getPrettyPrint(filterInfo));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the filter.
	 */
	@Override
	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

}