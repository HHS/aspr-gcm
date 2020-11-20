package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.StageInfo;
import gcm.output.reports.StateChange;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays materials producer resource changes over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the materials producer resource level was set
 * 
 * Resource -- the resource identifier
 *
 * MaterialsProducer -- the materials producer identifier
 *
 * Action -- the action taken on the resource
 *
 * Amount -- the amount of resource
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class MaterialsProducerResourceReport extends AbstractReport {

	private static enum Action {
		/*
		 * Used when a resource is directly added to a materials producer which only
		 * happens when the materials producer is being initialized from the scenario
		 */
		ADDED("Created"),
		/*
		 * Used when a stage is converted to a resource
		 * 
		 */
		CONVERTED("ResourceConverted"),

		/*
		 * Used when a materials producer transfers resource to a region
		 */

		TRANSFERRED("Transferred");

		private final String displayName;

		private Action(final String displayName) {
			this.displayName = displayName;
		}
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader.builder()//
					.add("Time")//
					.add("Resource")//
					.add("MaterialsProducer")//
					.add("Action")//
					.add("Amount")//
					.build();//
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.STAGE_CONVERTED_TO_RESOURCE);
		result.add(StateChange.MATERIALS_PRODUCER_RESOURCE_ADDITION);
		result.add(StateChange.MATERIALS_PRODUCER_RESOURCE_TRANSFER);
		return result;
	}

	@Override
	public void handleMaterialsProducerResourceAddition(ObservableEnvironment observableEnvironment,
			final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		writeReportItem(observableEnvironment, resourceId, materialsProducerId, Action.ADDED, amount);
	}

	@Override
	public void handleStageConversionToResource(ObservableEnvironment observableEnvironment, StageInfo stageInfo,
			final ResourceId resourceId, final long amount) {
		writeReportItem(observableEnvironment, resourceId, stageInfo.getMaterialsProducerId(), Action.CONVERTED,
				amount);
	}

	@Override
	public void handleTransferResourceFromMaterialsProducerToRegion(ObservableEnvironment observableEnvironment,
			final MaterialsProducerId materialsProducerId, final RegionId regionId, final ResourceId resourceId,
			final long amount) {
		writeReportItem(observableEnvironment, resourceId, materialsProducerId, Action.TRANSFERRED, amount);
	}

	private void writeReportItem(ObservableEnvironment observableEnvironment, final ResourceId resourceId,
			final MaterialsProducerId materialsProducerId, final Action action, final double amount) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(amount);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		for (MaterialsProducerId materialsProducerId : observableEnvironment.getMaterialsProducerIds()) {
			for (ResourceId resourceId : observableEnvironment.getResourceIds()) {
				long materialsProducerResourceLevel = observableEnvironment
						.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				writeReportItem(observableEnvironment, resourceId, materialsProducerId, Action.ADDED,
						materialsProducerResourceLevel);
			}
		}
	}

}
