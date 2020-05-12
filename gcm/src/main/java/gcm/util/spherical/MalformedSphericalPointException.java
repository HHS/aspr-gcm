package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A RuntimeException thrown when a {@link SphericalPoint} cannot be formed due
 * to a non-normalizable input.
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public class MalformedSphericalPointException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalPointException() {
		super();
	}

	public MalformedSphericalPointException(String message) {
		super(message);
	}

}
