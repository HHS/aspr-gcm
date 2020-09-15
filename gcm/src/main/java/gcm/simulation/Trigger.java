package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.FilterInfo;
import gcm.simulation.partition.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.partition.FilterInfo.GroupMemberFilterInfo;
import gcm.simulation.partition.FilterInfo.GroupsForPersonAndGroupTypeFilterInfo;
import gcm.simulation.partition.FilterInfo.PropertyFilterInfo;
import gcm.simulation.partition.FilterInfo.RegionFilterInfo;
import gcm.simulation.partition.FilterInfo.ResourceFilterInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A distillation of the values, types and other relevant data used by a filter.
 * It is used by the Index Population Manager to efficiently map changes to a
 * person or its relationships to just those population indexes that may be
 * effected.
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class Trigger {
	private final Context context;

	public Trigger(FilterInfo filterInfo, Context context) {
		this.context = context;
		FilterInfo.getHierarchyAsList(filterInfo).forEach(this::processFilterInfo);

		/*
		 * For every person property id that has some sub-filter interested in
		 * all values of the property, we remove that property id from those
		 * that are interested in specific values so that re-evaluation of a
		 * person against the total filter reflects the needs of all the sub
		 * filters.
		 */
		for (PersonPropertyId personPropertyId : propertyIdentifiers) {
			propertyValuesMap.remove(personPropertyId);
		}
	}

	private void processFilterInfo(FilterInfo filterInfo) {
		switch (filterInfo.getFilterInfoType()) {
		case ALL:
			break;
		case AND:
			break;
		case COMPARTMENT:
			CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
			compartmentIdentifiers.add(compartmentFilterInfo.getCompartmentId());
			break;
		case EMPTY:
			break;
		case GROUPS_FOR_PERSON:
			groupTypeIds.addAll(context.getScenario().getGroupTypeIds());
			break;
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			GroupsForPersonAndGroupTypeFilterInfo groupsForPersonAndGroupTypeFilterInfo = (GroupsForPersonAndGroupTypeFilterInfo) filterInfo;
			groupTypeIds.add(groupsForPersonAndGroupTypeFilterInfo.getGroupTypeId());
			break;
		case GROUP_MEMBER:
			GroupMemberFilterInfo groupMemberFilterInfo = (GroupMemberFilterInfo) filterInfo;
			groupIds.add(groupMemberFilterInfo.getGroupId());
			break;
		case GROUP_TYPES_FOR_PERSON:
			groupTypeIds.addAll(context.getScenario().getGroupTypeIds());
			break;
		case NEGATE:
			break;
		case OR:
			break;
		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			PersonPropertyId personPropertyId = propertyFilterInfo.getPersonPropertyId();
			Object personPropertyValue = propertyFilterInfo.getPersonPropertyValue();
			Equality equality = propertyFilterInfo.getEquality();
			if ((equality == Equality.EQUAL) || (equality == Equality.NOT_EQUAL)) {
				Set<Object> propertyValues = propertyValuesMap.get(personPropertyId);
				if (propertyValues == null) {
					propertyValues = new LinkedHashSet<>();
					propertyValuesMap.put(personPropertyId, propertyValues);
				}
				propertyValues.add(personPropertyValue);
			} else {
				propertyIdentifiers.add(personPropertyId);
			}
			break;
		case REGION:
			RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
			regionIdentifiers.addAll(regionFilterInfo.getRegionIds());
			break;
		case RESOURCE:
			ResourceFilterInfo resourceFilterInfo = (ResourceFilterInfo) filterInfo;
			resourceIdentifiers.add(resourceFilterInfo.getResourceId());
			break;
		default:
			throw new RuntimeException("Unhandled FilterInfoType " + filterInfo.getFilterInfoType());
		}
	}

	private final Set<PersonPropertyId> propertyIdentifiers = new LinkedHashSet<>();

	private final Map<PersonPropertyId, Set<Object>> propertyValuesMap = new LinkedHashMap<>();

	private final Set<ResourceId> resourceIdentifiers = new LinkedHashSet<>();

	private final Set<CompartmentId> compartmentIdentifiers = new LinkedHashSet<>();

	private final Set<RegionId> regionIdentifiers = new LinkedHashSet<>();

	private final Set<GroupId> groupIds = new LinkedHashSet<>();

	private final Set<GroupTypeId> groupTypeIds = new LinkedHashSet<>();

	/**
	 * Returns the compartment identifiers associated with this trigger's
	 * filter.
	 */
	public Set<CompartmentId> getCompartmentIdentifiers() {
		return new LinkedHashSet<>(compartmentIdentifiers);
	}

	/**
	 * Returns the group identifiers associated with this trigger's filter.
	 */
	public Set<GroupId> getGroupIdentifiers() {
		return new LinkedHashSet<>(groupIds);
	}

	/**
	 * Returns the group type identifiers associated with this trigger's filter.
	 */
	public Set<GroupTypeId> getGroupTypeIdentifiers() {
		return new LinkedHashSet<>(groupTypeIds);
	}

	/**
	 * Returns the property values associated with this trigger's filter.
	 */
	public Set<Object> getPropertyValues(final PersonPropertyId personPropertyId) {
		final Set<Object> result = new LinkedHashSet<>();
		final Set<Object> set = propertyValuesMap.get(personPropertyId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns the region identifiers associated with this trigger's filter.
	 */
	public Set<RegionId> getRegionIdentifiers() {
		return new LinkedHashSet<>(regionIdentifiers);
	}

	/**
	 * Returns the resource identifiers associated with this trigger's filter.
	 */
	public Set<ResourceId> getResourceIdentifiers() {
		return new LinkedHashSet<>(resourceIdentifiers);
	}

	/**
	 * Returns the property identifiers associated with all values of the
	 * property
	 */
	public Set<PersonPropertyId> getValueInsensitivePropertyIdentifiers() {
		return new LinkedHashSet<>(propertyIdentifiers);
	}

	/**
	 * Returns the property identifiers associated to specific values of the
	 * property
	 */
	public Set<PersonPropertyId> getValueSensitivePropertyIdentifiers() {
		return new LinkedHashSet<>(propertyValuesMap.keySet());
	}

}