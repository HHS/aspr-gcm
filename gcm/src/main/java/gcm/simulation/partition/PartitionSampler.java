package gcm.simulation.partition;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;

public class PartitionSampler {

	private PartitionSampler() {
		
	}
	
	static class ExcludedPersonPartitionSampler extends PartitionSampler{
		final PersonId personId;
		ExcludedPersonPartitionSampler(PersonId personId){
			this.personId = personId;
		}
	}
	
	public static PartitionSampler excludedPerson(PersonId personId) {
		if(personId == null) {
			throw new RuntimeException("null person id");
		}
		return new ExcludedPersonPartitionSampler(personId);
	}
	
	static class LabelSetPartitionSampler extends PartitionSampler{
		final LabelSet labelSet;
		LabelSetPartitionSampler(LabelSet labelSet){
			this.labelSet = labelSet;
		}
	}
	
	public static PartitionSampler labelSet(LabelSet labelSet) {
		if(labelSet == null) {
			throw new RuntimeException("null labelSet");
		}
		return new LabelSetPartitionSampler(labelSet);
	}
	
	static class LabelSetWeightingFunctionPartitionSampler extends PartitionSampler{
		final LabelSetWeightingFunction labelSetWeightingFunction;
		LabelSetWeightingFunctionPartitionSampler(LabelSetWeightingFunction labelSetWeightingFunction){
			this.labelSetWeightingFunction = labelSetWeightingFunction;
		}
	}
	
	public static PartitionSampler labelWeights(LabelSetWeightingFunction labelSetWeightingFunction) {
		if(labelSetWeightingFunction == null) {
			throw new RuntimeException("null labelSetWeightingFunction");
		}
		return new LabelSetWeightingFunctionPartitionSampler(labelSetWeightingFunction);
	}
	
	static class RandomNumberGeneratorIdPartitionSampler extends PartitionSampler{
		final RandomNumberGeneratorId randomNumberGeneratorId;
		RandomNumberGeneratorIdPartitionSampler(RandomNumberGeneratorId randomNumberGeneratorId){
			this.randomNumberGeneratorId = randomNumberGeneratorId;
		}
	}
	
	public static PartitionSampler randomGenerator(RandomNumberGeneratorId randomNumberGeneratorId) {
		if(randomNumberGeneratorId == null) {
			throw new RuntimeException("null randomNumberGeneratorId");
		}
		return new RandomNumberGeneratorIdPartitionSampler(randomNumberGeneratorId);
	}
	
	static class WithPartitionSampler extends PartitionSampler{
		final PartitionSampler a;
		final PartitionSampler b;

		public WithPartitionSampler(final PartitionSampler a, final PartitionSampler b) {
			this.a = a;
			this.b = b;
		}
	}
	
	public final PartitionSampler with(PartitionSampler other) {
		if(other == null) {
			throw new RuntimeException("null partition sampler");
		}
		return new WithPartitionSampler(this, other);
	}
	
	
}
