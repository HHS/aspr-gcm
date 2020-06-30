package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.simulation.Equality;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link Equality}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Equality.class)
public class AT_Equality {

	/**
	 * Tests {@link Equality#isCompatibleComparisonValue(int)}
	 */
	@Test
	@UnitTestMethod(name = "isCompatibleComparisonValue", args= {int.class})
	public void testIsCompatibleComparisonValue() {

		assertEquals(6, Equality.values().length);

		/*
		 * Show that the six Equality members return the proper compatibility
		 * with integer comparison values.
		 */

		for (int i = 1; i <= 10; i++) {
			assertFalse(Equality.EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.EQUAL.isCompatibleComparisonValue(0));
			assertFalse(Equality.EQUAL.isCompatibleComparisonValue(i));

			assertTrue(Equality.NOT_EQUAL.isCompatibleComparisonValue(-i));
			assertFalse(Equality.NOT_EQUAL.isCompatibleComparisonValue(0));
			assertTrue(Equality.NOT_EQUAL.isCompatibleComparisonValue(i));

			assertTrue(Equality.LESS_THAN.isCompatibleComparisonValue(-i));
			assertFalse(Equality.LESS_THAN.isCompatibleComparisonValue(0));
			assertFalse(Equality.LESS_THAN.isCompatibleComparisonValue(i));

			assertTrue(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(0));
			assertFalse(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(i));

			assertFalse(Equality.GREATER_THAN.isCompatibleComparisonValue(-i));
			assertFalse(Equality.GREATER_THAN.isCompatibleComparisonValue(0));
			assertTrue(Equality.GREATER_THAN.isCompatibleComparisonValue(i));

			assertFalse(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(0));
			assertTrue(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(i));
		}
	}
		
	/**
	 * Tests {@link Equality#getNegation(Equality)}
	 */
	@Test
	@UnitTestMethod(name = "getNegation", args= {Equality.class})
	public void testGetNegation() {
		assertEquals(6, Equality.values().length);
		assertEquals(Equality.NOT_EQUAL, Equality.getNegation(Equality.EQUAL));
		assertEquals(Equality.EQUAL, Equality.getNegation(Equality.NOT_EQUAL));
		assertEquals(Equality.LESS_THAN_EQUAL, Equality.getNegation(Equality.GREATER_THAN));
		assertEquals(Equality.LESS_THAN, Equality.getNegation(Equality.GREATER_THAN_EQUAL));
		assertEquals(Equality.GREATER_THAN_EQUAL, Equality.getNegation(Equality.LESS_THAN));
		assertEquals(Equality.GREATER_THAN, Equality.getNegation(Equality.LESS_THAN_EQUAL));
	}
}