package gcm.automated;

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
import gcm.automated.support.TestResourceId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.LabelSetInfo;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link LabelSetInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LabelSetInfo.class)

public class AT_LabelSetInfo {
	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(346345534578886785L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large gaps
	 * in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
//		System.out.println(MT_LabelSet.class.getSimpleName() + " " + SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@linkplain LabelSetInfo#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 20; i++) {
			Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

			LabelSet labelSet = LabelSet.create();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					labelSet = labelSet.with(LabelSet.create().property(testPersonPropertyId, "label"));
					expectedPersonPropertyIds.add(testPersonPropertyId);
				}
			}
			LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
			Set<PersonPropertyId> actualPersonPropertyIds = labelSetInfo.getPersonPropertyIds();
			assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);
		}
	}

	/**
	 * Tests {@linkplain LabelSetInfo#build(LabelSet)
	 */
	@Test
	@UnitTestMethod(name = "build", args = { LabelSet.class })
	public void testBuild() {

		LabelSet labelSet = LabelSet.create()//
				.compartment("compartment")//
				.region("region")//
				.group("group")//
				.property(TestPersonPropertyId.PERSON_PROPERTY_1, "prop1")//
				.property(TestPersonPropertyId.PERSON_PROPERTY_2, 45)//
				.resource(TestResourceId.RESOURCE2, 2342L);//

		LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
		assertNotNull(labelSetInfo);

		// precondition tests
		assertException(() -> LabelSetInfo.build(LabelSet.create().property(null, "label")), RuntimeException.class);
		assertException(
				() -> LabelSetInfo.build(LabelSet.create().property(TestPersonPropertyId.PERSON_PROPERTY_1, null)),
				RuntimeException.class);
		assertException(() -> LabelSetInfo.build(LabelSet.create().resource(null, "label")), RuntimeException.class);
		assertException(() -> LabelSetInfo.build(LabelSet.create().resource(TestResourceId.RESOURCE1, null)),
				RuntimeException.class);
	}

	/**
	 * Tests {@linkplain LabelSetInfo#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 20; i++) {
			Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

			LabelSet labelSet = LabelSet.create();
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					labelSet = labelSet.with(LabelSet.create().resource(testResourceId, "label"));
					expectedResourceIds.add(testResourceId);
				}
			}

			LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
			Set<ResourceId> actualResourceIds = labelSetInfo.getPersonResourceIds();
			assertEquals(expectedResourceIds, actualResourceIds);
		}

	}

	/**
	 * Tests {@linkplain LabelSetInfo#getGroupLabel()
	 */
	@Test
	@UnitTestMethod(name = "getGroupLabel", args = {})
	public void testGetGroupLabel() {
		Object expectedGroupLabel = "Group Label";
		LabelSetInfo labelSetInfo = LabelSetInfo.build(LabelSet.create().group(expectedGroupLabel));
		assertTrue(labelSetInfo.getGroupLabel().isPresent());
		Object actualGroupLabel = labelSetInfo.getGroupLabel().get();
		assertEquals(expectedGroupLabel, actualGroupLabel);
	}

	/**
	 * Tests {@linkplain LabelSet#getRegionLabel()
	 */
	@Test
	@UnitTestMethod(name = "getRegionLabel", args = {})
	public void testGetRegionLabel() {
		Object expectedRegionLabel = "Region Label";
		LabelSetInfo labelSetInfo = LabelSetInfo.build(LabelSet.create().region(expectedRegionLabel));
		assertTrue(labelSetInfo.getRegionLabel().isPresent());
		Object actualRegionLabel = labelSetInfo.getRegionLabel().get();
		assertEquals(expectedRegionLabel, actualRegionLabel);
	}

	/**
	 * Tests {@linkplain LabelSet#getCompartmentLabel()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentLabel", args = {})
	public void testGetCompartmentLabel() {
		Object expectedCompartmentLabel = "Compartment Label";
		LabelSetInfo labelSetInfo = LabelSetInfo.build(LabelSet.create().compartment(expectedCompartmentLabel));
		assertTrue(labelSetInfo.getCompartmentLabel().isPresent());
		Object actualCompartmentLabel = labelSetInfo.getCompartmentLabel().get();
		assertEquals(expectedCompartmentLabel, actualCompartmentLabel);
	}

	/**
	 * Tests {@linkplain LabelSetInfo#getPersonPropertyLabel(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyLabel", args = { PersonPropertyId.class })
	public void testGetPersonPropertyLabel() {
		LabelSet labelSet = LabelSet.create();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			labelSet = labelSet.with(LabelSet.create().property(testPersonPropertyId, testPersonPropertyId.toString()));
		}

		LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			Object expectedPersonPropertyLabel = testPersonPropertyId.toString();
			assertTrue(labelSetInfo.getPersonPropertyLabel(testPersonPropertyId).isPresent());
			Object actualPersonPropertyLabel = labelSetInfo.getPersonPropertyLabel(testPersonPropertyId).get();
			assertEquals(expectedPersonPropertyLabel, actualPersonPropertyLabel);
		}
	}

	/**
	 * Tests {@linkplain LabelSetInfo#getPersonResourceLabel(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceLabel", args = { ResourceId.class })
	public void testGetPersonResourceLabel() {
		LabelSet labelSet = LabelSet.create();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			labelSet = labelSet.with(LabelSet.create().resource(testResourceId, testResourceId.toString()));
		}

		LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);

		for (TestResourceId testResourceId : TestResourceId.values()) {
			Object expectedResourceLabel = testResourceId.toString();
			assertTrue(labelSetInfo.getPersonResourceLabel(testResourceId).isPresent());
			Object actualResourceLabel = labelSetInfo.getPersonResourceLabel(testResourceId).get();
			assertEquals(expectedResourceLabel, actualResourceLabel);
		}
	}

	/**
	 * Tests {@linkplain LabelSetInfo#equals(Object)
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		LabelSetInfo labelSetInfo1 = LabelSetInfo.build(LabelSet.create().compartment("compartment label"));
		LabelSetInfo labelSetInfo2 = LabelSetInfo.build(LabelSet.create().compartment("compartment label"));
		LabelSetInfo labelSetInfo3 = LabelSetInfo.build(LabelSet.create().region("compartment label"));

		assertFalse(labelSetInfo1 == labelSetInfo2);
		assertTrue(labelSetInfo1.equals(labelSetInfo1));
		assertTrue(labelSetInfo1.equals(labelSetInfo2));
		assertTrue(labelSetInfo2.equals(labelSetInfo1));
		assertFalse(labelSetInfo1.equals(labelSetInfo3));

	}

	/**
	 * Tests {@linkplain LabelSetInfo#hashCode()
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		LabelSetInfo labelSetInfo1 = LabelSetInfo.build(LabelSet.create().compartment("compartment label"));
		LabelSetInfo labelSetInfo2 = LabelSetInfo.build(LabelSet.create().compartment("compartment label"));

		assertFalse(labelSetInfo1 == labelSetInfo2);
		assertEquals(labelSetInfo1, labelSetInfo2);
		assertEquals(labelSetInfo1.hashCode(), labelSetInfo2.hashCode());
	}

}
