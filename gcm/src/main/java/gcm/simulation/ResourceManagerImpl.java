package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.scenario.MaterialsProducerId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.scenario.Scenario;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.DoubleValueContainer;
import gcm.util.containers.IntValueContainer;

/**
 * Implementor for ResourceManager
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class ResourceManagerImpl extends BaseElement implements ResourceManager {

	private PersonIdManager personIdManager;
	private EventManager eventManager;

	/*
	 * Stores resource amounts per person keyed by the resourceId
	 */
	private final Map<ResourceId, IntValueContainer> personResourceValues = new LinkedHashMap<>();

	/*
	 * Stores resource assignment times per person keyed by the resourceId. Key
	 * existence subject to time recording policies specified by the scenario.
	 */
	private final Map<ResourceId, DoubleValueContainer> personResourceTimes = new LinkedHashMap<>();

	private final Map<ResourceId, TimeTrackingPolicy> resourceTimeTrackingPolicies = new LinkedHashMap<>();

	/*
	 * Static utility class for tracking component resources. Used for regions
	 * and materials producers.
	 */
	private static class ComponentResourceRecord {
		private final EventManager eventManager;

		public ComponentResourceRecord(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		private long amount;

		private double assignmentTime;

		public long getAmount() {
			return amount;
		}

		public double getAssignmentTime() {
			return assignmentTime;
		}

		public void incrementAmount(long amount) {
			if (amount < 0) {
				throw new RuntimeException("negative amount");
			}
			this.amount = Math.addExact(this.amount, amount);
			assignmentTime = eventManager.getTime();
		}

		public void decrementAmount(long amount) {
			if (amount < 0) {
				throw new RuntimeException("negative amount");
			}

			if (this.amount < amount) {
				throw new RuntimeException("cannot decrement to a negative level");
			}
			this.amount = Math.subtractExact(this.amount, amount);
			assignmentTime = eventManager.getTime();
		}

	}

	private Map<RegionId, Map<ResourceId, ComponentResourceRecord>> regionResources = new LinkedHashMap<>();

	private Map<MaterialsProducerId, Map<ResourceId, ComponentResourceRecord>> materialProducerResources = new LinkedHashMap<>();

	/**
	 * Constructs the PersonResourceManager
	 */
	@Override
	public void init(final Context context) {
		super.init(context);

		this.personIdManager = context.getPersonIdManager();
		this.eventManager = context.getEventManager();
		Scenario scenario = context.getScenario();

		/*
		 * Initialize region resources
		 */
		for (RegionId regionId : scenario.getRegionIds()) {
			Map<ResourceId, ComponentResourceRecord> map = new LinkedHashMap<>();
			regionResources.put(regionId, map);
			for (ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new ComponentResourceRecord(eventManager));
			}
		}

		/*
		 * Initialize materials producers resources
		 */
		for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			Map<ResourceId, ComponentResourceRecord> map = new LinkedHashMap<>();
			materialProducerResources.put(materialsProducerId, map);
			for (ResourceId resourceId : scenario.getResourceIds()) {
				map.put(resourceId, new ComponentResourceRecord(eventManager));
			}
		}

		/*
		 * For each resource where time tracking has been turned on, associate a
		 * DoubleValueContainer having a zero default value.
		 */
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		for (final ResourceId resourceId : scenario.getResourceIds()) {
			final TimeTrackingPolicy resourceTimeTrackingPolicy = scenario.getPersonResourceTimeTrackingPolicy(resourceId);
			IntValueContainer intValueContainer = new IntValueContainer(0L);
			intValueContainer.setCapacity(suggestedPopulationSize);
			personResourceValues.put(resourceId, intValueContainer);
			if (resourceTimeTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
				DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0D);
				doubleValueContainer.setCapacity(suggestedPopulationSize);
				personResourceTimes.put(resourceId, doubleValueContainer);
			}
		}

		for (final ResourceId resourceId : scenario.getResourceIds()) {
			TimeTrackingPolicy personResourceTimeTrackingPolicy = scenario.getPersonResourceTimeTrackingPolicy(resourceId);
			resourceTimeTrackingPolicies.put(resourceId, personResourceTimeTrackingPolicy);
		}

	}

	@Override
	public void decrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {
		personResourceValues.get(resourceId).decrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), eventManager.getTime());
		}
	}

	@Override
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {

		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
		final int n = personIdManager.getPersonIdLimit();
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personIdManager.personIndexExists(personIndex)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personIndex);
				if (resourceLevel == 0) {
					count++;
				}
			}
		}

		/*
		 * Now we create the list
		 */
		final List<PersonId> result = new ArrayList<>(count);

		/*
		 * We loop again and add the people to the list
		 */
		for (int personId = 0; personId < n; personId++) {
			if (personIdManager.personIndexExists(personId)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personId);
				if (resourceLevel == 0) {
					result.add(personIdManager.getBoxedPersonId(personId));
				}
			}
		}
		return result;
	}

	@Override
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		/*
		 * First, we loop through all possible person id values and determine
		 * the exact size of the returned list.
		 */
		int count = 0;
		final IntValueContainer intValueContainer = personResourceValues.get(resourceId);
		final int n = personIdManager.getPersonIdLimit();
		for (int personId = 0; personId < n; personId++) {
			if (personIdManager.personIndexExists(personId)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personId);
				if (resourceLevel > 0) {
					count++;
				}
			}
		}
		/*
		 * Now we create the list
		 */
		final List<PersonId> result = new ArrayList<>(count);
		/*
		 * We loop again and add the people to the list
		 */
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personIdManager.personIndexExists(personIndex)) {
				final long resourceLevel = intValueContainer.getValueAsLong(personIndex);
				if (resourceLevel > 0) {
					result.add(personIdManager.getBoxedPersonId(personIndex));
				}
			}
		}
		return result;

	}

	@Override
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId) {
		return personResourceValues.get(resourceId).getValueAsLong(personId.getValue());
	}

	@Override
	public double getPersonResourceTime(final ResourceId resourceId, final PersonId personId) {
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		return doubleValueContainer.getValue(personId.getValue());
	}

	@Override
	public void incrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount) {		
		personResourceValues.get(resourceId).incrementLongValue(personId.getValue(), resourceAmount);
		/*
		 * if the resource assignment times are being tracked, then record the
		 * resource time.
		 */
		final DoubleValueContainer doubleValueContainer = personResourceTimes.get(resourceId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(personId.getValue(), eventManager.getTime());
		}
	}


	@Override
	public void incrementRegionResourceLevel(RegionId regionId, ResourceId resourceId, long amount) {		
		ComponentResourceRecord componentResourceRecord = regionResources.get(regionId).get(resourceId);
		componentResourceRecord.incrementAmount(amount);
	}

	@Override
	public void decrementRegionResourceLevel(RegionId regionId, ResourceId resourceId, long amount) {
		ComponentResourceRecord componentResourceRecord = regionResources.get(regionId).get(resourceId);
		componentResourceRecord.decrementAmount(amount);
	}

	@Override
	public long getRegionResourceLevel(RegionId regionId, ResourceId resourceId) {
		ComponentResourceRecord componentResourceRecord = regionResources.get(regionId).get(resourceId);
		return componentResourceRecord.getAmount();
	}

	@Override
	public double getRegionResourceTime(RegionId regionId, ResourceId resourceId) {
		ComponentResourceRecord componentResourceRecord = regionResources.get(regionId).get(resourceId);
		return componentResourceRecord.getAssignmentTime();
	}

	@Override
	public void incrementMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount) {
		ComponentResourceRecord componentResourceRecord = materialProducerResources.get(materialsProducerId).get(resourceId);
		componentResourceRecord.incrementAmount(amount);
	}

	@Override
	public void decrementMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount) {
		ComponentResourceRecord componentResourceRecord = materialProducerResources.get(materialsProducerId).get(resourceId);
		componentResourceRecord.decrementAmount(amount);
	}


	@Override
	public long getMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		ComponentResourceRecord componentResourceRecord = materialProducerResources.get(materialsProducerId).get(resourceId);
		return componentResourceRecord.getAmount();
	}

	@Override
	public double getMaterialsProducerResourceTime(MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		ComponentResourceRecord componentResourceRecord = materialProducerResources.get(materialsProducerId).get(resourceId);
		return componentResourceRecord.getAssignmentTime();
	}

	@Override
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(ResourceId resourceId) {
		return resourceTimeTrackingPolicies.get(resourceId);
	}

}
