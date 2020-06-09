package gcm.test.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.output.reports.ReportPeriod;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link ReportPeriod}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ReportPeriod.class)
public class AT_ReportPeriod {

	@Test
	public void test() {
		//show that there are three report periods
		assertEquals(3, ReportPeriod.values().length);
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
