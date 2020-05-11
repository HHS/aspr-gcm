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
import gcm.util.vector.MutableVector2D;

public class PlanarDelaunaySolver<T extends PlanarCoordinate> {

	private static class Rec<T extends PlanarCoordinate> implements Comparable<Rec<T>>{
		T planarCoordinate;
		MutableVector2D v;		
		double distanceToCentroid;
		double angle;
		int step;
		@Override
		public int compareTo(Rec<T> other) {
			int result = Integer.compare(step, other.step);
			if(result == 0) {
				result = Double.compare(angle, other.angle);
			}
			return result;
		}		
	}
	
	
	private List<T> spiralize(List<T> planarCoordinates) {
		
		MutableVector2D centroid = new MutableVector2D();
		
		List<Rec<T>> list = new ArrayList<>();
		planarCoordinates.forEach(planarCoordinate -> {
			Rec<T> rec = new Rec<>();
			rec.planarCoordinate = planarCoordinate;			
			rec.v = new MutableVector2D(planarCoordinate.getX(),planarCoordinate.getY());			
			list.add(rec);
			centroid.add(rec.v);
		});
		
		centroid.scale(1.0/list.size());
		
		MutableVector2D xAxis  = new MutableVector2D(1,0);
		double maxDistance = Double.NEGATIVE_INFINITY;
			
		MutableVector2D v = new MutableVector2D();
		for (Rec<T> rec : list) {
			rec.distanceToCentroid = centroid.distanceTo(rec.v);
			v.assign(rec.v);
			v.sub(centroid);			
			rec.angle =  v.angle(xAxis)*v.cross(xAxis);
			maxDistance = FastMath.max(maxDistance, rec.distanceToCentroid);
		}
		
		
		double area = 2*FastMath.PI*maxDistance*maxDistance;		
		double stepDistance = FastMath.sqrt(area/list.size());
		
		for (Rec<T> rec : list) {
			rec.step = (int)(rec.distanceToCentroid/stepDistance);
		}	
		
		Collections.sort(list);
		
		List<T> result = new ArrayList<>();
		for(Rec<T> rec : list) {
			result.add(rec.planarCoordinate);
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

	private static class Vertex<T extends PlanarCoordinate> {
		int id;
		MutableVector2D position;
		T planarCoordinate;

		public Vertex(final int id, final MutableVector2D position, T planarCoordinate) {
			super();
			this.id = id;
			this.position = position;
			this.planarCoordinate = planarCoordinate;
		}
	}

	public static <T extends PlanarCoordinate> List<Pair<T, T>> solve(List<T> points) {
		return new PlanarDelaunaySolver<>(points).solve();
	}

	private final int scaffoldCount = 4;
	
	private List<T> points;

	private final Map<Triangle, List<Edge>> triangleToEdgeMap = new LinkedHashMap<>();

	private final Map<Edge, List<Triangle>> edgeToTriangleMap = new LinkedHashMap<>();

	private final List<Vertex<T>> vertexes = new ArrayList<>();

	private VolumetricDimensionTree<Triangle> searchTree;

	private PlanarDelaunaySolver(List<T> points) {
		 this.points = points;
	}

	private void addTriangle(final int id1, final int id2, final int id3) {
		int[] ids = new int[3];

		ids[0] = id1;
		ids[1] = id2;
		ids[2] = id3;

		Arrays.sort(ids);

		MutableVector2D a = new MutableVector2D(vertexes.get(ids[0]).position);
		MutableVector2D b = new MutableVector2D(vertexes.get(ids[1]).position);
		MutableVector2D c = new MutableVector2D(vertexes.get(ids[2]).position);

		MutableVector2D m1 = new MutableVector2D(a);
		m1.add(b);
		m1.scale(0.5);

		MutableVector2D p1 = new MutableVector2D(a);
		p1.sub(b);
		p1.perpTo(p1, true);

		MutableVector2D m2 = new MutableVector2D(b);
		m2.add(c);
		m2.scale(0.5);

		MutableVector2D p2 = new MutableVector2D(b);
		p2.sub(c);
		p2.perpTo(p2, true);

		MutableVector2D q = new MutableVector2D(c);
		q.sub(b);

		m2.sub(m1);
		double j = m2.dot(q) / p1.dot(q);

		MutableVector2D center = new MutableVector2D(p1);
		center.scale(j);
		center.add(m1);

		double radius = center.distanceTo(a);
		

		Triangle triangle = new Triangle(radius);

		int[] edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[1];
		Edge edge1 = new Edge(edgeIds);
		List<Triangle> list = edgeToTriangleMap.get(edge1);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge1, list);			
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[1];
		edgeIds[1] = ids[2];
		Edge edge2 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge2);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge2, list);			
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[2];
		Edge edge3 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge3);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge3, list);			
		}
		list.add(triangle);

		List<Edge> edgeList = new ArrayList<>();
		edgeList.add(edge1);
		edgeList.add(edge2);
		edgeList.add(edge3);
		triangleToEdgeMap.put(triangle, edgeList);

		searchTree.add(center.toArray(), triangle.radius, triangle);
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
	
	private void initialize() {
		points = spiralize(points);
		
		
		
		double maxX = Double.NEGATIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;

		for (PlanarCoordinate planarCoordinate : points) {
			maxX = FastMath.max(maxX, planarCoordinate.getX());
			minX = FastMath.min(minX, planarCoordinate.getX());
			maxY = FastMath.max(maxY, planarCoordinate.getY());
			minY = FastMath.min(minY, planarCoordinate.getY());
		}
		double[] lowerBounds = { minX, minY };
		double[] upperBounds = { maxX, maxY };

		searchTree = VolumetricDimensionTree.builder(Triangle.class)//
											.setFastRemovals(true)//
											.setLowerBounds(lowerBounds)//
											.setUpperBounds(upperBounds)//
											.build();//

		double pad = 0.01;

		double padX = (maxX - minX) * pad;
		double padY = (maxY - minY) * pad;

		minX -= padX;
		maxX += padX;

		minY -= padY;
		maxY += padY;

		Vertex<T> vertex0 = new Vertex<>(0, new MutableVector2D(minX, minY),null);
		vertexes.add(vertex0);

		Vertex<T> vertex1 = new Vertex<>(1, new MutableVector2D(minX, maxY),null);
		vertexes.add(vertex1);

		Vertex<T> vertex2 = new Vertex<>(2, new MutableVector2D(maxX, minY),null);
		vertexes.add(vertex2);

		Vertex<T> vertex3 = new Vertex<>(3, new MutableVector2D(maxX, maxY),null);
		vertexes.add(vertex3);
		
		int n = points.size();
		for (int i = 0; i < n; i++) {			
			T planarCoordinate = points.get(i);
			MutableVector2D v = new MutableVector2D(planarCoordinate.getX(),planarCoordinate.getY());			
			vertexes.add(new Vertex<>(i + scaffoldCount, v, planarCoordinate));
		}
		

		addTriangle(vertex0.id, vertex1.id, vertex2.id);

		addTriangle(vertex1.id, vertex2.id, vertex3.id);
		
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
			if(vertex0.planarCoordinate!=null) {
				Vertex<T> vertex1 = vertexes.get(edge.vertexIds[1]);
				Pair<T,T> pair = new Pair<>(vertex0.planarCoordinate,vertex1.planarCoordinate);
				result.add(pair);
			}			
		});
		
		return result;
	}

}
