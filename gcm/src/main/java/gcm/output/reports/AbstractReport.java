package gcm.output.reports;

import java.util.Set;

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
import gcm.util.annotations.TestStatus;

/**
 * An abstract implementor of Report that implements all handle() methods. Each
 * such method throws a RuntimeException if invoked. Other methods of Report are
 * selectively implemented for convenience.
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public abstract class AbstractReport implements Report {

	@Override
	public void close(ObservableEnvironment observableEnvironment) {

	}
	
	@Override
	public void handleBatchCreation(ObservableEnvironment observableEnvironment, final BatchId batchId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleBatchDestruction(ObservableEnvironment observableEnvironment, BatchInfo batchInfo) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleBatchPropertyValueAssignment(ObservableEnvironment observableEnvironment, final BatchId batchId, final BatchPropertyId batchPropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleBatchShift(ObservableEnvironment observableEnvironment, final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleCompartmentAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final CompartmentId sourceCompartmentId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleCompartmentPropertyValueAssignment(ObservableEnvironment observableEnvironment, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGlobalPropertyValueAssignment(ObservableEnvironment observableEnvironment, final GlobalPropertyId globalPropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleMaterialsProducerPropertyValueAssignment(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleMaterialsProducerResourceAddition(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonPropertyValueAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final PersonPropertyId personPropertyId,
			final Object oldPersonPropertyValue) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonRemoval(ObservableEnvironment observableEnvironment, PersonInfo personInfo) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGroupMembershipAddition(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGroupMembershipRemoval(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonResourceAddition(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonResourceRemoval(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handlePersonResourceTransferToRegion(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final RegionId sourceRegionId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleRegionPropertyValueAssignment(ObservableEnvironment observableEnvironment, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleRegionResourceAddition(ObservableEnvironment observableEnvironment, final RegionId regionId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleRegionResourceRemoval(ObservableEnvironment observableEnvironment, final RegionId regionId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleRegionResourceTransferToPerson(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleResourcePropertyValueAssignment(ObservableEnvironment observableEnvironment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageConversionToBatch(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final BatchId batchId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageConversionToResource(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageCreation(ObservableEnvironment observableEnvironment, final StageId stageId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageDestruction(ObservableEnvironment observableEnvironment, StageInfo stageInfo) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageOfferChange(ObservableEnvironment observableEnvironment, final StageId stageId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleStageTransfer(ObservableEnvironment observableEnvironment, final StageId stageId, final MaterialsProducerId materialsProducerId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleTransferResourceBetweenRegions(ObservableEnvironment observableEnvironment, final RegionId sourceRegionId, final RegionId destinationRegionId, final ResourceId resourceId,
			final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleTransferResourceFromMaterialsProducerToRegion(ObservableEnvironment observableEnvironment, final MaterialsProducerId materialsProducerId, final RegionId regionId,
			final ResourceId resourceId, final long amount) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleUnStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId, final StageId stageId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGroupPropertyValueAssignment(ObservableEnvironment observableEnvironment, GroupId groupId, GroupPropertyId groupPropertyId, Object oldGroupPropertyValue) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGroupAddition(ObservableEnvironment observableEnvironment, GroupId groupId) {
		throwNoConcreteImplementation();
	}

	@Override
	public void handleGroupRemoval(ObservableEnvironment observableEnvironment, GroupInfo groupInfo) {
		throwNoConcreteImplementation();
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment,Set<Object> initialData) {
		
	}

	private void throwNoConcreteImplementation() {
		throw new RuntimeException("no concrete implementation " + getClass().getName());
	}
}
