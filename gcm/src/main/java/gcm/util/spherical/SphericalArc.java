package gcm.util.spherical;

import org.apache.commons.math3.util.FastMath;

import gcm.util.vector.Vector3D;

/**
 * Represents a great arc segment on the unit sphere defined by two distinct
 * SphericalPoints.
 * 
 * @author Shawn Hatch
 *
 */
public class SphericalArc {

	private SphericalArc(Scaffold scaffold) {

		sphericalPoints = scaffold.sphericalPoints;
		for (int i = 0; i < 3; i++) {
			int j = (i + 1) % 3;
			int k = (i + 2) % 3;
			double value = sphericalPoints[0].getCoordinate(j) * sphericalPoints[1].getCoordinate(k) - sphericalPoints[0].getCoordinate(k) * sphericalPoints[1].getCoordinate(j);
			perp[i] = value;

		}
		double value = 0;
		for (int i = 0; i < 3; i++) {
			value += sphericalPoints[0].getCoordinate(i) * sphericalPoints[1].getCoordinate(i);
		}
		value = FastMath.min(value, 1);
		value = FastMath.max(value, -1);
		length = FastMath.acos(value);

	}

	public static class Builder {

		private Builder() {

		}

		public Builder setSphereicalPoint(int index, SphericalPoint sphericalPoint) {
			scaffold.sphericalPoints[index] = sphericalPoint;
			return this;
		}

		private Scaffold scaffold = new Scaffold();

