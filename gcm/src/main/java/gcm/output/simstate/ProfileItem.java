package gcm.output.simstate;

import gcm.output.OutputItem;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.simulation.Context;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.stats.ImmutableStat;
import gcm.util.stats.Stat;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An {@link OutputItem} that records basic statistics for methods duration of
 * execution. Each ProfileItem is associated with the specific component that
 * was in focus when the method was invoked. The methods name and class name are
 * also recorded.
 * 
 * Each ProfileItem has an id and a parent id. The scope of these id values is
 * limited to the associated simulation run(i.e. the scenario and replication id
 * pair). For example, a method labeled with id=45 in
 * (scenario=2,replication=15) is not guaranteed to be the same method labeled
 * with id=45 in (scenario=3,replication=10). The parent id refers to the id of
 * the method that invoked the method of this ProfileItem. A parent id of -1
 * indicates that the method has been invoked directly from a component or the
 * Context.
 * 
 * Instances are constructed through the included builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.UNEXPECTED)
public final class ProfileItem implements OutputItem {

	/*
	 * 
	 * Container class for collecting the data for the ProfileItem
	 *
	 */
	private static class Scaffold {

		private int id;

		private int depth;

		private int parentId;

		private ComponentId componentId;

		private String className;

		private String methodName;

		private ScenarioId scenarioId;

		private ReplicationId replicationId;

