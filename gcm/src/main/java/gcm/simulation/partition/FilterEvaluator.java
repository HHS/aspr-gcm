package gcm.simulation.partition;

import java.util.Set;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.partition.FilterInfo.AndFilterInfo;
import gcm.simulation.partition.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.partition.FilterInfo.FilterInfoType;
import gcm.simulation.partition.FilterInfo.GroupMemberFilterInfo;
import gcm.simulation.partition.FilterInfo.GroupTypesForPersonFilterInfo;
import gcm.simulation.partition.FilterInfo.GroupsForPersonAndGroupTypeFilterInfo;
import gcm.simulation.partition.FilterInfo.GroupsForPersonFilterInfo;
import gcm.simulation.partition.FilterInfo.NegateFilterInfo;
import gcm.simulation.partition.FilterInfo.OrFilterInfo;
import gcm.simulation.partition.FilterInfo.PropertyFilterInfo;
import gcm.simulation.partition.FilterInfo.RegionFilterInfo;
import gcm.simulation.partition.FilterInfo.ResourceFilterInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A utility class that evaluates a FilterInfo against the current state of the
 * simulation for individual people.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public abstract class FilterEvaluator {

	private FilterEvaluator() {
	}

	/**
	 * Returns a {@link FilterEvaluator} from the given {@link FilterInfo}
	 */
	public static FilterEvaluator build(FilterInfo filterInfo) {

		FilterInfoType filterInfoType = filterInfo.getFilterInfoType();

		switch (filterInfoType) {
		case AND:
			AndFilterInfo andFilterInfo = (AndFilterInfo) filterInfo;
			return new AndFilterEvaluator(build(andFilterInfo.getA()), build(andFilterInfo.getB()));
		case COMPARTMENT:
			CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
			return new CompartmentFilterEvaluator(compartmentFilterInfo.getCompartmentId());
		case REGION:
			RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
			return new RegionFilterEvaluator(regionFilterInfo.getRegionIds());
		case NEGATE:
			NegateFilterInfo negateFilterInfo = (NegateFilterInfo) filterInfo;
			return new NegateFilterEvaluator(build(negateFilterInfo.getA()));
		case OR:
			OrFilterInfo orFilterInfo = (OrFilterInfo) filterInfo;
			return new OrFilterEvaluator(build(orFilterInfo.getA()), build(orFilterInfo.getB()));
		case ALL:
			return new AllPeopleFilterEvaluator();
		case NONE:
			return new EmptyPeopleFilterEvaluator();
		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			return new PropertyFilterEvaluator(propertyFilterInfo.getPersonPropertyId(), propertyFilterInfo.getEquality(), propertyFilterInfo.getPersonPropertyValue());
		case RESOURCE:
			ResourceFilterInfo resourceFilterInfo = (ResourceFilterInfo) filterInfo;
			return new ResourceFilterEvaluator(resourceFilterInfo.getResourceId(), resourceFilterInfo.getEquality(), resourceFilterInfo.getResourceValue());
		case GROUP_MEMBER:
			GroupMemberFilterInfo groupMemberFilterInfo = (GroupMemberFilterInfo) filterInfo;
			return new GroupMemberFilterEvaluator(groupMemberFilterInfo.getGroupId());
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			GroupsForPersonAndGroupTypeFilterInfo groupsForPersonAndGroupTypeFilterInfo = (GroupsForPersonAndGroupTypeFilterInfo) filterInfo;
			return new GroupsForPersonAndGroupTypeFilterEvaluator(groupsForPersonAndGroupTypeFilterInfo.getGroupTypeId(), groupsForPersonAndGroupTypeFilterInfo.getEquality(),
					groupsForPersonAndGroupTypeFilterInfo.getGroupCount());
		case GROUPS_FOR_PERSON:
			GroupsForPersonFilterInfo groupsForPersonFilterInfo = (GroupsForPersonFilterInfo) filterInfo;
			return new GroupsForPersonFilterEvaluator(groupsForPersonFilterInfo.getEquality(), groupsForPersonFilterInfo.getGroupCount());
		case GROUP_TYPES_FOR_PERSON:
			GroupTypesForPersonFilterInfo groupTypesForPersonFilterInfo = (GroupTypesForPersonFilterInfo) filterInfo;
			return new GroupTypesForPersonFilterEvaluator(groupTypesForPersonFilterInfo.getEquality(), groupTypesForPersonFilterInfo.getGroupTypeCount());
		default:
			throw new RuntimeException("unhandled filter info type " + filterInfoType);
		}
	}

	private static class AllPeopleFilterEvaluator extends FilterEvaluator {

		@Override
		public boolean evaluate(Environment environment, PersonId personId) {
			return true;
		}

	}

	private static class EmptyPeopleFilterEvaluator extends FilterEvaluator {

		@Override
		public boolean evaluate(Environment environment, PersonId personId) {
			return false;
		}

	}

	private static final class CompartmentFilterEvaluator extends FilterEvaluator {

		private final CompartmentId compartmentId;

		public CompartmentFilterEvaluator(CompartmentId compartmentId) {
			this.compartmentId = compartmentId;
		}

		@Override
		public boolean evaluate(Environment environment, PersonId personId) {
			return environment.getPersonCompartment(personId).equals(compartmentId);
		}

	}

	private static final class RegionFilterEvaluator extends FilterEvaluator {

		// the region ids will be an unmodifiable list
		private final Set<RegionId> regionIds;

		public RegionFilterEvaluator(Set<RegionId> regionIds) {
			this.regionIds = regionIds;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			return regionIds.contains(environment.getPersonRegion(personId));
		}
	}

	private static final class AndFilterEvaluator extends FilterEvaluator {

		private final FilterEvaluator a;
		private final FilterEvaluator b;

		public AndFilterEvaluator(FilterEvaluator a, FilterEvaluator b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			return a.evaluate(environment, personId) && b.evaluate(environment, personId);
		}

	}

	private static final class OrFilterEvaluator extends FilterEvaluator {

		private final FilterEvaluator a;
		private final FilterEvaluator b;

		public OrFilterEvaluator(FilterEvaluator a, FilterEvaluator b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			return a.evaluate(environment, personId) || b.evaluate(environment, personId);
		}

	}

	private static final class NegateFilterEvaluator extends FilterEvaluator {

		private final FilterEvaluator a;

		public NegateFilterEvaluator(FilterEvaluator a) {
			this.a = a;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			return !a.evaluate(environment, personId);
		}

	}

	private static final class PropertyFilterEvaluator extends FilterEvaluator {

		private final PersonPropertyId personPropertyId;
		private final Object personPropertyValue;
		private final Equality equality;

		public PropertyFilterEvaluator(PersonPropertyId personPropertyId, Equality equality, Object personPropertyValue) {
			this.personPropertyId = personPropertyId;
			this.equality = equality;
			this.personPropertyValue = personPropertyValue;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			// we do not assume that the returned property value is
			// comparable unless we are forced to.
			final Object propVal = environment.getPersonPropertyValue(personId, personPropertyId);

			if (equality.equals(Equality.EQUAL)) {
				return propVal.equals(personPropertyValue);
			} else if (equality.equals(Equality.NOT_EQUAL)) {
				return !propVal.equals(personPropertyValue);
			} else {
				Comparable comparablePropertyValue = (Comparable) propVal;
				int evaluation = comparablePropertyValue.compareTo(personPropertyValue);
				return equality.isCompatibleComparisonValue(evaluation);
			}
		}

	}

	private static final class ResourceFilterEvaluator extends FilterEvaluator {

		private final ResourceId resourceId;
		private final long resourceValue;
		private final Equality equality;

		public ResourceFilterEvaluator(ResourceId resourceId, Equality equality, long resourceValue) {
			this.resourceId = resourceId;
			this.equality = equality;
			this.resourceValue = resourceValue;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			final long level = environment.getPersonResourceLevel(personId, resourceId);
			return equality.isCompatibleComparisonValue(Long.compare(level, resourceValue));
		}

	}

	private static final class GroupMemberFilterEvaluator extends FilterEvaluator {

		private final GroupId groupId;

		public GroupMemberFilterEvaluator(GroupId groupId) {
			super();
			this.groupId = groupId;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			return environment.isGroupMember(personId, groupId);
		}
	}

	private static final class GroupsForPersonAndGroupTypeFilterEvaluator extends FilterEvaluator {

		private final GroupTypeId groupTypeId;
		private final Equality equality;
		private final int groupCount;

		public GroupsForPersonAndGroupTypeFilterEvaluator(GroupTypeId groupTypeId, Equality equality, int groupCount) {
			super();
			this.groupTypeId = groupTypeId;
			this.equality = equality;
			this.groupCount = groupCount;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			final int count = environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
		}

	}

	private static final class GroupsForPersonFilterEvaluator extends FilterEvaluator {

		private final Equality equality;
		private final int groupCount;

		public GroupsForPersonFilterEvaluator(Equality equality, int groupCount) {
			super();
			this.equality = equality;
			this.groupCount = groupCount;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			final int count = environment.getGroupCountForPerson(personId);
			return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
		}

	}

	private static final class GroupTypesForPersonFilterEvaluator extends FilterEvaluator {

		private final Equality equality;
		private final int groupTypeCount;

		public GroupTypesForPersonFilterEvaluator(Equality equality, int groupTypeCount) {
			super();
			this.equality = equality;
			this.groupTypeCount = groupTypeCount;
		}

		@Override
		public boolean evaluate(final Environment environment, final PersonId personId) {
			final int count = environment.getGroupTypeCountForPerson(personId);
			return equality.isCompatibleComparisonValue(Integer.compare(count, groupTypeCount));
		}
	}

	/**
	 * Returns true if and only if the given {@link PersonId} passes the
	 * {@link FilterInfo} used to construct this {@link FilterEvaluator}
	 */
	public abstract boolean evaluate(Environment environment, PersonId personId);

}
