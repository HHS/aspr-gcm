package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays assigned resource property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global resource was set
 *
 * Resource -- the resource identifier
 *
 * Property -- the resource property identifier
 *
 * Value -- the value of the resource property
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class ResourcePropertyReport extends AbstractReport {

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Resource");
			reportHeaderBuilder.add("Property");
			reportHeaderBuilder.add("Value");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.RESOURCE_PROPERTY_VALUE_ASSIGNMENT);
		return result;
	}

	@Override
	public void handleResourcePropertyValueAssignment(ObservableEnvironment observableEnvironment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		writeProperty(observableEnvironment, resourceId, resourcePropertyId);
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);
		for (final ResourceId resourceId : observableEnvironment.getResourceIds()) {
			for (final ResourcePropertyId resourcePropertyId : observableEnvironment.getResourcePropertyIds(resourceId)) {
				writeProperty(observableEnvironment, resourceId, resourcePropertyId);
			}
		}
	}

	private void writeProperty(ObservableEnvironment observableEnvironment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {

		final Object resourcePropertyValue = observableEnvironment.getResourcePropertyValue(resourceId, resourcePropertyId);
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(resourcePropertyId.toString());
		reportItemBuilder.addValue(resourcePropertyValue);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

}