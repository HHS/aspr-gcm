package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.plaf.synth.Region;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.Equality;
import gcm.util.annotations.Source;

/**
 * A filter is used to determine membership in partitions.
 * 
 * Filter does not support descendant classes but rather provides 12 curated
 * implementors that are accessed via public static methods. These correspond to
 * the various properties and associations of a person such as their
 * compartment, region and resource levels. Filter instances provide AND, OR and
 * NEGATE functions like bi-predicates and thus can be used to compose complex
 * filters.
 * 
 * Example: A filter for people who are working, over the age of 65 and living
 * in one of two particular regions might be composed as
 *
 * 
 * Filter filter = groupsForPersonAndGroupType(GroupType.WORK,
 * Equality.GREATER_THAN, 0).and(property(PersonProperty.AGE,
 * Equality.GREATER_THAN_EQUAL, 65))
 * .and(region(Region.REGION_1).or(region(Region.REGION_1));
 * 
 *
 * @author Shawn Hatch
 *
 */
@Source
public abstract class Filter {
	
	static class AllPeopleFilter extends Filter {
		
	}

	static class AndFilter extends Filter {
		final Filter a;
		final Filter b;

		public AndFilter(final Filter a, final Filter b) {
			this.a = a;
			this.b = b;
		}
	}

	static class CompartmentFilter extends Filter {
		final CompartmentId compartmentId;

		public CompartmentFilter(final CompartmentId compartmentId) {
			this.compartmentId = compartmentId;
		}

	}

	static class NonePeopleFilter extends Filter {

		public NonePeopleFilter() {

		}

	}

	static class GroupMemberFilter extends Filter {
		final GroupId groupId;

		public GroupMemberFilter(final GroupId groupId) {
			this.groupId = groupId;
		}

	}

	static class GroupsForPersonAndGroupTypeFilter extends Filter {
		final GroupTypeId groupTypeId;
		final Equality equality;
		final int groupCount;

		public GroupsForPersonAndGroupTypeFilter(final GroupTypeId groupTypeId, final Equality equality,
				final int groupCount) {

			this.equality = equality;
			this.groupCount = groupCount;
			this.groupTypeId = groupTypeId;
		}

	}

	static class GroupsForPersonFilter extends Filter {
		final Equality equality;
		final int groupCount;

		public GroupsForPersonFilter(final Equality equality, final int groupCount) {
			this.equality = equality;
			this.groupCount = groupCount;
		}

	}

	static class GroupTypesForPersonFilter extends Filter {

		final Equality equality;
		final int groupTypeCount;

		public GroupTypesForPersonFilter(final Equality equality, final int groupTypeCount) {
			this.equality = equality;
			this.groupTypeCount = groupTypeCount;
		}

	}

	static class NegateFilter extends Filter {
		final Filter a;

		public NegateFilter(final Filter a) {
			this.a = a;
		}

	}

	static class OrFilter extends Filter {
		final Filter a;
		final Filter b;

		public OrFilter(final Filter a, final Filter b) {
			this.a = a;
			this.b = b;
		}

	}

	static class PropertyFilter extends Filter {
		final PersonPropertyId personPropertyId;
		final Object personPropertyValue;
		final Equality equality;

		public PropertyFilter(final PersonPropertyId personPropertyId, final Equality equality,
				final Object personPropertyValue) {
			this.personPropertyId = personPropertyId;
			this.personPropertyValue = personPropertyValue;
			this.equality = equality;
		}
	}

	static class RegionFilter extends Filter {
		final Set<RegionId> regionIds = new LinkedHashSet<>();

		Set<RegionId> getRegionIds() {
			return Collections.unmodifiableSet(regionIds);
		}

		public RegionFilter(final Set<RegionId> regionIds) {
			this.regionIds.addAll(regionIds);
		}

	}

	static class ResourceFilter extends Filter {

		final ResourceId resourceId;
		final long resourceValue;
		final Equality equality;

