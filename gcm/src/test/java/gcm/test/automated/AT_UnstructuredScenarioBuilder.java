package gcm.test.automated;

import static gcm.test.support.ExceptionAssertion.assertScenarioException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.Set;

import org.junit.Test;

import gcm.components.AbstractComponent;
import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalComponentId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MapOption;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.scenario.ScenarioException.ScenarioErrorType;
import gcm.simulation.Environment;
import gcm.test.automated.AT_Simulation.EmptyComponent;
import gcm.test.support.TestCompartmentId;
import gcm.test.support.TestGlobalComponentId;
import gcm.test.support.TestGlobalPropertyId;
import gcm.test.support.TestGroupTypeId;
import gcm.test.support.TestMaterialId;
import gcm.test.support.TestMaterialsProducerId;
import gcm.test.support.TestMaterialsProducerPropertyId;
import gcm.test.support.TestPersonPropertyId;
import gcm.test.support.TestRegionId;
import gcm.test.support.TestRegionPropertyId;
import gcm.test.support.TestResourceId;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link UnstructuredScenarioBuilder}
 * 
 * UnstructuredScenarioBuilder methods are invoked in an order that is
 * inconsistent with the ordering requirements of the StructuredScenarioBuilder.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = UnstructuredScenarioBuilder.class)
public class AT_UnstructuredScenarioBuilder {

	/*
	 * A placeholder implementation to satisfy scenario construction
	 */
	private static class PlaceholderComponent extends AbstractComponent {

		@Override
		public void close(Environment environment) {

		}

		@Override
		public void init(Environment environment) {

		}

	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#build() }
	 */
	@Test
	public void testBuild() {
		// No test performed: The build method is tested by proxy via the other
		// test methods.
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addBatch(BatchId, MaterialId, double, gcm.scenario.MaterialsProducerId)}
	 */
	@Test
	public void testAddBatch() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		BatchId batchId = new BatchId(14);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 10;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the batch id is null
		scenarioBuilder.addBatch(null, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_ID);

		// precondition: if the material id is null
		scenarioBuilder.addBatch(batchId, null, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIAL_ID);

		// precondition: if the material id is unknown
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIAL_ID);

