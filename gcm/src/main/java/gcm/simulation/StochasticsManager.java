package gcm.simulation;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;

/**
 * Manager for the {@link RandomGenerator} instance for the simulation
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface StochasticsManager extends Element {

	/**
	 * Returns the {@link RandomGenerator} instance for the simulation
	 */
	public RandomGenerator getRandomGenerator();
	
	/**
	 * Returns the {@link RandomGenerator} instance for the simulation
	 */
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId);

}
