package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Enumeration representing the chirality of triangle vertex ordering on the
 * surface of a sphere.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public enum Chirality {
	RIGHT_HANDED, // counter clockwise
	LEFT_HANDED;// clockwise
}
