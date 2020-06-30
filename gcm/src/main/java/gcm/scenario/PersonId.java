package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all people
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED,proxy = IntId.class)
public final class PersonId extends IntId{

	public PersonId(int id) {
		super(id);
	}
}
