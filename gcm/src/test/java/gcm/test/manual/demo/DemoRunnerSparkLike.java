
package gcm.test.manual.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import gcm.experiment.Experiment;
import gcm.experiment.ExperimentExecutorSparkLike;
import gcm.experiment.outputitemhandlersuppliers.SparkLikeMetaItemHandlerSupplier;
import gcm.experiment.progress.ExperimentProgressLog;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.output.simstate.LogItem;
import gcm.output.simstate.SimulationStatusItem;
import gcm.scenario.ExperimentBuilder;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.MapOption;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.test.manual.demo.components.DeadCompartment;
import gcm.test.manual.demo.components.ExposedCompartment;
import gcm.test.manual.demo.components.Immunizer;
import gcm.test.manual.demo.components.InfectedCompartment;
import gcm.test.manual.demo.components.PopulationLoader;
import gcm.test.manual.demo.components.ProducerAlpha;
import gcm.test.manual.demo.components.ProducerBeta;
import gcm.test.manual.demo.components.ProducerGamma;
import gcm.test.manual.demo.components.StandardRegion;
import gcm.test.manual.demo.components.SusceptibleCompartment;
import gcm.test.manual.demo.datatypes.PopulationDescription;
import gcm.test.manual.demo.datatypes.PopulationDescription.Builder;
import gcm.test.manual.demo.datatypes.PopulationDescription.PopulationElement;
import gcm.test.manual.demo.identifiers.Compartment;
import gcm.test.manual.demo.identifiers.CompartmentProperty;
import gcm.test.manual.demo.identifiers.GlobalComponent;
import gcm.test.manual.demo.identifiers.GlobalProperty;
import gcm.test.manual.demo.identifiers.GroupProperty;
import gcm.test.manual.demo.identifiers.GroupType;
import gcm.test.manual.demo.identifiers.Material;
import gcm.test.manual.demo.identifiers.MaterialsProducers;
import gcm.test.manual.demo.identifiers.PersonProperty;
import gcm.test.manual.demo.identifiers.RandomGeneratorId;
import gcm.test.manual.demo.identifiers.RegionProperty;
import gcm.test.manual.demo.identifiers.Resource;
import gcm.test.manual.demo.identifiers.TractId;
import gcm.test.manual.demo.trigger.RegionResourceTrigger;
import gcm.test.manual.demo.trigger.RegionResourceTriggerComponent;
import gcm.test.manual.demo.trigger.TriggerContainer;

public class DemoRunnerSparkLike {

	private final Path tractsPath;
	private final Path populationPath;

	private DemoRunnerSparkLike(Path tractsPath, Path populationPath) {
		this.tractsPath = tractsPath;
		this.populationPath = populationPath;
	}

	private void defineProperties(ExperimentBuilder experimentBuilder) {

		for (RegionProperty regionProperty : RegionProperty.values()) {
			experimentBuilder.defineRegionProperty(regionProperty, regionProperty.getPropertyDefinition());
		}

		for (PersonProperty personProperty : PersonProperty.values()) {
			experimentBuilder.definePersonProperty(personProperty, personProperty.getPropertyDefinition());
		}

		for (GroupProperty groupProperty : GroupProperty.values()) {
			experimentBuilder.defineGroupProperty(groupProperty.getGroupType(), GroupProperty.SHARED_IMMUNITY, groupProperty.getPropertyDefinition());
		}

		for (GlobalProperty globalProperty : GlobalProperty.values()) {
			experimentBuilder.defineGlobalProperty(globalProperty, globalProperty.getPropertyDefinition());
		}

		for (CompartmentProperty compartmentProperty : CompartmentProperty.values()) {
			experimentBuilder.defineCompartmentProperty(compartmentProperty.getCompartment(), compartmentProperty, compartmentProperty.getPropertyDefinition());
		}
	}

