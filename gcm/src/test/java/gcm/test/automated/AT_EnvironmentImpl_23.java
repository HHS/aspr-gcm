package gcm.test.automated;

import static gcm.simulation.Filter.compartment;
import static gcm.simulation.Filter.property;
import static gcm.simulation.Filter.region;
import static gcm.test.support.EnvironmentSupport.addObservationContainer;
import static gcm.test.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.test.support.EnvironmentSupport.addStandardPeople;
import static gcm.test.support.EnvironmentSupport.addStandardPropertyDefinitions;
import static gcm.test.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.test.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.test.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.test.support.EnvironmentSupport.generatePropertyValue;
import static gcm.test.support.EnvironmentSupport.getRandomGenerator;
import static gcm.test.support.EnvironmentSupport.getReplication;
import static gcm.test.support.ExceptionAssertion.assertModelException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.replication.Replication;
import gcm.scenario.BatchId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.PersonId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.StageId;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Equality;
import gcm.simulation.Filter;
import gcm.simulation.ObservationType;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.test.support.EnvironmentSupport;
import gcm.test.support.ObservationContainer;
import gcm.test.support.SeedProvider;
import gcm.test.support.TaskPlan;
import gcm.test.support.TaskPlanContainer;
import gcm.test.support.TestCompartmentId;
import gcm.test.support.TestGlobalComponentId;
import gcm.test.support.TestMaterialId;
import gcm.test.support.TestMaterialsProducerId;
import gcm.test.support.TestPersonPropertyId;
import gcm.test.support.TestRegionId;
import gcm.test.support.TestRegionPropertyId;
import gcm.test.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.test.support.TaskPlan.Task;
import gcm.util.MultiKey;
import gcm.util.annotations.UnitTest;

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_23 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(23));
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
	 * Tests {@link Environment#getPlanKeys()}
	 */
	@Test
	public void testGetPlanKeys() {

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

		Task emptyTask = new Task() {

			@Override
			public void execute(Environment environment) {

			}
		};
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			double scheduledTime = 10;
			List<String> expectedPlanKeys = new ArrayList<>();
			expectedPlanKeys.add("A");
			expectedPlanKeys.add("B");
			expectedPlanKeys.add("C");

			for (String planKey : expectedPlanKeys) {
				TaskPlan taskPlan = new TaskPlan(scheduledTime, planKey, emptyTask);
				environment.addPlan(taskPlan, scheduledTime, planKey);
			}

			List<Object> actualPlanKeys = environment.getPlanKeys();

			assertEquals(expectedPlanKeys, actualPlanKeys);

		});

		// no precondition tests

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link Environment#observePopulationIndexChange(boolean, Object)}
	 */
	@Test
	public void testObservePopulationIndexChange() {
		/*
		 * We test for the post conditions by first having the components
		 * execute a series time-separated plans and then examining the
		 * observations recorded by each component. Precondition tests are added
		 * at the end.
		 *
		 * Actions
		 *
		 * Time 1 : Global Component 1 creates a population index
		 * 
		 * Time 2 : Global Component 2 starts observing the index
		 *
		 * Time 3 : Global Component 3 makes changes to the index
		 * 
		 * Time 4 : Global Component 2 makes changes to the index
		 * 
		 * Time 5 : Global Component 2 stop observation of the index
		 * 
		 * Time 6 : Global Component 1 start observation of the index
		 * 
		 * Time 7 : Global Component 3 makes changes to the index
		 *
		 * Expected observations
		 *
		 * Compartment 2: first property change --> demonstrates Post Conditions
		 * 1 and 3
		 *
		 * Compartment 2: no observations demonstrates Post Condition 4
		 *
		 * Compartment 1: second property change --> demonstrates Post
		 * Conditions 1 and 2
		 *
		 * All others: no observations --> demonstrates Post Condition 2
		 */
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);

		Map<Object, PropertyDefinition> forcedPropertyDefinitions = new LinkedHashMap<>();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(0)//
																	.build();

		forcedPropertyDefinitions.put(TestPersonPropertyId.PERSON_PROPERTY_1, propertyDefinition);

		addStandardPropertyDefinitions(scenarioBuilder, forcedPropertyDefinitions, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		ObservationContainer observationContainer = addObservationContainer(scenarioBuilder);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		Object key = "key";

		final Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// Time 1 : Global Component 1 creates a population index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Filter c3gt5 = compartment(TestCompartmentId.COMPARTMENT_3).and(property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.GREATER_THAN, 5));

			Filter c2lt3 = compartment(TestCompartmentId.COMPARTMENT_2).and(property(TestPersonPropertyId.PERSON_PROPERTY_1, Equality.LESS_THAN, 3));

			Filter compartmentsAndProps = c3gt5.or(c2lt3);

			Filter filter = region(TestRegionId.REGION_1).and(compartmentsAndProps);

			environment.addPopulationIndex(filter, key);
		});

		// Time 2 : Global Component 2 starts observing the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.observePopulationIndexChange(true, key);
		});

		// Time 3 : Global Component 3 makes changes to the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_3, testTime++, (environment) -> {
			// show that there are a few people in compartment 2
			assertTrue(environment.getCompartmentPopulationCount(TestCompartmentId.COMPARTMENT_2) > 10);

			// determine the people in region1, compartment2
			List<PersonId> peopleInCompartment = environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_2);
			List<PersonId> peopleInCompartmentAndRegion = new ArrayList<>();
			for (PersonId personId : peopleInCompartment) {
				RegionId personRegion = environment.getPersonRegion(personId);
				if (personRegion.equals(TestRegionId.REGION_1)) {
					peopleInCompartmentAndRegion.add(personId);

				}
			}

			/*
			 * Select half of them at random to have a person property of 5,
			 * recording the expectations.
			 * 
			 */
			Collections.shuffle(peopleInCompartmentAndRegion, new Random(environment.getRandomGenerator().nextLong()));
			for (int i = 0; i < peopleInCompartmentAndRegion.size() / 2; i++) {
				PersonId personId = peopleInCompartmentAndRegion.get(i);
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, 5);
				expectedObservations.add(new MultiKey(environment.getTime(), TestGlobalComponentId.GLOBAL_COMPONENT_2, ObservationType.POPULATION_INDEX_PERSON_REMOVAL, key, personId));
			}

		});

		// Time 4 : Global Component 2 makes changes to the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			List<PersonId> peopleInCompartment = environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_3);
			List<PersonId> peopleInCompartmentAndRegion = new ArrayList<>();
			// determine the people in region1, compartment3
			for (PersonId personId : peopleInCompartment) {
				RegionId personRegion = environment.getPersonRegion(personId);
				if (personRegion.equals(TestRegionId.REGION_1)) {
					peopleInCompartmentAndRegion.add(personId);
				}
			}
			// find a person who has property1 < 5
			PersonId selectedPerson = null;
			for (PersonId personId : peopleInCompartmentAndRegion) {
				Integer personPropertyValue = environment.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1);
				if (personPropertyValue < 5) {
					selectedPerson = personId;
					break;
				}
			}
			assertNotNull(selectedPerson);

			// move that person to compartment 1
			environment.setPersonCompartment(selectedPerson, TestCompartmentId.COMPARTMENT_1);
			// there should be no observations

		});

		// Time 5 : Global Component 2 stop observation of the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.observePopulationIndexChange(false, key);
		});

		// Time 6 : Global Component 1 start observation of the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			environment.observePopulationIndexChange(true, key);
		});

		// Time 7 : Global Component 3 makes changes to the index
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_3, testTime++, (environment) -> {

			// for every person in compartment 3, region 1, that has a value
			// less than 5, set their value to 10
			List<PersonId> peopleInCompartment = environment.getPeopleInCompartment(TestCompartmentId.COMPARTMENT_3);
			List<PersonId> peopleInCompartmentAndRegion = new ArrayList<>();
			// determine the people in region1, compartment3
			for (PersonId personId : peopleInCompartment) {
				RegionId personRegion = environment.getPersonRegion(personId);
				if (personRegion.equals(TestRegionId.REGION_1)) {
					peopleInCompartmentAndRegion.add(personId);
				}
			}
			List<PersonId> selectedPeople = new ArrayList<>();
			for (PersonId personId : peopleInCompartmentAndRegion) {
				Integer personPropertyValue = environment.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1);
				if (personPropertyValue < 5) {
					selectedPeople.add(personId);
				}
			}
			assertTrue(selectedPeople.size() > 0);
			for (PersonId personId : selectedPeople) {
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, 10);
				expectedObservations.add(new MultiKey(environment.getTime(), TestGlobalComponentId.GLOBAL_COMPONENT_1, ObservationType.POPULATION_INDEX_PERSON_ADDITION, key, personId));
			}

		});

		// precondition tests
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			Object badKey = "bad key";
			// if the property id is null
			assertModelException(() -> environment.observePopulationIndexChange(true, badKey), SimulationErrorType.UNKNOWN_POPULATION_INDEX_KEY);
			// if the property is unknown
			assertModelException(() -> environment.observePopulationIndexChange(true, null), SimulationErrorType.NULL_POPULATION_INDEX_KEY);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		final Set<MultiKey> actualObservations = observationContainer.getObservations();

		assertEquals(expectedObservations, actualObservations);
	}

	/**
	 * Tests
	 * {@link Environment#observeStageTransferBySourceMaterialsProducerId(boolean, gcm.scenario.MaterialsProducerId)}
	 *
	 */
	@Test
	public void testObserveStageTransferBySourceMaterialsProducerId() {
		/*
		 * We test for the post conditions by first having the components
		 * execute a series time-separated plans and then examining the
		 * observations recorded by each component. Precondition tests are added
		 * at the end.
		 *
		 * Actions
		 *
		 *
		 * Time 1 : Materials Producer 1 creates Stage S0 and S1 and offers them
		 * for transfer
		 *
		 * Time 2 : Compartment 1 starts observation of stage transfers from
		 * Materials Producer 1
		 *
		 * Time 3 : Compartment 2 starts observation of stage transfers from
		 * Materials Producer 1
		 *
		 * Time 4 : Global Component 2 starts observation of stage transfers
		 * from Materials Producer 1
		 *
		 * Time 5 : Global Component 2 transfers Stage S0 to Materials Producer
		 * 2
		 *
		 * Time 6 : Compartment 2 stops observation of stage transfers from
		 * Materials Producer 1
		 *
		 * Time 7 : Global Component 1 transfers Stage S1 to Materials Producer
		 * 3
		 *
		 * Expected observations
		 *
		 * Materials Producer 1:
		 *
		 * Compartment 1: first and second transfer demonstrates post condition
		 * 1
		 *
		 * Compartment 2: first transfer: demonstrates post conditions 1 and 3
		 *
		 * Global Component 2: second transfer: demonstrates post conditions 1
		 * and 4
		 *
		 * All others: no observations --> demonstrates Post Condition 2
		 *
		 */
		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		ObservationContainer observationContainer = addObservationContainer(scenarioBuilder);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * Time 1 : Materials Producer 1 creates Stage S0 and S1 and offers them
		 * for transfer
		 *
		 */

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			final BatchId batchId1 = environment.createBatch(TestMaterialId.MATERIAL_5, 200);
			final BatchId batchId2 = environment.createBatch(TestMaterialId.MATERIAL_4, 100);
			final StageId stageId0 = environment.createStage();
			assertEquals(stageId0, new StageId(0));
			environment.moveBatchToStage(batchId1, stageId0);
			environment.moveBatchToStage(batchId2, stageId0);
			environment.setStageOffer(stageId0, true);

			final BatchId batchId3 = environment.createBatch(TestMaterialId.MATERIAL_1, 800);
			final BatchId batchId4 = environment.createBatch(TestMaterialId.MATERIAL_2, 500);
			final StageId stageId1 = environment.createStage();
			assertEquals(stageId1, new StageId(1));
			environment.moveBatchToStage(batchId3, stageId1);
			environment.moveBatchToStage(batchId4, stageId1);
			environment.setStageOffer(stageId1, true);

		});

		/*
		 * Time 2 : Compartment 1 starts observation of stage transfers from
		 * Materials Producer 1
		 */

		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			environment.observeStageTransferBySourceMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		});

		/*
		 * Time 3 : Compartment 2 starts observation of stage transfers from
		 * Materials Producer 1
		 *
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_2, testTime++, (environment) -> {
			environment.observeStageTransferBySourceMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		});

		/*
		 * Time 4 : Global Component 2 starts observation of stage transfers
		 * from Materials Producer 1
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.observeStageTransferBySourceMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		});

		/*
		 * Time 5 : Global Component 2 transfers Stage S0 to Materials Producer
		 * 2
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.transferOfferedStageToMaterialsProducer(new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		});

		/*
		 * Time 6 : Compartment 2 stops observation of stage transfers from
		 * Materials Producer 1
		 *
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_2, testTime++, (environment) -> {
			environment.observeStageTransferBySourceMaterialsProducerId(false, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		});

		/*
		 * Time 7 : Global Component 1 transfers Stage S1 to Materials Producer
		 * 3
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			environment.transferOfferedStageToMaterialsProducer(new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			MaterialsProducerId unknownMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
			// if the materials producer id is null
			assertModelException(() -> environment.observeStageTransferBySourceMaterialsProducerId(true, null), SimulationErrorType.NULL_MATERIALS_PRODUCER_ID);
			// if the materials producer id is unknown
			assertModelException(() -> environment.observeStageTransferBySourceMaterialsProducerId(true, unknownMaterialsProducerId), SimulationErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		final Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		/*
		 * Expected observations
		 */

		/*
		 * Compartment 1: first transfer: second transfer demonstrates post
		 * condition 1
		 *
		 */
		expectedObservations.add(new MultiKey(5.0, TestCompartmentId.COMPARTMENT_1, ObservationType.STAGE_TRANSFER, new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_2));
		expectedObservations.add(new MultiKey(7.0, TestCompartmentId.COMPARTMENT_1, ObservationType.STAGE_TRANSFER, new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_3));
		/*
		 * Compartment 2: first transfer: demonstrates post conditions 1 and 3
		 *
		 */
		expectedObservations.add(new MultiKey(5.0, TestCompartmentId.COMPARTMENT_2, ObservationType.STAGE_TRANSFER, new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_2));

		/*
		 * Global Component 2:second transfer: demonstrates post conditions 1
		 * and 4
		 *
		 */
		expectedObservations.add(new MultiKey(7.0, TestGlobalComponentId.GLOBAL_COMPONENT_2, ObservationType.STAGE_TRANSFER, new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_3));

		final Set<MultiKey> actualObservations = observationContainer.getObservations();

		assertEquals(expectedObservations, actualObservations);

	}

	/**
	 * Tests
	 * {@link Environment#observeStageTransferByDestinationMaterialsProducerId(boolean, MaterialsProducerId)}
	 *
	 */
	@Test
	public void testObserveStageTransferByDestinationMaterialsProducerId() {
		/*
		 * We test for the post conditions by first having the components
		 * execute a series time-separated plans and then examining the
		 * observations recorded by each component. Precondition tests are added
		 * at the end.
		 *
		 * Actions
		 *
		 *
		 * Time 1 : Materials Producer 1 creates Stage S0 and S1 and offers them
		 * for transfer
		 *
		 * Time 2 : Compartment 1 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 *
		 * Time 3 : Compartment 2 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 *
		 * Time 4 : Global Component 2 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 *
		 * Time 5 : Global Component 2 transfers Stage S0 to Materials Producer
		 * 2
		 *
		 * Time 6 : Compartment 2 stops observation of stage transfers to
		 * Materials Producer 3
		 *
		 * Time 7 : Global Component 1 transfers Stage S1 to Materials Producer
		 * 3
		 *
		 * Expected observations
		 *
		 * Materials Producer 1:
		 *
		 * Compartment 1: first and second transfer demonstrates post condition
		 * 1
		 *
		 * Compartment 2: first transfer: demonstrates post conditions 1 and 3
		 *
		 * Global Component 2: second transfer: demonstrates post conditions 1
		 * and 4
		 *
		 * All others: no observations --> demonstrates Post Condition 2
		 *
		 */
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		ObservationContainer observationContainer = addObservationContainer(scenarioBuilder);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * Time 1 : Materials Producer 1 creates Stage S0 and S1 and offers them
		 * for transfer
		 *
		 */

		taskPlanContainer.addTaskPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, testTime++, (environment) -> {
			final BatchId batchId1 = environment.createBatch(TestMaterialId.MATERIAL_5, 200);
			final BatchId batchId2 = environment.createBatch(TestMaterialId.MATERIAL_4, 100);
			final StageId stageId0 = environment.createStage();
			assertEquals(stageId0, new StageId(0));
			environment.moveBatchToStage(batchId1, stageId0);
			environment.moveBatchToStage(batchId2, stageId0);
			environment.setStageOffer(stageId0, true);

			final BatchId batchId3 = environment.createBatch(TestMaterialId.MATERIAL_1, 800);
			final BatchId batchId4 = environment.createBatch(TestMaterialId.MATERIAL_2, 500);
			final StageId stageId1 = environment.createStage();
			assertEquals(stageId1, new StageId(1));
			environment.moveBatchToStage(batchId3, stageId1);
			environment.moveBatchToStage(batchId4, stageId1);
			environment.setStageOffer(stageId1, true);

		});

		/*
		 * Time 2 : Compartment 1 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 */

		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Time 3 : Compartment 2 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 *
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_2, testTime++, (environment) -> {
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Time 4 : Global Component 2 starts observation of stage transfers to
		 * Materials Producers 2 and 3
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
			environment.observeStageTransferByDestinationMaterialsProducerId(true, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Time 5 : Global Component 2 transfers Stage S0 to Materials Producer
		 * 2
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_2, testTime++, (environment) -> {
			environment.transferOfferedStageToMaterialsProducer(new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		});

		/*
		 * Time 6 : Compartment 2 stops observation of stage transfers to
		 * Materials Producer 3
		 *
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_2, testTime++, (environment) -> {
			environment.observeStageTransferByDestinationMaterialsProducerId(false, TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Time 7 : Global Component 1 transfers Stage S1 to Materials Producer
		 * 3
		 *
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			environment.transferOfferedStageToMaterialsProducer(new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_3);
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			MaterialsProducerId unknownMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
			// if the materials producer id is null
			assertModelException(() -> environment.observeStageTransferByDestinationMaterialsProducerId(true, null), SimulationErrorType.NULL_MATERIALS_PRODUCER_ID);
			// if the materials producer id is unknown
			assertModelException(() -> environment.observeStageTransferByDestinationMaterialsProducerId(true, unknownMaterialsProducerId), SimulationErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		final Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		/*
		 * Expected observations
		 */

		/*
		 * Compartment 1: first transfer: second transfer demonstrates post
		 * condition 1
		 *
		 */
		expectedObservations.add(new MultiKey(5.0, TestCompartmentId.COMPARTMENT_1, ObservationType.STAGE_TRANSFER, new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_2));
		expectedObservations.add(new MultiKey(7.0, TestCompartmentId.COMPARTMENT_1, ObservationType.STAGE_TRANSFER, new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_3));
		/*
		 * Compartment 2: first transfer: demonstrates post conditions 1 and 3
		 *
		 */
		expectedObservations.add(new MultiKey(5.0, TestCompartmentId.COMPARTMENT_2, ObservationType.STAGE_TRANSFER, new StageId(0), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_2));

		/*
		 * Global Component 2:second transfer: demonstrates post conditions 1
		 * and 4
		 *
		 */
		expectedObservations.add(new MultiKey(7.0, TestGlobalComponentId.GLOBAL_COMPONENT_2, ObservationType.STAGE_TRANSFER, new StageId(1), TestMaterialsProducerId.MATERIALS_PRODUCER_1,
				TestMaterialsProducerId.MATERIALS_PRODUCER_3));

		final Set<MultiKey> actualObservations = observationContainer.getObservations();

		assertEquals(expectedObservations, actualObservations);

	}

	/**
	 * Tests
	 * {@link Environment#observeGlobalRegionPropertyChange(boolean, gcm.scenario.RegionPropertyId)}
	 *
	 */
	@Test
	public void testObserveGlobalRegionPropertyChange() {
		/*
		 * We test for the post conditions by first having the components
		 * execute a series time-separated plans and then examining the
		 * observations recorded by each component. Precondition tests are added
		 * at the end.
		 *
		 * Actions
		 *
		 * Time 1 : Region 1 starts observation 
		 *
		 * Time 2 : Region 2 starts observation 
		 * 
		 * Time 3 : Region 2 changes its Property P value
		 *
		 * Time 4 : Region 1 stops observation 
		 *
		 * Time 5 : Compartment 1 starts observation 
		 *
		 * Time 6 : Region 2 changes its Property P value
		 *
		 * Expected observations
		 *
		 * Region 1: first property change --> demonstrates Post Conditions 1
		 * and 3
		 *
		 * Region 2: no observations demonstrates Post Condition 4
		 *
		 * Compartment 1: second property change --> demonstrates Post
		 * Conditions 1 and 2
		 *
		 * All others: no observations --> demonstrates Post Condition 2
		 */

		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.TRUE, randomGenerator);

		ObservationContainer observationContainer = addObservationContainer(scenarioBuilder);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		/*
		 * Establish a non-boolean property so that we can have two distinct
		 * values different from the current value.
		 */
		TestRegionPropertyId candidatePropertyId = null;
		for (final TestRegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
			final PropertyDefinition propertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
			if (propertyDefinition.getType() != Boolean.class) {
				candidatePropertyId = regionPropertyId;
				break;
			}

		}
		assertNotNull(candidatePropertyId);
		final TestRegionPropertyId selectedRegionPropertyId = candidatePropertyId;
		final PropertyDefinition propertyDefinition = scenario.getRegionPropertyDefinition(selectedRegionPropertyId);
		assertTrue(propertyDefinition.getDefaultValue().isPresent());
		final Object currentPropertyValue = propertyDefinition.getDefaultValue().get();
		Object propertyValue1 = null;
		while (currentPropertyValue.equals(propertyValue1) || (propertyValue1 == null)) {
			propertyValue1 = generatePropertyValue(propertyDefinition, randomGenerator);
		}
		assertNotNull(propertyValue1);
		assertFalse(propertyValue1.equals(currentPropertyValue));
		Object propertyValue2 = null;
		while (currentPropertyValue.equals(propertyValue2) || propertyValue1.equals(propertyValue2) || (propertyValue2 == null)) {
			propertyValue2 = generatePropertyValue(propertyDefinition, randomGenerator);
		}
		assertNotNull(propertyValue2);
		assertFalse(propertyValue2.equals(currentPropertyValue));
		assertFalse(propertyValue2.equals(propertyValue1));

		final Object firstPropertyValue = propertyValue1;
		final Object secondPropertyValue = propertyValue2;

		/*
		 * Time 1 : Region 1 starts observation of all Regions Property P
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_1, testTime++, (environment) -> {
			environment.observeGlobalRegionPropertyChange(true, selectedRegionPropertyId);
		});

		/*
		 * Time 2 : Region 2 starts observation of Region 2 Property P
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_2, testTime++, (environment) -> {
			environment.observeGlobalRegionPropertyChange(true, selectedRegionPropertyId);
		});

		/*
		 * Time 3 : Region 2 changes its Property P value
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_2, testTime++, (environment) -> {
			environment.setRegionPropertyValue(TestRegionId.REGION_2, selectedRegionPropertyId, firstPropertyValue);
		});

		/*
		 * Time 4 : Region 1 stops observation 
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_1, testTime++, (environment) -> {
			environment.observeGlobalRegionPropertyChange(false,  selectedRegionPropertyId);
		});

		/*
		 * Time 5 : Compartment 1 starts observation 
		 */
		taskPlanContainer.addTaskPlan(TestCompartmentId.COMPARTMENT_1, testTime++, (environment) -> {
			environment.observeGlobalRegionPropertyChange(true, selectedRegionPropertyId);
		});

		/*
		 * Time 6 : Region 2 changes its Property P value
		 */
		taskPlanContainer.addTaskPlan(TestRegionId.REGION_2, testTime++, (environment) -> {
			environment.setRegionPropertyValue(TestRegionId.REGION_2, selectedRegionPropertyId, secondPropertyValue);
		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			// if the property id is null
			assertModelException(() -> environment.observeGlobalRegionPropertyChange(true, null), SimulationErrorType.NULL_REGION_PROPERTY_ID);
			// if the property is unknown
			assertModelException(() -> environment.observeGlobalRegionPropertyChange(true, TestRegionPropertyId.getUnknownRegionPropertyId()),
					SimulationErrorType.UNKNOWN_REGION_PROPERTY_ID);
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		final Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		/*
		 * Region 1: first property change --> demonstrates Post Conditions 1
		 * and 3
		 */

		expectedObservations.add(new MultiKey(3.0, TestRegionId.REGION_1, ObservationType.REGION_PROPERTY, TestRegionId.REGION_2, selectedRegionPropertyId, firstPropertyValue));

		/*
		 * Region 2: no observations demonstrates Post Condition 4
		 */

		/*
		 * Compartment 1: second property change --> demonstrates Post
		 * Conditions 1 and 2
		 */

		expectedObservations.add(new MultiKey(6.0, TestCompartmentId.COMPARTMENT_1, ObservationType.REGION_PROPERTY, TestRegionId.REGION_2, selectedRegionPropertyId, secondPropertyValue));

		/*
		 * All others: no observations --> demonstrates Post Condition 2
		 */
		final Set<MultiKey> actualObservations = observationContainer.getObservations();

		assertEquals(expectedObservations, actualObservations);

	}
}
