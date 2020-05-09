package gcm.util.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import gcm.util.dimensiontree.VolumetricDimensionTree;
import gcm.util.vector.MutableVector3D;

public class GeoDelaunaySolver<T extends GeoCoordinate> {

	private static class Rec<T extends GeoCoordinate> implements Comparable<Rec<T>>{
		T geoCoordinate;
		MutableVector3D v;		
		double angleToCentroid;
		double azimuthAngle;
		int step;
		@Override
		public int compareTo(Rec<T> other) {
			int result = Integer.compare(step, other.step);
			if(result == 0) {
				result = Double.compare(azimuthAngle, other.azimuthAngle);
			}
			return result;
		}		
	}
	
	
	private List<T> spiralize(List<T> geoCoordinates) {
		
		Rec<T> centroid = new Rec<>();
		centroid.v = new MutableVector3D();
		List<Rec<T>> list = new ArrayList<>();
		geoCoordinates.forEach(geoCoordinate -> {
			Rec<T> rec = new Rec<>();
			rec.geoCoordinate = geoCoordinate;			
			rec.v = getPositionFromGeoCoordinate(rec.geoCoordinate);			
			list.add(rec);
			centroid.v.add(rec.v);
		});

		
		centroid.v.normalize();
		
		MutableVector3D north  = new MutableVector3D(0,0,1);
		MutableVector3D northTangent = new MutableVector3D(centroid.v);
		northTangent.rotateToward(north, FastMath.PI/2);
		
		double maxGroundRange = Double.NEGATIVE_INFINITY;
		for (Rec<T> rec : list) {			
			rec.angleToCentroid = centroid.v.angle(rec.v);
			MutableVector3D tangent = new MutableVector3D(centroid.v);
			tangent.rotateToward(rec.v, FastMath.PI/2);
			rec.azimuthAngle = northTangent.angle(tangent);
			tangent.cross(northTangent);
			if(tangent.dot(centroid.v)<0) {
				rec.azimuthAngle *=-1;
			}			
			maxGroundRange = FastMath.max(maxGroundRange, rec.angleToCentroid);
		}
		
		
		double area = 2*FastMath.PI*(1-FastMath.cos(maxGroundRange));
		
		double stepDistance = FastMath.sqrt(area/list.size());
		for (Rec<T> rec : list) {
			rec.step = (int)(rec.angleToCentroid/stepDistance);
		}	
		
		Collections.sort(list);
		
		List<T> result = new ArrayList<>();
		for(Rec<T> rec : list) {
			result.add(rec.geoCoordinate);
		}

		return result;
	}
	
	private static class Edge {
		int[] vertexIds;
		boolean markedForRemoval;

