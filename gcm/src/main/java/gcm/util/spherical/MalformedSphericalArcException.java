package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A RuntimeException thrown when a {@link SphericalArc} cannot be formed due
 * to a null input or by two vertices that are too close together.
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public class MalformedSphericalArcException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalArcException() {
		super();
	}

	public MalformedSphericalArcException(String message) {
		super(message);
	}

}
