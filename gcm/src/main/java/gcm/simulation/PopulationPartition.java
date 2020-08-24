package gcm.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.BasePeopleContainer;
import gcm.util.containers.PeopleContainer;

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PopulationPartition {

	private static class Key {
		private Object[] keys;

		private Key(Key key) {
			this.keys = Arrays.copyOf(key.keys, key.keys.length);
		}

		private Key(int size) {
			keys = new Object[size];
		}

		// We are using the older Arrays.equals and Arrays.hashCode since it suffices
		// for our use case and uses half the runtime of the newer deep versions
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			// result = prime * result + Arrays.deepHashCode(keys);
			result = prime * result + Arrays.hashCode(keys);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			// We are guaranteed that obj is a Key
//			if (getClass() != obj.getClass()) {
//				return false;
//			}
			Key other = (Key) obj;
//			if (!Arrays.deepEquals(keys, other.keys)) {
//				return false;
//			}
			if (!Arrays.equals(keys, other.keys)) {
				return false;
			}
			return true;
		}

	}

	private final int keySize;

	private int regionLabelIndex = -1;

	private int compartmentLabelIndex = -1;

	private int groupLabelIndex = -1;

	private Map<PersonPropertyId, Integer> personPropertyLabelIndexes = new LinkedHashMap<>();

	private Map<ResourceId, Integer> resourceLabelIndexes = new LinkedHashMap<>();

	// private Map<Key, Set<PersonId>> keyToPeopleMap = new LinkedHashMap<>();
	private Map<Key, PeopleContainer> keyToPeopleMap = new LinkedHashMap<>();

	private List<Key> personToKeyMap = new ArrayList<>();

	private Map<Key, Key> keyMap = new LinkedHashMap<>();

	private ComponentId owningComponentId;

	private final PopulationPartitionDefinition populationPartitionDefinition;

	private final Environment environment;

	private PersonIdManager personIdManager;

	private final Context context;

	public ComponentId getOwningComponentId() {
		return owningComponentId;
	}

	private final StochasticsManager stochasticsManager;

	public PopulationPartition(final Context context, final PopulationPartitionDefinition populationPartitionDefinition,
			final ComponentId owningComponentId) {
		this.context = context;
		this.personIdManager = context.getPersonIdManager();
		this.stochasticsManager = context.getStochasticsManager();
		personToKeyMap = new ArrayList<>(context.getPersonIdManager().getPersonIdLimit());
		this.populationPartitionDefinition = populationPartitionDefinition;
		this.environment = context.getEnvironment();
		this.owningComponentId = owningComponentId;
		int size = 0;

		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			regionLabelIndex = size++;
		}

		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			compartmentLabelIndex = size++;
		}

		for (PersonPropertyId personPropertyId : environment.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, -1);
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, size++);
		}

		for (ResourceId resourceId : environment.getResourceIds()) {
			resourceLabelIndexes.put(resourceId, -1);
		}

		for (ResourceId resourceId : populationPartitionDefinition.getPersonResourceIds()) {
			resourceLabelIndexes.put(resourceId, size++);
		}

		if (populationPartitionDefinition.getGroupPartitionFunction() != null) {
			groupLabelIndex = size++;
		}

		keySize = size;
	}

	private Key buildKey(Key key, int index, Object newLabel) {
		Key result = new Key(key);
		result.keys[index] = newLabel;
		return result;
	}

	public void handleAddPerson(PersonId personId) {
		while (personId.getValue() >= personToKeyMap.size()) {
			personToKeyMap.add(null);
		}
		int index = 0;
		Key key = new Key(keySize);
		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			key.keys[index++] = populationPartitionDefinition.getRegionPartitionFunction()
					.apply(environment.getPersonRegion(personId));
		}
		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			key.keys[index++] = populationPartitionDefinition.getCompartmentPartitionFunction()
					.apply(environment.getPersonCompartment(personId));
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			key.keys[index++] = populationPartitionDefinition.getPersonPropertyPartitionFunction(personPropertyId)
					.apply(environment.getPersonPropertyValue(personId, personPropertyId));
		}

		for (ResourceId resourceId : populationPartitionDefinition.getPersonResourceIds()) {
			key.keys[index++] = populationPartitionDefinition.getPersonResourcePartitionFunction(resourceId)
					.apply(environment.getPersonResourceLevel(personId, resourceId));
		}

		if (populationPartitionDefinition.getGroupPartitionFunction() != null) {

			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
			for (GroupTypeId groupTypeId : environment.getGroupTypeIds()) {
				int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
				builder.setCount(groupTypeId, count);
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			key.keys[index++] = populationPartitionDefinition.getGroupPartitionFunction().apply(groupTypeCountMap);
		}

		Key cleanedKey = keyMap.get(key);
		if (cleanedKey == null) {
			cleanedKey = key;
			keyMap.put(cleanedKey, cleanedKey);
			keyToPeopleMap.put(cleanedKey, new BasePeopleContainer(context));
		}
		personToKeyMap.set(personId.getValue(), cleanedKey);
		keyToPeopleMap.get(cleanedKey).add(personId);
	}

	/**
	 * Precondition: Person must exist
	 *
	 */
	public void handleRemovePerson(PersonId personId) {
		Key key = personToKeyMap.get(personId.getValue());
		personToKeyMap.set(personId.getValue(), null);
		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		peopleContainer.remove(personId);
		if (peopleContainer.size() == 0) {
			keyToPeopleMap.remove(key);
			keyMap.remove(key);
		}
	}

	public void handleRegionChange(PersonId personId) {

		if (regionLabelIndex < 0) {
			return;
		}
		Key currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentRegionLabel = currentKey.keys[regionLabelIndex];

		// get the new label
		Object newRegionLabel = populationPartitionDefinition.getRegionPartitionFunction()
				.apply(environment.getPersonRegion(personId));

		if (newRegionLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentRegionLabel.equals(newRegionLabel)) {
			return;
		}

		// build the new key from the person
		Key newKey = buildKey(currentKey, regionLabelIndex, newRegionLabel);

		move(currentKey, newKey, personId);
	}

	public void handlePersonPropertyChange(PersonId personId, PersonPropertyId personPropertyId) {
		int personPropertyLabelIndex = personPropertyLabelIndexes.get(personPropertyId);
		if (personPropertyLabelIndex < 0) {
			return;
		}
		Key currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentPropertyLabel = currentKey.keys[personPropertyLabelIndex];

		// get the new label
		Object newPropertyLabel = populationPartitionDefinition.getPersonPropertyPartitionFunction(personPropertyId)
				.apply(environment.getPersonPropertyValue(personId, personPropertyId));

		if (newPropertyLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentPropertyLabel.equals(newPropertyLabel)) {
			return;
		}

		// build the new key from the person
		Key newKey = buildKey(currentKey, personPropertyLabelIndex, newPropertyLabel);

		move(currentKey, newKey, personId);
	}

	public void handlePersonResourceChange(PersonId personId, ResourceId resourceId) {
		int resourceLabelIndex = resourceLabelIndexes.get(resourceId);
		if (resourceLabelIndex < 0) {
			return;
		}
		Key currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentResourceLabel = currentKey.keys[resourceLabelIndex];

		// get the new label
		Object newResourceLabel = populationPartitionDefinition.getPersonResourcePartitionFunction(resourceId)
				.apply(environment.getPersonResourceLevel(personId, resourceId));

		if (newResourceLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentResourceLabel.equals(newResourceLabel)) {
			return;
		}

		// build the new key from the person
		Key newKey = buildKey(currentKey, resourceLabelIndex, newResourceLabel);

		move(currentKey, newKey, personId);
	}

	public void handleCompartmentChange(PersonId personId) {

		if (compartmentLabelIndex < 0) {
			return;
		}
		Key currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentCompartmentLabel = currentKey.keys[compartmentLabelIndex];

		// get the new label
		Object newCompartmentLabel = populationPartitionDefinition.getCompartmentPartitionFunction()
				.apply(environment.getPersonCompartment(personId));

		if (newCompartmentLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentCompartmentLabel.equals(newCompartmentLabel)) {
			return;
		}

		// build the new key from the person
		Key newKey = buildKey(currentKey, compartmentLabelIndex, newCompartmentLabel);

		move(currentKey, newKey, personId);
	}

	public void handleGroupMembershipChange(PersonId personId) {

		if (groupLabelIndex < 0) {
			return;
		}

		Key currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentGroupLabel = currentKey.keys[groupLabelIndex];

		// get the new label
		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : environment.getGroupTypeIds()) {
			int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			builder.setCount(groupTypeId, count);
		}

		GroupTypeCountMap groupTypeCountMap = builder.build();

		Object newGroupLabel = populationPartitionDefinition.getGroupPartitionFunction().apply(groupTypeCountMap);

		if (newGroupLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentGroupLabel.equals(newGroupLabel)) {
			return;
		}

		// build the new key from the person
		Key newKey = buildKey(currentKey, groupLabelIndex, newGroupLabel);

		move(currentKey, newKey, personId);
	}

	private void move(Key currentKey, Key newKey, PersonId personId) {
		Key cleanedNewKey = keyMap.get(newKey);
		if (cleanedNewKey == null) {
			cleanedNewKey = newKey;
			keyMap.put(cleanedNewKey, cleanedNewKey);
			keyToPeopleMap.put(cleanedNewKey, new BasePeopleContainer(context));
		}
		keyToPeopleMap.get(currentKey).remove(personId);
		keyToPeopleMap.get(cleanedNewKey).add(personId);
		personToKeyMap.set(personId.getValue(), cleanedNewKey);
	}

	public boolean validatePopulationPartitionQuery(PopulationPartitionQuery populationPartitionQuery) {
		boolean b1 = populationPartitionDefinition.getRegionPartitionFunction() == null;
		boolean b2 = populationPartitionQuery.getRegionLabel() == null;
		if (b1 != b2) {
			return false;
		}

		b1 = populationPartitionDefinition.getCompartmentPartitionFunction() == null;
		b2 = populationPartitionQuery.getCompartmentLabel() == null;
		if (b1 != b2) {
			return false;
		}

		b1 = populationPartitionDefinition.getPersonPropertyIds()
				.equals(populationPartitionQuery.getPersonPropertyIds());
		if (!b1) {
			return false;
		}

		b1 = populationPartitionDefinition.getPersonResourceIds()
				.equals(populationPartitionQuery.getPersonResourceIds());
		if (!b1) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a randomly chosen person identifier from the index, excluding the
	 * person identifier given. When the excludedPersonId is null it indicates that
	 * no person is being excluded. Returns null if the index is either empty or
	 * only contains the excluded person.
	 */
	public PersonId getRandomPersonId(final PersonId excludedPersonId,
			PopulationPartitionQuery populationPartitionQuery) {
		
		Key key = getKey(populationPartitionQuery);

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		
		if (peopleContainer == null) {
			return null;
		}

		/*
		 * Since we are potentially excluding a person, we need to determine how many
		 * candidates are available. To avoid an infinite loop, we must not have zero
		 * candidates.
		 */
		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			RandomGenerator randomGenerator = stochasticsManager.getRandomGenerator();
			while (true) {
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
	 * person identifier given. When the excludedPersonId is null it indicates that
	 * no person is being excluded. Returns null if the index is either empty or
	 * only contains the excluded person.
	 */

	public PersonId getRandomPersonFromGenerator(final PersonId excludedPersonId,
			PopulationPartitionQuery populationPartitionQuery, RandomNumberGeneratorId randomNumberGeneratorId) {
		
		Key key = getKey(populationPartitionQuery);

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		
		if (peopleContainer == null) {
			return null;
		}

		/*
		 * Since we are potentially excluding a person, we need to determine how many
		 * candidates are available. To avoid an infinite loop, we must not have zero
		 * candidates.
		 */
		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			RandomGenerator randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
			while (true) {
				result = peopleContainer.getRandomPersonId(randomGenerator);				
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return result;
		
	}

	public int getPeopleCount(PopulationPartitionQuery populationPartitionQuery) {
		Key key = getKey(populationPartitionQuery);

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		if (peopleContainer == null) {
			return 0;
		}
		return peopleContainer.size();
	}

	private Key getKey(PopulationPartitionQuery populationPartitionQuery) {
		Key key = new Key(keySize);
		int index = 0;
		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			Object regionLabel = populationPartitionQuery.getRegionLabel();
			key.keys[index++] = regionLabel;
		}

		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			Object compartmentLabel = populationPartitionQuery.getCompartmentLabel();
			key.keys[index++] = compartmentLabel;
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			key.keys[index++] = populationPartitionQuery.getPersonPropertyLabel(personPropertyId);
		}

		for (ResourceId resourceId : populationPartitionDefinition.getPersonResourceIds()) {
			key.keys[index++] = populationPartitionQuery.getPersonResourceLabel(resourceId);
		}

		if (populationPartitionDefinition.getGroupPartitionFunction() != null) {
			Object groupLabel = populationPartitionQuery.getGroupLabel();
			key.keys[index++] = groupLabel;
		}
		return key;
	}
	
	public boolean contains(PersonId personId, PopulationPartitionQuery populationPartitionQuery) {

		Key key = getKey(populationPartitionQuery);

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		
		if (peopleContainer == null) {
			return false;
		}
		return peopleContainer.contains(personId);
	}

	/**
	 * 
	 * 
	 * Precondition: the population partition query must match the population
	 * partition definition
	 * 
	 */
	public List<PersonId> getPeople(PopulationPartitionQuery populationPartitionQuery) {

		Key key = getKey(populationPartitionQuery);

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		
		if (peopleContainer == null) {
			return new ArrayList<>();
		}
		return peopleContainer.getPeople();
	}

	public void init() {
		for (PersonId personId : personIdManager.getPeople()) {
			handleAddPerson(personId);
		}
	}

}
