package gcm.simulation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.GroupInfo;
import gcm.output.reports.PersonInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.scenario.StageId;
import gcm.simulation.group.PersonGroupManger;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of ObservationManager
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class ObservationManagerImpl extends BaseElement implements ObservationManager {

	/*
	 * The threshold for rebuilding the observation queue when the queue is
	 * empty. Like most core java collections, a Deque does not reduce its
	 * memory footprint or shrink as it loses contained elements. Occasionally a
	 * modeler may commit some mutations that spawn millions of observations,
	 * leaving a very large memory allocation in the observation queue that
	 * holds no observations. When this happens, we rebuild the deque.
	 */
	private final static int OBSERVATION_QUEUE_REBUILD_THRESHOLD = 1000;

	private ComponentManager componentManager;

	private PersonGroupManger personGroupManger;

	private PersonLocationManger personLocationManger;

	/*
	 * Various collections that are tailored to fit the observation constraint
	 * patterns in the observe???() methods.
	 * 
	 */

	private final Map<GlobalPropertyId, Set<ComponentId>> globalPropertyChangeObservers = new LinkedHashMap<>();

	private final Map<PersonId, Map<PersonPropertyId, Set<ComponentId>>> individualPersonPropertyChangeObservers = new LinkedHashMap<>();

	private final Map<CompartmentId, Map<PersonPropertyId, Set<ComponentId>>> compartmentPersonPropertyChangeObservers = new LinkedHashMap<>();

	private final Map<RegionId, Map<PersonPropertyId, Set<ComponentId>>> regionPersonPropertyChangeObservers = new LinkedHashMap<>();

	private final Map<PersonPropertyId, Set<ComponentId>> globalPersonPropertyChangeObservers = new LinkedHashMap<>();

	private final Map<PersonId, Map<ResourceId, Set<ComponentId>>> individualPersonResourceObservers = new LinkedHashMap<>();

	private final Map<RegionId, Map<ResourceId, Set<ComponentId>>> regionPersonResourceObservers = new LinkedHashMap<>();

	private final Map<CompartmentId, Map<ResourceId, Set<ComponentId>>> compartmentPersonResourceObservers = new LinkedHashMap<>();

	private final Map<ResourceId, Set<ComponentId>> globalPersonResourceObservers = new LinkedHashMap<>();

	private final Map<PersonId, Set<ComponentId>> individualPersonRegionChangeObservers = new LinkedHashMap<>();

	private final Map<PersonId, Set<ComponentId>> individualPersonCompartmentChangeObservers = new LinkedHashMap<>();

	private final Map<CompartmentId, Set<ComponentId>> compartmentArrivalObservers = new LinkedHashMap<>();

	private final Map<CompartmentId, Set<ComponentId>> compartmentDepartureObservers = new LinkedHashMap<>();

	private final Map<RegionId, Set<ComponentId>> regionArrivalObservers = new LinkedHashMap<>();

	private final Map<RegionId, Set<ComponentId>> regionDepartureObservers = new LinkedHashMap<>();

	private final Set<ComponentId> globalArrivalObservers = new LinkedHashSet<>();

	private final Set<ComponentId> globalDepartureObservers = new LinkedHashSet<>();

	private final Map<CompartmentId, Map<CompartmentPropertyId, Set<ComponentId>>> compartmentPropertyObservers = new LinkedHashMap<>();

	private final Map<RegionId, Map<RegionPropertyId, Set<ComponentId>>> regionPropertyObservers = new LinkedHashMap<>();
	
	private final Map<RegionPropertyId, Set<ComponentId>> globalRegionPropertyObservers = new LinkedHashMap<>();

	private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Set<ComponentId>>> materialsProducerPropertyObservers = new LinkedHashMap<>();

	private final Map<ResourceId, Map<ResourcePropertyId, Set<ComponentId>>> resourcePropertyObservers = new LinkedHashMap<>();

	private final Map<RegionId, Map<ResourceId, Set<ComponentId>>> regionResourceObservers = new LinkedHashMap<>();

	private final Map<MaterialsProducerId, Map<ResourceId, Set<ComponentId>>> individualMaterialsProducersResourceObservers = new LinkedHashMap<>();

	private final Map<ResourceId, Set<ComponentId>> materialsProducersResourceObservers = new LinkedHashMap<>();

	private final Map<StageId, Set<ComponentId>> individualStageOfferObservers = new LinkedHashMap<>();

	private final Set<ComponentId> stageOfferObservers = new LinkedHashSet<>();

	private final Map<StageId, Set<ComponentId>> individualStageTransferObservers = new LinkedHashMap<>();
	
	
	private final Map<Object, Set<ComponentId>> partitionObservers = new LinkedHashMap<>();

	private final Set<ComponentId> stageTransferObservers = new LinkedHashSet<>();
	
	private final Map<MaterialsProducerId,Set<ComponentId>> sourceStageTransferObservers = new LinkedHashMap<>();
	
	private final Map<MaterialsProducerId,Set<ComponentId>> destinationStageTransferObservers = new LinkedHashMap<>();

	/*
	 * As mutations are executed, the components that registered for passive
	 * observation of those mutations must be alerted after the current
	 * component completes its activities. These alerts are stored as
	 * ObservationRecords in a queue that is flushed immediately (by invocation
	 * of observation methods on the observing components) after the active
	 * component completes its actions.
	 */
	private Deque<ObservationRecord> observationQueue = new ArrayDeque<>();

	/*
	 * The max number of elements that have been stored in the observationQueue
	 * during its life span. If this number is very large relative to the
	 * current size of the queue, then it may be warranted to rebuild the queue
	 * to recover memory.
	 */
	private int observationQueueCapacity;

	private final Set<ComponentId> groupArrivalObservers = new LinkedHashSet<>();

	private final Map<GroupId, Set<ComponentId>> groupArrivalObserversByGroup = new LinkedHashMap<>();

	private final Map<GroupId, Map<PersonId, Set<ComponentId>>> groupArrivalObserversByGroupAndPerson = new LinkedHashMap<>();

	private final Map<PersonId, Set<ComponentId>> groupArrivalObserversByPerson = new LinkedHashMap<>();

	private final Map<GroupTypeId, Set<ComponentId>> groupArrivalObserversByType = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<PersonId, Set<ComponentId>>> groupArrivalObserversByTypeAndPerson = new LinkedHashMap<>();

	private final Set<ComponentId> groupConstructionObservers = new LinkedHashSet<>();

	private final Map<GroupTypeId, Set<ComponentId>> groupConstructionObserversByType = new LinkedHashMap<>();

	private final Set<ComponentId> groupDepartureObservers = new LinkedHashSet<>();

	private final Map<GroupId, Set<ComponentId>> groupDepartureObserversByGroup = new LinkedHashMap<>();

	private final Map<GroupId, Map<PersonId, Set<ComponentId>>> groupDepartureObserversByGroupAndPerson = new LinkedHashMap<>();

	private final Map<PersonId, Set<ComponentId>> groupDepartureObserversByPerson = new LinkedHashMap<>();
	
	private final Map<GroupTypeId, Set<ComponentId>> groupDepartureObserversByType = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<PersonId, Set<ComponentId>>> groupDepartureObserversByTypeAndPerson = new LinkedHashMap<>();

	private final Set<ComponentId> groupDestructionObservers = new LinkedHashSet<>();

	private final Map<GroupId, Set<ComponentId>> groupDestructionObserversByGroup = new LinkedHashMap<>();

	private final Map<GroupTypeId, Set<ComponentId>> groupDestructionObserversByType = new LinkedHashMap<>();

	private final Set<ComponentId> groupPropertyChangeObservers = new LinkedHashSet<>();

	private final Map<GroupId, Set<ComponentId>> groupPropertyChangeObserversByGroup = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>>> groupPropertyChangeObserversByPropertyAndGroup = new LinkedHashMap<>();

	private final Map<GroupTypeId, Set<ComponentId>> groupPropertyChangeObserversByType = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<GroupPropertyId, Set<ComponentId>>> groupPropertyChangeObserversByTypeAndProperty = new LinkedHashMap<>();

	/*
	 * For each change that occurs in GCM, we may have zero to many registered
	 * observers for that change.
	 *
	 * This is a delaying technique so that we are not invoking an observeXXX()
	 * method on one component whilst another component is active. We store
	 * instead the arguments that would have been in the observeXXX() method in
	 * an ObservationRecord, with the first element of that ObservationRecord
	 * being the ObservationType that indicates which method we should call
	 * later.
	 *
	 * It seems odd to stimulate a component with observations of changes that
	 * the component committed on the Environment, so we exclude the current
	 * focal component from receiving the observation.
	 */
	private void addToObservationQueue(final Set<ComponentId> observers, final Object... arguments) {
		final ComponentId focalComponentId = componentManager.getFocalComponentId();
		for (final ComponentId observer : observers) {
			/*
			 * Make sure that we do not have the mutating component be an
			 * observer of its own mutation
			 */
			if (!focalComponentId.equals(observer)) {
				final ObservationRecord observationRecord = new ObservationRecord(observer, arguments);
				observationQueueCapacity++;
				observationQueue.add(observationRecord);
			}
		}
	}

	@Override
	public ObservationRecord getNextObservation() {
		/*
		 * If the observationQueue is empty and its current capacity(max number
		 * of items it has held in the past) exceeds the
		 * OBSERVATION_QUEUE_REBUILD_THRESHOLD then we rebuild the
		 * observationQueue to release unused memory bound up in the queue.
		 */
		if (observationQueue.isEmpty()) {
			if (observationQueueCapacity > OBSERVATION_QUEUE_REBUILD_THRESHOLD) {
				observationQueueCapacity = 0;
				observationQueue = new ArrayDeque<>();
			}
			return null;
		}
		return observationQueue.pop();
	}

	@Override
	public void handleCompartmentPropertyChange(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, Set<ComponentId>> map = compartmentPropertyObservers.get(compartmentId);
		final Set<ComponentId> set = map.get(compartmentPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.COMPARTMENT_PROPERTY, compartmentId, compartmentPropertyId);
		}
	}

	@Override
	public void handleGlobalPropertyChange(final GlobalPropertyId globalPropertyId) {
		final Set<ComponentId> set = globalPropertyChangeObservers.get(globalPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.GLOBAL_PROPERTY, globalPropertyId);
		}
	}

	@Override
	public void handleGroupAddition(final GroupId groupId) {
		if (!groupConstructionObservers.isEmpty()) {
			addToObservationQueue(groupConstructionObservers, ObservationType.GROUP_CONSTRUCTION, groupId);
		}
		final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
		final Set<ComponentId> set = groupConstructionObserversByType.get(groupTypeId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.GROUP_CONSTRUCTION, groupId);
		}
	}

	@Override
	public void handleGroupPropertyChange(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		if (!groupPropertyChangeObservers.isEmpty()) {
			addToObservationQueue(groupPropertyChangeObservers, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId);
		}

		final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
		final Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> map = groupPropertyChangeObserversByPropertyAndGroup.get(groupTypeId);
		final Map<GroupId, Set<ComponentId>> map2 = map.get(groupPropertyId);
		Set<ComponentId> observers = map2.get(groupId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId);
		}

		final Map<GroupPropertyId, Set<ComponentId>> map3 = groupPropertyChangeObserversByTypeAndProperty.get(groupTypeId);
		observers = map3.get(groupPropertyId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId);
		}

		observers = groupPropertyChangeObserversByGroup.get(groupId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId);
		}

		observers = groupPropertyChangeObserversByType.get(groupTypeId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId);
		}
	}

	@Override
	public void handleGroupRemovalByGroupInfo(final GroupInfo groupInfo) {
		groupArrivalObserversByGroup.remove(groupInfo.getGroupId());
		groupDepartureObserversByGroup.remove(groupInfo.getGroupId());
		groupPropertyChangeObserversByGroup.remove(groupInfo.getGroupId());
		groupArrivalObserversByGroupAndPerson.remove(groupInfo.getGroupId());
		groupDepartureObserversByGroupAndPerson.remove(groupInfo.getGroupId());

		Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> map = groupPropertyChangeObserversByPropertyAndGroup.get(groupInfo.getGroupTypeId());
		for (Map<GroupId, Set<ComponentId>> subMap : map.values()) {
			subMap.remove(groupInfo.getGroupId());
		}

		if (!groupDestructionObservers.isEmpty()) {
			addToObservationQueue(groupDestructionObservers, ObservationType.GROUP_DESTRUCTION, groupInfo);
		}

		Set<ComponentId> observers = groupDestructionObserversByGroup.get(groupInfo.getGroupId());
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_DESTRUCTION, groupInfo);
		}

		observers = groupDestructionObserversByType.get(groupInfo.getGroupTypeId());
		if (!observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_DESTRUCTION, groupInfo);
		}
	}

	@Override
	public void handleGroupRemoval(final GroupId groupId) {
		groupArrivalObserversByGroup.remove(groupId);
		groupDepartureObserversByGroup.remove(groupId);
		groupPropertyChangeObserversByGroup.remove(groupId);
		groupArrivalObserversByGroupAndPerson.remove(groupId);
		groupDepartureObserversByGroupAndPerson.remove(groupId);

		for (Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> map : groupPropertyChangeObserversByPropertyAndGroup.values()) {
			for (Map<GroupId, Set<ComponentId>> subMap : map.values()) {
				subMap.remove(groupId);
			}
		}
	}

	@Override
	public void handleMaterialsProducerPropertyChange(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		final Map<MaterialsProducerPropertyId, Set<ComponentId>> map = materialsProducerPropertyObservers.get(materialsProducerId);
		final Set<ComponentId> set = map.get(materialsProducerPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.MATERIALS_PRODUCER_PROPERTY, materialsProducerId, materialsProducerPropertyId);
		}
	}

	@Override
	public void handleMaterialsProducerResourceChange(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		Set<ComponentId> set = materialsProducersResourceObservers.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.MATERIALS_PRODUCER_RESOURCE, materialsProducerId, resourceId);
		}
		final Map<ResourceId, Set<ComponentId>> map = individualMaterialsProducersResourceObservers.get(materialsProducerId);
		set = map.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.MATERIALS_PRODUCER_RESOURCE, materialsProducerId, resourceId);
		}
	}

	@Override
	public void handlePersonAddition(final PersonId personId) {

		final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
		Set<ComponentId> set = compartmentArrivalObservers.get(compartmentId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.COMPARTMENT_PERSON_ARRIVAL, personId);
		}

		final RegionId regionId = personLocationManger.getPersonRegion(personId);
		set = regionArrivalObservers.get(regionId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_PERSON_ARRIVAL, personId);
		}

		if (!globalArrivalObservers.isEmpty()) {
			addToObservationQueue(globalArrivalObservers, ObservationType.GLOBAL_PERSON_ARRIVAL, personId);
		}

	}

	@Override
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId, final CompartmentId newCompartmentId) {

		Set<ComponentId> set = individualPersonCompartmentChangeObservers.get(personId);
		if (set != null) {
			if (!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.PERSON_COMPARTMENT, personId);
			}
		}

		set = compartmentArrivalObservers.get(newCompartmentId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.COMPARTMENT_PERSON_ARRIVAL, personId);
		}
		set = compartmentDepartureObservers.get(oldCompartmentId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.COMPARTMENT_PERSON_DEPARTURE, oldCompartmentId, personId);
		}
	}

	@Override
	public void handlePersonGroupAddition(final GroupId groupId, final PersonId personId) {

		if (!groupArrivalObservers.isEmpty()) {
			addToObservationQueue(groupArrivalObservers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
		}

		Set<ComponentId> observers = groupArrivalObserversByGroup.get(groupId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
		}

		observers = groupArrivalObserversByPerson.get(personId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
		}

		final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
		observers = groupArrivalObserversByType.get(groupTypeId);
		if (!observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
		}

		Map<PersonId, Set<ComponentId>> map = groupArrivalObserversByTypeAndPerson.get(groupTypeId);
		observers = map.get(personId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
		}

		map = groupArrivalObserversByGroupAndPerson.get(groupId);
		if (map != null) {
			observers = map.get(personId);
			if ((observers != null) && !observers.isEmpty()) {
				addToObservationQueue(observers, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId);
			}
		}
	}

	@Override
	public void handlePersonGroupRemoval(final GroupId groupId, final PersonId personId) {
		if (!groupDepartureObservers.isEmpty()) {
			addToObservationQueue(groupDepartureObservers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
		}

		Set<ComponentId> observers = groupDepartureObserversByGroup.get(groupId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
		}

		observers = groupDepartureObserversByPerson.get(personId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
		}

		final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
		observers = groupDepartureObserversByType.get(groupTypeId);
		if (!observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
		}

		Map<PersonId, Set<ComponentId>> map = groupDepartureObserversByTypeAndPerson.get(groupTypeId);
		observers = map.get(personId);
		if ((observers != null) && !observers.isEmpty()) {
			addToObservationQueue(observers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
		}

		map = groupDepartureObserversByGroupAndPerson.get(groupId);
		if (map != null) {
			observers = map.get(personId);
			if ((observers != null) && !observers.isEmpty()) {
				addToObservationQueue(observers, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId);
			}
		}
	}

	@Override
	public void handlePersonPropertyChange(final PersonId personId, final PersonPropertyId personPropertyId) {

		Map<PersonPropertyId, Set<ComponentId>> map = individualPersonPropertyChangeObservers.get(personId);
		if (map != null) {
			final Set<ComponentId> set = map.get(personPropertyId);
			if (set != null) {
				if (!set.isEmpty()) {
					addToObservationQueue(set, ObservationType.PERSON_PROPERTY, personId, personPropertyId);
				}
			}
		}

		Set<ComponentId> set = globalPersonPropertyChangeObservers.get(personPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_PROPERTY, personId, personPropertyId);
		}

		map = regionPersonPropertyChangeObservers.get(personLocationManger.getPersonRegion(personId));
		set = map.get(personPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_PROPERTY, personId, personPropertyId);
		}

		map = compartmentPersonPropertyChangeObservers.get(personLocationManger.getPersonCompartment(personId));
		set = map.get(personPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_PROPERTY, personId, personPropertyId);
		}
	}

	@Override
	public void handlePersonRegionChange(final PersonId personId, final RegionId oldRegionId, final RegionId newRegionId) {
		Set<ComponentId> set = individualPersonRegionChangeObservers.get(personId);
		if (set != null) {
			if (!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.PERSON_REGION, personId);
			}
		}

		set = regionArrivalObservers.get(newRegionId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_PERSON_ARRIVAL, personId);
		}
		set = regionDepartureObservers.get(oldRegionId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_PERSON_DEPARTURE, oldRegionId, personId);
		}

	}

	@Override
	public void handlePersonRemovalByPersonInfo(final PersonInfo personInfo) {
		individualPersonCompartmentChangeObservers.remove(personInfo.getPersonId());
		individualPersonPropertyChangeObservers.remove(personInfo.getPersonId());
		individualPersonRegionChangeObservers.remove(personInfo.getPersonId());
		individualPersonResourceObservers.remove(personInfo.getPersonId());

		if (!globalDepartureObservers.isEmpty()) {
			addToObservationQueue(globalDepartureObservers, ObservationType.GLOBAL_PERSON_DEPARTURE, personInfo.getPersonId());
		}
	}
	
	
	@Override
	public void handlePartitionRemoval(final Object key) {
		partitionObservers.remove(key);
	}
	

	@Override
	public void handlePersonRemoval(final PersonId personId) {
		individualPersonCompartmentChangeObservers.remove(personId);
		individualPersonPropertyChangeObservers.remove(personId);
		individualPersonRegionChangeObservers.remove(personId);
		individualPersonResourceObservers.remove(personId);
	}

	@Override
	public void handlePersonResourceChange(final PersonId personId, final ResourceId resourceId) {

		Map<ResourceId, Set<ComponentId>> map = individualPersonResourceObservers.get(personId);
		if (map != null) {
			final Set<ComponentId> set = map.get(resourceId);
			if (set != null) {
				if (!set.isEmpty()) {
					addToObservationQueue(set, ObservationType.PERSON_RESOURCE, personId, resourceId);
				}
			}
		}

		map = regionPersonResourceObservers.get(personLocationManger.getPersonRegion(personId));
		Set<ComponentId> set = map.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_RESOURCE, personId, resourceId);
		}

		map = compartmentPersonResourceObservers.get(personLocationManger.getPersonCompartment(personId));
		set = map.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_RESOURCE, personId, resourceId);
		}

		set = globalPersonResourceObservers.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PERSON_RESOURCE, personId, resourceId);
		}
	}

	@Override
	public void handleRegionPropertyChange(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		final Map<RegionPropertyId, Set<ComponentId>> map = regionPropertyObservers.get(regionId);
		Set<ComponentId> set = map.get(regionPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_PROPERTY, regionId, regionPropertyId);
		}
		
		set = globalRegionPropertyObservers.get(regionPropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_PROPERTY, regionId, regionPropertyId);
		}
	}

	@Override
	public void handleRegionResourceChange(final RegionId regionId, final ResourceId resourceId) {
		final Map<ResourceId, Set<ComponentId>> map = regionResourceObservers.get(regionId);
		final Set<ComponentId> set = map.get(resourceId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.REGION_RESOURCE, regionId, resourceId);
		}
	}

	@Override
	public void handleResourcePropertyChange(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, Set<ComponentId>> map = resourcePropertyObservers.get(resourceId);
		final Set<ComponentId> set = map.get(resourcePropertyId);
		if (!set.isEmpty()) {
			addToObservationQueue(set, ObservationType.RESOURCE_PROPERTY, resourceId, resourcePropertyId);
		}
	}

	@Override
	public void handleStageDestruction(final StageId stageId) {
		individualStageOfferObservers.remove(stageId);
	}

	@Override
	public void handleStageOfferChange(final StageId stageId) {
		if (!stageOfferObservers.isEmpty()) {
			addToObservationQueue(stageOfferObservers, ObservationType.STAGE_OFFER, stageId);
		}
		final Set<ComponentId> set = individualStageOfferObservers.get(stageId);
		if (set != null) {
			if (!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.STAGE_OFFER, stageId);
			}
		}
	}

	@Override
	public void handleStageTransfer(final StageId stageId,MaterialsProducerId sourceMaterialsProducerId, MaterialsProducerId destinationMaterialsProducerId) {
		if (!stageTransferObservers.isEmpty()) {
			addToObservationQueue(stageTransferObservers, ObservationType.STAGE_TRANSFER, stageId,sourceMaterialsProducerId,destinationMaterialsProducerId);
		}
		Set<ComponentId> set = individualStageTransferObservers.get(stageId);
		if (set != null) {
			if (!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.STAGE_TRANSFER, stageId,sourceMaterialsProducerId,destinationMaterialsProducerId);
			}
		}
		
		set = sourceStageTransferObservers.get(sourceMaterialsProducerId);
		if(set != null) {
			if(!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.STAGE_TRANSFER, stageId,sourceMaterialsProducerId,destinationMaterialsProducerId);
			}
		}
		
		set = destinationStageTransferObservers.get(destinationMaterialsProducerId);
		if(set != null) {
			if(!set.isEmpty()) {
				addToObservationQueue(set, ObservationType.STAGE_TRANSFER, stageId,sourceMaterialsProducerId,destinationMaterialsProducerId);
			}
		}		
	}

	@Override
	public boolean requiresGroupInfoForGroupRemoval(GroupId groupId) {
		if (groupDestructionObservers.size() > 0) {
			return true;
		}

		Set<ComponentId> observers = groupDestructionObserversByGroup.get(groupId);
		if (observers != null && observers.size() > 0) {
			return true;
		}

		GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);

		observers = groupDestructionObserversByType.get(groupTypeId);
		if (observers != null && observers.size() > 0) {
			return true;
		}

		return false;
	}

	@Override
	public boolean requiresPersonInfoForPersonRemoval() {
		return !globalDepartureObservers.isEmpty();
	}

	@Override
	public void init(final Context context) {
		super.init(context);

		final Scenario scenario = context.getScenario();
		personGroupManger = context.getPersonGroupManger();

		componentManager = context.getComponentManager();
		personLocationManger = context.getPersonLocationManger();

		/*
		 * We initialize all collections that have a fixed set of keys. Stages
		 * and people are not fixed, so we do not pre-fill those observer
		 * collections.
		 */

		for (final GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
			globalPropertyChangeObservers.put(globalPropertyId, new LinkedHashSet<>());
		}
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			compartmentArrivalObservers.put(compartmentId, new LinkedHashSet<>());
		}
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			compartmentDepartureObservers.put(compartmentId, new LinkedHashSet<>());
		}
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			final Map<PersonPropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			compartmentPersonPropertyChangeObservers.put(compartmentId, map);
			for (final PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
				map.put(personPropertyId, new LinkedHashSet<>());
			}
		}

		for (final RegionId regionID : scenario.getRegionIds()) {
			final Map<PersonPropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			regionPersonPropertyChangeObservers.put(regionID, map);
			for (final PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
				map.put(personPropertyId, new LinkedHashSet<>());
			}
		}
		for (final RegionId regionID : scenario.getRegionIds()) {
			regionArrivalObservers.put(regionID, new LinkedHashSet<>());
		}
		for (final RegionId regionID : scenario.getRegionIds()) {
			regionDepartureObservers.put(regionID, new LinkedHashSet<>());
		}
		for (final PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
			globalPersonPropertyChangeObservers.put(personPropertyId, new LinkedHashSet<>());
		}

		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			final Map<CompartmentPropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			compartmentPropertyObservers.put(compartmentId, map);
			for (final CompartmentPropertyId compartmentPropertyId : scenario.getCompartmentPropertyIds(compartmentId)) {
				map.put(compartmentPropertyId, new LinkedHashSet<>());
			}
		}
		for (final RegionId regionId : scenario.getRegionIds()) {
			final Map<RegionPropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			regionPropertyObservers.put(regionId, map);
			for (final RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
				map.put(regionPropertyId, new LinkedHashSet<>());
			}
		}
		for (final RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
			globalRegionPropertyObservers.put(regionPropertyId, new LinkedHashSet<>());
		}
		
		for (final MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			final Map<MaterialsProducerPropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			materialsProducerPropertyObservers.put(materialsProducerId, map);
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
				map.put(materialsProducerPropertyId, new LinkedHashSet<>());
			}
		}

		for (final RegionId regionId : scenario.getRegionIds()) {
			final Map<ResourceId, Set<ComponentId>> map = new LinkedHashMap<>();
			regionPersonResourceObservers.put(regionId, map);
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new LinkedHashSet<>());
			}
		}

		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			final Map<ResourceId, Set<ComponentId>> map = new LinkedHashMap<>();
			compartmentPersonResourceObservers.put(compartmentId, map);
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new LinkedHashSet<>());
			}
		}

		for (final ResourceId resourceId : scenario.getResourceIds()) {
			globalPersonResourceObservers.put(resourceId, new LinkedHashSet<>());
		}

		for (final ResourceId resourceId : scenario.getResourceIds()) {
			final Map<ResourcePropertyId, Set<ComponentId>> map = new LinkedHashMap<>();
			resourcePropertyObservers.put(resourceId, map);
			for (final ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
				map.put(resourcePropertyId, new LinkedHashSet<>());
			}
		}

		for (final RegionId regionId : scenario.getRegionIds()) {
			final Map<ResourceId, Set<ComponentId>> map = new LinkedHashMap<>();
			regionResourceObservers.put(regionId, map);
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new LinkedHashSet<>());
			}
		}

		for (final MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			final Map<ResourceId, Set<ComponentId>> map = new LinkedHashMap<>();
			individualMaterialsProducersResourceObservers.put(materialsProducerId, map);
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new LinkedHashSet<>());
			}
		}
		for (final ResourceId resourceId : scenario.getResourceIds()) {
			materialsProducersResourceObservers.put(resourceId, new LinkedHashSet<>());
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> propertyMap = new LinkedHashMap<>();
			groupPropertyChangeObserversByPropertyAndGroup.put(groupTypeId, propertyMap);
			for (final GroupPropertyId groupPropertyId : scenario.getGroupPropertyIds(groupTypeId)) {
				final Map<GroupId, Set<ComponentId>> groupMap = new LinkedHashMap<>();
				propertyMap.put(groupPropertyId, groupMap);
			}
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Map<GroupPropertyId, Set<ComponentId>> propertyMap = new LinkedHashMap<>();
			groupPropertyChangeObserversByTypeAndProperty.put(groupTypeId, propertyMap);
			for (final GroupPropertyId groupPropertyId : scenario.getGroupPropertyIds(groupTypeId)) {
				final Set<ComponentId> set = new LinkedHashSet<>();
				propertyMap.put(groupPropertyId, set);
			}
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Map<PersonId, Set<ComponentId>> map = new LinkedHashMap<>();
			groupArrivalObserversByTypeAndPerson.put(groupTypeId, map);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Map<PersonId, Set<ComponentId>> map = new LinkedHashMap<>();
			groupDepartureObserversByTypeAndPerson.put(groupTypeId, map);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Set<ComponentId> set = new LinkedHashSet<>();
			groupArrivalObserversByType.put(groupTypeId, set);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Set<ComponentId> set = new LinkedHashSet<>();
			groupConstructionObserversByType.put(groupTypeId, set);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Set<ComponentId> set = new LinkedHashSet<>();
			groupDepartureObserversByType.put(groupTypeId, set);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Set<ComponentId> set = new LinkedHashSet<>();
			groupDestructionObserversByType.put(groupTypeId, set);
		}

		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			final Set<ComponentId> set = new LinkedHashSet<>();
			groupPropertyChangeObserversByType.put(groupTypeId, set);
		}
	}

	@Override
	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId, final PersonPropertyId personPropertyId) {
		final Map<PersonPropertyId, Set<ComponentId>> map = compartmentPersonPropertyChangeObservers.get(compartmentId);
		final Set<ComponentId> set = map.get(personPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId, final ResourceId resourceId) {
		final Map<ResourceId, Set<ComponentId>> map = compartmentPersonResourceObservers.get(compartmentId);
		final Set<ComponentId> set = map.get(resourceId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeCompartmentPersonArrivals(final boolean observe, final CompartmentId compartmentId) {
		if (observe) {
			final Set<ComponentId> set = compartmentArrivalObservers.get(compartmentId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = compartmentArrivalObservers.get(compartmentId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of departures of people in the given
	 * compartment for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 */
	@Override
	public void observeCompartmentPersonDepartures(final boolean observe, final CompartmentId compartmentId) {
		if (observe) {
			final Set<ComponentId> set = compartmentDepartureObservers.get(compartmentId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = compartmentDepartureObservers.get(compartmentId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the given compartment
	 * property and compartment for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, Set<ComponentId>> map = compartmentPropertyObservers.get(compartmentId);
		final Set<ComponentId> set = map.get(compartmentPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of the addition of people to the
	 * simulation for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	@Override
	public void observeGlobalPersonArrivals(final boolean observe) {
		if (observe) {
			globalArrivalObservers.add(componentManager.getFocalComponentId());

		} else {
			globalArrivalObservers.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of removal of people from the simulation
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	@Override
	public void observeGlobalPersonDepartures(final boolean observe) {
		if (observe) {
			globalDepartureObservers.add(componentManager.getFocalComponentId());

		} else {
			globalDepartureObservers.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of person property changes for the given
	 * property without regard to the location of the person for the current
	 * focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId) {
		final Set<ComponentId> set = globalPersonPropertyChangeObservers.get(personPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of person resource changes for the given
	 * property without regard to the location of the person for the current
	 * focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 *            cannot be null
	 */
	@Override
	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId) {
		final Set<ComponentId> set = globalPersonResourceObservers.get(resourceId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of global property changes for the given
	 * global property id for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId) {
		if (observe) {
			globalPropertyChangeObservers.get(globalPropertyId).add(componentManager.getFocalComponentId());
		} else {
			globalPropertyChangeObservers.get(globalPropertyId).remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupArrival(final boolean observe) {
		if (observe) {
			groupArrivalObservers.add(componentManager.getFocalComponentId());
		} else {
			groupArrivalObservers.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId) {
		if (observe) {
			Set<ComponentId> set = groupArrivalObserversByGroup.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupArrivalObserversByGroup.put(groupId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupArrivalObserversByGroup.get(groupId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId) {
		if (observe) {
			Map<PersonId, Set<ComponentId>> map = groupArrivalObserversByGroupAndPerson.get(groupId);
			if (map == null) {
				map = new LinkedHashMap<>();
				groupArrivalObserversByGroupAndPerson.put(groupId, map);
			}
			Set<ComponentId> set = map.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<PersonId, Set<ComponentId>> map = groupArrivalObserversByGroupAndPerson.get(groupId);
			if (map != null) {
				final Set<ComponentId> set = map.get(personId);
				if (set != null) {
					set.remove(componentManager.getFocalComponentId());
				}
			}
		}
	}

	@Override
	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId) {
		if (observe) {
			Set<ComponentId> set = groupArrivalObserversByPerson.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupArrivalObserversByPerson.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupArrivalObserversByPerson.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId) {
		if (observe) {
			final Set<ComponentId> set = groupArrivalObserversByType.get(groupTypeId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupArrivalObserversByType.get(groupTypeId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId) {
		if (observe) {
			final Map<PersonId, Set<ComponentId>> map = groupArrivalObserversByTypeAndPerson.get(groupTypeId);
			Set<ComponentId> set = map.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<PersonId, Set<ComponentId>> map = groupArrivalObserversByTypeAndPerson.get(groupTypeId);
			final Set<ComponentId> set = map.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupConstruction(final boolean observe) {
		if (observe) {
			groupConstructionObservers.add(componentManager.getFocalComponentId());
		} else {
			groupConstructionObservers.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		if (observe) {
			final Set<ComponentId> set = groupConstructionObserversByType.get(groupTypeId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupConstructionObserversByType.get(groupTypeId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupDeparture(final boolean observe) {
		if (observe) {
			groupDepartureObservers.add(componentManager.getFocalComponentId());
		} else {
			groupDepartureObservers.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId) {
		if (observe) {
			Set<ComponentId> set = groupDepartureObserversByGroup.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupDepartureObserversByGroup.put(groupId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupDepartureObserversByGroup.get(groupId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId) {
		if (observe) {
			Map<PersonId, Set<ComponentId>> map = groupDepartureObserversByGroupAndPerson.get(groupId);
			if (map == null) {
				map = new LinkedHashMap<>();
				groupDepartureObserversByGroupAndPerson.put(groupId, map);
			}
			Set<ComponentId> set = map.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<PersonId, Set<ComponentId>> map = groupDepartureObserversByGroupAndPerson.get(groupId);
			if (map != null) {
				final Set<ComponentId> set = map.get(personId);
				if (set != null) {
					set.remove(componentManager.getFocalComponentId());
				}
			}
		}
	}

	@Override
	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId) {
		if (observe) {
			Set<ComponentId> set = groupDepartureObserversByPerson.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupDepartureObserversByPerson.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupDepartureObserversByPerson.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId) {
		if (observe) {
			final Set<ComponentId> set = groupDepartureObserversByType.get(groupTypeId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupDepartureObserversByType.get(groupTypeId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId) {
		if (observe) {
			final Map<PersonId, Set<ComponentId>> map = groupDepartureObserversByTypeAndPerson.get(groupTypeId);
			Set<ComponentId> set = map.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<PersonId, Set<ComponentId>> map = groupDepartureObserversByTypeAndPerson.get(groupTypeId);

			final Set<ComponentId> set = map.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupDestruction(final boolean observe) {
		if (observe) {
			groupDestructionObservers.add(componentManager.getFocalComponentId());
		} else {
			groupDestructionObservers.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupDestructionByGroup(final boolean observe, final GroupId groupId) {
		if (observe) {
			Set<ComponentId> set = groupDestructionObserversByGroup.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupDestructionObserversByGroup.put(groupId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupDestructionObserversByGroup.get(groupId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		if (observe) {
			final Set<ComponentId> set = groupDestructionObserversByType.get(groupTypeId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupDestructionObserversByType.get(groupTypeId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupPropertyChange(final boolean observe) {
		if (observe) {
			groupPropertyChangeObservers.add(componentManager.getFocalComponentId());
		} else {
			groupPropertyChangeObservers.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId) {
		if (observe) {
			Set<ComponentId> set = groupPropertyChangeObserversByGroup.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				groupPropertyChangeObserversByGroup.put(groupId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupPropertyChangeObserversByGroup.get(groupId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupPropertyId groupPropertyId, final GroupId groupId) {
		if (observe) {
			final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			final Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> map1 = groupPropertyChangeObserversByPropertyAndGroup.get(groupTypeId);
			final Map<GroupId, Set<ComponentId>> map2 = map1.get(groupPropertyId);
			Set<ComponentId> set = map2.get(groupId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map2.put(groupId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			final Map<GroupPropertyId, Map<GroupId, Set<ComponentId>>> map1 = groupPropertyChangeObserversByPropertyAndGroup.get(groupTypeId);
			final Map<GroupId, Set<ComponentId>> map2 = map1.get(groupPropertyId);
			final Set<ComponentId> set = map2.get(groupId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
			}
		}
	}

	@Override
	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId) {
		if (observe) {
			final Set<ComponentId> set = groupPropertyChangeObserversByType.get(groupTypeId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = groupPropertyChangeObserversByType.get(groupTypeId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	@Override
	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (observe) {
			final Map<GroupPropertyId, Set<ComponentId>> map = groupPropertyChangeObserversByTypeAndProperty.get(groupTypeId);
			final Set<ComponentId> set = map.get(groupPropertyId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<GroupPropertyId, Set<ComponentId>> map = groupPropertyChangeObserversByTypeAndProperty.get(groupTypeId);
			final Set<ComponentId> set = map.get(groupPropertyId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of the change of compartment for a
	 * specific person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 */
	@Override
	public void observeIndividualPersonCompartmentChange(final boolean observe, final PersonId personId) {
		if (observe) {

			Set<ComponentId> set = individualPersonCompartmentChangeObservers.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				individualPersonCompartmentChangeObservers.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {

			final Set<ComponentId> set = individualPersonCompartmentChangeObservers.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					individualPersonCompartmentChangeObservers.remove(personId);
				}
			}
		}
	}

	/**
	 * Starts or stops the observation of a change in the given property value
	 * for a specific person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeIndividualPersonPropertyChange(final boolean observe, final PersonId personId, final PersonPropertyId personPropertyId) {
		if (observe) {

			Map<PersonPropertyId, Set<ComponentId>> map = individualPersonPropertyChangeObservers.get(personId);
			if (map == null) {
				map = new LinkedHashMap<>();
				individualPersonPropertyChangeObservers.put(personId, map);
			}
			Set<ComponentId> set = map.get(personPropertyId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(personPropertyId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<PersonPropertyId, Set<ComponentId>> map = individualPersonPropertyChangeObservers.get(personId);
			if (map != null) {
				final Set<ComponentId> set = map.get(personPropertyId);
				if (set != null) {
					set.remove(componentManager.getFocalComponentId());
					if (set.isEmpty()) {
						map.remove(personPropertyId);
						if (map.isEmpty()) {
							individualPersonPropertyChangeObservers.remove(personId);
						}
					}
				}
			}
		}
	}

	/**
	 * Starts or stops the observation of the change of region for a specific
	 * person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 */
	@Override
	public void observeIndividualPersonRegionChange(final boolean observe, final PersonId personId) {
		if (observe) {
			Set<ComponentId> set = individualPersonRegionChangeObservers.get(personId);
			if (set == null) {
				set = new LinkedHashSet<>();
				individualPersonRegionChangeObservers.put(personId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {

			final Set<ComponentId> set = individualPersonRegionChangeObservers.get(personId);
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					individualPersonRegionChangeObservers.remove(personId);
				}
			}
		}
	}

	/**
	 * Starts or stops the observation of the change of resource value for a
	 * specific person and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	@Override
	public void observeIndividualPersonResourceChange(final boolean observe, final PersonId personId, final ResourceId resourceId) {
		if (observe) {

			Map<ResourceId, Set<ComponentId>> map = individualPersonResourceObservers.get(personId);
			if (map == null) {
				map = new LinkedHashMap<>();
				individualPersonResourceObservers.put(personId, map);
			}
			Set<ComponentId> set = map.get(resourceId);
			if (set == null) {
				set = new LinkedHashSet<>();
				map.put(resourceId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			final Map<ResourceId, Set<ComponentId>> map = individualPersonResourceObservers.get(personId);
			if (map != null) {
				final Set<ComponentId> set = map.get(resourceId);
				if (set != null) {
					set.remove(componentManager.getFocalComponentId());
					if (set.isEmpty()) {
						map.remove(resourceId);
						if (map.isEmpty()) {
							individualPersonResourceObservers.remove(personId);
						}
					}
				}
			}
		}
	}

	/**
	 * Starts or stops the observation of changes to a materials producer
	 * property for a specific materials producer for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param materialProducerId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeMaterialsProducerPropertyChange(final boolean observe, final MaterialsProducerId materialProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		final Map<MaterialsProducerPropertyId, Set<ComponentId>> map = materialsProducerPropertyObservers.get(materialProducerId);
		final Set<ComponentId> set = map.get(materialsProducerPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the resource level for a
	 * specific materials producer and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param materialProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	@Override
	public void observeMaterialsProducerResourceChangeByResourceId(final boolean observe, final MaterialsProducerId materialProducerId, final ResourceId resourceId) {
		final Map<ResourceId, Set<ComponentId>> map = individualMaterialsProducersResourceObservers.get(materialProducerId);
		final Set<ComponentId> set = map.get(resourceId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to a materials producer
	 * property for all materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 */
	@Override
	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId) {
		final Set<ComponentId> set = materialsProducersResourceObservers.get(resourceId);

		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of arrivals of people in the given region
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 */
	@Override
	public void observeRegionPersonArrivals(final boolean observe, final RegionId regionId) {
		if (observe) {
			final Set<ComponentId> set = regionArrivalObservers.get(regionId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = regionArrivalObservers.get(regionId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of departures of people in the given
	 * region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 */
	@Override
	public void observeRegionPersonDepartures(final boolean observe, final RegionId regionId) {
		if (observe) {
			final Set<ComponentId> set = regionDepartureObservers.get(regionId);
			set.add(componentManager.getFocalComponentId());
		} else {
			final Set<ComponentId> set = regionDepartureObservers.get(regionId);
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the person property value
	 * for any person located in the given region for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId, final PersonPropertyId personPropertyId) {
		final Map<PersonPropertyId, Set<ComponentId>> map = regionPersonPropertyChangeObservers.get(regionId);
		final Set<ComponentId> set = map.get(personPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the person resource value
	 * for any person located in the given region for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	@Override
	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId) {
		final Map<ResourceId, Set<ComponentId>> map = regionPersonResourceObservers.get(regionId);
		final Set<ComponentId> set = map.get(resourceId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the given region property
	 * and region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		final Map<RegionPropertyId, Set<ComponentId>> map = regionPropertyObservers.get(regionId);
		final Set<ComponentId> set = map.get(regionPropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}
	
	@Override
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId) {
		final Set<ComponentId> set = globalRegionPropertyObservers.get(regionPropertyId);		
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the resource level for the
	 * given region and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	@Override
	public void observeRegionResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId) {
		final Map<ResourceId, Set<ComponentId>> map = regionResourceObservers.get(regionId);
		final Set<ComponentId> set = map.get(resourceId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of changes to the given region property
	 * and region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	@Override
	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, Set<ComponentId>> map = resourcePropertyObservers.get(resourceId);
		final Set<ComponentId> set = map.get(resourcePropertyId);
		if (observe) {
			set.add(componentManager.getFocalComponentId());
		} else {
			set.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of the offer state for all stages for the
	 * current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	@Override
	public void observeStageOfferChange(final boolean observe) {
		if (observe) {
			stageOfferObservers.add(componentManager.getFocalComponentId());
		} else {
			stageOfferObservers.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of the offer state for the given stage
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param stageId
	 *            cannot be null
	 */
	@Override
	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId) {
		Set<ComponentId> set = individualStageOfferObservers.get(stageId);
		if (observe) {
			if (set == null) {
				set = new LinkedHashSet<>();
				individualStageOfferObservers.put(stageId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					individualStageOfferObservers.remove(stageId);
				}
			}
		}
	}

	/**
	 * Starts or stops the observation of the transfer of all stages between
	 * materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	@Override
	public void observeStageTransfer(final boolean observe) {
		if (observe) {
			stageTransferObservers.add(componentManager.getFocalComponentId());
		} else {
			stageTransferObservers.remove(componentManager.getFocalComponentId());
		}
	}

	/**
	 * Starts or stops the observation of the transfer of the given stage
	 * between materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param stageId
	 *            cannot be null
	 */
	@Override
	public void observeStageTransferByStageId(final boolean observe, final StageId stageId) {
		Set<ComponentId> set = individualStageTransferObservers.get(stageId);
		if (observe) {
			if (set == null) {
				set = new LinkedHashSet<>();
				individualStageTransferObservers.put(stageId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					individualStageTransferObservers.remove(stageId);
				}
			}
		}
	}

	
	
	@Override
	public void observePartitionChange(boolean observe, Object key) {
		Set<ComponentId> set = partitionObservers.get(key);
		if (observe) {
			if (set == null) {
				set = new LinkedHashSet<>();
				partitionObservers.put(key, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					partitionObservers.remove(key);
				}
			}
		}		
	}
	
	
	@Override
	public void handlePartitionPersonAddition(Object key, PersonId personId) {
		Set<ComponentId> set = partitionObservers.get(key);
		if(set != null && !set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PARTITION_PERSON_ADDITION, key,personId);
		}
	}

	@Override
	public void handlePartitionPersonRemoval(Object key, PersonId personId) {
		Set<ComponentId> set = partitionObservers.get(key);
		if(set != null && !set.isEmpty()) {
			addToObservationQueue(set, ObservationType.PARTITION_PERSON_REMOVAL, key,personId);
		}		
	}
	
	@Override
	public void observeStageTransferBySourceMaterialsProducerId(boolean observe, MaterialsProducerId sourceMaterialsProducerId) {
		Set<ComponentId> set = sourceStageTransferObservers.get(sourceMaterialsProducerId);
		if (observe) {
			if (set == null) {
				set = new LinkedHashSet<>();
				sourceStageTransferObservers.put(sourceMaterialsProducerId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					sourceStageTransferObservers.remove(sourceMaterialsProducerId);
				}
			}
		}		
	}

	@Override
	public void observeStageTransferByDestinationMaterialsProducerId(boolean observe, MaterialsProducerId destinationMaterialsProducerId) {
		Set<ComponentId> set = destinationStageTransferObservers.get(destinationMaterialsProducerId);
		if (observe) {
			if (set == null) {
				set = new LinkedHashSet<>();
				destinationStageTransferObservers.put(destinationMaterialsProducerId, set);
			}
			set.add(componentManager.getFocalComponentId());
		} else {
			if (set != null) {
				set.remove(componentManager.getFocalComponentId());
				if (set.isEmpty()) {
					destinationStageTransferObservers.remove(destinationMaterialsProducerId);
				}
			}
		}		
	}


}