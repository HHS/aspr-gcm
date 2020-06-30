package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all material stages
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED,proxy = IntId.class)
public final class StageId extends IntId{

	public StageId(int id) {
		super(id);
	}

}
