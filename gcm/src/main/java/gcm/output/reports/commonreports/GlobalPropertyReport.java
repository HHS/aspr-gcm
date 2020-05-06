package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.GlobalPropertyId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays assigned global property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global property was set
 *
 * Property -- the global property identifier
 *
 * Value -- the value of the global property
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class GlobalPropertyReport extends AbstractReport {

	private ReportHeader reportHeader;

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<GlobalPropertyId> globalPropertyIds = new LinkedHashSet<>();

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Property");
			reportHeaderBuilder.add("Value");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.GLOBAL_PROPERTY_VALUE_ASSIGNMENT);
		return result;
	}

	@Override
	public void handleGlobalPropertyValueAssignment(ObservableEnvironment observableEnvironment, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyIds.contains(globalPropertyId)) {
			writeProperty(observableEnvironment, globalPropertyId);
		}
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		for (Object initialDatum : initialData) {
			if (initialDatum instanceof GlobalPropertyId) {
				GlobalPropertyId globalPropertyId = (GlobalPropertyId) initialDatum;
				globalPropertyIds.add(globalPropertyId);
			}
		}

		/*
		 * If no global properties were specified, then assume all are wanted
		 */
		if (globalPropertyIds.size() == 0) {
			globalPropertyIds.addAll(observableEnvironment.getGlobalPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<GlobalPropertyId> validPropertyIds = observableEnvironment.getGlobalPropertyIds();
		for (final GlobalPropertyId globalPropertyId : globalPropertyIds) {
			if (!validPropertyIds.contains(globalPropertyId)) {
				throw new RuntimeException("invalid property id " + globalPropertyId);
			}
		}

		for (final GlobalPropertyId globalPropertyId : globalPropertyIds) {
			writeProperty(observableEnvironment, globalPropertyId);
		}
	}

	private void writeProperty(ObservableEnvironment observableEnvironment, final GlobalPropertyId globalPropertyId) {
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader());

		final Object globalPropertyValue = observableEnvironment.getGlobalPropertyValue(globalPropertyId);
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(globalPropertyId.toString());
		reportItemBuilder.addValue(globalPropertyValue);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

}