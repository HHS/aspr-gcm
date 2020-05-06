package gcm.output.reports.commonreports;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays assigned compartment property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the compartment property was set
 *
 * Compartment -- the compartment identifier
 *
 * Property -- the compartment property identifier
 *
 * Value -- the value of the compartment property
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class CompartmentPropertyReport extends AbstractReport {
	

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Compartment");
			reportHeaderBuilder.add("Property");
			reportHeaderBuilder.add("Value");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;

	}
	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT);
		return result;
	}

	@Override
	public void handleCompartmentPropertyValueAssignment(ObservableEnvironment observableEnvironment,final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		writeProperty(observableEnvironment,compartmentId, compartmentPropertyId);
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment,Set<Object> initialData) {
		super.init(observableEnvironment,initialData);
		for (final CompartmentId compartmentId : observableEnvironment.getCompartmentIds()) {
			for (final CompartmentPropertyId compartmentPropertyId : observableEnvironment.getCompartmentPropertyIds(compartmentId)) {
				writeProperty(observableEnvironment,compartmentId, compartmentPropertyId);
			}
		}
		
	   observableEnvironment.getCompartmentIds().forEach(compartmentId->{
		   observableEnvironment.getCompartmentPropertyIds(compartmentId).forEach(compartmentPropertyId->{
			   writeProperty(observableEnvironment, compartmentId, compartmentPropertyId);
		   });
	   });
	}

	private void writeProperty(ObservableEnvironment observableEnvironment,final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader());
		final Object compartmentPropertyValue = observableEnvironment.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(compartmentId.toString());
		reportItemBuilder.addValue(compartmentPropertyId.toString());
		reportItemBuilder.addValue(compartmentPropertyValue);
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

}