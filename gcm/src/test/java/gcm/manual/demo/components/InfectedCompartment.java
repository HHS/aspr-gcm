package gcm.test.manual.demo.components;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.components.AbstractComponent;
import gcm.scenario.PersonId;
import gcm.simulation.Environment;
import gcm.simulation.Plan;
import gcm.test.manual.demo.identifiers.Compartment;
import gcm.test.manual.demo.identifiers.CompartmentProperty;
import gcm.test.manual.demo.identifiers.PersonProperty;
import gcm.test.manual.demo.identifiers.Resource;
import gcm.test.manual.demo.plans.MovePlan;

public class InfectedCompartment extends AbstractComponent {
	
	@Override
	public void init(Environment environment) {
		double weightThreshold = environment.getRandomGenerator().nextDouble() * 20 + 60;
		environment.setCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD, weightThreshold);
		environment.observeCompartmentPersonArrival(true, Compartment.INFECTED);
	}

	@Override
	public void observeCompartmentPersonArrival(final Environment environment, final PersonId personId) {
		Float weight = environment.getPersonPropertyValue(personId, PersonProperty.WEIGHT);
		RandomGenerator randomGenerator = environment.getRandomGenerator();

		double weightThreshold = environment.getCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD);
		if (weight > weightThreshold) {
			double moveTime = randomGenerator.nextDouble() * 5 + environment.getTime();
			environment.addPlan(new MovePlan(personId, Compartment.DEAD), moveTime);
		}
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		MovePlan movePlan = (MovePlan) plan;
		PersonId personId = movePlan.getPersonId();
		long personResourceLevel = environment.getPersonResourceLevel(personId, Resource.RESOURCE_1);
		if (personResourceLevel > 0) {
			environment.removeResourceFromPerson(Resource.RESOURCE_1, personId, personResourceLevel);
		}
		environment.setPersonCompartment(movePlan.getPersonId(), movePlan.getCompartment());
	}

	@Override
	public void close(Environment environment) {
	}
}
