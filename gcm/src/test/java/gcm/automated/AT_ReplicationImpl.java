package gcm.test.automated;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.Test;

import gcm.replication.Replication;
import gcm.replication.ReplicationImpl;
import gcm.scenario.ReplicationId;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link ReplicationImpl}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ReplicationImpl.class)
public class AT_ReplicationImpl {

	public void testGetSeed() {
		int[] ids = { 17, 2344, -12352356, 776785768, 2, 0 };
		long[] seeds = { 340983453345L, 34563455L, 97735136456L, 36694567567L, 234333456L, 8876867225L };

		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			long seed = seeds[i];
			ReplicationId replicationId = new ReplicationId(id);
			Replication replication = new ReplicationImpl(replicationId, seed);
			assertEquals(seed, replication.getSeed().longValue());
		}

	}
	
	public void testGetId() {
		int[] ids = { 17, 2344, -12352356, 776785768, 2, 0 };
		long[] seeds = { 340983453345L, 34563455L, 97735136456L, 36694567567L, 234333456L, 8876867225L };

		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			long seed = seeds[i];
			ReplicationId replicationId = new ReplicationId(id);
			Replication replication = new ReplicationImpl(replicationId, seed);
			assertEquals(replicationId, replication.getId());
		}

	}

	private static void testGetReplicationsInvocation(final int replicationCount, final long seed) {

		final List<Replication> replications = Replication.getReplications(replicationCount, seed);

		final RandomGenerator randomGenerator = new Well44497b(seed);
		assertEquals(replicationCount, replications.size());
		for (int i = 0; i < replications.size(); i++) {
			final Replication replication = replications.get(i);
			final int expectedId = i + 1;
			assertEquals(expectedId, replication.getId().getValue());
			assertEquals(randomGenerator.nextLong(), (long) replication.getSeed());
		}

	}

	/*
	 * Tests several instances of a List of Replications built by the
	 * ReplicationFactory and asserts that the inputs to the factory match both
	 * the number of Replications built, but also the expected field values for
	 * each replication. Note that we have a strong expectation as to the field
	 * values of the replications since the ReplicationFactory documentation
	 * states that it is using a Well44497b random generator.
	 */
	@Test
	public void testGetReplications() {
		testGetReplicationsInvocation(0, 6346345456764L);
		testGetReplicationsInvocation(1, 457567566764L);
		testGetReplicationsInvocation(10, -3567856456764L);
		testGetReplicationsInvocation(17, 2745644564568889L);
		testGetReplicationsInvocation(35, -8856756785456L);

	}
}
