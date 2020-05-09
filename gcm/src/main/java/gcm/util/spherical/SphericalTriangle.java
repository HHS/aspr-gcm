package gcm.util.spherical;

import org.apache.commons.math3.util.FastMath;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.vector.MutableVector3D;
import net.jcip.annotations.Immutable;

/**
 * Represents an immutable triangle on the unit sphere.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
@Source(status = TestStatus.REQUIRED)
public class SphericalTriangle {

	/**
	 * Static builder class for {@link SphericalArc}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		/*
		 * Hidden constructor
		 */
		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Sets the {@link SphericalArc} for the given index.
		 */
		public Builder setSphericalPoint(int index, SphericalPoint sphericalPoint) {
			scaffold.sphericalPoints[index] = sphericalPoint;
			return this;
		}

		/**
		 * Returns the {@link SphericalTriangle} from the contributed
		 * {@link SphericalPoint} values
		 * 
		 * @throws NullPointerException
		 *             <li>if not all {@link SphericalPoint} values were
		 *             contributed or were null
		 * 
		 */
		public SphericalTriangle build() {
			try {
				return new SphericalTriangle(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	/**
	 * Returns the shortest distance from the point to this triangle. Points on
	 * the inside of the triangle will return 0.
	 */
	public double distanceTo(SphericalPoint sphericalPoint) {

		if (contains(sphericalPoint)) {
			return 0;
		}
		double result = Double.POSITIVE_INFINITY;
		for (SphericalArc sphericalArc : sphericalArcs) {
			result = FastMath.min(result, sphericalArc.distanceTo(sphericalPoint));
		}
		return result;
	}

	private static double getTangentialAngle(MutableVector3D v1, MutableVector3D v2, MutableVector3D v3) {
		MutableVector3D p = new MutableVector3D();
		p.assign(v1);
		p.cross(v2);
		p.cross(v2);

		MutableVector3D q = new MutableVector3D();
		q.assign(v3);
		q.cross(v2);
		q.cross(v2);

		return p.angle(q);
	}

	/*
	 * Hidden constructor
	 */
	private SphericalTriangle(Scaffold scaffold) {

		for (SphericalPoint sphericalPoint : scaffold.sphericalPoints) {
			if (sphericalPoint == null) {
				throw new NullPointerException("null/no spherical point contributed to build of spherical triangle");
			}
		}
		sphericalPoints = scaffold.sphericalPoints;
		sphericalArcs = new SphericalArc[3];

		SphericalArc.Builder sphereicalArcBuilder = SphericalArc.builder();
		for (int i = 0; i < 3; i++) {
			sphereicalArcBuilder.setSphereicalPoint(0, sphericalPoints[i]);
			sphereicalArcBuilder.setSphereicalPoint(1, sphericalPoints[(i + 1) % 3]);
			sphericalArcs[i] = sphereicalArcBuilder.build();
		}

		spin = sphericalArcs[0].getSpin(sphericalPoints[2]);

		final MutableVector3D v0 = sphericalPoints[0].toVector3D();
		final MutableVector3D v1 = sphericalPoints[1].toVector3D();
		final MutableVector3D v2 = sphericalPoints[2].toVector3D();

		final MutableVector3D perp = new MutableVector3D(v0);
		perp.cross(v1);
		final boolean leftHanded = perp.dot(v2) < 0;

		MutableVector3D midPoint = new MutableVector3D(v0);
		midPoint.add(v1);
		final MutableVector3D c = new MutableVector3D(v0);
		c.cross(v1);
		c.cross(midPoint);

		midPoint = new MutableVector3D(v1);
		midPoint.add(v2);
		final MutableVector3D d = new MutableVector3D(v1);
		d.cross(v2);
		d.cross(midPoint);

		c.cross(d);
		c.normalize();

		if (leftHanded) {
			c.reverse();
		}
		radius = c.distanceTo(v0);
		centroid = SphericalPoint.builder().fromVector3D(c).build();

		double alpha = getTangentialAngle(v0, v1, v2);
		double beta = getTangentialAngle(v1, v2, v0);
		double gamma = getTangentialAngle(v2, v0, v1);

		area = (alpha + beta + gamma) - FastMath.PI;

	}

	/**
	 * Returns a new builder instance for {@link SphericalTriangle}
	 */
	public static Builder builder() {
		return new Builder();
	}

	private static class Scaffold {
		SphericalPoint[] sphericalPoints = new SphericalPoint[3];
	}

	private final SphericalArc[] sphericalArcs;

	private final SphericalPoint[] sphericalPoints;

	private final SphericalPoint centroid;

	private final double radius;

	private final double area;

	private final Spin spin;
	
	
	/**
	 * Returns true if and only if the given {@link SphericalPoint} is in the
	 * interior or the arcs of this {@link SphericalTriangle}
	 */
	public boolean contains(SphericalPoint sphericalPoint) {
		for (int i = 0; i < 3; i++) {
			if (sphericalArcs[i].getSpin(sphericalPoint) != spin) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the area of this {@link SphericalTriangle}
	 */
	public double getArea() {
		return area;
	}

	/**
	 * Returns the centroid point of this {@link SphericalTriangle}
	 */
	public SphericalPoint getCentroid() {
		return centroid;
	}

	/**
	 * Returns the radius of this {@link SphericalTriangle}. The radius is
	 * defined as the distance to the centroid from the vertices.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Returns the spin of this {@link SphericalTriangle} relative to the
	 * natural order of its SphericalPoints.
	 */
	public Spin getSpin() {
		return spin;
	}

	/**
	 * Returns true if this {@link SphericalTriangle} overlaps with any part of
	 * the given {@link SphericalTriangle}
	 */
	public boolean intersects(SphericalTriangle sphericalTriangle) {
		for (int i = 0; i < 3; i++) {
			if (contains(sphericalTriangle.getSphericalpoint(i))) {
				return true;
			}
			if (sphericalTriangle.contains(getSphericalpoint(i))) {
				return true;
			}
		}

		for (int i = 0; i < 3; i++) {
			SphericalArc proximalArc =

					getSphericalArc(i);

			for (int j = 0; j < 3; i++) {
				SphericalArc distalArc = getSphericalArc(i);
				if (proximalArc.intersectsArc(distalArc)) {
					return true;
				}

			}

		}
		return false;
	}

	/**
	 * Returns the {@link SphericalArc} that corresponds to the index. These
	 * arcs are formed from the original {@link SphericalPoint} members that
	 * define this {@link SphericalTriangle}. Arc[0] is formed(in order) from
	 * Point[0] and Point[1]. Arc[1] is formed(in order) from Point[1] and
	 * Point[2]. Arc[2] is formed(in order) from Point[2] and Point[0].
	 * 
	 */
	public SphericalArc getSphericalArc(int index) {
		return sphericalArcs[index];
	}

	/**
	 * Returns the {@link SphericalPoint} that corresponds to the index and was
	 * used to construct this {@link SphericalTriangle}
	 */
	public SphericalPoint getSphericalpoint(int index) {
		return sphericalPoints[index];
	}

	/**
	 * Returns true if this {@link SphericalTriangle} intersects the given
	 * {@link SphericalArc}
	 */
	public boolean intersects(SphericalArc sphericalArc) {
		for (int i = 0; i < 2; i++) {
			if (contains(sphericalArc.getSphericalPoint(i))) {
				return true;
			}
		}

		for (int i = 0; i < 3; i++) {
			if (sphericalArcs[i].intersectsArc(sphericalArc)) {
				return true;
			}
		}

		return false;
	}
}
