package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gcm.automated.support.ExceptionAssertion;
import gcm.automated.support.ExceptionAssertion.ExceptionGenerator;
import gcm.automated.support.TestCompartmentId;
import gcm.automated.support.TestGlobalComponentId;
import gcm.automated.support.TestGlobalPropertyId;
import gcm.automated.support.TestGroupTypeId;
import gcm.automated.support.TestMaterialsProducerId;
import gcm.automated.support.TestMaterialsProducerPropertyId;
import gcm.automated.support.TestPersonPropertyId;
import gcm.automated.support.TestRegionId;
import gcm.automated.support.TestRegionPropertyId;
import gcm.automated.support.TestResourceId;
import gcm.components.AbstractComponent;
import gcm.output.reports.GroupInfo;
import gcm.replication.Replication;
import gcm.replication.ReplicationImpl;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
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
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioBuilder;
import gcm.scenario.StageId;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Environment;
import gcm.simulation.Plan;
import gcm.simulation.Simulation;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Unit tests for {@link AbstractComponent}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = AbstractComponent.class)
public class AT_AbstractComponent {

	public static class TestComponent extends AbstractComponent {

		@Override
		public void init(Environment environment) {

			TestAcceptance testAcceptance = environment.getGlobalPropertyValue(TEST_ACCEPTANCE_ID);

			Plan plan = new Plan() {
			};
			PersonId personId = new PersonId(0);
			RegionId regionId = TestRegionId.REGION_1;
			StageId stageId = new StageId(0);
			GroupId groupId = new GroupId(0);
			CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
			MaterialsProducerId materialsProducerId1 = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			MaterialsProducerId materialsProducerId2 = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE1;
			CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
			GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
			ResourcePropertyId resourcePropertyId = TestResourceId.RESOURCE2.getResourcePropertyIds()[0];
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_5;
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3;
			GroupInfo groupInfo = GroupInfo.builder()//
					.setGroupId(groupId)//
					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
					.build();//
			GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
			Object key = new Object();

			assertUnimplemented(() -> executePlan(environment, plan));
			assertUnimplemented(() -> observeCompartmentPersonArrival(environment, personId));
			assertUnimplemented(() -> observeCompartmentPersonDeparture(environment, compartmentId, personId));
			assertUnimplemented(
					() -> observeCompartmentPropertyChange(environment, compartmentId, compartmentPropertyId));
			assertUnimplemented(() -> observeGlobalPersonArrival(environment, personId));
			assertUnimplemented(() -> observeGlobalPersonDeparture(environment, personId));
			assertUnimplemented(() -> observeGlobalPropertyChange(environment, globalPropertyId));
			assertUnimplemented(() -> observeMaterialsProducerPropertyChange(environment, materialsProducerId1,
					materialsProducerPropertyId));
			assertUnimplemented(
					() -> observeMaterialsProducerResourceChange(environment, materialsProducerId1, resourceId));
			assertUnimplemented(() -> observePersonCompartmentChange(environment, personId));
			assertUnimplemented(() -> observePersonPropertyChange(environment, personId, personPropertyId));
			assertUnimplemented(() -> observePersonRegionChange(environment, personId));
			assertUnimplemented(() -> observePersonResourceChange(environment, personId, resourceId));
			assertUnimplemented(() -> observeRegionPersonArrival(environment, personId));
			assertUnimplemented(() -> observeRegionPersonDeparture(environment, regionId, personId));
			assertUnimplemented(() -> observeRegionPropertyChange(environment, regionId, regionPropertyId));
			assertUnimplemented(() -> observeRegionResourceChange(environment, regionId, resourceId));
			assertUnimplemented(() -> observeResourcePropertyChange(environment, resourceId, resourcePropertyId));
			assertUnimplemented(() -> observeStageOfferChange(environment, stageId));
			assertUnimplemented(
					() -> observeStageTransfer(environment, stageId, materialsProducerId1, materialsProducerId2));
			assertUnimplemented(() -> observeGroupConstruction(environment, groupId));
			assertUnimplemented(() -> observeGroupDestruction(environment, groupInfo));
			assertUnimplemented(() -> observeGroupPropertyChange(environment, groupId, groupPropertyId));
			assertUnimplemented(() -> observeGroupPersonArrival(environment, groupId, personId));
			assertUnimplemented(() -> observeGroupPersonDeparture(environment, groupId, personId));
			assertUnimplemented(() -> observePartitionPersonAddition(environment, key, personId));
			assertUnimplemented(() -> observePartitionPersonRemoval(environment, key, personId));

			testAcceptance.passed = true;
		}

