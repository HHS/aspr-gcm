package gcm.output.simstate;

import gcm.output.OutputItem;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.simulation.Plan;
import gcm.util.annotations.Source;
import gcm.util.stats.Stat;
import gcm.util.stats.ImmutableStat.ImmutableStatBuilder;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An {@link OutputItem} that records basic statistics for a subset of the
 * planning queue defined by 1) the component that issued/received the plan, 2)
 * the class type of the plan and 3) whether the plan was associated with a key.
 * 
 * Instances are constructed through the included builder class.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
@Source
public final class PlanningQueueReportItem implements OutputItem {

	private static class Scaffold {
		private ScenarioId scenarioId;
		private ReplicationId replicationId;
		private ComponentId componentId;
		private Class<? extends Plan> planningClass;
		private Boolean keyed;
		private Stat stat;
		private Double startTime;
		private Double endTime;
		private long additionCount;
		private long removalCount;
		private long cancellationCount;
	}

	/*
	 * 
	 * @throws RuntimeException <li>if the component id is null <li>if the
	 * planning class is null <li>if the scenario id is null <li>if the
	 * replication id is null <li>if the stat is null <li>if the keyed boolean
	 * is null <li>if the start time is null <li>if the end time is null <li>if
	 * the start time is greater than the end time
	 * 
	 *
	 */
	@NotThreadSafe
	public static class PlanningQueueReportItemBuilder {
		private Scaffold scaffold = new Scaffold();

		private void validateScaffold() {
			if (scaffold.componentId == null) {
				throw new RuntimeException("component Id is null");
			}

			if (scaffold.planningClass == null) {
				throw new RuntimeException("planning class is null");
			}

			if (scaffold.scenarioId == null) {
				throw new RuntimeException("scenario Id is null");
			}

			if (scaffold.replicationId == null) {
				throw new RuntimeException("replication Id is null");
			}

			if (scaffold.stat == null) {
				throw new RuntimeException("stat is null");
			}

			if (scaffold.keyed == null) {
				throw new RuntimeException("keyed is null");
			}

			if (scaffold.startTime == null) {
				throw new RuntimeException("startTime is null");
			}
			if (scaffold.endTime == null) {
				throw new RuntimeException("endTime is null");
			}
			if (scaffold.endTime < scaffold.startTime) {
				throw new RuntimeException("end Time < start Time");
			}
			
			if (scaffold.additionCount < 0) {
				throw new RuntimeException("addition count is negative");
			}
			if (scaffold.removalCount < 0) {
				throw new RuntimeException("removal count is negative");
			}
			if (scaffold.cancellationCount < 0) {
				throw new RuntimeException("cancellation count is negative");
			}

		}

		/**
		 * Builds the {@link PlanningQueueReportItem}
		 * 
		 * @throws RuntimeException
		 *             <li>if the component id was not set
		 *             <li>if the planning class was not set
		 *             <li>if the scenario id was not set
		 *             <li>if the replication was not set
		 *             <li>if the stat was not set
		 *             <li>if the keyed boolean was not set
		 *             <li>if the start time was not set
		 *             <li>if the end time was not set
		 *             <li>if the start time is greater than the end time
		 */
		public PlanningQueueReportItem build() {
			try {
				validateScaffold();
				return new PlanningQueueReportItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}

		}

		/**
		 * Sets the scenario id
		 * 
		 * @throws RuntimeException
		 *             if the scenario id is null
		 */
		public void setScenarioId(ScenarioId scenarioId) {
			if (scenarioId == null) {
				throw new RuntimeException("scenario Id is null");
			}
			scaffold.scenarioId = scenarioId;
		}

		/**
		 * Sets the replication id
		 * 
		 * @throws RuntimeException
		 *             if the replication id is null
		 */
		public void setReplicationId(ReplicationId replicationId) {
			if (replicationId == null) {
				throw new RuntimeException("replication Id is null");
			}
			scaffold.replicationId = replicationId;
		}

		/**
		 * Sets the component id
		 * 
		 * @throws RuntimeException
		 *             if the component id is null
		 */
		public void setComponentId(ComponentId componentId) {
			if (componentId == null) {
				throw new RuntimeException("component Id is null");
			}
			scaffold.componentId = componentId;
		}

