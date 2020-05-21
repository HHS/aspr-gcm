package gcmexample.gettingstarted;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.experiment.progress.ExperimentProgressLog;
import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class VaccineOutputItemHandler implements OutputItemHandler {

	@Override
	public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		System.out.println("Simulation has started: scenario = " + scenarioId + " replication = " + replicationId);
	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
		System.out.println("Experiment has started");
	}

	@Override
	public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		System.out.println("Simulation has ended: scenario = " + scenarioId + " replication = " + replicationId);

	}

	@Override
	public void closeExperiment() {
		System.out.println("Experiment has ended");

	}

	@Override
	public void handle(OutputItem outputItem) {
		VaccineInventoryOutputItem vaccineInventoryOutputItem = (VaccineInventoryOutputItem)outputItem;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(vaccineInventoryOutputItem.getScenarioId());
		sb.append(vaccineInventoryOutputItem.getReplicationId());
		sb.append(vaccineInventoryOutputItem.getTime());
		sb.append(vaccineInventoryOutputItem.getRegionId());
		sb.append(vaccineInventoryOutputItem.getVaccineInventory());
		
		System.out.println(sb);
		
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(VaccineInventoryOutputItem.class);
		return result;
	}

}
