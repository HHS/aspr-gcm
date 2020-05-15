package gcm.test.automated;

import static gcm.test.support.EnvironmentSupport.getRandomGenerator;
import static gcm.test.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.test.support.SeedProvider;
import gcm.util.annotations.UnitTest;
import gcm.util.earth.LatLon;
import gcm.util.earth.LatLonAlt;
/**
 * Test class for {@link LatLon}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = LatLon.class)
public class AT_LatLon {
	private static SeedProvider SEED_PROVIDER;

	private static final double TOLERANCE = 0.0001;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(3478756697899L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		// System.out.println(AT_LatLon.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link LatLon#getLatitude()}
	 */
	@Test
	public void testGetLatitude() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = 35;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLon#getLongitude()}
	 */

	@Test
	public void testGetLongitude() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLon#toString()}
	 */
	@Test
	public void testToString() {
		LatLon latLon = new LatLon(35, 128);
		assertEquals("LatLon [latitude=35.0, longitude=128.0]", latLon.toString());

		latLon = new LatLon(-35, 128);
		assertEquals("LatLon [latitude=-35.0, longitude=128.0]", latLon.toString());

		latLon = new LatLon(35, -128);
		assertEquals("LatLon [latitude=35.0, longitude=-128.0]", latLon.toString());

		latLon = new LatLon(-35, -128);
		assertEquals("LatLon [latitude=-35.0, longitude=-128.0]", latLon.toString());
	}

	/**
	 * Tests {@link LatLon#hashCode()}
	 */
	@Test
	public void testHashCode() {

		// Show equal objects have equal hash codes
		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon1 = new LatLon(latitude, longitude);
			LatLon latLon2 = new LatLon(latitude, longitude);
			assertEquals(latLon1.hashCode(), latLon2.hashCode());
		}

	}

	/**
	 * Tests {@link LatLon#equals(Object)}
	 */
	@Test
	public void testEquals() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon1 = new LatLon(latitude, longitude);
			LatLon latLon2 = new LatLon(latitude, longitude);
			LatLon latLon3 = new LatLon(latitude, longitude);

			// reflexive
			assertEquals(latLon2, latLon1);

			// associative
			assertEquals(latLon1, latLon2);
			assertEquals(latLon2, latLon1);

			// transitive
			assertEquals(latLon1, latLon3);
			assertEquals(latLon2, latLon3);
		}
	}

	/**
	 * Tests {@link LatLon constructors}
	 */
	@Test
	public void testConstructors() {

		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			LatLon latLon = new LatLon(latLonAlt);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}

		// pre-condition tests
		assertException(() -> new LatLon(null), RuntimeException.class);

		assertException(() -> new LatLon(-91, 0), RuntimeException.class);

		assertException(() -> new LatLon(91, 0), RuntimeException.class);

		assertException(() -> new LatLon(0, 181), RuntimeException.class);

		assertException(() -> new LatLon(0, -181), RuntimeException.class);

	}

}
