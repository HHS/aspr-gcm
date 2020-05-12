package gcm.simulation;

import gcm.scenario.RegionId;
import gcm.simulation.FilterInfo.AndFilterInfo;
import gcm.simulation.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.FilterInfo.GroupMemberFilterInfo;
import gcm.simulation.FilterInfo.GroupTypesForPersonFilterInfo;
import gcm.simulation.FilterInfo.GroupsForPersonAndGroupTypeFilterInfo;
import gcm.simulation.FilterInfo.GroupsForPersonFilterInfo;
import gcm.simulation.FilterInfo.NegateFilterInfo;
import gcm.simulation.FilterInfo.OrFilterInfo;
import gcm.simulation.FilterInfo.PropertyFilterInfo;
import gcm.simulation.FilterInfo.RegionFilterInfo;
import gcm.simulation.FilterInfo.ResourceFilterInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A static utility class for creating a string representation of a
 * {@linkplain FilterInfo} that uses tabs and line feeds to make the filter more
 * readable.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public class FilterDisplay {

	private static String getTabString(final int n) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	private static String getPrettyPrintInternal(FilterInfo filterInfo, int tabDepth) {
		final String tabString = getTabString(tabDepth);
		final StringBuilder sb = new StringBuilder();

		switch (filterInfo.getFilterInfoType()) {
		case AND:
			AndFilterInfo andFilterInfo = (AndFilterInfo) filterInfo;

			sb.append(tabString);
			sb.append("AND[");
			sb.append("\n");

			sb.append(getPrettyPrintInternal(andFilterInfo.getA(), tabDepth + 1));
			sb.append("\n");

			sb.append(getPrettyPrintInternal(andFilterInfo.getB(), tabDepth + 1));
			sb.append("\n");

			sb.append(tabString);
			sb.append("]");
			sb.append("\n");

			break;
		case NEGATE:
			NegateFilterInfo negateFilterInfo = (NegateFilterInfo) filterInfo;

			sb.append(tabString);
			sb.append("NOT[");
			sb.append("\n");

			sb.append(getPrettyPrintInternal(negateFilterInfo.getA(), tabDepth + 1));
			sb.append("\n");

			sb.append(tabString);
			sb.append("]");
			sb.append("\n");
			break;
		case OR:
			OrFilterInfo orFilterInfo = (OrFilterInfo) filterInfo;

			sb.append(tabString);
			sb.append("OR[");
			sb.append("\n");

			sb.append(getPrettyPrintInternal(orFilterInfo.getA(), tabDepth + 1));
			sb.append("\n");

			sb.append(getPrettyPrintInternal(orFilterInfo.getB(), tabDepth + 1));
			sb.append("\n");

			sb.append(tabString);
			sb.append("]");
			sb.append("\n");

			break;

		case ALL:
			sb.append(tabString);
			sb.append("TRUE[");
			sb.append("]");
			break;
		case COMPARTMENT:
			CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("COMPARTMENT[");
			sb.append(compartmentFilterInfo.getCompartmentId());
			sb.append("]");
			break;
		case EMPTY:
			sb.append(tabString);
			sb.append("FALSE[");
			sb.append("]");
			break;
		case GROUPS_FOR_PERSON:
			GroupsForPersonFilterInfo groupsForPersonFilterInfo = (GroupsForPersonFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("GROUPS_FOR_PERSON[");
			sb.append(groupsForPersonFilterInfo.getEquality());
			sb.append(",");
			sb.append(groupsForPersonFilterInfo.getGroupCount());
			sb.append("]");
			break;
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			GroupsForPersonAndGroupTypeFilterInfo groupsForPersonAndGroupTypeFilterInfo = (GroupsForPersonAndGroupTypeFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("GROUPS_FOR_PERSON_AND_GROUP_TYPE[");
			sb.append(groupsForPersonAndGroupTypeFilterInfo.getGroupTypeId());
			sb.append(",");
			sb.append(groupsForPersonAndGroupTypeFilterInfo.getEquality());
			sb.append(",");
			sb.append(groupsForPersonAndGroupTypeFilterInfo.getGroupCount());
			sb.append("]");
			break;
		case GROUP_MEMBER:
			GroupMemberFilterInfo groupMemberFilterInfo = (GroupMemberFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("GROUP_MEMBER[");
			sb.append(groupMemberFilterInfo.getGroupId());
			sb.append("]");
			break;
		case GROUP_TYPES_FOR_PERSON:
			GroupTypesForPersonFilterInfo groupTypesForPersonFilterInfo = (GroupTypesForPersonFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("GROUP_TYPES_FOR_PERSON[");
			sb.append(groupTypesForPersonFilterInfo.getEquality());
			sb.append(",");
			sb.append(groupTypesForPersonFilterInfo.getGroupTypeCount());
			sb.append("]");
			break;
		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("PROPERTY[");
			sb.append(propertyFilterInfo.getPersonPropertyId());
			sb.append(",");
			sb.append(propertyFilterInfo.getEquality());
			sb.append(",");
			sb.append(propertyFilterInfo.getPersonPropertyValue());
			sb.append("]");
			break;
		case REGION:
			RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
			sb.append(tabString);
			sb.append("REGION[");
			boolean first = true;
			for (RegionId regionId : regionFilterInfo.getRegionIds()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(regionId);
			}
			sb.append("]");
			break;
		case RESOURCE:
			ResourceFilterInfo resourceFilterInfo = (ResourceFilterInfo) filterInfo;

			sb.append(tabString);
			sb.append("RESOURCE[");
			sb.append(resourceFilterInfo.getResourceId());
			sb.append(",");
			sb.append(resourceFilterInfo.getEquality());
			sb.append(",");
			sb.append(resourceFilterInfo.getResourceValue());
			sb.append("]");
			break;
		default:
			throw new RuntimeException("unhandled FilterInfoType " + filterInfo.getFilterInfoType());

		}
		return sb.toString();

	}

	public static String getPrettyPrint(FilterInfo filterInfo) {

		return getPrettyPrintInternal(filterInfo, 0);

	}

	private FilterDisplay() {

	}

}
