package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.CompartmentId;
import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.ArrayIntSet;
import gcm.util.containers.DoubleValueContainer;
import gcm.util.containers.HashIntSet;
import gcm.util.containers.IntSet;
import gcm.util.containers.IntValueContainer;

/**
 * Implementor of PersonLocationManger
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class PersonLocationMangerImpl extends BaseElement implements PersonLocationManger {

	/*
	 * Record for maintaining the number of people either globally, regionally
	 * or by compartment. Also maintains the time when the population count was
	 * last changed. PopulationRecords are maintained to eliminate iterations
	 * over other tracking structures to answer queries about population counts.
	 */
	private static class PopulationRecord {
		private int populationCount;
		private double assignmentTime;
	}

	/*
	 * Tracking record for the total number of people in the simulation. This
	 * should not be confused with the number of person identifiers that have
	 * been issued by the environment.
	 */
	private final PopulationRecord globalPopulationRecord = new PopulationRecord();

	/*
	 * Tracking record for the total number of people in each region.
	 */
	private final Map<RegionId, PopulationRecord> regionPopulationRecordMap = new LinkedHashMap<>();

	/*
	 * Supports the conversion of region ids into int values.
	 */
	private final Map<RegionId, Integer> regionToIndexMap = new LinkedHashMap<>();

	/*
	 * Tracking record for the total number of people in each compartment.
	 */
	private final Map<CompartmentId, PopulationRecord> compartmentPopulationRecordMap = new LinkedHashMap<>();

	/*
	 * Supports the conversion of compartment ids into int values
	 */
	private final Map<CompartmentId, Integer> compartmentToIndexMap = new LinkedHashMap<>();

	/*
	 * Supports conversion of int into RegionId values
	 */
	private RegionId[] indexToRegionMap;
	/*
	 * Supports the conversion of ints into CompartmentId values
	 */
	private CompartmentId[] indexToCompartmentMap;

	/*
	 * Stores region identifiers as int values indexed by person id values
	 */
	private IntValueContainer regionValues;

	/*
	 * Stores compartment identifiers as int values indexed by person id values
	 */
	private IntValueContainer compartmentValues;

	/*
	 * Stores double region arrival values indexed by person id values.
	 * Maintenance depends upon tracking policy.
	 */
	private DoubleValueContainer regionArrivalTimes;

	/*
	 * Stores double compartment arrival values indexed by person id values.
	 * Maintenance depends upon tracking policy.
	 */
	private DoubleValueContainer compartmentArrivalTimes;

	/*
	 * Aids with conversion of int based person identifiers into the existing
	 * PersonIds.
	 */
	private PersonIdManager personIdManager;

	/*
	 * Stores the mapping of compartments to people. Maintenance depends upon
	 * mapping policy.
	 */
	private final Map<CompartmentId, IntSet<PersonId>> compartmentPeople = new LinkedHashMap<>();

	/*
	 * Stores the mapping of regions to people. Maintenance depends upon mapping
	 * policy.
	 */
	private final Map<RegionId, IntSet<PersonId>> regionPeople = new LinkedHashMap<>();

	/*
	 * Stores the modeler's choice of mapping option for regions
	 */
	private MapOption regionMapOption;
	/*
	 * Stores the modeler's choice of mapping option for compartments
	 */

	private MapOption compartmentMapOption;

	/**
	 * Constructs the PersonLocationManger.
	 */
	private EventManager eventManager;

	@Override
	public void init(final Context context) {
		super.init(context);

		personIdManager = context.getPersonIdManager();

		Scenario scenario = context.getScenario();
		eventManager = context.getEventManager();

	
		int suggestedPopulationSize = scenario.getSuggestedPopulationSize();
		/*
		 * By setting the default value to 0, we are allowing the container to
		 * grow without having to set values in its array. HOWEVER, THIS IMPLIES
		 * THAT REGIONS MUST BE CONVERTED TO INTEGER VALUES STARTING AT ONE, NOT
		 * ZERO.
		 * 
		 * The same holds true for compartments.
		 */
		regionValues = new IntValueContainer(0, suggestedPopulationSize);
		
		compartmentValues = new IntValueContainer(0, suggestedPopulationSize);

		if (scenario.getPersonCompartmentArrivalTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {
			compartmentArrivalTimes = new DoubleValueContainer(0,suggestedPopulationSize);
		}

		if (scenario.getPersonRegionArrivalTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {		
			regionArrivalTimes = new DoubleValueContainer(0, suggestedPopulationSize);
		}

		for (final RegionId regionId : scenario.getRegionIds()) {
			regionPopulationRecordMap.put(regionId, new PopulationRecord());
		}
		
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			compartmentPopulationRecordMap.put(compartmentId, new PopulationRecord());
		}

		compartmentMapOption = scenario.getCompartmentMapOption();
		regionMapOption = scenario.getRegionMapOption();

		// Note that regions are numbered starting with one and not zero to take
		// advantage of using zero as the default value in the regionValues container
		final Set<RegionId> regionIds = scenario.getRegionIds();
		int index = 1;
		for (final RegionId regionId : regionIds) {
			
			regionToIndexMap.put(regionId, index++);
		}
		
		indexToRegionMap = new RegionId[regionIds.size() + 1];
		index = 1;
		for (final RegionId regionId : regionIds) {
		
			indexToRegionMap[index++] = regionId;
		}

		final Set<CompartmentId> compartmentIds = scenario.getCompartmentIds();
		index = 1;
		for (final CompartmentId compartmentId : compartmentIds) {
			compartmentToIndexMap.put(compartmentId, index++);
		}
		indexToCompartmentMap = new CompartmentId[compartmentIds.size()+1];
		index = 1;
		for (final CompartmentId compartmentId : compartmentIds) {
			indexToCompartmentMap[index++] = compartmentId;
		}

	}

	@Override
	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		return compartmentPopulationRecordMap.get(compartmentId).populationCount;
	}

	@Override
	public double getCompartmentPopulationTime(final CompartmentId compartmentId) {
		return compartmentPopulationRecordMap.get(compartmentId).assignmentTime;
	}

	@Override
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId) {
		final List<PersonId> result = new ArrayList<>(getCompartmentPopulationCount(compartmentId));

		if (compartmentMapOption != MapOption.NONE) {
			final IntSet<PersonId> intSet = compartmentPeople.get(compartmentId);
			if (intSet != null) {
				for (final PersonId personId : intSet.getValues()) {
					if (personIdManager.personExists(personId)) {
						result.add(personId);
					}
				}
			}
		} else {
			final int maxPersonIndex = personIdManager.getPersonIdLimit();
			for (int personIndex = 0; personIndex < maxPersonIndex; personIndex++) {
				if (personIdManager.personIndexExists(personIndex)) {
					PersonId personId = personIdManager.getBoxedPersonId(personIndex);
					if (getPersonCompartment(personId).equals(compartmentId)) {
						result.add(personId);
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		final List<PersonId> result = new ArrayList<>(getRegionPopulationCount(regionId));

		if (regionMapOption != MapOption.NONE) {
			final IntSet<PersonId> intSet = regionPeople.get(regionId);
			if (intSet != null) {
				for (final PersonId personId : intSet.getValues()) {
					if (personIdManager.personExists(personId)) {
						result.add(personId);
					}
				}
			}
		} else {
			final int maxPersonIndex = personIdManager.getPersonIdLimit();
			for (int personIndex = 0; personIndex < maxPersonIndex; personIndex++) {
				if (personIdManager.personIndexExists(personIndex)) {
					PersonId personId = personIdManager.getBoxedPersonId(personIndex);
					if (getPersonRegion(personId).equals(regionId)) {
						result.add(personId);
					}
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		final int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		return (T) indexToCompartmentMap[compartmentIndex];
	}

	@Override
	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		return compartmentArrivalTimes.getValue(personId.getValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		// pop
		final int r = regionValues.getValueAsInt(personId.getValue());
		// pop
		return (T) indexToRegionMap[r];
	}

	@Override
	public double getPersonRegionArrivalTime(final PersonId personId) {
		// pop
		return regionArrivalTimes.getValue(personId.getValue());
	}

	@Override
	public int getPopulationCount() {
		return globalPopulationRecord.populationCount;
	}

	@Override
	public double getPopulationTime() {
		return globalPopulationRecord.assignmentTime;
	}

	@Override
	public int getRegionPopulationCount(final RegionId regionId) {
		return regionPopulationRecordMap.get(regionId).populationCount;
	}

	@Override
	public double getRegionPopulationTime(final RegionId regionId) {
		return regionPopulationRecordMap.get(regionId).assignmentTime;
	}

	/*
	 * Creates a new IntSet instance based on the compartment map option
	 * obtained from the environment. Should not be invoked when compartments
	 * are not being mapped to the people.
	 */
	private IntSet<PersonId> newIntSetForCompartment() {
		switch (compartmentMapOption) {
		case ARRAY:
			return new ArrayIntSet<>();
		case HASH:
			return new HashIntSet<>();
		case NONE:// fall through
		default:
			throw new RuntimeException("unhandled map option " + regionMapOption);
		}
	}

	/*
	 * Creates a new IntSet instance based on the region map option obtained
	 * from the environment. Should not be invoked when regions are not being
	 * mapped to the people.
	 */
	private IntSet<PersonId> newIntSetForRegion() {
		switch (regionMapOption) {
		case ARRAY:
			return new ArrayIntSet<>();
		case HASH:
			return new HashIntSet<>();
		case NONE:// fall through
		default:
			throw new RuntimeException("unhandled map option " + regionMapOption);
		}
	}

	@Override
	public void removePerson(final PersonId personId) {

		final int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		if (compartmentIndex > 0) {
			final CompartmentId oldCompartmentId = indexToCompartmentMap[compartmentIndex];
			final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(oldCompartmentId);
			populationRecord.populationCount--;
			populationRecord.assignmentTime = eventManager.getTime();

			globalPopulationRecord.populationCount--;
			globalPopulationRecord.assignmentTime = eventManager.getTime();

			compartmentValues.setIntValue(personId.getValue(), -1);

			if (compartmentMapOption != MapOption.NONE) {
				final IntSet<PersonId> people = compartmentPeople.get(oldCompartmentId);
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						compartmentPeople.remove(oldCompartmentId);
					}
				}
			}
		}

		// pop
		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		if (regionIndex > 0) {
			// pop
			final RegionId oldRegionId = indexToRegionMap[regionIndex];
			final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
			populationRecord.populationCount--;
			populationRecord.assignmentTime = eventManager.getTime();
			// pop
			regionValues.setIntValue(personId.getValue(), 0);

			if (regionMapOption != MapOption.NONE) {
				final IntSet<PersonId> people = regionPeople.get(oldRegionId);
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						regionPeople.remove(oldRegionId);
					}
				}
			}
		}
	}

	@Override
	public PersonId addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId) {
		setPersonCompartment(personId, compartmentId);
		setPersonRegion(personId, regionId);
		return personId;
	}

	@Override
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
		/*
		 * Retrieve the int value that represents the current compartment of the
		 * person
		 */
		int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		CompartmentId oldCompartmentId;
		if (compartmentIndex > 0) {
			/*
			 * Convert the int reference into a compartment identifier
			 */
			oldCompartmentId = indexToCompartmentMap[compartmentIndex];
			final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(oldCompartmentId);
			/*
			 * Update the population count associated with the old compartment
			 */
			populationRecord.populationCount--;
			populationRecord.assignmentTime = eventManager.getTime();

		} else {
			/*
			 * The person was not known to this manager, so the old compartment
			 * is null and the global population must be incremented
			 */
			globalPopulationRecord.populationCount++;
			globalPopulationRecord.assignmentTime = eventManager.getTime();
			oldCompartmentId = null;
		}

		/*
		 * Update the population count of the new compartment
		 */
		final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(compartmentId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = eventManager.getTime();

		/*
		 * Convert the new compartment id into an int
		 */
		compartmentIndex = compartmentToIndexMap.get(compartmentId).intValue();
		/*
		 * Store in the int at the person's index
		 */
		compartmentValues.setIntValue(personId.getValue(), compartmentIndex);

		/*
		 * If compartment arrival times are being tracked, do so.
		 */
		if (compartmentArrivalTimes != null) {
			compartmentArrivalTimes.setValue(personId.getValue(), eventManager.getTime());
		}

		/*
		 * If compartment to people maps are being maintained, do so.
		 */
		if (compartmentMapOption != MapOption.NONE) {
			if (oldCompartmentId != null) {
				final IntSet<PersonId> people = compartmentPeople.get(oldCompartmentId);
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						compartmentPeople.remove(oldCompartmentId);
					}
				}
			}
			IntSet<PersonId> people = compartmentPeople.get(compartmentId);
			if (people == null) {
				people = newIntSetForCompartment();
				compartmentPeople.put(compartmentId, people);
			}
			people.add(personId);
		}
	}

	@Override
	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		/*
		 * Retrieve the int value that represents the current region of the
		 * person
		 */
		// pop
		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		RegionId oldRegionId;
		if (regionIndex > 0) {
			/*
			 * Convert the int reference into a region identifier
			 */
			// pop
			oldRegionId = indexToRegionMap[regionIndex];
			final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
			/*
			 * Update the population count associated with the old region
			 */
			populationRecord.populationCount--;
			populationRecord.assignmentTime = eventManager.getTime();
		} else {
			/*
			 * The person was not known to this manager, but we only update the
			 * global population on the change to a compartment
			 * 
			 */
			oldRegionId = null;
		}
		/*
		 * Update the population count of the new region
		 */
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = eventManager.getTime();
		/*
		 * Convert the new region id into an int
		 */
		// pop
		regionIndex = regionToIndexMap.get(regionId).intValue();
		/*
		 * Store in the int at the person's index
		 */
		// pop
		regionValues.setIntValue(personId.getValue(), regionIndex);
		/*
		 * If region arrival times are being tracked, do so.
		 */
		if (regionArrivalTimes != null) {
			// pop
			regionArrivalTimes.setValue(personId.getValue(), eventManager.getTime());
		}
		/*
		 * If region to people maps are being maintained, do so.
		 */
		if (regionMapOption != MapOption.NONE) {

			if (oldRegionId != null) {
				final IntSet<PersonId> people = regionPeople.get(oldRegionId);
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						regionPeople.remove(oldRegionId);
					}
				}
			}
			IntSet<PersonId> people = regionPeople.get(regionId);
			if (people == null) {
				people = newIntSetForRegion();
				regionPeople.put(regionId, people);
			}
			people.add(personId);
		}
	}
}
