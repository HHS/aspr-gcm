package gcm.simulation.partition;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = PartitionSamplerInfo.class)
public class PartitionSampler {

	private PartitionSampler() {
		
	}
	
	
	static class EmptyPartitionSampler extends PartitionSampler{
	}
	
	public final static PartitionSampler create() {
		return new EmptyPartitionSampler();
	}
	
	static class ExcludedPersonPartitionSampler extends PartitionSampler{
		final PersonId personId;
		ExcludedPersonPartitionSampler(PersonId personId){
			this.personId = personId;
		}
	}
	
	public final PartitionSampler excludePerson(PersonId personId) {
		if(personId == null) {
			throw new RuntimeException("null person id");
		}
		return new WithPartitionSampler(this, new ExcludedPersonPartitionSampler(personId));
	}
	
	static class LabelSetPartitionSampler extends PartitionSampler{
		final LabelSet labelSet;
		LabelSetPartitionSampler(LabelSet labelSet){
			this.labelSet = labelSet;
		}
	}
	
	public final PartitionSampler labelSet(LabelSet labelSet) {
		if(labelSet == null) {
			throw new RuntimeException("null labelSet");
		}
		return new WithPartitionSampler(this, new LabelSetPartitionSampler(labelSet));
	}
	
	static class LabelSetWeightingFunctionPartitionSampler extends PartitionSampler{
		final LabelSetWeightingFunction labelSetWeightingFunction;
		LabelSetWeightingFunctionPartitionSampler(LabelSetWeightingFunction labelSetWeightingFunction){
			this.labelSetWeightingFunction = labelSetWeightingFunction;
		}
	}
	
	public final PartitionSampler labelWeight(LabelSetWeightingFunction labelSetWeightingFunction) {
		if(labelSetWeightingFunction == null) {
			throw new RuntimeException("null labelSetWeightingFunction");
		}
		return new WithPartitionSampler(this,
		new LabelSetWeightingFunctionPartitionSampler(labelSetWeightingFunction));
	}
	
	static class RandomNumberGeneratorIdPartitionSampler extends PartitionSampler{
		final RandomNumberGeneratorId randomNumberGeneratorId;
		RandomNumberGeneratorIdPartitionSampler(RandomNumberGeneratorId randomNumberGeneratorId){
			this.randomNumberGeneratorId = randomNumberGeneratorId;
		}
	}
	
	public final PartitionSampler generator(RandomNumberGeneratorId randomNumberGeneratorId) {
		if(randomNumberGeneratorId == null) {
			throw new RuntimeException("null randomNumberGeneratorId");
		}
		return new WithPartitionSampler(this,
		new RandomNumberGeneratorIdPartitionSampler(randomNumberGeneratorId));
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
