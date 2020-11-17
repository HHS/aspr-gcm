package gcm.simulation.partition;

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
import gcm.simulation.PartitionEfficiencyWarning;
import gcm.simulation.PartitionEfficiencyWarning.Builder;
import gcm.simulation.ProfileManager;
import gcm.simulation.PropertyManager;
import gcm.simulation.SimulationWarningManager;
import gcm.simulation.StochasticPersonSelection;
import gcm.simulation.Trigger;
import gcm.simulation.group.PersonGroupManger;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of PartitionManager
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
	private final Map<Object, PopulationPartition> indexedPopulationMap = new LinkedHashMap<>();

	/*
	 * When an index's filter cannot be matched to any of the maps that we use to
	 * selectively update indexes, we choose to add that index to the
	 * unfilteredIndexedPopulations. The indexes in this set will be updated for
	 * every change as a precaution even though the filter is essentially empty.
	 */
	private final Set<PopulationPartition> unfilteredIndexedPopulations = new LinkedHashSet<>();

	/*
	 * Matches filters that are triggered by person resource changes for a
	 * particular resource.
	 */
	private final Map<ResourceId, Set<PopulationPartition>> resourceIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person region changes for a particular
	 * region.
	 */
	private final Map<RegionId, Set<PopulationPartition>> regionIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person compartment changes for a
	 * particular compartment.
	 */
	private final Map<CompartmentId, Set<PopulationPartition>> compartmentIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property.
	 */
	private final Map<PersonPropertyId, Set<PopulationPartition>> propertyIdIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property and property value.
	 */
	private final Map<PersonPropertyId, Map<Object, Set<PopulationPartition>>> propertyValueIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group.
	 */
	private final Map<GroupId, Set<PopulationPartition>> groupIndexedPopulations = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group type.
	 */
	private final Map<GroupTypeId, Set<PopulationPartition>> groupTypeIndexedPopulations = new LinkedHashMap<>();

	private final Set<PopulationPartition> groupIndexedPopulationsWhoJustDontCare = new LinkedHashSet<>();

	private PersonLocationManger personLocationManger;

	private PropertyManager propertyManager;

	private PersonGroupManger personGroupManger;

	private Context context;

	private boolean useProfiledPartitions;

	private ProfileManager profileManager;

	private SimulationWarningManager simulationWarningManager;

	private long masterTransactionId;

	private long getNextTransactionId() {
		return masterTransactionId++;
	}

	@Override
	public void init(final Context context) {
		super.init(context);
		useProfiledPartitions = context.produceProfileItems();
		this.context = context;
		this.personLocationManger = context.getPersonLocationManger();
		this.propertyManager = context.getPropertyManager();
		this.personGroupManger = context.getPersonGroupManger();
		this.profileManager = context.getProfileManager();
		this.simulationWarningManager = context.getSimulationWarningManager();
	}

	@Override
	public void addPartition(final ComponentId componentId, final Partition partition, final Object key) {
		/*
		 * 
		 * We must integrate the indexedPopulation into the various mapping structures
		 * based on the filtering associated with the indexedPopulation. This allows the
		 * manager to observe a data change to a person and quickly identify the set of
		 * Indexed populations that may need to be updated.
		 * 
		 * If no filtering was found, then we place the indexed population in the
		 * unfilteredIndexedPopulations set. These indexed populations are forced to
		 * evaluate each person for every data change.
		 * 
		 * After the indexed population is fully integrated we initialize the indexed
		 * population. This will cause the indexed population to perform a one-time,
		 * potentially expensive and complex query of the Environment to establish its
		 * sub-set of the current population. Maintenance of the indexed population is
		 * performed by the various handle() methods of this IndexedPopulationManager.
		 * 
		 * 
		 */
		if (componentId == null) {
			throw new RuntimeException("null component id");
		}

		if (partition == null) {
			throw new RuntimeException("null population partition definition");
		}

		if (key == null) {
			throw new RuntimeException("null key");
		}

		if (indexedPopulationMap.get(key) != null) {
			throw new RuntimeException("duplicated key" + key);
		}

		
		FilterInfo filterInfo = FilterInfo.build(partition.getFilter().orElse(Filter.allPeople()));

		/*
		 * Review the filter, looking for map option setting that might improve the
		 * performance of the index initialization and warning the user if any of those
		 * map options are set to NONE
		 */
		List<Object> filterAttributesNeedingReverseMapSupport = FilterMapOptionAnalyzer
				.getAttributesNeedingReverseMapping(filterInfo, context);
		if (!filterAttributesNeedingReverseMapSupport.isEmpty()) {
			Builder builder = PartitionEfficiencyWarning.builder();
			for (Object attribute : filterAttributesNeedingReverseMapSupport) {
				builder.addAttribute(attribute);
			}

			PartitionEfficiencyWarning partitionEfficiencyWarning = //
					builder.setFilterInfo(filterInfo)//
							.setPartitionId(key)//
							.build(); //
			simulationWarningManager.processPartitionEfficiencyWarning(partitionEfficiencyWarning);
		}

		PopulationPartition populationPartition;
		if (partition.isDegenerate()) {
			populationPartition = new DegeneratePopulationPartitionImpl(key, context, partition,
					componentId);
		} else {
			populationPartition = new PopulationPartitionImpl(key, context, partition,
					componentId);
		}
		
		if (useProfiledPartitions) {
			populationPartition = profileManager.getProfiledProxy(populationPartition);
		}

		int filterCount = 0;

		Trigger trigger = new Trigger(filterInfo, context);

		final Set<CompartmentId> compartmentIds;
		 
		if (partition.getCompartmentPartitionFunction().isPresent()) {
			compartmentIds = context.getScenario().getCompartmentIds();
		} else {
			compartmentIds = trigger.getCompartmentIdentifiers();
		}

		for (final CompartmentId compartmentId : compartmentIds) {
			Set<PopulationPartition> set = compartmentIndexedPopulations.get(compartmentId);
			if (set == null) {
				set = new LinkedHashSet<>();
				compartmentIndexedPopulations.put(compartmentId, set);
			}
			set.add(populationPartition);
			filterCount++;
		}

		final Set<RegionId> regionIds;
		if (partition.getRegionPartitionFunction().isPresent()) {
			// TODO -- do something faster than this
			regionIds = context.getScenario().getRegionIds();
		} else {
			regionIds = trigger.getRegionIdentifiers();
		}

		for (final RegionId regionId : regionIds) {
			Set<PopulationPartition> set = regionIndexedPopulations.get(regionId);
			if (set == null) {
				set = new LinkedHashSet<>();
				regionIndexedPopulations.put(regionId, set);
			}
			set.add(populationPartition);
			filterCount++;
		}

		Set<ResourceId> resourceIds = new LinkedHashSet<>(partition.getPersonResourceIds());
		resourceIds.addAll(trigger.getResourceIdentifiers());
		for (final ResourceId resourceId : resourceIds) {

			Set<PopulationPartition> set = resourceIndexedPopulations.get(resourceId);
			if (set == null) {
				set = new LinkedHashSet<>();
				resourceIndexedPopulations.put(resourceId, set);
			}
			set.add(populationPartition);
			filterCount++;
		}

		// The trigger's value-sensitive and value-insensitive property ids are expected
		// to be disjoint. However, the partition may demand evaluation for a particular
		// property id, and thus for all values of that property, overriding the
		// expectations of the trigger. We blend those expectations, favoring
		// value-insensitive re-evaluations.

		Set<PersonPropertyId> valueInsensitivePropertyIdentifiers = trigger.getValueInsensitivePropertyIdentifiers();
		valueInsensitivePropertyIdentifiers.addAll(partition.getPersonPropertyIds());

		Set<PersonPropertyId> valueSensitivePropertyIdentifiers = trigger.getValueSensitivePropertyIdentifiers();
		valueSensitivePropertyIdentifiers.removeAll(valueInsensitivePropertyIdentifiers);

		for (final PersonPropertyId personPropertyId : valueInsensitivePropertyIdentifiers) {
			Set<PopulationPartition> set = propertyIdIndexedPopulations.get(personPropertyId);
			if (set == null) {
				set = new LinkedHashSet<>();
				propertyIdIndexedPopulations.put(personPropertyId, set);
			}
			set.add(populationPartition);

			filterCount++;
		}

		for (final PersonPropertyId personPropertyId : valueSensitivePropertyIdentifiers) {

			final Set<Object> personPropertyValues = trigger.getPropertyValues(personPropertyId);

			Map<Object, Set<PopulationPartition>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map == null) {
				map = new LinkedHashMap<>();
				propertyValueIndexedPopulations.put(personPropertyId, map);
			}

			for (final Object personPropertyValue : personPropertyValues) {
				Set<PopulationPartition> set = map.get(personPropertyValue);
				if (set == null) {
					set = new LinkedHashSet<>();
					map.put(personPropertyValue, set);
				}
				set.add(populationPartition);
				filterCount++;
			}

		}

		if (partition.getGroupPartitionFunction().isPresent()) {
			groupIndexedPopulationsWhoJustDontCare.add(populationPartition);
		}

		for (GroupId groupId : trigger.getGroupIdentifiers()) {
			Set<PopulationPartition> set = groupIndexedPopulations.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupIndexedPopulations.put(groupId, set);
			}
			set.add(populationPartition);

			filterCount++;
		}

		for (GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			Set<PopulationPartition> set = groupTypeIndexedPopulations.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupTypeIndexedPopulations.put(groupTypeId, set);
			}
			set.add(populationPartition);
			filterCount++;
		}

		if (filterCount == 0) {
			unfilteredIndexedPopulations.add(populationPartition);
		}

		populationPartition.init();
		indexedPopulationMap.put(key, populationPartition);
	}

	@Override
	public List<PersonId> getPeople(final Object key) {
		return indexedPopulationMap.get(key).getPeople();
	}

	@Override
	public int getPersonCount(final Object key) {
		return indexedPopulationMap.get(key).getPeopleCount();
	}

	@Override
	public int getPersonCount(final Object key, LabelSet labelSet) {
		// TODO -- we should convert to label set info instances at the environment
		// level during validation
		return indexedPopulationMap.get(key).getPeopleCount(labelSet);
	}

	@Override
	public StochasticPersonSelection samplePartition(Object key, PartitionSampler partitionSampler) {
		return indexedPopulationMap.get(key).samplePartition(partitionSampler);
	}

	@Override
	public void handlePersonAddition(final PersonId personId) {

		/*
		 * We want to avoid having an indexed population evaluate a person when the
		 * filter associated with that indexed population is guaranteed to reject the
		 * person.
		 * 
		 * We also want to avoid having an indexed population evaluate a person more
		 * than once. In practice, it seems that few of the indexed populations will be
		 * called to evaluate a person multiple times and that creating a set of unique
		 * indexed populations to limit duplicate evaluations is more runtime expensive
		 * than just allowing this to happen.
		 * 
		 * Integrate the person into the relevant indices. For each property id we get
		 * the indices that are known to filter on that property value and ask it to
		 * evaluate the person for inclusion in the index. We then follow a similar
		 * process for regions, compartments and resources.
		 * 
		 * 
		 */
		long transactionId = getNextTransactionId();

		for (final ResourceId resourceId : resourceIndexedPopulations.keySet()) {
			Set<PopulationPartition> populationPartitions = resourceIndexedPopulations.get(resourceId);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handleAddPerson(transactionId, personId);
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyValueIndexedPopulations.keySet()) {
			final Object personPropertyValue = propertyManager.getPersonPropertyValue(personId, personPropertyId);
			final Map<Object, Set<PopulationPartition>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map != null) {
				final Set<PopulationPartition> populationPartitions = map.get(personPropertyValue);
				if (populationPartitions != null) {
					for (final PopulationPartition populationPartition : populationPartitions) {
						populationPartition.handleAddPerson(transactionId, personId);
					}
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyIdIndexedPopulations.keySet()) {
			final Set<PopulationPartition> populationPartitions = propertyIdIndexedPopulations.get(personPropertyId);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handleAddPerson(transactionId, personId);
				}
			}
		}

		final RegionId regionId = personLocationManger.getPersonRegion(personId);
		Set<PopulationPartition> populationPartitions = regionIndexedPopulations.get(regionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleAddPerson(transactionId, personId);
			}
		}

		final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
		populationPartitions = compartmentIndexedPopulations.get(compartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleAddPerson(transactionId, personId);
			}
		}

		/*
		 * Note that we do not consider the group-based filtered poplulation partitions
		 * since a newly added person should not yet be in any group
		 */

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleAddPerson(transactionId, personId);
		}

	}

	@Override
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId,
			final CompartmentId newCompartmentId) {

		/*
		 * We identify the indexed populations associated with the two compartments and
		 * have each evaluate the person. Unfiltered indexed populations also evaluate
		 * the person.
		 */
		long transactionId = getNextTransactionId();
		Set<PopulationPartition> populationPartitions = compartmentIndexedPopulations.get(oldCompartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleCompartmentChange(transactionId, personId);
			}
		}

		populationPartitions = compartmentIndexedPopulations.get(newCompartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleCompartmentChange(transactionId, personId);
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleCompartmentChange(transactionId, personId);
		}

	}

	@Override
	public void handlePersonRegionChange(final PersonId personId, final RegionId oldRegionId,
			final RegionId newRegionId) {

		/*
		 * We identify the indexed populations associated with the two regions.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		long transactionId = getNextTransactionId();

		Set<PopulationPartition> populationPartitions = regionIndexedPopulations.get(oldRegionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRegionChange(transactionId, personId);
			}
		}

		populationPartitions = regionIndexedPopulations.get(newRegionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRegionChange(transactionId, personId);
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleRegionChange(transactionId, personId);
		}
	}

	@Override
	public void handlePersonGroupAddition(GroupId groupId, PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group or its
		 * group type.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		long transactionId = getNextTransactionId();

		for (final PopulationPartition populationPartition : groupIndexedPopulationsWhoJustDontCare) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}

		Set<PopulationPartition> populationPartitions = groupIndexedPopulations.get(groupId);
		if (populationPartitions != null) {
			for (PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}
		GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		populationPartitions = groupTypeIndexedPopulations.get(groupType);

		if (populationPartitions != null) {
			for (PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}
	}

	@Override
	public void handlePersonGroupRemoval(GroupId groupId, PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group or its
		 * group type.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		long transactionId = getNextTransactionId();
		for (final PopulationPartition populationPartition : groupIndexedPopulationsWhoJustDontCare) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}

		Set<PopulationPartition> populationPartitions = groupIndexedPopulations.get(groupId);
		if (populationPartitions != null) {
			for (PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}
		GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		populationPartitions = groupTypeIndexedPopulations.get(groupType);

		if (populationPartitions != null) {
			for (PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}

	}

	@Override
	public void handlePersonRemoval(final PersonId personId) {

		/*
		 * Reverses the handleAddPerson() and drops all references to the person.
		 */

		long transactionId = getNextTransactionId();

		for (final ResourceId resourceId : resourceIndexedPopulations.keySet()) {
			final Set<PopulationPartition> populationPartitions = resourceIndexedPopulations.get(resourceId);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handleRemovePerson(transactionId, personId);
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyValueIndexedPopulations.keySet()) {
			final Object personPropertyValue = propertyManager.getPersonPropertyValue(personId, personPropertyId);
			final Map<Object, Set<PopulationPartition>> map = propertyValueIndexedPopulations.get(personPropertyId);
			if (map != null) {
				final Set<PopulationPartition> populationPartitions = map.get(personPropertyValue);
				if (populationPartitions != null) {
					for (final PopulationPartition populationPartition : populationPartitions) {
						populationPartition.handleRemovePerson(transactionId, personId);
					}
				}
			}
		}

		for (final PersonPropertyId personPropertyId : propertyIdIndexedPopulations.keySet()) {
			final Set<PopulationPartition> populationPartitions = propertyIdIndexedPopulations.get(personPropertyId);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handleRemovePerson(transactionId, personId);
				}
			}
		}

		final RegionId regionId = personLocationManger.getPersonRegion(personId);
		Set<PopulationPartition> populationPartitions = regionIndexedPopulations.get(regionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRemovePerson(transactionId, personId);
			}
		}

		final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
		populationPartitions = compartmentIndexedPopulations.get(compartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRemovePerson(transactionId, personId);
			}
		}

		/*
		 * Note that we do not consider group-base filtered population partitions since
		 * a person should have been removed from their groups prior to their removal
		 * here.
		 */

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handleRemovePerson(transactionId, personId);
		}

	}

	@Override
	public void handlePersonResourceLevelChange(final PersonId personId, final ResourceId resourceId) {

		long transactionId = getNextTransactionId();

		final Set<PopulationPartition> populationPartitions = resourceIndexedPopulations.get(resourceId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handlePersonResourceChange(transactionId, personId, resourceId);
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handlePersonResourceChange(transactionId, personId, resourceId);
		}
	}

	@Override
	public boolean partitionExists(final Object key) {
		PopulationPartition populationPartition = indexedPopulationMap.get(key);
		return populationPartition != null;
	}

	@Override
	public void removePartition(final Object key) {

		/*
		 * Attempt to remove the indexed population from the main storage container for
		 * all indexed populations.
		 */
		final PopulationPartition populationPartition = indexedPopulationMap.get(key);

		indexedPopulationMap.remove(key);

		/*
		 * Remove the indexed population from the various filter-related maps and set.
		 */
		FilterInfo filterInfo = populationPartition.getFilterInfo();
		Partition partition = populationPartition.getPartition();

		Trigger trigger = new Trigger(filterInfo, context);

		final Set<CompartmentId> compartmentIds;
		if (partition.getCompartmentPartitionFunction().isPresent()) {
			compartmentIds = context.getScenario().getCompartmentIds();
		} else {
			compartmentIds = trigger.getCompartmentIdentifiers();
		}

		for (final CompartmentId compartmentId : compartmentIds) {
			final Set<PopulationPartition> set = compartmentIndexedPopulations.get(compartmentId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				compartmentIndexedPopulations.remove(compartmentId);
			}
		}

		final Set<RegionId> regionIds;
		if (partition.getRegionPartitionFunction().isPresent()) {
			// TODO -- do something faster than this
			regionIds = context.getScenario().getRegionIds();
		} else {
			regionIds = trigger.getRegionIdentifiers();
		}

		for (final RegionId regionId : regionIds) {
			final Set<PopulationPartition> set = regionIndexedPopulations.get(regionId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				regionIndexedPopulations.remove(regionId);
			}
		}

		Set<ResourceId> resourceIds = new LinkedHashSet<>(partition.getPersonResourceIds());
		resourceIds.addAll(trigger.getResourceIdentifiers());
		for (final ResourceId resourceId : resourceIds) {

			Set<PopulationPartition> set = resourceIndexedPopulations.get(resourceId);
			if (set == null) {
				set = new LinkedHashSet<>();
				resourceIndexedPopulations.put(resourceId, set);
			}
			set.add(populationPartition);

		}

		for (final ResourceId resourceId : resourceIds) {
			final Set<PopulationPartition> indexedPopulations = resourceIndexedPopulations.get(resourceId);
			indexedPopulations.remove(populationPartition);
		}

		// The trigger's value-sensitive and value-insensitive property ids are expected
		// to be disjoint. However, the partition may demand evaluation for a particular
		// property id, and thus for all values of that property, overriding the
		// expectations of the trigger. We blend those expectations, favoring
		// value-insensitive re-evaluations.

		Set<PersonPropertyId> valueInsensitivePropertyIdentifiers = trigger.getValueInsensitivePropertyIdentifiers();
		valueInsensitivePropertyIdentifiers.addAll(partition.getPersonPropertyIds());

		Set<PersonPropertyId> valueSensitivePropertyIdentifiers = trigger.getValueSensitivePropertyIdentifiers();
		valueSensitivePropertyIdentifiers.removeAll(valueInsensitivePropertyIdentifiers);

		for (final PersonPropertyId personPropertyId : valueInsensitivePropertyIdentifiers) {
			final Set<PopulationPartition> set = propertyIdIndexedPopulations.get(personPropertyId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				propertyIdIndexedPopulations.remove(personPropertyId);
			}
		}

		for (final PersonPropertyId personPropertyId : valueSensitivePropertyIdentifiers) {
			final Map<Object, Set<PopulationPartition>> map = propertyValueIndexedPopulations.get(personPropertyId);
			for (final Object personPropertyValue : trigger.getPropertyValues(personPropertyId)) {
				final Set<PopulationPartition> set = map.get(personPropertyValue);
				if (set != null) {
					set.remove(populationPartition);
					if (set.size() == 0) {
						map.remove(personPropertyValue);
					}
				}
			}
			if (map.size() == 0) {
				propertyValueIndexedPopulations.remove(personPropertyId);
			}
		}

		if (partition.getGroupPartitionFunction().isPresent()) {
			groupIndexedPopulationsWhoJustDontCare.remove(populationPartition);
		}

		for (GroupId groupId : trigger.getGroupIdentifiers()) {
			Set<PopulationPartition> set = groupIndexedPopulations.get(groupId);
			if (set != null) {
				set.remove(populationPartition);
				if (set.size() == 0) {
					groupIndexedPopulations.remove(groupId);
				}
			}
		}

		for (GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			Set<PopulationPartition> set = groupTypeIndexedPopulations.get(groupTypeId);
			if (set != null) {
				set.remove(populationPartition);
				if (set.size() == 0) {
					groupTypeIndexedPopulations.remove(groupTypeId);
				}
			}
		}

		unfilteredIndexedPopulations.remove(populationPartition);

	}

	@Override
	public void handlePersonPropertyValueChange(final PersonId personId, final PersonPropertyId personPropertyId,
			final Object oldValue, final Object newValue) {

		/*
		 * We identify the indexed populations associated with either the property id or
		 * the property value. Some indexed populations are interested in all values for
		 * a property whereas others are interested(usually due to equality constraints)
		 * in specific property values.
		 * 
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 * 
		 */

		long transactionId = getNextTransactionId();

		Set<PopulationPartition> populationPartitions = propertyIdIndexedPopulations.get(personPropertyId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handlePersonPropertyChange(transactionId, personId, personPropertyId);
			}
		}

		final Map<Object, Set<PopulationPartition>> map = propertyValueIndexedPopulations.get(personPropertyId);
		if (map != null) {
			populationPartitions = map.get(oldValue);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handlePersonPropertyChange(transactionId, personId, personPropertyId);
				}
			}

			populationPartitions = map.get(newValue);
			if (populationPartitions != null) {
				for (final PopulationPartition populationPartition : populationPartitions) {
					populationPartition.handlePersonPropertyChange(transactionId, personId, personPropertyId);
				}
			}
		}

		for (final PopulationPartition populationPartition : unfilteredIndexedPopulations) {
			populationPartition.handlePersonPropertyChange(transactionId, personId, personPropertyId);
		}
	}

	@Override
	public ComponentId getOwningComponent(final Object key) {
		return indexedPopulationMap.get(key).getOwningComponentId();
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		// TODO implement memory reporting
//		Map<ComponentId, List<IndexedPopulation>> map = new LinkedHashMap<>();
//		for (IndexedPopulation indexedPopulation : indexedPopulationMap.values()) {
//			ComponentId owningComponentId = indexedPopulation.getOwningComponentId();
//			List<IndexedPopulation> list = map.get(owningComponentId);
//			if (list == null) {
//				list = new ArrayList<>();
//				map.put(owningComponentId, list);
//			}
//			list.add(indexedPopulation);
//		}
//
//		for (ComponentId componentId : map.keySet()) {
//			List<IndexedPopulation> list = map.get(componentId);
//			boolean useIndex = list.size() > 1;
//			int index = 1;
//			for (IndexedPopulation indexedPopulation : list) {
//				String name = "Index for " + componentId.toString();
//				if (useIndex) {
//					name += "_" + index;
//					index++;
//				}
//				memoryPartition.addMemoryLink(this, indexedPopulation, name);
//			}
//		}

	}

	@Override
	public List<PersonId> getPeople(Object key, LabelSet labelSet) {
		return indexedPopulationMap.get(key).getPeople(labelSet);
	}

	@Override
	public boolean contains(PersonId personId, Object key) {
		return indexedPopulationMap.get(key).contains(personId);
	}

	@Override
	public boolean contains(PersonId personId, LabelSet labelSet, Object key) {
		return indexedPopulationMap.get(key).contains(personId, labelSet);
	}

	@Override
	public boolean validateLabelSet(Object key, LabelSet labelSet) {
		return indexedPopulationMap.get(key).validateLabelSetInfo(labelSet);
	}

	
//	public long getMemSizeOfPartition(Object partitionId) {
//		 PopulationPartition populationPartition = indexedPopulationMap.get(partitionId);
//		 populationPartition.report();
//		 MemSizer memSizer = context.getContextFreeMemSizer();
//		 //MemSizer memSizer = new MemSizer(false);
//		 memSizer.excludeClass(PersonId.class);
//		 return memSizer.getByteCount(populationPartition);
//	}
}