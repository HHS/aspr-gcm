package gcm.util.spherical;

import org.apache.commons.math3.util.FastMath;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.vector.MutableVector3D;
import gcm.util.vector.Vector3D;
import net.jcip.annotations.Immutable;

/**
 * Represents an immutable great arc segment on the unit sphere defined by two
 * distinct SphericalPoints. Instances are created via included builder class.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
@Source(status = TestStatus.REQUIRED)
public class SphericalArc {

	/**
	 * Hidden constructor
	 * 
	 * @throws MalformedSphericalArcException
	 *             <li>if the two {@link SphericalPoint} values are too close
	 *             together to properly form a normal vector.
	 * 
	 * @throws NullPointerException
	 *             <li>if either {@link SphericalPoint} is null
	 */
	public SphericalArc(SphericalPoint sphericalPoint1, SphericalPoint sphericalPoint2) {

		if (sphericalPoint1 == null) {
			throw new NullPointerException("null spherical point contributed to build of spherical arc");
		}

		if (sphericalPoint2 == null) {
			throw new NullPointerException("null spherical point contributed to build of spherical arc");
		}

		sphericalPoints = new SphericalPoint[] { sphericalPoint1, sphericalPoint2 };
		perp = sphericalPoint1.getPosition().cross(sphericalPoint2.getPosition()).normalize();
		if (!perp.isNormal()) {
			throw new NullPointerException("spherical points too similar to form arc");
		}
		if (!perp.isPerpendicularTo(sphericalPoint1.getPosition())) {
			throw new NullPointerException("spherical points too similar to form arc");
		}
		if (!perp.isPerpendicularTo(sphericalPoint2.getPosition())) {
			throw new NullPointerException("spherical points too similar to form arc");
		}
		length = sphericalPoint1.getPosition().angle(sphericalPoint2.getPosition());
	}

	private final SphericalPoint[] sphericalPoints;

	/**
	 * Returns the {@link Chirality} of a SphericalPoint relative to this
	 * SphericalArc in the natural order of its SphericalPoints.
	 * 
	 * @param sphericalPoint
	 * @return
	 */
	public Chirality getChirality(SphericalPoint sphericalPoint) {

		double dot = perp.dot(sphericalPoint.getPosition());

		if (dot >= 0) {
			return Chirality.RIGHT_HANDED;
		}
		return Chirality.LEFT_HANDED;
	}

	private final double length;

	private final Vector3D perp;

	/**
	 * Returns true if and only if this SphericalArc intersects the given
	 * SphericalArc at a point
	 * 
	 * @param arc
	 * @return
	 */
	public boolean intersectsArc(SphericalArc arc) {
		Chirality chirality1 = getChirality(arc.getSphericalPoint(0));
		Chirality chirality2 = getChirality(arc.getSphericalPoint(1));
		Chirality chirality3 = arc.getChirality(getSphericalPoint(0));
		Chirality chirality4 = arc.getChirality(getSphericalPoint(1));
		return (chirality1 != chirality2) && (chirality3 != chirality4);
	}

	/**
	 * Returns the SphericalPoint associated with the index
	 * 
	 * @param index
	 *            Values may be 0 or 1
	 * 
	 * @throws IndexOutOfBoundsException
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

		Vector3D a = sphericalPoints[0].getPosition();
		Vector3D b = sphericalPoints[1].getPosition();

		Vector3D c = arc.getSphericalPoint(0).getPosition();
		Vector3D d = arc.getSphericalPoint(0).getPosition();

		MutableVector3D p = new MutableVector3D(a);
		p.cross(b);

		// p o(a+sb) = p o(c+td)

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

		return new SphericalPoint(new Vector3D(FastMath.cos(polar) * FastMath.cos(azimuth), FastMath.cos(polar) * FastMath.sin(azimuth), FastMath.sin(polar)));
	}

	/**
	 * Returns the distance from the given {@linkplain SphericalPoint} to this
	 * {@linkplain SphericalArc}
	 */
	public double distanceTo(SphericalPoint sphericalPoint) {
		Vector3D a = sphericalPoints[0].getPosition();
		Vector3D b = sphericalPoints[1].getPosition();

		/*
		 * Create a normal to the plane containing the two end points --
		 */
		MutableVector3D p = new MutableVector3D(a);
		p.cross(b);

		/*
		 * Create a vector3D for the input
		 */
		MutableVector3D q = new MutableVector3D(sphericalPoint.getPosition());

		/*
		 * Calculate the angle to rotate toward p such that q will move onto the
		 * plane containing the end points
		 */
		double angle = q.angle(p) - FastMath.PI / 2;

		/*
		 * Create a new vector3D that is q rotated onto the plane containing the
		 * end points
		 */
		MutableVector3D qOnPlane = new MutableVector3D(q);
		qOnPlane.rotateToward(p, angle);

		/*
		 * If q lies between the end points, then return angle, otherwise return
		 * the angle between q and the closest end point.
		 */

		MutableVector3D v = new MutableVector3D(qOnPlane);
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
