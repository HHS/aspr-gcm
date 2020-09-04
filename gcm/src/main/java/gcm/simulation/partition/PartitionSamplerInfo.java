package gcm.simulation.partition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.simulation.partition.PartitionSampler.EmptyPartitionSampler;
import gcm.simulation.partition.PartitionSampler.ExcludedPersonPartitionSampler;
import gcm.simulation.partition.PartitionSampler.LabelSetPartitionSampler;
import gcm.simulation.partition.PartitionSampler.LabelSetWeightingFunctionPartitionSampler;
import gcm.simulation.partition.PartitionSampler.RandomNumberGeneratorIdPartitionSampler;
import gcm.simulation.partition.PartitionSampler.WithPartitionSampler;
import net.jcip.annotations.Immutable;

@Immutable
public final class PartitionSamplerInfo {
	private final Scaffold scaffold = new Scaffold();
		
	private static class Scaffold{

		private PersonId excludedPerson;
		
		private RandomNumberGeneratorId randomNumberGeneratorId;
		
		private LabelSet labelSet;
		
		private LabelSetWeightingFunction labelSetWeightingFunction;
	}
	
	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(scaffold.excludedPerson);
	}

	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(scaffold.randomNumberGeneratorId);
	}

	public Optional<LabelSet> getLabelSet() {
		return Optional.ofNullable(scaffold.labelSet);
	}

	public Optional<LabelSetWeightingFunction> getLabelSetWeightingFunction() {
		return Optional.ofNullable(scaffold.labelSetWeightingFunction);
	}

	private PartitionSamplerInfo(PartitionSampler partitionSampler) {		
		processPartitionSampler(partitionSampler);
	}
	
	public static PartitionSamplerInfo build(PartitionSampler partitionSampler) {
		return new PartitionSamplerInfo(partitionSampler);
	}
	
	private static enum PartitionSampleType {

		EMPTY(EmptyPartitionSampler.class),
		
		WITH(WithPartitionSampler.class),

		EXCLUDED_PERSON(ExcludedPersonPartitionSampler.class),

		RANDOM_GENERATOR(RandomNumberGeneratorIdPartitionSampler.class),

		LABEL_SET(LabelSetPartitionSampler.class),

		LABEL_WEIGHT(LabelSetWeightingFunctionPartitionSampler.class);

		private static Map<Class<? extends PartitionSampler>, PartitionSampleType> map = buildMap();

		private static Map<Class<? extends PartitionSampler>, PartitionSampleType> buildMap() {
			Map<Class<? extends PartitionSampler>, PartitionSampleType> result = new LinkedHashMap<>();
			for (PartitionSampleType partitionSampleType : PartitionSampleType.values()) {
				result.put(partitionSampleType.c, partitionSampleType);
			}
			return result;
		}

		private final Class<? extends PartitionSampler> c;

		private PartitionSampleType(Class<? extends PartitionSampler> c) {
			this.c = c;
		}

		static PartitionSampleType getPartitionSampleType(PartitionSampler partitionSampler) {
			PartitionSampleType result = map.get(partitionSampler.getClass());
			if (result == null) {
				throw new RuntimeException("unrecognized partition type for " + partitionSampler.getClass().getSimpleName());
			}
			return result;
		}
	}
	
	private void processPartitionSampler(PartitionSampler partitionSampler) {
		PartitionSampleType partitionSampleType = PartitionSampleType.getPartitionSampleType(partitionSampler);
		
		switch (partitionSampleType) {
		case EXCLUDED_PERSON:
			ExcludedPersonPartitionSampler excludedPersonPartitionSampler = (ExcludedPersonPartitionSampler)partitionSampler;
			scaffold.excludedPerson = excludedPersonPartitionSampler.personId;
			break;
		case RANDOM_GENERATOR:
			RandomNumberGeneratorIdPartitionSampler randomNumberGeneratorIdPartitionSampler = (RandomNumberGeneratorIdPartitionSampler)partitionSampler;
			scaffold.randomNumberGeneratorId = randomNumberGeneratorIdPartitionSampler.randomNumberGeneratorId;
			break;
		case LABEL_SET:
			LabelSetPartitionSampler labelSetPartitionSampler = (LabelSetPartitionSampler) partitionSampler;
			scaffold.labelSet = labelSetPartitionSampler.labelSet;
			break;
		case LABEL_WEIGHT:
			LabelSetWeightingFunctionPartitionSampler labelSetWeightingFunctionPartitionSampler = (LabelSetWeightingFunctionPartitionSampler)partitionSampler;
			scaffold.labelSetWeightingFunction = labelSetWeightingFunctionPartitionSampler.labelSetWeightingFunction;
			break;		
		case WITH:
			WithPartitionSampler withPartitionSampler = (WithPartitionSampler) partitionSampler;
			processPartitionSampler(withPartitionSampler.a);
			processPartitionSampler(withPartitionSampler.b);
			break;
		case EMPTY:
			break;
		default:
			throw new RuntimeException("unhandled case");
		}
	}
}
