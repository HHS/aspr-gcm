package gcm.simulation;

import gcm.output.reports.BatchInfo;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.PersonInfo;
import gcm.output.reports.Report;
import gcm.output.reports.StageInfo;
import gcm.output.reports.StateChange;
import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;

/**
 * Reports manager for GCM. As the Environment mutates data, it notifies this
 * manager that in turn maps the information to the reports that are registered
 * to observe those changes.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface ReportsManager extends Element {

	/**
	 * Signals to the {@link ReportsManager} that reports should close. Invoked
	 * by the {@link Context} at the end of the simulation run.
	 */
	public void closeReports();


	/**
	 * Handler for StateChange.BATCH_CREATION
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleBatchCreation(final BatchId batchId);

	/**
	 * Handler for StateChange.BATCH_DESTRUCTION
	 *
	 * @param batchInfo
	 *            The information about a batch that has been removed from the
	 *            simulation.
	 *
	 */

	public void handleBatchDestruction(BatchInfo batchInfo);

	/**
	 * A convenience method to ensure that the environment does not
	 * unnecessarily calculate the expensive maps that need to be passed when
	 * reporting the removal of a batch.
	 * 
	 * @return
	 */
	public boolean hasBatchDestructionReports();

	/**
	 * A convenience method to ensure that the environment does not
	 * unnecessarily calculate the expensive maps that need to be passed when
	 * reporting the removal of a stage.
	 * 
	 * @return
	 */
	public boolean hasStageDestructionReports();

	/**
	 * Handler for StateChange.BATCH_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param batchId
	 *            the identifier of the batch that had a property value change
	 * @param batchPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleBatchPropertyValueAssignment(final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Handler for StateChange.BATCH_SHIFT
	 *
	 * @param sourceBatchId
	 *            the identifier of the source batch
	 * @param destinationBatchId
	 *            the identifier of the destination batch
	 * @param amount
	 *            the amount that was shift between the batches
	 */
	public void handleBatchShift(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount);

	/**
	 * Handler for StateChange. COMPARTMENT_ASSIGNMENT
	 *
	 * @param personId
	 *            the identifier of the person who was assigned to a new
	 *            compartment
	 * @param sourceCompartmentId
	 *            the compartment identifier of the person's previous
	 *            compartment
	 */
	public void handleCompartmentAssignment(final PersonId personId, final CompartmentId sourceCompartmentId);

	/**
	 * Handler for StateChange.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param compartmentId
	 *            the identifier of the compartment that had a property value
	 *            change
	 * @param compartmentPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleCompartmentPropertyValueAssignment(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Handler for StateChange.GLOBAL_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param globalPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleGlobalPropertyValueAssignment(final GlobalPropertyId globalPropertyId);

	/**
	 * Handler for StateChange.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param materialsProducerId
	 *            the identifier of the materials producer that had a property
	 *            value change
	 * @param materialsProducerPropertyId
	 *            the identifier of the property that changed
	 */

	public void handleMaterialsProducerPropertyValueAssignment(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Handler for StateChange.MATERIALS_PRODUCER_RESOURCE_ADDITION
	 *
	 * @param materialsProducerId
	 *            the identifier of the materials producer that had a resource
	 *            addition
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 */
	public void handleMaterialsProducerResourceAddition(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.PERSON_ADDITION
	 *
	 * @param personId
	 *            the identifier of the person who was added to the simulation
	 *
	 */
	public void handlePersonAddition(final PersonId personId);

	/**
	 * Handler for StateChange.GROUP_MEMBERSHIP_ADDITION
	 *
	 *
	 */
	public void handleGroupMembershipAddition(final GroupId groupId, final PersonId personId);

	/**
	 * Handler for StateChange.GROUP_MEMBERSHIP_REMOVAL
	 *
	 *
	 */
	public void handleGroupMembershipRemoval(final GroupId groupId, final PersonId personId);

	/**
	 * Handler for StateChange.PERSON_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param personId
	 *            the identifier of the person who had a property value change
	 * @param personPropertyId
	 *            the identifier of the property that changed
	 * @param oldValue
	 *            the value of the property prior the change
	 */
	public void handlePersonPropertyValueAssignment(final PersonId personId, final PersonPropertyId personPropertyId, final Object oldPersonPropertyValue);

	/**
	 * Handler for StateChange.GROUP_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param groupId
	 *            the identifier of the group that had a property value change
	 * @param groupPropertyId
	 *            the identifier of the property that changed
	 * @param oldValue
	 *            the value of the property prior the change
	 */
	public void handleGroupPropertyValueAssignment(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object oldGroupPropertyValue);

	/**
	 * Handler for StateChange.PERSON_REMOVAL
	 *
	 * @param personInfo
	 *            The information about the person who has been removed from the
	 *            simulation.
	 * 
	 */
	public void handlePersonRemoval(PersonInfo personInfo);

	/**
	 * Handler for {@link StateChange#GROUP_REMOVAL}
	 *
	 * @param groupInfo
	 *            The information about the person who has been removed from the
	 *            simulation.
	 * 
	 */
	public void handleGroupRemoval(GroupInfo groupInfo);

	/**
	 * Handler for {@link StateChange#GROUP_ADDITION}
	 * 
	 */
	public void handleGroupAddition(GroupId groupId);

	/**
	 * A convenience method to ensure that the environment does not
	 * unnecessarily calculate the expensive maps that need to be passed when
	 * reporting the removal of a person.
	 * 
	 * @return
	 */
	public boolean hasPersonRemovalReports();

	/**
	 * A convenience method to ensure that the environment does not
	 * unnecessarily calculate the expensive maps that need to be passed when
	 * reporting the removal of a group.
	 * 
	 * @return
	 */
	public boolean hasGroupRemovalReports();

	/**
	 * Handler for StateChange.PERSON_RESOURCE_ADDITION
	 *
	 * @param personId
	 *            the identifier of the person to which a resource was
	 *            transferred from its region
	 * @param resourceId
	 *            the identifier of the resource that was transferred
	 * @param amount
	 *            the amount of the resource that was transferred
	 *
	 */
	public void handlePersonResourceAddition(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.PERSON_RESOURCE_REMOVAL
	 *
	 * @param personId
	 *            the identifier of the person possessing a resource that was
	 *            eliminated from the simulation
	 * @param resourceId
	 *            the identifier of the resource that was eliminated
	 * @param amount
	 *            the amount of the resource that was eliminated
	 *
	 */
	public void handlePersonResourceRemoval(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.PERSON_RESOURCE_TRANSFER_TO_REGION
	 *
	 * @param personId
	 *            the identifier of the person possessing a resource that was
	 *            transferred to the person's region
	 * @param resourceId
	 *            the identifier of the resource that was transferred
	 * @param amount
	 *            the amount of the resource that was transferred
	 *
	 */
	public void handlePersonResourceTransferToRegion(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.REGION_ASSIGNMENT
	 *
	 * @param personId
	 *            the identifier of the person who was assigned to a new region
	 * @param sourceRegionId
	 *            the region identifier of the person's previous region
	 */
	public void handleRegionAssignment(final PersonId personId, final RegionId sourceRegionId);

	/**
	 * Handler for StateChange.REGION_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param regionId
	 *            the identifier of the region that had a property value change
	 * @param regionPropertyId
	 *            the identifier of the property that changed
	 */

	public void handleRegionPropertyValueAssignment(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Handler for StateChange.REGION_RESOURCE_ADDITION
	 *
	 * @param regionId
	 *            the identifier of the region that had a resource addition
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 */
	public void handleRegionResourceAddition(final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.REGION_RESOURCE_REMOVAL
	 *
	 * @param regionId
	 *            the identifier of the region to which a resource was added
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 *
	 */

	public void handleRegionResourceRemoval(final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.REGION_RESOURCE_TRANSFER_TO_PERSON
	 *
	 * @param personId
	 *            the identifier of the person to which a resource was
	 *            transferred from its region
	 * @param resourceId
	 *            the identifier of the resource that was transferred
	 * @param amount
	 *            the amount of the resource that was transferred
	 *
	 */

	public void handleRegionResourceTransferToPerson(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.RESOURCE_PROPERTY_VALUE_ASSIGNMENT
	 *
	 * @param resourceId
	 *            the identifier of the resource that had a property value
	 *            change
	 * @param resourcePropertyId
	 *            the identifier of the property that changed
	 */
	public void handleResourcePropertyValueAssignment(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Handler for StateChange.STAGE_CONVERTED_TO_BATCH
	 *
	 * @param batchId
	 *            the identifier of the batch
	 * @param stageInfo
	 *            the information about the stage that was converted
	 *
	 */
	public void handleStageConversionToBatch(StageInfo stageInfo, BatchId batchId);

	/**
	 * Handler for StateChange.STAGE_CONVERTED_TO_RESOURCE
	 *
	 * @param stageInfo
	 *            the information about the stage that has been converted
	 *
	 * @param resourceId
	 *            the type of resource that was created
	 * @param amount
	 *            the number of units of resource that were created
	 *
	 */
	public void handleStageConversionToResource(StageInfo stageInfo, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.STAGE_CREATION
	 *
	 * @param stageId
	 *            the identifier of the stage
	 *
	 */
	public void handleStageCreation(final StageId stageId);

	/**
	 * Handler for StateChange.BATCH_STAGED
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleStagedBatch(final BatchId batchId);

	/**
	 * Handler for StateChange.STAGE_DESTRUCTION
	 *
	 * @param stageInfo
	 *            the information about a stage and its batches that have been
	 *            removed from the simulation
	 *
	 */
	public void handleStageDestruction(StageInfo stageInfo);

	/**
	 * Handler for StateChange.STAGE_OFFERED
	 *
	 *
	 * @param stageId
	 *            the identifier of the stage that had a change to its offer
	 *            state
	 *
	 */
	public void handleStageOfferChange(final StageId stageId);

	/**
	 * Handler for StateChange.STAGE_TRANSFERRED
	 *
	 *
	 * @param stageId
	 *            the identifier of the stage that was converted
	 * @param materialsProducerId
	 *            the identifier of the materials producer that previously owned
	 *            the stage
	 *
	 */
	public void handleStageTransfer(final StageId stageId, final MaterialsProducerId materialsProducerId);

	/**
	 * Handler for StateChange.INTER_REGION_RESOURCE_TRANSFER
	 *
	 * @param sourceRegionId
	 *            the identifier of the region from which a resource was removed
	 * @param destinationRegionId
	 *            the identifier of the region to which a resource was added
	 * @param resourceId
	 *            the identifier of the resource that was transferred
	 * @param amount
	 *            the amount of the resource that was transferred
	 *
	 */

	public void handleTransferResourceBetweenRegions(final RegionId sourceRegionId, final RegionId destinationRegionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.MATERIALS_PRODUCER_RESOURCE_TRANSFER
	 *
	 * @param materialsProducerId
	 *            the identifier of the materials producer that is the source of
	 *            the resource
	 * @param regionId
	 *            the identifier of the region that the destination of the
	 *            resource
	 * @param resourceId
	 *            the type of resource that was transferred
	 * @param amount
	 *            the number of units of resource that were transferred
	 *
	 */
	public void handleTransferResourceFromMaterialsProducerToRegion(final MaterialsProducerId materialsProducerId, final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for StateChange.BATCH_UNSTAGED
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleUnStagedBatch(final BatchId batchId, final StageId stageId);
	
	
	public boolean isActiveReport(Class<? extends Report> reportClass);

}
