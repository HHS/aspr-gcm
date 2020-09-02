
package gcm.simulation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.components.Component;
import gcm.output.OutputItem;
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
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.simulation.partition.LabelSet;
import gcm.simulation.partition.LabelSetWeightingFunction;
import gcm.simulation.partition.Partition;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * <p>
 * The General Compartment Model (GCM) environment interface.
 *
 * <p>
 * GCM is primarily composed of a simulation instance and a set of contributed
 * components that work together through the environment.
 *
 * <p>
 * The Environment:
 * <li>is common to multiple modeling efforts</li>
 * <li>does not represent the business rules for a specific model</li>
 * <li>groups data (properties) by component types: global, region, compartment,
 * resource, and materials production
 * <li>provides a means to inspect data, observe changes to data, alter data and
 * make plans for future action</li>
 *
 * <p>
 * Components:
 * <li>can act upon the data</li>
 * <li>provide business logic that represents a specific model</li>
 * <li>use the simulation to work with data and make plans</li>
 *
 * <p>
 * Global Components:
 * <li>are identified by a model-defined global component id</li>
 * <li>represent an activity or feature that generally spans all regions and
 * compartments</li>
 * <li>can act upon the simulation data</li>
 * <li>are usually concerned with global issues such as vaccination, addition of
 * people, etc</li>
 *
 * <p>
 * Region Components:
 * <li>represent a physical location or area</li>
 * <li>are identified by a model-defined region id</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>have resource inventory</li>
 * <li>contain compartments</li>
 * <li>manage resource production</li>
 *
 * <p>
 * Compartments Components:
 * <li>represent a general state of people such as a disease state</li>
 * <li>are identified by a model-defined compartment id</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>contain people</li>
 * <li>manage resource distribution and consumption by people</li>
 * <li>move people to other compartments</li>
 * <li>remove people from the simulation</li>
 * 
 * <p>
 * Materials Producer Components:
 * <li>represent the production capability to create the resources that will be
 * consumed by people</li>
 * <li>are identified by a model-defined materials producer</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>manage materials creation and processing</li>
 * <li>produce resources from materials</li>
 * 
 *
 * <p>
 * Resources:
 * <li>represent a resource type(but not quantity)such as medicines, hospital
 * beds, etc</li>
 * <li>are identified by a model-defined resource id</li>
 * <li>are global in scope</li>
 * <li>are not components and cannot act on their own</li>
 * <li>have property values</li>
 * <li>are associated with people and regions</li>
 *
 * <p>
 * Materials:
 * <li>represent a material type(but not quantity) such as eggs used for vaccine
 * production</li>
 * <li>are identified by a model-defined material id</li>
 * <li>are global in scope</li>
 * <li>are not components and cannot act on their own</li>
 * <li>have property values</li>
 * <li>are associated with materials producer components</li>
 *
 * <p>
 * People:
 * <li>represent a single person</li>
 * <li>are identified by an integer id</li>
 * <li>are not components and cannot act on their own</li>
 * <li>may be added and removed from the simulation</li>
 * <li>are always in a compartment</li>
 * <li>may move from one compartment to another</li>
 * <li>have resource inventory</li>
 * 
 * <p>
 * Groups:
 * <li>represent groups of physically co-located people
 * <li>are identified by an integer id</li>
 * <li>are identified by an integer id</li>
 * <li>are not components and cannot act on their own</li>
 * <li>may be added and removed from the simulation</li>
 * <li>may have people added and removed from them without limitation
 * 
 * <p>
 * Properties:
 * <li>represent model-defined data values that are associated with components,
 * people and resources</li>
 * <li>are composed of a definition and a value</li>
 * <li>property definitions establish the data type and default value of a
 * property</li>
 * <li>property values are associated with components, people and resources</li>
 * <li>may be of any data type</li>
 *
 * <p>
 * Resource Amounts:
 * <li>represent a non-negative integral quantity of a specific resource type *
 * <li>are associated with people and regions</li>
 * 
 * <p>
 * Materials Amounts:
 * <li>represent a floating point quantity of a material that will support the
 * production of resources
 * <li>are associated with materials producer components</li>
 * 
 * @NotThreadSafe
 * @author Shawn Hatch
 */
@NotThreadSafe
@Source
public interface Environment extends Element {

	/**
	 * Returns the group identifier for a newly created group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} the
	 *                        group type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type id is unknown
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component, a
	 *                        region component or a compartment component
	 */
	public GroupId addGroup(final GroupTypeId groupTypeId);

	/**
	 * Returns the PersonId for a new person who is placed into the given
	 * compartment and assigned default person property values. Person identifier
	 * values are unique to the people in the simulation at any given time, but may
	 * be reused by the simulation at people are removed.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment id is unknown
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component
	 *
	 */

	public PersonId addPerson(final RegionId regionId, final CompartmentId compartmentId);

	/**
	 * Adds a person to the group associated with the given group type and group
	 * identifiers.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#DUPLICATE_GROUP_MEMBERSHIP}
	 *                        if the person is already a member of the group
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component, a
	 *                        region component or a compartment component
	 */
	public void addPersonToGroup(final PersonId personId, final GroupId groupId);

	/**
	 * Schedules a plan. The plan is identified by the ordered keys provided. When
	 * time progresses to the planTime, the plan is removed from the simulation and
	 * returned to the invoking component. Plans without any keys cannot be
	 * retrieved or removed.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PLAN} if the plan
	 *                        is null
	 *                        <li>{@link SimulationErrorType#PAST_PLANNING_TIME} if
	 *                        the plan time is in the past
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component, a
	 *                        region component or a compartment component or a
	 *                        materials producer component
	 *
	 *
	 */
	public void addPlan(final Plan plan, final double planTime);

	/**
	 * Schedules a plan. The plan is identified by the ordered keys provided. When
	 * time progresses to the planTime, the plan is removed from the simulation and
	 * returned to the invoking component. Plans without any keys cannot be
	 * retrieved or removed.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PLAN} if the plan
	 *                        is null
	 *                        <li>{@link SimulationErrorType#PAST_PLANNING_TIME} if
	 *                        the plan time is in the past
	 *                        <li>{@link SimulationErrorType#NULL_PLAN_KEY} if the
	 *                        key is null
	 *                        <li>{@link SimulationErrorType#DUPLICATE_PLAN_KEY} if
	 *                        the key corresponds to an active plan
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component, a
	 *                        region component or a compartment component or a
	 *                        materials producer component
	 *
	 *
	 */
	public void addPlan(final Plan plan, final double planTime, final Object key);

	/**
	 * Adds an indexed population using the supplied filter.
	 *
	 * Only the component that creates an index may remove that index, but other
	 * components may access the index if they know its key. The resulting indexed
	 * population is actively maintained by the GCM and is accessed by the key
	 * provided. This index is owned by the component that created it in that only
	 * that component may remove it from the Environment. However, all components
	 * have access to the index via its key.
	 *
	 * Filters are created using the various static constructors provided in
	 * {@linkplain Filter}.
	 * 
	 * 
	 *
	 *
	 * @throws ModelException *
	 *                        <li>{@link SimulationErrorType#NULL_FILTER} if the
	 *                        filter is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#DUPLICATE_INDEXED_POPULATION}
	 *                        if the key corresponds to an existing population index
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if this method is invoked while the simulation has no
	 *                        active component
	 *
	 */
	public void addPopulationIndex(final Filter filter, final Object key);

