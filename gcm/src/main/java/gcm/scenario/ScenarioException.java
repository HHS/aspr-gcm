package gcm.scenario;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A {@link RuntimeException} thrown during Scenario/Experiment construction. It
 * indicates that the cause of the error is very likely due to invalid input and
 * not an underlying error in GCM. It contains a ScenarioErrorType that
 * indicates the general cause of the exception and may also contain additional
 * message text.
 * 
 * Although this is a RuntimeException, it functions like a checked exception,
 * leaving the object throwing the exception in a recoverable state.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public class ScenarioException extends RuntimeException {

	public static enum ScenarioErrorType {
		BATCH_ALREADY_STAGED("Batch is already staged"),
		BATCH_STAGED_TO_DIFFERENT_OWNER("Cannot stage a batch onto a stage not owned by the same materials producer"),
		COVARIANT_WITHOUT_VALUES("Covariance specified that lacks corresponding variation in values"),
		DUPLICATE_BATCH_PROPERTY_DEFINITION("Duplicate batch property definition"),
		DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION("Duplicate compartment property definition"),
		DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION("duplicate experiment covariant declaration"),
		DUPLICATE_EXPERIMENT_DIMENSION_VALUE("The covariant values form repeated tuples of values and thus creating an experiment with duplicate scenarios."),
		DUPLICATE_GLOBAL_PROPERTY_DEFINITION("Duplicate global property definition"),
		DUPLICATE_GROUP_MEMBERSHIP("Person was previously assigned to group"),
		DUPLICATE_GROUP_PROPERTY_DEFINITION("Duplicate group property definition"),
		DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION("Duplicate materials producer property definition"),
		DUPLICATE_PERSON_PROPERTY_DEFINITION("Duplicate person property definition"),
		DUPLICATE_REGION_PROPERTY_DEFINITION("Duplicate region property definition"),
		DUPLICATE_RESOURCE_PROPERTY_DEFINITION("Duplicate resource property definition"),
		EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS("experiment covariant declaration spans multiple dimensions"),
		EXPERIMENT_VARIABLE_SIZE_MISMATCH("experiment variable size mismatch"),
		INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"),
		INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT("Property definition default value is null and not replaced with sufficient property value assignments"),
		NEGATIVE_MATERIAL_AMOUNT("Material amount is negative"),
		NEGATIVE_RESOURCE_AMOUNT("Resource amount is negative"),
		NEGATIVE_SUGGGESTED_POPULATION("Suggested population size is negative"),
		NON_FINITE_MATERIAL_AMOUNT("Material amount is not finite"),
		NON_POSITIVE_SCENARIO_ID("Scenario id is negative"),
		NULL_BATCH_ID("Null batch id"),
		NULL_BATCH_PROPERTY_DEFINITION("Null batch property definition"),
		NULL_BATCH_PROPERTY_ID("Null batch property id"),
		NULL_BATCH_PROPERTY_VALUE("Null batch property value"),
		NULL_COMPARTMENT_ID("Null compartment id"),
		NULL_COMPARTMENT_MAP_OPTION("Null compartment map option"),
		NULL_COMPARTMENT_PROPERTY_DEFINITION("Null compartment property definition"),
		NULL_COMPARTMENT_PROPERTY_ID("Null compartment property id"),
		NULL_COMPARTMENT_PROPERTY_VALUE("Null compartment value"),
		NULL_COMPARTMENT_TRACKING_POLICY("Null compartment tracking policy"),
		NULL_COMPONENT_CLASS("Null component class"),
		NULL_COMPONENT_IDENTIFIER("Component identifier is null"),
		NULL_DEFAULT_VALUE("Property definition default value is null and cannot be replaced with property value assignments due to dynamic nature of proerty holders"),
		NULL_DIMENSION_IDENTIFIER("null dimension identifier"),
		NULL_GLOBAL_COMPONENT_ID("Null global component id"),
		NULL_GLOBAL_PROPERTY_DEFINITION("Null global property definition"),
		NULL_GLOBAL_PROPERTY_ID("Null global property id"),
		NULL_GLOBAL_PROPERTY_VALUE("Null global property value"),
		NULL_GROUP_ID("Null group id"),
		NULL_GROUP_PROPERTY_DEFINITION("Null group property definition"),
		NULL_GROUP_PROPERTY_ID("Null group property id"),
		NULL_GROUP_PROPERTY_VALUE("Null group property value"),
		NULL_GROUP_TYPE_ID("Null group type id"),
		NULL_MATERIAL_ID("Null material id"),
		NULL_MATERIALS_PRODUCER_ID("Null materials producer id"),
		NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION("Null materials producer property definition"),
		NULL_MATERIALS_PRODUCER_PROPERTY_ID("Null materials producer property id"),
		NULL_MATERIALS_PRODUCER_PROPERTY_VALUE("Null materials producer property value"),
		NULL_PERSON_ID("Null person id"),
		NULL_PERSON_PROPERTY_DEFINITION("Null person property definition"),
		NULL_PERSON_PROPERTY_ID("Null person property id"),
		NULL_PERSON_PROPERTY_VALUE("Null person property value"),
		NULL_RANDOM_NUMBER_GENERATOR_ID("Null random number generator id"),
		NULL_REGION_ID("Null region id"),
		NULL_REGION_MAP_OPTION("Null region map option"),
		NULL_REGION_PROPERTY_DEFINITION("Null region property definition"),
		NULL_REGION_PROPERTY_ID("Null region property id"),
		NULL_REGION_PROPERTY_VALUE("Null region property value"),
		NULL_REGION_TRACKING_POLICY("Null region tracking policy"),
		NULL_RESOURCE_ID("Null resource id"),
		NULL_RESOURCE_PROPERTY_DEFINITION("Null resource property definition"),
		NULL_RESOURCE_PROPERTY_ID("Null resource property id"),
		NULL_RESOURCE_PROPERTY_VALUE("Null resource property value"),
		NULL_RESOURCE_TRACKING_POLICY("Null resource tracking policy"),
		NULL_SCENARIO("No baseline scenario provided"),
		NULL_SCENARIO_ID("Scenario identifier is null"),
		NULL_STAGE_ID("Null stage id"),
		NULL_SUGGESTED_POPULATION_SIZE("Scenario identifier is null"),
		NULL_VARIALBLE_ID("Variable Id is null"),
		PREVIOUSLY_ADDED_IDENTIFIER("Previously added identifier"),
		PREVIOUSLY_ASSIGNED_VALUE("This value was previously assigned"),
		UNKNOWN_BATCH_ID("Unknown batch id"),
		UNKNOWN_BATCH_PROPERTY_ID("Unknown batch property id"),
		UNKNOWN_COMPARTMENT_ID("Unknown compartment id"),
		UNKNOWN_COMPARTMENT_PROPERTY_ID("Unknown compartment property id"),
		UNKNOWN_GLOBAL_COMPONENT_ID("Unknown global component id"),
		UNKNOWN_GLOBAL_PROPERTY_ID("Unknown global property id"),
		UNKNOWN_GROUP_ID("Unknown group id"),
		UNKNOWN_GROUP_PROPERTY_ID("Unknown group property id"),
		UNKNOWN_GROUP_TYPE_ID("Unknown group type id"),
		UNKNOWN_MATERIAL_ID("Unknown material id"),
		UNKNOWN_MATERIALS_PRODUCER_ID("Unknown materials producer id"),
		UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID("Unknown material producer property id"),
		UNKNOWN_PERSON_ID("Unknown person id"),
		UNKNOWN_PERSON_PROPERTY_ID("Unknown person property id"),
		UNKNOWN_REGION_ID("Unknown region id"),
		UNKNOWN_REGION_PROPERTY_ID("Unknown region property id"),
		UNKNOWN_RESOURCE_ID("Unknown resource id"),
		UNKNOWN_RESOURCE_PROPERTY_ID("Unknown resource property id"),
		UNKNOWN_STAGE_ID("Unknown stage id");

		private final String description;

		private ScenarioErrorType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	private static final long serialVersionUID = 8956278699714119176L;

	private final ScenarioErrorType scenarioErrorType;

	/*
	 * Package access constructor
	 */
	ScenarioException(ScenarioErrorType scenarioErrorType) {
		super(scenarioErrorType.description);
		this.scenarioErrorType = scenarioErrorType;
	}

	/*
	 * Package access constructor
	 */
	ScenarioException(ScenarioErrorType scenarioErrorType, String description) {
		super(scenarioErrorType.description+":"+description);
		this.scenarioErrorType = scenarioErrorType;
	}

	/**
	 * Returns the ScenarioErrorType that documents the general issue that
	 * caused the exception.
	 */
	public ScenarioErrorType getScenarioErrorType() {
		return scenarioErrorType;
	}
}