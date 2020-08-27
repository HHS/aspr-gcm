package gcm.simulation;

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

public final class PopulationPartitionDefinition {

	private PopulationPartitionDefinition() {
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
	 * {@link PopulationPartitionDefinition}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(personResourcePartitionFunctions.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link PopulationPartitionDefinition}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(personPropertyPartitionFunctions.keySet());
	}

	@Source(status = TestStatus.REQUIRED, proxy = PopulationPartitionDefinition.class)
	public static final class Builder {

		private PopulationPartitionDefinition populationPartitionDefinition = new PopulationPartitionDefinition();

		// hidden constructor
		private Builder() {

		}

		public PopulationPartitionDefinition build() {
			try {
				return populationPartitionDefinition;
			} finally {
				populationPartitionDefinition = new PopulationPartitionDefinition();
			}
		}

		public Builder setGroupPartitionFunction(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
			populationPartitionDefinition.groupPartitionFunction = groupPartitionFunction;
			return this;
		}

		public Builder setCompartmentPartition(Function<CompartmentId, Object> compartmentPartitionFunction) {
			populationPartitionDefinition.compartmentPartitionFunction = compartmentPartitionFunction;
			return this;
		}

		public Builder setRegionPartition(Function<RegionId, Object> regionPartitionFunction) {
			populationPartitionDefinition.regionPartitionFunction = regionPartitionFunction;
			return this;
		}

		public Builder setPersonPropertyPartition(PersonPropertyId personPropertyId,
				Function<Object, Object> personPropertyPartitionFunction) {
			populationPartitionDefinition.personPropertyPartitionFunctions.put(personPropertyId,
					personPropertyPartitionFunction);
			return this;
		}

		public Builder setPersonResourcePartition(ResourceId resourceId,
				Function<Long, Object> personResourcePartitionFunction) {
			populationPartitionDefinition.personResourcePartitionFunctions.put(resourceId,
					personResourcePartitionFunction);
			return this;
		}

	}
}