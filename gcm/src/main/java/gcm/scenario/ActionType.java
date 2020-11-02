package gcm.scenario;

import gcm.util.annotations.Source;

/**
 * An enumeration that corresponds to types of data actions used in scenario and
 * experiment construction. Many of the actions in this enumeration correspond
 * directly with mutations allowed by the environment. However, many do not and
 * represent the establishment of the initial state of the simulation such as
 * the setting of various memory policies and the definitions of properties.
 * 
 * @author Shawn Hatch
 *
 */
@Source()
public enum ActionType {

	// assignment of the scenario identifier
	SCENARIO_ID_ASSIGNMENT("scenario_id_assignment"),
	SUGGESTED_POPULATION_SIZE("suggested_population_size"),
	DENSE_PARTITIONS("dense partitions"),

	// assignments of property values
	GLOBAL_PROPERTY_VALUE_ASSIGNMENT("global_property"),
	REGION_PROPERTY_VALUE_ASSIGNMENT("region_property"),
	COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT("compartment_property"),
	PERSON_PROPERTY_VALUE_ASSIGNMENT("person_property"),
	RESOURCE_PROPERTY_VALUE_ASSIGNMENT("resource_property"),
	MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT("materials_producer_property_value_assignment"),
	GROUP_PROPERTY_VALUE_ASSIGNMENT("group_property_value_assignment"),
	BATCH_PROPERTY_VALUE_ASSIGNMENT("batch_property_value_assignment"),

	// assignments of resource values
	PERSON_RESOURCE_ASSIGNMENT("person_resource"),
	REGION_RESOURCE_ASSIGNMENT("region_resource"),

	// assignments of various data control policies
	PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT("person_compartment_arrival_tracking_assignment"),
	PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT("person_region_arrival_tracking_assignment"),
	COMPARTMENT_MAP_OPTION_ASSIGNMENT("compartment_map_option_assignment"),
	REGION_MAP_OPTION_ASSIGNMENT("region_map_option_assignment"),
	MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT("materials_producer_resource_assignment"),
	RESOURCE_TIME_TRACKING_ASSIGNMENT("resource_time_tracking_assignment"),

	// assignments of associations
	GROUP_MEMBERSHIP_ASSIGNMENT("group_membership_assignment"),
	STAGE_MEMBERSHIP_ASSIGNMENT("stage_membership_assignment"),

	// property definitions
	RESOURCE_PROPERTY_DEFINITION("resource_property_definition"),
	REGION_PROPERTY_DEFINITION("region_property_definition"),
	BATCH_PROPERTY_DEFINITION("batch_property_definition"),
	MATERIALS_PRODUCER_PROPERTY_DEFINITION("materials_producer_property_definition"),
	COMPARTMENT_PROPERTY_DEFINITION("compartment_property_definition"),
	GLOBAL_PROPERTY_DEFINITION("global_property_definition"),
	PERSON_PROPERTY_DEFINITION("person_property_definition"),
	GROUP_PROPERTY_DEFINITION("group_property_definition"),

	// additions of identifiers
	PERSON_ID_ADDITION("person_id_addition"),
	GROUP_ID_ADDITION("group_id_addition"),
	RESOURCE_ID_ADDITION("resource_id_addition"),	
	MATERIAL_ID_ADDITION("material_id_addition"),
	GLOBAL_COMPONENT_ID_ADDITION("global_component_id_addition"),
	REGION_COMPONENT_ID_ADDITION("region_component_id_addition"),
	GROUP_TYPE_ID_ADDITION("group_type_id_addition"),
	MATERIALS_PRODUCER_COMPONENT_ID_ADDITION("materials_producer_component_id_addition"),
	COMPARTMENT_COMPONENT_ID_ADDITION("compartment_component_id_addition"),
	STAGE_ID_ADDITION("stage_id_addition"),
	BATCH_ID_ADDITION("batch_id_addition"),
	RANDOM_NUMBER_GENERATOR_ID_ADDITION("random number generator id addition");
		
	private final String descriptor;

	private ActionType(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String toString() {
		return descriptor;
	}
}