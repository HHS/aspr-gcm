package gcm.simulation.partition;

import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;

public final class PartitionSamplerInfo {

	private PersonId excludedPerson;
	private RandomNumberGeneratorId randomNumberGeneratorId;
	private LabelSet labelSet;
	private LabelSetWeightingFunction labelSetWeightingFunction;
	
	private PartitionSamplerInfo(PartitionSampler partitionSampler) {
		processPartitionSampler(partitionSampler);
	}
	
	public static PartitionSamplerInfo build(PartitionSampler partitionSampler) {
		return new PartitionSamplerInfo(partitionSampler);
	}
	
	private void processPartitionSampler(PartitionSampler partitionSampler) {
		
	}
}
