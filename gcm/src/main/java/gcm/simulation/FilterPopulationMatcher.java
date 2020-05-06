package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.simulation.FilterInfo.AndFilterInfo;
import gcm.simulation.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.FilterInfo.GroupMemberFilterInfo;
import gcm.simulation.FilterInfo.NegateFilterInfo;
import gcm.simulation.FilterInfo.OrFilterInfo;
import gcm.simulation.FilterInfo.PropertyFilterInfo;
import gcm.simulation.FilterInfo.RegionFilterInfo;

/**
 * A static utility class for determining which people match a filter. This is
 * generally much faster than checking each person against the filter.
 * 
 * @author Shawn Hatch
 *
 */
public class FilterPopulationMatcher {
	private static interface PersonIdSupplier {
		public List<PersonId> supply();
	}

	private static class UnionedPersonIdSupplier implements PersonIdSupplier {

		private final List<PersonIdSupplier> personIdSuppliers;

		private UnionedPersonIdSupplier(final List<PersonIdSupplier> personIdSuppliers) {
			this.personIdSuppliers = personIdSuppliers;
		}

		@Override
		public List<PersonId> supply() {
			final Set<PersonId> set = new LinkedHashSet<>();
			for (final PersonIdSupplier personIdSupplier : personIdSuppliers) {
				set.addAll(personIdSupplier.supply());
			}
			return new ArrayList<>(set);
		}

	}

	/*
	 * Represents the solution to a filter node. Note that this contains a
	 * PersonIdSupplier and not the actual person ids that match the filter
	 * node's filter.
	 */
	private static class FilterSolution {
		private final PersonIdSupplier personIdSupplier;
		private final boolean mayContainFalsePositives;
		private final int searchSize;

		private FilterSolution(final PersonIdSupplier personIdSupplier, final int searchSize, final boolean mayContainFalsePositives) {
			this.personIdSupplier = personIdSupplier;
			this.searchSize = searchSize;
			this.mayContainFalsePositives = mayContainFalsePositives;
		}

		public PersonIdSupplier getPersonIdSupplier() {
			return personIdSupplier;
		}

		public int getSearchSize() {
			return searchSize;
		}

		public boolean mayContainFalsePositives() {
			return mayContainFalsePositives;
		}
	}

	private List<PersonId> execute() {

		// Ask the filter for a solution
		final FilterSolution filterSolution = processNode(filterInfo, false);

		// Ask the solution to supply the person ids that may pass the filter
		final List<PersonId> personIds = filterSolution.getPersonIdSupplier().supply();

		/*
		 * If the solution possibly contains no false positives -- person ids
		 * that will not pass the filter -- then simply return the list of
		 * person ids
		 */
		if (!filterSolution.mayContainFalsePositives()) {
			return personIds;
		}
		/*
		 * Since there are possible false positives, test each person id against
		 * the filter and return only those that pass
		 */

		FilterEvaluator filterEvaluator = FilterEvaluator.build(filterInfo);

		return personIds.stream()//
						.filter(personId -> filterEvaluator.evaluate(environment, personId))//
						.collect(Collectors.toCollection(ArrayList::new));//

	}

	private static FilterSolution getFullPopulationFilterSolution(final Environment environment, final boolean mayContainFalsePositives) {
		return new FilterSolution(() -> environment.getPeople(), environment.getPopulationCount(), mayContainFalsePositives);
	}

	private static FilterSolution getEmptyPopulationFilterSolution() {
		return new FilterSolution(() -> new ArrayList<>(), 0, false);
	}

