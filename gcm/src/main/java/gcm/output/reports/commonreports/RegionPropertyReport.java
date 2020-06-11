package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.output.reports.StateChange;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays assigned region property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the region property was set
 *
 * Region -- the region identifier
 *
 * Property -- the region property identifier
 *
 * Value -- the value of the region property
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class RegionPropertyReport extends AbstractReport {

	private ReportHeader reportHeader;

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<RegionPropertyId> regionPropertyIds = new LinkedHashSet<>();

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Region");
			reportHeaderBuilder.add("Property");
			reportHeaderBuilder.add("Value");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.REGION_PROPERTY_VALUE_ASSIGNMENT);
		return result;
	}

	@Override
	public void handleRegionPropertyValueAssignment(ObservableEnvironment observableEnvironment, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		if (regionPropertyIds.contains(regionPropertyId)) {
			writeProperty(observableEnvironment, regionId, regionPropertyId);
		}
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		for (Object initialDatum : initialData) {
			if (initialDatum instanceof RegionPropertyId) {
				RegionPropertyId regionPropertyId = (RegionPropertyId) initialDatum;
				regionPropertyIds.add(regionPropertyId);
			}
		}

		/*
		 * If no region properties were specified, then assume all are wanted
		 */
		if (regionPropertyIds.size() == 0) {
			regionPropertyIds.addAll(observableEnvironment.getRegionPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<RegionPropertyId> validPropertyIds = observableEnvironment.getRegionPropertyIds();
		for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
			if (!validPropertyIds.contains(regionPropertyId)) {
				throw new RuntimeException("invalid property id " + regionPropertyId);
			}
		}

		for (final RegionId regionId : observableEnvironment.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
				writeProperty(observableEnvironment, regionId, regionPropertyId);
			}
		}

	}

	private void writeProperty(ObservableEnvironment observableEnvironment, final RegionId regionId, final RegionPropertyId regionPropertyId) {

		final Object regionPropertyValue = observableEnvironment.getRegionPropertyValue(regionId, regionPropertyId);
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

}