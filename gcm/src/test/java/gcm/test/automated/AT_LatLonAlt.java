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
import gcm.util.vector.Vector3D;
/**
 * Test class for {@link LatLonAlt}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LatLonAlt.class)
public class AT_LatLonAlt {
	private static SeedProvider SEED_PROVIDER;

	private static final double TOLERANCE = 0.0001;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(34453452345789L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		// System.out.println(AT_LatLonAlt.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link LatLonAlt#getLatitude()}
	 */
	@Test
	public void testGetLatitude() {
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = 35;
			double altitude = 1000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#getLongitude()}
	 */
	@Test
	public void testGetLongitude() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = 1000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#getAltitude()}
	 */
	@Test
	public void testGetAltitude() {
		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = 128;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(altitude, latLonAlt.getAltitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#toString()}
	 */
	@Test
	public void testToString() {
		LatLonAlt latLonAlt = new LatLonAlt(35, 128, 1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, 128, 1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, -128, 1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=-128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, -128, 1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=-128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, 128, -1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, 128, -1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, -128, -1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=-128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, -128, -1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=-128.0, altitude=-1000.0]", latLonAlt.toString());
	}

	/**
	 * Tests {@link LatLonAlt#hashCode()}
	 */
	@Test
	public void testHashCode() {

		// Show equal objects have equal hash codes
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt1 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt2 = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(latLonAlt1.hashCode(), latLonAlt2.hashCode());
		}

	}

	/**
	 * Tests {@link LatLonAlt#equals(Object)}
	 */
	@Test
	public void testEquals() {
		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt1 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt2 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt3 = new LatLonAlt(latitude, longitude, altitude);

			// reflexive
			assertEquals(latLonAlt1, latLonAlt1);

			// associative
			assertEquals(latLonAlt1, latLonAlt2);
			assertEquals(latLonAlt2, latLonAlt1);

			// transitive
			assertEquals(latLonAlt1, latLonAlt3);
			assertEquals(latLonAlt2, latLonAlt3);
		}
	}

	/**
	 * Tests {@link LatLonAlt constructors}
	 */
	@SuppressWarnings("unused")
	@Test
	public void testConstructors() {
		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLon latLon = new LatLon(latitude, longitude);
			LatLonAlt latLonAlt = new LatLonAlt(latLon);
			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
			assertEquals(0, latLonAlt.getAltitude(), 0);

			Vector3D v = new Vector3D(latitude, longitude, altitude);
			latLonAlt = new LatLonAlt(v);
			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
			assertEquals(altitude, latLonAlt.getAltitude(), TOLERANCE);
		}

		// pre-condition tests
		assertException(() -> {
			Vector3D v = null;
			new LatLonAlt(v);
		}, RuntimeException.class);

		assertException(() -> {
			LatLon latLon = null;
			new LatLonAlt(latLon);
		}, RuntimeException.class);

		assertException(() -> new LatLonAlt(-91, 0, 1000), RuntimeException.class);

		assertException(() -> new LatLonAlt(91, 0, 1000), RuntimeException.class);

		assertException(() -> new LatLonAlt(0, 181, 1000), RuntimeException.class);

		assertException(() -> new LatLonAlt(0, -181, 1000), RuntimeException.class);

	}

	/**
	 * Tests {@link LatLonAlt#toVector3D()}
	 */
	@Test
	public void testToVector3D() {
		final long seed = SEED_PROVIDER.getSeedValue(6);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			Vector3D v = latLonAlt.toVector3D();
			assertEquals(latLonAlt.getLatitude(), v.getX(), TOLERANCE);
			assertEquals(latLonAlt.getLongitude(), v.getY(), TOLERANCE);
			assertEquals(latLonAlt.getAltitude(), v.getZ(), TOLERANCE);
		}
	}

}
