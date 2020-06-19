package gcm.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.math3.util.FastMath;

import gcm.components.Component;
import gcm.experiment.Experiment;
import gcm.scenario.ScenarioException.ScenarioErrorType;
import gcm.simulation.ModelException;
import gcm.simulation.SimulationErrorType;
import gcm.util.MultiKey;
import gcm.util.MultiKey.MultiKeyBuilder;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * A builder class for a list of Scenarios. This class parallels the
 * ScenarioBuilder, but builds multiple scenarios. Whereas the ScenarioBuilder
 * "sets" property values, the Scenario Factory "adds" them. Each value for
 * every scenario property is used to build the experiment space, with multiple
 * scenarios taking on the various values. It also allows scenario property
 * values to be grouped so that they vary dependently. There is no need to set
 * all values since the property definitions will supply default values for
 * every scenario value. Whenever any values are added for a particular
 * property, the default value provided by the property's definition is not used
 * to define one of the values in the experiment dimension.
 * 
 * Model Exceptions for inconsistent input values are thrown only once a build
 * method is called.
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
@Source
public final class ExperimentBuilder {

	/*
	 * Return the last key of the multikey. Requires a non empty multikey
	 */
	private static Object getValue(MultiKey multiKey) {
		return multiKey.getKey(multiKey.size() - 1);
	}

	/*
	 * Returns a multikey formed from all but the last key. Requires a non empty
	 * multikey
	 */
	private static MultiKey getVariableId(MultiKey multiKey) {
		MultiKeyBuilder multiKeyBuilder = new MultiKeyBuilder();
		for (int i = 0; i < multiKey.size() - 1; i++) {
			multiKeyBuilder.addKey(multiKey.getKey(i));
		}
		return multiKeyBuilder.build();
	}

	/*
	 * A class for storing the clients data supplied to this builder. Data falls
	 * into three categories: fixed , variant and covariant. Fixed data are all
	 * those values that must be identical across all scenarios such as the core
	 * identifiers and the property definitions. Variant data consists of all
	 * the property values (but not default values as defined in the property
	 * definitions).
	 */
	private static class Scaffold {

		private int baseScenarioId;

		/*
		 * The data that is shared across all scenarios. Multikeys consist of an
		 * ActionType followed by the relevant arguments. The last argument
		 * tends to be the value.
		 */
		private final List<MultiKey> scenarioData = new ArrayList<>();
		/*
		 * The data values that vary across scenarios. Multikeys consist of an
		 * ActionType followed by the relevant arguments. The last argument
		 * tends to be the value.
		 */
		private final List<MultiKey> experimentValueData = new ArrayList<>();

		/*
		 * The variables that are being forced to be in the experiment headers.
		 * Multikeys consist of an ActionType followed by the relevant
		 * arguments. The values will correspond to the content of the
		 * experimentValueData, except that each will lack the value part.
		 * Forcing in this context will only work if at least one value is added
		 * to the experiment builder. If no values are added, then the default
		 * value in the corresponding property definition is used and will not
		 * be part of the experiment fields.
		 */
		private final Set<MultiKey> experimentColumnForcingData = new LinkedHashSet<>();
		/*
		 * The data that marks variables for covariance. Multikeys consist of an
		 * ActionType followed by the relevant arguments. There are no value
		 * arguments. The last argument tends to be the key that will
		 * covariantly link variables.
		 */
		private final List<MultiKey> experimentDimensionData = new ArrayList<>();

		private final MultiKeyBuilder multiKeyBuilder = new MultiKeyBuilder();

		private MultiKey getMultiKey(final Object... objects) {
			for (final Object object : objects) {
				multiKeyBuilder.addKey(object);
			}
			return multiKeyBuilder.build();
		}

		private void putScenarioData(final Object... objects) {
			scenarioData.add(getMultiKey(objects));
		}

		private void putExperimentValueData(final Object... objects) {
			experimentValueData.add(getMultiKey(objects));
		}

		private void putExperimentColumnForceData(final Object... objects) {
			experimentColumnForcingData.add(getMultiKey(objects));
		}

		private void putExperimentDimensionData(final Object... objects) {
			experimentDimensionData.add(getMultiKey(objects));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			int colCount = 0;
			for (MultiKey m : scenarioData) {
				colCount = FastMath.max(colCount, m.getKeys().length);
			}
			for (MultiKey m : experimentValueData) {
				colCount = FastMath.max(colCount, m.getKeys().length);
			}
			for (MultiKey m : experimentDimensionData) {
				colCount = FastMath.max(colCount, m.getKeys().length);
			}
			sb.append("Data Type");

			for (int i = 0; i < colCount; i++) {
				sb.append("\t");
				sb.append("col " + i);
			}
			sb.append("\n");

			scenarioData.forEach(m -> {
				sb.append("scenario data");
				sb.append("\t");
				sb.append(m.toTabString());
				sb.append("\n");
			});
			experimentValueData.forEach(m -> {
				sb.append("experiment value data");
				sb.append("\t");
				sb.append(m.toTabString());
				sb.append("\n");
			});
			experimentDimensionData.forEach(m -> {
				sb.append("experiment dimension data");
				sb.append("\t");
				sb.append(m.toTabString());
				sb.append("\n");
			});

			return sb.toString();
		}

	}

	private Scaffold scaffold = new Scaffold();