	private FilterSolution processNode(FilterInfo filterInfo, boolean negation) {
		switch (filterInfo.getFilterInfoType()) {
		case REGION:
			RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
			if (negation) {
				int regionPopulationCount = 0;
				for (RegionId regionId : regionFilterInfo.getRegionIds()) {
					regionPopulationCount += environment.getRegionPopulationCount(regionId);
				}
				final int totalPopulationCount = environment.getPopulationCount();
				final int peopleNotInRegionCount = totalPopulationCount - regionPopulationCount;
				return new FilterSolution(() -> environment.getPeople(), peopleNotInRegionCount, true);
			}
			final MapOption regionMapOption = environment.getRegionMapOption();
			if (regionMapOption != MapOption.NONE) {
				int regionPopulationCount = 0;
				for (RegionId regionId : regionFilterInfo.getRegionIds()) {
					regionPopulationCount += environment.getRegionPopulationCount(regionId);
				}				
				return new FilterSolution(() -> {
					List<PersonId> result = new ArrayList<>();
					for (RegionId regionId : regionFilterInfo.getRegionIds()) {
						result.addAll(environment.getPeopleInRegion(regionId));
					}
					return result;
				}, regionPopulationCount, false);				
			} else {
				final int populationCount = environment.getPopulationCount();
				return new FilterSolution(() -> environment.getPeople(), populationCount, true);
			}
		case COMPARTMENT:
			CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
			CompartmentId compartmentId = compartmentFilterInfo.getCompartmentId();
			if (negation) {
				final int compartmentPopulationCount = environment.getCompartmentPopulationCount(compartmentId);
				final int totalPopulationCount = environment.getPopulationCount();
				final int peopleNotInCompartmentCount = totalPopulationCount - compartmentPopulationCount;
				return new FilterSolution(() -> environment.getPeople(), peopleNotInCompartmentCount, true);
			}

			final MapOption compartmentMapOption = environment.getCompartmentMapOption();
			if (compartmentMapOption != MapOption.NONE) {
				final int compartmentPopulationCount = environment.getCompartmentPopulationCount(compartmentId);
				return new FilterSolution(() -> environment.getPeopleInCompartment(compartmentId), compartmentPopulationCount, false);
			} else {
				return getFullPopulationFilterSolution(environment, true);
			}
		case NEGATE:
			NegateFilterInfo negateFilterInfo = (NegateFilterInfo) filterInfo;
			return processNode(negateFilterInfo.getA(), !negation);
		case AND:
			AndFilterInfo andFilterInfo = (AndFilterInfo) filterInfo;
			if (negation) {
				final List<PersonIdSupplier> personIdSuppliers = new ArrayList<>();
				int searchSize = 0;
				boolean mayContainFalsePositives = false;

				FilterSolution filterSolution = processNode(andFilterInfo.getA(), negation);
				personIdSuppliers.add(filterSolution.getPersonIdSupplier());
				searchSize += filterSolution.searchSize;
				mayContainFalsePositives |= filterSolution.mayContainFalsePositives();

				filterSolution = processNode(andFilterInfo.getB(), negation);
				personIdSuppliers.add(filterSolution.getPersonIdSupplier());
				searchSize += filterSolution.searchSize;
				mayContainFalsePositives |= filterSolution.mayContainFalsePositives();

				// If the search size is greater than the population, then don't
				// bother with a union of the various person id suppliers and
				// just return one that includes the entire population.
				if (searchSize >= environment.getPopulationCount()) {
					return getFullPopulationFilterSolution(environment, true);
				}
				return new FilterSolution(new UnionedPersonIdSupplier(personIdSuppliers), searchSize, mayContainFalsePositives);
			} else {
				FilterSolution bestFilterSolution = processNode(andFilterInfo.getA(), negation);
				final FilterSolution bSolution = processNode(andFilterInfo.getB(), negation);
				if (bSolution.getSearchSize() < bestFilterSolution.searchSize) {
					bestFilterSolution = bSolution;
				}
				return new FilterSolution(bestFilterSolution.getPersonIdSupplier(), bestFilterSolution.searchSize, true);
			}
		case OR:
			OrFilterInfo orFilterInfo = (OrFilterInfo) filterInfo;
			if (negation) {
				FilterSolution bestFilterSolution = processNode(orFilterInfo.getA(), negation);
				final FilterSolution bSolution = processNode(orFilterInfo.getB(), negation);
				if (bSolution.getSearchSize() < bestFilterSolution.searchSize) {
					bestFilterSolution = bSolution;
				}
				return new FilterSolution(bestFilterSolution.getPersonIdSupplier(), bestFilterSolution.searchSize, true);

			} else {
				final List<PersonIdSupplier> personIdSuppliers = new ArrayList<>();
				int searchSize = 0;
				boolean mayContainFalsePositives = false;

				FilterSolution filterSolution = processNode(orFilterInfo.getA(), negation);
				personIdSuppliers.add(filterSolution.getPersonIdSupplier());
				searchSize += filterSolution.searchSize;
				mayContainFalsePositives |= filterSolution.mayContainFalsePositives();

				filterSolution = processNode(orFilterInfo.getB(), negation);
				personIdSuppliers.add(filterSolution.getPersonIdSupplier());
				searchSize += filterSolution.searchSize;
				mayContainFalsePositives |= filterSolution.mayContainFalsePositives();

				// If the search size is greater than the population, then don't
				// bother with a union of the various person id suppliers and
				// just return one that includes the entire population.
				if (searchSize >= environment.getPopulationCount()) {
					return getFullPopulationFilterSolution(environment, true);
				}
				return new FilterSolution(new UnionedPersonIdSupplier(personIdSuppliers), searchSize, mayContainFalsePositives);

			}

		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			PersonPropertyId personPropertyId = propertyFilterInfo.getPersonPropertyId();
			Equality equality = propertyFilterInfo.getEquality();
			Object personPropertyValue = propertyFilterInfo.getPersonPropertyValue();

			final PropertyDefinition personPropertyDefinition = environment.getPersonPropertyDefinition(personPropertyId);
			final boolean valuesAreMappedToPeople = personPropertyDefinition.getMapOption() != MapOption.NONE;

			if (valuesAreMappedToPeople) {
				final boolean propertyClassIsBoolean = personPropertyDefinition.getType().equals(Boolean.class);

				if (propertyClassIsBoolean) {
					/*
					 * Special handling for boolean properties : We know that
					 * the property value with be either TRUE or FALSE. We will
					 * derive the logically equivalent value to query from the
					 * obervable environment by combining the conditions in the
					 * filter and the possible negation being imposed by the
					 * filters above us.
					 */
					final boolean booleanPropertyValue = (Boolean) personPropertyValue;
					boolean equivalentBooleanPropertyValueAfterEqualityAndNegation;
					Equality equalityAfterNegation = equality;
					if (negation) {
						equalityAfterNegation = Equality.getNegation(equalityAfterNegation);
					}
					switch (equalityAfterNegation) {
					case EQUAL:
						if (booleanPropertyValue) {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = true;
						} else {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = false;
						}
						break;
					case GREATER_THAN:
						if (booleanPropertyValue) {
							return getEmptyPopulationFilterSolution();
						} else {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = true;
						}
						break;
					case GREATER_THAN_EQUAL:
						if (booleanPropertyValue) {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = true;
						} else {
							return getFullPopulationFilterSolution(environment, false);
						}
						break;
					case LESS_THAN:
						if (booleanPropertyValue) {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = false;
						} else {
							return getEmptyPopulationFilterSolution();
						}
						break;
					case LESS_THAN_EQUAL:
						if (booleanPropertyValue) {
							return getFullPopulationFilterSolution(environment, false);
						} else {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = false;
						}
						break;
					case NOT_EQUAL:
						if (booleanPropertyValue) {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = false;
						} else {
							equivalentBooleanPropertyValueAfterEqualityAndNegation = true;
						}
						break;
					default:
						throw new RuntimeException("unhandled case " + equalityAfterNegation);
					}

					final int personCountForPropertyValue = environment.getPersonCountForPropertyValue(personPropertyId, equivalentBooleanPropertyValueAfterEqualityAndNegation);
					return new FilterSolution(() -> environment.getPeopleWithPropertyValue(personPropertyId, equivalentBooleanPropertyValueAfterEqualityAndNegation), personCountForPropertyValue,
							false);

				}
				/*
				 * Special handling for properties that are logically equivalent
				 * to an equality comparison to a value.
				 *
				 * We have a special and oft encountered case where the filter
				 * is selecting for people who have a specific property value
				 * and that value is currently mapped to people in the property
				 * manger.
				 */
				final boolean logicallyEquivalentToEquality = (!negation && (equality == Equality.EQUAL)) || (negation && (equality == Equality.NOT_EQUAL));
				if (logicallyEquivalentToEquality) {
					final int personCountForPropertyValue = environment.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
					return new FilterSolution(() -> environment.getPeopleWithPropertyValue(personPropertyId, personPropertyValue), personCountForPropertyValue, false);
				}
			}
			return getFullPopulationFilterSolution(environment, true);
		case ALL:
			return getFullPopulationFilterSolution(environment, false);
		case EMPTY:
			return getEmptyPopulationFilterSolution();
		case GROUPS_FOR_PERSON:
			/*
			 * There is no listing of people or count of people who have some
			 * particular number of group associations.
			 */
			return getFullPopulationFilterSolution(environment, true);
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			/*
			 * There is no fast way to determine how many people have at least
			 * one membership in a group of the given type, let alone how many
			 * people have some specific count of such memberships.
			 */

			// GroupsForPersonAndGroupTypeFilter
			// groupsForPersonAndGroupTypeFilter =
			// (GroupsForPersonAndGroupTypeFilter) filter;
			// groupsForPersonAndGroupTypeFilter.groupTypeId
			// groupsForPersonAndGroupTypeFilter.groupCount
			// groupsForPersonAndGroupTypeFilter.equality
			// environment.getPersonCountForGroupType(groupsForPersonAndGroupTypeFilter.groupTypeId)

			return getFullPopulationFilterSolution(environment, true);
		case GROUP_MEMBER:
			GroupMemberFilterInfo groupMemberFilterInfo = (GroupMemberFilterInfo) filterInfo;
			GroupId groupId = groupMemberFilterInfo.getGroupId();
			if (!negation) {
				final int personCountForGroup = environment.getPersonCountForGroup(groupId);
				return new FilterSolution(() -> environment.getPeopleForGroup(groupId), personCountForGroup, false);
			}
			return getFullPopulationFilterSolution(environment, true);
		case GROUP_TYPES_FOR_PERSON:
			/*
			 * There is no mapping of group membership counts to people to
			 * support this filter.
			 */
			return getFullPopulationFilterSolution(environment, true);
		case RESOURCE:
			// currently, there is no support for anything other than a
			// brute force walk
			return getFullPopulationFilterSolution(environment, true);
		default:
			throw new RuntimeException("unhandled FilterInfoType " + filterInfo.getFilterInfoType());
		}

	}

	public static List<PersonId> getMatchingPeople(FilterInfo filterInfo, Environment environment) {
		return new FilterPopulationMatcher(filterInfo, environment).execute();
	}

	private final FilterInfo filterInfo;
	private final Environment environment;

	private FilterPopulationMatcher(FilterInfo filterInfo, Environment environment) {
		this.filterInfo = filterInfo;
		this.environment = environment;
	}

}
