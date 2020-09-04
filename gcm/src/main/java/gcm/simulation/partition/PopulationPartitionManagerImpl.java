package gcm.simulation.partition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.ComponentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.ResourceId;
import gcm.simulation.BaseElement;
import gcm.simulation.Context;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.StochasticsManager;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class PopulationPartitionManagerImpl extends BaseElement implements PopulationPartitionManager {

	private Map<Object, PopulationPartition> populationPartitions = new LinkedHashMap<>();

	private Context context;

	private StochasticsManager stochasticsManager;

	@Override
	public void init(Context context) {
		super.init(context);
		this.context = context;
		this.stochasticsManager = context.getStochasticsManager();

	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		// TODO -- implement
	}

	@Override
	public void addPopulationPartition(ComponentId componentId, Partition partition, Object key) {
		if (componentId == null) {
			throw new RuntimeException("null component id");
		}

		if (partition == null) {
			throw new RuntimeException("null population partition definition");
		}

		if (key == null) {
			throw new RuntimeException("null key");
		}

		if (populationPartitions.containsKey(key)) {
			throw new RuntimeException("duplicate key");
		}

		PopulationPartition populationPartition = new PopulationPartition(context, PartitionInfo.build(partition),
				componentId);

		populationPartitions.put(key, populationPartition);

		populationPartition.init();
	}

	@Override
	public List<PersonId> getPartitionPeople(Object key, LabelSet labelSet) {
		return populationPartitions.get(key).getPeople(LabelSetInfo.build(labelSet));
	}

	@Override
	public int getPartitionSize(Object key, LabelSet labelSet) {
		return populationPartitions.get(key).getPeopleCount(LabelSetInfo.build(labelSet));
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key, LabelSet labelSet,PersonId excludedPersonId) {
		return populationPartitions.get(key).samplePartition(LabelSetInfo.build(labelSet),null,
				stochasticsManager.getRandomGenerator(),excludedPersonId);
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key, LabelSet labelSet,
			RandomNumberGeneratorId randomNumberGeneratorId,PersonId excludedPersonId) {

		return populationPartitions.get(key).samplePartition(LabelSetInfo.build(labelSet),null,
				stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId),excludedPersonId);
	}

	@Override
	public boolean personInPartition(PersonId personId, Object key, LabelSet labelSet) {
		return populationPartitions.get(key).contains(personId, LabelSetInfo.build(labelSet));
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
	public boolean validateLabelSet(Object key, LabelSet labelSet) {
		return populationPartitions.get(key).validateLabelSetInfo(LabelSetInfo.build(labelSet));
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction,PersonId excludedPersonId) {
		return populationPartitions.get(key).samplePartition(null,labelSetWeightingFunction,stochasticsManager.getRandomGenerator(),excludedPersonId);
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction,RandomNumberGeneratorId randomNumberGeneratorId,PersonId excludedPersonId) {
		return populationPartitions.get(key).samplePartition(null,labelSetWeightingFunction,stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId),excludedPersonId);
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key, PartitionSampler partitionSampler) {
		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(partitionSampler);
		
		RandomGenerator randomGenerator;
		RandomNumberGeneratorId randomNumberGeneratorId = partitionSamplerInfo.getRandomNumberGeneratorId().orElse(null);
		if(randomNumberGeneratorId == null) {
			randomGenerator = stochasticsManager.getRandomGenerator();
		}else {
			randomGenerator = stochasticsManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		}
		
		LabelSet labelSet = partitionSamplerInfo.getLabelSet().orElse(null);
		
		LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
		
		LabelSetWeightingFunction labelSetWeightingFunction = partitionSamplerInfo.getLabelSetWeightingFunction().orElse(null);
		
		PersonId excludedPersonId = partitionSamplerInfo.getExcludedPerson().orElse(null);
		
		return populationPartitions.get(key).samplePartition(labelSetInfo, labelSetWeightingFunction, randomGenerator, excludedPersonId);
		
		}
}
