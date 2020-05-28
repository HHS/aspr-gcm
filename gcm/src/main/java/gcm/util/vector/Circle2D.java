package gcm.test.manual;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import gcm.util.spherical.Chirality;
import gcm.util.vector.MutableVector2D;
import gcm.util.vector.Vector2D;

/**
 * A utility for calculating the smallest circle that encompasses a set of 2D
 * points.
 * 
 * @author Shawn Hatch
 *
 */
public class Circle2D {

	public static enum SolutionAlgorithm {
		CENTROID, N3, N4, COLLAPSING_BUBBLE
	}

	private final Vector2D center;

	private final double radius;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Circle2D [radius=");
		builder.append(radius);
		builder.append(", center=");
		builder.append(center);
		builder.append("]");
		return builder.toString();
	}

	public double getRadius() {
		return radius;
	}

	public boolean isFinite() {
		return Double.isFinite(radius) && center.isFinite();
	}

	public Vector2D getCenter() {
		return center;
	}

	public Circle2D() {
		center = new Vector2D();
		radius = Double.POSITIVE_INFINITY;
	}

	public Circle2D(Vector2D a) {
		center = a;
		radius = 0;
	}

	public Circle2D(Vector2D a, double radius) {
		center = a;
		if (radius < 0) {
			throw new RuntimeException("negative radius");
		}
		this.radius = radius;
	}

	public Circle2D(Vector2D a, Vector2D b) {
		center = a.add(b).scale(0.5);
		radius = FastMath.max(center.distanceTo(a), center.distanceTo(b));
	}

	// public Circle2D(Vector2D a, Vector2D b, Vector2D c) {
	//
	// List<Circle2D> candidateCircles = new ArrayList<>();
	//
	// candidateCircles.add(new Circle2D(a, b));
	// candidateCircles.add(new Circle2D(b, c));
	// candidateCircles.add(new Circle2D(a, c));
	//
	// Vector2D midAB = a.add(b).scale(0.5);
	// Vector2D midBC = b.add(c).scale(0.5);
	// Vector2D bSubA = b.sub(a);
	// Vector2D perpBC = b.sub(c).perpendicularRotation(Chirality.RIGHT_HANDED);
	// double k = midAB.sub(midBC).dot(bSubA) / perpBC.dot(bSubA);
	// Vector2D centerPoint = perpBC.scale(k).add(midBC);
	// double r = FastMath.max(FastMath.max(centerPoint.distanceTo(a),
	// centerPoint.distanceTo(b)), centerPoint.distanceTo(c));
	//
	// candidateCircles.add(new Circle2D(centerPoint, r));
	//
	// List<Vector2D> points = new ArrayList<>();
	// points.add(a);
	// points.add(b);
	// points.add(c);
	//
	// Circle2D bestCircle = null;
	// double leastRadius = Double.POSITIVE_INFINITY;
	// for (Circle2D circle2d : candidateCircles) {
	// if (circle2d.radius < leastRadius && circle2d.contains(points)) {
	// bestCircle = circle2d;
	// }
	// }
	// radius = bestCircle.radius;
	// center = bestCircle.center;
	//
	// }

	public boolean contains(Vector2D v) {
		return center.distanceTo(v) <= radius;
	}

	public boolean contains(List<Vector2D> points) {
		for (Vector2D point : points) {
			if (!contains(point)) {
				return false;
			}
		}
		return true;
	}

	public Circle2D(List<Vector2D> points, SolutionAlgorithm solutionAlgorithm) {
		Circle2D c;
		switch (points.size()) {
		case 0:
			c = new Circle2D();
			break;
		case 1:
			c = new Circle2D(points.get(0));
			break;
		case 2:
			c = new Circle2D(points.get(0), points.get(1));
			break;
		default:
			switch (solutionAlgorithm) {
			case CENTROID:
				c = getCentroidSolution(points);
				break;
			case N3:
				c = getN3Solution(points);
				break;
			case N4:
				c = getN4Solution(points);
				break;
			case COLLAPSING_BUBBLE:
				c = getCollapsingBubbleSolution(points);
				break;
			default:
				throw new RuntimeException("unhandled case " + solutionAlgorithm);
			}
			break;
		}
		center = c.center;
		radius = c.radius;
	}

	private Circle2D getCentroidSolution(List<Vector2D> points) {
		Circle2D c;

		MutableVector2D v = new MutableVector2D();
		for (Vector2D point : points) {
			v.add(point);
		}
		v.scale(1.0 / points.size());

		Vector2D center = new Vector2D(v);
		double r = Double.NEGATIVE_INFINITY;
		for (Vector2D point : points) {
			r = FastMath.max(r, v.distanceTo(point));
		}
		c = new Circle2D(center, r);

		return c;
	}

