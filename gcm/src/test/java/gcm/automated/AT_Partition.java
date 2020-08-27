package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import org.junit.Test;

import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestResourceId;
import gcm.scenario.CompartmentId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.GroupTypeCountMap;
import gcm.simulation.partition.PopulationPartitionDefinition;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link PopulationPartitionDefinition}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PopulationPartitionDefinition.class)
public class AT_PopulationPartitionDefinition {
	

	private static int getCompartmentLabel(CompartmentId compartmentId) {
		return compartmentId.hashCode();

	}

	private static int getPersonProperty1Label(Object value) {
		return value.hashCode();
	}

	private static int getPersonProperty2Label(Object value) {
		return -value.hashCode();
	}

	private static String getResource1Label(Long amount) {
		int x = (int) amount.longValue();
		switch (x) {
		case 0:
			return "none";
		case 1:
			return "one";
		default:
			return "many";
		}

	}

	private static String getResource2Label(Long amount) {
		int x = (int) amount.longValue();
		switch (x) {
		case 0:
			return "zilch";
		case 1:
			return "uno";
		default:
			return "whoa";
		}

	}

	private static Integer getGroupTypeCountLabel(GroupTypeCountMap groupTypeCountMap) {
		int result = 0;
		for (GroupTypeId groupTypeId : groupTypeCountMap.getGroupTypeIds()) {
			result += groupTypeCountMap.getGroupCount(groupTypeId);
		}
		return result;
	}

	private static int getRegionLabel(RegionId regionId) {
		return regionId.hashCode();
	}

