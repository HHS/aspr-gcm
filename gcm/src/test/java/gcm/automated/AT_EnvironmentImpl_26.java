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
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.automated.support.TestRandomGeneratorId;
import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestRegionId;
import gcm.replication.Replication;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.simulation.partition.LabelSetInfo;
import gcm.simulation.partition.LabelSetWeightingFunction;
import gcm.simulation.partition.Partition;
import gcm.util.MultiKey;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_26 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(26));
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large gaps
	 * in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
//		System.out
//				.println(AT_EnvironmentImpl_26.class.getSimpleName() + " " + SEED_PROVIDER.generateUnusedSeedReport());
	}

	/*
	 * Utility class for getting random people from population indices
	 */
	private static class Counter {

		public Counter(int count) {
			this.count = count;
		}

		int count;
	}

	private static double getWeight(ObservableEnvironment observableEnvironment, LabelSetInfo labelSetInfo) {
		if (!labelSetInfo.getCompartmentLabel().isPresent()) {
			throw new RuntimeException("no compartment label");
		}
		if (!labelSetInfo.getRegionLabel().isPresent()) {
			throw new RuntimeException("no region label");
		}

		Integer compartmentLabel = (Integer) labelSetInfo.getCompartmentLabel().get();
		Integer regionLabel = (Integer) labelSetInfo.getRegionLabel().get();

		double p1;
		switch (compartmentLabel) {
		case 0:
			p1 = 1;
			break;
		default:
			p1 = 0;
			break;
		}

		double p2;
		switch (regionLabel) {
		case 0:
			p2 = 0.1;
			break;
		case 1:
			p2 = 0.3;
			break;
		default:
			p2 = 0.6;
			break;
		}

		return p1 * p2;

	}

	private static double getZeroWeight(ObservableEnvironment observableEnvironment, LabelSetInfo labelSetInfo) {
		return 0;
	}

	// defines how we label compartments
	static Function<CompartmentId, Object> compartmentPartitionFunction = (CompartmentId compartmentId) -> {
		TestCompartmentId testCompartmentId = (TestCompartmentId) compartmentId;
		switch (testCompartmentId) {
		case COMPARTMENT_1:
		case COMPARTMENT_2:
		case COMPARTMENT_3:
			return 0;
		case COMPARTMENT_4:
		case COMPARTMENT_5:
		default:
			return 1;
		}
	};

	// defines how we label regions
	static Function<RegionId, Object> regionPartitionFunction = (RegionId regionId) -> {
		TestRegionId testRegionId = (TestRegionId) regionId;
		switch (testRegionId) {
		case REGION_1:
		case REGION_2:
			return 0;
		case REGION_3:
		case REGION_4:
			return 1;
		default:
			return 2;
		}
	};

	/**
	 * Tests
	 * {@link EnvironmentImpl#samplePartition(Object, LabelSetWeightingFunction, RandomNumberGeneratorId)}
	 */
	@Test
	@UnitTestMethod(name = "samplePartition", args = { Object.class,
			LabelSetWeightingFunction.class, RandomNumberGeneratorId.class })
	public void testSamplePartition() {

		/*
		 * Assert that group contacts via MonoWeightingFunctions work properly
		 */

		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 10);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.RANDOM, randomGenerator);

		RandomNumberGeneratorId randomNumberGeneratorId = TestRandomGeneratorId.BLITZEN;

		scenarioBuilder.addRandomNumberGeneratorId(randomNumberGeneratorId);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Object key = "key1";
			Partition partition = Partition.create()
					.compartment(AT_EnvironmentImpl_26.compartmentPartitionFunction)
					.region(AT_EnvironmentImpl_26.regionPartitionFunction);
			environment.addPopulationPartition(partition, key);

			// use a uniform distribution with 10000 repetitions

			final Map<MultiKey, Counter> actualSelections = new LinkedHashMap<>();
			actualSelections.put(new MultiKey(0, 0), new Counter(0));
			actualSelections.put(new MultiKey(0, 1), new Counter(0));
			actualSelections.put(new MultiKey(0, 2), new Counter(0));
			actualSelections.put(new MultiKey(1, 0), new Counter(0));
			actualSelections.put(new MultiKey(1, 1), new Counter(0));
			actualSelections.put(new MultiKey(1, 2), new Counter(0));

			final Map<MultiKey, Counter> expectedSelections = new LinkedHashMap<>();

			int sampleCount = 10000;

			expectedSelections.put(new MultiKey(0, 0), new Counter(sampleCount / 10));
			expectedSelections.put(new MultiKey(0, 1), new Counter(3 * sampleCount / 10));
			expectedSelections.put(new MultiKey(0, 2), new Counter(6 * sampleCount / 10));
			expectedSelections.put(new MultiKey(1, 0), new Counter(0));
			expectedSelections.put(new MultiKey(1, 1), new Counter(0));
			expectedSelections.put(new MultiKey(1, 2), new Counter(0));

			for (int i = 0; i < 10000; i++) {

				Optional<PersonId> opt = environment.samplePartition(key,
						AT_EnvironmentImpl_26::getWeight, randomNumberGeneratorId);

				assertTrue(opt.isPresent());
				PersonId personId = opt.get();
				CompartmentId compartmentId = environment.getPersonCompartment(personId);
				Object compartmentLabel = compartmentPartitionFunction.apply(compartmentId);
				RegionId regionId = environment.getPersonRegion(personId);
				Object regionLabel = regionPartitionFunction.apply(regionId);
				MultiKey multiKey = new MultiKey(compartmentLabel, regionLabel);
				Counter counter = actualSelections.get(multiKey);
				counter.count++;
			}

			for (MultiKey multiKey : expectedSelections.keySet()) {
				Counter expectedCounter = expectedSelections.get(multiKey);
				Counter actualCounter = actualSelections.get(multiKey);
				int tolerance = expectedCounter.count / 10;
				assertTrue(FastMath.abs(expectedCounter.count - actualCounter.count) <= tolerance);
			}

		});

		// show that a weighting function that returns all zeros will result in
		// an optional where no value is present
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Object key = "key2";
			Partition partition = Partition.create()
					.compartment(AT_EnvironmentImpl_26.compartmentPartitionFunction)
					.region(AT_EnvironmentImpl_26.regionPartitionFunction);
			environment.addPopulationPartition(partition, key);

			Optional<PersonId> opt = environment.samplePartition(key,
					AT_EnvironmentImpl_26::getZeroWeight, randomNumberGeneratorId);
			assertTrue(!opt.isPresent());
		});

		// test preconditions
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			RandomNumberGeneratorId unknownRandomNumberGeneratorId = TestRandomGeneratorId.COMET;

			Object key = "key3";
			Object badKey = "badKey";
			Partition partition = Partition.create()
					.compartment(AT_EnvironmentImpl_26.compartmentPartitionFunction)
					.region(AT_EnvironmentImpl_26.regionPartitionFunction);

			environment.addPopulationPartition(partition, key);

			// if the key is null
			assertModelException(
					() -> environment.samplePartition(null,
							AT_EnvironmentImpl_26::getWeight, randomNumberGeneratorId),
					SimulationErrorType.NULL_POPULATION_PARTITION_KEY);

			// if the key does not correspond to an existing population partition
			assertModelException(
					() -> environment.samplePartition(badKey,
							AT_EnvironmentImpl_26::getWeight, randomNumberGeneratorId),
					SimulationErrorType.UNKNOWN_POPULATION_PARTITION_KEY);

			// if the weighting function is null
			LabelSetWeightingFunction nullLabelSetWeightingFunction = null;
			assertModelException(() -> environment.samplePartition(key, nullLabelSetWeightingFunction,
					randomNumberGeneratorId), SimulationErrorType.NULL_WEIGHTING_FUNCTION);

			// if the randomNumberGeneratorId is null

			TestRandomGeneratorId nullRandomGeneratorId = null;
			assertModelException(
					() -> environment.samplePartition(key,AT_EnvironmentImpl_26::getWeight, nullRandomGeneratorId),
					SimulationErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID);

			// if the randomNumberGeneratorId does not correspond to an existing random
			// Number Generator Id in the scenario
			assertModelException(
					() -> environment.samplePartition(key,
							AT_EnvironmentImpl_26::getWeight, unknownRandomNumberGeneratorId),
					SimulationErrorType.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID);

		});
		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

}