		private Stat stat;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	/*
	 * 
	 * @throws RuntimeException
	 * 
	 * <li>if the component id is null
	 * 
	 * <li>if the class name is null
	 * 
	 * <li>if the method name is null
	 * 
	 * <li>if the scenario id is null
	 * 
	 * <li>if the replication id is null
	 * 
	 * <li>if the stat is null
	 *
	 */
	@NotThreadSafe
	public static class Builder {
		private Builder() {
			
		}
		private void validateScaffold() {
			if (scaffold.componentId == null) {
				throw new RuntimeException("component Id is null");
			}

			if (scaffold.className == null) {
				throw new RuntimeException("class name is null");
			}

			if (scaffold.methodName == null) {
				throw new RuntimeException("method Name  is null");
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

		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Builds the ProfileItem from the collected data.
		 * 
		 * @throws RuntimeException
		 * 
		 *             <li>if the component id has not been set
		 *             <li>if the class name has not been set
		 *             <li>if the method name has not been set
		 *             <li>if the scenario id has not been set
		 *             <li>if the replication id has not been set
		 *             <li>if the stat has not been set
		 *
		 */
		public ProfileItem build() {
			try {
				validateScaffold();
				return new ProfileItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the component id
		 * 
		 * @throws RuntimeException
		 *             if the component id is null
		 */
		public Builder setComponentId(ComponentId componentId) {
			if (componentId == null) {
				throw new RuntimeException("component Id is null");
			}
			scaffold.componentId = componentId;
			return this;
		}

		/**
		 * Sets the class name
		 * 
		 * @throws RuntimeException
		 *             if the class name is null
		 */
		public Builder setClassName(String className) {
			if (className == null) {
				throw new RuntimeException("class name is null");
			}
			scaffold.className = className;
			return this;
		}

		/**
		 * Sets the method name
		 * 
		 * @throws RuntimeException
		 *             if the method name is null
		 */
		public Builder setMethodName(String methodName) {
			if (methodName == null) {
				throw new RuntimeException("method Name  is null");
			}
			scaffold.methodName = methodName;
			return this;
		}

		/**
		 * Sets the scenario id
		 * 
		 * @throws RuntimeException
		 *             if the scenario id is null
		 */
		public Builder setScenarioId(ScenarioId scenarioId) {
			if (scenarioId == null) {
				throw new RuntimeException("scenario Id is null");
			}
			scaffold.scenarioId = scenarioId;
			return this;
		}

		/**
		 * Sets the replication id
		 * 
		 * @throws RuntimeException
		 *             if the replication id is null
		 */
		public Builder setReplicationId(ReplicationId replicationId) {
			if (replicationId == null) {
				throw new RuntimeException("replication Id is null");
			}
			scaffold.replicationId = replicationId;
			return this;
		}

		/**
		 * Sets the {@link Stat}
		 * 
		 * @throws RuntimeException
		 *             if the stat is null
		 */
		public Builder setStat(Stat stat) {
			ImmutableStat.Builder builder = ImmutableStat.builder();
			builder.setMin(stat.getMin().get());
			builder.setMax(stat.getMax().get());
			builder.setMean(stat.getMean().get());
			builder.setVariance(stat.getVariance().get());
			builder.setSize(stat.size());
			scaffold.stat = builder.build();
			return this;
		}

		/**
		 * Sets the id
		 * 
		 * @throws RuntimeException
		 *             if the id is negative
		 * 
		 */
		public Builder setId(int id) {
			if (id < 0) {
				throw new RuntimeException("negative id");
			}
			scaffold.id = id;
			return this;
		}

		/**
		 * Sets the depth
		 * 
		 * 
		 * @throws RuntimeException
		 *             if the depth is negative
		 * 
		 */
		public Builder setDepth(int depth) {
			if (depth < 0) {
				throw new RuntimeException("negative depth");
			}
			scaffold.depth = depth;
			return this;
		}

		/**
		 * Sets the parent id
		 * 
		 * @throws RuntimeException
		 *             if the parent id is less than -1
		 * 
		 */
		public Builder setParentId(int parentId) {
			if (parentId < -1) {
				throw new RuntimeException("unallowed parent id");
			}
			scaffold.parentId = parentId;
			return this;
		}

	}

	private ProfileItem(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	private final Scaffold scaffold;

	/**
	 * Returns the id for this profile item. Id values are only valid within the
	 * scope of a single simulation execution.
	 */
	public int getId() {
		return scaffold.id;
	}

	/**
	 * Returns the depth of this profile item in the parent-child tree formed by
	 * related profile items associated with the particular simulation execution
	 * that created them. A depth of zero indicates the method was not invoked
	 * by another tracked method and was instead invoked directly by either a
	 * component or the {@link Context}
	 */
	public int getDepth() {
		return scaffold.depth;
	}

	/**
	 * Returns the parent id for this profile item. Id values are only valid
	 * within the scope of a single simulation execution. A parent id of -1
	 * indicates that the method was not invoked by another tracked method and
	 * was instead invoked directly by either a component or the {@link Context}
	 */
	public int getParentId() {
		return scaffold.parentId;
	}

	/**
	 * Returns the component id of the Component that held focus when the
	 * profile items method was invoked. When no component holds focus, such as
	 * when the simulation is initializing, the coponent is identified as "GCM"
	 */
	public ComponentId getComponentId() {
		return scaffold.componentId;
	}

	/**
	 * Returns the class name of the method represented by this profile item.
	 */
	public String getClassName() {
		return scaffold.className;
	}

	/**
	 * Returns the name of the method represented by this profile item.
	 */
	public String getMethodName() {
		return scaffold.methodName;
	}

	/**
	 * Returns the {@link Stat} for this profile item. The stat aggregates the
	 * duration of the methods invocation.
	 */
	public Stat getStat() {
		return scaffold.stat;
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
	 * Returns a boilerplate string representation in the form:
	 * 
	 * ProfileItem [scenarioId=1, replicationId=2,id=3, depth=4,
	 * componentId=ABC, className=xyz, methodName=name,stat=stat, parentId=24]
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProfileItem [scenarioId=");
		builder.append(scaffold.scenarioId);
		builder.append(", replicationId=");
		builder.append(scaffold.replicationId);
		builder.append(", id=");
		builder.append(scaffold.id);
		builder.append(", depth=");
		builder.append(scaffold.depth);
		builder.append(", componentId=");
		builder.append(scaffold.componentId);
		builder.append(", className=");
		builder.append(scaffold.className);
		builder.append(", methodName=");
		builder.append(scaffold.methodName);
		builder.append(", stat=");
		builder.append(scaffold.stat);
		builder.append(", parentId=");
		builder.append(scaffold.parentId);
		builder.append("]");
		return builder.toString();
	}

}
