package gcm.util.spherical;

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
		return new Vector3D(coordinates[0],coordinates[1],coordinates[2]);
	}

}
