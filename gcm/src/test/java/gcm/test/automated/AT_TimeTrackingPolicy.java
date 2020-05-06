package gcm.test.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link TimeTrackingPolicy}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = TimeTrackingPolicy.class)
public class AT_TimeTrackingPolicy {

	@Test
	public void test() {
		// there should be two tracking policies
		assertEquals(2, TimeTrackingPolicy.values().length);
	}
	
	@Test
	public void testValueOf() {
		//nothing to test

	}
	
	@Test
	public void testValues() {
		//nothing to test
	}

}
