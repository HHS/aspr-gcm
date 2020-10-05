package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.automated.support.TestRandomGeneratorId;
import gcm.scenario.PersonId;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.LabelSetWeightingFunction;
import gcm.simulation.partition.PartitionSampler;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionSampler}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PartitionSampler.class)
public class AT_PartitionSampler {

	/**
	 * Tests {@linkplain PartitionSampler#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = { })
	public void testBuilder() {
		assertNotNull(PartitionSampler.builder());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getExcludedPerson()
	 */
	@Test
	@UnitTestMethod(name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setExcludedPerson(new PersonId(67)).build();
		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getExcludedPerson());
		assertTrue(partitionSampler.getExcludedPerson().isPresent());
		assertEquals(67, partitionSampler.getExcludedPerson().get().getValue());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getRandomNumberGeneratorId()
	 */
	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		PartitionSampler partitionSampler = PartitionSampler.builder()
				.setRandomNumberGeneratorId(TestRandomGeneratorId.DASHER)
				.setRandomNumberGeneratorId(TestRandomGeneratorId.VIXEN).build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getRandomNumberGeneratorId());
		assertTrue(partitionSampler.getRandomNumberGeneratorId().isPresent());
		assertEquals(TestRandomGeneratorId.VIXEN, partitionSampler.getRandomNumberGeneratorId().get());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getLabelSet()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSet", args = {})
	public void testGetLabelSet() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setLabelSet(
				LabelSet.builder().setCompartmentLabel("compartmentLabel").setRegionLabel("regionLabel").build())
				.build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getLabelSet());
		assertTrue(partitionSampler.getLabelSet().isPresent());
		LabelSet labelSet = partitionSampler.getLabelSet().get();
		
		assertTrue(labelSet.getCompartmentLabel().isPresent());
		assertEquals("compartmentLabel", labelSet.getCompartmentLabel().get());
		assertTrue(labelSet.getRegionLabel().isPresent());
		assertEquals("regionLabel", labelSet.getRegionLabel().get());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getLabelSetWeightingFunction()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSetWeightingFunction", args = {})
	public void testGetLabelSetWeightingFunction() {

		double expectedValue = 17.5;

		PartitionSampler partitionSampler = PartitionSampler.builder()
				.setLabelSetWeightingFunction((ObservableEnvironment observableEnvironment, LabelSet labelSet) -> expectedValue)
				.build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getLabelSetWeightingFunction());
		assertTrue(partitionSampler.getLabelSetWeightingFunction().isPresent());
		LabelSetWeightingFunction labelSetWeightingFunction = partitionSampler.getLabelSetWeightingFunction().get();
		assertNotNull(labelSetWeightingFunction);

		assertEquals(expectedValue, labelSetWeightingFunction.getWeight(null, null), 0);
	}

}
