package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all replications
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.PROXY,proxy = IntId.class)
public final class ReplicationId extends IntId{

	public ReplicationId(int id) {
		super(id);
	}
}
