package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gcm.automated.support.SimpleStat;
import gcm.output.simstate.PlanningQueueReportItem;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.simulation.Plan;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.stats.Stat;

/**
 * Test for {@link PlanningQueueReportItem}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PlanningQueueReportItem.class)
public class AT_PlanningQueueReportItem {

	private static class TestPlan implements Plan {
	}

	private PlanningQueueReportItem.Builder getPlanningQueueReportItemBuilder() {
		SimpleStat simpleStat = new SimpleStat();
		simpleStat.add(4.5);
		simpleStat.add(6.5);
		simpleStat.add(4.8);

		PlanningQueueReportItem.Builder planningQueueReportItemBuilder = PlanningQueueReportItem.builder()//
				.setComponentId(new ComponentId() {
				})//
				.setEndTime(1000d)//
				.setKeyed(true)//
				.setPlanningClass(TestPlan.class)//
				.setReplicationId(new ReplicationId(45))//
				.setScenarioId(new ScenarioId(99))//
				.setStartTime(100d)//
				.setStat(simpleStat);//

		return planningQueueReportItemBuilder;
	}

	/**
	 * Tests {@link PlanningQueueReportItem#getComponentId()}
	 */
	@Test
	@UnitTestMethod(name = "getComponentId", args = {})
	public void testGetComponentId() {

		ComponentId componentId = new ComponentId() {
		};

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder()
				.setComponentId(componentId).build();

		assertEquals(componentId, planningQueueReportItem.getComponentId());
	}

	/**
	 * Tests {@link PlanningQueueReportItem#getEndTime()}
	 */
	@Test
	@UnitTestMethod(name = "getEndTime", args = {})
	public void testGetEndTime() {

		double endTime = 53445;
		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().setEndTime(endTime)
				.build();

		assertEquals(endTime, planningQueueReportItem.getEndTime(), 0);
	}

	/**
	 * Test {@link PlanningQueueReportItem#getPlanningClass()}
	 */
	@Test
	@UnitTestMethod(name = "getPlanningClass", args = {})
	public void testGetPlanningClass() {
		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().build();
		assertEquals(TestPlan.class, planningQueueReportItem.getPlanningClass());
	}

	/**
	 * Test {@link PlanningQueueReportItem#getReplicationId()}
	 */
	@Test
	@UnitTestMethod(name = "getReplicationId", args = {})
	public void testGetReplicationId() {
		ReplicationId replicationId = new ReplicationId(766);

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder()
				.setReplicationId(replicationId).build();

		assertEquals(replicationId, planningQueueReportItem.getReplicationId());
	}

	/**
	 * Tests {@link PlanningQueueReportItem#getAdditionCount()}
	 */
	@Test
	@UnitTestMethod(name = "getAdditionCount", args = {})
	public void testGetAdditionCount() {

		long additionCount = 34534645745L;

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder()
				.setAdditionCount(additionCount).build();

		assertEquals(additionCount, planningQueueReportItem.getAdditionCount());
	}

	/**
	 * Test {@link PlanningQueueReportItem#getRemovalCount()}
	 */
	@Test
	@UnitTestMethod(name = "getRemovalCount", args = {})
	public void testGetRemovalCount() {

		long removalCount = 34534645745L;

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder()
				.setRemovalCount(removalCount).build();

		assertEquals(removalCount, planningQueueReportItem.getRemovalCount());
	}

	/**
	 * Tests {@link PlanningQueueReportItem#getCancellationCount()}
	 */
	@Test
	@UnitTestMethod(name = "getCancellationCount", args = {})
	public void testGetCancellationCount() {
		long cancellationCount = 34534645745L;

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder()
				.setCancellationCount(cancellationCount).build();

		assertEquals(cancellationCount, planningQueueReportItem.getCancellationCount());
	}

	/**
	 * Test {@link PlanningQueueReportItem#getScenarioId()}
	 */
	@Test
	@UnitTestMethod(name = "getScenarioId", args = {})
	public void testGetScenarioId() {
		ScenarioId scenarioId = new ScenarioId(236);

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().setScenarioId(scenarioId)
				.build();

		assertEquals(scenarioId, planningQueueReportItem.getScenarioId());
	}

	/**
	 * Test {@link PlanningQueueReportItem#getStartTime()}
	 */
	@Test
	@UnitTestMethod(name = "getStartTime", args = {})
	public void testGetStartTime() {
		double startTime = 200.4;
		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().setStartTime(startTime)
				.build();
		assertEquals(startTime, planningQueueReportItem.getStartTime(), 0);
	}

	/**
	 * Test {@link PlanningQueueReportItem#getStat()}
	 */
	@Test
	@UnitTestMethod(name = "getStat", args = {})
	public void testGetStat() {

		// first test with an empty stat
		SimpleStat expectedStat = new SimpleStat();

		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().setStat(expectedStat)
				.build();

		Stat actualStat = planningQueueReportItem.getStat();

		assertEquals(expectedStat.getMean().isPresent(), actualStat.getMean().isPresent());
		assertEquals(expectedStat.getVariance().isPresent(), actualStat.getVariance().isPresent());
		assertEquals(expectedStat.getStandardDeviation().isPresent(), actualStat.getStandardDeviation().isPresent());
		assertEquals(expectedStat.getMax().isPresent(), actualStat.getMax().isPresent());
		assertEquals(expectedStat.getMin().isPresent(), actualStat.getMin().isPresent());

		// Second, use values
		expectedStat = new SimpleStat();
		expectedStat.add(3.45);
		expectedStat.add(7.99);
		expectedStat.add(4.88);
		expectedStat.add(6.7);
		expectedStat.add(6.3);

		planningQueueReportItem = getPlanningQueueReportItemBuilder().setStat(expectedStat).build();

		actualStat = planningQueueReportItem.getStat();

		assertEquals(expectedStat.getMean().get(), actualStat.getMean().get(), 0);
		assertEquals(expectedStat.getVariance().get(), actualStat.getVariance().get(), 0);
		assertEquals(expectedStat.getStandardDeviation().get(), actualStat.getStandardDeviation().get(), 0);
		assertEquals(expectedStat.getMax().get(), actualStat.getMax().get(), 0);
		assertEquals(expectedStat.getMin().get(), actualStat.getMin().get(), 0);

	}

	/**
	 * Test {@link PlanningQueueReportItem#isKeyed()}
	 */
	@Test
	@UnitTestMethod(name = "isKeyed", args = {})
	public void testIsKeyed() {

		boolean isKeyed = true;
		PlanningQueueReportItem planningQueueReportItem = getPlanningQueueReportItemBuilder().setKeyed(isKeyed).build();

		assertEquals(isKeyed, planningQueueReportItem.isKeyed());

		isKeyed = false;
		planningQueueReportItem = getPlanningQueueReportItemBuilder().setKeyed(isKeyed).build();

		assertEquals(isKeyed, planningQueueReportItem.isKeyed());
	}
}
