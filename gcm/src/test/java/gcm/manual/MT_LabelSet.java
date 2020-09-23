package gcm.manual;

import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.ExceptionAssertion.assertException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.SeedProvider;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRandomGeneratorId;
import gcm.automated.support.TestResourceId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.LabelSetInfo;
import gcm.simulation.partition.PartitionSampler;
import gcm.simulation.partition.PartitionSamplerInfo;
import gcm.util.TimeElapser;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link LabelSetInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LabelSetInfo.class)

public class MT_LabelSet {
	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(346345534578886785L);
	}



	@Test
	public void testLabelSet() {

		TimeElapser timeElapser = new TimeElapser();
		for (int i = 0; i < 1_000_000; i++) {
			LabelSet labelSet = LabelSet.create()//
					.compartment("compartment")//
					.region("region")//					
					.group("group")//
					.property(TestPersonPropertyId.PERSON_PROPERTY_1, "prop1")//
					.property(TestPersonPropertyId.PERSON_PROPERTY_2, 45)//
					.resource(TestResourceId.RESOURCE2, 2342L);//

			LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
			if(!labelSetInfo.getCompartmentLabel().isPresent()) {
				throw new RuntimeException();
			}
		}
		System.out.println(timeElapser.getElapsedMilliSeconds());
		
		
		timeElapser.reset();
		for (int i = 0; i < 1_000_000; i++) {
			LabelSet2 labelSet2 = LabelSet2.builder()//
					.setCompartmentLabel("compartment")//
					.setRegionLabel("region")//					
					.setGroupLabel("group")//
					.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1, "prop1")//
					.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2, 45)//
					.setResourceLabel(TestResourceId.RESOURCE2, 2342L)//
					.build();//

			
			if(!labelSet2.getCompartmentLabel().isPresent()) {
				throw new RuntimeException();
			}
		}
		System.out.println(timeElapser.getElapsedMilliSeconds());
	}
	
	public static double getWeight1(ObservableEnvironment observableEnvironment, LabelSetInfo labelSetInfo) {
		return 0;
	}
	public static double getWeight2(ObservableEnvironment observableEnvironment, LabelSet2 labelSet2) {
		return 0;
	}
	
	//@Test
	public void testPartitionSampler() {
		
		PersonId personId = new PersonId(1234);

		TimeElapser timeElapser = new TimeElapser();
		for (int i = 0; i < 1_000_000; i++) {
			PartitionSampler partitionSampler = PartitionSampler.create()
				.excludePerson(personId)
				.generator(TestRandomGeneratorId.COMET)
				.labelSet(LabelSet.create()//
						.compartment("compartment")//
						.region("region")//					
						.group("group")//
						.property(TestPersonPropertyId.PERSON_PROPERTY_1, "prop1")//
						.property(TestPersonPropertyId.PERSON_PROPERTY_2, 45)//
						.resource(TestResourceId.RESOURCE2, 2342L))//
				.labelWeight(MT_LabelSet::getWeight1);

			PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(partitionSampler);
			
			if(!partitionSamplerInfo.getLabelSet().isPresent()) {
				throw new RuntimeException();
			}
		}
		System.out.println("sampler "+timeElapser.getElapsedMilliSeconds());
		
		
		timeElapser.reset();
		for (int i = 0; i < 1_000_000; i++) {
			PartitionSampler2 partitionSampler2 = PartitionSampler2.builder()
				.setExcludedPerson(personId)
				.setRandomNumberGeneratorId(TestRandomGeneratorId.COMET)
				.setLabelSet(LabelSet2.builder()//
						.setCompartmentLabel("compartment")//
						.setRegionLabel("region")//					
						.setGroupLabel("group")//
						.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1, "prop1")//
						.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2, 45)//
						.setResourceLabel(TestResourceId.RESOURCE2, 2342L)
				        .build()) //
				.setLabelSetWeightingFunction(MT_LabelSet::getWeight2)
				.build();			
			
			if(!partitionSampler2.getLabelSet().isPresent()) {
				throw new RuntimeException();
			}
		}
		System.out.println("sampler "+timeElapser.getElapsedMilliSeconds());
	}


}