		// precondition: if the amount is negative
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, -1, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NEGATIVE_MATERIAL_AMOUNT);

		// precondition: if the amount is not finite
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, Double.POSITIVE_INFINITY, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);

		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, Double.NEGATIVE_INFINITY, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);

		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, Double.NaN, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);

		// precondition: if the materials producer id is null
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, amount, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition: if the materials producer id is unknown
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition: if the batch id was previously added
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the batch has the expected amount
		assertEquals(amount, scenario.getBatchAmount(batchId).doubleValue(), 0);
		// postcondition: the batch has the expected material type
		assertEquals(materialId, scenario.getBatchMaterial(batchId));
		// postcondition: the batch is owned by the expected materials producer
		assertEquals(materialsProducerId, scenario.getBatchMaterialsProducer(batchId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addBatchToStage(StageId, BatchId)}
	 */
	@Test
	public void testAddBatchToStage() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		StageId stageId = new StageId(37);
		BatchId batchId = new BatchId(15);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 13.5;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		// precondition: if the stage id is null
		scenarioBuilder.addBatchToStage(null, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_STAGE_ID);

		// precondition: if the stage id is unknown
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_STAGE_ID);

		// precondition: if the batch id is null
		scenarioBuilder.addBatchToStage(stageId, null);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_ID);

		// precondition: if the batch id is unknown
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_BATCH_ID);

		// precondition: if the stage and batch are not associated with the same
		// materials producer
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER);

		// precondition: if the batch is already associated any stage
		StageId alternateStageId = new StageId(319);
		scenarioBuilder.addBatchToStage(alternateStageId, batchId);
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addStage(alternateStageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.BATCH_ALREADY_STAGED);

		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		Set<BatchId> stageBatches = scenario.getStageBatches(stageId);

		// postcondition: the stage is associated with only the batches added
		assertEquals(1, stageBatches.size());

		// postcondition: the stage is associated with the batch
		assertTrue(stageBatches.contains(batchId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addCompartmentId(CompartmentId, Class)}
	 */
	@Test
	public void testAddCompartmentId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition: if the compartment id is null
		scenarioBuilder.addCompartmentId(null, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition: if the compartment id is equal to another previously
		// added component id
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// precondition: if the component class is null
		scenarioBuilder.addCompartmentId(compartmentId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario should contain the compartment and
		// component class
		assertTrue(scenario.getCompartmentIds().contains(compartmentId));
		assertEquals(PlaceholderComponent.class, scenario.getCompartmentComponentClass(compartmentId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addGlobalComponentId(GlobalComponentId, Class)}
	 */
	@Test
	public void testAddGlobalComponentId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GlobalComponentId globalComponentId = TestGlobalComponentId.GLOBAL_COMPONENT_1;

		// precondition: if the global component id is null
		scenarioBuilder.addGlobalComponentId(null, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition: if the global component id is equal to another
		// previously
		// added component id
		scenarioBuilder.addGlobalComponentId(globalComponentId, PlaceholderComponent.class);
		scenarioBuilder.addGlobalComponentId(globalComponentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// precondition : if the global component class is null
		scenarioBuilder.addGlobalComponentId(globalComponentId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addGlobalComponentId(globalComponentId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGlobalComponentIds().contains(globalComponentId));
		assertEquals(PlaceholderComponent.class, scenario.getGlobalComponentClass(globalComponentId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addGroup(GroupId, GroupTypeId)}
	 */
	@Test
	public void testAddGroup() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(15);
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition : if the group id is null
		scenarioBuilder.addGroup(null, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_ID);

		// precondition : if the group type id is null
		scenarioBuilder.addGroup(groupId, null);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_TYPE_ID);

		// precondition : if the group type id is unknown
		scenarioBuilder.addGroup(groupId, groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID);

		// precondition : if the group was previously added
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();
		// post condition: the scenario contains the group
		scenario.getGroupIds().contains(groupId);
		// post condition: the group has the expected group type
		assertEquals(groupTypeId, scenario.getGroupTypeId(groupId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addGroupTypeId(GroupTypeId)}
	 */
	@Test
	public void testAddGroupTypeId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition: if the group type id is null
		scenarioBuilder.addGroupTypeId(null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_TYPE_ID);

		// precondition: if the group type was previously added
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the group type id
		assertTrue(scenario.getGroupTypeIds().contains(groupTypeId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addMaterial(MaterialId)}
	 */
	@Test
	public void testAddMaterial() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		// precondition: if the material id is null
		scenarioBuilder.addMaterial(null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIAL_ID);

		// precondition: if the material was previously added
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterial(materialId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addMaterial(materialId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition : the scenario contains the material id
		assertTrue(scenario.getMaterialIds().contains(materialId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addMaterialsProducerId(gcm.scenario.MaterialsProducerId, Class)}
	 */
	@Test
	public void testAddMaterialsProducerId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the materials producer id is null
		scenarioBuilder.addMaterialsProducerId(null, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition: if the materials producer id is equal to another
		// previously added component id
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// precondition: if the materials producer component class is null
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the materials producer id
		assertTrue(scenario.getMaterialsProducerIds().contains(materialsProducerId));
		assertEquals(PlaceholderComponent.class, scenario.getMaterialsProducerComponentClass(materialsProducerId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addPerson(PersonId, RegionId, CompartmentId)}
	 */
	@Test
	public void testAddPerson() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(56);
		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition: if the person id is null
		scenarioBuilder.addPerson(null, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_ID);

		// precondition: if the region id is null
		scenarioBuilder.addPerson(personId, null, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_ID);

		// precondition: if the region id is unknown
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_REGION_ID);

		// precondition: if the compartment id is null
		scenarioBuilder.addPerson(personId, regionId, null);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_ID);

		// precondition: if the compartment id is unknown
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);

		// precondition: if the person was previously added
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the person
		assertTrue(scenario.getPeopleIds().contains(personId));
		// postcondition: the person has the expected compartment
		assertEquals(compartmentId, scenario.getPersonCompartment(personId));
		// postcondition: the person has the expected region
		assertEquals(regionId, scenario.getPersonRegion(personId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addPersonToGroup(GroupId, PersonId)}
	 */
	@Test
	public void testAddPersonToGroup() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(45);
		PersonId personId = new PersonId(37);
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_2;
		RegionId regionId = TestRegionId.REGION_5;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;

		// precondition: if the person id is null
		scenarioBuilder.addPersonToGroup(groupId, null);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_ID);

		// precondition: if the person id is unknown
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_PERSON_ID);

		// precondition: if the group id is null
		scenarioBuilder.addPersonToGroup(null, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_ID);

		// precondition: if the group id is unknown
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GROUP_ID);

		// precondition: if the person was previously added to the group
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_GROUP_MEMBERSHIP);

		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the person is in the group
		assertTrue(scenario.getGroupMembers(groupId).contains(personId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addRegionId(RegionId, Class)}
	 */
	@Test
	public void testAddRegionId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionId regionId = TestRegionId.REGION_4;

		// precondition: if the region id is null
		scenarioBuilder.addRegionId(null, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition: if the region was previously added
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// precondition: if the region component class is null
		scenarioBuilder.addRegionId(regionId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the region and component class
		assertTrue(scenario.getRegionIds().contains(regionId));
		assertEquals(PlaceholderComponent.class, scenario.getRegionComponentClass(regionId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addResource(ResourceId)}
	 */
	@Test
	public void testAddResource() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE2;

		// precondition: if the resource id is null
		scenarioBuilder.addResource(null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource was previously added
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getResourceIds().contains(resourceId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addStage(StageId, boolean, gcm.scenario.MaterialsProducerId)}
	 */
	@Test
	public void testAddStage() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		StageId stageId = new StageId(67);

		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		// precondition: if the stage id is null
		scenarioBuilder.addStage(null, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_STAGE_ID);

		// precondition: if the materials producer id is null
		scenarioBuilder.addStage(stageId, true, null);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition: if the materials producer id is unknown
		scenarioBuilder.addStage(stageId, true, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition: if the stage id was previously added
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// an initially offered stage
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.getStageIds().contains(stageId));
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.isStageOffered(stageId));
		// postcondition: the stage is owned by the materials producer in the
		// scenario
		assertEquals(materialsProducerId, scenario.getStageMaterialsProducer(stageId));
		// postcondition: the stage has no associated batches in the scenario
		assertTrue(scenario.getStageBatches(stageId).isEmpty());

		// a stage not yet offered
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.getStageIds().contains(stageId));
		// postcondition: the scenario contains the stage id
		assertFalse(scenario.isStageOffered(stageId));
		// postcondition: the stage is owned by the materials producer in the
		// scenario
		assertEquals(materialsProducerId, scenario.getStageMaterialsProducer(stageId));
		// postcondition: the stage has no associated batches in the scenario
		assertTrue(scenario.getStageBatches(stageId).isEmpty());

	}

	private PropertyDefinition generateRandomPropertyDefinition(Random random) {
		Class<?> type;
		final int typeCase = random.nextInt(4);
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = random.nextBoolean();
			break;
		case 1:
			type = Integer.class;
			defaultValue = random.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + random.nextInt();
			break;
		default:
			type = Long.class;
			defaultValue = random.nextLong();
			break;
		}
		boolean propertyValuesAreMutability = random.nextBoolean();
		MapOption mapOption = MapOption.values()[random.nextInt(MapOption.values().length)];
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[random.nextInt(TimeTrackingPolicy.values().length)];

		final PropertyDefinition result = PropertyDefinition.builder()//
															.setType(type)//
															.setDefaultValue(defaultValue)//
															.setPropertyValueMutability(propertyValuesAreMutability)//
															.setMapOption(mapOption)//
															.setTimeTrackingPolicy(timeTrackingPolicy)//
															.build();//
		return result;
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineBatchProperty(MaterialId, BatchPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineBatchProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random(47348457892L);
		PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(random);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		BatchPropertyId batchPropertyId = TestMaterialId.MATERIAL_1.getBatchPropertyIds()[0];

		// precondition: if the material id is null
		scenarioBuilder.defineBatchProperty(null, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIAL_ID);

		// precondition: if the material id is unknown
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIAL_ID);

		// precondition: if the property id is null
		scenarioBuilder.defineBatchProperty(materialId, null, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, null);
		scenarioBuilder.addMaterial(materialId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_PROPERTY_DEFINITION);

		// precondition: if the batch property was previously defined
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_BATCH_PROPERTY_DEFINITION);

		for (TestMaterialId material : TestMaterialId.values()) {
			for (BatchPropertyId property : material.getBatchPropertyIds()) {
				propertyDefinition = generateRandomPropertyDefinition(random);
				scenarioBuilder.defineBatchProperty(material, property, propertyDefinition);
				scenarioBuilder.addMaterial(material);
				Scenario scenario = scenarioBuilder.build();
				// postcondition: the scenario should contain the property id
				assertTrue(scenario.getBatchPropertyIds(material).contains(property));
				// postcondition: the scenario should contain the property
				// definition
				assertEquals(propertyDefinition, scenario.getBatchPropertyDefinition(material, property));
			}
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineCompartmentProperty(CompartmentPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineCompartmentProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_3.getCompartmentPropertyId(0);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(5)//
																	.build();//

		// precondition: if the compartment id is null
		scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);
		scenarioBuilder.defineCompartmentProperty(null, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_ID);

		// precondition: if the compartment id is unknown
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);

		// precondition: if the property id is null
		scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_DEFINITION);

		// precondition: if the compartment property was previously defined
		scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION);

		for (int i = 0; i < 100; i++) {
			CompartmentPropertyId property = new CompartmentPropertyId() {
			};
			PropertyDefinition propertyDef = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineCompartmentProperty(compartmentId, property, propertyDef);
			scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getCompartmentPropertyIds(compartmentId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDef, scenario.getCompartmentPropertyDefinition(compartmentId, property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineGlobalProperty(GlobalPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineGlobalProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random();
		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(5)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineGlobalProperty(null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.defineGlobalProperty(globalPropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GLOBAL_PROPERTY_DEFINITION);

		// precondition: if the global property was previously defined
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_GLOBAL_PROPERTY_DEFINITION);

		for (int i = 0; i < 1000; i++) {
			GlobalPropertyId property = new GlobalPropertyId() {
			};
			PropertyDefinition propertyDef = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineGlobalProperty(property, propertyDef);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getGlobalPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDef, scenario.getGlobalPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineGroupProperty(GroupTypeId, GroupPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineGroupProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(235)//
																	.build();//

		// precondition: if the group type id is null
		scenarioBuilder.defineGroupProperty(null, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_TYPE_ID);

		// precondition: if the group type id is unknown
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID);

		// precondition: if the property id is null
		scenarioBuilder.defineGroupProperty(groupTypeId, null, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, null);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_PROPERTY_DEFINITION);

		// precondition: if the group property was previously defined
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_GROUP_PROPERTY_DEFINITION);

		Random random = new Random(34593450987L);
		for (int i = 0; i < 1000; i++) {
			GroupPropertyId property = new GroupPropertyId() {
			};
			groupTypeId = TestGroupTypeId.values()[random.nextInt(TestGroupTypeId.values().length)];
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineGroupProperty(groupTypeId, property, propertyDefinition);
			scenarioBuilder.addGroupTypeId(groupTypeId);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getGroupPropertyIds(groupTypeId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getGroupPropertyDefinition(groupTypeId, property));
		}

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineMaterialsProducerProperty(MaterialsProducerPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineMaterialsProducerProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(6)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineMaterialsProducerProperty(null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION);

		// precondition: if the materials producer property was previously
		// defined
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION);

		Random random = new Random(474563456347L);
		for (int i = 0; i < 100; i++) {
			MaterialsProducerPropertyId matProducerPropertyId = new MaterialsProducerPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineMaterialsProducerProperty(matProducerPropertyId, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getMaterialsProducerPropertyIds().contains(matProducerPropertyId));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getMaterialsProducerPropertyDefinition(matProducerPropertyId));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#definePersonProperty(PersonPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefinePersonProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("value")//
																	.build();//

		// precondition if the property id is null
		scenarioBuilder.definePersonProperty(null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_PROPERTY_ID);

		// precondition if the property definition is null
		scenarioBuilder.definePersonProperty(personPropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_PROPERTY_DEFINITION);

		// precondition if the person property was previously defined
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_PERSON_PROPERTY_DEFINITION);

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			PersonPropertyId property = new PersonPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.definePersonProperty(property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getPersonPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getPersonPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineRegionProperty(RegionPropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineRegionProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(6.7)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineRegionProperty(null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.defineRegionProperty(regionPropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_PROPERTY_DEFINITION);

		// precondition: if the region property was previously defined
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_REGION_PROPERTY_DEFINITION);

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			RegionPropertyId property = new RegionPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineRegionProperty(property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getRegionPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getRegionPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineResourceProperty(ResourcePropertyId, PropertyDefinition)}
	 */
	@Test
	public void testDefineResourceProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE3;
		ResourcePropertyId resourcePropertyId = TestResourceId.RESOURCE3.getResourcePropertyIds()[0];
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Long.class)//
																	.setDefaultValue(3454L)//
																	.build();//

		// precondition: if the resource id is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(null, resourcePropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(TestResourceId.getUnknownResourceId(), resourcePropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the property id is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, null, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID);

		// precondition: if the property definition is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_PROPERTY_DEFINITION);

		// precondition: if the resource property was previously defined
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.DUPLICATE_RESOURCE_PROPERTY_DEFINITION);

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			ResourcePropertyId property = new ResourcePropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.addResource(resourceId);
			scenarioBuilder.defineResourceProperty(resourceId, property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getResourcePropertyIds(resourceId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getResourcePropertyDefinition(resourceId, property));
		}
	}

	private static Object generateIncompatiblePropertyValue(final PropertyDefinition propertyDefinition, final Random random) {

		final Class<?> type = propertyDefinition.getType();

		if (type == Boolean.class) {
			return random.nextLong();
		} else if (type == Integer.class) {
			return random.nextBoolean();

		} else if (type == String.class) {
			return random.nextInt();

		} else if (type == Long.class) {
			return "String " + random.nextInt();
		} else {
			throw new RuntimeException("unknown type " + type);
		}
	}

	/*
	 * Generates a property value consistent with the property definition that
	 * is not equal to the definition's default value.
	 */
	private static Object generatePropertyValue(final PropertyDefinition propertyDefinition, final Random random) {

		final Class<?> type = propertyDefinition.getType();
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("requires a property definition with a non-null default value");
		}
		
		Object defaultValue = propertyDefinition.getDefaultValue().get();
		Object result = defaultValue;
		while (result.equals(defaultValue)) {
			if (type == Boolean.class) {
				result = random.nextBoolean();

			} else if (type == Integer.class) {
				result = random.nextInt();

			} else if (type == String.class) {
				result = "String " + random.nextInt();

			} else if (type == Long.class) {
				result = random.nextLong();

			} else {
				throw new RuntimeException("unknown type " + type);
			}
		}
		return result;
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#setBatchPropertyValue}
	 */
	@Test
	public void testSetBatchPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random(5745690788442345906L);

		BatchId batchId = new BatchId(645778);
		BatchPropertyId batchPropertyId = TestMaterialId.MATERIAL_3.getBatchPropertyIds()[0];
		PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(random);
		Object propertyValue = generatePropertyValue(propertyDefinition, random);
		Object incompatiblePropertyValue = generateIncompatiblePropertyValue(propertyDefinition, random);
		MaterialId materialId = TestMaterialId.MATERIAL_3;
		double amount = 2341456;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the batch id is null
		scenarioBuilder.setBatchPropertyValue(null, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_ID);

		// precondition: if the batch id is unknown
		scenarioBuilder.setBatchPropertyValue(new BatchId(234), batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_BATCH_ID);

		// precondition: if the batch property id is null
		scenarioBuilder.setBatchPropertyValue(batchId, null, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_PROPERTY_ID);

		// precondition: if the batch property id is unknown
		scenarioBuilder.setBatchPropertyValue(batchId, TestMaterialId.getUnknownBatchPropertyId(), propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, null);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_BATCH_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, incompatiblePropertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the batch property value was previously set
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the batch has the expected property value
		assertEquals(propertyValue, scenario.getBatchPropertyValue(batchId, batchPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setCompartmentMapOption(MapOption)}
	 */
	@Test
	public void testSetCompartmentMapOption() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		for (MapOption mapOption : MapOption.values()) {

			// precondition: if the mapOption is null
			scenarioBuilder.setCompartmentMapOption(null);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_MAP_OPTION);

			// precondition: if the mapOption was previously set
			scenarioBuilder.setCompartmentMapOption(mapOption);
			scenarioBuilder.setCompartmentMapOption(mapOption);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			scenarioBuilder.setCompartmentMapOption(mapOption);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario has the expected compartment map
			// option
			assertEquals(mapOption, scenario.getCompartmentMapOption());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setCompartmentPropertyValue(CompartmentId, CompartmentPropertyId, Object)}
	 */
	@Test
	public void testSetCompartmentPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_4;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_4.getCompartmentPropertyId(0);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(15)//
																	.build();//
		Object propertyValue = 77;

		// precondition: if the compartment property id is null
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, null, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID);

		// precondition: if the compartment property id is unknown
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, TestCompartmentId.getUnknownCompartmentPropertyId(), propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID);

		// precondition: if the compartment id is null
		scenarioBuilder.setCompartmentPropertyValue(null, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_ID);

		// precondition: if the compartment id is unknown
		scenarioBuilder.setCompartmentPropertyValue(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);

		// precondition: if the value is null
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, null);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, "incompatible value");
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the compartment property value was previously set
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the compartment has the expected property value
		assertEquals(propertyValue, scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setGlobalPropertyValue(GlobalPropertyId, Object)}
	 */
	@Test
	public void testSetGlobalPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		Object propertyValue = "value";
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("default")//
																	.build();//

		// precondition: if the global property id is null
		scenarioBuilder.setGlobalPropertyValue(null, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID);

		// precondition: if the global property id is unknown
		scenarioBuilder.setGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId(), propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, null);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GLOBAL_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, 67);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the global property value was previously set
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the global property has the expected value
		assertEquals(propertyValue, scenario.getGlobalPropertyValue(globalPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setGroupPropertyValue(GroupId, GroupPropertyId, Object)}
	 */
	@Test
	public void testSetGroupPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(64);
		GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_4.getGroupPropertyIds()[0];
		Object propertyValue = 78;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(45)//
																	.build();//
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition: if the group id is null
		scenarioBuilder.setGroupPropertyValue(null, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_ID);

		// precondition: if the group id is unknown
		scenarioBuilder.setGroupPropertyValue(new GroupId(345), groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GROUP_ID);

		// precondition: if the group property is null
		scenarioBuilder.setGroupPropertyValue(groupId, null, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_PROPERTY_ID);

		// precondition: if the group property is unknown
		scenarioBuilder.setGroupPropertyValue(groupId, TestGroupTypeId.getUnknownGroupPropertyId(), propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, null);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_GROUP_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, "incompatible value");
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the group property value was previously set
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the group has the expected property value
		assertEquals(propertyValue, scenario.getGroupPropertyValue(groupId, groupPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setMaterialsProducerPropertyValue(gcm.scenario.MaterialsProducerId, MaterialsProducerPropertyId, Object)}
	 */
	@Test
	public void testSetMaterialsProducerPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1;
		Object propertyValue = 45;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(634)//
																	.build();//

		// precondition: if the materials producer property id is null
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);

		// precondition: if the materials producer property id is unknown
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID);

		// precondition: if the materials producer id is null
		scenarioBuilder.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition: if the materials producer id is unknown
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition: if the value is null
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, "incompatible value");
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the materials producer property value was previously
		// set
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the materials producer has the expected property value
		assertEquals(propertyValue, scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setMaterialsProducerResourceLevel(gcm.scenario.MaterialsProducerId, ResourceId, long)}
	 */
	@Test
	public void testSetMaterialsProducerResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		ResourceId resourceId = TestResourceId.RESOURCE3;
		long amount = 234;

		// precondition: if the materials producer id is null
		scenarioBuilder.setMaterialsProducerResourceLevel(null, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition: if the materials producer id is unknown
		scenarioBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition: if the resource id is null
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, null, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the amount is negative
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, -234L);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition: if the materials producer resource level was previously
		// set
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId).longValue());
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonCompartmentArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	public void testSetPersonCompartmentArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition: if the trackPersonCompartmentArrivalTimes is null
			scenarioBuilder.setPersonCompartmentArrivalTracking(null);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_COMPARTMENT_TRACKING_POLICY);

			// precondition: if the compartment arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario has the expected compartment tracking
			// arrival tracking policy
			assertEquals(timeTrackingPolicy, scenario.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonPropertyValue(PersonId, PersonPropertyId, Object)}
	 */
	@Test
	public void testSetPersonPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(68);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1;
		Object propertyValue = 38;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//
		RegionId regionId = TestRegionId.REGION_5;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;

		// precondition: if the person id is null
		scenarioBuilder.setPersonPropertyValue(null, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_ID);

		// precondition: if the person id is unknown
		scenarioBuilder.setPersonPropertyValue(new PersonId(444), personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_PERSON_ID);

		// precondition: if the person property id is null
		scenarioBuilder.setPersonPropertyValue(personId, null, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_PROPERTY_ID);

		// precondition: if the person property id is unknown
		scenarioBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.getUnknownPersonPropertyId(), propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, null);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, "incompatible value");
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the person property value was previously set
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the person have the expected property value
		assertEquals(propertyValue, scenario.getPersonPropertyValue(personId, personPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonRegionArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	public void testSetPersonRegionArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition: if the trackPersonRegionArrivalTimes is null
			scenarioBuilder.setPersonRegionArrivalTracking(null);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_TRACKING_POLICY);

			// precondition: if the region arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			Scenario scenario = scenarioBuilder.build();

			// postcondition: the scenario has the expected region arrival
			// tracking policy
			assertEquals(timeTrackingPolicy, scenario.getPersonRegionArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonResourceLevel(PersonId, ResourceId, long)}
	 */
	@Test
	public void testSetPersonResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(76);
		ResourceId resourceId = TestResourceId.RESOURCE2;
		long amount = 3453L;
		RegionId regionId = TestRegionId.REGION_4;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;

		// precondition: if the person id is null
		scenarioBuilder.setPersonResourceLevel(null, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_PERSON_ID);

		// precondition: if the person id is unknown
		scenarioBuilder.setPersonResourceLevel(new PersonId(88), resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_PERSON_ID);

		// precondition: if the resource id is null
		scenarioBuilder.setPersonResourceLevel(personId, null, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.setPersonResourceLevel(personId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the amount is negative
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, -321L);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition: if the person resource level was previously set
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the person has the expected resource level
		assertEquals(amount, scenario.getPersonResourceLevel(personId, resourceId).longValue());
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#setRegionMapOption(MapOption)}
	 */
	@Test
	public void testSetRegionMapOption() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		for (MapOption mapOption : MapOption.values()) {

			// precondition: if the mapOption is null
			scenarioBuilder.setRegionMapOption(null);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_MAP_OPTION);

			// precondition: if the mapOption was previously set
			scenarioBuilder.setRegionMapOption(mapOption);
			scenarioBuilder.setRegionMapOption(mapOption);
			assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			scenarioBuilder.setRegionMapOption(mapOption);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario has the expected region map option
			assertEquals(mapOption, scenario.getRegionMapOption());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setRegionPropertyValue(RegionId, RegionPropertyId, Object)}
	 */
	@Test
	public void testSetRegionPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		RegionId regionId = TestRegionId.REGION_3;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_7;
		Object propertyValue = 88;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(789)//
																	.build();//

		// precondition: if the region id is null
		scenarioBuilder.setRegionPropertyValue(null, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_ID);

		// precondition: if the region id is unknown
		scenarioBuilder.setRegionPropertyValue(TestRegionId.getUnknownRegionId(), regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_REGION_ID);

		// precondition: if the region property id is null
		scenarioBuilder.setRegionPropertyValue(regionId, null, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_PROPERTY_ID);

		// precondition: if the region property id is unknown
		scenarioBuilder.setRegionPropertyValue(regionId, TestRegionPropertyId.getUnknownRegionPropertyId(), propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_REGION_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, null);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, "incompatible value");
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the region property value was previously set
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the region has the expected property value
		assertEquals(propertyValue, scenario.getRegionPropertyValue(regionId, regionPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setRegionResourceLevel(RegionId, ResourceId, long)}
	 */
	@Test
	public void testSetRegionResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionId regionId = TestRegionId.REGION_6;
		ResourceId resourceId = TestResourceId.RESOURCE4;
		long amount = 345;

		// precondition: if the region id is null
		scenarioBuilder.setRegionResourceLevel(null, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_REGION_ID);

		// precondition: if the region id is unknown
		scenarioBuilder.setRegionResourceLevel(TestRegionId.getUnknownRegionId(), resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_REGION_ID);

		// precondition: if the resource id is null
		scenarioBuilder.setRegionResourceLevel(regionId, null, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.setRegionResourceLevel(regionId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the amount is negative
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, -12312L);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition: if the region resource level was previously set
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the region has the expected resource amount
		assertEquals(amount, scenario.getRegionResourceLevel(regionId, resourceId).longValue());

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setResourcePropertyValue(ResourceId, ResourcePropertyId, Object)}
	 */
	@Test
	public void testSetResourcePropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE8;
		ResourcePropertyId resourcePropertyId = TestResourceId.RESOURCE8.getResourcePropertyIds()[0];
		Object propertyValue = 534;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(12)//
				.build();//

		// precondition: if the resource id is null
		scenarioBuilder.setResourcePropertyValue(null, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the resource property id is null
		scenarioBuilder.setResourcePropertyValue(resourceId, null, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID);

		// precondition: if the resource property id is unknown
		scenarioBuilder.setResourcePropertyValue(resourceId, TestResourceId.getUnknownResourcePropertyId(), propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID);

		// precondition: if the value is null
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, null);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_PROPERTY_VALUE);

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, "incompatible value");
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition: if the resource property value was previously set
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the resource has the expected resource property value
		assertEquals(propertyValue, scenario.getResourcePropertyValue(resourceId, resourcePropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setResourceTimeTracking(ResourceId, TimeTrackingPolicy)}
	 */
	@Test
	public void testSetResourceTimeTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE5;
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;

		// precondition: if the resource id is null
		scenarioBuilder.setResourceTimeTracking(null, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource id is unknown
		scenarioBuilder.setResourceTimeTracking(TestResourceId.getUnknownResourceId(), timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition: if the trackValueAssignmentTimes is null
		scenarioBuilder.setResourceTimeTracking(resourceId, null);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_RESOURCE_TRACKING_POLICY);

		// precondition: if the resource TimeTrackingPolicy was previously set
		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the resource has the expected time tracking policy
		// value
		assertEquals(timeTrackingPolicy, scenario.getPersonResourceTimeTrackingPolicy(resourceId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#setScenarioId(ScenarioId)}
	 */
	@Test
	public void testSetScenarioId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ScenarioId scenarioId = new ScenarioId(67);

		// precondition: if the scenario id is null
		scenarioBuilder.setScenarioId(null);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NULL_SCENARIO_ID);

		// precondition: if the scenario id is negative
		scenarioBuilder.setScenarioId(new ScenarioId(-5));
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.NON_POSITIVE_SCENARIO_ID);

		// precondition: if the scenario id was previously set
		scenarioBuilder.setScenarioId(scenarioId);
		scenarioBuilder.setScenarioId(scenarioId);
		assertScenarioException(() -> scenarioBuilder.build(), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		scenarioBuilder.setScenarioId(scenarioId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario has the expected scenario id
		assertEquals(scenarioId, scenario.getScenarioId());

	}

}
