package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A RuntimeException thrown when a {@link SphericalPolygon} cannot be formed
 * from its {@link SphericalPoint} values.
 */
@Source(status = TestStatus.UNREQUIRED)
public class MalformedSphericalPolygonException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalPolygonException() {
		super();
	}

	public MalformedSphericalPolygonException(String message) {
		super(message);
	}

}
