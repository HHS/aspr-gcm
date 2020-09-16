package gcm.simulation;

import java.util.Optional;

import gcm.components.Component;
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
import gcm.scenario.StageId;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.Partition;
import gcm.util.annotations.Source;

/**
 * The MutationResolver resolves each of the mutations presented from the
 * {@link Environment}. Mutation resolution involves controlling locks via the
 * {@link ExternalAccessManager} and has been isolated here largely to simplify
 * the content of the {@link EnvironmentImpl}. All methods defined here parallel
 * the mutation methods of the {@link Environment}.
 * 
 * The MutationResolver has an important role during the bootstrap of the
 * simulation. The context orchestrates the loading of data that does not
 * correspond to mutations available to the components such as adding property
 * definitions, the existence of region and compartments, etc. Once that phase
 * is complete, reports are activated and the MutationResolver is initialized.
 * The MutationResolver handles the loading of mutable data such as property
 * values and resource levels so that reporting behavior for the bootstrap and
 * for normal component-driven activities function identically.
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface MutationResolver extends Element {

	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue);

	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue);

	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue);

	public void setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue);

	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue);

	public StageId createStage(MaterialsProducerId materialsProducerId);

	public BatchId createBatch(final MaterialsProducerId materialsProducerId, final MaterialId materialId, final double amount);

	public void moveBatchToStage(final BatchId batchId, final StageId stageId);

	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue);

	public void setStageOffer(final StageId stageId, final boolean offer);

	public PersonId addPerson(final RegionId regionId, final CompartmentId compartmentId);

	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue);

	public void addResourceToRegion(final ResourceId resourceId, final RegionId regionId, final long amount);

	public GroupId addGroup(GroupTypeId groupTypeId);

	public void addPersonToGroup(PersonId personId, GroupId groupId);

	public void setGroupPropertyValue(GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue);

	public void addPlan(final Plan plan, final double planTime, final Object key);

	public void addPopulationIndex(ComponentId componentId, final Filter filter, final Object key);

	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount);

	public void convertStageToResource(final StageId stageId, final ResourceId resourceId, final long amount);

	public void destroyBatch(final BatchId batchId);

	public void destroyStage(final StageId stageId, final boolean destroyBatches);

	public void halt();

	public void moveBatchToInventory(final BatchId batchId);

	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId, final PersonPropertyId personPropertyId);

	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId, final ResourceId resourceId);

	public void observeCompartmentPersonArrival(final boolean observe, final CompartmentId compartmentId);

	public void observeCompartmentPersonDeparture(final boolean observe, final CompartmentId compartmentId);

	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	public void observeGlobalPersonArrival(final boolean observe);

	public void observeGlobalPersonDeparture(final boolean observe);

	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId);

	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId);

	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId);

	public void observeMaterialsProducerPropertyChange(final boolean observe, final MaterialsProducerId materialProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId);

	public void observeMaterialsProducerResourceChangeByMaterialsProducerId(final boolean observe, final MaterialsProducerId materialsProducerId, final ResourceId resourceId);

	public void observePersonCompartmentChange(final boolean observe, final PersonId personId);

	public void observePersonPropertyChange(final boolean observe, final PersonId personId, final PersonPropertyId personPropertyId);

	public void observePersonRegionChange(final boolean observe, final PersonId personId);

	public void observePersonResourceChange(final boolean observe, final PersonId personId, final ResourceId resourceId);

	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId, final PersonPropertyId personPropertyId);

	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId);

	public void observeRegionPersonArrival(final boolean observe, final RegionId regionId);

	public void observeRegionPersonDeparture(final boolean observe, final RegionId regionId);

	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId, final RegionPropertyId regionPropertyId);
	
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId);

	public void observeRegionResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId);

	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	public void observeStageOfferChange(final boolean observe);

	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId);

	public void observeStageTransfer(final boolean observe);

	public void observeStageTransferByStageId(final boolean observe, final StageId stageId);

	public void removeGroup(GroupId groupId);

	public void removePerson(final PersonId personId);

	public void removePersonFromGroup(PersonId personId, GroupId groupId);

	public <T> Optional<T> removePlan(final Object key);

	public void removePopulationIndex(final Object key);

	public void removeResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	public void removeResourceFromRegion(final ResourceId resourceId, final RegionId regionId, final long amount);

	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId);

	public void setPersonRegion(final PersonId personId, final RegionId regionId);

	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount);

	public void transferOfferedStageToMaterialsProducer(final StageId stageId, final MaterialsProducerId materialsProducerId);

	public void transferProducedResourceToRegion(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final RegionId regionId, final long amount);

	public void transferResourceBetweenRegions(final ResourceId resourceId, final RegionId sourceRegionId, final RegionId destinationRegionId, final long amount);

	public void transferResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	public void transferResourceToPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	public void observeGroupArrival(final boolean observe);

	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId);

	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId);

	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId);

	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId);

	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId);

	public void observeGroupConstruction(final boolean observe);

	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId);

	public void observeGroupDeparture(final boolean observe);

	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId);

	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId);

	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId);

	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId);

	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId);

	public void observeGroupDestruction(final boolean observe);

	public void observeGroupDestructionByGroup(final boolean observe, final GroupId groupId);

	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId);

	public void observeGroupPropertyChange(final boolean observe);

	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId);

	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupPropertyId groupPropertyId, final GroupId groupId);

	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId);

	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId);
	
	public void observePopulationIndexChange(boolean observe, Object key);
	
	public void observeStageTransferBySourceMaterialsProducerId(boolean observe, MaterialsProducerId sourceMaterialsProducerId);

	public void observeStageTransferByDestinationMaterialsProducerId(boolean observe, MaterialsProducerId destinationMaterialsProducerId);
	
	public void addGlobalComponent(GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass);

	public void addPartition(ComponentId componentId, final Partition partition, final Object key);
	
	public void removePartition(Object key);
}
