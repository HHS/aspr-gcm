package gcm.manual.demo.plans;

import gcm.manual.demo.identifiers.Compartment;
import gcm.scenario.PersonId;
import gcm.simulation.Plan;

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