		@Override
		public void close(Environment environment) {

		}

	}

	private static GlobalPropertyId TEST_ACCEPTANCE_ID = new GlobalPropertyId() {
	};

	private static class TestAcceptance {
		boolean passed;
	}

	private static void assertUnimplemented(ExceptionGenerator exceptionGenerator) {
		ExceptionAssertion.assertException(exceptionGenerator, RuntimeException.class);
	}

	private void testAbstractComponent() {
		/*
		 * Show that each abstract method of the AbstractComponent class will throw a
		 * RuntimeException if the overriding class does not implement the method.
		 */

		// Create an object that will store the result(pass/fail) for the test.
		// The test will be executed by a TestComponent that does not override
		// any of the abstract methods.
		TestAcceptance testAcceptance = new TestAcceptance();

		// Create a simulation containing the one component and the
		// TestAcceptance stored as a global property
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, TestComponent.class);
		scenarioBuilder.defineGlobalProperty(TEST_ACCEPTANCE_ID, //
				PropertyDefinition.builder()//
						.setType(TestAcceptance.class)//
						.setDefaultValue(testAcceptance)//
						.setPropertyValueMutability(false)//
						.build());

		Scenario scenario = scenarioBuilder.build();

		Replication replication = new ReplicationImpl(new ReplicationId(234423), 645765856766L);

		Simulation simulation = new Simulation();
		simulation.setReplication(replication);
		simulation.setScenario(scenario);
		simulation.execute();