		public SphericalArc build() {
			try {
				return new SphericalArc(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private static class Scaffold {
		SphericalPoint[] sphericalPoints = new SphericalPoint[2];
	}

	private final SphericalPoint[] sphericalPoints;

	/**
	 * Returns the spin of a SphericalPoint relative to this SphericalArc in the
	 * natural order of its SphericalPoints.
	 * 
	 * @param sphericalPoint
	 * @return
	 */
	public Spin getSpin(SphericalPoint sphericalPoint) {

		double dot = 0;
		for (int i = 0; i < 3; i++) {
			dot += perp[i] * sphericalPoint.getCoordinate(i);
		}
		if (dot >= 0) {
			return Spin.RIGHT_HANDED;
		}
		return Spin.LEFT_HANDED;
	}

	private final double length;

	private final double[] perp = new double[3];

	/**
	 * Returns true if and only if this SphericalArc intersects the given
	 * SphericalArc at a point
	 * 
	 * @param arc
	 * @return
	 */
	public boolean intersectsArc(SphericalArc arc) {
		Spin spin1 = getSpin(arc.getSphericalPoint(0));
		Spin spin2 = getSpin(arc.getSphericalPoint(1));
		Spin spin3 = arc.getSpin(getSphericalPoint(0));
		Spin spin4 = arc.getSpin(getSphericalPoint(1));
		return (spin1 != spin2) && (spin3 != spin4);
	}

	/**
	 * Returns the SphericalPoint associated with the index
	 * 
	 * @param index
	 *            Values may be 0 or 1
	 * @return
	 * @throws RuntimeException
	 *             if any other index is used
	 */
	public SphericalPoint getSphericalPoint(int index) {
		return sphericalPoints[index];
	}

	/**
	 * Returns the arc length of this SphericalArc
	 * 
	 * @return
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the intersection with the given SphericalArc. If no intersection
	 * exists, returns null
	 * 
	 * @param arc
	 * @return
	 */
	public SphericalPoint getInterSection(SphericalArc arc) {

		Vector3D a = new Vector3D();
		a.setX(sphericalPoints[0].getCoordinate(0));
		a.setY(sphericalPoints[0].getCoordinate(1));
		a.setZ(sphericalPoints[0].getCoordinate(2));

		Vector3D b = new Vector3D();
		b.setX(sphericalPoints[1].getCoordinate(0));
		b.setY(sphericalPoints[1].getCoordinate(1));
		b.setZ(sphericalPoints[1].getCoordinate(2));

		Vector3D c = new Vector3D();
		c.setX(arc.getSphericalPoint(0).getCoordinate(0));
		c.setY(arc.getSphericalPoint(0).getCoordinate(1));
		c.setZ(arc.getSphericalPoint(0).getCoordinate(2));

		Vector3D d = new Vector3D();
		d.setX(arc.getSphericalPoint(1).getCoordinate(0));
		d.setY(arc.getSphericalPoint(1).getCoordinate(1));
		d.setZ(arc.getSphericalPoint(1).getCoordinate(2));

		Vector3D p = new Vector3D(a);
		p.cross(b);

		// po(a+sb) = po(c+td)

		double k = -p.dot(c) / p.dot(d);
		if ((k < 0) || (k > 1)) {
			return null;
		}

		p.assign(c);
		p.cross(d);

		k = -p.dot(a) / p.dot(b);
		if ((k < 0) || (k > 1)) {
			return null;
		}

		b.scale(k);
		b.add(a);
		b.normalize();

		double polar = FastMath.asin(b.getZ());
		double cosPolar = FastMath.sqrt(1 - b.getZ() * b.getZ());
		double cosAzimuth = b.getX() / cosPolar;
		cosAzimuth = FastMath.min(1, cosAzimuth);
		cosAzimuth = FastMath.max(-1, cosAzimuth);
		double sinAzimuth = b.getY() / cosPolar;
		sinAzimuth = FastMath.min(1, sinAzimuth);
		sinAzimuth = FastMath.max(-1, sinAzimuth);
		double azimuth = FastMath.acos(cosAzimuth);
		if (FastMath.asin(sinAzimuth) < 0) {
			azimuth *= -1;
		}

		SphericalPoint.Builder sphericalPointBuilder = SphericalPoint.builder();
		sphericalPointBuilder.setCoordinate(0, FastMath.cos(polar) * FastMath.cos(azimuth));
		sphericalPointBuilder.setCoordinate(1, FastMath.cos(polar) * FastMath.sin(azimuth));
		sphericalPointBuilder.setCoordinate(2, FastMath.sin(polar));
		return sphericalPointBuilder.build();
	}

	/**
	 * Return the distance from the given {@linkplain SphericalPoint} to this
	 * {@linkplain SphericalArc}
	 */
	public double distanceTo(SphericalPoint sphericalPoint) {
		Vector3D a = sphericalPoints[0].toVector3D();
		Vector3D b = sphericalPoints[1].toVector3D();

		/*
		 * Create a normal to the plane containing the two end points --
		 */
		Vector3D p = new Vector3D(a);
		p.cross(b);

		/*
		 * Create a vector3D for the input
		 */
		Vector3D q = sphericalPoint.toVector3D();

		/*
		 * Calculate the angle to rotate toward p such that q will move onto the
		 * plane containing the end points
		 */
		double angle = q.angle(p) - FastMath.PI / 2;

		/*
		 * Create a new vector3D that is q rotated onto the plane containing the
		 * end points
		 */
		Vector3D qOnPlane = new Vector3D(q);
		qOnPlane.rotateToward(p, angle);

		/*
		 * If q lies between the end points, then return angle, otherwise return
		 * the angle between q and the closest end point.
		 */

		Vector3D v = new Vector3D(qOnPlane);
		v.cross(a);
		double aDot = v.dot(p);// positive if clockwise from a
		if (aDot <= 0) {
			v.assign(qOnPlane);
			v.cross(b);
			double bDot = v.dot(p);// positive if clockwise from b
			if (bDot >= 0) {
				/*
				 * qOnPlane, from the perspective of the normal, is CCW from A
				 * and CW from B, so it lies between them
				 */
				
				return -angle;
			}
		}
		

		/*
		 * q's projection onto the plane does not lie between the end points
		 */

		return FastMath.min(q.angle(a), q.angle(b));

	}

}
