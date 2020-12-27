package gcm.simulation.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.CompartmentId;
import gcm.scenario.ComponentId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.Context;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.ModelException;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.ObservationManager;
import gcm.simulation.PersonIdManager;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.StochasticsManager;
import gcm.simulation.partition.LabelSet.Builder;
import gcm.util.Tuplator;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.people.BasePeopleContainer;
import gcm.util.containers.people.PeopleContainer;
/**
 * Primary implementor for {@link PopulationPartition}
 * 
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PopulationPartitionImpl implements PopulationPartition {

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

		// We are using the older Arrays.equals and Arrays.hashCode since it
		// suffices
		// for our use case and uses half the runtime of the newer deep versions
		@Override
		public int hashCode() {
			return Arrays.hashCode(keys);
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
			// if (getClass() != obj.getClass()) {
			// return false;
			// }
			Key other = (Key) obj;

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

	private Map<Key, LabelSet> labelSetInfoMap = new LinkedHashMap<>();

	private LabelManager[] labelManagers;

	private ComponentId owningComponentId;

	private final Partition partition;

	private final FilterInfo filterInfo;

	private final FilterEvaluator filterEvaluator;

	private final Environment environment;

	private final Context context;

	private final ObservableEnvironment observableEnvironment;

	private final ObservationManager observationManager;

	private final Object identifierKey;

	private int personCount;

	private boolean isEmpty() {

		return personCount == 0;
	}

	@Override
	public ComponentId getOwningComponentId() {
		return owningComponentId;
	}

	// Returns true if and only if the person is contained in this filtered
	// population partition after the evaluation of the person against the
	// filter.
	// This will force the addition or removal of the person from the
	// corresponding
	// partition cell.

	private boolean evaluate(final PersonId personId) {
		if (filterEvaluator.evaluate(environment, personId)) {
			boolean added = addPerson(personId);
			if (added) {
				observationManager.handlePartitionPersonAddition(identifierKey, personId);
			}
			return true;
		}
		boolean removed = removePerson(personId);
		if (removed) {
			observationManager.handlePartitionPersonRemoval(identifierKey, personId);
		}
		return false;
	}

	private StochasticsManager stochasticsManager;

	public PopulationPartitionImpl(final Object identifierKey, final Context context, final Partition partition, final ComponentId owningComponentId) {
		this.context = context;
		this.identifierKey = identifierKey;
		this.observableEnvironment = context.getObservableEnvironment();
		personToKeyMap = new ArrayList<>(context.getPersonIdManager().getPersonIdLimit());
		this.partition = partition;
		Filter filter = partition.getFilter().orElse(Filter.allPeople());
		this.filterInfo = FilterInfo.build(filter);
		this.environment = context.getEnvironment();
		this.owningComponentId = owningComponentId;
		this.filterEvaluator = FilterEvaluator.build(filterInfo);
		this.observationManager = context.getObservationManager();
		this.stochasticsManager = context.getStochasticsManager();

		int size = 0;

		if (partition.getRegionPartitionFunction().isPresent()) {
			regionLabelIndex = size++;
		}

		if (partition.getCompartmentPartitionFunction().isPresent()) {
			compartmentLabelIndex = size++;
		}

		for (PersonPropertyId personPropertyId : environment.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, -1);
		}

		for (PersonPropertyId personPropertyId : partition.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, size++);
		}

		for (ResourceId resourceId : environment.getResourceIds()) {
			resourceLabelIndexes.put(resourceId, -1);
		}

		for (ResourceId resourceId : partition.getPersonResourceIds()) {
			resourceLabelIndexes.put(resourceId, size++);
		}

		if (partition.getGroupPartitionFunction().isPresent()) {
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

	private long lastTransactionId = -1;

	/*
	 * Returns true if and only if the given transaction id is not the most
	 * recent transaction id. This prevents duplicate updates when a filtered
	 * partition meets more than one of the trigger conditions maintained by the
	 * filtered partition manager.
	 */
	private boolean acceptTransactionId(long transactionId) {
		if (transactionId == lastTransactionId) {
			return false;
		}
		lastTransactionId = transactionId;
		return true;
	}

	/**
	 * Precondition : the person id is not null.
	 */
	@Override
	public void handleAddPerson(long transactionId, PersonId personId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		evaluate(personId);
	}

	private boolean addPerson(PersonId personId) {

		if (personId == null) {
			return false;
		}

		if (contains(personId)) {
			return false;
		}

		while (personId.getValue() >= personToKeyMap.size()) {
			personToKeyMap.add(null);
		}
		int index = 0;
		Key key = new Key(keySize);
		if (partition.getRegionPartitionFunction().isPresent()) {

			RegionId regionId = environment.getPersonRegion(personId);
			Object label = partition.getRegionPartitionFunction().get().apply(regionId);
			if (label == null) {
				throwModelException(SimulationErrorType.NULL_REGION_LABEL, regionId);
			}
			key.keys[index++] = label;
		}
		if (partition.getCompartmentPartitionFunction().isPresent()) {
			CompartmentId compartmentId = environment.getPersonCompartment(personId);
			Object label = partition.getCompartmentPartitionFunction().get().apply(compartmentId);
			if (label == null) {
				throwModelException(SimulationErrorType.NULL_COMPARTMENT_LABEL, compartmentId);
			}
			key.keys[index++] = label;
		}

		for (PersonPropertyId personPropertyId : partition.getPersonPropertyIds()) {
			Object personPropertyValue = environment.getPersonPropertyValue(personId, personPropertyId);
			Object label = partition.getPersonPropertyPartitionFunction(personPropertyId).get().apply(personPropertyValue);
			if (label == null) {
				throwModelException(SimulationErrorType.NULL_PROPERTY_LABEL, personPropertyId+"="+personPropertyValue);
			}			
			key.keys[index++] = label; 
		}

		for (ResourceId resourceId : partition.getPersonResourceIds()) {
			long personResourceLevel = environment.getPersonResourceLevel(personId, resourceId);
			Object label = partition.getPersonResourcePartitionFunction(resourceId).get().apply(personResourceLevel);
			if (label == null) {
				throwModelException(SimulationErrorType.NULL_RESOURCE_LABEL, resourceId+"="+personResourceLevel);
			}			
			key.keys[index++] = label; 
		}

		if (partition.getGroupPartitionFunction().isPresent()) {

			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
			for (GroupTypeId groupTypeId : environment.getGroupTypeIds()) {
				int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
				builder.setCount(groupTypeId, count);
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			Object label = partition.getGroupPartitionFunction().get().apply(groupTypeCountMap);
			if (label == null) {
				throwModelException(SimulationErrorType.NULL_GROUP_LABEL, groupTypeCountMap);
			}	
			key.keys[index++] = label; 
		}

		Key cleanedKey = keyMap.get(key);
		if (cleanedKey == null) {
			cleanedKey = key;
			keyMap.put(cleanedKey, cleanedKey);
			BasePeopleContainer basePeopleContainer = new BasePeopleContainer(context);
			keyToPeopleMap.put(cleanedKey, basePeopleContainer);
			LabelSet labelSet = getLabelSet(cleanedKey);
			labelSetInfoMap.put(cleanedKey, labelSet);
		}
		for (int i = 0; i < keySize; i++) {
			LabelManager labelManager = labelManagers[i];
			labelManager.addLabel(cleanedKey.keys[i]);
		}
		personToKeyMap.set(personId.getValue(), cleanedKey);
		keyToPeopleMap.get(cleanedKey).unsafeAdd(personId);
		personCount++;
		return true;
	}

	private LabelSet getLabelSet(Key key) {
		Builder builder = LabelSet.builder();
		if (compartmentLabelIndex >= 0) {
			builder.setCompartmentLabel(key.keys[compartmentLabelIndex]);

		}
		if (regionLabelIndex >= 0) {
			builder.setRegionLabel(key.keys[regionLabelIndex]);
		}
		if (groupLabelIndex >= 0) {
			builder.setGroupLabel(key.keys[groupLabelIndex]);
		}

		for (PersonPropertyId personPropertyId : personPropertyLabelIndexes.keySet()) {
			Integer personPropertyIndex = personPropertyLabelIndexes.get(personPropertyId);
			if (personPropertyIndex >= 0) {
				builder.setPropertyLabel(personPropertyId, key.keys[personPropertyIndex]);
			}
		}

		for (ResourceId resourceId : resourceLabelIndexes.keySet()) {
			Integer resourceIndex = resourceLabelIndexes.get(resourceId);
			if (resourceIndex >= 0) {
				builder.setResourceLabel(resourceId, key.keys[resourceIndex]);

			}
		}
		return builder.build();
	}

	/**
	 * Precondition: Person must exist
	 *
	 */
	@Override
	public void handleRemovePerson(long transactionId, PersonId personId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		boolean removed = removePerson(personId);
		if (removed) {
			observationManager.handlePartitionPersonRemoval(identifierKey, personId);
		}
	}

	private Key getKeyForPerson(PersonId personId) {
		if (personId == null) {
			return null;
		}
		if (personToKeyMap.size() <= personId.getValue()) {
			return null;
		}
		Key key = personToKeyMap.get(personId.getValue());

		return key;
	}

	@Override
	public boolean contains(PersonId personId) {
		return getKeyForPerson(personId) != null;
	}

	private boolean removePerson(PersonId personId) {
		Key key = getKeyForPerson(personId);
		if (key == null) {
			return false;
		}
		personToKeyMap.set(personId.getValue(), null);
		PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		boolean removed = peopleContainer.remove(personId);
		if (removed) {
			if (peopleContainer.size() == 0) {
				keyToPeopleMap.remove(key);
				keyMap.remove(key);
				labelSetInfoMap.remove(key);
			}
			for (int i = 0; i < keySize; i++) {
				LabelManager labelManager = labelManagers[i];
				labelManager.removeLabel(key.keys[i]);
			}
			personCount--;
		}
		return removed;

	}

	@Override
	public void handleRegionChange(long transactionId, PersonId personId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		if (!evaluate(personId)) {
			return;
		}
		if (regionLabelIndex < 0) {
			return;
		}
		Key currentKey = getKeyForPerson(personId);
		if (currentKey == null) {
			return;
		}

		// get the current label
		Object currentRegionLabel = currentKey.keys[regionLabelIndex];

		// get the new label
		Object newRegionLabel = partition.getRegionPartitionFunction().get().apply(environment.getPersonRegion(personId));

		if (newRegionLabel == null) {
			throwModelException(SimulationErrorType.NULL_REGION_LABEL,environment.getPersonRegion(personId));
		}

		// if the label did not change there is nothing to do
		if (currentRegionLabel.equals(newRegionLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[regionLabelIndex];
		labelManager.removeLabel(currentRegionLabel);
		labelManager.addLabel(newRegionLabel);

		// build the new identifierKey from the person
		Key newKey = buildKey(currentKey, regionLabelIndex, newRegionLabel);

		move(currentKey, newKey, personId);
	}

	@Override
	public void handlePersonPropertyChange(long transactionId, PersonId personId, PersonPropertyId personPropertyId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		if (!evaluate(personId)) {
			return;
		}
		int personPropertyLabelIndex = personPropertyLabelIndexes.get(personPropertyId);
		if (personPropertyLabelIndex < 0) {
			return;
		}
		Key currentKey = getKeyForPerson(personId);
		if (currentKey == null) {
			return;
		}

		// get the current label
		Object currentPropertyLabel = currentKey.keys[personPropertyLabelIndex];

		// get the new label
		Object newPropertyLabel = partition.getPersonPropertyPartitionFunction(personPropertyId).get().apply(environment.getPersonPropertyValue(personId, personPropertyId));

		if (newPropertyLabel == null) {
			throwModelException(SimulationErrorType.NULL_PROPERTY_LABEL,personPropertyId+"="+environment.getPersonPropertyValue(personId, personPropertyId));
		}

		// if the label did not change there is nothing to do
		if (currentPropertyLabel.equals(newPropertyLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[personPropertyLabelIndex];
		labelManager.removeLabel(currentPropertyLabel);
		labelManager.addLabel(newPropertyLabel);

		// build the new identifierKey from the person
		Key newKey = buildKey(currentKey, personPropertyLabelIndex, newPropertyLabel);

		move(currentKey, newKey, personId);
	}

	@Override
	public void handlePersonResourceChange(long transactionId, PersonId personId, ResourceId resourceId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		if (!evaluate(personId)) {
			return;
		}
		int resourceLabelIndex = resourceLabelIndexes.get(resourceId);
		if (resourceLabelIndex < 0) {
			return;
		}
		Key currentKey = getKeyForPerson(personId);
		if (currentKey == null) {
			return;
		}

		// get the current label
		Object currentResourceLabel = currentKey.keys[resourceLabelIndex];

		// get the new label
		Object newResourceLabel = partition.getPersonResourcePartitionFunction(resourceId).get().apply(environment.getPersonResourceLevel(personId, resourceId));

		if (newResourceLabel == null) {
			throwModelException(SimulationErrorType.NULL_RESOURCE_LABEL, resourceId+"="+environment.getPersonResourceLevel(personId, resourceId));
		}

		// if the label did not change there is nothing to do
		if (currentResourceLabel.equals(newResourceLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[resourceLabelIndex];
		labelManager.removeLabel(currentResourceLabel);
		labelManager.addLabel(newResourceLabel);

		// build the new identifierKey from the person
		Key newKey = buildKey(currentKey, resourceLabelIndex, newResourceLabel);

		move(currentKey, newKey, personId);
	}

	@Override
	public void handleCompartmentChange(long transactionId, PersonId personId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		if (!evaluate(personId)) {
			return;
		}
		if (compartmentLabelIndex < 0) {
			return;
		}
		Key currentKey = getKeyForPerson(personId);
		if (currentKey == null) {
			return;
		}

		// get the current label
		Object currentCompartmentLabel = currentKey.keys[compartmentLabelIndex];

		// get the new label
		Object newCompartmentLabel = partition.getCompartmentPartitionFunction().get().apply(environment.getPersonCompartment(personId));

		if (newCompartmentLabel == null) {
			throwModelException(SimulationErrorType.NULL_COMPARTMENT_LABEL, environment.getPersonCompartment(personId));
		}

		// if the label did not change there is nothing to do
		if (currentCompartmentLabel.equals(newCompartmentLabel)) {
			return;
		}
		LabelManager labelManager = labelManagers[compartmentLabelIndex];
		labelManager.removeLabel(currentCompartmentLabel);
		labelManager.addLabel(newCompartmentLabel);

		// build the new identifierKey from the person
		Key newKey = buildKey(currentKey, compartmentLabelIndex, newCompartmentLabel);

		move(currentKey, newKey, personId);
	}

	@Override
	public void handleGroupMembershipChange(long transactionId, PersonId personId) {
		if (!acceptTransactionId(transactionId)) {
			return;
		}
		if (groupLabelIndex < 0) {
			return;
		}
		if (!evaluate(personId)) {
			return;
		}
		Key currentKey = getKeyForPerson(personId);
		if (currentKey == null) {
			return;
		}

		// get the current label
		Object currentGroupLabel = currentKey.keys[groupLabelIndex];

		// get the new label
		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : environment.getGroupTypeIds()) {
			int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			builder.setCount(groupTypeId, count);
		}

		GroupTypeCountMap groupTypeCountMap = builder.build();

		Object newGroupLabel = partition.getGroupPartitionFunction().get().apply(groupTypeCountMap);

		if (newGroupLabel == null) {
			throwModelException(SimulationErrorType.NULL_GROUP_LABEL, groupTypeCountMap);
		}

		// if the label did not change there is nothing to do
		if (currentGroupLabel.equals(newGroupLabel)) {
			return;
		}

		LabelManager labelManager = labelManagers[groupLabelIndex];
		labelManager.removeLabel(currentGroupLabel);
		labelManager.addLabel(newGroupLabel);

		// build the new identifierKey from the person
		Key newKey = buildKey(currentKey, groupLabelIndex, newGroupLabel);

		move(currentKey, newKey, personId);
	}

	private void move(Key currentKey, Key newKey, PersonId personId) {
		Key cleanedNewKey = keyMap.get(newKey);
		if (cleanedNewKey == null) {
			cleanedNewKey = newKey;
			keyMap.put(cleanedNewKey, cleanedNewKey);
			keyToPeopleMap.put(cleanedNewKey, new BasePeopleContainer(context));
			LabelSet labelSet = getLabelSet(cleanedNewKey);
			labelSetInfoMap.put(cleanedNewKey, labelSet);
		}

		PeopleContainer peopleContainer = keyToPeopleMap.get(currentKey);
		peopleContainer.remove(personId);
		if (peopleContainer.size() == 0) {
			keyToPeopleMap.remove(currentKey);
			keyMap.remove(currentKey);
			labelSetInfoMap.remove(currentKey);
		}
		keyToPeopleMap.get(cleanedNewKey).unsafeAdd(personId);
		personToKeyMap.set(personId.getValue(), cleanedNewKey);
	}

	@Override
	public boolean validateLabelSetInfo(LabelSet labelSet) {
		boolean b1 = !partition.getRegionPartitionFunction().isPresent();
		boolean b2 = labelSet.getRegionLabel().isPresent();
		if (b1 && b2) {
			return false;
		}

		b1 = !partition.getCompartmentPartitionFunction().isPresent();
		b2 = labelSet.getCompartmentLabel().isPresent();
		if (b1 && b2) {
			return false;
		}

		b1 = !partition.getGroupPartitionFunction().isPresent();
		b2 = labelSet.getGroupLabel().isPresent();
		if (b1 && b2) {
			return false;
		}

		Set<PersonPropertyId> allowedPersonPropertyIds = partition.getPersonPropertyIds();
		for (PersonPropertyId personPropertyId : labelSet.getPersonPropertyIds()) {
			if (!allowedPersonPropertyIds.contains(personPropertyId)) {
				return false;
			}
		}

		Set<ResourceId> allowedPersonResourceIds = partition.getPersonResourceIds();
		for (ResourceId resourceId : labelSet.getPersonResourceIds()) {
			if (!allowedPersonResourceIds.contains(resourceId)) {
				return false;
			}
		}

		return true;
	}

	private static interface KeyIterator extends Iterator<Key> {
		public int size();
	}

	private class FullKeyIterator implements KeyIterator {

		private Iterator<Key> iterator = keyToPeopleMap.keySet().iterator();

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Key next() {
			return iterator.next();
		}

		@Override
		public int size() {
			return keyToPeopleMap.keySet().size();
		}

	}

	private class PartialKeyIterator implements KeyIterator {
		private final Tuplator tuplator;
		private int index;
		private Key baseKey;
		private Key nextKey;
		int[] tuple;
		int dimensionCount;
		int[] keyIndexes;

		public PartialKeyIterator(Key partialKey) {

			dimensionCount = 0;
			for (int i = 0; i < keySize; i++) {
				if (partialKey.keys[i] == null) {
					dimensionCount++;
				}
			}
			tuple = new int[dimensionCount];
			keyIndexes = new int[dimensionCount];
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
			tuplator = builder.build();

			baseKey = new Key(partialKey);
			calculateNextKey();
		}

		@Override
		public boolean hasNext() {
			return nextKey != null;
		}

		private void calculateNextKey() {
			if (index >= tuplator.size()) {
				nextKey = null;
			} else {
				while (index < tuplator.size()) {
					tuplator.getTuple(index++, tuple);
					for (int j = 0; j < dimensionCount; j++) {
						LabelManager labelManager = labelManagers[keyIndexes[j]];
						baseKey.keys[keyIndexes[j]] = labelManager.getLabel(tuple[j]);
					}
					nextKey = keyMap.get(baseKey);
					if (nextKey != null) {
						break;
					}

				}
			}
		}

		@Override
		public Key next() {
			if (nextKey == null) {
				throw new NoSuchElementException();
			}
			Key result = nextKey;
			calculateNextKey();
			return result;
		}

		@Override
		public int size() {
			return tuplator.size();
		}

	}

	private PersonId getRandomPersonId(Key key, RandomGenerator randomGenerator, final PersonId excludedPersonId) {

		PeopleContainer peopleContainer = keyToPeopleMap.get(key);

		if (peopleContainer == null) {
			return null;
		}

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
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return result;
	}

	@Override
	public int getPeopleCount() {
		return personCount;
		// int result = 0;
		// for (PeopleContainer peopleContainer : keyToPeopleMap.values()) {
		// result += peopleContainer.size();
		// }
		// return result;
	}

	@Override
	public int getPeopleCount(LabelSet labelSet) {
		if (isEmpty()) {
			return 0;
		}
		Key key = getKey(labelSet);

		if (key.isPartialKey()) {
			PartialKeyIterator partialKeyIterator = new PartialKeyIterator(key);
			int result = 0;
			while (partialKeyIterator.hasNext()) {
				Key fullKey = partialKeyIterator.next();
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

	private Key getKey(LabelSet labelSet) {
		Key key = new Key(keySize);
		int index = 0;
		if (partition.getRegionPartitionFunction().isPresent()) {
			Object regionLabel = labelSet.getRegionLabel().orElse(null);
			key.keys[index++] = regionLabel;
		}

		if (partition.getCompartmentPartitionFunction().isPresent()) {
			Object compartmentLabel = labelSet.getCompartmentLabel().orElse(null);
			key.keys[index++] = compartmentLabel;
		}

		for (PersonPropertyId personPropertyId : partition.getPersonPropertyIds()) {
			key.keys[index++] = labelSet.getPersonPropertyLabel(personPropertyId).orElse(null);
		}

		for (ResourceId resourceId : partition.getPersonResourceIds()) {
			key.keys[index++] = labelSet.getPersonResourceLabel(resourceId).orElse(null);
		}

		if (partition.getGroupPartitionFunction().isPresent()) {
			Object groupLabel = labelSet.getGroupLabel().orElse(null);
			key.keys[index++] = groupLabel;
		}
		return key;
	}

	@Override
	public boolean contains(PersonId personId, LabelSet labelSet) {
		Key key = getKeyForPerson(personId);
		if (key == null) {
			return false;
		}
		LabelSet fullLabelSet = labelSetInfoMap.get(key);
		return fullLabelSet.isSubsetMatch(labelSet);
	}

	/**
	 * 
	 * 
	 * Precondition: the population partition query must match the population
	 * partition definition
	 * 
	 */
	@Override
	public List<PersonId> getPeople(LabelSet labelSet) {
		if (isEmpty()) {
			return new ArrayList<>();
		}

		Key key = getKey(labelSet);

		if (key.isPartialKey()) {
			PartialKeyIterator partialKeyIterator = new PartialKeyIterator(key);

			List<PersonId> result = new ArrayList<>();
			while (partialKeyIterator.hasNext()) {
				Key fullKey = partialKeyIterator.next();
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

	@Override
	public List<PersonId> getPeople() {
		List<PersonId> result = null;
		for (PeopleContainer peopleContainer : keyToPeopleMap.values()) {
			if (result == null) {
				result = peopleContainer.getPeople();
			} else {
				result.addAll(peopleContainer.getPeople());
			}
		}
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	@Override
	public void init() {
		// FilterPopulationMatcher.getMatchingPeople(filterInfo, environment)//
		// .forEach(personId -> addPerson(personId));//
		//

		PersonIdManager personIdManager = context.getPersonIdManager();
		int personIdLimit = personIdManager.getPersonIdLimit();
		for (int i = 0; i < personIdLimit; i++) {
			if (personIdManager.personIndexExists(i)) {
				PersonId personId = personIdManager.getBoxedPersonId(i);
				evaluate(personId);
			}
		}
	}

	// Guard for both weights array and weightedKeys array
	private boolean weightsAreLocked;

	private double[] weights;

	private Key[] weightedKeys;

	private void aquireWeightsLock() {
		if (weightsAreLocked) {
			throw new RuntimeException("weights arrray is locked");
		}
		weightsAreLocked = true;
	}

	private void releaseWeightsLock() {
		if (!weightsAreLocked) {
			throw new RuntimeException("weights array is not locked");
		}
		weightsAreLocked = false;
	}

	/*
	 * Allocates the weights array to the given size or 50% larger than the
	 * current size, whichever is largest. Size must be non-negative
	 */
	private void allocateWeights(final int size) {
		if (weights == null) {
			weights = new double[size];
			weightedKeys = new Key[size];
		}
		if (weights.length < size) {
			int newSize = Math.max(size, weights.length + weights.length / 2);
			weights = new double[newSize];
			weightedKeys = new Key[newSize];
		}
	}

	/*
	 * Returns the index in the weights array that is the first to meet or
	 * exceed the target value. Assumes a strictly increasing set of values for
	 * indices 0 through keyCount. Decreasing values are strictly prohibited.
	 * Consecutive equal values may return an ambiguous result. The target value
	 * must not exceed weights[peopleCount].
	 *
	 */
	private int findTargetIndex(final double targetValue, final int keyCount) {
		int low = 0;
		int high = keyCount - 1;

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

	private KeyIterator getKeyIterator(LabelSet labelSet) {
		if (labelSet == null) {
			return new FullKeyIterator();
		}
		Key key = getKey(labelSet);
		return new PartialKeyIterator(key);
	}

	private double getDefaultWeight(ObservableEnvironment observableEnvironment, LabelSet labelSet) {
		return 1;
	}

	/**
	 * Returns a randomly chosen person identifier from the partition consistent
	 * with the partition sampler info. Note that the sampler must be consistent
	 * with the partition definition used to create this population partition.
	 * No precondition tests will be performed.
	 */
	@Override
	public StochasticPersonSelection samplePartition(final PartitionSampler partitionSampler) {
		if (isEmpty()) {
			return new StochasticPersonSelection(null, false);
		}

		RandomGenerator randomGenerator;
		RandomNumberGeneratorId randomNumberGeneratorId = partitionSampler.getRandomNumberGeneratorId().orElse(null);
		if (randomNumberGeneratorId == null) {
			randomGenerator = stochasticsManager.getRandomGenerator();
		} else {
			randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		}

		PersonId excludedPersonId = partitionSampler.getExcludedPerson().orElse(null);

		LabelSet labelSet = partitionSampler.getLabelSet().orElse(LabelSet.builder().build());

		LabelSetWeightingFunction labelSetWeightingFunction = partitionSampler.getLabelSetWeightingFunction().orElse(this::getDefaultWeight);

		Key selectedKey = null;
		Key keyForExcludedPersonId = getKeyForPerson(excludedPersonId);
		KeyIterator keyIterator;

		aquireWeightsLock();
		try {
			keyIterator = getKeyIterator(labelSet);
			allocateWeights(keyIterator.size());
			/*
			 * Initialize the sum of the weights to zero and set the index in
			 * the weights and weightedKeys to zero.
			 */

			double sum = 0;
			int weightsLength = 0;
			while (keyIterator.hasNext()) {
				Key fullKey = keyIterator.next();
				LabelSet fullLableSet = labelSetInfoMap.get(fullKey);
				PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				double weight = labelSetWeightingFunction.getWeight(observableEnvironment, fullLableSet);
				if (fullKey != keyForExcludedPersonId) {
					weight *= peopleContainer.size();
				} else {
					weight *= (peopleContainer.size() - 1);
				}

				if (!Double.isFinite(weight) || (weight < 0)) {
					return new StochasticPersonSelection(null, true);
				}
				/*
				 * Keys having a zero weight are rejected for selection
				 */
				if (weight > 0) {
					sum += weight;
					weights[weightsLength] = sum;
					weightedKeys[weightsLength] = fullKey;
					weightsLength++;
				}

			}

			/*
			 * If at least one identifierKey was accepted for selection, then we
			 * attempt a random selection.
			 */
			if (weightsLength > 0) {
				/*
				 * Although the individual weights may have been finite, if the
				 * sum of those weights is not finite no legitimate selection
				 * can be made
				 */
				if (!Double.isFinite(sum)) {
					return new StochasticPersonSelection(null, true);
				}

				final double targetValue = randomGenerator.nextDouble() * sum;
				final int targetIndex = findTargetIndex(targetValue, weightsLength);
				selectedKey = weightedKeys[targetIndex];
			}
		} finally {
			releaseWeightsLock();
		}

		if (selectedKey == null) {
			return new StochasticPersonSelection(null, false);
		}

		PersonId selectedPerson = getRandomPersonId(selectedKey, randomGenerator, excludedPersonId);
		return new StochasticPersonSelection(selectedPerson, false);
	}

	@Override
	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	@Override
	public Partition getPartition() {
		return partition;
	}

	private void throwModelException(final SimulationErrorType simulationErrorType, final Object details) {
		final StringBuilder sb = new StringBuilder();

		sb.append("Active Component");
		sb.append("[");
		sb.append(context.getComponentManager().getFocalComponentType());
		sb.append(",");
		sb.append(context.getComponentManager().toString());
		sb.append("]");
		sb.append(simulationErrorType.getDescription());

		if (details != null) {
			sb.append(": ");
			sb.append(details);
		}
		String errorDescription = sb.toString();

		throw new ModelException(simulationErrorType, errorDescription);
	}

}
