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
import static gcm.simulation.Filter.compartment;
import static gcm.simulation.Filter.region;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.Cat;
import gcm.automated.support.CatImpl;
import gcm.automated.support.EnvironmentSupport;
import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestMaterialsProducerId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestResourceId;
import gcm.output.simstate.NIOProfileItemHandler;
import gcm.replication.Replication;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.ResourceId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.ScenarioId;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Filter;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test Unit for {@link EnvironmentImpl} class. See
 * {@linkplain EnvironmentSupport} for details.
 *
 * @author Shawn Hatch
 *
 */

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_11 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(11));
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		// System.out.println(SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#transferResourceToPerson(ResourceId, PersonId, long)}
	 */
	@Test
	@UnitTestMethod(name = "transferResourceToPerson", args = {ResourceId.class, PersonId.class, long.class})
	public void testTransferResourceToPerson() {

		/*
		 * For each person in every region and for every resource type, transfer
		 * a random amount of that resource to the person and check that the
		 * resulting resource levels for both the person and region are what we
		 * expect.
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
			// we assume that there are people in every region
			for (final TestRegionId testRegionId : TestRegionId.values()) {
				assertTrue(environment.getRegionPopulationCount(testRegionId) > 0);
			}

			// set the max amount we will give any person
			final long maxAmount = 100;

			/*
			 * first, make sure that we have enough resource in each region to
			 * give to all the people the max amount
			 */
			for (final TestRegionId testRegionId : TestRegionId.values()) {
				final long safeAmountForRegion = environment.getRegionPopulationCount(testRegionId) * maxAmount;
				for (final TestResourceId testResourceId : TestResourceId.values()) {
					final long currentLevel = environment.getRegionResourceLevel(testRegionId, testResourceId);
					final long amountToAdd = safeAmountForRegion - currentLevel;
					if (amountToAdd > 0) {
						environment.addResourceToRegion(testResourceId, testRegionId, amountToAdd);
					}
				}
			}

			/*
			 * Next transfer random amounts from the region to each person,
			 * testing the levels as we go.
			 */
			for (final TestRegionId testRegionId : TestRegionId.values()) {
				final List<PersonId> peopleInRegion = environment.getPeopleInRegion(testRegionId);

				for (final TestResourceId testResourceId : TestResourceId.values()) {
					for (final PersonId personId : peopleInRegion) {
						final long currentRegionResourceLevel = environment.getRegionResourceLevel(testRegionId, testResourceId);
						final long currentPersonLevel = environment.getPersonResourceLevel(personId, testResourceId);

						// determine the amount to transfer
						final long amountToTransfer = environment.getRandomGenerator().nextInt((int) maxAmount) + 1;

						// transfer
						environment.transferResourceToPerson(testResourceId, personId, amountToTransfer);

						// test the levels

						final long expectedRegionResourceLevel = currentRegionResourceLevel - amountToTransfer;
						final long actualRegionResourceLevel = environment.getRegionResourceLevel(testRegionId, testResourceId);
						assertEquals(expectedRegionResourceLevel, actualRegionResourceLevel);

						final long expectedPersonResourceLevel = currentPersonLevel + amountToTransfer;
						final long actualPersonResourceLevel = environment.getPersonResourceLevel(personId, testResourceId);
						assertEquals(expectedPersonResourceLevel, actualPersonResourceLevel);
					}
				}
			}
		});

		final int amount = 100;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			final Object key = new Object();
			final Filter filter = compartment(TestCompartmentId.COMPARTMENT_1).and(region(TestRegionId.REGION_1));

			environment.addPopulationIndex(filter, key);
			final PersonId personId = environment.sampleIndex(key).get();
			environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, amount);
			final long regionResourceLevel = environment.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.RESOURCE1);

			// if the person id is null
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, null, amount), SimulationErrorType.NULL_PERSON_ID);
			// if the person id is unknown
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, new PersonId(-1), amount), SimulationErrorType.UNKNOWN_PERSON_ID);
			// if the resource id is null
			assertModelException(() -> environment.transferResourceToPerson(null, personId, amount), SimulationErrorType.NULL_RESOURCE_ID);
			// if the resource id is unknown
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.getUnknownResourceId(), personId, amount), SimulationErrorType.UNKNOWN_RESOURCE_ID);
			// if the amount is negative
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, personId, -1), SimulationErrorType.NEGATIVE_RESOURCE_AMOUNT);
			// if the amount is in excess of the amount the region
			// possesses
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, personId, regionResourceLevel + 1), SimulationErrorType.INSUFFICIENT_RESOURCES_AVAILABLE);

			// if the amount overflows the person's inventory level

			// zero out the person's resource level
			long personLevel = environment.getPersonResourceLevel(personId, TestResourceId.RESOURCE2);
			environment.removeResourceFromPerson(TestResourceId.RESOURCE2, personId, personLevel);

			// max out the the region's resource level
			long regionLevel = environment.getRegionResourceLevel(TestRegionId.REGION_1, TestResourceId.RESOURCE2);
			environment.addResourceToRegion(TestResourceId.RESOURCE2, TestRegionId.REGION_1, Long.MAX_VALUE - regionLevel);

			// transfer all of the resource to the person
			environment.transferResourceToPerson(TestResourceId.RESOURCE2, personId, Long.MAX_VALUE);

			// add a bit more resource to the region
			environment.addResourceToRegion(TestResourceId.RESOURCE2, TestRegionId.REGION_1, 100);

			// transfer it to the person
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE2, personId, 100), SimulationErrorType.RESOURCE_ARITHMETIC_EXCEPTION);

			environment.removePopulationIndex(key);
		});

		/*
		 * We must first ensure that region 1 has sufficient resources for the
		 * next two tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			environment.addResourceToRegion(TestResourceId.RESOURCE1, TestRegionId.REGION_1, amount);
		});

		taskPlanContainer.addTaskPlan(TestRegionId.REGION_2, testTime++, (environment) -> {
			final Object key = new Object();
			final Filter filter = compartment(TestCompartmentId.COMPARTMENT_1).and(region(TestRegionId.REGION_1));

			environment.addPopulationIndex(filter, key);
			final PersonId personId = environment.sampleIndex(key).get();

			// if invoker is not a global component , the person's region
			// or the person's compartment
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, personId, amount), SimulationErrorType.COMPONENT_LACKS_PERMISSION);

			environment.removePopulationIndex(key);
		});

		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_2, testTime++, (environment) -> {
			final Object key = new Object();
			final Filter filter = //
					compartment(TestCompartmentId.COMPARTMENT_1).and(//
							region(TestRegionId.REGION_1));//

			environment.addPopulationIndex(filter, key);
			final PersonId personId = environment.sampleIndex(key).get();

			// if invoker is not a global component , the person's region
			// or the person's compartment
			assertModelException(() -> environment.transferResourceToPerson(TestResourceId.RESOURCE1, personId, amount), SimulationErrorType.COMPONENT_LACKS_PERMISSION);

			environment.removePopulationIndex(key);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link EnvironmentImpl#getProfiledProxy(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getProfiledProxy", args = {Object.class})
	public void testGetProfiledProxy() {

		// The first test will show that the proxy is not the same as the
		// proxied object when the profile report is activated.
		long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		scenarioBuilder.setScenarioId(new ScenarioId(randomGenerator.nextInt(1000) + 1));
		addStandardComponentsAndTypes(scenarioBuilder);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			Cat cat = new CatImpl(23);
			Cat proxyCat = environment.getProfiledProxy(cat);
			assertEquals(cat, proxyCat);
			assertTrue(cat != proxyCat);
		});

		Simulation simulation = new Simulation();
		simulation.addOutputItemHandler(new NIOProfileItemHandler(null));
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		// The second test will show that the proxy is the same as the
		// proxied object when the profile report is not activated.
		seed = SEED_PROVIDER.getSeedValue(4);
		randomGenerator = getRandomGenerator(seed);

		scenarioBuilder = new UnstructuredScenarioBuilder();
		scenarioBuilder.setScenarioId(new ScenarioId(randomGenerator.nextInt(1000) + 1));
		addStandardComponentsAndTypes(scenarioBuilder);
		taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		scenario = scenarioBuilder.build();

		replication = getReplication(randomGenerator);

		testTime = 1;

		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			Cat cat = new CatImpl(23);
			Cat proxyCat = environment.getProfiledProxy(cat);
			assertEquals(cat, proxyCat);			
		});

		simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}
	

	/**
	 * Tests
	 * {@link EnvironmentImpl#getPersonPropertyTime(PersonId, PersonPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "getPersonPropertyTime", args = {PersonId.class, PersonPropertyId.class})
	public void testGetPersonPropertyTime() {
		/*
		 * For each person property first show that the property time is zero.
		 * Next change each property so that the new property times will be the
		 * current time and test that this is so. Finally, move forward in time
		 * and show that the property value times still reflect the time when we
		 * set them and not the current time.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);

		Map<Object, PropertyDefinition> forcedPropertyDefinitions = new LinkedHashMap<>();
		forcedPropertyDefinitions.put(TestPersonPropertyId.PERSON_PROPERTY_2, //
				PropertyDefinition	.builder()//
									.setType(String.class)//
									.setDefaultValue("default value")//
									.build());
		addStandardPropertyDefinitions(scenarioBuilder, forcedPropertyDefinitions, PropertyAssignmentPolicy.TRUE, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * Show that the current values of all person properties were set at
		 * time = 0
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestPersonPropertyId propertyID : TestPersonPropertyId.values()) {
					if (!propertyID.equals(TestPersonPropertyId.PERSON_PROPERTY_2)) {
						final double expectedPropertyTime = 0;
						final double actualPropertyTime = environment.getPersonPropertyTime(personId, propertyID);
						assertEquals(expectedPropertyTime, actualPropertyTime, 0);
					}
				}
			}

		});

		/*
		 * Update all person properties and show that the property time values
		 * are the current time.
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestPersonPropertyId propertyID : TestPersonPropertyId.values()) {
					if (!propertyID.equals(TestPersonPropertyId.PERSON_PROPERTY_2)) {
						final PropertyDefinition propertyDefinition = environment.getPersonPropertyDefinition(propertyID);
						final Object actualPropertyValue = environment.getPersonPropertyValue(personId, propertyID);
						Object updatedPropertyValue = null;
						while ((updatedPropertyValue == null) || updatedPropertyValue.equals(actualPropertyValue)) {
							updatedPropertyValue = generatePropertyValue(propertyDefinition, environment.getRandomGenerator());
						}
						environment.setPersonPropertyValue(personId, propertyID, updatedPropertyValue);
						final double expectedPropertyTime = 2.0;
						final double actualPropertyTime = environment.getPersonPropertyTime(personId, propertyID);
						assertEquals(expectedPropertyTime, actualPropertyTime, 0);
					}

				}
			}

		});

		/*
		 * Show that all person properties have associated time values to when
		 * they were set.
		 */
		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {

			for (final PersonId personId : environment.getPeople()) {
				for (final TestPersonPropertyId propertyID : TestPersonPropertyId.values()) {
					if (!propertyID.equals(TestPersonPropertyId.PERSON_PROPERTY_2)) {
						final double expectedPropertyTime = 2.0;
						final double actualPropertyTime = environment.getPersonPropertyTime(personId, propertyID);
						assertEquals(expectedPropertyTime, actualPropertyTime, 0);
					}
				}
			}

		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {

			// make sure that person 1 actually exists
			assertTrue(environment.personExists(new PersonId(1)));
			// if the person is null
			assertModelException(() -> environment.getPersonPropertyTime(null, TestPersonPropertyId.PERSON_PROPERTY_1), SimulationErrorType.NULL_PERSON_ID);
			// if the person is unknown
			assertModelException(() -> environment.getPersonPropertyTime(new PersonId(-1), TestPersonPropertyId.PERSON_PROPERTY_1), SimulationErrorType.UNKNOWN_PERSON_ID);
			// if the property id is null
			assertModelException(() -> environment.getPersonPropertyTime(new PersonId(1), null), SimulationErrorType.NULL_PERSON_PROPERTY_ID);
			// if the property is unknown
			assertModelException(() -> environment.getPersonPropertyTime(new PersonId(1), TestPersonPropertyId.getUnknownPersonPropertyId()), SimulationErrorType.UNKNOWN_PERSON_PROPERTY_ID);
			// if the person property time is not tracked
			assertModelException(() -> environment.getPersonPropertyTime(new PersonId(1), TestPersonPropertyId.PERSON_PROPERTY_2), SimulationErrorType.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}
}