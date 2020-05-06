package gcm.simulation;

import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;

/**
 * The OutputItemManager is the manager for all {@link OutputItem} values that
 * are leaving the simulation and being consumed by experiment level
 * {@link OutputItemHandler} handlers.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface OutputItemManager extends Element {

	/**
	 * Publishes the {@link OutputItem} to all {@link OutputItemHandler}
	 * instances registered for the specific implementing class of the output
	 * item.
	 */
	public void releaseOutputItem(OutputItem outputItem);

	/**
	 * Invoked once at the end of the simulation, this signals to the
	 * {@link OutputItemManager} that it should in turn signal to the
	 * {@link OutputItemHandler} instances that the simulation is closing.
	 */
	public void close();
}
