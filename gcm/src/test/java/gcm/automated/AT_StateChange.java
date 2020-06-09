package gcm.automated;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.output.reports.StateChange;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link StateChange}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StateChange.class)
public class AT_StateChange {

	@Test
	public void test() {
		for (StateChange stateChange : StateChange.values()) {
			// show that each state change has a non-null, positive length
			// string value
			assertNotNull(stateChange.toString());
			assertTrue(stateChange.toString().length() > 0);
		}
	}

	@Test
	public void testValueOf() {
		// nothing to test

	}

	@Test
	public void testValues() {
		// nothing to test
	}
}
