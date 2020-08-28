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
import gcm.simulation.partition.Partition;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link Partition}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Partition.class)
public class AT_Partition {
	

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
	 * Tests {@linkplain Partition#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		Partition.Builder builder = Partition.builder();
		Partition partition = builder.build();
		assertNotNull(partition);
		assertNull(partition.getCompartmentPartitionFunction());
		assertNull(partition.getRegionPartitionFunction());
		assertNull(partition.getGroupPartitionFunction());
		assertTrue(partition.getPersonResourceIds().isEmpty());
		assertTrue(partition.getPersonPropertyIds().isEmpty());
	}

	/**
	 * Tests
	 * {@linkplain Partition#getCompartmentPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPartitionFunction", args = {})
	public void testGetCompartmentPartitionFunction() {
		Partition.Builder builder = Partition.builder();
		builder.setCompartmentPartition(AT_Partition::getCompartmentLabel);
		Partition partition = builder.build();

		assertNotNull(partition.getCompartmentPartitionFunction());
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Integer expectedLabel = getCompartmentLabel(testCompartmentId);
			Function<CompartmentId, Object> compartmentPartitionFunction = partition
					.getCompartmentPartitionFunction();
			Object actualLabel = compartmentPartitionFunction.apply(testCompartmentId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

	/**
	 * Tests {@linkplain Partition#getGroupPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getGroupPartitionFunction", args = {})
	public void testGetGroupPartitionFunction() {
		Partition.Builder builder = Partition.builder();
		builder.setGroupPartitionFunction(AT_Partition::getGroupTypeCountLabel);
		Partition partition = builder.build();

		assertNotNull(partition.getGroupPartitionFunction());

		GroupTypeCountMap groupTypeCountMap = GroupTypeCountMap.builder()//
				.setCount(TestGroupTypeId.GROUP_TYPE_1, 5)//
				.setCount(TestGroupTypeId.GROUP_TYPE_2, 0)//
				.setCount(TestGroupTypeId.GROUP_TYPE_4, 7)//
				.setCount(TestGroupTypeId.GROUP_TYPE_6, 1)//
				.build();//
		
		Integer expectedLabel = getGroupTypeCountLabel(groupTypeCountMap);
		
		Object actualLabel = partition.getGroupPartitionFunction().apply(groupTypeCountMap);

		
		assertEquals(expectedLabel, actualLabel);

	}

	/**
	 * Tests {@linkplain Partition#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		Partition.Builder builder = Partition.builder();
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_1,
				AT_Partition::getPersonProperty1Label);
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_2,
				AT_Partition::getPersonProperty1Label);
		Partition partition = builder.build();

		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2);
		Set<PersonPropertyId> actualPersonPropertyIds = partition.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

	}

	/**
	 * Tests
	 * {@linkplain Partition#getPersonPropertyPartitionFunction(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyPartitionFunction", args = { PersonPropertyId.class })
	public void testGetPersonPropertyPartitionFunction() {
		Partition.Builder builder = Partition.builder();
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_1,
				AT_Partition::getPersonProperty1Label);
		builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_2,
				AT_Partition::getPersonProperty2Label);
		Partition partition = builder.build();

		assertNotNull(partition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1));

		for (int i = 0; i < 10; i++) {
			int expectedProperty1Label = getPersonProperty1Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1);
			Object actualProperty1Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty1Label, actualProperty1Label);
		}

		assertNotNull(partition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2));
		for (int i = 0; i < 10; i++) {
			int expectedProperty2Label = getPersonProperty2Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2);
			Object actualProperty2Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty2Label, actualProperty2Label);
		}

		Function<Object, Object> personPropertyPartitionFunction = partition
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_3);

		assertNull(personPropertyPartitionFunction);

	}

	/**
	 * Tests {@linkplain Partition#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {
		Partition.Builder builder = Partition.builder();
		builder.setPersonResourcePartition(TestResourceId.RESOURCE1,
				AT_Partition::getResource1Label);
		builder.setPersonResourcePartition(TestResourceId.RESOURCE2,
				AT_Partition::getResource2Label);
		Partition partition = builder.build();
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		expectedResourceIds.add(TestResourceId.RESOURCE1);
		expectedResourceIds.add(TestResourceId.RESOURCE2);
		Set<ResourceId> actualResourceIds = partition.getPersonResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);
	}

	/**
	 * Tests
	 * {@linkplain Partition#getPersonResourcePartitionFunction(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourcePartitionFunction", args = { ResourceId.class })
	public void testGetPersonResourcePartitionFunction() {
		Partition.Builder builder = Partition.builder();
		builder.setPersonResourcePartition(TestResourceId.RESOURCE1,
				AT_Partition::getResource1Label);
		builder.setPersonResourcePartition(TestResourceId.RESOURCE2,
				AT_Partition::getResource2Label);
		Partition partition = builder.build();

		assertNotNull(partition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1));

		for (long i = 0; i < 10; i++) {
			String expectedResource1Label = getResource1Label(i);
			Function<Long, Object> personResourcePartitionFunction = partition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1);
			Object actualResource1Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource1Label, actualResource1Label);
		}

		assertNotNull(partition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2));

		for (long i = 0; i < 10; i++) {
			String expectedResource2Label = getResource2Label(i);
			Function<Long, Object> personResourcePartitionFunction = partition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2);
			Object actualResource2Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource2Label, actualResource2Label);
		}

		Function<Long, Object> personResourcePartitionFunction = partition
				.getPersonResourcePartitionFunction(TestResourceId.RESOURCE3);

		assertNull(personResourcePartitionFunction);
	}

	/**
	 * Tests {@linkplain Partition#getRegionPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getRegionPartitionFunction", args = {})
	public void testGetRegionPartitionFunction() {
		Partition.Builder builder = Partition.builder();
		builder.setRegionPartition(AT_Partition::getRegionLabel);
		Partition partition = builder.build();

		assertNotNull(partition.getRegionPartitionFunction());
		for (TestRegionId testRegionId : TestRegionId.values()) {
			Integer expectedLabel = getRegionLabel(testRegionId);
			Function<RegionId, Object> regionPartitionFunction = partition
					.getRegionPartitionFunction();
			Object actualLabel = regionPartitionFunction.apply(testRegionId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

}
