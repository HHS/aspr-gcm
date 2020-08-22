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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestResourceId;
import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Environment;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.GroupTypeCountMap;
import gcm.simulation.PopulationPartitionDefinition;
import gcm.simulation.PopulationPartitionQuery;
import gcm.simulation.Simulation;
import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

@UnitTest(target = EnvironmentImpl.class)

public class AT_EnvironmentImpl_25 {

	private static SeedProvider SEED_PROVIDER;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(EnvironmentSupport.getMetaSeed(25));
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large gaps
	 * in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		System.out
				.println(AT_EnvironmentImpl_25.class.getSimpleName() + " " + SEED_PROVIDER.generateUnusedSeedReport());
	}

	private static enum PartitionChoice {
		COMPARTMENT, REGION, PROPERTY1, PROPERTY2, RESOURCE1, RESOURCE2, GROUP;
	}

	/**
	 * Tests {@link EnvironmentImpl#getPartitionSize(PersonPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "getPartitionSize", args = { Object.class, PopulationPartitionQuery.class })
	public void testGetPartitionSize() {
		fail();
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getRandomPartitionedPersonFromGenerator(Object, PopulationPartitionQuery, RandomNumberGeneratorId)
	 */
	@Test
	@UnitTestMethod(name = "getRandomPartitionedPersonFromGenerator", args = { Object.class,
			PopulationPartitionQuery.class, RandomNumberGeneratorId.class })
	public void testGetRandomPartitionedPersonFromGenerator() {
		fail();
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getRandomPartitionedPersonWithExclusion(PersonId, Object, PopulationPartitionQuery)
	 */
	@Test
	@UnitTestMethod(name = "getRandomPartitionedPersonWithExclusion", args = { PersonId.class, Object.class,
			PopulationPartitionQuery.class })
	public void testGetRandomPartitionedPersonWithExclusion() {
		fail();
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

	// defines how we label resource 1
	static Function<Long, Object> personResource1PartitionFunction = (Long value) -> {
		if (value == 0) {
			return "zilch";
		}
		if (value < 4) {
			return "some";
		}
		return "whoa";
	};

	// defines how we label resource 2
	static Function<Long, Object> personResource2PartitionFunction = (Long value) -> {
		if (value == 0) {
			return "none";
		}
		if (value < 2) {
			return "good";
		}
		return "too much";
	};

	// defines how we label property1 values
	static Function<Object, Object> property1Function = (Object value) -> ((Integer) value) % 2;

	// defines how we label property2 values
	static Function<Object, Object> property2Function = (Object value) -> ((String) value).length() % 3;

	// defies how we label group types
	static Function<GroupTypeCountMap, Object> groupPartitionFunction = (GroupTypeCountMap groupTypeCountMap) -> {
		int group1Count = groupTypeCountMap.getGroupCount(TestGroupTypeId.GROUP_TYPE_1);
		int group2Count = groupTypeCountMap.getGroupCount(TestGroupTypeId.GROUP_TYPE_2);
		return 2 * group1Count + group2Count;
	};

	// helps generate randomized values for property 1
	static Function<RandomGenerator, Integer> property1InitializerFunction = (RandomGenerator r) -> {
		return r.nextInt(2);
	};

	// helps generate randomized values for property 2
	static Function<RandomGenerator, String> property2InitializerFunction = (RandomGenerator r) -> {
		switch (r.nextInt(3)) {
		case 0:
			return "yep";
		case 1:
			return "nope";
		default:
			return "maybe";
		}
	};

	// helps generate randomized values for property 1
	static Function<RandomGenerator, Long> resource1InitializerFunction = (RandomGenerator r) -> {
		return (long) r.nextInt(5);
	};

	// helps generate randomized values for property 2
	static Function<RandomGenerator, Long> resource2InitializerFunction = (RandomGenerator r) -> {
		return (long) r.nextInt(4);
	};

	private static void makeRandomPersonAssignments(Environment environment, RandomGenerator randomGenerator) {
		// add some groups to the simulation
		List<GroupId> type1Groups = new ArrayList<>();
		List<GroupId> type2Groups = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			type1Groups.add(environment.addGroup(TestGroupTypeId.GROUP_TYPE_1));
			type2Groups.add(environment.addGroup(TestGroupTypeId.GROUP_TYPE_2));
		}

		List<PersonId> people = environment.getPeople();
		for (int i = 0; i < people.size(); i++) {
			PersonId personId = people.get(i);
			environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_1,
					property1InitializerFunction.apply(randomGenerator));
			environment.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2,
					property2InitializerFunction.apply(randomGenerator));

			RegionId personRegion = environment.getPersonRegion(personId);

			long amount = resource1InitializerFunction.apply(randomGenerator);
			environment.addResourceToRegion(TestResourceId.RESOURCE1, personRegion, amount);
			environment.transferResourceToPerson(TestResourceId.RESOURCE1, personId, amount);

			amount = resource2InitializerFunction.apply(randomGenerator);
			environment.addResourceToRegion(TestResourceId.RESOURCE2, personRegion, amount);
			environment.transferResourceToPerson(TestResourceId.RESOURCE2, personId, amount);

			int groupCount = randomGenerator.nextInt(3);
			Collections.shuffle(type1Groups, new Random(randomGenerator.nextLong()));
			for (int j = 0; j < groupCount; j++) {
				GroupId groupId = type1Groups.get(j);
				environment.addPersonToGroup(personId, groupId);
			}

			groupCount = randomGenerator.nextInt(4);
			Collections.shuffle(type2Groups, new Random(randomGenerator.nextLong()));
			for (int j = 0; j < groupCount; j++) {
				GroupId groupId = type2Groups.get(j);
				environment.addPersonToGroup(personId, groupId);
			}

		}

	}

	private static PopulationPartitionDefinition createPopulationPartitionDefinition(
			Set<PartitionChoice> partionChoices) {
		PopulationPartitionDefinition.Builder builder = PopulationPartitionDefinition.builder();//

		for (PartitionChoice partitionChoice : partionChoices) {
			switch(partitionChoice) {
			case COMPARTMENT:
				builder.setCompartmentPartition(compartmentPartitionFunction);
				break;
			case GROUP:
				builder.setGroupPartitionFunction(groupPartitionFunction);
				break;
			case PROPERTY1:
				builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_1, property1Function);
				break;
			case PROPERTY2:
				builder.setPersonPropertyPartition(TestPersonPropertyId.PERSON_PROPERTY_2, property2Function);
				break;
			case REGION:
				builder.setRegionPartition(regionPartitionFunction);
				break;
			case RESOURCE1:
				builder.setPersonResourcePartition(TestResourceId.RESOURCE1, personResource1PartitionFunction);
				break;
			case RESOURCE2:
				builder.setPersonResourcePartition(TestResourceId.RESOURCE2, personResource2PartitionFunction);
				break;
			default:
				throw new RuntimeException("unhandled case");
			}
		}

		return builder.build();
	}
	
	public static Map<PopulationPartitionQuery, Set<PersonId>> getExpectedPartitionedPeople(Environment environment,Set<PartitionChoice> partionChoices){
		
		List<PersonId> people = environment.getPeople();
		Map<PopulationPartitionQuery, Set<PersonId>> expectedPartitioning = new LinkedHashMap<>();
		for (PersonId personId : people) {
			// retrieve the various characteristics of the person and convert them into
			// labels
			RegionId personRegion = environment.getPersonRegion(personId);
			Object regionLabel = regionPartitionFunction.apply(personRegion);

			CompartmentId personCompartment = environment.getPersonCompartment(personId);
			Object compartmentLabel = compartmentPartitionFunction.apply(personCompartment);

			Object personProperty1Value = environment.getPersonPropertyValue(personId,
					TestPersonPropertyId.PERSON_PROPERTY_1);
			Object personProperty1Label = property1Function.apply(personProperty1Value);

			Object personProperty2Value = environment.getPersonPropertyValue(personId,
					TestPersonPropertyId.PERSON_PROPERTY_2);
			Object personProperty2Label = property2Function.apply(personProperty2Value);

			long personResource1Level = environment.getPersonResourceLevel(personId, TestResourceId.RESOURCE1);
			Object personResource1Label = personResource1PartitionFunction.apply(personResource1Level);

			long personResource2Level = environment.getPersonResourceLevel(personId, TestResourceId.RESOURCE2);
			Object personResource2Label = personResource2PartitionFunction.apply(personResource2Level);

			int groupCount1 = environment.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1,
					personId);
			int groupCount2 = environment.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_2,
					personId);
			GroupTypeCountMap groupTypeCountMap = GroupTypeCountMap.builder()
					.setCount(TestGroupTypeId.GROUP_TYPE_1, groupCount1)
					.setCount(TestGroupTypeId.GROUP_TYPE_2, groupCount2).build();

			Object groupLabel = groupPartitionFunction.apply(groupTypeCountMap);

			// use the labels to create a population query that fits the person
			PopulationPartitionQuery.Builder builder = PopulationPartitionQuery.builder();
			for(PartitionChoice partitionChoice : partionChoices) {
				switch(partitionChoice) {
				case COMPARTMENT:
					builder.setCompartmentLabel(compartmentLabel);
					break;
				case GROUP:
					builder.setGroupLabel(groupLabel);
					break;
				case PROPERTY1:
					builder.setPersonPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1, personProperty1Label);
					break;
				case PROPERTY2:
					builder.setPersonPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2, personProperty2Label);
					break;
				case REGION:
					builder.setRegionLabel(regionLabel);
					break;
				case RESOURCE1:
					builder.setPersonResourceLabel(TestResourceId.RESOURCE1, personResource1Label);
					break;
				case RESOURCE2:
					builder.setPersonResourceLabel(TestResourceId.RESOURCE2, personResource2Label);
					break;
				default:
					throw new RuntimeException("unhandled case");				
				}
			}
			
			PopulationPartitionQuery populationPartitionQuery = builder.build();

			// add the population query to the expectedPartitioning if it is new and add the
			// person to the set of people we expect to be associated with this query
			Set<PersonId> partitionPeople = expectedPartitioning.get(populationPartitionQuery);
			if (partitionPeople == null) {
				partitionPeople = new LinkedHashSet<>();
				expectedPartitioning.put(populationPartitionQuery, partitionPeople);
			}
			partitionPeople.add(personId);
		}
		return expectedPartitioning;
	}
	
	private static void buildBaseScenario(ScenarioBuilder scenarioBuilder,RandomGenerator randomGenerator) {
		
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		Map<Object, PropertyDefinition> forcedPropertyDefinitions = new LinkedHashMap<>();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0)
				.build();
		forcedPropertyDefinitions.put(TestPersonPropertyId.PERSON_PROPERTY_1, propertyDefinition);
		propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("nope").build();
		forcedPropertyDefinitions.put(TestPersonPropertyId.PERSON_PROPERTY_2, propertyDefinition);
		addStandardPropertyDefinitions(scenarioBuilder, forcedPropertyDefinitions, PropertyAssignmentPolicy.TRUE,
				randomGenerator);
		addStandardPeople(scenarioBuilder, 300);

		
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getPartitionPeople(Object, PopulationPartitionQuery)
	 */
	@Test
	@UnitTestMethod(name = "getPartitionPeople", args = { Object.class, PopulationPartitionQuery.class })
	public void testGetPartitionPeople() {
		/*
		 * Go through the boilerplate steps of generating a scenario that will support
		 * the testing of a population partition that exercises all of the partition's
		 * dimensions.
		 */

		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		buildBaseScenario(scenarioBuilder,randomGenerator);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);
		Scenario scenario = scenarioBuilder.build();

		/*
		 * Create various functions that will 1) help assign initial values to people
		 * and 2) define the partition
		 */

		int testTime = 1;
		Object key = "key";

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Set<PartitionChoice> partitionChoices = EnumSet.allOf(PartitionChoice.class);
			
			
			// define the partition
			PopulationPartitionDefinition populationPartitionDefinition =
			createPopulationPartitionDefinition(partitionChoices);


			// add the partition to the simulation
			environment.addPopulationPartition(populationPartitionDefinition, key);

			makeRandomPersonAssignments(environment, randomGenerator);

			// Group the people in the simulation by their labels, giving us the expected
			// partitioning
			Map<PopulationPartitionQuery, Set<PersonId>> expectedPartitioning = getExpectedPartitionedPeople(environment,partitionChoices);

			// show that each set of people we expect should be associated with a given set
			// of labels matches the set returned by the simulation
			for (PopulationPartitionQuery populationPartitionQuery : expectedPartitioning.keySet()) {

				Set<PersonId> expectedPeople = expectedPartitioning.get(populationPartitionQuery);
				Set<PersonId> actualPeople = environment.getPartitionPeople(key, populationPartitionQuery).stream()
						.collect(Collectors.toSet());
				assertEquals(expectedPeople, actualPeople);				
			}

		});

		/*
		 * Precondition tests
		 */
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			PopulationPartitionQuery populationPartitionQuery = PopulationPartitionQuery.builder()
					.setCompartmentLabel(0).setPersonPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_1, 1).build();
			PopulationPartitionQuery incompatiblePopulationPartitionQuery = PopulationPartitionQuery.builder()
					.setCompartmentLabel(0).setPersonPropertyLabel(TestPersonPropertyId.PERSON_PROPERTY_2, 1).build();

			// if the key is null
			assertModelException(() -> environment.getPartitionPeople(null, populationPartitionQuery),
					SimulationErrorType.NULL_POPULATION_PARTITION_KEY);
			// if the key does not correspond to an existing partition
			assertModelException(() -> environment.getPartitionPeople("bad key", populationPartitionQuery),
					SimulationErrorType.UNKNOWN_POPULATION_PARTITION_KEY);
			// if the populationQuery is null
			assertModelException(() -> environment.getPartitionPeople(key, null),
					SimulationErrorType.NULL_POPULATION_PARTITION_QUERY);
			// if the key does not correspond to an existing partition
			assertModelException(() -> environment.getPartitionPeople(key, incompatiblePopulationPartitionQuery),
					SimulationErrorType.INCOMPATIBLE_POPULATION_PARTITION_QUERY);

		});

		Simulation simulation = new Simulation();
		simulation.setReplication(getReplication(randomGenerator));
		simulation.setScenario(scenario);
		simulation.execute();
		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#addPopulationPartition(PopulationPartitionDefinition, Object)
	 */
	@Test
	@UnitTestMethod(name = "addPopulationPartition", args = { PopulationPartitionDefinition.class, Object.class })
	public void testAddPopulationPartition() {
		fail();
	}

	/**
	 * Tests {@link EnvironmentImpl#removePopulationPartition(Object)
	 */
	@Test
	@UnitTestMethod(name = "removePopulationPartition", args = { Object.class })
	public void testRemovePopulationPartition() {
		fail();
	}

	/**
	 * Tests {@link EnvironmentImpl#populationPartitionExists(Object)
	 */
	@Test
	@UnitTestMethod(name = "populationPartitionExists", args = { Object.class })
	public void testPopulationPartitionExists() {
		fail();
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#personIsInPopulationPartition(PersonId, Object, PopulationPartitionQuery)
	 */
	@Test
	@UnitTestMethod(name = "personIsInPopulationPartition", args = { PersonId.class, Object.class,
			PopulationPartitionQuery.class })
	public void testPersonIsInPopulationPartition() {
		fail();
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getRandomPartitionedPerson(Object, PopulationPartitionQuery)
	 */
	@Test
	@UnitTestMethod(name = "getRandomPartitionedPerson", args = { Object.class, PopulationPartitionQuery.class })
	public void testGetRandomPartitionedPerson() {
		fail();
	}

	/**
	 * Tests
	 * {@link EnvironmentImpl#getRandomPartitionedPersonWithExclusionFromGenerator(PersonId, Object, PopulationPartitionQuery, RandomNumberGeneratorId)
	 */
	@Test
	@UnitTestMethod(name = "getRandomPartitionedPersonWithExclusionFromGenerator", args = { PersonId.class,
			Object.class, PopulationPartitionQuery.class, RandomNumberGeneratorId.class })
	public void testGetRandomPartitionedPersonWithExclusionFromGenerator() {
		fail();
	}

}
