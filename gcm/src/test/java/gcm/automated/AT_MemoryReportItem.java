package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gcm.output.simstate.MemoryReportItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

@UnitTest(target = MemoryReportItem.class)
public class AT_MemoryReportItem {

	private MemoryReportItem.Builder getMemoryReportItemBuilder() {
		return MemoryReportItem.builder()//
				.setChildByteCount(5678L)//
				.setDescriptor("descriptor")//
				.setId(67)//
				.setItemClass(Object.class)//
				.setParentId(45)//
				.setReplicationId(new ReplicationId(77))//
				.setScenarioId(new ScenarioId(97))//
				.setSelfByteCount(234L)//
				.setTime(5346d);//
	}

	/**
	 * Tests {@link MemoryReportItem#getChildByteCount()}
	 */
	@Test
	@UnitTestMethod(name = "getChildByteCount", args = {})
	public void testGetChildByteCount() {
		long expectedChildByteCount = 12345;
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setChildByteCount(expectedChildByteCount)
				.build();
		assertEquals(expectedChildByteCount, memoryReportItem.getChildByteCount());
	}

	/**
	 * Tests {@link MemoryReportItem#getDescriptor()}
	 */
	@Test
	@UnitTestMethod(name = "getDescriptor", args = {})
	public void testGetDescriptor() {
		String expectedDescriptor = "some descriptor";
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setDescriptor(expectedDescriptor).build();
		assertEquals(expectedDescriptor, memoryReportItem.getDescriptor());
	}

	/**
	 * Tests {@link MemoryReportItem#getId()}
	 */
	@Test
	@UnitTestMethod(name = "getId", args = {})
	public void testGetId() {
		int expectedId = 8888;
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setId(expectedId).build();
		assertEquals(expectedId, memoryReportItem.getId());
	}

	/**
	 * Tests {@link MemoryReportItem#getItemClass()}
	 */
	@Test
	@UnitTestMethod(name = "getItemClass", args = {})
	public void testGetItemClass() {
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setItemClass(Integer.class).build();
		assertEquals(Integer.class, memoryReportItem.getItemClass());
	}

	/**
	 * Tests {@link MemoryReportItem#getParentId()}
	 */
	@Test
	@UnitTestMethod(name = "getParentId", args = {})
	public void testGetParentId() {
		int expectedParentId = 85674;
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setParentId(expectedParentId).build();
		assertEquals(expectedParentId, memoryReportItem.getParentId());
	}

	/**
	 * Tests {@link MemoryReportItem#getReplicationId()}
	 */
	@Test
	@UnitTestMethod(name = "getReplicationId", args = {})
	public void testGetReplicationId() {
		ReplicationId expectedReplicationId = new ReplicationId(85674);
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setReplicationId(expectedReplicationId)
				.build();
		assertEquals(expectedReplicationId, memoryReportItem.getReplicationId());
	}

	/**
	 * Tests {@link MemoryReportItem#getScenarioId()}
	 */
	@Test
	@UnitTestMethod(name = "getScenarioId", args = {})
	public void testGetScenarioId() {
		ScenarioId expectedScenarioId = new ScenarioId(85674);
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setScenarioId(expectedScenarioId).build();
		assertEquals(expectedScenarioId, memoryReportItem.getScenarioId());
	}

	/**
	 * Tests {@link MemoryReportItem#getSelfByteCount()}
	 */
	@Test
	@UnitTestMethod(name = "getSelfByteCount", args = {})
	public void testGetSelfByteCount() {
		long expectedSelfByteCount = 7645345L;
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setSelfByteCount(expectedSelfByteCount)
				.build();
		assertEquals(expectedSelfByteCount, memoryReportItem.getSelfByteCount());
	}

	/**
	 * Tests {@link MemoryReportItem#getTime()}
	 */
	@Test
	@UnitTestMethod(name = "getTime", args = {})
	public void testGetTime() {
		double expectedTime = 7645.345;
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().setTime(expectedTime).build();
		assertEquals(expectedTime, memoryReportItem.getTime(), 0);
	}

	/**
	 * Tests {@link MemoryReportItem#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		// boiler plate implementation
		MemoryReportItem memoryReportItem = getMemoryReportItemBuilder().build();
		String expectedString = "MemoryReportItem [scenarioId=97, replicationId=77, time=5346.0, descriptor=descriptor, parentId=45, id=67, selfByteCount=234, childByteCount=5678]";
		assertEquals(expectedString, memoryReportItem.toString());
	}
}
