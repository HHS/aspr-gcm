package gcm.simulation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import gcm.components.Component;
import gcm.output.OutputItem;
import gcm.output.reports.Report;
import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.ComponentId;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MapOption;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * The sub-set of the Environment interface that 1)cannot cause a state change
 * and 2) provides information not supplied by the {@link Component} or
 * {@link Report} interfaces such as the current value of time.
 *
 * @NotThreadSafe
 * @author Shawn Hatch
 */
@NotThreadSafe
@Source
public interface ObservableEnvironment extends Element {

	/**
	 * Returns true if and only if the batch exists
	 *
	 */
	public boolean batchExists(final BatchId batchId);

	/**
	 * Returns the amount in the batch
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batchId id is null
	 *             <li>if the batchId id is unknown
	 *
	 */
	public double getBatchAmount(final BatchId batchId);

	/**
	 * Returns the material id of the batch
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batchId id is null
	 *             <li>if the batchId id is unknown
	 *
	 */
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId);

	/**
	 * Returns the materials producer identifier of the batch
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batchId id is null
	 *             <li>if the batchId id is unknown
	 *
	 */
	public <T> T getBatchProducer(final BatchId batchId);

	/**
	 * Returns the batch property definition associated with the given material
	 * and property identifiers
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materialId id is null
	 *             <li>if the materialId id unknown
	 *             <li>if the batchPropertyId id is null
	 *             <li>if the batchPropertyId id unknown
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch property identifiers supplied to the simulation by the
	 * scenario for the given material identifier.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the material id is null
	 *             <li>if the material id is unknown
	 */
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given batch and property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batch id is null
	 *             <li>if the batch id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property is unknown
	 *
	 */
	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch property value for the given property identifier
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batch id is null
	 *             <li>if the batch id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property id is unknown
	 *
	 */
	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch's stage id. Returns null if the batch is not associated
	 * with a stage.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batchId id is null
	 *             <li>if the batchId id is unknown
	 *
	 */
	public Optional<StageId> getBatchStageId(final BatchId batchId);

	/**
	 * Returns the creation time of the batch
	 *
	 * @throws ModelException
	 *
	 *             <li>if the batchId id is null
	 *             <li>if the batchId id is unknown
	 *
	 */
	public double getBatchTime(final BatchId batchId);

	/**
	 * Returns the set of compartment identifiers as provided during simulation
	 * construction.
	 *
	 */
	public Set<CompartmentId> getCompartmentIds();

	/**
	 * Returns the MapOption for compartments
	 *
	 */
	public MapOption getCompartmentMapOption();

	/**
	 * Returns the number of people in given compartment for the given region.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 *
	 * <li>if the compartment id is null
	 * <li>if the compartment is unknown
	 *             </pre>
	 */
	public int getCompartmentPopulationCount(final CompartmentId compartmentId);

	/**
	 * Returns the simulation time when compartment's population count was last
	 * set.
	 *
	 */
	public double getCompartmentPopulationCountTime(final CompartmentId compartmentId);

	/**
	 * Returns the compartment property definition associated with the given
	 * property identifier
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * 				<li>if the property id is null
	 * 				<li>if the property id does not correspond to a known compartment property identifier
	 *             </pre>
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the compartment property identifiers supplied to the simulation
	 * by the scenario.
	 */
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given compartment and compartment property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 * <li>if the compartment id is null
	 * <li>if the compartment is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 */
	public double getCompartmentPropertyTime(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the value associated with the given compartment and property
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the compartment id is null
	 * <li>if the compartment is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 */
	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the set of global component identifiers as provided during
	 * simulation construction.
	 *
	 */
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds();

	/**
	 * Returns the global property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * 				<li>if the property id is null
	 * 				<li>if the property id does not correspond to a known global property identifier
	 *             </pre>
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the global property identifiers supplied to the simulation by the
	 * scenario.
	 */
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds();

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given global property identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the global id is null
	 * <li>if the global id is unknown
	 *             </pre>
	 *
	 */
	public double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the value associated with the given global property identifier.
	 *
	 * @throws ModelException
	 *             *
	 *
	 *             <pre>
	 * <li>if the global id is null
	 * <li>if the global id is unknown
	 *             </pre>
	 *
	 *
	 */
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the number of groups associated with the given group type
	 * identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the number of groups associated with the given group type
	 * identifier and person identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the number of groups associated with the given person id.
	 *
	 * @throws ModelException
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public int getGroupCountForPerson(final PersonId personId);

	/**
	 * Returns the group property definition associated with the given group
	 * type, group and property identifiers
	 *
	 * @throws ModelException
	 *
	 *             <li>if the group type id is null
	 *             <li>if the group type id unknown
	 *             <li>if the groupPropertyId id is null
	 *             <li>if the groupPropertyId id unknown
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the group property identifiers supplied to the simulation by the
	 * scenario for the given group type identifier.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the group type id is null
	 *             <li>if the group type id is unknown
	 */
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given group and property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the group type id is null
	 *             <li>if the group type id is unknown
	 *             <li>if the group id is null
	 *             <li>if the group id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property is unknown
	 *
	 */
	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the group's property value for the given property identifier
	 *
	 * @throws ModelException
	 *
	 *             <li>if the group type id is null
	 *             <li>if the group type id is unknown
	 *             <li>if the group id is null
	 *             <li>if the group id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property id is unknown
	 *
	 */
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the list of group identifiers associated with the given group
	 * type identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the list of group identifiers associated with the given group
	 * type identifier and person identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the group type identifiers associated with the given person id
	 * and group id.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the group id is null
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId);

	/**
	 * Returns the group type of the given group.
	 *
	 * @throws ModelException
	 *             <li>if the groupId id is null
	 *             <li>if the groupId id is unknown
	 */
	public <T> T getGroupType(final GroupId groupId);

	/**
	 * Returns the number of group types associated the person's groups.
	 *
	 * @throws ModelException
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public int getGroupTypeCountForPerson(final PersonId personId);

	/**
	 * Returns the set of group type identifiers as provided during simulation
	 * construction.
	 *
	 */

	public <T extends GroupTypeId> Set<T> getGroupTypeIds();

	/**
	 * Returns the group type identifiers associated the person's groups.
	 *
	 * @throws ModelException
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId);

	/**
	 * Returns the batches owned by a particular materials producer that are in
	 * inventory (not staged).
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materialsProducer id is null
	 *             <li>if the materialsProducer id is unknown
	 *
	 */
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the batches having a particular material id and owned by a
	 * particular materials producer that are in inventory (not staged).
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materialsProducer id is null
	 *             <li>if the materialsProducer id is unknown
	 *             <li>if the material id is null
	 *             <li>if the material id is unknown
	 *
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId);

	/**
	 * Returns the set of material identifiers as provided during simulation
	 * construction.
	 *
	 */
	public <T extends MaterialId> Set<T> getMaterialIds();

	/**
	 * Returns the set of materials producer component identifiers as provided
	 * during simulation construction.
	 *
	 */
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds();

	/**
	 * Returns the property definition associated with the given materials
	 * producer property identifier
	 *
	 * @throws ModelException
	 *
	 *             <li>if the property id is null
	 *             <li>if the property id does not correspond to a known
	 *             materials producer property identifier
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the materials producer property identifiers supplied to the
	 * simulation by the scenario.
	 */
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds();

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given materials producer and materials producer property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materials producer id is null
	 *             <li>if the materials producer id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property is unknown
	 *
	 */
	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the value associated with the given materials producer and
	 * materials producer property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materials Producer id is null
	 *             <li>if the materials Producer id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property is unknown
	 *
	 *
	 */
	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the materials producer's current resource level for the given
	 * resource identifier.
	 *
	 * @throws ModelException
	 *
	 *
	 *             <li>if the materials producer id is null
	 *             <li>if the materials producer id is unknown
	 *             <li>if the resource id is null
	 *             <li>if the resource is unknown
	 */
	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId);

	/**
	 * Returns the simulation time when the materials producer's resource level
	 * was last set for the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materials producer id is null
	 *             <li>if the materials producer id is unknown
	 *             <li>if the resource id is null
	 *             <li>if the resource is unknown
	 *
	 */
	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId, final ResourceId resourceId);

	/**
	 * Returns the offered stage's for the given materials producer.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materials producer id is null
	 *             <li>if the materials producer id is unknown
	 */
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the simulation.
	 *
	 */
	public List<PersonId> getPeople();

	/**
	 * Returns the list of people identifiers associated with the given group
	 * type identifier and group identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group id is null
	 *             <li>if the group id is unknown(group does not exist)
	 *
	 *
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId);

	/**
	 * Returns the list of people identifiers associated with the given group
	 * type identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the the given compartment.
	 *
	 * throws ModelException
	 *
	 * <pre>
	 * <li>if the compartment id is null
	 * <li>if the compartment is unknown
	 * </pre>
	 */
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId);

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the the given region.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 *             </pre>
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId);

	/**
	 * Returns the list of person identifier values for all people currently
	 * having zero units of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <pre>
	 * <li>if the resource id is null
	 * <li>if the resource id is not known
	 * </pre>
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId);

	/**
	 * Returns the list of person identifier values for all people currently
	 * having the the given property value for the given person property
	 * identifier.
	 *
	 * throws ModelException
	 *
	 * <pre>
	 * <li>if the property id is null
	 * <li>if the property id is not a person property
	 * <li>if the property value is null
	 * </pre>
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue);

	/**
	 * Returns the number of people currently having the the given property
	 * value for the given person property identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID} if the property
	 * id is null
	 * <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID} if the
	 * property id is unknown
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_VALUE} if the
	 * property value is null
	 * <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if the property value
	 * is not compatible with the property definition
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue);

	
	/**
	 * Returns the list of person identifier values for all people currently
	 * having at least one unit of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <pre>
	 * <li>if the resource id is null
	 * <li>if the resource id is not known
	 * </pre>
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId);

	/**
	 * Returns the compartment identifier for the given person.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 *             <li>if the person is null
	 *             <li>if the person is unknown
	 *             </pre>
	 */
	public <T> T getPersonCompartment(final PersonId personId);

	/**
	 * Returns the simulation time when the person arrived in their current
	 * compartment. Movement between regions within a single compartment does
	 * not alter this value.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 *             <li>if the person is null
	 *             <li>if the person is unknown
	 *             </pre>
	 */
	public double getPersonCompartmentArrivalTime(final PersonId personId);

	/**
	 * Returns true if and only if the simulation is actively tracking
	 * compartment arrival times for people.
	 *
	 * @return
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy();

	/**
	 * Returns the number of people associated with the given group type
	 * identifier and group identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group id is null
	 *             <li>if the group id is unknown(group does not exist)
	 *
	 *
	 */
	public int getPersonCountForGroup(final GroupId groupId);

	/**
	 * Returns the number of people associated with the given group type
	 * identifier.
	 *
	 * @throws ModelException
	 *             <li>if the group Type id is null
	 *             <li>if the group Type id is unknown
	 *
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the person property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * 				<li>if the property id is null
	 * 				<li>if the property id does not correspond to a known person property identifier
	 *             </pre>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId);

	/**
	 * Returns the person property identifiers supplied to the simulation by the
	 * scenario.
	 */
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds();

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given person and person property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is null
	 * <li>if the person is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Returns the value associated with the given person and property
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is null
	 * <li>if the person is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 */
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Returns the region identifier for the given person.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is unknown
	 * <li>if the person is null
	 *             </pre>
	 */

	public <T> T getPersonRegion(final PersonId personId);

	/**
	 * Returns the simulation time when the person arrived in their current
	 * region. Movement between compartments within a single region does not
	 * alter this value.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is null
	 * <li>if the person is unknown
	 *             </pre>
	 */
	public double getPersonRegionArrivalTime(final PersonId personId);

	/**
	 * Returns true if and only if the simulation is actively tracking region
	 * arrival times for people.
	 *
	 * @return
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy();

	/**
	 * Returns the person's current resource level for the given resource
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is null
	 * <li>if the person is unknown
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 *             </pre>
	 */
	public long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId);

	/**
	 * Returns the simulation time when the person's resource level was last set
	 * for the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the person is null
	 * <li>if the person is unknown
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 * <li>if the resource assignment time is not actively tracked
	 *             </pre>
	 *
	 */
	public double getPersonResourceTime(final PersonId personId, final ResourceId resourceId);

	/**
	 * Returns true if and only if the simulation is actively tracking resource
	 * value assignment times for the given resource id.
	 *
	 * @throws ModelException
	 *             <li>if the resource id is null
	 *             <li>if the resource id is unknown
	 *
	 * @param resourceId
	 * @return
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId);

	/**
	 * Returns the number of people currently in the simulation.
	 *
	 */
	public int getPopulationCount();

	/**
	 * Returns the simulation time when population count was last set.
	 *
	 */
	public double getPopulationTime();

	/**
	 * Returns the set of region component identifiers as provided during
	 * simulation construction.
	 *
	 */
	public Set<RegionId> getRegionIds();

	/**
	 * Returns the MapOption for the regions
	 *
	 */
	public MapOption getRegionMapOption();

	/**
	 * Returns the number of people currently in the simulation.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 *             </pre>
	 */
	public int getRegionPopulationCount(final RegionId regionId);

	/**
	 * Returns the simulation time when region's population count was last set.
	 *
	 */
	public double getRegionPopulationCountTime(final RegionId regionId);

	/**
	 * Returns the region property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *             <li>if the property id is null
	 *             <li>if the property id does not correspond to a known region
	 *             property identifier
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId);

	/**
	 * Returns the region property identifiers supplied to the simulation by the
	 * scenario.
	 */
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds();

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given region and region property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 */
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Returns the value associated with the given region and region property
	 * identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 */
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Returns the region's current resource level for the given resource
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 *             </pre>
	 */
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId);

	/**
	 * Returns the simulation time when the region's resource level was last set
	 * for the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the region id is null
	 * <li>if the region is unknown
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 *             </pre>
	 *
	 */
	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId);

	/**
	 * Returns the replication id that was set during simulation construction.
	 *
	 * @return
	 */
	public ReplicationId getReplicationId();

	/**
	 * Returns the set of resource identifiers as provided during simulation
	 * construction.
	 *
	 */
	public <T extends ResourceId> Set<T> getResourceIds();

	/**
	 * Returns the resource property definition associated with the given
	 * property identifier
	 *
	 * @throws ModelException
	 *
	 *
	 *             <li>if the resource id is null
	 *             <li>if the resource id is unknown
	 *             <li>if the property id is null
	 *             <li>if the property id does not correspond to a known
	 *             resource property identifier
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the resource property identifiers supplied to the simulation by
	 * the scenario.
	 * 
	 * @throws ModelException
	 * 
	 *             <li>if the resource id is null
	 *             <li>if the resource id is unknown
	 * 
	 */
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given resource and resource property identifiers.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 */
	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	// resources
	/**
	 * Returns the value associated with the given resource and property
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *             <pre>
	 * <li>if the resource id is null
	 * <li>if the resource is unknown
	 * <li>if the property id is null
	 * <li>if the property is unknown
	 *             </pre>
	 *
	 *
	 */
	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the scenario id that was set during simulation construction.
	 *
	 * @return
	 */
	public ScenarioId getScenarioId();

	/**
	 * Returns the batches associated with a stage.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the stageId id is null
	 *             <li>if the stageId id is unknown
	 *
	 */
	public List<BatchId> getStageBatches(final StageId stageId);

	/**
	 * Returns the batches having the given material type associated with a
	 * particular stage.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the stageId id is null
	 *             <li>if the stageId id is unknown
	 *             <li>if the material id is null
	 *             <li>if the material id is unknown
	 *
	 */
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId);

	/**
	 * Returns the stage's materials producer
	 *
	 * @throws ModelException
	 *
	 *             <li>if the stage id is null
	 *             <li>if the stage id is unknown
	 *
	 */

	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId);

	/**
	 * Returns the stage's for the given materials producer.
	 *
	 * @throws ModelException
	 *
	 *             <li>if the materials producer id is null
	 *             <li>if the materials producer id is unknown
	 */
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the current time. Time is measured in days and initializes to
	 * zero. Time progresses via planning.
	 *
	 */
	public double getTime();

	/**
	 * Returns true if and only if the group associated with the given group
	 * type and group identifiers exists.
	 *
	 */
	public boolean groupExists(final GroupId groupId);

	/**
	 * Returns true if and only if the person is a member of the group
	 * identified by the group type and group identifiers.
	 *
	 * @throws ModelException
	 *             <li>if the group id is null
	 *             <li>if the group id is unknown(group does not exist)
	 *             <li>if the person id is null
	 *             <li>if the person id is unknown
	 */
	public boolean isGroupMember(final PersonId personId, final GroupId groupId);

	/**
	 * Returns the stage's offer state. Offered stages cannot be altered until
	 * they are no longer offered
	 *
	 * @throws ModelException
	 *
	 *             <li>if the stage id is null
	 *             <li>if the stage id is unknown
	 *
	 */
	public boolean isStageOffered(final StageId stageId);

	/**
	 * Returns true if and only if the given person identifier is associated
	 * with a person in the simulation.
	 *
	 */
	public boolean personExists(final PersonId personId);

	/**
	 * Returns true if and only if there is a stage with the given id.
	 *
	 * @throws ModelException
	 *
	 */
	public boolean stageExists(final StageId stageId);

	/**
	 * Releases an output item to the OutputItemManger
	 */
	public void releaseOutputItem(OutputItem outputItem);

	public ComponentId getCurrentComponentId();

	public Class<? extends Component> getGlobalComponentClass(GlobalComponentId globalComponentId);

	public Class<? extends Component> getCompartmentComponentClass(CompartmentId compartmentId);

	public Class<? extends Component> getMaterialsProducerComponentClass(MaterialsProducerId materialsProducerId);

	public Class<? extends Component> getRegionComponentClass(RegionId regionId);

	public <T> T getProfiledProxy(T instance);

	/**
	 * Returns the suggested population size from the scenario.
	 *
	 */
	public int getSuggestedPopulationSize();
	
	/**
	 * Returns the list of all group identifiers.
	 */
	public List<GroupId> getGroupIds();
}
