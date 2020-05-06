package gcm.test.automated;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gcm.scenario.PersonId;
import gcm.simulation.StochasticPersonSelection;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link StochasticPersonSelection}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StochasticPersonSelection.class)
public class AT_StochasticPersonSelection {
	@Test
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
	
	@Test
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
