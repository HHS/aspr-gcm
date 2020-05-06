package gcm.output.reports.commonreports;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.PersonInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A periodic Report that displays the number of people exhibiting a tuple of
 * person property values for a given region/compartment pair. Only non-zero
 * person counts are reported.
 *
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * Compartment -- the compartment identifier
 *
 * [PropertyIds] -- the tuple of property field values
 *
 * PersonCount -- the number of people having the tuple of property values
 * within the region/compartment pair
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class PersonPropertyInteractionReport extends PeriodicReport {

	/*
	 * Represents a count of people in a particular region, particular
	 * compartment and having a particular tuple of property values.
	 */
	private static class Counter {
		int count;
	}

	private final List<PersonPropertyId> propertyIds = new ArrayList<>();

	/*
	 * Map of <Region, Map<Compartment, Map<Property Value, ... Map<Property
	 * Value,Counter>...>>>
	 * 
	 * A map of map of map... that starts with regions, compartment, each
	 * property id in order and ends with Counter
	 */

	private final Map<Object, Object> regionMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	/*
	 * Returns the report header for this report having columns for the selected
	 * property id values
	 */
	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			addTimeFieldHeaders(reportHeaderBuilder);

			reportHeaderBuilder.add("Region");
			reportHeaderBuilder.add("Compartment");
			for (final PersonPropertyId personPropertyId : propertyIds) {
				reportHeaderBuilder.add(personPropertyId.toString());
			}
			reportHeaderBuilder.add("PersonCount");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	/*
	 * Decrements the Counter for the given region, compartment and person
	 * property values associated with the person
	 */
	private void decrement(ObservableEnvironment observableEnvironment, final Object regionId, final CompartmentId compartmentId, final PersonId personId) {
		getCounter(observableEnvironment, regionId, compartmentId, personId, null, null).count--;
	}

	/*
	 * Decrements the Counter for the given region, compartment and person
	 * property values associated with the person with the old property value
	 * being used instead of the current property value.
	 */
	private void decrementOldPropertyValue(ObservableEnvironment observableEnvironment, final Object regionId, final CompartmentId compartmentId, final PersonId personId, final Object oldPropertyId,
			final Object oldPropertyValue) {
		getCounter(observableEnvironment, regionId, compartmentId, personId, oldPropertyId, oldPropertyValue).count--;
	}

	@Override
	protected void flush(ObservableEnvironment observableEnvironment) {

		/*
		 * For each (region,compartment) pair, execute the recursive
		 * propertyFlush
		 */
		final Object[] propertyValues = new Object[propertyIds.size()];
		for (final Object regionId : regionMap.keySet()) {
			@SuppressWarnings("unchecked")
			final Map<Object, Object> compartmentMap = (Map<Object, Object>) regionMap.get(regionId);

			for (final Object compartmentId : compartmentMap.keySet()) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> map = (Map<Object, Object>) compartmentMap.get(compartmentId);
				propertyFlush(observableEnvironment, regionId, compartmentId, map, propertyValues, 0);
			}
		}
	}

	/*
	 * Selects the counter that is accounting for the people in the compartment
	 * and region who have the same tuple of property values that the given
	 * person currently has. If the selectedPropertyId is not null, then the
	 * formerPropertyValue is used instead for forming the tuple. This is done
	 * to select the counter for the previous property value so that the counter
	 * may decremented.
	 */
	private Counter getCounter(ObservableEnvironment observableEnvironment, final Object regionId, final CompartmentId compartmentId, final PersonId personId, final Object selectedPropertyId,
			final Object formerPropertyValue) {

		/*
		 * First, push through the region map with the region and compartment to
		 * arrive at a nested map of maps for the properties
		 */
		@SuppressWarnings("unchecked")
		Map<Object, Object> compartmentMap = (Map<Object, Object>) regionMap.get(regionId);
		if (compartmentMap == null) {
			compartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, compartmentMap);
		}

		@SuppressWarnings("unchecked")
		Map<Object, Object> propertyValueMap = (Map<Object, Object>) compartmentMap.get(compartmentId);
		if (propertyValueMap == null) {
			propertyValueMap = new LinkedHashMap<>();
			compartmentMap.put(compartmentId, propertyValueMap);
		}

		/*
		 * Push downward through the mapping layers until all property values
		 * have been used. The last layer will have Counters as its values.
		 */
		final int n = propertyIds.size();
		for (int i = 0; i < n; i++) {
			final PersonPropertyId personPropertyId = propertyIds.get(i);
			Object personPropertyValue;
			/*
			 * When this method is being used to decrement a counter for a
			 * previous value of a property, we select the former property value
			 * instead of the current property value.
			 */
			if (personPropertyId.equals(selectedPropertyId)) {
				personPropertyValue = formerPropertyValue;
			} else {
				personPropertyValue = observableEnvironment.getPersonPropertyValue(personId, personPropertyId);
			}

			/*
			 * The last map level has Counters as its values. All other levels
			 * will have maps as their values.
			 */
			if (i == (n - 1)) {
				Counter counter = (Counter) propertyValueMap.get(personPropertyValue);
				if (counter == null) {
					counter = new Counter();
					propertyValueMap.put(personPropertyValue, counter);
				}
				return counter;
			}
			@SuppressWarnings("unchecked")
			Map<Object, Object> subMap = (Map<Object, Object>) propertyValueMap.get(personPropertyValue);
			if (subMap == null) {
				subMap = new LinkedHashMap<>();
				propertyValueMap.put(personPropertyValue, subMap);
			}
			propertyValueMap = subMap;
		}
		return null;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.PERSON_PROPERTY_VALUE_ASSIGNMENT);
		result.add(StateChange.COMPARTMENT_ASSIGNMENT);
		result.add(StateChange.REGION_ASSIGNMENT);
		result.add(StateChange.PERSON_ADDITION);
		result.add(StateChange.PERSON_REMOVAL);

		return result;
	}

	@Override
	public void handleCompartmentAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final CompartmentId sourceCompartmentId) {
		setCurrentReportingPeriod(observableEnvironment);

		final Object regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);

		increment(observableEnvironment, regionId, compartmentId, personId);
		decrement(observableEnvironment, regionId, sourceCompartmentId, personId);
	}

	@Override
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId) {
		setCurrentReportingPeriod(observableEnvironment);
		final Object regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		increment(observableEnvironment, regionId, compartmentId, personId);
	}

	@Override
	public void handlePersonPropertyValueAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final PersonPropertyId personPropertyId, final Object oldValue) {
		if (propertyIds.contains(personPropertyId)) {
			setCurrentReportingPeriod(observableEnvironment);
			final Object regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(observableEnvironment, regionId, compartmentId, personId);
			decrementOldPropertyValue(observableEnvironment, regionId, compartmentId, personId, personPropertyId, oldValue);
		}
	}

	@Override
	public void handlePersonRemoval(ObservableEnvironment observableEnvironment, PersonInfo personInfo) {
		setCurrentReportingPeriod(observableEnvironment);
		decrement(observableEnvironment, personInfo.getRegionId(), personInfo.getCompartmentId(), personInfo.getPersonId());
	}

	@Override
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final RegionId sourceRegionId) {
		setCurrentReportingPeriod(observableEnvironment);
		final Object regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		increment(observableEnvironment, regionId, compartmentId, personId);
		decrement(observableEnvironment, sourceRegionId, compartmentId, personId);
	}

	/*
	 * Increments the Counter for the given region, compartment and person
	 * property values associated with the person
	 */
	private void increment(ObservableEnvironment observableEnvironment, final Object regionId, final CompartmentId compartmentId, final PersonId personId) {
		getCounter(observableEnvironment, regionId, compartmentId, personId, null, null).count++;
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		/*
		 * Find the person property ids selected by the client
		 */
		for (Object initialDatum : initialData) {
			if (initialDatum instanceof PersonPropertyId) {
				PersonPropertyId personPropertyId = (PersonPropertyId) initialDatum;
				propertyIds.add(personPropertyId);
			}
		}

		/*
		 * if the client did not choose any properties, then we assume that all
		 * properties are selected
		 */
		if (propertyIds.size() == 0) {
			propertyIds.addAll(observableEnvironment.getPersonPropertyIds());
		}

		/*
		 * Validate the client's property ids and ignore any that are not known
		 * to the environment
		 */
		final Set<PersonPropertyId> validPersonPropertyIds = observableEnvironment.getPersonPropertyIds();

		final Iterator<PersonPropertyId> iterator = propertyIds.iterator();
		while (iterator.hasNext()) {
			if (!validPersonPropertyIds.contains(iterator.next())) {
				iterator.remove();
			}
		}

		setCurrentReportingPeriod(observableEnvironment);
		for (PersonId personId : observableEnvironment.getPeople()) {
			final Object regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(observableEnvironment, regionId, compartmentId, personId);
		}
	}

	/*
	 * Flushes the positive counters recursively.
	 */
	private void propertyFlush(ObservableEnvironment observableEnvironment, final Object regionId, final Object compartmentId, final Map<Object, Object> map, final Object[] personPropertyValues,
			final int level) {

		for (final Object personPropertyValue : map.keySet()) {
			personPropertyValues[level] = personPropertyValue;
			if (level < (propertyIds.size() - 1)) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> subMap = (Map<Object, Object>) map.get(personPropertyValue);
				propertyFlush(observableEnvironment, regionId, compartmentId, subMap, personPropertyValues, level + 1);
			} else {
				final Counter counter = (Counter) map.get(personPropertyValue);

				if (counter.count > 0) {
					final Map<String, Object> propertyIdsAndValues = new LinkedHashMap<>();
					for (int i = 0; i < propertyIds.size(); i++) {
						propertyIdsAndValues.put(propertyIds.get(i).toString(), personPropertyValues[i]);
					}
					final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportType(getClass());
					reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
					reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

					buildTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());
					reportItemBuilder.addValue(compartmentId.toString());
					for (int i = 0; i < propertyIds.size(); i++) {
						reportItemBuilder.addValue(personPropertyValues[i]);
					}
					reportItemBuilder.addValue(counter.count);

					observableEnvironment.releaseOutputItem(reportItemBuilder.build());
				}
			}
		}

	}
}
