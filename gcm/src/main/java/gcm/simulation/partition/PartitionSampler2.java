package gcm.simulation.partition;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;

public class PartitionSampler2 {

	private PartitionSampler2() {
		
	}
	
	static class EmptyPartitionSampler extends PartitionSampler2{
		
	}
	
	public static PartitionSampler2 create() {
		return new EmptyPartitionSampler();
	}
	
	static class ExcludedPersonPartitionSampler extends PartitionSampler2{
		final PersonId personId;
		ExcludedPersonPartitionSampler(PersonId personId){
			this.personId = personId;
		}
	}
	
	public PartitionSampler2 excludedPerson(PersonId personId) {
		if(personId == null) {
			throw new RuntimeException("null person id");
		}
		return new WithPartitionSampler(this, new ExcludedPersonPartitionSampler(personId));
	}
	
	static class LabelSetPartitionSampler extends PartitionSampler2{
		final LabelSet labelSet;
		LabelSetPartitionSampler(LabelSet labelSet){
			this.labelSet = labelSet;
		}
	}
	
	public PartitionSampler2 labelSet(LabelSet labelSet) {
		if(labelSet == null) {
			throw new RuntimeException("null labelSet");
		}
		return new WithPartitionSampler(this, new LabelSetPartitionSampler(labelSet));
	}
	
	static class LabelSetWeightingFunctionPartitionSampler extends PartitionSampler2{
		final LabelSetWeightingFunction labelSetWeightingFunction;
		LabelSetWeightingFunctionPartitionSampler(LabelSetWeightingFunction labelSetWeightingFunction){
			this.labelSetWeightingFunction = labelSetWeightingFunction;
		}
	}
	
	public PartitionSampler2 labelWeights(LabelSetWeightingFunction labelSetWeightingFunction) {
		if(labelSetWeightingFunction == null) {
			throw new RuntimeException("null labelSetWeightingFunction");
		}
		return new WithPartitionSampler(this,
		new LabelSetWeightingFunctionPartitionSampler(labelSetWeightingFunction));
	}
	
	static class RandomNumberGeneratorIdPartitionSampler extends PartitionSampler2{
		final RandomNumberGeneratorId randomNumberGeneratorId;
		RandomNumberGeneratorIdPartitionSampler(RandomNumberGeneratorId randomNumberGeneratorId){
			this.randomNumberGeneratorId = randomNumberGeneratorId;
		}
	}
	
	public PartitionSampler2 randomGenerator(RandomNumberGeneratorId randomNumberGeneratorId) {
		if(randomNumberGeneratorId == null) {
			throw new RuntimeException("null randomNumberGeneratorId");
		}
		return new WithPartitionSampler(this,
		new RandomNumberGeneratorIdPartitionSampler(randomNumberGeneratorId));
	}
	
	static class WithPartitionSampler extends PartitionSampler2{
		final PartitionSampler2 a;
		final PartitionSampler2 b;

		public WithPartitionSampler(final PartitionSampler2 a, final PartitionSampler2 b) {
			this.a = a;
			this.b = b;
		}
	}
	
	
	
	
}
