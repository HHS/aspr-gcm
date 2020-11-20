package gcm.output.reports.commonreports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.PersonInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.StateChange;
import gcm.scenario.CompartmentId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A periodic Report that displays the creation, transfer or consumption of
 * resources within a region/compartment pair. Some activities have no
 * compartment association and will leave the compartment field blank. Only
 * activities with non-zero action counts are reported.
 *
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * Compartment -- the compartment identifier
 *
 * Resource -- the resource identifier
 *
 * Activity -- the activity that lead to the creation, transfer or consumption
 * of a resource unit(s)
 *
 * Actions -- the number of individual actions that were associated with the
 * activity
 *
 * Items -- the number of units of the resource that were associated with the
 * activity
 *
 *
 *
 * Activities
 *
 * PersonAddition -- the addition of a person to the simulation
 *
 * PersonDeparture -- the removal of a person from the simulation
 *
 * PersonRegionArrival -- the arrival of a person into the region from another
 * region
 *
 * PersonRegionDeparture -- the departure of a person from the region to another
 * region
 *
 * PersonCompartmentArrival -- the arrival of a person into the compartment from
 * another compartment
 *
 * PersonCompartmentDeparture -- the departure of a person from the compartment
 * to another compartment
 *
 * RegionResourceAddition -- the creation of a resource unit(s) on the region
 *
 * PersonResourceAddition -- the creation of a resource unit(s) on a
 * person(associate with simulation bootstrap)
 *
 * RegionResourceRemoval -- the destruction of a resource unit(s) on the region
 *
 * ResourceTransferIntoRegion -- the transfer of units of resource from another
 * region
 *
 * ResourceTransferOutOfRegion -- the transfer of units of resource to another
 * region
 *
 * ResourceTransferFromPerson -- the return of resource units from a person in
 * the region to the region
 *
 * ResourceTransferToPerson -- the distribution of resource units to a person in
 * the region from the region
 *
 * ResourceRemovalFromPerson -- the destruction of a resource unit(s) on a
 * person
 *
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class ResourceReport extends PeriodicReport {

	private static enum Activity {
		PERSON_ARRIVAL("PersonAddition"), PERSON_DEPARTURE("PersonDeparture"),
		PERSON_REGION_ARRIVAL("PersonRegionArrival"), PERSON_REGION_DEPARTURE("PersonRegionDeparture"),
		PERSON_COMPARTMENT_ARRIVAL("PersonCompartmentArrival"),
		PERSON_COMPARTMENT_DEPARTURE("PersonCompartmentDeparture"), REGION_RESOURCE_ADDITION("RegionResourceAddition"),
		PERSON_RESOURCE_ADDITION("PersonResourceAddition"), REGION_RESOURCE_REMOVAL("RegionResourceRemoval"),
		RESOURCE_TRANSFER_INTO_REGION("ResourceTransferIntoRegion"),
		RESOURCE_TRANSFER_OUT_OF_REGION("ResourceTransferOutOfRegion"),
		RESOURCE_TRANSFER_FROM_MATERIALS_PRODUCER("ResourceTransferFromMaterialsProducer"),
		TRANSFER_RESOURCE_FROM_PERSON("ResourceTransferFromPerson"),
		TRANSFER_RESOURCE_TO_PERSON("ResourceTransferToPerson"),
		REMOVE_RESOURCE_FROM_PERSON("ResourceRemovalFromPerson");

		private final String displayName;

		Activity(final String displayName) {
			this.displayName = displayName;
		}
	}

	private static class Counter {
		private int actionCount;
		private long itemCount;

		private void reset() {
			actionCount = 0;
			itemCount = 0;
		}
	}

	/*
	 * The resource covered by this report. Determined in the init() method.
	 */
	private final List<ResourceId> resourceIds = new ArrayList<>();

	/*
	 * The mapping of (Region, Compartment, Resource, Activity) tuples to counters
	 * that record the number of actions and the number of items handled across
	 * those actions.
	 */
	private final Map<RegionId, Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>>> regionMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder).add("Region")//
					.add("Compartment")//
					.add("Resource")//
					.add("Activity")//
					.add("Actions")//
					.add("Items")//
					.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ObservableEnvironment observableEnvironment) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (final RegionId regionId : regionMap.keySet()) {
			final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = regionMap.get(regionId);
			for (final CompartmentId compartmentId : compartmentMap.keySet()) {
				final Map<ResourceId, Map<Activity, Counter>> resourceMap = compartmentMap.get(compartmentId);
				for (final ResourceId resourceId : resourceMap.keySet()) {
					final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
					for (final Activity activity : activityMap.keySet()) {
						final Counter counter = activityMap.get(activity);
						if (counter.actionCount > 0) {
							reportItemBuilder.setReportHeader(getReportHeader());
							reportItemBuilder.setReportType(getClass());
							reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
							reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
							buildTimeFields(reportItemBuilder);

							reportItemBuilder.addValue(regionId.toString());
							if (compartmentId != null) {
								reportItemBuilder.addValue(compartmentId.toString());
							} else {
								reportItemBuilder.addValue("");
							}
							reportItemBuilder.addValue(resourceId.toString());
							reportItemBuilder.addValue(activity.displayName);
							reportItemBuilder.addValue(counter.actionCount);
							reportItemBuilder.addValue(counter.itemCount);
							observableEnvironment.releaseOutputItem(reportItemBuilder.build());
							counter.reset();
						}
					}
				}
			}
		}
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.REGION_RESOURCE_ADDITION);
		result.add(StateChange.REGION_RESOURCE_REMOVAL);
		result.add(StateChange.PERSON_RESOURCE_TRANSFER_TO_REGION);
		result.add(StateChange.REGION_RESOURCE_TRANSFER_TO_PERSON);
		result.add(StateChange.PERSON_RESOURCE_ADDITION);
		result.add(StateChange.INTER_REGION_RESOURCE_TRANSFER);
		result.add(StateChange.MATERIALS_PRODUCER_RESOURCE_TRANSFER);
		result.add(StateChange.PERSON_ADDITION);
		result.add(StateChange.PERSON_REMOVAL);
		result.add(StateChange.REGION_ASSIGNMENT);
		result.add(StateChange.COMPARTMENT_ASSIGNMENT);
		result.add(StateChange.PERSON_RESOURCE_REMOVAL);
		return result;
	}

	@Override
	public void handleCompartmentAssignment(ObservableEnvironment observableEnvironment, final PersonId personId,
			final CompartmentId sourceCompartmentId) {
		setCurrentReportingPeriod(observableEnvironment);
		final RegionId regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = observableEnvironment.getPersonResourceLevel(personId, resourceId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_COMPARTMENT_ARRIVAL,
						personResourceLevel);
				increment(regionId, sourceCompartmentId, resourceId, Activity.PERSON_COMPARTMENT_DEPARTURE,
						personResourceLevel);
			}
		}
	}

	@Override
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId) {
		setCurrentReportingPeriod(observableEnvironment);
		final RegionId regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = observableEnvironment.getPersonResourceLevel(personId, resourceId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
			}
		}
	}

	@Override
	public void handlePersonRemoval(ObservableEnvironment observableEnvironment, PersonInfo personInfo) {
		setCurrentReportingPeriod(observableEnvironment);
		RegionId regionId = personInfo.getRegionId();
		CompartmentId compartmentId = personInfo.getCompartmentId();
		Map<ResourceId, Long> resourceValues = personInfo.getResourceValues();
		for (final ResourceId resourceId : resourceIds) {
			final Long personResourceLevel = resourceValues.get(resourceId);
			if ((personResourceLevel != null) && (personResourceLevel > 0)) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_DEPARTURE, personResourceLevel);
			}
		}
	}

	@Override
	public void handlePersonResourceAddition(ObservableEnvironment observableEnvironment, final PersonId personId,
			final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.PERSON_RESOURCE_ADDITION, amount);
		}
	}

	@Override
	public void handlePersonResourceRemoval(ObservableEnvironment observableEnvironment, final PersonId personId,
			final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.REMOVE_RESOURCE_FROM_PERSON, amount);
		}
	}

	@Override
	public void handlePersonResourceTransferToRegion(ObservableEnvironment observableEnvironment,
			final PersonId personId, final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.TRANSFER_RESOURCE_FROM_PERSON, amount);
		}
	}

	@Override
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId,
			final RegionId sourceRegionId) {
		setCurrentReportingPeriod(observableEnvironment);
		final RegionId regionId = observableEnvironment.getPersonRegion(personId);
		final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = observableEnvironment.getPersonResourceLevel(personId, resourceId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_REGION_ARRIVAL, personResourceLevel);
				increment(sourceRegionId, compartmentId, resourceId, Activity.PERSON_REGION_DEPARTURE,
						personResourceLevel);
			}
		}
	}

	@Override
	public void handleRegionResourceAddition(ObservableEnvironment observableEnvironment, final RegionId regionId,
			final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			increment(regionId, null, resourceId, Activity.REGION_RESOURCE_ADDITION, amount);
		}
	}

	@Override
	public void handleRegionResourceRemoval(ObservableEnvironment observableEnvironment, final RegionId regionId,
			final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			increment(regionId, null, resourceId, Activity.REGION_RESOURCE_REMOVAL, amount);
		}
	}

	@Override
	public void handleRegionResourceTransferToPerson(ObservableEnvironment observableEnvironment,
			final PersonId personId, final ResourceId resourceId, final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.TRANSFER_RESOURCE_TO_PERSON, amount);
		}
	}

	@Override
	public void handleTransferResourceBetweenRegions(ObservableEnvironment observableEnvironment,
			final RegionId sourceRegionId, final RegionId destinationRegionId, final ResourceId resourceId,
			final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			increment(destinationRegionId, null, resourceId, Activity.RESOURCE_TRANSFER_INTO_REGION, amount);
			increment(sourceRegionId, null, resourceId, Activity.RESOURCE_TRANSFER_OUT_OF_REGION, amount);
		}
	}

	@Override
	public void handleTransferResourceFromMaterialsProducerToRegion(ObservableEnvironment observableEnvironment,
			final MaterialsProducerId materialsProducerId, final RegionId regionId, final ResourceId resourceId,
			final long amount) {
		if ((amount > 0) && resourceIds.contains(resourceId)) {
			setCurrentReportingPeriod(observableEnvironment);
			increment(regionId, null, resourceId, Activity.RESOURCE_TRANSFER_FROM_MATERIALS_PRODUCER, amount);
		}
	}

	/*
	 * Increments the counter for the given tuple
	 */
	private void increment(final RegionId regionId, final CompartmentId compartmentId, final ResourceId resourceId,
			final Activity activity, final long count) {
		final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = regionMap.get(regionId);
		final Map<ResourceId, Map<Activity, Counter>> resourceMap = compartmentMap.get(compartmentId);
		final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
		final Counter counter = activityMap.get(activity);
		counter.actionCount++;
		counter.itemCount += count;
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);

		for (Object initialDatum : initialData) {
			if (initialData instanceof ResourceId) {
				ResourceId resourceId = (ResourceId) initialDatum;
				resourceIds.add(resourceId);
			}
		}
		if (resourceIds.size() == 0) {
			resourceIds.addAll(observableEnvironment.getResourceIds());
		}
		/*
		 * Ensure that every client supplied resource identifier is valid
		 */
		final Set<ResourceId> validResourceIds = observableEnvironment.getResourceIds();
		for (final ResourceId resourceId : resourceIds) {
			if (!validResourceIds.contains(resourceId)) {
				throw new RuntimeException("invalid resource id " + resourceId);
			}
		}

		/*
		 * We add the null compartment to the set of compartment ids to provide a place
		 * in the region map to house counters that do not correspond to any
		 * compartment.
		 */
		final Set<CompartmentId> compartmentIds = observableEnvironment.getCompartmentIds();
		compartmentIds.add(null);

		/*
		 * Filling the region map with empty counters
		 */
		for (final RegionId regionId : observableEnvironment.getRegionIds()) {
			final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, compartmentMap);
			for (final CompartmentId compartmentId : compartmentIds) {
				final Map<ResourceId, Map<Activity, Counter>> resourceMap = new LinkedHashMap<>();
				compartmentMap.put(compartmentId, resourceMap);
				for (final ResourceId resourceId : resourceIds) {
					final Map<Activity, Counter> activityMap = new LinkedHashMap<>();
					resourceMap.put(resourceId, activityMap);
					for (final Activity activity : Activity.values()) {
						final Counter counter = new Counter();
						activityMap.put(activity, counter);
					}
				}
			}
		}

		setCurrentReportingPeriod(observableEnvironment);

		for (PersonId personId : observableEnvironment.getPeople()) {
			final RegionId regionId = observableEnvironment.getPersonRegion(personId);
			final CompartmentId compartmentId = observableEnvironment.getPersonCompartment(personId);
			for (final ResourceId resourceId : resourceIds) {
				final long personResourceLevel = observableEnvironment.getPersonResourceLevel(personId, resourceId);
				if (personResourceLevel > 0) {
					increment(regionId, compartmentId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
				}
			}
		}

		for (RegionId regionId : observableEnvironment.getRegionIds()) {
			for (ResourceId resourceId : observableEnvironment.getResourceIds()) {
				long regionResourceLevel = observableEnvironment.getRegionResourceLevel(regionId, resourceId);
				if (resourceIds.contains(resourceId)) {
					setCurrentReportingPeriod(observableEnvironment);
					increment(regionId, null, resourceId, Activity.REGION_RESOURCE_ADDITION, regionResourceLevel);
				}
			}
		}
	}
}