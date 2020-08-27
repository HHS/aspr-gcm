
package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.components.Component;
import gcm.output.OutputItem;
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
import gcm.scenario.MapOption;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.simulation.FilterInfo.CompartmentFilterInfo;
import gcm.simulation.FilterInfo.GroupMemberFilterInfo;
import gcm.simulation.FilterInfo.GroupTypesForPersonFilterInfo;
import gcm.simulation.FilterInfo.GroupsForPersonAndGroupTypeFilterInfo;
import gcm.simulation.FilterInfo.GroupsForPersonFilterInfo;
import gcm.simulation.FilterInfo.PropertyFilterInfo;
import gcm.simulation.FilterInfo.RegionFilterInfo;
import gcm.simulation.FilterInfo.ResourceFilterInfo;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.PopulationPartitionDefinition;
import gcm.simulation.partition.PopulationPartitionManager;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;

/**
 * Implementor of {@link Environment}
 * 
 * Executes mutations in the following pattern:
 * 
 * <pre>
	public ReturnType mutateSomething(various args...) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateArgument1();
			validateArgument2();
			validateArgument3();
			...			
			validateComponentPermissionToExecuteMutation();
			mutationResolver.perfomTheMutation(args...);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}
 * </pre>
 * 
 * 
 * Executes queries in the following pattern:
 * 
 * <pre>
  public ReturnType getSomething(various args...) {
 	externalAccessManager.acquireReadAccess();
  	try {
  		validateArgument1();
		validateArgument2();
		validateArgument3();
		...
 		return someDataManager.getSomeData(args...);
 	} finally {
 		externalAccessManager.releaseReadAccess();
 	}
  }
 * </pre>
 * 
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
@Source(status = TestStatus.REQUIRED)
public final class EnvironmentImpl extends BaseElement implements Environment {

	private Context context;

	/*
	 * The PopulationPartitionManager maintains the population partitions. The
	 * Environment works with the components to create the partitions, but once
	 * created delegates custody to the manager. Then environment informs the
	 * manager of changes that are relevant to partition management without having
	 * to know the actual partitions.
	 */
	private PopulationPartitionManager populationPartitionManager;

	/*
	 * The IndexedPopulationManager maintains the population indexes. The
	 * Environment works with the components to create the indices, but once created
	 * delegates custody to the manager. Then environment informs the manager of
	 * changes that are relevant to index management without having to know the
	 * actual indices.
	 */
	private IndexedPopulationManager indexedPopulationManager;

	/*
	 * The ObservableEnvironment is a sub-interface of the Environment interface
	 * that exposes only the non-mutative parts of the environment that are possibly
	 * needed by State Change Listeners and other contributed items that do not need
	 * mutation access. This is done so that we need only pass the absolute minimum
	 * of information when a state change occurs. We leave it to the state change
	 * listeners to fill in whatever information they need from the
	 * ObservableEnvironment.
	 */
	private ObservableEnvironment observableEnvironment;

	private ProfileManager profileManager;

	private MaterialsManager materialsManager;

	private ResourceManager resourceManager;

	private PropertyDefinitionManager propertyDefinitionManager;

	private ComponentManager componentManager;

	private OutputItemManager outputItemManager;

	private EventManager eventManager;

	private PropertyManager propertyManager;

	private PersonLocationManger personLocationManger;

	private PersonGroupManger personGroupManger;

	private PersonIdManager personIdManager;

	private MutationResolver mutationResolver;

	private Set<RegionId> regionIds;

	private Set<MaterialsProducerId> materialsProducerIds;

	private Set<CompartmentId> compartmentIds;

	private Set<ResourceId> resourceIds;

	private Set<RandomNumberGeneratorId> randomNumberGeneratorIds;

	private Map<GroupTypeId, Set<GroupPropertyId>> groupTypeIds = new LinkedHashMap<>();

	/*
	 * The external access manager MUST be used for every mutation and query and
	 * prevents callbacks from corrupting state during the execution of mutations.
	 */
	private ExternalAccessManager externalAccessManager;

	private Map<MaterialId, Set<BatchPropertyId>> materialIds = new LinkedHashMap<>();

	private Map<CompartmentId, Set<CompartmentPropertyId>> compartmentPropertyIds = new LinkedHashMap<>();

	private Set<GlobalPropertyId> globalPropertyIds;

	private Map<ResourceId, Set<ResourcePropertyId>> resourcePropertyIdsMap;

	private Set<RegionPropertyId> regionPropertyIds;

	private Set<PersonPropertyId> personPropertyIds;

	private Set<MaterialsProducerPropertyId> materialsProducerPropertyIds;

	private Scenario scenario;
	private ScenarioId scenarioId;
	private ReplicationId replicationId;

	@Override
	public GroupId addGroup(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateFocalComponent(true, true, true, false, null, null, null);
			validateGroupTypeId(groupTypeId);
			return mutationResolver.addGroup(groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public PersonId addPerson(final RegionId regionId, final CompartmentId compartmentId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateFocalComponent(true, false, false, false, null, null, null);
			validateCompartmentId(compartmentId);
			validateRegionId(regionId);
			return mutationResolver.addPerson(regionId, compartmentId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void addPersonToGroup(final PersonId personId, final GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateFocalComponent(true, true, true, false, null, null, null);
			validateGroupExists(groupId);
			validatePersonNotInGroup(personId, groupId);
			mutationResolver.addPersonToGroup(personId, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void addPlan(final Plan plan, final double planTime) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePlanNotNull(plan);
			validatePlanTime(planTime);
			mutationResolver.addPlan(plan, planTime, null);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void addPlan(final Plan plan, final double planTime, final Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePlanKeyNotNull(key);
			validatePlanKeyNotDuplicate(key);
			validatePlanNotNull(plan);
			validatePlanTime(planTime);
			validateComponentHasFocus();
			mutationResolver.addPlan(plan, planTime, key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void addPopulationIndex(final Filter filter, final Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateFilter(filter);
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexDoesNotExist(key);
			mutationResolver.addPopulationIndex(componentManager.getFocalComponentId(), filter, key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void addResourceToRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateFocalComponent(true, false, false, false, regionId, null, null);
			validateRegionId(regionId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			long regionResourceLevel = resourceManager.getRegionResourceLevel(regionId, resourceId);
			validateResourceAdditionValue(regionResourceLevel, amount);
			mutationResolver.addResourceToRegion(resourceId, regionId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public boolean batchExists(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			return materialsManager.batchExists(batchId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	private void validateEqualityCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(propertyDefinition.getType())) {
			throwModelException(SimulationErrorType.NON_COMPARABLE_PROPERTY,
					"Property values for " + propertyId + " are not comparable via " + equality);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throwModelException(SimulationErrorType.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (componentManager.getFocalComponentType() != ComponentType.SIM) {
			if (!propertyDefinition.getPropertyValuesAreMutability()) {
				throwModelException(SimulationErrorType.IMMUTABLE_VALUE);
			}
		}
	}

	@Override
	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateMaterialId(materialId);
			validateStageId(stageId);
			final MaterialsProducerId materialProducerId = materialsManager.getStageProducer(stageId);
			validateFocalComponent(false, false, false, false, null, null, materialProducerId);
			validateStageIsNotOffered(stageId);
			validateNonnegativeFiniteMaterialAmount(amount);
			return mutationResolver.convertStageToBatch(stageId, materialId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void convertStageToResource(final StageId stageId, final ResourceId resourceId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateResourceId(resourceId);
			validateStageId(stageId);
			validateStageIsNotOffered(stageId);
			final MaterialsProducerId materialsProducerId = materialsManager.getStageProducer(stageId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			validateNonnegativeResourceAmount(amount);
			long currentResourceLevel = resourceManager.getMaterialsProducerResourceLevel(materialsProducerId,
					resourceId);
			validateResourceAdditionValue(currentResourceLevel, amount);
			mutationResolver.convertStageToResource(stageId, resourceId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public BatchId createBatch(final MaterialId materialId, final double amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateFocalComponent(false, false, false, true, null, null, null);
			validateMaterialId(materialId);
			validateNonnegativeFiniteMaterialAmount(amount);
			final MaterialsProducerId materialsProducerId = (MaterialsProducerId) componentManager
					.getFocalComponentId();
			return mutationResolver.createBatch(materialsProducerId, materialId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public StageId createStage() {
		externalAccessManager.acquireWriteAccess();
		try {
			validateFocalComponent(false, false, false, true, null, null, null);
			final MaterialsProducerId materialsProducerId = (MaterialsProducerId) componentManager
					.getFocalComponentId();
			return mutationResolver.createStage(materialsProducerId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void destroyBatch(final BatchId batchId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateBatchId(batchId);
			validateBatchIsNotOnOfferedStage(batchId);
			final MaterialsProducerId materialsProducerId = materialsManager.getBatchProducer(batchId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			mutationResolver.destroyBatch(batchId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void destroyStage(final StageId stageId, final boolean destroyBatches) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateStageId(stageId);
			validateStageIsNotOffered(stageId);
			final MaterialsProducerId materialsProducerId = materialsManager.getStageProducer(stageId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			mutationResolver.destroyStage(stageId, destroyBatches);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public double getBatchAmount(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			return materialsManager.getBatchAmount(batchId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			return materialsManager.getBatchMaterial(batchId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getBatchProducer(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			return materialsManager.getBatchProducer(batchId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId,
			final BatchPropertyId batchPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialId(materialId);
			validateBatchPropertyId(materialId, batchPropertyId);
			return propertyDefinitionManager.getBatchPropertyDefinition(materialId, batchPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialId(materialId);
			return propertyDefinitionManager.getBatchPropertyIds(materialId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			final MaterialId materialId = materialsManager.getBatchMaterial(batchId);
			validateBatchPropertyId(materialId, batchPropertyId);
			return propertyManager.getBatchPropertyTime(batchId, batchPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			final MaterialId batchMaterial = materialsManager.getBatchMaterial(batchId);
			validateBatchPropertyId(batchMaterial, batchPropertyId);
			return propertyManager.getBatchPropertyValue(batchId, batchPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<StageId> getBatchStageId(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			final StageId stageId = materialsManager.getBatchStageId(batchId);
			if (stageId == null) {
				return Optional.empty();
			}
			return Optional.of(stageId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getBatchTime(final BatchId batchId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateBatchId(batchId);
			return materialsManager.getBatchTime(batchId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getBiWeightedGroupContact(final GroupId groupId, final PersonId sourcePersonId,
			final boolean excludeSourcePerson, final BiWeightingFunction biWeightingFunction) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(sourcePersonId);
			validateGroupExists(groupId);
			validateBiWeightingFunctionNotNull(biWeightingFunction);

			final StochasticPersonSelection stochasticPersonSelection = personGroupManger.getBiWeightedContact(groupId,
					sourcePersonId, excludeSourcePerson, biWeightingFunction);
			validateStochasticPersonSelection(stochasticPersonSelection);
			if (stochasticPersonSelection.getPersonId() == null) {
				return Optional.empty();
			}
			return Optional.of(stochasticPersonSelection.getPersonId());

		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getBiWeightedGroupContactFromGenerator(final GroupId groupId,
			final PersonId sourcePersonId, final boolean excludeSourcePerson,
			final BiWeightingFunction biWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(sourcePersonId);
			validateGroupExists(groupId);
			validateBiWeightingFunctionNotNull(biWeightingFunction);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);

			final StochasticPersonSelection stochasticPersonSelection = personGroupManger
					.getBiWeightedContactFromGenerator(groupId, sourcePersonId, excludeSourcePerson,
							biWeightingFunction, randomNumberGeneratorId);
			validateStochasticPersonSelection(stochasticPersonSelection);
			if (stochasticPersonSelection.getPersonId() == null) {
				return Optional.empty();
			}
			return Optional.of(stochasticPersonSelection.getPersonId());

		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends CompartmentId> Set<T> getCompartmentIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getCompartmentIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public MapOption getCompartmentMapOption() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getCompartmentMapOption();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			return personLocationManger.getCompartmentPopulationCount(compartmentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getCompartmentPopulationCountTime(final CompartmentId compartmentId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			return personLocationManger.getCompartmentPopulationTime(compartmentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			validateCompartmentProperty(compartmentId, compartmentPropertyId);
			return propertyDefinitionManager.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			return propertyDefinitionManager.getCompartmentPropertyIds(compartmentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getCompartmentPropertyTime(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			validateCompartmentProperty(compartmentId, compartmentPropertyId);
			return propertyManager.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			validateCompartmentProperty(compartmentId, compartmentPropertyId);
			return propertyManager.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getGlobalComponentIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Class<? extends Component> getGlobalComponentClass(GlobalComponentId globalComponentId) {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getGlobalComponentClass(globalComponentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Class<? extends Component> getCompartmentComponentClass(CompartmentId compartmentId) {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getCompartmentComponentClass(compartmentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}

	}

	@Override
	public Class<? extends Component> getMaterialsProducerComponentClass(MaterialsProducerId materialsProducerId) {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getMaterialsProducerComponentClass(materialsProducerId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}

	}

	@Override
	public Class<? extends Component> getRegionComponentClass(RegionId regionId) {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getRegionComponentClass(regionId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}

	}

	@Override
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGlobalPropertyId(globalPropertyId);
			return propertyDefinitionManager.getGlobalPropertyDefinition(globalPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return propertyDefinitionManager.getGlobalPropertyIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGlobalPropertyId(globalPropertyId);
			return propertyManager.getGlobalPropertyTime(globalPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGlobalPropertyId(globalPropertyId);
			return propertyManager.getGlobalPropertyValue(globalPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getGroupCountForGroupType(groupTypeId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getGroupCountForPerson(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personGroupManger.getGroupCountForPerson(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<GroupId> getGroupIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return personGroupManger.getGroupIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			return propertyDefinitionManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			return propertyDefinitionManager.getGroupPropertyIds(groupTypeId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			return propertyManager.getGroupPropertyTime(groupId, groupPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			return propertyManager.getGroupPropertyValue(groupId, groupPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getGroupsForGroupType(groupTypeId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getGroupsForGroupTypeAndPerson(groupTypeId, personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personGroupManger.getGroupsForPerson(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			return personGroupManger.getGroupType(groupId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getGroupTypeCountForPerson(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personGroupManger.getGroupTypeCountForPersonId(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getGroupTypeIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personGroupManger.getGroupTypesForPerson(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getIndexedPeople(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			return indexedPopulationManager.getIndexedPeople(key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getIndexSize(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			return indexedPopulationManager.getIndexSize(key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			return materialsManager.getInventoryBatches(materialsProducerId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId,
			final MaterialId materialId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateMaterialId(materialId);
			return materialsManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends MaterialId> Set<T> getMaterialIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getMaterialIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getMaterialsProducerIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getMaterialsProducerPropertyDefinition(
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			return propertyDefinitionManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return propertyDefinitionManager.getMaterialsProducerPropertyIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			return propertyManager.getMaterialsProducerPropertyTime(materialsProducerId, materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			return propertyManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateResourceId(resourceId);
			return resourceManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateResourceId(resourceId);
			return resourceManager.getMaterialsProducerResourceTime(materialsProducerId, resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getMonoWeightedGroupContact(final GroupId groupId,
			final MonoWeightingFunction monoWeightingFunction) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			validateMonoWeightingFunctionNotNull(monoWeightingFunction);
			final StochasticPersonSelection stochasticPersonSelection = personGroupManger
					.getMonoWeightedContact(groupId, monoWeightingFunction);
			validateStochasticPersonSelection(stochasticPersonSelection);
			if (stochasticPersonSelection.getPersonId() == null) {
				return Optional.empty();
			}
			return Optional.of(stochasticPersonSelection.getPersonId());
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getMonoWeightedGroupContactFromGenerator(final GroupId groupId,
			final MonoWeightingFunction monoWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			validateMonoWeightingFunctionNotNull(monoWeightingFunction);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final StochasticPersonSelection stochasticPersonSelection = personGroupManger
					.getMonoWeightedContactFromGenerator(groupId, monoWeightingFunction, randomNumberGeneratorId);
			validateStochasticPersonSelection(stochasticPersonSelection);
			if (stochasticPersonSelection.getPersonId() == null) {
				return Optional.empty();
			}
			return Optional.of(stochasticPersonSelection.getPersonId());
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getNonWeightedGroupContact(final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			final PersonId personId = personGroupManger.getNonWeightedContact(groupId, null);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);

		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getNonWeightedGroupContactWithExclusion(final GroupId groupId,
			final PersonId excludedPersonId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			validatePersonExists(excludedPersonId);
			final PersonId personId = personGroupManger.getNonWeightedContact(groupId, excludedPersonId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getNonWeightedGroupContactFromGenerator(final GroupId groupId,
			RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = personGroupManger.getNonWeightedContactFromGenerator(groupId, null,
					randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);

		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getNonWeightedGroupContactWithExclusionFromGenerator(final GroupId groupId,
			final PersonId excludedPersonId, RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			validatePersonExists(excludedPersonId);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = personGroupManger.getNonWeightedContactFromGenerator(groupId, excludedPersonId,
					randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public ObservableEnvironment getObservableEnvironment() {
		externalAccessManager.acquireReadAccess();
		try {
			return observableEnvironment;
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			return materialsManager.getOfferedStages(materialsProducerId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeople() {
		externalAccessManager.acquireReadAccess();
		try {
			return personIdManager.getPeople();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			return personGroupManger.getPeopleForGroup(groupId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getPeopleForGroupType(groupTypeId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateCompartmentId(compartmentId);
			return personLocationManger.getPeopleInCompartment(compartmentId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			return personLocationManger.getPeopleInRegion(regionId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			return resourceManager.getPeopleWithoutResource(resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonPropertyId(personPropertyId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getPersonPropertyDefinition(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
			return propertyManager.getPeopleWithPropertyValue(personPropertyId, personPropertyValue);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonPropertyId(personPropertyId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getPersonPropertyDefinition(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
			return propertyManager.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			return resourceManager.getPeopleWithResource(resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personLocationManger.getPersonCompartment(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePersonCompartmentArrivalsTimesTracked();
			return personLocationManger.getPersonCompartmentArrivalTime(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getPersonCompartmentArrivalTrackingPolicy();
		} finally {
			externalAccessManager.releaseReadAccess();
		}

	}

	@Override
	public int getPersonCountForGroup(final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupExists(groupId);
			return personGroupManger.getPersonCountForGroup(groupId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateGroupTypeId(groupTypeId);
			return personGroupManger.getPersonCountForGroupType(groupTypeId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonPropertyId(personPropertyId);
			return propertyDefinitionManager.getPersonPropertyDefinition(personPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return propertyDefinitionManager.getPersonPropertyIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePersonPropertyId(personPropertyId);
			validatePersonPropertyAssignmentTimesTracked(personPropertyId);
			return propertyManager.getPersonPropertyTime(personId, personPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePersonPropertyId(personPropertyId);
			return propertyManager.getPersonPropertyValue(personId, personPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			return personLocationManger.getPersonRegion(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPersonRegionArrivalTime(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePersonRegionArrivalsTimesTracked();
			return personLocationManger.getPersonRegionArrivalTime(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getPersonRegionArrivalTrackingPolicy();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validateResourceId(resourceId);
			return resourceManager.getPersonResourceLevel(resourceId, personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPersonResourceTime(final PersonId personId, final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validateResourceId(resourceId);
			validatePersonResourceTimesTracked(resourceId);
			return resourceManager.getPersonResourceTime(resourceId, personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			return resourceManager.getPersonResourceTimeTrackingPolicy(resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> Optional<T> getPlan(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePlanKeyNotNull(key);
			final T t = eventManager.getPlan(key);
			if (t == null) {
				return Optional.empty();
			}
			return Optional.of(t);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPlanTime(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePlanKeyNotNull(key);
			return eventManager.getPlanTime(key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getPopulationCount() {
		externalAccessManager.acquireReadAccess();
		try {
			return personLocationManger.getPopulationCount();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getPopulationTime() {
		externalAccessManager.acquireReadAccess();
		try {
			return personLocationManger.getPopulationTime();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomIndexedPerson(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			final PersonId personId = indexedPopulationManager.getRandomIndexedPerson(null, key);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomIndexedPersonWithExclusion(final PersonId excludedPersonId, final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			validatePersonExists(excludedPersonId);
			final PersonId personId = indexedPopulationManager.getRandomIndexedPerson(excludedPersonId, key);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomIndexedPersonFromGenerator(final Object key,
			RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = indexedPopulationManager.getRandomIndexedPersonFromGenerator(null, key,
					randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomIndexedPersonWithExclusionFromGenerator(final PersonId excludedPersonId,
			final Object key, RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			validatePersonExists(excludedPersonId);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = indexedPopulationManager.getRandomIndexedPersonFromGenerator(excludedPersonId,
					key, randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Set<RegionId> getRegionIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getRegionIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public MapOption getRegionMapOption() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getRegionMapOption();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public int getRegionPopulationCount(final RegionId regionId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			return personLocationManger.getRegionPopulationCount(regionId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getRegionPopulationCountTime(final RegionId regionId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			return personLocationManger.getRegionPopulationTime(regionId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionPropertyId(regionPropertyId);
			return propertyDefinitionManager.getRegionPropertyDefinition(regionPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return propertyDefinitionManager.getRegionPropertyIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			validateRegionPropertyId(regionPropertyId);
			return propertyManager.getRegionPropertyTime(regionId, regionPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			validateRegionPropertyId(regionPropertyId);
			return propertyManager.getRegionPropertyValue(regionId, regionPropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			validateResourceId(resourceId);
			return resourceManager.getRegionResourceLevel(regionId, resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRegionId(regionId);
			validateResourceId(resourceId);
			return resourceManager.getRegionResourceTime(regionId, resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public ReplicationId getReplicationId() {
		externalAccessManager.acquireReadAccess();
		try {
			return replicationId;
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends ResourceId> Set<T> getResourceIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getResourceIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			validateResourcePropertyId(resourceId, resourcePropertyId);
			return propertyDefinitionManager.getResourcePropertyDefinition(resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		externalAccessManager.acquireReadAccess();
		try {
			return propertyDefinitionManager.getResourcePropertyIds(resourceId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			validateResourcePropertyId(resourceId, resourcePropertyId);
			return propertyManager.getResourcePropertyTime(resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateResourceId(resourceId);
			validateResourcePropertyId(resourceId, resourcePropertyId);
			return propertyManager.getResourcePropertyValue(resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public ScenarioId getScenarioId() {
		externalAccessManager.acquireReadAccess();
		try {
			return scenarioId;
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<BatchId> getStageBatches(final StageId stageId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateStageId(stageId);
			return materialsManager.getStageBatches(stageId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateStageId(stageId);
			validateMaterialId(materialId);
			return materialsManager.getStageBatchesByMaterialId(stageId, materialId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateStageId(stageId);
			return materialsManager.getStageProducer(stageId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			return materialsManager.getStages(materialsProducerId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public double getTime() {
		externalAccessManager.acquireReadAccess();
		try {
			return eventManager.getTime();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public boolean groupExists(final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			return personGroupManger.groupExists(groupId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public void halt() {
		externalAccessManager.acquireWriteAccess();
		try {
			mutationResolver.halt();
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	/*
	 * Initializes(loads into local data structures) the scenario and replication
	 * data as well as constructs the simulations components using the
	 * ComponentFactory
	 */
	@Override
	public void init(final Context context) {
		super.init(context);
		this.context = context;
		profileManager = context.getProfileManager();
		outputItemManager = context.getOutputItemManager();
		externalAccessManager = context.getExternalAccessManager();
		componentManager = context.getComponentManager();
		eventManager = context.getEventManager();
		indexedPopulationManager = context.getIndexedPopulationManager();
		populationPartitionManager = context.getPopulationPartitionManager();

		observableEnvironment = context.getObservableEnvironment();
		propertyDefinitionManager = context.getPropertyDefinitionsManager();
		resourceManager = context.getResourceManager();
		propertyManager = context.getPropertyManager();
		personLocationManger = context.getPersonLocationManger();
		personGroupManger = context.getPersonGroupManger();

		materialsManager = context.getMaterialsManager();
		personIdManager = context.getPersonIdManager();
		mutationResolver = context.getMutationResolver();

		// initialize data structures that aid with input validation
		scenario = context.getScenario();
		scenarioId = scenario.getScenarioId();
		replicationId = context.getReplication().getId();

		regionIds = scenario.getRegionIds();
		materialsProducerIds = scenario.getMaterialsProducerIds();
		compartmentIds = scenario.getCompartmentIds();
		resourceIds = scenario.getResourceIds();
		randomNumberGeneratorIds = scenario.getRandomNumberGeneratorIds();
		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			groupTypeIds.put(groupTypeId, scenario.getGroupPropertyIds(groupTypeId));
		}
		for (final MaterialId materialId : scenario.getMaterialIds()) {
			materialIds.put(materialId, scenario.getBatchPropertyIds(materialId));
		}
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			compartmentPropertyIds.put(compartmentId, scenario.getCompartmentPropertyIds(compartmentId));
		}
		globalPropertyIds = scenario.getGlobalPropertyIds();
		resourcePropertyIdsMap = new LinkedHashMap<>();
		for (ResourceId resourceId : scenario.getResourceIds()) {
			Set<ResourcePropertyId> resourcePropertyIds = scenario.getResourcePropertyIds(resourceId);
			resourcePropertyIdsMap.put(resourceId, resourcePropertyIds);
		}
		regionPropertyIds = scenario.getRegionPropertyIds();
		personPropertyIds = scenario.getPersonPropertyIds();
		materialsProducerPropertyIds = scenario.getMaterialsProducerPropertyIds();
	}

	@Override
	public boolean isGroupMember(final PersonId personId, final GroupId groupId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validateGroupExists(groupId);
			return personGroupManger.isGroupMember(groupId, personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public boolean isStageOffered(final StageId stageId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateStageId(stageId);
			return materialsManager.isStageOffered(stageId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public void moveBatchToInventory(final BatchId batchId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateBatchId(batchId);
			final MaterialsProducerId materialsProducerId = materialsManager.getBatchProducer(batchId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			validateBatchIsStaged(batchId);
			final StageId stageId = materialsManager.getBatchStageId(batchId);
			validateStageIsNotOffered(stageId);
			mutationResolver.moveBatchToInventory(batchId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void moveBatchToStage(final BatchId batchId, final StageId stageId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateBatchId(batchId);
			validateBatchIsNotStaged(batchId);
			validateStageId(stageId);
			validateStageIsNotOffered(stageId);
			validateBatchAndStageOwnersMatch(batchId, stageId);
			final MaterialsProducerId materialsProducerId = materialsManager.getBatchProducer(batchId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			mutationResolver.moveBatchToStage(batchId, stageId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateCompartmentId(compartmentId);
			validatePersonPropertyId(personPropertyId);
			mutationResolver.observeCompartmentalPersonPropertyChange(observe, compartmentId, personPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId,
			final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateCompartmentId(compartmentId);
			validateResourceId(resourceId);
			mutationResolver.observeCompartmentalPersonResourceChange(observe, compartmentId, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeCompartmentPersonArrival(final boolean observe, final CompartmentId compartmentId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateCompartmentId(compartmentId);
			mutationResolver.observeCompartmentPersonArrival(observe, compartmentId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeCompartmentPersonDeparture(final boolean observe, final CompartmentId compartmentId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateCompartmentId(compartmentId);
			mutationResolver.observeCompartmentPersonDeparture(observe, compartmentId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateCompartmentId(compartmentId);
			validateCompartmentProperty(compartmentId, compartmentPropertyId);
			mutationResolver.observeCompartmentPropertyChange(observe, compartmentId, compartmentPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalPersonArrival(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGlobalPersonArrival(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalPersonDeparture(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGlobalPersonDeparture(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonPropertyId(personPropertyId);
			mutationResolver.observeGlobalPersonPropertyChange(observe, personPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateResourceId(resourceId);
			mutationResolver.observeGlobalPersonResourceChange(observe, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGlobalPropertyId(globalPropertyId);
			mutationResolver.observeGlobalPropertyChange(observe, globalPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeMaterialsProducerPropertyChange(final boolean observe,
			final MaterialsProducerId materialProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateMaterialsProducerId(materialProducerId);
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			mutationResolver.observeMaterialsProducerPropertyChange(observe, materialProducerId,
					materialsProducerPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeMaterialsProducerResourceChangeByMaterialsProducerId(final boolean observe,
			final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateMaterialsProducerId(materialsProducerId);
			validateResourceId(resourceId);
			mutationResolver.observeMaterialsProducerResourceChangeByMaterialsProducerId(observe, materialsProducerId,
					resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateResourceId(resourceId);
			mutationResolver.observeMaterialsProducerResourceChange(observe, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observePersonCompartmentChange(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateComponentHasFocus();
			mutationResolver.observePersonCompartmentChange(observe, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observePersonPropertyChange(final boolean observe, final PersonId personId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonExists(personId);
			validatePersonPropertyId(personPropertyId);
			mutationResolver.observePersonPropertyChange(observe, personId, personPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observePersonRegionChange(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonExists(personId);
			mutationResolver.observePersonRegionChange(observe, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observePersonResourceChange(final boolean observe, final PersonId personId,
			final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonExists(personId);
			validateResourceId(resourceId);
			mutationResolver.observePersonResourceChange(observe, personId, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionPersonArrival(final boolean observe, final RegionId regionId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateRegionId(regionId);
			mutationResolver.observeRegionPersonArrival(observe, regionId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionPersonDeparture(final boolean observe, final RegionId regionId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateRegionId(regionId);
			mutationResolver.observeRegionPersonDeparture(observe, regionId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId,
			final PersonPropertyId personPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateRegionId(regionId);
			validatePersonPropertyId(personPropertyId);
			mutationResolver.observeRegionPersonPropertyChange(observe, regionId, personPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateRegionId(regionId);
			validateResourceId(resourceId);
			mutationResolver.observeRegionPersonResourceChange(observe, regionId, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateRegionPropertyId(regionPropertyId);
			mutationResolver.observeGlobalRegionPropertyChange(observe, regionPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId,
			final RegionPropertyId regionPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateRegionId(regionId);
			validateRegionPropertyId(regionPropertyId);
			mutationResolver.observeRegionPropertyChange(observe, regionId, regionPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeRegionResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateRegionId(regionId);
			validateResourceId(resourceId);
			mutationResolver.observeRegionResourceChange(observe, regionId, resourceId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateResourceId(resourceId);
			validateResourcePropertyId(resourceId, resourcePropertyId);
			mutationResolver.observeResourcePropertyChange(observe, resourceId, resourcePropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeStageOfferChange(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeStageOfferChange(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateStageId(stageId);
			materialsManager.stageExists(stageId);
			mutationResolver.observeStageOfferChangeByStageId(observe, stageId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void observeStageTransfer(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeStageTransfer(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeStageTransferByStageId(final boolean observe, final StageId stageId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateStageId(stageId);
			mutationResolver.observeStageTransferByStageId(observe, stageId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public boolean personExists(final PersonId personId) {
		externalAccessManager.acquireReadAccess();
		try {
			return personIdManager.personExists(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public boolean personIsInPopulationIndex(final PersonId personId, final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			return indexedPopulationManager.personInPopulationIndex(personId, key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public boolean populationIndexExists(final Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			return indexedPopulationManager.populationIndexExists(key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public void removeGroup(final GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateGroupExists(groupId);
			validateFocalComponent(true, true, true, false, null, null, null);
			mutationResolver.removeGroup(groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void removePerson(final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(false, false, false, false, null, compartmentId, null);
			mutationResolver.removePerson(personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void removePersonFromGroup(final PersonId personId, final GroupId groupId) {
		/*
		 * Set the mutation type state for error reporting
		 */
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateFocalComponent(true, true, true, false, null, null, null);
			validateGroupExists(groupId);
			validatePersonInGroup(personId, groupId);
			mutationResolver.removePersonFromGroup(personId, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public <T> Optional<T> removePlan(final Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePlanKeyNotNull(key);
			return mutationResolver.removePlan(key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void removePopulationIndex(final Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			validatePopulationIndexIsOwnedByFocalComponent(key);
			mutationResolver.removePopulationIndex(componentManager.getFocalComponentId(), key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void removeResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			validatePersonHasSufficientResources(resourceId, personId, amount);
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(true, false, false, false, regionId, compartmentId, null);
			mutationResolver.removeResourceFromPerson(resourceId, personId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void removeResourceFromRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateRegionId(regionId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			validateRegionHasSufficientResources(resourceId, regionId, amount);
			validateFocalComponent(true, false, false, false, regionId, null, null);
			mutationResolver.removeResourceFromRegion(resourceId, regionId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId,
			final Object batchPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateBatchId(batchId);
			final MaterialId materialId = materialsManager.getBatchMaterial(batchId);
			validateBatchPropertyId(materialId, batchPropertyId);
			final MaterialsProducerId materialsProducerId = materialsManager.getBatchProducer(batchId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getBatchPropertyDefinition(materialId, batchPropertyId);
			validatePropertyMutability(propertyDefinition);
			validateBatchPropertyValueNotNull(batchPropertyValue);
			validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
			validateBatchIsNotOnOfferedStage(batchId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			mutationResolver.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setCompartmentPropertyValue(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateCompartmentId(compartmentId);
			validateCompartmentProperty(compartmentId, compartmentPropertyId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
			validatePropertyMutability(propertyDefinition);
			validateCompartmentPropertyValueNotNull(compartmentPropertyValue);
			validateValueCompatibility(compartmentPropertyId, propertyDefinition, compartmentPropertyValue);
			validateFocalComponent(true, false, false, false, null, compartmentId, null);
			mutationResolver.setCompartmentPropertyValue(compartmentId, compartmentPropertyId,
					compartmentPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateGlobalPropertyId(globalPropertyId);
			validateGlobalPropertyValueNotNull(globalPropertyValue);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getGlobalPropertyDefinition(globalPropertyId);
			validatePropertyMutability(propertyDefinition);
			validateValueCompatibility(globalPropertyId, propertyDefinition, globalPropertyValue);
			validateFocalComponent(true, false, false, false, null, null, null);
			mutationResolver.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId,
			final Object groupPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateGroupExists(groupId);
			final GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
			validatePropertyMutability(propertyDefinition);
			validateGroupPropertyValueNotNull(groupPropertyValue);
			validateValueCompatibility(groupPropertyId, propertyDefinition, groupPropertyValue);
			validateFocalComponent(true, true, true, false, null, null, null);
			mutationResolver.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateMaterialsProducerId(materialsProducerId);
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			validatePropertyMutability(propertyDefinition);
			validateMaterialProducerPropertyValueNotNull(materialsProducerPropertyValue);
			validateValueCompatibility(materialsProducerPropertyId, propertyDefinition, materialsProducerPropertyValue);
			validateFocalComponent(true, false, false, false, null, null, materialsProducerId);
			mutationResolver.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId,
					materialsProducerPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateCompartmentId(compartmentId);
			validatePersonNotInCompartment(personId, compartmentId);
			final CompartmentId currentCompartment = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(true, false, false, false, null, currentCompartment, null);
			final PersonId cleanedPersonId = personIdManager.getCleanedPersonId(personId);
			mutationResolver.setPersonCompartment(cleanedPersonId, compartmentId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validatePersonPropertyId(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getPersonPropertyDefinition(personPropertyId);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
			validatePropertyMutability(propertyDefinition);
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(true, false, false, false, regionId, compartmentId, null);
			final PersonId cleanedPersonId = personIdManager.getCleanedPersonId(personId);
			mutationResolver.setPersonPropertyValue(cleanedPersonId, personPropertyId, personPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateRegionId(regionId);
			validatePersonNotInRegion(personId, regionId);
			final RegionId currentRegionId = personLocationManger.getPersonRegion(personId);
			validateFocalComponent(true, false, false, false, currentRegionId, null, null);
			final PersonId cleanedPersonId = personIdManager.getCleanedPersonId(personId);
			mutationResolver.setPersonRegion(cleanedPersonId, regionId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId,
			final Object regionPropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateRegionId(regionId);
			validateRegionPropertyId(regionPropertyId);
			validateRegionPropertyValueNotNull(regionPropertyValue);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getRegionPropertyDefinition(regionPropertyId);
			validateValueCompatibility(regionPropertyId, propertyDefinition, regionPropertyValue);
			validatePropertyMutability(propertyDefinition);
			validateFocalComponent(true, false, false, false, regionId, null, null);
			mutationResolver.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,
			final Object resourcePropertyValue) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateResourceId(resourceId);
			validateResourcePropertyId(resourceId, resourcePropertyId);
			validateResourcePropertyValueNotNull(resourcePropertyValue);
			final PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getResourcePropertyDefinition(resourceId, resourcePropertyId);
			validateValueCompatibility(resourcePropertyId, propertyDefinition, resourcePropertyValue);
			validatePropertyMutability(propertyDefinition);
			validateFocalComponent(true, false, false, false, null, null, null);
			mutationResolver.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void setStageOffer(final StageId stageId, final boolean offer) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateStageId(stageId);
			final MaterialsProducerId materialsProducerId = materialsManager.getStageProducer(stageId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);
			mutationResolver.setStageOffer(stageId, offer);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	private void validateMaterialAdditionValue(final double currentResourceLevel, final double amount) {
		if (!Double.isFinite(currentResourceLevel + amount)) {
			throwModelException(SimulationErrorType.MATERIAL_ARITHMETIC_EXCEPTION);
		}
	}

	@Override
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateBatchId(sourceBatchId);
			validateBatchId(destinationBatchId);
			validateDifferentBatchesForShift(sourceBatchId, destinationBatchId);
			validateMaterialsMatchForShift(sourceBatchId, destinationBatchId);
			validateProducersMatchForShift(sourceBatchId, destinationBatchId);
			validateBatchIsNotOnOfferedStage(sourceBatchId);
			validateBatchIsNotOnOfferedStage(destinationBatchId);
			validateNonnegativeFiniteMaterialAmount(amount);
			validateBatchHasSufficientMaterial(sourceBatchId, amount);
			double currentDestinationBatchAmount = materialsManager.getBatchAmount(destinationBatchId);
			validateMaterialAdditionValue(currentDestinationBatchAmount, amount);
			final MaterialsProducerId materialsProducerId = materialsManager.getBatchProducer(sourceBatchId);
			validateFocalComponent(false, false, false, false, null, null, materialsProducerId);

			mutationResolver.shiftBatchContent(sourceBatchId, destinationBatchId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public boolean stageExists(final StageId stageId) {
		externalAccessManager.acquireReadAccess();
		try {
			return materialsManager.stageExists(stageId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	private void throwModelException(final SimulationErrorType simulationErrorType) {
		throwModelException(simulationErrorType, null);
	}

	private void throwModelException(final SimulationErrorType simulationErrorType, final Object details) {
		final StringBuilder sb = new StringBuilder();

		sb.append("Active Component");
		sb.append("[");
		sb.append(componentManager.getFocalComponentType());
		sb.append(",");
		sb.append(componentManager.getFocalComponentId().toString());
		sb.append("]");
		sb.append(simulationErrorType.getDescription());

		if (details != null) {
			sb.append(": ");
			sb.append(details);
		}
		String errorDescription = sb.toString();

		throw new ModelException(simulationErrorType, errorDescription);
	}

	private void validateBatchPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_BATCH_PROPERTY_VALUE);
		}
	}

	private void validateCompartmentPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_COMPARTMENT_PROPERTY_VALUE);
		}
	}

	private void validateGlobalPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_GLOBAL_PROPERTY_VALUE);
		}
	}

	private void validateGroupPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_GROUP_PROPERTY_VALUE);
		}
	}

	private void validateMaterialProducerPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE);
		}
	}

	private void validatePersonPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_PERSON_PROPERTY_VALUE);
		}
	}

	private void validateRegionPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_REGION_PROPERTY_VALUE);
		}
	}

	private void validateResourcePropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throwModelException(SimulationErrorType.NULL_RESOURCE_PROPERTY_VALUE);
		}
	}

	@Override
	public void transferOfferedStageToMaterialsProducer(final StageId stageId,
			final MaterialsProducerId materialsProducerId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateStageId(stageId);
			validateMaterialsProducerId(materialsProducerId);
			validateStageIsOffered(stageId);
			validateStageNotOwnedByReceivingMaterialsProducer(stageId, materialsProducerId);
			validateFocalComponent(true, false, false, true, null, null, null);
			mutationResolver.transferOfferedStageToMaterialsProducer(stageId, materialsProducerId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void transferProducedResourceToRegion(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final RegionId regionId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateResourceId(resourceId);
			validateRegionId(regionId);
			validateMaterialsProducerId(materialsProducerId);
			validateNonnegativeResourceAmount(amount);
			validateMaterialsProducerHasSufficientResource(materialsProducerId, resourceId, amount);
			validateFocalComponent(true, false, false, false, regionId, null, materialsProducerId);
			long currentResourceLevel = resourceManager.getRegionResourceLevel(regionId, resourceId);
			validateResourceAdditionValue(currentResourceLevel, amount);
			mutationResolver.transferProducedResourceToRegion(materialsProducerId, resourceId, regionId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void transferResourceBetweenRegions(final ResourceId resourceId, final RegionId sourceRegionId,
			final RegionId destinationRegionId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateRegionId(sourceRegionId);
			validateRegionId(destinationRegionId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			validateDifferentRegionsForResourceTransfer(sourceRegionId, destinationRegionId);
			validateRegionHasSufficientResources(resourceId, sourceRegionId, amount);
			long regionResourceLevel = resourceManager.getRegionResourceLevel(destinationRegionId, resourceId);
			validateResourceAdditionValue(regionResourceLevel, amount);
			validateFocalComponent(true, false, false, false, null, null, null);
			mutationResolver.transferResourceBetweenRegions(resourceId, sourceRegionId, destinationRegionId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void transferResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			validatePersonHasSufficientResources(resourceId, personId, amount);
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			long regionResourceLevel = resourceManager.getRegionResourceLevel(regionId, resourceId);
			validateResourceAdditionValue(regionResourceLevel, amount);
			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(true, false, false, false, regionId, compartmentId, null);
			final PersonId cleanedPersonId = personIdManager.getCleanedPersonId(personId);
			mutationResolver.transferResourceFromPerson(resourceId, cleanedPersonId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void transferResourceToPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePersonExists(personId);
			validateResourceId(resourceId);
			validateNonnegativeResourceAmount(amount);
			final RegionId regionId = personLocationManger.getPersonRegion(personId);
			validateRegionHasSufficientResources(resourceId, regionId, amount);
			final CompartmentId compartmentId = personLocationManger.getPersonCompartment(personId);
			validateFocalComponent(true, false, false, false, regionId, compartmentId, null);
			final PersonId cleanedPersonId = personIdManager.getCleanedPersonId(personId);
			long personResourceLevel = resourceManager.getPersonResourceLevel(resourceId, cleanedPersonId);
			validateResourceAdditionValue(personResourceLevel, amount);
			mutationResolver.transferResourceToPerson(resourceId, cleanedPersonId, amount);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	private void validateBatchAndStageOwnersMatch(final BatchId batchId, final StageId stageId) {
		final MaterialsProducerId batchProducerId = materialsManager.getBatchProducer(batchId);
		final MaterialsProducerId stageProducerId = materialsManager.getStageProducer(stageId);
		if (!batchProducerId.equals(stageProducerId)) {
			throwModelException(SimulationErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER);
		}
	}

	private void validateBatchHasSufficientMaterial(final BatchId batchId, final double amount) {
		if (materialsManager.getBatchAmount(batchId) < amount) {
			throwModelException(SimulationErrorType.INSUFFICIENT_MATERIAL_AVAILABLE);
		}
	}

	private void validateBatchId(final BatchId batchId) {

		if (batchId == null) {
			throwModelException(SimulationErrorType.NULL_BATCH_ID);
		}

		if (!materialsManager.batchExists(batchId)) {
			throwModelException(SimulationErrorType.UNKNOWN_BATCH_ID, batchId);
		}
	}

	private void validateBatchIsNotStaged(final BatchId batchId) {
		if (materialsManager.getBatchStageId(batchId) != null) {
			throwModelException(SimulationErrorType.BATCH_ALREADY_STAGED);
		}
	}

	private void validateBatchIsStaged(final BatchId batchId) {
		final StageId stageId = materialsManager.getBatchStageId(batchId);
		if (stageId == null) {
			throwModelException(SimulationErrorType.BATCH_NOT_STAGED);
		}
	}

	private void validateBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_BATCH_PROPERTY_ID);
		}
		final Set<BatchPropertyId> batchPropertyIds = materialIds.get(materialId);
		if (!batchPropertyIds.contains(batchPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_BATCH_PROPERTY_ID);
		}

	}

	private void validateBiWeightingFunctionNotNull(final BiWeightingFunction biWeightingFunction) {
		if (biWeightingFunction == null) {
			throwModelException(SimulationErrorType.NULL_WEIGHTING_FUNCTION);
		}
	}

	/*
	 * Validates the compartment id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if the compartment id is
	 * null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID} if the compartment id
	 * does not correspond to a known compartment
	 */
	private void validateCompartmentId(final CompartmentId compartmentId) {
		if (compartmentId == null) {
			throwModelException(SimulationErrorType.NULL_COMPARTMENT_ID);
		}

		if (!compartmentIds.contains(compartmentId)) {
			throwModelException(SimulationErrorType.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validateEquality(final Equality equality) {
		if (equality == null) {
			throwModelException(SimulationErrorType.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateCompartmentProperty(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId) {

		if (compartmentPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_COMPARTMENT_PROPERTY_ID);
		}
		Set<CompartmentPropertyId> set = compartmentPropertyIds.get(compartmentId);
		if (set == null || !set.contains(compartmentPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		}

	}

	private void validateComponentHasFocus() {
		if (componentManager.getFocalComponent() == null) {
			throwModelException(SimulationErrorType.NO_ACTIVE_COMPONENT);
		}
	}

	private void validateDifferentBatchesForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		if (sourceBatchId.equals(destinationBatchId)) {
			throwModelException(SimulationErrorType.REFLEXIVE_BATCH_SHIFT);
		}

	}

	private void validateDifferentRegionsForResourceTransfer(final RegionId sourceRegionId,
			final RegionId destinationRegionId) {
		if (sourceRegionId.equals(destinationRegionId)) {
			throwModelException(SimulationErrorType.REFLEXIVE_RESOURCE_TRANSFER);
		}
	}

	private void validateFilterInfo(final FilterInfo filterInfo) {
		switch (filterInfo.getFilterInfoType()) {
		case ALL:
			// nothing to validate
			break;
		case AND:
			// do nothing -- FilterInfo.class guarantees that AND will function
			// correctly
			break;
		case COMPARTMENT:
			CompartmentFilterInfo compartmentFilterInfo = (CompartmentFilterInfo) filterInfo;
			validateCompartmentId(compartmentFilterInfo.getCompartmentId());
			break;
		case EMPTY:
			// nothing to validate
			break;
		case GROUPS_FOR_PERSON:
			GroupsForPersonFilterInfo groupsForPersonFilterInfo = (GroupsForPersonFilterInfo) filterInfo;
			validateEquality(groupsForPersonFilterInfo.getEquality());
			break;
		case GROUPS_FOR_PERSON_AND_GROUP_TYPE:
			GroupsForPersonAndGroupTypeFilterInfo groupsForPersonAndGroupTypeFilterInfo = (GroupsForPersonAndGroupTypeFilterInfo) filterInfo;
			validateEquality(groupsForPersonAndGroupTypeFilterInfo.getEquality());
			validateGroupTypeId(groupsForPersonAndGroupTypeFilterInfo.getGroupTypeId());
			break;
		case GROUP_MEMBER:
			GroupMemberFilterInfo groupMemberFilterInfo = (GroupMemberFilterInfo) filterInfo;
			validateGroupExists(groupMemberFilterInfo.getGroupId());
			break;
		case GROUP_TYPES_FOR_PERSON:
			GroupTypesForPersonFilterInfo groupTypesForPersonFilterInfo = (GroupTypesForPersonFilterInfo) filterInfo;
			validateEquality(groupTypesForPersonFilterInfo.getEquality());
			break;
		case NEGATE:
			// do nothing -- FilterInfo.class guarantees that NEGATE will
			// function
			// correctly
			break;
		case OR:
			// do nothing -- FilterInfo.class guarantees that OR will function
			// correctly
			break;
		case PROPERTY:
			PropertyFilterInfo propertyFilterInfo = (PropertyFilterInfo) filterInfo;
			PersonPropertyId personPropertyId = propertyFilterInfo.getPersonPropertyId();
			Equality equality = propertyFilterInfo.getEquality();
			Object personPropertyValue = propertyFilterInfo.getPersonPropertyValue();

			validatePersonPropertyId(personPropertyId);
			validateEquality(equality);
			validatePersonPropertyValueNotNull(personPropertyValue);

			PropertyDefinition propertyDefinition = propertyDefinitionManager
					.getPersonPropertyDefinition(personPropertyId);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
			validateEqualityCompatibility(personPropertyId, propertyDefinition, equality);
			break;
		case REGION:
			RegionFilterInfo regionFilterInfo = (RegionFilterInfo) filterInfo;
			regionFilterInfo.getRegionIds().forEach(regionId -> validateRegionId(regionId));
			break;
		case RESOURCE:
			ResourceFilterInfo resourceFilterInfo = (ResourceFilterInfo) filterInfo;
			validateResourceId(resourceFilterInfo.getResourceId());
			validateEquality(resourceFilterInfo.getEquality());
			break;
		default:
			break;
		}
	}

	private void validateFilter(final Filter filter) {
		if (filter == null) {
			throwModelException(SimulationErrorType.NULL_FILTER);
		}

		/*
		 * Get a FilterInfo for the filter, decompose it into all its individual children
		 * and validate each child
		 */
		FilterInfo.getHierarchyAsList(FilterInfo.build(filter)).forEach(this::validateFilterInfo);
	}

	private void validateFocalComponent(final boolean globalAllowed, final boolean allRegionsAllowed,
			final boolean allCompartmentsAllowed, final boolean allMaterialsProducersAllowed, final RegionId regionId,
			final CompartmentId compartmentId, final MaterialsProducerId materialsProducerId) {
		/*
		 * Determine the type of the current focus
		 */
		final ComponentType componentType = componentManager.getFocalComponentType();

		final ComponentId focalComponentId = componentManager.getFocalComponentId();

		/*
		 * Determine if the current focus matches one of the focal options
		 */
		boolean compatibleFocusFound;
		switch (componentType) {
		case SIM:
			compatibleFocusFound = true;
			break;
		case GLOBAL:
			compatibleFocusFound = globalAllowed;
			break;
		case REGION:
			compatibleFocusFound = allRegionsAllowed || focalComponentId.equals(regionId);
			break;
		case COMPARTMENT:
			compatibleFocusFound = allCompartmentsAllowed || focalComponentId.equals(compartmentId);
			break;
		case MATERIALS_PRODUCER:
			compatibleFocusFound = allMaterialsProducersAllowed || focalComponentId.equals(materialsProducerId);
			break;
		case INTERNAL:
			compatibleFocusFound = false;
			break;
		default:
			throw new RuntimeException("Unknown Focal Type: " + componentType.name());
		}

		if (!compatibleFocusFound) {
			final StringBuilder sb = new StringBuilder();
			sb.append("The current focal component, ");
			sb.append(focalComponentId);
			sb.append(", is not one of the components allowed to perform this action.  The allowed componenets are  ");

			List<String> candidates = new ArrayList<>();
			if (globalAllowed) {
				candidates.add("any global component");
			}
			if (allRegionsAllowed) {
				candidates.add("any region");
			} else {
				if (regionId != null) {
					candidates.add("the region " + regionId);
				}
			}
			if (allCompartmentsAllowed) {
				candidates.add("any compartment");

			} else {
				if (compartmentId != null) {
					candidates.add("the compartment " + compartmentId);
				}
			}
			if (allMaterialsProducersAllowed) {
				candidates.add("any materials producer");

			} else {
				if (materialsProducerId != null) {
					candidates.add("the materials producer " + materialsProducerId);
				}
			}

			for (int i = 0; i < candidates.size(); i++) {
				String candidate = candidates.get(i);
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(candidate);
			}

			throwModelException(SimulationErrorType.COMPONENT_LACKS_PERMISSION, sb.toString());
		}
	}

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalPropertyIds.contains(globalPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

	/**
	 * Validates that the group exists
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group does not exist
	 *
	 *
	 */
	private void validateGroupExists(final GroupId groupId) {
		if (groupId == null) {
			throwModelException(SimulationErrorType.NULL_GROUP_ID);
		}
		if (!personGroupManger.groupExists(groupId)) {
			throwModelException(SimulationErrorType.UNKNOWN_GROUP_ID);
		}
	}

	private void validateGroupPropertyId(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_GROUP_PROPERTY_ID);
		}

		final Set<GroupPropertyId> groupPropertyIds = groupTypeIds.get(groupTypeId);
		if (!groupPropertyIds.contains(groupPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_GROUP_PROPERTY_ID);
		}
	}

	/**
	 * Validates the group type id
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group type id is null
	 *
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type id is unknown
	 */
	private void validateGroupTypeId(final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			throwModelException(SimulationErrorType.NULL_GROUP_TYPE_ID);
		}

		if (!groupTypeIds.containsKey(groupTypeId)) {
			throwModelException(SimulationErrorType.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}

	}

	/*
	 * Validates a material id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if the material id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if the material id does
	 * not correspond to a known material
	 */
	private void validateMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throwModelException(SimulationErrorType.NULL_MATERIAL_ID);
		}

		if (!materialIds.containsKey(materialId)) {
			throwModelException(SimulationErrorType.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private void validateMaterialsMatchForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialId sourceMaterialId = materialsManager.getBatchMaterial(sourceBatchId);
		final MaterialId destinationMaterialId = materialsManager.getBatchMaterial(destinationBatchId);

		if (!sourceMaterialId.equals(destinationMaterialId)) {
			throwModelException(SimulationErrorType.MATERIAL_TYPE_MISMATCH);
		}

	}

	private void validateMaterialsProducerHasSufficientResource(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final long amount) {
		final long materialsProducerResourceLevel = resourceManager
				.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		if (materialsProducerResourceLevel < amount) {
			throwModelException(SimulationErrorType.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	/*
	 * Validates a materials producer id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID} if the materials
	 * Producer id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 * materials Producer id does not correspond to a known material producer
	 */
	private void validateMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throwModelException(SimulationErrorType.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsProducerIds.contains(materialsProducerId)) {
			throwModelException(SimulationErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateMaterialsProducerPropertyId(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}
		if (!materialsProducerPropertyIds.contains(materialsProducerPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID,
					materialsProducerPropertyId);
		}
	}

	private void validateMonoWeightingFunctionNotNull(final MonoWeightingFunction monoWeightingFunction) {
		if (monoWeightingFunction == null) {
			throwModelException(SimulationErrorType.NULL_WEIGHTING_FUNCTION);
		}
	}

	private void validateNonnegativeFiniteMaterialAmount(final double amount) {
		if (!Double.isFinite(amount)) {
			throwModelException(SimulationErrorType.NON_FINITE_MATERIAL_AMOUNT);
		}
		if (amount < 0) {
			throwModelException(SimulationErrorType.NEGATIVE_MATERIAL_AMOUNT);
		}
	}

	private void validateNonnegativeResourceAmount(final long amount) {
		if (amount < 0) {
			throwModelException(SimulationErrorType.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	private void validateResourceAdditionValue(final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (ArithmeticException e) {
			throwModelException(SimulationErrorType.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	private void validatePersonCompartmentArrivalsTimesTracked() {
		if (scenario.getPersonCompartmentArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throwModelException(SimulationErrorType.COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throwModelException(SimulationErrorType.NULL_PERSON_ID);
		}
		if (!personIdManager.personExists(personId)) {
			throwModelException(SimulationErrorType.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonHasSufficientResources(final ResourceId resourceId, final PersonId personId,
			final long amount) {
		final long oldValue = resourceManager.getPersonResourceLevel(resourceId, personId);
		if (oldValue < amount) {
			throwModelException(SimulationErrorType.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private void validatePersonInGroup(final PersonId personId, final GroupId groupId) {
		if (!personGroupManger.isGroupMember(groupId, personId)) {
			throwModelException(SimulationErrorType.NON_GROUP_MEMBERSHIP,
					"Person " + personId + " is not a member of group " + groupId);
		}
	}

	private void validatePersonNotInCompartment(final PersonId personId, final CompartmentId compartmentId) {
		final CompartmentId currentCompartmentId = personLocationManger.getPersonCompartment(personId);
		if (currentCompartmentId.equals(compartmentId)) {
			throwModelException(SimulationErrorType.SAME_COMPARTMENT, compartmentId);
		}
	}

	private void validatePersonNotInGroup(final PersonId personId, final GroupId groupId) {
		if (personGroupManger.isGroupMember(groupId, personId)) {
			throwModelException(SimulationErrorType.DUPLICATE_GROUP_MEMBERSHIP,
					"Person " + personId + " is already a member of group " + groupId);
		}
	}

	private void validatePersonNotInRegion(final PersonId personId, final RegionId regionId) {
		final RegionId currentRegionId = personLocationManger.getPersonRegion(personId);
		if (currentRegionId.equals(regionId)) {
			throwModelException(SimulationErrorType.SAME_REGION, regionId);
		}
	}

	private void validatePersonPropertyAssignmentTimesTracked(final PersonPropertyId personPropertyId) {
		final PropertyDefinition personPropertyDefinition = propertyDefinitionManager
				.getPersonPropertyDefinition(personPropertyId);
		if (personPropertyDefinition.getTimeTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throwModelException(SimulationErrorType.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validatePersonPropertyId(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_PERSON_PROPERTY_ID);
		}
		if (!personPropertyIds.contains(personPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}

	private void validatePersonRegionArrivalsTimesTracked() {
		if (scenario.getPersonRegionArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throwModelException(SimulationErrorType.REGION_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validatePersonResourceTimesTracked(final ResourceId resourceId) {
		if (scenario.getPersonResourceTimeTrackingPolicy(resourceId) != TimeTrackingPolicy.TRACK_TIME) {
			throwModelException(SimulationErrorType.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validatePlanKeyNotDuplicate(final Object key) {
		if (eventManager.getPlan(key) != null) {
			throwModelException(SimulationErrorType.DUPLICATE_PLAN_KEY);
		}
	}

	private void validatePlanKeyNotNull(final Object key) {
		if (key == null) {
			throwModelException(SimulationErrorType.NULL_PLAN_KEY);
		}
	}

	private void validatePlanNotNull(final Plan plan) {
		if (plan == null) {
			throwModelException(SimulationErrorType.NULL_PLAN);
		}
	}

	private void validateNewGlobalComponentId(GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			throwModelException(SimulationErrorType.NULL_GLOBAL_COMPONENT_ID, globalComponentId);
		}
		if (context.getScenario().getGlobalComponentIds().contains(globalComponentId)) {
			throwModelException(SimulationErrorType.GLOBAL_COMPONENT_ID_ALREADY_EXISTS);
		}
	}

	private void validateComponentClass(Class<? extends Component> globalComponentClass) {
		if (globalComponentClass == null) {
			throwModelException(SimulationErrorType.NULL_GLOBAL_COMPONENT_CLASS);
		}
	}

	private void validatePlanTime(final double planTime) {
		if (planTime < eventManager.getTime()) {
			throwModelException(SimulationErrorType.PAST_PLANNING_TIME);
		}
	}

	private void validatePopulationIndexDoesNotExist(final Object key) {
		if (indexedPopulationManager.populationIndexExists(key)) {
			throwModelException(SimulationErrorType.DUPLICATE_INDEXED_POPULATION, key);
		}
	}

	private void validatePopulationIndexExists(final Object key) {
		if (!indexedPopulationManager.populationIndexExists(key)) {
			throwModelException(SimulationErrorType.UNKNOWN_POPULATION_INDEX_KEY, key);
		}
	}

	private void validatePopulationIndexIsOwnedByFocalComponent(final Object key) {
		if (!indexedPopulationManager.getOwningComponent(key).equals(componentManager.getFocalComponentId())) {
			throwModelException(SimulationErrorType.INDEXED_POPULATION_DELETION_BY_NON_OWNER, key);
		}
	}

	private void validatePopulationIndexKeyNotNull(final Object key) {
		if (key == null) {
			throwModelException(SimulationErrorType.NULL_POPULATION_INDEX_KEY);
		}
	}

	private void validateProducersMatchForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialsProducerId sourceMaterialsProducerId = materialsManager.getBatchProducer(sourceBatchId);
		final MaterialsProducerId destinationMaterialsProducerId = materialsManager
				.getBatchProducer(destinationBatchId);

		if (!sourceMaterialsProducerId.equals(destinationMaterialsProducerId)) {
			throwModelException(SimulationErrorType.BATCH_SHIFT_WITH_MULTIPLE_OWNERS);
		}

	}

	private void validateRegionHasSufficientResources(final ResourceId resourceId, final RegionId regionId,
			final long amount) {
		final long currentAmount = resourceManager.getRegionResourceLevel(regionId, resourceId);
		if (currentAmount < amount) {
			throwModelException(SimulationErrorType.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	/*
	 * Validates the region id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_REGION_ID} if the region id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if the region id does not
	 * correspond to a known region
	 */
	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			throwModelException(SimulationErrorType.NULL_REGION_ID);
		}

		if (!regionIds.contains(regionId)) {
			throwModelException(SimulationErrorType.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throwModelException(SimulationErrorType.NULL_REGION_PROPERTY_ID);
		}
		if (!regionPropertyIds.contains(regionPropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
		}
	}

	/*
	 * Validates the resource id.
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if the resource id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if the resource id does
	 * not correspond to a known resource
	 */
	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			throwModelException(SimulationErrorType.NULL_RESOURCE_ID);
		}
		if (!resourceIds.contains(resourceId)) {
			throwModelException(SimulationErrorType.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Validates the random generator id.
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID} if the random
	 * number generator id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID} if the
	 * random number generator id does not correspond to a known random number
	 * generator id
	 */
	private void validateRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			throwModelException(SimulationErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		if (!randomNumberGeneratorIds.contains(randomNumberGeneratorId)) {
			throwModelException(SimulationErrorType.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}

	/*
	 * Assumes a valid resource id
	 */
	private void validateResourcePropertyId(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throwModelException(SimulationErrorType.NULL_RESOURCE_PROPERTY_ID);
		}

		Set<ResourcePropertyId> set = resourcePropertyIdsMap.get(resourceId);
		if (set == null) {
			throwModelException(SimulationErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}
		if (!set.contains(resourcePropertyId)) {
			throwModelException(SimulationErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}

	}

	/*
	 * Validates a stage id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_STAGE_ID} if the stage id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if the stage id does not
	 * correspond to a known stage
	 */
	private void validateStageId(final StageId stageId) {
		if (stageId == null) {
			throwModelException(SimulationErrorType.NULL_STAGE_ID);
		}

		if (!materialsManager.stageExists(stageId)) {
			throwModelException(SimulationErrorType.UNKNOWN_STAGE_ID, stageId);
		}
	}

	private void validateStageIsNotOffered(final StageId stageId) {
		if (materialsManager.isStageOffered(stageId)) {
			throwModelException(SimulationErrorType.OFFERED_STAGE_UNALTERABLE);
		}
	}

	private void validateBatchIsNotOnOfferedStage(final BatchId batchId) {
		StageId stageId = materialsManager.getBatchStageId(batchId);
		if (stageId != null) {
			if (materialsManager.isStageOffered(stageId)) {
				throwModelException(SimulationErrorType.OFFERED_STAGE_UNALTERABLE);
			}
		}
	}

	private void validateStageIsOffered(final StageId stageId) {
		if (!materialsManager.isStageOffered(stageId)) {
			throwModelException(SimulationErrorType.UNOFFERED_STAGE_NOT_TRANSFERABLE);
		}
	}

	private void validateStageNotOwnedByReceivingMaterialsProducer(final StageId stageId,
			final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerId stageProducer = materialsManager.getStageProducer(stageId);
		if (materialsProducerId.equals(stageProducer)) {
			throwModelException(SimulationErrorType.REFLEXIVE_STAGE_TRANSFER);
		}
	}

	private void validateStochasticPersonSelection(final StochasticPersonSelection stochasticPersonSelection) {
		if (stochasticPersonSelection.errorOccured()) {
			throwModelException(SimulationErrorType.MALFORMED_WEIGHTING_FUNCTION);
		}
	}

	@Override
	public <T extends ComponentId> T getCurrentComponentId() {
		return componentManager.getFocalComponentId();
	}

	@Override
	public void releaseOutputItem(OutputItem outputItem) {
		externalAccessManager.acquireReadAccess();
		try {
			validateOutputItem(outputItem);
			outputItemManager.releaseOutputItem(outputItem);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	private void validateOutputItem(OutputItem reportItem) {
		if (reportItem == null) {
			throwModelException(SimulationErrorType.NULL_OUTPUT_ITEM);
		}

		if (!reportItem.getScenarioId().equals(scenarioId)) {
			throwModelException(SimulationErrorType.INCORRECT_SCENARIO_ID_FOR_OUTPUT_ITEM);
		}

		if (!reportItem.getReplicationId().equals(replicationId)) {
			throwModelException(SimulationErrorType.INCORRECT_REPLICATION_ID_FOR_OUTPUT_ITEM);
		}

	}

	@Override
	public void observeGroupArrival(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGroupArrival(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonExists(personId);
			mutationResolver.observeGroupArrivalByPerson(observe, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			mutationResolver.observeGroupArrivalByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			mutationResolver.observeGroupArrivalByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			validatePersonExists(personId);
			mutationResolver.observeGroupArrivalByTypeAndPerson(observe, groupTypeId, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			validatePersonExists(personId);
			mutationResolver.observeGroupArrivalByGroupAndPerson(observe, groupId, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDeparture(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGroupDeparture(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePersonExists(personId);
			mutationResolver.observeGroupDepartureByPerson(observe, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			mutationResolver.observeGroupDepartureByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			mutationResolver.observeGroupDepartureByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			validatePersonExists(personId);
			mutationResolver.observeGroupDepartureByTypeAndPerson(observe, groupTypeId, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			validatePersonExists(personId);
			mutationResolver.observeGroupDepartureByGroupAndPerson(observe, groupId, personId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupConstruction(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGroupConstruction(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			mutationResolver.observeGroupConstructionByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDestruction(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGroupDestruction(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDestructionByGroup(final boolean observe, GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			mutationResolver.observeGroupDestructionByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			mutationResolver.observeGroupDestructionByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupPropertyChange(final boolean observe) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			mutationResolver.observeGroupPropertyChange(observe);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			mutationResolver.observeGroupPropertyChangeByType(observe, groupTypeId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupTypeId(groupTypeId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			mutationResolver.observeGroupPropertyChangeByTypeAndProperty(observe, groupTypeId, groupPropertyId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			mutationResolver.observeGroupPropertyChangeByGroup(observe, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupId groupId,
			final GroupPropertyId groupPropertyId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validateGroupExists(groupId);
			GroupTypeId groupTypeId = getGroupType(groupId);
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			mutationResolver.observeGroupPropertyChangeByGroupAndProperty(observe, groupPropertyId, groupId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public <T> T getProfiledProxy(T instance) {
		return profileManager.getProfiledProxy(instance);
	}

	@Override
	public int getSuggestedPopulationSize() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getSuggestedPopulationSize();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public List<Object> getPlanKeys() {
		externalAccessManager.acquireReadAccess();
		try {
			return eventManager.getPlanKeys();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public void observePopulationIndexChange(boolean observe, Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePopulationIndexKeyNotNull(key);
			validatePopulationIndexExists(key);
			mutationResolver.observePopulationIndexChange(observe, key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public void observeStageTransferBySourceMaterialsProducerId(boolean observe,
			MaterialsProducerId sourceMaterialsProducerId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateMaterialsProducerId(sourceMaterialsProducerId);
			validateComponentHasFocus();
			mutationResolver.observeStageTransferBySourceMaterialsProducerId(observe, sourceMaterialsProducerId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}

	}

	@Override
	public void observeStageTransferByDestinationMaterialsProducerId(boolean observe,
			MaterialsProducerId destinationMaterialsProducerId) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateMaterialsProducerId(destinationMaterialsProducerId);
			validateComponentHasFocus();
			mutationResolver.observeStageTransferByDestinationMaterialsProducerId(observe,
					destinationMaterialsProducerId);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getScenario().getRandomNumberGeneratorIds();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public RandomGenerator getRandomGenerator() {
		externalAccessManager.acquireReadAccess();
		try {
			return context.getStochasticsManager().getRandomGenerator();
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			return context.getStochasticsManager().getRandomGeneratorFromId(randomNumberGeneratorId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public void addGlobalComponent(GlobalComponentId globalComponentId,
			Class<? extends Component> globalComponentClass) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateNewGlobalComponentId(globalComponentId);
			validateComponentClass(globalComponentClass);
			mutationResolver.addGlobalComponent(globalComponentId, globalComponentClass);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	private void validatePopulationPartitionExists(final Object key) {
		if (!populationPartitionManager.populationPartitionExists(key)) {
			throwModelException(SimulationErrorType.UNKNOWN_POPULATION_PARTITION_KEY, key);
		}
	}

	private void validateLabelSet(Object key, LabelSet labelSet) {
		if (labelSet == null) {
			throwModelException(SimulationErrorType.NULL_LABEL_SET, key);
		}
		if (!populationPartitionManager.populationPartitionExists(key)) {
			throwModelException(SimulationErrorType.UNKNOWN_POPULATION_PARTITION_KEY);
		}
		
		if (!populationPartitionManager.validateLabelSet(key, labelSet)) {
			throwModelException(SimulationErrorType.INCOMPATIBLE_LABEL_SET, key);
		}
	}

	@Override
	public List<PersonId> getPartitionPeople(Object key, LabelSet labelSet) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			return populationPartitionManager.getPartitionPeople(key, labelSet);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	private void validatePopulationPartitionDefinition(
			final PopulationPartitionDefinition populationPartitionDefinition) {
		if (populationPartitionDefinition == null) {
			throwModelException(SimulationErrorType.NULL_POPULATION_PARTITION_DEFINITION);
		}

		for (PersonPropertyId personPropertyId : populationPartitionDefinition.getPersonPropertyIds()) {
			validatePersonPropertyId(personPropertyId);
		}

		for (ResourceId resourceId : populationPartitionDefinition.getPersonResourceIds()) {
			validateResourceId(resourceId);
		}

	}

	private void validatePopulationPartitionKeyNotNull(final Object key) {
		if (key == null) {
			throwModelException(SimulationErrorType.NULL_POPULATION_PARTITION_KEY);
		}
	}

	private void validatePopulationPartitionDoesNotExist(final Object key) {
		if (populationPartitionManager.populationPartitionExists(key)) {
			throwModelException(SimulationErrorType.DUPLICATE_POPULATION_PARTITION, key);
		}
	}

	@Override
	public void addPopulationPartition(PopulationPartitionDefinition populationPartitionDefinition, Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validateComponentHasFocus();
			validatePopulationPartitionDefinition(populationPartitionDefinition);
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionDoesNotExist(key);
			mutationResolver.addPopulationPartition(componentManager.getFocalComponentId(),
					populationPartitionDefinition, key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public int getPartitionSize(final Object key, LabelSet labelSet) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			return populationPartitionManager.getPartitionSize(key, labelSet);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	private void validatePopulationPartitionIsOwnedByFocalComponent(final Object key) {
		if (!populationPartitionManager.getOwningComponent(key).equals(componentManager.getFocalComponentId())) {
			throwModelException(SimulationErrorType.POPULATION_PARTITION_DELETION_BY_NON_OWNER, key);
		}
	}

	@Override
	public void removePopulationPartition(final Object key) {
		externalAccessManager.acquireWriteAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validatePopulationPartitionIsOwnedByFocalComponent(key);
			mutationResolver.removePopulationPartition(key);
		} finally {
			externalAccessManager.releaseWriteAccess();
		}
	}

	@Override
	public boolean populationPartitionExists(Object key) {
		externalAccessManager.acquireReadAccess();
		try {
			return populationPartitionManager.populationPartitionExists(key);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public boolean personIsInPopulationPartition(PersonId personId, Object key,
			LabelSet labelSet) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePersonExists(personId);
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			return populationPartitionManager.personInPartition(personId, key, labelSet);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomPartitionedPerson(Object key,
			LabelSet labelSet) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			final PersonId personId = populationPartitionManager.getRandomPartitionedPerson(null, key,
					labelSet);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomPartitionedPersonFromGenerator(Object key,
			LabelSet labelSet, RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = populationPartitionManager.getRandomPartionedPersonFromGenerator(null, key,
					labelSet, randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomPartitionedPersonWithExclusion(PersonId excludedPersonId, Object key,
			LabelSet labelSet) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validatePersonExists(excludedPersonId);
			validateLabelSet(key, labelSet);			
			final PersonId personId = populationPartitionManager.getRandomPartitionedPerson(excludedPersonId, key,
					labelSet);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}

	@Override
	public Optional<PersonId> getRandomPartitionedPersonWithExclusionFromGenerator(PersonId excludedPersonId,
			Object key, LabelSet labelSet,
			RandomNumberGeneratorId randomNumberGeneratorId) {
		externalAccessManager.acquireReadAccess();
		try {
			validatePopulationPartitionKeyNotNull(key);
			validatePopulationPartitionExists(key);
			validateLabelSet(key, labelSet);
			validatePersonExists(excludedPersonId);
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
			final PersonId personId = populationPartitionManager.getRandomPartionedPersonFromGenerator(excludedPersonId,
					key, labelSet, randomNumberGeneratorId);
			if (personId == null) {
				return Optional.empty();
			}
			return Optional.of(personId);
		} finally {
			externalAccessManager.releaseReadAccess();
		}
	}
}