package gcm.output.reports;

import gcm.components.Component;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.ObservationType;
import gcm.util.annotations.Source;
import net.jcip.annotations.ThreadSafe;

/**
 * An enumeration representing the data change events in GCM that can be
 * passively observed by Reports. Passive observation is accomplished via the
 * {@link Report} when its {@link Report#getListenedStateChanges()} method is
 * invoked at the start of the simulation run.
 * 
 * A related but quite different enumeration {@link ObservationType} is used by
 * components {@link Component} to observe changes to data content rather than
 * being focused on data change events.
 *
 * Each {@link Report} is given access to an {@link ObservableEnvironment} in
 * order to derive any auxiliary information that is needed by the report to
 * process the {@link StateChange}
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source
public enum StateChange {

	/**
	 * Triggered by the addition of resources to a Materials Resource Producer
	 * during scenario load. Handled by
	 * {@link Report#handleMaterialsProducerResourceAddition(gcm.scenario.MaterialsProducerId, gcm.scenario.ResourceId, long)}
	 */
	MATERIALS_PRODUCER_RESOURCE_ADDITION,

	/**
	 * Triggered by the transfer of resources from a Materials Resource Producer
	 * to a Region. Handled by {@link Report#}
	 */
	MATERIALS_PRODUCER_RESOURCE_TRANSFER,

	/**
	 * Triggered by the direct change of the offer state of a stage by the
	 * owning Materials Resource Producer or by a transfer of a stage that was
	 * in the offered state(==true) to a different Materials Resource Producer.
	 * Handled by {@link Report#handleStageOfferChange(gcm.scenario.StageId)}
	 */
	STAGE_OFFERED,

	/**
	 * Triggered by the by the transfer of a stage to a different Materials
	 * Resource Producer. Handled by
	 * {@link Report#handleStageTransfer(gcm.scenario.StageId, gcm.scenario.MaterialsProducerId)}
	 */
	STAGE_TRANSFERRED,

	/**
	 * Triggered by the conversion of a stage(possible containing multiple
	 * batches of different material types) into a new batch. Handled by
	 * {@link Report#handleStageConversionToBatch(StageInfo, gcm.scenario.BatchId)}
	 */
	STAGE_CONVERTED_TO_BATCH,

	/**
	 * Triggered by the conversion of a stage(possible containing multiple
	 * batches of different material types) into a new resource amount. Handled
	 * by
	 * {@link Report#handleStageConversionToResource(StageInfo, gcm.scenario.ResourceId, long)}
	 */
	STAGE_CONVERTED_TO_RESOURCE,

	/**
	 * Triggered by the creation of a stage by scenario load or by Materials
	 * Resource Producer Action. Handled by
	 * {@link Report#handleStageCreation(gcm.scenario.StageId)}
	 */
	STAGE_CREATION,

	/**
	 * Triggered by the creation of a batch by scenario load or by Materials
	 * Resource Producer Action. Handled by
	 * {@link Report#handleBatchCreation(gcm.scenario.BatchId)}
	 */
	BATCH_CREATION,

	/**
	 * Triggered by the destruction of a stage by the owning Materials Resource
	 * Producer. Handled by {@link Report#handleStageDestruction(StageInfo)}
	 */
	STAGE_DESTRUCTION,

	/**
	 * Triggered by the destruction of a batch by the owning Materials Resource
	 * Producer. Handled by {@link Report#handleBatchDestruction(BatchInfo)}
	 */
	BATCH_DESTRUCTION,

	/**
	 * Triggered by the shift of material from one batch to another. Handled by
	 * {@link Report#handleBatchShift(gcm.scenario.BatchId, gcm.scenario.BatchId, double)}
	 */
	BATCH_SHIFT,

	/**
	 * Triggered by the association of a batch that is in the inventory of a
	 * Materials Resource Producer to a stage under that Producer's control.
	 * Handled by {@link Report#handleStagedBatch(gcm.scenario.BatchId)}
	 */
	BATCH_STAGED,

	/**
	 * Triggered by the disassociation of a batch that is in the inventory of a
	 * Materials Resource Producer from a stage under that Producer's control..
	 * Handled by {@link Report#handleUnStagedBatch(gcm.scenario.BatchId, gcm.scenario.StageId)}
	 */
	BATCH_UNSTAGED,

	/**
	 * Triggered by assignment of a property value to a batch. Handled by
	 * {@link Report#handleBatchPropertyValueAssignment(gcm.scenario.BatchId, gcm.scenario.BatchPropertyId)}
	 */
	BATCH_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by addition of a person either from scenario load or by the
	 * action of a Component. Handled by
	 * {@link Report#handlePersonAddition(gcm.scenario.PersonId)}
	 */
	PERSON_ADDITION,

	/**
	 * Triggered by the removal of a person. Handled by
	 * {@link Report#handlePersonRemoval(PersonInfo)}
	 */
	PERSON_REMOVAL,

	/**
	 * Triggered by addition of a group either from scenario load or by the
	 * action of a Component. Handled by
	 * {@link Report#handleGroupAddition(gcm.scenario.GroupId)}
	 */	
	GROUP_ADDITION,

	/**
	 * Triggered by the removal of a group. Handled by
	 * {@link Report#handleGroupRemoval(GroupInfo)}
	 */
	GROUP_REMOVAL,
	
	/**
	 * Triggered by addition of a person to a group either from scenario load or by the
	 * action of a Component. Handled by
	 * {@link Report#handleGroupMembershipAddition(gcm.scenario.GroupId, gcm.scenario.PersonId)}
	 */	
	GROUP_MEMBERSHIP_ADDITION,

	/**
	 * Triggered by the removal of a person from a group. Handled by
	 * {@link Report#handleGroupMembershipRemoval(gcm.scenario.GroupId, gcm.scenario.PersonId)}
	 */
	GROUP_MEMBERSHIP_REMOVAL,
	
	/**
	 * Triggered by the assignment of a person to a region. Initial assignment
	 * of region to a person is covered under
	 * {@link StateChange#PERSON_ADDITION} Handled by
	 * {@link Report#handleRegionAssignment(gcm.scenario.PersonId, gcm.scenario.RegionId)}
	 */
	REGION_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a person to a compartment. Initial
	 * assignment of compartment to a person is covered under
	 * {@link StateChange#PERSON_ADDITION} Handled by
	 * {@link Report#handleCompartmentAssignment(gcm.scenario.PersonId, gcm.scenario.CompartmentId)}
	 */
	COMPARTMENT_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a property value to a compartment either
	 * from scenario load or by the action of a Component. Handled by
	 * {@link Report#handleCompartmentPropertyValueAssignment(gcm.scenario.CompartmentId, gcm.scenario.CompartmentPropertyId)}
	 */
	COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a global property value either from
	 * scenario load or by the action of a Component. Handled by
	 * {@link Report#handleGlobalPropertyValueAssignment(gcm.scenario.GlobalPropertyId)}
	 */
	GLOBAL_PROPERTY_VALUE_ASSIGNMENT,
	
	/**
	 * Triggered by the assignment of a group property value either from
	 * scenario load or by the action of a Component. Handled by
	 * {@link Report#handleGroupPropertyValueAssignment(gcm.scenario.GroupId, gcm.scenario.GroupPropertyId, Object)}
	 */
	GROUP_PROPERTY_VALUE_ASSIGNMENT,
	
	/**
	 * Triggered by the assignment of a person property value either from
	 * scenario load or by the action of a Component. Handled by
	 * {@link Report#handlePersonPropertyValueAssignment(gcm.scenario.PersonId, gcm.scenario.PersonPropertyId, Object)}
	 */
	PERSON_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a region property value either from
	 * scenario load or by the action of a Component. Handled by
	 * {@link Report#handleRegionPropertyValueAssignment(gcm.scenario.RegionId, gcm.scenario.RegionPropertyId)}
	 */
	REGION_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a Materials Resource Producer property
	 * value either from scenario load or by the action of a Component. Handled
	 * by
	 * {@link Report#handleMaterialsProducerPropertyValueAssignment(gcm.scenario.MaterialsProducerId, gcm.scenario.MaterialsProducerPropertyId)}
	 */
	MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by the assignment of a Resource property value either from
	 * scenario load or by the action of a Component. Handled by
	 * {@link Report#handleResourcePropertyValueAssignment(gcm.scenario.ResourceId, gcm.scenario.ResourcePropertyId)}
	 */
	RESOURCE_PROPERTY_VALUE_ASSIGNMENT,

	/**
	 * Triggered by the addition of resource values to a person from scenario
	 * load . Handled by
	 * {@link Report#handlePersonResourceAddition(gcm.scenario.PersonId, gcm.scenario.ResourceId, long)}
	 */
	PERSON_RESOURCE_ADDITION,

	/**
	 * Triggered by the addition(not transfer) of resources to a region either
	 * from scenario load or by the action of a Component. Handled by
	 * {@link Report#handleRegionResourceAddition(gcm.scenario.RegionId, gcm.scenario.ResourceId, long)}
	 */
	REGION_RESOURCE_ADDITION,

	/**
	 * Triggered by the removal(not transfer) of resources from a region.
	 * Handled by
	 * {@link Report#handleRegionResourceRemoval(gcm.scenario.RegionId, gcm.scenario.ResourceId, long)}
	 */
	REGION_RESOURCE_REMOVAL,

	/**
	 * Triggered by the removal(not transfer) of resources from a person.
	 * Handled by
	 * {@link Report#handlePersonResourceRemoval(gcm.scenario.PersonId, gcm.scenario.ResourceId, long)}
	 */
	PERSON_RESOURCE_REMOVAL,

	/**
	 * Triggered by transfer of a resource from a person to the region
	 * containing the person. Handled by
	 * {@link Report#handlePersonResourceTransferToRegion(gcm.scenario.PersonId, gcm.scenario.ResourceId, long)}
	 */
	PERSON_RESOURCE_TRANSFER_TO_REGION,

	/**
	 * Triggered by the transfer of a resource from a region to a person in the
	 * region. Handled by
	 * {@link Report#handleRegionResourceTransferToPerson(gcm.scenario.PersonId, gcm.scenario.ResourceId, long)}
	 */
	REGION_RESOURCE_TRANSFER_TO_PERSON,

	/**
	 * Triggered by the transfer of a resource between two regions. Handled by
	 * {@link Report#handleTransferResourceBetweenRegions(gcm.scenario.RegionId, gcm.scenario.RegionId, gcm.scenario.ResourceId, long)}
	 */
	INTER_REGION_RESOURCE_TRANSFER

}