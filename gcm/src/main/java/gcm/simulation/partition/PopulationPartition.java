package gcm.simulation.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.Context;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.PersonIdManager;
import gcm.util.Tuplator;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.BasePeopleContainer;
import gcm.util.containers.PeopleContainer;

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PopulationPartition {

	private static class LabelCounter {
		int count;
	}

	private static class LabelManager {

		private Map<Object, LabelCounter> labels = new LinkedHashMap<>();
		private List<Object> labelList = new ArrayList<>();

		public void addLabel(Object label) {
			LabelCounter labelCounter = labels.get(label);
			if (labelCounter == null) {
				labelList.add(label);
				labelCounter = new LabelCounter();
				labels.put(label, labelCounter);
			}
			labelCounter.count++;
		}

		public void removeLabel(Object label) {
			LabelCounter labelCounter = labels.get(label);
			labelCounter.count--;
			if (labelCounter.count == 0) {
				labels.remove(label);
				labelList.remove(label);
			}
		}

		public int getLabelCount() {
			return labelList.size();
		}

		public Object getLabel(int index) {
			return labelList.get(index);
		}

	}

	private static class Key {

		private Object[] keys;

		public boolean isPartialKey() {
			for (int i = 0; i < keys.length; i++) {
				if (keys[i] == null) {
					return true;
				}
			}
			return false;
		}

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

		@Override
		public String toString() {
			return "Key [keys=" + Arrays.toString(keys) + "]";
		}

	}

	private final int keySize;

	private int regionLabelIndex = -1;

	private int compartmentLabelIndex = -1;

	private int groupLabelIndex = -1;

	private Map<PersonPropertyId, Integer> personPropertyLabelIndexes = new LinkedHashMap<>();

	private Map<ResourceId, Integer> resourceLabelIndexes = new LinkedHashMap<>();

	private Map<Key, PeopleContainer> keyToPeopleMap = new LinkedHashMap<>();

	private List<Key> personToKeyMap = new ArrayList<>();

	private Map<Key, Key> keyMap = new LinkedHashMap<>();

	private LabelManager[] labelManagers;

	private ComponentId owningComponentId;

	private final PartitionInfo partitionInfo;

	private final Environment environment;

	private PersonIdManager personIdManager;

	private final Context context;

	public ComponentId getOwningComponentId() {
		return owningComponentId;
	}

	public PopulationPartition(final Context context, final PartitionInfo partitionInfo,
			final ComponentId owningComponentId) {
		this.context = context;
		this.personIdManager = context.getPersonIdManager();
		personToKeyMap = new ArrayList<>(context.getPersonIdManager().getPersonIdLimit());
		this.partitionInfo = partitionInfo;
		this.environment = context.getEnvironment();
		this.owningComponentId = owningComponentId;
		int size = 0;

		if (partitionInfo.getRegionPartitionFunction() != null) {
			regionLabelIndex = size++;
		}

		if (partitionInfo.getCompartmentPartitionFunction() != null) {
			compartmentLabelIndex = size++;
		}

		for (PersonPropertyId personPropertyId : environment.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, -1);
		}

		for (PersonPropertyId personPropertyId : partitionInfo.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, size++);
		}

		for (ResourceId resourceId : environment.getResourceIds()) {
			resourceLabelIndexes.put(resourceId, -1);
		}

		for (ResourceId resourceId : partitionInfo.getPersonResourceIds()) {
			resourceLabelIndexes.put(resourceId, size++);
		}

		if (partitionInfo.getGroupPartitionFunction() != null) {
			groupLabelIndex = size++;
		}

		keySize = size;

		labelManagers = new LabelManager[keySize];
		for (int i = 0; i < keySize; i++) {
			labelManagers[i] = new LabelManager();
		}
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
		if (partitionInfo.getRegionPartitionFunction() != null) {
			key.keys[index++] = partitionInfo.getRegionPartitionFunction().apply(environment.getPersonRegion(personId));
		}
		if (partitionInfo.getCompartmentPartitionFunction() != null) {
			key.keys[index++] = partitionInfo.getCompartmentPartitionFunction()
					.apply(environment.getPersonCompartment(personId));
		}

		for (PersonPropertyId personPropertyId : partitionInfo.getPersonPropertyIds()) {
			key.keys[index++] = partitionInfo.getPersonPropertyPartitionFunction(personPropertyId)
					.apply(environment.getPersonPropertyValue(personId, personPropertyId));
		}

		for (ResourceId resourceId : partitionInfo.getPersonResourceIds()) {
			key.keys[index++] = partitionInfo.getPersonResourcePartitionFunction(resourceId)
					.apply(environment.getPersonResourceLevel(personId, resourceId));
		}

		if (partitionInfo.getGroupPartitionFunction() != null) {

			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
			for (GroupTypeId groupTypeId : environment.getGroupTypeIds()) {
				int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
				builder.setCount(groupTypeId, count);
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			key.keys[index++] = partitionInfo.getGroupPartitionFunction().apply(groupTypeCountMap);
		}

		Key cleanedKey = keyMap.get(key);
		if (cleanedKey == null) {
			cleanedKey = key;
			keyMap.put(cleanedKey, cleanedKey);
			keyToPeopleMap.put(cleanedKey, new BasePeopleContainer(context));

		}
		for (int i = 0; i < keySize; i++) {
			LabelManager labelManager = labelManagers[i];
			labelManager.addLabel(cleanedKey.keys[i]);
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
		for (int i = 0; i < keySize; i++) {
			LabelManager labelManager = labelManagers[i];
			labelManager.removeLabel(key.keys[i]);
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
		Object newRegionLabel = partitionInfo.getRegionPartitionFunction().apply(environment.getPersonRegion(personId));

		if (newRegionLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentRegionLabel.equals(newRegionLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[regionLabelIndex];
		labelManager.removeLabel(currentRegionLabel);
		labelManager.addLabel(newRegionLabel);

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
		Object newPropertyLabel = partitionInfo.getPersonPropertyPartitionFunction(personPropertyId)
				.apply(environment.getPersonPropertyValue(personId, personPropertyId));

		if (newPropertyLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentPropertyLabel.equals(newPropertyLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[personPropertyLabelIndex];
		labelManager.removeLabel(currentPropertyLabel);
		labelManager.addLabel(newPropertyLabel);

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
		Object newResourceLabel = partitionInfo.getPersonResourcePartitionFunction(resourceId)
				.apply(environment.getPersonResourceLevel(personId, resourceId));

		if (newResourceLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentResourceLabel.equals(newResourceLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[resourceLabelIndex];
		labelManager.removeLabel(currentResourceLabel);
		labelManager.addLabel(newResourceLabel);

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
		Object newCompartmentLabel = partitionInfo.getCompartmentPartitionFunction()
				.apply(environment.getPersonCompartment(personId));

		if (newCompartmentLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentCompartmentLabel.equals(newCompartmentLabel)) {
			return;
		}
		LabelManager labelManager = labelManagers[compartmentLabelIndex];
		labelManager.removeLabel(currentCompartmentLabel);
		labelManager.addLabel(newCompartmentLabel);

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

		Object newGroupLabel = partitionInfo.getGroupPartitionFunction().apply(groupTypeCountMap);

		if (newGroupLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentGroupLabel.equals(newGroupLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[groupLabelIndex];
		labelManager.removeLabel(currentGroupLabel);
		labelManager.addLabel(newGroupLabel);

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

	public boolean validateLabelSetInfo(LabelSetInfo labelSetInfo) {
		boolean b1 = partitionInfo.getRegionPartitionFunction() == null;
		boolean b2 = labelSetInfo.getRegionLabel() == null;
		if (b1 && !b2) {
			return false;
		}

		b1 = partitionInfo.getCompartmentPartitionFunction() == null;
		b2 = labelSetInfo.getCompartmentLabel() == null;
		if (b1 && !b2) {
			return false;
		}

		Set<PersonPropertyId> allowedPersonPropertyIds = partitionInfo.getPersonPropertyIds();
		for (PersonPropertyId personPropertyId : labelSetInfo.getPersonPropertyIds()) {
			if (!allowedPersonPropertyIds.contains(personPropertyId)) {
				return false;
			}
		}

		Set<ResourceId> allowedPersonResourceIds = partitionInfo.getPersonResourceIds();
		for (ResourceId resourceId : labelSetInfo.getPersonResourceIds()) {
			if (!allowedPersonResourceIds.contains(resourceId)) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Returns a list of non-partial keys from the given partial key where each full
	 * key is currently present in the key map and is associated with a Population
	 * Container.
	 */
	private List<Key> getFullKeys(Key partialKey) {

		List<Key> result = new ArrayList<>();

		int dimensionCount = 0;
		for (int i = 0; i < keySize; i++) {
			if (partialKey.keys[i] == null) {
				dimensionCount++;
			}
		}
		int[] tuple = new int[dimensionCount];
		int[] keyIndexes = new int[dimensionCount];
		int index = 0;
		for (int i = 0; i < keySize; i++) {
			if (partialKey.keys[i] == null) {
				keyIndexes[index++] = i;
			}
		}

		Tuplator.Builder builder = Tuplator.builder();
		for (int i = 0; i < dimensionCount; i++) {
			int labelIndex = keyIndexes[i];
			LabelManager labelManager = labelManagers[labelIndex];
			builder.addDimension(labelManager.getLabelCount());
		}
		Tuplator tuplator = builder.build();

		for (int i = 0; i < tuplator.size(); i++) {
			tuplator.getTuple(i, tuple);
			Key fullKey = new Key(partialKey);
			for (int j = 0; j < dimensionCount; j++) {
				LabelManager labelManager = labelManagers[keyIndexes[j]];
				fullKey.keys[keyIndexes[j]] = labelManager.getLabel(tuple[j]);
			}
			fullKey = keyMap.get(fullKey);
			if (fullKey != null) {
				result.add(fullKey);
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
	public PersonId getRandomPersonId(final PersonId excludedPersonId, LabelSetInfo labelSetInfo,
			RandomGenerator randomGenerator) {

		// TODO -- multi-sampling from the random generator is likely inefficient
		Key key = getKey(labelSetInfo);
		Key selectedKey = null;
		// RandomGenerator randomGenerator = stochasticsManager.getRandomGenerator();
		if (key.isPartialKey()) {
			List<Key> fullKeys = getFullKeys(key);
			int personCount = 0;

			for (Key fullKey : fullKeys) {
				PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				int containerSize = peopleContainer.size();
				personCount += peopleContainer.size();
				if (peopleContainer.contains(excludedPersonId)) {
					personCount--;
					containerSize--;
				}
				double selectionProbabilty = containerSize;
				selectionProbabilty /= personCount;
				// if the selection probability is NaN, the fullKey will not be selected
				if (randomGenerator.nextDouble() < selectionProbabilty) {
					selectedKey = fullKey;
				}
			}
		} else {
			selectedKey = key;
		}
		if (selectedKey == null) {
			return null;
		}
		return getRandomPersonId(excludedPersonId, selectedKey, randomGenerator);
	}

	private PersonId getRandomPersonId(final PersonId excludedPersonId, Key key, RandomGenerator randomGenerator) {

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

			while (true) {
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return result;
	}

	public int getPeopleCount(LabelSetInfo labelSetInfo) {
		Key key = getKey(labelSetInfo);

		if (key.isPartialKey()) {
			List<Key> fullKeys = getFullKeys(key);

			int result = 0;
			for (Key fullKey : fullKeys) {
				PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				if (peopleContainer != null) {
					result += peopleContainer.size();
				}
			}
			return result;
		} else {

			PeopleContainer peopleContainer = keyToPeopleMap.get(key);
			if (peopleContainer == null) {
				return 0;
			}
			return peopleContainer.size();
		}
	}

	private Key getKey(LabelSetInfo labelSetInfo) {
		Key key = new Key(keySize);
		int index = 0;
		if (partitionInfo.getRegionPartitionFunction() != null) {
			Object regionLabel = labelSetInfo.getRegionLabel();
			key.keys[index++] = regionLabel;
		}

		if (partitionInfo.getCompartmentPartitionFunction() != null) {
			Object compartmentLabel = labelSetInfo.getCompartmentLabel();
			key.keys[index++] = compartmentLabel;
		}

		for (PersonPropertyId personPropertyId : partitionInfo.getPersonPropertyIds()) {
			key.keys[index++] = labelSetInfo.getPersonPropertyLabel(personPropertyId);
		}

		for (ResourceId resourceId : partitionInfo.getPersonResourceIds()) {
			key.keys[index++] = labelSetInfo.getPersonResourceLabel(resourceId);
		}

		if (partitionInfo.getGroupPartitionFunction() != null) {
			Object groupLabel = labelSetInfo.getGroupLabel();
			key.keys[index++] = groupLabel;
		}
		return key;
	}

	public boolean contains(PersonId personId, LabelSetInfo labelSetInfo) {
		Key key = getKey(labelSetInfo);

		if (key.isPartialKey()) {
			List<Key> fullKeys = getFullKeys(key);
			for (Key fullKey : fullKeys) {
				PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				if (peopleContainer != null && peopleContainer.contains(personId)) {
					return true;
				}
			}
			return false;
		} else {
			PeopleContainer peopleContainer = keyToPeopleMap.get(key);

			if (peopleContainer == null) {
				return false;
			}
			return peopleContainer.contains(personId);
		}
	}

	/**
	 * 
	 * 
	 * Precondition: the population partition query must match the population
	 * partition definition
	 * 
	 */
	public List<PersonId> getPeople(LabelSetInfo labelSetInfo) {

		Key key = getKey(labelSetInfo);

		if (key.isPartialKey()) {
			List<Key> fullKeys = getFullKeys(key);
			List<PersonId> result = new ArrayList<>();
			for (Key fullKey : fullKeys) {
				PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				result.addAll(peopleContainer.getPeople());
			}
			return result;
		} else {
			PeopleContainer peopleContainer = keyToPeopleMap.get(key);

			if (peopleContainer == null) {
				return new ArrayList<>();
			}
			return peopleContainer.getPeople();
		}
	}

	public void init() {
		for (PersonId personId : personIdManager.getPeople()) {
			handleAddPerson(personId);
		}
	}

}
