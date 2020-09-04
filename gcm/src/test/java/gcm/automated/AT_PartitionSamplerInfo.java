package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.automated.support.TestRandomGeneratorId;
import gcm.scenario.PersonId;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.LabelSetInfo;
import gcm.simulation.partition.LabelSetWeightingFunction;
import gcm.simulation.partition.PartitionSampler;
import gcm.simulation.partition.PartitionSamplerInfo;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionSamplerInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PartitionSamplerInfo.class)
public class AT_PartitionSamplerInfo {
	
	/**
	 * Tests {@linkplain PartitionSamplerInfo#build(PartitionSampler)
	 */
	@Test
	@UnitTestMethod(name = "build", args = { PartitionSampler.class })
	public void testBuilder() {

		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(PartitionSampler.create());

		assertNotNull(partitionSamplerInfo);
		assertNull(partitionSamplerInfo.getExcludedPerson());
		assertNull(partitionSamplerInfo.getLabelSet());
		assertNull(partitionSamplerInfo.getLabelSetWeightingFunction());
		assertNull(partitionSamplerInfo.getRandomNumberGeneratorId());

	}

	/**
	 * Tests {@linkplain PartitionSamplerInfo#getExcludedPerson()
	 */
	@Test
	@UnitTestMethod(name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo
				.build(PartitionSampler.create().excludePerson(new PersonId(67)));

		assertNotNull(partitionSamplerInfo.getExcludedPerson());
		assertEquals(67, partitionSamplerInfo.getExcludedPerson().getValue());

	}

	/**
	 * Tests {@linkplain PartitionSamplerInfo#getRandomNumberGeneratorId()
	 */
	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(PartitionSampler.create()
				.generator(TestRandomGeneratorId.DASHER).generator(TestRandomGeneratorId.VIXEN));

		assertNotNull(partitionSamplerInfo.getRandomNumberGeneratorId());
		assertEquals(TestRandomGeneratorId.VIXEN, partitionSamplerInfo.getRandomNumberGeneratorId());
	}

	/**
	 * Tests {@linkplain PartitionSamplerInfo#getLabelSet()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSet", args = {})
	public void testGetLabelSet() {
		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(PartitionSampler.create()
				.labelSet(LabelSet.create().compartment("compartmentLabel").region("regionLabel")));

		LabelSet labelSet = partitionSamplerInfo.getLabelSet();
		assertNotNull(labelSet);

		LabelSetInfo labelSetInfo = LabelSetInfo.build(labelSet);
		assertTrue(labelSetInfo.getCompartmentLabel().isPresent());
		assertEquals("compartmentLabel", labelSetInfo.getCompartmentLabel().get());
		assertTrue(labelSetInfo.getRegionLabel().isPresent());
		assertEquals("regionLabel", labelSetInfo.getRegionLabel().get());
	}

	/**
	 * Tests {@linkplain PartitionSamplerInfo#getLabelSetWeightingFunction()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSetWeightingFunction", args = {})
	public void testGetLabelSetWeightingFunction() {
		
		double expectedValue = 17.5;
		
		PartitionSamplerInfo partitionSamplerInfo = PartitionSamplerInfo.build(PartitionSampler.create()
				.labelWeight((ObservableEnvironment observableEnvironment, LabelSetInfo labelSetInfo)-> expectedValue));

		LabelSetWeightingFunction labelSetWeightingFunction = partitionSamplerInfo.getLabelSetWeightingFunction();
		assertNotNull(labelSetWeightingFunction);
		
		assertEquals(expectedValue,labelSetWeightingFunction.getWeight(null, null),0);		
	}

}
