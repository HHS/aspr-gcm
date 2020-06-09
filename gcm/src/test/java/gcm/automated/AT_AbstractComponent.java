package gcm.test.automated;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.components.AbstractComponent;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.GroupInfo.GroupInfoBuilder;
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
import gcm.test.support.ExceptionAssertion;
import gcm.test.support.TestCompartmentId;
import gcm.test.support.TestGlobalComponentId;
import gcm.test.support.TestGlobalPropertyId;
import gcm.test.support.TestGroupTypeId;
import gcm.test.support.TestMaterialsProducerId;
import gcm.test.support.TestMaterialsProducerPropertyId;
import gcm.test.support.TestPersonPropertyId;
import gcm.test.support.TestRegionId;
import gcm.test.support.TestRegionPropertyId;
import gcm.test.support.TestResourceId;
import gcm.test.support.ExceptionAssertion.ExceptionGenerator;
import gcm.util.annotations.UnitTest;

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
			GroupInfoBuilder groupInfoBuilder = new GroupInfoBuilder();
			groupInfoBuilder.setGroupId(groupId);
			groupInfoBuilder.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
			GroupInfo groupInfo = groupInfoBuilder.build();
			GroupPropertyId groupPropertyId = TestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];

			assertUnimplemented(() -> executePlan(environment, plan));
			assertUnimplemented(() -> observeCompartmentPersonArrival(environment, personId));
			assertUnimplemented(() -> observeCompartmentPersonDeparture(environment, compartmentId, personId));
			assertUnimplemented(() -> observeCompartmentPropertyChange(environment, compartmentId, compartmentPropertyId));
			assertUnimplemented(() -> observeGlobalPersonArrival(environment, personId));
			assertUnimplemented(() -> observeGlobalPersonDeparture(environment, personId));
			assertUnimplemented(() -> observeGlobalPropertyChange(environment, globalPropertyId));
			assertUnimplemented(() -> observeMaterialsProducerPropertyChange(environment, materialsProducerId1, materialsProducerPropertyId));
			assertUnimplemented(() -> observeMaterialsProducerResourceChange(environment, materialsProducerId1, resourceId));
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
			assertUnimplemented(() -> observeStageTransfer(environment, stageId, materialsProducerId1, materialsProducerId2));
			assertUnimplemented(() -> observeGroupConstruction(environment, groupId));
			assertUnimplemented(() -> observeGroupDestruction(environment, groupInfo));
			assertUnimplemented(() -> observeGroupPropertyChange(environment, groupId, groupPropertyId));
			assertUnimplemented(() -> observeGroupPersonArrival(environment, groupId, personId));
			assertUnimplemented(() -> observeGroupPersonDeparture(environment, groupId, personId));

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

	@Test
	public void test() {
		/*
		 * Show that each abstract method of the AbstractComponent class will
		 * throw a RuntimeException if the overriding class does not implement
		 * the method.
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
				PropertyDefinition	.builder()//
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
}
