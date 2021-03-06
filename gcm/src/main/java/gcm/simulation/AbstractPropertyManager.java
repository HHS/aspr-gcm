package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.ArrayIntSet;
import gcm.util.containers.DoubleValueContainer;
import gcm.util.containers.HashIntSet;
import gcm.util.containers.IntSet;

/**
 * The abstract base class for all PropertyManager implementors.
 * 
 * It implements all property time recording and reverse mapping of property
 * values to people. Its implementation of these methods is final.
 * 
 * It also implements setPropertyValue() and descendant classes are expected to
 * invoke super.setPropertyValue()
 * 
 * Finally, it leaves the implementation of getPropertyValue() to its descendant
 * classes
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public abstract class AbstractPropertyManager implements PersonPropertyManager {

	private PersonIdManager personIdManager;

	private EventManager eventManger;

	/*
	 * Maps property values to IntSets that represent the people having the
	 * property.
	 */
	private Map<Object, IntSet<PersonId>> propertyValuesToPeopleMap;

	/*
	 * Contains the assignment times for this property value. Subject to
	 * tracking policy.
	 */
	private DoubleValueContainer timeTrackingContainer;

	/*
	 * The time tracking policy.
	 */
	private final boolean trackTime;

	/*
	 * The property that this AbstractProperty Manager represents.
	 */
	private final PersonPropertyId personPropertyId;

	/*
	 * The scenario-defined mapping option that determines which, if any, IntSet
	 * implementor we are to use. ArrayIntSet is slightly slower, but takes much
	 * less memory. HashIntSet is just a wrapper around a LinkedHashSet.
	 */
	private final MapOption mapOption;

	/*
	 * Constructs an IntSet based on the mapOption setting.
	 */
	private IntSet<PersonId> newIntSet() {
		switch (mapOption) {
		case ARRAY:
			return new ArrayIntSet<>();
		case HASH:
			return new HashIntSet<>();
		case NONE:// fall through
		default:
			throw new RuntimeException("unhandled map option " + mapOption);
		}
	}

	/**
	 * Constructs an AbstractPropertyManger. Establishes the time tracking and
	 * map option policies from the environment. Establishes the property value
	 * to people mapping if the MapOption is not NONE.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param personPropertyId
	 */
	public AbstractPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId personPropertyId) {
		this.personIdManager = context.getPersonIdManager();
		this.eventManger = context.getEventManager();
		this.personPropertyId = personPropertyId;
		trackTime = propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME;
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		timeTrackingContainer = new DoubleValueContainer(0, suggestedPopulationSize);
		mapOption = propertyDefinition.getMapOption();
		switch (mapOption) {
		case ARRAY:
		case HASH:
			propertyValuesToPeopleMap = new LinkedHashMap<>();
			break;
		case NONE:// fall through
		default:
			// do nothing
			break;
		}

	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		/*
		 * Record the time value if we are tracking assignment times.
		 */
		if (trackTime) {
			timeTrackingContainer.setValue(personId.getValue(), eventManger.getTime());
		}

		/*
		 * If we are mapping property values to people, move the person from the
		 * container for the old property value to the new one.
		 */
		if (propertyValuesToPeopleMap != null) {
			Object oldValue = getPropertyValue(personId);
			IntSet<PersonId> intSet = propertyValuesToPeopleMap.get(oldValue);
			intSet.remove(personId);
			intSet = propertyValuesToPeopleMap.get(personPropertyValue);
			if (intSet == null) {
				intSet = newIntSet();
				propertyValuesToPeopleMap.put(personPropertyValue, intSet);
			}
			intSet.add(personId);
		}
	}
	
	@Override
	public int getPersonCountForPropertyValue(final Object propertyValue) {
		/*
		 * If we are supporting the mapping of property values to people, then
		 * we simply return what values are in the IntSet
		 */
		if (propertyValuesToPeopleMap != null) {
			IntSet<PersonId> people = propertyValuesToPeopleMap.get(propertyValue);
			if (people != null) {
				return people.size();
			}
			return 0;
		}

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */
		int n = personIdManager.getPersonIdLimit();
		int count = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personIdManager.personIndexExists(personIndex)) {
				PersonId personId = personIdManager.getBoxedPersonId(personIndex);
				Object personPropertyValue = getPropertyValue(personId);
				if (personPropertyValue.equals(propertyValue)) {
					count++;
				}
			}
		}
		return count;
	}
	
	@Override
	public final List<PersonId> getPeopleWithPropertyValue(final Object propertyValue) {

		/*
		 * If we are supporting the mapping of property values to people, then
		 * we simply return what values are in the IntSet
		 */
		if (propertyValuesToPeopleMap != null) {
			IntSet<PersonId> people = propertyValuesToPeopleMap.get(propertyValue);
			if (people != null) {
				return people.getValues();
			}
			return new ArrayList<>();
		}

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */
		int n = personIdManager.getPersonIdLimit();
		int count = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personIdManager.personIndexExists(personIndex)) {
				PersonId personId = personIdManager.getBoxedPersonId(personIndex);
				Object personPropertyValue = getPropertyValue(personId);
				if (personPropertyValue.equals(propertyValue)) {
					count++;
				}
			}
		}

		/*
		 * Now we fill the list.
		 */
		List<PersonId> result = new ArrayList<>(count);

		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personIdManager.personIndexExists(personIndex)) {
				PersonId personId = personIdManager.getBoxedPersonId(personIndex);
				Object personPropertyValue = getPropertyValue(personId);
				if (personPropertyValue.equals(propertyValue)) {
					result.add(personId);
				}
			}
		}

		return result;
	}

	@Override
	public final void handlePersonAddition(final PersonId personId) {
		if (propertyValuesToPeopleMap != null) {
			Object personPropertyValue = getPropertyValue(personId);
			IntSet<PersonId> intSet = propertyValuesToPeopleMap.get(personPropertyValue);
			if (intSet == null) {
				intSet = newIntSet();
				propertyValuesToPeopleMap.put(personPropertyValue, intSet);
			}
			intSet.add(personId);
		}

	}

	@Override
	public final void handlePersonRemoval(final PersonId personId) {
		if (propertyValuesToPeopleMap != null) {
			Object propertyValue = getPropertyValue(personId);
			IntSet<PersonId> intSet = propertyValuesToPeopleMap.get(propertyValue);
			intSet.remove(personId);
		}
	}

	@Override
	public final double getPropertyTime(PersonId personId) {
		double result = 0;
		if (trackTime) {
			result = timeTrackingContainer.getValue(personId.getValue());
		} else {
			throw new RuntimeException("Property time values are not being tracked for this property " + personPropertyId);
		}
		return result;
	}
}
