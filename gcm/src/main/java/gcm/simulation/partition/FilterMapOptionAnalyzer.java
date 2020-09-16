package gcm.simulation.partition;

import java.util.ArrayList;
import java.util.List;

import gcm.scenario.MapOption;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.simulation.Context;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.partition.FilterInfo.AndFilterInfo;
import gcm.simulation.partition.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.partition.FilterInfo.NegateFilterInfo;
import gcm.simulation.partition.FilterInfo.OrFilterInfo;
import gcm.simulation.partition.FilterInfo.PropertyFilterInfo;
import gcm.simulation.partition.FilterInfo.RegionFilterInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A static utility class for determining which attributes of a filter could
 * benefit from having reverse mappings activated.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class FilterMapOptionAnalyzer {

	private List<Object> execute() {
		List<Object> result = new ArrayList<>();
		processNode(filterInfo, result, false);
		return result;
	}

	private void processNode(FilterInfo filterInfo, List<Object> response, boolean negation) {
		switch (filterInfo.getFilterInfoType()) {
		case REGION:
			if (!negation) {
				RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
				MapOption regionMapOption = context.getScenario().getRegionMapOption();
				if (regionMapOption == MapOption.NONE) {
					response.addAll(regionFilterInfo.getRegionIds());
				}
			}
			break;
		case COMPARTMENT:
			if (!negation) {
				CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
				MapOption compartmentMapOption = context.getScenario().getCompartmentMapOption();
				if (compartmentMapOption == MapOption.NONE) {
					response.add(compartmentFilterInfo.getCompartmentId());
				}
			}
			break;
		case NEGATE:
			NegateFilterInfo negateFilterInfo = (NegateFilterInfo) filterInfo;
			processNode(negateFilterInfo.getA(), response, !negation);
			break;
		case AND:
			AndFilterInfo andFilterInfo = (AndFilterInfo) filterInfo;
			processNode(andFilterInfo.getA(), response, negation);
			processNode(andFilterInfo.getB(), response, negation);
			break;
		case OR:
			OrFilterInfo orFilterInfo = (OrFilterInfo) filterInfo;
			processNode(orFilterInfo.getA(), response, negation);
			processNode(orFilterInfo.getB(), response, negation);
			break;
		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			PersonPropertyId personPropertyId = propertyFilterInfo.getPersonPropertyId();
			Equality equality = propertyFilterInfo.getEquality();

			final PropertyDefinition personPropertyDefinition = context.getPropertyDefinitionsManager().getPersonPropertyDefinition(personPropertyId);
			final boolean valuesAreMappedToPeople = personPropertyDefinition.getMapOption() != MapOption.NONE;

			if (!valuesAreMappedToPeople) {
				final boolean propertyClassIsBoolean = personPropertyDefinition.getType().equals(Boolean.class);

				if (propertyClassIsBoolean) {
					response.add(personPropertyId);
				} else {
					/*
					 * Special handling for properties that are logically
					 * equivalent to an equality comparison to a value.
					 *
					 * We have a special and oft encountered case where the
					 * filter is selecting for people who have a specific
					 * property value and that value is currently mapped to
					 * people in the property manger.
					 */
					final boolean logicallyEquivalentToEquality = (!negation && (equality == Equality.EQUAL)) || (negation && (equality == Equality.NOT_EQUAL));
					if (logicallyEquivalentToEquality) {
						response.add(personPropertyId);
					}
				}
			}
			break;
		case ALL:// fall through
		case NONE:// fall through
		case GROUPS_FOR_PERSON:// fall through
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:// fall through
		case GROUP_MEMBER:// fall through
		case GROUP_TYPES_FOR_PERSON:// fall through
		case RESOURCE:
			// do nothing
			break;
		default:
			throw new RuntimeException("unhandled FilterInfoType " + filterInfo.getFilterInfoType());
		}
	}

	public static List<Object> getAttributesNeedingReverseMapping(FilterInfo filterInfo, Context context) {
		return new FilterMapOptionAnalyzer(filterInfo, context).execute();
	}

	private final FilterInfo filterInfo;
	private final Context context;

	private FilterMapOptionAnalyzer(FilterInfo filterInfo, Context context) {
		this.filterInfo = filterInfo;
		this.context = context;
	}

}
