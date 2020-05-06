package gcm.output.reports;

import java.util.Set;

import gcm.output.OutputItemHandler;
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
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;

/**
 * A general reporting interface.
 *
 * Life Cycle of a Report:
 *
 * <li>The Report is constructed from a class reference stored in the scenario.
 * This is done to confine the report to a single thread.
 *
 * <li>The simulation invokes the getListenedStateChanges() method of the Report
 * to determine which state changes the listener is interested in.
 *
 * <li>The simulation invokes the init() method of the Report at the beginning
 * of the simulation. This occurs exactly once and happens before any data is
 * loaded from the scenario that could correspond to the registered
 * StateChanges. During init() thread-safe data is passed to the report that
 * initializes the report to the correct state.
 *
 * <li>The simulation invokes the various handleXXX() methods corresponding to
 * the state changes for which the Report has registered interest. These
 * handleXXX() methods pass only the minimum information needed for the Report
 * to interpret the event and the Report will rely on an ObservableEnvironment
 * instance to supply auxiliary data. The ObservableEnvironment will reflect the
 * data state of the simulation as it is immediately after the event has
 * occurred. GCM does not support querying past data state and the handle()
 * methods often contain arguments that support past data state values that are
 * of likely interest. However, unlike the registered observations of
 * Components, Reports are stimulated immediately after a mutation. Thus if a
 * component were to mutate a property or other value repeatedly during its
 * activation, the Report would receive a handleXXX() invocation for each
 * mutation possibly containing all the intermediate data states.
 *
 * <li>The simulation invokes the close() method when all plans and observations
 * are completed, signaling to the Report that it should finalize its
 * activities.
 *
 *
 * Reports produce ReportItems and deposit them in the ObservableEnvironment.
 * The ReportItems are then sent to the an {@link OutputItemHandler} to handle the
 * final disposition of these items such as recording to a text file or sending
 * the report item over a network, etc. Reports must create their report items
 * using their ReportId so that the ReportItemHandler can make the proper
 * determinations for processing the ReportItems.
 *
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface Report {

	/**
	 * Invoked once at the end of the simulation
	 */
	public void close(ObservableEnvironment observableEnvironment);

	/**
	 * Returns the set of StateChange that this listener is interested in
	 * receiving. The simulation will invoke this method exactly once during
	 * initialization of the simulation run.
	 */
	public Set<StateChange> getListenedStateChanges();

	/**
	 * Handler for {@link StateChange#BATCH_CREATION}
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleBatchCreation(ObservableEnvironment observableEnvironment, final BatchId batchId);

	/**
	 * Handler for {@link StateChange#BATCH_DESTRUCTION}. This should only be
	 * invoked when a batch is destroyed by direct action and not as a
	 * consequence of stage destruction or stage conversion to materials or
	 * resources.
	 *
	 * @param batchInfo
	 *
	 *
	 */
	public void handleBatchDestruction(ObservableEnvironment observableEnvironment, BatchInfo batchInfo);

	/**
	 * Handler for {@link StateChange#BATCH_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param batchId
	 *            the identifier of the batch that had a property value change
	 * @param batchPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleBatchPropertyValueAssignment(ObservableEnvironment observableEnvironment, final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Handler for {@link StateChange#BATCH_SHIFT}
	 *
	 * @param sourceBatchId
	 *            the identifier of the source batch
	 * @param destinationBatchId
	 *            the identifier of the destination batch
	 * @param amount
	 *            the amount that was shift between the batches
	 */
	public void handleBatchShift(ObservableEnvironment observableEnvironment, final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount);

	/**
	 * Handler for {@link StateChange# COMPARTMENT_ASSIGNMENT}
	 *
	 * @param personId
	 *            the identifier of the person who was assigned to a new
	 *            compartment
	 * @param sourceCompartmentId
	 *            the compartment identifier of the person's previous
	 *            compartment
	 */
	public void handleCompartmentAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final CompartmentId sourceCompartmentId);

	/**
	 * Handler for {@link StateChange#COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param compartmentId
	 *            the identifier of the compartment that had a property value
	 *            change
	 * @param compartmentPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleCompartmentPropertyValueAssignment(ObservableEnvironment observableEnvironment, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Handler for {@link StateChange#GLOBAL_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param globalPropertyId
	 *            the identifier of the property that changed
	 */
	public void handleGlobalPropertyValueAssignment(ObservableEnvironment observableEnvironment, final GlobalPropertyId globalPropertyId);

	/**
	 * Handler for
	 * {@link StateChange#MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param materialsProducerId
	 *            the identifier of the materials producer that had a property
	 *            value change
	 * @param materialsProducerPropertyId
	 *            the identifier of the property that changed
	 */

	public void handleMaterialsProducerPropertyValueAssignment(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Handler for {@link StateChange#MATERIALS_PRODUCER_RESOURCE_ADDITION}
	 *
	 * @param materialsProducerId
	 *            the identifier of the materials producer that had a resource
	 *            addition
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 */
	public void handleMaterialsProducerResourceAddition(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#PERSON_ADDITION}
	 *
	 * @param personId
	 *            the identifier of the person who was added to the simulation
	 *
	 */
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId);

	/**
	 * Handler for {@link StateChange#PERSON_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param personId
	 *            the identifier of the person who had a property value change
	 * @param personPropertyId
	 *            the identifier of the property that changed
	 * @param oldPersonPropertyValue
	 *            the value of the property prior the change
	 */
	public void handlePersonPropertyValueAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final PersonPropertyId personPropertyId, final Object oldPersonPropertyValue);

	/**
	 * Handler for {@link StateChange#PERSON_REMOVAL}
	 *
	 * @param personInfo
	 *            the container for all information on the person who has been
	 *            removed from the simulation simulation
	 */
	public void handlePersonRemoval(ObservableEnvironment observableEnvironment, PersonInfo personInfo);

	/**
	 * Handler for {@link StateChange#GROUP_MEMBERSHIP_ADDITION}
	 *
	 *
	 */
	public void handleGroupMembershipAddition(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId);

	/**
	 * Handler for {@link StateChange#GROUP_MEMBERSHIP_REMOVAL}
	 *
	 */
	public void handleGroupMembershipRemoval(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId);

	/**
	 * Handler for {@link StateChange#PERSON_RESOURCE_ADDITION}
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
	public void handlePersonResourceAddition(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#PERSON_RESOURCE_REMOVAL}
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
	public void handlePersonResourceRemoval(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#PERSON_RESOURCE_TRANSFER_TO_REGION}
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
	public void handlePersonResourceTransferToRegion(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#REGION_ASSIGNMENT}
	 *
	 * @param personId
	 *            the identifier of the person who was assigned to a new region
	 * @param sourceRegionId
	 *            the region identifier of the person's previous region
	 */
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final RegionId sourceRegionId);

	/**
	 * Handler for {@link StateChange#REGION_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param regionId
	 *            the identifier of the region that had a property value change
	 * @param regionPropertyId
	 *            the identifier of the property that changed
	 */

	public void handleRegionPropertyValueAssignment(ObservableEnvironment observableEnvironment, final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Handler for {@link StateChange#REGION_RESOURCE_ADDITION}
	 *
	 * @param regionId
	 *            the identifier of the region that had a resource addition
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 */
	public void handleRegionResourceAddition(ObservableEnvironment observableEnvironment, final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#REGION_RESOURCE_REMOVAL}
	 *
	 * @param regionId
	 *            the identifier of the region to which a resource was added
	 * @param resourceId
	 *            the identifier of the resource that was added
	 * @param amount
	 *            the amount of the resource that was added
	 *
	 */

	public void handleRegionResourceRemoval(ObservableEnvironment observableEnvironment, final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#REGION_RESOURCE_TRANSFER_TO_PERSON}
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

	public void handleRegionResourceTransferToPerson(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#RESOURCE_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param resourceId
	 *            the identifier of the resource that had a property value
	 *            change
	 * @param resourcePropertyId
	 *            the identifier of the property that changed
	 */
	public void handleResourcePropertyValueAssignment(ObservableEnvironment observableEnvironment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Handler for {@link StateChange#STAGE_CONVERTED_TO_BATCH}
	 *
	 * @param stageInfo
	 *            the information on the stage that was converted
	 * @param batchId
	 *            the identifier of the batch that was created
	 *
	 */
	public void handleStageConversionToBatch(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final BatchId batchId);

	/**
	 * Handler for {@link StateChange#STAGE_CONVERTED_TO_RESOURCE}
	 *
	 * @param stageInfo
	 *            the information on the stage that was converted
	 *
	 * @param resourceId
	 *            the type of resource that was created
	 * 
	 * @param amount
	 *            the number of units of resource that were created
	 *
	 */
	public void handleStageConversionToResource(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#STAGE_CREATION}
	 *
	 * @param stageId
	 *            the identifier of the stage
	 *
	 */
	public void handleStageCreation(ObservableEnvironment observableEnvironment, final StageId stageId);

	/**
	 * Handler for {@link StateChange#BATCH_STAGED}
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId);

	/**
	 * Handler for {@link StateChange#STAGE_DESTRUCTION }This should only be
	 * invoked when a stage is destroyed by direct action and not as a
	 * consequence of stage conversion to materials or resources.
	 *
	 *
	 */
	public void handleStageDestruction(ObservableEnvironment observableEnvironment, StageInfo stageInfo);

	/**
	 * Handler for {@link StateChange#STAGE_OFFERED}
	 *
	 *
	 * @param stageId
	 *            the identifier of the stage that had a change to its offer
	 *            state
	 *
	 */
	public void handleStageOfferChange(ObservableEnvironment observableEnvironment, final StageId stageId);

	/**
	 * Handler for {@link StateChange#STAGE_TRANSFERRED}
	 *
	 *
	 * @param stageId
	 *            the identifier of the stage that was converted
	 * @param materialsProducerId
	 *            the identifier of the materials producer that previously owned
	 *            the stage
	 *
	 */
	public void handleStageTransfer(ObservableEnvironment observableEnvironment, final StageId stageId, final MaterialsProducerId materialsProducerId);

	/**
	 * Handler for {@link StateChange#INTER_REGION_RESOURCE_TRANSFER}
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

	public void handleTransferResourceBetweenRegions(ObservableEnvironment observableEnvironment, final RegionId sourceRegionId, final RegionId destinationRegionId, final ResourceId resourceId,
			final long amount);

	/**
	 * Handler for {@link StateChange#MATERIALS_PRODUCER_RESOURCE_TRANSFER}
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
	public void handleTransferResourceFromMaterialsProducerToRegion(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId, final RegionId regionId,
			final ResourceId resourceId, final long amount);

	/**
	 * Handler for {@link StateChange#BATCH_UNSTAGED}
	 *
	 * @param batchId
	 *            the identifier of the batch
	 *
	 */
	public void handleUnStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId, final StageId stageId);

	/**
	 * Handler for {@link StateChange#GROUP_PROPERTY_VALUE_ASSIGNMENT}
	 *
	 * @param groupId
	 *            the identifier of the batch
	 *
	 */
	public void handleGroupPropertyValueAssignment(ObservableEnvironment observableEnvironment, GroupId groupId, GroupPropertyId groupPropertyId, Object oldGroupPropertyValue);

	/**
	 * Handler for {@link StateChange#GROUP_ADDITION}
	 *
	 * @param groupId
	 *            the identifier of the batch
	 *
	 */
	public void handleGroupAddition(ObservableEnvironment observableEnvironment, GroupId groupId);

	/**
	 * Handler for {@link StateChange#GROUP_REMOVAL}
	 *
	 * @param groupId
	 *            the identifier of the batch
	 *
	 */
	public void handleGroupRemoval(ObservableEnvironment observableEnvironment, GroupInfo groupInfo);

	/**
	 * Invoked once at the beginning of the simulation.
	 */
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData);

}
