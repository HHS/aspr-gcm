package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED)

public class Partition {

	public static Builder builder() {
		return new Builder();
	}

	@Source(proxy = Partition.class)
	public static class Builder {
		
		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		public Partition build() {
			try {
				return new Partition(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
		
		public Builder setGroupFunction(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
			scaffold.groupPartitionFunction = groupPartitionFunction;
			return this;
		}
		
		public Builder setCompartmentFunction(Function<CompartmentId, Object> compartmentPartitionFunction) {
			scaffold.compartmentPartitionFunction = compartmentPartitionFunction;
			return this;
		}
		
		public Builder setRegionFunction(Function<RegionId, Object> regionPartitionFunction) {
			scaffold.regionPartitionFunction = regionPartitionFunction;
			return this;
		}

		public Builder setPersonPropertyFunction(PersonPropertyId personPropertyId, Function<Object, Object> personPropertyFunction) {
			scaffold.personPropertyPartitionFunctions.put(personPropertyId, personPropertyFunction);
			return this;
		}

		public Builder setPersonResourceFunction(ResourceId resourceId, Function<Long, Object> personResourceFunction) {
			scaffold.personResourcePartitionFunctions.put(resourceId, personResourceFunction);
			return this;
		}
		
		public Builder setFilter(Filter filter) {
			scaffold.filter = filter;
			return this;
		}
		
		
	}

	private Partition(Scaffold scaffold) {

		this.groupPartitionFunction = scaffold.groupPartitionFunction;

		this.compartmentPartitionFunction = scaffold.compartmentPartitionFunction;

		this.regionPartitionFunction = scaffold.regionPartitionFunction;

		this.personPropertyPartitionFunctions = scaffold.personPropertyPartitionFunctions;

		this.personResourcePartitionFunctions = scaffold.personResourcePartitionFunctions;
		
		this.filter = scaffold.filter;

		degenerate = (regionPartitionFunction == null) && (compartmentPartitionFunction == null)
				&& (groupPartitionFunction == null) && personPropertyPartitionFunctions.isEmpty()
				&& personResourcePartitionFunctions.isEmpty();

	}

	public boolean isDegenerate() {
		return degenerate;
	}

	private static class Scaffold {
		private Function<GroupTypeCountMap, Object> groupPartitionFunction;

		private Function<CompartmentId, Object> compartmentPartitionFunction;

		private Function<RegionId, Object> regionPartitionFunction;

		private Map<PersonPropertyId, Function<Object, Object>> personPropertyPartitionFunctions = new LinkedHashMap<>();

		private Map<ResourceId, Function<Long, Object>> personResourcePartitionFunctions = new LinkedHashMap<>();

		private Filter filter;
	}

	private final boolean degenerate;

	private final Function<GroupTypeCountMap, Object> groupPartitionFunction;

	private final Function<CompartmentId, Object> compartmentPartitionFunction;

	private final Function<RegionId, Object> regionPartitionFunction;

	private final Map<PersonPropertyId, Function<Object, Object>> personPropertyPartitionFunctions;

	private final Map<ResourceId, Function<Long, Object>> personResourcePartitionFunctions;

	private final Filter filter;

	public Optional<Filter> getFilter() {
		return Optional.ofNullable(filter);
	}

	public Optional<Function<GroupTypeCountMap, Object>> getGroupPartitionFunction() {
		return Optional.ofNullable(groupPartitionFunction);		
	}

	public Optional<Function<CompartmentId, Object>> getCompartmentPartitionFunction() {
		return Optional.ofNullable(compartmentPartitionFunction);
	}

	public Optional<Function<RegionId, Object>> getRegionPartitionFunction() {
		return Optional.ofNullable(regionPartitionFunction);
	}

	public Optional<Function<Object, Object>> getPersonPropertyPartitionFunction(PersonPropertyId personPropertyId) {
		return Optional.ofNullable(personPropertyPartitionFunctions.get(personPropertyId));
	}

	public Optional<Function<Long, Object>> getPersonResourcePartitionFunction(ResourceId resourceId) {
		return Optional.ofNullable(personResourcePartitionFunctions.get(resourceId));
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with this
	 * {@link PartitionInfo}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(personResourcePartitionFunctions.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link PartitionInfo}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(personPropertyPartitionFunctions.keySet());
	}

}