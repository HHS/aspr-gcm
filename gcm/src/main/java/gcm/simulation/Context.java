package gcm.simulation;

import java.util.ArrayList;
import java.util.List;

import gcm.output.OutputItemHandler;
import gcm.output.simstate.NIOMemoryReportItemHandler;
import gcm.output.simstate.NIOPlanningQueueReportItemHandler;
import gcm.output.simstate.ProfileItem;
import gcm.replication.Replication;
import gcm.scenario.Scenario;
import gcm.simulation.group.PersonGroupManger;
import gcm.simulation.group.PersonGroupMangerImpl;
import gcm.simulation.partition.PartitionManager;
import gcm.simulation.partition.PartitionManagerImpl;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;

/**
 * The Context is the core of the simulation. It is not exposed directly to the
 * modeler client or any contributed objects created by the modeler. The Context
 * manages all Elements through construction, initialization, finalization and
 * destruction. It acts as a gateway to resources for Elements that are authored
 * in GCM, but is NEVER exposed to any contributed objects.
 * 
 * Construction is managed through the contained builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = Simulation.class)
@NotThreadSafe
public final class Context implements MemoryReportParticipant {

	private Replication replication;
	private Scenario scenario;
	private StochasticsManager stochasticsManager;
	private ComponentManager componentManager;
	private ReportsManager reportsManager;
	private Environment environment;
	private MemoryReportManager memoryReportManager;
	private ObservationManager observationManager;
	private MaterialsManager materialsManager;
	private ResourceManager resourceManager;
	private PropertyDefinitionManager propertyDefinitionManager;
	private PropertyManager propertyManager;
	private PersonLocationManger personLocationManger;
	private PersonGroupManger personGroupManger;
	private EventManager eventManager;	
	private PartitionManager partitionManager;
	private ObservableEnvironment observableEnvironment;
	private PersonIdManager personIdManager;
	private ExternalAccessManager externalAccessManager;
	private MutationResolver mutationResolver;
	private List<OutputItemHandler> outputItemHandlers;
	private OutputItemManager outputItemManager;
	private ProfileManager profileManager;
	private PlanningQueueReportItemManager planningQueueReportItemManager;
	private SimulationWarningManager simulationWarningManager;
	private double memoryReportInterval;
	private boolean produceProfileItems;
	private long planningQueueReportThreshold;

	private Context(Scaffold scaffold) {
		/*
		 * Construct all of the parts of the simulation
		 */

		scenario = scaffold.scenario;
		replication = scaffold.replication;
		profileManager = new ProfileManager();
		outputItemManager = new OutputItemManagerImpl();
		simulationWarningManager = new SimulationWarningManagerImpl();
		componentManager = new ComponentManagerImpl();
		outputItemHandlers = new ArrayList<>(scaffold.outputItemHandlers);
		reportsManager = new ReportsManagerImpl();
		environment = new EnvironmentImpl();
		stochasticsManager = new StochasticsManagerImpl();
		observationManager = new ObservationManagerImpl();
		materialsManager = new MaterialsManagerImpl();
		resourceManager = new ResourceManagerImpl();
		propertyDefinitionManager = new PropertyDefinitionManagerImpl();
		propertyManager = new PropertyManagerImpl();
		personLocationManger = new PersonLocationMangerImpl();
		personGroupManger = new PersonGroupMangerImpl();
		eventManager = new EventManagerImpl();
		partitionManager = new PartitionManagerImpl();
		observableEnvironment = new ObservableEnvironmentImpl();
		personIdManager = new PersonIdManagerImpl();
		externalAccessManager = new ExternalAccessManagerImpl();
		mutationResolver = new MutationResolverImpl();
		planningQueueReportItemManager = new PlanningQueueReportItemManagerImpl();
		memoryReportManager = new MemoryReportManagerImpl();
	}

	/*
	 * Container class for the builder
	 */
	private static class Scaffold {
		private Scenario scenario;
		private Replication replication;
		private List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
	}

	/*
	 * Boolean to protect this Context from executing more than once
	 */
	private boolean started;

	public void execute() {
		// The life cycle does not support multiple executions
		if (started) {
			throw new IllegalStateException("The context may be executed only once");
		}
		started = true;

		/*
		 * Determine if we are producing profile items.
		 */
		produceProfileItems = false;

		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			produceProfileItems |= outputItemHandler.getHandledClasses().contains(ProfileItem.class);
		}

		/*
		 * Determine if we are producing memory reports
		 */
		memoryReportInterval = 0;
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			if (outputItemHandler instanceof NIOMemoryReportItemHandler) {
				NIOMemoryReportItemHandler nioMemoryReportItemHandler = (NIOMemoryReportItemHandler) outputItemHandler;
				memoryReportInterval = Math.max(memoryReportInterval, nioMemoryReportItemHandler.getMemoryReportInterval());
			}
		}

		/*
		 * Determine if we are producing planning queue reports
		 */
		planningQueueReportThreshold = 0;
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			if (outputItemHandler instanceof NIOPlanningQueueReportItemHandler) {
				NIOPlanningQueueReportItemHandler nioPlanningQueueReportItemHandler = (NIOPlanningQueueReportItemHandler) outputItemHandler;
				planningQueueReportThreshold = Math.max(planningQueueReportThreshold, nioPlanningQueueReportItemHandler.getPlanningQueueReportThreshold());
			}
		}

		/*
		 * The core structure of the simulation that will not be subject to
		 * mutation is built. Reporting is not engaged. At the end of init() the
		 * simulation is in a consistent state. Each Element relies on the
		 * scenario for data to reduce order dependencies.
		 */

		/*
		 * The Component Manager and Profile Manager need to be initialized in
		 * order and before any profile proxies are created. This is done to
		 * prevent feedback during the profiling process that can occur if the
		 * profile manager is calling proxies during its own functioning. This
		 * is a somewhat brittle, but simple solution and only works because the
		 * profile manager relies on the component manager not invoking a
		 * proxied instance.
		 */
		componentManager.init(this);
		profileManager.init(this);

		if (produceProfileItems) {
			environment = profileManager.getProfiledProxy(environment);
			scenario = profileManager.getProfiledProxy(scenario);
			replication = profileManager.getProfiledProxy(replication);
			propertyManager = profileManager.getProfiledProxy(propertyManager);
			reportsManager = profileManager.getProfiledProxy(reportsManager);
			personLocationManger = profileManager.getProfiledProxy(personLocationManger);
			personIdManager = profileManager.getProfiledProxy(personIdManager);
			observationManager = profileManager.getProfiledProxy(observationManager);
			mutationResolver = profileManager.getProfiledProxy(mutationResolver);
			eventManager = profileManager.getProfiledProxy(eventManager);
			stochasticsManager = profileManager.getProfiledProxy(stochasticsManager);
			propertyDefinitionManager = profileManager.getProfiledProxy(propertyDefinitionManager);
			externalAccessManager = profileManager.getProfiledProxy(externalAccessManager);
			resourceManager = profileManager.getProfiledProxy(resourceManager);
			materialsManager = profileManager.getProfiledProxy(materialsManager);
			partitionManager = profileManager.getProfiledProxy(partitionManager);			
			personGroupManger = profileManager.getProfiledProxy(personGroupManger);
			observableEnvironment = profileManager.getProfiledProxy(observableEnvironment);
			outputItemManager = profileManager.getProfiledProxy(outputItemManager);
			simulationWarningManager = profileManager.getProfiledProxy(simulationWarningManager);
			componentManager = profileManager.getProfiledProxy(componentManager);
			planningQueueReportItemManager = profileManager.getProfiledProxy(planningQueueReportItemManager);
			memoryReportManager = profileManager.getProfiledProxy(memoryReportManager);
		}

		/*
		 * Lock down access for the bulk of initialization
		 */
		externalAccessManager.init(this);
		externalAccessManager.acquireGlobalReadAccessLock();
		externalAccessManager.acquireGlobalWriteAccessLock();

		/*
		 * Initialize the various other elements and major parts of the
		 * simulation.
		 */
		outputItemManager.init(this);
		simulationWarningManager.init(this);
		environment.init(this);
		observableEnvironment.init(this);
		stochasticsManager.init(this);
		observationManager.init(this);
		materialsManager.init(this);
		resourceManager.init(this);
		propertyDefinitionManager.init(this);
		propertyManager.init(this);
		personLocationManger.init(this);
		personGroupManger.init(this);
		eventManager.init(this);
		partitionManager.init(this);
		personIdManager.init(this);
		planningQueueReportItemManager.init(this);
		memoryReportManager.init(this);

		/*
		 * Allow external access to the Environment. The simulation should now
		 * be in a consistent state, even though the scenario data is not fully
		 * loaded.
		 */
		externalAccessManager.releaseGlobalReadAccessLock();

		/*
		 * The mutation resolver is now initialized after all fixed/structural
		 * data has been loaded from the scenario. The remaining property
		 * values, people, groups, resources, batches and stages are now loaded
		 * by the mutation resolver.
		 */
		mutationResolver.init(this);

		/*
		 * Reports are activated
		 */
		reportsManager.init(this);

		/*
		 * Start the simulation. This allows Components to execute plans and
		 * react to observations.
		 */
		externalAccessManager.releaseGlobalWriteAccessLock();
		eventManager.execute();

		/*
		 * Close the various reporting mechanisms
		 */
		reportsManager.closeReports();
		profileManager.close();
		outputItemManager.close();
		planningQueueReportItemManager.close();

		/*
		 * Turn off external access to the Environment
		 */
		externalAccessManager.acquireGlobalWriteAccessLock();
	}
	
	
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for {@link Context}
	 * 
	 * @author Shawn Hatch
	 * 
	 */
	@NotThreadSafe
	public static class Builder {
		
		private Builder() {}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Constructs a Context from the collected data.
		 * 
		 * @throws RuntimeException
		 *             <li>if no scenario was set
		 *             <li>if no replication was set
		 */
		public Context build() {
			try {
				if (scaffold.scenario == null) {
					throw new RuntimeException("Scenario not set");
				}
				if (scaffold.replication == null) {
					throw new RuntimeException("Replication not set");
				}
				return new Context(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the {@link Replication}
		 * 
		 * @throws RuntimeException
		 *             if the replication is null
		 */
		public void setReplication(final Replication replication) {
			
			scaffold.replication = replication;
		}

		/**
		 * Add an {@link OutputItemHandler}
		 * 
		 * @throws RuntimeException
		 *             if the outputItemHandler is null
		 */
		public void addOutputItemHandler(OutputItemHandler outputItemHandler) {
			if (outputItemHandler == null) {
				throw new RuntimeException("Output Item Handler is null");
			}
			scaffold.outputItemHandlers.add(outputItemHandler);
		}

		/**
		 * Sets the {@link Scenario}
		 * 
		 * @throws RuntimeException
		 *             if the scenario is null
		 */
		public void setScenario(final Scenario scenario) {			
			scaffold.scenario = scenario;
		}
	}

	/**
	 * Returns the replication for the simulation instance
	 */
	public Replication getReplication() {
		return replication;
	}

	/**
	 * Returns the scenario for the simulation instance
	 */
	public Scenario getScenario() {
		return scenario;
	}

	/**
	 * Returns the component manager for the simulation instance
	 */
	public ComponentManager getComponentManager() {
		return componentManager;
	}

	/**
	 * Returns the environment for the simulation instance
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Returns the reports manager for the simulation instance
	 */
	public ReportsManager getReportsManager() {
		return reportsManager;
	}

	/**
	 * Returns the observation manager for the simulation instance
	 */
	public ObservationManager getObservationManager() {
		return observationManager;
	}

	/**
	 * Returns the event manager for the simulation instance
	 */
	public EventManager getEventManager() {
		return eventManager;
	}
	
	/**
	 * Returns the population partition manager for the simulation instance
	 */
	public PartitionManager getPartitionManager() {
		return partitionManager;
	}
	
	/**
	 * Returns the observable environment for the simulation instance
	 */
	public ObservableEnvironment getObservableEnvironment() {
		return observableEnvironment;
	}

	/**
	 * Returns the for the simulation instance
	 */
	public PropertyDefinitionManager getPropertyDefinitionsManager() {
		return propertyDefinitionManager;
	}

	/**
	 * Returns the resource manager for the simulation instance
	 */
	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	/**
	 * Returns the property manager for the simulation instance
	 */
	public PropertyManager getPropertyManager() {
		return propertyManager;
	}

	/**
	 * Returns the person location manager for the simulation instance
	 */
	public PersonLocationManger getPersonLocationManger() {
		return personLocationManger;
	}

	/**
	 * Returns the person group manager for the simulation instance
	 */
	public PersonGroupManger getPersonGroupManger() {
		return personGroupManger;
	}

	/**
	 * Returns the materials manager for the simulation instance
	 */
	public MaterialsManager getMaterialsManager() {
		return materialsManager;
	}

	/**
	 * Returns the stochastics for the simulation instance
	 */
	public StochasticsManager getStochasticsManager() {
		return stochasticsManager;
	}

	/**
	 * Returns the person id manager for the simulation instance
	 */
	public PersonIdManager getPersonIdManager() {
		return personIdManager;
	}

	/**
	 * Returns the mutation resolver for the simulation instance
	 */
	public MutationResolver getMutationResolver() {
		return mutationResolver;
	}

	/**
	 * Returns the external access manager for the simulation instance
	 */
	public ExternalAccessManager getExternalAccessManager() {
		return externalAccessManager;
	}

	/**
	 * Returns the output item handlers for the simulation instance
	 */
	public List<OutputItemHandler> getOutputItemHandlers() {
		return new ArrayList<>(outputItemHandlers);
	}

	/**
	 * Returns the output item manager for the simulation instance
	 */
	public OutputItemManager getOutputItemManager() {
		return outputItemManager;
	}

	/**
	 * Returns the warning manager for the simulation instance
	 */
	public SimulationWarningManager getSimulationWarningManager() {
		return simulationWarningManager;
	}

	/**
	 * Returns the profile manager for the simulation instance
	 */
	public ProfileManager getProfileManager() {
		return profileManager;
	}

	/**
	 * Returns the planning queue report item manager for the simulation
	 * instance
	 */
	public PlanningQueueReportItemManager getPlanningQueueReportItemManager() {
		return planningQueueReportItemManager;
	}

	/**
	 * Returns the memory report manager for the simulation instance
	 */
	public MemoryReportManager getMemoryReportManager() {
		return memoryReportManager;
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		memoryPartition.addMemoryLink(this, replication, "Replication");
		memoryPartition.addMemoryLink(this, scenario, "Scenario");
		memoryPartition.addMemoryLink(this, stochasticsManager, "Stochastics Manager");
		memoryPartition.addMemoryLink(this, componentManager, "Component Manager");
		componentManager.collectMemoryLinks(memoryPartition);
		memoryPartition.addMemoryLink(this, reportsManager, "Reports Manager");
		memoryPartition.addMemoryLink(this, environment, "Environment");
		memoryPartition.addMemoryLink(this, memoryReportManager, "Memory Report Manager");
		memoryPartition.addMemoryLink(this, observationManager, "Observation Manager");
		memoryPartition.addMemoryLink(this, materialsManager, "Materials Manager");
		memoryPartition.addMemoryLink(this, resourceManager, "Resource Manager");
		memoryPartition.addMemoryLink(this, propertyDefinitionManager, "Property Definition Manager");
		memoryPartition.addMemoryLink(this, propertyManager, "Property Manager");
		propertyManager.collectMemoryLinks(memoryPartition);
		memoryPartition.addMemoryLink(this, personLocationManger, "Person Location Manger");
		memoryPartition.addMemoryLink(this, personGroupManger, "Person Group Manger");
		memoryPartition.addMemoryLink(this, eventManager, "Event Manager");
		memoryPartition.addMemoryLink(this, partitionManager, "Filtered Partition Manager");		
		memoryPartition.addMemoryLink(this, observableEnvironment, "Observable Environment");
		memoryPartition.addMemoryLink(this, personIdManager, "Person Id Manager");
		memoryPartition.addMemoryLink(this, externalAccessManager, "External Access Manager");
		memoryPartition.addMemoryLink(this, mutationResolver, "Mutation Resolver");
		memoryPartition.addMemoryLink(this, outputItemManager, "Output Item Manager");
		outputItemManager.collectMemoryLinks(memoryPartition);
		memoryPartition.addMemoryLink(this, profileManager, "Profile Manager");
		memoryPartition.addMemoryLink(this, planningQueueReportItemManager, "Planning Queue Report Item Manager");
		memoryPartition.addMemoryLink(this, simulationWarningManager, "Warning Manager");

	}

	/**
	 * Returns true if and only if the simulation should produce profile items
	 */
	public boolean produceProfileItems() {
		return produceProfileItems;
	}

	/**
	 * Returns the report interval for memory reporting. A non-positive value
	 * indicates that reporting is turned off.
	 */
	public double getMemoryReportInterval() {
		return memoryReportInterval;
	}

	/**
	 * Returns the number of planning queue additions/removals are needed to
	 * trigger planning queue reporting. A non-positive value indicates that
	 * reporting is turned off.
	 */
	public long getPlanningQueueReportThreshold() {
		return planningQueueReportThreshold;
	}
	
//	public MemSizer getContextFreeMemSizer() {
//		MemSizer result=  new MemSizer(false);
//		
//		result.excludeObject(this);
//		result.excludeObject(replication);
//		result.excludeObject(scenario);
//		result.excludeObject(stochasticsManager);
//		result.excludeObject(componentManager);
//		result.excludeObject(reportsManager);
//		result.excludeObject(environment);
//		result.excludeObject(memoryReportManager);
//		result.excludeObject(observationManager);
//		result.excludeObject(materialsManager);
//		result.excludeObject(resourceManager);
//		result.excludeObject(propertyDefinitionManager);
//		result.excludeObject(propertyManager);
//		result.excludeObject(personLocationManger);
//		result.excludeObject(personGroupManger);
//		result.excludeObject(eventManager);
//		result.excludeObject(partitionManager);
//		result.excludeObject(observableEnvironment);
//		result.excludeObject(personIdManager);
//		result.excludeObject(externalAccessManager);
//		result.excludeObject(mutationResolver);
//		for(OutputItemHandler outputItemHandler :outputItemHandlers) {
//			result.excludeObject(outputItemHandler);
//		}
//		result.excludeObject(outputItemManager);
//		result.excludeObject(profileManager);
//		result.excludeObject(planningQueueReportItemManager);
//		result.excludeObject(simulationWarningManager);
//		
//		return result;
//	}

}
