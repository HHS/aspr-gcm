package gcm.manual;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardPeople;
import static gcm.automated.support.EnvironmentSupport.addStandardPropertyDefinitions;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;
import static gcm.simulation.partition.Filter.compartment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.automated.support.EnvironmentSupport.PropertyAssignmentPolicy;
import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.replication.Replication;
import gcm.scenario.CompartmentId;
import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.Simulation;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.Partition;
import gcm.simulation.partition.PartitionSampler;
import gcm.util.StopWatch;
import gcm.util.TimeElapser;
import gcm.util.annotations.UnitTestMethod;



public class MT_Sample {

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
//				.println(MT_Sample.class.getSimpleName() + " " + SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link EnvironmentImpl#sampleIndex(Object,PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "sampleIndex", args = { Object.class, PersonId.class })
	public void testIndex() {
		/*
		 * Show that we can retrieve people from a population index while excluding a
		 * person who is in the index. We will do this repeatedly to show that the
		 * person retrieved is always from the index but never the excluded person.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 1000000);
		addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.TRUE, randomGenerator);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			StopWatch stopWatch = new StopWatch();
			EnvironmentImpl.indexStopWatch.reset();
			for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				final Set<PersonId> peopleInCompartment = new LinkedHashSet<>();
				for (final PersonId personId : scenario.getPeopleIds()) {
					if (scenario.getPersonCompartment(personId).equals(testCompartmentId)) {
						peopleInCompartment.add(personId);
					}
				}

				final Object key = new Object();
				environment.addPopulationIndex(compartment(testCompartmentId), key);
				for (int i = 0; i < 1000; i++) {
//					for (final PersonId personId : peopleInCompartment) {
					stopWatch.start();
					environment.sampleIndex(key);
					stopWatch.stop();
//						assertTrue(peopleInCompartment.contains(selectedPersonId));
//						assertFalse(selectedPersonId.equals(personId));
//					}
				}

				environment.removePopulationIndex(key);
			}
			System.out.println("MT_Sample.testIndex()" + stopWatch.getElapsedMilliSeconds());
			System.out.println("MT_Sample.testIndex()" + EnvironmentImpl.indexStopWatch.getElapsedMilliSeconds());
		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}
	
	private static Object prop1LabelFunction(Object propertyValue) {
		Integer value = (Integer)propertyValue;
		return value/10;
	}
	
	private static Object prop2LabelFunction(Object propertyValue) {
		Double value = (Double)propertyValue;
		return (int)(value*10);
	}

	/**
	 * Tests {@link EnvironmentImpl#samplePartition(Object, PartitionSampler)}
	 */
	@Test
	@UnitTestMethod(name = "samplePartition", args = { Object.class, PartitionSampler.class })
	public void testPartition() {
		/*
		 * Show that we can retrieve people from a population index while excluding a
		 * person who is in the index. We will do this repeatedly to show that the
		 * person retrieved is always from the index but never the excluded person.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setMapOption(MapOption.ARRAY).setType(Integer.class).setDefaultValue(35).build();
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1, propertyDefinition);
		
		propertyDefinition = PropertyDefinition.builder().setMapOption(MapOption.ARRAY).setType(Double.class).setDefaultValue(2.7).build();
		scenarioBuilder.definePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_2, propertyDefinition);
		
		
		//addStandardPropertyDefinitions(scenarioBuilder, PropertyAssignmentPolicy.TRUE, randomGenerator);
		int n = 1_000_000;		
		
		scenarioBuilder.setSuggestedPopulationSize(n);

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();
		
		
		System.out.println("scenario built");

		Replication replication = getReplication(randomGenerator);

		int testTime = 1;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			StopWatch stopWatch = new StopWatch();
			EnvironmentImpl.partitionStopWatch.reset();

			System.out.println("starting the addition of people");
			TimeElapser timeElapser = new TimeElapser();
			for (int i = 0; i < n; i++) {				
				PersonId personId = environment.addPerson(//
						TestRegionId.getRandomRegionId(randomGenerator),//
						TestCompartmentId.getRandomCompartmentId(randomGenerator)//
						);//
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1, environment.getRandomGenerator().nextInt(30));
				environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2, environment.getRandomGenerator().nextDouble());
			}
			System.out.println("adding people took "+timeElapser.getElapsedMilliSeconds());

			Filter filter = Filter.compartment(TestCompartmentId.COMPARTMENT_1).or(Filter.compartment(TestCompartmentId.COMPARTMENT_2));
			
			final Object key = new Object();
			
			timeElapser.reset();
			environment.addPartition(
					Partition.builder()
					.setFilter(filter)
					.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_1, MT_Sample::prop1LabelFunction)
					.setPersonPropertyFunction(TestPersonPropertyId.PERSON_PROPERTY_2, MT_Sample::prop2LabelFunction)
					.build()
					,key);
			System.out.println("partition addition took "+timeElapser.getElapsedMilliSeconds());
			System.out.println("I'm Ready!");

			for (int i = 0; i < n; i++) {
				PersonId excludedPersonId = new PersonId(environment.getRandomGenerator().nextInt(n));
				
				stopWatch.start();
				Optional<PersonId> optional = environment.samplePartition(key, 
						PartitionSampler.builder()
						.setExcludedPerson(excludedPersonId)
						.setLabelSet(LabelSet.builder()
								.setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1, 2)
						        .setPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2, 5)
								.build())				
						.build());
				stopWatch.stop();
				
				assertTrue(optional.isPresent());
				PersonId personId = optional.get();
				CompartmentId personCompartment = environment.getPersonCompartment(personId);
				assertTrue(personCompartment.equals(TestCompartmentId.COMPARTMENT_1)||personCompartment.equals(TestCompartmentId.COMPARTMENT_2));
				assertEquals(2,prop1LabelFunction(environment.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1)));
				assertEquals(5,prop2LabelFunction(environment.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2)));
				
			}

			environment.removePartition(key);

			System.out.println("MT_Sample.testPartition()" + stopWatch.getElapsedMilliSeconds());
			System.out
					.println("MT_Sample.testPartition()" + EnvironmentImpl.partitionStopWatch.getElapsedMilliSeconds());
//			System.out.println("MT_Sample.testSamplePartition_Object_PersonId()"+PopulationPartition.partitionStopWatch.getElapsedMilliSeconds());

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

	}

	public static void main(String[] args) {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(26));
		MT_Sample mt_Sample = new MT_Sample();
		mt_Sample.testPartition();

	}

}