	/**
	 * Adds the amount of resource to the given region.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in an overflow of the
	 *                        corresponding region's inventory level
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component or
	 *                        region
	 *
	 */
	public void addResourceToRegion(final ResourceId resourceId, final RegionId regionId, final long amount);

	/**
	 * Returns true if and only if the batch exists
	 *
	 */
	public boolean batchExists(final BatchId batchId);

	/**
	 * Converts a stage to a batch that will be held in the inventory of the
	 * invoking materials producer. The stage and its associated batches are
	 * destroyed. Returns the newly created batch's id. The stage must be owned by
	 * the invoking materials producer and must not be in the offered state.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if stage is in the offered state
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        material id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        material is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_MATERIAL_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#NON_FINITE_MATERIAL_AMOUNT}
	 *                        if the amount is not finite
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 */
	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount);

	/**
	 * Converts a stage to a resource that will be held in the inventory of the
	 * invoking materials producer. The stage and its associated batches are
	 * destroyed. Returns the new created batch's id. The stage must be owned by the
	 * invoking materials producer and must not be in the offered state.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if stage is in the offered state
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        resource is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative *
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in an overflow of the
	 *                        corresponding materials producer's inventory level
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 */
	public void convertStageToResource(final StageId stageId, final ResourceId resourceId, final long amount);

	/**
	 * Returns the batch id of a new created batch that is stored in the inventory
	 * of the invoking materials producer.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        the material id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        the material id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_MATERIAL_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#NON_FINITE_MATERIAL_AMOUNT}
	 *                        if the amount is not finite
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a materials producer component
	 *
	 */
	public BatchId createBatch(final MaterialId materialId, final double amount);

	/**
	 * Creates a new stage owned by the invoking materials producer component and
	 * returns its id .
	 *
	 * @throws ModelException
	 * 
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a materials producer component
	 *
	 */
	public StageId createStage();

	/**
	 * Destroys the indicated batch that is owned by the invoking materials
	 * producer. The batch may not be part of an offered stage.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batch id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batch id is unknown for the materials producer
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if the batch is part of an offered stage
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 *
	 */
	public void destroyBatch(final BatchId batchId);

	/**
	 * Destroys a stage owned by the invoking materials producer component. If
	 * destroyBatches is set to true, then all batches associated with the stage are
	 * also destroyed, otherwise they are returned to inventory.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if stage is in an offered state
	 *                        <li>{@link ErrorType#} if invoker is not the owning
	 *                        materials producer component
	 *
	 */
	public void destroyStage(final StageId stageId, final boolean destroyBatches);

	/**
	 * Returns the amount in the batch
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batchId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batchId id is unknown
	 *
	 */
	public double getBatchAmount(final BatchId batchId);

	/**
	 * Returns the material id of the batch
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link ErrorType#} if the batchId id is null
	 *                        <li>{@link ErrorType#} if the batchId id is unknown
	 *
	 */
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId);

	/**
	 * Returns the materials producer identifier of the batch
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batchId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batchId id is unknown
	 *
	 */
	public <T> T getBatchProducer(final BatchId batchId);

	/**
	 * Returns the batch property definition associated with the given material and
	 * property identifiers
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        the materialId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        the materialId id unknown
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_PROPERTY_ID}
	 *                        if the batchPropertyId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_PROPERTY_ID}
	 *                        if the batchPropertyId id unknown
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId,
			final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch property identifiers supplied to the simulation by the
	 * scenario for the given material identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        the material id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        the material id is unknown
	 */
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given batch and property identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batch id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batch id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch property value for the given property identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batch id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batch id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_PROPERTY_ID}
	 *                        if the property id is unknown
	 *
	 */
	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the batch's stage id.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batchId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batchId id is unknown
	 *
	 */
	public Optional<StageId> getBatchStageId(final BatchId batchId);

	/**
	 * Returns the creation time of the batch
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        batchId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the batchId id is unknown
	 *
	 */
	public double getBatchTime(final BatchId batchId);

	/**
	 * Returns a contacted person from the group specified by the groupId by using
	 * the supplied BiWeightingFunction from the sourcePersonId against all members
	 * of the group. The sourcePersonId can be excluded as a return value. Optional
	 * result will reflect when no contact was possible.
	 * 
	 * Note that the BiWeightingFunction must be stable: Repeated invocations of the
	 * function with any fixed set of arguments must result in a single return value
	 * during the scope of this method.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_WEIGHTING_FUNCTION}
	 *                        if the biWeightingFunction is null
	 *                        <li>{@link SimulationErrorType#MALFORMED_WEIGHTING_FUNCTION}
	 *                        if the biWeightingFunction is malformed. (some
	 *                        evaluate to negative numbers,NaN, infinity, etc. --
	 *                        note that if all weights are zero then the optional
	 *                        will return isPresent() of false)
	 * 
	 */
	public Optional<PersonId> getBiWeightedGroupContact(final GroupId groupId, final BiWeightingFunction biWeightingFunction, final PersonId sourcePersonId,
			final boolean excludeSourcePerson);

	/**
	 * Returns a contacted person from the group specified by the groupId by using
	 * the supplied BiWeightingFunction from the sourcePersonId against all members
	 * of the group. The sourcePersonId can be excluded as a return value. Optional
	 * result will reflect when no contact was possible. Uses the random generator
	 * associated with the RandomNumberGeneratorId.
	 * 
	 * Note that the BiWeightingFunction must be stable: Repeated invocations of the
	 * function with any fixed set of arguments must result in a single return value
	 * during the scope of this method.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_WEIGHTING_FUNCTION}
	 *                        if the biWeightingFunction is null
	 *                        <li>{@link SimulationErrorType#MALFORMED_WEIGHTING_FUNCTION}
	 *                        if the biWeightingFunction is malformed. (some
	 *                        evaluate to negative numbers,NaN, infinity, etc. --
	 *                        note that if all weights are zero then the optional
	 *                        will return isPresent() of false)
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing RandomNumberGeneratorId found in the
	 *                        scenario.
	 * 
	 */
	public Optional<PersonId> getBiWeightedGroupContactFromGenerator(final GroupId groupId,			
			final BiWeightingFunction biWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId,
			final PersonId sourcePersonId, final boolean excludeSourcePerson
			);

	/**
	 * Returns the set of compartment identifiers as provided during simulation
	 * construction.
	 */
	public <T extends CompartmentId> Set<T> getCompartmentIds();

	/**
	 * Returns the MapOption for the compartments
	 */
	public MapOption getCompartmentMapOption();

	/**
	 * Returns the number of people in given compartment for the given region
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 */
	public int getCompartmentPopulationCount(final CompartmentId compartmentId);

	/**
	 * Returns the simulation time when compartment's population count was last set.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 */
	public double getCompartmentPopulationCountTime(final CompartmentId compartmentId);

	/**
	 * Returns the compartment property definition associated with the given
	 * property identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        compartment property identifier
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the compartment property identifiers supplied to the simulation by
	 * the scenario.
	 */
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given compartment and compartment property identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getCompartmentPropertyTime(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the value associated with the given compartment and property
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *                        if the property is unknown
	 */
	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the set of global component identifiers as provided during simulation
	 * construction.
	 *
	 */
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds();

	/**
	 * Returns the Component class associated with the given GlobalComponentId
	 */
	public Class<? extends Component> getGlobalComponentClass(GlobalComponentId globalComponentId);

	/**
	 * Returns the Component class associated with the given CompartmentId
	 */
	public Class<? extends Component> getCompartmentComponentClass(CompartmentId compartmentId);

	/**
	 * Returns the Component class associated with the given MaterialsProducerId
	 */
	public Class<? extends Component> getMaterialsProducerComponentClass(MaterialsProducerId materialsProducerId);

	/**
	 * Returns the Component class associated with the given RegionId
	 */
	public Class<? extends Component> getRegionComponentClass(RegionId regionId);

	/**
	 * Returns the global property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        global property identifier
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
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *                        if the property id is unknown
	 *
	 */
	public double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the value associated with the given global property identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *                        if the property id is unknown
	 */
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the number of groups associated with the given group type identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the number of groups associated with the given group type identifier
	 * and person identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the number of groups associated with the given person id.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public int getGroupCountForPerson(final PersonId personId);

	/**
	 * Returns the list of all group identifiers.
	 */
	public List<GroupId> getGroupIds();

	/**
	 * Returns the group property definition associated with the given group type,
	 * group and property identifiers
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type id unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the groupPropertyId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the groupPropertyId id unknown
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId);

	/**
	 * Returns the group property identifiers supplied to the simulation by the
	 * scenario for the given group type identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type id is unknown
	 */
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given group and property identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the group's property value for the given property identifier
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the property id is unknown
	 *
	 */
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the list of group identifiers associated with the given group type
	 * identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the list of group identifiers associated with the given group type
	 * identifier and person identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the group type identifiers associated with the given person id and
	 * group id.
	 *
	 * @throws ModelException
	 * 
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId);

	/**
	 * Returns the group type of the given group.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId id is unknown
	 */
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId);

	/**
	 * Returns the number of group types associated the person's groups.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
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
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId);

	/**
	 * Returns a list of person identifiers associated with the indexed population.
	 *
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *
	 */
	public List<PersonId> getIndexedPeople(final Object key);

	/**
	 * Returns the size of an indexed population.
	 *
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 */
	public int getIndexSize(final Object key);

	/**
	 * Returns the batches owned by a particular materials producer that are in
	 * inventory (not staged).
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materialsProducer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materialsProducer id is unknown
	 *
	 */
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the batches having a particular material id and owned by a particular
	 * materials producer that are in inventory (not staged).
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materialsProducer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materialsProducer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        the material id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        the material id is unknown
	 *
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId,
			final MaterialId materialId);

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
	 * Returns the property definition associated with the given materials producer
	 * property identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        materials producer property identifier
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(
			final MaterialsProducerPropertyId materialsProducerPropertyId);

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
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the value associated with the given materials producer and materials
	 * producer property identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials Producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials Producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 *
	 */
	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the materials producer's current resource level for the given
	 * resource identifier.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 */
	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId);

	/**
	 * Returns the simulation time when the materials producer's resource level was
	 * last set for the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *
	 */
	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId);

	/**
	 * Returns a contacted person from the group specified by the groupId by using
	 * the supplied MonoWeightingFunction against all members of the group. Optional
	 * result will reflect when no contact was possible.
	 *
	 * Note that the MonoWeightingFunction must be stable: Repeated invocations of
	 * the function with any fixed set of arguments must result in a single return
	 * value during the scope of this method.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist) *
	 *                        <li>{@link SimulationErrorType#NULL_WEIGHTING_FUNCTION}
	 *                        if the monoWeightingFunction is null
	 *                        <li>{@link SimulationErrorType#MALFORMED_WEIGHTING_FUNCTION}
	 *                        if the monoWeightingFunction is malformed. (some
	 *                        evaluate to negative numbers, etc. -- note that if all
	 *                        weights are zero then the optional will return
	 *                        isPresent() of false)
	 */
	public Optional<PersonId> getMonoWeightedGroupContact(final GroupId groupId,
			final MonoWeightingFunction monoWeightingFunction);

	/**
	 * Returns a contacted person from the group specified by the groupId by using
	 * the supplied MonoWeightingFunction against all members of the group. Optional
	 * result will reflect when no contact was possible.
	 *
	 * Note that the MonoWeightingFunction must be stable: Repeated invocations of
	 * the function with any fixed set of arguments must result in a single return
	 * value during the scope of this method. Uses the random generator associated
	 * with the RandomNumberGeneratorId.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist) *
	 *                        <li>{@link SimulationErrorType#NULL_WEIGHTING_FUNCTION}
	 *                        if the monoWeightingFunction is null
	 *                        <li>{@link SimulationErrorType#MALFORMED_WEIGHTING_FUNCTION}
	 *                        if the monoWeightingFunction is malformed. (some
	 *                        evaluate to negative numbers, etc. -- note that if all
	 *                        weights are zero then the optional will return
	 *                        isPresent() of false)
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing RandomNumberGeneratorId found in the
	 *                        scenario.
	 */
	public Optional<PersonId> getMonoWeightedGroupContactFromGenerator(final GroupId groupId,
			final MonoWeightingFunction monoWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a randomly contacted person from the group specified by the groupId.
	 * Optional result will reflect when no contact was possible.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist)
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId);

	/**
	 * Returns a randomly contacted person from the group specified by the groupId
	 * that will exclude the specified person. The excludedPersonId is not required
	 * to be a member of the group. Optional result will reflect when no contact was
	 * possible.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist)
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excludedPersonId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excludedPersonId is unknown
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId,
			final PersonId excludedPersonId);

	/**
	 * Returns a randomly contacted person from the group specified by the groupId.
	 * Optional result will reflect when no contact was possible. Uses the random
	 * generator associated with the given RandomNumberGeneratorId.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist)
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing RandomNumberGeneratorId found in the
	 *                        scenario.
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId,
			RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a randomly contacted person from the group specified by the groupId
	 * that will exclude the specified person. The excludedPersonId is not required
	 * to be a member of the group. Optional result will reflect when no contact was
	 * possible. Uses the random generator associated with the given
	 * RandomNumberGeneratorId.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist)
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excludedPersonId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excludedPersonId is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing RandomNumberGeneratorId found in the
	 *                        scenario.
	 */
	public Optional<PersonId> getNonWeightedGroupContactWithExclusionFromGenerator(final GroupId groupId,
			RandomNumberGeneratorId randomNumberGeneratorId,final PersonId excludedPersonId);

	/**
	 * Returns an ObservableEnvironment implementor that wraps this Environment
	 *
	 * @return
	 */
	public ObservableEnvironment getObservableEnvironment();

	/**
	 * Returns the offered stage's for the given materials producer.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 */
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the list of person identifier values for all people currently in the
	 * simulation.
	 *
	 */
	public List<PersonId> getPeople();

	/**
	 * Returns the list of people identifiers associated with the given group type
	 * identifier and group identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown(group does not exist)
	 *
	 *
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId);

	/**
	 * Returns the list of people identifiers associated with the given group type
	 * identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the list of person identifier values for all people currently in the
	 * the given compartment.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if the compartment id is
	 * null
	 * <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID} if the compartment is
	 * unknown
	 */
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId);

	/**
	 * Returns the list of person identifier values for all people currently in the
	 * the given region.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId);

	/**
	 * Returns the list of person identifier values for all people currently having
	 * zero units of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if the resource id is null
	 * <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if the resource id is not
	 * known
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId);

	/**
	 * Returns the list of person identifier values for all people currently having
	 * the the given property value for the given person property identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID} if the property id is
	 * null
	 * <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID} if the property id
	 * is unknown
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_VALUE} if the property
	 * value is null
	 * <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if the property value is
	 * not compatible with the property definition
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue);

	/**
	 * Returns the number of people currently having the the given property value
	 * for the given person property identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID} if the property id is
	 * null
	 * <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID} if the property id
	 * is unknown
	 * <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_VALUE} if the property
	 * value is null
	 * <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if the property value is
	 * not compatible with the property definition
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue);

	/**
	 * Returns the list of person identifier values for all people currently having
	 * at least one unit of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if the resource id is null
	 * <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if the resource id is not
	 * known
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId);

	/**
	 * Returns the compartment identifier for the given person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 */
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId);

	/**
	 * Returns the simulation time when the person arrived in their current
	 * compartment. Movement between regions within a single compartment does not
	 * alter this value.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED}
	 *                        if compartment arrival times are not selected for
	 *                        tracking in the scenario
	 * 
	 */
	public double getPersonCompartmentArrivalTime(final PersonId personId);

	/**
	 * Returns true if and only if the simulation is actively tracking compartment
	 * arrival times for people.
	 *
	 * @return
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy();

	/**
	 * Returns the number of people associated with the given group type identifier
	 * and group identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *
	 *
	 */
	public int getPersonCountForGroup(final GroupId groupId);

	/**
	 * Returns the number of people associated with the given group type identifier.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group Type id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group Type id is unknown
	 *
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the person property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        person property identifier
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
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *                        if person property times are not selected for tracking
	 *                        in the scenario
	 *
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Returns the value associated with the given person and property identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property id is not a person property
	 */
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId);

	/**
	 * Returns the region identifier for the given person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 */
	public <T extends RegionId> T getPersonRegion(final PersonId personId);

	/**
	 * Returns the simulation time when the person arrived in their current region.
	 * Movement between compartments within a single region does not alter this
	 * value.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#REGION_ARRIVAL_TIMES_NOT_TRACKED}
	 *                        if person region arrival times are not selected for
	 *                        tracking in the scenario
	 * 
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
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 */
	public long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId);

	/**
	 * Returns the simulation time when the person's resource level was last set for
	 * the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED}
	 *                        if person resource assignment times are not selected
	 *                        for tracking in the scenario
	 *
	 */
	public double getPersonResourceTime(final PersonId personId, final ResourceId resourceId);

	/**
	 * Returns true if and only if the simulation is actively tracking resource
	 * value assignment times for the given resource id.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *
	 * @param resourceId
	 * @return
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId);

	/**
	 * Retrieves a plan that was added with the given key. The returned play remains
	 * scheduled.
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_PLAN_KEY} if the
	 *                        key is null
	 */
	public <T> Optional<T> getPlan(final Object key);

	/**
	 * Returns the planned execution time for a scheduled plan that was added with
	 * the given key. Returns a negative value if the plan cannot be found.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PLAN_KEY} if the
	 *                        key is null
	 */

	public double getPlanTime(final Object key);

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
	 * Returns the standard RandomGenerator instance from the simulation. This
	 * RandomGenerator is initialized with a seed value during simulation
	 * construction.
	 */
	public RandomGenerator getRandomGenerator();

	/**
	 * Returns the RandomGenerator instance from the simulation associated with the
	 * given RandomNumberGeneratorId. This RandomGenerator is initialized with a
	 * seed value during simulation construction that is a hash of the replication
	 * seed and the randomNumberGeneratorId.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing RandomNumberGeneratorId found in the
	 *                        scenario.
	 */
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a randomly selected person identifier from an indexed population.
	 * Returns null if the population index is empty. See the
	 * createPopulationIndex() method for activating the index from the various
	 * prepare*() methods.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 */
	public Optional<PersonId> getRandomIndexedPerson(final Object key);

	/**
	 * Returns a randomly selected person identifier from an indexed population.
	 * Returns null if the population index is empty. See the
	 * createPopulationIndex() method for activating the index from the various
	 * prepare*() methods.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 * 
	 */
	public Optional<PersonId> getRandomIndexedPersonFromGenerator(final Object key,
			RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a randomly selected person identifier from an indexed population
	 * excluding the given person identifier if that parameter is not null. Returns
	 * null if the population index is empty or contains only the excluded person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 */
	public Optional<PersonId> getRandomIndexedPersonWithExclusion(final PersonId excludedPersonId, final Object key);

	/**
	 * Returns a randomly selected person identifier from an indexed population
	 * excluding the given person identifier if that parameter is not null. Returns
	 * null if the population index is empty or contains only the excluded person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 */
	public Optional<PersonId> getRandomIndexedPersonWithExclusionFromGenerator(final PersonId excludedPersonId,
			final Object key, RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns the set of region component identifiers as provided during simulation
	 * construction.
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
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 */
	public int getRegionPopulationCount(final RegionId regionId);

	/**
	 * Returns the simulation time when region's population count was last set.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 */
	public double getRegionPopulationCountTime(final RegionId regionId);

	/**
	 * Returns the region property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        region property identifier
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
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Returns the value associated with the given region and region property
	 * identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 *
	 */
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId);

	/**
	 * Returns the region's current resource level for the given resource
	 * identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 */
	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId);

	/**
	 * Returns the simulation time when the region's resource level was last set for
	 * the given resource identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
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
	 * Returns the resource property definition associated with the given property
	 * identifier
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        resource id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *                        if the property id does not correspond to a known
	 *                        resource property identifier
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the resource property identifiers supplied to the simulation by the
	 * scenario.
	 * 
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        resource id is unknown
	 */
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId);

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given resource and resource property identifiers.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *                        if the property is unknown
	 *
	 */
	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the value associated with the given resource and property identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *                        if the property is unknown
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
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if the
	 *                        stageId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        the stageId id is unknown
	 *
	 */
	public List<BatchId> getStageBatches(final StageId stageId);

	/**
	 * Returns the batches having the given material type associated with a
	 * particular stage.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if the
	 *                        stageId id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        the stageId id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if
	 *                        the material id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if
	 *                        the material id is unknown
	 *
	 */
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId);

	/**
	 * Returns the stage's materials producer
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if the
	 *                        stage id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        the stage id is unknown
	 *
	 */
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId);

	/**
	 * Returns the stage's for the given materials producer.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 */
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the suggested population size from the scenario.
	 *
	 */
	public int getSuggestedPopulationSize();

	/**
	 * Returns the current time. Time is measured in days and initializes to zero.
	 * Time progresses via planning.
	 *
	 */
	public double getTime();

	/**
	 * Returns true if and only if the group associated with the given group id
	 * exists. Tolerates null.
	 */
	public boolean groupExists(final GroupId groupId);

	/**
	 * Gracefully stops the processing of plans and observations, allowing the
	 * simulation to finalize state change listeners.
	 */
	public void halt();

	/**
	 * Returns true if and only if the person is a member of the group identified by
	 * the group type and group identifiers.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 */
	public boolean isGroupMember(final PersonId personId, final GroupId groupId);

	/**
	 * Returns the stage's offer state. Offered stages cannot be altered until they
	 * are no longer offered
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if the
	 *                        stage id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        the stage id is unknown
	 *
	 */
	public boolean isStageOffered(final StageId stageId);

	/**
	 * Disassociates a batch from its current stage .The batch must owned by the
	 * invoking materials producer and the its stage may not be in the offered
	 * state.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if batch
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        batch id is unknown
	 *                        <li>{@link SimulationErrorType#BATCH_NOT_STAGED} if
	 *                        batch id is not associated with a stage
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if batch's stage is in the offered state
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 */
	public void moveBatchToInventory(final BatchId batchId);

	/**
	 * Associates a batch with a stage, both of which are owned by the invoking
	 * materials producer. The batch must be in the inventory of the invoking
	 * materials producer and not associated with a stage and the stage must not be
	 * in an offered state.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if batch
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        batch id is unknown
	 *                        <li>{@link SimulationErrorType#BATCH_ALREADY_STAGED}
	 *                        if batch id is already associated with a stage
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if stage is in an offered state
	 *                        <li>{@link SimulationErrorType# BATCH_STAGED_TO_DIFFERENT_OWNER}
	 *                        if the batch and stage are owned by different
	 *                        materials producers
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component of the batch
	 * 
	 *
	 */
	public void moveBatchToStage(final BatchId batchId, final StageId stageId);

	/**
	 * Starts or stops observation of property value changes on all people in a
	 * particular compartment for the calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops observation of resource level changes on all people in the
	 * given compartment for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId,
			final ResourceId resourceId);

	/**
	 * Starts or stops observation of people arriving in the given compartment for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeCompartmentPersonArrival(final boolean observe, final CompartmentId compartmentId);

	/**
	 * Starts or stops observation of people departing in the given compartment for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeCompartmentPersonDeparture(final boolean observe, final CompartmentId compartmentId);

	/**
	 * Starts or stops observation of property value changes on the given
	 * compartment for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Starts or stops observation of people being added to the simulation for the
	 * calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGlobalPersonArrival(final boolean observe);

	/**
	 * Starts or stops observation of people being removed from the simulation for
	 * the calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGlobalPersonDeparture(final boolean observe);

	/**
	 * Starts or stops observation of property value changes on all people for the
	 * calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops observation of resource level changes on all people for the
	 * calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId);

	/**
	 * Starts or stops observation of the given global property for the calling
	 * component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *
	 */
	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId);

	/**
	 * Starts or stops observation of property value changes on the given materials
	 * producer for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeMaterialsProducerPropertyChange(final boolean observe,
			final MaterialsProducerId materialProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Starts or stops observation of resource level changes on the given materials
	 * producer for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeMaterialsProducerResourceChangeByMaterialsProducerId(final boolean observe,
			final MaterialsProducerId materialsProducerId, final ResourceId resourceId);

	/**
	 * Starts or stops observation of resource level changes on all materials
	 * producers for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId);

	/**
	 * Starts or stops observation of compartment changes on the given person for
	 * the calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observePersonCompartmentChange(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of property value changes on the given person for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observePersonPropertyChange(final boolean observe, final PersonId personId,
			final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops observation of region changes on the given person for the
	 * calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observePersonRegionChange(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of resource level changes on the given person for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observePersonResourceChange(final boolean observe, final PersonId personId,
			final ResourceId resourceId);

	/**
	 * Starts or stops observation of people arriving in the given region for the
	 * calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionPersonArrival(final boolean observe, final RegionId regionId);

	/**
	 * Starts or stops observation of people departing in the given region for the
	 * calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionPersonDeparture(final boolean observe, final RegionId regionId);

	/**
	 * Starts or stops observation of property value changes on all people in a
	 * particular region for the calling component.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId,
			final PersonPropertyId personPropertyId);

	/**
	 * Starts or stops observation of resource level changes on all people in the
	 * given region for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId);

	/**
	 * Starts or stops observation of property value changes on the given region for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId,
			final RegionPropertyId regionPropertyId);

	/**
	 * Starts or stops observation of property value changes for all regions for the
	 * calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId);

	/**
	 * Starts or stops observation of resource level changes on the given region for
	 * the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeRegionResourceChange(final boolean observe, final RegionId regionId,
			final ResourceId resourceId);

	/**
	 * Starts or stops observation of property value changes on the given resource
	 * for the calling component.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *                        if the property is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId,
			final ResourcePropertyId resourcePropertyId);

	/**
	 * Starts or stops observation of changes to the offer state of all stages.
	 *
	 * @throws ModelException
	 *
	 * 
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeStageOfferChange(final boolean observe);

	/**
	 * Starts or stops observation of changes to the offer state of the stage.
	 *
	 * @throws ModelException
	 *
	 * 
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId);

	/**
	 * Starts or stops observation of the transfer of all stages.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *
	 */
	public void observeStageTransfer(final boolean observe);

	/**
	 * Starts or stops observation of the transfer of a stage
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if the
	 *                        stage id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        the stage id is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeStageTransferByStageId(final boolean observe, final StageId stageId);

	/**
	 * Starts or stops observation of the transfer of stages from the given source
	 * materials producer
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the source materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the source materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeStageTransferBySourceMaterialsProducerId(final boolean observe,
			final MaterialsProducerId sourceMaterialsProducerId);

	/**
	 * Starts or stops observation of the transfer of stages to the given
	 * destination materials producer
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the destination materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the destination materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeStageTransferByDestinationMaterialsProducerId(final boolean observe,
			final MaterialsProducerId destinationMaterialsProducerId);

	/**
	 * Returns true if and only if the given person identifier is associated with a
	 * person in the simulation. Tolerates null person id.
	 *
	 */
	public boolean personExists(final PersonId personId);

	/**
	 *
	 * Returns true if and only if the person is associated with the index specified
	 * by the population index keys.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 * 
	 */
	public boolean personIsInPopulationIndex(final PersonId personId, final Object key);

	/**
	 * Returns true if and only if the a population index exists with the given key.
	 * Tolerates null key.
	 */
	public boolean populationIndexExists(final Object key);

	/**
	 * Removes the group associated with the given group type and group identifiers.
	 * People associated with the group are removed from the group.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is not a global component, a
	 *                        region component or a compartment component
	 */
	public void removeGroup(final GroupId groupId);

	/**
	 * Removes the person associated with the given person identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not the compartment containing the
	 *                        person
	 *
	 */
	public void removePerson(final PersonId personId);

	/**
	 * Removes a person to the group associated with the given group type and group
	 * identifiers.
	 *
	 * @throws ModelException
	 *                        <li>{@link ErrorType#} if the group id is null
	 *                        <li>{@link ErrorType#} if the group id is
	 *                        unknown(group does not exist)
	 *                        <li>{@link ErrorType#} if the person id is null
	 *                        <li>{@link ErrorType#} if the person id is unknown
	 *                        <li>{@link ErrorType#} if the person is not a member
	 *                        of the group
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoking component is no either a global
	 *                        component, a region or a compartment
	 */
	public void removePersonFromGroup(final PersonId personId, final GroupId groupId);

	/**
	 * Removes a plan. The plan is identified by the key provided. Returns an empty
	 * optional if the plan cannot be found.
	 *
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PLAN_KEY} if the
	 *                        key is null
	 *
	 */
	public <T> Optional<T> removePlan(final Object key);

	/**
	 * Removes an indexed population.
	 *
	 * Population indices are prepared and created in a multi-step process. First,
	 * filtering conditions are collected on behalf of the invoking component via
	 * the prepareXXX() methods. Then, the createPopulationIndex() is invoked to
	 * gather those filters and create a population index that is identified by keys
	 * supplied by the component. Once created, the filters for the index cannot be
	 * updated, but the simulation dynamically maintains the people associated with
	 * the index as conditions on the population change.
	 *
	 * Once the index is added via createPopulationIndex() the component may add a
	 * new set of filters via the prepareXXX() again.
	 *
	 * Only the component that creates an index may remove that index, but other
	 * components may access the index if they know its keys.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#INDEXED_POPULATION_DELETION_BY_NON_OWNER}
	 *                        if the invoker is not the component that created the
	 *                        index
	 *
	 */
	public void removePopulationIndex(final Object key);

	/**
	 * Removes the amount of resource to the given person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the amount the person
	 *                        possesses
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a global component or the person's
	 *                        region or the person's compartment
	 *
	 */
	public void removeResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	/**
	 * Removes the amount of resource to the given region.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the amount the region
	 *                        possesses
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a global component or the given
	 *                        region
	 *
	 */
	public void removeResourceFromRegion(final ResourceId resourceId, final RegionId regionId, final long amount);

	/**
	 * Sets a property value on the indicated batch.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if batch
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        batch id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_PROPERTY_ID}
	 *                        if property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_PROPERTY_ID}
	 *                        if property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if the batch is part of an offered stage
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 */
	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId,
			final Object batchPropertyValue);

	/**
	 * Sets property value for the given compartment and property. Compartment
	 * properties may only be set by the owning compartment.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component of the given
	 *                        compartment
	 *
	 */
	public void setCompartmentPropertyValue(final CompartmentId compartmentId,
			final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue);

	/**
	 * Sets property value for the given global property identifier.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component
	 *
	 */
	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue);

	/**
	 * Sets a property value on the indicated group.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if property id is null
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component, region
	 *                        component or compartment component
	 *
	 *
	 */
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId,
			final Object groupPropertyValue);

	/**
	 * Sets property value for the given materials producer and property.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component or the given
	 *                        materials producer
	 *
	 */
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId, final Object materialsProducerPropertyValue);

	/**
	 * Sets the person's compartment. Compartment assignment may only be set by the
	 * owning compartment, except for person creation.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if
	 *                        the compartment id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID}
	 *                        if the compartment id is unknown
	 *                        <li>{@link SimulationErrorType#SAME_COMPARTMENT} if
	 *                        the compartment id is currently assigned to the person
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component or the
	 *                        person's current compartment
	 *
	 */
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId);

	/**
	 * Sets property value for the given person and property.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_VALUE}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component or the the
	 *                        person's current region or the the person's current
	 *                        compartment
	 *
	 */
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId,
			final Object personPropertyValue);

	/**
	 * Sets the person's region. Region assignment may only be set by the owning
	 * compartment, except for person creation.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#SAME_REGION} if the
	 *                        region is the current region for the person
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component or the
	 *                        person's current region
	 *
	 */
	public void setPersonRegion(final PersonId personId, final RegionId regionId);

	/**
	 * Sets property value for the given region and property.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component or the given
	 *                        region
	 *
	 */
	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId,
			final Object regionPropertyValue);

	/**
	 * Sets property value for the given resource and property.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_ID}
	 *                        if the property id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *                        if the property id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_PROPERTY_VALUE}
	 *                        if the value is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_VALUE} if
	 *                        the value is incompatible with the defined type for
	 *                        the property
	 *                        <li>{@link SimulationErrorType#IMMUTABLE_VALUE} if the
	 *                        property has been defined as immutable
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if the invoker is not a global component
	 *
	 */
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,
			final Object resourcePropertyValue);

	/**
	 * Sets the offer state for a stage owned by the invoking materials producer. An
	 * offered stage is available for transfer to another materials producer, but
	 * has batches that cannot be mutated until the stage's offered state is either
	 * set to false or is transferred to another materials producer.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component
	 *
	 */
	public void setStageOffer(final StageId stageId, final boolean offer);

	/**
	 * Transfers the given amount from one batch to another. The batches must be
	 * distinct and owned by the invoking materials producer component and neither
	 * may be part of an offered stage.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        source batch id is null
	 *                        <li>{@link SimulationErrorType#NULL_BATCH_ID} if the
	 *                        destination batch id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the source batch id is unknown
	 *                        <li>{@link SimulationErrorType#UNKNOWN_BATCH_ID} if
	 *                        the destination batch id is unknown
	 *                        <li>{@link SimulationErrorType#REFLEXIVE_BATCH_SHIFT}
	 *                        if the source and destination batch ids are equal
	 *                        <li>{@link SimulationErrorType#MATERIAL_TYPE_MISMATCH}
	 *                        if the material ids of the batches are not equal
	 *                        <li>{@link SimulationErrorType#BATCH_SHIFT_WITH_MULTIPLE_OWNERS}if
	 *                        the batches are owned by different material producers
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if the source batch is part of an offered stage
	 *                        <li>{@link SimulationErrorType#OFFERED_STAGE_UNALTERABLE}
	 *                        if the destination batch is part of an offered stage
	 *                        <li>{@link SimulationErrorType#NEGATIVE_MATERIAL_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType.NON_FINITE_MATERIAL_AMOUNT}
	 *                        if the amount is not finite
	 *                        <li>{@link SimulationErrorType.MATERIAL_NON_FINITE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in a non finite amount in the
	 *                        receiving batch
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_MATERIAL_AVAILABLE}
	 *                        if the amount exceeds the capacity of the source batch
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component for both batches
	 */
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount);

	/**
	 * Returns true if and only if there is a stage with the given id. Tolerates
	 * null stage id.
	 */
	public boolean stageExists(final StageId stageId);

	/**
	 * Transfers an offered stage to the provided materials producer. Once
	 * transferred, the stage will not be in the offered state.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_STAGE_ID} if stage
	 *                        id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if
	 *                        stage id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if the materials producer is unknown
	 *                        <li>{@link SimulationErrorType#UNOFFERED_STAGE_NOT_TRANSFERABLE}
	 *                        if stage is not in the offered state
	 *                        <li>{@link SimulationErrorType#REFLEXIVE_STAGE_TRANSFER}
	 *                        if the material producer is the invoking materials
	 *                        producer
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a materials producer component or a
	 *                        global component
	 *
	 */
	public void transferOfferedStageToMaterialsProducer(final StageId stageId,
			final MaterialsProducerId materialsProducerId);

	/**
	 * Transfers an amount of resource from a materials producer to a region.
	 *
	 * @throws ModelException
	 *
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if
	 *                        region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        resource id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID}
	 *                        if materials producer id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                        if materials producer id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in an overflow of the
	 *                        corresponding region's inventory level
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the the resource level
	 *                        of the materials producer
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not the owning materials producer
	 *                        component, the receiving region or a global component
	 *
	 */
	public void transferProducedResourceToRegion(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final RegionId regionId, final long amount);

	/**
	 * Transfers the amount of resource from one region to another
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        source region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the source region id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_REGION_ID} if the
	 *                        destination region id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if
	 *                        the destination region id is unknown
	 *                        <li>{@link SimulationErrorType#REFLEXIVE_RESOURCE_TRANSFER}
	 *                        if the source region id and the destination region are
	 *                        the same
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in an overflow of the
	 *                        corresponding destination region's inventory level
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the amount the source
	 *                        region possesses
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a global component
	 *
	 */
	public void transferResourceBetweenRegions(final ResourceId resourceId, final RegionId sourceRegionId,
			final RegionId destinationRegionId, final long amount);

	/**
	 * Transfers the amount of resource from the given person back to their region.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount results in an overflow of the
	 *                        corresponding region's inventory level
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the amount the person
	 *                        possesses
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not a global component or the person's
	 *                        region or the person's compartment
	 *
	 */
	public void transferResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	/**
	 * Transfers the amount of resource to the given person back from their region.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if
	 *                        the resource id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the resource id is unknown
	 *                        <li>{@link SimulationErrorType#NEGATIVE_RESOURCE_AMOUNT}
	 *                        if the amount is negative
	 *                        <li>{@link SimulationErrorType#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                        if the amount is negative if the amount results in an
	 *                        overflow of the corresponding person's inventory level
	 *                        <li>{@link SimulationErrorType#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                        if the amount is in excess of the amount the region
	 *                        possesses
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if invoker is not global component , the person's
	 *                        region or the person's compartment
	 *
	 */
	public void transferResourceToPerson(final ResourceId resourceId, final PersonId personId, final long amount);

	/**
	 * Returns the ComponentId of the currently active Component
	 */
	public <T extends ComponentId> T getCurrentComponentId();

	/**
	 * Releases an output item to the OutputItemManger
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_OUTPUT_ITEM} if
	 *                        the output item is null
	 *                        <li>{@link SimulationErrorType#INCORRECT_SCENARIO_ID_FOR_OUTPUT_ITEM}
	 *                        if the output item does not have a scenario id that
	 *                        matches the simulation that produces it.
	 *                        <li>{@link SimulationErrorType#INCORRECT_REPLICATION_ID_FOR_OUTPUT_ITEM}
	 *                        if the output item does not have a replication id that
	 *                        matches the simulation that produces it.
	 */
	public void releaseOutputItem(OutputItem outputItem);

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGroupArrival(final boolean observe);

	/**
	 * Starts or stops observation of the arrival of the given person into any group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 */
	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 * having the given group type
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 */
	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the arrival of any person into the given group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId is unknown
	 */
	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of the arrival of the given person into any group
	 * having the given group type
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 */
	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId);

	/**
	 * Starts or stops observation of the arrival of the given person into the given
	 * group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 */
	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId);

	/**
	 * Starts or stops observation of the departure of any person from any group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */

	public void observeGroupDeparture(final boolean observe);

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 */
	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId);

	/**
	 * Starts or stops observation of the departure of any person from any group
	 * having the given group type
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 */
	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the departure of any person from the given
	 * group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId is unknown
	 */
	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group having the given group type
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 */
	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId,
			final PersonId personId);

	/**
	 * Starts or stops observation of a change to a population index
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_INDEX_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_INDEX_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 */
	public void observePopulationIndexChange(final boolean observe, final Object key);

	/**
	 * Starts or stops observation of the departure of the given person from the
	 * given group
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        personId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the personId is unknown
	 * 
	 * @throws ModelException
	 *
	 */
	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId,
			final PersonId personId);

	/**
	 * Starts or stops observation of all group construction
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGroupConstruction(final boolean observe);

	/**
	 * Starts or stops observation of group construction for groups having the given
	 * group type
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 */
	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of all group destruction
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 */
	public void observeGroupDestruction(final boolean observe);

	/**
	 * Starts or stops observation of group destruction for the given group
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        groupId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the groupId is unknown
	 */
	public void observeGroupDestructionByGroup(final boolean observe, GroupId groupId);

	/**
	 * Starts or stops observation of group destruction for groups having the given
	 * group type
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the groupTypeId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the groupTypeId is unknown
	 */
	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of all group property changes for all groups
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 * 
	 */
	public void observeGroupPropertyChange(final boolean observe);

	/**
	 * Starts or stops observation of all group property changes for all groups of
	 * the given group type
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group type is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type is unknown
	 * 
	 */
	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId);

	/**
	 * Starts or stops observation of the given group property changes for all
	 * groups of the given group type
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_TYPE_ID} if
	 *                        the group type is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_TYPE_ID}
	 *                        if the group type is unknown *
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the group property id null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the group property id is unknown
	 * 
	 */
	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId);

	/**
	 * Starts or stops observation of all group property changes for the given group
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the group property id null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the group property id is unknown
	 * 
	 */
	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId);

	/**
	 * Starts or stops observation of all group property changes for the given group
	 * property and group
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NO_ACTIVE_COMPONENT} if
	 *                        invoker is not a component
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_ID} if the
	 *                        group id is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_ID} if
	 *                        the group id is unknown
	 *                        <li>{@link SimulationErrorType#NULL_GROUP_PROPERTY_ID}
	 *                        if the group property id null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_GROUP_PROPERTY_ID}
	 *                        if the group property id is unknown
	 * 
	 */
	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupId groupId,
			final GroupPropertyId groupPropertyId);

	/**
	 * If the profile report is active, this method returns a proxy instance of the
	 * given instance that implements all of that instance's interface
	 * implementations. Otherwise, it returns the given instance. The proxied
	 * methods will track profiling information for the replaced instance. Any
	 * non-interface defined methods on the original instance will throw an
	 * exception if invoked. Each interface that is implemented by the instance must
	 * be defined in top level classes and not as an internal part of another class.
	 */
	public <T> T getProfiledProxy(T instance);

	/**
	 * Returns the set of plan keys for the current Component as a list. Items on
	 * the list are unique.
	 */
	public List<Object> getPlanKeys();

	/**
	 * Returns the set of random number generator identifiers as provided during
	 * simulation construction.
	 *
	 */
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds();

	/**
	 * Adds a global component dynamically to the simulation
	 * 
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_GLOBAL_COMPONENT_ID}
	 *                        if the component id is null
	 * 
	 */
	public void addGlobalComponent(GlobalComponentId globalComponentId,
			Class<? extends Component> globalComponentClass);

	/**
	 * Returns a list of person identifiers associated with the population
	 * partition.
	 *
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null *
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 *
	 * 
	 */
	public List<PersonId> getPartitionPeople(final Object key, LabelSet labelSet);

	/**
	 * Adds a population partition using the supplied population partition
	 * definition.
	 *
	 * Only the component that creates a partition may remove that partition, but
	 * other components may access the partition if they know its key. The resulting
	 * population partition is actively maintained by the GCM and is accessed by the
	 * key provided. This index is owned by the component that created it in that
	 * only that component may remove it from the Environment. However, all
	 * components have access to the partition via its key.
	 *
	 *
	 * @throws ModelException *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_DEFINITION}
	 *                        if the population partition definition is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#DUPLICATE_POPULATION_PARTITION}
	 *                        if the key corresponds to an existing population
	 *                        partition
	 *                        <li>{@link SimulationErrorType#COMPONENT_LACKS_PERMISSION}
	 *                        if this method is invoked while the simulation has no
	 *                        active component
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_PROPERTY_ID}
	 *                        if the definition contains an unknown person property
	 *                        id
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if
	 *                        the definition contains an unknown resource id
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public void addPopulationPartition(Partition partition, Object key);

	/**
	 * Returns the size of an indexed population.
	 *
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null *
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 */
	public int getPartitionSize(final Object key, LabelSet labelSet);

	/**
	 * Removes an indexed population.
	 *
	 * Population indices are prepared and created in a multi-step process. First,
	 * filtering conditions are collected on behalf of the invoking component via
	 * the prepareXXX() methods. Then, the createPopulationIndex() is invoked to
	 * gather those filters and create a population index that is identified by keys
	 * supplied by the component. Once created, the filters for the index cannot be
	 * updated, but the simulation dynamically maintains the people associated with
	 * the index as conditions on the population change.
	 *
	 * Once the index is added via createPopulationIndex() the component may add a
	 * new set of filters via the prepareXXX() again.
	 *
	 * Only the component that creates an index may remove that index, but other
	 * components may access the index if they know its keys.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#POPULATION_PARTITION_DELETION_BY_NON_OWNER}
	 *                        if the invoker is not the component that created the
	 *                        index
	 *
	 */
	public void removePopulationPartition(final Object key);

	/**
	 * Returns true if and only if the a population partition exists with the given
	 * key. Tolerates null key.
	 */
	public boolean populationPartitionExists(final Object key);

	/**
	 *
	 * Returns true if and only if the person is associated with the index specified
	 * by the population index keys.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        person is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null *
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 * 
	 */
	public boolean personIsInPopulationPartition(final PersonId personId, final Object key, final LabelSet labelSet);

	/**
	 * Returns a randomly selected person identifier from a population partition.
	 * Returns null if the population partition is empty.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population index
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 */
	public Optional<PersonId> samplePartition(final Object key, final LabelSet labelSet);

	/**
	 * Returns a randomly selected person identifier from a population partition.
	 * Returns empty optional if the population partition is empty.
	 *
	 * @throws ModelException
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 * 
	 */
	public Optional<PersonId> samplePartition(final Object key, final LabelSet labelSet,
			RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a randomly selected person identifier from a population partition
	 * excluding the given person identifier if that parameter is not null. Returns
	 * null if the population partition is empty or contains only the excluded
	 * person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 * 
	 */
	public Optional<PersonId> samplePartition(final Object key, final LabelSet labelSet,final PersonId excludedPersonId);

	/**
	 * Returns a randomly selected person identifier from a population partition
	 * excluding the given person identifier if that parameter is not null. Returns
	 * null if the population partition is empty or contains only the excluded
	 * person.
	 *
	 * @throws ModelException
	 *
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#NULL_LABEL_SET} if the
	 *                        label set is null
	 *                        <li>{@link SimulationErrorType#INCOMPATIBLE_LABEL_SET}
	 *                        if the label set is incompatible with the population
	 *                        partition
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 */
	public Optional<PersonId> samplePartition(final Object key, final LabelSet labelSet, RandomNumberGeneratorId randomNumberGeneratorId,final PersonId excludedPersonId);

	/**
	 * Returns a random person from the partition using the given
	 * {@link LabelSetWeightingFunction}
	 * 
	 * @throws ModelException

	 *                        <li>
	 *                        {@link SimulationErrorType#NULL_WEIGHTING_FUNCTION} if
	 *                        the weighting function is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key for the partition is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 * 
	 */
	public Optional<PersonId> samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction);
	
	/**
	 * Returns a random person from the partition using the given
	 * {@link LabelSetWeightingFunction}
	 * 
	 * @throws ModelException

	 *                        <li>
	 *                        {@link SimulationErrorType#NULL_WEIGHTING_FUNCTION} if
	 *                        the weighting function is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key for the partition is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 * 
	 */
	public Optional<PersonId> samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction,final PersonId excludedPersonId);

	/**
	 * Returns a random person from the partition using the given
	 * {@link LabelSetWeightingFunction} and {@link RandomNumberGeneratorId}
	 * 
	 * @throws ModelException
	 *                        <li>
	 *                        {@link SimulationErrorType#NULL_WEIGHTING_FUNCTION} if
	 *                        the weighting function is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key for the partition is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 * 
	 */
	public Optional<PersonId> samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Returns a random person from the partition using the given
	 * {@link LabelSetWeightingFunction} and {@link RandomNumberGeneratorId}
	 * 
	 * @throws ModelException
	 *                        <li>
	 *                        {@link SimulationErrorType#NULL_WEIGHTING_FUNCTION} if
	 *                        the weighting function is null
	 *                        <li>{@link SimulationErrorType#NULL_POPULATION_PARTITION_KEY}
	 *                        if the key for the partition is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                        if the key does not correspond to an existing
	 *                        population partition
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null                        
	 *                        <li>{@link SimulationErrorType#UNKNOWN_PERSON_ID} if
	 *                        the excluded person is unknown
	 *                        <li>{@link SimulationErrorType#NULL_PERSON_ID} if the
	 *                        excluded person is null
	 *                        <li>{@link SimulationErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId is null
	 *                        <li>{@link SimulationErrorType#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                        if the randomNumberGeneratorId does not correspond to
	 *                        an existing random Number Generator Id in the scenario
	 * 
	 */
	public Optional<PersonId> samplePartition(Object key,
			LabelSetWeightingFunction labelSetWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId,final PersonId excludedPersonId);

}