package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gcm.scenario.PersonId;
import gcm.simulation.StochasticPersonSelection;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link StochasticPersonSelection}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StochasticPersonSelection.class)
public class AT_StochasticPersonSelection {

	/**
	 * Tests {@link StochasticPersonSelection#errorOccured()}
	 */
	@Test
	@UnitTestMethod(name = "errorOccured", args = {})
	public void testErrorOccured() {
		// show that a constructed StochasticPersonSelection contains the values
		// used to create it
		PersonId personId = new PersonId(4576);
		boolean errorOccured = true;

		StochasticPersonSelection stochasticPersonSelection = new StochasticPersonSelection(personId, errorOccured);
		assertEquals(errorOccured, stochasticPersonSelection.errorOccured());

		personId = new PersonId(5346);
		errorOccured = false;

		stochasticPersonSelection = new StochasticPersonSelection(personId, errorOccured);
		assertEquals(errorOccured, stochasticPersonSelection.errorOccured());

	}

	/**
	 * Test
	 * {@link StochasticPersonSelection#StochasticPersonSelection(PersonId, boolean)}
	 */
	@Test
	@UnitTestConstructor(args = {PersonId.class, boolean.class})
	public void testConstructor() {
		PersonId personId = new PersonId(7);

		StochasticPersonSelection stochasticPersonSelection = new StochasticPersonSelection(personId, false);
		assertNotNull(stochasticPersonSelection);
		assertEquals(personId, stochasticPersonSelection.getPersonId());
		assertFalse(stochasticPersonSelection.errorOccured());
		
		stochasticPersonSelection = new StochasticPersonSelection(personId, true);
		assertNotNull(stochasticPersonSelection);
		assertEquals(personId, stochasticPersonSelection.getPersonId());
		assertTrue(stochasticPersonSelection.errorOccured());
		
	}

	/**
	 * Test {@link StochasticPersonSelection#getPersonId()}
	 */
	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		// show that a constructed StochasticPersonSelection contains the values
		// used to create it
		PersonId personId = new PersonId(4576);
		boolean errorOccured = true;

		StochasticPersonSelection stochasticPersonSelection = new StochasticPersonSelection(personId, errorOccured);
		assertEquals(personId, stochasticPersonSelection.getPersonId());

		personId = new PersonId(5346);
		errorOccured = false;

		stochasticPersonSelection = new StochasticPersonSelection(personId, errorOccured);
		assertEquals(personId, stochasticPersonSelection.getPersonId());

	}

}
