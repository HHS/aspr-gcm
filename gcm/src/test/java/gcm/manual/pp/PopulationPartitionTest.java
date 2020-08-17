package gcm.manual.pp;

import java.util.Random;

public class PopulationPartitionTest {

	public static void main(String[] args) {

		Random random = new Random();

		PopulationPartitionDefinition populationPartitionDefinition = PopulationPartitionDefinition.builder()//
				.setCompartmentPartition(SampleLamdas::getCompartmentLabel)//
				.setRegionPartition(SampleLamdas::getRegionLabel)//
				.build();//

//		for (int i = 0; i < 10; i++) {
//			PPCompartment compartment = PPCompartment.getRandomCompartment(random);
//			PPRegion region = PPRegion.getRandomRegion(random);
//
//			MultiKey multiKey = populationPartition.getMultiKey(compartment, region);
//
//			System.out.println(compartment + "\t" + region + "\t" + multiKey);
//		}
	}

}
