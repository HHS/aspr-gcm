package gcm.automated;

import static gcm.automated.support.EnvironmentSupport.getRandomGenerator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gcm.automated.support.SeedProvider;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.spherical.Chirality;
import gcm.util.vector.MutableVector2D;
import gcm.util.vector.MutableVector3D;
import gcm.util.vector.Vector2D;

/**
 * Test class for {@link Vector2D}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Vector2D.class)
public class AT_Vector2D {
	private static SeedProvider SEED_PROVIDER;

	private static final double TOLERANCE = 0.000001;

	@BeforeClass
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(346345534578886785L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterClass
	public static void afterClass() {
		// System.out.println(AT_Vector2D.class.getSimpleName() + " " +
		// SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests {@linkplain Vector2D#add(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { Vector2D.class })
	public void testAdd_Vector2D() {

		final long seed = SEED_PROVIDER.getSeedValue(29);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			Vector2D v3 = v1.add(v2);

			assertEquals(x1 + x2, v3.getX(), 0);
			assertEquals(y1 + y2, v3.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#add(double, double)}
	 * 
	 * Tests {@linkplain Vector2D#add(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double.class, double.class })
	public void testAdd_Doubles() {

		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = v1.add(x2, y2);

			assertEquals(x1 + x2, v2.getX(), 0);
			assertEquals(y1 + y2, v2.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructors_Empty() {
		Vector2D v = new Vector2D();
		assertEquals(0, v.getX(), 0);
		assertEquals(0, v.getY(), 0);
	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(MutableVector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { MutableVector2D.class })
	public void testConstructors_MutableVector2D() {

		final long seed = SEED_PROVIDER.getSeedValue(31);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x, y);

			Vector2D v2 = new Vector2D(v1);

			assertEquals(v2.getX(), x, 0);
			assertEquals(v2.getY(), y, 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(Vector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { Vector2D.class })
	public void testConstructors_Vector2D() {

		final long seed = SEED_PROVIDER.getSeedValue(32);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(v1);

			assertEquals(v2.getX(), x, 0);
			assertEquals(v2.getY(), y, 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(double, double)}
	 */
	@Test
	@UnitTestConstructor(args = { double.class, double.class })
	public void testConstructors_Doubles() {

		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x, y);

			assertEquals(v1.getX(), x, 0);
			assertEquals(v1.getY(), y, 0);
		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#addScaled(Vector2D, double)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "addScaled", args = { Vector2D.class, double.class })
	public void testAddScaled() {

		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v3 = v1.addScaled(v2, scale);

			assertEquals(x1 + x2 * scale, v3.getX(), 0);
			assertEquals(y1 + y2 * scale, v3.getY(), 0);
		}

	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#angle(Vector2D)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "angle", args = { Vector2D.class })
	public void testAngle() {
		final long seed = SEED_PROVIDER.getSeedValue(3);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double length1 = FastMath.sqrt(x1 * x1 + y1 * y1);
			double length2 = FastMath.sqrt(x2 * x2 + y2 * y2);
			double dotProduct = x1 * x2 + y1 * y2;
			double cosTheta = dotProduct / (length1 * length2);
			double expectedValue = FastMath.acos(cosTheta);

			double actualValue = v1.angle(v2);

			assertEquals(expectedValue, actualValue, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#cross(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "cross", args = { Vector2D.class })
	public void testCross() {

		final long seed = SEED_PROVIDER.getSeedValue(5);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);
			int actual = v2.cross(v1);

			double zComponentOf3DCrossProduct = (x2 * y1) - (x1 * y2);
			int expected;
			if (zComponentOf3DCrossProduct < 0) {
				expected = -1;
			} else {
				expected = 1;
			}
			assertEquals(expected, actual);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#distanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "distanceTo", args = { Vector2D.class })
	public void testDistanceTo() {

		final long seed = SEED_PROVIDER.getSeedValue(6);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

			double actual = v2.distanceTo(v1);

			assertEquals(expected, actual, TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain Vector2D#dot(Vector2D))}
	 */
	@Test
	@UnitTestMethod(name = "dot", args = { Vector2D.class })
	public void testDot() {

		final long seed = SEED_PROVIDER.getSeedValue(7);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = x1 * x2 + y1 * y2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#get(int)}
	 */
	@Test
	@UnitTestMethod(name = "get", args = { int.class })
	public void testGet() {

		final long seed = SEED_PROVIDER.getSeedValue(9);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(x, v.get(0), 0);
			assertEquals(y, v.get(1), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#getX()}
	 */
	@Test
	@UnitTestMethod(name = "getX", args = {})
	public void testGetX() {

		final long seed = SEED_PROVIDER.getSeedValue(12);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(x, v.getX(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#getY()}
	 */
	@Test
	@UnitTestMethod(name = "getY", args = {})
	public void testGetY() {

		final long seed = SEED_PROVIDER.getSeedValue(10);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#scale(double)}
	 */
	@Test
	@UnitTestMethod(name = "scale", args = { double.class })
	public void testScale() {

		final long seed = SEED_PROVIDER.getSeedValue(13);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double scalar = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = v.scale(scalar);

			assertEquals(x * scalar, v1.getX(), 0);
			assertEquals(y * scalar, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#sub(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { Vector2D.class })
	public void testSub_Vector2D() {

		final long seed = SEED_PROVIDER.getSeedValue(30);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			v1 = v1.sub(v2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#sub(double, double)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { double.class, double.class })
	public void testSub_Doubles() {

		final long seed = SEED_PROVIDER.getSeedValue(17);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new Vector2D(x1, y1);
			v1 = v1.sub(x2, y2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#isInfinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isInfinite", args = {})
	public void testIsInfinite() {

		final long seed = SEED_PROVIDER.getSeedValue(18);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			assertFalse(v.isInfinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, y);
			assertTrue(v.isInfinite());

			v = new Vector2D(x, Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, y);
			assertTrue(v.isInfinite());

			v = new Vector2D(x, Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());
		}
	}

	/**
	 * Tests {@linkplain Vector2D#isNaN()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isNaN", args = {})
	public void testIsNaN() {

		final long seed = SEED_PROVIDER.getSeedValue(19);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			assertFalse(v.isNaN());

			v = new Vector2D(x, Double.NaN);
			assertTrue(v.isNaN());

			v = new Vector2D(Double.NaN, y);
			assertTrue(v.isNaN());

			v = new Vector2D(Double.NaN, Double.NaN);
			assertTrue(v.isNaN());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#isFinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isFinite", args = {})
	public void testIsFinite() {

		final long seed = SEED_PROVIDER.getSeedValue(20);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v = new Vector2D(x, y);
			assertTrue(v.isFinite());

			v = new Vector2D(Double.NaN, y);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, y);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, y);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#squareDistanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "squareDistanceTo", args = { Vector2D.class })
	public void testSquareDistanceTo() {

		final long seed = SEED_PROVIDER.getSeedValue(21);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

			double actual = v2.squareDistanceTo(v1);

			assertEquals(expected, actual, 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#reverse()}
	 */
	@Test
	@UnitTestMethod(name = "reverse", args = {})
	public void testReverse() {

		final long seed = SEED_PROVIDER.getSeedValue(22);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			Vector2D v1 = v.reverse();

			assertEquals(-x, v1.getX(), 0);
			assertEquals(-y, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#length()}
	 */
	@Test
	@UnitTestMethod(name = "length", args = {})
	public void testLength() {

		final long seed = SEED_PROVIDER.getSeedValue(23);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double expectedLength = FastMath.sqrt(x * x + y * y);
			double actualLength = v.length();

			assertEquals(expectedLength, actualLength, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#squareLength()}
	 */
	@Test
	@UnitTestMethod(name = "squareLength", args = {})
	public void testSquareLength() {

		final long seed = SEED_PROVIDER.getSeedValue(24);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double expectedLength = x * x + y * y;
			double actualLength = v.squareLength();

			assertEquals(expectedLength, actualLength, 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#toArray()}
	 */
	@Test
	@UnitTestMethod(name = "toArray", args = {})
	public void testToArray() {

		final long seed = SEED_PROVIDER.getSeedValue(25);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double[] array = v.toArray();

			assertEquals(v.getX(), array[0], 0);
			assertEquals(v.getY(), array[1], 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#normalize()}
	 */
	@Test
	@UnitTestMethod(name = "normalize", args = {})
	public void testNormalize() {

		final long seed = SEED_PROVIDER.getSeedValue(26);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			Vector2D v1 = v.normalize();

			assertEquals(1, v1.length(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#equals(Object))}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {

		final long seed = SEED_PROVIDER.getSeedValue(27);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(x, y);

			Vector2D v3 = new Vector2D(x, y);

			// reflexive
			assertEquals(v1, v1);

			// symetric
			assertEquals(v1, v2);
			assertEquals(v2, v1);

			// transitive
			assertEquals(v1, v2);
			assertEquals(v2, v3);
			assertEquals(v3, v1);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		final long seed = SEED_PROVIDER.getSeedValue(28);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(x, y);

			assertEquals(v1.hashCode(), v2.hashCode());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		final long seed = SEED_PROVIDER.getSeedValue(14);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			String expected = "Vector2D [x=" + x + ", y=" + y + "]";

			String actual = v.toString();

			assertEquals(expected, actual);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#rotate(double)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "rotate", args = { double.class })
	public void testRotate() {
		final long seed = SEED_PROVIDER.getSeedValue(15);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {
			// ensure that v1 is not too close to a zero vector
			Vector2D v1 = new Vector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector2D(x1, y1);
			}

			// Copy v1 and rotate the copy
			Vector2D v2 = new Vector2D(v1);
			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;
			v2 = v2.rotate(theta);

			// v1 under rotation should have its length preserved
			assertEquals(v1.length(), v2.length(), TOLERANCE);

			// v2 and v1 should have theta as their angle
			double expectedAngle = theta;
			while (expectedAngle < 0) {
				expectedAngle += 2 * FastMath.PI;
			}
			while (expectedAngle > FastMath.PI) {
				expectedAngle = 2 * FastMath.PI - expectedAngle;
			}
			assertEquals(expectedAngle, v2.angle(v1), TOLERANCE);

			// v2 and v1 should be oriented correctly
			if (theta < FastMath.PI) {
				assertEquals(1, v1.cross(v2));
			} else {
				assertEquals(-1, v1.cross(v2));
			}

			// v2 when rotated back should return to its original position
			Vector2D v3 = v2.rotate(-theta);
			assertEquals(v1.getX(), v3.getX(), TOLERANCE);
			assertEquals(v1.getY(), v3.getY(), TOLERANCE);
		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#rotateToward(Vector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "rotateToward", args = { Vector2D.class, double.class })
	public void testRotateToward() {

		final long seed = SEED_PROVIDER.getSeedValue(11);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			// Ensure that v1 is not too close to the zero vector
			Vector2D v1 = new Vector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector2D(x1, y1);
			}

			// Ensure that v2 is not too close to the zero vector
			Vector2D v2 = new Vector2D();
			while (v2.length() < TOLERANCE) {
				double x2 = randomGenerator.nextDouble() * 1000 - 500;
				double y2 = randomGenerator.nextDouble() * 1000 - 500;
				v2 = new Vector2D(x2, y2);
			}

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			Vector2D v3 = new Vector2D(v2);
			v3.rotateToward(v1, theta);

			// Rotation toward another vector is equivalent to plain rotation
			// with a possible sign change due to the relative orientation of
			// the two vectors.
			Vector2D v4 = new Vector2D(v2);
			if (v2.cross(v1) > 0) {
				v4.rotate(theta);
			} else {
				v4.rotate(-theta);
			}

			assertEquals(v4.getX(), v3.getX(), TOLERANCE);
			assertEquals(v4.getY(), v3.getY(), TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain Vector2D#perpendicularRotation(Chirality)}
	 */
	@Test
	@UnitTestMethod(name = "perpendicularRotation", args = { Chirality.class })
	public void testPerpendicularRotation() {

		final long seed = SEED_PROVIDER.getSeedValue(16);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v2 = new Vector2D(x2, y2);

			v2 = v1.perpendicularRotation(Chirality.LEFT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points up
			assertEquals(1, v2.cross(v1));

			v2 = v1.perpendicularRotation(Chirality.RIGHT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points down
			assertEquals(-1, v2.cross(v1));

		}

	}

	/**
	 * Tests {@linkplain Vector2D#isNormal()}
	 */
	@Test
	@UnitTestMethod(name = "isNormal", args = {})
	public void testIsNormal() {

		final long seed = SEED_PROVIDER.getSeedValue(8);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		int activeTestCount = 0;
		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			if (FastMath.abs(v.length() - 1) > MutableVector3D.NORMAL_LENGTH_TOLERANCE) {
				v = v.normalize();
				assertTrue(v.isNormal());
				activeTestCount++;

				Vector2D u = v.scale(1 - 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());

				u = v.scale(1 + 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());
			}
		}
		assertTrue(activeTestCount > 90);
	}

	/**
	 * Tests {@linkplain Vector2D#isPerpendicularTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "isPerpendicularTo", args = { Vector2D.class })
	public void testIsPerpendicularTo() {

		final long seed = SEED_PROVIDER.getSeedValue(4);
		RandomGenerator randomGenerator = getRandomGenerator(seed);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			Vector2D v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.PI / 2 - 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.PI / 2 + 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}
	}
}
