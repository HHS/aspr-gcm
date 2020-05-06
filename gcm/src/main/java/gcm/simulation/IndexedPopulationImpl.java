package gcm.simulation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

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
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class IndexedPopulationImpl implements IndexedPopulation{

	/*
	 * Represents the triggering threshold of 0.5% of the population. When an
	 * IndexedPopulation is currently storing its people in a BooleanContainer
	 * and the IndexedPopulation's people are fewer than 0.5% of the total
	 * simulation population, then those people should be stored in a
	 * LinkedHashSet. See {@link MT_BooleanContainer} for a derivation of this
	 * threshold. SET_THRESHOLD and BOOLEAN_CONTAINER_THRESHOLD work as a dual
	 * threshold mechanism to keep thrashing between the two container types to
	 * a minimum.
	 */
	private static int MAP_THRESHOLD = 200;

	/*
	 * Represents the triggering threshold of 2/3% of the population. When an
	 * IndexedPopulation is currently storing its people in a LinkedHashSet and
	 * the IndexedPopulation's people are more than 1% of the total simulation
	 * population, then those people should be stored in a BooleanContainer. See
	 * {@link MT_BooleanContainer} for a derivation of this threshold.
	 * SET_THRESHOLD and BOOLEAN_CONTAINER_THRESHOLD work as a dual threshold
	 * mechanism to keep thrashing between the two container types to a minimum.
	 */

	private static int TREE_BIT_SET_THRESHOLD = 150;

	/*
	 * Enumeration for the two ways that people are stored in an
	 * IndexedPopulation
	 */
	private static enum PeopleContainerMode {
		MAP, TREE_BIT_SET
	}

	/*
	 * PeopleContainer implementor that uses a LinkedHashMap to contain the
	 * people. Since the LinkedHashMap uses significant overhead per person this
	 * container is preferred to a BooleanContainer-based implementor when the
	 * number of people in the set is less than 0.5% of the total population.
	 */

	private static class MapPeopleContainer implements PeopleContainer {

		public MapPeopleContainer() {
		}

		public MapPeopleContainer(PeopleContainer peopleContainer) {
			for (PersonId personId : peopleContainer.getPeople()) {
				add(personId);
			}
		}

		private Map<PersonId, Integer> map = new LinkedHashMap<>();

		private List<PersonId> list = new ArrayList<>();

		@Override
		public List<PersonId> getPeople() {
			return new ArrayList<>(map.keySet());
		}

		@Override
		public boolean add(PersonId personId) {
			boolean result = !map.containsKey(personId);
			if (result) {
				map.put(personId, list.size());
				list.add(personId);
			}
			return result;
		}

		@Override
		public boolean remove(PersonId personId) {
			boolean result = map.containsKey(personId);
			if (result) {
				Integer index = map.remove(personId);
				list.set(index, null);
				/*
				 * If the list is too big we will need to rebuild both the map
				 * and the list since the list has many null values and the map
				 * will no longer point to the correct indices in the list
				 * unless we rebuild the map as well.
				 */
				if (list.size() > 2 * map.size()) {
					list = new ArrayList<>(map.keySet());
					map = new LinkedHashMap<>();
					for (int i = 0; i < list.size(); i++) {
						map.put(list.get(i), i);
					}
				}
			}
			return result;
		}

		@Override
		public int size() {
			return map.keySet().size();
		}

		@Override
		public void addAll(Collection<PersonId> collection) {
			for (PersonId personId : collection) {
				add(personId);
			}
		}

		@Override
		public boolean contains(PersonId personId) {
			return map.containsKey(personId);
		}

		@Override
		public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
			/*
			 * We require that there be at least one person to select
			 */
			if (map.size() > 0) {
				/*
				 * We repeatedly select from the list until we get someone who
				 * is in the set.
				 */
				while (true) {
					PersonId personId = list.get(randomGenerator.nextInt(list.size()));
					if (personId != null) {
						return personId;
					}
				}
			}
			return null;
		}
	}

	/*
	 * PeopleContainer implementor that uses a BitSet to record a boolean value
	 * of true for each person contained. Since the BitSet will tend to be the
	 * size of the full population of the simulation at approximately three
	 * bytes per person, this container is preferred to a Set-based implementor
	 * when the number of people in the set exceeds 0.67% of the total
	 * population.
	 */

	private static final class TreeBitSetPeopleContainer implements PeopleContainer {
		// MAX_POWER is the highest power of two in a positive integer
		private final int MAX_POWER = 30;
		// BLOCK_POWER is the power of two that is the block length -- i.e. 64
		// bits
		private final int BLOCK_POWER = 6;
		// BLOCK_LENGTH is the 64 bit length of a block
		private final int BLOCK_LENGTH = 1 << BLOCK_POWER;
		// power is the power of two that sets the length of the tree array
		private int power = 1;
		private final PersonIdManager personIdManager;
		// bitSet holds the values for each person
		private BitSet bitSet;
		// the tree holds summation nodes in an array that is length two the
		// power
		private int[] tree = new int[1 << power];
		// maxPid is the maximum(exclusive) person id value(int) that can be
		// contained at the current power.
		int maxPid = 1 << (power + BLOCK_POWER - 1);

		public TreeBitSetPeopleContainer(Context context, PeopleContainer peopleContainer) {
			personIdManager = context.getPersonIdManager();
			// initialize the size of the bitSet to that of the full population,
			// including removed people
			int capacity = personIdManager.getPersonIdLimit();
			bitSet = new BitSet(capacity);

			// take the people from the people container that is being replaced
			for (PersonId personId : peopleContainer.getPeople()) {
				add(personId);
			}
		}

		@Override
		public List<PersonId> getPeople() {
			List<PersonId> result = new ArrayList<>(size());
			int n = bitSet.size();
			for (int i = 0; i < n; i++) {
				if (bitSet.get(i)) {
					result.add(personIdManager.getBoxedPersonId(i));
				}
			}
			return result;
		}

		/*
		 * Returns the nearest(<=) integer that is a power of two. For example,
		 * getNearestPowerOfTwo(37) = 32. Requires a positive input.
		 */
		private int getNearestPowerOfTwo(int n) {
			if (n < 1) {
				throw new RuntimeException("Non-positive value");
			}
			int p = 1 << MAX_POWER;

			while (p > 0) {
				if (n >= p) {
					return p;
				}
				p >>= 1;
			}
			return 0;
		}

		/*
		 * Returns the nearest(<=) power of two. For example,
		 * getNearestPowerOfTwo(37) = 5. Requires a positive input.
		 */
		private int getPower(int n) {

			if (n < 1) {
				throw new RuntimeException("Non-positive value");
			}

			int power = MAX_POWER;
			int p = 1 << power;

			while (p > 0) {
				if ((n & p) != 0) {
					return power;
				}
				p >>= 1;
				power--;
			}

			return 0;
		}

		@Override
		public boolean add(PersonId personId) {
			int pid = personId.getValue();
			// do we need to grow?
			if (pid >= maxPid) {
				// determine the new size of the tree array
				int newTreeSize = getNearestPowerOfTwo(pid) >> (BLOCK_POWER - 2);

				/*
				 * The tree array grows by powers of two. We determine how many
				 * power levels the new tree array is over the existing one to
				 * help us transport values from the old array into the new
				 * array rather than recalculate those values. Essentially, the
				 * old tree will slide down the left hand side of the new tree,
				 * while leaving a trail of the old tree's head value behind.
				 * 
				 */

				// moving the old tree's values into the new tree
				int powerShift = getPower(newTreeSize) - power;
				int[] newTree = new int[newTreeSize];
				int base = 1;
				int newBase = base << powerShift;
				for (int p = 0; p < power; p++) {
					for (int i = 0; i < base; i++) {
						newTree[newBase + i] = tree[i + base];
					}
					base <<= 1;
					newBase <<= 1;
				}
				/*
				 * The old tree's root value now has to propagate up the new
				 * tree to its root
				 */
				base = 1 << powerShift;
				while (base > 1) {
					base >>= 1;
					newTree[base] = tree[1];
				}
				// swap the tree
				tree = newTree;

				// update maxPid and power
				maxPid <<= powerShift;
				power += powerShift;
			}
			// add the value
			if (!bitSet.get(pid)) {
				bitSet.set(pid);
				// select the block(index) that will receive the bit flip.
				int block = pid >> BLOCK_POWER;
				block += (tree.length >> 1);
				/*
				 * Propagate the change up through the tree to the root node
				 */
				while (block > 0) {
					tree[block] += 1;
					block >>= 1;
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean remove(PersonId personId) {
			/*
			 * If the person is not contained, then don't try to remove them.
			 * This protects us from removals that are >= maxPid.
			 */
			if (!contains(personId)) {
				return false;
			}
			int pid = personId.getValue();
			if (bitSet.get(pid)) {
				bitSet.set(pid, false);
				// select the block(index) that will receive the bit flip.
				int block = pid >> BLOCK_POWER;
				block += (tree.length >> 1);
				/*
				 * Propagate the change up through the tree to the root node
				 */
				while (block > 0) {
					tree[block] -= 1;
					block >>= 1;
				}
				return true;
			}
			return false;
		}

		@Override
		public int size() {
			return tree[1];
		}

		@Override
		public void addAll(Collection<PersonId> collection) {
			for (PersonId personId : collection) {
				add(personId);
			}
		}

		@Override
		public boolean contains(PersonId personId) {
			return bitSet.get(personId.getValue());
		}

		@Override
		public PersonId getRandomPersonId(RandomGenerator randomGenerator) {

			/*
			 * We need to use an integer that is at least one, so we add one to
			 * the randomly selected index. We will reduce this amount until it
			 * reaches zero.
			 */
			int targetCount = randomGenerator.nextInt(size()) + 1;

			/*
			 * Find the mid point of the tree. Think of the tree array as a
			 * triangle with a single root node at the top. This will be the
			 * first array element in the last row(last half) in the tree. This
			 * is the row that maps to the blocks in the bitset.
			 */
			int midTreeIndex = tree.length >> 1;
			int treeIndex = 1;

			/*
			 * Walk downward in the tree. If we move to the right, we have to
			 * reduce the target value.
			 */
			while (treeIndex < midTreeIndex) {
				treeIndex = treeIndex << 1;
				if (tree[treeIndex] < targetCount) {
					targetCount -= tree[treeIndex];
					treeIndex++;
				}
			}
			/*
			 * We have arrived element of the tree array that corresponds to the
			 * desired block in the bitset. We will need to determine the
			 * positions to scan in the bitset
			 */
			int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
			int bitSetStopIndex = bitSetStartIndex + BLOCK_LENGTH;
			/*
			 * Finally, we scan the bits and reduce the target count until it
			 * reaches zero.
			 */
			for (int i = bitSetStartIndex; i < bitSetStopIndex; i++) {
				if (bitSet.get(i)) {
					targetCount--;
					if (targetCount == 0) {
						return personIdManager.getBoxedPersonId(i);
					}
				}
			}
			return null;
		}
	}

	/*
	 * Implementor of PeopleContainer that acts as a dynamic switching mechanism
	 * between the two lower-level PeopleContainer implementors
	 */
	private static class BasePeopleContainer implements PeopleContainer {

		/*
		 * indexed populations start small and so we default to
		 * PeopleContainerMode.SET
		 */
		private PeopleContainerMode mode = PeopleContainerMode.MAP;

		private final Context context;

		private final PersonLocationManger personLocationManger;

		private PeopleContainer internalPeopleContainer = new MapPeopleContainer();

		public BasePeopleContainer(Context context) {
			this.context = context;
			this.personLocationManger = context.getPersonLocationManger();
		}

		/*
		 * Switches the internal container between BooleanPeopleContainer and
		 * SetPeopleContainer as needed whenever the appropriate threshold has
		 * been crossed. If the size of the container is less than 0.5% of the
		 * total world population, then the SetPeopleContainer should be chosen.
		 * If the size of the container is greater than 1% of the total world
		 * population, then the BooleanPeopleContainer should be chosen. By
		 * setting two separate thresholds, we avoid modality thrash.
		 */
		private void determineMode(int size) {

			switch (mode) {
			case TREE_BIT_SET:
				if (size <= personLocationManger.getPopulationCount() / MAP_THRESHOLD) {
					mode = PeopleContainerMode.MAP;
					internalPeopleContainer = new MapPeopleContainer(internalPeopleContainer);
				}
				break;
			case MAP:
				if (size >= personLocationManger.getPopulationCount() / TREE_BIT_SET_THRESHOLD) {
					mode = PeopleContainerMode.TREE_BIT_SET;
					internalPeopleContainer = new TreeBitSetPeopleContainer(context, internalPeopleContainer);
				}
				break;
			default:
				throw new RuntimeException("unhandled mode " + mode);
			}
		}

		@Override
		public List<PersonId> getPeople() {
			return internalPeopleContainer.getPeople();
		}

		@Override
		public boolean add(PersonId personId) {
			boolean result = internalPeopleContainer.add(personId);
			determineMode(size());
			return result;
		}

		@Override
		public boolean remove(PersonId personId) {
			boolean result = internalPeopleContainer.remove(personId);
			determineMode(size());
			return result;
		}

		@Override
		public int size() {
			return internalPeopleContainer.size();
		}

		@Override
		public void addAll(Collection<PersonId> collection) {
			/*
			 * We are not sure if the collection and the current contents of the
			 * internalPeopleContainer overlap and we don't want to waste time
			 * combining the two sets. So we assume conservatively that we will
			 * be at least as large as the incoming collection.
			 */
			determineMode(collection.size());
			internalPeopleContainer.addAll(collection);
			/*
			 * now that all the adds are done, we finally determine the mode
			 * correctly
			 */
			determineMode(size());
		}

		@Override
		public boolean contains(PersonId personId) {
			return internalPeopleContainer.contains(personId);
		}

		/*
		 * Returns a randomly selected person if this container has any people.
		 * Returns null otherwise.
		 */
		@Override
		public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
			return internalPeopleContainer.getRandomPersonId(randomGenerator);
		}
	}

	/*
	 * Interface for abstracting the details of how people ids are stored as
	 * either a Set or a Boolean container.
	 */
	private static interface PeopleContainer {

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
		 * Returns a randomly selected person if this container has any people.
		 * Returns null otherwise.
		 */
		public PersonId getRandomPersonId(RandomGenerator randomGenerator);
	}

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
	public PersonId getRandomPersonId(final PersonId excludedPersonId) {
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
	public PersonId getRandomPersonFromGenerator(final PersonId excludedPersonId, RandomNumberGeneratorId randomNumberGeneratorId) {
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
								.forEach(personId -> peopleContainer.add(personId));//
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