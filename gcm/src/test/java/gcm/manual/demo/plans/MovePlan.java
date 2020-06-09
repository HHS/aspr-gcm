package gcm.test.manual.demo.plans;

import gcm.scenario.PersonId;
import gcm.simulation.Plan;
import gcm.test.manual.demo.identifiers.Compartment;

public class MovePlan implements Plan {
		private final PersonId personId;
		private final Compartment compartment;

		public MovePlan(final PersonId personId, final Compartment compartment) {
			this.personId = personId;
			this.compartment = compartment;
		}

		public PersonId getPersonId() {
			return personId;
		}

		public Compartment getCompartment() {
			return compartment;
		}
		
	}