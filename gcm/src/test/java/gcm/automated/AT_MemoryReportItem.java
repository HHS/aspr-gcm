package gcm.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.output.simstate.MemoryReportItem;
import gcm.output.simstate.MemoryReportItem.MemoryReportItemBuilder;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.UnitTest;

@UnitTest(target = MemoryReportItem.class)
public class AT_MemoryReportItem {

	private MemoryReportItemBuilder getMemoryReportItemBuilder() {
		MemoryReportItemBuilder result = new MemoryReportItemBuilder();
		result.setChildByteCount(5678L);
		result.setDescriptor("descriptor");
		result.setId(67);
		result.setItemClass(Object.class);
		result.setParentId(45);
		result.setReplicationId(new ReplicationId(77));
		result.setScenarioId(new ScenarioId(97));
		result.setSelfByteCount(234L);
		result.setTime(5346d);
		return result;
	}

	@Test
	public void testGetChildByteCount() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		long expectedChildByteCount = 12345;
		memoryReportItemBuilder.setChildByteCount(expectedChildByteCount);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedChildByteCount, memoryReportItem.getChildByteCount());
	}

	@Test
	public void testGetDescriptor() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		String expectedDescriptor = "some descriptor";
		memoryReportItemBuilder.setDescriptor(expectedDescriptor);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedDescriptor, memoryReportItem.getDescriptor());
	}

	@Test
	public void testGetId() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		int expectedId = 8888;
		memoryReportItemBuilder.setId(expectedId);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedId, memoryReportItem.getId());
	}

	@Test
	public void testGetItemClass() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		memoryReportItemBuilder.setItemClass(Integer.class);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(Integer.class, memoryReportItem.getItemClass());
	}

	@Test
	public void testGetParentId() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		int expectedParentId = 85674;
		memoryReportItemBuilder.setParentId(expectedParentId);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedParentId, memoryReportItem.getParentId());
	}

	@Test
	public void testGetReplicationId() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		ReplicationId expectedReplicationId = new ReplicationId(85674);
		memoryReportItemBuilder.setReplicationId(expectedReplicationId);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedReplicationId, memoryReportItem.getReplicationId());
	}

	@Test
	public void testGScenarioId() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		ScenarioId expectedScenarioId = new ScenarioId(85674);
		memoryReportItemBuilder.setScenarioId(expectedScenarioId);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedScenarioId, memoryReportItem.getScenarioId());
	}

	@Test
	public void testGetSelfByteCount() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		long expectedSelfByteCount = 7645345L;
		memoryReportItemBuilder.setSelfByteCount(expectedSelfByteCount);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedSelfByteCount, memoryReportItem.getSelfByteCount());
	}

	@Test
	public void testGetTime() {
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		double expectedTime = 7645.345;
		memoryReportItemBuilder.setTime(expectedTime);
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();
		assertEquals(expectedTime, memoryReportItem.getTime(), 0);
	}

	@Test
	public void testToString() {
		// boiler plate implementation
		MemoryReportItemBuilder memoryReportItemBuilder = getMemoryReportItemBuilder();
		MemoryReportItem memoryReportItem = memoryReportItemBuilder.build();

		String expectedString = "MemoryReportItem [scenarioId=97, replicationId=77, time=5346.0, descriptor=descriptor, parentId=45, id=67, selfByteCount=234, childByteCount=5678]";
		assertEquals(expectedString, memoryReportItem.toString());

	}
}
