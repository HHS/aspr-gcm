package gcm.simulation.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.Scenario;
import gcm.simulation.BaseElement;
import gcm.simulation.Context;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.StochasticsManager;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.IntValueContainer;
import gcm.util.containers.ObjectValueContainer;

/**
 * Implementor of PersonGroupManger
 *
 ** 
 * 
 * Group membership is managed through four mappings: 1)People to Groups,
 * 2)Groups to People, 3)Groups to GroupTypes and 4)GroupTypes to Groups. Except
 * for groups to types, all are implemented as ObjectValueContainers over
 * ArrayLists of Integers rather than as straight-forward maps. This design was
 * chosen to help minimize the memory requirements for storing grouping data for
 * millions of people.
 *
 * The principle assumptions that have driven this design are:
 *
 * 1) The number of people per group is usually fairly small and rarely exceeds
 * 100
 *
 * 2) The number of groups per person is usually very small and rarely exceeds
 * 10
 *
 * 3) The number of group types is fairly small and rarely exceeds 20
 *
 * The mapping from groups to types is accomplished with an IntValueContainer
 * since the number of group types is small and we can treat the group type ids
 * as Bytes or Shorts. The typesToIndexesMap and indexesToTypesMap serve to help
 * convert group-type Object references to and from integers.
 *
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PersonGroupMangerImpl extends BaseElement implements PersonGroupManger {

	/*
	 * Used to generate new group id values
	 */
	private int masterGroupId;

	// Guard for both weights array and weightedPersonIds array
	private boolean weightsAreLocked;

	private double[] weights;

	private PersonId[] weightedPersonIds;

	private final ObjectValueContainer typesToGroupsMap = new ObjectValueContainer(null, 0);

	private final ObjectValueContainer groupsToPeopleMap = new ObjectValueContainer(null, 0);

	private final ObjectValueContainer peopleToGroupsMap = new ObjectValueContainer(null, 0);

	private final IntValueContainer groupsToTypesMap = new IntValueContainer(-1);

	private final Map<GroupTypeId, Integer> typesToIndexesMap = new LinkedHashMap<>();

	private GroupTypeId[] indexesToTypesMap;

	private ObservableEnvironment observableEnvironment;

	private StochasticsManager stochasticsManager;

	@Override
	public GroupId addGroup(final GroupTypeId groupTypeId) {
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups == null) {
			groups = new ArrayList<>();
			typesToGroupsMap.setValue(typeIndex, groups);
		}
		final GroupId result = new GroupId(masterGroupId++);
		groups.add(result);
		groupsToTypesMap.setIntValue(result.getValue(), typeIndex);
		return result;
	}

	@Override
	public void addPersonToGroup(final GroupId groupId, final PersonId personId) {

		List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people == null) {
			people = new ArrayList<>();
			groupsToPeopleMap.setValue(groupId.getValue(), people);
		}
		people.add(personId);

		List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups == null) {
			groups = new ArrayList<>(1);
			peopleToGroupsMap.setValue(personId.getValue(), groups);
		}
		groups.add(groupId);

	}

	/*
	 * Allocates the weights array to the given size or 50% larger than the current
	 * size, whichever is largest. Size must be non-negative
	 */
	private void allocateWeights(final int size) {
		if (weights == null) {
			weights = new double[size];
			weightedPersonIds = new PersonId[size];
		}
		if (weights.length < size) {
			int newSize = Math.max(size, weights.length + weights.length / 2);
			weights = new double[newSize];
			weightedPersonIds = new PersonId[newSize];
		}
	}

	private void aquireWeightsLock() {
		if (weightsAreLocked) {
			throw new RuntimeException("weights arrray is locked");
		}
		weightsAreLocked = true;
	}

	/*
	 * Returns the index in the weights array that is the first to meet or exceed
	 * the target value. Assumes a strictly increasing set of values for indices 0
	 * through peopleCount. Decreasing values are strictly prohibited. Consecutive
	 * equal values may return an ambiguous result. The target value must not exceed
	 * weights[peopleCount].
	 *
	 */
	private int findTargetIndex(final double targetValue, final int peopleCount) {
		int low = 0;
		int high = peopleCount - 1;

		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final double midVal = weights[mid];
			if (midVal < targetValue) {
				low = mid + 1;
			} else if (midVal > targetValue) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return low;
	}

	@Override
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	@Override
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		int result = 0;
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final GroupTypeId groupType = getGroupType(groupId);
				if (groupType.equals(groupTypeId)) {
					result++;
				}
			}
		}
		return result;
	}

	@Override
	public int getGroupCountForPerson(final PersonId personId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	@Override
	public List<GroupId> getGroupIds() {
		final List<GroupId> result = new ArrayList<>();
		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			result.addAll(getGroupsForGroupType(groupTypeId));
		}
		return result;
	}

	@Override
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);		
		if (groups != null) {
			return new ArrayList<>(groups);			
		}
		return new ArrayList<>();
	}

	@Override
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		final List<GroupId> result = new ArrayList<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				if (getGroupType(groupId).equals(groupTypeId)) {
					result.add(groupId);
				}
			}
		}
		return result;
	}

	@Override
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return new ArrayList<>(groups);
		}
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		return (T) indexesToTypesMap[groupsToTypesMap.getValueAsInt(groupId.getValue())];
	}

	@Override
	public int getGroupTypeCountForPersonId(final PersonId personId) {
		final Set<GroupTypeId> types = new LinkedHashSet<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				types.add(getGroupType(groupId));
			}
		}
		return types.size();
	}

	@Override
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		final Set<T> types = new LinkedHashSet<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				types.add(getGroupType(groupId));
			}
		}
		return new ArrayList<>(types);
	}

	@Override
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return new ArrayList<>(people);
		}
		return new ArrayList<>();
	}

	@Override
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		final Set<PersonId> allPeople = new LinkedHashSet<>();
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					allPeople.addAll(people);
				}
			}
		}
		return new ArrayList<>(allPeople);
	}

	@Override
	public int getPersonCountForGroup(final GroupId groupId) {
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return people.size();
		}
		return 0;
	}

	@Override
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		final Set<PersonId> allPeople = new LinkedHashSet<>();
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					allPeople.addAll(people);
				}
			}
		}
		return allPeople.size();
	}

	@Override
	public boolean groupExists(final GroupId groupId) {
		if (groupId == null) {
			return false;
		}
		if (groupId.getValue() < 0) {
			return false;
		}
		return groupsToTypesMap.getValueAsLong(groupId.getValue()) >= 0;
	}

	@Override
	public void init(final Context context) {
		super.init(context);

		final Scenario scenario = context.getScenario();

		observableEnvironment = context.getObservableEnvironment();

		this.stochasticsManager = context.getStochasticsManager();
		/*
		 * We expect that the group types are already defined and immutable in the
		 * environment. Thus we may build the typesToIndexesMap and indexesToTypesMap
		 * immediately.
		 */
		final Set<GroupTypeId> groupTypeIds = scenario.getGroupTypeIds();
		indexesToTypesMap = new GroupTypeId[groupTypeIds.size()];
		for (final GroupTypeId groupTypeId : groupTypeIds) {
			final int index = typesToIndexesMap.size();
			typesToIndexesMap.put(groupTypeId, index);
			indexesToTypesMap[index] = groupTypeId;
		}
	}

	@Override
	public boolean isGroupMember(final GroupId groupId, final PersonId personId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return groups.contains(groupId);
		}
		return false;
	}

	private void releaseWeightsLock() {
		if (!weightsAreLocked) {
			throw new RuntimeException("weights array is not locked");
		}
		weightsAreLocked = false;
	}

	@Override
	public void removeGroup(final GroupId groupId) {
		if (groupExists(groupId)) {
			final GroupTypeId groupTypeId = getGroupType(groupId);
			groupsToTypesMap.setIntValue(groupId.getValue(), -1);
			final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			groups.remove(groupId);
			if (groups.size() == 0) {
				typesToGroupsMap.setValue(typeIndex, null);
			}
			final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
			groupsToPeopleMap.setValue(groupId.getValue(), null);
			if (people != null) {
				for (final PersonId personId : people) {
					groups = peopleToGroupsMap.getValue(personId.getValue());
					groups.remove(groupId);
				}
			}
		} else {
			throw new RuntimeException("Cannot remove nonexistent group");
		}
	}

	@Override
	public void removePerson(final PersonId personId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		peopleToGroupsMap.setValue(personId.getValue(), null);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						groupsToPeopleMap.setValue(groupId.getValue(), null);
					}
				}
			}
		}
	}

	@Override
	public void removePersonFromGroup(final GroupId groupId, final PersonId personId) {
		if (groupExists(groupId)) {
			final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
			if (people != null) {
				people.remove(personId);
				if (people.size() == 0) {
					groupsToPeopleMap.setValue(groupId.getValue(), null);
				}
			}
			final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
			if (groups != null) {
				groups.remove(groupId);
			}
		} else {
			throw new RuntimeException("Cannot remove person from nonexistent group");
		}
	}

	@Override
	public StochasticPersonSelection sampleGroup(GroupId groupId, GroupSampler groupSampler) {
		GroupSamplerInfo groupSamplerInfo = GroupSamplerInfo.build(groupSampler);
		RandomGenerator randomGenerator;
		if(groupSamplerInfo.getRandomNumberGeneratorId().isPresent()) {
			RandomNumberGeneratorId randomNumberGeneratorId = groupSamplerInfo.getRandomNumberGeneratorId().get();
			randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		}else {
			randomGenerator = stochasticsManager.getRandomGenerator();
		}
		GroupWeightingFunction groupWeightingFunction = groupSamplerInfo.getWeightingFunction().orElse(null);
		PersonId excludedPersonId = groupSamplerInfo.getExcludedPerson().orElse(null);

		final boolean exclude = (excludedPersonId != null) && isGroupMember(groupId, excludedPersonId);
		PersonId selectedPersonId = null;
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());

		if (groupWeightingFunction != null) {

			int candidateCount = people.size();
			if (exclude) {
				candidateCount--;
			}
			if ((people != null) && (candidateCount > 0)) {
				aquireWeightsLock();
				try {
					allocateWeights(people.size());
					/*
					 * Initialize the sum of the weights to zero and set the index in the weights
					 * and weightedPersonId to zero.
					 */
					double sum = 0;
					int weightsLength = 0;
					/*
					 * Collect a weight for each person in the group
					 */
					for (PersonId personId : people) {
						if (personId.equals(excludedPersonId)) {
							continue;
						}
						/*
						 * Determine the weight of the person. Any weight that is negative , infinite or
						 * NAN is cause to return immediately since no person may be legitimately
						 * selected.
						 */
						final double weight = groupWeightingFunction.getWeight(observableEnvironment, personId,
								groupId);
						if (!Double.isFinite(weight) || (weight < 0)) {
							return new StochasticPersonSelection(null, true);
						}
						/*
						 * People having a zero weight are rejected for selection
						 */
						if (weight > 0) {
							sum += weight;
							weights[weightsLength] = sum;
							weightedPersonIds[weightsLength] = personId;
							weightsLength++;
						}
					}

					/*
					 * If at least one person was accepted for selection, then we attempt a random
					 * selection.
					 */
					if (weightsLength > 0) {
						/*
						 * Although the individual weights may have been finite, if the sum of those
						 * weights is not finite no legitimate selection can be made
						 */
						if (!Double.isFinite(sum)) {
							return new StochasticPersonSelection(null, true);
						}

						final double targetValue = randomGenerator.nextDouble() * sum;
						final int targetIndex = findTargetIndex(targetValue, weightsLength);
						selectedPersonId = weightedPersonIds[targetIndex];
					}
				} finally {
					releaseWeightsLock();
				}
			}
		} else {
			if (exclude) {
				if ((people != null) && (people.size() > 1)) {
					while (true) {
						final int selectedIndex = randomGenerator.nextInt(people.size());
						PersonId personId = people.get(selectedIndex);
						if (!personId.equals(excludedPersonId)) {
							selectedPersonId = personId;
							break;
						}
					}
				}
			} else {
				if ((people != null) && (people.size() > 0)) {
					final int selectedIndex = randomGenerator.nextInt(people.size());
					selectedPersonId = people.get(selectedIndex);
				}
			}
		}
		return new StochasticPersonSelection(selectedPersonId, false);
	}

}
