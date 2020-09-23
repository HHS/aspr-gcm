package gcm.manual;

import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;

/**
 * A functional interface for selecting people from a partition based on assigning a
 * weighting value to a {@link LabelSet}.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface LabelSetWeightingFunction2 {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * Repeated invocations with the same arguments should return the same value
	 * while no mutations to simulation state have taken place.
	 */
	public double getWeight(ObservableEnvironment observableEnvironment, LabelSet2 labelSet);
}
