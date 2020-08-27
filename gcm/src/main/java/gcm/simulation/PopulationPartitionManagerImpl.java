package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.ResourceId;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class PopulationPartitionManagerImpl extends BaseElement implements PopulationPartitionManager {

	private Map<Object, PopulationPartition> populationPartitions = new LinkedHashMap<>();

	private Context context;

	@Override
	public void init(Context context) {
		super.init(context);
		this.context = context;
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		// TODO -- implement
	}

	@Override
	public void addPopulationPartition(ComponentId componentId, PopulationPartitionDefinition populationPartitionDefinition,
			Object key) {
		if (componentId == null) {
			throw new RuntimeException("null component id");
		}

		if (populationPartitionDefinition == null) {
			throw new RuntimeException("null population partition definition");
		}

		if (key == null) {
			throw new RuntimeException("null key");
		}

		if (populationPartitions.containsKey(key)) {
			throw new RuntimeException("duplicate key");
		}

		PopulationPartition populationPartition = new PopulationPartition(context, populationPartitionDefinition,
				componentId);

		populationPartitions.put(key, populationPartition);
		
		populationPartition.init();
	}

	@Override
	public List<PersonId> getPartitionPeople(Object key, LabelSet labelSet) {
		return populationPartitions.get(key).getPeople(labelSet);
	}

	@Override
	public int getPartitionSize(Object key, LabelSet labelSet) {
		return populationPartitions.get(key).getPeopleCount(labelSet);
	}

	@Override
	public PersonId getRandomPartitionedPerson(PersonId excludedPersonId, Object key,
			LabelSet labelSet) {
		return populationPartitions.get(key).getRandomPersonId(excludedPersonId, labelSet);
	}

	@Override
	public PersonId getRandomPartionedPersonFromGenerator(PersonId excludedPersonId, Object key,
			LabelSet labelSet, RandomNumberGeneratorId randomNumberGeneratorId) {
		return populationPartitions.get(key).getRandomPersonFromGenerator(excludedPersonId, labelSet,
				randomNumberGeneratorId);
	}

	@Override
	public boolean personInPartition(PersonId personId, Object key, LabelSet labelSet) {
		return populationPartitions.get(key).contains(personId, labelSet);
	}

	@Override
	public void handlePersonAddition(PersonId personId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handleAddPerson(personId);
		}
	}

	@Override
	public void handlePersonCompartmentChange(PersonId personId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handleCompartmentChange(personId);
		}
	}
	
	@Override
	public void handleGroupMembershipChange(PersonId personId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handleGroupMembershipChange(personId);
		}
	}

	@Override
	public void handlePersonRegionChange(PersonId personId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handleRegionChange(personId);
		}
	}

	@Override
	public void handlePersonRemoval(PersonId personId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handleRemovePerson(personId);
		}
	}

	@Override
	public void handlePersonResourceLevelChange(PersonId personId, ResourceId resourceId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handlePersonResourceChange(personId, resourceId);
		}
	}

	@Override
	public boolean populationPartitionExists(Object key) {
		return populationPartitions.containsKey(key);
	}

	@Override
	public void removePartition(Object key) {
		populationPartitions.remove(key);
	}

	@Override
	public void handlePersonPropertyValueChange(PersonId personId, PersonPropertyId personPropertyId) {
		for (PopulationPartition populationPartition : populationPartitions.values()) {
			populationPartition.handlePersonPropertyChange(personId, personPropertyId);
		}
	}

	@Override
	public ComponentId getOwningComponent(Object key) {
		return populationPartitions.get(key).getOwningComponentId();
	}
	
	@Override
	public boolean validateLabelSet(Object key,LabelSet labelSet) {
		return populationPartitions.get(key).validateLabelSet(labelSet);
	}

}
