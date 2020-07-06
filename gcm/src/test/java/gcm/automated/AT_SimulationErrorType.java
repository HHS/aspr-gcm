package gcm.automated;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link SimulationErrorType}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = SimulationErrorType.class)
public class AT_SimulationErrorType {

	/**
	 * Tests {@link SimulationErrorType#getDescription()}
	 */
	@Test
	@UnitTestMethod(name = "getDescription", args = {})
	public void testGetDescription() {
		// show that each ErrorType has a non-null, non-empty description
		for (SimulationErrorType simulationErrorType : SimulationErrorType.values()) {
			assertNotNull(simulationErrorType.getDescription());
			assertTrue(simulationErrorType.getDescription().length() > 0);
		}

		// show that each description is unique (ignoring case as well)
		Set<String> descriptions = new LinkedHashSet<>();
		for (SimulationErrorType simulationErrorType : SimulationErrorType.values()) {
			assertTrue("Duplicate ErrorType description: " + simulationErrorType.getDescription(), descriptions.add(simulationErrorType.getDescription().toLowerCase()));
		}
	}

	/**
	 * Tests {@link SimulationErrorType#valueOf(String)}
	 */
	@Test
	@UnitTestMethod(name = "valueOf", args = { String.class })
	public void testValueOf() {
		// nothing to test
	}

	/**
	 * Tests {@link SimulationErrorType#values()}
	 */
	@Test
	@UnitTestMethod(name = "values", args = {})
	public void testValues() {
		// nothing to test
	}

}
