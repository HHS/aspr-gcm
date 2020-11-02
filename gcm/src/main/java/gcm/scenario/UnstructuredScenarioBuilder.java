package gcm.scenario;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.components.Component;
import gcm.util.MultiKey;
import gcm.util.annotations.Source;
import net.jcip.annotations.NotThreadSafe;

/**
 * A builder class for Scenario. This class spares its client from ordering
 * conditions in the StructuredScenarioBuilder by storing all arguments passed
 * through its method invocations and then using a StructureScenarioBuilder to
 * construct the scenario by using a proper invocation order.
 * 
 * All exceptions documented in {@link ScenarioBuilder} are delayed until the
 * build() method is invoked.
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
@Source
public final class UnstructuredScenarioBuilder implements ScenarioBuilder {

	private Map<ActionType, List<MultiKey>> data = new LinkedHashMap<>();

	@Override
	public void addCompartmentId(final CompartmentId compartmentId, Class<? extends Component> comparmentComponentClass) {
		put(ActionType.COMPARTMENT_COMPONENT_ID_ADDITION, compartmentId, comparmentComponentClass);
	}

	@Override
	public void addGlobalComponentId(final GlobalComponentId globalComponentId, Class<? extends Component> globalComponentClass) {
		put(ActionType.GLOBAL_COMPONENT_ID_ADDITION, globalComponentId, globalComponentClass);
	}

	@Override
	public void addGroup(final GroupId groupId, final GroupTypeId groupTypeId) {
		put(ActionType.GROUP_ID_ADDITION, groupId, groupTypeId);
	}

	@Override
	public void addPersonToGroup(final GroupId groupId, final PersonId personId) {
		put(ActionType.GROUP_MEMBERSHIP_ASSIGNMENT, groupId, personId, personId);
	}

	@Override
	public void addGroupTypeId(final GroupTypeId groupTypeId) {
		put(ActionType.GROUP_TYPE_ID_ADDITION, groupTypeId, groupTypeId);
	}

	@Override
	public void addMaterial(final MaterialId materialId) {
		put(ActionType.MATERIAL_ID_ADDITION, materialId);
	}

	@Override
	public void addMaterialsProducerId(final MaterialsProducerId materialsProducerId, Class<? extends Component> materialProducerComponentClass) {
		put(ActionType.MATERIALS_PRODUCER_COMPONENT_ID_ADDITION, materialsProducerId, materialProducerComponentClass);
	}

	@Override
	public void addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId) {
		put(ActionType.PERSON_ID_ADDITION, personId, regionId, compartmentId);
	}

	@Override
	public void addRegionId(final RegionId regionId, Class<? extends Component> regionComponentClass) {
		put(ActionType.REGION_COMPONENT_ID_ADDITION, regionId, regionComponentClass);
	}

	@Override
	public void addResource(final ResourceId resourceId) {
		put(ActionType.RESOURCE_ID_ADDITION, resourceId, resourceId);
	}
	
	@Override
	public Scenario build() {
		final StructuredScenarioBuilder structuredScenarioBuilder = new StructuredScenarioBuilder();
		fillStructuredScenarioBuilder(structuredScenarioBuilder);
		return structuredScenarioBuilder.build();
	}

	private void fillStructuredScenarioBuilder(StructuredScenarioBuilder structuredScenarioBuilder) {
		/*
		 * We transfer the arguments that have been stored in data to a
		 * StructuredScenarioBuilder. The order of the invocations to the
		 * structured builder is sensitive to preconditions. For example, to add
		 * a person to a group one must first add the person and add the group.
		 * Further, to add the group one must first add the group type for the
		 * group.
		 */

		try {

			

			for (final MultiKey multiKey : get(ActionType.PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT)) {
				final TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(0);
				structuredScenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			}

			for (final MultiKey multiKey : get(ActionType.PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT)) {
				final TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(0);
				structuredScenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			}

			for (final MultiKey multiKey : get(ActionType.SCENARIO_ID_ASSIGNMENT)) {
				final ScenarioId scenarioId = multiKey.getKey(0);
				structuredScenarioBuilder.setScenarioId(scenarioId);
			}

			for (final MultiKey multiKey : get(ActionType.REGION_MAP_OPTION_ASSIGNMENT)) {
				final MapOption mapOption = multiKey.getKey(0);
				structuredScenarioBuilder.setRegionMapOption(mapOption);
			}

			for (final MultiKey multiKey : get(ActionType.COMPARTMENT_MAP_OPTION_ASSIGNMENT)) {
				final MapOption mapOption = multiKey.getKey(0);
				structuredScenarioBuilder.setCompartmentMapOption(mapOption);
			}

			for (final MultiKey multiKey : get(ActionType.GLOBAL_COMPONENT_ID_ADDITION)) {
				final GlobalComponentId globalComponentId = multiKey.getKey(0);
				Class<? extends Component> globalComponentClass = multiKey.getKey(1);
				structuredScenarioBuilder.addGlobalComponentId(globalComponentId, globalComponentClass);
			}

			for (final MultiKey multiKey : get(ActionType.REGION_COMPONENT_ID_ADDITION)) {
				final RegionId regionId = multiKey.getKey(0);
				Class<? extends Component> regionComponentClass = multiKey.getKey(1);
				structuredScenarioBuilder.addRegionId(regionId, regionComponentClass);
			}

			for (final MultiKey multiKey : get(ActionType.COMPARTMENT_COMPONENT_ID_ADDITION)) {
				final CompartmentId compartmentId = multiKey.getKey(0);
				Class<? extends Component> compartmentComponentClass = multiKey.getKey(1);
				structuredScenarioBuilder.addCompartmentId(compartmentId, compartmentComponentClass);
			}

			for (final MultiKey multiKey : get(ActionType.MATERIALS_PRODUCER_COMPONENT_ID_ADDITION)) {
				final MaterialsProducerId materialsProducerId = multiKey.getKey(0);
				Class<? extends Component> materialsProducerComponentClass = multiKey.getKey(1);
				structuredScenarioBuilder.addMaterialsProducerId(materialsProducerId, materialsProducerComponentClass);
			}

			for (final MultiKey multiKey : get(ActionType.MATERIAL_ID_ADDITION)) {
				final MaterialId materialId = multiKey.getKey(0);
				structuredScenarioBuilder.addMaterial(materialId);
			}

			for (final MultiKey multiKey : get(ActionType.RANDOM_NUMBER_GENERATOR_ID_ADDITION)) {
				final RandomNumberGeneratorId randomNumberGeneratorId = multiKey.getKey(0);
				structuredScenarioBuilder.addRandomNumberGeneratorId(randomNumberGeneratorId);
			}

			for (final MultiKey multiKey : get(ActionType.GROUP_TYPE_ID_ADDITION)) {
				final GroupTypeId groupTypeId = multiKey.getKey(0);
				structuredScenarioBuilder.addGroupTypeId(groupTypeId);
			}

			for (final MultiKey multiKey : get(ActionType.RESOURCE_ID_ADDITION)) {
				final ResourceId resourceId = multiKey.getKey(0);
				structuredScenarioBuilder.addResource(resourceId);
			}

			for (final MultiKey multiKey : get(ActionType.BATCH_PROPERTY_DEFINITION)) {
				final MaterialId materialId = multiKey.getKey(0);
				final BatchPropertyId batchPropertyId = multiKey.getKey(1);
				final PropertyDefinition propertyDefinition = multiKey.getKey(2);
				structuredScenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.COMPARTMENT_PROPERTY_DEFINITION)) {
				final CompartmentId compartmentId = multiKey.getKey(0);
				final CompartmentPropertyId compartmentPropertyId = multiKey.getKey(1);
				final PropertyDefinition propertyDefinition = multiKey.getKey(2);
				structuredScenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.REGION_PROPERTY_DEFINITION)) {
				final RegionPropertyId regionPropertyId = multiKey.getKey(0);
				final PropertyDefinition propertyDefinition = multiKey.getKey(1);
				structuredScenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.RESOURCE_PROPERTY_DEFINITION)) {
				final ResourceId resourceId = multiKey.getKey(0);
				final ResourcePropertyId resourcePropertyId = multiKey.getKey(1);
				final PropertyDefinition propertyDefinition = multiKey.getKey(2);
				structuredScenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.GLOBAL_PROPERTY_DEFINITION)) {
				final GlobalPropertyId globalPropertyId = multiKey.getKey(0);
				final PropertyDefinition propertyDefinition = multiKey.getKey(1);
				structuredScenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.PERSON_PROPERTY_DEFINITION)) {
				final PersonPropertyId personPropertyId = multiKey.getKey(0);
				final PropertyDefinition propertyDefinition = multiKey.getKey(1);
				structuredScenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.MATERIALS_PRODUCER_PROPERTY_DEFINITION)) {
				final MaterialsProducerPropertyId materialsProducerPropertyId = multiKey.getKey(0);
				final PropertyDefinition propertyDefinition = multiKey.getKey(1);
				structuredScenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.GROUP_PROPERTY_DEFINITION)) {
				final GroupTypeId groupTypeId = multiKey.getKey(0);
				final GroupPropertyId groupPropertyId = multiKey.getKey(1);
				final PropertyDefinition propertyDefinition = multiKey.getKey(2);
				structuredScenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
			}

			for (final MultiKey multiKey : get(ActionType.RESOURCE_TIME_TRACKING_ASSIGNMENT)) {
				final ResourceId resourceId = multiKey.getKey(0);
				final TimeTrackingPolicy timeTrackingPolicy = multiKey.getKey(1);
				structuredScenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
			}

			for (final MultiKey multiKey : get(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT)) {
				final ResourceId resourceId = multiKey.getKey(0);
				final ResourcePropertyId resourcePropertyId = multiKey.getKey(1);
				final Object resourcePropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT)) {
				final RegionId regionId = multiKey.getKey(0);
				final RegionPropertyId regionPropertyId = multiKey.getKey(1);
				final Object regionPropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT)) {
				final MaterialsProducerId materialsProducerId = multiKey.getKey(0);
				final MaterialsProducerPropertyId materialsProducerPropertyId = multiKey.getKey(1);
				final Object materialsPropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, materialsPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT)) {
				final CompartmentId compartmentId = multiKey.getKey(0);
				final CompartmentPropertyId compartmentPropertyId = multiKey.getKey(1);
				final Object compartmentPropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT)) {
				final GlobalPropertyId globalPropertyId = multiKey.getKey(0);
				final Object globalPropertyValue = multiKey.getKey(1);
				structuredScenarioBuilder.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.REGION_RESOURCE_ASSIGNMENT)) {
				final RegionId regionId = multiKey.getKey(0);
				final ResourceId resourceId = multiKey.getKey(1);
				final Long amount = multiKey.getKey(2);
				structuredScenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
			}

			for (final MultiKey multiKey : get(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT)) {
				final MaterialsProducerId materialsProducerId = multiKey.getKey(0);
				final ResourceId resourceId = multiKey.getKey(1);
				final Long amount = multiKey.getKey(2);
				structuredScenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
			}

			for (final MultiKey multiKey : get(ActionType.PERSON_ID_ADDITION)) {
				final PersonId personId = multiKey.getKey(0);
				final RegionId regionId = multiKey.getKey(1);
				final CompartmentId compartmentId = multiKey.getKey(2);
				structuredScenarioBuilder.addPerson(personId, regionId, compartmentId);
			}

			for (final MultiKey multiKey : get(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT)) {
				final PersonId personId = multiKey.getKey(0);
				final PersonPropertyId personPropertyId = multiKey.getKey(1);
				final Object personPropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.PERSON_RESOURCE_ASSIGNMENT)) {
				final PersonId personId = multiKey.getKey(0);
				final ResourceId resourceId = multiKey.getKey(1);
				final Long amount = multiKey.getKey(2);
				structuredScenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
			}

			for (final MultiKey multiKey : get(ActionType.GROUP_ID_ADDITION)) {
				final GroupId groupId = multiKey.getKey(0);
				final GroupTypeId groupTypeId = multiKey.getKey(1);
				structuredScenarioBuilder.addGroup(groupId, groupTypeId);
			}

			for (final MultiKey multiKey : get(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT)) {
				final GroupId groupId = multiKey.getKey(0);
				final GroupPropertyId groupPropertyId = multiKey.getKey(1);
				final Object propertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.GROUP_MEMBERSHIP_ASSIGNMENT)) {
				final GroupId groupId = multiKey.getKey(0);
				final PersonId personId = multiKey.getKey(1);
				structuredScenarioBuilder.addPersonToGroup(groupId, personId);
			}

			for (final MultiKey multiKey : get(ActionType.BATCH_ID_ADDITION)) {
				final BatchId batchId = multiKey.getKey(0);
				final MaterialId materialId = multiKey.getKey(1);
				final Double amount = multiKey.getKey(2);
				final MaterialsProducerId materialsProducerId = multiKey.getKey(3);
				structuredScenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
			}

			for (final MultiKey multiKey : get(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT)) {
				final BatchId batchId = multiKey.getKey(0);
				final BatchPropertyId batchPropertyId = multiKey.getKey(1);
				final Object batchPropertyValue = multiKey.getKey(2);
				structuredScenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
			}

			for (final MultiKey multiKey : get(ActionType.STAGE_ID_ADDITION)) {
				final StageId stageId = multiKey.getKey(0);
				final Boolean offered = multiKey.getKey(1);
				final MaterialsProducerId materialsProducerId = multiKey.getKey(2);
				structuredScenarioBuilder.addStage(stageId, offered, materialsProducerId);
			}

			for (final MultiKey multiKey : get(ActionType.STAGE_MEMBERSHIP_ASSIGNMENT)) {
				final StageId stageId = multiKey.getKey(0);
				final BatchId batchId = multiKey.getKey(1);
				structuredScenarioBuilder.addBatchToStage(stageId, batchId);
			}

			for (final MultiKey multiKey : get(ActionType.SUGGESTED_POPULATION_SIZE)) {
				final Integer suggestedPopulationSize = multiKey.getKey(0);
				structuredScenarioBuilder.setSuggestedPopulationSize(suggestedPopulationSize);
			}
			
			for (final MultiKey multiKey : get(ActionType.DENSE_PARTITIONS)) {
				final Boolean useDensePartitions = multiKey.getKey(0);
				structuredScenarioBuilder.setUseDensePartitions(useDensePartitions);
			}
			
			
		} finally {
			data = new LinkedHashMap<>();
		}
	}

	@Override
	public void defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.BATCH_PROPERTY_DEFINITION, materialId, batchPropertyId, propertyDefinition);
	}

	@Override
	public void defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.COMPARTMENT_PROPERTY_DEFINITION, compartmentId, compartmentPropertyId, propertyDefinition);
	}

	@Override
	public void defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.GLOBAL_PROPERTY_DEFINITION, globalPropertyId, propertyDefinition);
	}

	@Override
	public void defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.GROUP_PROPERTY_DEFINITION, groupTypeId, groupPropertyId, propertyDefinition);
	}

	@Override
	public void defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.MATERIALS_PRODUCER_PROPERTY_DEFINITION, materialsProducerPropertyId, propertyDefinition);
	}

	@Override
	public void definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.PERSON_PROPERTY_DEFINITION, personPropertyId, propertyDefinition);
	}

	@Override
	public void defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.REGION_PROPERTY_DEFINITION, regionPropertyId, propertyDefinition);
	}

	@Override
	public void defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
		put(ActionType.RESOURCE_PROPERTY_DEFINITION, resourceId, resourcePropertyId, propertyDefinition);
	}

	private List<MultiKey> get(final ActionType scenarioDataType) {
		List<MultiKey> result = data.get(scenarioDataType);
		if (result == null) {
			result = new ArrayList<>();
			data.put(scenarioDataType, result);
		}
		return result;
	}

	private void put(final ActionType scenarioDataType, final Object... arguments) {
		List<MultiKey> list = data.get(scenarioDataType);
		if (list == null) {
			list = new ArrayList<>();
			data.put(scenarioDataType, list);
		}
		final MultiKey.MultiKeyBuilder multiKeyBuilder = new MultiKey.MultiKeyBuilder();
		for (final Object argument : arguments) {
			multiKeyBuilder.addKey(argument);
		}
		list.add(multiKeyBuilder.build());
	}

	@Override
	public void setCompartmentMapOption(final MapOption mapOption) {
		put(ActionType.COMPARTMENT_MAP_OPTION_ASSIGNMENT, mapOption);
	}

	@Override
	public void setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		put(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentId, compartmentPropertyId, compartmentPropertyValue);
	}

	@Override
	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		put(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId, globalPropertyValue);
	}

	@Override
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		put(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupId, groupPropertyId, groupPropertyValue);
	}

	@Override
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		put(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
	}

	@Override
	public void setMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		put(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, materialsProducerId, resourceId, amount);
	}

	@Override
	public void setPersonCompartmentArrivalTracking(final TimeTrackingPolicy trackPersonCompartmentArrivalTimes) {
		put(ActionType.PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT, trackPersonCompartmentArrivalTimes);
	}

	@Override
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		put(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personId, personPropertyId, personPropertyValue);
	}

	@Override
	public void setPersonRegionArrivalTracking(final TimeTrackingPolicy trackPersonRegionArrivalTimes) {
		put(ActionType.PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT, trackPersonRegionArrivalTimes);
	}

	@Override
	public void setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount) {
		put(ActionType.PERSON_RESOURCE_ASSIGNMENT, personId, resourceId, amount);

	}

	@Override
	public void setRegionMapOption(final MapOption mapOption) {
		put(ActionType.REGION_MAP_OPTION_ASSIGNMENT, mapOption);
	}

	@Override
	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
		put(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionId, regionPropertyId, regionPropertyValue);
	}

	@Override
	public void setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		put(ActionType.REGION_RESOURCE_ASSIGNMENT, regionId, resourceId, amount);

	}

	@Override
	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object propertyValue) {
		put(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourceId, resourcePropertyId, propertyValue);
	}

	@Override
	public void setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes) {
		put(ActionType.RESOURCE_TIME_TRACKING_ASSIGNMENT, resourceId, trackValueAssignmentTimes);
	}

	@Override
	public void setScenarioId(final ScenarioId scenarioId) {
		put(ActionType.SCENARIO_ID_ASSIGNMENT, scenarioId);
	}

	@Override
	public void setBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue) {
		put(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchId, batchPropertyId, batchPropertyValue);
	}

	@Override
	public void addBatch(BatchId batchId, MaterialId materialId, double amount, MaterialsProducerId materialsProducerId) {
		put(ActionType.BATCH_ID_ADDITION, batchId, materialId, amount, materialsProducerId);
	}

	@Override
	public void addStage(StageId stageId, boolean offered, MaterialsProducerId materialsProducerId) {
		put(ActionType.STAGE_ID_ADDITION, stageId, offered, materialsProducerId);
	}

	@Override
	public void addBatchToStage(StageId stageId, BatchId batchId) {
		put(ActionType.STAGE_MEMBERSHIP_ASSIGNMENT, stageId, batchId);
	}

	@Override
	public void setSuggestedPopulationSize(int suggestedPopulationSize) {
		put(ActionType.SUGGESTED_POPULATION_SIZE, suggestedPopulationSize);
	}

	@Override
	public void addRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		put(ActionType.RANDOM_NUMBER_GENERATOR_ID_ADDITION, randomNumberGeneratorId);
	}

	@Override
	public void setUseDensePartitions(boolean useDensePartitions) {
		// TODO Auto-generated method stub
		put(ActionType.DENSE_PARTITIONS,useDensePartitions);
	}

}
