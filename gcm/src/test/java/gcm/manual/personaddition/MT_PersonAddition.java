package gcm.manual.personaddition;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.SeedProvider;
import gcm.automated.support.TaskPlanContainer;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestRegionId;
import gcm.replication.Replication;
import gcm.scenario.MapOption;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.PersonConstructionInfo;
import gcm.simulation.PersonConstructionInfo.Builder;
import gcm.simulation.Simulation;
import gcm.util.TimeElapser;

public class MT_PersonAddition {
	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(234523456458997689L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large gaps
	 * in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		// System.out.println(SEED_PROVIDER.generateUnusedSeedReport());
	}
	
	private static class LocalPersonPropertyId implements PersonPropertyId{
		private final int id;
		public LocalPersonPropertyId(int id) {
			this.id = id;
		}
		public String toString() {return "LocalPersonPropertyId "+id;}
	}

	@Test
	public void test() {
		int propertyCount = 10;
		int populationSize = 1_000_000;
		
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		
		
		for(int i = 0;i<propertyCount;i++) {			
			PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
					.setDefaultValue(5.5)//
					.setMapOption(MapOption.NONE)//
					.setPropertyValueMutability(true)//
					//.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
					.setType(Double.class)//
					.build();//			
			
			LocalPersonPropertyId localPersonPropertyId = new LocalPersonPropertyId(i);
			scenarioBuilder.definePersonProperty(localPersonPropertyId, propertyDefinition);
		}

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

	
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 2, (environment) -> {
			TimeElapser timeElapser = new TimeElapser();
			
			for (int i = 0; i < populationSize; i++) {
				environment.addPerson(TestRegionId.getRandomRegionId(randomGenerator), TestCompartmentId.getRandomCompartmentId(randomGenerator));
			}
			System.out.println("person addition "+timeElapser.getElapsedMilliSeconds());
			
			Set<PersonPropertyId> personPropertyIds = environment.getPersonPropertyIds();
			
			List<PersonId> people = environment.getPeople();
			
			timeElapser.reset();
			
			for(PersonPropertyId personPropertyId : personPropertyIds) {
				for (PersonId personId : people) {
					environment.setPersonPropertyValue(personId, personPropertyId, randomGenerator.nextDouble());
				}
			}
			
			System.out.println("property resets "+timeElapser.getElapsedMilliSeconds());

		});
		
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, 1, (environment) -> {
			
			TimeElapser timeElapser = new TimeElapser();
			Set<PersonPropertyId> personPropertyIds = environment.getPersonPropertyIds();
			for (int i = 0; i < populationSize; i++) {
				Builder builder = PersonConstructionInfo.builder()//
				.setPersonRegionId(TestRegionId.getRandomRegionId(randomGenerator))//
				.setPersonCompartmentId(TestCompartmentId.getRandomCompartmentId(randomGenerator));//
				
				for(PersonPropertyId personPropertyId : personPropertyIds) {
					builder.setPersonPropertyValue(personPropertyId, randomGenerator.nextDouble());
				}	
				
				PersonConstructionInfo personConstructionInfo = builder.build();
				environment.addPerson(personConstructionInfo);
			}
			System.out.println("person addition with properties "+timeElapser.getElapsedMilliSeconds());

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);
	}

}
