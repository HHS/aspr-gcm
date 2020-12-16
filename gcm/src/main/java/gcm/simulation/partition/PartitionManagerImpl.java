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
import gcm.simulation.ProfileManager;
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
	 * The principle container for all contained PopulationPartitions.
	 */
	private final Map<Object, PopulationPartition> partitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person resource changes for a
	 * particular resource.
	 */
	private final Map<ResourceId, Set<PopulationPartition>> resourceSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person region changes for a
	 * particular region.
	 */
	private final Map<RegionId, Set<PopulationPartition>> regionSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person compartment changes for a
	 * particular compartment.
	 */
	private final Map<CompartmentId, Set<PopulationPartition>> compartmentSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property.
	 */
	private final Map<PersonPropertyId, Set<PopulationPartition>> propertyIdSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by person property value change for a
	 * particular person property and property value.
	 */
	private final Map<PersonPropertyId, Map<Object, Set<PopulationPartition>>> propertyValueSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group.
	 */
	private final Map<GroupId, Set<PopulationPartition>> groupIdSensitivePartitions = new LinkedHashMap<>();

	/*
	 * Matches filters that are triggered by group membership change for a
	 * particular group type.
	 */
	private final Map<GroupTypeId, Set<PopulationPartition>> groupTypeSensitivePartitions = new LinkedHashMap<>();

	private final Set<PopulationPartition> groupSensitivePartitions = new LinkedHashSet<>();

	private PersonGroupManger personGroupManger;

	private Context context;

	private boolean useProfiledPartitions;

	private ProfileManager profileManager;

	private long masterTransactionId;

	@Override
	public void addPartition(final ComponentId componentId, final Partition partition, final Object key) {
		/*
		 *
		 * We must integrate the partition into the various mapping structures
		 * based on the filtering and cell structure. This allows the manager to
		 * observe a data change to a person and quickly identify the set of
		 * partitions that may need to be updated.
		 *
		 * After the partition is categorized it is initialized. This will cause
		 * the partition to perform a one-time, potentially expensive and
		 * complex query of the Environment to establish its sub-set of the
		 * current population. Maintenance of the partition is performed by the
		 * various handle() methods of this PartitionManager.
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

		if (partitions.get(key) != null) {
			throw new RuntimeException("duplicated key" + key);
		}

		final FilterInfo filterInfo = FilterInfo.build(partition.getFilter().orElse(Filter.allPeople()));
		PopulationPartition populationPartition;
		if (partition.isDegenerate()) {
			populationPartition = new DegeneratePopulationPartitionImpl(key, context, partition, componentId);
		} else {
			populationPartition = new PopulationPartitionImpl(key, context, partition, componentId);
		}

		if (useProfiledPartitions) {
			populationPartition = profileManager.getProfiledProxy(populationPartition);
		}

		final Trigger trigger = new Trigger(filterInfo, context);

		final Set<CompartmentId> compartmentIds;

		if (partition.getCompartmentPartitionFunction().isPresent()) {
			compartmentIds = context.getScenario().getCompartmentIds();
		} else {
			compartmentIds = trigger.getCompartmentIdentifiers();
		}

		for (final CompartmentId compartmentId : compartmentIds) {
			Set<PopulationPartition> set = compartmentSensitivePartitions.get(compartmentId);
			if (set == null) {
				set = new LinkedHashSet<>();
				compartmentSensitivePartitions.put(compartmentId, set);
			}
			set.add(populationPartition);
		}

		final Set<RegionId> regionIds;
		if (partition.getRegionPartitionFunction().isPresent()) {
			// TODO -- do something faster than this
			regionIds = context.getScenario().getRegionIds();
		} else {
			regionIds = trigger.getRegionIdentifiers();
		}

		for (final RegionId regionId : regionIds) {
			Set<PopulationPartition> set = regionSensitivePartitions.get(regionId);
			if (set == null) {
				set = new LinkedHashSet<>();
				regionSensitivePartitions.put(regionId, set);
			}
			set.add(populationPartition);
		}

		final Set<ResourceId> resourceIds = new LinkedHashSet<>(partition.getPersonResourceIds());
		resourceIds.addAll(trigger.getResourceIdentifiers());
		for (final ResourceId resourceId : resourceIds) {

			Set<PopulationPartition> set = resourceSensitivePartitions.get(resourceId);
			if (set == null) {
				set = new LinkedHashSet<>();
				resourceSensitivePartitions.put(resourceId, set);
			}
			set.add(populationPartition);
		}

		// The trigger's value-sensitive and value-insensitive property ids are
		// expected
		// to be disjoint. However, the partition may demand evaluation for a
		// particular
		// property id, and thus for all values of that property, overriding the
		// expectations of the trigger. We blend those expectations, favoring
		// value-insensitive re-evaluations.

		final Set<PersonPropertyId> valueInsensitivePropertyIdentifiers = trigger.getValueInsensitivePropertyIdentifiers();
		valueInsensitivePropertyIdentifiers.addAll(partition.getPersonPropertyIds());

		final Set<PersonPropertyId> valueSensitivePropertyIdentifiers = trigger.getValueSensitivePropertyIdentifiers();
		valueSensitivePropertyIdentifiers.removeAll(valueInsensitivePropertyIdentifiers);

		for (final PersonPropertyId personPropertyId : valueInsensitivePropertyIdentifiers) {
			Set<PopulationPartition> set = propertyIdSensitivePartitions.get(personPropertyId);
			if (set == null) {
				set = new LinkedHashSet<>();
				propertyIdSensitivePartitions.put(personPropertyId, set);
			}
			set.add(populationPartition);

		}

		for (final PersonPropertyId personPropertyId : valueSensitivePropertyIdentifiers) {

			final Set<Object> personPropertyValues = trigger.getPropertyValues(personPropertyId);

			Map<Object, Set<PopulationPartition>> map = propertyValueSensitivePartitions.get(personPropertyId);
			if (map == null) {
				map = new LinkedHashMap<>();
				propertyValueSensitivePartitions.put(personPropertyId, map);
			}

			for (final Object personPropertyValue : personPropertyValues) {
				Set<PopulationPartition> set = map.get(personPropertyValue);
				if (set == null) {
					set = new LinkedHashSet<>();
					map.put(personPropertyValue, set);
				}
				set.add(populationPartition);
			}

		}

		if (partition.getGroupPartitionFunction().isPresent()) {
			groupSensitivePartitions.add(populationPartition);
		}

		for (final GroupId groupId : trigger.getGroupIdentifiers()) {
			Set<PopulationPartition> set = groupIdSensitivePartitions.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupIdSensitivePartitions.put(groupId, set);
			}
			set.add(populationPartition);

		}

		for (final GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			Set<PopulationPartition> set = groupTypeSensitivePartitions.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupTypeSensitivePartitions.put(groupTypeId, set);
			}
			set.add(populationPartition);
		}

		populationPartition.init();
		partitions.put(key, populationPartition);
	}

	@Override
	public void collectMemoryLinks(final MemoryPartition memoryPartition) {
		
		for(Object key : partitions.keySet()) {
			PopulationPartition populationPartition = partitions.get(key);
			populationPartition.getOwningComponentId();
			memoryPartition.addMemoryLink(this, populationPartition, key.toString());
		}
		
	}

	@Override
	public boolean contains(final PersonId personId, final LabelSet labelSet, final Object key) {
		return partitions.get(key).contains(personId, labelSet);
	}

	@Override
	public boolean contains(final PersonId personId, final Object key) {
		return partitions.get(key).contains(personId);
	}

	private long getNextTransactionId() {
		return masterTransactionId++;
	}

	@Override
	public ComponentId getOwningComponent(final Object key) {
		return partitions.get(key).getOwningComponentId();
	}

	@Override
	public List<PersonId> getPeople(final Object key) {
		return partitions.get(key).getPeople();
	}

	@Override
	public List<PersonId> getPeople(final Object key, final LabelSet labelSet) {
		return partitions.get(key).getPeople(labelSet);
	}

	@Override
	public int getPersonCount(final Object key) {
		return partitions.get(key).getPeopleCount();
	}

	@Override
	public int getPersonCount(final Object key, final LabelSet labelSet) {
		return partitions.get(key).getPeopleCount(labelSet);
	}

	@Override
	public void handlePersonAddition(final PersonId personId) {

		final long transactionId = getNextTransactionId();

		for (final PopulationPartition populationPartition : partitions.values()) {
			populationPartition.handleAddPerson(transactionId, personId);
		}

	}

	@Override
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId, final CompartmentId newCompartmentId) {

		/*
		 * We identify the indexed populations associated with the two
		 * compartments and have each evaluate the person. Unfiltered indexed
		 * populations also evaluate the person.
		 */
		final long transactionId = getNextTransactionId();
		Set<PopulationPartition> populationPartitions = compartmentSensitivePartitions.get(oldCompartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleCompartmentChange(transactionId, personId);
			}
		}

		populationPartitions = compartmentSensitivePartitions.get(newCompartmentId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleCompartmentChange(transactionId, personId);
			}
		}

	}

	@Override
	public void handlePersonGroupAddition(final GroupId groupId, final PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group
		 * or its group type.
		 *
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 *
		 */

		final long transactionId = getNextTransactionId();

		for (final PopulationPartition populationPartition : groupSensitivePartitions) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}

		Set<PopulationPartition> populationPartitions = groupIdSensitivePartitions.get(groupId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}
		final GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		populationPartitions = groupTypeSensitivePartitions.get(groupType);

		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}

	}

	@Override
	public void handlePersonGroupRemoval(final GroupId groupId, final PersonId personId) {
		/*
		 * We identify the indexed populations associated with either the group
		 * or its group type.
		 *
		 * Each identified indexed population, including the unfiltered indexed
		 * populations, are invoked to evaluate the person.
		 *
		 */

		final long transactionId = getNextTransactionId();
		for (final PopulationPartition populationPartition : groupSensitivePartitions) {
			populationPartition.handleGroupMembershipChange(transactionId, personId);
		}

		Set<PopulationPartition> populationPartitions = groupIdSensitivePartitions.get(groupId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}
		final GroupTypeId groupType = personGroupManger.getGroupType(groupId);

		populationPartitions = groupTypeSensitivePartitions.get(groupType);

		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleGroupMembershipChange(transactionId, personId);
			}
		}

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

		final long transactionId = getNextTransactionId();

		Set<PopulationPartition> populationPartitions = propertyIdSensitivePartitions.get(personPropertyId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handlePersonPropertyChange(transactionId, personId, personPropertyId);
			}
		}

		final Map<Object, Set<PopulationPartition>> map = propertyValueSensitivePartitions.get(personPropertyId);
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

		final long transactionId = getNextTransactionId();

		Set<PopulationPartition> populationPartitions = regionSensitivePartitions.get(oldRegionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRegionChange(transactionId, personId);
			}
		}

		populationPartitions = regionSensitivePartitions.get(newRegionId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handleRegionChange(transactionId, personId);
			}
		}

	}

	@Override
	public void handlePersonRemoval(final PersonId personId) {
		final long transactionId = getNextTransactionId();
		for (final PopulationPartition populationPartition : partitions.values()) {
			populationPartition.handleRemovePerson(transactionId, personId);
		}
	}

	@Override
	public void handlePersonResourceLevelChange(final PersonId personId, final ResourceId resourceId) {

		final long transactionId = getNextTransactionId();

		final Set<PopulationPartition> populationPartitions = resourceSensitivePartitions.get(resourceId);
		if (populationPartitions != null) {
			for (final PopulationPartition populationPartition : populationPartitions) {
				populationPartition.handlePersonResourceChange(transactionId, personId, resourceId);
			}
		}

	}

	@Override
	public void init(final Context context) {
		super.init(context);
		useProfiledPartitions = context.produceProfileItems();
		this.context = context;
		personGroupManger = context.getPersonGroupManger();
		profileManager = context.getProfileManager();
	}

	@Override
	public boolean partitionExists(final Object key) {
		final PopulationPartition populationPartition = partitions.get(key);
		return populationPartition != null;
	}

	@Override
	public void removePartition(final Object key) {

		/*
		 * Attempt to remove the indexed population from the main storage
		 * container for all indexed populations.
		 */
		final PopulationPartition populationPartition = partitions.get(key);

		partitions.remove(key);

		/*
		 * Remove the indexed population from the various filter-related maps
		 * and set.
		 */
		final FilterInfo filterInfo = populationPartition.getFilterInfo();
		final Partition partition = populationPartition.getPartition();

		final Trigger trigger = new Trigger(filterInfo, context);

		final Set<CompartmentId> compartmentIds;
		if (partition.getCompartmentPartitionFunction().isPresent()) {
			compartmentIds = context.getScenario().getCompartmentIds();
		} else {
			compartmentIds = trigger.getCompartmentIdentifiers();
		}

		for (final CompartmentId compartmentId : compartmentIds) {
			final Set<PopulationPartition> set = compartmentSensitivePartitions.get(compartmentId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				compartmentSensitivePartitions.remove(compartmentId);
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
			final Set<PopulationPartition> set = regionSensitivePartitions.get(regionId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				regionSensitivePartitions.remove(regionId);
			}
		}

		final Set<ResourceId> resourceIds = new LinkedHashSet<>(partition.getPersonResourceIds());
		resourceIds.addAll(trigger.getResourceIdentifiers());
		for (final ResourceId resourceId : resourceIds) {

			Set<PopulationPartition> set = resourceSensitivePartitions.get(resourceId);
			if (set == null) {
				set = new LinkedHashSet<>();
				resourceSensitivePartitions.put(resourceId, set);
			}
			set.add(populationPartition);

		}

		for (final ResourceId resourceId : resourceIds) {
			final Set<PopulationPartition> indexedPopulations = resourceSensitivePartitions.get(resourceId);
			indexedPopulations.remove(populationPartition);
		}

		// The trigger's value-sensitive and value-insensitive property ids are
		// expected
		// to be disjoint. However, the partition may demand evaluation for a
		// particular
		// property id, and thus for all values of that property, overriding the
		// expectations of the trigger. We blend those expectations, favoring
		// value-insensitive re-evaluations.

		final Set<PersonPropertyId> valueInsensitivePropertyIdentifiers = trigger.getValueInsensitivePropertyIdentifiers();
		valueInsensitivePropertyIdentifiers.addAll(partition.getPersonPropertyIds());

		final Set<PersonPropertyId> valueSensitivePropertyIdentifiers = trigger.getValueSensitivePropertyIdentifiers();
		valueSensitivePropertyIdentifiers.removeAll(valueInsensitivePropertyIdentifiers);

		for (final PersonPropertyId personPropertyId : valueInsensitivePropertyIdentifiers) {
			final Set<PopulationPartition> set = propertyIdSensitivePartitions.get(personPropertyId);
			set.remove(populationPartition);
			if (set.size() == 0) {
				propertyIdSensitivePartitions.remove(personPropertyId);
			}
		}

		for (final PersonPropertyId personPropertyId : valueSensitivePropertyIdentifiers) {
			final Map<Object, Set<PopulationPartition>> map = propertyValueSensitivePartitions.get(personPropertyId);
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
				propertyValueSensitivePartitions.remove(personPropertyId);
			}
		}

		if (partition.getGroupPartitionFunction().isPresent()) {
			groupSensitivePartitions.remove(populationPartition);
		}

		for (final GroupId groupId : trigger.getGroupIdentifiers()) {
			final Set<PopulationPartition> set = groupIdSensitivePartitions.get(groupId);
			if (set != null) {
				set.remove(populationPartition);
				if (set.size() == 0) {
					groupIdSensitivePartitions.remove(groupId);
				}
			}
		}

		for (final GroupTypeId groupTypeId : trigger.getGroupTypeIdentifiers()) {
			final Set<PopulationPartition> set = groupTypeSensitivePartitions.get(groupTypeId);
			if (set != null) {
				set.remove(populationPartition);
				if (set.size() == 0) {
					groupTypeSensitivePartitions.remove(groupTypeId);
				}
			}
		}

	}

	@Override
	public StochasticPersonSelection samplePartition(final Object key, final PartitionSampler partitionSampler) {
		return partitions.get(key).samplePartition(partitionSampler);
	}

	@Override
	public boolean validateLabelSet(final Object key, final LabelSet labelSet) {
		return partitions.get(key).validateLabelSetInfo(labelSet);
	}

}