package gcm.test.support.experimentspace;

import gcm.scenario.ExperimentBuilder;
import gcm.scenario.Scenario;

/**
 * Interface for extracting values from a scenario. It is used by
 * {@link ExperimentTestVariable} to allow the building of value tuples from a
 * {@link Scenario} generated by a {@link ExperimentBuilder} to be compared with
 * the expected tuples generated by the test.
 * 
 * @author Shawn Hatch
 *
 */
public interface ValueExtractor {
	public Object extractValue(Scenario scenario);
}