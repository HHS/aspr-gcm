package gcm.test.automated;

import static gcm.test.support.EnvironmentSupport.getRandomGenerator;
import static gcm.test.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.test.support.SeedProvider;
import gcm.util.annotations.UnitTest;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLon;
import gcm.util.earth.LatLonBox;
import gcm.util.earth.LatLonBox.LatLonBoxBuilder;
import gcm.util.spherical.SphericalPoint;
import gcm.util.spherical.SphericalTriangle;
import gcm.util.vector.Vector3D;

/**
 * Test class for {@link LatLonBox}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = LatLonBox.class)
public class AT_LatLonBox {
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
		// System.out.println(AT_LatLonBox.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@link LatLonBox#getWestLongitude()}
	 */
	@Test
	public void testGetWestLongitude() {
		// covered by testGetNorthLatitude() method
	}

	/**
	 * Tests {@link LatLonBox#getSouthWestLatLon()}. Shows that the LatLon has
	 * latitude and longitude values that match the LatLonBox latitude and
	 * longitude values.
	 */
	@Test
	public void testGetSouthWestLatLon() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		LatLon southWestLatLon = latLonBox.getSouthWestLatLon();
		assertEquals(latLonBox.getSouthLatitude(), southWestLatLon.getLatitude(), TOLERANCE);
		assertEquals(latLonBox.getWestLongitude(), southWestLatLon.getLongitude(), TOLERANCE);
	}

	/**
	 * Tests {@link LatLonBox#getSouthLatitude()}
	 */
	@Test
	public void testGetSouthLatitude() {
		// covered by testGetNorthLatitude() method
	}

	/**
	 * Tests {@link LatLonBox#getSouthEastLatLon()}. Shows that the LatLon has
	 * latitude and longitude values that match the LatLonBox latitude and
	 * longitude values.
	 */
	@Test
	public void testGetSouthEastLatLon() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		LatLon southEastLatLon = latLonBox.getSouthEastLatLon();
		assertEquals(latLonBox.getSouthLatitude(), southEastLatLon.getLatitude(), TOLERANCE);
		assertEquals(latLonBox.getEastLongitude(), southEastLatLon.getLongitude(), TOLERANCE);
	}

	/**
	 * Tests {@link LatLonBox#getNorthWestLatLon()}. Shows that the LatLon has
	 * latitude and longitude values that match the LatLonBox latitude and
	 * longitude values.
	 */
	@Test
	public void testGetNorthWestLatLon() {
		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		LatLon northWestLatLon = latLonBox.getNorthWestLatLon();
		assertEquals(latLonBox.getNorthLatitude(), northWestLatLon.getLatitude(), TOLERANCE);
		assertEquals(latLonBox.getWestLongitude(), northWestLatLon.getLongitude(), TOLERANCE);
	}

	/**
	 * Tests {@link LatLonBox#getNorthLatitude()}.
	 */
	@Test
	public void testGetNorthLatitude() {

		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		/*
		 * The LatLonBox should contain all the points, however, some points may
		 * fall out of the box due to precision issues.
		 */

		/*
		 * First, cut the box into two SphericalTriangles.
		 */
		List<SphericalTriangle> triangles = getTriangles(latLonBox);
		Earth earth = Earth.fromMeanRadius();

		/*
		 * Each point should fall into one of the the triangles
		 */
		for (LatLon latLon : points) {			
			SphericalPoint sphericalPoint = new SphericalPoint(earth.getECCFromLatLon(latLon));
			boolean pointContained = false;
			for (SphericalTriangle sphericalTriangle : triangles) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					pointContained = true;
					break;
				}
			}
			/*
			 * If the point did not fall into any of the triangles, then we must
			 * see how far they fall outside due to precision errors.
			 */
			if (!pointContained) {
				for (SphericalTriangle sphericalTriangle : triangles) {
					double distance = sphericalTriangle.distanceTo(sphericalPoint);
					distance *= earth.getRadius();
					// distance is now in meters, so this is a very small error
					if (distance < TOLERANCE) {
						// at least one triangle is very close to containing the
						// point
						pointContained = true;
						break;
					}
				}
			}
			assertTrue(pointContained);
		}

		// the lat lon box should have at least one point on each of its edges
		boolean northEdgeHit = false;
		boolean southEdgeHit = false;
		boolean eastEdgeHit = false;
		boolean westEdgeHit = false;
		for (LatLon latLon : points) {
			northEdgeHit |= latLon.getLatitude() == latLonBox.getNorthLatitude();
			southEdgeHit |= latLon.getLatitude() == latLonBox.getSouthLatitude();
			westEdgeHit |= latLon.getLongitude() == latLonBox.getWestLongitude();
			eastEdgeHit |= latLon.getLongitude() == latLonBox.getEastLongitude();
		}

		assertTrue(northEdgeHit);
		assertTrue(southEdgeHit);
		assertTrue(eastEdgeHit);
		assertTrue(westEdgeHit);

	}

	/**
	 * Tests {@link LatLonBox#getNorthEastLatLon()}. Shows that the LatLon has
	 * latitude and longitude values that match the LatLonBox latitude and
	 * longitude values.
	 */
	@Test
	public void testGetNorthEastLatLon() {
		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		LatLon northEastLatLon = latLonBox.getNorthEastLatLon();
		assertEquals(latLonBox.getNorthLatitude(), northEastLatLon.getLatitude(), TOLERANCE);
		assertEquals(latLonBox.getEastLongitude(), northEastLatLon.getLongitude(), TOLERANCE);
	}

	/**
	 * Tests {@link LatLonBox#getEastLongitude()}
	 */
	@Test
	public void testGetEastLongitude() {
		// covered by testGetNorthLatitude() method
	}

	private static List<LatLon> getScatteredPoints(double lat, double lon, double radius, int pointCount, RandomGenerator randomGenerator) {

		List<LatLon> result = new ArrayList<>();

		Earth earth = Earth.fromMeanRadius();

		Vector3D center = earth.getECCFromLatLon(new LatLon(lat, lon));

		Vector3D north = new Vector3D(0, 0, 1);

		for (int i = 0; i < pointCount; i++) {
			Vector3D v = new Vector3D(center);
			double distance = FastMath.sqrt(randomGenerator.nextDouble() * radius);
			double angle = distance / earth.getRadius();
			v = v.rotateToward(north, angle);
			angle = randomGenerator.nextDouble() * 2 * FastMath.PI;
			v = v.rotateAbout(center, angle);
			result.add(new LatLon(earth.getLatLonAlt(new Vector3D(v))));
		}

		return result;
	}

	private List<SphericalTriangle> getTriangles(LatLonBox latLonBox) {
		List<SphericalTriangle> result = new ArrayList<>();
		Earth earth = Earth.fromMeanRadius();
		Vector3D ne = earth.getECCFromLatLon(new LatLon(latLonBox.getNorthLatitude(), latLonBox.getEastLongitude()));
		Vector3D se = earth.getECCFromLatLon(new LatLon(latLonBox.getSouthLatitude(), latLonBox.getEastLongitude()));
		Vector3D sw = earth.getECCFromLatLon(new LatLon(latLonBox.getSouthLatitude(), latLonBox.getWestLongitude()));
		Vector3D nw = earth.getECCFromLatLon(new LatLon(latLonBox.getNorthLatitude(), latLonBox.getWestLongitude()));

		SphericalPoint neSphericalPoint = new SphericalPoint(ne);
		SphericalPoint seSphericalPoint = new SphericalPoint(se);
		SphericalPoint swSphericalPoint = new SphericalPoint(sw);
		SphericalPoint nwSphericalPoint = new SphericalPoint(nw);

		result.add(new SphericalTriangle(
									neSphericalPoint,
									nwSphericalPoint,
									swSphericalPoint
									));//

		result.add(new SphericalTriangle(
									neSphericalPoint,
									swSphericalPoint,
									seSphericalPoint
									));//

		return result;

	}

	/**
	 * Tests {@link LatLonBox#toString()}
	 */
	@Test
	public void testToString() {
		// Show equal objects have equal hash codes
		final long seed = SEED_PROVIDER.getSeedValue(6);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(15, 45, 20_000, 3, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox = builder.build();

		String expected = //
				"LatLonBox [northWestLatLon=LatLon [latitude=15.00068637139908, longitude=44.998943676442686],"//
						+ " southWestLatLon=LatLon [latitude=14.999520090478596, longitude=44.998943676442686],"//
						+ " southEastLatLon=LatLon [latitude=14.999520090478596, longitude=44.999469081381584],"//
						+ " northEastLatLon=LatLon [latitude=15.00068637139908, longitude=44.999469081381584]]";//
		String actual = latLonBox.toString();

		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link LatLonBox#hashCode()}
	 */
	@Test
	public void testHashCode() {
		// Show equal objects have equal hash codes
		final long seed = SEED_PROVIDER.getSeedValue(8);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox1 = builder.build();

		randomGenerator = getRandomGenerator(seed);

		// create an identical scattered set of LatLon values
		points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox2 = builder.build();

		assertEquals(latLonBox1.hashCode(), latLonBox2.hashCode());
	}

	/**
	 * Tests {@link LatLonBox#equals(Object)}
	 */
	@Test
	public void testEquals() {
		// Show equal objects have equal hash codes
		final long seed = SEED_PROVIDER.getSeedValue(7);

		// create a scattered set of LatLon values
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		List<LatLon> points = getScatteredPoints(35, 128, 3000, 10, randomGenerator);
		LatLonBoxBuilder builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox1 = builder.build();

		randomGenerator = getRandomGenerator(seed);
		points = getScatteredPoints(35, 128, 3000, 10, randomGenerator);
		builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox2 = builder.build();

		randomGenerator = getRandomGenerator(seed);
		points = getScatteredPoints(35, 128, 3000, 10, randomGenerator);
		builder = LatLonBox.builder();
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox3 = builder.build();

		// reflexive
		assertEquals(latLonBox2, latLonBox1);

		// associative
		assertEquals(latLonBox1, latLonBox2);
		assertEquals(latLonBox2, latLonBox1);

		// transitive
		assertEquals(latLonBox1, latLonBox3);
		assertEquals(latLonBox2, latLonBox3);
	}

	/**
	 * Tests {@link LatLonBox constructors}
	 */
	@Test
	public void testConstructors() {
		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);
		LatLonBoxBuilder builder = LatLonBox.builder();

		// Show equal objects have equal hash codes

		// create a scattered set of LatLon values
		List<LatLon> points = getScatteredPoints(35, 128, 300_000, 100, randomGenerator);
		for (LatLon latLon : points) {
			builder.add(latLon);
		}
		LatLonBox latLonBox1 = builder.build();

		// build a new LatLonBox from the first one
		builder.add(latLonBox1);
		LatLonBox latLonBox2 = builder.build();

		// show that they are equal
		assertEquals(latLonBox1, latLonBox2);

		// pre-condition tests
		assertException(() -> {
			LatLonBox latLonBox = null;
			LatLonBox.builder().add(latLonBox);
		}, RuntimeException.class);

		assertException(() -> {
			LatLon latLon = null;
			LatLonBox.builder().add(latLon);
		}, RuntimeException.class);

	}

}
