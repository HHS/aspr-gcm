package gcm.components;

import gcm.output.reports.GroupInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
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
import gcm.simulation.Plan;
import gcm.util.annotations.Source;

/**
 * Components are the active parts of a GCM simulation that are contributed by a
 * modeler. Components are able to add and remove people from the simulation,
 * alter the properties of people and control the production and allocation of
 * resources. In addition, components are able to plan for future actions and
 * actively observe changes to other components, people and resources.
 *
 * Components are designed and contributed to GCM by a modeler-developer. GCM
 * constructs instances of Components per simulation instance to ensure that the
 * components are thread confined.
 *
 * This interface serves as the singular contract that each component must
 * fulfill as a participant of the simulation.
 *
 * The observation methods each correspond to the most common mutations to the
 * state of the Environment. The mutations that are excluded comprise the
 * alterations to observation status, planning and the various people-index
 * methods.
 *
 * An observation method is invoked when three conditions are met: 1) the state
 * change occurs, 2) the component has registered to observe the state change
 * and 3) THE COMPONENT IS NOT THE COMPONENT THAT CAUSED THE CHANGE. Each
 * observation method is invoked by the simulation only after the corresponding
 * state change has taken place. The arguments passed in each observation method
 * are of two types: 1) arguments that show the identity of the value that
 * changed and 2) arguments that show relevant values that are no longer
 * available from the environment. Other data items that can be derived from the
 * environment are omitted since they can be derived as needed.
 *
 * For example, the method observeRegionPersonDeparture(RegionId regionId,
 * PersonId personId) includes a personId to identify the person. The regionId
 * is included since it is the region formerly associated with the person and is
 * not available from the environment.
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface Component {

	/**
	 * When a component schedules as plan it does so with a future time when
	 * that plan should come to fruition. If the scheduled time comes to pass in
	 * the simulation without the plan being canceled, the simulation sends the
	 * plan back to the component by invoking this method. The execution of the
	 * plan is entirely up to the component and the simulation serves only a
	 * repository for planning.
	 *
	 * Typically planning activities include: 1) creating new people, 2)
	 * removing people 3) moving people to new regions or compartments, 4)
	 * updating person properties and 5) resource allocations.
	 *
	 * @param plan
	 */
	public void executePlan(final Environment environment, Plan plan);

	/**
	 * At the end of a simulation, the simulation will invoke this method
	 * exactly once and it will be the last method of this interface to be
	 * invoked.
	 *
	 * @param environment
	 */

	public void close(Environment environment);

	/**
	 * After the inclusion of any component into the simulation, the simulation
	 * will invoke this method exactly once and it will be the first method of
	 * this interface to be invoked. The Environment instance that is passed to
	 * the component serves as the component's gateway into all aspects of the
	 * simulation.
	 *
	 * @param environment
	 */
	public void init(Environment environment);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has arrived at a new compartment.
	 *
	 * @param personId
	 */
	public void observeCompartmentPersonArrival(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has departed a compartment.
	 *
	 * @param compartmentId--
	 *            the compartmentId the person has departed from
	 * @param personId
	 */
	public void observeCompartmentPersonDeparture(Environment environment, CompartmentId compartmentId, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a
	 * compartment's property value has changed.
	 *
	 * @param compartmentId
	 * @param compartmentPropertyId
	 */
	public void observeCompartmentPropertyChange(Environment environment, CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId);

	/**
	 * An alert from the simulation to the component that a person has arrived
	 * into the simulation.
	 *
	 * @param personId
	 */
	public void observeGlobalPersonArrival(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has departed the simulation. Person id values, unlike all other id
	 * values, are assigned by the simulation and are re-issued on an as needed
	 * basis. Therefore the departure of a person with id 1234 may be followed
	 * by the arrival of another person with the same id.
	 *
	 * @param personId
	 */
	public void observeGlobalPersonDeparture(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component that a global level
	 * property value has changed.
	 *
	 * @param globalPropertyId
	 */

	public void observeGlobalPropertyChange(Environment environment, GlobalPropertyId globalPropertyId);

	/**
	 * An alert from the simulation to the component indicating that a materials
	 * producer's property value has changed.
	 *
	 * @param materialsProducerId
	 * @param materialsProducerPropertyId
	 */
	public void observeMaterialsProducerPropertyChange(Environment environment, MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * level has changed for the given materialsProducer
	 *
	 * @param materialsProducerId
	 * @param resourceId
	 */
	public void observeMaterialsProducerResourceChange(Environment environment, MaterialsProducerId materialsProducerId, ResourceId resourceId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved to a new compartment.
	 *
	 * @param personId
	 */
	public void observePersonCompartmentChange(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person's
	 * property value has changed.
	 *
	 * @param personId
	 * @param personPropertyId
	 */
	public void observePersonPropertyChange(Environment environment, PersonId personId, PersonPropertyId personPropertyId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved to a new region.
	 *
	 * @param personId
	 */
	public void observePersonRegionChange(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has had a resource level change.
	 *
	 * @param personId
	 * @param resourceId
	 */
	public void observePersonResourceChange(Environment environment, PersonId personId, ResourceId resourceId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has arrived in a new region.
	 *
	 * @param personId
	 */
	public void observeRegionPersonArrival(Environment environment, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has departed from the region.
	 *
	 * @param regionId
	 *            -- the regionId the person has departed from
	 * @param personId
	 */
	public void observeRegionPersonDeparture(Environment environment, RegionId regionId, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a region's
	 * property value has changed.
	 *
	 * @param regionId
	 * @param regionPropertyId
	 */
	public void observeRegionPropertyChange(Environment environment, RegionId regionId, RegionPropertyId regionPropertyId);

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * level has changed for the given region
	 *
	 * @param regionId
	 * @param resourceId
	 */
	public void observeRegionResourceChange(Environment environment, RegionId regionId, ResourceId resourceId);

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * property has changed for the given resource
	 *
	 * @param resourceId
	 * @param resourcePropertyId
	 */
	public void observeResourcePropertyChange(Environment environment, ResourceId resourceId, ResourcePropertyId resourcePropertyId);

	/**
	 * An alert from the simulation to the component indicating that a stage's
	 * offer state has changed.
	 *
	 * @param stageId
	 */

	public void observeStageOfferChange(Environment environment, StageId stageId);

	/**
	 * An alert from the simulation to the component indicating that a stage has
	 * been transferred from on materials producer to another.
	 *
	 * @param stageId
	 */

	public void observeStageTransfer(Environment environment, StageId stageId, MaterialsProducerId sourceMaterialsProducerId, MaterialsProducerId destinationMaterialsProducerId);

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * been created.
	 *
	 */
	public void observeGroupConstruction(Environment environment, GroupId groupId);

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * been destroyed.
	 *
	 */
	public void observeGroupDestruction(Environment environment, GroupInfo groupInfo);

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * had a property value change.
	 *
	 */
	public void observeGroupPropertyChange(Environment environment, GroupId groupId, GroupPropertyId groupPropertyId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been added to a group
	 *
	 */
	public void observeGroupPersonArrival(Environment environment, GroupId groupId, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been removed from a group
	 */
	public void observeGroupPersonDeparture(Environment environment, GroupId groupId, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been added to a population index
	 */
	public void observePopulationIndexPersonAddition(Environment environment, Object key, PersonId personId);

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been removed from a population index
	 */
	public void observePopulationIndexPersonRemoval(Environment environment, Object key, PersonId personId);
}
