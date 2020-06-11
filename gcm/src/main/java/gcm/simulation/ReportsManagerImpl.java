package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.output.OutputItemHandler;
import gcm.output.reports.BatchInfo;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.PersonInfo;
import gcm.output.reports.Report;
import gcm.output.reports.ReportItemHandler;
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
import gcm.util.annotations.TestStatus;

/**
 * Reports manager for GCM. As the Environment mutates data, it informs this
 * manager which maps the information to the reports that are registered to
 * observe those changes.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class ReportsManagerImpl extends BaseElement implements ReportsManager {

	private final Map<Report, Set<Object>> reports = new LinkedHashMap<>();

	private ObservableEnvironment observableEnvironment;

	@Override
	public void init(Context context) {
		super.init(context);

		observableEnvironment = context.getObservableEnvironment();

		// Establish that there is an NIOReportItemHandler listening for
		// ReportItems. If there is none, then there is no point in setting up
		// the reports.
		ReportItemHandler reportItemHandler = null;
		for (OutputItemHandler outputItemHandler : context.getOutputItemHandlers()) {
			if (outputItemHandler instanceof ReportItemHandler) {
				reportItemHandler = (ReportItemHandler) outputItemHandler;
			}
		}

		if (reportItemHandler == null) {
			return;
		}

		/*
		 * If the profile report is active, then we will need to get a
		 * proxy-wrapped instance of each report so that profile reporting will
		 * cover the details of the reports.
		 */
		if (context.produceProfileItems()) {
			ProfileManager profileManager = context.getProfileManager();
			for (Report report : reportItemHandler.getReports()) {
				Report profiledProxyReport = profileManager.getProfiledProxy(report);
				Set<Object> initializationData = reportItemHandler.getInitializationData(report);
				reports.put(profiledProxyReport, initializationData);
			}
		} else {
			for (Report report : reportItemHandler.getReports()) {
				Set<Object> initializationData = reportItemHandler.getInitializationData(report);
				reports.put(report, initializationData);
			}
		}

		/*
		 * Fill the reportMap so that we can map events(StateChanges) to the
		 * reports that are listening for those events.
		 */
		for (Report report : reports.keySet()) {
			Set<StateChange> listenedStateChanges = report.getListenedStateChanges();
			for (final StateChange stateChange : listenedStateChanges) {
				Set<Report> set = reportMap.get(stateChange);
				if (set == null) {
					set = new LinkedHashSet<>();
					reportMap.put(stateChange, set);
				}
				set.add(report);
			}
		}
		
		for (Report report : reports.keySet()) {
			report.init(observableEnvironment, reports.get(report));
		}
	}

	@Override
	public void closeReports() {
		for (Report report : reports.keySet()) {
			report.close(observableEnvironment);
		}
	}

	/*
	 * Maps the reports to the various state changes. Recall that reports can
	 * register for more than one state change.
	 */
	private Map<StateChange, Set<Report>> reportMap = new LinkedHashMap<>();

	@Override
	public void handleBatchCreation(final BatchId batchId) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_CREATION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleBatchCreation(observableEnvironment, batchId);
			}
		}
	}

	@Override
	public void handleBatchDestruction(BatchInfo batchInfo) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_DESTRUCTION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleBatchDestruction(observableEnvironment, batchInfo);
			}
		}
	}

	@Override
	public boolean hasBatchDestructionReports() {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_DESTRUCTION);
		return (reports != null) && (reports.size() > 0);
	}

	@Override
	public boolean hasStageDestructionReports() {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_DESTRUCTION);
		return (reports != null) && (reports.size() > 0);
	}

	@Override
	public void handleBatchPropertyValueAssignment(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleBatchPropertyValueAssignment(observableEnvironment, batchId, batchPropertyId);
			}
		}
	}

	@Override
	public void handleBatchShift(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_SHIFT);
		if (reports != null) {

			for (final Report report : reports) {
				report.handleBatchShift(observableEnvironment, sourceBatchId, destinationBatchId, amount);
			}
		}
	}

	@Override
	public void handleCompartmentAssignment(final PersonId personId, final CompartmentId sourceCompartmentId) {
		final Set<Report> reports = reportMap.get(StateChange.COMPARTMENT_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleCompartmentAssignment(observableEnvironment, personId, sourceCompartmentId);
			}
		}
	}

	@Override
	public void handleCompartmentPropertyValueAssignment(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleCompartmentPropertyValueAssignment(observableEnvironment, compartmentId, compartmentPropertyId);
			}
		}
	}

	@Override
	public void handleGlobalPropertyValueAssignment(final GlobalPropertyId globalPropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.GLOBAL_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGlobalPropertyValueAssignment(observableEnvironment, globalPropertyId);
			}
		}
	}

	@Override
	public void handleMaterialsProducerPropertyValueAssignment(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleMaterialsProducerPropertyValueAssignment(observableEnvironment, materialsProducerId, materialsProducerPropertyId);
			}
		}
	}

	@Override
	public void handleMaterialsProducerResourceAddition(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.MATERIALS_PRODUCER_RESOURCE_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleMaterialsProducerResourceAddition(observableEnvironment, materialsProducerId, resourceId, amount);
			}
		}
	}

	@Override
	public void handlePersonAddition(final PersonId personId) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonAddition(observableEnvironment, personId);
			}
		}
	}

	@Override
	public void handleGroupMembershipAddition(final GroupId groupId, final PersonId personId) {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_MEMBERSHIP_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGroupMembershipAddition(observableEnvironment, groupId, personId);
			}
		}
	}

	@Override
	public void handleGroupMembershipRemoval(final GroupId groupId, final PersonId personId) {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_MEMBERSHIP_REMOVAL);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGroupMembershipRemoval(observableEnvironment, groupId, personId);
			}
		}
	}

	@Override
	public void handlePersonPropertyValueAssignment(final PersonId personId, final PersonPropertyId personPropertyId, final Object oldPersonPropertyValue) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonPropertyValueAssignment(observableEnvironment, personId, personPropertyId, oldPersonPropertyValue);
			}
		}
	}

	@Override
	public void handleGroupPropertyValueAssignment(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object oldGroupPropertyValue) {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGroupPropertyValueAssignment(observableEnvironment, groupId, groupPropertyId, oldGroupPropertyValue);
			}
		}
	}

	@Override
	public void handlePersonRemoval(PersonInfo personInfo) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_REMOVAL);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonRemoval(observableEnvironment, personInfo);
			}
		}
	}

	@Override
	public void handleGroupRemoval(GroupInfo groupInfo) {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_REMOVAL);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGroupRemoval(observableEnvironment, groupInfo);
			}
		}
	}

	@Override
	public void handleGroupAddition(GroupId groupId) {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleGroupAddition(observableEnvironment, groupId);
			}
		}
	}

	@Override
	public boolean hasPersonRemovalReports() {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_REMOVAL);
		return (reports != null) && (reports.size() > 0);
	}

	@Override
	public boolean hasGroupRemovalReports() {
		final Set<Report> reports = reportMap.get(StateChange.GROUP_REMOVAL);
		return (reports != null) && (reports.size() > 0);
	}

	@Override
	public void handlePersonResourceAddition(final PersonId personId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_RESOURCE_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonResourceAddition(observableEnvironment, personId, resourceId, amount);
			}
		}
	}

	@Override
	public void handlePersonResourceRemoval(final PersonId personId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_RESOURCE_REMOVAL);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonResourceRemoval(observableEnvironment, personId, resourceId, amount);
			}
		}
	}

	@Override
	public void handlePersonResourceTransferToRegion(final PersonId personId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.PERSON_RESOURCE_TRANSFER_TO_REGION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handlePersonResourceTransferToRegion(observableEnvironment, personId, resourceId, amount);
			}
		}
	}

	@Override
	public void handleRegionAssignment(final PersonId personId, final RegionId sourceRegionId) {
		final Set<Report> reports = reportMap.get(StateChange.REGION_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleRegionAssignment(observableEnvironment, personId, sourceRegionId);
			}
		}
	}

	@Override
	public void handleRegionPropertyValueAssignment(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.REGION_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleRegionPropertyValueAssignment(observableEnvironment, regionId, regionPropertyId);
			}
		}
	}

	@Override
	public void handleRegionResourceAddition(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.REGION_RESOURCE_ADDITION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleRegionResourceAddition(observableEnvironment, regionId, resourceId, amount);
			}
		}
	}

	@Override
	public void handleRegionResourceRemoval(final RegionId regionId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.REGION_RESOURCE_REMOVAL);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleRegionResourceRemoval(observableEnvironment, regionId, resourceId, amount);
			}
		}
	}

	@Override
	public void handleRegionResourceTransferToPerson(final PersonId personId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.REGION_RESOURCE_TRANSFER_TO_PERSON);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleRegionResourceTransferToPerson(observableEnvironment, personId, resourceId, amount);
			}
		}
	}

	@Override
	public void handleResourcePropertyValueAssignment(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Set<Report> reports = reportMap.get(StateChange.RESOURCE_PROPERTY_VALUE_ASSIGNMENT);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleResourcePropertyValueAssignment(observableEnvironment, resourceId, resourcePropertyId);
			}
		}
	}

	@Override
	public void handleStageConversionToBatch(StageInfo stageInfo, BatchId batchId) {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_CONVERTED_TO_BATCH);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageConversionToBatch(observableEnvironment, stageInfo, batchId);
			}
		}
	}

	@Override
	public void handleStageConversionToResource(StageInfo stageInfo, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_CONVERTED_TO_RESOURCE);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageConversionToResource(observableEnvironment, stageInfo, resourceId, amount);
			}
		}
	}

	@Override
	public void handleStageCreation(final StageId stageId) {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_CREATION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageCreation(observableEnvironment, stageId);
			}
		}
	}

	@Override
	public void handleStagedBatch(final BatchId batchId) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_STAGED);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStagedBatch(observableEnvironment, batchId);
			}
		}
	}

	@Override
	public void handleStageDestruction(StageInfo stageInfo) {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_DESTRUCTION);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageDestruction(observableEnvironment, stageInfo);
			}
		}
	}

	@Override
	public void handleStageOfferChange(final StageId stageId) {
		final Set<Report> reports = reportMap.get(StateChange.STAGE_OFFERED);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageOfferChange(observableEnvironment, stageId);
			}
		}
	}

	@Override
	public void handleStageTransfer(final StageId stageId, final MaterialsProducerId materialsProducerId) {
		Set<Report> reports = reportMap.get(StateChange.STAGE_TRANSFERRED);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleStageTransfer(observableEnvironment, stageId, materialsProducerId);
			}
		}
	}

	@Override
	public void handleTransferResourceBetweenRegions(final RegionId sourceRegionId, final RegionId destinationRegionId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.INTER_REGION_RESOURCE_TRANSFER);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleTransferResourceBetweenRegions(observableEnvironment, sourceRegionId, destinationRegionId, resourceId, amount);
			}
		}
	}

	@Override
	public void handleTransferResourceFromMaterialsProducerToRegion(final MaterialsProducerId materialsProducerId, final RegionId regionId, final ResourceId resourceId, final long amount) {
		final Set<Report> reports = reportMap.get(StateChange.MATERIALS_PRODUCER_RESOURCE_TRANSFER);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleTransferResourceFromMaterialsProducerToRegion(observableEnvironment, materialsProducerId, regionId, resourceId, amount);
			}
		}

	}

	@Override
	public void handleUnStagedBatch(final BatchId batchId, final StageId stageId) {
		final Set<Report> reports = reportMap.get(StateChange.BATCH_UNSTAGED);
		if (reports != null) {
			for (final Report report : reports) {
				report.handleUnStagedBatch(observableEnvironment, batchId, stageId);
			}
		}
	}

}
