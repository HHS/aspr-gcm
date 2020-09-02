package gcm.manual.demo.components;

import static gcm.simulation.Filter.compartment;
import static gcm.simulation.Filter.groupsForPersonAndGroupType;
import static gcm.simulation.Filter.property;

import java.util.Optional;

import gcm.components.AbstractComponent;
import gcm.manual.demo.identifiers.Compartment;
import gcm.manual.demo.identifiers.GlobalProperty;
import gcm.manual.demo.identifiers.GroupType;
import gcm.manual.demo.identifiers.PersonProperty;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.simulation.Environment;
import gcm.simulation.Equality;
import gcm.simulation.Filter;
import gcm.simulation.Plan;

public class Immunizer extends AbstractComponent {

	private static enum IndexKey {
		IMMUNIZABLE_CHILDREN, IMMUNIZABLE_ADULTS, NON_IMMUNIZED_WORKING_ADULTS;
	}

	private static class ImmunizationPlan implements Plan {
		private final PersonId personId;

		public ImmunizationPlan(PersonId personId) {
			this.personId = personId;
		}
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		environment.setGlobalPropertyValue(GlobalProperty.ALPHA, environment.getRandomGenerator().nextDouble());
		ImmunizationPlan immunizationPlan = (ImmunizationPlan) plan;
		PersonId personId = immunizationPlan.personId;
		if (personId != null) {
			Boolean immune = environment.getPersonPropertyValue(personId, PersonProperty.IMMUNE);
			if (!immune) {
				immunizeFamily(environment, personId);
			}
		} else {
			addPopulationIndexes(environment);
		}
		planNextImmunization(environment);
	}

	private void immunizeFamily(final Environment environment, final PersonId personId) {
		GroupId groupId = environment.addGroup(GroupType.HOME);
		environment.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);

		int childCount = environment.getRandomGenerator().nextInt(3) + 1;
		childCount = Math.min(childCount, environment.getIndexSize(IndexKey.IMMUNIZABLE_CHILDREN));
		environment.addPersonToGroup(personId, groupId);

		for (int i = 0; i < childCount; i++) {
			Optional<PersonId> optional = environment.sampleIndex(IndexKey.IMMUNIZABLE_CHILDREN);
			if (optional.isPresent()) {
				PersonId familyMemberId = optional.get();
				environment.setPersonPropertyValue(familyMemberId, PersonProperty.IMMUNE, true);
				environment.addPersonToGroup(familyMemberId, groupId);
			} else {
				break;
			}
		}

		int adultCount = environment.getRandomGenerator().nextInt(2) + 1;
		adultCount = Math.min(adultCount, environment.getIndexSize(IndexKey.IMMUNIZABLE_ADULTS));
		for (int i = 0; i < adultCount; i++) {
			Optional<PersonId> optional = environment.sampleIndex(IndexKey.IMMUNIZABLE_ADULTS);
			if (optional.isPresent()) {
				PersonId familyMemberId = optional.get();
				environment.setPersonPropertyValue(familyMemberId, PersonProperty.IMMUNE, true);
				environment.addPersonToGroup(familyMemberId, groupId);
			} else {
				break;
			}
		}

	}

	private void planNextImmunization(Environment environment) {

		Optional<PersonId> option = environment.sampleIndex(IndexKey.IMMUNIZABLE_CHILDREN);
		if (option.isPresent()) {
			environment.addPlan(new ImmunizationPlan(option.get()), environment.getTime() + 0.01);
		}
	}

	private void addPopulationIndexes(Environment environment) {

		Filter filter = compartment(Compartment.SUSCEPTIBLE)//
						.and(property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true))//
						.and(property(PersonProperty.AGE, Equality.LESS_THAN, 15));//

		environment.addPopulationIndex(filter, IndexKey.IMMUNIZABLE_CHILDREN);

		
		filter = compartment(Compartment.SUSCEPTIBLE)//
				.and(property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true))//
				.and(property(PersonProperty.AGE, Equality.GREATER_THAN, 25));//

		environment.addPopulationIndex(filter, IndexKey.IMMUNIZABLE_ADULTS);
		
		 
		filter = property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true)
					.or(property(PersonProperty.AGE, Equality.GREATER_THAN_EQUAL, 18)
						.and(groupsForPersonAndGroupType(GroupType.WORK, Equality.GREATER_THAN, 0)));
		
		environment.addPopulationIndex(filter, IndexKey.NON_IMMUNIZED_WORKING_ADULTS);
		
	}

	@Override
	public void init(Environment environment) {
		 environment.addPlan(new ImmunizationPlan(null), environment.getTime()
		 + 1);
	}

	@Override
	public void close(Environment environment) {

	}
}
