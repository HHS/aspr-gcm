package gcm.test.automated;

import static gcm.simulation.Filter.allPeople;
import static gcm.simulation.Filter.compartment;
import static gcm.simulation.Filter.groupMember;
import static gcm.simulation.Filter.groupTypesForPerson;
import static gcm.simulation.Filter.groupsForPerson;
import static gcm.simulation.Filter.groupsForPersonAndGroupType;
import static gcm.simulation.Filter.property;
import static gcm.simulation.Filter.region;
import static gcm.simulation.Filter.resource;
import static gcm.test.support.ExceptionAssertion.assertModelException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.replication.Replication;
import gcm.replication.ReplicationImpl;
import gcm.scenario.ComponentId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.ScenarioId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Environment;
import gcm.simulation.Equality;
import gcm.simulation.Filter;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.test.support.SeedProvider;
import gcm.test.support.TaskComponent;
import gcm.test.support.TaskPlan;
import gcm.test.support.TaskPlanContainer;
import gcm.test.support.TestCompartmentId;
import gcm.test.support.TestGlobalComponentId;
import gcm.test.support.TestGlobalPropertyId;
import gcm.test.support.TestGroupTypeId;
import gcm.test.support.TestMaterialId;
import gcm.test.support.TestMaterialsProducerId;
import gcm.test.support.TestPersonPropertyId;
import gcm.test.support.TestRegionId;
import gcm.test.support.TestResourceId;
import gcm.util.annotations.UnitTest;

