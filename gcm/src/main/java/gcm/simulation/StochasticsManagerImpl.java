package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import gcm.scenario.RandomNumberGeneratorId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implements {@link StochasticsManager}
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class StochasticsManagerImpl extends BaseElement implements StochasticsManager {

	private Map<RandomNumberGeneratorId, RandomGenerator> randomGeneratorMap = new LinkedHashMap<>();

	private RandomGenerator randomGenerator;

	@Override
	public void init(Context context) {
		super.init(context);
		long replicationSeed = context.getReplication().getSeed();
		// create RandomGenerators for each of the ids using a hash built from
		// the id and the replication seed
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = context.getScenario().getRandomNumberGeneratorIds();
		for(RandomNumberGeneratorId randomNumberGeneratorId : randomNumberGeneratorIds) {
			String name = randomNumberGeneratorId.toString();
			long seedForId = name.hashCode()+replicationSeed;
			RandomGenerator randomGeneratorForID = new Well44497b(seedForId);
			randomGeneratorMap.put(randomNumberGeneratorId, randomGeneratorForID);
		}

		// finally, set up the standard RandomGenerator
		randomGenerator = new Well44497b(replicationSeed);
	}

	@Override
	public RandomGenerator getRandomGenerator() {
		return randomGenerator;
	}

	@Override
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId) {
		return randomGeneratorMap.get(randomNumberGeneratorId);
	}
}
