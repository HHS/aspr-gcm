package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.StateChange;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays assigned materials producer property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the materials producer property was set
 *
 * MaterialsProducer -- the materials producer identifier
 *
 * Property -- the region property identifier
 *
 * Value -- the value of the region property
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class MaterialsProducerPropertyReport extends AbstractReport {

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader.builder()//
					.add("Time")//
					.add("MaterialsProducer")//
					.add("Property")//
					.add("Value")//
					.build();//
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT);
		return result;
	}

	@Override
	public void handleMaterialsProducerPropertyValueAssignment(ObservableEnvironment observableEnvironment,
			final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		writeProperty(observableEnvironment, materialsProducerId, materialsProducerPropertyId);
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);
		for (final MaterialsProducerId materialsProducerId : observableEnvironment.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : observableEnvironment
					.getMaterialsProducerPropertyIds()) {
				writeProperty(observableEnvironment, materialsProducerId, materialsProducerPropertyId);
			}
		}
	}

	private void writeProperty(ObservableEnvironment observableEnvironment,
			final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId) {

		final Object materialsProducerPropertyValue = observableEnvironment
				.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyValue);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

}