package gcm.simulation.partition;

import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = PartitionInfo.class)
public class Partition {

	private Partition() {
	}

	static class CompartmentPartition extends Partition {
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
	public final  Partition compartment(Function<CompartmentId, Object> compartmentPartitionFunction) {
		if (compartmentPartitionFunction == null) {
			throw new RuntimeException("null compartment partition function");
		}
		return new WithPartition(this,new CompartmentPartition(compartmentPartitionFunction));
	}

	static class RegionPartition extends Partition {
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
	public final Partition region(Function<RegionId, Object> regionPartitionFunction) {
		if (regionPartitionFunction == null) {
			throw new RuntimeException("null region partition function");
		}
		
		return new WithPartition(this,
				new RegionPartition(regionPartitionFunction));
	}
	
	
	static class FilterPartition extends Partition {
		final Filter filter;

		private FilterPartition(Filter filter) {
			this.filter = filter;
		}
	}
	/**
	 * Sets a filter on the partition
	 * 
	 * @throws RuntimeException
	 *                          <li>if the filter is null
	 */
	public final Partition filter(Filter filter) {
		if (filter == null) {
			throw new RuntimeException("null fitler");
		}
		return new WithPartition(this,
		new FilterPartition(filter));
	}

	static class GroupPartition extends Partition {
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
	public final Partition group(Function<GroupTypeCountMap, Object> groupPartitionFunction) {
		if (groupPartitionFunction == null) {
			throw new RuntimeException("null group partition function");
		}
		return new WithPartition(this,
		new GroupPartition(groupPartitionFunction));
	}

	static class PropertyPartition extends Partition {
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
	public final Partition property(PersonPropertyId personPropertyId,
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

	static class ResourcePartition extends Partition {
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
	public final Partition resource(ResourceId resourceId, Function<Long, Object> resourcePartitionFunction) {
		if (resourceId == null) {
			throw new RuntimeException("null resource property id function");
		}
		if (resourcePartitionFunction == null) {
			throw new RuntimeException("null resource partition function");
		}
		return new WithPartition(this,  
				new ResourcePartition(resourceId, resourcePartitionFunction));
	}

	static class EmptyPartition extends Partition {
	}

	public final static Partition create() {
		return new EmptyPartition();
	}

	static class WithPartition extends Partition {
		final Partition a;
		final Partition b;

		public WithPartition(final Partition a, final Partition b) {
			this.a = a;
			this.b = b;
		}
	}

	/**
	 * Returns a composed {@link Partition2} that joins partition functions
	 * 
	 * @throws RuntimeException
	 *                          <li>if other is null
	 * 
	 */
	public final Partition with(final Partition other) {
		if (other == null) {
			throw new RuntimeException("null partition");
		}
		return new WithPartition(this, other);
	}}