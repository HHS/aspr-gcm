package gcm.automated;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.Test;

import gcm.replication.Replication;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link Replication}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Replication.class)
public class AT_Replication {


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

	/**
	 * Tests {@link Replication#getReplications(int, long)}
	 */
	@Test	
	@UnitTestMethod(name ="getReplications", args= {int.class, long.class} )
	public void testGetReplications() {
		testGetReplicationsInvocation(0, 6346345456764L);
		testGetReplicationsInvocation(1, 457567566764L);
		testGetReplicationsInvocation(10, -3567856456764L);
		testGetReplicationsInvocation(17, 2745644564568889L);
		testGetReplicationsInvocation(35, -8856756785456L);
	}
	
	/**
	 * Tests {@link Replication#getReplication(int, long)}
	 */
	@Test	
	@UnitTestMethod(name ="getReplication", args= {int.class, long.class} )
	public void testGetReplication() {		
		long seed = 234234234234L;		
		List<Replication> replications = Replication.getReplications(100, seed);
		int repId = 45;
		Replication replication = Replication.getReplication(45, 234234234234L);
		assertEquals(repId,replication.getId().getValue());
		assertEquals(replications.get(repId-1).getSeed(),replication.getSeed());
	}
}