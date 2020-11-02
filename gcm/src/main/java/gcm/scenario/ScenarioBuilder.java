package gcm.scenario;

import gcm.components.Component;
import gcm.scenario.ScenarioException.ScenarioErrorType;
import gcm.simulation.SimulationErrorType;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * An interface for Scenario Builders.
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
@Source
public interface ScenarioBuilder {

	/**
	 * Adds a batch to the scenario for the given materials producer
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID}if the material
	 *             id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID}if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the amount is not finite
	 *             <li>{@link ScenarioErrorType#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative and finite
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the batch id was previously added
	 * 
	 */
	public void addBatch(final BatchId batchId, MaterialId materialId, double amount, MaterialsProducerId materialsProducerId);

	/**
	 * Associates a batch with a stage
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_STAGE_ID} if the stage id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#BATCH_STAGED_TO_DIFFERENT_OWNER}
	 *             if the stage and batch are not associated with the same
	 *             materials producer
	 *             <li>{@link ScenarioErrorType#BATCH_ALREADY_STAGED} if the
	 *             batch is already associated any stage
	 * 
	 */
	public void addBatchToStage(final StageId stageId, final BatchId batchId);

	/**
	 * Adds a compartment id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the compartment id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the compartment id is equal to another previously added
	 *             component id
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_CLASS} if the
	 *             comparmentComponentClass is null
	 */
	public void addCompartmentId(final CompartmentId compartmentId, Class<? extends Component> comparmentComponentClass);

	/**
	 * Adds the global component id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the GlobalComponentId is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the global component id is equal to another previously added
	 *             component id *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_CLASS} if the
	 *             globalComponentClass is null
	 */
	public void addGlobalComponentId(final GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass);

	/**
	 * Adds a group to the scenario with the given group type id.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the group was previously added
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown
	 * 
	 */
	public void addGroup(final GroupId groupId, final GroupTypeId groupTypeId);

	/**
	 * Adds a group type identifier to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the group type was previously added
	 */
	public void addGroupTypeId(final GroupTypeId groupTypeId);

	/**
	 * Adds a material to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the material was previously added
	 *
	 */
	public void addMaterial(final MaterialId materialId);

