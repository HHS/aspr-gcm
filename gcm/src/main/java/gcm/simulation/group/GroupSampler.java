package gcm.simulation.group;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = GroupSamplerInfo.class)
public class GroupSampler {

	
	private GroupSampler() {

	}

	static class EmptyGroupSampler extends GroupSampler {
	}

	public final static GroupSampler create() {
		return new EmptyGroupSampler();
	}

	static class ExcludedPersonGroupSampler extends GroupSampler {
		final PersonId personId;

		ExcludedPersonGroupSampler(PersonId personId) {
			this.personId = personId;
		}
	}

	public final GroupSampler excludePerson(PersonId personId) {
		if (personId == null) {
			throw new RuntimeException("null person id");
		}
		return new WithGroupSampler(this, new ExcludedPersonGroupSampler(personId));
	}

	static class WeightingFunctionGroupSampler extends GroupSampler {
		final GroupWeightingFunction groupWeightingFunction;

		WeightingFunctionGroupSampler(GroupWeightingFunction groupWeightingFunction) {
			this.groupWeightingFunction = groupWeightingFunction;
		}
	}

	public final GroupSampler weight(GroupWeightingFunction groupWeightingFunction) {
		if (groupWeightingFunction == null) {
			throw new RuntimeException("null groupWeightingFunction");
		}
		return new WithGroupSampler(this, new WeightingFunctionGroupSampler(groupWeightingFunction));
	}

	static class RandomNumberGeneratorIdGroupSampler extends GroupSampler {
		final RandomNumberGeneratorId randomNumberGeneratorId;

		RandomNumberGeneratorIdGroupSampler(RandomNumberGeneratorId randomNumberGeneratorId) {
			this.randomNumberGeneratorId = randomNumberGeneratorId;
		}
	}

	public final GroupSampler generator(RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			throw new RuntimeException("null randomNumberGeneratorId");
		}
		return new WithGroupSampler(this, new RandomNumberGeneratorIdGroupSampler(randomNumberGeneratorId));
	}

	static class WithGroupSampler extends GroupSampler {
		final GroupSampler a;
		final GroupSampler b;

		public WithGroupSampler(final GroupSampler a, final GroupSampler b) {
			this.a = a;
			this.b = b;
		}
	}

	public final GroupSampler with(GroupSampler other) {
		if (other == null) {
			throw new RuntimeException("null partition sampler");
		}
		return new WithGroupSampler(this, other);
	}
}