		// show that the TestAcceptance had passed set to true
		assertTrue(testAcceptance.passed);
	}

	/**
	 * Tests {@link AbstractComponent#AbstractComponent()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		AbstractComponent abstractComponent = new AbstractComponent() {

			@Override
			public void init(Environment environment) {

			}
		};

		assertNotNull(abstractComponent);

	}

	/**
	 * Tests {@link AbstractComponent#close(Environment)}
	 */
	@Test
	@UnitTestMethod(name = "close", args = { Environment.class })
	public void testClose() {
		// see testAbstractComponent
	}

	/**
	 * Tests {@link AbstractComponent#executePlan(Environment, Plan)}
	 */
	@Test
	@UnitTestMethod(name = "executePlan", args = { Environment.class, Plan.class })
	public void testExecutePlan() {
		testAbstractComponent();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeCompartmentPersonArrival(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeCompartmentPersonArrival", args = { Environment.class, PersonId.class })
	public void testObserveCompartmentPersonArrival() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeCompartmentPersonDeparture(Environment, CompartmentId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeCompartmentPersonDeparture", args = { Environment.class, CompartmentId.class,
			PersonId.class })
	public void testObserveCompartmentPersonDeparture() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeCompartmentPropertyChange(Environment, CompartmentId, CompartmentPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeCompartmentPropertyChange", args = { Environment.class, CompartmentId.class,
			CompartmentPropertyId.class })
	public void testObserveCompartmentPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGlobalPersonArrival(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGlobalPersonArrival", args = { Environment.class, PersonId.class })
	public void testObserveGlobalPersonArrival() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGlobalPersonDeparture(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGlobalPersonDeparture", args = { Environment.class, PersonId.class })
	public void testObserveGlobalPersonDeparture() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGlobalPropertyChange(Environment, GlobalPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGlobalPropertyChange", args = { Environment.class, GlobalPropertyId.class })
	public void testObserveGlobalPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGroupConstruction(Environment, GroupId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGroupConstruction", args = { Environment.class, GroupId.class })
	public void testObserveGroupConstruction() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGroupDestruction(Environment, GroupInfo)}
	 */
	@Test
	@UnitTestMethod(name = "observeGroupDestruction", args = { Environment.class, GroupInfo.class })
	public void testObserveGroupDestruction() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGroupPersonArrival(Environment, GroupId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGroupPersonArrival", args = { Environment.class, GroupId.class, PersonId.class })
	public void testObserveGroupPersonArrival() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGroupPersonDeparture(Environment, GroupId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGroupPersonDeparture", args = { Environment.class, GroupId.class, PersonId.class })
	public void testObserveGroupPersonDeparture() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeGroupPropertyChange(Environment, GroupId, GroupPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeGroupPropertyChange", args = { Environment.class, GroupId.class,
			GroupPropertyId.class })
	public void testObserveGroupPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeMaterialsProducerPropertyChange(Environment, MaterialsProducerId, MaterialsProducerPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeMaterialsProducerPropertyChange", args = { Environment.class,
			MaterialsProducerId.class, MaterialsProducerPropertyId.class })
	public void testObserveMaterialsProducerPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeMaterialsProducerResourceChange(Environment, MaterialsProducerId, ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "observeMaterialsProducerResourceChange", args = { Environment.class,
			MaterialsProducerId.class, ResourceId.class })
	public void testObserveMaterialsProducerResourceChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePersonCompartmentChange(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observePersonCompartmentChange", args = { Environment.class, PersonId.class })
	public void testObservePersonCompartmentChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePersonPropertyChange(Environment, PersonId, PersonPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observePersonPropertyChange", args = { Environment.class, PersonId.class,
			PersonPropertyId.class })
	public void testObservePersonPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePersonRegionChange(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observePersonRegionChange", args = { Environment.class, PersonId.class })
	public void testObservePersonRegionChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePersonResourceChange(Environment, PersonId, ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "observePersonResourceChange", args = { Environment.class, PersonId.class,
			ResourceId.class })
	public void testObservePersonResourceChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePartitionPersonAddition(Environment, Object, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observePartitionPersonAddition", args = { Environment.class, Object.class, PersonId.class })
	public void testObservePartitionPersonAddition() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observePartitionPersonRemoval(Environment, Object, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observePartitionPersonRemoval", args = { Environment.class, Object.class, PersonId.class })
	public void testObservePartitionPersonRemoval() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeRegionPersonArrival(Environment, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeRegionPersonArrival", args = { Environment.class, PersonId.class })
	public void testObserveRegionPersonArrival() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeRegionPersonDeparture(Environment, RegionId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "observeRegionPersonDeparture", args = { Environment.class, RegionId.class, PersonId.class })
	public void testObserveRegionPersonDeparture() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeRegionPropertyChange(Environment, RegionId, RegionPropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeRegionPropertyChange", args = { Environment.class, RegionId.class,
			RegionPropertyId.class })
	public void testObserveRegionPropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeRegionResourceChange(Environment, RegionId, ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "observeRegionResourceChange", args = { Environment.class, RegionId.class,
			ResourceId.class })
	public void testObserveRegionResourceChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeResourcePropertyChange(Environment, ResourceId, ResourcePropertyId)}
	 */
	@Test
	@UnitTestMethod(name = "observeResourcePropertyChange", args = { Environment.class, ResourceId.class,
			ResourcePropertyId.class })
	public void testObserveResourcePropertyChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests {@link AbstractComponent#observeStageOfferChange(Environment, StageId)}
	 */
	@Test
	@UnitTestMethod(name = "observeStageOfferChange", args = { Environment.class, StageId.class })
	public void testObserveStageOfferChange() {
		// covered by testExecutePlan();
	}

	/**
	 * Tests
	 * {@link AbstractComponent#observeStageTransfer(Environment, StageId, MaterialsProducerId, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "observeStageTransfer", args = { Environment.class, StageId.class, MaterialsProducerId.class,
			MaterialsProducerId.class })
	public void testObserveStageTransfer() {
		// covered by testExecutePlan();
	}

}
