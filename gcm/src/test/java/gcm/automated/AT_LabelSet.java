package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gcm.automated.support.SeedProvider;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestResourceId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.LabelSet;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link LabelSetInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LabelSet.class)

public class AT_LabelSet {
	private static SeedProvider SEED_PROVIDER;

	@BeforeAll
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(346345534578886785L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large gaps
	 * in the seed cases generated by the SeedProvider.
	 */
	@AfterAll
	public static void afterClass() {
//		System.out.println(AT_LabelSet.class.getSimpleName() + " " + SEED_PROVIDER.generateUnusedSeedReport());
	}

	
	/**
	 * Tests {@linkplain LabelSet#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 20; i++) {
			Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

			LabelSet.Builder builder = LabelSet.builder();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					builder.setPropertyLabel(testPersonPropertyId, "label");
					expectedPersonPropertyIds.add(testPersonPropertyId);
				}
			}
			LabelSet labelSet = builder.build();
			Set<PersonPropertyId> actualPersonPropertyIds = labelSet.getPersonPropertyIds();
			assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);
		}
	}

	


	/**
	 * Tests {@linkplain LabelSet#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = { })
	public void testBuilder() {
		assertNotNull(LabelSet.builder());
	}
	
	/**
	 * Tests {@linkplain LabelSet#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 20; i++) {
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

			LabelSet.Builder builder = LabelSet.builder();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					builder.setResourceLabel(testResourceId, "label");
					expectedResourceIds.add(testResourceId);
				}
			}

			LabelSet labelSet = builder.build();
			Set<ResourceId> actualResourceIds = labelSet.getPersonResourceIds();
			assertEquals(expectedResourceIds, actualResourceIds);
		}

	}

	
	
	/**
	 * Tests {@linkplain LabelSet#getGroupLabel()
	 */
	@Test
	@UnitTestMethod(name = "getGroupLabel", args = {})
	public void testGetGroupLabel() {
		Object expectedGroupLabel = "Group Label";
		LabelSet labelSet = LabelSet.builder().setGroupLabel(expectedGroupLabel).build();
		assertTrue(labelSet.getGroupLabel().isPresent());
		Object actualGroupLabel = labelSet.getGroupLabel().get();
		assertEquals(expectedGroupLabel, actualGroupLabel);
	}

	

	/**
	 * Tests {@linkplain LabelSet#getRegionLabel()
	 */
	@Test
	@UnitTestMethod(name = "getRegionLabel", args = {})
	public void testGetRegionLabel() {
		Object expectedRegionLabel = "Region Label";
		LabelSet labelSet = LabelSet.builder().setRegionLabel(expectedRegionLabel).build();
		assertTrue(labelSet.getRegionLabel().isPresent());
		Object actualRegionLabel = labelSet.getRegionLabel().get();
		assertEquals(expectedRegionLabel, actualRegionLabel);
	}



	/**
	 * Tests {@linkplain LabelSet#getCompartmentLabel()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentLabel", args = {})
	public void testGetCompartmentLabel() {
		Object expectedCompartmentLabel = "Compartment Label";
		LabelSet labelSet = LabelSet.builder().setCompartmentLabel(expectedCompartmentLabel).build();
		assertTrue(labelSet.getCompartmentLabel().isPresent());
		Object actualCompartmentLabel = labelSet.getCompartmentLabel().get();
		assertEquals(expectedCompartmentLabel, actualCompartmentLabel);
	}

	
	
	/**
	 * Tests {@linkplain LabelSet#getPersonPropertyLabel(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyLabel", args = { PersonPropertyId.class })
	public void testGetPersonPropertyLabel() {
		LabelSet.Builder builder = LabelSet.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			builder.setPropertyLabel(testPersonPropertyId, testPersonPropertyId.toString());
		}

		LabelSet labelSet = builder.build();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			Object expectedPersonPropertyLabel = testPersonPropertyId.toString();
			assertTrue(labelSet.getPersonPropertyLabel(testPersonPropertyId).isPresent());
			Object actualPersonPropertyLabel = labelSet.getPersonPropertyLabel(testPersonPropertyId).get();
			assertEquals(expectedPersonPropertyLabel, actualPersonPropertyLabel);
		}
	}

	

	/**
	 * Tests {@linkplain LabelSet#getPersonResourceLabel(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceLabel", args = { ResourceId.class })
	public void testGetPersonResourceLabel() {
		LabelSet.Builder builder = LabelSet.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			builder.setResourceLabel(testResourceId, testResourceId.toString());
		}

		LabelSet labelSet = builder.build();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Object expectedResourceLabel = testResourceId.toString();
			assertTrue(labelSet.getPersonResourceLabel(testResourceId).isPresent());
			Object actualResourceLabel = labelSet.getPersonResourceLabel(testResourceId).get();
			assertEquals(expectedResourceLabel, actualResourceLabel);
		}
	}


	
	/**
	 * Tests {@linkplain LabelSet#equals(Object)
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		LabelSet labelSet1 = LabelSet.builder().setCompartmentLabel("compartment label").build();
		LabelSet labelSet2 = LabelSet.builder().setCompartmentLabel("compartment label").build();
		LabelSet labelSet3 = LabelSet.builder().setCompartmentLabel("compartment label2").build();

		assertFalse(labelSet1 == labelSet2);
		assertTrue(labelSet1.equals(labelSet1));
		assertTrue(labelSet1.equals(labelSet2));
		assertTrue(labelSet2.equals(labelSet1));
		assertFalse(labelSet1.equals(labelSet3));

	}

	/**
	 * Tests {@linkplain LabelSet#hashCode()
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		LabelSet labelSet1 = LabelSet.builder().setCompartmentLabel("compartment label").build();
		LabelSet labelSet2 = LabelSet.builder().setCompartmentLabel("compartment label").build();

		assertFalse(labelSet1 == labelSet2);
		assertEquals(labelSet1, labelSet2);
		assertEquals(labelSet1.hashCode(), labelSet2.hashCode());
	}
	
	
	/**
	 * Tests {@linkplain LabelSet#isSubsetMatch(LabelSet)
	 */
	@Test
	@UnitTestMethod(name = "isSubsetMatch", args = {LabelSet.class})
	public void testIsSubsetMatch() {

		LabelSet labelSet1 = LabelSet.builder()//
				.setCompartmentLabel("compartment label")//				
				.build();//
		
		LabelSet labelSet2 = LabelSet.builder()//
				.setCompartmentLabel("compartment label")//
				.build();//
		
		

		assertTrue(labelSet1.isSubsetMatch(labelSet2));
		assertTrue(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setCompartmentLabel("compartment label")//
				.setRegionLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setCompartmentLabel("compartment label")//
				.build();//
		
		

		assertTrue(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setGroupLabel("group label")//				
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setGroupLabel("group label2")//	
				.build();//
		assertFalse(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		labelSet1 = LabelSet.builder()//
				.setGroupLabel("group label")//				
				.setCompartmentLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setGroupLabel("group label")//	
				.build();//
		assertTrue(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1,"property label")//				
				.setCompartmentLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2,"property label")//	
				.build();//
		assertFalse(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1,"property label")//				
				.setCompartmentLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1,"property label")//	
				.build();//
		assertTrue(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setResourceLabel(TestResourceId.RESOURCE1,"resource label")//				
				.setCompartmentLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setResourceLabel(TestResourceId.RESOURCE2,"resource label")//	
				.build();//
		assertFalse(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));
		
		
		labelSet1 = LabelSet.builder()//
				.setResourceLabel(TestResourceId.RESOURCE1,"resource label")//				
				.setCompartmentLabel("compartment label")//
				.build();//
		
		labelSet2 = LabelSet.builder()//
				.setResourceLabel(TestResourceId.RESOURCE1,"resource label")//	
				.build();//
		assertTrue(labelSet1.isSubsetMatch(labelSet2));
		assertFalse(labelSet2.isSubsetMatch(labelSet1));

		
	}
	

}
