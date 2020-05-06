package gcm.output.reports.commonreports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.GroupInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A periodic Report that displays the number of groups having a particular
 * number of people for a given group type.
 *
 * Fields
 *
 * GroupType -- the group type of group
 *
 * PersonCount -- the number of people in each group
 * 
 * GroupCount -- the number of groups having the person count
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class GroupPopulationReport extends PeriodicReport {

	/*
	 * 
	 * Count of the number of groups having a particular person count for a particular group type
	 *
	 */
	private static class Counter {
		int count;
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			addTimeFieldHeaders(reportHeaderBuilder);
			reportHeaderBuilder.add("GroupType");
			reportHeaderBuilder.add("PersonCount");
			reportHeaderBuilder.add("GroupCount");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	protected void flush(ObservableEnvironment observableEnvironment) {

		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();

		/*
		 * Count the number of groups of each size that exist for each group type
		 */
		Map<GroupTypeId, Map<Integer, Counter>> groupTypePopulationMap = new LinkedHashMap<>();
		for (GroupTypeId groupTypeId : observableEnvironment.getGroupTypeIds()) {
			Map<Integer, Counter> groupSizeMap = new LinkedHashMap<>();
			groupTypePopulationMap.put(groupTypeId, groupSizeMap);
			for (GroupId groupId : observableEnvironment.getGroupsForGroupType(groupTypeId)) {
				Integer personCountForGroup = observableEnvironment.getPersonCountForGroup(groupId);
				Counter counter = groupSizeMap.get(personCountForGroup);
				if (counter == null) {
					counter = new Counter();
					groupSizeMap.put(personCountForGroup, counter);
				}
				counter.count++;
			}
		}

		/*
		 * Report the collected group counters
		 */
		for (final GroupTypeId groupTypeId : groupTypePopulationMap.keySet()) {
			Map<Integer, Counter> groupSizeMap = groupTypePopulationMap.get(groupTypeId);
			for (final Integer personCount : groupSizeMap.keySet()) {
				Counter counter = groupSizeMap.get(personCount);

				final int groupCount = counter.count;
				reportItemBuilder.setReportHeader(getReportHeader());
				reportItemBuilder.setReportType(getClass());
				reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
				reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
				buildTimeFields(reportItemBuilder);
				reportItemBuilder.addValue(groupTypeId.toString());
				reportItemBuilder.addValue(personCount);
				reportItemBuilder.addValue(groupCount);

				observableEnvironment.releaseOutputItem(reportItemBuilder.build());

			}
		}

	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.GROUP_ADDITION);
		result.add(StateChange.GROUP_REMOVAL);
		result.add(StateChange.GROUP_MEMBERSHIP_ADDITION);
		result.add(StateChange.GROUP_MEMBERSHIP_REMOVAL);
		return result;
	}

	@Override
	public void handleGroupAddition(ObservableEnvironment observableEnvironment, GroupId groupId) {
		setCurrentReportingPeriod(observableEnvironment);
	}

	@Override
	public void handleGroupRemoval(ObservableEnvironment observableEnvironment, GroupInfo groupInfo) {
		setCurrentReportingPeriod(observableEnvironment);
	}

	@Override
	public void handleGroupMembershipAddition(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		setCurrentReportingPeriod(observableEnvironment);
	}

	@Override
	public void handleGroupMembershipRemoval(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		setCurrentReportingPeriod(observableEnvironment);
	}

}