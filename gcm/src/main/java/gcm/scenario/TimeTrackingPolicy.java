package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * An enumeration used to control the modeler choice of tracking the last
 * assignment times of properties and other values.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public enum TimeTrackingPolicy {
	TRACK_TIME, DO_NOT_TRACK_TIME
}