	/**
	 * Adds a random number generator id to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random generator id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the random number generator id was previously added
	 *
	 */
	public void addRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Adds a materials producer component id to the scenario. This identifier
	 * informs the simulation that a component having this identifier is
	 * expected to exist. Component identifiers must be unique without regard to
	 * the type of component.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the materials producer id is equal to another previously
	 *             added component id
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_CLASS} if the
	 *             materialProducerComponentClass is null
	 */
	public void addMaterialsProducerId(final MaterialsProducerId materialsProducerId, Class<? extends Component> materialProducerComponentClass);

	/**
	 * Adds a person to the simulation in the given region and compartment.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the person was previously added
	 *
	 */
	public void addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId);

	/**
	 * Adds a person to a group in the scenario
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GROUP_MEMBERSHIP} if
	 *             the person was previously added to the group
	 *
	 */
	public void addPersonToGroup(final GroupId groupId, final PersonId personId);

	/**
	 * Adds a region component id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the region id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the region was previously added *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_CLASS} if the
	 *             regionComponentClass is null
	 * 
	 */
	public void addRegionId(final RegionId regionId, Class<? extends Component> regionComponentClass);

	/**
	 * Adds a resource to the scenario
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource was previously added
	 */
	public void addResource(final ResourceId resourceId);

	/**
	 * Adds a stage to the scenario for the given materials producer with the
	 * given offer state
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_STAGE_ID} if the stage id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the stage id was previously added
	 */
	public void addStage(final StageId stageId, boolean offered, MaterialsProducerId materialsProducerId);

	/**
	 * Returns the scenario instance and resets this builder's state.
	 * 
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if any property values cannot be determined from direct
	 *             property value contributions or default property values
	 *             associated with property definitions.
	 */
	public Scenario build();

	/**
	 * Defines a batch property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_BATCH_PROPERTY_DEFINITION}
	 *             if the batch property was previously defined
	 */
	public void defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a compartment property.
	 *
	 * @throws ScenarioException
	 *             <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION}
	 *             if the compartment property was previously defined
	 */
	public void defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a global property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
	 *             if the global property was previously defined
	 */
	public void defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a group property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GROUP_PROPERTY_DEFINITION}
	 *             if the group property was previously defined
	 */
	public void defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a materials producer property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the materials producer property was previously defined
	 */
	public void defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a person property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_PERSON_PROPERTY_DEFINITION}
	 *             if the person property was previously defined
	 */
	public void definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a regional property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the region property was previously defined
	 */
	public void defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a resource property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_RESOURCE_PROPERTY_DEFINITION}
	 *             if the resource property was previously defined
	 */
	public void defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Sets a batch property value for the given batch.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the batch property value was previously set
	 */
	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue);

	/**
	 * Sets the mapping option for all compartments. Defaulted to NONE.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_MAP_OPTION} if
	 *             the mapOption is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the mapOption was previously set
	 */
	public void setCompartmentMapOption(final MapOption mapOption);

	/**
	 * Sets a compartment property value for the given property.
	 *
	 * @throws ScenarioException
	 * 
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the compartment property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_VALUE}
	 *             if the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment property value was previously set
	 */
	public void setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue);

	/**
	 * Sets a global property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the global property value was previously set
	 *
	 */

	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue);

	/**
	 * Sets the group property value for the given group
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             group property is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_PROPERTY_ID} if
	 *             the group property is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the group property value was previously set
	 */
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue);

	/**
	 * Sets a materials producer property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE}
	 *             if the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the materials producer property value was previously set
	 */
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue);

	/**
	 * Sets a materials producer's initial resource level.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the materials producer resource level was previously set
	 */
	public void setMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount);

	/**
	 * Sets the person compartment time arrival tracking policy, which is
	 * defaulted to DO_NOT_TRACK_TIME.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_TRACKING_POLICY}
	 *             if the trackPersonCompartmentArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment arrival TimeTrackingPolicy was previously set
	 *
	 */
	public void setPersonCompartmentArrivalTracking(final TimeTrackingPolicy trackPersonCompartmentArrivalTimes);

	/**
	 * Sets a person property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             person property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_PROPERTY_ID} if
	 *             the person property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the person property value was previously set
	 *
	 */
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue);

	/**
	 * Sets the person region time arrival tracking policy, which is defaulted
	 * to DO_NOT_TRACK_TIME.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_TRACKING_POLICY} if
	 *             the trackPersonRegionArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region arrival TimeTrackingPolicy was previously set
	 *
	 */
	public void setPersonRegionArrivalTracking(final TimeTrackingPolicy trackPersonRegionArrivalTimes);

	/**
	 * Sets a person's initial resource level.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the person resource level was previously set
	 */
	public void setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Sets the mapping option for all regions. Defaulted to NONE.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_MAP_OPTION} if the
	 *             mapOption is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the mapOption was previously set
	 */
	public void setRegionMapOption(final MapOption mapOption);

	/**
	 * Sets a region property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_ID} if the
	 *             region property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the region property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region property value was previously set
	 */
	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue);

	/**
	 * Sets a region's initial resource level.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region resource level was previously set
	 */
	public void setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Sets a resource property value for the given property.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 * 
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource property value was previously set
	 */
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue);

	/**
	 * Sets the resource time tracking policy for resource assignments to people
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_TRACKING_POLICY}
	 *             if the trackValueAssignmentTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource TimeTrackingPolicy was previously set
	 */
	public void setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes);

	/**
	 * Sets the scenario's id
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_SCENARIO_ID} if the
	 *             scenario id is null
	 *             <li>{@link ScenarioErrorType#NON_POSITIVE_SCENARIO_ID} if the
	 *             scenario id is non-positive
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the scenario id was previously set
	 */
	public void setScenarioId(final ScenarioId scenarioId);

	/**
	 * Sets the suggested population size size
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NEGATIVE_SUGGGESTED_POPULATION}
	 *             if the suggested population size is negative. Default value
	 *             is zero.
	 * 
	 */
	public void setSuggestedPopulationSize(int suggestedPopulationSize);
	
	/**
	 * Set the policy of using dense partitions for all partitions in the simulation.
	 */
	public void setUseDensePartitions(boolean useDensePartitions);

}
