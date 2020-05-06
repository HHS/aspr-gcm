package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.util.annotations.Source;

/**
 * A convenience class to ease dealing with generating a random weighted person contact
 * via an Optional interface.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class StochasticPersonSelection {

	private final PersonId personId;

	private final boolean errorOccured;

	/**
	 * Returns the contacted person. Null value indicates that no person was
	 * selected for contact.
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * True if and only if no error occurred while trying to resolve a contact.
	 * Errors are 1) a negative weight value 2) an infinite weight value or 3)
	 * the sum of all weights is infinite.
	 */
	public boolean errorOccured() {
		return errorOccured;
	}

	/**
	 * Constructor for StochasticPersonSelection
	 * 
	 * @param personId
	 *            null indicates that no person is selected
	 * @param errorOccured
	 *            indicates that weight values were in some way inconsistent
	 *            with selecting a single person
	 */
	public StochasticPersonSelection(PersonId personId, boolean errorOccured) {
		super();
		this.personId = personId;
		this.errorOccured = errorOccured;
	}

}
