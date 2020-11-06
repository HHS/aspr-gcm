package gcm.manual.personaddition;

import static gcm.automated.support.EnvironmentSupport.addStandardComponentsAndTypes;
import static gcm.automated.support.EnvironmentSupport.addStandardTrackingAndScenarioId;
import static gcm.automated.support.EnvironmentSupport.addTaskPlanContainer;
import static gcm.automated.support.EnvironmentSupport.assertAllPlansExecuted;
import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.EnvironmentSupport.getReplication;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import gcm.scenario.TimeTrackingPolicy;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Equality;
import gcm.simulation.ObservableEnvironment;
import gcm.simulation.PersonConstructionInfo;
import gcm.simulation.PersonConstructionInfo.Builder;
import gcm.simulation.Simulation;
import gcm.simulation.partition.Filter;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.Partition;
import gcm.simulation.partition.PartitionSampler;
import gcm.util.MemSizer;
import gcm.util.TimeElapser;

public class MT_PartitionManagement {
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


	private static enum LocalPersonPropertyId implements PersonPropertyId {

		AGE, IMMUNE, VACCINATED, SERUM_DENSITY;

		public PropertyDefinition getPropertyDefinition(MapOption mapOption, TimeTrackingPolicy timeTrackingPolicy) {
			switch (this) {
			case AGE:

				return PropertyDefinition.builder()//
						.setDefaultValue(0)//
						.setMapOption(mapOption)//
						.setTimeTrackingPolicy(timeTrackingPolicy)//
						.setType(Integer.class)//
						.build();
			case IMMUNE:
				return PropertyDefinition.builder()//
						.setDefaultValue(false)//
						.setMapOption(mapOption)//
						.setTimeTrackingPolicy(timeTrackingPolicy)//
						.setType(Boolean.class)//
						.build();
			case SERUM_DENSITY:
				return PropertyDefinition.builder()//
						.setDefaultValue(5.5)//
						.setMapOption(mapOption)//
						.setTimeTrackingPolicy(timeTrackingPolicy)//
						.setType(Double.class)//
						.build();
			case VACCINATED:
				return PropertyDefinition.builder()//
						.setDefaultValue(false)//
						.setMapOption(mapOption)//
						.setTimeTrackingPolicy(timeTrackingPolicy)//
						.setType(Boolean.class)//
						.build();
			default:
				throw new RuntimeException("unhandled case " + this);

			}

		}

	}

	private static enum Phase {
		LOAD_POPULATION, LOAD_PARTITION, UPDATE_PARTITION, SAMPLE_PARTITION, MEASURE_MEMORY;
	}

	private static enum AgeGroup {
		CHILD, ADULT, SENIOR;
	}

	private Map<Integer, Integer> serumDensityLabels = new LinkedHashMap<>();

	private double getWeight(ObservableEnvironment observableEnvironment, LabelSet labelSet) {
		return 1;
	}

	private Object getSerumDensityLabel(Object serumDensity) {
		Double sd = (Double) serumDensity;
		Integer label = (int) (sd * 1000);
		if (!serumDensityLabels.containsKey(label)) {
			serumDensityLabels.put(label, label);
		}
		return serumDensityLabels.get(label);
	}

	private Object getAgeLabel(Object age) {
		Integer a = (Integer) age;
		if (a < 20) {
			return AgeGroup.CHILD;
		}
		if (a < 50) {
			return AgeGroup.ADULT;
		}
		return AgeGroup.SENIOR;
	}

	@Test
	public void test2() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		int populationSize = 1_000;

		boolean useDefaultPropertyValues = false;
		boolean loadPopulationFirst = false;
		boolean useArray = false;
		
