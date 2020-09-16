package gcm.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gcm.components.Component;
import gcm.output.reports.BatchInfo;
import gcm.output.reports.BatchInfo.BatchInfoBuilder;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.GroupInfo.GroupInfoBuilder;
import gcm.output.reports.PersonInfo;
import gcm.output.reports.PersonInfo.PersonInfoBuilder;
import gcm.output.reports.StageInfo;
import gcm.output.reports.StageInfo.StageInfoBuilder;
import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MaterialId;
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
import gcm.simulation.index.IndexedPopulationManager;
import gcm.simulation.partition.PartitionManager;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.Partition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor for MutationResolver.
 * 
 * The general pattern for mutation methods:
 * 
 * <pre>
	public ReturnType mutateSomething(Arguments...) {
		//set up needed information
		....
		//acquire global read access lock -- reentrant access from a modeler contributed item will result in an exception.  
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			//work with the managers to mutate the data understanding that read access is turned off for modeler contributed items such as filters, components, etc.
			
		} finally {
			//force the release of the lock
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		//work with managers that will definitely require read access such as reports
		reportsManager.handleSomething()
		
		return someValue;
	}
 * </pre>
 * 
 */

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class MutationResolverImpl extends BaseElement implements MutationResolver {

	private IndexedPopulationManager indexedPopulationManager;
	private PartitionManager populationPartitionManager;
	private ObservationManager observationManager;
	private MaterialsManager materialsManager;
	private ReportsManager reportsManager;
	private ResourceManager resourceManager;
	private PropertyManager propertyManager;
	private PersonLocationManger personLocationManger;
	private PersonIdManager personIdManager;
	private PersonGroupManger personGroupManger;
	private EventManager eventManager;
	private ExternalAccessManager externalAccessManager;
	private ComponentManager componentManager;

	@Override
	public void init(Context context) {
		super.init(context);
		/*
		 * Establish all the convenience references
		 */
		externalAccessManager = context.getExternalAccessManager();

		propertyManager = context.getPropertyManager();
		observationManager = context.getObservationManager();
		reportsManager = context.getReportsManager();
		materialsManager = context.getMaterialsManager();
		personLocationManger = context.getPersonLocationManger();
		indexedPopulationManager = context.getIndexedPopulationManager();
		populationPartitionManager = context.getPartitionManager();
		resourceManager = context.getResourceManager();
		personGroupManger = context.getPersonGroupManger();
		eventManager = context.getEventManager();
		personIdManager = context.getPersonIdManager();
		componentManager = context.getComponentManager();
		Scenario scenario = context.getScenario();

		for (MaterialId materialId : scenario.getMaterialIds()) {
			materialToBatchPropertyIdsMap.put(materialId, scenario.getBatchPropertyIds(materialId));
		}
		for (GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			groupTypeToGroupPropertyIdsMap.put(groupTypeId, scenario.getGroupPropertyIds(groupTypeId));
		}
		personPropertyIds = scenario.getPersonPropertyIds();
		resourceIds = scenario.getResourceIds();

		/*
		 * Load the remaining data from the scenario that generally corresponds to
		 * mutations available to components so that reporting will properly reflect
		 * these data. The adding of resources directly to people and material producers
		 * is covered here but do not correspond to mutations allowed to components.
		 */

		loadGlobalPropertyValues(scenario);
		loadRegionPropertyValues(scenario);
		loadMaterialsProducerPropertyValues(scenario);
		loadCompartmentPropertyValues(scenario);
		loadResourcePropertyValues(scenario);

		Map<StageId, StageId> scenarioToSimStageMap = loadStages(scenario);
		Map<BatchId, BatchId> scenarioToSimBatchMap = loadBatches(scenario, scenarioToSimStageMap);
		loadBatchProperties(scenario, scenarioToSimBatchMap);
		loadStageOfferings(scenario, scenarioToSimStageMap);

		Map<PersonId, PersonId> scenarioToSimPeopleMap = loadPeople(scenario);
		loadPersonPropertyValues(scenario, scenarioToSimPeopleMap);
		loadRegionResources(scenario);
		loadMaterialsProducerResources(scenario);
		loadPersonResources(scenario, scenarioToSimPeopleMap);
		Map<GroupId, GroupId> scenarioToSimGroupMap = loadGroups(scenario);
		loadGroupMembership(scenario, scenarioToSimPeopleMap, scenarioToSimGroupMap);
		loadGroupPropertyValues(scenario, scenarioToSimGroupMap);

	}

	private Set<PersonPropertyId> personPropertyIds;

	private Set<ResourceId> resourceIds;

	private void loadGlobalPropertyValues(final Scenario scenario) {
		for (final GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
			final Object globalPropertyValue = scenario.getGlobalPropertyValue(globalPropertyId);
			if (globalPropertyValue != null) {
				setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			}
		}
	}

	@Override
	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			observationManager.handleGlobalPropertyChange(globalPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleGlobalPropertyValueAssignment(globalPropertyId);
	}

	private void loadRegionPropertyValues(final Scenario scenario) {
		for (final RegionId regionId : scenario.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
				final Object regionPropertyValue = scenario.getRegionPropertyValue(regionId, regionPropertyId);
				if (regionPropertyValue != null) {
					setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
				}
			}
		}
	}

	@Override
	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId,
			final Object regionPropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
			observationManager.handleRegionPropertyChange(regionId, regionPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleRegionPropertyValueAssignment(regionId, regionPropertyId);
	}

	private void loadMaterialsProducerPropertyValues(final Scenario scenario) {
		for (final MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : scenario
					.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = scenario
						.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				if (materialsProducerPropertyValue != null) {
					setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId,
							materialsProducerPropertyValue);
				}
			}
		}
	}

	@Override
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId,
					materialsProducerPropertyValue);
			observationManager.handleMaterialsProducerPropertyChange(materialsProducerId, materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleMaterialsProducerPropertyValueAssignment(materialsProducerId, materialsProducerPropertyId);
	}

	private void loadCompartmentPropertyValues(final Scenario scenario) {
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			for (final CompartmentPropertyId compartmentPropertyId : scenario
					.getCompartmentPropertyIds(compartmentId)) {
				final Object compartmentPropertyValue = scenario.getCompartmentPropertyValue(compartmentId,
						compartmentPropertyId);
				if (compartmentPropertyValue != null) {
					setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
				}
			}
		}
	}

	@Override
	public void setCompartmentPropertyValue(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
			observationManager.handleCompartmentPropertyChange(compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleCompartmentPropertyValueAssignment(compartmentId, compartmentPropertyId);
	}

	private void loadResourcePropertyValues(final Scenario scenario) {
		for (final ResourceId resourceId : scenario.getResourceIds()) {
			for (final ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
				final Object resourcePropertyValue = scenario.getResourcePropertyValue(resourceId, resourcePropertyId);
				if (resourcePropertyValue != null) {
					setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
				}
			}
		}
	}

	@Override
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,
			final Object resourcePropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
			observationManager.handleResourcePropertyChange(resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleResourcePropertyValueAssignment(resourceId, resourcePropertyId);
	}

	private Map<StageId, StageId> loadStages(Scenario scenario) {
		Map<StageId, StageId> result = new LinkedHashMap<>();

		List<StageId> scenarioStageIds = new ArrayList<>(scenario.getStageIds());
		Collections.sort(scenarioStageIds);
		for (StageId scenarioStageId : scenarioStageIds) {
			MaterialsProducerId materialsProducerId = scenario.getStageMaterialsProducer(scenarioStageId);
			StageId simulationStageId = createStage(materialsProducerId);
			result.put(scenarioStageId, simulationStageId);
		}
		return result;
	}

	@Override
	public StageId createStage(MaterialsProducerId materialsProducerId) {
		StageId stageId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			stageId = materialsManager.createStage(materialsProducerId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStageCreation(stageId);
		return stageId;
	}

	private Map<BatchId, BatchId> loadBatches(Scenario scenario, Map<StageId, StageId> scenarioToSimStageMap) {
		Map<BatchId, BatchId> result = new LinkedHashMap<>();
		List<BatchId> scenarioBatchIds = new ArrayList<>(scenario.getBatchIds());
		Collections.sort(scenarioBatchIds);
		for (BatchId scenarioBatchId : scenarioBatchIds) {
			MaterialsProducerId materialsProducerId = scenario.getBatchMaterialsProducer(scenarioBatchId);
			MaterialId materialId = scenario.getBatchMaterial(scenarioBatchId);
			double amount = scenario.getBatchAmount(scenarioBatchId);
			BatchId simulationBatchId = createBatch(materialsProducerId, materialId, amount);
			result.put(scenarioBatchId, simulationBatchId);
		}
		for (StageId scenarioStageId : scenario.getStageIds()) {
			Set<BatchId> scenarioBatches = scenario.getStageBatches(scenarioStageId);
			StageId simulationStageId = scenarioToSimStageMap.get(scenarioStageId);
			for (BatchId scenarioBatchId : scenarioBatches) {
				BatchId simulationBatchId = result.get(scenarioBatchId);
				moveBatchToStage(simulationBatchId, simulationStageId);
			}
		}

		return result;
	}

	@Override
	public BatchId createBatch(final MaterialsProducerId materialsProducerId, final MaterialId materialId,
			final double amount) {
		BatchId batchId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			batchId = materialsManager.createBatch(materialsProducerId, materialId, amount);
			propertyManager.handleBatchAddition(batchId, materialId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleBatchCreation(batchId);
		return batchId;
	}

	@Override
	public void moveBatchToStage(final BatchId batchId, final StageId stageId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			materialsManager.moveBatchToStage(batchId, stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStagedBatch(batchId);
	}

	private void loadBatchProperties(Scenario scenario, Map<BatchId, BatchId> scenarioToSimBatchMap) {
		Set<BatchId> scenarioBatchIds = scenario.getBatchIds();

		for (BatchId scenarioBatchId : scenarioBatchIds) {
			MaterialId materialId = scenario.getBatchMaterial(scenarioBatchId);
			Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				Object batchPropertyValue = scenario.getBatchPropertyValue(scenarioBatchId, batchPropertyId);
				BatchId simulationBatchId = scenarioToSimBatchMap.get(scenarioBatchId);
				setBatchPropertyValue(simulationBatchId, batchPropertyId, batchPropertyValue);
			}
		}
	}

	@Override
	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId,
			final Object batchPropertyValue) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			propertyManager.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleBatchPropertyValueAssignment(batchId, batchPropertyId);
	}

	private void loadStageOfferings(Scenario scenario, Map<StageId, StageId> scenarioToSimStageMap) {

		/*
		 * The stage offer state cannot be set until all of the batches have been
		 * associated with their stages.
		 */
		for (StageId scenarioStageId : scenario.getStageIds()) {
			StageId simulationStageId = scenarioToSimStageMap.get(scenarioStageId);
			boolean stageIsOffered = scenario.isStageOffered(scenarioStageId);
			setStageOffer(simulationStageId, stageIsOffered);
		}

	}

	@Override
	public void setStageOffer(final StageId stageId, final boolean offer) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			materialsManager.setStageOffer(stageId, offer);
			observationManager.handleStageOfferChange(stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStageOfferChange(stageId);
	}

	private Map<PersonId, PersonId> loadPeople(final Scenario scenario) {
		/*
		 * Build the map that will translate the person ids recorded in the scenario
		 * into a contiguous set of integers starting with zero.
		 */
		List<PersonId> scenarioPeopleIds = new ArrayList<>(scenario.getPeopleIds());
		Collections.sort(scenarioPeopleIds);
		Map<PersonId, PersonId> result = new LinkedHashMap<>();
		for (final PersonId scenarioPersonId : scenarioPeopleIds) {
			final RegionId regionId = scenario.getPersonRegion(scenarioPersonId);
			final CompartmentId compartmentId = scenario.getPersonCompartment(scenarioPersonId);
			PersonId simulationPersonId = addPerson(regionId, compartmentId);
			result.put(scenarioPersonId, simulationPersonId);
		}
		return result;
	}

	@Override
	public PersonId addPerson(final RegionId regionId, final CompartmentId compartmentId) {
		PersonId personId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			personId = personIdManager.addPersonId();
			personLocationManger.addPerson(personId, regionId, compartmentId);
			observationManager.handlePersonAddition(personId);
			propertyManager.handlePersonAddition(personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonAddition(personId);
		populationPartitionManager.handlePersonAddition(personId);
		reportsManager.handlePersonAddition(personId);
		return personId;
	}

	private void loadPersonPropertyValues(final Scenario scenario, Map<PersonId, PersonId> scenarioToSimPeopleMap) {
		for (final PersonId scenarioPersonId : scenario.getPeopleIds()) {
			for (final PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
				final Object personPropertyValue = scenario.getPersonPropertyValue(scenarioPersonId, personPropertyId);
				if (personPropertyValue != null) {
					PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
					setPersonPropertyValue(simulationPersonId, personPropertyId, personPropertyValue);
				}
			}
		}
	}

	@Override
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		Object oldValue;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			oldValue = propertyManager.getPersonPropertyValue(personId, personPropertyId);
			propertyManager.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
			observationManager.handlePersonPropertyChange(personId, personPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonPropertyValueChange(personId, personPropertyId, oldValue,
				personPropertyValue);
		populationPartitionManager.handlePersonPropertyValueChange(personId, personPropertyId,oldValue,personPropertyValue);
		reportsManager.handlePersonPropertyValueAssignment(personId, personPropertyId, oldValue);
	}

	private void loadRegionResources(final Scenario scenario) {
		for (final RegionId regionId : scenario.getRegionIds()) {
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				final Long amount = scenario.getRegionResourceLevel(regionId, resourceId);
				if (amount != null) {
					addResourceToRegion(resourceId, regionId, amount);
				}
			}
		}
	}

	@Override
	public void addResourceToRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			resourceManager.incrementRegionResourceLevel(regionId, resourceId, amount);
			observationManager.handleRegionResourceChange(regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleRegionResourceAddition(regionId, resourceId, amount);
	}

	private void loadMaterialsProducerResources(final Scenario scenario) {
		for (final MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				final Long amount = scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				if (amount != null) {
					addResourceToMaterialsProducer(materialsProducerId, resourceId, amount);
				}
			}
		}
	}

	/*
	 * Only called during scenario loading
	 */
	private void addResourceToMaterialsProducer(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final long amount) {
		resourceManager.incrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		observationManager.handleMaterialsProducerResourceChange(materialsProducerId, resourceId);
		reportsManager.handleMaterialsProducerResourceAddition(materialsProducerId, resourceId, amount);
	}

	private void loadPersonResources(final Scenario scenario, Map<PersonId, PersonId> scenarioToSimPeopleMap) {
		for (final PersonId scenarioPersonId : scenario.getPeopleIds()) {
			for (final ResourceId resourceId : scenario.getResourceIds()) {
				final Long amount = scenario.getPersonResourceLevel(scenarioPersonId, resourceId);
				if (amount != null) {
					PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
					addResourceToPerson(simulationPersonId, resourceId, amount);
				}
			}
		}
	}

	/*
	 * Only called during scenario loading
	 */
	private void addResourceToPerson(final PersonId personId, final ResourceId resourceId, final long amount) {
		resourceManager.incrementPersonResourceLevel(resourceId, personId, amount);
		indexedPopulationManager.handlePersonResourceLevelChange(personId, resourceId);
		populationPartitionManager.handlePersonResourceLevelChange(personId, resourceId);
		observationManager.handlePersonResourceChange(personId, resourceId);
		reportsManager.handlePersonResourceAddition(personId, resourceId, amount);
	}

	private Map<GroupId, GroupId> loadGroups(final Scenario scenario) {
		/*
		 * Build the map that will translate the group ids recorded in the scenario into
		 * a contiguous set of integers starting with zero.
		 */

		List<GroupId> scenarioGroupIds = new ArrayList<>(scenario.getGroupIds());
		Collections.sort(scenarioGroupIds);
		Map<GroupId, GroupId> result = new LinkedHashMap<>();
		for (final GroupId scenarioGroupId : scenarioGroupIds) {
			final GroupTypeId groupTypeId = scenario.getGroupTypeId(scenarioGroupId);
			GroupId simulationGroupId = addGroup(groupTypeId);
			result.put(scenarioGroupId, simulationGroupId);
		}
		return result;
	}

	@Override
	public GroupId addGroup(GroupTypeId groupTypeId) {
		GroupId groupId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			groupId = personGroupManger.addGroup(groupTypeId);
			observationManager.handleGroupAddition(groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleGroupAddition(groupId);
		return groupId;
	}

	private void loadGroupMembership(final Scenario scenario, Map<PersonId, PersonId> scenarioToSimPeopleMap,
			Map<GroupId, GroupId> scenarioToSimGroupMap) {
		for (final GroupId scenarioGroupId : scenario.getGroupIds()) {
			Set<PersonId> scenarioGroupMembers = scenario.getGroupMembers(scenarioGroupId);
			GroupId simulationGroupId = scenarioToSimGroupMap.get(scenarioGroupId);
			for (PersonId scenarioPersonId : scenarioGroupMembers) {
				PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
				addPersonToGroup(simulationPersonId, simulationGroupId);
			}
		}
	}

	@Override
	public void addPersonToGroup(PersonId personId, GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			personGroupManger.addPersonToGroup(groupId, personId);
			observationManager.handlePersonGroupAddition(groupId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonGroupAddition(groupId, personId);
		populationPartitionManager.handlePersonGroupAddition(groupId,personId);
		reportsManager.handleGroupMembershipAddition(groupId, personId);
	}

	private void loadGroupPropertyValues(final Scenario scenario, Map<GroupId, GroupId> scenarioToSimGroupMap) {
		for (final GroupId scenarioGroupId : scenario.getGroupIds()) {
			GroupTypeId groupType = scenario.getGroupTypeId(scenarioGroupId);
			for (final GroupPropertyId groupPropertyId : scenario.getGroupPropertyIds(groupType)) {
				final Object groupPropertyValue = scenario.getGroupPropertyValue(scenarioGroupId, groupPropertyId);
				if (groupPropertyValue != null) {
					GroupId simulationGroupId = scenarioToSimGroupMap.get(scenarioGroupId);
					setGroupPropertyValue(simulationGroupId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}

	@Override
	public void setGroupPropertyValue(GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue) {
		Object oldValue;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			oldValue = propertyManager.getGroupPropertyValue(groupId, groupPropertyId);
			propertyManager.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
			observationManager.handleGroupPropertyChange(groupId, groupPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleGroupPropertyValueAssignment(groupId, groupPropertyId, oldValue);
	}

	@Override
	public void addPlan(final Plan plan, final double planTime, final Object key) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			eventManager.addPlan(plan, planTime, key);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void addPopulationIndex(ComponentId componentId, final Filter filter, final Object key) {
		// externalAccessManager.acquireExternalReadAccessLock();
		// try {
		//
		// } finally {
		// externalAccessManager.releaseExternalReadAccessLock();
		// }
		indexedPopulationManager.addIndex(componentId, filter, key);
		//TODO why do we observe index removal and not addition?
	}
	/*
	 * Creates the information needed to support reports after the stage has been
	 * removed from the simulation
	 */

	private StageInfo getStageInfo(StageId stageId, boolean includeBatchInfo) {
		StageInfoBuilder stageInfoBuilder = new StageInfoBuilder();
		stageInfoBuilder.setMaterialsProducerId(materialsManager.getStageProducer(stageId));
		stageInfoBuilder.setStageId(stageId);
		stageInfoBuilder.setStageOffered(materialsManager.isStageOffered(stageId));
		List<BatchId> stageBatches = materialsManager.getStageBatches(stageId);
		if (includeBatchInfo) {
			BatchInfoBuilder batchInfoBuilder = new BatchInfoBuilder();
			for (final BatchId batchId : stageBatches) {
				batchInfoBuilder.setBatchId(batchId);
				batchInfoBuilder.setAmount(materialsManager.getBatchAmount(batchId));
				batchInfoBuilder.setCreationTime(materialsManager.getBatchAmount(batchId));
				batchInfoBuilder.setMaterialId(materialsManager.getBatchMaterial(batchId));
				batchInfoBuilder.setStageId(materialsManager.getBatchStageId(batchId));
				batchInfoBuilder.setMaterialsProducerId(materialsManager.getBatchProducer(batchId));
				MaterialId materialId = materialsManager.getBatchMaterial(batchId);

				Set<BatchPropertyId> batchPropertyIds = materialToBatchPropertyIdsMap.get(materialId);
				for (BatchPropertyId batchPropertyId : batchPropertyIds) {
					Object batchPropertyValue = propertyManager.getBatchPropertyValue(batchId, batchPropertyId);
					batchInfoBuilder.setPropertyValue(batchPropertyId, batchPropertyValue);
				}
				BatchInfo batchInfo = batchInfoBuilder.build();
				stageInfoBuilder.addBatchInfo(batchInfo);
			}
		}
		return stageInfoBuilder.build();
	}

	private Map<MaterialId, Set<BatchPropertyId>> materialToBatchPropertyIdsMap = new LinkedHashMap<>();
	private Map<GroupTypeId, Set<GroupPropertyId>> groupTypeToGroupPropertyIdsMap = new LinkedHashMap<>();

	@Override
	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount) {
		StageInfo stageInfo;
		BatchId batchId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			stageInfo = getStageInfo(stageId, true);
			batchId = materialsManager.convertStageToBatch(stageId, materialId, amount);
			observationManager.handleStageDestruction(stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStageConversionToBatch(stageInfo, batchId);
		return batchId;
	}

	@Override
	public void convertStageToResource(final StageId stageId, final ResourceId resourceId, final long amount) {
		StageInfo stageInfo;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			MaterialsProducerId materialsProducerId = materialsManager.getStageProducer(stageId);
			stageInfo = getStageInfo(stageId, true);
			materialsManager.destroyStage(stageId, true);
			resourceManager.incrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
			observationManager.handleMaterialsProducerResourceChange(materialsProducerId, resourceId);
			observationManager.handleStageDestruction(stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStageConversionToResource(stageInfo, resourceId, amount);
	}

	/*
	 * Creates the information needed to support reports after the batch has been
	 * removed from the simulation
	 */
	private BatchInfo getBatchInfo(BatchId batchId) {
		BatchInfoBuilder batchInfoBuilder = new BatchInfoBuilder();
		batchInfoBuilder.setBatchId(batchId);
		batchInfoBuilder.setAmount(materialsManager.getBatchAmount(batchId));
		batchInfoBuilder.setCreationTime(materialsManager.getBatchTime(batchId));
		batchInfoBuilder.setMaterialId(materialsManager.getBatchMaterial(batchId));
		batchInfoBuilder.setStageId(materialsManager.getBatchStageId(batchId));
		batchInfoBuilder.setMaterialsProducerId(materialsManager.getBatchProducer(batchId));
		Set<BatchPropertyId> batchPropertyIds = materialToBatchPropertyIdsMap
				.get(materialsManager.getBatchMaterial(batchId));
		for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
			Object batchPropertyValue = propertyManager.getBatchPropertyValue(batchId, batchPropertyId);
			batchInfoBuilder.setPropertyValue(batchPropertyId, batchPropertyValue);
		}
		return batchInfoBuilder.build();
	}

	@Override
	public void destroyBatch(final BatchId batchId) {
		BatchInfo batchInfo = null;
		externalAccessManager.acquireGlobalReadAccessLock();

		try {
			if (reportsManager.hasBatchDestructionReports()) {
				batchInfo = getBatchInfo(batchId);
			}
			materialsManager.destroyBatch(batchId);
			propertyManager.handleBatchRemoval(batchId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

		if (batchInfo != null) {
			reportsManager.handleBatchDestruction(batchInfo);
		}
	}

	@Override
	public void destroyStage(final StageId stageId, final boolean destroyBatches) {
		StageInfo stageInfo = null;
		List<BatchId> stageBatches = null;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			if (reportsManager.hasStageDestructionReports()) {
				stageInfo = getStageInfo(stageId, destroyBatches);
			}

			if (!destroyBatches) {
				stageBatches = materialsManager.getStageBatches(stageId);
			}

			materialsManager.destroyStage(stageId, destroyBatches);
			observationManager.handleStageDestruction(stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

		if (stageBatches != null) {
			for (final BatchId batchId : stageBatches) {
				reportsManager.handleUnStagedBatch(batchId, stageId);
			}
		}
		if (stageInfo != null) {
			reportsManager.handleStageDestruction(stageInfo);
		}
	}

	@Override
	public void halt() {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			eventManager.halt();
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void moveBatchToInventory(final BatchId batchId) {
		StageId stageId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			stageId = materialsManager.getBatchStageId(batchId);
			materialsManager.moveBatchToInventory(batchId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleUnStagedBatch(batchId, stageId);
	}

	@Override
	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeCompartmentalPersonPropertyChange(observe, compartmentId, personPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId,
			final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeCompartmentalPersonResourceChange(observe, compartmentId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeCompartmentPersonArrival(final boolean observe, final CompartmentId compartmentId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeCompartmentPersonArrivals(observe, compartmentId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeCompartmentPersonDeparture(final boolean observe, final CompartmentId compartmentId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeCompartmentPersonDepartures(observe, compartmentId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeCompartmentPropertyChange(observe, compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalPersonArrival(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalPersonArrivals(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalPersonDeparture(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalPersonDepartures(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalPersonPropertyChange(observe, personPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalPersonResourceChange(observe, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalPropertyChange(observe, globalPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeMaterialsProducerPropertyChange(final boolean observe,
			final MaterialsProducerId materialProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeMaterialsProducerPropertyChange(observe, materialProducerId,
					materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeMaterialsProducerResourceChange(observe, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeMaterialsProducerResourceChangeByMaterialsProducerId(final boolean observe,
			final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeMaterialsProducerResourceChangeByResourceId(observe, materialsProducerId,
					resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observePersonCompartmentChange(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeIndividualPersonCompartmentChange(observe, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observePersonPropertyChange(final boolean observe, final PersonId personId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeIndividualPersonPropertyChange(observe, personId, personPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observePersonRegionChange(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeIndividualPersonRegionChange(observe, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observePersonResourceChange(final boolean observe, final PersonId personId,
			final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeIndividualPersonResourceChange(observe, personId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionPersonPropertyChange(observe, regionId, personPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionPersonResourceChange(observe, regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionPersonArrival(final boolean observe, final RegionId regionId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionPersonArrivals(observe, regionId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionPersonDeparture(final boolean observe, final RegionId regionId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionPersonDepartures(observe, regionId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGlobalRegionPropertyChange(observe, regionPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId,
			final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionPropertyChange(observe, regionId, regionPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeRegionResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeRegionResourceChange(observe, regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeResourcePropertyChange(observe, resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeStageOfferChange(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageOfferChange(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageOfferChangeByStageId(observe, stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeStageTransfer(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageTransfer(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeStageTransferByStageId(final boolean observe, final StageId stageId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageTransferByStageId(observe, stageId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void removeGroup(GroupId groupId) {
		List<PersonId> peopleForGroup;
		GroupInfo groupInfo = null;
		externalAccessManager.acquireGlobalReadAccessLock();
		boolean reportsActive = reportsManager.hasGroupRemovalReports();
		boolean observationManagerRequiresGroupInfo = observationManager.requiresGroupInfoForGroupRemoval(groupId);
		try {
			peopleForGroup = personGroupManger.getPeopleForGroup(groupId);

			// don't create the groupInfo if it is not needed
			if (reportsActive || observationManagerRequiresGroupInfo) {
				GroupInfoBuilder groupInfoBuilder = new GroupInfoBuilder();
				groupInfoBuilder.setGroupId(groupId);
				GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
				groupInfoBuilder.setGroupTypeId(groupTypeId);
				Set<GroupPropertyId> groupPropertyIds = groupTypeToGroupPropertyIdsMap.get(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					Object groupPropertyValue = propertyManager.getGroupPropertyValue(groupId, groupPropertyId);
					groupInfoBuilder.setGroupPropertyValue(groupPropertyId, groupPropertyValue);
				}
				for (PersonId personId : peopleForGroup) {
					groupInfoBuilder.addPerson(personId);
				}
				groupInfo = groupInfoBuilder.build();
			}

			for (PersonId personId : peopleForGroup) {
				indexedPopulationManager.handlePersonGroupRemoval(groupId, personId);
				populationPartitionManager.handlePersonGroupRemoval(groupId,personId);
			}
			personGroupManger.removeGroup(groupId);
			propertyManager.handleGroupRemoval(groupId);
			if (observationManagerRequiresGroupInfo) {
				observationManager.handleGroupRemovalByGroupInfo(groupInfo);
			} else {
				observationManager.handleGroupRemoval(groupId);
			}

		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

		if (reportsActive) {
			reportsManager.handleGroupRemoval(groupInfo);
		}
		for (PersonId personId : peopleForGroup) {
			reportsManager.handleGroupMembershipRemoval(groupId, personId);
		}

	}

	@Override
	public void removePerson(final PersonId personId) {
		PersonInfo personInfo = null;
		externalAccessManager.acquireGlobalReadAccessLock();
		boolean reportsActive = reportsManager.hasPersonRemovalReports();
		boolean observationManagerRequiresPersonInfo = observationManager.requiresPersonInfoForPersonRemoval();

		try {

			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);

			final RegionId regionId = personLocationManger.getPersonRegion(personId);

			if (reportsActive || observationManagerRequiresPersonInfo) {
				PersonInfoBuilder personInfoBuilder = new PersonInfoBuilder();
				personInfoBuilder.setPersonRegionId(regionId);
				personInfoBuilder.setPersonCompartmentId(compartmentId);
				personInfoBuilder.setPersonId(personId);
				/*
				 * Before we drop the person, we collect the person's property values in a local
				 * map so that we can pass it to the state change listeners.
				 */

				for (final PersonPropertyId personPropertyId : personPropertyIds) {
					final Object personPropertyValue = propertyManager.getPersonPropertyValue(personId,
							personPropertyId);
					personInfoBuilder.setPersonPropertyValue(personPropertyId, personPropertyValue);
				}

				/*
				 * Before we drop the person, we collect the person's resource values in a local
				 * map so that we can pass it to the state change listeners.
				 */

				for (final ResourceId resourceId : resourceIds) {
					final long resourceLevel = resourceManager.getPersonResourceLevel(resourceId, personId);
					personInfoBuilder.setPersonResourceValue(resourceId, resourceLevel);
				}
				personInfo = personInfoBuilder.build();
			}

			indexedPopulationManager.handlePersonRemoval(personId);
			populationPartitionManager.handlePersonRemoval(personId);
			propertyManager.handlePersonRemoval(personId);
			if (observationManagerRequiresPersonInfo) {
				observationManager.handlePersonRemovalByPersonInfo(personInfo);
			} else {
				observationManager.handlePersonRemoval(personId);
			}
			personLocationManger.removePerson(personId);
			personGroupManger.removePerson(personId);
			personIdManager.removePerson(personId);

		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

		if (reportsActive) {
			reportsManager.handlePersonRemoval(personInfo);
		}
	}

	@Override
	public void removePersonFromGroup(PersonId personId, GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			personGroupManger.removePersonFromGroup(groupId, personId);
			observationManager.handlePersonGroupRemoval(groupId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonGroupRemoval(groupId, personId);
		populationPartitionManager.handlePersonGroupRemoval(groupId,personId);
		reportsManager.handleGroupMembershipRemoval(groupId, personId);
	}

	@Override
	public <T> Optional<T> removePlan(final Object key) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			return eventManager.removePlan(key);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void removePopulationIndex(final Object key) {
		// externalAccessManager.acquireExternalReadAccessLock();
		// try {
		//
		// } finally {
		// externalAccessManager.releaseExternalReadAccessLock();
		// }
		indexedPopulationManager.removeIndex(key);
		observationManager.handlePopulationIndexRemoval(key);
	}

	@Override
	public void removeResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			resourceManager.decrementPersonResourceLevel(resourceId, personId, amount);
			observationManager.handlePersonResourceChange(personId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

		indexedPopulationManager.handlePersonResourceLevelChange(personId, resourceId);
		populationPartitionManager.handlePersonResourceLevelChange(personId, resourceId);
		reportsManager.handlePersonResourceRemoval(personId, resourceId, amount);
	}

	@Override
	public void removeResourceFromRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			resourceManager.decrementRegionResourceLevel(regionId, resourceId, amount);
			observationManager.handleRegionResourceChange(regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleRegionResourceRemoval(regionId, resourceId, amount);
	}

	@Override
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
		CompartmentId oldCompartmentId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			oldCompartmentId = personLocationManger.getPersonCompartment(personId);
			personLocationManger.setPersonCompartment(personId, compartmentId);
			observationManager.handlePersonCompartmentChange(personId, oldCompartmentId, compartmentId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonCompartmentChange(personId, oldCompartmentId, compartmentId);
		populationPartitionManager.handlePersonCompartmentChange(personId, oldCompartmentId, compartmentId);
		reportsManager.handleCompartmentAssignment(personId, oldCompartmentId);
	}

	@Override
	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		RegionId oldRegionId;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			oldRegionId = personLocationManger.getPersonRegion(personId);
			personLocationManger.setPersonRegion(personId, regionId);
			observationManager.handlePersonRegionChange(personId, oldRegionId, regionId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonRegionChange(personId, oldRegionId, regionId);
		populationPartitionManager.handlePersonRegionChange(personId, oldRegionId, regionId);
		reportsManager.handleRegionAssignment(personId, oldRegionId);
	}

	@Override
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			materialsManager.shiftBatchContent(sourceBatchId, destinationBatchId, amount);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleBatchShift(sourceBatchId, destinationBatchId, amount);
	}

	@Override
	public void transferOfferedStageToMaterialsProducer(final StageId stageId,
			final MaterialsProducerId materialsProducerId) {
		MaterialsProducerId stageProducer;
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			stageProducer = materialsManager.getStageProducer(stageId);
			materialsManager.transferOfferedStageToMaterialsProducer(materialsProducerId, stageId);
			observationManager.handleStageTransfer(stageId, stageProducer, materialsProducerId);
			observationManager.handleStageOfferChange(stageId);

		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleStageTransfer(stageId, stageProducer);
		reportsManager.handleStageOfferChange(stageId);
	}

	@Override
	public void transferProducedResourceToRegion(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			resourceManager.decrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
			resourceManager.incrementRegionResourceLevel(regionId, resourceId, amount);
			observationManager.handleMaterialsProducerResourceChange(materialsProducerId, resourceId);
			observationManager.handleRegionResourceChange(regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleTransferResourceFromMaterialsProducerToRegion(materialsProducerId, regionId, resourceId,
				amount);
	}

	@Override
	public void transferResourceBetweenRegions(final ResourceId resourceId, final RegionId sourceRegionId,
			final RegionId destinationRegionId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			resourceManager.decrementRegionResourceLevel(sourceRegionId, resourceId, amount);
			resourceManager.incrementRegionResourceLevel(destinationRegionId, resourceId, amount);
			observationManager.handleRegionResourceChange(sourceRegionId, resourceId);
			observationManager.handleRegionResourceChange(destinationRegionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		reportsManager.handleTransferResourceBetweenRegions(sourceRegionId, destinationRegionId, resourceId, amount);
	}

	@Override
	public void transferResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			resourceManager.decrementPersonResourceLevel(resourceId, personId, amount);
			resourceManager.incrementRegionResourceLevel(regionId, resourceId, amount);
			observationManager.handlePersonResourceChange(personId, resourceId);
			observationManager.handleRegionResourceChange(regionId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonResourceLevelChange(personId, resourceId);
		populationPartitionManager.handlePersonResourceLevelChange(personId, resourceId);
		reportsManager.handlePersonResourceTransferToRegion(personId, resourceId, amount);
	}

	@Override
	public void transferResourceToPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			resourceManager.decrementRegionResourceLevel(regionId, resourceId, amount);
			resourceManager.incrementPersonResourceLevel(resourceId, personId, amount);
			observationManager.handleRegionResourceChange(regionId, resourceId);
			observationManager.handlePersonResourceChange(personId, resourceId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
		indexedPopulationManager.handlePersonResourceLevelChange(personId, resourceId);
		populationPartitionManager.handlePersonResourceLevelChange(personId, resourceId);
		reportsManager.handleRegionResourceTransferToPerson(personId, resourceId, amount);
	}

	@Override
	public void observeGroupArrival(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrival(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrivalByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrivalByGroupAndPerson(observe, groupId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

	}

	@Override
	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrivalByPerson(observe, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrivalByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupArrivalByTypeAndPerson(observe, groupTypeId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupConstruction(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupConstruction(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupConstructionByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDeparture(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDeparture(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDepartureByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDepartureByGroupAndPerson(observe, groupId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDepartureByPerson(observe, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDepartureByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDepartureByTypeAndPerson(observe, groupTypeId, personId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDestruction(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDestruction(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDestructionByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDestructionByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupDestructionByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupPropertyChange(final boolean observe) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupPropertyChange(observe);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupPropertyChangeByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe,
			final GroupPropertyId groupPropertyId, final GroupId groupId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupPropertyChangeByGroupAndProperty(observe, groupPropertyId, groupId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupPropertyChangeByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeGroupPropertyChangeByTypeAndProperty(observe, groupTypeId, groupPropertyId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void observePopulationIndexChange(boolean observe, Object key) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observePopulationIndexChange(observe, key);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

	}

	@Override
	public void observeStageTransferBySourceMaterialsProducerId(boolean observe,
			MaterialsProducerId sourceMaterialsProducerId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageTransferBySourceMaterialsProducerId(observe, sourceMaterialsProducerId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

	}

	@Override
	public void observeStageTransferByDestinationMaterialsProducerId(boolean observe,
			MaterialsProducerId destinationMaterialsProducerId) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			observationManager.observeStageTransferByDestinationMaterialsProducerId(observe,
					destinationMaterialsProducerId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}

	}

	@Override
	public void addGlobalComponent(GlobalComponentId globalComponentId,
			Class<? extends Component> globalComponentClass) {
		externalAccessManager.acquireGlobalReadAccessLock();
		try {
			componentManager.addGlobalComponent(globalComponentId, globalComponentClass);
			eventManager.initGlobalComponent(globalComponentId);
		} finally {
			externalAccessManager.releaseGlobalReadAccessLock();
		}
	}

	@Override
	public void addPartition(ComponentId componentId,
			Partition partition, Object key) {
		//TODO -- review methods that require callbacks and perhaps lock down external write access?
		// externalAccessManager.acquireExternalReadAccessLock();
		// try {
		//
		// } finally {
		// externalAccessManager.releaseExternalReadAccessLock();
		// }
		populationPartitionManager.addPartition(componentId, partition, key);
	}

	@Override
	public void removePartition(Object key) {		
		populationPartitionManager.removePartition(key);		
	}

}
