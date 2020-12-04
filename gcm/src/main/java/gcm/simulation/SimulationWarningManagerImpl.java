package gcm.simulation;

import gcm.output.simstate.SimulationWarningItem;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public class SimulationWarningManagerImpl extends BaseElement implements SimulationWarningManager {

	private OutputItemManager outputItemManager;


	@Override
	public void init(Context context) {
		super.init(context);
		outputItemManager = context.getOutputItemManager();
	}
	
	@Override
	public void processWarning(SimulationWarningItem simulationWarningItem) {
		outputItemManager.releaseOutputItem(simulationWarningItem);
	}

}