		boolean useFilter = false;
		boolean measureMemory = true;
		testInternal(randomGenerator, populationSize, loadPopulationFirst, useArray,  useFilter,
				useDefaultPropertyValues, measureMemory);

	}
	
	@Test 
	public void test3() {
		List<Integer> values = new ArrayList<>();
		
		for(int i = 0;i<1000;i++) {
			values.add(i);
		}
		
		Map<Integer,Integer> map = new LinkedHashMap<>();
		
		for(Integer value : values) {
			map.put(value, value);		
		}
		
		MemSizer memSizer = new MemSizer(false);
		System.out.println(memSizer.getByteCount(map));
		
	}

	/**
	 * 1_000_000 people
	 * 
	 * 1 partition -- filtering on 2 props, partitioning on one of those and yet
	 * another into several hundred cells
	 * 
	 * demonstrate the use of ARRAY mapping vs NONE against pre and post population
	 * loading relative to the partition
	 * 
	 * demonstrate the execution of several million random updates to the
	 * partition's contents
	 * 
	 * compare total runtime for each combination
	 * 
	 * popsize,density, pop loading order, array vs none, filter vs no filter
	 * 
	 */
	@Test
	public void test1() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		int populationSize = 1_000_000;
		boolean measureMemory = false;

		List<Boolean> populationFirstList = generateBooleanList();
		List<Boolean> useArrayList = generateBooleanList();
		List<Boolean> useFilterList = generateBooleanList();
		List<Boolean> useDefaultPropertyValuesList = generateBooleanList();

		System.out.println(Report.toHeader());
		for (Boolean loadPopulationFirst : populationFirstList) {
			for (Boolean useDefaultPropertyValues : useDefaultPropertyValuesList) {
				for (Boolean useArray : useArrayList) {
					
						for (Boolean useFilter : useFilterList) {
							Report report = testInternal(randomGenerator, populationSize, loadPopulationFirst, useArray,
									 useFilter, useDefaultPropertyValues, measureMemory);
							System.out.println(report.toString());
						}
					
				}
			}
		}

	}

	private static List<Boolean> generateBooleanList() {
		List<Boolean> result = new ArrayList<>();
		result.add(false);
		result.add(true);
		return result;
	}

	private static class Report {

		int populationSize;
		boolean loadPopulationFirst;
		MapOption mapOption;

		boolean useFilter;
		double partitionLoadTime;
		int partitionSize;
		double populationLoadTime;
		double partitionUpdateTime;
		int partitionUpdateCount;
		double partitionSampleTime;
		int partitionSampleCount;
		boolean useDefaultPropertyValues;

		public void setPopulationSize(int populationSize) {
			this.populationSize = populationSize;
		}

		public void setLoadPopulationFirst(boolean loadPopulationFirst) {
			this.loadPopulationFirst = loadPopulationFirst;
		}

		public void setMapOption(MapOption mapOption) {
			this.mapOption = mapOption;
		}

		public void setUseFilter(boolean useFilter) {
			this.useFilter = useFilter;
		}

		public void setPartitionLoadTime(double partitionLoadTime) {
			this.partitionLoadTime = partitionLoadTime;
		}

		public void setPartitionSize(int partitionSize) {
			this.partitionSize = partitionSize;
		}

		public void setPopulationLoadTime(double populationLoadTime) {
			this.populationLoadTime = populationLoadTime;
		}

		public void setPartitionUpdateTime(double partitionUpdateTime) {
			this.partitionUpdateTime = partitionUpdateTime;
		}

		public void setPartitionUpdateCount(int partitionUpdateCount) {
			this.partitionUpdateCount = partitionUpdateCount;
		}

		public void setPartitionSampleTime(double partitionSampleTime) {
			this.partitionSampleTime = partitionSampleTime;
		}

		public void setPartitionSampleCount(int partitionSampleCount) {
			this.partitionSampleCount = partitionSampleCount;
		}

		public void setUseDefaultPropertyValues(boolean useDefaultPropertyValues) {
			this.useDefaultPropertyValues = useDefaultPropertyValues;
		}

		public static String toHeader() {
			StringBuilder sb = new StringBuilder();
			sb.append("population");
			sb.append("\t");
			sb.append("population first");
			sb.append("\t");
			sb.append("useDefaultPropertyValues");
			sb.append("\t");
			sb.append("map option");
			sb.append("\t");
			sb.append("filtered");
			sb.append("\t");
			sb.append("partition load time");
			sb.append("\t");
			sb.append("partition size");
			sb.append("\t");
			sb.append("population load time");
			sb.append("\t");
			sb.append("partition update time");
			sb.append("\t");
			sb.append("partition update count");
			sb.append("\t");
			sb.append("partition sample time");
			sb.append("\t");
			sb.append("partition sample count");

			return sb.toString();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(populationSize);
			sb.append("\t");
			sb.append(loadPopulationFirst);
			sb.append("\t");
			sb.append(useDefaultPropertyValues);
			sb.append("\t");
			sb.append(mapOption);
			sb.append("\t");
			sb.append(useFilter);
			sb.append("\t");
			sb.append(partitionLoadTime);
			sb.append("\t");
			sb.append(partitionSize);
			sb.append("\t");
			sb.append(populationLoadTime);
			sb.append("\t");
			sb.append(partitionUpdateTime);
			sb.append("\t");
			sb.append(partitionUpdateCount);
			sb.append("\t");
			sb.append(partitionSampleTime);
			sb.append("\t");
			sb.append(partitionSampleCount);

			return sb.toString();
		}

	}

	private Report testInternal(//
			RandomGenerator randomGenerator, //
			int populationSize, //
			boolean loadPopulationFirst, //
			boolean useArray, //
			boolean useFilter, //
			boolean useDefaultPropertyValues, //
			boolean measureMemory) {//

		Report report = new Report();
		report.setPopulationSize(populationSize);
		report.setLoadPopulationFirst(loadPopulationFirst);
		report.setUseFilter(useFilter);
		report.setUseDefaultPropertyValues(useDefaultPropertyValues);

		Map<Phase, Double> phaseTimeMap = new LinkedHashMap<>();
		if (loadPopulationFirst) {
			phaseTimeMap.put(Phase.LOAD_POPULATION, 1.0);
			phaseTimeMap.put(Phase.LOAD_PARTITION, 2.0);
		} else {
			phaseTimeMap.put(Phase.LOAD_POPULATION, 2.0);
			phaseTimeMap.put(Phase.LOAD_PARTITION, 1.0);
		}
		phaseTimeMap.put(Phase.UPDATE_PARTITION, 3.0);
		phaseTimeMap.put(Phase.SAMPLE_PARTITION, 4.0);
		phaseTimeMap.put(Phase.MEASURE_MEMORY, 5.0);

		MapOption mapOption;
		if (useArray) {
			mapOption = MapOption.ARRAY;
		} else {
			mapOption = MapOption.NONE;
		}
		report.setMapOption(mapOption);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);

		// load the person properties
		for (LocalPersonPropertyId localPersonPropertyId : LocalPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = localPersonPropertyId.getPropertyDefinition(mapOption,
					TimeTrackingPolicy.DO_NOT_TRACK_TIME);
			scenarioBuilder.definePersonProperty(localPersonPropertyId, propertyDefinition);
		}

		Object partitionId = new Object();

		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		// load population
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, phaseTimeMap.get(Phase.LOAD_POPULATION),
				(environment) -> {

					TimeElapser timeElapser = new TimeElapser();

					if (useDefaultPropertyValues) {
						for (int i = 0; i < populationSize; i++) {

							PersonId personId = environment.addPerson(TestRegionId.getRandomRegionId(randomGenerator),
									TestCompartmentId.getRandomCompartmentId(randomGenerator));

							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.AGE,
									randomGenerator.nextInt(60));
							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.IMMUNE,
									randomGenerator.nextBoolean());
							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.VACCINATED,
									randomGenerator.nextBoolean());
							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.SERUM_DENSITY,
									randomGenerator.nextDouble() * 0.9 + 0.1);

						}
					} else {

						for (int i = 0; i < populationSize; i++) {
							Builder builder = PersonConstructionInfo.builder()//
									.setPersonRegionId(TestRegionId.getRandomRegionId(randomGenerator))//
									.setPersonCompartmentId(TestCompartmentId.getRandomCompartmentId(randomGenerator));//

							builder.setPersonPropertyValue(LocalPersonPropertyId.AGE, randomGenerator.nextInt(60));
							builder.setPersonPropertyValue(LocalPersonPropertyId.IMMUNE, randomGenerator.nextBoolean());
							builder.setPersonPropertyValue(LocalPersonPropertyId.VACCINATED,
									randomGenerator.nextBoolean());
							builder.setPersonPropertyValue(LocalPersonPropertyId.SERUM_DENSITY,
									randomGenerator.nextDouble() * 0.9 + 0.1);

							PersonConstructionInfo personConstructionInfo = builder.build();
							environment.addPerson(personConstructionInfo);
						}
					}
					report.setPopulationLoadTime(timeElapser.getElapsedMilliSeconds());

				});

		// load partition

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, phaseTimeMap.get(Phase.LOAD_PARTITION),
				(environment) -> {

					TimeElapser timeElapser = new TimeElapser();
					Filter filter;
					if (useFilter) {
						filter = Filter.property(LocalPersonPropertyId.AGE, Equality.GREATER_THAN, 15)
								.and(Filter.property(LocalPersonPropertyId.IMMUNE, Equality.EQUAL, false))
								.and(Filter.property(LocalPersonPropertyId.VACCINATED, Equality.EQUAL, false));
					} else {
						filter = Filter.allPeople();
					}
					Partition partition = Partition.create()//
							.filter(filter)//
							.property(LocalPersonPropertyId.AGE, this::getAgeLabel)//
							.property(LocalPersonPropertyId.SERUM_DENSITY, this::getSerumDensityLabel);//

					// partition = Partition.create().property(LocalPersonPropertyId.AGE,
					// (age)->age);

					environment.addPartition(partition, partitionId);

					report.setPartitionLoadTime(timeElapser.getElapsedMilliSeconds());
					report.setPartitionSize(environment.getPartitionSize(partitionId));

				});

