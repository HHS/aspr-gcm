package gcm.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.scenario.MapOption;
import gcm.util.annotations.UnitTest;

/**
 * Test class {@link MapOption}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MapOption.class)
public class AT_MapOption {

	@Test
	public void test() {
		// show that there are three map options
		assertEquals(3, MapOption.values().length);
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
