package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.components.Component;
import gcm.scenario.CompartmentId;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.InternalComponentId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.RegionId;
import gcm.scenario.Scenario;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor for {@link ComponentManager}
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class ComponentManagerImpl extends BaseElement implements ComponentManager {

	private static class ComponentRecord {

		private Component component;

		private final ComponentType componentType;

		private final ComponentId componentId;

		public ComponentRecord(final Component component, final ComponentType componentType, final ComponentId componentId) {
			this.component = component;
			this.componentType = componentType;
			this.componentId = componentId;
		}

	}

	/*
	 * Produce a ComponentId that can be used when no Component has focus and
	 * that is guaranteed to not match any other ComponentId.
	 */
	private static final ComponentId GCM = new ComponentId() {
		@Override
		public String toString() {
			return "GCM";
		}
	};

	private ProfileManager profileManager;

	private ComponentRecord focalComponentRecord;

	private final Map<ComponentId, ComponentRecord> componentRecords = new LinkedHashMap<>();

	@Override
	public void clearFocus() {
		setFocus(GCM);
	}

	@Override
	public void collectMemoryLinks(final MemoryPartition memoryPartition) {
		for (final ComponentId componentId : componentRecords.keySet()) {
			final ComponentRecord componentRecord = componentRecords.get(componentId);
			if ((componentRecord.component != null) && (componentRecord.componentType != ComponentType.INTERNAL)) {
				memoryPartition.addMemoryLink(this, componentRecord.component, componentId.toString());
			}
		}
	}

	/*
	 * When profile reporting is turned on, we need to wrap the component with a
	 * proxy class instance so that profile reporting is engaged around the
	 * component.
	 */
	private Component getComponent(final Class<? extends Component> componentClass) {

		try {
			return  componentClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Component class " + componentClass.getName() + " could not be instantiated.  This is likely due to the class not having an empty, public constructor.", e);
		}
	}

	@Override
	public Set<ComponentId> getComponentIds() {
		return new LinkedHashSet<>(componentRecords.keySet());
	}

	@Override
	public Component getFocalComponent() {
		return focalComponentRecord.component;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ComponentId> T getFocalComponentId() {
		return (T) focalComponentRecord.componentId;
	}

	@Override
	public ComponentType getFocalComponentType() {
		return focalComponentRecord.componentType;
	}

	/*
	 * Create an InternalComponentId for a memory report supporting component if
	 * one is needed.
	 */
	private InternalComponentId getMemoryReportComponentId() {
		return new InternalComponentId() {
			@Override
			public String toString() {
				return "Memory Report Internal ComponentId";
			}
		};
	}

	@Override
	public void init(final Context context) {
		super.init(context);

		profileManager = context.getProfileManager();

		final Scenario scenario = context.getScenario();
		
		/*
		 * ORDER MUST BE GLOBAL, REGION, COMPARTMENT, MATERIAL IN ORDER TO
		 * SATISFY CONTRACT REQUIREMENTS ON THE ORDER OF COMPONENT INITIAL
		 * ACTIVATIONS.
		 */

		for (final GlobalComponentId globalComponentId : scenario.getGlobalComponentIds()) {
			final Class<? extends Component> componentClass = scenario.getGlobalComponentClass(globalComponentId);
			final Component component = getComponent(componentClass);
			componentRecords.put(globalComponentId, new ComponentRecord(component, ComponentType.GLOBAL, globalComponentId));
		}
		for (final RegionId regionId : scenario.getRegionIds()) {
			final Class<? extends Component> componentClass = scenario.getRegionComponentClass(regionId);
			final Component component = getComponent(componentClass);
			componentRecords.put(regionId, new ComponentRecord(component, ComponentType.REGION, regionId));
		}

		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			final Class<? extends Component> componentClass = scenario.getCompartmentComponentClass(compartmentId);
			final Component component = getComponent(componentClass);
			componentRecords.put(compartmentId, new ComponentRecord(component, ComponentType.COMPARTMENT, compartmentId));
		}

		for (final MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			final Class<? extends Component> componentClass = scenario.getMaterialsProducerComponentClass(materialsProducerId);
			final Component component = getComponent(componentClass);
			componentRecords.put(materialsProducerId, new ComponentRecord(component, ComponentType.MATERIALS_PRODUCER, materialsProducerId));
		}

		/*
		 * Add an internal component if memory reporting is active
		 */

		if (context.getMemoryReportInterval() > 0) {
			final MemoryReportComponent memoryReportComponent = new MemoryReportComponent(context);
			final InternalComponentId memoryReportComponentId = getMemoryReportComponentId();
			componentRecords.put(memoryReportComponentId, new ComponentRecord(memoryReportComponent, ComponentType.INTERNAL, memoryReportComponentId));
		}

		/*
		 * Add a placeholder element -- we know that it cannot clash with
		 * modeler provided identifiers since it does not implement any of the
		 * required marker interfaces.
		 */
		componentRecords.put(GCM, new ComponentRecord(null, ComponentType.SIM, GCM));

		// initialize the focus
		setFocus(GCM);

	}
	@Override
	public void wrapComponentsInProxies() {
		for(ComponentRecord componentRecord : componentRecords.values()) {
			if(componentRecord.component != null) {
				componentRecord.component = profileManager.getProfiledProxy(componentRecord.component);						
			}			
		}
	}
	
	@Override
	public void setFocus(final ComponentId componentId) {
		final ComponentRecord componentRecord = componentRecords.get(componentId);
		if (componentRecord == null) {
			throw new RuntimeException("unknown component id " + componentId);
		}
		focalComponentRecord = componentRecord;
	}

	@Override
	public void addGlobalComponent(GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass) {
		//TODO -- proxy wrapping not addressed
		final Component component = getComponent(globalComponentClass);
		componentRecords.put(globalComponentId, new ComponentRecord(component, ComponentType.GLOBAL, globalComponentId));
	}
}
