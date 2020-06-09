package gcm.test.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.output.simstate.PlanningQueueReportItem;
import gcm.output.simstate.PlanningQueueReportItem.PlanningQueueReportItemBuilder;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.simulation.Plan;
import gcm.test.support.SimpleStat;
import gcm.util.annotations.UnitTest;
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

	private PlanningQueueReportItemBuilder getPlanningQueueReportItemBuilder() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = new PlanningQueueReportItemBuilder();
		planningQueueReportItemBuilder.setComponentId(new ComponentId() {
		});
		planningQueueReportItemBuilder.setEndTime(1000d);
		planningQueueReportItemBuilder.setKeyed(true);
		planningQueueReportItemBuilder.setPlanningClass(TestPlan.class);
		planningQueueReportItemBuilder.setReplicationId(new ReplicationId(45));
		planningQueueReportItemBuilder.setScenarioId(new ScenarioId(99));
		planningQueueReportItemBuilder.setStartTime(100d);
		SimpleStat simpleStat = new SimpleStat();
		simpleStat.add(4.5);
		simpleStat.add(6.5);
		simpleStat.add(4.8);
		planningQueueReportItemBuilder.setStat(simpleStat);
		return planningQueueReportItemBuilder;
	}

	@Test
	public void testGetComponentId() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		ComponentId componentId = new ComponentId() {
		};
		planningQueueReportItemBuilder.setComponentId(componentId);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(componentId, planningQueueReportItem.getComponentId());
	}

	@Test
	public void testGetEndTime() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		double endTime = 53445;
		planningQueueReportItemBuilder.setEndTime(endTime);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(endTime, planningQueueReportItem.getEndTime(), 0);
	}

	@Test
	public void testGetPlanningClass() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(TestPlan.class, planningQueueReportItem.getPlanningClass());
	}

	@Test
	public void testGetReplicationId() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		ReplicationId replicationId = new ReplicationId(766);
		planningQueueReportItemBuilder.setReplicationId(replicationId);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(replicationId, planningQueueReportItem.getReplicationId());
	}

	@Test
	public void testGetScenarioId() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		ScenarioId scenarioId = new ScenarioId(236);
		planningQueueReportItemBuilder.setScenarioId(scenarioId);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(scenarioId, planningQueueReportItem.getScenarioId());
	}

	@Test
	public void testGetStartTime() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		double startTime = 200.4;
		planningQueueReportItemBuilder.setStartTime(startTime);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(startTime, planningQueueReportItem.getStartTime(), 0);
	}

	@Test
	public void testGetStat() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();

		// first test with an empty stat
		SimpleStat expectedStat = new SimpleStat();

		planningQueueReportItemBuilder.setStat(expectedStat);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
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

		planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		planningQueueReportItemBuilder.setStat(expectedStat);
		planningQueueReportItem = planningQueueReportItemBuilder.build();
		actualStat = planningQueueReportItem.getStat();

		assertEquals(expectedStat.getMean().get(), actualStat.getMean().get(), 0);
		assertEquals(expectedStat.getVariance().get(), actualStat.getVariance().get(), 0);
		assertEquals(expectedStat.getStandardDeviation().get(), actualStat.getStandardDeviation().get(), 0);
		assertEquals(expectedStat.getMax().get(), actualStat.getMax().get(), 0);
		assertEquals(expectedStat.getMin().get(), actualStat.getMin().get(), 0);

	}

	@Test
	public void testIsKeyed() {
		PlanningQueueReportItemBuilder planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();

		boolean isKeyed = true;
		planningQueueReportItemBuilder.setKeyed(isKeyed);
		PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(isKeyed, planningQueueReportItem.isKeyed());

		planningQueueReportItemBuilder = getPlanningQueueReportItemBuilder();
		isKeyed = false;
		planningQueueReportItemBuilder.setKeyed(isKeyed);
		planningQueueReportItem = planningQueueReportItemBuilder.build();
		assertEquals(isKeyed, planningQueueReportItem.isKeyed());
	}
}
