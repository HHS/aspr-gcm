package gcm.simulation.partition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.CompartmentId;
import gcm.scenario.ComponentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.BaseElement;
import gcm.simulation.Context;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.PersonLocationManger;
import gcm.simulation.PopulationIndexEfficiencyWarning;
import gcm.simulation.PopulationIndexEfficiencyWarning.Builder;
import gcm.simulation.ProfileManager;
import gcm.simulation.PropertyManager;
import gcm.simulation.SimulationWarningManager;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.Trigger;
import gcm.simulation.group.PersonGroupManger;
import gcm.simulation.index.Filter;
import gcm.simulation.index.FilterInfo;
import gcm.simulation.index.FilterMapOptionAnalyzer;
import gcm.simulation.index.IndexedPopulation;
import gcm.simulation.index.IndexedPopulationImpl;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of IndexedPopulationManager
 *
 * @author Shawn Hatch
 *
 */

/*
 *
 * 
 * <li>Various maps : When a population index is added to this manager, the
 * manager examines the population index to determine what type of data changes
 * to a person might require the index to evaluate the person for membership
 * status. Population indexes can filter on compartments, regions, resources,
 * entire person property types and specific person property values. Thus a map
 * is kept for each of these keyed to the particular data type and valued with
 * the set of Population Indexes sensitive to changes in those keys. Special
 * treatment is given to cover the case where a population index does not filter
 * the population.
 * 
 * 
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PartitionManagerImpl extends BaseElement implements PartitionManager {

	/*
	 * The principle container for all contained IndexedPopulations.
	 */
	private final Map<Object, IndexedPopulation> indexedPopulationMap = new LinkedHashMap<>();

	/*
	 * When an index's filter cannot be matched to any of the maps that we use
	 * to selectively update indexes, we choose to add that index to the
	 * unfilteredIndexedPopulations. The indexes in this set will be updated for
	 * every change as a precaution even though the filter is essentially empty.
	 */
	private final Set<IndexedPopulation> unfilteredIndexedPopulations = new LinkedHashSet<>();

	/*
	 * Matches filters that are triggered by person resource changes for a
	 * particular resource.
	 */
	private final Map<ResourceId, Set<IndexedPopulation>> resourceIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person region changes for a
	 * particular region.
	 */
	private final Map<RegionId, Set<IndexedPopulation>> regionIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person compartment changes for a
	 * particular compartment.
	 */
	private final Map<CompartmentId, Set<IndexedPopulation>> compartmentIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property.
	 */
	private final Map<PersonPropertyId, Set<IndexedPopulation>> propertyIdIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property and property value.
	 */
	private final Map<PersonPropertyId, Map<Object, Set<IndexedPopulation>>> propertyValueIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group.
	 */
	private final Map<GroupId, Set<IndexedPopulation>> groupIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group type.
	 */
	private final Map<GroupTypeId, Set<IndexedPopulation>> groupTypeIndexedPopulations = new LinkedHashMap<>();

	private PersonLocationManger personLocationManger;

	private PropertyManager propertyManager;

	private PersonGroupManger personGroupManger;

	private Context context;

	private boolean useProfiledFilters;

	private ProfileManager profileManager;

	private SimulationWarningManager simulationWarningManager;

	@Override
	public void init(final Context context) {
		super.init(context);
		useProfiledFilters = context.produceProfileItems();
		this.context = context;
		this.personLocationManger = context.getPersonLocationManger();
		this.propertyManager = context.getPropertyManager();
		this.personGroupManger = context.getPersonGroupManger();
		this.profileManager = context.getProfileManager();
		this.simulationWarningManager = context.getSimulationWarningManager();
	}

	@Override
	public void addFilteredPartition(final ComponentId componentId, final Filter filter, final Partition partition, final Object key) {
		/*
		 * 
		 * We must integrate the indexedPopulation into the various mapping
		 * structures based on the filtering associated with the
		 * indexedPopulation. This allows the manager to observe a data change
		 * to a person and quickly identify the set of Indexed populations that
		 * may need to be updated.
		 * 
		 * If no filtering was found, then we place the indexed population in
		 * the unfilteredIndexedPopulations set. These indexed populations are
		 * forced to evaluate each person for every data change.
		 * 
		 * After the indexed population is fully integrated we initialize the
		 * indexed population. This will cause the indexed population to perform
		 * a one-time, potentially expensive and complex query of the
		 * Environment to establish its sub-set of the current population.
		 * Maintenance of the indexed population is performed by the various
		 * handle() methods of this IndexedPopulationManager.
		 * 
		 * 
		 */
		FilterInfo filterInfo = FilterInfo.build(filter);

		/*
		 * Review the filter, looking for map option setting that might improve
		 * the performance of the index initialization and warning the user if
		 * any of those map options are set to NONE
		 */
		List<Object> filterAttributesNeedingReverseMapSupport = FilterMapOptionAnalyzer.getAttributesNeedingReverseMapping(filterInfo, context);
		if (!filterAttributesNeedingReverseMapSupport.isEmpty()) {
			Builder builder = PopulationIndexEfficiencyWarning.builder();
			for (Object attribute : filterAttributesNeedingReverseMapSupport) {
				builder.addAttribute(attribute);
			}

			PopulationIndexEfficiencyWarning populationIndexEfficiencyWarning = //
					builder	.setFilterInfo(filterInfo)//
							.setPopulationIndexId(key)//
							.build(); //
			simulationWarningManager.processPopulationIndexEfficiencyWarning(populationIndexEfficiencyWarning);
		}

		IndexedPopulation indexedPopulation = new IndexedPopulationImpl(context, componentId, key, filterInfo);
		if (useProfiledFilters) {
			indexedPopulation = profileManager.getProfiledProxy(indexedPopulation);
		}

		if (indexedPopulationMap.get(key) != null) {
			throw new RuntimeException("duplicated key" + key);
		}

		int filterCount = 0;

		Trigger trigger = new Trigger(filterInfo, context);

		final Set<CompartmentId> compartmentIds = trigger.getCompartmentIdentifiers();

		for (final CompartmentId compartmentId : compartmentIds) {
			Set<IndexedPopulation> set = compartmentIndexedPopulations.get(compartmentId);
			if (set == null) {
				set = new LinkedHashSet<>();
				compartmentIndexedPopulations.put(compartmentId, set);
			}
			set.add(indexedPopulation);
			filterCount++;
		}
		final Set<RegionId> regionIds = trigger.getRegionIdentifiers();

		for (final RegionId regionId : regionIds) {
			Set<IndexedPopulation> set = regionIndexedPopulations.get(regionId);
			if (set == null) {
				set = new LinkedHashSet<>();
				regionIndexedPopulations.put(regionId, set);
			}
			set.add(indexedPopulation);
			filterCount++;
		}

		for (final PersonPropertyId personPropertyId : trigger.getValueInsensitivePropertyIdentifiers()) {
			Set<IndexedPopulation> indexedPopulations = propertyIdIndexedPopulations.get(personPropertyId);
			if (indexedPopulations == null) {
				indexedPopulations = new LinkedHashSet<>();
				propertyIdIndexedPopulations.put(personPropertyId, indexedPopulations);
			}
			indexedPopulations.add(indexedPopulation);

			filterCount++;
		}

		for (final PersonPropertyId personPropertyId : trigger.getValueSensitivePropertyIdentifiers()) {

			final Set<Object> personPropertyValues = trigger.getPropertyValues(personPropertyId);

			Map<Object, Set<IndexedPopulation>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map == null) {
				map = new LinkedHashMap<>();
				propertyValueIndexedPopulations.put(personPropertyId, map);
			}

			for (final Object personPropertyValue : personPropertyValues) {
				Set<IndexedPopulation> set = map.get(personPropertyValue);
				if (set == null) {
					set = new LinkedHashSet<>();
					map.put(personPropertyValue, set);
				}
				set.add(indexedPopulation);
				filterCount++;
			}

		}

		for (final ResourceId resourceId : trigger.getResourceIdentifiers()) {

			Set<IndexedPopulation> indexedPopulations = resourceIndexedPopulations.get(resourceId);
			if (indexedPopulations == null) {
				indexedPopulations = new LinkedHashSet<>();
				resourceIndexedPopulations.put(resourceId, indexedPopulations);
			}
			indexedPopulations.add(indexedPopulation);
			filterCount++;
		}

		for (GroupId groupId : trigger.getGroupIdentifiers()) {
			Set<IndexedPopulation> indexedPopulations = groupIndexedPopulations.get(groupId);
			if (indexedPopulations == null) {
				indexedPopulations = new LinkedHashSet<>();
				groupIndexedPopulations.put(groupId, indexedPopulations);
			}
			indexedPopulations.add(indexedPopulation);

			filterCount++;
		}

		for (GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			Set<IndexedPopulation> indexedPopulations = groupTypeIndexedPopulations.get(groupTypeId);
			if (indexedPopulations == null) {
				indexedPopulations = new LinkedHashSet<>();
				groupTypeIndexedPopulations.put(groupTypeId, indexedPopulations);
			}
			indexedPopulations.add(indexedPopulation);
			filterCount++;
		}

		if (filterCount == 0) {
			unfilteredIndexedPopulations.add(indexedPopulation);
		}

		indexedPopulation.init();
		indexedPopulationMap.put(key, indexedPopulation);

	}

	@Override
	public List<PersonId> getPeople(final Object key) {
		return indexedPopulationMap.get(key).getPeople();
	}

	@Override
	public int getPersonCount(final Object key) {
		return indexedPopulationMap.get(key).size();
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key, PartitionSampler partitionSampler) {
		//TODO implement
		
		//return indexedPopulationMap.get(key).sampleIndex(excludedPersonId);
		return null;
	}


	

	@Override
	public void handlePersonAddition(final PersonId personId) {

		/*
		 * We want to avoid having an indexed population evaluate a person when
		 * the filter associated with that indexed population is guaranteed to
		 * reject the person.
		 * 
		 * We also want to avoid having an indexed population evaluate a person
		 * more than once. In practice, it seems that few of the indexed
		 * populations will be called to evaluate a person multiple times and
		 * that creating a set of unique indexed populations to limit duplicate
		 * evaluations is more runtime expensive than just allowing this to
		 * happen.
		 * 
		 * Integrate the person into the relevant indices. For each property id
		 * we get the indices that are known to filter on that property value
		 * and ask it to evaluate the person for inclusion in the index. We then
		 * follow a similar process for regions, compartments and resources.
		 * 
		 * 
		 */

		for (final ResourceId resourceId : resourceIndexedPopulations.keySet()) {

			final Set<IndexedPopulation> indexedPopulations = resourceIndexedPopulations.get(resourceId);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.evaluate(personId);
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyValueIndexedPopulations.keySet()) {
			final Object personPropertyValue = propertyManager.getPersonPropertyValue(personId, personPropertyId);
			final Map<Object, Set<IndexedPopulation>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map != null) {
				final Set<IndexedPopulation> indexedPopulations = map.get(personPropertyValue);
				if (indexedPopulations != null) {
					for (final IndexedPopulation indexedPopulation : indexedPopulations) {
						indexedPopulation.evaluate(personId);
					}
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyIdIndexedPopulations.keySet()) {
			final Set<IndexedPopulation> indexedPopulations = propertyIdIndexedPopulations.get(personPropertyId);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.evaluate(personId);
				}
			}
		}

		final RegionId regionId = personLocationManger.getPersonRegion(personId);
		Set<IndexedPopulation> indexedPopulations = regionIndexedPopulations.get(regionId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
		indexedPopulations = compartmentIndexedPopulations.get(compartmentId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}

	}

	@Override
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId, final CompartmentId newCompartmentId) {

		/*
		 * We identify the indexed populations associated with the two
		 * compartments and have each evaluate the person. Unfiltered indexed
		 * populations also evaluate the person.
		 */

		Set<IndexedPopulation> indexedPopulations = compartmentIndexedPopulations.get(oldCompartmentId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		indexedPopulations = compartmentIndexedPopulations.get(newCompartmentId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}

	}

	@Override
	public void handlePersonRegionChange(final PersonId personId, final RegionId oldRegionId, final RegionId newRegionId) {

		/*
		 * We identify the indexed populations associated with the two regions.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		Set<IndexedPopulation> indexedPopulations = regionIndexedPopulations.get(oldRegionId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		indexedPopulations = regionIndexedPopulations.get(newRegionId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}
	}

	@Override
	public void handlePersonGroupAddition(GroupId groupId, PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group
		 * or its group type.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		Set<IndexedPopulation> indexedPopulations = groupIndexedPopulations.get(groupId);
		if (indexedPopulations != null) {
			for (IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}
		GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		indexedPopulations = groupTypeIndexedPopulations.get(groupType);
		if (indexedPopulations != null) {
			for (IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}
		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}

	}

	@Override
	public void handlePersonGroupRemoval(GroupId groupId, PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group
		 * or its group type.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		Set<IndexedPopulation> indexedPopulations = groupIndexedPopulations.get(groupId);
		if (indexedPopulations != null) {
			for (IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}
		GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		indexedPopulations = groupTypeIndexedPopulations.get(groupType);
		if (indexedPopulations != null) {
			for (IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}
		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}
	}

	@Override
	public void handlePersonRemoval(final PersonId personId) {

		/*
		 * Reverses the handleAddPerson() and drops all references to the
		 * person.
		 */

		for (final ResourceId resourceId : resourceIndexedPopulations.keySet()) {
			final Set<IndexedPopulation> indexedPopulations = resourceIndexedPopulations.get(resourceId);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.remove(personId);
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyValueIndexedPopulations.keySet()) {
			final Object personPropertyValue = propertyManager.getPersonPropertyValue(personId, personPropertyId);
			final Map<Object, Set<IndexedPopulation>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map != null) {
				final Set<IndexedPopulation> indexedPopulations = map.get(personPropertyValue);
				if (indexedPopulations != null) {
					for (final IndexedPopulation indexedPopulation : indexedPopulations) {
						indexedPopulation.remove(personId);
					}
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyIdIndexedPopulations.keySet()) {
			final Set<IndexedPopulation> indexedPopulations = propertyIdIndexedPopulations.get(personPropertyId);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.remove(personId);
				}
			}
		}

		final RegionId regionId = personLocationManger.getPersonRegion(personId);
		Set<IndexedPopulation> indexedPopulations = regionIndexedPopulations.get(regionId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.remove(personId);
			}
		}

		final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
		indexedPopulations = compartmentIndexedPopulations.get(compartmentId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.remove(personId);
			}
		}

		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.remove(personId);
		}

	}

	@Override
	public void handlePersonResourceLevelChange(final PersonId personId, final ResourceId resourceId) {

		/*
		 * We identify the indexed populations associated with the two levels of
		 * resources.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		/*
		 * Evaluation of the unfiltered is done now since we may escape out on
		 * the resource associated indexed populations
		 */
		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}

		final Set<IndexedPopulation> indexedPopulations = resourceIndexedPopulations.get(resourceId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

	}

	@Override
	public boolean partitionExists(final Object key) {
		final IndexedPopulation indexedPopulation = indexedPopulationMap.get(key);
		return indexedPopulation != null;
	}

	@Override
	public void removePartition(final Object key) {

		/*
		 * Attempt to remove the indexed population from the main storage
		 * container for all indexed populations.
		 */
		final IndexedPopulation indexedPopulation = indexedPopulationMap.get(key);

		indexedPopulationMap.remove(key);

		/*
		 * Remove the indexed population from the various filter-related maps
		 * and set.
		 */
		FilterInfo filterInfo = indexedPopulation.getFilterInfo();
		Trigger trigger = new Trigger(filterInfo, context);

		for (final CompartmentId compartmentId : trigger.getCompartmentIdentifiers()) {
			final Set<IndexedPopulation> set = compartmentIndexedPopulations.get(compartmentId);
			set.remove(indexedPopulation);
			if (set.size() == 0) {
				compartmentIndexedPopulations.remove(compartmentId);
			}
		}

		for (final RegionId regionId : trigger.getRegionIdentifiers()) {
			final Set<IndexedPopulation> set = regionIndexedPopulations.get(regionId);
			set.remove(indexedPopulation);
			if (set.size() == 0) {
				regionIndexedPopulations.remove(regionId);
			}
		}

		for (final PersonPropertyId personPropertyId : trigger.getValueInsensitivePropertyIdentifiers()) {
			final Set<IndexedPopulation> indexedPopulations = propertyIdIndexedPopulations.get(personPropertyId);
			indexedPopulations.remove(indexedPopulation);
			if (indexedPopulations.size() == 0) {
				propertyIdIndexedPopulations.remove(personPropertyId);
			}
		}

		for (final PersonPropertyId personPropertyId : trigger.getValueSensitivePropertyIdentifiers()) {
			final Map<Object, Set<IndexedPopulation>> map = propertyValueIndexedPopulations.get(personPropertyId);
			for (final Object personPropertyValue : trigger.getPropertyValues(personPropertyId)) {
				final Set<IndexedPopulation> indexedPopulations = map.get(personPropertyValue);
				indexedPopulations.remove(indexedPopulation);
				if (indexedPopulations.size() == 0) {
					map.remove(personPropertyValue);
				}
			}
			if (map.size() == 0) {
				propertyValueIndexedPopulations.remove(personPropertyId);
			}
		}

		for (GroupId groupId : trigger.getGroupIdentifiers()) {
			Set<IndexedPopulation> indexedPopulations = groupIndexedPopulations.get(groupId);
			if (indexedPopulations != null) {
				indexedPopulations.remove(indexedPopulation);
				if (indexedPopulations.size() == 0) {
					groupIndexedPopulations.remove(groupId);
				}
			}
		}

		for (GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			Set<IndexedPopulation> indexedPopulations = groupTypeIndexedPopulations.get(groupTypeId);
			if (indexedPopulations != null) {
				indexedPopulations.remove(indexedPopulation);
				if (indexedPopulations.size() == 0) {
					groupTypeIndexedPopulations.remove(groupTypeId);
				}
			}
		}

		for (final ResourceId resourceId : trigger.getResourceIdentifiers()) {
			final Set<IndexedPopulation> indexedPopulations = resourceIndexedPopulations.get(resourceId);
			indexedPopulations.remove(indexedPopulation);
		}

		unfilteredIndexedPopulations.remove(indexedPopulation);

	}

	@Override
	public void handlePersonPropertyValueChange(final PersonId personId, final PersonPropertyId personPropertyId, final Object oldValue, final Object newValue) {

		/*
		 * We identify the indexed populations associated with either the
		 * property id or the property value. Some indexed populations are
		 * interested in all values for a property whereas others are
		 * interested(usually due to equality constraints) in specific property
		 * values.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		Set<IndexedPopulation> indexedPopulations = propertyIdIndexedPopulations.get(personPropertyId);
		if (indexedPopulations != null) {
			for (final IndexedPopulation indexedPopulation : indexedPopulations) {
				indexedPopulation.evaluate(personId);
			}
		}

		final Map<Object, Set<IndexedPopulation>> map = propertyValueIndexedPopulations.get(personPropertyId);
		if (map != null) {
			indexedPopulations = map.get(oldValue);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.evaluate(personId);
				}
			}

			indexedPopulations = map.get(newValue);
			if (indexedPopulations != null) {
				for (final IndexedPopulation indexedPopulation : indexedPopulations) {
					indexedPopulation.evaluate(personId);
				}
			}
		}

		for (final IndexedPopulation indexedPopulation : unfilteredIndexedPopulations) {
			indexedPopulation.evaluate(personId);
		}
	}

	@Override
	public ComponentId getOwningComponent(final Object key) {
		return indexedPopulationMap.get(key).getOwningComponentId();
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		Map<ComponentId, List<IndexedPopulation>> map = new LinkedHashMap<>();
		for (IndexedPopulation indexedPopulation : indexedPopulationMap.values()) {
			ComponentId owningComponentId = indexedPopulation.getOwningComponentId();
			List<IndexedPopulation> list = map.get(owningComponentId);
			if (list == null) {
				list = new ArrayList<>();
				map.put(owningComponentId, list);
			}
			list.add(indexedPopulation);
		}

		for (ComponentId componentId : map.keySet()) {
			List<IndexedPopulation> list = map.get(componentId);
			boolean useIndex = list.size() > 1;
			int index = 1;
			for (IndexedPopulation indexedPopulation : list) {
				String name = "Index for " + componentId.toString();
				if (useIndex) {
					name += "_" + index;
					index++;
				}
				memoryPartition.addMemoryLink(this, indexedPopulation, name);
			}
		}

	}

	@Override
	public List<PersonId> getPeople(Object key, LabelSet labelSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCellPersonCount(Object key, LabelSet labelSet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean contains(PersonId personId, Object key) {
		//return indexedPopulationMap.get(key).personInPopulationIndex(personId);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(PersonId personId, LabelSet labelSet, Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validateLabelSet(Object key, LabelSet labelSet) {
		// TODO Auto-generated method stub
		return false;
	}

}