package gcm.manual.pp;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.simulation.Context;
import gcm.simulation.Environment;
import gcm.util.MultiKey;
import gcm.util.MultiKey.MultiKeyBuilder;

public final class PopulationPartition {

	private Map<MultiKey, Set<PersonId>> keyMap = new LinkedHashMap<>();

	private MultiKey[] personMap;

	private final PopulationPartitionDefinition populationPartitionDefinition;

	private final Environment environment;

	public PopulationPartition(final Context context,
			final PopulationPartitionDefinition populationPartitionDefinition) {

		personMap = new MultiKey[context.getPersonIdManager().getPersonIdLimit()];
		this.populationPartitionDefinition = populationPartitionDefinition;
		this.environment = context.getEnvironment();
	}

	private int getRegionLabelIndex() {		
		return 0;
	}

	private int getCompartmentLabelIndex() {
		return 1;
	}

	public void handleRegionChange(PersonId personId) {
		int regionLabelIndex = getRegionLabelIndex();
		if(regionLabelIndex < 0) {
			return;
		}
		MultiKey currentKey = personMap[personId.getValue()];
		
		// get the current label
		Object currentCompartmentLabel = currentKey.getKey(regionLabelIndex);
		
		// get the new label
		Object newCompartmentLabel = populationPartitionDefinition.getRegionPartitionFunction()
				.apply(environment.getPersonCompartment(personId));
		if (newCompartmentLabel == null) {
			throw new RuntimeException("change to model exception");
		}

		// if the label did not change there is nothing to do
		if (currentCompartmentLabel.equals(newCompartmentLabel)) {
			return;
		}

		// build the new key from the person
		MultiKey newKey = null;

		// determine the index where the old value should be
		// build the old key from the person and the old value
		MultiKey oldKey = null;
		move(oldKey, newKey, personId);
	}

	private void move(MultiKey oldKey, MultiKey newKey, PersonId personId) {

		Set<PersonId> set = keyMap.get(oldKey);
		if (set != null) {
			set.remove(personId);
		}

		set = keyMap.get(newKey);
		if (set == null) {
			set = new LinkedHashSet<>();
			keyMap.put(newKey, set);
		}
		set.add(personId);
	}

	public Set<PersonId> getPeople(PopulationPartitionQuery populationPartitionQuery) {

		MultiKeyBuilder multiKeyBuilder = new MultiKeyBuilder();

		Object compartmentLabel = populationPartitionQuery.getCompartmentLabel();
		if (compartmentLabel != null) {
			multiKeyBuilder.addKey(compartmentLabel);
		}
		Object regionLabel = populationPartitionQuery.getRegionLabel();
		if (regionLabel != null) {
			multiKeyBuilder.addKey(regionLabel);
		}

		MultiKey multiKey = multiKeyBuilder.build();
		Set<PersonId> result = keyMap.get(multiKey);
		if (result == null) {
			result = new LinkedHashSet<>();
		}

		return result;
	}

}
