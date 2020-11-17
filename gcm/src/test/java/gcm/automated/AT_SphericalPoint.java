package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gcm.automated.support.SeedProvider;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.spherical.MalformedSphericalPointException;
import gcm.util.spherical.SphericalPoint;
import gcm.util.vector.MutableVector3D;
import gcm.util.vector.Vector3D;

import static gcm.automated.support.ExceptionAssertion.*;

/**
 * Test class for {@link SphericalPoint}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = SphericalPoint.class)
public class AT_SphericalPoint {

	private static SeedProvider SEED_PROVIDER;

	@BeforeAll
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(341745979674534L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterAll
	public static void afterClass() {
		// System.out.println(AT_SphericalPoint.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link SphericalPoint#SphericalPoint(MutableVector3D)}
	 */
	@Test
	@UnitTestConstructor(args = { MutableVector3D.class })
	public void testConstructors_MutableVector3D() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			double length = FastMath.sqrt(x * x + y * y + z * z);
			SphericalPoint sphericalPoint = new SphericalPoint(new MutableVector3D(x, y, z));

			assertTrue(FastMath.abs(x / length - sphericalPoint.getPosition().getX()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(y / length - sphericalPoint.getPosition().getY()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(z / length - sphericalPoint.getPosition().getZ()) < Vector3D.NORMAL_LENGTH_TOLERANCE);

		}

	}

	/**
	 * Tests {@link SphericalPoint#SphericalPoint(Vector3D)}
	 */
	@Test
	@UnitTestConstructor(args = { Vector3D.class })
	public void testConstructors_Vector3D() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			double length = FastMath.sqrt(x * x + y * y + z * z);
			SphericalPoint sphericalPoint = new SphericalPoint(new Vector3D(x, y, z));

			assertTrue(FastMath.abs(x / length - sphericalPoint.getPosition().getX()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(y / length - sphericalPoint.getPosition().getY()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(z / length - sphericalPoint.getPosition().getZ()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			
			assertException(()->new SphericalPoint(new Vector3D(0,0,0)), MalformedSphericalPointException.class);
			assertException(()->new SphericalPoint(new MutableVector3D(0,0,0)), MalformedSphericalPointException.class);

		}
		
		//precondition tests
		

	}

	/**
	 * Tests {@link SphericalPoint#getPosition()}
	 */
	@Test
	@UnitTestMethod(name = "getPosition", args = {})
	public void testGetPosition() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		for (int i = 0; i < 100; i++) {
			RandomGenerator randomGenerator = getRandomGenerator(seed);
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v = new Vector3D(x, y, z);
			SphericalPoint sphericalPoint = new SphericalPoint(v);
			v = v.normalize();
			Vector3D u = sphericalPoint.getPosition();
			assertEquals(v, u);
		}

	}

	/**
	 * Tests {@link SphericalPoint#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		final long seed = SEED_PROVIDER.getSeedValue(2);

		for (int i = 0; i < 100; i++) {
			RandomGenerator randomGenerator = getRandomGenerator(seed);
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v = new Vector3D(x, y, z);
			SphericalPoint sphericalPoint = new SphericalPoint(v);
			v = v.normalize();

			String expected = v.toString();
			expected = "SphericalPoint [position=" + expected + "]";

			String actual = sphericalPoint.toString();
			assertEquals(expected, actual);
		}

	}

}
