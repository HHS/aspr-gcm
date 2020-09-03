package gcm.manual;

import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.GroupTypeCountMap;
import gcm.simulation.partition.PartitionInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = PartitionInfo.class)
public class Partition2 {

	private Partition2() {
	}

	static class CompartmentPartition extends Partition2 {
		final Function<CompartmentId, Object> compartmentPartitionFunction;

		private CompartmentPartition(Function<CompartmentId, Object> compartmentPartitionFunction) {

			this.compartmentPartitionFunction = compartmentPartitionFunction;
		}
	}

	/**
	 * Creates a compartment partition function for a partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the compartment partition function is null
	 */
	public  Partition2 compartment(Function<CompartmentId, Object> compartmentPartitionFunction) {
		if (compartmentPartitionFunction == null) {
			throw new RuntimeException("null compartment partition function");
		}
		return new WithPartition(this,new CompartmentPartition(compartmentPartitionFunction));
	}

	static class RegionPartition extends Partition2 {
		final Function<RegionId, Object> regionPartitionFunction;

		private RegionPartition(Function<RegionId, Object> regionPartitionFunction) {
			this.regionPartitionFunction = regionPartitionFunction;
		}
	}

	/**
	 * Creates a region partition function for a partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the region partition function is null
	 */
	public  Partition2 region(Function<RegionId, Object> regionPartitionFunction) {
		if (regionPartitionFunction == null) {
			throw new RuntimeException("null region partition function");
		}
		
		return new WithPartition(this,
				new RegionPartition(regionPartitionFunction));
	}

	static class GroupPartition extends Partition2 {
		final Function<GroupTypeCountMap, Object> groupPartitionFunction;

		private GroupPartition(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
			this.groupPartitionFunction = groupPartitionFunction;
		}
	}

	/**
	 * Creates a group partition function for a partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the group partition function is null
	 */
	public  Partition2 group(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
		if (groupPartitionFunction == null) {
			throw new RuntimeException("null group partition function");
		}
		return new WithPartition(this,
		new GroupPartition(groupPartitionFunction));
	}

	static class PropertyPartition extends Partition2 {
		final PersonPropertyId personPropertyId;
		final Function<Object, Object> personPropertyPartitionFunction;

		private PropertyPartition(PersonPropertyId personPropertyId,
				Function<Object, Object> personPropertyPartitionFunction) {
			this.personPropertyId = personPropertyId;
			this.personPropertyPartitionFunction = personPropertyPartitionFunction;
		}
	}

	/**
	 * Creates a person property partition function for a partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the person property id is null
	 *                          <li>if the person property partition function is
	 *                          null
	 */
	public  Partition2 property(PersonPropertyId personPropertyId,
			Function<Object, Object> personPropertyPartitionFunction) {
		if (personPropertyId == null) {
			throw new RuntimeException("null person property id function");
		}
		if (personPropertyPartitionFunction == null) {
			throw new RuntimeException("null person property partition function");
		}
		return new WithPartition(this,
				new PropertyPartition(personPropertyId, personPropertyPartitionFunction));
	}

	static class ResourcePartition extends Partition2 {
		final ResourceId resourceId;
		final Function<Long, Object> personResourcePartitionFunction;

		private ResourcePartition(ResourceId resourceId, Function<Long, Object> personResourcePartitionFunction) {
			this.resourceId = resourceId;
			this.personResourcePartitionFunction = personResourcePartitionFunction;
		}
	}

	/**
	 * Creates a resource partition function for a partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the resource id is null
	 *                          <li>if the person resource partition function is
	 *                          null
	 */
	public  Partition2 resource(ResourceId resourceId, Function<Long, Object> resourcePartitionFunction) {
		if (resourceId == null) {
			throw new RuntimeException("null resource property id function");
		}
		if (resourcePartitionFunction == null) {
			throw new RuntimeException("null resource partition function");
		}
		return new WithPartition(this,  
				new ResourcePartition(resourceId, resourcePartitionFunction));
	}

	static class EmptyPartition extends Partition2 {
	}

	public static Partition2 create() {
		return new EmptyPartition();
	}

	static class WithPartition extends Partition2 {
		final Partition2 a;
		final Partition2 b;

		public WithPartition(final Partition2 a, final Partition2 b) {
			this.a = a;
			this.b = b;
		}
	}

//	/**
//	 * Returns a composed {@link Partition2} that joins partition functions
//	 * 
//	 * @throws RuntimeException
//	 *                          <li>if other is null
//	 * 
//	 */
//	public final Partition2 with(final Partition2 other) {
//		if (other == null) {
//			throw new RuntimeException("null partition");
//		}
//		return new WithPartition(this, other);
//	}
}