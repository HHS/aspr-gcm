package gcm.util.spherical;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a non-crossing polygon on the surface of a unit sphere. 
 * 
 * @author Shawn Hatch
 *
 */
public class SphericalPolygon {

	private static class Scaffold {
		private List<SphericalPoint> sphericalPoints = new ArrayList<>();

	}

	public static class Builder {

		public void addSphericalPoint(SphericalPoint sphericalPoint) {
			scaffold.sphericalPoints.add(sphericalPoint);
		}

		private Scaffold scaffold = new Scaffold();

		private Builder() {

		}

		public SphericalPolygon build() {
			try {
				return new SphericalPolygon(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final List<SphericalPoint> sphericalPoints;
	private final List<SphericalArc> sphericalArcs = new ArrayList<>();

	public List<SphericalTriangle> getSphericalTriangles() {
		return new ArrayList<>(sphericalTriangles);
	}

	private final List<SphericalTriangle> sphericalTriangles;

	private final Spin spin;

	public Spin getSpin() {
		return spin;
	}

	public boolean containsPosition(SphericalPoint sphericalPoint) {
		for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
			if (sphericalTriangle.contains(sphericalPoint)) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(SphericalPolygon sphericalPolygon) {
		for (SphericalTriangle triangle1 : sphericalTriangles) {
			for (SphericalTriangle triangle2 : sphericalPolygon.getSphericalTriangles()) {
				if (triangle1.intersects(triangle2)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<SphericalArc> getSphericalArcs() {
		return new ArrayList<>(sphericalArcs);

	}

	public List<SphericalPoint> getSphericalPoints() {
		return new ArrayList<>(sphericalPoints);
	}

	public boolean intersects(SphericalArc sphericalArc) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalArc)) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(SphericalTriangle sphericalTriangle) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalTriangle)) {
				return true;
			}
		}
		return false;
	}

	private SphericalTriangle popEar(int index, List<SphericalPoint> points, Spin spin) {
		// form the triangle from the index
		SphericalTriangle.Builder sphericalTriangleBuilder = SphericalTriangle.builder();
		for (int i = 0; i < 3; i++) {
			sphericalTriangleBuilder.setSphericalPoint(i, points.get((index + i) % 3));
		}
		SphericalTriangle t = sphericalTriangleBuilder.build();

		/*
		 * if the triangle does not agree with cw then we are done
		 * 
		 */
		if (t.getSpin() != spin) {
			return null;
		}
		/*
		 * if the triangle's new side crosses any of the non-attached legs, then
		 * we are done
		 */

		// here are the indices of the attached legs. We will not attempt to
		// intersect them with the newly formed arc
		int previousArcIndex = (index - 2 + points.size()) % points.size();
		int nextArcIndex = (index + 1) % points.size();

		SphericalArc sphericalArcFormedByTriangle = t.getSphericalArc(2);
		SphericalArc.Builder sphereicalArcBuilder = SphericalArc.builder();
		for (int i = 0; i < index; i++) {
			if ((i != previousArcIndex) && (i != nextArcIndex)) {
				int j = (i + 1) % points.size();
				sphereicalArcBuilder.setSphereicalPoint(0, points.get(i));
				sphereicalArcBuilder.setSphereicalPoint(1, points.get(j));
				SphericalArc potentiallyIntersectingArc = sphereicalArcBuilder.build();
				if (sphericalArcFormedByTriangle.intersectsArc(potentiallyIntersectingArc)) {
					return null;
				}
			}
		}

		// if any of the other nodes are in the triangle, then we are done
		int previousPointIndex = (index - 1 + points.size()) % points.size();
		int nexPointIndex = (index + 1) % points.size();
		for (int i = 0; i < points.size(); i++) {
			if ((i != previousPointIndex) && (i != index) && (i != nexPointIndex)) {
				if (t.contains(points.get(i))) {
					return null;
				}
			}

		}

		return t;
	}

	private List<SphericalTriangle> triangulate(List<SphericalPoint> sphericalPoints, Spin spin) {

		/*
		 * We will attempt to remove nodes from the list one by one by popping
		 * an ear off the polygon -- that is, we will remove a triangle and get
		 * an increasingly simple polygon. If all goes well, we will be left
		 * with a degenerate polygon (one that has no nodes)
		 * 
		 */

		List<SphericalTriangle> result = new ArrayList<>();
		List<SphericalPoint> points = new ArrayList<>(sphericalPoints);

		/*
		 * Roll around the polygon, trying to pop off a node and its
		 * corresponding triangle. Popping an ear is successful only when the
		 * triangle so formed is fully inside the polygon. If we fail
		 * repeatedly, we stop once we have come full circle.
		 * 
		 */

		int index = 0;
		int failurecount = 0;
		while ((points.size() > 0) && (failurecount < points.size())) {
			SphericalTriangle sphericalTriangle = popEar(index, points, spin);
			if (sphericalTriangle != null) {
				result.add(sphericalTriangle);
				failurecount = 0;

			} else {
				failurecount++;
				index++;
				index = (index + 1) % points.size();
			}
		}

		// if there are any nodes remaining,
		// then return false and clear out any triangles
		// that were formed

		if (points.size() > 0) {
			result.clear();
		}
		return result;
	}

	private SphericalPolygon(Scaffold scaffold) {
		if (scaffold.sphericalPoints.size() < 3) {
			throw new RuntimeException("spherical polygons require at least three points");
		}

		Spin spin = Spin.RIGHT_HANDED;
		List<SphericalTriangle> triangles = triangulate(scaffold.sphericalPoints, Spin.RIGHT_HANDED);
		if (triangles.size() == 0) {
			triangles = triangulate(scaffold.sphericalPoints, Spin.LEFT_HANDED);
			spin = Spin.RIGHT_HANDED;
		}
		
		
		if (triangles.size() == 0) {
			throw new RuntimeException("cannot form polygon");
		}
		
		this.spin = spin;
		sphericalTriangles = triangles;

		sphericalPoints = scaffold.sphericalPoints;
		SphericalArc.Builder sphereicalArcBuilder = SphericalArc.builder();
		for (int i = 0; i < scaffold.sphericalPoints.size(); i++) {
			int j = (i + 1) % scaffold.sphericalPoints.size();
			sphereicalArcBuilder.setSphereicalPoint(0, scaffold.sphericalPoints.get(i));
			sphereicalArcBuilder.setSphereicalPoint(1, scaffold.sphericalPoints.get(j));
			sphericalArcs.add(sphereicalArcBuilder.build());
		}
		

	}
}
