package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.GroupTypeCountMap;
import gcm.simulation.partition.Partition;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionInfo}
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
		Partition partition = Partition.builder().build();

		assertNotNull(partition);
		assertFalse(partition.getCompartmentPartitionFunction().isPresent());
		assertFalse(partition.getRegionPartitionFunction().isPresent());
		assertFalse(partition.getGroupPartitionFunction().isPresent());
		assertTrue(partition.getPersonResourceIds().isEmpty());
		assertTrue(partition.getPersonPropertyIds().isEmpty());
	}

	/**
	 * Tests {@linkplain Partition#getCompartmentPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPartitionFunction", args = {})
	public void testGetCompartmentPartitionFunction() {
		Partition partition = Partition.builder().setCompartmentFunction(AT_Partition::getCompartmentLabel).build();
		assertTrue(partition.getCompartmentPartitionFunction().isPresent());
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Integer expectedLabel = getCompartmentLabel(testCompartmentId);
			Function<CompartmentId, Object> compartmentPartitionFunction = partition.getCompartmentPartitionFunction()
					.get();
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
		Partition partition = Partition.builder().setGroupFunction(AT_Partition::getGroupTypeCountLabel).build();

		assertTrue(partition.getGroupPartitionFunction().isPresent());

		GroupTypeCountMap groupTypeCountMap = GroupTypeCountMap.builder()//
				.setCount(TestGroupTypeId.GROUP_TYPE_1, 5)//
				.setCount(TestGroupTypeId.GROUP_TYPE_2, 0)//
				.setCount(TestGroupTypeId.GROUP_TYPE_4, 7)//
				.setCount(TestGroupTypeId.GROUP_TYPE_6, 1)//
				.build();//

		Integer expectedLabel = getGroupTypeCountLabel(groupTypeCountMap);

		Object actualLabel = partition.getGroupPartitionFunction().get().apply(groupTypeCountMap);

		assertEquals(expectedLabel, actualLabel);

	}

	/**
	 * Tests {@linkplain Partition#getPersonPropertyIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		Partition partition = Partition.builder()
				.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_1,
						AT_Partition::getPersonProperty1Label)//
				.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_2,
						AT_Partition::getPersonProperty1Label)//
				.build();

		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2);
		Set<PersonPropertyId> actualPersonPropertyIds = partition.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

	}

	/**
	 * Tests {@linkplain
	 * Partition#getPersonPropertyPartitionFunction(PersonPropertyId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyPartitionFunction", args = { PersonPropertyId.class })
	public void testGetPersonPropertyPartitionFunction() {
		Partition partition = Partition.builder()//
				.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_1,
						AT_Partition::getPersonProperty1Label)//
				.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_2,
						AT_Partition::getPersonProperty2Label)//
				.build();//

		assertTrue(partition.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1).isPresent());

		for (int i = 0; i < 10; i++) {
			int expectedProperty1Label = getPersonProperty1Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_1).get();
			Object actualProperty1Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty1Label, actualProperty1Label);
		}

		assertTrue(partition.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2).isPresent());
		for (int i = 0; i < 10; i++) {
			int expectedProperty2Label = getPersonProperty2Label(i);
			Function<Object, Object> personPropertyPartitionFunction = partition
					.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_2).get();
			Object actualProperty2Label = personPropertyPartitionFunction.apply(i);
			assertEquals(expectedProperty2Label, actualProperty2Label);
		}

		assertFalse(partition.getPersonPropertyPartitionFunction(TestPersonPropertyId.PERSON_PROPERTY_3).isPresent());

	}

	/**
	 * Tests {@linkplain Partition#getPersonResourceIds()
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceIds", args = {})
	public void testGetPersonResourceIds() {

		Partition partition = Partition.builder()//
				.setPersonResourceFunction(TestResourceId.RESOURCE1, AT_Partition::getResource1Label)//
				.setPersonResourceFunction(TestResourceId.RESOURCE2, AT_Partition::getResource2Label)//
				.build();

		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();
		expectedResourceIds.add(TestResourceId.RESOURCE1);
		expectedResourceIds.add(TestResourceId.RESOURCE2);
		Set<ResourceId> actualResourceIds = partition.getPersonResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);
	}

	/**
	 * Tests {@linkplain Partition#getPersonResourcePartitionFunction(ResourceId)
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourcePartitionFunction", args = { ResourceId.class })
	public void testGetPersonResourcePartitionFunction() {

		Partition partition = Partition.builder()//
				.setPersonResourceFunction(TestResourceId.RESOURCE1, AT_Partition::getResource1Label)//
				.setPersonResourceFunction(TestResourceId.RESOURCE2, AT_Partition::getResource2Label)//
				.build();

		assertTrue(partition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1).isPresent());

		for (long i = 0; i < 10; i++) {
			String expectedResource1Label = getResource1Label(i);
			Function<Long, Object> personResourcePartitionFunction = partition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE1).get();
			Object actualResource1Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource1Label, actualResource1Label);
		}

		assertTrue(partition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2).isPresent());

		for (long i = 0; i < 10; i++) {
			String expectedResource2Label = getResource2Label(i);
			Function<Long, Object> personResourcePartitionFunction = partition
					.getPersonResourcePartitionFunction(TestResourceId.RESOURCE2).get();
			Object actualResource2Label = personResourcePartitionFunction.apply(i);
			assertEquals(expectedResource2Label, actualResource2Label);
		}

		assertFalse(partition.getPersonResourcePartitionFunction(TestResourceId.RESOURCE3).isPresent());
	}

	/**
	 * Tests {@linkplain Partition#getRegionPartitionFunction()
	 */
	@Test
	@UnitTestMethod(name = "getRegionPartitionFunction", args = {})
	public void testGetRegionPartitionFunction() {
		
		Partition partition = Partition.builder().setRegionFunction(AT_Partition::getRegionLabel).build();//
		

		assertTrue(partition.getRegionPartitionFunction().isPresent());
		for (TestRegionId testRegionId : TestRegionId.values()) {
			Integer expectedLabel = getRegionLabel(testRegionId);
			Function<RegionId, Object> regionPartitionFunction = partition.getRegionPartitionFunction().get();
			Object actualLabel = regionPartitionFunction.apply(testRegionId);
			assertEquals(expectedLabel, actualLabel);
		}
	}

	
	/**
	 * Tests {@linkplain Partition#getFilter()
	 */
	@Test
	@UnitTestMethod(name = "getFilter", args = {})
	public void testGetFilter() {

		Partition partition = Partition.builder().build();//
		assertFalse(partition.getFilter().isPresent());
		
		partition = Partition.builder().setFilter(Filter.allPeople()).build();//
		assertTrue(partition.getFilter().isPresent());
		
	}
	
	/**
	 * Tests {@linkplain Partition#isDegenerate()
	 */
	@Test
	@UnitTestMethod(name = "isDegenerate", args = {})
	public void testIsDegenerate() {

		Partition partition = Partition.builder().build();//
		assertTrue(partition.isDegenerate());
		
		partition = Partition.builder().setFilter(Filter.allPeople()).build();//
		assertTrue(partition.getFilter().isPresent());
		
	}
	


}
