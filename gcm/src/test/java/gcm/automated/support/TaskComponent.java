package gcm.automated.support;

import java.util.List;

import gcm.components.Component;
import gcm.output.reports.GroupInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.ComponentId;
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
import gcm.simulation.Environment;
import gcm.simulation.ObservationType;
import gcm.simulation.Plan;
import gcm.util.MultiKey;

/**
 * Component implementor for all tests.
 *
 * The general @Test life cycle is to create a scenario, a replication and
 * possible some output item handlers and execute a simulation using them. Each
 * of the components added to the scenario should be TaskComponents. A
 * TaskPlanContainer is added to the scenario as a global property value and the
 * test fills this container with TaskPlans that are slated for specific
 * components to execute at specific times.
 * 
 * Each TaskComponent retrieves the list of TaskPlans from the TaskPlanContainer
 * during its init() method. Using the planning system, the component schedules
 * each TaskPlan for eventual execution. Each TaskPlan contains execution logic
 * that is contained in the unit test method.
 *
 * Task Plans often contain assertions that are executed during the simulation
 * run. However, it is impractical to test observations this way. The component
 * implements all of the observation methods of Component by recording the
 * observation and its time value in a MultiKey instance. These are retrievable
 * after the simulation run completes and are used in assertions on what was and
 * was not observed. Observations are collected in an ObservationContainer that
 * is optionally supplied by the test via a global property value. If the
 * execution logic of the Tasks in the test include registration for
 * observations, then the ObservationContainer must be present.
 */
public class TaskComponent implements Component {