	/**
	 * Tests {@linkplain PopulationPartitionDefinition#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();
		assertNotNull(populationPartitionDefinition);
		assertNull(populationPartitionDefinition.getCompartmentPartitionFunction());
		assertNull(populationPartitionDefinition.getRegionPartitionFunction());
		assertNull(populationPartitionDefinition.getGroupPartitionFunction());
		assertTrue(populationPartitionDefinition.getPersonResourceIds().isEmpty());
		assertTrue(populationPartitionDefinition.getPersonPropertyIds().isEmpty());
	}

	/**
	 * Tests
	 * {@linkplain PopulationPartitionDefinition#getCompartmentPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPartitionFunction", args = {})
	public void testGetCompartmentPartitionFunction() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setCompartmentPartition(AT_PopulationPartitionDefinition::getCompartmentLabel);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		assertNotNull(populationPartitionDefinition.getCompartmentPartitionFunction());
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Integer expectedLabel = getCompartmentLabel(testCompartmentId);
			Function<CompartmentId, Object> compartmentPartitionFunction = populationPartitionDefinition
					.getCompartmentPartitionFunction();
			Object actualLabel = compartmentPartitionFunction.apply(testCompartmentId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

	/**
	 * Tests {@linkplain PopulationPartitionDefinition#getGroupPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getGroupPartitionFunction", args = {})
	public void testGetGroupPartitionFunction() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setGroupPartitionFunction(AT_PopulationPartitionDefinition::getGroupTypeCountLabel);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		assertNotNull(populationPartitionDefinition.getGroupPartitionFunction());

		GroupTypeCountMap groupTypeCountMap = GroupTypeCountMap.builder()//
				.setCount(TestGroupTypeId.GROUP_TYPE_1, 5)//
				.setCount(TestGroupTypeId.GROUP_TYPE_2, 0)//
				.setCount(TestGroupTypeId.GROUP_TYPE_4, 7)//
				.setCount(TestGroupTypeId.GROUP_TYPE_6, 1)//
				.build();//
		
		Integer expectedLabel = getGroupTypeCountLabel(groupTypeCountMap);
		
		Object actualLabel = populationPartitionDefinition.getGroupPartitionFunction().apply(groupTypeCountMap);

		
		assertEquals(expectedLabel, actualLabel);

	}

	/**
	 * Tests {@linkplain PopulationPartitionDefinition#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_1,
				AT_PopulationPartitionDefinition::getPersonProperty1Label);
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_2,
				AT_PopulationPartitionDefinition::getPersonProperty1Label);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2);
		Set<PersonPropertyId> actualPersonPropertyIds = populationPartitionDefinition.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

	}

	/**
	 * Tests
	 * {@linkplain PopulationPartitionDefinition#getPersonPropertyPartitionFunction(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyPartitionFunction", args = { PersonPropertyId.class })
	public void testGetPersonPropertyPartitionFunction() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_1,
				AT_PopulationPartitionDefinition::getPersonProperty1Label);
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_2,
				AT_PopulationPartitionDefinition::getPersonProperty2Label);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		assertNotNull(populationPartitionDefinition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1));

		for (int i = 0; i < 10; i++) {
			int expectedProperty1Label = getPersonProperty1Label(i);
			Function<Object, Object> personPropertyPartitionFunction = populationPartitionDefinition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1);
			Object actualProperty1Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty1Label, actualProperty1Label);
		}

		assertNotNull(populationPartitionDefinition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2));
		for (int i = 0; i < 10; i++) {
			int expectedProperty2Label = getPersonProperty2Label(i);
			Function<Object, Object> personPropertyPartitionFunction = populationPartitionDefinition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2);
			Object actualProperty2Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty2Label, actualProperty2Label);
		}

		Function<Object, Object> personPropertyPartitionFunction = populationPartitionDefinition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_3);

		assertNull(personPropertyPartitionFunction);

	}

	/**
	 * Tests {@linkplain PopulationPartitionDefinition#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setPersonResourcePartition(TestResourceId.RESOURCE1,
				AT_PopulationPartitionDefinition::getResource1Label);
		builder.setPersonResourcePartition(TestResourceId.RESOURCE2,
				AT_PopulationPartitionDefinition::getResource2Label);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		expectedResourceIds.add(TestResourceId.RESOURCE1);
		expectedResourceIds.add(TestResourceId.RESOURCE2);
		Set<ResourceId> actualResourceIds = populationPartitionDefinition.getPersonResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);
	}

	/**
	 * Tests
	 * {@linkplain PopulationPartitionDefinition#getPersonResourcePartitionFunction(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourcePartitionFunction", args = { ResourceId.class })
	public void testGetPersonResourcePartitionFunction() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setPersonResourcePartition(TestResourceId.RESOURCE1,
				AT_PopulationPartitionDefinition::getResource1Label);
		builder.setPersonResourcePartition(TestResourceId.RESOURCE2,
				AT_PopulationPartitionDefinition::getResource2Label);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		assertNotNull(populationPartitionDefinition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1));

		for (long i = 0; i < 10; i++) {
			String expectedResource1Label = getResource1Label(i);
			Function<Long, Object> personResourcePartitionFunction = populationPartitionDefinition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1);
			Object actualResource1Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource1Label, actualResource1Label);
		}

		assertNotNull(populationPartitionDefinition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2));

		for (long i = 0; i < 10; i++) {
			String expectedResource2Label = getResource2Label(i);
			Function<Long, Object> personResourcePartitionFunction = populationPartitionDefinition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2);
			Object actualResource2Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource2Label, actualResource2Label);
		}

		Function<Long, Object> personResourcePartitionFunction = populationPartitionDefinition
				.getPersonResourcePartitionFunction(TestResourceId.RESOURCE3);

		assertNull(personResourcePartitionFunction);
	}

	/**
	 * Tests {@linkplain PopulationPartitionDefinition#getRegionPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getRegionPartitionFunction", args = {})
	public void testGetRegionPartitionFunction() {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();
		builder.setRegionPartition(AT_PopulationPartitionDefinition::getRegionLabel);
		PopulationPartitionDefinition populationPartitionDefinition = builder.build();

		assertNotNull(populationPartitionDefinition.getRegionPartitionFunction());
		for (TestRegionId testRegionId : TestRegionId.values()) {
			Integer expectedLabel = getRegionLabel(testRegionId);
			Function<RegionId, Object> regionPartitionFunction = populationPartitionDefinition
					.getRegionPartitionFunction();
			Object actualLabel = regionPartitionFunction.apply(testRegionId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

}