/**
 * Test unit for {@link Filters}. The AT_Environment covers adding population
 * indexes generally and these tests could be added to that unit, but would not
 * adhere to its test method nomenclature. Rather than make some of those tests
 * extremely long, we break up the tests into numerous sub-tests, limiting the
 * scope to adding population indexes using the filter-style and custom filters
 * only.
 *
 * Tests each of the static filter constructions, sometimes in compositions, and
 * their corresponding constructions via the FilterBuilder. Tests are executed
 * through an instance of the simulation. Rather than invoking the filter for
 * each person, we will use {@link Environment#getIndexedPeople(Object)}.
 * 
 * Seed cases for creating TestPlanExecutors range from 1000 to 1999
 *
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Filter.class)
public class AT_Filters {

	private static void assertAllPlansExecuted(TaskPlanContainer taskPlanContainer) {
		for (ComponentId componentId : taskPlanContainer.getComponentIds()) {
			for (TaskPlan taskPlan : taskPlanContainer.getTaskPlans(componentId)) {
				assertTrue(componentId + ":" + taskPlan.getKey() + " was not executed", taskPlan.planExecuted());
			}
		}
	}

	private static void addStandardTrackingAndScenarioId(ScenarioBuilder scenarioBuilder, RandomGenerator randomGenerator) {
		scenarioBuilder.setScenarioId(new ScenarioId(randomGenerator.nextInt(1000) + 1));
		scenarioBuilder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		scenarioBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		scenarioBuilder.setCompartmentMapOption(MapOption.ARRAY);
		scenarioBuilder.setRegionMapOption(MapOption.ARRAY);		
	}

	private static void addStandardComponentsAndTypes(ScenarioBuilder scenarioBuilder) {
		for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, TaskComponent.class);
		}
		for (final TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			scenarioBuilder.addGlobalComponentId(testGlobalComponentId, TaskComponent.class);
		}
		for (final TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, TaskComponent.class);
		}
		for (final TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, TaskComponent.class);
		}
		for (final TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
			scenarioBuilder.setResourceTimeTracking(testResourceId, testResourceId.trackValueAssignmentTimes());
		}
		for (final TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}
		for (final TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}
	}

	private static void addStandardPeople(ScenarioBuilder scenarioBuilder, int peoplePerRegionAndCompartmentPair) {
		if (peoplePerRegionAndCompartmentPair < 1) {
			throw new RuntimeException("requires at least one person per (region,compartment) pair");
		}
		int personIndex = 0;
		for (final TestRegionId testRegionId : TestRegionId.values()) {
			for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (int i = 0; i < peoplePerRegionAndCompartmentPair; i++) {
					PersonId personId = new PersonId(personIndex++);
					scenarioBuilder.addPerson(personId, testRegionId, testCompartmentId);
				}
			}
		}
	}

	private static TaskPlanContainer addTaskPlanContainer(ScenarioBuilder scenarioBuilder) {
		TaskPlanContainer taskPlanContainer = new TaskPlanContainer();
		scenarioBuilder.defineGlobalProperty(TestGlobalPropertyId.TASK_PLAN_CONTAINER_PROPERTY_ID, //
				PropertyDefinition	.builder()//
									.setType(TaskPlanContainer.class)//
									.setDefaultValue(taskPlanContainer)//
									.setPropertyValueMutability(false)//
									.build());//

		return taskPlanContainer;
	}

	private static Replication getReplication(RandomGenerator randomGenerator) {
		return new ReplicationImpl(new ReplicationId(randomGenerator.nextInt(1000) + 1), randomGenerator.nextLong());
	}

	private static RandomGenerator getRandomGenerator(long seed) {
		return new Well44497b(seed);
	}

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(653445557734517533L);
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
	 * Tests {@link Filter#and(Filter)}
	 */
	@Test
	public void testAnd() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());

		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_2, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// precondition: if the filter is null			
			assertModelException(() -> environment.addPopulationIndex(allPeople().and(null),"bad filter"), SimulationErrorType.NULL_FILTER);
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertTrue(environment.getPopulationCount() > 100);

			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// initialize values for person property 1 and 2 for each person
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 2);
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2, personId.getValue() % 3);
				if (personId.getValue() % 2 == 1 && personId.getValue() % 3 == 2) {
					expectedPeople.add(personId);
				}
			}

			// create the filters
			Filter filter1 = //
					property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.EQUAL, 1).and(//
					property(TestPersonPropertyId.PERSON_PROPERTY_2, Equality.EQUAL, 2));//

			Object key = "key";
			environment.addPopulationIndex(filter1, key);

			// show that the filters meet our expectations
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			expectedPeople = new LinkedHashSet<>();
			// alter the population
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 5);
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2, personId.getValue() % 7);
				if (personId.getValue() % 5 == 1 && personId.getValue() % 7 == 2) {
					expectedPeople.add(personId);
				}
			}
			// show that the filters meet our expectations
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link Filter#or(Filter)}
	 */
	public void testOr() {

		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());

		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_2, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// precondition: if the filter is null			
			Filter badFilter = allPeople().or(null);
			assertModelException(() -> environment.addPopulationIndex(badFilter, "bad filter"), SimulationErrorType.NULL_FILTER);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertTrue(environment.getPopulationCount() > 100);

			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// initialize values for person property 1 and 2 for each person
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 2);
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2, personId.getValue() % 3);
				if (personId.getValue() % 2 == 1 || personId.getValue() % 3 == 2) {
					expectedPeople.add(personId);
				}
			}

			// create the filters
			Filter filter1 =// 
					property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.EQUAL, 1).or(// 
					property(TestPersonPropertyId.PERSON_PROPERTY_2, Equality.EQUAL, 2));//

			Object key = "key";
			environment.addPopulationIndex(filter1, key);

			// show that the filters meet our expectations
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			expectedPeople = new LinkedHashSet<>();
			// alter the population
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 5);
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2, personId.getValue() % 7);
				if (personId.getValue() % 5 == 1 || personId.getValue() % 7 == 2) {
					expectedPeople.add(personId);
				}
			}
			// show that the filters meet our expectations
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link Filter#negate()}
	 */
	@Test
	public void testNegate() {
		final long seed = SEED_PROVIDER.getSeedValue(14);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/*
			 * Show that there are enough people in the simulation to make a
			 * valid test
			 */
			assertTrue(environment.getPopulationCount() > 100);

			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// initialize values for person property 1 and 2 for each person
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 2);
				if (!(personId.getValue() % 2 == 1)) {
					expectedPeople.add(personId);
				}
			}

			// create the filters
			Filter filter1 = property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.EQUAL, 1).negate();

			Object key = "key";
			environment.addPopulationIndex(filter1, key);

			// show that the filters meet our expectations
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			expectedPeople = new LinkedHashSet<>();
			// alter the population
			for (PersonId personId : environment.getPeople()) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, personId.getValue() % 5);
				if (!(personId.getValue() % 5 == 1)) {
					expectedPeople.add(personId);
				}
			}
			// show that the filters meet our expectations
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link Filter#property(PersonPropertyId, Equality, Object)}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testProperty() {

		final long seed = SEED_PROVIDER.getSeedValue(11);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1, //
				PropertyDefinition	.builder()//
									.setType(Integer.class)//
									.setDefaultValue(0)//
									.setMapOption(MapOption.ARRAY)//
									.build());
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_2, //
				PropertyDefinition	.builder()//
									.setType(Boolean.class)//
									.setDefaultValue(false)//
									.setMapOption(MapOption.ARRAY)//
									.build());

		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_3, //
				PropertyDefinition	.builder()//
									.setType(String.class)//
									.setDefaultValue("A")//
									.setMapOption(MapOption.ARRAY)//
									.build());

		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_4, //				
				PropertyDefinition	.builder()//
									.setType(Double.class)//
									.setDefaultValue(0.0)//
									.setMapOption(MapOption.ARRAY)//
									.build());
		
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_5, //				
				PropertyDefinition	.builder()//
									.setType(List.class)//
									.setDefaultValue(new ArrayList<>())//
									.setMapOption(MapOption.ARRAY)//
									.build());
		
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			assertTrue(environment.getPopulationCount() > 0);
			
			/*
			 * precondition: if the property id is null
			 */
			assertModelException(() -> environment.addPopulationIndex(property(null, Equality.EQUAL, 15),"bad filter"), SimulationErrorType.NULL_PERSON_PROPERTY_ID);

			/*
			 * precondition: if the equality is null
			 */

			assertModelException(() -> environment.addPopulationIndex(property(TestPersonPropertyId.PERSON_PROPERTY_1, null, 15),"bad filter"), SimulationErrorType.NULL_EQUALITY_OPERATOR);

			/*
			 * precondition: if the property value is null
			 */
			assertModelException(() -> environment.addPopulationIndex(property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.EQUAL, null),"bad filter"), SimulationErrorType.NULL_PERSON_PROPERTY_VALUE);

			/*
			 * precondition: if the property is not defined
			 */
			// show that the filter will be evaluated against at least one
			// person
			
			Filter undefinedPersonPropertyFilter = property(TestPersonPropertyId.getUnknownPersonPropertyId(), Equality.EQUAL, 15);
			assertModelException(() -> environment.addPopulationIndex(undefinedPersonPropertyFilter, "undefinedPersonPropertyFilter"), SimulationErrorType.UNKNOWN_PERSON_PROPERTY_ID);
			
			/*
			 * precondition : if the property is not compatible with inequality comparisons.
			 */
			
			Filter incomparableFilter = property(TestPersonPropertyId.PERSON_PROPERTY_5, Equality.GREATER_THAN, new ArrayList<>());
			assertModelException(() -> environment.addPopulationIndex(incomparableFilter, "incomparableFilter"), SimulationErrorType.NON_COMPARABLE_PROPERTY);


		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// We establish a pool of values for each of the four chosen
			// person property definitions that will aide in our tests.
			Map<PersonPropertyId, Set<Comparable<?>>> propertyValuePool = new LinkedHashMap<>();
			propertyValuePool.put(TestPersonPropertyId.PERSON_PROPERTY_1, new LinkedHashSet<>());
			propertyValuePool.put(TestPersonPropertyId.PERSON_PROPERTY_2, new LinkedHashSet<>());
			propertyValuePool.put(TestPersonPropertyId.PERSON_PROPERTY_3, new LinkedHashSet<>());
			propertyValuePool.put(TestPersonPropertyId.PERSON_PROPERTY_4, new LinkedHashSet<>());

			Set<Comparable<?>> propertyValueSet = propertyValuePool.get(TestPersonPropertyId.PERSON_PROPERTY_1);
			propertyValueSet.add(-1);
			propertyValueSet.add(0);
			propertyValueSet.add(1);
			propertyValueSet.add(2);
			propertyValueSet.add(3);

			propertyValueSet = propertyValuePool.get(TestPersonPropertyId.PERSON_PROPERTY_2);
			propertyValueSet.add(true);
			propertyValueSet.add(false);

			propertyValueSet = propertyValuePool.get(TestPersonPropertyId.PERSON_PROPERTY_3);
			propertyValueSet.add("A");
			propertyValueSet.add("B");
			propertyValueSet.add("C");
			propertyValueSet.add("D");
			propertyValueSet.add("E");
			propertyValueSet.add("F");

			propertyValueSet = propertyValuePool.get(TestPersonPropertyId.PERSON_PROPERTY_4);
			propertyValueSet.add(-1.0d);
			propertyValueSet.add(0.0d);
			propertyValueSet.add(1.1d);
			propertyValueSet.add(5.2342d);
			propertyValueSet.add(8.124d);
			propertyValueSet.add(100.1234223d);
			RandomGenerator rng = environment.getRandomGenerator();

			/*
			 * Show that the test is valid by asserting there are some people in
			 * the simulation
			 */
			assertTrue(environment.getPopulationCount() > 0);

			/*
			 * Create filters and test them against the population for every
			 * combination of pooled value and Equality value
			 */
			for (PersonPropertyId personPropertyId : propertyValuePool.keySet()) {
				List<Comparable> propertyValueList = new ArrayList<>(propertyValuePool.get(personPropertyId));
				for (Comparable filterPropertyValue : propertyValueList) {
					for (Equality equality : Equality.values()) {

						/*
						 * Randomly assign values to the population
						 */
						for (PersonId personId : environment.getPeople()) {
							int propertyIndex = rng.nextInt(propertyValueList.size());
							Object personPropertyValue = propertyValueList.get(propertyIndex);
							environment.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
						}

						/*
						 * Create the filters
						 */
						Object key = "key";
						
						Filter filter1 = property(personPropertyId, equality, filterPropertyValue);
						environment.addPopulationIndex(filter1, key);

						/*
						 * test that our expectations match the filters
						 */
						Set<PersonId> expectedPeople = new LinkedHashSet<>();
						for (PersonId personId : environment.getPeople()) {
							Comparable personPropertyValue = environment.getPersonPropertyValue(personId, personPropertyId);
							switch (equality) {
							case EQUAL:
								if (personPropertyValue.equals(filterPropertyValue)) {
									expectedPeople.add(personId);
								}
								break;
							case GREATER_THAN:
								if (personPropertyValue.compareTo(filterPropertyValue) > 0) {
									expectedPeople.add(personId);
								}
								break;
							case GREATER_THAN_EQUAL:
								if (personPropertyValue.compareTo(filterPropertyValue) >= 0) {
									expectedPeople.add(personId);
								}
								break;
							case LESS_THAN:
								if (personPropertyValue.compareTo(filterPropertyValue) < 0) {
									expectedPeople.add(personId);
								}

								break;
							case LESS_THAN_EQUAL:
								if (personPropertyValue.compareTo(filterPropertyValue) <= 0) {
									expectedPeople.add(personId);
								}
								break;
							case NOT_EQUAL:
								if (!personPropertyValue.equals(filterPropertyValue)) {
									expectedPeople.add(personId);
								}
								break;
							default:
								throw new RuntimeException("unhandled equality case " + equality);

							}
						}

						
						Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
						assertEquals(expectedPeople, actualPeople);

						/*
						 * Randomly assign values to the population
						 */
						for (PersonId personId : environment.getPeople()) {
							int propertyIndex = randomGenerator.nextInt(propertyValueList.size());
							Object personPropertyValue = propertyValueList.get(propertyIndex);
							environment.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
						}
						/*
						 * test the filters were properly updated
						 */
						expectedPeople = new LinkedHashSet<>();
						for (PersonId personId : environment.getPeople()) {
							Comparable personPropertyValue = environment.getPersonPropertyValue(personId, personPropertyId);
							switch (equality) {
							case EQUAL:
								if (personPropertyValue.equals(filterPropertyValue)) {
									expectedPeople.add(personId);
								}
								break;
							case GREATER_THAN:
								if (personPropertyValue.compareTo(filterPropertyValue) > 0) {
									expectedPeople.add(personId);
								}
								break;
							case GREATER_THAN_EQUAL:
								if (personPropertyValue.compareTo(filterPropertyValue) >= 0) {
									expectedPeople.add(personId);
								}
								break;
							case LESS_THAN:
								if (personPropertyValue.compareTo(filterPropertyValue) < 0) {
									expectedPeople.add(personId);
								}

								break;
							case LESS_THAN_EQUAL:
								if (personPropertyValue.compareTo(filterPropertyValue) <= 0) {
									expectedPeople.add(personId);
								}
								break;
							case NOT_EQUAL:
								if (!personPropertyValue.equals(filterPropertyValue)) {
									expectedPeople.add(personId);
								}
								break;
							default:
								throw new RuntimeException("unhandled equality case " + equality);
							}
						}

						actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));						
						assertEquals(expectedPeople, actualPeople);

						/*
						 * remove the population indexes
						 */
						environment.removePopulationIndex(key);

					}
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
	 * Tests {@link Filter#allPeople()}
	 */
	@Test
	public void testAllPeople() {

		final long seed = SEED_PROVIDER.getSeedValue(6);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// show that the test is valid
			assertTrue(environment.getPopulationCount() > 0);

			/*
			 * Use to two construction methods to create all-true filters
			 */
			final Object key = "key";
			final Filter filter1 = allPeople();
			environment.addPopulationIndex(filter1, key);

			/*
			 * Show that every person is in both indexes
			 */
			final Set<PersonId> expectedPeople = new LinkedHashSet<>(environment.getPeople());

			final Set<PersonId> actualPeople1 = new LinkedHashSet<>(environment.getIndexedPeople(key));

			assertEquals(expectedPeople, actualPeople1);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link Filter#compartment(gcm.scenario.CompartmentId)}
	 */
	@Test
	public void testCompartment() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: if the compartment id is null */
			Filter nullCompartmentFilter  = compartment(null);
			assertModelException(() -> environment.addPopulationIndex(nullCompartmentFilter,"nullCompartmentFilter"), SimulationErrorType.NULL_COMPARTMENT_ID);

			/* precondition: if the compartment is unknown */
			// show that the filter will be evaluated against at least one
			// person
			assertTrue(environment.getPopulationCount() > 0);
			Filter unknownCompartmentFilter = compartment(TestCompartmentId.getUnknownCompartmentId());
			assertModelException(() -> environment.addPopulationIndex(unknownCompartmentFilter, "unknownCompartmentFilter"), SimulationErrorType.UNKNOWN_COMPARTMENT_ID);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			final int populationCount = environment.getPopulationCount();

			// derive the people in compartment 1
			Set<PersonId> expectedPeople = new LinkedHashSet<>(environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_1));

			// show that there are some people in compartment 1
			assertTrue(expectedPeople.size() > 0);

			// show that there are some people not in compartment 1
			assertTrue((populationCount - expectedPeople.size()) > 0);

			// create filter
			final Filter filter1 = compartment(TestCompartmentId.COMPARTMENT_1);
			final Object key = "key";
			environment.addPopulationIndex(filter1, key);

			// show that the filters match our expectations
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			// move half of the people in compartment 1 to compartment 3
			int n = environment.getCompartmentPopulationCount(TestCompartmentId.COMPARTMENT_1) / 2;
			assertTrue(n > 0);
			List<PersonId> people = environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_1);
			for (int i = 0; i < n; i++) {
				final PersonId personId = people.get(i);
				environment.setPersonCompartment(personId, TestCompartmentId.COMPARTMENT_3);
			}

			// move half of the people in compartment 2 to compartment 1
			n = environment.getCompartmentPopulationCount(TestCompartmentId.COMPARTMENT_2) / 2;
			assertTrue(n > 0);
			people = environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_2);
			for (int i = 0; i < n; i++) {
				final PersonId personId = people.get(i);
				environment.setPersonCompartment(personId, TestCompartmentId.COMPARTMENT_1);
			}

			expectedPeople = new LinkedHashSet<>(environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_1));

			// show that the filters match our expectations
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link Filter#groupMember(GroupId)}
	 */
	@Test
	public void testGroupMember() {

		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: if the group id is null */
			assertModelException(() -> environment.addPopulationIndex(groupMember(null),"bad filter"), SimulationErrorType.NULL_GROUP_ID);

			/* precondition: if the group is unknown */
			GroupId groupId = new GroupId(-1);
			assertModelException(() -> environment.addPopulationIndex(groupMember(groupId),"bad filter"), SimulationErrorType.UNKNOWN_GROUP_ID);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			// show that the test is valid
			assertTrue(environment.getPopulationCount() > 0);

			final GroupId groupId = environment.addGroup(TestGroupTypeId.GROUP_TYPE_5);
			/*
			 * Use to two construction methods to create group-membership
			 * filters
			 */
			final Object key = "key";
			final Filter filter1 = groupMember(groupId);
			environment.addPopulationIndex(filter1, key);

			final RandomGenerator rng = environment.getRandomGenerator();
			final Set<PersonId> expectedPeopleInGroup = new LinkedHashSet<>();
			for (final PersonId personId : environment.getPeople()) {
				if (rng.nextBoolean()) {
					expectedPeopleInGroup.add(personId);
					environment.addPersonToGroup(personId, groupId);
				}
			}

			// show that the test is valid
			assertTrue(expectedPeopleInGroup.size() > 0);

			// show that each person is in each index if and only if we
			// selected them to be in the group
			final List<PersonId> indexedPeople1 = environment.getIndexedPeople(key);
			for (final PersonId personId : environment.getPeople()) {
				assertEquals(expectedPeopleInGroup.contains(personId), indexedPeople1.contains(personId));
			}

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	/**
	 * Tests {@link Filter#groupsForPerson(Equality, int)}
	 */
	@Test
	public void testGroupsForPerson() {

		final long seed = SEED_PROVIDER.getSeedValue(9);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			/*
			 * precondition:if equality is null
			 */
			assertModelException(() -> environment.addPopulationIndex(groupsForPerson(null, 0),"bad filter"), SimulationErrorType.NULL_EQUALITY_OPERATOR);
		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// create some groups
			final int maxGroupCount = 5;
			final GroupId[] groupIds = new GroupId[maxGroupCount];
			for (int i = 0; i < maxGroupCount; i++) {
				groupIds[i] = environment.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			}

			// initialize the people to group map
			final Map<PersonId, Set<GroupId>> peopleToGroupsMap = new LinkedHashMap<>();

			final RandomGenerator rng = environment.getRandomGenerator();
			assertTrue(environment.getPopulationCount() > 100);
			for (final PersonId personId : environment.getPeople()) {
				final Set<GroupId> groupsForPerson = new LinkedHashSet<>();
				peopleToGroupsMap.put(personId, groupsForPerson);
				for (int i = 0; i < maxGroupCount; i++) {
					if (rng.nextBoolean()) {
						final GroupId groupId = groupIds[i];
						environment.addPersonToGroup(personId, groupId);
						groupsForPerson.add(groupId);
					}
				}
			}

			final Object key = "key";
			for (int groupCount = -1; groupCount <= maxGroupCount; groupCount++) {
				for (final Equality equality : Equality.values()) {

					// build the filters
					final Filter filter = groupsForPerson(equality, groupCount);

					environment.addPopulationIndex(filter, key);

					// test that the filters produced the expected results
					Set<PersonId> expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final int expectedGroupCount = peopleToGroupsMap.get(personId).size();
						int comparisonValue = Integer.compare(expectedGroupCount, groupCount);
						if (equality.isCompatibleComparisonValue(comparisonValue)) {
							expectedPeople.add(personId);
						}
					}

					Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));

					assertEquals(expectedPeople, actualPeople);

					// update the population's group memberships
					for (final PersonId personId : environment.getPeople()) {
						final Set<GroupId> groupsForPerson = peopleToGroupsMap.get(personId);
						for (int i = 0; i < maxGroupCount; i++) {
							if (randomGenerator.nextBoolean()) {
								final GroupId groupId = groupIds[i];
								if (environment.isGroupMember(personId, groupId)) {
									environment.removePersonFromGroup(personId, groupId);
									groupsForPerson.remove(groupId);
								} else {
									environment.addPersonToGroup(personId, groupId);
									groupsForPerson.add(groupId);
								}
							}
						}
					}

					// test that the filters produced the expected results
					// after the updates
					expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final int expectedGroupCount = peopleToGroupsMap.get(personId).size();
						if (equality.isCompatibleComparisonValue(Integer.compare(expectedGroupCount, groupCount))) {
							expectedPeople.add(personId);
						}
					}

					actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
					assertEquals(expectedPeople, actualPeople);

					// remove the indexed populations
					environment.removePopulationIndex(key);
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
	 * Tests
	 * {@link Filter#groupsForPersonAndGroupType(gcm.scenario.GroupTypeId, Equality, int)}
	 */

	@Test
	public void testGroupsForPersonAndGroupType() {
		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;
		/*
		 * Tests the filter style index construction by distributing resources
		 * to some, but not all people and filtering on the amount given.
		 */

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: >if equality is null */
			assertModelException(() -> environment.addPopulationIndex(groupsForPersonAndGroupType(TestGroupTypeId.GROUP_TYPE_1, null, 0),"bad filter"), SimulationErrorType.NULL_EQUALITY_OPERATOR);

			/* precondition: if groupTypeId is null */
			assertModelException(() -> environment.addPopulationIndex(groupsForPersonAndGroupType(null, Equality.EQUAL, 0),"bad filter"), SimulationErrorType.NULL_GROUP_TYPE_ID);

			/* precondition: if the group type is not defined */
			Filter invalidGroupTypeIdFilter = groupsForPersonAndGroupType(TestGroupTypeId.getUnknownGroupTypeId(), Equality.EQUAL, 0);
			assertModelException(() -> environment.addPopulationIndex(invalidGroupTypeIdFilter, "invalidGroupTypeIdFilter"), SimulationErrorType.UNKNOWN_GROUP_TYPE_ID);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/*
			 * Create groups using two group types
			 */

			final GroupId group1 = environment.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			final GroupId group2 = environment.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			final GroupId group3 = environment.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			final GroupId group4 = environment.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			final GroupId group5 = environment.addGroup(TestGroupTypeId.GROUP_TYPE_2);

			/*
			 * Ensure that there are enough people in the simulation to make a
			 * valid test
			 */
			assertTrue(environment.getPopulationCount() >= 40);

			/*
			 * Put people into various numbers of each group type
			 */
			for (int personIndex = 0; personIndex < 10; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group1);

				environment.addPersonToGroup(personId, group4);
			}

			for (int personIndex = 10; personIndex < 20; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group1);
				environment.addPersonToGroup(personId, group2);

				environment.addPersonToGroup(personId, group4);
				environment.addPersonToGroup(personId, group5);
			}

			for (int personIndex = 20; personIndex < 30; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group1);
				environment.addPersonToGroup(personId, group3);

				environment.addPersonToGroup(personId, group4);
			}

			for (int personIndex = 30; personIndex < 40; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group1);
				environment.addPersonToGroup(personId, group2);
			}

			// Create the filter where people must have exactly two groups
			// of type 1 and fewer than two groups of type 2. Use both
			// styles of filter construction.
			final Filter filter1 =// 					
					groupsForPersonAndGroupType(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 2)//
					.and(groupsForPersonAndGroupType(TestGroupTypeId.GROUP_TYPE_2, Equality.LESS_THAN, 2));//
					
			final String key = "key";
			environment.addPopulationIndex(filter1, key);

			/*
			 * Assert that the people returned for the filters match our
			 * expectations.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (int personIndex = 20; personIndex < 40; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				expectedPeople.add(personId);
			}
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			/*
			 * Make changes to the group associations and show that the filters
			 * reflect those changes
			 */
			for (int personIndex = 0; personIndex < 10; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group3);
			}
			for (int personIndex = 10; personIndex < 20; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.removePersonFromGroup(personId, group5);
			}

			for (int personIndex = 20; personIndex < 30; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.addPersonToGroup(personId, group5);
			}

			for (int personIndex = 30; personIndex < 40; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				environment.removePersonFromGroup(personId, group2);
			}

			expectedPeople = new LinkedHashSet<>();
			for (int personIndex = 0; personIndex < 20; personIndex++) {
				PersonId personId = new PersonId(personIndex);
				expectedPeople.add(personId);
			}
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link Filter#groupTypesForPerson(Equality, int)}
	 */
	@Test
	public void testGroupTypesForPerson() {
		final long seed = SEED_PROVIDER.getSeedValue(10);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: if equality is null */
			assertModelException(() ->environment.addPopulationIndex(groupTypesForPerson(null, 0),"bad filter"), SimulationErrorType.NULL_EQUALITY_OPERATOR);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// create some groups
			final int maxGroupCount = 20;
			final GroupId[] groupIds = new GroupId[maxGroupCount];
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < maxGroupCount; i++) {
				groupIds[i] = environment.addGroup(testGroupTypeId);
				testGroupTypeId = testGroupTypeId.next();
			}

			// initialize the people to group map
			final Map<PersonId, Set<GroupId>> peopleToGroupsMap = new LinkedHashMap<>();

			final RandomGenerator rng = environment.getRandomGenerator();
			assertTrue(environment.getPopulationCount() > 100);
			for (final PersonId personId : environment.getPeople()) {
				final Set<GroupId> groupsForPerson = new LinkedHashSet<>();
				peopleToGroupsMap.put(personId, groupsForPerson);
				for (int i = 0; i < maxGroupCount; i++) {
					if (rng.nextBoolean()) {
						final GroupId groupId = groupIds[i];
						if (environment.isGroupMember(personId, groupId)) {
							environment.removePersonFromGroup(personId, groupId);
							groupsForPerson.remove(groupId);

						} else {
							environment.addPersonToGroup(personId, groupId);
							groupsForPerson.add(groupId);
						}
					}
				}
			}

			final Object key = "key";

			for (int groupTypeCount = -1; groupTypeCount <= maxGroupCount; groupTypeCount++) {
				for (final Equality equality : Equality.values()) {

					// build the filters
					final Filter filter1 = groupTypesForPerson(equality, groupTypeCount);
					environment.addPopulationIndex(filter1, key);

					// test that the filters produced the expected results
					Set<PersonId> expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final Set<GroupTypeId> groupTypesForPerson = new LinkedHashSet<>();
						for (final GroupId groupId : peopleToGroupsMap.get(personId)) {
							final GroupTypeId groupType = environment.getGroupType(groupId);
							groupTypesForPerson.add(groupType);
						}

						final int expectedGroupTypeCount = groupTypesForPerson.size();
						if (equality.isCompatibleComparisonValue(Integer.compare(expectedGroupTypeCount, groupTypeCount))) {
							expectedPeople.add(personId);
						}
					}

					Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
					assertEquals(expectedPeople, actualPeople);

					// update the population's group memberships
					for (final PersonId personId : environment.getPeople()) {
						final Set<GroupId> groupsForPerson = peopleToGroupsMap.get(personId);
						for (int i = 0; i < maxGroupCount; i++) {
							if (randomGenerator.nextBoolean()) {
								final GroupId groupId = groupIds[i];
								if (environment.isGroupMember(personId, groupId)) {

									environment.removePersonFromGroup(personId, groupId);
									groupsForPerson.remove(groupId);
								} else {

									environment.addPersonToGroup(personId, groupId);
									groupsForPerson.add(groupId);
								}
							}
						}
					}

					// test that the filters produced the expected results
					// after the updates
					expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final Set<GroupTypeId> groupTypesForPerson = new LinkedHashSet<>();
						for (final GroupId groupId : peopleToGroupsMap.get(personId)) {
							final GroupTypeId groupType = environment.getGroupType(groupId);
							groupTypesForPerson.add(groupType);
						}

						final int expectedGroupTypeCount = groupTypesForPerson.size();
						if (equality.isCompatibleComparisonValue(Integer.compare(expectedGroupTypeCount, groupTypeCount))) {
							expectedPeople.add(personId);
						}
					}

					actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
					assertEquals(expectedPeople, actualPeople);

					// remove the indexed populations
					environment.removePopulationIndex(key);
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
	 * Tests {@link Filter#region(RegionId)}
	 */
	@Test
	public void testRegion() {
		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: if the region id is null */
			Set<RegionId> regionIds = new LinkedHashSet<>();
			regionIds.add(null);
			assertModelException(() -> environment.addPopulationIndex(region(regionIds),"bad filter"), SimulationErrorType.NULL_REGION_ID);

			/* precondition: if the region id is unknown */

			// show that the filter will be evaluated against at least one
			// person
			assertTrue(environment.getPopulationCount() > 0);
			Filter unknownRegionFilter = region(TestRegionId.getUnknownRegionId());
			assertModelException(() -> environment.addPopulationIndex(unknownRegionFilter, "unknownRegionFilter"), SimulationErrorType.UNKNOWN_REGION_ID);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			final int populationCount = environment.getPopulationCount();

			// derive the people in region 1
			Set<PersonId> expectedPeople = new LinkedHashSet<>(environment.getPeopleInRegion(TestRegionId.REGION_1));

			// show that there are some people in region 1
			assertTrue(expectedPeople.size() > 0);

			// show that there are some people not in region 1
			assertTrue((populationCount - expectedPeople.size()) > 0);

			// create filter
			final Filter filter1 = region(TestRegionId.REGION_1);
			final Object key = "key";
			environment.addPopulationIndex(filter1, key);

			// show that the filters match our expectations
			Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

			// move half of the people in region 1 to region 3
			int n = environment.getRegionPopulationCount(TestRegionId.REGION_1) / 2;
			assertTrue(n > 0);
			List<PersonId> people = environment.getPeopleInRegion(TestRegionId.REGION_1);
			for (int i = 0; i < n; i++) {
				final PersonId personId = people.get(i);
				environment.setPersonRegion(personId, TestRegionId.REGION_3);
			}

			// move half of the people in region 2 to region 1
			n = environment.getRegionPopulationCount(TestRegionId.REGION_2) / 2;
			assertTrue(n > 0);
			people = environment.getPeopleInRegion(TestRegionId.REGION_2);
			for (int i = 0; i < n; i++) {
				final PersonId personId = people.get(i);
				environment.setPersonRegion(personId, TestRegionId.REGION_1);
			}

			expectedPeople = new LinkedHashSet<>(environment.getPeopleInRegion(TestRegionId.REGION_1));

			// show that the filters match our expectations
			actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
			assertEquals(expectedPeople, actualPeople);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link Filter#resource(ResourceId, Equality, long)}
	 */
	@Test
	public void testResource() {

		final long seed = SEED_PROVIDER.getSeedValue(8);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			/* precondition: if the resource id is null */
			assertModelException(() -> environment.addPopulationIndex(resource(null, Equality.EQUAL, 0),"bad filter"), SimulationErrorType.NULL_RESOURCE_ID);

			/* precondition: if the equality is null */
			assertModelException(() -> environment.addPopulationIndex(resource(TestResourceId.RESOURCE1, null, 0),"bad filter"), SimulationErrorType.NULL_EQUALITY_OPERATOR);

			/* precondition: if the resource is not defined */
			Filter undefinedResourceFilter = resource(TestResourceId.getUnknownResourceId(), Equality.EQUAL, 15L);
			assertModelException(() -> environment.addPopulationIndex(undefinedResourceFilter, "undefinedResourceFilter"), SimulationErrorType.UNKNOWN_RESOURCE_ID);

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// show that the test is valid
			assertTrue(environment.getPopulationCount() >= 300);

			final long[] thresholdValues = new long[] { -1, 0, 1, 10 };
			final ResourceId resourceId = TestResourceId.RESOURCE3;
			final RandomGenerator rng = environment.getRandomGenerator();
			Filter filter;

			Integer key = 0;

			for (final Equality equality : Equality.values()) {
				for (final long thresholdValue : thresholdValues) {

					filter = resource(resourceId, equality, thresholdValue);

					// initialize random amount for each person
					for (final PersonId personId : environment.getPeople()) {
						final long newLevel = rng.nextInt(13);
						final long currentLevel = environment.getPersonResourceLevel(personId, resourceId);
						final RegionId regionId = environment.getPersonRegion(personId);
						if (newLevel > currentLevel) {
							final long amount = newLevel - currentLevel;
							environment.addResourceToRegion(resourceId, regionId, amount);
							environment.transferResourceToPerson(resourceId, personId, amount);
						} else if (newLevel < currentLevel) {
							final long amount = currentLevel - newLevel;
							environment.transferResourceFromPerson(resourceId, personId, amount);
							environment.removeResourceFromRegion(resourceId, regionId, amount);
						}
					}

					// add the filter via a population index and refresh
					// its key
					key++;
					environment.addPopulationIndex(filter, key);

					// show that the people match the filter as expected
					Set<PersonId> expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final long personResourceLevel = environment.getPersonResourceLevel(personId, resourceId);
						final boolean expectedMember = equality.isCompatibleComparisonValue(Long.compare(personResourceLevel, thresholdValue));
						if (expectedMember) {
							expectedPeople.add(personId);
						}
					}
					Set<PersonId> actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
					assertEquals(expectedPeople, actualPeople);
					// change the amounts for each person
					for (final PersonId personId : environment.getPeople()) {
						final long newLevel = rng.nextInt(13);
						final long currentLevel = environment.getPersonResourceLevel(personId, resourceId);
						final RegionId regionId = environment.getPersonRegion(personId);

						if (newLevel > currentLevel) {
							final long amount = newLevel - currentLevel;
							environment.addResourceToRegion(resourceId, regionId, amount);
							environment.transferResourceToPerson(resourceId, personId, amount);
						} else if (newLevel < currentLevel) {
							final long amount = currentLevel - newLevel;
							environment.transferResourceFromPerson(resourceId, personId, amount);
							environment.removeResourceFromRegion(resourceId, regionId, amount);
						}
					}
					// show that the filter is maintained
					expectedPeople = new LinkedHashSet<>();
					for (final PersonId personId : environment.getPeople()) {
						final long personResourceLevel = environment.getPersonResourceLevel(personId, resourceId);
						final boolean expectedMember = equality.isCompatibleComparisonValue(Long.compare(personResourceLevel, thresholdValue));
						if (expectedMember) {
							expectedPeople.add(personId);
						}
					}
					actualPeople = new LinkedHashSet<>(environment.getIndexedPeople(key));
					assertEquals(expectedPeople, actualPeople);
					// remove the population index
					environment.removePopulationIndex(key);

				}
			}

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

}