		public ResourceFilter(final ResourceId resourceId, final Equality equality, final long resourceValue) {
			this.resourceId = resourceId;
			this.resourceValue = resourceValue;
			this.equality = equality;
		}
	}

	/**
	 * Returns a filter that passes all people. Used for concatenating filters in an
	 * AND loop.
	 */
	public static Filter allPeople() {
		return new AllPeopleFilter();
	}

	/**
	 * Returns a filter that selects people associated with the given {@link CompartmentId}.
	 */
	public static Filter compartment(final CompartmentId compartmentId) {
		return new CompartmentFilter(compartmentId);
	}

	/**
	 * Returns a filter that selects people associated with the given {@link GroupId}.
	 */
	public static Filter groupMember(final GroupId groupId) {
		return new GroupMemberFilter(groupId);
	}

	/**
	 * Returns a filter that selects people who are associated with the equality
	 * relation to the given number of groups.
	 */
	public static Filter groupsForPerson(final Equality equality, final int groupCount) {
		return new GroupsForPersonFilter(equality, groupCount);
	}

	/**
	 * Returns a filter that selects people who are associated with the equality
	 * relation to the given number of groups of the given group type.
	 */
	public static Filter groupsForPersonAndGroupType(final GroupTypeId groupTypeId, final Equality equality,
			final int groupCount) {
		return new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality, groupCount);
	}

	/**
	 * Returns a filter that selects people who are associated with the equality
	 * relation to the given number of group types.
	 */
	public static Filter groupTypesForPerson(final Equality equality, final int groupTypeCount) {
		return new GroupTypesForPersonFilter(equality, groupTypeCount);
	}

	/**
	 * Returns a filter that passes no people. Used for concatenating filters in an
	 * OR loop.
	 */
	public static Filter noPeople() {
		return new NonePeopleFilter();
	}

	/**
	 * Returns a filter that selects people who are associated with the equality
	 * relation to the given {@link PersonPropertyId} and value.
	 */
	public static Filter property(final PersonPropertyId personPropertyId, final Equality equality,
			final Object personPropertyValue) {
		return new PropertyFilter(personPropertyId, equality, personPropertyValue);
	}

	/**
	 * Returns a filter that selects people associated with the given {@link RegionId} values.
	 */
	public static Filter region(final RegionId... regionIds) {
		Set<RegionId> set = new LinkedHashSet<>();
		for (RegionId regionId : regionIds) {
			set.add(regionId);
		}
		return new RegionFilter(set);
	}

	/**
	 * Returns a filter that selects people associated with the given {@link Region} values.
	 */
	public static Filter region(Set<RegionId> regionIds) {
		return new RegionFilter(regionIds);
	}

	/**
	 * Returns a filter that selects people who are associated with the equality
	 * relation to the given {@link ResourceId} and value.
	 */
	public static Filter resource(final ResourceId resourceId, final Equality equality, final long resourceValue) {
		return new ResourceFilter(resourceId, equality, resourceValue);
	}

	private Filter() {
	}

	/**
	 * Returns a composed filter that represents a short-circuiting logical AND of
	 * this filter and another.
	 * 
	 * @throws RuntimeException
	 *                          <li>if the given filter is null
	 */
	public final Filter and(final Filter other) {
		if(other == null) {
			throw new RuntimeException("null filter");
		}
		return new AndFilter(this, other);
	}

	/**
	 * Returns a filter that represents the logical negation of this filter.
	 */
	public final Filter negate() {
		return new NegateFilter(this);
	}

	/**
	 * Returns a composed filter that represents a short-circuiting logical OR of
	 * this filter and another.
	 * 
	 * @throws RuntimeException
	 *                          <li>if the given filter is null
 
	 */
	public final Filter or(final Filter other) {
		if(other == null) {
			throw new RuntimeException("null filter");
		}
		return new OrFilter(this, other);
	}
}
