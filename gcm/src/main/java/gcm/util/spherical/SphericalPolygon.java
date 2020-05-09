package gcm.util.spherical;

import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.dimensiontree.VolumetricDimensionTree;
import net.jcip.annotations.Immutable;

/**
 * Represents an immutable, non-crossing polygon on the surface of a unit
 * sphere.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
@Source(status = TestStatus.REQUIRED)

public class SphericalPolygon {

	private final static int SEARCH_TREE_THRESHOLD = 30;

	/*
	 * static class for containing the contributed points that will the form the
	 * polygon
	 */
	private static class Scaffold {
		private List<SphericalPoint> sphericalPoints = new ArrayList<>();
		boolean useSearchTree = true;
	}

	/**
	 * Builder class for {@link SphericalPolygon}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		/**
		 * Adds a {@link SphericalPoint} to the {@link SphericalPolygon}. Order
		 * of addition dictates the order of the vertices of the polygon.
		 * Winding order may be either left or right handed.
		 */
		public void addSphericalPoint(SphericalPoint sphericalPoint) {
			scaffold.sphericalPoints.add(sphericalPoint);
		}
		
		/**
		 * Sets the useSearchTree policy for the {@link SphericalPolygon}.  Default is true;
		 */
		public void setUseSearchTree(boolean useSearchTree) {
			scaffold.useSearchTree = useSearchTree;			
		}

		private Scaffold scaffold = new Scaffold();

		/*
		 * Hidden constructor
		 */
		private Builder() {

		}

		/**
		 * Builds a {@link SphericalPolygon} from the supplied
		 * {@link SphericalPoint} values.
		 * 
		 * @throws MalformedSphericalPolygonException
		 * 
		 *             <li>if any of the {@link SphericalPoint} values are null
		 *             <li>if there are fewer than three {@link SphericalPoint}
		 *             values
		 *             <li>if the {@link SphericalPoint} values form a crossing
		 *             polygon
		 * 
		 */
		public SphericalPolygon build() {
			try {
				return new SphericalPolygon(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	/**
	 * Returns a new builder instance for {@link SphericalTriangle}
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final List<SphericalPoint> sphericalPoints;

	private final List<SphericalArc> sphericalArcs = new ArrayList<>();

	/**
	 * Returns the a list of {@link SphericalTriangle} values that cover this
	 * {@link SphericalPolygon} without overlap.
	 */
	public List<SphericalTriangle> getSphericalTriangles() {
		return new ArrayList<>(sphericalTriangles);
	}

	private final List<SphericalTriangle> sphericalTriangles;

	private final Spin spin;

	private final VolumetricDimensionTree<SphericalTriangle> searchTree;

	/**
	 * Returns the spin of this {@link SphericalPolygon} relative to the natural
	 * order of its SphericalPoints.
	 */
	public Spin getSpin() {
		return spin;
	}

	/**
	 * Returns true if and only if the given {@link SphericalPoint} is in the
	 * interior or the arcs of this {@link SphericalPolygon}
	 */
	public boolean containsPosition(SphericalPoint sphericalPoint) {
		if (searchTree == null) {
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					return true;
				}
			}
		} else {
			List<SphericalTriangle> membersInSphere = searchTree.getMembersInSphere(0, sphericalPoint.toArray());
			for (SphericalTriangle sphericalTriangle : membersInSphere) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalPolygon}
	 */
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

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalArc}
	 */
	public boolean intersects(SphericalArc sphericalArc) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalArc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalTriangle}
	 */
	public boolean intersects(SphericalTriangle sphericalTriangle) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalTriangle)) {
				return true;
			}
		}
		return false;
	}

	private static SphericalTriangle popEar(int index, List<SphericalPoint> points, Spin spin) {
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

	private static List<SphericalTriangle> triangulate(List<SphericalPoint> sphericalPoints, Spin spin) {

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
			throw new MalformedSphericalPolygonException("spherical polygons require at least three points");
		}

		for (SphericalPoint sphericalPoint : scaffold.sphericalPoints) {
			if (sphericalPoint == null) {
				throw new MalformedSphericalPolygonException("spherical polygons require non-null spherical points");
			}
		}

		Spin spin = Spin.RIGHT_HANDED;
		List<SphericalTriangle> triangles = triangulate(scaffold.sphericalPoints, Spin.RIGHT_HANDED);
		if (triangles.size() == 0) {
			triangles = triangulate(scaffold.sphericalPoints, Spin.LEFT_HANDED);
			spin = Spin.RIGHT_HANDED;
		}

		if (triangles.size() == 0) {
			throw new MalformedSphericalPolygonException("the spherical points form a crossing polygon");
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

		if (sphericalTriangles.size() > SEARCH_TREE_THRESHOLD && scaffold.useSearchTree) {
			searchTree = VolumetricDimensionTree.builder(SphericalTriangle.class)//
												.setFastRemovals(true)//
												.setLowerBounds(new double[] { -1, -1, -1 })//
												.setUpperBounds(new double[] { 1, 1, 1 })//
												.build();//

			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				double[] triangleCenter = sphericalTriangle.getCentroid().toArray();
				searchTree.add(triangleCenter, sphericalTriangle.getRadius(), sphericalTriangle);
			}
		} else {
			searchTree = null;
		}

	}
}