	/**
	 * Adds a batch to all scenarios for the given materials producer
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID}if the material
	 *             id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID}if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the batch id was previously added
	 * 
	 */
	public void addBatch(final BatchId batchId, final MaterialId materialId, final double amount, final MaterialsProducerId materialsProducerId) {
		scaffold.putScenarioData(ActionType.BATCH_ID_ADDITION, batchId, materialId, amount, materialsProducerId);
	}

	/**
	 * Sets the EXCLUSIVE lower bound of the scenario ids produced by this
	 * builder. For example, setting this value to 0 will cause the scenarios to
	 * numbered 1, 2,...
	 */
	public void setBaseScenarioId(int baseScenarioId) {
		scaffold.baseScenarioId = baseScenarioId;
	}

	/**
	 * Adds a batch property value for the given batch as an experiment
	 * dimension value
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the batch property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue) {
		scaffold.putExperimentValueData(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchId, batchPropertyId, batchPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding batch property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 * 
	 * 
	 */
	public void forceBatchPropertyExperimentColumn(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchId, batchPropertyId);
	}

	/**
	 * Associates a batch with a stage for all scenarios
	 *
	 * @throws ModelException
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
	public void addBatchToStage(final StageId stageId, final BatchId batchId) {
		scaffold.putScenarioData(ActionType.STAGE_MEMBERSHIP_ASSIGNMENT, stageId, batchId);
	}

	/**
	 * Adds a compartment id to all scenarios. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the compartment id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment id is equal to another previously added
	 *             component id
	 */
	public void addCompartmentId(final CompartmentId compartmentId, Class<? extends Component> comparmentComponentClass) {
		scaffold.putScenarioData(ActionType.COMPARTMENT_COMPONENT_ID_ADDITION, compartmentId, comparmentComponentClass);
	}

	/**
	 * Adds a compartment property value for the given property as an experiment
	 * dimension value
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the compartment property value causes a duplicate
	 *             experiment dimension value taking into account covariant
	 *             tuples.
	 */
	public void addCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		scaffold.putExperimentValueData(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentId, compartmentPropertyId, compartmentPropertyValue);

	}

	/**
	 * Attempts to force the expression of the corresponding compartment
	 * property experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceCompartmentPropertyExperimentColumn(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentId, compartmentPropertyId);
	}

	/**
	 * Adds the global component id to all scenarios. This identifier informs
	 * the simulation that a component having this identifier is expected to
	 * exist. Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the GlobalComponentId is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the global component id is equal to another previously added
	 *             component id
	 */
	public void addGlobalComponentId(final GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass) {
		scaffold.putScenarioData(ActionType.GLOBAL_COMPONENT_ID_ADDITION, globalComponentId, globalComponentClass);
	}

	/**
	 * Adds global property value for the given property as an experiment
	 * dimension value.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the global property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 *
	 *
	 */

	public void addGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		scaffold.putExperimentValueData(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId, globalPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding global property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceGlobalPropertyExperimentColumn(final GlobalPropertyId globalPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId);
	}

	/**
	 * Adds a group to all scenarios with the given group type id.
	 *
	 * @throws ModelException
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
	public void addGroup(final GroupId groupId, final GroupTypeId groupTypeId) {
		scaffold.putScenarioData(ActionType.GROUP_ID_ADDITION, groupId, groupTypeId);
	}

	/**
	 * Adds a person to a group in all scenarios
	 *
	 * @throws ModelException
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
	public void addPersonToGroup(final GroupId groupId, final PersonId personId) {
		scaffold.putScenarioData(ActionType.GROUP_MEMBERSHIP_ASSIGNMENT, groupId, personId, personId);
	}

	/**
	 * Adds the group property value for the given group as an experiment
	 * dimension value
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the group property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		scaffold.putExperimentValueData(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupId, groupPropertyId, groupPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding group property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */
	public void forceGroupPropertyExperimentColumn(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupId, groupPropertyId);
	}

	/**
	 * Adds a group type identifier to all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the group type was previously added
	 */
	public void addGroupTypeId(final GroupTypeId groupTypeId) {
		scaffold.putScenarioData(ActionType.GROUP_TYPE_ID_ADDITION, groupTypeId, groupTypeId);
	}

	/**
	 * Adds a material to all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the material was previously added
	 *
	 */
	public void addMaterial(final MaterialId materialId) {
		scaffold.putScenarioData(ActionType.MATERIAL_ID_ADDITION, materialId, materialId);
	}

	/**
	 * Adds a random number generator id to all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the material id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the material was previously added
	 *
	 */
	public void addRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId) {
		scaffold.putScenarioData(ActionType.RANDOM_NUMBER_GENERATOR_ID_ADDITION, randomNumberGeneratorId, randomNumberGeneratorId);
	}

	/**
	 * Adds a materials producer component id to all scenarios. This identifier
	 * informs the simulation that a component having this identifier is
	 * expected to exist. Component identifiers must be unique without regard to
	 * the type of component.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the materials producer id is equal to another previously
	 *             added component id
	 */
	public void addMaterialsProducerId(final MaterialsProducerId materialsProducerId, Class<? extends Component> materialProducerComponentClass) {
		scaffold.putScenarioData(ActionType.MATERIALS_PRODUCER_COMPONENT_ID_ADDITION, materialsProducerId, materialProducerComponentClass);
	}

	/**
	 * Sets a materials producer property value for the given property.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the materials producer property value causes a duplicate
	 *             experiment dimension value taking into account covariant
	 *             tuples.
	 */
	public void addMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		scaffold.putExperimentValueData(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding materials producer
	 * property experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */
	public void forceMaterialsProducerPropertyExperimentColumn(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerId, materialsProducerPropertyId);
	}

	/**
	 * Sets a materials producer's initial resource level.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the materials producer resource level causes a duplicate
	 *             experiment dimension value taking into account covariant
	 *             tuples.
	 */
	public void addMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		scaffold.putExperimentValueData(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, materialsProducerId, resourceId, amount);
	}

	/**
	 * Attempts to force the expression of the corresponding material producer
	 * resource experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceMaterialsProducerResourceExperimentColumn(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		scaffold.putExperimentColumnForceData(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, materialsProducerId, resourceId);
	}

	/**
	 * Adds a person to all scenarios in the given region and compartment.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the person was previously added
	 *
	 */
	public void addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId) {
		scaffold.putScenarioData(ActionType.PERSON_ID_ADDITION, personId, regionId, compartmentId);
	}

	/**
	 * Sets a person property value for the given property.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the person property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 *
	 */
	public void addPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		scaffold.putExperimentValueData(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personId, personPropertyId, personPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding person property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forcePersonPropertyExperimentColumn(final PersonId personId, final PersonPropertyId personPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personId, personPropertyId);
	}

	/**
	 * Sets a person's initial resource level.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the person resource level causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount) {
		scaffold.putExperimentValueData(ActionType.PERSON_RESOURCE_ASSIGNMENT, personId, resourceId, amount);
	}

	/**
	 * Attempts to force the expression of the corresponding person resource
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forcePersonResourceExperimentColumn(final PersonId personId, final ResourceId resourceId) {
		scaffold.putExperimentColumnForceData(ActionType.PERSON_RESOURCE_ASSIGNMENT, personId, resourceId);
	}

	/**
	 * Adds a region component id to all scenarios. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the region id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region was previously added
	 */
	public void addRegionId(final RegionId regionId, Class<? extends Component> regionComponentClass) {
		scaffold.putScenarioData(ActionType.REGION_COMPONENT_ID_ADDITION, regionId, regionComponentClass);
	}

	/**
	 * Sets a region property value for the given property.
	 *
	 * @throws ModelException
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
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the region property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
		scaffold.putExperimentValueData(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionId, regionPropertyId, regionPropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding region property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceRegionPropertyExperimentColumn(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionId, regionPropertyId);
	}

	/**
	 * Adds a region's initial resource level as an experiment dimension value
	 * as an experiment dimension value.
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative compatible with the property definition
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the region resource level causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		scaffold.putExperimentValueData(ActionType.REGION_RESOURCE_ASSIGNMENT, regionId, resourceId, amount);
	}

	/**
	 * Attempts to force the expression of the corresponding region resource
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceRegionResourceExperimentColumn(final RegionId regionId, final ResourceId resourceId) {
		scaffold.putExperimentColumnForceData(ActionType.REGION_RESOURCE_ASSIGNMENT, regionId, resourceId);
	}

	/**
	 * See {@link ScenarioBuilder#addResource(ResourceId)}
	 */
	public void addResource(final ResourceId resourceId) {
		scaffold.putScenarioData(ActionType.RESOURCE_ID_ADDITION, resourceId, resourceId);
	}

	/**
	 * Adds a resource property value for the given property as an experiment
	 * dimension value.
	 *
	 *
	 * @throws ModelException
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
	 *             is not compatible with the property definition *
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_DIMENSION_VALUE}
	 *             if the resource property value causes a duplicate experiment
	 *             dimension value taking into account covariant tuples.
	 */
	public void addResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
		scaffold.putExperimentValueData(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourceId, resourcePropertyId, resourcePropertyValue);
	}

	/**
	 * Attempts to force the expression of the corresponding resource property
	 * experiment column in output when there is only one value in that
	 * dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceResourcePropertyExperimentColumn(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		scaffold.putExperimentColumnForceData(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourceId, resourcePropertyId);
	}

	/**
	 * See {@link ScenarioBuilder#addStage(StageId, boolean, Object)}
	 */
	public void addStage(final StageId stageId, final boolean offered, final MaterialsProducerId materialsProducerId) {
		scaffold.putScenarioData(ActionType.STAGE_ID_ADDITION, stageId, offered, materialsProducerId);
	}

	/**
	 * Defines a batch property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the batch property was previously defined
	 */
	public void defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.BATCH_PROPERTY_DEFINITION, materialId, batchPropertyId, propertyDefinition);
	}

	/**
	 * Defines a compartment property for all scenarios.
	 *
	 * @throws ModelException
	 *             <li>{@link SimulationErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link SimulationErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the compartment property was previously defined
	 */
	public void defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.COMPARTMENT_PROPERTY_DEFINITION, compartmentId, compartmentPropertyId, propertyDefinition);
	}

	/**
	 * Defines a global property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the global property was previously defined
	 */
	public void defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.GLOBAL_PROPERTY_DEFINITION, globalPropertyId, propertyDefinition);
	}

	/**
	 * Defines a group property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the group property was previously defined
	 */
	public void defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.GROUP_PROPERTY_DEFINITION, groupTypeId, groupPropertyId, propertyDefinition);
	}

	/**
	 * Defines a materials producer property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the materials producer property was previously defined
	 */
	public void defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.MATERIALS_PRODUCER_PROPERTY_DEFINITION, materialsProducerPropertyId, propertyDefinition);
	}

	/**
	 * Defines a person property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the person property was previously defined
	 */
	public void definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.PERSON_PROPERTY_DEFINITION, personPropertyId, propertyDefinition);
	}

	/**
	 * See
	 * {@link ScenarioBuilder#defineRegionProperty(RegionPropertyId, PropertyDefinition)}
	 */
	public void defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.REGION_PROPERTY_DEFINITION, regionPropertyId, propertyDefinition);

	}

	/**
	 * Defines a resource property for all scenarios.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE}
	 *             if the resource property was previously defined
	 */
	public void defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
		scaffold.putScenarioData(ActionType.RESOURCE_PROPERTY_DEFINITION, resourceId, resourcePropertyId, propertyDefinition);
	}

	/**
	 * Returns the experiment formed from the various contributions to this
	 * builder
	 *
	 */
	public Experiment build() {
		try {
			return new ExperimentImpl(scaffold);
		} finally {
			scaffold = new Scaffold();
		}
	}

	/**
	 * Marks a particular compartment and compartment property with a dimension.
	 * All property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if the number of property values for the compartment and
	 *             property does not match the number of values for all other
	 *             variables joined under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the compartment property was previously declared as
	 *             covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 * 
	 * 
	 */
	public void covaryCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentId, compartmentPropertyId, dimensionId);
	}

	/**
	 * Marks a particular batch and batch property with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the compartment and property
	 *             does not match the number of values for all other variables
	 *             joined under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the batch property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 * 
	 * 
	 */
	public void covaryBatchProperty(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchId, batchPropertyId, dimensionId);
	}

	/**
	 * Marks a particular global property with a group. All property values
	 * marked with the same dimension identifier will vary dependently and so
	 * must have the same number of associated property value variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property * id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of values for the global property does not match
	 *             the number of values for all other variables joined under the
	 *             given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the global property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryGlobalProperty(final GlobalPropertyId globalPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId, dimensionId);
	}

	/**
	 * Marks a particular group and group property with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the group and property does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the group property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryGroupProperty(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupId, groupPropertyId, dimensionId);
	}

	/**
	 * Marks a particular person and person property with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the person and property does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the person property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryPersonProperty(final PersonId personId, final PersonPropertyId personPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personId, personPropertyId, dimensionId);
	}

	/**
	 * Marks a particular materials producer and material producer property with
	 * a dimension. All property values marked with the same dimension
	 * identifier will vary dependently and so must have the same number of
	 * associated property value variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the materials producer and
	 *             property does not match the number of values for all other
	 *             variables joined under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the materials producer property was previously declared as
	 *             covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryMaterialsProducerProperty(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerId, materialsProducerPropertyId, dimensionId);
	}

	/**
	 * Marks a particular person and person resource with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of resource values for the person and resource does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the person resource was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryPersonResource(final PersonId personId, final ResourceId resourceId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.PERSON_RESOURCE_ASSIGNMENT, personId, resourceId, dimensionId);
	}

	/**
	 * Marks a materials producer and resource with a dimension. All property
	 * values marked with the same dimension identifier will vary dependently
	 * and so must have the same number of associated property value variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the person id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the person id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of resource values for the person and resource does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the materials producer resource was previously declared as
	 *             covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryMaterialsProducerResource(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, materialsProducerId, resourceId, dimensionId);
	}

	/**
	 * Marks a particular region and region property with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the region and property does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the region property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryRegionProperty(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionId, regionPropertyId, dimensionId);
	}

	/**
	 * Marks a particular region and region resource with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of resource values for the region and resource does
	 *             not match the number of values for all other variables joined
	 *             under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the region resource was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryRegionResource(final RegionId regionId, final ResourceId resourceId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.REGION_RESOURCE_ASSIGNMENT, regionId, resourceId, dimensionId);
	}

	/**
	 * Marks a particular resource and resource property with a dimension. All
	 * property values marked with the same dimension identifier will vary
	 * dependently and so must have the same number of associated property value
	 * variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of property values for the resource and property
	 *             does not match the number of values for all other variables
	 *             joined under the given dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the resource property was previously declared as covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 * 
	 */
	public void covaryResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourceId, resourcePropertyId, dimensionId);
	}

	/**
	 * Sets the mapping option for all compartments in all scenarios. Defaulted
	 * to NONE.
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_MAP_OPTION} if
	 *             the mapOption is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the mapOption was previously set
	 */
	public void setCompartmentMapOption(final MapOption mapOption) {
		scaffold.putScenarioData(ActionType.COMPARTMENT_MAP_OPTION_ASSIGNMENT, mapOption);
	}

	/**
	 * Sets the person compartment time arrival tracking policy, which is
	 * defaulted to DO_NOT_TRACK_TIME for all scenarios.
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_TRACKING_POLICY}
	 *             if the trackPersonCompartmentArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment arrival TimeTrackingPolicy was previously set
	 *
	 */
	public void setPersonCompartmentArrivalTracking(final TimeTrackingPolicy trackPersonCompartmentArrivalTimes) {
		scaffold.putScenarioData(ActionType.PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT, trackPersonCompartmentArrivalTimes);
	}

	/**
	 * Sets the person region time arrival tracking policy, which is defaulted
	 * to DO_NOT_TRACK_TIME for all scenarios.
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_TRACKING_POLICY} if
	 *             the trackPersonRegionArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region arrival TimeTrackingPolicy was previously set
	 *
	 */
	public void setPersonRegionArrivalTracking(final TimeTrackingPolicy trackPersonRegionArrivalTimes) {
		scaffold.putScenarioData(ActionType.PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT, trackPersonRegionArrivalTimes);
	}

	/**
	 * Sets the mapping option for all regions in all scenarios. Defaulted to
	 * NONE.
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_MAP_OPTION} if the
	 *             mapOption is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the mapOption was previously set
	 */
	public void setRegionMapOption(final MapOption mapOption) {
		scaffold.putScenarioData(ActionType.REGION_MAP_OPTION_ASSIGNMENT, mapOption);
	}

	/**
	 * Sets the resource time tracking policy for resource assignments to people
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_TRACKING_POLICY}
	 *             if the trackValueAssignmentTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource TimeTrackingPolicy was previously set
	 */
	public void setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes) {
		scaffold.putScenarioData(ActionType.RESOURCE_TIME_TRACKING_ASSIGNMENT, resourceId, trackValueAssignmentTimes);
	}

	/*
	 * Returns a string representation of the multikey by appending each key's
	 * string values with a period delimiter.
	 */
	private static String getMultiKeyString(final MultiKey multiKey) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < multiKey.size(); i++) {
			final Object key = multiKey.getKey(i);
			if (i > 0) {
				sb.append(".");
			}
			sb.append(key);
		}
		return sb.toString();
	}

	private static class CovariantGroup {
		private Object dimensionTag;
		private int size;
		private List<Variable> variables = new ArrayList<>();
		private int modulus;
	}

	private static class Variable {
		private boolean forceAsExperimentVariable;
		private CovariantGroup covariantGroup;
		private String name;
		private List<MultiKey> multiKeys = new ArrayList<>();
	}

	/*
	 * 
	 * Implementor of Experiment
	 *
	 */
	private final static class ExperimentImpl implements Experiment {

		private final Map<ActionType, BiConsumer<ScenarioBuilder, MultiKey>> actionMap = new LinkedHashMap<>();

		private final List<Variable> experimentVariables = new ArrayList<>();

		private final int baseScenarioId;

		private final List<CovariantGroup> covariantGroups = new ArrayList<>();

		private final int scenarioCount;

		@Override
		public String getExperimentFieldName(int fieldIndex) {
			return experimentVariables.get(fieldIndex).name;
		}

		@Override
		public int getExperimentFieldCount() {
			return experimentVariables.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getExperimentFieldValue(ScenarioId scenarioId, int fieldIndex) {
			Variable variable = experimentVariables.get(fieldIndex);
			int k = scenarioId.getValue() - baseScenarioId - 1;
			k /= variable.covariantGroup.modulus;
			k %= variable.multiKeys.size();
			MultiKey multiKey = variable.multiKeys.get(k);
			Object value = multiKey.getKey(multiKey.size() - 1);
			return (T) value;
		}

		/*
		 * Constructs the experiment from the information collected in the
		 * scaffold.
		 */
		private ExperimentImpl(Scaffold scaffold) {

			buildActionMap();

			baseScenarioId = scaffold.baseScenarioId;

			/*
			 * The scaffold contains four collections of multikeys that specify
			 * all the values that make up a collection of scenarios. There is
			 * enough freedom in this system to present many edge cases where
			 * there is conflict between these collections. In practice, this is
			 * very rarely an issue for the client, but we still have to check
			 * these edge cases. This is not a full validation of each
			 * scenario's data. The ScenarioBuilder will validate each scenario
			 * and we concentrate here on conflicts that arise with the
			 * experiment level.
			 */

			Map<MultiKey, Variable> variableMap = new LinkedHashMap<>();
			for (MultiKey multiKey : scaffold.scenarioData) {
				if (variableMap.containsKey(multiKey)) {
					throw new ScenarioException(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, multiKey.toKeyString());
				}
				Variable variable = new Variable();
				variable.multiKeys.add(multiKey);
				variableMap.put(multiKey, variable);
			}

			for (MultiKey multiKey : scaffold.experimentValueData) {
				MultiKey variableId = getVariableId(multiKey);
				Variable variable = variableMap.get(variableId);
				if (variable == null) {
					variable = new Variable();
					variable.name = getMultiKeyString(variableId);
					variableMap.put(variableId, variable);
				}
				variable.multiKeys.add(multiKey);
			}

			// We need to check that there are no repeats in the
			// scaffold.experimentDimensionData --
			// DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION
			// This should include checking that the same variable is not being
			// associated with multiple dimensions
			Map<MultiKey, CovariantGroup> variableIdToCovariantGroupMap = new LinkedHashMap<>();

			Map<Object, CovariantGroup> covariantGroupMap = new LinkedHashMap<>();
			for (MultiKey multiKey : scaffold.experimentDimensionData) {
				MultiKey variableId = getVariableId(multiKey);

				Variable variable = variableMap.get(variableId);
				if (variable == null) {
					throw new ScenarioException(ScenarioErrorType.COVARIANT_WITHOUT_VALUES, variableId.toKeyString());
				}
				if (variable.name == null) {
					// some part of the scenario that does not vary across the
					// scenario is being marked for participation in a covariant
					// experiment dimension. This should not happen and is a
					// programming mistake.
					throw new RuntimeException("Improper variable type");
				}
				Object dimensionTag = getValue(multiKey);
				if (dimensionTag == null) {
					throw new ScenarioException(ScenarioErrorType.NULL_DIMENSION_IDENTIFIER, variableId.toKeyString());
				}

				CovariantGroup currentCovariantGroup = variableIdToCovariantGroupMap.get(variableId);
				if (currentCovariantGroup != null) {
					if (currentCovariantGroup.dimensionTag.equals(dimensionTag)) {
						throw new ScenarioException(ScenarioErrorType.DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION);
					} else {
						throw new ScenarioException(ScenarioErrorType.EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS);
					}
				}

				CovariantGroup covariantGroup = covariantGroupMap.get(dimensionTag);
				if (covariantGroup == null) {
					covariantGroup = new CovariantGroup();
					covariantGroup.dimensionTag = dimensionTag;
					covariantGroupMap.put(dimensionTag, covariantGroup);
					covariantGroup.size = variable.multiKeys.size();
				}
				variableIdToCovariantGroupMap.put(variableId, covariantGroup);

				if (covariantGroup.size != variable.multiKeys.size()) {
					throw new ScenarioException(ScenarioErrorType.EXPERIMENT_VARIABLE_SIZE_MISMATCH,
							"Size of " + variable.name + " is " + variable.multiKeys.size() + " and does not match other co-dependent variables that have size " + covariantGroup.size);
				}
				variable.covariantGroup = covariantGroup;
				covariantGroup.variables.add(variable);
			}
			covariantGroups.addAll(covariantGroupMap.values());

			// map the remaining variables into new covariant groups consisting
			// of one variable each
			for (Variable variable : variableMap.values()) {
				if (variable.covariantGroup == null) {
					CovariantGroup covariantGroup = new CovariantGroup();
					covariantGroup.dimensionTag = new Object();
					covariantGroup.size = variable.multiKeys.size();
					covariantGroup.variables.add(variable);
					variable.covariantGroup = covariantGroup;
					covariantGroups.add(covariantGroup);
				}
			}

			for (CovariantGroup covariantGroup : covariantGroups) {
				Set<MultiKey> valueTuples = new LinkedHashSet<>();
				for (int i = 0; i < covariantGroup.size; i++) {
					MultiKeyBuilder multiKeyBuilder = new MultiKeyBuilder();
					for (Variable variable : covariantGroup.variables) {
						Object value = getValue(variable.multiKeys.get(i));
						multiKeyBuilder.addKey(value);
					}
					boolean added = valueTuples.add(multiKeyBuilder.build());
					if (!added) {
						Variable variable = covariantGroup.variables.get(0);
						MultiKey variableId = getVariableId(variable.multiKeys.get(0));
						throw new ScenarioException(ScenarioErrorType.DUPLICATE_EXPERIMENT_DIMENSION_VALUE, variableId.toKeyString());
					}
				}

			}

			for (MultiKey multiKey : scaffold.experimentColumnForcingData) {
				Variable variable = variableMap.get(multiKey);
				if (variable != null) {
					if (variable.name == null) {
						/*
						 * This should not happen and reflects a programming
						 * error. Only variables that correspond to the
						 * experiment variant portions of the scenario can be
						 * forced into the experiment columns
						 */
						throw new RuntimeException("Improper variable type");
					}
					variable.forceAsExperimentVariable = true;
				} else {
					/*
					 * Since we control the construction of the variable, we
					 * only hold the client responsible for attempting the force
					 * for a column that has no variant values.
					 */
					throw new ScenarioException(ScenarioErrorType.COVARIANT_WITHOUT_VALUES, multiKey.toKeyString());
				}
			}

			for (Variable variable : variableMap.values()) {
				if (variable.name != null) {
					if (variable.multiKeys.size() > 1 || variable.forceAsExperimentVariable) {
						experimentVariables.add(variable);
					}
				}
			}

			int modulus = 1;
			for (CovariantGroup covariantGroup : covariantGroups) {
				covariantGroup.modulus = modulus;
				modulus *= covariantGroup.size;
			}
			this.scenarioCount = modulus;

		}

		@Override
		public int getScenarioCount() {
			return scenarioCount;
		}

		@Override
		public Scenario getScenario(int index) {
			ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
			scenarioBuilder.setScenarioId(new ScenarioId(baseScenarioId + 1 + index));
			for (int i = 0; i < covariantGroups.size(); i++) {
				CovariantGroup covariantGroup = covariantGroups.get(i);
				int valueIndex = (index / covariantGroup.modulus) % covariantGroup.size;
				for (Variable variable : covariantGroup.variables) {
					MultiKey multiKey = variable.multiKeys.get(valueIndex);
					ActionType actionType = multiKey.getKey(0);
					BiConsumer<ScenarioBuilder, MultiKey> biConsumer = actionMap.get(actionType);
					if (biConsumer == null) {
						throw new RuntimeException("unexpected ActionType " + actionType);
					}
					biConsumer.accept(scenarioBuilder, multiKey);
				}
			}
			return scenarioBuilder.build();
		}

		private void buildActionMap() {

			actionMap.put(ActionType.COMPARTMENT_COMPONENT_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				CompartmentId compartmentId = multiKey.getKey(1);
				Class<? extends Component> compartmentComponentClass = multiKey.getKey(2);
				scenarioBuilder.addCompartmentId(compartmentId, compartmentComponentClass);
			});

			actionMap.put(ActionType.COMPARTMENT_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				CompartmentId compartmentId = multiKey.getKey(1);
				CompartmentPropertyId compartmentPropertyId = multiKey.getKey(2);
				PropertyDefinition propertyDefinition = multiKey.getKey(3);
				scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.BATCH_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				MaterialId materialId = multiKey.getKey(1);
				BatchPropertyId batchPropertyId = multiKey.getKey(2);
				PropertyDefinition propertyDefinition = multiKey.getKey(3);
				scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.GROUP_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				GroupTypeId groupTypeId = multiKey.getKey(1);
				GroupPropertyId groupPropertyId = multiKey.getKey(2);
				PropertyDefinition propertyDefinition = multiKey.getKey(3);
				scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.GLOBAL_COMPONENT_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				GlobalComponentId globalComponentId = multiKey.getKey(1);
				Class<? extends Component> globalComponentClass = multiKey.getKey(2);
				scenarioBuilder.addGlobalComponentId(globalComponentId, globalComponentClass);
			});

			actionMap.put(ActionType.GLOBAL_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				GlobalPropertyId globalPropertyId = multiKey.getKey(1);
				PropertyDefinition propertyDefinition = multiKey.getKey(2);
				scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.PERSON_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				PersonPropertyId personPropertyId = multiKey.getKey(1);
				PropertyDefinition propertyDefinition = multiKey.getKey(2);
				scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.PERSON_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				PersonId personId = multiKey.getKey(1);
				RegionId regionId = multiKey.getKey(2);
				CompartmentId compartmentId = multiKey.getKey(3);
				scenarioBuilder.addPerson(personId, regionId, compartmentId);
			});

			actionMap.put(ActionType.REGION_COMPONENT_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				RegionId regionId = multiKey.getKey(1);
				Class<? extends Component> regionComponentClass = multiKey.getKey(2);
				scenarioBuilder.addRegionId(regionId, regionComponentClass);
			});

			actionMap.put(ActionType.GROUP_TYPE_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				GroupTypeId groupTypeId = multiKey.getKey(1);
				scenarioBuilder.addGroupTypeId(groupTypeId);
			});

			actionMap.put(ActionType.REGION_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				RegionPropertyId regionPropertyId = multiKey.getKey(1);
				PropertyDefinition propertyDefinition = multiKey.getKey(2);
				scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.RESOURCE_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				ResourceId resourceId = multiKey.getKey(1);
				scenarioBuilder.addResource(resourceId);
			});

			actionMap.put(ActionType.RESOURCE_TIME_TRACKING_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				ResourceId resourceId = multiKey.getKey(1);
				TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(2);
				scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
			});

			actionMap.put(ActionType.RESOURCE_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				ResourceId resourceId = multiKey.getKey(1);
				ResourcePropertyId resourcePropertyId = multiKey.getKey(2);
				PropertyDefinition propertyDefinition = multiKey.getKey(3);
				scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.MATERIALS_PRODUCER_PROPERTY_DEFINITION, (scenarioBuilder, multiKey) -> {
				MaterialsProducerPropertyId materialsProducerPropertyId = multiKey.getKey(1);
				PropertyDefinition propertyDefinition = multiKey.getKey(2);
				scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
			});

			actionMap.put(ActionType.MATERIAL_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				MaterialId materialId = multiKey.getKey(1);
				scenarioBuilder.addMaterial(materialId);
			});

			actionMap.put(ActionType.RANDOM_NUMBER_GENERATOR_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				RandomNumberGeneratorId randomNumberGeneratorId = multiKey.getKey(1);
				scenarioBuilder.addRandomNumberGeneratorId(randomNumberGeneratorId);
			});

			actionMap.put(ActionType.MATERIALS_PRODUCER_COMPONENT_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				MaterialsProducerId materialsProducerId = multiKey.getKey(1);
				Class<? extends Component> materialsProducerComponentClass = multiKey.getKey(2);
				scenarioBuilder.addMaterialsProducerId(materialsProducerId, materialsProducerComponentClass);
			});

			actionMap.put(ActionType.PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(1);
				scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			});

			actionMap.put(ActionType.PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(1);
				scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			});

			actionMap.put(ActionType.COMPARTMENT_MAP_OPTION_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				MapOption mapOption = multiKey.getKey(1);
				scenarioBuilder.setCompartmentMapOption(mapOption);
			});

			actionMap.put(ActionType.REGION_MAP_OPTION_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				MapOption mapOption = multiKey.getKey(1);
				scenarioBuilder.setRegionMapOption(mapOption);
			});

			actionMap.put(ActionType.BATCH_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				BatchId batchId = multiKey.getKey(1);
				MaterialId materialId = multiKey.getKey(2);
				double amount = multiKey.getKey(3);
				MaterialsProducerId materialsProducerId = multiKey.getKey(4);
				scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
			});

			actionMap.put(ActionType.STAGE_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				StageId stageId = multiKey.getKey(1);
				Boolean offered = multiKey.getKey(2);
				MaterialsProducerId materialsProducerId = multiKey.getKey(3);
				scenarioBuilder.addStage(stageId, offered, materialsProducerId);
			});

			actionMap.put(ActionType.STAGE_MEMBERSHIP_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				StageId stageId = multiKey.getKey(1);
				BatchId batchId = multiKey.getKey(2);
				scenarioBuilder.addBatchToStage(stageId, batchId);
			});

			actionMap.put(ActionType.GROUP_MEMBERSHIP_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				GroupId groupId = multiKey.getKey(1);
				PersonId personId = multiKey.getKey(2);
				scenarioBuilder.addPersonToGroup(groupId, personId);
			});

			actionMap.put(ActionType.GROUP_ID_ADDITION, (scenarioBuilder, multiKey) -> {
				GroupId groupId = multiKey.getKey(1);
				GroupTypeId groupTypeId = multiKey.getKey(2);
				scenarioBuilder.addGroup(groupId, groupTypeId);
			});

			actionMap.put(ActionType.SUGGESTED_POPULATION_SIZE, (scenarioBuilder, multiKey) -> {
				Integer suggestePopulationSize = multiKey.getKey(1);
				scenarioBuilder.setSuggestedPopulationSize(suggestePopulationSize);
			});

			actionMap.put(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				ResourceId resourceId = multiKey.getKey(1);
				ResourcePropertyId resourcePropertyId = multiKey.getKey(2);
				Object resourcePropertyValue = multiKey.getKey(3);
				scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
			});

			actionMap.put(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				RegionId regionId = multiKey.getKey(1);
				RegionPropertyId regionPropertyId = multiKey.getKey(2);
				Object regionPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
			});

			actionMap.put(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				GroupId groupId = multiKey.getKey(1);
				GroupPropertyId groupPropertyId = multiKey.getKey(2);
				Object groupPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
			});

			actionMap.put(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				CompartmentId compartmentId = multiKey.getKey(1);
				CompartmentPropertyId compartmentPropertyId = multiKey.getKey(2);
				Object compartmentPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
			});

			actionMap.put(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				GlobalPropertyId globalPropertyId = multiKey.getKey(1);
				Object globalPropertyValue = multiKey.getKey(2);
				scenarioBuilder.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			});

			actionMap.put(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				PersonId personId = multiKey.getKey(1);
				PersonPropertyId personPropertyId = multiKey.getKey(2);
				Object personPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
			});

			actionMap.put(ActionType.PERSON_RESOURCE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				PersonId personId = multiKey.getKey(1);
				ResourceId resourceId = multiKey.getKey(2);
				Long personResourceLevel = multiKey.getKey(3);
				scenarioBuilder.setPersonResourceLevel(personId, resourceId, personResourceLevel);
			});

			actionMap.put(ActionType.REGION_RESOURCE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				RegionId regionId = multiKey.getKey(1);
				ResourceId resourceId = multiKey.getKey(2);
				Long regionResourceLevel = multiKey.getKey(3);
				scenarioBuilder.setRegionResourceLevel(regionId, resourceId, regionResourceLevel);
			});

			actionMap.put(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				MaterialsProducerId materialsProducerId = multiKey.getKey(1);
				MaterialsProducerPropertyId materialsProducerPropertyId = multiKey.getKey(2);
				Object materialsProducerPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
			});

			actionMap.put(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				MaterialsProducerId materialsProducerId = multiKey.getKey(1);
				ResourceId resourceId = multiKey.getKey(2);
				Long materialsProducerResourceLevel = multiKey.getKey(3);
				scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, materialsProducerResourceLevel);
			});

			actionMap.put(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, (scenarioBuilder, multiKey) -> {
				BatchId batchId = multiKey.getKey(1);
				BatchPropertyId batchPropertyId = multiKey.getKey(2);
				Object batchPropertyValue = multiKey.getKey(3);
				scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
			});

		}

		@Override
		public ScenarioId getScenarioId(int index) {
			return new ScenarioId(baseScenarioId + index + 1);
		}
	}

	/**
	 * Adds a resource property value for the given property as an experiment
	 * dimension value.
	 *
	 *
	 * @throws ModelException
	 *             <li>{@link ScenarioErrorType#NEGATIVE_SUGGGESTED_POPULATION}
	 *             if the suggested population size is negative
	 */
	public void addSuggestedPopulationSize(int suggestedPopulationSize) {
		scaffold.putExperimentValueData(ActionType.SUGGESTED_POPULATION_SIZE, suggestedPopulationSize);
	}

	/**
	 * Attempts to force the expression of the corresponding suggested
	 * population size experiment column in output when there is only one value
	 * in that dimension.
	 * 
	 * @throws RuntimeException
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with the experiment
	 *             column
	 */

	public void forceSuggestedPopulationSizeExperimentColumn() {
		scaffold.putExperimentColumnForceData(ActionType.SUGGESTED_POPULATION_SIZE);
	}

	/**
	 * Marks the suggested population size with a dimension. All property values
	 * marked with the same dimension identifier will vary dependently and so
	 * must have the same number of associated property value variants.
	 *
	 * @throws ModelException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_DIMENSION_IDENTIFIER} if
	 *             the dimension id is null
	 *             <li>{@linkplain ScenarioErrorType#EXPERIMENT_VARIABLE_SIZE_MISMATCH}
	 *             if number of population sizes does not match the number of
	 *             values for all other variables joined under the given
	 *             dimensionId
	 *             <li>{@link ScenarioErrorType#DUPLICATE_EXPERIMENT_COVARIANT_DECLARATION}
	 *             if the suggested population size was previously declared as
	 *             covariant
	 *             <li>{@link ScenarioErrorType#COVARIANT_WITHOUT_VALUES} if
	 *             there are no added values to associate with this dimension
	 *             <li>{@link ScenarioErrorType#EXPERIMENT_COVARIANT_DECLARATION_SPANS_MULTIPLE_DIMENSIONS}
	 *             if there are more than one dimensionId values with the same
	 *             associated data
	 */
	public void covarySuggestedPopulationSize(final Object dimensionId) {
		scaffold.putExperimentDimensionData(ActionType.SUGGESTED_POPULATION_SIZE, dimensionId);
	}

//	/**
//	 * Returns a row-based listing of all the data that will be used to form the
//	 * experiment.
//	 */
//	@Override
//	public String toString() {
//		return scaffold.toString();
//	}
}