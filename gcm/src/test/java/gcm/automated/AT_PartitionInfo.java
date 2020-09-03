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
import gcm.simulation.partition.PartitionInfo;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionInfoO}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PartitionInfo.class)
public class AT_PartitionInfo {

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
	 * Tests {@linkplain PartitionInfo#build(Partition)
	 */
	@Test
	@UnitTestMethod(name = "build", args = { Partition.class })
	public void testBuilder() {
		PartitionInfo partitionInfo = PartitionInfo.build(Partition.create());

		assertNotNull(partitionInfo);
		assertNull(partitionInfo.getCompartmentPartitionFunction());
		assertNull(partitionInfo.getRegionPartitionFunction());
		assertNull(partitionInfo.getGroupPartitionFunction());
		assertTrue(partitionInfo.getPersonResourceIds().isEmpty());
		assertTrue(partitionInfo.getPersonPropertyIds().isEmpty());
	}

	/**
	 * Tests {@linkplain PartitionInfo#getCompartmentPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPartitionFunction", args = {})
	public void testGetCompartmentPartitionFunction() {
		PartitionInfo partitionInfo = PartitionInfo
				.build(Partition.create().compartment(AT_PartitionInfo::getCompartmentLabel));
		assertNotNull(partitionInfo.getCompartmentPartitionFunction());
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Integer expectedLabel = getCompartmentLabel(testCompartmentId);
			Function<CompartmentId, Object> compartmentPartitionFunction = partitionInfo
					.getCompartmentPartitionFunction();
			Object actualLabel = compartmentPartitionFunction.apply(testCompartmentId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

	/**
	 * Tests {@linkplain PartitionInfo#getGroupPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getGroupPartitionFunction", args = {})
	public void testGetGroupPartitionFunction() {
		PartitionInfo partitionInfo = PartitionInfo
				.build(Partition.create().group(AT_PartitionInfo::getGroupTypeCountLabel));

		assertNotNull(partitionInfo.getGroupPartitionFunction());

		GroupTypeCountMap groupTypeCountMap = GroupTypeCountMap.builder()//
				.setCount(TestGroupTypeId.GROUP_TYPE_1, 5)//
				.setCount(TestGroupTypeId.GROUP_TYPE_2, 0)//
				.setCount(TestGroupTypeId.GROUP_TYPE_4, 7)//
				.setCount(TestGroupTypeId.GROUP_TYPE_6, 1)//
				.build();//

		Integer expectedLabel = getGroupTypeCountLabel(groupTypeCountMap);

		Object actualLabel = partitionInfo.getGroupPartitionFunction().apply(groupTypeCountMap);

		assertEquals(expectedLabel, actualLabel);

	}

	/**
	 * Tests {@linkplain PartitionInfo#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		PartitionInfo partitionInfo = PartitionInfo.build(Partition.create()//
				.property(TestPersonPropertyId.PERSON_PROPERTY_1, AT_PartitionInfo::getPersonProperty1Label)//
				.property(TestPersonPropertyId.PERSON_PROPERTY_2, AT_PartitionInfo::getPersonProperty1Label)//
		);

		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2);
		Set<PersonPropertyId> actualPersonPropertyIds = partitionInfo.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

	}

	/**
	 * Tests {@linkplain
	 * PartitionInfo#getPersonPropertyPartitionFunction(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyPartitionFunction", args = { PersonPropertyId.class })
	public void testGetPersonPropertyPartitionFunction() {
		Partition partition = Partition.create()//
				.property(TestPersonPropertyId.PERSON_PROPERTY_1, AT_PartitionInfo::getPersonProperty1Label)//
				.property(TestPersonPropertyId.PERSON_PROPERTY_2, AT_PartitionInfo::getPersonProperty2Label);//

		PartitionInfo partitionInfo = PartitionInfo.build(partition);

		assertNotNull(partitionInfo.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1));

		for (int i = 0; i < 10; i++) {
			int expectedProperty1Label = getPersonProperty1Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partitionInfo
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1);
			Object actualProperty1Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty1Label, actualProperty1Label);
		}

		assertNotNull(partitionInfo.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2));
		for (int i = 0; i < 10; i++) {
			int expectedProperty2Label = getPersonProperty2Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partitionInfo
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2);
			Object actualProperty2Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty2Label, actualProperty2Label);
		}

		Function<Object, Object> personPropertyPartitionFunction = partitionInfo
				.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_3);

		assertNull(personPropertyPartitionFunction);

	}

	/**
	 * Tests {@linkplain PartitionInfo#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {
		PartitionInfo partitionInfo = PartitionInfo.build(//
				Partition.create()//
						.resource(TestResourceId.RESOURCE1, AT_PartitionInfo::getResource1Label)//
						.resource(TestResourceId.RESOURCE2, AT_PartitionInfo::getResource2Label)//
		);//

		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		expectedResourceIds.add(TestResourceId.RESOURCE1);
		expectedResourceIds.add(TestResourceId.RESOURCE2);
		Set<ResourceId> actualResourceIds = partitionInfo.getPersonResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);
	}

	/**
	 * Tests
	 * {@linkplain PartitionInfo#getPersonResourcePartitionFunction(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourcePartitionFunction", args = { ResourceId.class })
	public void testGetPersonResourcePartitionFunction() {
		PartitionInfo partitionInfo = PartitionInfo.build(//
				Partition.create()//
						.resource(TestResourceId.RESOURCE1, AT_PartitionInfo::getResource1Label)//
						.resource(TestResourceId.RESOURCE2, AT_PartitionInfo::getResource2Label)//
		);//

		assertNotNull(partitionInfo.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1));

		for (long i = 0; i < 10; i++) {
			String expectedResource1Label = getResource1Label(i);
			Function<Long, Object> personResourcePartitionFunction = partitionInfo
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1);
			Object actualResource1Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource1Label, actualResource1Label);
		}

		assertNotNull(partitionInfo.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2));

		for (long i = 0; i < 10; i++) {
			String expectedResource2Label = getResource2Label(i);
			Function<Long, Object> personResourcePartitionFunction = partitionInfo
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2);
			Object actualResource2Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource2Label, actualResource2Label);
		}

		Function<Long, Object> personResourcePartitionFunction = partitionInfo
				.getPersonResourcePartitionFunction(TestResourceId.RESOURCE3);

		assertNull(personResourcePartitionFunction);
	}

	/**
	 * Tests {@linkplain PartitionInfo#getRegionPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getRegionPartitionFunction", args = {})
	public void testGetRegionPartitionFunction() {
		PartitionInfo partitionInfo = PartitionInfo.build(//
				Partition.create().region(AT_PartitionInfo::getRegionLabel)//
		);//

		assertNotNull(partitionInfo.getRegionPartitionFunction());
		for (TestRegionId testRegionId : TestRegionId.values()) {
			Integer expectedLabel = getRegionLabel(testRegionId);
			Function<RegionId, Object> regionPartitionFunction = partitionInfo.getRegionPartitionFunction();
			Object actualLabel = regionPartitionFunction.apply(testRegionId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

}
