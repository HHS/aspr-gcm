package gcmexample.gettingstarted;

import gcm.scenario.PersonId;
import gcm.simulation.Plan;

public class SeekTreatmentPlan implements Plan {
	private final PersonId personId;

	public SeekTreatmentPlan(final PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}
}