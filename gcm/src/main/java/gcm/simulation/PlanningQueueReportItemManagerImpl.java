package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.Map;

import gcm.output.simstate.PlanningQueueReportItem;
import gcm.output.simstate.PlanningQueueReportItem.PlanningQueueReportItemBuilder;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.stats.MutableStat;
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class PlanningQueueReportItemManagerImpl extends BaseElement implements PlanningQueueReportItemManager {

	private static class Counter {

		private long count;
		private long additions;
		private long removals;
		private long cancellations;

		public long getAdditions() {
			return additions;
		}

		public long getRemovals() {
			return removals;
		}

		public long getCancellations() {
			return cancellations;
		}

		private final MutableStat mutableStat = new MutableStat();

		public void decrement(boolean fromCancellation) {
			if(fromCancellation) {
				cancellations++;
			}else {
				removals++;
			}
			if (count <= 0) {
				throw new RuntimeException("cannot decrement count");
			}
			count--;
			mutableStat.add(count);
		}

		public void increment() {
			additions++;
			count++;
			mutableStat.add(count);
		}

		public void clear() {
			additions = 0;
			removals = 0;
			cancellations = 0;
			mutableStat.clear();
		}
	}

	private OutputItemManager outputItemManager;

	private long planningQueueReportThreshold;

	private boolean active;

	private long actionCount;

	private double reportStartTime;

	private final Map<ComponentId, Map<Class<? extends Plan>, Map<Boolean, Counter>>> counterMap = new LinkedHashMap<>();

	private ScenarioId scenarioId;

	private ReplicationId replicationId;
	private EventManager eventManager;

	@Override
	public void close() {
		active = false;
		flush();
	}

	private void flush() {
		final double reportEndTime = eventManager.getTime();

		final PlanningQueueReportItemBuilder planningQueueReportItemBuilder = new PlanningQueueReportItemBuilder();

		for (final ComponentId componentId : counterMap.keySet()) {
			final Map<Class<? extends Plan>, Map<Boolean, Counter>> planningMap = counterMap.get(componentId);
			for (final Class<? extends Plan> planningClass : planningMap.keySet()) {
				final Map<Boolean, Counter> booleanMap = planningMap.get(planningClass);
				for (final Boolean isKeyed : booleanMap.keySet()) {
					final Counter counter = booleanMap.get(isKeyed);
					if (counter.mutableStat.size() > 0) {
						planningQueueReportItemBuilder.setComponentId(componentId);
						planningQueueReportItemBuilder.setEndTime(reportEndTime);
						planningQueueReportItemBuilder.setKeyed(isKeyed);
						planningQueueReportItemBuilder.setPlanningClass(planningClass);
						planningQueueReportItemBuilder.setReplicationId(replicationId);
						planningQueueReportItemBuilder.setScenarioId(scenarioId);
						planningQueueReportItemBuilder.setStartTime(reportStartTime);
						planningQueueReportItemBuilder.setStat(counter.mutableStat);
						planningQueueReportItemBuilder.setAdditionCount(counter.getAdditions());
						planningQueueReportItemBuilder.setRemovalCount(counter.getRemovals());
						planningQueueReportItemBuilder.setCancellationCount(counter.getCancellations());
						counter.clear();
						final PlanningQueueReportItem planningQueueReportItem = planningQueueReportItemBuilder.build();
						outputItemManager.releaseOutputItem(planningQueueReportItem);
					}
				}
			}
		}

		actionCount = 0;
		reportStartTime = reportEndTime;
	}

	private Counter getCounter(final ComponentId componentId, final Plan plan, final Object key) {
		Map<Class<? extends Plan>, Map<Boolean, Counter>> planTypeMap = counterMap.get(componentId);
		if (planTypeMap == null) {
			planTypeMap = new LinkedHashMap<>();
			counterMap.put(componentId, planTypeMap);
		}
		Map<Boolean, Counter> booleanMap = planTypeMap.get(plan.getClass());
		if (booleanMap == null) {
			booleanMap = new LinkedHashMap<>();
			planTypeMap.put(plan.getClass(), booleanMap);
		}
		final Boolean isKeyed = key != null;
		Counter counter = booleanMap.get(isKeyed);

		if (counter == null) {
			counter = new Counter();
			booleanMap.put(isKeyed, counter);
		}
		return counter;
	}

	@Override
	public void init(final Context context) {
		super.init(context);
		eventManager = context.getEventManager();
		planningQueueReportThreshold = context.getPlanningQueueReportThreshold();
		active = planningQueueReportThreshold > 0;
		scenarioId = context.getScenario().getScenarioId();
		replicationId = context.getReplication().getId();
		outputItemManager = context.getOutputItemManager();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void reportPlanningQueueAddition(final ComponentId componentId, final Plan plan, final Object key) {
		if (active) {
			final Counter counter = getCounter(componentId, plan, key);
			counter.increment();
			updateActionCount();
		}
	}

	@Override
	public void reportPlanningQueueRemoval(final ComponentId componentId, final Plan plan, final Object key) {
		if (active) {
			final Counter counter = getCounter(componentId, plan, key);
			counter.decrement(false);
			updateActionCount();
		}
	}
	@Override
	public void reportPlanningQueueCancellation(final ComponentId componentId, final Plan plan, final Object key) {
		if (active) {
			final Counter counter = getCounter(componentId, plan, key);
			counter.decrement(true);
			updateActionCount();
		}
	}

	private void updateActionCount() {
		actionCount++;
		if (actionCount >= planningQueueReportThreshold) {
			flush();
		}
	}
}
