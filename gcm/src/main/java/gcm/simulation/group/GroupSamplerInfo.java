package gcm.simulation.group;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.simulation.group.GroupSampler.EmptyGroupSampler;
import gcm.simulation.group.GroupSampler.ExcludedPersonGroupSampler;
import gcm.simulation.group.GroupSampler.RandomNumberGeneratorIdGroupSampler;
import gcm.simulation.group.GroupSampler.WeightingFunctionGroupSampler;
import gcm.simulation.group.GroupSampler.WithGroupSampler;


public class GroupSamplerInfo {
	private final Scaffold scaffold = new Scaffold();
	
	private static class Scaffold{

		private PersonId excludedPerson;
		
		private RandomNumberGeneratorId randomNumberGeneratorId;
		
		private GroupWeightingFunction weightingFunction;
	}
	
	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(scaffold.excludedPerson);
	}

	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(scaffold.randomNumberGeneratorId);
	}
	
	public Optional<GroupWeightingFunction> getWeightingFunction() {
		return Optional.ofNullable(scaffold.weightingFunction);
	}

	private GroupSamplerInfo(GroupSampler groupSampler) {		
		processGroupSampler(groupSampler);
	}
	
	public static GroupSamplerInfo build(GroupSampler groupSampler) {
		return new GroupSamplerInfo(groupSampler);
	}
	
	private static enum GroupSampleType {

		EMPTY(EmptyGroupSampler.class),
		
		WITH(WithGroupSampler.class),

		EXCLUDED_PERSON(ExcludedPersonGroupSampler.class),

		RANDOM_GENERATOR(RandomNumberGeneratorIdGroupSampler.class),

		WEIGHT(WeightingFunctionGroupSampler.class);

		private static Map<Class<? extends GroupSampler>, GroupSampleType> map = buildMap();

		private static Map<Class<? extends GroupSampler>, GroupSampleType> buildMap() {
			Map<Class<? extends GroupSampler>, GroupSampleType> result = new LinkedHashMap<>();
			for (GroupSampleType goupSampleType : GroupSampleType.values()) {
				result.put(goupSampleType.c, goupSampleType);
			}
			return result;
		}

		private final Class<? extends GroupSampler> c;

		private GroupSampleType(Class<? extends GroupSampler> c) {
			this.c = c;
		}

		static GroupSampleType getGroupSampleType(GroupSampler groupSampler) {
			GroupSampleType result = map.get(groupSampler.getClass());
			if (result == null) {
				throw new RuntimeException("unrecognized group sampler type for " + groupSampler.getClass().getSimpleName());
			}
			return result;
		}
	}
	
	private void processGroupSampler(GroupSampler groupSampler) {
		GroupSampleType groupSampleType = GroupSampleType.getGroupSampleType(groupSampler);
		
		switch (groupSampleType) {
		case EXCLUDED_PERSON:
			ExcludedPersonGroupSampler excludedPersonGroupSampler = (ExcludedPersonGroupSampler)groupSampler;
			scaffold.excludedPerson = excludedPersonGroupSampler.personId;
			break;
		case RANDOM_GENERATOR:
			RandomNumberGeneratorIdGroupSampler randomNumberGeneratorIdGroupSampler = (RandomNumberGeneratorIdGroupSampler)groupSampler;
			scaffold.randomNumberGeneratorId = randomNumberGeneratorIdGroupSampler.randomNumberGeneratorId;
			break;
		
		case WEIGHT:
			WeightingFunctionGroupSampler weightingFunctionGroupSampler = (WeightingFunctionGroupSampler)groupSampler;
			scaffold.weightingFunction = weightingFunctionGroupSampler.groupWeightingFunction;
			break;		
		case WITH:
			WithGroupSampler withGroupSampler = (WithGroupSampler) groupSampler;
			processGroupSampler(withGroupSampler.a);
			processGroupSampler(withGroupSampler.b);
			break;
		case EMPTY:
			break;
		default:
			throw new RuntimeException("unhandled case");
		}
	}

}
