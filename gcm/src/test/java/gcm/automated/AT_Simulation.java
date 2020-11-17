package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gcm.experiment.progress.ExperimentProgressLog;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.output.simstate.SimulationStatusItem;
import gcm.replication.Replication;
import gcm.scenario.ReplicationId;
import gcm.scenario.Scenario;
import gcm.scenario.ScenarioId;
import gcm.scenario.UnstructuredScenarioBuilder;
import gcm.simulation.Simulation;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link Simulation}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Simulation.class)
public class AT_Simulation {

	/*
	 * 
	 * Local Output handler to demonstrate that the simulation uses contributed
	 * output item handlers
	 *
	 */
	private static class TestOutputItemHandler implements OutputItemHandler {
		private ReplicationId replicationId;
		private ScenarioId scenarioId;
		private boolean initialized;
		private boolean finalized;
		private boolean receivedSimulationStatusItems;

		public boolean isFinalized() {
			return finalized;
		}

		public boolean isInitialized() {
			return initialized;
		}

		public boolean hasReceivedSimulationStatusItems() {
			return receivedSimulationStatusItems;
		}

		public ReplicationId getRelicationId() {
			return replicationId;
		}

		public ScenarioId getScenarioId() {
			return scenarioId;
		}

		@Override
		public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			initialized = true;

		}

		@Override
		public void openExperiment(ExperimentProgressLog experimentProgressLog) {

		}

		@Override
		public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
			finalized = true;
		}

		@Override
		public void closeExperiment() {

		}

		@Override
		public void handle(OutputItem outputItem) {
			receivedSimulationStatusItems = true;
			SimulationStatusItem simulationStatusItem = (SimulationStatusItem) outputItem;
			replicationId = simulationStatusItem.getReplicationId();
			scenarioId = simulationStatusItem.getScenarioId();
		}

		@Override
		public Set<Class<? extends OutputItem>> getHandledClasses() {
			Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
			result.add(SimulationStatusItem.class);
			return result;
		}

	}

	/**
	 * Test {@link Simulation#Simulation()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		Simulation simulation = new Simulation();
		assertNotNull(simulation);
	}

	/**
	 * Test {@link Simulation#addOutputItemHandler(OutputItemHandler)}
	 */
	@Test
	@UnitTestMethod(name = "addOutputItemHandler", args = { OutputItemHandler.class })
	public void testAddOutputItemHandler() {
		Simulation simulation = new Simulation();

		// precondition test: if the output item handler is null

		assertException(() -> simulation.addOutputItemHandler(null), RuntimeException.class);

		// postcondition test: show that a contributed output item handler is
		// initialized and finalized by the simulation
		TestOutputItemHandler testOutputItemHandler = new TestOutputItemHandler();
		simulation.addOutputItemHandler(testOutputItemHandler);
		simulation.setReplication(Replication.getReplication(1, 456456456L));
		UnstructuredScenarioBuilder unstructuredScenarioBuilder = new UnstructuredScenarioBuilder();
		unstructuredScenarioBuilder.setScenarioId(new ScenarioId(45));
		Scenario scenario = unstructuredScenarioBuilder.build();
		simulation.setScenario(scenario);

		simulation.execute();
		assertTrue(testOutputItemHandler.isInitialized());
		assertTrue(testOutputItemHandler.isFinalized());

	}

	/**
	 * Test {@link Simulation#execute()}
	 */
	@Test
	@UnitTestMethod(name = "execute", args = {})
	public void testExecute() {
		UnstructuredScenarioBuilder unstructuredScenarioBuilder = new UnstructuredScenarioBuilder();
		unstructuredScenarioBuilder.setScenarioId(new ScenarioId(45));
		Scenario scenario = unstructuredScenarioBuilder.build();
		Replication replication = Replication.getReplication(1, 456456456L);

		// precondition test: if the scenario has not been set
		Simulation simulation1 = new Simulation();
		simulation1.setReplication(replication);
		assertException(() -> simulation1.execute(), RuntimeException.class);

		// precondition test: if the replication has not been set
		Simulation simulation2 = new Simulation();
		simulation2.setScenario(scenario);
		assertException(() -> simulation2.execute(), RuntimeException.class);

		// postcondition test: show that a simulation executed
		Simulation simulation3 = new Simulation();
		TestOutputItemHandler testOutputItemHandler = new TestOutputItemHandler();
		simulation3.addOutputItemHandler(testOutputItemHandler);
		simulation3.setReplication(replication);
		simulation3.setScenario(scenario);

		simulation3.execute();
		assertTrue(testOutputItemHandler.hasReceivedSimulationStatusItems());

	}

	/**
	 * Test {@link Simulation#setReplication(Replication)}
	 */
	@Test
	@UnitTestMethod(name = "setReplication", args = { Replication.class })
	public void testSetReplication() {
		UnstructuredScenarioBuilder unstructuredScenarioBuilder = new UnstructuredScenarioBuilder();
		unstructuredScenarioBuilder.setScenarioId(new ScenarioId(45));
		Scenario scenario = unstructuredScenarioBuilder.build();
		Replication replication = Replication.getReplication(1, 456456456L);

		// precondition test: if the replication has not been set
		Simulation simulation1 = new Simulation();
		simulation1.setScenario(scenario);
		assertException(() -> simulation1.execute(), RuntimeException.class);

		// precondition test: if the replication was set to null
		Simulation simulation2 = new Simulation();
		simulation2.setScenario(scenario);
		simulation2.setReplication(null);
		assertException(() -> simulation2.execute(), RuntimeException.class);

		// postcondition test: show that the replication is used by the
		// simulation
		Simulation simulation3 = new Simulation();
		TestOutputItemHandler testOutputItemHandler = new TestOutputItemHandler();
		simulation3.addOutputItemHandler(testOutputItemHandler);
		simulation3.setReplication(replication);
		simulation3.setScenario(scenario);

		simulation3.execute();
		assertEquals(replication.getId(), testOutputItemHandler.getRelicationId());
	}

	/**
	 * Test {@link Simulation#setScenario(Scenario)}
	 */
	@Test
	@UnitTestMethod(name = "setScenario", args = { Scenario.class })
	public void testSetScenario() {
		UnstructuredScenarioBuilder unstructuredScenarioBuilder = new UnstructuredScenarioBuilder();
		unstructuredScenarioBuilder.setScenarioId(new ScenarioId(45));
		Scenario scenario = unstructuredScenarioBuilder.build();
		Replication replication = Replication.getReplication(1, 456456456L);

		// precondition test: if the scenario has not been set
		Simulation simulation1 = new Simulation();
		simulation1.setReplication(replication);
		assertException(() -> simulation1.execute(), RuntimeException.class);

		// precondition test: if the scenario was set to null
		Simulation simulation2 = new Simulation();
		simulation2.setScenario(null);
		simulation2.setReplication(replication);
		assertException(() -> simulation2.execute(), RuntimeException.class);

		// postcondition test: show that the replication is used by the
		// simulation
		Simulation simulation3 = new Simulation();
		TestOutputItemHandler testOutputItemHandler = new TestOutputItemHandler();
		simulation3.addOutputItemHandler(testOutputItemHandler);
		simulation3.setReplication(replication);
		simulation3.setScenario(scenario);

		simulation3.execute();
		assertEquals(scenario.getScenarioId(), testOutputItemHandler.getScenarioId());
	}

}
