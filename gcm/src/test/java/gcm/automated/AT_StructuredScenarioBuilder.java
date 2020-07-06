package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertScenarioException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import gcm.automated.AT_Simulation_SetScenario.EmptyComponent;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestGlobalPropertyId;
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestMaterialId;
import gcm.automated.support.TestMaterialsProducerId;
import gcm.automated.support.TestMaterialsProducerPropertyId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestRegionPropertyId;
import gcm.automated.support.TestResourceId;
import gcm.components.AbstractComponent;
import gcm.manual.demo.identifiers.RandomGeneratorId;
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
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RandomNumberGeneratorId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.ScenarioException.ScenarioErrorType;
import gcm.scenario.ScenarioId;
import gcm.scenario.StageId;
import gcm.scenario.StructuredScenarioBuilder;
import gcm.scenario.TimeTrackingPolicy;
import gcm.simulation.Environment;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link StructuredScenarioBuilder}
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StructuredScenarioBuilder.class)
public class AT_StructuredScenarioBuilder {

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
	 * Tests {@link StructuredScenarioBuilder#StructuredScenarioBuilder()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		StructuredScenarioBuilder structuredScenarioBuilder = new StructuredScenarioBuilder();
		assertNotNull(structuredScenarioBuilder);				
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addBatch(BatchId, MaterialId, double, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addBatch", args = { BatchId.class, MaterialId.class, double.class, MaterialsProducerId.class })
	public void testAddBatch() {

		// identifiers and values
		Double amount = 4.0;
		BatchId batchId1 = new BatchId(1);
		BatchId batchId2 = new BatchId(2);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if the batch id is null
		scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.addBatch(null, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1), ScenarioErrorType.NULL_BATCH_ID);

		// precondition : ModelException thrown if the material does not exist
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, null, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1), ScenarioErrorType.NULL_MATERIAL_ID);
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.getUnknownMaterialId(), amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1),
				ScenarioErrorType.UNKNOWN_MATERIAL_ID);

		// precondition : ModelException thrown if the amount is negative
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, -1.0, TestMaterialsProducerId.MATERIALS_PRODUCER_1), ScenarioErrorType.NEGATIVE_MATERIAL_AMOUNT);

		// precondition : ModelException thrown if the amount is not finite
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, Double.NEGATIVE_INFINITY, TestMaterialsProducerId.MATERIALS_PRODUCER_1),
				ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, Double.POSITIVE_INFINITY, TestMaterialsProducerId.MATERIALS_PRODUCER_1),
				ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, Double.NaN, TestMaterialsProducerId.MATERIALS_PRODUCER_1),
				ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT);

		// precondition : ModelException thrown if the materials producer does
		// not exist
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, amount, null), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.getUnknownMaterialsProducerId()),
				ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition : ModelException thrown if the batch id was previously
		// added
		scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		assertScenarioException(() -> scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1),
				ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : the batch has the expected amount
		scenarioBuilder.addBatch(batchId2, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getBatchAmount(batchId2));

		// postcondition : the batch has the expected material
		assertEquals(TestMaterialId.MATERIAL_1, scenario.getBatchMaterial(batchId2));

		// postcondition : the batch is owned by the correct materials producer
		assertEquals(TestMaterialsProducerId.MATERIALS_PRODUCER_1, scenario.getBatchMaterialsProducer(batchId2));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addBatchToStage(StageId, BatchId)}
	 */

	@Test
	@UnitTestMethod(name = "addBatchToStage", args = { StageId.class, BatchId.class })
	public void testAddBatchToStage() {
		// identifiers and values
		BatchId batchId = new BatchId(5);
		StageId stageId1 = new StageId(7);
		StageId stageId2 = new StageId(8);
		StageId stageId3 = new StageId(9);
		Double amount = 4.0;

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if the stage does not exist
		scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		scenarioBuilder.addStage(stageId1, false, TestMaterialsProducerId.MATERIALS_PRODUCER_1);

		assertScenarioException(() -> scenarioBuilder.addBatchToStage(null, batchId), ScenarioErrorType.NULL_STAGE_ID);
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(new StageId(55), batchId), ScenarioErrorType.UNKNOWN_STAGE_ID);

		// precondition : ModelException thrown if the batch does not exist
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(stageId1, null), ScenarioErrorType.NULL_BATCH_ID);
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(stageId1, new BatchId(99)), ScenarioErrorType.UNKNOWN_BATCH_ID);

		// precondition : ModelException thrown if the stage and batch are not
		// associated with the same
		// materials producer
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2, PlaceholderComponent.class);
		scenarioBuilder.addStage(stageId2, false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(stageId2, batchId), ScenarioErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER);

		// precondition : ModelException thrown if the batch is already
		// associated with any stage
		scenarioBuilder.addBatchToStage(stageId1, batchId);
		scenarioBuilder.addStage(stageId3, false, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(stageId3, batchId), ScenarioErrorType.BATCH_ALREADY_STAGED);
		assertScenarioException(() -> scenarioBuilder.addBatchToStage(stageId1, batchId), ScenarioErrorType.BATCH_ALREADY_STAGED);

		// postcondition : the batch is on the stage
		Scenario scenario = scenarioBuilder.build();
		Set<BatchId> stageBatches = scenario.getStageBatches(stageId1);
		assertTrue(stageBatches.contains(batchId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addCompartmentId(CompartmentId, Class)}
	 */

	@Test
	@UnitTestMethod(name = "addCompartmentId", args = { CompartmentId.class, Class.class })
	public void testAddCompartmentId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : compartment id cannot be null
		assertScenarioException(() -> scenarioBuilder.addCompartmentId(null, PlaceholderComponent.class), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition : comparmentComponentClass cannot be null
		assertScenarioException(() -> scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, null), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, PlaceholderComponent.class);

		// precondition : compartment id cannot duplicate previous compartment
		assertScenarioException(() -> scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, PlaceholderComponent.class), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : scenario contains only the compartment added
		Scenario scenario = scenarioBuilder.build();
		Set<CompartmentId> compartmentIds = scenario.getCompartmentIds();
		assertTrue(compartmentIds.size() == 1);
		assertTrue(compartmentIds.contains(TestCompartmentId.COMPARTMENT_1));
		assertEquals(PlaceholderComponent.class, scenario.getCompartmentComponentClass(TestCompartmentId.COMPARTMENT_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addGlobalComponentId(GlobalComponentId, Class)}
	 */

	@Test
	@UnitTestMethod(name = "addGlobalComponentId", args = { GlobalComponentId.class, Class.class })
	public void testAddGlobalComponentId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : global component id cannot be null
		assertScenarioException(() -> scenarioBuilder.addGlobalComponentId(null, PlaceholderComponent.class), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition : globalComponentClass cannot be null
		assertScenarioException(() -> scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, null), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, PlaceholderComponent.class);

		// precondition : global component id cannot duplicate previous global
		// component id
		assertScenarioException(() -> scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, PlaceholderComponent.class), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : scenario contains only the global component id added
		Scenario scenario = scenarioBuilder.build();
		Set<GlobalComponentId> globalComponentIds = scenario.getGlobalComponentIds();
		assertTrue(globalComponentIds.size() == 1);
		assertTrue(globalComponentIds.contains(TestGlobalComponentId.GLOBAL_COMPONENT_1));
		assertEquals(PlaceholderComponent.class, scenario.getGlobalComponentClass(TestGlobalComponentId.GLOBAL_COMPONENT_1));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addGroup(GroupId, GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupId.class, GroupTypeId.class })
	public void testAddGroup() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		GroupId groupId = new GroupId(45);

		// precondition : if the group id is null
		assertScenarioException(() -> scenarioBuilder.addGroup(null, TestGroupTypeId.GROUP_TYPE_1), ScenarioErrorType.NULL_GROUP_ID);

		// precondition : the group type must have been previously added
		assertScenarioException(() -> scenarioBuilder.addGroup(groupId, null), ScenarioErrorType.NULL_GROUP_TYPE_ID);

		// precondition : the group type must have been previously added
		assertScenarioException(() -> scenarioBuilder.addGroup(groupId, TestGroupTypeId.GROUP_TYPE_1), ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID);

		scenarioBuilder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addGroup(groupId, TestGroupTypeId.GROUP_TYPE_1);

		// precondition : the group must not have been previously added
		assertScenarioException(() -> scenarioBuilder.addGroup(groupId, TestGroupTypeId.GROUP_TYPE_1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : the group exists and is of the correct type
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupIds().size() == 1);
		assertTrue(scenario.getGroupIds().contains(groupId));
		assertEquals(TestGroupTypeId.GROUP_TYPE_1, scenario.getGroupTypeId(groupId));

	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addGroupTypeId(GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroupTypeId", args = { GroupTypeId.class })
	public void testAddGroupTypeId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : the group type is not null
		assertScenarioException(() -> scenarioBuilder.addGroupTypeId(null), ScenarioErrorType.NULL_GROUP_TYPE_ID);

		scenarioBuilder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);

		// precondition : the group must not have been previously added
		assertScenarioException(() -> scenarioBuilder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : the group type exists
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupTypeIds().size() == 1);
		assertTrue(scenario.getGroupTypeIds().contains(TestGroupTypeId.GROUP_TYPE_1));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addMaterial(MaterialId)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterial", args = { MaterialId.class })
	public void testAddMaterial() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : the material id is not null
		assertScenarioException(() -> scenarioBuilder.addMaterial(null), ScenarioErrorType.NULL_MATERIAL_ID);

		scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1);

		// precondition : the material id was not previouslyAdded
		assertScenarioException(() -> scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition: the material id is the the single material in the
		// scenario
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getMaterialIds().size() == 1);
		assertTrue(scenario.getMaterialIds().contains(TestMaterialId.MATERIAL_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addMaterialsProducerId(MaterialsProducerId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterialsProducerId", args = { MaterialsProducerId.class, Class.class })
	public void testAddMaterialsProducerId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : materials producer id cannot be null
		assertScenarioException(() -> scenarioBuilder.addMaterialsProducerId(null, PlaceholderComponent.class), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition : materialsProducerComponentClass cannot be null
		assertScenarioException(() -> scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, null), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class);

		// precondition : materials producer id cannot duplicate previous
		// materials producer id
		assertScenarioException(() -> scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : scenario contains only the materials producer id
		// added
		Scenario scenario = scenarioBuilder.build();
		Set<MaterialsProducerId> materialsProducerIds = scenario.getMaterialsProducerIds();
		assertTrue(materialsProducerIds.size() == 1);
		assertTrue(materialsProducerIds.contains(TestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(PlaceholderComponent.class, scenario.getMaterialsProducerComponentClass(TestMaterialsProducerId.MATERIALS_PRODUCER_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addPerson(PersonId, RegionId, CompartmentId)}
	 */
	@Test
	@UnitTestMethod(name = "addPerson", args = { PersonId.class, RegionId.class, CompartmentId.class })
	public void testAddPerson() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		PersonId personId = new PersonId(45);
		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(TestRegionId.REGION_1, PlaceholderComponent.class);

		// precondition : if the person id is null
		assertScenarioException(() -> scenarioBuilder.addPerson(null, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1), ScenarioErrorType.NULL_PERSON_ID);
		// precondition : if the region id is null
		assertScenarioException(() -> scenarioBuilder.addPerson(personId, null, TestCompartmentId.COMPARTMENT_1), ScenarioErrorType.NULL_REGION_ID);
		// precondition : if the region id is unknown
		assertScenarioException(() -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_2, TestCompartmentId.COMPARTMENT_1), ScenarioErrorType.UNKNOWN_REGION_ID);
		// precondition : if the compartment id is null
		assertScenarioException(() -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, null), ScenarioErrorType.NULL_COMPARTMENT_ID);
		// precondition : if the compartment id is unknown
		assertScenarioException(() -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_2), ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);
		// precondition : if the person was previously added
		scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);
		assertScenarioException(() -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : the person is in the scenario and has the expected
		// region and compartment assignments
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getPeopleIds().size() == 1);
		assertTrue(scenario.getPeopleIds().contains(personId));
		assertEquals(TestCompartmentId.COMPARTMENT_1, scenario.getPersonCompartment(personId));
		assertEquals(TestRegionId.REGION_1, scenario.getPersonRegion(personId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addPersonToGroup(GroupId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {
		PersonId personId = new PersonId(1);
		GroupId groupId = new GroupId(2);

		PersonId placeHolderPersonId = new PersonId(3);
		GroupId placeHolderGroupId = new GroupId(4);

		GroupId unknownGroupId = new GroupId(5);
		PersonId unknownPersonId = new PersonId(6);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		// establish some place holders so that the precondition tests each have
		// exactly one expected failure point
		scenarioBuilder.addRegionId(TestRegionId.REGION_1, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, PlaceholderComponent.class);
		scenarioBuilder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addGroup(placeHolderGroupId, TestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addPerson(placeHolderPersonId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);

		// precondition: if the group id is null
		assertScenarioException(() -> scenarioBuilder.addPersonToGroup(null, placeHolderPersonId), ScenarioErrorType.NULL_GROUP_ID);

		// precondition: if the person id is null
		assertScenarioException(() -> scenarioBuilder.addPersonToGroup(placeHolderGroupId, null), ScenarioErrorType.NULL_PERSON_ID);

		// precondition: if the group id is unknown
		assertScenarioException(() -> scenarioBuilder.addPersonToGroup(unknownGroupId, placeHolderPersonId), ScenarioErrorType.UNKNOWN_GROUP_ID);

		// precondition: if the person id is unknown
		assertScenarioException(() -> scenarioBuilder.addPersonToGroup(placeHolderGroupId, unknownPersonId), ScenarioErrorType.UNKNOWN_PERSON_ID);

		scenarioBuilder.addGroup(groupId, TestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);
		scenarioBuilder.addPersonToGroup(groupId, personId);

		// precondition: if the person was previously added to the group
		assertScenarioException(() -> scenarioBuilder.addPersonToGroup(groupId, personId), ScenarioErrorType.DUPLICATE_GROUP_MEMBERSHIP);

		// postcondition : the person is a member of the group
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupMembers(groupId).contains(personId));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addRegionId(RegionId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addRegionId", args = { RegionId.class, Class.class })
	public void testAddRegionId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : region id cannot be null
		assertScenarioException(() -> scenarioBuilder.addRegionId(null, PlaceholderComponent.class), ScenarioErrorType.NULL_COMPONENT_IDENTIFIER);

		// precondition : regionComponentClass cannot be null
		assertScenarioException(() -> scenarioBuilder.addRegionId(TestRegionId.REGION_1, null), ScenarioErrorType.NULL_COMPONENT_CLASS);

		scenarioBuilder.addRegionId(TestRegionId.REGION_1, PlaceholderComponent.class);

		// precondition : region id cannot duplicate previous global
		// component id
		assertScenarioException(() -> scenarioBuilder.addRegionId(TestRegionId.REGION_1, PlaceholderComponent.class), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : scenario contains only the region id added
		Scenario scenario = scenarioBuilder.build();
		Set<RegionId> regionIds = scenario.getRegionIds();
		assertTrue(regionIds.size() == 1);
		assertTrue(regionIds.contains(TestRegionId.REGION_1));
		assertEquals(PlaceholderComponent.class, scenario.getRegionComponentClass(TestRegionId.REGION_1));

	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addResource(ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "addResource", args = { ResourceId.class })
	public void testAddResource() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition: if the resource id is null
		assertScenarioException(() -> scenarioBuilder.addResource(null), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition: if the resource was previously added
		scenarioBuilder.addResource(TestResourceId.RESOURCE1);
		assertScenarioException(() -> scenarioBuilder.addResource(TestResourceId.RESOURCE1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		Scenario scenario = scenarioBuilder.build();
		Set<ResourceId> resourceIds = scenario.getResourceIds();
		assertTrue(resourceIds.size() == 1);
		assertTrue(resourceIds.contains(TestResourceId.RESOURCE1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addStage(StageId, boolean, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addStage", args = { StageId.class, boolean.class, MaterialsProducerId.class })
	public void testAddStage() {

		StageId stageId1 = new StageId(50);
		StageId stageId2 = new StageId(13);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if if the stage id is null
		scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class);
		assertScenarioException(() -> scenarioBuilder.addStage(null, true, TestMaterialsProducerId.MATERIALS_PRODUCER_1), ScenarioErrorType.NULL_STAGE_ID);
		// precondition : ModelException thrown if if the materials producer
		// does not exist
		assertScenarioException(() -> scenarioBuilder.addStage(stageId1, true, null), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);
		assertScenarioException(() -> scenarioBuilder.addStage(stageId1, true, TestMaterialsProducerId.getUnknownMaterialsProducerId()), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);

		// precondition : ModelException thrown if if the stage id was
		// previously added
		scenarioBuilder.addStage(stageId2, false, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		assertScenarioException(() -> scenarioBuilder.addStage(stageId2, true, TestMaterialsProducerId.MATERIALS_PRODUCER_1), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		// postcondition : the stage has the offered state
		scenarioBuilder.addStage(stageId1, true, TestMaterialsProducerId.MATERIALS_PRODUCER_1);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(true, scenario.isStageOffered(stageId1));
		assertEquals(false, scenario.isStageOffered(stageId2));

		// postcondition : the stage is owned by the correct materials producer
		assertEquals(TestMaterialsProducerId.MATERIALS_PRODUCER_1, scenario.getStageMaterialsProducer(stageId1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineBatchProperty(MaterialId, BatchPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineBatchProperty", args = { MaterialId.class, BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		TestMaterialId testMaterialId = TestMaterialId.MATERIAL_1;
		scenarioBuilder.addMaterial(testMaterialId);
		BatchPropertyId batchPropertyId = testMaterialId.getBatchPropertyIds()[0];
		String defaultValue = "Default";
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue(defaultValue)//
																	.build();//

		// precondition : if the material id is null
		assertScenarioException(() -> scenarioBuilder.defineBatchProperty(null, batchPropertyId, propertyDefinition), ScenarioErrorType.NULL_MATERIAL_ID);
		// precondition : if the material id is unknown
		assertScenarioException(() -> scenarioBuilder.defineBatchProperty(TestMaterialId.getUnknownMaterialId(), batchPropertyId, propertyDefinition), ScenarioErrorType.UNKNOWN_MATERIAL_ID);
		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineBatchProperty(testMaterialId, null, propertyDefinition), ScenarioErrorType.NULL_BATCH_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, null), ScenarioErrorType.NULL_BATCH_PROPERTY_DEFINITION);
		// precondition : if the batch property was previously defined
		scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition), ScenarioErrorType.DUPLICATE_BATCH_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getBatchPropertyDefinition(testMaterialId, batchPropertyId);
		assertEquals(propertyDefinition, outputPropertyDefinition);

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineCompartmentProperty(CompartmentId, CompartmentPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineCompartmentProperty", args = { CompartmentId.class, CompartmentPropertyId.class, PropertyDefinition.class })
	public void testDefineCompartmentProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		String defaultValue = "Default";
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue(defaultValue)//
																		.build();//
		scenarioBuilder.addCompartmentId(compartmentId, EmptyComponent.class);

		// precondition : if the compartment id is unknown
		assertScenarioException(() -> scenarioBuilder.defineCompartmentProperty(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, inputPropertyDefinition),
				ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);

		// precondition : if the compartment id is null
		assertScenarioException(() -> scenarioBuilder.defineCompartmentProperty(null, compartmentPropertyId, inputPropertyDefinition), ScenarioErrorType.NULL_COMPARTMENT_ID);

		// precondition : if the property id is null

		assertScenarioException(() -> scenarioBuilder.defineCompartmentProperty(compartmentId, null, inputPropertyDefinition), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID);

		// precondition : if the property definition is null

		assertScenarioException(() -> scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, null), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_DEFINITION);

		// precondition : if the compartment property was previously defined

		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, inputPropertyDefinition),
				ScenarioErrorType.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineGlobalProperty(GlobalPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		String defaultValue = "Default";
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue(defaultValue)//
																		.build();//

		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineGlobalProperty(null, inputPropertyDefinition), ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID);

		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineGlobalProperty(globalPropertyId, null), ScenarioErrorType.NULL_GLOBAL_PROPERTY_DEFINITION);

		// precondition : if the compartment property was previously defined
		scenarioBuilder.defineGlobalProperty(globalPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineGlobalProperty(globalPropertyId, inputPropertyDefinition), ScenarioErrorType.DUPLICATE_GLOBAL_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineGroupProperty(GroupTypeId, GroupPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGroupProperty", args = { GroupTypeId.class, GroupPropertyId.class, PropertyDefinition.class })
	public void testDefineGroupProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the group type id is not defined
		assertScenarioException(() -> scenarioBuilder.defineGroupProperty(null, groupPropertyId, inputPropertyDefinition), ScenarioErrorType.NULL_GROUP_TYPE_ID);
		assertScenarioException(() -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition), ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineGroupProperty(groupTypeId, null, inputPropertyDefinition), ScenarioErrorType.NULL_GROUP_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, null), ScenarioErrorType.NULL_GROUP_PROPERTY_DEFINITION);
		// precondition : if the group property was previously defined
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition), ScenarioErrorType.DUPLICATE_GROUP_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineMaterialsProducerProperty(MaterialsProducerPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1;

		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineMaterialsProducerProperty(null, inputPropertyDefinition), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, null), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION);
		// precondition : if the materials producer property was previously
		// defined
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, inputPropertyDefinition),
				ScenarioErrorType.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#definePersonProperty(PersonPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.definePersonProperty(null, inputPropertyDefinition), ScenarioErrorType.NULL_PERSON_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.definePersonProperty(personPropertyId, null), ScenarioErrorType.NULL_PERSON_PROPERTY_DEFINITION);
		// precondition : if the person property was previously defined
		scenarioBuilder.definePersonProperty(personPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.definePersonProperty(personPropertyId, inputPropertyDefinition), ScenarioErrorType.DUPLICATE_PERSON_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineRegionProperty(RegionPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineRegionProperty", args = { RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineRegionProperty(null, inputPropertyDefinition), ScenarioErrorType.NULL_REGION_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineRegionProperty(regionPropertyId, null), ScenarioErrorType.NULL_REGION_PROPERTY_DEFINITION);
		// precondition : if the region property was previously defined
		scenarioBuilder.defineRegionProperty(regionPropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineRegionProperty(regionPropertyId, inputPropertyDefinition), ScenarioErrorType.DUPLICATE_REGION_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineResourceProperty(ResourceId, ResourcePropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineResourceProperty", args = { ResourceId.class, ResourcePropertyId.class, PropertyDefinition.class })
	public void testDefineResourceProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE5;
		ResourcePropertyId resourcePropertyId = TestResourceId.RESOURCE5.getResourcePropertyIds()[0];
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		scenarioBuilder.addResource(resourceId);

		// precondition : if the resource id is null
		assertScenarioException(() -> scenarioBuilder.defineResourceProperty(null, resourcePropertyId, inputPropertyDefinition), ScenarioErrorType.NULL_RESOURCE_ID);
		// precondition : if the resource id is unknown
		assertScenarioException(() -> scenarioBuilder.defineResourceProperty(TestResourceId.getUnknownResourceId(), resourcePropertyId, inputPropertyDefinition),
				ScenarioErrorType.UNKNOWN_RESOURCE_ID);
		// precondition : if the property id is null
		assertScenarioException(() -> scenarioBuilder.defineResourceProperty(resourceId, null, inputPropertyDefinition), ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID);
		// precondition : if the property definition is null
		assertScenarioException(() -> scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, null), ScenarioErrorType.NULL_RESOURCE_PROPERTY_DEFINITION);
		// precondition : if the resource property was previously defined
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, inputPropertyDefinition);
		assertScenarioException(() -> scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, inputPropertyDefinition), ScenarioErrorType.DUPLICATE_RESOURCE_PROPERTY_DEFINITION);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setBatchPropertyValue(BatchId, BatchPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {
		BatchId batchId1 = new BatchId(1);
		BatchId batchId2 = new BatchId(2);
		Double propertyValue1 = 4.7;
		Integer propertyValue2 = 5;
		Double amount = 5.6;

		BatchPropertyId batchPropertyId1 = TestMaterialId.MATERIAL_1.getBatchPropertyIds()[0];
		BatchPropertyId batchPropertyId2 = TestMaterialId.MATERIAL_1.getBatchPropertyIds()[1];

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		scenarioBuilder.addMaterial(TestMaterialId.MATERIAL_1);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(17.5)//
																	.build();//
		scenarioBuilder.defineBatchProperty(TestMaterialId.MATERIAL_1, batchPropertyId1, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1, PlaceholderComponent.class);
		scenarioBuilder.addBatch(batchId1, TestMaterialId.MATERIAL_1, amount, TestMaterialsProducerId.MATERIALS_PRODUCER_1);

		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(null, batchPropertyId1, propertyValue1), ScenarioErrorType.NULL_BATCH_ID);

		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId2, batchPropertyId1, propertyValue1), ScenarioErrorType.UNKNOWN_BATCH_ID);

		// precondition : ModelException thrown if the batch property is not
		// defined
		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId1, null, propertyValue1), ScenarioErrorType.NULL_BATCH_PROPERTY_ID);

		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId2, propertyValue1), ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID);

		// precondition : ModelException thrown if the value is not compatible
		// with the property definition
		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue2), ScenarioErrorType.INCOMPATIBLE_VALUE);

		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, null), ScenarioErrorType.NULL_BATCH_PROPERTY_VALUE);

		// precondition : ModelException thrown if the batch property value was
		// previously set
		scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue1);
		assertScenarioException(() -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue1), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition : the batch has the expected property value
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue1, scenario.getBatchPropertyValue(batchId1, batchPropertyId1));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setCompartmentMapOption(MapOption)}
	 */
	@Test
	@UnitTestMethod(name = "setCompartmentMapOption", args = { MapOption.class })
	public void testSetCompartmentMapOption() {
		for (MapOption mapOption : MapOption.values()) {
			ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

			// precondition :if the mapOption is null
			assertScenarioException(() -> scenarioBuilder.setCompartmentMapOption(null), ScenarioErrorType.NULL_COMPARTMENT_MAP_OPTION);

			// precondition :if the mapOption was previously set
			scenarioBuilder.setCompartmentMapOption(mapOption);
			assertScenarioException(() -> scenarioBuilder.setCompartmentMapOption(mapOption), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(mapOption, scenario.getCompartmentMapOption());
		}

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setCompartmentPropertyValue(CompartmentId, CompartmentPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testSetCompartmentPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);

		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);

		// precondition : if the compartment property id is null
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, null, propertyValue), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID);

		// precondition : if the compartment property id is unknown
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, TestCompartmentId.getUnknownCompartmentPropertyId(), propertyValue),
				ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID);

		// precondition : if the compartment id is null
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(null, compartmentPropertyId, propertyValue), ScenarioErrorType.NULL_COMPARTMENT_ID);

		// precondition : if the compartment id is unknown
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, propertyValue),
				ScenarioErrorType.UNKNOWN_COMPARTMENT_ID);

		// precondition : if the value is null
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, null), ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the compartment property value was previously set
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setGlobalPropertyValue(GlobalPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//

		// precondition : if the global property is not a defined
		assertScenarioException(() -> scenarioBuilder.setGlobalPropertyValue(null, propertyValue), ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID);
		assertScenarioException(() -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);

		// precondition : if the value is null
		assertScenarioException(() -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, null), ScenarioErrorType.NULL_GLOBAL_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the global property value was previously set
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getGlobalPropertyValue(globalPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setGroupPropertyValue(GroupId, GroupPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		GroupId groupId = new GroupId(7);
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition : if the group id is null
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(null, groupPropertyId, propertyValue), ScenarioErrorType.NULL_GROUP_ID);

		// precondition : if the group id is unknown
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_GROUP_ID);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addGroup(groupId, groupTypeId);

		// precondition : if the group property id is null
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, null, propertyValue), ScenarioErrorType.NULL_GROUP_PROPERTY_ID);

		// precondition : if the group property id is unknown
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);

		// precondition : if the value is null
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, null), ScenarioErrorType.NULL_GROUP_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the group property value was previously set
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getGroupPropertyValue(groupId, groupPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setMaterialsProducerPropertyValue(MaterialsProducerId, MaterialsProducerPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2;
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);

		// precondition : if the materials producer id is null
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition : if the materials producer id is unknown
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue),
				ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);

		// precondition : if the materials property id is null
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue), ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		// precondition : if the materials property id is unknown
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue),
				ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID);

		// precondition: if the value is null
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null),
				ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the materials producer property value was
		// previously set
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setMaterialsProducerResourceLevel(MaterialsProducerId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
	public void testSetMaterialsProducerResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		Long amount = 2578L;
		ResourceId resourceId = TestResourceId.RESOURCE1;

		// precondition : if the materials producer id null
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(null, resourceId, amount), ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);

		// precondition : if the materials producer id is unknown
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount), ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, PlaceholderComponent.class);

		// precondition : if the resource does not exist
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, null, amount), ScenarioErrorType.NULL_RESOURCE_ID);
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount), ScenarioErrorType.UNKNOWN_RESOURCE_ID);
		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, -1L), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition : if the materials producer resource level was
		// previously set
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		assertScenarioException(() -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonCompartmentArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonCompartmentArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonCompartmentArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		for (TimeTrackingPolicy trackPersonCompartmentArrivalTimes : TimeTrackingPolicy.values()) {

			// precondition : if the trackPersonCompartmentArrivalTimes is null
			assertScenarioException(() -> scenarioBuilder.setPersonCompartmentArrivalTracking(null), ScenarioErrorType.NULL_COMPARTMENT_TRACKING_POLICY);

			// precondition : if the compartment arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonCompartmentArrivalTracking(trackPersonCompartmentArrivalTimes);
			assertScenarioException(() -> scenarioBuilder.setPersonCompartmentArrivalTracking(trackPersonCompartmentArrivalTimes), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(trackPersonCompartmentArrivalTimes, scenario.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonPropertyValue(PersonId, PersonPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		PersonId personId = new PersonId(25);

		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1;
		Object propertyValue = 17;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//

		// precondition : if the person does not exist
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(null, personPropertyId, propertyValue), ScenarioErrorType.NULL_PERSON_ID);

		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_PERSON_ID);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);

		// precondition : if the person property id is null
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, null, propertyValue), ScenarioErrorType.NULL_PERSON_PROPERTY_ID);

		// precondition : if the person property id is unknown
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);

		// precondition : if the value is null
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, null), ScenarioErrorType.NULL_PERSON_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the person property value was previously set
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getPersonPropertyValue(personId, personPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonRegionArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonRegionArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonRegionArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		for (TimeTrackingPolicy trackPersonCompartmentArrivalTimes : TimeTrackingPolicy.values()) {

			// precondition : if the trackPersonRegionArrivalTimes is null
			assertScenarioException(() -> scenarioBuilder.setPersonRegionArrivalTracking(null), ScenarioErrorType.NULL_REGION_TRACKING_POLICY);

			// precondition : if the region arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonRegionArrivalTracking(trackPersonCompartmentArrivalTimes);
			assertScenarioException(() -> scenarioBuilder.setPersonRegionArrivalTracking(trackPersonCompartmentArrivalTimes), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(trackPersonCompartmentArrivalTimes, scenario.getPersonRegionArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonResourceLevel(PersonId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonResourceLevel", args = { PersonId.class, ResourceId.class, long.class })
	public void testSetPersonResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		Long amount = 2578L;
		ResourceId resourceId = TestResourceId.RESOURCE1;
		PersonId personId = new PersonId(534);
		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition : if the person id is null
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(null, resourceId, amount), ScenarioErrorType.NULL_PERSON_ID);

		// precondition : if the person id is unknown
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount), ScenarioErrorType.UNKNOWN_PERSON_ID);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);
		scenarioBuilder.addCompartmentId(compartmentId, PlaceholderComponent.class);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);

		// precondition : if the resource id is null
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(personId, null, amount), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition : if the resource id is unknown
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount), ScenarioErrorType.UNKNOWN_RESOURCE_ID);
		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, -1L), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition : if the person resource level was previously set
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		assertScenarioException(() -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getPersonResourceLevel(personId, resourceId));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#setRegionMapOption(MapOption)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionMapOption", args = { MapOption.class })
	public void testSetRegionMapOption() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		for (MapOption mapOption : MapOption.values()) {
			// precondition : if the mapOption is null
			assertScenarioException(() -> scenarioBuilder.setRegionMapOption(null), ScenarioErrorType.NULL_REGION_MAP_OPTION);
			// precondition : if the mapOption was previously set
			scenarioBuilder.setRegionMapOption(mapOption);
			assertScenarioException(() -> scenarioBuilder.setRegionMapOption(mapOption), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(mapOption, scenario.getRegionMapOption());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setRegionPropertyValue(RegionId, RegionPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		RegionId regionId = TestRegionId.REGION_1;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object propertyValue = 17;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//

		// precondition : if the region id is null
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(null, regionPropertyId, propertyValue), ScenarioErrorType.NULL_REGION_ID);

		// precondition : if the region id is unknown
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_REGION_ID);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);

		// precondition : if the region property id is null
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, null, propertyValue), ScenarioErrorType.NULL_REGION_PROPERTY_ID);

		// precondition : if the region property id is unknown
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue), ScenarioErrorType.UNKNOWN_REGION_PROPERTY_ID);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);

		// precondition : if the value is null
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, null), ScenarioErrorType.NULL_REGION_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the region property value was previously set
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getRegionPropertyValue(regionId, regionPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setRegionResourceLevel(RegionId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionResourceLevel", args = { RegionId.class, ResourceId.class, long.class })
	public void testSetRegionResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		Long amount = 2578L;
		ResourceId resourceId = TestResourceId.RESOURCE1;
		RegionId regionId = TestRegionId.REGION_1;

		// precondition : if the region does not exist
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(null, resourceId, amount), ScenarioErrorType.NULL_REGION_ID);
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount), ScenarioErrorType.UNKNOWN_REGION_ID);
		scenarioBuilder.addRegionId(regionId, PlaceholderComponent.class);

		// precondition : if the resource does not exist
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(regionId, null, amount), ScenarioErrorType.NULL_RESOURCE_ID);
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount), ScenarioErrorType.UNKNOWN_RESOURCE_ID);
		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, -1L), ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT);

		// precondition : if the region resource level was previously set
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		assertScenarioException(() -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getRegionResourceLevel(regionId, resourceId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setResourcePropertyValue(ResourceId, ResourcePropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		ResourceId resourceId = TestResourceId.RESOURCE1;
		ResourcePropertyId resourcePropertyId = TestResourceId.RESOURCE1.getResourcePropertyIds()[0];
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);

		// precondition : if the resource id is null
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(null, resourcePropertyId, propertyValue), ScenarioErrorType.NULL_RESOURCE_ID);

		// precondition : if the resource id is unknown
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId, propertyValue), ScenarioErrorType.UNKNOWN_RESOURCE_ID);

		// precondition : if the resource property is null
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(resourceId, null, propertyValue), ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID);

		// precondition : if the resource property is unknown
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(resourceId, TestResourceId.getUnknownResourcePropertyId(), propertyValue),
				ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID);

		// precondition: if the value is null
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, null), ScenarioErrorType.NULL_RESOURCE_PROPERTY_VALUE);

		// precondition : if the value is not compatible with the property
		// definition
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, "incompatible value"), ScenarioErrorType.INCOMPATIBLE_VALUE);

		// precondition : if the resource property value was previously set
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		assertScenarioException(() -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getResourcePropertyValue(resourceId, resourcePropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setResourceTimeTracking(ResourceId, TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setResourceTimeTracking", args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testSetResourceTimeTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE1;

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition : if the resource does not exist
			assertScenarioException(() -> scenarioBuilder.setResourceTimeTracking(null, timeTrackingPolicy), ScenarioErrorType.NULL_RESOURCE_ID);
			assertScenarioException(() -> scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy), ScenarioErrorType.UNKNOWN_RESOURCE_ID);
			scenarioBuilder.addResource(resourceId);

			// precondition : if the trackValueAssignmentTimes is null
			assertScenarioException(() -> scenarioBuilder.setResourceTimeTracking(resourceId, null), ScenarioErrorType.NULL_RESOURCE_TRACKING_POLICY);

			// precondition : if the resource TimeTrackingPolicy was previously
			// set
			scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
			assertScenarioException(() -> scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(timeTrackingPolicy, scenario.getPersonResourceTimeTrackingPolicy(resourceId));
		}
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#setScenarioId(ScenarioId)}
	 */
	@Test
	@UnitTestMethod(name = "setScenarioId", args = { ScenarioId.class })
	public void testSetScenarioId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		ScenarioId scenarioId = new ScenarioId(644);
		// precondition : if the scenario id is null or negative
		assertScenarioException(() -> scenarioBuilder.setScenarioId(null), ScenarioErrorType.NULL_SCENARIO_ID);
		assertScenarioException(() -> scenarioBuilder.setScenarioId(new ScenarioId(-1)), ScenarioErrorType.NON_POSITIVE_SCENARIO_ID);

		// precondition : if the scenario id was previously set
		scenarioBuilder.setScenarioId(scenarioId);
		assertScenarioException(() -> scenarioBuilder.setScenarioId(scenarioId), ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE);

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(scenarioId, scenario.getScenarioId());
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#build() }
	 */
	@Test
	@UnitTestMethod(name = "build", args = {})
	public void testBuild() {
		// No test performed: The build method is tested by proxy via the other
		// test methods.
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#setSuggestedPopulationSize(int)}
	 */
	@Test
	@UnitTestMethod(name = "setSuggestedPopulationSize", args = { int.class })
	public void testSetSuggestedPopulationSize() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition: if suggested population size is negative
		assertScenarioException(() -> scenarioBuilder.setSuggestedPopulationSize(-1), ScenarioErrorType.NEGATIVE_SUGGGESTED_POPULATION);

		for (int i = 0; i < 20; i++) {
			scenarioBuilder.setSuggestedPopulationSize(i);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario has the expected region map option
			assertEquals(i, scenario.getSuggestedPopulationSize());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addRandomNumberGeneratorId(RandomNumberGeneratorId)}
	 */
	@Test
	@UnitTestMethod(name = "addRandomNumberGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomNumberGeneratorId() {

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : if the generator id is null
		assertScenarioException(() -> scenarioBuilder.addRandomNumberGeneratorId(null), ScenarioErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID);

		// precondition : if the generator id was previously added
		scenarioBuilder.addRandomNumberGeneratorId(RandomGeneratorId.BLITZEN);
		assertScenarioException(() -> scenarioBuilder.addRandomNumberGeneratorId(RandomGeneratorId.BLITZEN), ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER);

		ScenarioBuilder scenarioBuilder2 = new StructuredScenarioBuilder();
		Set<RandomNumberGeneratorId> expected = new LinkedHashSet<>();
		expected.add(RandomGeneratorId.COMET);
		expected.add(RandomGeneratorId.CUPID);
		expected.add(RandomGeneratorId.DONNER);
		expected.add(RandomGeneratorId.BLITZEN);

		for (RandomNumberGeneratorId randomNumberGeneratorId : expected) {
			scenarioBuilder2.addRandomNumberGeneratorId(randomNumberGeneratorId);
		}

		Scenario scenario = scenarioBuilder2.build();

		// postcondition: the scenario contains the expected ids
		Set<RandomNumberGeneratorId> actual = scenario.getRandomNumberGeneratorIds();
		assertEquals(expected, actual);

	}
}