	private void addIdentifiers(ExperimentBuilder experimentBuilder) {

		experimentBuilder.addCompartmentId(Compartment.SUSCEPTIBLE, SusceptibleCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.EXPOSED, ExposedCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.INFECTED, InfectedCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.DEAD, DeadCompartment.class);
		experimentBuilder.addGlobalComponentId(GlobalComponent.POPULATION_LOADER, PopulationLoader.class);
		experimentBuilder.addGlobalComponentId(GlobalComponent.IMMUNIZER, Immunizer.class);

		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_ALPHA, ProducerAlpha.class);
		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_BETA, ProducerBeta.class);
		experimentBuilder.addMaterialsProducerId(MaterialsProducers.PRODUCER_GAMMA, ProducerGamma.class);

		for (RandomGeneratorId randomGeneratorId : RandomGeneratorId.values()) {
			experimentBuilder.addRandomNumberGeneratorId(randomGeneratorId);
		}

		for (Resource resource : Resource.values()) {
			experimentBuilder.addResource(resource);
			// experimentBuilder.setResourceTimeTracking(resource,
			// TimeTrackingPolicy.TRACK_TIME);
		}

		for (Material material : Material.values()) {
			experimentBuilder.addMaterial(material);
		}

		for (GroupType groupType : GroupType.values()) {
			experimentBuilder.addGroupTypeId(groupType);
		}

	}

	private void addPropertyValues(ExperimentBuilder experimentBuilder) {
		
		Builder builder = PopulationDescription.builder();
		try {
			Files.readAllLines(populationPath).stream().skip(1).forEach(line -> {			
				String[] strings = line.split(",", -1);
				int age = Integer.parseInt(strings[0]);
				String homeId = strings[1];
				String schoolId = strings[2];
				String workPlaceId = strings[3];
				PopulationElement populationElement = new PopulationElement(age,homeId,schoolId,workPlaceId); 
				builder.addPopulationElement(populationElement);
			});
		} catch (IOException e) {
			throw new RuntimeException();
		}
		PopulationDescription populationDescription = builder.build();
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.POPULATION_DESCRIPTION, populationDescription);
		
		experimentBuilder.addCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD, 5d);

		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 3.7);
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 4.2);
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 5.1);
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 7.3);
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 9.4);
		experimentBuilder.addGlobalPropertyValue(GlobalProperty.ALPHA, 10.8);		
	}

	private List<String> addRegions(ExperimentBuilder experimentBuilder) throws IOException {

		List<String> result = new ArrayList<>();
		Set<String> regionStrings = Files.readAllLines(populationPath).stream().skip(1).map(line -> {
			String[] strings = line.split(",", -1);
			String homeId = strings[1];
			return homeId.substring(0, 11);
		}).collect(Collectors.toCollection(LinkedHashSet::new));

		Files.readAllLines(tractsPath).stream().skip(1).filter(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			return regionStrings.contains(name);
		}).forEach(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			result.add(name);
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			TractId tractId = new TractId(name);
			experimentBuilder.addRegionId(tractId, StandardRegion.class);
			experimentBuilder.addRegionPropertyValue(tractId, RegionProperty.LAT, lat);
			experimentBuilder.addRegionPropertyValue(tractId, RegionProperty.LON, lon);
			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_1, 1000);
			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_2, 10000);
			experimentBuilder.addRegionResourceLevel(tractId, Resource.RESOURCE_3, 1000);
		});
		return result;

	}

	private static class TriggerComponentId implements GlobalComponentId {
		private final String name;

		public TriggerComponentId(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private void workWithTriggers(ExperimentBuilder experimentBuilder, List<String> regionNames) {

		TriggerContainer.Builder builder = TriggerContainer.builder();

		for (String regionName : regionNames) {

			TractId tractId = new TractId(regionName);
			TriggerComponentId triggerComponentId = new TriggerComponentId(regionName);
			RegionResourceTrigger regionResourceTrigger = new RegionResourceTrigger(tractId, Resource.RESOURCE_1, 0.1);
			builder.addTrigger(triggerComponentId, regionResourceTrigger);
			experimentBuilder.addGlobalComponentId(triggerComponentId, RegionResourceTriggerComponent.class);
		}

		experimentBuilder.defineGlobalProperty(TriggerContainer.TRIGGER_CONTAINER, TriggerContainer.getTriggerContainerPropertyDefinition());
		experimentBuilder.addGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER, builder.build());
	}

	@SuppressWarnings("unused")
	private static class CustomLogItemHandler implements OutputItemHandler {

		@Override
		public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			// do nothing
		}

		@Override
		public void openExperiment(ExperimentProgressLog experimentProgressLog) {
			// do nothing
		}

		@Override
		public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			// do nothing
		}

		@Override
		public void closeExperiment() {
			// do nothing
		}

		@Override
		public void handle(OutputItem outputItem) {
			System.err.println("custom log item handler :" + outputItem.toString());
		}

		@Override
		public Set<Class<? extends OutputItem>> getHandledClasses() {
			Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
			result.add(LogItem.class);
			return result;
		}
	}

	private static class CustomSimulationStatusItemHandler implements OutputItemHandler {

		@Override
		public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			// do nothing
		}

		@Override
		public void openExperiment(ExperimentProgressLog experimentProgressLog) {
			// do nothing
		}

		@Override
		public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			// do nothing
		}

		@Override
		public void closeExperiment() {
			// do nothing
		}

		@Override
		public void handle(OutputItem outputItem) {
			System.err.println("custom simulation status handler :" + outputItem.toString());
		}

		@Override
		public Set<Class<? extends OutputItem>> getHandledClasses() {
			Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
			result.add(SimulationStatusItem.class);
			return result;
		}
	}

	@SuppressWarnings("unused")
	private static class CustomOutputItemHandlerSupplier implements Supplier<List<OutputItemHandler>> {

		@Override
		public List<OutputItemHandler> get() {
			List<OutputItemHandler> result = new ArrayList<>();
			result.add(new CustomSimulationStatusItemHandler());
			return result;
		}

	}

	private void execute() throws IOException {

		// build the experiment
		ExperimentBuilder experimentBuilder = new ExperimentBuilder();
		experimentBuilder.setBaseScenarioId(100);
		experimentBuilder.setRegionMapOption(MapOption.ARRAY);
		experimentBuilder.setCompartmentMapOption(MapOption.ARRAY);
		defineProperties(experimentBuilder);
		addIdentifiers(experimentBuilder);
		addPropertyValues(experimentBuilder);
		List<String> regionNames = addRegions(experimentBuilder);
		workWithTriggers(experimentBuilder, regionNames);
		Experiment experiment = experimentBuilder.build();

		// set the replication count
		int replicationCount = 10;

		// build meta level reporting
		SparkLikeMetaItemHandlerSupplier sparkLikeMetaItemHandlerSupplier = SparkLikeMetaItemHandlerSupplier.builder()//
																											.setProduceSimulationStatusOutput(true, experiment.getScenarioCount(), replicationCount)//
																											// .setLogItemHandler(new
																											// CustomLogItemHandler())
																											.build();//

		// build the experiment executor
		ExperimentExecutorSparkLike experimentExecutor = ExperimentExecutorSparkLike.builder()//
																					.setSeed(1234123512345234L)//
																					.setExperiment(experiment)//
																					.setReplicationCount(replicationCount)//
																					.setThreadCount(6)//
																					.addOuputItemSupplier(sparkLikeMetaItemHandlerSupplier)//
																					.build();//

		// run the experiment
		experimentExecutor.execute();
	}

	public static void main(String[] args) throws IOException {
		Path tractsPath = Paths.get(args[0]);
		Path populationPath = Paths.get(args[1]);
		new DemoRunnerSparkLike(tractsPath, populationPath).execute();
	}
}
