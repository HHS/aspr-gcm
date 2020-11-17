package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardPeople;
import static gcm.automated.support.EnvironmentSupport.addStandardPropertyDefinitions;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.generatePropertyValue;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;
import static gcm.automated.support.ExceptionAssertion.assertModelException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlan;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestResourceId;
import gcm.replication.Replication;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.ResourceId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.TimeTrackingPolicy;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_08 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(8));
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
//		 System.out.println(AT_EnvironmentImpl_08.class.getSimpleName() + " "
//		 + SEED_PROVIDER.generateUnusedSeedReport());		
	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonPropertyDefinition(PersonPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyDefinition", args = {PersonPropertyId.class})
	public void testGetPersonPropertyDefinition() {
		/*
		 * Retrieve each person property definition and assert that it equals
		 * the one held in the test plan executor.
		 */
		final long seed = SEED_PROVIDER.getSeedValue(11);
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

		for (final TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
				final PropertyDefinition expectedPropertyDefinition = scenario.getPersonPropertyDefinition(testPersonPropertyId);
				final PropertyDefinition actualPropertyDefinition = environment.getPersonPropertyDefinition(testPersonPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			});
		}

		/*
		 * Precondition Tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// if the property id is null
			assertModelException(() -> environment.getPersonPropertyDefinition(null), SimulationErrorType.NULL_PERSON_PROPERTY_ID);
			// if the property id does not correspond to a known
			// person property identifier
			assertModelException(() -> environment.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()), SimulationErrorType.UNKNOWN_PERSON_PROPERTY_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonPropertyIds()}
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		/*
		 * Assert that the person property identifiers from the environment are
		 * the same as the elements of the local PersonPropertyId enumeration.
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

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			final Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
			assertEquals(expectedPersonPropertyIds, environment.getPersonPropertyIds());
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getPersonPropertyValue(PersonId, PersonPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyValue", args = {PersonId.class, PersonPropertyId.class})
	public void testGetPersonPropertyValue() {

		/*
		 * For each person and person property definition, show that the current
		 * value is the default value we expect from the property definition.
		 * Then show that after we change the value that we can retrieve the new
		 * value just so we know that we are not observing an artifact of the
		 * original default value.
		 */
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.TRUE, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			for (final PersonId personId : environment.getPeople()) {
				for (final TestPersonPropertyId propertyID : TestPersonPropertyId.values()) {
					/*
					 * We first assert that the current value of the property is
					 * the default for the definition.
					 */

					final PropertyDefinition propertyDefinition = environment.getPersonPropertyDefinition(propertyID);
					assertTrue(propertyDefinition.getDefaultValue().isPresent());
					final Object expectedPropertyValue = propertyDefinition.getDefaultValue().get();
					final Object actualPropertyValue = environment.getPersonPropertyValue(personId, propertyID);
					assertEquals(expectedPropertyValue, actualPropertyValue);

					/*
					 * To be more thorough, we also assert that we can retrieve
					 * any arbitrary value since we know that default values are
					 * potentially handled as a special case.
					 */
					Object updatedPropertyValue = null;
					while ((updatedPropertyValue == null) || updatedPropertyValue.equals(actualPropertyValue)) {
						updatedPropertyValue = generatePropertyValue(propertyDefinition, environment.getRandomGenerator());
					}
					environment.setPersonPropertyValue(personId, propertyID, updatedPropertyValue);
					final Object retrievedPropertyValue = environment.getPersonPropertyValue(personId, propertyID);
					assertEquals(updatedPropertyValue, retrievedPropertyValue);
				}
			}

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// make sure that person 1 actually exists
			assertTrue(environment.personExists(new PersonId(1)));

			// if the person is null
			assertModelException(() -> environment.getPersonPropertyValue(null, TestPersonPropertyId.PERSON_PROPERTY_1), SimulationErrorType.NULL_PERSON_ID);
			// if the person is unknown
			assertModelException(() -> environment.getPersonPropertyValue(new PersonId(-1), TestPersonPropertyId.PERSON_PROPERTY_1), SimulationErrorType.UNKNOWN_PERSON_ID);
			// if the property id is null
			assertModelException(() -> environment.getPersonPropertyValue(new PersonId(1), null), SimulationErrorType.NULL_PERSON_PROPERTY_ID);
			// if the property is unknown
			assertModelException(() -> environment.getPersonPropertyValue(new PersonId(1), TestPersonPropertyId.getUnknownPersonPropertyId()), SimulationErrorType.UNKNOWN_PERSON_PROPERTY_ID);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonRegion(PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonRegion", args = {PersonId.class})
	public void testGetPersonRegion() {
		/*
		 * Assert that we get the region id associated with each person and that
		 * it will match the value given to that person at simulation
		 * initialization.
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

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			for (final PersonId personId : scenario.getPeopleIds()) {
				final TestRegionId expectedRegionId = scenario.getPersonRegion(personId);
				final TestRegionId actualRegionId = environment.getPersonRegion(personId);
				assertEquals(expectedRegionId, actualRegionId);
			}
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// make sure that person 1 actually exists
			assertTrue(environment.personExists(new PersonId(1)));

			// if the person is null
			assertModelException(() -> environment.getPersonRegion(null), SimulationErrorType.NULL_PERSON_ID);
			// if the person is unknown
			assertModelException(() -> environment.getPersonRegion(new PersonId(-1)), SimulationErrorType.UNKNOWN_PERSON_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonRegionArrivalTime(PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonRegionArrivalTime", args = {PersonId.class})
	public void testGetPersonRegionArrivalTime() {
		/*
		 * For each person first show that the region arrival time is zero. Next
		 * move each person so that the new region arrival times will be the
		 * current time and test that this is so. Finally, move forward in time
		 * and show that the region arrival times still reflect the time when we
		 * set them and not the current time.
		 */
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
		 * Show that each person arrived in its region at time = 0
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			final double expectedRegionArrivalTime = 0;
			for (final PersonId personId : scenario.getPeopleIds()) {
				final double actualRegionArrivalTime = environment.getPersonRegionArrivalTime(personId);
				assertEquals(expectedRegionArrivalTime, actualRegionArrivalTime, 0);
			}
		});

		/*
		 * Move each person to next region and show that the region arrival time
		 * is the current time
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			final double expectedRegionArrivalTime = 2.0;
			for (final PersonId personId : scenario.getPeopleIds()) {
				// move the person to the next Region
				final TestRegionId currentRegionId = environment.getPersonRegion(personId);
				final TestRegionId nextRegionId = currentRegionId.next();
				environment.setPersonRegion(personId, nextRegionId);

				// show that the person arrived at their current Region
				// at the current time

				final double actualRegionArrivalTime = environment.getPersonRegionArrivalTime(personId);
				assertEquals(expectedRegionArrivalTime, actualRegionArrivalTime, 0);
			}
		});

		/*
		 * Move forward in time and show that all the region arrival times still
		 * reflect the time when the people were moved.
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			final double expectedRegionArrivalTime = 2.0;
			for (final PersonId personId : scenario.getPeopleIds()) {
				// show that the person arrived at their current region
				// when they were previously moved and not the current time.

				final double actualRegionArrivalTime = environment.getPersonRegionArrivalTime(personId);
				assertEquals(expectedRegionArrivalTime, actualRegionArrivalTime, 0);
			}
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// if the person is unknown
			assertModelException(() -> environment.getPersonRegionArrivalTime(null), SimulationErrorType.NULL_PERSON_ID);

			// if the person is unknown
			assertModelException(() -> environment.getPersonRegionArrivalTime(new PersonId(-1)), SimulationErrorType.UNKNOWN_PERSON_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonResourceLevel(PersonId, ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceLevel", args = {PersonId.class, ResourceId.class})
	public void testGetPersonResourceLevel() {
		/*
		 * For each person and resource, show that the current resource level is
		 * zero. Then show that after we change the level that we can retrieve
		 * the new value just so we know that we are not observing an artifact
		 * of the original default value.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(10);
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

			for (final PersonId personId : environment.getPeople()) {
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					/*
					 * We first assert that the current resource level is zero
					 */
					long expectedResourceLevel = 0;
					long actualResourceLevel = environment.getPersonResourceLevel(personId, testResourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);

					/*
					 * To be more thorough, we also assert that we can retrieve
					 * any arbitrary level since we know that default values are
					 * potentially handled as a special case.
					 */
					final long amountToAdd = environment.getRandomGenerator().nextInt(100) + 1;
					/*
					 * We cannot add directly to a person and must first add to
					 * the person's region to ensure the inventory exists and
					 * then transfer that amount to the person
					 */
					final TestRegionId testRegionId = environment.getPersonRegion(personId);
					environment.addResourceToRegion(testResourceId, testRegionId, amountToAdd);
					environment.transferResourceToPerson(testResourceId, personId, amountToAdd);
					expectedResourceLevel = amountToAdd;
					actualResourceLevel = environment.getPersonResourceLevel(personId, testResourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);
				}
			}

		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			assertTrue(environment.personExists(new PersonId(1)));
			// if the person is null
			assertModelException(() -> environment.getPersonResourceLevel(null, TestResourceId.RESOURCE1), SimulationErrorType.NULL_PERSON_ID);
			// if the person is unknown
			assertModelException(() -> environment.getPersonResourceLevel(new PersonId(-1), TestResourceId.RESOURCE1), SimulationErrorType.UNKNOWN_PERSON_ID);
			// if the resource id is null
			assertModelException(() -> environment.getPersonResourceLevel(new PersonId(1), null), SimulationErrorType.NULL_RESOURCE_ID);
			// if the resource is unknown
			assertModelException(() -> environment.getPersonResourceLevel(new PersonId(1), TestResourceId.getUnknownResourceId()), SimulationErrorType.UNKNOWN_RESOURCE_ID);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link EnvironmentImpl#getPersonResourceTime(PersonId, ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonResourceTime", args = {PersonId.class, ResourceId.class})
	public void testGetPersonResourceTime() {
		/*
		 * For each person resource level first show that the resource time is
		 * zero. Next change each resource level so that the new resource times
		 * will be the current time and test that this is so. Finally, move
		 * forward in time and show that the resource times still reflect the
		 * time when we set them and not the current time.
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

		/*
		 * Show that tracking is turned on for at least one resource and turned
		 * off for at least one resource to ensure that every test below will be
		 * conducted at least once.
		 */

		int trackingOnCount = 0;
		int trackingOffCount = 0;
		for (final TestResourceId testResourceId : TestResourceId.values()) {
			if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.TRACK_TIME) {
				trackingOnCount++;
			} else {
				trackingOffCount++;
			}
		}
		assertTrue(trackingOffCount > 0);
		assertTrue(trackingOnCount > 0);

		/*
		 * Show that the current resource times are zero
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.TRACK_TIME) {
						final double expectedResourceTime = 0;
						final double actualResourceTime = environment.getPersonResourceTime(personId, testResourceId);
						assertEquals(expectedResourceTime, actualResourceTime, 0);
					}
				}
			}

		});

		/*
		 * Add some resource to each person and assert that the resource time is
		 * the current time
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.TRACK_TIME) {
						final long amountToAdd = environment.getRandomGenerator().nextInt(100) + 1;
						/*
						 * We cannot add directly to a person and must first add
						 * to the person's region to ensure the inventory exists
						 * and then transfer that amount to the person
						 */
						final TestRegionId testRegionId = environment.getPersonRegion(personId);
						environment.addResourceToRegion(testResourceId, testRegionId, amountToAdd);
						environment.transferResourceToPerson(testResourceId, personId, amountToAdd);
						final double expectedResourceTime = 2.0;
						final double actualResourceTime = environment.getPersonResourceTime(personId, testResourceId);
						assertEquals(expectedResourceTime, actualResourceTime, 0);
					}
				}
			}

		});

		/*
		 * Show that the resource times reflect the past resource additions and
		 * not the current time
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.TRACK_TIME) {
						final double expectedResourceTime = 2.0;
						final double actualResourceTime = environment.getPersonResourceTime(personId, testResourceId);
						assertEquals(expectedResourceTime, actualResourceTime, 0);
					}
				}
			}

		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// find a resource that has tracking turned on
			TestResourceId r = null;
			for (final TestResourceId testResourceId : TestResourceId.values()) {
				if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.TRACK_TIME) {
					r = testResourceId;
					break;
				}
			}
			final TestResourceId resourceWithTracking = r;

			for (final TestResourceId testResourceId : TestResourceId.values()) {
				if (testResourceId.trackValueAssignmentTimes() == TimeTrackingPolicy.DO_NOT_TRACK_TIME) {
					r = testResourceId;
					break;
				}
			}
			final TestResourceId resourceWithoutTracking = r;

			assertTrue(environment.personExists(new PersonId(1)));
			// if the person is null
			assertModelException(() -> environment.getPersonResourceTime(null, resourceWithTracking), SimulationErrorType.NULL_PERSON_ID);
			// if the person is unknown
			assertModelException(() -> environment.getPersonResourceTime(new PersonId(-1), resourceWithTracking), SimulationErrorType.UNKNOWN_PERSON_ID);
			// if the resource id is null
			assertModelException(() -> environment.getPersonResourceTime(new PersonId(1), null), SimulationErrorType.NULL_RESOURCE_ID);
			// if the resource is unknown
			assertModelException(() -> environment.getPersonResourceTime(new PersonId(1), TestResourceId.getUnknownResourceId()), SimulationErrorType.UNKNOWN_RESOURCE_ID);
			// if the resource is not not tracking assignment times
			assertModelException(() -> environment.getPersonResourceTime(new PersonId(1), resourceWithoutTracking), SimulationErrorType.RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPlan(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getPlan", args = {Object.class})
	public void testGetPlan() {
		/*
		 * Add three test plans and show that from the perspective of the second
		 * plan(second in time, third plan added) that the first and second
		 * plans are no longer accessible, but third(still in the future) is.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		TaskPlan taskPlan1 = taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 1, (environment) -> {
			// do nothing
		});

		TaskPlan taskPlan2 = taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 5, (environment) -> {
			// do nothing
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 2, (environment) -> {
			// test plan 1 executes before this plan
			assertFalse(environment.getPlan(taskPlan1.getKey()).isPresent());

			// test plan 2 executes after this plan so it should still be
			// retrievable
			assertEquals(taskPlan2, environment.getPlan(taskPlan2.getKey()).get());
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 3, (environment) -> {
			// if the key is null
			assertModelException(() -> environment.getPlan((Object[]) null), SimulationErrorType.NULL_PLAN_KEY);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPlanTime(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getPlanTime", args = {Object.class})
	public void testGetPlanTime() {
		/*
		 * Retrieve two future plans and assert that the plan times match the
		 * values we planned.
		 */
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

		TaskPlan taskPlan1 = taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 10, (environment) -> {
			// do nothing
		});

		TaskPlan taskPlan2 = taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 20, (environment) -> {
			// do nothing
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 1, (environment) -> {
			assertEquals(taskPlan1.getScheduledTime(), environment.getPlanTime(taskPlan1.getKey()), 0);
			assertEquals(taskPlan2.getScheduledTime(), environment.getPlanTime(taskPlan2.getKey()), 0);
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 4, (environment) -> {
			// if the key is null
			assertModelException(() -> environment.getPlanTime(null), SimulationErrorType.NULL_PLAN_KEY);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPopulationCount()}
	 */
	@Test
	@UnitTestMethod(name = "getPopulationCount", args = {})
	public void testGetPopulationCount() {
		/*
		 * Assert that the initial population is the expected size and that
		 * adding people results in the appropriate changes to the population
		 * count.
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

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			int expectedPopulationCount = scenario.getPeopleIds().size();
			assertEquals(expectedPopulationCount, environment.getPopulationCount());
			for (final TestRegionId testRegionId : TestRegionId.values()) {
				for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
					environment.addPerson(testRegionId, testCompartmentId);
					expectedPopulationCount++;
					assertEquals(expectedPopulationCount, environment.getPopulationCount());
				}
			}
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getPopulationTime()}
	 */
	@Test
	@UnitTestMethod(name = "getPopulationTime", args = {})
	public void testGetPopulationTime() {
		/*
		 * Create three test plans such that we show that the initial people
		 * were added at time zero and that if we add new people at time 2, we
		 * can verify that the new population time is 2 at both time 2 and
		 * later.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(8);
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
			assertEquals(0, environment.getPopulationTime(), 0);
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			environment.addPerson(TestRegionId.getRandomRegionId(environment.getRandomGenerator()), TestCompartmentId.getRandomCompartmentId(environment.getRandomGenerator()));
			assertEquals(2.0, environment.getPopulationTime(), 0);
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			assertEquals(2.0, environment.getPopulationTime(), 0);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getRandomGenerator()}
	 */
	@Test
	@UnitTestMethod(name = "getRandomGenerator", args = {})
	public void testGetRandomGenerator() {
		/*
		 * Assert that we can retrieve the RandomGenerator instance.
		 */
		final long seed = SEED_PROVIDER.getSeedValue(9);
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
			final RandomGenerator rng = environment.getRandomGenerator();
			assertNotNull(rng);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();
	}

	/**
	 * Tests {@link EnvironmentImpl#getRegionIds()}
	 */
	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		/*
		 * Assert that the region identifiers from the environment are the same
		 * as the elements of the local RegionId enumeration.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(12);
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
			final Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);
			assertEquals(expectedRegionIds, environment.getRegionIds());
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

}
