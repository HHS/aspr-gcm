package gcm.test.automated;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link SimulationErrorType}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = SimulationErrorType.class)
public class AT_SimulationErrorType {

	@Test
	public void testValueOf() {
		// nothing to test

	}

	@Test
	public void testValues() {
		// nothing to test
	}

	@Test
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
}