		/**
		 * Sets the planning class
		 * 
		 * @throws RuntimeException
		 *             if the planning class is null
		 */
		public void setPlanningClass(Class<? extends Plan> planningClass) {
			if (planningClass == null) {
				throw new RuntimeException("planning class is null");
			}
			scaffold.planningClass = planningClass;
		}

		/**
		 * Sets the keyed value
		 * 
		 */
		public void setKeyed(boolean keyed) {
			scaffold.keyed = keyed;
		}

		/**
		 * Sets the {@link Stat}
		 * 
		 * @throws RuntimeException
		 *             if the stat is null
		 * 
		 */
		public void setStat(Stat stat) {
			if (stat == null) {
				throw new RuntimeException("stat is null");
			}
			ImmutableStatBuilder immutableStatBuilder = new ImmutableStatBuilder();
			if (stat.size() > 0) {
				immutableStatBuilder.setMin(stat.getMin().get());
				immutableStatBuilder.setMax(stat.getMax().get());
				immutableStatBuilder.setMean(stat.getMean().get());
				immutableStatBuilder.setVariance(stat.getVariance().get());
				immutableStatBuilder.setSize(stat.size());
			}
			scaffold.stat = immutableStatBuilder.build();
		}
		
		public void setAdditionCount(long additionCount) {
			scaffold.additionCount = additionCount;
		}
		
		public void setRemovalCount(long removalCount) {
			scaffold.removalCount = removalCount;
		}

		public void setCancellationCount(long cancellationCount) {
			scaffold.cancellationCount = cancellationCount;
		}		

		/**
		 * Sets the start time
		 * 
		 * @throws RuntimeException
		 *             if the startTime is null
		 * 
		 */
		public void setStartTime(Double startTime) {
			if (startTime == null) {
				throw new RuntimeException("startTime is null");
			}

			scaffold.startTime = startTime;
		}

		/**
		 * Sets the end time
		 * 
		 * @throws RuntimeException
		 *             if the endTime is null
		 * 
		 */
		public void setEndTime(Double endTime) {
			if (endTime == null) {
				throw new RuntimeException("endTime is null");
			}
			scaffold.endTime = endTime;
		}
	}

	private PlanningQueueReportItem(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	private final Scaffold scaffold;

	/**
	 * Returns the start time for the recording of the portion of the size of
	 * the queue that is attributable to the component id, plan type and
	 * presence of a key.
	 */
	public double getStartTime() {
		return scaffold.startTime;
	}

	/**
	 * Returns the end time for the recording of the portion of the size of the
	 * queue that is attributable to the component id, plan type and presence of
	 * a key.
	 */

	public double getEndTime() {
		return scaffold.endTime;
	}

	@Override
	public ScenarioId getScenarioId() {
		return scaffold.scenarioId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return scaffold.replicationId;
	}

	/**
	 * Returns the component id for the portion of the planning queue
	 * represented by this {@link PlanningQueueReportItem}
	 */
	public ComponentId getComponentId() {
		return scaffold.componentId;
	}

	/**
	 * Returns the planning class type for the portion of the planning queue
	 * represented by this {@link PlanningQueueReportItem}
	 */
	public Class<? extends Plan> getPlanningClass() {
		return scaffold.planningClass;
	}

	/**
	 * Returns true if and only if the portion of the planning queue represented
	 * by this {@link PlanningQueueReportItem} had associated key values
	 */
	public boolean isKeyed() {
		return scaffold.keyed;
	}

	/**
	 * Returns the {@link Stat} instance for the portion of the planning queue
	 * represented by this {@link PlanningQueueReportItem}. The stat represents
	 * the queue size that can be attributed to the component id, plan type and
	 * presence of a key for this {@link PlanningQueueReportItem}
	 */

	public Stat getStat() {
		return scaffold.stat;
	}

	/**
	 * Returns the number of additions to the planning queue represented by this
	 * {@link PlanningQueueReportItem}
	 */
	public long getAdditionCount() {
		return scaffold.additionCount;
	}

	/**
	 * Returns the number of removals from the planning queue represented by
	 * this {@link PlanningQueueReportItem}
	 */
	public long getRemovalCount() {
		return scaffold.removalCount;
	}

	/**
	 * Returns the number of cancellation from the planning queue represented by
	 * this {@link PlanningQueueReportItem}
	 */
	public long getCancellationCount() {
		return scaffold.cancellationCount;
	}

}
