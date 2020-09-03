package gcm.manual;

import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRandomGeneratorId;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.Partition;
import gcm.simulation.partition.PartitionSampler;
import gcm.simulation.partition.PartitionSampler2;

public class Junk {

	private static Integer getCompartmentLabel(CompartmentId compartmentId) {
		return 0;
	}

	private static Integer getRegionLabel(RegionId regionId) {
		return 6;
	}

	private static String getPropertyLabel(Object value) {
		return "asdf";
	}

	public static void main(String[] args) {
		PartitionSampler partitionSampler = //
				PartitionSampler//
						.excludedPerson(new PersonId(34534))//
						.with(PartitionSampler.labelSet(LabelSet.compartment("asdf")))//
						.with(PartitionSampler.randomGenerator(TestRandomGeneratorId.CUPID));//

		PartitionSampler2 partitionSampler2 = PartitionSampler2.create().excludedPerson(new PersonId(34534))
				.labelSet(LabelSet.compartment("asdf")).randomGenerator(TestRandomGeneratorId.CUPID);

		Partition.compartment(Junk::getCompartmentLabel)//
				.with(Partition.region(Junk::getRegionLabel))//
				.with(Partition.property(TestPersonPropertyId.PERSON_PROPERTY_5, Junk::getPropertyLabel));

		Partition2.create()//
				.compartment(Junk::getCompartmentLabel)//
				.region(Junk::getRegionLabel)//
				.property(TestPersonPropertyId.PERSON_PROPERTY_5, Junk::getPropertyLabel);

	}

}