	private ComponentId id;
	private ObservationContainer observationContainer;

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		final TaskPlan taskPlan = (TaskPlan) plan;
		taskPlan.executeTask(environment);
	}

	@Override
	public void init(final Environment environment) {
		this.id = environment.getCurrentComponentId();
		TaskPlanContainer taskPlanContainer = environment.getGlobalPropertyValue(TestGlobalPropertyId.TASK_PLAN_CONTAINER_PROPERTY_ID);
		List<TaskPlan> taskPlans = taskPlanContainer.getTaskPlans(id);

		for (final TaskPlan taskPlan : taskPlans) {
			if (taskPlan.getKey() != null) {
				environment.addPlan(taskPlan, taskPlan.getScheduledTime(), taskPlan.getKey());
			}else {
				environment.addPlan(taskPlan, taskPlan.getScheduledTime());
			}
		}

		if (environment.getGlobalPropertyIds().contains(TestGlobalPropertyId.OBSERVATION_CONTAINER_PROPERTY_ID)) {
			observationContainer = environment.getGlobalPropertyValue(TestGlobalPropertyId.OBSERVATION_CONTAINER_PROPERTY_ID);
		}
	}

	@Override
	public void observeCompartmentPersonArrival(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.COMPARTMENT_PERSON_ARRIVAL, personId));
	}

	@Override
	public void observeCompartmentPersonDeparture(final Environment environment, final CompartmentId compartmentId, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.COMPARTMENT_PERSON_DEPARTURE, compartmentId, personId));
	}

	@Override
	public void observeCompartmentPropertyChange(final Environment environment, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Object compartmentPropertyValue = environment.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.COMPARTMENT_PROPERTY, compartmentId, compartmentPropertyId, compartmentPropertyValue));
	}

	@Override
	public void observeGlobalPersonArrival(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GLOBAL_PERSON_ARRIVAL, personId));
	}

	@Override
	public void observeGlobalPersonDeparture(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GLOBAL_PERSON_DEPARTURE, personId));
	}

	@Override
	public void observeGlobalPropertyChange(final Environment environment, final GlobalPropertyId globalPropertyId) {
		final Object globalPropertyValue = environment.getGlobalPropertyValue(globalPropertyId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GLOBAL_PROPERTY, globalPropertyId, globalPropertyValue));
	}

	@Override
	public void observePersonCompartmentChange(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PERSON_COMPARTMENT, personId));
	}

	@Override
	public void observePersonPropertyChange(final Environment environment, final PersonId personId, final PersonPropertyId personPropertyId) {
		final Object personPropertyValue = environment.getPersonPropertyValue(personId, personPropertyId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PERSON_PROPERTY, personId, personPropertyId, personPropertyValue));
	}

	@Override
	public void observePersonRegionChange(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PERSON_REGION, personId));
	}

	@Override
	public void observePersonResourceChange(final Environment environment, final PersonId personId, final ResourceId resourceId) {
		final long personResourceLevel = environment.getPersonResourceLevel(personId, resourceId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PERSON_RESOURCE, personId, resourceId, personResourceLevel));
	}

	@Override
	public void observeRegionPersonArrival(final Environment environment, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.REGION_PERSON_ARRIVAL, personId));
	}

	@Override
	public void observeRegionPersonDeparture(final Environment environment, final RegionId regionId, final PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.REGION_PERSON_DEPARTURE, regionId, personId));
	}

	@Override
	public void observeRegionPropertyChange(final Environment environment, final RegionId regionId, final RegionPropertyId RegionPropertyId) {
		final Object regionPropertyValue = environment.getRegionPropertyValue(regionId, RegionPropertyId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.REGION_PROPERTY, regionId, RegionPropertyId, regionPropertyValue));
	}

	@Override
	public void observeRegionResourceChange(final Environment environment, final RegionId regionId, final ResourceId resourceId) {
		final long regionResourceLevel = environment.getRegionResourceLevel(regionId, resourceId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.REGION_RESOURCE, regionId, resourceId, regionResourceLevel));
	}

	@Override
	public void observeResourcePropertyChange(final Environment environment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Object resourcePropertyValue = environment.getResourcePropertyValue(resourceId, resourcePropertyId);
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.RESOURCE_PROPERTY, resourceId, resourcePropertyId, resourcePropertyValue));
	}

	@Override
	public void observeMaterialsProducerPropertyChange(final Environment environment, MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		final Object materialsProducerPropertyValue = environment.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
		observationContainer.addObservation(
				new MultiKey(environment.getTime(), id, ObservationType.MATERIALS_PRODUCER_PROPERTY, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue));
	}

	@Override
	public void observeStageOfferChange(final Environment environment, StageId stageId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.STAGE_OFFER, stageId));
	}

	@Override
	public void observeStageTransfer(final Environment environment, StageId stageId, MaterialsProducerId sourceMaterialsProducerId, MaterialsProducerId destinationMaterialsProducerId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.STAGE_TRANSFER, stageId, sourceMaterialsProducerId, destinationMaterialsProducerId));
	}

	@Override
	public void observeMaterialsProducerResourceChange(final Environment environment, MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.MATERIALS_PRODUCER_RESOURCE, materialsProducerId, resourceId));
	}

	@Override
	public void close(Environment environment) {

	}

	@Override
	public void observeGroupConstruction(Environment environment, GroupId groupId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GROUP_CONSTRUCTION, groupId));
	}

	@Override
	public void observeGroupDestruction(Environment environment, GroupInfo groupInfo) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GROUP_DESTRUCTION, groupInfo.getGroupId()));
	}

	@Override
	public void observeGroupPropertyChange(Environment environment, GroupId groupId, GroupPropertyId groupPropertyId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GROUP_PROPERTY, groupId, groupPropertyId));
	}

	@Override
	public void observeGroupPersonArrival(Environment environment, GroupId groupId, PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GROUP_PERSON_ARRIVAL, groupId, personId));
	}

	@Override
	public void observeGroupPersonDeparture(Environment environment, GroupId groupId, PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.GROUP_PERSON_DEPARTURE, groupId, personId));
	}


	@Override
	public void observePartitionPersonAddition(Environment environment, Object key, PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PARTITION_PERSON_ADDITION, key, personId));
		
	}

	@Override
	public void observePartitionPersonRemoval(Environment environment, Object key, PersonId personId) {
		observationContainer.addObservation(new MultiKey(environment.getTime(), id, ObservationType.PARTITION_PERSON_REMOVAL, key, personId));
	}

}
