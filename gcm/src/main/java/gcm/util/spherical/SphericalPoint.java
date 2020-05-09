package gcm.util.spherical;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.earth.ECC;
import gcm.util.vector.NonNormalVectorException;
import gcm.util.vector.Vector3D;
import net.jcip.annotations.Immutable;

/**
 * Represents an immutable point on the unit sphere. Instances are created via
 * included builder class.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.REQUIRED)	
public final class SphericalPoint {

	/*
	 * Hidden constructor.
	 *
	 * @throws NonNormalVectorException
	 */
	private SphericalPoint(Scaffold scaffold) {
		this.coordinates = scaffold.coordinates;

		Vector3D v = new Vector3D(scaffold.coordinates[0], scaffold.coordinates[1], scaffold.coordinates[2]);
		v.normalize();

		if (!v.isNormal()) {
			throw new NonNormalVectorException("data cannot be normalized onto the unit sphere");
		}

		coordinates[0] = v.getX();
		coordinates[1] = v.getY();
		coordinates[2] = v.getZ();
	}

	/*
	 * static class for containing contributed values
	 */
	private static class Scaffold {
		double[] coordinates = new double[3];
	}

	private final double[] coordinates;

	/**
	 * Returns the coordinate value of the given index. Valid values are 0,1,2
	 */
	public double getCoordinate(int index) {
		return coordinates[index];
	}

	/**
	 * Returns a new builder instance for {@link SphericalPoint}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for {@link SphericalPoint}
	 */
	public static class Builder {

		/*
		 * Hidden constructor
		 */
		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Builds the {@link SphericalPoint} from the contributed values.
		 * 
		 * @throws NonNormalVectorException
		 *             <li>if the contributed data cannot be normalized to a
		 *             unit vector
		 */
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

		/**
		 * Sets the values of the coordinates of the {@link SphericalPoint} from
		 * the {@link Vector3D} in x, y, z order.
		 */
		public Builder fromVector3D(Vector3D vector3d) {
			scaffold.coordinates[0] = vector3d.getX();
			scaffold.coordinates[1] = vector3d.getY();
			scaffold.coordinates[2] = vector3d.getZ();
			return this;
		}

		/**
		 * Sets the values of the coordinates of the {@link SphericalPoint} from
		 * the {@link ECC} in x, y, z order.
		 */
		public Builder fromECC(ECC ecc) {
			scaffold.coordinates[0] = ecc.getX();
			scaffold.coordinates[1] = ecc.getY();
			scaffold.coordinates[2] = ecc.getZ();
			return this;
		}

	}

	/**
	 * Returns this {@link SphericalPoint} as a {@link Vector3D}
	 */
	public Vector3D toVector3D() {
		return new Vector3D(coordinates[0], coordinates[1], coordinates[2]);
	}
	
	/**
	 * Returns this {@link SphericalPoint} as an array
	 */
	public double[] toArray() {
		return new double[] {coordinates[0], coordinates[1], coordinates[2]};
	}

	/**
	 * Returns true if and only if the corresponding
	 * {@link Vector3D#isInfinite()} is true.
	 */
	public boolean isInfinite() {
		return Double.isInfinite(coordinates[0]) || Double.isInfinite(coordinates[1]) || Double.isInfinite(coordinates[2]);
	}

	/**
	 * Returns true if and only if the corresponding {@link Vector3D#isNaN()} is
	 * true.
	 */
	public boolean isNaN() {
		return Double.isNaN(coordinates[0]) || Double.isNaN(coordinates[1]) || Double.isNaN(coordinates[2]);
	}

	/**
	 * Returns true if and only if the corresponding {@link Vector3D#isFinite()}
	 * is true.
	 */
	public boolean isFinite() {
		return toVector3D().isFinite();
	}

	/**
	 * Returns true if and only if the corresponding {@link Vector3D#isNormal()}
	 * is normal.
	 */
	public boolean isNormal() {
		return toVector3D().isNormal();
	}

}
