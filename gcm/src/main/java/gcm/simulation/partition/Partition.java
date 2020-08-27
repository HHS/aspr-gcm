package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

public final class Partition {

	private Partition() {
	}

	private Function<GroupTypeCountMap, Object> groupPartitionFunction;

	private Function<CompartmentId, Object> compartmentPartitionFunction;

	private Function<RegionId, Object> regionPartitionFunction;

	private Map<PersonPropertyId, Function<Object, Object>> personPropertyPartitionFunctions = new LinkedHashMap<>();

	private Map<ResourceId, Function<Long, Object>> personResourcePartitionFunctions = new LinkedHashMap<>();

	public static Builder builder() {
		return new Builder();
	}

	public Function<GroupTypeCountMap, Object> getGroupPartitionFunction() {
		return groupPartitionFunction;
	}

	public Function<CompartmentId, Object> getCompartmentPartitionFunction() {
		return compartmentPartitionFunction;
	}

	public Function<RegionId, Object> getRegionPartitionFunction() {
		return regionPartitionFunction;
	}

	public Function<Object, Object> getPersonPropertyPartitionFunction(PersonPropertyId personPropertyId) {
		return personPropertyPartitionFunctions.get(personPropertyId);
	}

	public Function<Long, Object> getPersonResourcePartitionFunction(ResourceId resourceId) {
		return personResourcePartitionFunctions.get(resourceId);
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with this
	 * {@link Partition}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(personResourcePartitionFunctions.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link Partition}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(personPropertyPartitionFunctions.keySet());
	}

	@Source(status = TestStatus.REQUIRED, proxy = Partition.class)
	public static final class Builder {

		private Partition partition = new Partition();

		// hidden constructor
		private Builder() {

		}

		public Partition build() {
			try {
				return partition;
			} finally {
				partition = new Partition();
			}
		}

		public Builder setGroupPartitionFunction(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
			partition.groupPartitionFunction = groupPartitionFunction;
			return this;
		}

		public Builder setCompartmentPartition(Function<CompartmentId, Object> compartmentPartitionFunction) {
			partition.compartmentPartitionFunction = compartmentPartitionFunction;
			return this;
		}

		public Builder setRegionPartition(Function<RegionId, Object> regionPartitionFunction) {
			partition.regionPartitionFunction = regionPartitionFunction;
			return this;
		}

		public Builder setPersonPropertyPartition(PersonPropertyId personPropertyId,
				Function<Object, Object> personPropertyPartitionFunction) {
			partition.personPropertyPartitionFunctions.put(personPropertyId,
					personPropertyPartitionFunction);
			return this;
		}

		public Builder setPersonResourcePartition(ResourceId resourceId,
				Function<Long, Object> personResourcePartitionFunction) {
			partition.personResourcePartitionFunctions.put(resourceId,
					personResourcePartitionFunction);
			return this;
		}

	}
}