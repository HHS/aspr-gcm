package gcm.util.spherical;

import org.apache.commons.math3.util.FastMath;

import gcm.util.vector.Vector3D;

public class SphericalTriangle {

	public static class Builder {
		private Builder() {

		}

		private Scaffold scaffold = new Scaffold();

		public Builder setSphericalPoint(int index, SphericalPoint sphericalPoint) {
			scaffold.sphericalPoints[index] = sphericalPoint;
			return this;
		}

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

	private double getTangentialAngle(Vector3D v1, Vector3D v2, Vector3D v3) {
		Vector3D p = new Vector3D();
		p.assign(v1);
		p.cross(v2);
		p.cross(v2);

		Vector3D q = new Vector3D();
		q.assign(v3);
		q.cross(v2);
		q.cross(v2);

		return p.angle(q);
	}

	private SphericalTriangle(Scaffold scaffold) {

		sphericalPoints = scaffold.sphericalPoints;
		sphericalArcs = new SphericalArc[3];

		SphericalArc.Builder sphereicalArcBuilder = SphericalArc.builder();
		for (int i = 0; i < 3; i++) {
			sphereicalArcBuilder.setSphereicalPoint(0, sphericalPoints[i]);
			sphereicalArcBuilder.setSphereicalPoint(1, sphericalPoints[(i + 1) % 3]);
			sphericalArcs[i] = sphereicalArcBuilder.build();
		}

		spin = sphericalArcs[0].getSpin(sphericalPoints[2]);
		Vector3D c = new Vector3D();
		for (int i = 0; i < 3; i++) {
			Vector3D v = new Vector3D();
			v.setX(sphericalPoints[i].getCoordinate(0));
			v.setY(sphericalPoints[i].getCoordinate(1));
			v.setZ(sphericalPoints[i].getCoordinate(2));
			v.normalize();
			c.add(v);
		}

		SphericalPoint.Builder sphericalPointBuilder = SphericalPoint.builder();
		sphericalPointBuilder.setCoordinate(0, c.getX());
		sphericalPointBuilder.setCoordinate(1, c.getY());
		sphericalPointBuilder.setCoordinate(2, c.getZ());
		centroid = sphericalPointBuilder.build();

		sphereicalArcBuilder.setSphereicalPoint(0, sphericalPoints[0]);
		sphereicalArcBuilder.setSphereicalPoint(1, centroid);
		SphericalArc sphericalArc = sphereicalArcBuilder.build();
		radius = sphericalArc.getLength();

		Vector3D v1 = new Vector3D();
		v1.setX(sphericalPoints[0].getCoordinate(0));
		v1.setY(sphericalPoints[0].getCoordinate(1));
		v1.setZ(sphericalPoints[0].getCoordinate(2));

		Vector3D v2 = new Vector3D();
		v2.setX(sphericalPoints[1].getCoordinate(0));
		v2.setY(sphericalPoints[1].getCoordinate(1));
		v2.setZ(sphericalPoints[1].getCoordinate(2));

		Vector3D v3 = new Vector3D();
		v3.setX(sphericalPoints[2].getCoordinate(0));
		v3.setY(sphericalPoints[2].getCoordinate(1));
		v3.setZ(sphericalPoints[2].getCoordinate(2));

		double alpha = getTangentialAngle(v1, v2, v3);
		double beta = getTangentialAngle(v2, v3, v1);
		double gamma = getTangentialAngle(v3, v1, v2);

		area = (alpha + beta + gamma) - FastMath.PI;

	}

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

	public boolean contains(SphericalPoint sphericalPoint) {
		for (int i = 0; i < 3; i++) {
			if (sphericalArcs[i].getSpin(sphericalPoint) != spin) {
				return false;
			}
		}
		return true;
	}

	public double getArea() {
		return area;
	}

	public SphericalPoint getCentroid() {
		return centroid;
	}

	public double getRadius() {
		return radius;
	}

	public Spin getSpin() {
		return spin;
	}

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

	public SphericalArc getSphericalArc(int index) {
		return sphericalArcs[index];
	}

	public SphericalPoint getSphericalpoint(int index) {
		return sphericalPoints[index];
	}

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
