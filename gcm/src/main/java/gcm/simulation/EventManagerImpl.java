package gcm.simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import gcm.components.Component;
import gcm.output.reports.GroupInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class EventManagerImpl extends BaseElement implements EventManager {
	/*
	 * Each plan that is added is given a plan id value, incrementing the
	 * masterPlanId. The plan id values are used to resolve ties in the priority
	 * of the planQueue.
	 */
	private long masterPlanId;

	/*
	 * A data class for managing the plans submitted by components.
	 *
	 * The plan created by the component is not understood by the Environment
	 * per se. Instead, it records the focalId (the identifier of the component)
	 * and the key for the plan. This plan key is prepended with the focalId so
	 * that it is impossible for a component to effect another components plans.
	 * The planTime is the future time when the plan is sent back to the
	 * component and is the driver to progress time in GCM. PlanRecords are
	 * stored in a priority queue based on the plan time. When plans are
	 * cancelled the plan is set to null.When a cancelled plan pops out of the
	 * queue, it is ignored.
	 */
	private static class PlanRecord {
		private final ComponentId componentId;
		private final Object key;
		private Plan plan;
		private final double planTime;
		private final long planId;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PlanRecord [componentId=");
			builder.append(componentId);
			builder.append(", key=");
			builder.append(key);
			builder.append(", plan=");
			builder.append(plan);
			builder.append(", planTime=");
			builder.append(planTime);
			builder.append(", planId=");
			builder.append(planId);
			builder.append("]");
			return builder.toString();
		}

		public PlanRecord(final ComponentId componentId, final Object key, final Plan plan, final double planTime, final long planId) {
			super();
			this.key = key;
			this.componentId = componentId;
			this.plan = plan;
			this.planTime = planTime;
			this.planId = planId;
		}

	}

	private ComponentManager componentManager;

	private Environment environment;

	private ObservationManager observationManager;

	private PlanningQueueReportItemManager planningQueueReportItemManager;

	@Override
	public void init(Context context) {
		super.init(context);
		this.componentManager = context.getComponentManager();
		this.environment = context.getEnvironment();
		this.observationManager = context.getObservationManager();
		this.planningQueueReportItemManager = context.getPlanningQueueReportItemManager();
	}

	/*
	 * Boolean used to prevent execute() from being invoked more than once.
	 */
	private boolean started;

	@Override
	public void execute() {
		if (started) {
			throw new IllegalStateException("The simulation may be executed only once");
		}
		started = true;
		componentManager.clearFocus();

		/*
		 * We now initialize the global components, regions and compartments in
		 * the order they were loaded from the component factory. Note that
		 * there are placeholder Component Ids that have no corresponding
		 * Component.
		 */
		for (ComponentId componentId : componentManager.getComponentIds()) {
			componentManager.setFocus(componentId);
			Component focalComponent = componentManager.getFocalComponent();
			if (focalComponent != null) {
				focalComponent.init(environment);
			}
			componentManager.clearFocus();
		}

		/*
		 * Although we generally only expect plans and observations to have been
		 * created during initialization, it is possible that some components
		 * may have altered property states that may need to be observed by
		 * other components.
		 */
		executeInitQueue();
		executeObservationQueue();
		
		/*
		 * The flow of time is determined by the progress of planning. Note that
		 * plans that are cancelled do not move time forward.
		 */

		while (processEvents && !planQueue.isEmpty()) {
			final PlanRecord planRecord = planQueue.poll();
			if (planRecord.plan != null) {
				time = planRecord.planTime;
				executePlan(planRecord);
				executeInitQueue();
				executeObservationQueue();
			}
		}

		for (ComponentId componentId : componentManager.getComponentIds()) {
			componentManager.setFocus(componentId);
			Component focalComponent = componentManager.getFocalComponent();
			if (focalComponent != null) {
				focalComponent.close(environment);
			}
			componentManager.clearFocus();
		}

		componentManager.clearFocus();

		processEvents = false;

	}

	@Override
	public boolean isActive() {
		return processEvents && !planQueue.isEmpty();
	}

	/*
	 * Clears out the observation queue after a plan has been executed. Time has
	 * not moved forward since the execution of the plan and we are giving other
	 * components a chance to react to whatever may have been observed. Note
	 * that for each observation in the observation queue, we change the focus
	 * to the component that is observing and that component can in turn act
	 * immediately or plan for future action. Thus the observation queue can
	 * grow while we are processing it and we do not leave this method until the
	 * queue is exhausted.
	 */
	private void executeObservationQueue() {
		/*
		 * Keep stimulating components with observations until there is nothing
		 * left to observe.
		 */

		while (processEvents) {
			final ObservationRecord observationRecord = observationManager.getNextObservation();
			if (observationRecord == null) {
				break;
			}

			/*
			 * First, switch the focus to the component that will be sent the
			 * observation
			 */
			componentManager.setFocus(observationRecord.getComponentId());

			final Component component = componentManager.getFocalComponent();
			final ObservationType observationType = observationRecord.getArgument(0);

			RegionId regionId;
			MaterialsProducerId materialsProducerId;
			MaterialsProducerId materialsProducerId2;
			CompartmentId compartmentId;
			PersonId personId;
			MaterialsProducerPropertyId materialsProducerPropertyId;
			ResourcePropertyId resourcePropertyId;
			PersonPropertyId personPropertyId;
			CompartmentPropertyId compartmentPropertyId;
			RegionPropertyId regionPropertyId;
			GlobalPropertyId globalPropertyId;
			ResourceId resourceId;
			StageId stageId;
			GroupId groupId;
			GroupInfo groupInfo;
			GroupPropertyId groupPropertyId;
			Object key;

			/*
			 * Based on the type of event being observed, unpack the
			 * observationRecord.observedInfoKey and convert its elements into
			 * the arguments matching the appropriate observation method of the
			 * component interface.
			 */

			switch (observationType) {
			case COMPARTMENT_PERSON_ARRIVAL:
				personId = observationRecord.getArgument(1);
				component.observeCompartmentPersonArrival(environment, personId);
				break;
			case COMPARTMENT_PERSON_DEPARTURE:
				compartmentId = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observeCompartmentPersonDeparture(environment, compartmentId, personId);
				break;
			case COMPARTMENT_PROPERTY:
				compartmentId = observationRecord.getArgument(1);
				compartmentPropertyId = observationRecord.getArgument(2);
				component.observeCompartmentPropertyChange(environment, compartmentId, compartmentPropertyId);
				break;
			case GLOBAL_PERSON_ARRIVAL:
				personId = observationRecord.getArgument(1);
				component.observeGlobalPersonArrival(environment, personId);
				break;
			case GLOBAL_PERSON_DEPARTURE:
				personId = observationRecord.getArgument(1);
				component.observeGlobalPersonDeparture(environment, personId);
				break;
			case GLOBAL_PROPERTY:
				globalPropertyId = observationRecord.getArgument(1);
				component.observeGlobalPropertyChange(environment, globalPropertyId);
				break;
			case PERSON_COMPARTMENT:
				personId = observationRecord.getArgument(1);
				component.observePersonCompartmentChange(environment, personId);
				break;
			case PERSON_PROPERTY:
				personId = observationRecord.getArgument(1);
				personPropertyId = observationRecord.getArgument(2);
				component.observePersonPropertyChange(environment, personId, personPropertyId);
				break;
			case PERSON_REGION:
				personId = observationRecord.getArgument(1);
				component.observePersonRegionChange(environment, personId);
				break;
			case PERSON_RESOURCE:
				personId = observationRecord.getArgument(1);
				resourceId = observationRecord.getArgument(2);
				component.observePersonResourceChange(environment, personId, resourceId);
				break;
			case REGION_PERSON_ARRIVAL:
				personId = observationRecord.getArgument(1);
				component.observeRegionPersonArrival(environment, personId);
				break;
			case REGION_PERSON_DEPARTURE:
				regionId = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observeRegionPersonDeparture(environment, regionId, personId);
				break;
			case REGION_PROPERTY:
				regionId = observationRecord.getArgument(1);
				regionPropertyId = observationRecord.getArgument(2);
				component.observeRegionPropertyChange(environment, regionId, regionPropertyId);
				break;
			case REGION_RESOURCE:
				regionId = observationRecord.getArgument(1);
				resourceId = observationRecord.getArgument(2);
				component.observeRegionResourceChange(environment, regionId, resourceId);
				break;
			case RESOURCE_PROPERTY:
				resourceId = observationRecord.getArgument(1);
				resourcePropertyId = observationRecord.getArgument(2);
				component.observeResourcePropertyChange(environment, resourceId, resourcePropertyId);
				break;
			case MATERIALS_PRODUCER_PROPERTY:
				materialsProducerId = observationRecord.getArgument(1);
				materialsProducerPropertyId = observationRecord.getArgument(2);
				component.observeMaterialsProducerPropertyChange(environment, materialsProducerId, materialsProducerPropertyId);
				break;
			case STAGE_OFFER:
				stageId = observationRecord.getArgument(1);
				component.observeStageOfferChange(environment, stageId);
				break;
			case STAGE_TRANSFER:
				stageId = observationRecord.getArgument(1);
				materialsProducerId = observationRecord.getArgument(2);
				materialsProducerId2 = observationRecord.getArgument(3);
				component.observeStageTransfer(environment, stageId,materialsProducerId,materialsProducerId2);
				break;
			case MATERIALS_PRODUCER_RESOURCE:
				materialsProducerId = observationRecord.getArgument(1);
				resourceId = observationRecord.getArgument(2);
				component.observeMaterialsProducerResourceChange(environment, materialsProducerId, resourceId);
				break;
			case GROUP_CONSTRUCTION:
				groupId = observationRecord.getArgument(1);
				component.observeGroupConstruction(environment, groupId);
				break;
			case GROUP_DESTRUCTION:
				groupInfo = observationRecord.getArgument(1);
				component.observeGroupDestruction(environment, groupInfo);
				break;
			case GROUP_PERSON_ARRIVAL:
				groupId = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observeGroupPersonArrival(environment, groupId, personId);
				break;
			case GROUP_PERSON_DEPARTURE:
				groupId = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observeGroupPersonDeparture(environment, groupId, personId);
				break;
			case GROUP_PROPERTY:
				groupId = observationRecord.getArgument(1);
				groupPropertyId = observationRecord.getArgument(2);
				component.observeGroupPropertyChange(environment, groupId, groupPropertyId);
				break;
			case POPULATION_INDEX_PERSON_ADDITION:
				key = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observePopulationIndexPersonAddition(environment, key, personId);
				break;
			case POPULATION_INDEX_PERSON_REMOVAL:
				key = observationRecord.getArgument(1);
				personId = observationRecord.getArgument(2);
				component.observePopulationIndexPersonRemoval(environment, key, personId);
				break;				
			default:
				throw new RuntimeException("unhandled observation case " + observationType);
			}
			/*
			 * The component is done and we clear the focus.
			 */
			componentManager.clearFocus();
		}
	}

	/*
	 * Stimulates the current focus component to execute a plan that has come
	 * due.
	 */
	private void executePlan(final PlanRecord planRecord) {

		/*
		 * We are going to stimulate a component, so we record the focus
		 */
		componentManager.setFocus(planRecord.componentId);

		/*
		 * We are releasing custody of the plan, so we remove it from our
		 * tracking map
		 */
		if (planRecord.key != null) {
			planMap.get(planRecord.componentId).remove(planRecord.key);
		}

		/*
		 * Determine the component from the current focal id
		 */
		final Component component = componentManager.getFocalComponent();

		/*
		 *  
		 */
		if (planningQueueReportItemManager.isActive()) {
			planningQueueReportItemManager.reportPlanningQueueRemoval(planRecord.componentId, planRecord.plan, planRecord.key);
		}

		/*
		 * Instruct the component to execute the plan. Note that the component
		 * is free to do with the plan anything that it wants and that we cannot
		 * assume any action on its part.
		 */

		component.executePlan(environment, planRecord.plan);

		/*
		 * Now that the component is done we clear the focus
		 */
		componentManager.clearFocus();
	}

	/*
	 * Boolean that controls the continued execution of plans and observations.
	 */
	private boolean processEvents = true;

	/*
	 * The planQueue and the planMap contain an identical set of records and
	 * those records do not contain past plans.The planQueue sorts the plans by
	 * ascending planning time and arrival order and thus represent the intended
	 * order of execution for plans. Plans drive the flow of time and time is
	 * moved forward by setting it to the scheduled time of the plan at the head
	 * of the queue. The planQueue is non-performant for retrieving or canceling
	 * plans, so the planMap is used for these functions.
	 */
	private PriorityQueue<PlanRecord> planQueue = new PriorityQueue<>(new Comparator<PlanRecord>() {
		/*
		 * We sort by plan time ascending. Ties are broken by the planId, which
		 * is incremented as plans are added.
		 */
		@Override
		public int compare(final PlanRecord plan1, final PlanRecord plan2) {
			int result = Double.compare(plan1.planTime, plan2.planTime);
			if (result == 0) {
				result = Long.compare(plan1.planId, plan2.planId);
			}
			return result;
		}
	});

	/*
	 *
	 * The planQueue and the planMap contain an identical set of identified plan
	 * records and do not contain past plans. Plan's that are not associated
	 * with an identifier are not contained in the planMap and are not
	 * retrievable or removable. The planMap allows for the O(1) retrieval and
	 * cancellation of plans, but does not contain those records in the order of
	 * their execution.
	 */
	private Map<ComponentId, Map<Object, PlanRecord>> planMap = new LinkedHashMap<>();

	/*
	 * The current time in the simulation. It is measured in days and start at
	 * zero. It is updated by the progression of plans.
	 */
	private double time;

	@Override
	public void halt() {
		processEvents = false;
	}

	@Override
	public double getTime() {
		return time;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Plan> T getPlan(final Object key) {
		T t = null;
		Map<Object, PlanRecord> map = planMap.get(componentManager.getFocalComponentId());
		if (map != null) {
			final PlanRecord planRecord = map.get(key);
			if (planRecord != null) {
				t = (T) planRecord.plan;
			}
		}
		return t;
	}

	@Override
	public double getPlanTime(final Object key) {
		Map<Object, PlanRecord> map = planMap.get(componentManager.getFocalComponentId());
		double result = -1;
		if (map != null) {
			final PlanRecord planRecord = map.get(key);
			if (planRecord != null) {
				result = planRecord.planTime;
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> removePlan(final Object key) {

		/*
		 * We drop the plan out of the plan map and thus have no way to
		 * reference the plan directly. However, we do not remove the plan from
		 * the planQueue and instead simply mark the plan record as cancelled.
		 * When the cancelled plan record reaches the top of the queue, it is
		 * popped off and ignored. This avoids the inefficiency of walking the
		 * queue and removing the plan.
		 *
		 * Note that we are allowing components to delete plans that do not
		 * exist. This was done to ease any bookkeeping burdens on the component
		 * and seems generally harmless.
		 */
		ComponentId focalComponentId = componentManager.getFocalComponentId();
		Map<Object, PlanRecord> map = planMap.get(focalComponentId);
		T result = null;
		if (map != null) {
			final PlanRecord planRecord = map.remove(key);
			if (planRecord != null) {
				if (planningQueueReportItemManager.isActive()) {
					planningQueueReportItemManager.reportPlanningQueueCancellation(focalComponentId, planRecord.plan, key);
				}
				result = (T) planRecord.plan;
				planRecord.plan = null;
			}
		}

		if (result == null) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public void addPlan(final Plan plan, final double planTime, final Object key) {

		PlanRecord planRecord;
		if (key != null) {
			/*
			 * Make sure that the component is not registering a plan on top of
			 * an existing plan. We have chosen to throw an exception if this
			 * happens rather than overwrite the plan, forcing the component to
			 * explicitly remove the existing plan first
			 */
			ComponentId focalComponentId = componentManager.getFocalComponentId();
			Map<Object, PlanRecord> map = planMap.get(focalComponentId);
			if (map != null) {
				if (map.containsKey(key)) {
					throw new RuntimeException("Plan already exists for key = " + key);
				}
			}

			/*
			 * Finally, if it all looks good, build the plan record and put it
			 * in on the planMap and the planQueue. The planMap allows us to
			 * remove/retrieve plans and the planQueue allows us to return plans
			 * back to their creators when the scheduled time occurs.
			 */

			planRecord = new PlanRecord(focalComponentId, key, plan, planTime, masterPlanId++);
			if (planningQueueReportItemManager.isActive()) {
				planningQueueReportItemManager.reportPlanningQueueAddition(focalComponentId, plan, key);
			}

			if (map == null) {
				map = new LinkedHashMap<>();
				planMap.put(focalComponentId, map);
			}
			map.put(planRecord.key, planRecord);
		} else {
			/*
			 * The plan will not be retrievable via the plan map.
			 */
			planRecord = new PlanRecord(componentManager.getFocalComponentId(), null, plan, planTime, masterPlanId++);
			if (planningQueueReportItemManager.isActive()) {
				planningQueueReportItemManager.reportPlanningQueueAddition(componentManager.getFocalComponentId(), plan, key);
			}
		}
		planQueue.add(planRecord);

	}

	@Override
	public List<Object> getPlanKeys() {
		ComponentId focalComponentId = componentManager.getFocalComponentId();
		Map<Object, PlanRecord> map = planMap.get(focalComponentId);
		return new ArrayList<>(map.keySet());
	}
	
	private void executeInitQueue() {
		if(globalComponentInitQueue.isEmpty()) {
			return;
		}
		for(GlobalComponentId globalComponentId : globalComponentInitQueue) {
			componentManager.setFocus(globalComponentId);
			Component focalComponent = componentManager.getFocalComponent();
			focalComponent.init(environment);
		}
		globalComponentInitQueue.clear();
	}
	

	private Set<GlobalComponentId> globalComponentInitQueue = new LinkedHashSet<>();

	@Override
	public void initGlobalComponent(GlobalComponentId globalComponentId) {
		globalComponentInitQueue.add(globalComponentId);
	}

}
