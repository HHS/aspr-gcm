package gcm.output.reports.commonreports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.StateChange;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A periodic Report that displays the number of times a person transferred from
 * one region to another within a compartment. Only non-zero transfers are
 * reported.
 *
 *
 * Fields
 *
 * Compartment -- the compartment identifier
 *
 * SourceRegion -- the source region identifier
 *
 * DestinationRegion -- the destination region property identifier
 *
 * Transfers -- the number of transfers from the source region to the
 * destination region for people in the compartment
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class RegionTransferReport extends PeriodicReport {

	/*
	 * 
	 * A counter of the number of people transferring between regions.
	 *
	 */
	private static class Counter {
		int count;
	}

	/*
	 * A mapping from a (Compartment, Region, Region) tuple to a count of the number
	 * of transfers.
	 */
	private final Map<CompartmentId, Map<RegionId, Map<RegionId, Counter>>> compartmentMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
					.add("Compartment")//
					.add("SourceRegion")//
					.add("DestinationRegion")//
					.add("Transfers")//
					.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ObservableEnvironment observableEnvironment) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final CompartmentId compartmentId : compartmentMap.keySet()) {
			final Map<RegionId, Map<RegionId, Counter>> sourceRegionMap = compartmentMap.get(compartmentId);
			for (final RegionId sourceRegionId : sourceRegionMap.keySet()) {
				final Map<RegionId, Counter> destinationRegionMap = sourceRegionMap.get(sourceRegionId);
				for (final RegionId destinationRegionId : destinationRegionMap.keySet()) {
					final Counter counter = destinationRegionMap.get(destinationRegionId);
					if (counter.count > 0) {
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportType(getClass());
						reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
						reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

						buildTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(compartmentId.toString());
						reportItemBuilder.addValue(sourceRegionId.toString());
						reportItemBuilder.addValue(destinationRegionId.toString());
						reportItemBuilder.addValue(counter.count);
						observableEnvironment.releaseOutputItem(reportItemBuilder.build());
						counter.count = 0;
					}
				}
			}
		}
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.REGION_ASSIGNMENT);
		result.add(StateChange.PERSON_ADDITION);
		return result;
	}

	@Override
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId) {
		setCurrentReportingPeriod(observableEnvironment);
		final RegionId regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		increment(compartmentId, regionId, regionId);
	}

	@Override
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId,
			final RegionId sourceRegionId) {
		setCurrentReportingPeriod(observableEnvironment);
		final RegionId destinationRegionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		increment(compartmentId, sourceRegionId, destinationRegionId);
	}

	/*
	 * Increments the number of region transfers for the give tuple
	 */
	private void increment(final CompartmentId compartmentId, final RegionId sourceRegionId,
			final RegionId destinationRegionId) {
		final Counter counter = compartmentMap.get(compartmentId).get(sourceRegionId).get(destinationRegionId);
		counter.count++;
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		final Set<CompartmentId> compartmentIds = observableEnvironment.getCompartmentIds();
		final Set<RegionId> regionIds = observableEnvironment.getRegionIds();

		/*
		 * Fill the compartment map with empty counters
		 */
		for (final CompartmentId compartmentId : compartmentIds) {
			final Map<RegionId, Map<RegionId, Counter>> sourceRegionMap = new LinkedHashMap<>();
			compartmentMap.put(compartmentId, sourceRegionMap);
			for (final RegionId sourceRegionId : regionIds) {
				final Map<RegionId, Counter> destinationRegionMap = new LinkedHashMap<>();
				sourceRegionMap.put(sourceRegionId, destinationRegionMap);
				for (final RegionId destinationRegionId : regionIds) {
					final Counter counter = new Counter();
					destinationRegionMap.put(destinationRegionId, counter);
				}
			}
		}

		setCurrentReportingPeriod(observableEnvironment);
		for (PersonId personId : observableEnvironment.getPeople()) {
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(compartmentId, regionId, regionId);
		}
	}
}