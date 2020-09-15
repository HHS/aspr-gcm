package gcm.simulation.partition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.ModelException;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.partition.Filter.AllPeopleFilter;
import gcm.simulation.partition.Filter.AndFilter;
import gcm.simulation.partition.Filter.CompartmentFilter;
import gcm.simulation.partition.Filter.EmptyPeopleFilter;
import gcm.simulation.partition.Filter.GroupMemberFilter;
import gcm.simulation.partition.Filter.GroupTypesForPersonFilter;
import gcm.simulation.partition.Filter.GroupsForPersonAndGroupTypeFilter;
import gcm.simulation.partition.Filter.GroupsForPersonFilter;
import gcm.simulation.partition.Filter.NegateFilter;
import gcm.simulation.partition.Filter.OrFilter;
import gcm.simulation.partition.Filter.PropertyFilter;
import gcm.simulation.partition.Filter.RegionFilter;
import gcm.simulation.partition.Filter.ResourceFilter;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A utility class that mirrors the Filter class, publishes the internal data
 * of a filter and presents behaviors convenient for filter validation, trigger
 * formation and other filter related tasks.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public abstract class FilterInfo {

	public abstract FilterInfoType getFilterInfoType();

	public static enum FilterInfoType {

		AND(AndFilterInfo.class),

		OR(OrFilterInfo.class),

		NEGATE(NegateFilterInfo.class),

		COMPARTMENT(CompartmentFilterInfo.class),

		REGION(RegionFilterInfo.class),

		ALL(AllPeopleFilterInfo.class),

		EMPTY(EmptyPeopleFilterInfo.class),

		PROPERTY(PropertyFilterInfo.class),

		RESOURCE(ResourceFilterInfo.class),

		GROUP_MEMBER(GroupMemberFilterInfo.class),

		GROUPS_FOR_PERSON_AND_GROUP_TYPE(GroupsForPersonAndGroupTypeFilterInfo.class),

		GROUPS_FOR_PERSON(GroupsForPersonFilterInfo.class),

		GROUP_TYPES_FOR_PERSON(GroupTypesForPersonFilterInfo.class);

		private Class<? extends FilterInfo> filterInfoClass;

		public Class<? extends FilterInfo> getFilterInfoClass() {
			return filterInfoClass;
		}

		public void setFilterInfoClass(Class<? extends FilterInfo> filterInfoClass) {
			this.filterInfoClass = filterInfoClass;
		}

		private FilterInfoType(Class<? extends FilterInfo> filterInfoClass) {
			this.filterInfoClass = filterInfoClass;
		}

	}

	private static enum FilterType {

		AND(AndFilter.class),

		OR(OrFilter.class),

		NEGATE(NegateFilter.class),

		COMPARTMENT(CompartmentFilter.class),

		REGION(RegionFilter.class),

		ALL(AllPeopleFilter.class),

		EMPTY(EmptyPeopleFilter.class),

		PROPERTY(PropertyFilter.class),

		RESOURCE(ResourceFilter.class),

		GROUP_MEMBER(GroupMemberFilter.class),

		GROUPS_FOR_PERSON_AND_GROUP_TYPE(GroupsForPersonAndGroupTypeFilter.class),

		GROUPS_FOR_PERSON(GroupsForPersonFilter.class),

		GROUP_TYPES_FOR_PERSON(GroupTypesForPersonFilter.class);

		private static Map<Class<? extends Filter>, FilterType> map = buildMap();

		private static Map<Class<? extends Filter>, FilterType> buildMap() {
			Map<Class<? extends Filter>, FilterType> result = new LinkedHashMap<>();
			for (FilterType filterType : FilterType.values()) {
				result.put(filterType.c, filterType);
			}
			return result;
		}

		private final Class<? extends Filter> c;

		private FilterType(Class<? extends Filter> c) {
			this.c = c;
		}

		static FilterType getFilterType(Filter filter) {
			FilterType result = map.get(filter.getClass());
			if (result == null) {
				throw new RuntimeException("unrecognized filter type for " + filter.getClass().getSimpleName());
			}
			return result;
		}
	}

	private FilterInfo() {
	}
	
	
	/*
	 * Put self to list and recursively invoke on children.
	 */
	protected void putHierarchyToList(List<FilterInfo> list) {
		list.add(this);
	}
	
	public static List<FilterInfo> getHierarchyAsList(FilterInfo filterInfo){
		List<FilterInfo> result = new ArrayList<>();
		filterInfo.putHierarchyToList(result);
		return result;
	}

	public static FilterInfo build(Filter filter) {
		if(filter == null) {
			throw new ModelException(SimulationErrorType.NULL_FILTER, "");
		}
		FilterType filterType = FilterType.getFilterType(filter);
		switch (filterType) {
		case AND:
			AndFilter andFilter = (AndFilter) filter;
			return new AndFilterInfo(build(andFilter.a), build(andFilter.b));
		case COMPARTMENT:
			CompartmentFilter compartmentFilter = (CompartmentFilter) filter;
			return new CompartmentFilterInfo(compartmentFilter.compartmentId);
		case REGION:
			RegionFilter regionFilter = (RegionFilter) filter;
			return new RegionFilterInfo(regionFilter.regionIds);
		case NEGATE:
			NegateFilter negateFilter = (NegateFilter) filter;
			return new NegateFilterInfo(build(negateFilter.a));
		case OR:
			OrFilter orFilter = (OrFilter) filter;
			return new OrFilterInfo(build(orFilter.a), build(orFilter.b));
		case ALL:
			return new AllPeopleFilterInfo();
		case EMPTY:
			return new EmptyPeopleFilterInfo();
		case PROPERTY:
			PropertyFilter propertyFilter = (PropertyFilter) filter;
			return new PropertyFilterInfo(propertyFilter.personPropertyId, propertyFilter.equality, propertyFilter.personPropertyValue);
		case RESOURCE:
			ResourceFilter resourceFilter = (ResourceFilter) filter;
			return new ResourceFilterInfo(resourceFilter.resourceId, resourceFilter.equality, resourceFilter.resourceValue);
		case GROUP_MEMBER:
			GroupMemberFilter groupMemberFilter = (GroupMemberFilter) filter;
			return new GroupMemberFilterInfo(groupMemberFilter.groupId);
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			GroupsForPersonAndGroupTypeFilter groupsForPersonAndGroupTypeFilter = (GroupsForPersonAndGroupTypeFilter) filter;
			return new GroupsForPersonAndGroupTypeFilterInfo(groupsForPersonAndGroupTypeFilter.groupTypeId, groupsForPersonAndGroupTypeFilter.equality, groupsForPersonAndGroupTypeFilter.groupCount);
		case GROUPS_FOR_PERSON:
			GroupsForPersonFilter groupsForPersonFilter = (GroupsForPersonFilter) filter;
			return new GroupsForPersonFilterInfo(groupsForPersonFilter.equality, groupsForPersonFilter.groupCount);
		case GROUP_TYPES_FOR_PERSON:
			GroupTypesForPersonFilter groupTypesForPersonFilter = (GroupTypesForPersonFilter) filter;
			return new GroupTypesForPersonFilterInfo(groupTypesForPersonFilter.equality, groupTypesForPersonFilter.groupTypeCount);
		default:
			throw new RuntimeException("unhandled filter type " + filterType);
		}
	}

	public static class AllPeopleFilterInfo extends FilterInfo {

		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.ALL;
		}

	}

	public static class EmptyPeopleFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.EMPTY;
		}
	}

	public static final class CompartmentFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.COMPARTMENT;
		}

		private final CompartmentId compartmentId;

		public CompartmentFilterInfo(CompartmentId compartmentId) {
			this.compartmentId = compartmentId;
		}

		public CompartmentId getCompartmentId() {
			return compartmentId;
		}

	}

	public static final class RegionFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.REGION;
		}

		//the instance of the region ids will be unmodifiable
		private final Set<RegionId> regionIds;

		public RegionFilterInfo(Set<RegionId> regionIds) {
			this.regionIds = regionIds;
		}

		public Set<RegionId> getRegionIds() {
			return regionIds;
		}

	}

	public static final class AndFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.AND;
		}

		private final FilterInfo a;
		private final FilterInfo b;

		public AndFilterInfo(FilterInfo a, FilterInfo b) {
			this.a = a;
			this.b = b;
		}

		public FilterInfo getA() {
			return a;
		}

		public FilterInfo getB() {
			return b;
		}
		
		@Override
		protected void putHierarchyToList(List<FilterInfo> list) {			
			super.putHierarchyToList(list);
			a.putHierarchyToList(list);
			b.putHierarchyToList(list);
		}

	}

	public static final class OrFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.OR;
		}

		private final FilterInfo a;
		private final FilterInfo b;

		public OrFilterInfo(FilterInfo a, FilterInfo b) {			
			this.a = a;
			this.b = b;
		}

		public FilterInfo getA() {
			return a;
		}

		public FilterInfo getB() {
			return b;
		}
		
		@Override
		protected void putHierarchyToList(List<FilterInfo> list) {			
			super.putHierarchyToList(list);
			a.putHierarchyToList(list);
			b.putHierarchyToList(list);
		}

	}

	public static final class NegateFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.NEGATE;
		}

		private final FilterInfo a;

		public NegateFilterInfo(FilterInfo a) {
			this.a = a;
		}

		public FilterInfo getA() {
			return a;
		}
		
		@Override
		protected void putHierarchyToList(List<FilterInfo> list) {			
			super.putHierarchyToList(list);
			a.putHierarchyToList(list);
		}
	}

	public static final class PropertyFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.PROPERTY;
		}

		private final PersonPropertyId personPropertyId;
		private final Object personPropertyValue;
		private final Equality equality;

		public PropertyFilterInfo(PersonPropertyId personPropertyId, Equality equality, Object personPropertyValue) {
			this.personPropertyId = personPropertyId;
			this.equality = equality;
			this.personPropertyValue = personPropertyValue;
		}

		public PersonPropertyId getPersonPropertyId() {
			return personPropertyId;
		}

		public Object getPersonPropertyValue() {
			return personPropertyValue;
		}

		public Equality getEquality() {
			return equality;
		}
	}

	public static final class ResourceFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.RESOURCE;
		}

		private final ResourceId resourceId;
		private final long resourceValue;
		private final Equality equality;

		public ResourceFilterInfo(ResourceId resourceId, Equality equality, long resourceValue) {
			this.resourceId = resourceId;
			this.equality = equality;
			this.resourceValue = resourceValue;
		}

		public ResourceId getResourceId() {
			return resourceId;
		}

		public long getResourceValue() {
			return resourceValue;
		}

		public Equality getEquality() {
			return equality;
		}
	}

	public static final class GroupMemberFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.GROUP_MEMBER;
		}

		private final GroupId groupId;

		public GroupMemberFilterInfo(GroupId groupId) {
			super();
			this.groupId = groupId;
		}

		public GroupId getGroupId() {
			return groupId;
		}

	}

	public static final class GroupsForPersonAndGroupTypeFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.GROUPS_FOR_PERSON_AND_GROUP_TYPE;
		}

		private final GroupTypeId groupTypeId;
		private final Equality equality;
		private final int groupCount;

		public GroupsForPersonAndGroupTypeFilterInfo(GroupTypeId groupTypeId, Equality equality, int groupCount) {
			super();
			this.groupTypeId = groupTypeId;
			this.equality = equality;
			this.groupCount = groupCount;
		}

		public GroupTypeId getGroupTypeId() {
			return groupTypeId;
		}

		public Equality getEquality() {
			return equality;
		}

		public int getGroupCount() {
			return groupCount;
		}

	}

	public static final class GroupsForPersonFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.GROUPS_FOR_PERSON;
		}

		private final Equality equality;
		private final int groupCount;

		public GroupsForPersonFilterInfo(Equality equality, int groupCount) {
			super();
			this.equality = equality;
			this.groupCount = groupCount;
		}

		public Equality getEquality() {
			return equality;
		}

		public int getGroupCount() {
			return groupCount;
		}

	}

	public static final class GroupTypesForPersonFilterInfo extends FilterInfo {
		@Override
		public FilterInfoType getFilterInfoType() {
			return FilterInfoType.GROUP_TYPES_FOR_PERSON;
		}

		private final Equality equality;
		private final int groupTypeCount;

		public GroupTypesForPersonFilterInfo(Equality equality, int groupTypeCount) {
			super();
			this.equality = equality;
			this.groupTypeCount = groupTypeCount;
		}

		public Equality getEquality() {
			return equality;
		}

		public int getGroupTypeCount() {
			return groupTypeCount;
		}
	}
	
	@Override
	public String toString() {
		return FilterDisplay.getPrettyPrint(this);
	}

}
