package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A RuntimeException thrown when a {@link SphericalTriangle} cannot be formed.
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public class MalformedSphericalTriangleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalTriangleException() {
		super();
	}

	public MalformedSphericalTriangleException(String message) {
		super(message);
	}

}
