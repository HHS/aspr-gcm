package gcm.util.earth;

import org.apache.commons.math3.util.FastMath;

import gcm.util.annotations.Source;
import gcm.util.vector.MutableVector3D;

/**
 * A spherical geo-model for converting various coordinate representations and
 * calculating ground ranges. Earth instances are created from approximations to
 * the WGS84 oblate earth.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public class Earth {

	public final static double WGS84_EQUATORIAL_RADIUS_METERS = 6378137;

	public final static double WGS84_POLAR_RADIUS_METERS = 6356752.314245;

	public final static double WGS84_MEAN_RADIUS_METERS = (2 * WGS84_EQUATORIAL_RADIUS_METERS + WGS84_POLAR_RADIUS_METERS) / 3;

	private double radius;

	/**
	 * Returns the radius of this Earth instance in meters
	 */
	public double getRadius() {
		return radius;
	}

	/*
	 * Hidden constructor.
	 */
	private Earth() {

	}

	/**
	 * Static constructor that returns an Earth instance having a radius of
	 * Earth.WGS84_MEAN_RADIUS_METERS
	 */
	public static Earth fromMeanRadius() {
		Earth result = new Earth();
		result.radius = WGS84_MEAN_RADIUS_METERS;
		return result;
	}

	/**
	 * Static constructor that returns an Earth instance having the given radius
	 */
	public static Earth fromRadius(double radiusMeters) {
		Earth result = new Earth();
		result.radius = radiusMeters;
		return result;
	}

	/**
	 * Static constructor that returns an Earth instance having a radius of the
	 * WGS84 earth at the given latitude.
	 */
	public static Earth fromLatitude(double latitudeDegrees) {
		Earth result = new Earth();
		result.radius = getEffectiveEarthRadius(latitudeDegrees);
		return result;
	}

	/**
	 * Returns the effective earth radius for the given latitude from the WGS84 oblate geo-model
	 */
	public static double getEffectiveEarthRadius(double latitudeDegrees) {
		// calculate the effective spherical earth radius for the given
		// latitude from the WGS-84 oblate earth
		final double lat = FastMath.toRadians(latitudeDegrees);
		return 1.0 / FastMath.sqrt(FastMath.pow(FastMath.cos(lat) / WGS84_EQUATORIAL_RADIUS_METERS, 2) + FastMath.pow(FastMath.sin(lat) / WGS84_POLAR_RADIUS_METERS, 2));
	}

	/**
	 * Returns the ground distance in meters between the two positions.
	 */
	public double getGroundDistanceFromECC(ECC position1, ECC position2) {
		MutableVector3D v1 = position1.toVector3D();
		MutableVector3D v2 = position2.toVector3D();
		return v1.angle(v2) * radius;
	}
	
	/**
	 * Returns the ground distance in meters between the two positions.
	 */
	public double getGroundDistanceFromLatLon(LatLon position1, LatLon position2) {
		MutableVector3D v1 = getECCFromLatLonAlt(new LatLonAlt(position1)).toVector3D();
		MutableVector3D v2 = getECCFromLatLonAlt(new LatLonAlt(position2)).toVector3D();
		return v1.angle(v2) * radius;
	}

	/**
	 * Returns the ground distance in meters between the two positions.
	 */
	public double getGroundDistanceFromLatLonAlt(LatLonAlt position1, LatLonAlt position2) {
		MutableVector3D v1 = getECCFromLatLonAlt(position1).toVector3D();
		MutableVector3D v2 = getECCFromLatLonAlt(position2).toVector3D();
		return v1.angle(v2) * radius;
	}

	/**
	 * Converts an ECC position into a LatLonAlt
	 */
	public LatLonAlt getLatLonAlt(ECC eCC) {
		MutableVector3D v = eCC.toVector3D();
		double alt = v.length() - radius;
		v.normalize();
		double z = v.getZ();
		double lat = FastMath.asin(z);
		double lon = FastMath.acos(crunch(v.getX() / FastMath.sqrt(1 - z * z)));
		if (v.getY() < 0) {
			lon *= -1;
		}
		lat = FastMath.toDegrees(lat);
		lon = FastMath.toDegrees(lon);

		return new LatLonAlt(lat, lon, alt);
	}

	/**
	 * Returns an ECC position from the given LatLonAlt
	 */
	public ECC getECCFromLatLonAlt(LatLonAlt latLonAlt) {
		double coslat = FastMath.cos(FastMath.toRadians(latLonAlt.getLatitude()));
		double coslon = FastMath.cos(FastMath.toRadians(latLonAlt.getLongitude()));
		double sinlat = FastMath.sin(FastMath.toRadians(latLonAlt.getLatitude()));
		double sinlon = FastMath.sin(FastMath.toRadians(latLonAlt.getLongitude()));
		double distance = radius + latLonAlt.getAltitude();
		MutableVector3D v = new MutableVector3D(coslat * coslon, coslat * sinlon, sinlat);
		v.scale(distance);
		return new ECC(v);
	}

	/**
	 * Returns an ECC position from the given LatLon
	 */
	public ECC getECCFromLatLon(LatLon latLon) {
		double coslat = FastMath.cos(FastMath.toRadians(latLon.getLatitude()));
		double coslon = FastMath.cos(FastMath.toRadians(latLon.getLongitude()));
		double sinlat = FastMath.sin(FastMath.toRadians(latLon.getLatitude()));
		double sinlon = FastMath.sin(FastMath.toRadians(latLon.getLongitude()));
		MutableVector3D v = new MutableVector3D(coslat * coslon, coslat * sinlon, sinlat);
		v.scale(radius);
		return new ECC(v);
	}

	private static double crunch(double value) {

		if (value > 1) {
			return 1;
		}
		if (value < -1) {
			return -1;
		}
		return value;
	}

}
