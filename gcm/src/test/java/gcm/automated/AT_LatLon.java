package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import gcm.automated.support.SeedProvider;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.earth.LatLon;
import gcm.util.earth.LatLonAlt;
import org.junit.jupiter.api.Test;

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

	@BeforeAll
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(3478756697899L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterAll
	public static void afterClass() {
		// System.out.println(AT_LatLon.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link LatLon#getLatitude()}
	 */
	@Test
	@UnitTestMethod(name = "getLatitude", args = {})
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
	@UnitTestMethod(name = "getLongitude", args = {})
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
	@UnitTestMethod(name = "toString", args = {})
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
	@UnitTestMethod(name = "hashCode", args = {})
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
	@UnitTestMethod(name = "equals", args = { Object.class })
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
	 * Tests {@link LatLon#LatLon(double, double)}
	 */
	@Test
	@UnitTestConstructor(args= {double.class, double.class})
	public void testConstructor_Doubles() {

		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}

		// pre-condition tests

		assertException(() -> new LatLon(-91, 0), RuntimeException.class);

		assertException(() -> new LatLon(91, 0), RuntimeException.class);

		assertException(() -> new LatLon(0, 181), RuntimeException.class);

		assertException(() -> new LatLon(0, -181), RuntimeException.class);

	}
	
	/**
	 * Tests {@link LatLon#LatLon(LatLonAlt)}
	 */
	@Test
	@UnitTestConstructor(args= {LatLonAlt.class})
	public void testConstructor_LatLonAlt() {

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

	}


}
