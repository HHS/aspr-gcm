package gcm.simulation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;

/**
 * Implementor of {@link ObservableEnvironment}
 * 
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class ObservableEnvironmentImpl extends BaseElement implements ObservableEnvironment {

	/*
	 * The mutable Environment that is wrapped by this ObservableEnvironment
	 */
	private Environment environment;

	@Override
	public void init(final Context context) {
		super.init(context);
		this.environment = context.getEnvironment();
	}

	@Override
	public boolean batchExists(final BatchId batchId) {
		return environment.batchExists(batchId);
	}

	@Override
	public double getBatchAmount(final BatchId batchId) {
		return environment.getBatchAmount(batchId);
	}

	@Override
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		return environment.getBatchMaterial(batchId);
	}

	@Override
	public <T> T getBatchProducer(final BatchId batchId) {
		return environment.getBatchProducer(batchId);
	}

	@Override
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		return environment.getBatchPropertyDefinition(materialId, batchPropertyId);
	}

	@Override
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		return environment.getBatchPropertyIds(materialId);
	}

	@Override
	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		return environment.getBatchPropertyTime(batchId, batchPropertyId);
	}

	@Override
	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		return environment.getBatchPropertyValue(batchId, batchPropertyId);
	}

	@Override
	public Optional<StageId> getBatchStageId(final BatchId batchId) {
		return environment.getBatchStageId(batchId);
	}

	@Override
	public double getBatchTime(final BatchId batchId) {
		return environment.getBatchTime(batchId);
	}

	@Override
	public Set<CompartmentId> getCompartmentIds() {
		return environment.getCompartmentIds();
	}

	@Override
	public MapOption getCompartmentMapOption() {
		return environment.getCompartmentMapOption();
	}

	@Override
	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		return environment.getCompartmentPopulationCount(compartmentId);
	}

	@Override
	public double getCompartmentPopulationCountTime(final CompartmentId compartmentId) {
		return environment.getCompartmentPopulationCountTime(compartmentId);
	}

	@Override
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return environment.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
	}

	@Override
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId) {
		return environment.getCompartmentPropertyIds(compartmentId);
	}

	@Override
	public double getCompartmentPropertyTime(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return environment.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
	}

	@Override
	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return environment.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
	}

	@Override
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		return environment.getGlobalComponentIds();
	}

	@Override
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		return environment.getGlobalPropertyDefinition(globalPropertyId);
	}

	@Override
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		return environment.getGlobalPropertyIds();
	}

	@Override
	public double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId) {
		return environment.getGlobalPropertyTime(globalPropertyId);
	}

	@Override
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		return environment.getGlobalPropertyValue(globalPropertyId);
	}

	@Override
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {
		return environment.getGroupCountForGroupType(groupTypeId);
	}

	@Override
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return environment.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
	}

	@Override
	public int getGroupCountForPerson(final PersonId personId) {
		return environment.getGroupCountForPerson(personId);
	}

	@Override
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		return environment.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
	}

	@Override
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
		return environment.getGroupPropertyIds(groupTypeId);
	}

	@Override
	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		return environment.getGroupPropertyTime(groupId, groupPropertyId);
	}

	@Override
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		return environment.getGroupPropertyValue(groupId, groupPropertyId);
	}

	@Override
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		return environment.getGroupsForGroupType(groupTypeId);
	}

	@Override
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return environment.getGroupsForGroupTypeAndPerson(groupTypeId, personId);
	}

	@Override
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		return environment.getGroupsForPerson(personId);
	}

	@Override
	public <T> T getGroupType(final GroupId groupId) {
		return environment.getGroupType(groupId);
	}

	@Override
	public int getGroupTypeCountForPerson(final PersonId personId) {
		return environment.getGroupTypeCountForPerson(personId);
	}

	@Override
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		return environment.getGroupTypeIds();
	}

	@Override
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		return environment.getGroupTypesForPerson(personId);
	}

	@Override
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		return environment.getInventoryBatches(materialsProducerId);
	}

	@Override
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId) {
		return environment.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
	}

	@Override
	public <T extends MaterialId> Set<T> getMaterialIds() {
		return environment.getMaterialIds();
	}

	@Override
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		return environment.getMaterialsProducerIds();
	}

	@Override
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return environment.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
	}

	@Override
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		return environment.getMaterialsProducerPropertyIds();
	}

	@Override
	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return environment.getMaterialsProducerPropertyTime(materialsProducerId, materialsProducerPropertyId);
	}

	@Override
	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return environment.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
	}

	@Override
	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		return environment.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
	}

	@Override
	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		return environment.getMaterialsProducerResourceTime(materialsProducerId, resourceId);
	}

	@Override
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {
		return environment.getOfferedStages(materialsProducerId);
	}

	@Override
	public List<PersonId> getPeople() {
		return environment.getPeople();
	}

	@Override
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		return environment.getPeopleForGroup(groupId);
	}

	@Override
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		return environment.getPeopleForGroupType(groupTypeId);
	}

	@Override
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId) {
		return environment.getPeopleInCompartment(compartmentId);
	}

	@Override
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		return environment.getPeopleInRegion(regionId);
	}

	@Override
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		return environment.getPeopleWithoutResource(resourceId);
	}

	@Override
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return environment.getPeopleWithPropertyValue(personPropertyId, personPropertyValue);
	}
	
	@Override
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return environment.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
	}

	@Override
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		return environment.getPeopleWithResource(resourceId);
	}

	@Override
	public <T> T getPersonCompartment(final PersonId personId) {
		return environment.getPersonCompartment(personId);
	}

	@Override
	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		return environment.getPersonCompartmentArrivalTime(personId);
	}

	@Override
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		return environment.getPersonCompartmentArrivalTrackingPolicy();
	}

	@Override
	public int getPersonCountForGroup(final GroupId groupId) {
		return environment.getPersonCountForGroup(groupId);
	}

	@Override
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		return environment.getPersonCountForGroupType(groupTypeId);
	}

	@Override
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		return environment.getPersonPropertyDefinition(personPropertyId);
	}

	@Override
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		return environment.getPersonPropertyIds();
	}

	@Override
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		return environment.getPersonPropertyTime(personId, personPropertyId);
	}

	@Override
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		return environment.getPersonPropertyValue(personId, personPropertyId);
	}

	@Override
	public <T> T getPersonRegion(final PersonId personId) {
		return environment.getPersonRegion(personId);
	}

	@Override
	public double getPersonRegionArrivalTime(final PersonId personId) {
		return environment.getPersonRegionArrivalTime(personId);
	}

	@Override
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return environment.getPersonRegionArrivalTrackingPolicy();
	}

	@Override
	public long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId) {
		return environment.getPersonResourceLevel(personId, resourceId);
	}

	@Override
	public double getPersonResourceTime(final PersonId personId, final ResourceId resourceId) {
		return environment.getPersonResourceTime(personId, resourceId);
	}

	@Override
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		return environment.getPersonResourceTimeTrackingPolicy(resourceId);
	}

	@Override
	public int getPopulationCount() {
		return environment.getPopulationCount();
	}

	@Override
	public double getPopulationTime() {
		return environment.getPopulationTime();
	}

	@Override
	public Set<RegionId> getRegionIds() {
		return environment.getRegionIds();
	}

	@Override
	public MapOption getRegionMapOption() {
		return environment.getRegionMapOption();
	}

	@Override
	public int getRegionPopulationCount(final RegionId regionId) {
		return environment.getRegionPopulationCount(regionId);
	}

	@Override
	public double getRegionPopulationCountTime(final RegionId regionId) {
		return environment.getRegionPopulationCountTime(regionId);
	}

	@Override
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		return environment.getRegionPropertyDefinition(regionPropertyId);
	}

	@Override
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		return environment.getRegionPropertyIds();
	}

	@Override
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return environment.getRegionPropertyTime(regionId, regionPropertyId);
	}

	@Override
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return environment.getRegionPropertyValue(regionId, regionPropertyId);
	}

	@Override
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		return environment.getRegionResourceLevel(regionId, resourceId);
	}

	@Override
	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId) {
		return environment.getRegionResourceTime(regionId, resourceId);
	}

	@Override
	public ReplicationId getReplicationId() {
		return environment.getReplicationId();
	}

	@Override
	public <T extends ResourceId> Set<T> getResourceIds() {
		return environment.getResourceIds();
	}

	@Override
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return environment.getResourcePropertyDefinition(resourceId, resourcePropertyId);
	}

	@Override
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		return environment.getResourcePropertyIds(resourceId);
	}

	@Override
	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return environment.getResourcePropertyTime(resourceId, resourcePropertyId);
	}

	@Override
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return environment.getResourcePropertyValue(resourceId, resourcePropertyId);
	}

	@Override
	public ScenarioId getScenarioId() {
		return environment.getScenarioId();
	}

	@Override
	public List<BatchId> getStageBatches(final StageId stageId) {
		return environment.getStageBatches(stageId);
	}

	@Override
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
		return environment.getStageBatchesByMaterialId(stageId, materialId);
	}

	@Override
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		return environment.getStageProducer(stageId);
	}

	@Override
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		return environment.getStages(materialsProducerId);
	}

	@Override
	public double getTime() {
		return environment.getTime();
	}

	@Override
	public boolean groupExists(final GroupId groupId) {
		return environment.groupExists(groupId);
	}

	@Override
	public boolean isGroupMember(final PersonId personId, final GroupId groupId) {
		return environment.isGroupMember(personId, groupId);
	}

	@Override
	public boolean isStageOffered(final StageId stageId) {
		return environment.isStageOffered(stageId);
	}

	@Override
	public boolean personExists(final PersonId personId) {
		return environment.personExists(personId);
	}

	@Override
	public boolean stageExists(final StageId stageId) {
		return environment.stageExists(stageId);
	}

	@Override
	public boolean personIsInPopulationIndex(PersonId personId, Object key) {
		return environment.personIsInPopulationIndex(personId, key);
	}

	@Override
	public boolean populationIndexExists(Object key) {
		return environment.populationIndexExists(key);
	}

	@Override
	public int getIndexSize(Object key) {
		return environment.getIndexSize(key);
	}

	@Override
	public List<PersonId> getIndexedPeople(Object key) {
		return environment.getIndexedPeople(key);
	}

	@Override
	public void releaseOutputItem(OutputItem outputItem) {
		environment.releaseOutputItem(outputItem);
	}

	@Override
	public ComponentId getCurrentComponentId() {
		return environment.getCurrentComponentId();
	}

	@Override
	public Class<? extends Component> getGlobalComponentClass(GlobalComponentId globalComponentId) {
		return environment.getGlobalComponentClass(globalComponentId);
	}

	@Override
	public Class<? extends Component> getCompartmentComponentClass(CompartmentId compartmentId) {
		return environment.getCompartmentComponentClass(compartmentId);
	}

	@Override
	public Class<? extends Component> getMaterialsProducerComponentClass(MaterialsProducerId materialsProducerId) {
		return environment.getMaterialsProducerComponentClass(materialsProducerId);
	}

	@Override
	public Class<? extends Component> getRegionComponentClass(RegionId regionId) {
		return environment.getRegionComponentClass(regionId);
	}

	@Override
	public <T> T getProfiledProxy(T instance) {
		return environment.getProfiledProxy(instance);
	}

	@Override
	public int getSuggestedPopulationSize() {
		return environment.getSuggestedPopulationSize();
	}

	@Override
	public List<GroupId> getGroupIds() {		
		return environment.getGroupIds();
	}
}
