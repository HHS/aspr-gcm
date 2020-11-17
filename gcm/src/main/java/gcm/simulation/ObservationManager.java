package gcm.simulation;

import gcm.components.Component;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.PersonInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
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

/**
 * Manager for all component observations of mutations to the environment. As
 * mutations are executed by {@link Component}s, the {@link MutationResolver}
 * notifies this manager with the details of those mutations. Components also
 * inform this manager, via the {@link Environment}, of the mutations that they
 * are observing.
 *
 * The manager coordinates the observed mutations with the component's need to
 * observe by storing these observations as Observation Records. When the
 * {@link EventManager} has completed activity with the current active
 * component, it retrieves these Observation Records and distributes them to the
 * observing components.
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface ObservationManager extends Element {

	/**
	 * Returns the next stored observation record, releasing it from storage.
	 * Returns null if there are no observation records.
	 */
	public ObservationRecord getNextObservation();

	/**
	 * Creates observation records for component observers for a change to a
	 * compartment property value for the given compartment and compartment
	 * property.
	 *
	 * @param compartmentId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handleCompartmentPropertyChange(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Creates observation records for component observers for a change to the
	 * global property value for the given global property.
	 *
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handleGlobalPropertyChange(final GlobalPropertyId globalPropertyId);

	/**
	 * Creates observation records for component observers for an addition of a
	 * group.
	 *
	 * @param groupId
	 *            cannot be null
	 */
	public void handleGroupAddition(final GroupId groupId);

	/**
	 * Creates observation records for component observers for a change to a
	 * group property value.
	 *
	 * @param groupId
	 *            cannot be null
	 * @param groupPropertyId
	 *            cannot be null
	 */

	public void handleGroupPropertyChange(final GroupId groupId, final GroupPropertyId groupPropertyId);

	/**
	 * Creates observation records for component observers for a removal of a
	 * group where a comprehensive record of the group prior to its removal is
	 * needed.
	 *
	 * @param groupInfo
	 *            cannot be null
	 */
	public void handleGroupRemovalByGroupInfo(final GroupInfo groupInfo);

	/**
	 * Creates observation records for component observers for a removal of a
	 * group.
	 *
	 * @param groupInfo
	 *            cannot be null
	 */

	public void handleGroupRemoval(final GroupId groupId);

	/**
	 * Creates observation records for component observers for a to a materials
	 * producer property value for the given materials producer.
	 *
	 * @param materialsProducerId
	 *            cannot be null
	 * 
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handleMaterialsProducerPropertyChange(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Creates observation records for component observers for a to a materials
	 * producer resource value for the given materials producer and resource.
	 *
	 * @param materialsProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void handleMaterialsProducerResourceChange(final MaterialsProducerId materialsProducerId, final ResourceId resourceId);

	/**
	 * Creates observation records for component observers for an addition of a
	 * person to the simulation.
	 *
	 * @param personId
	 *            cannot be null
	 */
	public void handlePersonAddition(final PersonId personId);

	/**
	 * Creates observation records for component observers for a change to a
	 * specific person's compartment assignment.
	 *
	 * @param personId
	 *            cannot be null
	 * @param oldCompartmentId
	 *            cannot be null
	 * @param newCompartmentId
	 *            cannot be null
	 */
	public void handlePersonCompartmentChange(final PersonId personId, final CompartmentId oldCompartmentId, final CompartmentId newCompartmentId);

	/**
	 * Creates observation records for component observers for a person joining
	 * a group.
	 *
	 * @param groupId
	 *            cannot be null
	 * @param personId
	 *            cannot be null
	 */
	public void handlePersonGroupAddition(final GroupId groupId, final PersonId personId);

	/**
	 * Creates observation records for component observers for a person leaving
	 * a group.
	 *
	 * @param groupId
	 *            cannot be null
	 * @param personId
	 *            cannot be null
	 */
	public void handlePersonGroupRemoval(final GroupId groupId, final PersonId personId);

	/**
	 * Creates observation records for component observers for a person property
	 * value change for a specific person.
	 *
	 * @param personId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handlePersonPropertyChange(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Creates observation records for component observers for a change to a
	 * specific person's region assignment.
	 *
	 * @param personId
	 *            cannot be null
	 * @param oldRegionId
	 *            cannot be null
	 * @param newRegionId
	 *            cannot be null
	 */
	public void handlePersonRegionChange(final PersonId personId, final RegionId oldRegionId, final RegionId newRegionId);

	/**
	 * Creates observation records for component observers for a removal of a
	 * person from the simulation where a comprehensive record of the person
	 * just prior to the removal is needed.
	 *
	 * @param personId
	 *            cannot be null
	 * @param compartmentId
	 *            cannot be null
	 * @param regionId
	 *            cannot be null
	 */
	public void handlePersonRemovalByPersonInfo(final PersonInfo personInfo);

	/**
	 * Creates observation records for component observers for a removal of a
	 * person from the simulation.
	 *
	 * @param personId
	 *            cannot be null
	 * @param compartmentId
	 *            cannot be null
	 * @param regionId
	 *            cannot be null
	 */
	public void handlePersonRemoval(final PersonId personId);

	

	/**
	 * Removes observation of a partition that is being removed from the
	 * simulation
	 *
	 */
	public void handlePartitionRemoval(final Object key);

		
	/**
	 * Creates observation records for component observers for a person addition
	 * to a partition.
	 * 
	 *
	 */
	public void handlePartitionPersonAddition(Object key, PersonId personId);

	/**
	 * Creates observation records for component observers for a person removal
	 * from a partition. 
	 *
	 */
	public void handlePartitionPersonRemoval(Object key, PersonId personId);

	/**
	 * Creates observation records for component observers for a change to a
	 * resource level for the given person.
	 *
	 * @param personId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void handlePersonResourceChange(final PersonId personId, final ResourceId resourceId);

	/**
	 * Creates observation records for component observers for a change to a
	 * region property value for the given region and region property.
	 *
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handleRegionPropertyChange(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Creates observation records for component observers for a change to a
	 * region resource level for the given region and resource.
	 *
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void handleRegionResourceChange(final RegionId regionId, final ResourceId resourceId);

	/**
	 * Creates observation records for component observers for a change to a
	 * resource property value for the given resource and resource property.
	 *
	 * @param resourceId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void handleResourcePropertyChange(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Removes observers for the stage.
	 *
	 * @param stageId
	 */
	public void handleStageDestruction(final StageId stageId);

	/**
	 * Creates observation records for component observers for a change to a
	 * stage's offer state.
	 *
	 * @param stageId
	 *            cannot be null
	 */
	public void handleStageOfferChange(final StageId stageId);

	/**
	 * Creates observation records for component observers for a transfer of
	 * custody for a stage from one materials producer to another.
	 *
	 * @param stageId
	 *            cannot be null
	 */
	public void handleStageTransfer(final StageId stageId,MaterialsProducerId sourceMaterialsProducerId, MaterialsProducerId destinationMaterialsProducerId);

	/**
	 * Returns true if and only if there are component observers that need
	 * {@link GroupInfo} for a specific group if it is removed.
	 * 
	 * @param groupId
	 *            cannot be null
	 */
	public boolean requiresGroupInfoForGroupRemoval(GroupId groupId);

	/**
	 * Returns true if there are any observers for the given person
	 */
	public boolean requiresPersonInfoForPersonRemoval();

	/**
	 * Starts or stops the observation of changes to the person property value
	 * for any person located in the given compartment for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId, final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops the observation of changes to the person resource value
	 * for any person located in the given compartment for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of arrivals of people in the given
	 * compartment for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 */
	public void observeCompartmentPersonArrivals(final boolean observe, final CompartmentId compartmentId);

	/**
	 * Starts or stops the observation of departures of people in the given
	 * compartment for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 */
	public void observeCompartmentPersonDepartures(final boolean observe, final CompartmentId compartmentId);

	/**
	 * Starts or stops the observation of changes to the given compartment
	 * property and compartment for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param compartmentId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Starts or stops the observation of the addition of people to the
	 * simulation for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeGlobalPersonArrivals(final boolean observe);

	/**
	 * Starts or stops the observation of removal of people from the simulation
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeGlobalPersonDepartures(final boolean observe);

	/**
	 * Starts or stops the observation of person property changes for the given
	 * property without regard to the location of the person for the current
	 * focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops the observation of person resource changes for the given
	 * property without regard to the location of the person for the current
	 * focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of global property changes for the given
	 * global property id for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId);

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeGroupArrival(final boolean observe);

	/**
	 * Starts or stops observation of the arrival of any person into the given
	 * group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupId
	 *            cannot be null
	 */
	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of the arrival of the given person into the
	 * given group
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupId
	 *            cannot be null
	 * 
	 * @param personId
	 *            cannot be null
	 * 
	 */
	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId);

	/**
	 * Starts or stops observation of the arrival of the given person into any
	 * group
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param personId
	 *            cannot be null
	 * 
	 */
	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 * having the given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupTypeId
	 *            cannot be null
	 * 
	 */
	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the arrival of the given person into any
	 * group having the given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupTypeId
	 *            cannot be null
	 * 
	 * @param personId
	 *            cannot be null
	 */
	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Starts or stops observation of all group construction
	 * 
	 * @param observe
	 *            start or stop observation
	 */
	public void observeGroupConstruction(final boolean observe);

	/**
	 * Starts or stops observation of group construction for groups having the
	 * given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupTypeId
	 *            cannot be null
	 */
	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the departure of any person from any group
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 */
	public void observeGroupDeparture(final boolean observe);

	/**
	 * Starts or stops observation of the departure of any person from the given
	 * group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupId
	 *            cannot be null
	 */

	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of the departure of the given person from the
	 * given group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupId
	 *            cannot be null
	 * @param personId
	 *            cannot be null
	 */
	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId);

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 */
	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of the departure of any person from any group
	 * having the given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupTypeId
	 *            cannot be null
	 * 
	 */
	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group having the given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupTypeId
	 *            cannot be null
	 * @param personId
	 *            cannot be null
	 */
	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Starts or stops observation of all group destruction
	 * 
	 * @param observe
	 *            start or stop observation
	 */

	public void observeGroupDestruction(final boolean observe);

	/**
	 * Starts or stops observation of group destruction for the given group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupId
	 *            cannot be null
	 */

	public void observeGroupDestructionByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of group destruction for groups having the
	 * given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupTypeId
	 *            cannot be null
	 * 
	 */
	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of all group property changes for all groups
	 * 
	 * @param observe
	 *            start or stop observation
	 */
	public void observeGroupPropertyChange(final boolean observe);

	/**
	 * Starts or stops observation of all group property changes for the given
	 * group
	 * 
	 * @param observe
	 *            start or stop observation
	 * @param groupId
	 *            cannot be null
	 */
	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of all group property changes for the given
	 * group property and group
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupId
	 *            cannot be null
	 * 
	 * @param groupPropertyId
	 *            cannot be null
	 */
	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupPropertyId groupPropertyId, final GroupId groupId);

	/**
	 * Starts or stops observation of all group property changes for all groups
	 * of the given group type
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupPropertyId
	 *            cannot be null
	 * 
	 */
	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the given group property changes for all
	 * groups of the given group type
	 * 
	 * 
	 * @param observe
	 *            start or stop observation
	 * 
	 * @param groupTypeId
	 *            cannot be null
	 * 
	 * @param groupPropertyId
	 *            cannot be null
	 * 
	 */
	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId);

	/**
	 * Starts or stops the observation of the change of compartment for a
	 * specific person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 */
	public void observeIndividualPersonCompartmentChange(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops the observation of a change in the given property value
	 * for a specific person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeIndividualPersonPropertyChange(final boolean observe, final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops the observation of the change of region for a specific
	 * person for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 */
	public void observeIndividualPersonRegionChange(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops the observation of the change of resource value for a
	 * specific person and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param personId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeIndividualPersonResourceChange(final boolean observe, final PersonId personId, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of changes to a materials producer
	 * property for a specific materials producer for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param materialProducerId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeMaterialsProducerPropertyChange(final boolean observe, final MaterialsProducerId materialProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Starts or stops the observation of changes to the resource level for a
	 * specific materials producer and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param materialProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeMaterialsProducerResourceChangeByResourceId(final boolean observe, final MaterialsProducerId materialProducerId, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of changes to a materials producer
	 * property for all materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 */
	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of arrivals of people in the given region
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 */
	public void observeRegionPersonArrivals(final boolean observe, final RegionId regionId);

	/**
	 * Starts or stops the observation of departures of people in the given
	 * region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 */
	public void observeRegionPersonDepartures(final boolean observe, final RegionId regionId);

	/**
	 * Starts or stops the observation of changes to the person property value
	 * for any person located in the given region for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId, final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops the observation of changes to the person resource value
	 * for any person located in the given region for the current focal
	 * component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of changes to the given region property
	 * and region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId, final RegionPropertyId regionPropertyId);


	/**
	 * Starts or stops the observation of changes to the given region property
	 * across all regions for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId);
	/**
	 * Starts or stops the observation of changes to the resource level for the
	 * given region and resource for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public void observeRegionResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId);

	/**
	 * Starts or stops the observation of changes to the given region property
	 * and region for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param resourceId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Starts or stops the observation of the offer state for all stages for the
	 * current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeStageOfferChange(final boolean observe);

	/**
	 * Starts or stops the observation of the offer state for the given stage
	 * for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param stageId
	 *            cannot be null
	 */
	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId);

	/**
	 * Starts or stops the observation of the transfer of all stages between
	 * materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeStageTransfer(final boolean observe);

	/**
	 * Starts or stops the observation of the transfer of the given stage
	 * between materials producers for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param stageId
	 *            cannot be null
	 */
	public void observeStageTransferByStageId(final boolean observe, final StageId stageId);

	

	/**
	 * Starts or stops the observation of changes to the membership of the
	 * associated partition.
	 *
	 * @param observe
	 *            start or stop observation
	 * @param key
	 *            cannot be null must correspond to an existing population index
	 */
	public void observePartitionChange(boolean observe, Object key);

	/**
	 * Starts or stops the observation of the transfer of all stages from
	 * the given materials producer for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeStageTransferBySourceMaterialsProducerId(boolean observe, MaterialsProducerId sourceMaterialsProducerId);

	/**
	 * Starts or stops the observation of the transfer of all stages to
	 * the given materials producer for the current focal component.
	 *
	 * @param observe
	 *            start or stop observation
	 */
	public void observeStageTransferByDestinationMaterialsProducerId(boolean observe, MaterialsProducerId destinationMaterialsProducerId);


}