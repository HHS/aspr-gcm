package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all batches
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED, proxy = IntId.class)
public final class BatchId extends IntId{

	public BatchId(int id) {
		super(id);
	}
}
