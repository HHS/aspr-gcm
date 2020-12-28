package gcm.manual;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardPeople;
import static gcm.automated.support.EnvironmentSupport.addStandardPropertyDefinitions;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;
import static gcm.automated.support.ExceptionAssertion.assertModelException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestMaterialsProducerId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRandomGeneratorId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestRegionPropertyId;
import gcm.replication.Replication;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.Partition;
import gcm.simulation.partition.PartitionSampler;
import gcm.util.annotations.UnitTestMethod;

public class Junk {
	Object UNVACCINATED_SCHOOL_AGE_CHILDREN = new Object();

	/**
	 * Tests {@link EnvironmentImpl#addPersonToGroup(PersonId, GroupId)}
	 */
	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { PersonId.class, GroupId.class })
	public void testAddPersonToGroup() {
		/*
		 * Assert that people can be added to groups
		 */
		final long seed = 3453456345345345L;
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Set<RegionId> coveredRegions = new LinkedHashSet<>();

			Filter filter = Filter.property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.GREATER_THAN_EQUAL, 6);
			filter = filter.and(Filter.property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.LESS_THAN_EQUAL, 12));

			Partition partition = Partition	.builder().setFilter(filter)					
											.setRegionFunction((regionId) -> {
												String regionCode = environment.getRegionPropertyValue(regionId, TestRegionPropertyId.REGION_PROPERTY_1);
												String stateCode = regionCode.substring(3, 5);
												switch (stateCode) {
												case "123":
													return "VA";
												case "234":
													return "MD";
												default:
													return "DEL";
												}
											}).build();

			environment.addPartition(partition, UNVACCINATED_SCHOOL_AGE_CHILDREN);

			LabelSet labelSet = LabelSet.builder().setRegionLabel("MD").build();
			PartitionSampler partitionSampler = PartitionSampler.builder().					
					setLabelSet(labelSet).
					//setExcludedPerson(excludedPersonId).
					setRandomNumberGeneratorId(TestRandomGeneratorId.COMET).
					setLabelSetWeightingFunction((env, labels)->{						
						if(labels.getRegionLabel().equals("VA")) {
							return 0.5;
						}
						return 1;
					}).
					build();
			Optional<PersonId> partitionSample = environment.samplePartition(UNVACCINATED_SCHOOL_AGE_CHILDREN, partitionSampler);
			if (partitionSample.isPresent()) {
				PersonId sampledPersonid = partitionSample.get();
				// do something with this person...
			}

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			int groupCount = 0;
			// create some groups

			for (final TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (int i = 0; i < 5; i++) {
					final GroupId groupId = environment.addGroup(testGroupTypeId);
					assertEquals(groupCount, groupId.getValue());
					groupCount++;
				}
			}

			final List<PersonId> people = environment.getPeople();
			// show that there are enough people to make a valid test
			assertTrue(people.size() > 100);

			for (final PersonId personId : people) {
				// pick a random group
				final GroupId groupId = new GroupId(environment.getRandomGenerator().nextInt(groupCount));
				// put the person in the group
				environment.addPersonToGroup(personId, groupId);
				// show that the person is now in the group
				assertTrue(environment.isGroupMember(personId, groupId));
			}

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// if the group id is null
			assertModelException(() -> environment.addPersonToGroup(new PersonId(0), null), SimulationErrorType.NULL_GROUP_ID);
			// if the group id is unknown
			assertModelException(() -> environment.addPersonToGroup(new PersonId(0), new GroupId(-1)), SimulationErrorType.UNKNOWN_GROUP_ID);
			// if the person id is null
			assertModelException(() -> environment.addPersonToGroup(null, new GroupId(0)), SimulationErrorType.NULL_PERSON_ID);
			// if the person id is unknown
			assertModelException(() -> environment.addPersonToGroup(new PersonId(-1), new GroupId(0)), SimulationErrorType.UNKNOWN_PERSON_ID);
			GroupId groupId = environment.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			environment.addPersonToGroup(new PersonId(0), groupId);
			assertModelException(() -> environment.addPersonToGroup(new PersonId(0), groupId), SimulationErrorType.DUPLICATE_GROUP_MEMBERSHIP);
		});

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			// find a person who is not in a group and add them to that
			// group
			boolean candidateFound = false;
			List<PersonId> people = environment.getPeople();
			List<GroupId> groupIds = environment.getGroupIds();
			groupLoop: for (GroupId groupId : groupIds) {
				for (PersonId personId : people) {
					if (!environment.isGroupMember(personId, groupId)) {
						assertModelException(() -> environment.addPersonToGroup(personId, groupId), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
						candidateFound = true;
						break groupLoop;
					}
				}
			}
			// show that the model exception assertion was performed
			assertTrue(candidateFound);
		});
		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

}
