package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StageInfo;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.BatchId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.ResourceId;
import gcm.scenario.StageId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays the creation, destruction, offering, batch conversion,
 * resource conversion and transfer of stages.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global resource was set
 *
 * Stage -- the stage identifier
 *
 * MaterialsProducer -- the acting materials producer
 *
 * Action -- One of Create, Destroy, Offer, BatchConversion, ResourceConversion,
 * Transfer
 * 
 * Offered -- the offered state of the stage
 * 
 * ResourceMaterial
 * 
 * Amount
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class StageReport extends AbstractReport {

	/*
	 * An enumeration mirroring the cause of a change to a stage
	 */
	private static enum Action {
		CREATED("Create"),

		DESTROYED("Destroy"),

		OFFERED("Offer"),

		BATCH_CONVERTED("BatchConversion"),

		RESOURCE_CONVERTED("ResourceConversion"),

		TRANSFERRED("Transfer");

		private final String displayName;

		private Action(final String displayName) {
			this.displayName = displayName;
		}
	}

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Stage");
			reportHeaderBuilder.add("MaterialsProducer");
			reportHeaderBuilder.add("Action");
			reportHeaderBuilder.add("Offered");
			reportHeaderBuilder.add("ResourceMaterial");
			reportHeaderBuilder.add("Amount");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.STAGE_CONVERTED_TO_BATCH);
		result.add(StateChange.STAGE_CREATION);
		result.add(StateChange.STAGE_DESTRUCTION);
		result.add(StateChange.STAGE_OFFERED);
		result.add(StateChange.STAGE_TRANSFERRED);
		result.add(StateChange.STAGE_CONVERTED_TO_RESOURCE);
		return result;
	}

	@Override
	public void handleStageConversionToBatch(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final BatchId batchId) {
		final Object resourceOrMaterial = observableEnvironment.getBatchMaterial(batchId);
		final double amount = observableEnvironment.getBatchAmount(batchId);
		writeReportItem(observableEnvironment, stageInfo.getStageId(), stageInfo.getMaterialsProducerId(), Action.BATCH_CONVERTED, stageInfo.isStageOffered(), resourceOrMaterial, amount);
	}

	@Override
	public void handleStageConversionToResource(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final ResourceId resourceId, final long amount) {
		writeReportItem(observableEnvironment, stageInfo.getStageId(), stageInfo.getMaterialsProducerId(), Action.RESOURCE_CONVERTED, stageInfo.isStageOffered(), resourceId, amount);
	}

	@Override
	public void handleStageCreation(ObservableEnvironment observableEnvironment, final StageId stageId) {
		final Object resourceOrMaterial = "";
		final boolean offered = observableEnvironment.isStageOffered(stageId);
		final MaterialsProducerId materialsProducerId = observableEnvironment.getStageProducer(stageId);
		writeReportItem(observableEnvironment, stageId, materialsProducerId, Action.CREATED, offered, resourceOrMaterial, 0);
	}

	@Override
	public void handleStageDestruction(ObservableEnvironment observableEnvironment, StageInfo stageInfo) {
		final Object resourceOrMaterial = "";
		writeReportItem(observableEnvironment, stageInfo.getStageId(), stageInfo.getMaterialsProducerId(), Action.DESTROYED, stageInfo.isStageOffered(), resourceOrMaterial, 0);
	}

	@Override
	public void handleStageOfferChange(ObservableEnvironment observableEnvironment, final StageId stageId) {
		final Object resourceOrMaterial = "";
		final boolean offered = observableEnvironment.isStageOffered(stageId);
		final MaterialsProducerId materialsProducerId = observableEnvironment.getStageProducer(stageId);
		writeReportItem(observableEnvironment, stageId, materialsProducerId, Action.OFFERED, offered, resourceOrMaterial, 0);
	}

	@Override
	public void handleStageTransfer(ObservableEnvironment observableEnvironment, final StageId stageId, final MaterialsProducerId materialsProducerId) {
		final boolean offered = observableEnvironment.isStageOffered(stageId);
		final Object resourceOrMaterial = "";
		writeReportItem(observableEnvironment, stageId, materialsProducerId, Action.TRANSFERRED, offered, resourceOrMaterial, 0);
		final MaterialsProducerId newMaterialsProducerId = observableEnvironment.getStageProducer(stageId);
		writeReportItem(observableEnvironment, stageId, newMaterialsProducerId, Action.TRANSFERRED, offered, resourceOrMaterial, 0);
	}

	private void writeReportItem(ObservableEnvironment observableEnvironment, final StageId stageId, final MaterialsProducerId materialsProducerId, final Action action, final Boolean offered,
			final Object resourceOrMaterial, final double amount) {
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(stageId);
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(offered);
		reportItemBuilder.addValue(resourceOrMaterial.toString());
		reportItemBuilder.addValue(amount);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		final Object resourceOrMaterial = "";
		for (MaterialsProducerId materialsProducerId : observableEnvironment.getMaterialsProducerIds()) {
			for (StageId stageId : observableEnvironment.getStages(materialsProducerId)) {
				final boolean offered = observableEnvironment.isStageOffered(stageId);
				writeReportItem(observableEnvironment, stageId, materialsProducerId, Action.CREATED, offered, resourceOrMaterial, 0);
			}
		}
	}

}
