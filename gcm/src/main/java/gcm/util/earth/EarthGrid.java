package gcm.util.earth;

import org.apache.commons.math3.util.FastMath;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.vector.Vector2D;
import gcm.util.vector.Vector3D;

/**
 * A utility class for converting (x,y) two dimensional grid coordinates to an
 * from (lat,lon) coordinates. The grid is constructed at a particular (lat,
 * lon) position with an azimuth for grid orientation. The resulting grid is a
 * good approximation to the earth's surface for several kilometers. The earth
 * is approximated by a sphere using the WGS84 earth radius for the latitude of
 * the center of the grid.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status= TestStatus.UNEXPECTED)
public final class EarthGrid {

	public static final double MIN_ANGLE_FROM_POLE = 0.001;

	private final Earth earth;

	private Vector3D x;

	private Vector3D y;

	private Vector3D z;

	private Vector3D c;

	/**
	 * Constructs a new EarthGrid centered at the given LatLon where the (x,y)
	 * grid is right handed from the perspective of an observer above the earth.
	 * The positive y axis is aligned to the azimth. For example, an azimuth of
	 * 0 degrees will align the y axis to the north and an azimuth of 90 degrees
	 * will align the positive y axis to the east.
	 * 
	 * @throws RuntimeException
	 *             <li>if the center point is closer than
	 *             EarthGrid.MIN_ANGLE_FROM_POLE=0.001 degrees from one of the
	 *             poles.
	 * 
	 * @param center
	 * @param azimuthDegrees
	 */
	public EarthGrid(LatLon center, double azimuthDegrees) {
		if ((FastMath.abs(center.getLatitude()) - 90) < MIN_ANGLE_FROM_POLE) {
			throw new RuntimeException("Grid cannot be constructed within " + MIN_ANGLE_FROM_POLE + " degrees of a pole");
		}
		earth = Earth.fromLatitude(center.getLatitude());
		c = earth.getECCFromLatLon(center).toVector3D();
		z = new Vector3D(c);
		z.normalize();
		x = new Vector3D(0, 0, 1);
		x.cross(z);
		x.normalize();
		x.rotateAbout(z, -FastMath.toRadians(azimuthDegrees));
		y = new Vector3D(z);
		y.cross(x);
		y.normalize();
	}

	public Vector2D getCartesian2DCoordinate(LatLon latLon) {
		Vector3D v = earth.getECCFromLatLonAlt(new LatLonAlt(latLon)).toVector3D();
		v.normalize();
		v.scale(Earth.getEffectiveEarthRadius(latLon.getLatitude()));
		v.sub(c);
		return new Vector2D(v.dot(x), v.dot(y));
	}

	public LatLon getLatLon(Vector2D xyCoordinate) {
		double zlength = FastMath.sqrt(earth.getRadius() * earth.getRadius() - xyCoordinate.getX() * xyCoordinate.getX() - xyCoordinate.getY() * xyCoordinate.getY()) - earth.getRadius();
		Vector3D planarPosition = new Vector3D(c);
		planarPosition.addScaled(y, xyCoordinate.getY());
		planarPosition.addScaled(x, xyCoordinate.getX());
		planarPosition.addScaled(z, zlength);

		ECC ecc = new ECC(planarPosition);
		LatLonAlt latLonAlt = earth.getLatLonAlt(ecc);
		LatLon latLon = new LatLon(latLonAlt);
		return latLon;
	}
}
