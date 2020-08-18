package gcm.manual.pp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.simulation.Context;
import gcm.simulation.Environment;

public final class PopulationPartition {



	private final int keySize;
	
	private int regionLabelIndex = -1;
	
	private int compartmentLabelIndex = -1;
	
	private Map<PersonPropertyId,Integer> personPropertyLabelIndexes = new LinkedHashMap<>();

	private Map<Object[], Set<PersonId>> keyToPeopleMap = new LinkedHashMap<>();

	private List<Object[]> personToKeyMap;

	private Map<Object[], Object[]> keyMap;

	private final PopulationPartitionDefinition populationPartitionDefinition;

	private final Environment environment;

	public PopulationPartition(final Context context,
			final PopulationPartitionDefinition populationPartitionDefinition) {
		personToKeyMap = new ArrayList<>(context.getPersonIdManager().getPersonIdLimit());
		this.populationPartitionDefinition = populationPartitionDefinition;
		this.environment = context.getEnvironment();

		int size = 0;

		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			regionLabelIndex = size++;
		}

		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			compartmentLabelIndex = size++;
			
		}
		for(PersonPropertyId personPropertyId : environment.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, -1);
		}
		for(PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			personPropertyLabelIndexes.put(personPropertyId, size++);
		}

		keySize = size;
	}

	

	private Object[] buildKey(Object[] key, int index, Object newLabel) {
		Object[] result = Arrays.copyOf(key, key.length);
		result[index] = newLabel;
		return result;
	}

	public void handleAddPerson(PersonId personId) {
		while (personId.getValue() >= personToKeyMap.size()) {
			personToKeyMap.add(null);
		}
		int index = 0;
		Object[] key = new Object[keySize];
		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			key[index++] = populationPartitionDefinition.getRegionPartitionFunction()
					.apply(environment.getPersonRegion(personId));
		}
		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			key[index++] = populationPartitionDefinition.getCompartmentPartitionFunction()
					.apply(environment.getPersonCompartment(personId));
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			key[index++] = populationPartitionDefinition.getPersonPropertyPartitionFunction(personPropertyId)
					.apply(environment.getPersonPropertyValue(personId, personPropertyId));
		}

		Object[] cleanedKey = keyMap.get(key);
		if (cleanedKey == null) {
			cleanedKey = key;
			keyMap.put(cleanedKey, cleanedKey);
			keyToPeopleMap.put(cleanedKey, new LinkedHashSet<>());
		}
		personToKeyMap.set(personId.getValue(), cleanedKey);
		keyToPeopleMap.get(cleanedKey).add(personId);
	}

	/**
	 * Precondition: Person must exist
	 *
	 */
	public void handleRemovePerson(PersonId personId) {
		Object[] key = personToKeyMap.get(personId.getValue());
		personToKeyMap.set(personId.getValue(), null);
		Set<PersonId> people = keyToPeopleMap.get(key);
		people.remove(personId);
		if (people.isEmpty()) {
			keyToPeopleMap.remove(key);
			keyMap.remove(key);
		}
	}

	public void handleRegionChange(PersonId personId) {
		
		if (regionLabelIndex < 0) {
			return;
		}
		Object[] currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentRegionLabel = currentKey[regionLabelIndex];

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
		Object[] newKey = buildKey(currentKey, regionLabelIndex, newRegionLabel);

		move(currentKey, newKey, personId);
	}

	public void handlePersonPropertyChange(PersonId personId, PersonPropertyId personPropertyId) {
		int personPropertyLabelIndex = personPropertyLabelIndexes.get(personPropertyId);
		if (personPropertyLabelIndex < 0) {
			return;
		}
		Object[] currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentPropertyLabel = currentKey[personPropertyLabelIndex];

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
		Object[] newKey = buildKey(currentKey, personPropertyLabelIndex, newPropertyLabel);

		move(currentKey, newKey, personId);
	}

	public void handleCompartmentChange(PersonId personId) {
		
		if (compartmentLabelIndex < 0) {
			return;
		}
		Object[] currentKey = personToKeyMap.get(personId.getValue());

		// get the current label
		Object currentCompartmentLabel = currentKey[compartmentLabelIndex];

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
		Object[] newKey = buildKey(currentKey, compartmentLabelIndex, newCompartmentLabel);

		move(currentKey, newKey, personId);
	}

	private void move(Object[] currentKey, Object[] newKey, PersonId personId) {
		Object[] cleanedNewKey = keyMap.get(newKey);
		if (cleanedNewKey == null) {
			cleanedNewKey = newKey;
			keyMap.put(cleanedNewKey, cleanedNewKey);
			keyToPeopleMap.put(cleanedNewKey, new LinkedHashSet<>());
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

		return true;
	}

	/**
	 * 
	 * 
	 * Precondition: the population partition query must match the population
	 * partition definition
	 * 
	 */
	public Set<PersonId> getPeople(PopulationPartitionQuery populationPartitionQuery) {

		Object[] key = new Object[keySize];
		int index = 0;
		if (populationPartitionDefinition.getRegionPartitionFunction() != null) {
			Object regionLabel = populationPartitionQuery.getRegionLabel();
			key[index++] = regionLabel;
		}

		if (populationPartitionDefinition.getCompartmentPartitionFunction() != null) {
			Object compartmentLabel = populationPartitionQuery.getCompartmentLabel();
			key[index++] = compartmentLabel;
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			key[index++] = populationPartitionQuery.getPersonPropertyLabel(personPropertyId);
		}

		Set<PersonId> result = keyToPeopleMap.get(key);
		if (result == null) {
			result = new LinkedHashSet<>();
		}
		return result;
	}

}
