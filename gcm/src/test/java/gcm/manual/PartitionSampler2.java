package gcm.manual;

import java.util.Optional;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import net.jcip.annotations.Immutable;

@Immutable
public final class PartitionSampler2 {
	private final Scaffold scaffold;

	private static class Scaffold {

		private PersonId excludedPersonId;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private LabelSet2 labelSet;

		private LabelSetWeightingFunction2 labelSetWeightingFunction;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		private Scaffold scaffold = new Scaffold();
		private Builder() {}
		public PartitionSampler2 build() {
			try {
				return new PartitionSampler2(scaffold);				
			}finally {
				scaffold = new Scaffold();
			}
		}
		
		public Builder setExcludedPerson(PersonId excludedPersonId) {
			scaffold.excludedPersonId = excludedPersonId;
			return this;
		}
		
		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			scaffold.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}
		
		public Builder setLabelSet(LabelSet2 labelSet) {
			scaffold.labelSet = labelSet;
			return this;
		}
		
		public Builder setLabelSetWeightingFunction(LabelSetWeightingFunction2 labelSetWeightingFunction) {
			scaffold.labelSetWeightingFunction = labelSetWeightingFunction;
			return this;
		}

	}
	

	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(scaffold.excludedPersonId);
	}

	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(scaffold.randomNumberGeneratorId);
	}

	public Optional<LabelSet2> getLabelSet() {
		return Optional.ofNullable(scaffold.labelSet);
	}

	public Optional<LabelSetWeightingFunction2> getLabelSetWeightingFunction() {
		return Optional.ofNullable(scaffold.labelSetWeightingFunction);
	}
	
	private PartitionSampler2(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
}