		public Edge(final int[] vertexIds) {
			this.vertexIds = vertexIds;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Edge)) {
				return false;
			}
			final Edge other = (Edge) obj;
			if (!Arrays.equals(vertexIds, other.vertexIds)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + Arrays.hashCode(vertexIds);
			return result;
		}

	}

	private static class Triangle {
		private double radius;

		public Triangle(double radius) {
			this.radius = radius;
		}

		private boolean markedForRemoval;
	}

	private static class Vertex<T extends GeoCoordinate> {
		int id;
		MutableVector3D position;
		T geoCoordinate;

		public Vertex(final int id, final MutableVector3D position, T geoCoordinate) {
			super();
			this.id = id;
			this.position = position;
			this.geoCoordinate = geoCoordinate;
		}
	}

	public static <T extends GeoCoordinate> List<Pair<T, T>> solve(List<T> points) {
		return new GeoDelaunaySolver<>(points).solve();
	}

	private final int scaffoldCount = 3;
	
	private List<T> points;

	private final Map<Triangle, List<Edge>> triangleToEdgeMap = new LinkedHashMap<>();

	private final Map<Edge, List<Triangle>> edgeToTriangleMap = new LinkedHashMap<>();

	private final List<Vertex<T>> vertexes = new ArrayList<>();

	private VolumetricDimensionTree<Triangle> searchTree;

	private GeoDelaunaySolver(List<T> points) {
		 this.points = points;
	}

	private void addTriangle(final int id1, final int id2, final int id3) {

		final int[] ids = new int[3];

		ids[0] = id1;
		ids[1] = id2;
		ids[2] = id3;

		Arrays.sort(ids);

		final MutableVector3D v0 = vertexes.get(ids[0]).position;
		final MutableVector3D v1 = vertexes.get(ids[1]).position;
		final MutableVector3D v2 = vertexes.get(ids[2]).position;

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
		final double radius = c.distanceTo(v0);

		final Triangle triangle = new Triangle(radius);

		int[] edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[1];
		final Edge edge1 = new Edge(edgeIds);
		List<Triangle> list = edgeToTriangleMap.get(edge1);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge1, list);
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[1];
		edgeIds[1] = ids[2];
		final Edge edge2 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge2);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge2, list);
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[2];
		final Edge edge3 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge3);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge3, list);
		}
		list.add(triangle);
		final List<Edge> edgeList = new ArrayList<>();
		edgeList.add(edge1);
		edgeList.add(edge2);
		edgeList.add(edge3);
		triangleToEdgeMap.put(triangle, edgeList);
		searchTree.add(c.toArray(), radius, triangle);
	}

	private void addTriangles(final Vertex<T> vertex, final List<Edge> hullEdges) {
		for (final Edge edge : hullEdges) {
			addTriangle(edge.vertexIds[0], edge.vertexIds[1], vertex.id);
		}
	}

	private List<Edge> getEdgesToRemove(final List<Triangle> trianglesToRemove) {
		final List<Edge> result = new ArrayList<>();
		for (final Triangle triangleToRemove : trianglesToRemove) {
			final List<Edge> edges = triangleToEdgeMap.get(triangleToRemove);
			for (final Edge edge : edges) {
				final List<Triangle> triangles = edgeToTriangleMap.get(edge);
				int triangleRemovalCount = 0;
				for (final Triangle triangle : triangles) {
					if (triangle.markedForRemoval) {
						triangleRemovalCount++;
					}
				}
				if (triangleRemovalCount == 2) {
					if (!edge.markedForRemoval) {
						edge.markedForRemoval = true;
						result.add(edge);
					}
				}
			}
		}
		return result;
	}

	private List<Edge> getHullEdges(final List<Triangle> trianglesToRemove) {
		final List<Edge> result = new ArrayList<>();
		for (final Triangle badTriangle : trianglesToRemove) {
			final List<Edge> edges = triangleToEdgeMap.get(badTriangle);
			for (final Edge edge : edges) {
				if (!edge.markedForRemoval) {
					result.add(edge);
				}
			}
		}
		return result;
	}

	private List<Triangle> getTrianglesStruckByVertex(final Vertex<T> vertex) {
		final List<Triangle> result = new ArrayList<>();
		searchTree.getMembersInSphere(0, vertex.position.toArray()).forEach(triangle -> {
			triangle.markedForRemoval = true;
			result.add(triangle);
		});
		return result;
	}

	private MutableVector3D getPositionFromGeoCoordinate(GeoCoordinate geoCoordinate) {
		double coslat = FastMath.cos(FastMath.toRadians(geoCoordinate.getLatitude()));
		double coslon = FastMath.cos(FastMath.toRadians(geoCoordinate.getLongitude()));
		double sinlat = FastMath.sin(FastMath.toRadians(geoCoordinate.getLatitude()));
		double sinlon = FastMath.sin(FastMath.toRadians(geoCoordinate.getLongitude()));
		return new MutableVector3D(coslat * coslon, coslat * sinlon, sinlat);
	}
	
	private void initialize() {
		points = spiralize(points);
		
		/*
		 * Add three vertexes for the boundary
		 */
		for (int i = 0; i < scaffoldCount; i++) {
			vertexes.add(new Vertex<>(i, new MutableVector3D(), null));
		}

		/*
		 * Add vertexes for each point in the model and calculate the centroid
		 * of those points
		 */
		final MutableVector3D centroid = new MutableVector3D();
		int n = points.size();
		for (int i = 0; i < n; i++) {			
			T geoCoordinate = points.get(i);
			final MutableVector3D v = getPositionFromGeoCoordinate(geoCoordinate);
			centroid.add(v);
			vertexes.add(new Vertex<>(i + scaffoldCount, v, geoCoordinate));
		}
		centroid.normalize();

		double minDotProduct = Double.POSITIVE_INFINITY;

		/*
		 * Find the maximum angle to any point in the data from the centroid --
		 * we use dot products to avoid the acos costs
		 * 
		 */
		for (int i = scaffoldCount; i < vertexes.size(); i++) {
			final Vertex<T> vertex = vertexes.get(i);
			minDotProduct = FastMath.min(minDotProduct, centroid.dot(vertex.position));
		}
		double maxAngle = FastMath.acos(minDotProduct);

		// if the maxAngle to too close to PI, it could result in an ambiguous
		// winding.
		if (FastMath.abs(maxAngle - FastMath.PI) < 0.001) {
			maxAngle = FastMath.PI + 0.001;
		}

		// Push the angle a bit out toward PI since at least one of the data
		// point could lie inside one of the tangent planes

		maxAngle += (FastMath.PI - maxAngle) / 100;

		final MutableVector3D north = new MutableVector3D(0, 0, 1);

		final List<MutableVector3D> tangentPlaneNormals = new ArrayList<>();

		/*
		 * Form the vectors that will be perpendicular to the three planes that
		 * are tangent to the circle formed by the max angle
		 * 
		 */
		for (int i = 0; i < scaffoldCount; i++) {
			final MutableVector3D v = new MutableVector3D(centroid);
			v.rotateToward(north, maxAngle - (FastMath.PI / 2));
			v.rotateAbout(centroid, (2 * i * FastMath.PI) / 3);
			tangentPlaneNormals.add(v);
		}

		/*
		 * Find the three intersections of these plane in a right handed
		 * fashion. The resulting triangle will be right handed if the triangle
		 * is smaller than a hemisphere and left handed otherwise. Either way,
		 * the triangle will have all of the data points on its inside.
		 */
		for (int i = 0; i < scaffoldCount; i++) {
			final MutableVector3D v0 = tangentPlaneNormals.get(i);
			final MutableVector3D v1 = tangentPlaneNormals.get((i + 1) % 3);
			final MutableVector3D scaffoldPoint = new MutableVector3D(v0);
			scaffoldPoint.cross(v1);
			scaffoldPoint.normalize();

			Vertex<T> vertex = vertexes.get(i);
			vertex.position.assign(scaffoldPoint);			
		}

		/*
		 * We choose the bounds that will fit the unit sphere since we cannot
		 * easily anticipate the centroid position of each triangle, despite
		 * knowing all the vertex positions. This will help the tree keep its
		 * depth to a minimum.
		 */
		searchTree = VolumetricDimensionTree.builder(Triangle.class)//
											.setFastRemovals(true)//
											.setLowerBounds(new double[] { -1, -1, -1 })//
											.setUpperBounds(new double[] { 1, 1, 1 })//
											.build();//

		addTriangle(0, 1, 2);//
	}

	private void removeEdges(final List<Edge> edgesToRemove) {
		for (final Edge edge : edgesToRemove) {
			edgeToTriangleMap.remove(edge);
		}
	}

	private void removeTriangles(final List<Triangle> trianglesToRemove) {
		trianglesToRemove.forEach(triangle -> {
			triangleToEdgeMap.remove(triangle).forEach(edge -> {
				edgeToTriangleMap.get(edge).remove(triangle);
			});
			searchTree.remove(triangle.radius, triangle);
		});

	}

	private List<Pair<T, T>> solve() {
		initialize();

		for (int i = scaffoldCount; i < vertexes.size(); i++) {
			final Vertex<T> vertex = vertexes.get(i);
			final List<Triangle> trianglesToRemove = getTrianglesStruckByVertex(vertex);
			final List<Edge> edgesToRemove = getEdgesToRemove(trianglesToRemove);
			final List<Edge> hullEdges = getHullEdges(trianglesToRemove);
			removeTriangles(trianglesToRemove);
			removeEdges(edgesToRemove);
			addTriangles(vertex, hullEdges);
		}


		List<Pair<T,T>> result = new ArrayList<>();
		edgeToTriangleMap.keySet().forEach(edge -> {
			Vertex<T> vertex0 = vertexes.get(edge.vertexIds[0]);
			if(vertex0.geoCoordinate!=null) {
				Vertex<T> vertex1 = vertexes.get(edge.vertexIds[1]);
				Pair<T,T> pair = new Pair<>(vertex0.geoCoordinate,vertex1.geoCoordinate);
				result.add(pair);
			}			
		});
		
		return result;
	}

}
