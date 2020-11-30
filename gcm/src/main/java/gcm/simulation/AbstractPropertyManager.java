package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.DoubleValueContainer;

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
public abstract class AbstractPropertyManager implements PersonPropertyManager {


	private EventManager eventManger;

	

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
		this.eventManger = context.getEventManager();
		this.personPropertyId = personPropertyId;
		trackTime = propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME;
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		timeTrackingContainer = new DoubleValueContainer(0, suggestedPopulationSize);
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		/*
		 * Record the time value if we are tracking assignment times.
		 */
		if (trackTime) {
			timeTrackingContainer.setValue(personId.getValue(), eventManger.getTime());
		}

		
	}
	

	@Override
	public final void handlePersonAddition(final PersonId personId) {
		

	}

	@Override
	public final void handlePersonRemoval(final PersonId personId) {
		
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
