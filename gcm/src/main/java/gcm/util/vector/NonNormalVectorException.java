package gcm.util.vector;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
/**
 * A RuntimeException thrown when a vector is not normal(length = 1);
 */
@Source(status = TestStatus.UNREQUIRED)
public class NonNormalVectorException extends RuntimeException {

	
	private static final long serialVersionUID = 1L;

	
	public NonNormalVectorException() {
		super();
	}
	
	public NonNormalVectorException(String message) {
        super(message);
    }

}
