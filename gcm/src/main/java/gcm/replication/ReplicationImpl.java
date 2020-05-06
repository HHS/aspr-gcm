package gcm.replication;

import gcm.scenario.ReplicationId;
import gcm.util.annotations.Source;
import net.jcip.annotations.Immutable;

/**
 * Implementor of {@link ReplicationImpl}. A replication represents an immutable
 * random number generator seed that is to be used across all scenarios within
 * an experiment. Replications are used with scenarios within an experiment to
 * show the effects of random perturbation on each scenario. Each scenario in an
 * experiment is paired with each replication in an experiment to form the
 * simulation runs. Replications are uniquely numbered starting with ID 1.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
@Source
public final class ReplicationImpl implements Replication {

	/**
	 * Returns a list of Replication having the size indicated by the
	 * replication count. Seeds for each replication are generated using a
	 * Well44497b random generator using the given seed value. The resulting
	 * list of replications will have positive long-valued identifiers starting
	 * with 1.
	 *
	 * @param replicationCount
	 * @param seed
	 * @throws IllegalArgumentException
	 *             if the replication count is negative
	 */
	// public static List<ReplicationImpl> getReplications(final int
	// replicationCount, final long seed) {
	// final List<ReplicationImpl> result = new ArrayList<>();
	// if (replicationCount < 0) {
	// throw new IllegalArgumentException("negative count");
	// }
	// final RandomGenerator randomGenerator = new Well44497b(seed);
	// for (int i = 1; i <= replicationCount; i++) {
	// result.add(new ReplicationImpl(new ReplicationId(i),
	// randomGenerator.nextLong()));
	// }
	// return result;
	// }

	/**
	 * Returns an indexed Replication as generated from the given seed. Seeds
	 * for each replication are generated using a Well44497b random generator
	 * using the given seed value.
	 *
	 * @param replicationId
	 * @param seed
	 * @throws IllegalArgumentException
	 *             if the replication id is not positive
	 */
	// public static ReplicationImpl getReplication(final int replicationId,
	// final long seed) {
	// if (replicationId < 1) {
	// throw new IllegalArgumentException("non-positive replication id");
	// }
	// final RandomGenerator randomGenerator = new Well44497b(seed);
	// for (int i = 1; i < replicationId; i++) {
	// randomGenerator.nextLong();
	// }
	// return new ReplicationImpl(new ReplicationId(replicationId),
	// randomGenerator.nextLong());
	// }

	private final ReplicationId id;

	private final Long seed;

	public ReplicationImpl(final ReplicationId id, final Long seed) {
		super();
		this.id = id;
		this.seed = seed;
	}

	/**
	 * Returns the id of this replication. Replications within an experiment are
	 * numbered 1, 2, 3, ..., N.
	 *
	 * @return
	 */
	@Override
	public ReplicationId getId() {
		return id;
	}

	/**
	 * Returns the immutable random number generator seed that starts a
	 * simulation run.
	 *
	 * @return
	 */
	@Override
	public Long getSeed() {
		return seed;
	}

}
