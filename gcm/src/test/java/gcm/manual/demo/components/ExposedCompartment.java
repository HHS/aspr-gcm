package gcm.manual.demo.components;

import gcm.components.AbstractComponent;
import gcm.manual.demo.datatypes.Sex;
import gcm.manual.demo.identifiers.Compartment;
import gcm.manual.demo.identifiers.PersonProperty;
import gcm.manual.demo.identifiers.Resource;
import gcm.manual.demo.plans.MovePlan;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.simulation.Environment;
import gcm.simulation.Plan;

public class ExposedCompartment extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		environment.observeCompartmentPersonArrival(true, Compartment.EXPOSED);
	}

	@Override
	public void observeCompartmentPersonArrival(final Environment environment, final PersonId personId) {
		
		double treatmentDelay = environment.getRandomGenerator().nextDouble() * 8;

		if (environment.getRandomGenerator().nextDouble() < 0.2) {
			environment.addPlan(new CancellationPlan(personId), treatmentDelay*0.95+ environment.getTime());
			environment.addPlan(new TreatmentPlan(personId), treatmentDelay + environment.getTime(), personId);
		} else {
			environment.addPlan(new TreatmentPlan(personId), treatmentDelay + environment.getTime());
		}
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		if (plan instanceof TreatmentPlan) {
			// distribute treatment
			TreatmentPlan treatmentPlan = (TreatmentPlan) plan;
			PersonId personId = treatmentPlan.personId;
			RegionId regionId = environment.getPersonRegion(personId);
			long resourceLevel = environment.getRegionResourceLevel(regionId, Resource.RESOURCE_1);
			long amount = Math.min(resourceLevel, 3);
			if (amount > 0) {
				environment.transferResourceToPerson(Resource.RESOURCE_1, personId, amount);
			}
			// plan transfer
			Integer age = environment.getPersonPropertyValue(personId, PersonProperty.AGE);
			Double height = environment.getPersonPropertyValue(personId, PersonProperty.HEIGHT);
			Sex sex = environment.getPersonPropertyValue(personId, PersonProperty.SEX);
			if (age < 25) {
				double moveTime = environment.getRandomGenerator().nextDouble() * 25 + environment.getTime();
				environment.addPlan(new MovePlan(personId, Compartment.INFECTED), moveTime);
			} else {
				if (height < 1.75 && sex.equals(Sex.FEMALE)) {
					double moveTime = environment.getRandomGenerator().nextDouble() * 10 + environment.getTime();
					environment.addPlan(new MovePlan(personId, Compartment.DEAD), moveTime);
				}
			}

		} else if (plan instanceof MovePlan) {
			MovePlan movePlan = (MovePlan) plan;
			PersonId personId = movePlan.getPersonId();
			long personResourceLevel = environment.getPersonResourceLevel(personId, Resource.RESOURCE_1);

			// remove some of the distributed resource
			if (movePlan.getCompartment().equals(Compartment.INFECTED)) {
				long amount = Math.min(1, personResourceLevel);
				if (amount > 0) {
					environment.transferResourceFromPerson(Resource.RESOURCE_1, personId, amount);
				}
			} else {
				if (personResourceLevel > 0) {
					environment.removeResourceFromPerson(Resource.RESOURCE_1, personId, personResourceLevel);
				}
			}
			environment.setPersonCompartment(movePlan.getPersonId(), movePlan.getCompartment());
		} else if (plan instanceof CancellationPlan){
			CancellationPlan cancellationPlan = (CancellationPlan)plan;
			environment.removePlan(cancellationPlan.planId);
		}
	}

	private static class TreatmentPlan implements Plan {
		private PersonId personId;

		public TreatmentPlan(PersonId personId) {
			this.personId = personId;
		}
	}

	private static class CancellationPlan implements Plan {
		private final Object planId;

		public CancellationPlan(Object planId) {
			this.planId = planId;
		}
	}

	@Override
	public void close(Environment environment) {

	}

}
