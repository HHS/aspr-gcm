package gcm.util.spherical.prototype;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Set;
//
//import com.leidos.afk.earth.GeoUtils;
//import com.leidos.afk.vector.Vector3D;

/**
 * 
 * A simple algorithm for building a convex hull from a given set of
 * geographical vector positions.
 * 
 * @author Shawn Hatch
 * 
 */
public class ConvexHullSolver {
	
//	private List<Vector3D> solution = new ArrayList<Vector3D>();
//	
//	private List<Stub> stubs = new ArrayList<Stub>();
//	
//	private GeoUtils geoUtils = GeoUtils.fromMeanRadius();
//	
//	private Vector3D centroid = new Vector3D();
//	
//	private static class Stub {
//		
//		Vector3D position = new Vector3D();
//		
//		Vector3D geo;
//		
//		int solutionIndex = -1;
//		
//		double distanceToCentroid;
//		
//		@Override
//		public String toString() {
//			return "Stub [solutionIndex=" + solutionIndex + ", distanceToCentroid=" + distanceToCentroid + ", geo=" + geo + ", position=" + position + "]";
//		}
//		
//	}
//	
//	private void initializeStubs(Set<Vector3D> geoVectors) {
//		for (Vector3D v : geoVectors) {
//			Stub stub = new Stub();
//			stub.geo = v;
//			stub.position.assign(v);
//			geoUtils.geoToECC(stub.position);
//			stub.position.normalize();
//			centroid.add(stub.position);
//			stubs.add(stub);
//		}
//		centroid.normalize();
//		for (Stub stub : stubs) {
//			stub.distanceToCentroid = centroid.angleInRadians(stub.position);
//		}
//		
//		Collections.sort(stubs, new Comparator<Stub>() {
//			
//			@Override
//			public int compare(Stub stub1, Stub stub2) {
//				if (stub1.distanceToCentroid > stub2.distanceToCentroid) {
//					return -1;
//				}
//				if (stub1.distanceToCentroid < stub2.distanceToCentroid) {
//					return 1;
//				}
//				return 0;
//			}
//		});
//		
//	}
//	
//	private void solve() {
//		Stub nextVertex = stubs.get(0);
//		
//		nextVertex.solutionIndex = 0;
//		
//		Vector3D a = new Vector3D();
//		Vector3D b = new Vector3D();
//		Vector3D c = new Vector3D();
//		Vector3D d = new Vector3D();
//		
//		Stub currentVertex;
//		boolean solutionFound = false;
//		boolean degenerateCase = false;
//		while (!solutionFound && !degenerateCase) {
//			currentVertex = nextVertex;
//			nextVertex = null;
//			double bestAngle = FastMath.PI;
//			double minDistanceForNextVertex = 0;
//			for (Stub stub : stubs) {
//				if (stub == currentVertex) {
//					continue;
//				}
//				if (stub.distanceToCentroid < minDistanceForNextVertex) {
//					break;
//				}
//				a.assign(currentVertex.position);
//				a.cross(stub.position);
//				b.cross(currentVertex.position);
//				b.cross(centroid);
//				double angle = a.angleInRadians(b);
//				c.assign(a);				
//				c.cross(b);
//				if (c.dot(currentVertex.position) < 0) {
//					continue;
//				}
//				angle = FastMath.PI / 2 - angle;
//				if ((nextVertex == null) || (bestAngle > angle)) {
//					bestAngle = angle;
//					nextVertex = stub;
//					d.assign(currentVertex.position);
//					d.cross(nextVertex.position);
//					minDistanceForNextVertex = FastMath.max(FastMath.PI / 2 - d.angleInRadians(centroid), minDistanceForNextVertex);
//				}
//			}
//			if (nextVertex == null) {
//				degenerateCase = true;
//				break;
//			}
//			if (nextVertex.solutionIndex < 0) {
//				nextVertex.solutionIndex = currentVertex.solutionIndex + 1;
//			} else {
//				solutionFound = true;
//				break;
//			}
//			
//		}
//		if (solutionFound) {
//			List<Stub> solutionStubs = new ArrayList<ConvexHullSolver.Stub>();
//			for (Stub stub : stubs) {
//				if (stub.solutionIndex >= 0) {
//					solutionStubs.add(stub);
//				}
//			}
//			
//			Collections.sort(solutionStubs, new Comparator<Stub>() {
//				
//				@Override
//				public int compare(Stub stub1, Stub stub2) {
//					if (stub1.solutionIndex < stub2.solutionIndex) {
//						return -1;
//					}
//					if (stub1.solutionIndex > stub2.solutionIndex) {
//						return 1;
//					}
//					return 0;
//				}
//			});
//			
//			for (Stub stub : solutionStubs) {
//				solution.add(stub.geo);
//			}
//		}
//	}
//	
//	private ConvexHullSolver(Set<Vector3D> geoVectors) {
//		if (geoVectors.size() < 3) {
//			solution.addAll(geoVectors);
//		} else {
//			initializeStubs(geoVectors);
//			solve();
//		}
//	}
//	
//	/**
//	 * Returns an ordered list of geographical(latitude degrees ,longitude
//	 * degrees ,altitude meters) vector objects that are a literal(by pointer)
//	 * subset of the input geographical vectors and that form a convex hull
//	 * around the geographical vectors.
//	 * 
//	 * @param geoVectors
//	 * @return
//	 */
//	public static List<Vector3D> getGeoConvexHull(Set<Vector3D> geoVectors) {
//		return new ConvexHullSolver(geoVectors).solution;
//	}
	
}
