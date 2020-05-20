package gcm.output.simstate;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Status type for {@link LogItem}
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.UNEXPECTED)
public enum LogStatus {
	INFO, ERROR, WARNING, DEBUG, TRACE
}
