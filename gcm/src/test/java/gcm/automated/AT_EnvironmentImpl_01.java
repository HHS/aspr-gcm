package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardPeople;
import static gcm.automated.support.EnvironmentSupport.addStandardPropertyDefinitions;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;
import static gcm.automated.support.ExceptionAssertion.assertModelException;
import static gcm.simulation.Filter.compartment;
import static gcm.simulation.Filter.resource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.EmptyTaskPlan;
import gcm.automated.support.EnvironmentSupport;
import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestMaterialId;
import gcm.automated.support.TestMaterialsProducerId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestResourceId;
import gcm.replication.Replication;
import gcm.scenario.BatchId;
import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.Filter;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_01 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {		
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(1));		
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		//System.out.println(SEED_PROVIDER.generateUnusedSeedReport());
	}

	
	/**
	 * Tests {@link EnvironmentImpl#addGroup(GroupTypeId)}
	 */
	@Test
	public void testAddGroup() {
		/*
		 * Assert that the groups can be added
		 */

		final long seed = SEED_PROVIDER.getSeedValue(0);
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

			for (final TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				final GroupId groupId = environment.addGroup(testGroupTypeId);
				assertTrue(environment.groupExists(groupId));
			}
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// if the group type id is null
			assertModelException(() -> environment.addGroup(null), SimulationErrorType.NULL_GROUP_TYPE_ID);
			// if the group type id is unknown
			assertModelException(() -> environment.addGroup(TestGroupTypeId.getUnknownGroupTypeId()), SimulationErrorType.UNKNOWN_GROUP_TYPE_ID);
		});

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			// if the component is not a global,region or compartment
			// component
			assertModelException(() -> environment.addGroup(TestGroupTypeId.GROUP_TYPE_1), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

	}

	/**
	 * Tests {@link EnvironmentImpl#addPerson(RegionId, CompartmentId)}
	 */
	@Test
	public void testAddPerson() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * This first test plan just checks that people are added sequentially
		 * and are located where we expect them to be
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/*
			 * Determine the person id we expect will be returned by the
			 * environment upon the next addPerson() invocation.
			 */
			Integer expectedPersonIdValue = environment.getPopulationCount();

			for (final TestRegionId testRegionId : TestRegionId.values()) {
				for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
					final PersonId personId = environment.addPerson(testRegionId, testCompartmentId);
					/*
					 * Assert that the environment is assigning the person id as
					 * we expect
					 */
					PersonId expectedPersonId = new PersonId(expectedPersonIdValue++);
					assertEquals(expectedPersonId, personId);

					/*
					 * Assert that the new person is located where we expect
					 * them
					 */
					assertEquals(testRegionId, environment.getPersonRegion(personId));
					assertEquals(testCompartmentId, environment.getPersonCompartment(personId));

				}
			}

		});

		/*
		 * Now we test preconditions
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {

			assertModelException(() -> environment.addPerson(null, TestCompartmentId.COMPARTMENT_1), SimulationErrorType.NULL_REGION_ID);
			assertModelException(() -> environment.addPerson(TestRegionId.getUnknownRegionId(), TestCompartmentId.COMPARTMENT_1), SimulationErrorType.UNKNOWN_REGION_ID);
			assertModelException(() -> environment.addPerson(TestRegionId.REGION_1, null), SimulationErrorType.NULL_COMPARTMENT_ID);
			assertModelException(() -> environment.addPerson(TestRegionId.REGION_1, TestCompartmentId.getUnknownCompartmentId()), SimulationErrorType.UNKNOWN_COMPARTMENT_ID);

		});

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			assertModelException(() -> environment.addPerson(TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#addPersonToGroup(PersonId, GroupId)}
	 */
	@Test
	public void testAddPersonToGroup() {
		/*
		 * Assert that people can be added to groups
		 */
		final long seed = SEED_PROVIDER.getSeedValue(2);
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

	/**
	 * Tests {@link EnvironmentImpl#addPlan(gcm.simulation.Plan, double)
	 *
	 */
	@Test
	public void testAddPlan() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * We demonstrate that a plan is added successfully by showing that the
		 * plan gets executed at the appropriate time.
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// check that the time of execution is correct
			assertEquals(1.0, environment.getTime(), 0);
		});

		/*
		 * We now test preconditions
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// if the plan is null
			assertModelException(() -> environment.addPlan(null, 1000, "key1"), SimulationErrorType.NULL_PLAN);

			// if the plan time is in the past
			assertModelException(() -> environment.addPlan(new EmptyTaskPlan(400, "key2"), 0, "key2"), SimulationErrorType.PAST_PLANNING_TIME);

			// if the key is null
			assertModelException(() -> environment.addPlan(new EmptyTaskPlan(500, ""), 10, (Object[]) null), SimulationErrorType.NULL_PLAN_KEY);

			// if the key corresponds to an active plan
			environment.addPlan(new EmptyTaskPlan(600, "key3"), 25, "key3");
			assertModelException(() -> environment.addPlan(new EmptyTaskPlan(700, "key3"), 10, "key3"), SimulationErrorType.DUPLICATE_PLAN_KEY);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#addPopulationIndex(Filter, Object)}
	 *
	 */
	@Test
	public void testAddPopulationIndex() {

		/*
		 * Tests the population index by distributing resources to some, but not
		 * all people and filtering on the amount given. Shows that the index
		 * reflects the expected people before and after changes that should
		 * effect the index.
		 */
		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/*
			 * Build a filter that selects people who are in compartment 1 and
			 * have at least 3 units of resource 3
			 */

			
			Filter filter = compartment(TestCompartmentId.COMPARTMENT_1).and(
			resource(TestResourceId.RESOURCE3, Equality.GREATER_THAN_EQUAL, 3));
			
			 

			/*
			 * add the index to the environment
			 */
			final Object indexKey = "key 1";
			environment.addPopulationIndex(filter, indexKey);

			// show that there are no people in the index
			assertEquals(0, environment.getIndexedPeople(indexKey).size());

			// now give some people some the of the resource, making sure
			// that some people get it, but not enough to pass the filter.

			final Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (final PersonId personId : environment.getPeople()) {
				final RegionId regionId = environment.getPersonRegion(personId);
				final long amount = personId.getValue() % 4;
				if (amount > 0) {
					environment.addResourceToRegion(TestResourceId.RESOURCE3, regionId, amount);
					environment.transferResourceToPerson(TestResourceId.RESOURCE3, personId, amount);
					if (amount > 2) {
						final CompartmentId compartmentId = environment.getPersonCompartment(personId);
						if (compartmentId.equals(TestCompartmentId.COMPARTMENT_1)) {
							expectedPeople.add(personId);
						}
					}
				}

			}
			// make sure that we didn't make a mistake and are not about to
			// compare to empty sets
			assertTrue(expectedPeople.size() > 0);

			// now show that the people in the index match our expectations
			final Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(indexKey));

			assertEquals(expectedPeople, actualPeople);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Object key = "key 2";
			assertModelException(() -> environment.addPopulationIndex(null, key), SimulationErrorType.NULL_FILTER);

			Filter filter = compartment(TestCompartmentId.COMPARTMENT_2);
			assertModelException(() -> environment.addPopulationIndex(filter, null), SimulationErrorType.NULL_POPULATION_INDEX_KEY);

			environment.addPopulationIndex(filter, key);
			assertModelException(() -> environment.addPopulationIndex(filter, key), SimulationErrorType.DUPLICATE_INDEXED_POPULATION);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}


	/**
	 * Tests {@link EnvironmentImpl#addResourceToRegion(ResourceId, RegionId, long)}
	 */
	@Test
	public void testAddResourceToRegion() {
		final long seed = SEED_PROVIDER.getSeedValue(6);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final TestRegionId testRegionId : TestRegionId.values()) {
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					/*
					 * determine the current level
					 */
					final long currentResourceLevel = environment.getRegionResourceLevel(testRegionId, testResourceId);

					/*
					 * generate a positive amount to add
					 */
					final long amount = environment.getRandomGenerator().nextInt(99) + 1;

					/*
					 * add amount to region
					 */
					final long expectedResourceLevel = currentResourceLevel + amount;
					environment.addResourceToRegion(testResourceId, testRegionId, amount);

					/*
					 * make sure that we still agree
					 */
					final long actualResourceLevel = environment.getRegionResourceLevel(testRegionId, testResourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);
				}
			}

		});

		/*
		 * We now test all preconditions
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// if the region id is null
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, null, 1), SimulationErrorType.NULL_REGION_ID);
			// if the region id is unknown
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.getUnknownRegionId(), 1), SimulationErrorType.UNKNOWN_REGION_ID);
			// if the resource id is null
			assertModelException(() -> environment.addResourceToRegion(null, TestRegionId.REGION_1, 1), SimulationErrorType.NULL_RESOURCE_ID);
			// if the resource id is unknown
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.getUnknownResourceId(), TestRegionId.REGION_1, 1), SimulationErrorType.UNKNOWN_RESOURCE_ID);
			// if the amount is negative
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, -1), SimulationErrorType.NEGATIVE_RESOURCE_AMOUNT);

			// if the amount results in an overflow of the corresponding
			// region's inventory level
			// max out region1's inventory of resource2
			long regionResourceLevel = environment.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.RESOURCE2);
			environment.addResourceToRegion(TestResourceId.RESOURCE2, TestRegionId.REGION_1, Long.MAX_VALUE - regionResourceLevel);
			// now add a bit more
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE2, TestRegionId.REGION_1, 10), SimulationErrorType.RESOURCE_ARITHMETIC_EXCEPTION);

		});

		/*
		 * Precondition : Show that a non-global component cannot add resources
		 * to region
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			// if the invoker is not a global component or the region
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, 1), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
		});

		/*
		 * Precondition : Show that a region cannot add resource to another
		 * region
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_2, testTime++, (environment) -> {
			// if the invoker is not a global component or the region
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, 1), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
		});

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			// if the invoker is not a global component or the region
			assertModelException(() -> environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, 1), SimulationErrorType.COMPONENT_LACKS_PERMISSION);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link EnvironmentImpl#batchExists(BatchId)}
	 */
	@Test
	public void testBatchExists() {
		/*
		 * Show that batches exist as expected before construction, after
		 * construction and after destruction
		 */

		final long seed = SEED_PROVIDER.getSeedValue(7);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {

			final BatchId batchId1 = environment.createBatch(TestMaterialId.MATERIAL_1, 1);
			final BatchId batchId2 = environment.createBatch(TestMaterialId.MATERIAL_2, 2);
			assertTrue(environment.batchExists(batchId1));
			assertTrue(environment.batchExists(batchId2));
			environment.destroyBatch(batchId1);
			assertFalse(environment.batchExists(batchId1));
			assertTrue(environment.batchExists(batchId2));
			environment.destroyBatch(batchId2);
			assertFalse(environment.batchExists(batchId1));
			assertFalse(environment.batchExists(batchId2));

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

}
