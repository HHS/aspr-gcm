package gcmexample.gettingstarted;

import gcm.output.OutputItem;
import gcm.scenario.RegionId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;

public final class VaccineInventoryOutputItem implements OutputItem {

	private final ScenarioId scenarioId;

	private final ReplicationId replicationId;

	private final double time;

	private final long vaccineInventory;

	private final RegionId regionId;

	public VaccineInventoryOutputItem(final ScenarioId scenarioId, final ReplicationId replicationId, final double time, final RegionId regionId, final long vaccineInventory) {
		super();
		this.scenarioId = scenarioId;
		this.replicationId = replicationId;
		this.time = time;
		this.regionId = regionId;
		this.vaccineInventory = vaccineInventory;
	}

	public long getVaccineInventory() {
		return vaccineInventory;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	@Override
	public ReplicationId getReplicationId() {
		return replicationId;
	}

	@Override
	public ScenarioId getScenarioId() {
		return scenarioId;
	}

	public double getTime() {
		return time;
	}
	
	

}
