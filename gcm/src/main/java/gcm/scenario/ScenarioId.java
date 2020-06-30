package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all scenarios
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED,proxy = IntId.class)
public final class ScenarioId extends IntId{

	public ScenarioId(int id) {
		super(id);
	}
}
