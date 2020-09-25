package gcm.simulation.partition;

import java.util.Optional;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED)
public class PartitionSampler {

	private final Scaffold scaffold;

	private static class Scaffold {

		private PersonId excludedPersonId;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private LabelSet labelSet;

		private LabelSetWeightingFunction labelSetWeightingFunction;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		private Scaffold scaffold = new Scaffold();
		private Builder() {}
		public PartitionSampler build() {
			try {
				return new PartitionSampler(scaffold);				
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
		
		public Builder setLabelSet(LabelSet labelSet) {
			scaffold.labelSet = labelSet;
			return this;
		}
		
		public Builder setLabelSetWeightingFunction(LabelSetWeightingFunction labelSetWeightingFunction) {
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

	public Optional<LabelSet> getLabelSet() {
		return Optional.ofNullable(scaffold.labelSet);
	}

	public Optional<LabelSetWeightingFunction> getLabelSetWeightingFunction() {
		return Optional.ofNullable(scaffold.labelSetWeightingFunction);
	}
	
	private PartitionSampler(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
	
	
}
