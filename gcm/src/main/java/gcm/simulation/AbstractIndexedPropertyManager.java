package gcm.simulation;

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
public abstract class AbstractIndexedPropertyManager implements IndexedPropertyManager {

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

	/**
	 * Constructs an AbstractPropertyManger. Establishes the time tracking and
	 * map option policies from the environment. Establishes the property value
	 * to people mapping if the MapOption is not NONE.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public AbstractIndexedPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		this.eventManger = context.getEventManager();		
		trackTime = propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME;		
		timeTrackingContainer = new DoubleValueContainer(0, initialSize);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		/*
		 * Record the time value if we are tracking assignment times.
		 */
		if (trackTime) {
			timeTrackingContainer.setValue(id, eventManger.getTime());
		}		
	}

	@Override
	public final double getPropertyTime(int id) {
		double result = 0;
		if (trackTime) {
			result = timeTrackingContainer.getValue(id);
		} else {
			throw new RuntimeException("Property time values are not being tracked");
		}
		return result;
	}
	@Override
	public void removeId(int id) {
		//do nothing
	}
}
