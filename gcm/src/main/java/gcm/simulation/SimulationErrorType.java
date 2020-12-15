package gcm.simulation;

import gcm.util.annotations.Source;

/**
 * An enumeration supporting {@link ModelException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public enum SimulationErrorType {
	BATCH_ALREADY_STAGED("Batch is already staged"),
	BATCH_NOT_STAGED("Batch is not currently staged"),
	BATCH_STAGED_TO_DIFFERENT_OWNER("Cannot stage a batch onto a stage not owned by the same materials producer"),
	BATCH_SHIFT_WITH_MULTIPLE_OWNERS("Cannot shift material from a batch to another batch not owned by the same materials producer"),
	COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED("Person compartment arrival times not actively tracked"),
	COMPONENT_LACKS_PERMISSION("Current active component does not have permission"),
	DUPLICATE_GROUP_MEMBERSHIP("Person was previously assigned to group"),

	DUPLICATE_PLAN_KEY("There is an existing plan currently scheduled with the same key"),
	IMMUTABLE_VALUE("This property is defined as immutable"),
	INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"),
	NON_COMPARABLE_PROPERTY("The property definition is not compatible with innequality comparisons"),
	INSUFFICIENT_MATERIAL_AVAILABLE("Material level is insufficient for transaction amount"),
	INSUFFICIENT_RESOURCES_AVAILABLE("Resource level is insufficient for transaction amount"),
	MALFORMED_WEIGHTING_FUNCTION("Data used to form an enumerated distribution was malformed"),
	MATERIAL_TYPE_MISMATCH("Material identifiers do not match"),
	NEGATIVE_MATERIAL_AMOUNT("Material amount is negative"),
	NON_FINITE_MATERIAL_AMOUNT("Material amount is not finite"),
	MATERIAL_ARITHMETIC_EXCEPTION("Material arithmetic error due to non finite sum"),
	NEGATIVE_RESOURCE_AMOUNT("Resource amount is negative"),
	RESOURCE_ARITHMETIC_EXCEPTION("Resource arithmetic resulting in underflow/overflow"),
	NO_ACTIVE_COMPONENT("There is no active component"),	
	NON_GROUP_MEMBERSHIP("Person is not currently assigned to group"),
	NULL_BATCH_ID("Null batch id"),
	NULL_BATCH_PROPERTY_ID("Null batch property id"),
	NULL_BATCH_PROPERTY_VALUE("Null batch property value"),
	NULL_COMPARTMENT_ID("Null compartment id"),
	NULL_COMPARTMENT_PROPERTY_ID("Null compartment property id"),
	NULL_COMPARTMENT_PROPERTY_VALUE("Null compartment value"),
	NULL_EQUALITY_OPERATOR("Null equality operator"),
	NULL_FILTER("Null filter"),
	NULL_GLOBAL_PROPERTY_ID("Null global property id"),
	NULL_GLOBAL_COMPONENT_ID("Null global component id"),
	GLOBAL_COMPONENT_ID_ALREADY_EXISTS("Global component id already exists in the simulation"),
	NULL_GLOBAL_COMPONENT_CLASS("Global component class reference is null"),
	NULL_GLOBAL_PROPERTY_VALUE("Null global property value"),
	NULL_GROUP_ID("Null group id"),
	NULL_GROUP_CONSTRUCTION_INFO("Null group construction info"),
	NULL_BATCH_CONSTRUCTION_INFO("Null batch construction info"),
	NULL_GROUP_PROPERTY_ID("Null group property id"),
	NULL_GROUP_PROPERTY_VALUE("Null group property value"),
	NULL_GROUP_TYPE_ID("Null group type id"),
	NULL_MATERIAL_ID("Null material id"),
	NULL_MATERIALS_PRODUCER_ID("Null materials producer id"),
	NULL_MATERIALS_PRODUCER_PROPERTY_ID("Null materials producer property id"),
	NULL_MATERIALS_PRODUCER_PROPERTY_VALUE("Null materials producer property value"),
	NULL_PERSON_ID("Null person id"),
	NULL_PERSON_PROPERTY_ID("Null person property id"),
	NULL_PERSON_PROPERTY_VALUE("Null person property value"),
	NULL_PLAN("Null plan"),
	NULL_PLAN_KEY("Null planning key"),	
	NULL_RANDOM_NUMBER_GENERATOR_ID("Null random number generator id"),
	NULL_REGION_ID("Null region id"),
	NULL_REGION_PROPERTY_ID("Null region property id"),
	NULL_REGION_PROPERTY_VALUE("Null region property value"),
	NULL_REPORT_CLASS("Null report class"),
	NULL_RESOURCE_ID("Null resource id"),
	NULL_RESOURCE_PROPERTY_ID("Null resource property id"),
	NULL_RESOURCE_PROPERTY_VALUE("Null resource property value"),
	NULL_STAGE_ID("Null stage id"),
	NULL_WEIGHTING_FUNCTION("Weighting function is null"),
	NULL_OUTPUT_ITEM("Null report item"),	
	INCORRECT_SCENARIO_ID_FOR_OUTPUT_ITEM("Incorrect scenario id.  Output items must match the scenario of the simulation"),
	INCORRECT_REPLICATION_ID_FOR_OUTPUT_ITEM("Incorrect replication id.  Output items must match the replication of the simulation"),
	OFFERED_STAGE_UNALTERABLE("An offered stage and its batches cannot be altered"),
	PAST_PLANNING_TIME("Plan execution time is in the past"),
	REFLEXIVE_STAGE_TRANSFER("Producer cannot transfer a stage to itself"),
	PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED("Property assignment time not actively tracked"),
	REFLEXIVE_BATCH_SHIFT("Cannot shift material from a batch to itself"),
	REFLEXIVE_RESOURCE_TRANSFER("Cannot transfer resources from a region to itself"),
	REGION_ARRIVAL_TIMES_NOT_TRACKED("Person region arrival times not actively tracked"),
	RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED("Resource assignment time not actively tracked"),
	SAME_COMPARTMENT("Cannot move a person into the compartment they are already occupying"),
	SAME_REGION("Cannot move a person into the region they are already occupying"),
	UNKNOWN_BATCH_ID("Unknown batch id"),
	UNKNOWN_BATCH_PROPERTY_ID("Unknown batch property id"),
	UNKNOWN_COMPARTMENT_ID("Unknown compartment id"),
	UNKNOWN_COMPARTMENT_PROPERTY_ID("Unknown compartment property id"),
	UNKNOWN_GLOBAL_PROPERTY_ID("Unknown global property id"),
	UNKNOWN_GROUP_ID("Unknown group id"),
	UNKNOWN_GROUP_PROPERTY_ID("Unknown group property id"),
	UNKNOWN_GROUP_TYPE_ID("Unknown group type id"),
	UNKNOWN_MATERIAL_ID("Unknown material id"),
	UNKNOWN_MATERIALS_PRODUCER_ID("Unknown materials producer id"),
	UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID("Unknown material producer property id"),
	UNKNOWN_PERSON_ID("Unknown person id"),
	UNKNOWN_PERSON_PROPERTY_ID("Unknown person property id"),
	UNKNOWN_RANDOM_NUMBER_GENERATOR_ID("Unknown random number generator id"),
	UNKNOWN_REGION_ID("Unknown region id"),
	UNKNOWN_REGION_PROPERTY_ID("Unknown region property id"),
	UNKNOWN_RESOURCE_ID("Unknown resource id"),
	UNKNOWN_RESOURCE_PROPERTY_ID("Unknown resource property id"),
	UNKNOWN_STAGE_ID("Unknown stage id"),
	UNOFFERED_STAGE_NOT_TRANSFERABLE("Unoffered stages are not transferable"),
	
	NULL_POPULATION_PARTITION_KEY("Null population partition key"),
	UNKNOWN_POPULATION_PARTITION_KEY("No population partition found"),
	NULL_LABEL_SET("Null label set"),
	NULL_PARTITION_SAMPLER("Null partition sampler"),
	NULL_GROUP_SAMPLER("Null group sampler"),
	NULL_POPULATION_PARTITION_DEFINITION("Null population definition"),
	DUPLICATE_PARTITION("Duplicate partition key"),
	PARTITION_DELETION_BY_NON_OWNER("A population partition can only be deleted by its owner"),	
	INCOMPATIBLE_LABEL_SET("The label set is incompatible with the selected population partition definition"),
	
	NULL_COMPARTMENT_LABEL("A compartment labeling function returned a null label"),
	NULL_REGION_LABEL("A region labeling function returned a null label"),
	NULL_RESOURCE_LABEL("A resource labeling function returned a null label"),
	NULL_PROPERTY_LABEL("A property labeling function returned a null label"),
	NULL_GROUP_LABEL("A group labeling function returned a null label");
	 

	private final String description;

	private SimulationErrorType(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
