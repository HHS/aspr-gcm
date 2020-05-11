package gcm.util.vector;

import org.apache.commons.math3.util.FastMath;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * A immutable 2-dimensional vector class supporting common 2D transforms.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED)
public final class Vector2D {

	private static final long zeroLongBits = Double.doubleToLongBits(0);

	/*
	 * corrects a value that should be in the interval [-1,1]
	 */
	private static double crunch(final double value) {

		if (value > 1) {
			return 1;
		}
		if (value < -1) {
			return -1;
		}
		return value;
	}

	/*
	 * A function that masks Double.doubleToLongBits() by forcing the long bits
	 * representation of -0 to be that of +0.
	 */
	private static long toLongBits(final double value) {
		if (value == 0) {
			return zeroLongBits;
		} else {
			return Double.doubleToLongBits(value);
		}
	}

	private final double x;

	private final double y;

	/**
	 * Constructs a {@link Vector2D} where x and y are zero
	 */
	public Vector2D() {
		this.x = 0;
		this.y = 0;
	}

	/**
	 * Constructs a {@link Vector2D} with the given x and y values
	 */
	public Vector2D(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructs a {@link Vector2D} from another Vector2D
	 * 
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public Vector2D(final Vector2D v) {
		x = v.getX();
		y = v.getY();
	}

	/**
	 * Adds the given x and y values to the corresponding values of this
	 * {@link Vector2D}
	 */
	public Vector2D add(final double x, final double y) {
		return new Vector2D(this.x + x, this.y + y);
	}

	/**
	 * Adds the x and y of the given {@link Vector2D} to the corresponding
	 * values of this {@link Vector2D}
	 * 
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public Vector2D add(final Vector2D v) {
		return new Vector2D(x + v.x, y + v.y);
	}

	/**
	 * Add the given {@link Vector2D} as scaled by the scalar to this
	 * {@link Vector2D}
	 * 
	 * For example, if v=(5,7) and w = (2,3), then v.addScaled(w,10) would yield
	 * v=(25,37)
	 * 
	 */
	public Vector2D addScaled(final Vector2D v, final double scalar) {
		return new Vector2D(x + v.getX() * scalar, y + v.getY() * scalar);
	}

	/**
	 * Returns the angle in radians between this {@link Vector2D} and the given
	 * {@link Vector2D}
	 */
	public double angle(final Vector2D v) {
		final double value = dot(v) / (v.length() * length());
		return FastMath.acos(crunch(value));
	}

	/**
	 * Returns 1 if the acute angle from this {@link Vector2D} to the given
	 * Vector2D is clockwise and -1 if it is counter clockwise. Returns 0 if the
	 * angle is zero.
	 */
	public int cross(final Vector2D v) {
		final double direction = (x * v.y) - (v.x * y);
		if (direction < 0) {
			return -1;
		}
		if (direction > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns the distance between this {@link Vector2D} and the given Vector2D
	 */
	public double distanceTo(final Vector2D v) {
		return FastMath.sqrt(((x - v.x) * (x - v.x)) + ((y - v.y) * (y - v.y)));
	}

	/**
	 * Returns the dot product of this {@link Vector2D} and the given Vector2D
	 */
	public double dot(final Vector2D v) {
		return (x * v.x) + (y * v.y);
	}

	/**
	 * Equals contract of {@link Vector2D}. Two vectors a and b are equal if
	 * their x and y values convert to long bits in the same way. An exception
	 * is made for -0, which is calculated as if its long bits were representing
	 * +0. This is done to give a more intuitive meaning of equals.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Vector2D other = (Vector2D) obj;
		if (toLongBits(x) != toLongBits(other.x)) {
			return false;
		}
		if (toLongBits(y) != toLongBits(other.y)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the value at the given index: 0->x, 1->y
	 */
	public double get(final int index) {
		switch (index) {
		case 0:
			return x;
		case 1:
			return y;
		default:
			throw new RuntimeException("index out of bounds " + index);
		}
	}

	/**
	 * Returns the x component value of this {@link Vector2D}
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y component value of this {@link Vector2D}
	 */
	public double getY() {
		return y;
	}

	/**
	 * Returns a hash code consistent with the equals contract
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = toLongBits(x);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = toLongBits(y);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are positive or
	 * negative infinity.
	 *
	 * @return <tt>true</tt> if the vector is infinite.
	 */
	public boolean isInfinite() {
		return Double.isInfinite(x) || Double.isInfinite(y);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are NaN 'Not a
	 * Number'.
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are not NaN (i.e.
	 * 'Not a Number') and not infinite.
	 */
	public boolean isRealValued() {
		return !isNaN() && !isInfinite();
	}

	/**
	 * Returns the length of this {@link Vector2D}
	 */
	public double length() {
		return FastMath.sqrt((x * x) + (y * y));
	}

	/**
	 * Scales this {@link Vector2D} so that its length is 1.
	 */
	public Vector2D normalize() {
		final double len = length();
		return new Vector2D(x / len, y / len);
	}

	/**
	 * Assigns this {@link Vector2D} to the values of the given {@link Vector2D}
	 * under either a clockwise or counter clockwise rotation.
	 */
	public Vector2D perpTo(final Vector2D v, final boolean clockwise) {
		double newx, newy;
		if (clockwise) {
			newx = v.getY();
			newy = -v.getX();
		} else {
			newx = -v.getY();
			newy = v.getX();
		}
		return new Vector2D(newx, newy);
	}

	/**
	 * Reverses the direction of the this Vector2D. This is equivalent to
	 * scaling by -1.
	 */
	public Vector2D reverse() {
		return scale(-1);
	}

	/**
	 * Rotates this {@link Vector2D} about the origin by the given angle in
	 * radians in a counter clockwise(right handed) manner.
	 */
	public Vector2D rotate(final double theta) {
		double sinTheta = FastMath.sin(theta);
		double cosTheta = FastMath.cos(theta);
		return new Vector2D(x * cosTheta - y * sinTheta, y * cosTheta + x * sinTheta);
	}

	/**
	 * Rotates this {@link Vector2D} about the origin through the acute angle to
	 * the given {@link Vector2D} by the given angle in radians.
	 */
	public Vector2D rotateToward(final Vector2D v, final double theta) {
		return rotate(cross(v) * theta);
	}

	/**
	 * Scales this {@link Vector2D} by the scalar
	 */
	public Vector2D scale(final double scalar) {
		return new Vector2D(x * scalar, y * scalar);
	}

	/**
	 * Returns the square distance between this {@link Vector2D} and the given
	 * Vector2D
	 */
	public double squareDistanceTo(final Vector2D v) {
		return ((x - v.x) * (x - v.x)) + ((y - v.y) * (y - v.y));
	}

	/**
	 * Returns the square length of this {@link Vector2D}
	 */
	public double squareLength() {
		return (x * x) + (y * y);
	}

	/**
	 * Subtracts the given x and y values from the corresponding values of this
	 * {@link Vector2D}
	 */
	public Vector2D sub(final double x, final double y) {
		return new Vector2D(this.x - x, this.y - y);
	}

	/**
	 * Subtracts the x and y of the given {@link Vector2D} from the
	 * corresponding values of this {@link Vector2D}
	 * 
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public Vector2D sub(final Vector2D v) {
		return new Vector2D(x - v.getX(), y - v.getY());
	}

	/**
	 * Returns a length 2 array of double [x,y]
	 */
	public double[] toArray() {
		return new double[] { x, y };
	}

	/**
	 * Returns the string representation in the form
	 * 
	 * Vector2D [x=2.57,y=-34.1]
	 */
	@Override
	public String toString() {
		return "Vector2D [x=" + x + ", y=" + y + "]";
	}
	
}
