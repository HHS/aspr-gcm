package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.ArrayIntSet;
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
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PersonPropertyMapper {


	/*
	 * Maps property values to IntSets that represent the people having the
	 * property.
	 */
	private Map<Object, IntSet<PersonId>> propertyValuesToPeopleMap = new LinkedHashMap<>();

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
		case HASH:
			return new HashIntSet<>();
		default:
			return new ArrayIntSet<>(5);
		}
	}

	/**
	 * Constructs an AbstractPropertyManger. Establishes the time tracking and map
	 * option policies from the environment. Establishes the property value to
	 * people mapping if the MapOption is not NONE.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param personPropertyId
	 */
	public PersonPropertyMapper(MapOption mapOption) {
		this.mapOption = mapOption;
	}

	/**
	 * Integrate the person into this PropertyManager with the default value
	 * associated with property's definition. Note that this does not imply that the
	 * person exists in the simulation. The environment must guard against access to
	 * removed people.
	 * 
	 * @param personId
	 */
	public final void add(final PersonId personId, Object personPropertyValue) {
		IntSet<PersonId> intSet = propertyValuesToPeopleMap.get(personPropertyValue);
		if (intSet == null) {
			intSet = newIntSet();
			propertyValuesToPeopleMap.put(personPropertyValue, intSet);
		}
		intSet.add(personId);
	}

	/**
	 * Returns the number of people who have a particular property value.
	 * 
	 * @param personPropertyValue
	 */
	public int getPersonCountForPropertyValue(final Object propertyValue) {
		IntSet<PersonId> people = propertyValuesToPeopleMap.get(propertyValue);
		if (people != null) {
			return people.size();
		}
		return 0;
	}

	/**
	 * Returns the List of people who have a particular property value. The returned
	 * list will only include people who currently exist in the simulation given
	 * that handlePersonRemoval() has been executed for each person removal from the
	 * simulation. The environment must guard against access to removed people.
	 * 
	 * @param personPropertyValue
	 */
	public final List<PersonId> getPeopleWithPropertyValue(final Object propertyValue) {
		IntSet<PersonId> people = propertyValuesToPeopleMap.get(propertyValue);
		if (people != null) {
			return people.getValues();
		}
		return new ArrayList<>();
	}

	/**
	 * Removes the person such that the method getPeopleWithPropertyValue()
	 * functions properly. However, this does not guarantee that the person does not
	 * have a property value since there is no way to effectively remove people from
	 * most of the implementors of PropertyManager due to default values, array
	 * structures and use of primitives. Instead, the environment must guard against
	 * access to removed people.
	 * 
	 * @param personId
	 */
	public final void remove(final PersonId personId, Object personPropertyValue) {

		IntSet<PersonId> intSet = propertyValuesToPeopleMap.get(personPropertyValue);
		if (intSet != null) {
			intSet.remove(personId);
		}

	}

}
