package gcm.util.spherical;

import org.apache.commons.math3.util.FastMath;

import gcm.util.earth.ECC;
import gcm.util.vector.Vector3D;

/**
 * Represents a point on a unit sphere
 *
 * @author Shawn Hatch
 *
 */
public final class SphericalPoint {

	private SphericalPoint(Scaffold scaffold) {
		this.coordinates = scaffold.coordinates;

		Vector3D v = new Vector3D(scaffold.coordinates[0], scaffold.coordinates[1], scaffold.coordinates[2]);
		v.normalize();

		coordinates[0] = v.getX();
		coordinates[1] = v.getY();
		coordinates[2] = v.getZ();
	}

	private static class Scaffold {
		double[] coordinates = new double[3];
	}

	private final double[] coordinates;

	/**
	 * Returns the cartesian(x,y,z) coordinates corresponding to the
	 * (polar,azimuth) coordinates
	 * 
	 * @param index
	 * @return
	 */
	public double getCoordinate(int index) {
		return coordinates[index];
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		public SphericalPoint build() {
			try {
				return new SphericalPoint(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Set the coordinate value for the given index. Coordinate values are
		 * used in the build process via unit normalization, so any non-zero
		 * vector may be used to build by coordinates.
		 * 
		 * @param index
		 * @param value
		 */
		public Builder setCoordinate(int index, double value) {
			scaffold.coordinates[index] = value;
			return this;
		}

		public Builder fromVector3D(Vector3D vector3d) {
			scaffold.coordinates[0] = vector3d.getX();
			scaffold.coordinates[1] = vector3d.getY();
			scaffold.coordinates[2] = vector3d.getZ();
			return this;
		}

		public Builder fromECC(ECC ecc) {
			scaffold.coordinates[0] = ecc.getX();
			scaffold.coordinates[1] = ecc.getY();
			scaffold.coordinates[2] = ecc.getZ();
			return this;
		}

	}

	public Vector3D toVector3D() {
		return new Vector3D(coordinates[0], coordinates[1], coordinates[2]);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are positive or
	 * negative infinity.
	 * 
	 * @return <tt>true</tt> if the vector is infinite.
	 */
	public boolean isInfinite() {
		return Double.isInfinite(coordinates[0]) || Double.isInfinite(coordinates[1]) || Double.isInfinite(coordinates[2]);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are NaN 'Not a
	 * Number'.
	 * 
	 * @return <tt>true</tt> if the vector is NaN 'Not a Number'.
	 */
	public boolean isNaN() {
		return Double.isNaN(coordinates[0]) || Double.isNaN(coordinates[1]) || Double.isNaN(coordinates[2]);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are not NaN (i.e.
	 * 'Not a Number') and is not infinite.
	 * 
	 * @return <tt>true</tt> if the vector is not NaN (i.e. 'Not a Number') and
	 *         is not infinite.
	 */
	public boolean isRealValued() {
		return !isNaN() && !isInfinite();
	}

	public boolean isNormal() {
		double sum = 0;
		for (int i = 0; i < 3; i++) {
			sum += coordinates[i];
		}
		return isRealValued() && FastMath.abs(sum - 1) < 1E-13;
	}

}
