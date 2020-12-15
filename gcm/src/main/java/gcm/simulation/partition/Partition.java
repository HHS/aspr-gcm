package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import gcm.components.Component;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * A {@linkplain Partition} is the general description of a partitioning of the
 * people contained in the simulation. It is composed of a filter and various
 * functions that map values associated with each person to labels. The space
 * formed by the labels forms the formal partition. Partitions significantly
 * reduce the runtime required to sample/select people from the simulation who
 * meet some set of criteria.
 * 
 * This class functions as a description of a population partition and is
 * immutable. However, the implementation of the partition within the simulation
 * is dynamic and the contents(people) of the partition remain consistent with
 * the filter and label mapping functions as people change their properties,
 * group associations, resources, regions and compartments.
 * 
 * Partitions are built by the modeler via the supplied Builder class.
 * Partitions may be added and removed from the simulation and are identified by
 * a unique identifier. Only the {@linkplain Component} that adds a partition
 * may remove that partition.
 * 
 * The filter: The role of the filter supplied to the partition is simply to
 * determine which people will be included in the partition's cells. If no
 * filter is supplied, the resulting partition will include all people. For
 * example, suppose there is a Boolean person property IS_VACCINTATED. If the
 * filter is [IS_VACCINATED EQUALS == FALSE] then only people who had not been
 * vaccinated would be included in the cells of the partition.
 * 
 * The labeling functions: The labeling functions serve to group people into the
 * cells of the partition. For example, suppose their are numerous regions in
 * the simulation with each representing a single census tract. The modeler may
 * wish to group these regions by state and would thus provide a function that
 * accepts a {@linkplain RegionId} and returns a state name as the label. Labels
 * are not required to be of any particular type or even to be of the same type
 * for any particular function. In this example, the modeler could create an
 * enumeration composed of values for each of the states and
 * territories(ALABAMA, ALASKA, ...) and would thus return the associated
 * enumeration member as the label value.
 * 
 * The inclusion of multiple labeling functions serves to refine the cells of
 * the partition. For example, suppose that there is a person property for the
 * (Integer) age of each person. The modeler may want to group people under age
 * 30 as "YOUNG" and those older as "OLD". Combined with the previous region
 * labeling function, the partition will have cells such as [UTAH,OLD],
 * [OHIO,YOUNG], etc.
 * 
 * Thus to retrieve or randomly sample from the simulation those people who are
 * unvaccinated, live in Maryland, and are below the age of 30 the modeler
 * queries the environment with the the id of the partition and the label values
 * [MARYLAND,YOUNG].
 * 
 * When implementing a supplied labeling function, the modeler must take some
 * care to only consider the inputs of the function and to guarantee the
 * stability of the return value. For example, in the age labeling function
 * above, the function will receive an Integer (such as 35) and must always
 * return the same label(OLD) in every invocation of the function. Without this
 * the partition will not function correctly.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED)
@Immutable
public final class Partition {

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Source(proxy = Partition.class)
	@NotThreadSafe
	/**
	 * Standard builder class for partitions. All inputs are optional.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Returns the {@linkplain Partition} formed from the inputs collected
		 * by this builder and resets the state of the builder to empty.
		 */
		public Partition build() {
			try {
				return new Partition(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the group partition labeling function. This function receives a
		 * {@linkplain GroupTypeCountMap} and returns a non-null Object as a
		 * label. Function results must be stable over the life of the
		 * partition.
		 */
		public Builder setGroupFunction(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
			scaffold.groupPartitionFunction = groupPartitionFunction;
			return this;
		}

		/**
		 * Sets the compartment partition labeling function. This function
		 * receives a {@linkplain CompartmentId} and returns a non-null Object
		 * as a label. Function results must be stable over the life of the
		 * partition.
		 */
		public Builder setCompartmentFunction(Function<CompartmentId, Object> compartmentPartitionFunction) {
			scaffold.compartmentPartitionFunction = compartmentPartitionFunction;
			return this;
		}

		/**
		 * Sets the region partition labeling function. This function receives a
		 * {@linkplain RegionId} and returns a non-null Object as a label.
		 * Function results must be stable over the life of the partition.
		 */
		public Builder setRegionFunction(Function<RegionId, Object> regionPartitionFunction) {
			scaffold.regionPartitionFunction = regionPartitionFunction;
			return this;
		}

		/**
		 * Sets the person property partition labeling function for the
		 * specified {@linkplain PersonPropertyId} This function receives a
		 * person property value and returns a non-null Object as a label.
		 * Function results must be stable over the life of the partition.
		 */
		public Builder setPersonPropertyFunction(PersonPropertyId personPropertyId, Function<Object, Object> personPropertyFunction) {
			if (personPropertyFunction != null) {
				scaffold.personPropertyPartitionFunctions.put(personPropertyId, personPropertyFunction);
			} else {
				scaffold.personPropertyPartitionFunctions.remove(personPropertyId);
			}
			return this;
		}

		/**
		 * Sets the resource partition labeling function for the specified
		 * {@linkplain ResourceId} This function receives a resource
		 * level(amount of resource assigned to a person) and returns a non-null
		 * Object as a label. Function results must be stable over the life of
		 * the partition.
		 */
		public Builder setPersonResourceFunction(ResourceId resourceId, Function<Long, Object> personResourceFunction) {
			if (personResourceFunction != null) {
				scaffold.personResourcePartitionFunctions.put(resourceId, personResourceFunction);
			} else {
				scaffold.personResourcePartitionFunctions.remove(resourceId);
			}
			return this;
		}

		/**
		 * Sets the filter for the {@linkplain Partition}. If no filter is provided, a
		 * default filter that accepts all people is used instead.
		 */
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

		degenerate = (regionPartitionFunction == null) && (compartmentPartitionFunction == null) && (groupPartitionFunction == null) && personPropertyPartitionFunctions.isEmpty()
				&& personResourcePartitionFunctions.isEmpty();

	}

	/**
	 * Returns true if and only if the {@linkplain Partition} contains no
	 * labeling functions.
	 */
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

	/**
	 * Returns the filter contained in this {@linkplain Partition}
	 */
	public Optional<Filter> getFilter() {
		return Optional.ofNullable(filter);
	}

	/**
	 * Returns the group partition labeling function contained in this
	 * {@link Partition}
	 */
	public Optional<Function<GroupTypeCountMap, Object>> getGroupPartitionFunction() {
		return Optional.ofNullable(groupPartitionFunction);
	}

	/**
	 * Returns the compartment partition labeling function contained in this
	 * {@link Partition}
	 */
	public Optional<Function<CompartmentId, Object>> getCompartmentPartitionFunction() {
		return Optional.ofNullable(compartmentPartitionFunction);
	}

	/**
	 * Returns the region partition labeling function contained in this
	 * {@link Partition}
	 */
	public Optional<Function<RegionId, Object>> getRegionPartitionFunction() {
		return Optional.ofNullable(regionPartitionFunction);
	}

	/**
	 * Returns the person property partition labeling function contained in this
	 * {@linkplain Partition} for the given person property id.
	 */
	public Optional<Function<Object, Object>> getPersonPropertyPartitionFunction(PersonPropertyId personPropertyId) {
		return Optional.ofNullable(personPropertyPartitionFunctions.get(personPropertyId));
	}

	/**
	 * Returns the resource partition labeling function contained in this
	 * {@linkplain Partition} for the given resource id.
	 */
	public Optional<Function<Long, Object>> getPersonResourcePartitionFunction(ResourceId resourceId) {
		return Optional.ofNullable(personResourcePartitionFunctions.get(resourceId));
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with
	 * this {@link PartitionInfo}
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