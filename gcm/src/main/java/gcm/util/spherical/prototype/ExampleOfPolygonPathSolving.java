package gcm.util.spherical.prototype;

//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
//import com.leidos.afk.earth.GeoUtils;
//import com.leidos.afk.poi.engine.AdapterFactory;
//import com.leidos.afk.table.TableReader;
//import com.leidos.afk.vector.Vector3D;
//import com.saic.afk.graph.GenericMutableGraph;
//import com.saic.afk.graph.GraphPathSolver;
//import com.saic.afk.graph.GraphPathSolver.EdgeCostEvaluator;
//import com.saic.afk.graph.GraphPathSolver.TravelCostEvaluator;
//import com.saic.afk.graph.MutableGraph;
//import com.saic.afk.graph.Path;
//import com.saic.afk.table.Table;
//import com.saic.afk.table.TableDefinition;
//import com.saic.afk.table.TableRow;
//import com.saic.afk.table.TableSet;
//import com.saic.afk.table.impl.TableColumnBuilder;
//import com.saic.afk.table.impl.TableDefinitionBuilder;

/**
 * An example class showing how to implement a simple way to plot a shortest
 * course that avoids having the path cross a group of non-overlapping polygons.
 * 
 * @author Shawn Hatch
 * 
 */
public class ExampleOfPolygonPathSolving {
	
//	private static class Cluster {
//		
//		Vector3D geoCenter = new Vector3D();
//		
//		List<Vector3D> geoVectors = new ArrayList<Vector3D>();
//		
//		Vector3D eccCenter = new Vector3D();
//		
//		double radius;
//		
//		List<Vector3D> geoVectorHull;
//		
//	}
//	
//	private static class Node {
//		
//		Cluster cluster;
//		
//		Vector3D geo = new Vector3D();
//		
//		Vector3D ecc = new Vector3D();
//	}
//	
//	private static class Edge {
//		
//		Node node1;
//		
//		Node node2;
//	}
//	
//	private GeoUtils geoUtils = GeoUtils.fromMeanRadius();
//	
//	private List<Edge> hullEdges = new ArrayList<Edge>();
//	
//	private List<Edge> interclusterEdges = new ArrayList<Edge>();
//	
//	private MutableGraph<Node, Edge> graph = new GenericMutableGraph<Node, Edge>();
//	
//	private Set<Cluster> clusters = new LinkedHashSet<Cluster>();
//	
//	private List<Node> nodes = new ArrayList<Node>();
//	
//	private Vector3D originGeo = new Vector3D();
//	
//	private Vector3D destinationGeo = new Vector3D();
//	
//	private Node originNode = new Node();
//	
//	private Node destinationNode = new Node();
//	
//	@SuppressWarnings("unused")
//    private void loadProblem() {
//		
//		TableDefinitionBuilder tableDefinitionBuilder = TableDefinitionBuilder.newBuilder();
//		tableDefinitionBuilder.setName("problem");
//		tableDefinitionBuilder.setType(String.class);
//		TableColumnBuilder tableColumnBuilder = TableColumnBuilder.newBuilder();
//		tableColumnBuilder.setName("Cluster");
//		tableColumnBuilder.setType(String.class);
//		tableDefinitionBuilder.addTableColumn(tableColumnBuilder.build());
//		tableColumnBuilder.setName("Lat");
//		tableColumnBuilder.setType(Double.class);
//		tableDefinitionBuilder.addTableColumn(tableColumnBuilder.build());
//		tableColumnBuilder.setName("Lon");
//		tableColumnBuilder.setType(Double.class);
//		tableDefinitionBuilder.addTableColumn(tableColumnBuilder.build());
//		TableDefinition tableDefinition = tableDefinitionBuilder.build();
//		
//		Map<String, TableDefinition> tableDefinitions = new LinkedHashMap<String, TableDefinition>();
//		tableDefinitions.put(tableDefinition.getName(), tableDefinition);
//		
//		TableSet tableSet = null;
//		String fileName = "C:\\temp\\poly.xlsx";
//		try (InputStream inputStream = Files.newInputStream(Paths.get(fileName))) {
//			TableReader tableReader = AdapterFactory.getXlsxTableReader();
//			tableSet = tableReader.read(tableDefinitions, inputStream);
//		} catch (Exception e) {			
//			e.printStackTrace();
//		}
//		Table table = tableSet.getTable(tableDefinition.getName());
//		Map<String, Cluster> clusterMap = new LinkedHashMap<String, Cluster>();
//		for (TableRow tableRow : table.getTableRows()) {
//			String clusterName = tableRow.getCell("Cluster").getValue();
//			Double lat = tableRow.getCell("Lat").getValue();
//			Double lon = tableRow.getCell("Lon").getValue();
//			
//			if (clusterName.equals("Origin")) {
//				originGeo.assign(lat, lon, 0);
//			} else if (clusterName.equals("Destination")) {
//				destinationGeo.assign(lat, lon, 0);
//			} else {
//				Cluster cluster = clusterMap.get(clusterName);
//				if (cluster == null) {
//					cluster = new Cluster();
//					clusterMap.put(clusterName, cluster);
//				}
//				cluster.geoVectors.add(new Vector3D(lat, lon, 0));
//			}
//		}
//		for (Cluster cluster : clusterMap.values()) {
//			clusters.add(cluster);
//		}
//		
//	}
//	
//	private void loadProblem2() {
//		double clusterRadius = 10000;// meters
//		originGeo.assign(35, 120, 0);
//		destinationGeo.assign(36, 121, 0);
//		
//		Random random = new Random();
//		int lowCount = 5;
//		int highCount = 15;
//		int failCount = 0;
//		int shrinkCount = 0;
//		Vector3D north = new Vector3D(0, 0, 1);
//		int maxFailCount = 100;
//		while ((failCount < maxFailCount) && (shrinkCount <= 5)) {
//			Vector3D center = new Vector3D(originGeo.x + random.nextDouble(), originGeo.y + random.nextDouble(), 0);
//			geoUtils.geoToECC(center);
//			boolean goodCenterCandidate = true;
//			for (Cluster cluster : clusters) {
//				Vector3D establishedCenter = cluster.eccCenter;
//				double dist = geoUtils.arcDistance(center, establishedCenter);
//				double standoff = cluster.radius + clusterRadius;
//				if (dist <= standoff) {
//					goodCenterCandidate = false;
//					break;
//				}
//			}
//			if (goodCenterCandidate) {
//				Cluster cluster = new Cluster();
//				cluster.eccCenter.assign(center);
//				cluster.geoCenter.assign(center);
//				geoUtils.eccToGEO(cluster.geoCenter);
//				cluster.radius = clusterRadius;
//				
//				int n = random.nextInt(highCount - lowCount) + lowCount;
//				for (int i = 0; i < n; i++) {
//					double theta = random.nextDouble() * 2 * FastMath.PI;
//					double dist = random.nextDouble();
//					dist = FastMath.sqrt(dist);
//					dist *= clusterRadius;
//					double alpha = dist / geoUtils.getRadius();
//					Vector3D v = new Vector3D();
//					v.assign(center);
//					v.rotateToward(north, alpha);
//					v.rotate(center, theta);
//					geoUtils.eccToGEO(v);
//					cluster.geoVectors.add(v);
//				}
//				clusters.add(cluster);
//			} else {
//				failCount++;
//				if (failCount == maxFailCount) {
//					failCount = 0;
//					shrinkCount++;
//					clusterRadius *= 0.5;
//				}
//			}
//		}
//		
//	}
//	
//	private void solveConvexHulls() {
//		for (Cluster cluster : clusters) {
//			Set<Vector3D> set = new LinkedHashSet<Vector3D>(cluster.geoVectors);
//			cluster.geoVectorHull = ConvexHullSolver.getGeoConvexHull(set);
//		}
//	}
//	
//	private void formNodesAndHullEdges() {
//		originNode.geo.assign(originGeo);
//		originNode.ecc.assign(originGeo);
//		originNode.cluster = new Cluster();
//		geoUtils.geoToECC(originNode.ecc);
//		
//		destinationNode.geo.assign(destinationGeo);
//		destinationNode.ecc.assign(destinationGeo);
//		destinationNode.cluster = new Cluster();
//		geoUtils.geoToECC(destinationNode.ecc);
//		
//		for (Cluster cluster : clusters) {
//			List<Node> clusterNodes = new ArrayList<Node>();
//			for (Vector3D geo : cluster.geoVectorHull) {
//				Node node = new Node();
//				node.geo.assign(geo);
//				node.ecc.assign(geo);
//				node.cluster = cluster;
//				geoUtils.geoToECC(node.ecc);
//				clusterNodes.add(node);
//				nodes.add(node);
//			}
//			for (int i = 0; i < clusterNodes.size(); i++) {
//				Node node1 = clusterNodes.get(i);
//				Node node2 = clusterNodes.get((i + 1) % clusterNodes.size());
//				Edge edge = new Edge();
//				edge.node1 = node1;
//				edge.node2 = node2;
//				hullEdges.add(edge);
//			}
//		}
//		
//	}
//	
//	private void addNodesToGraph() {
//		graph.addNode(originNode);
//		graph.addNode(destinationNode);
//		for (Node node : nodes) {
//			graph.addNode(node);
//		}
//	}
//	
//	private void attemptAddingEdgeToInterClusterEdges(Edge edge) {
//		// edges formed from the same cluster are not valid edges
//		if (edge.node1.cluster.equals(edge.node2.cluster)) {
//			return;
//		}
//		
//		Vector3D perp = new Vector3D();
//		
//		for (Edge hullEdge : hullEdges) {
//			// if the hull edge and the edge have a common corner, then don't
//			// test for intersection
//			if (hullEdge.node1.equals(edge.node1) || hullEdge.node1.equals(edge.node2) || hullEdge.node2.equals(edge.node1) || hullEdge.node2.equals(edge.node2)) {
//				continue;
//			}
//			perp.assign(hullEdge.node1.ecc);
//			perp.cross(hullEdge.node2.ecc);
//			boolean split = perp.dot(edge.node1.ecc) * perp.dot(edge.node2.ecc) < 0;
//			if (split) {
//				perp.assign(edge.node1.ecc);
//				perp.cross(edge.node2.ecc);
//				split = perp.dot(hullEdge.node1.ecc) * perp.dot(hullEdge.node2.ecc) < 0;
//				if (split) {
//					return;
//				}
//			}
//			
//		}
//		interclusterEdges.add(edge);
//	}
//	
//	private void formInterClusterEdges() {
//		
//		Edge edge = new Edge();
//		edge.node1 = originNode;
//		edge.node2 = destinationNode;
//		attemptAddingEdgeToInterClusterEdges(edge);
//		
//		for (Node node : nodes) {
//			edge = new Edge();
//			edge.node1 = originNode;
//			edge.node2 = node;
//			attemptAddingEdgeToInterClusterEdges(edge);
//		}
//		
//		for (Node node : nodes) {
//			edge = new Edge();
//			edge.node1 = destinationNode;
//			edge.node2 = node;
//			attemptAddingEdgeToInterClusterEdges(edge);
//		}
//		
//		for (Node node1 : nodes) {
//			for (Node node2 : nodes) {
//				edge = new Edge();
//				edge.node1 = node1;
//				edge.node2 = node2;
//				attemptAddingEdgeToInterClusterEdges(edge);
//			}
//		}
//		
//	}
//	
//	private void addEdgeToGraph(Edge edge) {
//		graph.addEdge(edge, edge.node1, edge.node2);
//		
//		Edge edge2 = new Edge();
//		edge2.node1 = edge.node2;
//		edge2.node2 = edge.node1;
//		
//		graph.addEdge(edge2, edge2.node1, edge2.node2);
//		
//	}
//	
//	private void addEdgesToGraph() {
//		for (Edge edge : interclusterEdges) {
//			addEdgeToGraph(edge);
//		}
//		for (Edge edge : hullEdges) {
//			addEdgeToGraph(edge);
//		}
//	}
//	
//	EdgeCostEvaluator<Edge> edgeCostEvaluator = new EdgeCostEvaluator<Edge>() {
//		
//		@Override
//		public double getEdgeCost(Edge edge) {
//			return geoUtils.arcDistance(edge.node1.ecc, edge.node2.ecc);
//		}
//		
//	};
//	
//	TravelCostEvaluator<Node> travelCostEvaluator = new TravelCostEvaluator<Node>() {
//		
//		@Override
//		public double getMinimumCost(Node originNode, Node destination) {
//			return geoUtils.arcDistance(originNode.ecc, destination.ecc);
//		}
//	};
//	
//	Path<Edge> solution;
//	
//	private void solveGraph() {
//		solution = GraphPathSolver.getPath(graph, originNode, destinationNode, edgeCostEvaluator, travelCostEvaluator);
//		
//	}
//	
//	private void report() {
//
//		
////		for (Cluster cluster : clusters) {
////			
////			boolean firstNode = true;
////			for (int i = 0; i < cluster.geoVectorHull.size(); i++) {
////				Vector3D v1 = cluster.geoVectorHull.get(i);
////				Vector3D v2 = cluster.geoVectorHull.get((i + 1) % cluster.geoVectorHull.size());
////				if (firstNode) {
////					firstNode = false;
////				}
////				
////			}
////		}
//		
//	}
//	
//	private ExampleOfPolygonPathSolving() {
//		loadProblem2();
//		solveConvexHulls();
//		formNodesAndHullEdges();
//		addNodesToGraph();
//		formInterClusterEdges();
//		addEdgesToGraph();
//		solveGraph();
//		report();
//	}
//	
//	public static void main(String[] args) {
//		new ExampleOfPolygonPathSolving();
//		
//	}
}
