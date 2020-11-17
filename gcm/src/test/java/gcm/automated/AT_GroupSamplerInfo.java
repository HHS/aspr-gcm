package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gcm.automated.support.TestRandomGeneratorId;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.group.GroupSampler;
import gcm.simulation.group.GroupSamplerInfo;
import gcm.simulation.group.GroupWeightingFunction;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link GroupSamplerInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = GroupSamplerInfo.class)
public class AT_GroupSamplerInfo {
	
	/**
	 * Tests {@linkplain GroupSamplerInfo#build(GroupSampler)
	 */
	@Test
	@UnitTestMethod(name = "build", args = { GroupSampler.class })
	public void testBuilder() {

		GroupSamplerInfo groupSamplerInfo = GroupSamplerInfo.build(GroupSampler.create());

		assertNotNull(groupSamplerInfo);
		
		assertNotNull(groupSamplerInfo.getExcludedPerson());
		assertFalse(groupSamplerInfo.getExcludedPerson().isPresent());
		
		
		assertNotNull(groupSamplerInfo.getWeightingFunction());
		assertFalse(groupSamplerInfo.getWeightingFunction().isPresent());
		
		assertNotNull(groupSamplerInfo.getRandomNumberGeneratorId());
		assertFalse(groupSamplerInfo.getRandomNumberGeneratorId().isPresent());

	}

	/**
	 * Tests {@linkplain GroupSamplerInfo#getExcludedPerson()
	 */
	@Test
	@UnitTestMethod(name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		GroupSamplerInfo groupSamplerInfo = GroupSamplerInfo.build(GroupSampler.create().excludePerson(new PersonId(67)));
		assertNotNull(groupSamplerInfo);
		assertNotNull(groupSamplerInfo.getExcludedPerson());
		assertTrue(groupSamplerInfo.getExcludedPerson().isPresent());
		assertEquals(67, groupSamplerInfo.getExcludedPerson().get().getValue());
	}

	/**
	 * Tests {@linkplain GroupSamplerInfo#getRandomNumberGeneratorId()
	 */
	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		GroupSamplerInfo groupSamplerInfo = GroupSamplerInfo.build(GroupSampler.create().generator(TestRandomGeneratorId.DASHER).generator(TestRandomGeneratorId.VIXEN));
		assertNotNull(groupSamplerInfo);
		assertNotNull(groupSamplerInfo.getRandomNumberGeneratorId());
		assertTrue(groupSamplerInfo.getRandomNumberGeneratorId().isPresent());
		assertEquals(TestRandomGeneratorId.VIXEN, groupSamplerInfo.getRandomNumberGeneratorId().get());
	}

	

	/**
	 * Tests {@linkplain GroupSamplerInfo#getWeightingFunction()
	 */
	@Test
	@UnitTestMethod(name = "getWeightingFunction", args = {})
	public void testGetLabelSetWeightingFunction() {
		
		double expectedValue = 17.5;
		GroupSamplerInfo groupSamplerInfo = GroupSamplerInfo.build(
				GroupSampler.create()
				.weight((ObservableEnvironment observableEnvironment, PersonId personId, GroupId groupId)->expectedValue)
				);
		
		
		assertNotNull(groupSamplerInfo);
		assertNotNull(groupSamplerInfo.getWeightingFunction());
		assertTrue(groupSamplerInfo.getWeightingFunction().isPresent());
		GroupWeightingFunction groupWeightingFunction = groupSamplerInfo.getWeightingFunction().get();
		assertNotNull(groupWeightingFunction);
		
		
		assertEquals(expectedValue,groupWeightingFunction.getWeight(null, null,null),0);		
	}

}
