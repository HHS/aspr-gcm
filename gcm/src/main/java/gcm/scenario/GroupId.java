package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all groups
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.PROXY,proxy = IntId.class)
public final class GroupId extends IntId{

	public GroupId(int id) {
		super(id);
	}
}
