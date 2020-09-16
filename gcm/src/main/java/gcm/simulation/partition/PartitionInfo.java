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
import gcm.simulation.partition.Partition.CompartmentPartition;
import gcm.simulation.partition.Partition.EmptyPartition;
import gcm.simulation.partition.Partition.FilterPartition;
import gcm.simulation.partition.Partition.GroupPartition;
import gcm.simulation.partition.Partition.PropertyPartition;
import gcm.simulation.partition.Partition.RegionPartition;
import gcm.simulation.partition.Partition.ResourcePartition;
import gcm.simulation.partition.Partition.WithPartition;

public final class PartitionInfo {

	private PartitionInfo(Partition partition) {
		processPartition(partition);
	}

	private Function<GroupTypeCountMap, Object> groupPartitionFunction;

	private Function<CompartmentId, Object> compartmentPartitionFunction;

	private Function<RegionId, Object> regionPartitionFunction;

	private Map<PersonPropertyId, Function<Object, Object>> personPropertyPartitionFunctions = new LinkedHashMap<>();

	private Map<ResourceId, Function<Long, Object>> personResourcePartitionFunctions = new LinkedHashMap<>();
	
	private FilterInfo filterInfo = FilterInfo.build(Filter.allPeople());
	
	public FilterInfo getFilterInfo() {
		return filterInfo;
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

	/**
	 * Builds a {@link LabelSetInfo} from the given {@link LabelSet}
	 *
	 */
	public static PartitionInfo build(Partition partition) {
		return new PartitionInfo(partition);
	}
	private static enum PartitionType {

		WITH(WithPartition.class),

		COMPARTMENT(CompartmentPartition.class),

		REGION(RegionPartition.class),

		PROPERTY(PropertyPartition.class),

		RESOURCE(ResourcePartition.class),

		GROUP(GroupPartition.class),
		
		FILTER(FilterPartition.class),

		EMPTY(EmptyPartition.class);

		private static Map<Class<? extends Partition>, PartitionType> map = buildMap();

		private static Map<Class<? extends Partition>, PartitionType> buildMap() {
			Map<Class<? extends Partition>, PartitionType> result = new LinkedHashMap<>();
			for (PartitionType patitionType : PartitionType.values()) {
				result.put(patitionType.c, patitionType);
			}
			return result;
		}

		private final Class<? extends Partition> c;

		private PartitionType(Class<? extends Partition> c) {
			this.c = c;
		}

		static PartitionType getPartitionType(Partition partition) {
			PartitionType result = map.get(partition.getClass());
			if (result == null) {
				throw new RuntimeException("unrecognized partition type for " + partition.getClass().getSimpleName());
			}
			return result;
		}
	}
	
	private void processPartition(Partition partition) {
		PartitionType partitionType = PartitionType.getPartitionType(partition);
		switch (partitionType) {
		case COMPARTMENT:
			CompartmentPartition compartmentPartition = (CompartmentPartition) partition;
			compartmentPartitionFunction = compartmentPartition.compartmentPartitionFunction;
			break;
		case GROUP:
			GroupPartition groupPartition = (GroupPartition) partition;
			groupPartitionFunction = groupPartition.groupPartitionFunction;
			break;
		case PROPERTY:
			PropertyPartition propertyPartition = (PropertyPartition) partition;
			personPropertyPartitionFunctions.put(propertyPartition.personPropertyId, propertyPartition.personPropertyPartitionFunction);
			break;
		case REGION:
			RegionPartition regionPartition = (RegionPartition) partition;
			this.regionPartitionFunction = regionPartition.regionPartitionFunction;
			break;
		case RESOURCE:
			ResourcePartition resourcePartition = (ResourcePartition) partition;
			this.personResourcePartitionFunctions.put(resourcePartition.resourceId, resourcePartition.personResourcePartitionFunction);
			break;
		case WITH:
			WithPartition withPartition = (WithPartition) partition;
			processPartition(withPartition.a);
			processPartition(withPartition.b);
			break;
		case FILTER:
			FilterPartition filterPartition = (FilterPartition)partition;
			this.filterInfo = FilterInfo.build(filterPartition.filter);
			break;
		case EMPTY:
			break;
		default:
			throw new RuntimeException("unhandled case");
		}

	}
}