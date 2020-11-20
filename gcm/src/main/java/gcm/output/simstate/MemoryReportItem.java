package gcm.output.simstate;

import gcm.output.OutputItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * An OutputItem that records the number of bytes allocated to parts of GCM.
 * Construction is executed via the contained builder class.
 * 
 * @author Shawn Hatch
 *
 */

@ThreadSafe
@Source
public final class MemoryReportItem implements OutputItem {

	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Builder class for {@link MemoryReportItem}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	@NotThreadSafe
	@Source(status = TestStatus.REQUIRED, proxy = MemoryReportItem.class)
	public static class Builder {
		
		private Builder() {}
		
		private Scaffold scaffold = new Scaffold();

		/**
		 * Returns the built {@link MemoryReportItem}
		 * 
		 * @throws RuntimeException
		 *             <li>if the scenario id was not set
		 *             <li>if the replication id was not set
		 *             <li>if the item class was not set *
		 */
		public MemoryReportItem build() {
			try {
				validateScaffold();
				return new MemoryReportItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * @throws RuntimeException
		 *             <li>if the scenario id is null
		 *             <li>if the replication id is null
		 *             <li>if the item class is null
		 */
		private void validateScaffold() {

			if (scaffold.scenarioId == null) {
				throw new RuntimeException("null scenarioId");
			}

			if (scaffold.replicationId == null) {
				throw new RuntimeException("null replicationId");
			}

			if (scaffold.itemClass == null) {
				throw new RuntimeException("null item class");
			}
		}

		/**
		 * Sets the self byte count
		 * 
		 * @throws RuntimeException
		 *             if selfByteCount is negative. Default value is zero.
		 */
		public Builder setSelfByteCount(final long selfByteCount) {
			if (selfByteCount < 0) {
				throw new RuntimeException("negative self byte count");
			}
			scaffold.selfByteCount = selfByteCount;
			return this;
		}

		/**
		 * Sets the child byte count. Default value is zero.
		 * 
		 * @throws RuntimeException
		 *             if childByteCount is negative
		 */
		public Builder setChildByteCount(final long childByteCount) {

			if (childByteCount < 0) {
				throw new RuntimeException("negative child byte count");
			}
			scaffold.childByteCount = childByteCount;
			return this;
		}

		/**
		 * Sets the descriptor. Default value is ""
		 * 
		 * @throws RuntimeException
		 *             if the descriptor is null
		 */
		public Builder setDescriptor(final String descriptor) {
			if (descriptor == null) {
				throw new RuntimeException("null descriptor");
			}
			scaffold.descriptor = descriptor;
			return this;
		}

		/**
		 * Sets the item class. Default value is null
		 * 
		 * @throws RuntimeException
		 *             if the itemClass is null
		 */
		public Builder setItemClass(Class<?> itemClass) {
			if (itemClass == null) {
				throw new RuntimeException("null item class");
			}
			scaffold.itemClass = itemClass;
			return this;
		}

		/**
		 * Set the id. Default value is 0.
		 * 
		 * @throws RuntimeException
		 *             if the value is negative
		 * 
		 */
		public Builder setId(final int id) {
			if (id < 0) {
				throw new RuntimeException("negative id");
			}
			scaffold.id = id;
			return this;
		}

		/**
		 * Set the parent id. Default value is 0. To indicate that the item has
		 * no parent item, use parentId = -1.
		 * 
		 * @throws RuntimeException
		 *             if the value less than -1
		 * 
		 */
		public Builder setParentId(final int parentId) {
			if (scaffold.parentId < -1) {
				throw new RuntimeException("illegal parent id");
			}
			scaffold.parentId = parentId;
			return this;
		}

		/**
		 * Sets the time.
		 * 
		 * @throws RuntimeException
		 *             if the time is negative
		 */
		public Builder setTime(final double time) {
			if (time < 0) {
				throw new RuntimeException("negative time value");
			}
			scaffold.time = time;
			return this;
		}

		/**
		 * Sets the {@link ScenarioId}.
		 * 
		 * @throws RuntimeException
		 *             if the scenario id is null
		 */
		public Builder setScenarioId(ScenarioId scenarioId) {

			if (scenarioId == null) {
				throw new RuntimeException("null scenarioId");
			}

			scaffold.scenarioId = scenarioId;
			return this;
		}

		/**
		 * Sets the {@link ReplicationId}
		 * 
		 * @throws RuntimeException
		 *             if the replication id is null
		 */
		public Builder setReplicationId(ReplicationId replicationId) {
			if (replicationId == null) {
				throw new RuntimeException("null replicationId");
			}

			scaffold.replicationId = replicationId;
			return this;
		}
	}

	/*
	 * Container for the collected data of a MemoryReportItem
	 */
	private static class Scaffold {
		private double time;
		private String descriptor = "";
		private Class<?> itemClass;
		private int parentId;
		private int id;
		private long selfByteCount;
		private long childByteCount;
		private ScenarioId scenarioId;
		private ReplicationId replicationId;

	}

	private final ScenarioId scenarioId;

	private final ReplicationId replicationId;

	private final double time;

	private final String descriptor;

	private final Class<?> itemClass;

	private final int parentId;

	private final int id;

	private final long selfByteCount;
	private final long childByteCount;

	private MemoryReportItem(final Scaffold scaffold) {
		time = scaffold.time;
		descriptor = scaffold.descriptor;
		itemClass = scaffold.itemClass;
		parentId = scaffold.parentId;
		id = scaffold.id;
		selfByteCount = scaffold.selfByteCount;
		childByteCount = scaffold.childByteCount;
		scenarioId = scaffold.scenarioId;
		replicationId = scaffold.replicationId;

	}

	public long getSelfByteCount() {
		return selfByteCount;
	}

	public long getChildByteCount() {
		return childByteCount;
	}

	public Class<?> getItemClass() {
		return itemClass;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public int getId() {
		return id;
	}

	public int getParentId() {
		return parentId;
	}

	public double getTime() {
		return time;
	}

	@Override
	public ScenarioId getScenarioId() {
		return scenarioId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return replicationId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MemoryReportItem [scenarioId=");
		builder.append(scenarioId);
		builder.append(", replicationId=");
		builder.append(replicationId);
		builder.append(", time=");
		builder.append(time);
		builder.append(", descriptor=");
		builder.append(descriptor);
		builder.append(", parentId=");
		builder.append(parentId);
		builder.append(", id=");
		builder.append(id);
		builder.append(", selfByteCount=");
		builder.append(selfByteCount);
		builder.append(", childByteCount=");
		builder.append(childByteCount);
		builder.append("]");
		return builder.toString();
	}
}
