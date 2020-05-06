package gcm.util.earth;

import gcm.util.annotations.Source;
import gcm.util.vector.Vector3D;
import net.jcip.annotations.Immutable;

/**
 * Earth Centered Coordinate : Represents an immutable, absolute Cartesian
 * vector from the center of a spherical earth measured in meters.
 * 
 * 
 * (0,0,0) is the center of the Earth
 * 
 * (0,0,1) is a vector pointing to the North Pole
 * 
 * (0,1,0) is a vector pointing to 0 degrees latitude and +90 degrees longitude
 * 
 * (1,0,0) is a vector pointing to 0 degrees latitude and 0 degrees longitude
 * 
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source
public class ECC {

	private final double x;

	private final double y;

	private final double z;

	/**
	 * Returns the x coordinate of this ECC
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y coordinate of this ECC
	 */
	public double getY() {
		return y;
	}

	/**
	 * Returns the z coordinate of this ECC
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Constructs an ECC from the given values
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public ECC(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Constructs an ECC from the given vector
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * 
	 * @throws RuntimeException
	 *             <li>if the vector3D is null
	 * 
	 */
	public ECC(Vector3D vector3D) {
		if (vector3D == null) {
			throw new RuntimeException("null vector");
		}
		this.x = vector3D.getX();
		this.y = vector3D.getY();
		this.z = vector3D.getZ();
	}

	/**
	 * Returns a Vector3D from x, y, and z
	 */
	public Vector3D toVector3D() {
		return new Vector3D(x, y, z);
	}

	/**
	 * Returns a double array of x, y and z
	 */
	public double[] toArray() {
		return new double[] { x, y, z };
	}

	/**
	 * Returns a string of the form ECC [x=234234435.4, y=234643466.1,
	 * z=-534534566.6]
	 */
	@Override
	public String toString() {
		return "ECC [x=" + x + ", y=" + y + ", z=" + z + "]";
	}

	/**
	 * Returns a hash code consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns true if and only if this ECC is compared to another non-null
	 * instance of ECC with matching x, y and z values
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ECC other = (ECC) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

}
