package gcm.output;

import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import net.jcip.annotations.Immutable;

/**
 * The base interface for all data items produced by the the experiment.
 * 
 * @author Shawn Hatch
 *
 */
@Source
@Immutable
public interface OutputItem {

	/**
	 * Returns the {@link ScenarioId} of the assigned scenario of the simulation
	 * instance that produced this {@link OutputItem}
	 */
	public ScenarioId getScenarioId();
	
	/**
	 * Returns the {@link ReplicationId} of the assigned replication of the simulation
	 * instance that produced this {@link OutputItem}
	 */
	public ReplicationId getReplicationId();

}