//		update the property values

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1,
				phaseTimeMap.get(Phase.UPDATE_PARTITION), (environment) -> {
					int count = 0;

					TimeElapser timeElapser = new TimeElapser();
					List<PersonId> people = environment.getPeople();

					for (PersonId personId : people) {
						Boolean vaccinated = environment.getPersonPropertyValue(personId,
								LocalPersonPropertyId.VACCINATED);
						if (!vaccinated && randomGenerator.nextDouble() < 0.3) {
							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.VACCINATED, true);
							count++;
						}

						if (randomGenerator.nextDouble() < 0.25) {
							Double serumDensity = environment.getPersonPropertyValue(personId,
									LocalPersonPropertyId.SERUM_DENSITY);
							environment.setPersonPropertyValue(personId, LocalPersonPropertyId.SERUM_DENSITY,
									serumDensity / 2);
							count++;
						}
					}

					report.setPartitionUpdateTime(timeElapser.getElapsedMilliSeconds());
					report.setPartitionUpdateCount(count);

				});

//		take samples from the partition
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1,
				phaseTimeMap.get(Phase.SAMPLE_PARTITION), (environment) -> {

					TimeElapser timeElapser = new TimeElapser();

					int sampleRepetitionCount = 1000;
					for (int i = 0; i < sampleRepetitionCount; i++) {
						for (AgeGroup ageGroup : AgeGroup.values()) {
							for (Integer serumDensityLabel : serumDensityLabels.keySet()) {

								LabelSet labelSet = LabelSet.builder()//
										.setPropertyLabel(LocalPersonPropertyId.AGE, ageGroup)//
										.setPropertyLabel(LocalPersonPropertyId.SERUM_DENSITY, serumDensityLabel)
										.build();//

								PartitionSampler partitionSampler = PartitionSampler.builder()//
										.setLabelSet(labelSet)//
										.setLabelSetWeightingFunction(this::getWeight)//
										.build();//

								environment.samplePartition(partitionId, partitionSampler);
							}
						}
					}
					int numberOfSamples = sampleRepetitionCount;
					numberOfSamples *= AgeGroup.values().length;
					numberOfSamples *= serumDensityLabels.size();
					report.setPartitionSampleTime(timeElapser.getElapsedMilliSeconds());
					report.setPartitionSampleCount(numberOfSamples);

				});

//		measure the size of the partition
		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, phaseTimeMap.get(Phase.MEASURE_MEMORY),
				(environment) -> {
//					if (measureMemory) {
//						long memSizeOfPartition = ((EnvironmentImpl) environment).getMemSizeOfPartition(partitionId);
//						System.out.println("memSizeOfPartition = " + memSizeOfPartition);
//					}
				});

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		assertAllPlansExecuted(taskPlanContainer);

		return report;
	}

}
