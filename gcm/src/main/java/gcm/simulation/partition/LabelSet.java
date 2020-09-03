package gcm.simulation.partition;

import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = LabelSetInfo.class)
public class LabelSet {

	@Override
	@SourceMethod(status = TestStatus.UNREQUIRED)
	public String toString() {
		return LabelSetInfo.build(this).toString();
	}

	private LabelSet() {

	}

	static class CompartmentLabelSet extends LabelSet {
		final Object compartmentLabel;

		public CompartmentLabelSet(Object compartmentLabel) {
			this.compartmentLabel = compartmentLabel;
		}
	}

	/**
	 * Creates a compartment label for a label set
	 * 
	 * @throws RuntimeException
	 *                          <li>if the compartmentLabel is null
	 */
	public static LabelSet compartment(final Object compartmentLabel) {
		if (compartmentLabel == null) {
			throw new RuntimeException("null compartment label");
		}
		return new CompartmentLabelSet(compartmentLabel);
	}

	static class RegionLabelSet extends LabelSet {
		final Object regionLabel;

		public RegionLabelSet(Object regionLabel) {
			this.regionLabel = regionLabel;
		}
	}

	/**
	 * Creates a region label for a label set.
	 * 
	 * @throws RuntimeException
	 *                          <li>if the regionLabel is null
	 */
	public static LabelSet region(final Object regionLabel) {
		if (regionLabel == null) {
			throw new RuntimeException("null region label");
		}
		return new RegionLabelSet(regionLabel);
	}

	static class PersonPropertyLabelSet extends LabelSet {
		final PersonPropertyId personPropertyId;
		final Object personPropertyLabel;

		public PersonPropertyLabelSet(PersonPropertyId personPropertyId, Object personPropertyLabel) {
			this.personPropertyLabel = personPropertyLabel;
			this.personPropertyId = personPropertyId;
		}
	}

	/**
	 * Creates a person property label for a label set.
	 * 
	 * @throws RuntimeException
	 *                          <li>if the personPropertyId is null
	 *                          <li>if the personPropertyLabel is null
	 */
	public static LabelSet property(PersonPropertyId personPropertyId, Object personPropertyLabel) {
		if(personPropertyId == null) {
			throw new RuntimeException("null person property id");
		}
		
		if(personPropertyLabel == null) {
			throw new RuntimeException("null person property label");
		}

		return new PersonPropertyLabelSet(personPropertyId, personPropertyLabel);
	}

	static class ResourceLabelSet extends LabelSet {
		final ResourceId resourceId;
		final Object personResourceLabel;

		public ResourceLabelSet(ResourceId resourceId, Object personResourceLabel) {
			this.personResourceLabel = personResourceLabel;
			this.resourceId = resourceId;
		}
	}

	/**
	 * Creates a resource label for a label set.
	 * 
	 * @throws RuntimeException
	 * <li> if the resourceId is null
	 * <li> if the resourceLabel is null
	 */
	public static LabelSet resource(ResourceId resourceId, Object resourceLabel) {
		
		if(resourceId == null) {
			throw new RuntimeException("null resource id");
		}
		
		if(resourceLabel == null) {
			throw new RuntimeException("null resource label");
		}

		return new ResourceLabelSet(resourceId, resourceLabel);
	}

	static class GroupLabelSet extends LabelSet {
		final Object groupLabel;

		public GroupLabelSet(Object groupLabel) {
			this.groupLabel = groupLabel;
		}
	}

	/**
	 * Creates a compartment label for a label set
	 * 
	 * @throws RuntimeException
	 * <li> if the groupLabel is null
	 */
	public static LabelSet group(final Object groupLabel) {
		
		if(groupLabel == null) {
			throw new RuntimeException("null group label");
		}
		
		return new GroupLabelSet(groupLabel);
	}

	static class WithLabelSet extends LabelSet {
		final LabelSet a;
		final LabelSet b;

		public WithLabelSet(final LabelSet a, final LabelSet b) {
			this.a = a;
			this.b = b;
		}
	}

	/**
	 * Returns a composed {@link LabelSet} that joins label sets
	 * 
	 * @throws RuntimeException
	 * <li> if other is null 
	 * 
	 */
	public final LabelSet with(final LabelSet other) {
		if(other == null) {
			throw new RuntimeException("null label set");
		}
		return new WithLabelSet(this, other);
	}

	static class EmptyLabelSet extends LabelSet {

	}

	/**
	 * Creates an empty label for a label set.
	 */
	public static LabelSet empty() {
		return new EmptyLabelSet();
	}

}
