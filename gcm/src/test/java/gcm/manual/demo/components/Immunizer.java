package gcm.manual.demo.components;

import static gcm.simulation.partition.Filter.compartment;
import static gcm.simulation.partition.Filter.groupsForPersonAndGroupType;
import static gcm.simulation.partition.Filter.property;

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
import gcm.simulation.Plan;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.Partition;
import gcm.simulation.partition.PartitionSampler;

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
			addPartitions(environment);
		}
		planNextImmunization(environment);
	}

	private void immunizeFamily(final Environment environment, final PersonId personId) {
		GroupId groupId = environment.addGroup(GroupType.HOME);
		environment.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);

		int childCount = environment.getRandomGenerator().nextInt(3) + 1;
		childCount = Math.min(childCount, environment.getPartitionSize(IndexKey.IMMUNIZABLE_CHILDREN));
		environment.addPersonToGroup(personId, groupId);

		for (int i = 0; i < childCount; i++) {
			PartitionSampler partitionSampler = PartitionSampler.builder().build();
			Optional<PersonId> optional = environment.samplePartition(IndexKey.IMMUNIZABLE_CHILDREN,partitionSampler);
			if (optional.isPresent()) {
				PersonId familyMemberId = optional.get();
				environment.setPersonPropertyValue(familyMemberId, PersonProperty.IMMUNE, true);
				environment.addPersonToGroup(familyMemberId, groupId);
			} else {
				break;
			}
		}

		int adultCount = environment.getRandomGenerator().nextInt(2) + 1;
		adultCount = Math.min(adultCount, environment.getPartitionSize(IndexKey.IMMUNIZABLE_ADULTS));
		for (int i = 0; i < adultCount; i++) {
			PartitionSampler partitionSampler = PartitionSampler.builder().build();
			Optional<PersonId> optional = environment.samplePartition(IndexKey.IMMUNIZABLE_ADULTS,partitionSampler);
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

		PartitionSampler partitionSampler = PartitionSampler.builder().build();
		Optional<PersonId> option = environment.samplePartition(IndexKey.IMMUNIZABLE_CHILDREN,partitionSampler);
		if (option.isPresent()) {
			environment.addPlan(new ImmunizationPlan(option.get()), environment.getTime() + 0.01);
		}
	}

	private void addPartitions(Environment environment) {

		Filter filter = compartment(Compartment.SUSCEPTIBLE)//
						.and(property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true))//
						.and(property(PersonProperty.AGE, Equality.LESS_THAN, 15));//

		Partition partition = Partition.builder().setFilter(filter).build();
		environment.addPartition(partition, IndexKey.IMMUNIZABLE_CHILDREN);

		
		filter = compartment(Compartment.SUSCEPTIBLE)//
				.and(property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true))//
				.and(property(PersonProperty.AGE, Equality.GREATER_THAN, 25));//

		partition = Partition.builder().setFilter(filter).build();
		environment.addPartition(partition, IndexKey.IMMUNIZABLE_ADULTS);
		
		 
		filter = property(PersonProperty.IMMUNE, Equality.NOT_EQUAL, true)
					.or(property(PersonProperty.AGE, Equality.GREATER_THAN_EQUAL, 18)
						.and(groupsForPersonAndGroupType(GroupType.WORK, Equality.GREATER_THAN, 0)));
		partition = Partition.builder().setFilter(filter).build();		
		environment.addPartition(partition, IndexKey.NON_IMMUNIZED_WORKING_ADULTS);
		
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