	private Circle2D getCollapsingBubbleSolution(List<Vector2D> points) {
		// cen - the evolving best solution to the center
		// a - the first contact point
		// b - the second contact point
		// c - the third contact point - may not exist

		// Establish the first contact point

		Vector2D a = null;

		Vector2D cen = new Vector2D();
		for (Vector2D point : points) {
			cen = cen.add(point);
		}
		cen = cen.scale(1.0 / points.size());

		double r = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < points.size(); i++) {
			Vector2D point = points.get(i);
			double distance = cen.distanceTo(point);
			if (distance > r) {
				a = point;
				System.out.println("Collapse first contact at " + i);
				r = distance;
			}
		}

		// Establish the second contact point

		Vector2D b = null;
		double minJ = 1;
		for (int i = 0; i < points.size(); i++) {
			Vector2D v = points.get(i);
			if (v != a) {
				Vector2D m = a.add(v).scale(0.5);
				Vector2D q = v.sub(a);
				double j = m.sub(cen).dot(q) / a.sub(cen).dot(q);
				if (j > 0 && j <= 1) {
					if (j < minJ) {
						b = v;
						System.out.println("Collapse second contact at " + i);
						minJ = j;
					}
				}
			}
		}
		cen = a.sub(cen).scale(minJ).add(cen);
		r = cen.distanceTo(a);
		// System.out.println("two contact solution at radius = " + r + " and
		// center = " + cen);

		// Establish the third contact point
		Vector2D m1 = a.add(b).scale(0.5);
		minJ = 1;
		for (int i = 0; i < points.size(); i++) {
			Vector2D v = points.get(i);
			Vector2D g = v.sub(a);
			if (v != a && v != b) {
				Vector2D m2 = a.add(v).scale(0.5);
				double j = m2.sub(cen).dot(g) / m1.sub(cen).dot(g);
				if (j >= 0 && j <= 1) {
					if (j < minJ) {
						minJ = j;
						System.out.println("Collapse third contact at " + i);
					}
				}
			}
		}
		cen = m1.sub(cen).scale(minJ).add(cen);
		r = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < points.size(); i++) {
			Vector2D point = points.get(i);
			double distance = cen.distanceTo(point);
			if (distance > r) {
				a = point;
				r = distance;
			}
		}
		return new Circle2D(cen, r);
	}

	private Circle2D getN3Solution(List<Vector2D> points) {
		return null;

		// for every pair, we can determine the range of k values to include the
		// other points
		// if this range includes both positive and negative values, then there
		// is no solution for the pair
		// if only 0+, take the greatest solution
		// if only 0-, take the least solution
	}

	private Circle2D getN4Solution(List<Vector2D> points) {

		Circle2D solution = new Circle2D();

		for (int i = 0; i < points.size() - 1; i++) {
			for (int j = i + 1; j < points.size(); j++) {
				Circle2D circle = new Circle2D(points.get(i), points.get(j));
				boolean allPointsContained = true;
				for (Vector2D point : points) {
					if (!circle.contains(point)) {
						allPointsContained = false;
						break;
					}
				}
				if (allPointsContained && circle.radius < solution.radius) {
					solution = circle;
					System.out.println("N4 degree two solution at " + i + " " + j);
				}
			}
		}

		for (int i = 0; i < points.size() - 2; i++) {
			for (int j = i + 1; j < points.size() - 1; j++) {
				for (int k = j + 1; k < points.size(); k++) {
					Vector2D a = points.get(i);
					Vector2D b = points.get(j);
					Vector2D c = points.get(k);
					Vector2D m1 = a.add(b).scale(0.5);
					Vector2D m2 = b.add(c).scale(0.5);
					Vector2D d = b.sub(a);
					Vector2D p = b.sub(c).perpendicularRotation(Chirality.RIGHT_HANDED);
					double h = m1.sub(m2).dot(d) / p.dot(d);
					Vector2D cen = p.scale(h).add(m2);
					double radius = FastMath.max(cen.distanceTo(a), cen.distanceTo(b));
					radius = FastMath.max(radius, cen.distanceTo(c));
					Circle2D circle = new Circle2D(cen, radius);

					boolean allPointsContained = true;
					for (Vector2D point : points) {
						if (!circle.contains(point)) {
							allPointsContained = false;
							break;
						}
					}
					if (allPointsContained && circle.radius < solution.radius) {
						solution = circle;
						System.out.println("N4 degree three solution at " + i + " " + j + " " + k);
					}
				}
			}
		}

		return solution;
	}

}
