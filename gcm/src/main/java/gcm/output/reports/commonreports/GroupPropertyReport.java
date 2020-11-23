package gcm.output.reports.commonreports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.GroupInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.StateChange;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A periodic Report that displays the number of groups having particular values
 * for each group property for a given group type. Only non-zero person counts
 * are reported. The report is further limited to the
 * (GroupType,GroupPropertyId) pairs contained in the
 * GroupPropertyReportSettings instance used to initialize this report.
 * 
 *
 *
 * Fields
 *
 * GroupType -- the group type of group
 *
 * Property -- the group property identifier
 *
 * Value -- the value of the property
 *
 * GroupCount -- the number of groups having the property value for the given
 * group type
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class GroupPropertyReport extends PeriodicReport {

	/**
	 * A container for (GroupType,GroupPropertyId) pairs that are used to define
	 * which group properties are reported.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static interface GroupPropertyReportSettings {
		public Set<GroupPropertyId> getGroupPropertyId(GroupTypeId groupTypeId);
	}

	/*
	 * Implementor for GroupPropertyReportSettings
	 */
	private final static class GroupPropertyReportSettingsImpl implements GroupPropertyReportSettings {

		private final Map<GroupTypeId, Set<GroupPropertyId>> propertyIdMap;

		@Override
		public Set<GroupPropertyId> getGroupPropertyId(GroupTypeId groupTypeId) {
			Set<GroupPropertyId> set = propertyIdMap.get(groupTypeId);
			if (set != null) {
				return new LinkedHashSet<>(set);
			}
			return new LinkedHashSet<>();
		}

		private GroupPropertyReportSettingsImpl(Scaffold scaffold) {
			propertyIdMap = new LinkedHashMap<>();
			for (GroupTypeId groupTypeId : scaffold.propertyIdMap.keySet()) {
				propertyIdMap.put(groupTypeId, new LinkedHashSet<>(scaffold.propertyIdMap.get(groupTypeId)));
			}
		}
	}

	/*
	 * A static class to hold the (GroupTypeId,GroupPropertyId) pairs used to limit
	 * this report
	 */
	private final static class Scaffold {
		private Map<GroupTypeId, Set<GroupPropertyId>> propertyIdMap = new LinkedHashMap<>();
	}
	
	public static GroupPropertyReportSettingsBuilder settingsBuilder() {
		return new GroupPropertyReportSettingsBuilder();
	}

	/**
	 * Builder class for GroupPropertyReportSettings
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static final class GroupPropertyReportSettingsBuilder {
		private GroupPropertyReportSettingsBuilder() {}
		
		private Scaffold scaffold = new Scaffold();

		public GroupPropertyReportSettings build() {
			try {
				return new GroupPropertyReportSettingsImpl(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		public GroupPropertyReportSettingsBuilder addGroupPropertyId(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {

			if (groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}

			if (groupPropertyId == null) {
				throw new RuntimeException("null group property id");
			}

			Set<GroupPropertyId> set = scaffold.propertyIdMap.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				scaffold.propertyIdMap.put(groupTypeId, set);
			}
			set.add(groupPropertyId);
			return this;
		}

	}

	private static class Counter {
		int count;
	}

	/*
	 * The set of (GroupTypeId,GroupPropertyId) pairs collected from the
	 * GroupPropertyReportSettings supplied during initialization
	 */
	private final Map<GroupTypeId, Set<GroupPropertyId>> clientPropertyMap = new LinkedHashMap<>();

	/*
	 * For each (GroupTypeId,GroupPropertyId,property value) triplet, count the
	 * number of groups having that triplet
	 */
	private final Map<GroupTypeId, Map<GroupPropertyId, Map<Object, Counter>>> groupTypeMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
					.add("GroupType")//
					.add("Property")//
					.add("Value")//
					.add("GroupCount")//
					.build();//
		}
		return reportHeader;
	}

	/*
	 * Decrement the number of groups for the given
	 * (GroupTypeId,GroupPropertyId,property value) triplet
	 */
	private void decrement(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId,
			final Object groupPropertyValue) {
		getCounter(groupTypeId, groupPropertyId, groupPropertyValue).count--;
	}

	@Override
	protected void flush(ObservableEnvironment observableEnvironment) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final GroupTypeId groupTypeId : groupTypeMap.keySet()) {
			final Map<GroupPropertyId, Map<Object, Counter>> propertyIdMap = groupTypeMap.get(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIdMap.keySet()) {
				final Map<Object, Counter> groupPropertyValueMap = propertyIdMap.get(groupPropertyId);
				for (final Object groupPropertyValue : groupPropertyValueMap.keySet()) {
					final Counter counter = groupPropertyValueMap.get(groupPropertyValue);
					if (counter.count > 0) {
						final int personCount = counter.count;
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportType(getClass());
						reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
						reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

						buildTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(groupTypeId.toString());
						reportItemBuilder.addValue(groupPropertyId.toString());
						reportItemBuilder.addValue(groupPropertyValue);
						reportItemBuilder.addValue(personCount);

						observableEnvironment.releaseOutputItem(reportItemBuilder.build());
					}
				}
			}
		}
	}

	private Counter getCounter(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId,
			final Object groupPropertyValue) {
		final Map<Object, Counter> propertyValueMap = groupTypeMap.get(groupTypeId).get(groupPropertyId);
		Counter counter = propertyValueMap.get(groupPropertyValue);
		if (counter == null) {
			counter = new Counter();
			propertyValueMap.put(groupPropertyValue, counter);
		}
		return counter;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.GROUP_PROPERTY_VALUE_ASSIGNMENT);
		result.add(StateChange.GROUP_ADDITION);
		result.add(StateChange.GROUP_REMOVAL);
		return result;
	}

	/*
	 * Increment the number of groups for the given
	 * (GroupTypeId,GroupPropertyId,property value) triplet
	 */
	private void increment(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId,
			final Object groupPropertyValue) {
		getCounter(groupTypeId, groupPropertyId, groupPropertyValue).count++;
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		/*
		 * Absorb the GroupPropertyReportSettings into the clientPropertyMap
		 */
		for (Object initialDatum : initialData) {
			if (initialDatum instanceof GroupPropertyReportSettings) {
				GroupPropertyReportSettings groupPropertyReportSettings = (GroupPropertyReportSettings) initialDatum;
				for (GroupTypeId groupTypeId : observableEnvironment.getGroupTypeIds()) {
					Set<GroupPropertyId> groupPropertyIds = groupPropertyReportSettings.getGroupPropertyId(groupTypeId);
					if (groupPropertyIds.size() > 0) {
						Set<GroupPropertyId> set = clientPropertyMap.get(groupTypeId);
						if (set == null) {
							clientPropertyMap.put(groupTypeId, set);
						}
						set.addAll(groupPropertyIds);
					}
				}
			}
		}

		/*
		 * Assume that the client wants all properties reported if none were specified
		 */
		if (clientPropertyMap.size() == 0) {
			for (GroupTypeId groupTypeId : observableEnvironment.getGroupTypeIds()) {
				Set<GroupPropertyId> groupPropertyIds = observableEnvironment.getGroupPropertyIds(groupTypeId);
				if (groupPropertyIds.size() > 0) {
					clientPropertyMap.put(groupTypeId, groupPropertyIds);
				}
			}
		} else {
			/*
			 * Ensure that every client supplied group type identifier is valid
			 */
			Set<GroupTypeId> validGroupTypeIds = observableEnvironment.getGroupTypeIds();
			for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
				if (!validGroupTypeIds.contains(groupTypeId)) {
					throw new RuntimeException("invalid group type id " + groupTypeId);
				}
				Set<GroupPropertyId> validPropertyIds = observableEnvironment.getGroupPropertyIds(groupTypeId);
				Set<GroupPropertyId> groupPropertyIds = clientPropertyMap.get(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					if (!validPropertyIds.contains(groupPropertyId)) {
						throw new RuntimeException(
								"invalid group property id " + groupTypeId + " for group type " + groupTypeId);
					}
				}
			}
		}

		/*
		 * Fill the top layers of the groupTypeMap. We do not yet know the set of
		 * property values, so we leave that layer empty.
		 *
		 */

		for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
			final Map<GroupPropertyId, Map<Object, Counter>> propertyIdMap = new LinkedHashMap<>();
			groupTypeMap.put(groupTypeId, propertyIdMap);
			Set<GroupPropertyId> groupPropertyIds = clientPropertyMap.get(groupTypeId);
			for (final GroupPropertyId groupPropertyId : groupPropertyIds) {
				final Map<Object, Counter> propertyValueMap = new LinkedHashMap<>();
				propertyIdMap.put(groupPropertyId, propertyValueMap);
			}
		}

		setCurrentReportingPeriod(observableEnvironment);

		// group addition
		for (GroupId groupId : observableEnvironment.getGroupIds()) {
			final GroupTypeId groupTypeId = observableEnvironment.getGroupType(groupId);
			if (clientPropertyMap.containsKey(groupTypeId)) {
				for (final GroupPropertyId groupPropertyId : clientPropertyMap.get(groupTypeId)) {
					final Object groupPropertyValue = observableEnvironment.getGroupPropertyValue(groupId,
							groupPropertyId);
					increment(groupTypeId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}

	@Override
	public void handleGroupPropertyValueAssignment(ObservableEnvironment observableEnvironment, GroupId groupId,
			GroupPropertyId groupPropertyId, Object oldGroupPropertyValue) {
		setCurrentReportingPeriod(observableEnvironment);
		final GroupTypeId groupTypeId = observableEnvironment.getGroupType(groupId);
		if (clientPropertyMap.containsKey(groupTypeId)) {
			if (clientPropertyMap.get(groupTypeId).contains(groupPropertyId)) {
				setCurrentReportingPeriod(observableEnvironment);
				final Object currentValue = observableEnvironment.getGroupPropertyValue(groupId, groupPropertyId);
				increment(groupTypeId, groupPropertyId, currentValue);
				decrement(groupTypeId, groupPropertyId, oldGroupPropertyValue);
			}
		}

	}

	@Override
	public void handleGroupAddition(ObservableEnvironment observableEnvironment, GroupId groupId) {
		setCurrentReportingPeriod(observableEnvironment);
		final GroupTypeId groupTypeId = observableEnvironment.getGroupType(groupId);
		if (clientPropertyMap.containsKey(groupTypeId)) {
			for (final GroupPropertyId groupPropertyId : clientPropertyMap.get(groupTypeId)) {
				final Object groupPropertyValue = observableEnvironment.getGroupPropertyValue(groupId, groupPropertyId);
				increment(groupTypeId, groupPropertyId, groupPropertyValue);
			}
		}
	}

	@Override
	public void handleGroupRemoval(ObservableEnvironment observableEnvironment, GroupInfo groupInfo) {
		setCurrentReportingPeriod(observableEnvironment);
		final GroupTypeId groupTypeId = groupInfo.getGroupTypeId();
		if (clientPropertyMap.containsKey(groupTypeId)) {
			Map<GroupPropertyId, Object> propertyValues = groupInfo.getPropertyValues();
			for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
				if (clientPropertyMap.get(groupTypeId).contains(groupPropertyId)) {
					final Object groupPropertyValue = propertyValues.get(groupPropertyId);
					decrement(groupTypeId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}
}