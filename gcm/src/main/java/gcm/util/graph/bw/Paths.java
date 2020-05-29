package gcm.util.graph.bw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class Paths {
	
	private static class PathImpl<E> implements Path<E> {
		
		private PathImpl() {
			
		}
		
		private PathImpl(List<E> edges) {
			this.edges.addAll(edges);
		}
		
		private List<E> edges = new ArrayList<>();
		
		@Override
		public int length() {
			
			return edges.size();
		}
		
		@Override
		public List<E> getEdges() {
			return new ArrayList<>(edges);
		}
		
		@Override
		public boolean isEmpty() {
			return length() == 0;
		}
		
	}
	
	public static <E> Path<E> newPath(List<E> edges) {
		return new PathImpl<>(edges);
	}
	
	public static <E> Path<E> newEmptyPath() {
		return new PathImpl<>();
	}
	
	public interface EdgeCostEvaluator<E> {
		
		public double getEdgeCost(E edge);
	}
	
	public static interface PathCostBoundEvaluator<N> {
		
		public double getMinimumCost(N originNode, N destination);
	}
	
	public static <E> double getPathCost(Path<E> path, EdgeCostEvaluator<E> edgeCostEvaluator) {
		double result = 0;
		for (E edge : path.getEdges()) {
			result += edgeCostEvaluator.getEdgeCost(edge);
		}
		return result;
	}
	
	public static <N, E> Path<E> getShortestPath(Graph<N, E> graph, N originNode, N destinationNode) {
		return getShortestPath(graph, originNode, destinationNode, null, null);
	}
	
	public static <N, E> Path<E> getShortestPath(Graph<N, E> graph, N originNode, N destinationNode, EdgeCostEvaluator<E> edgeCostEvaluator) {
		return getShortestPath(graph, originNode, destinationNode, edgeCostEvaluator, null);
	}
	
	public static <N, E> Path<E> getShortestPath(Graph<N, E> graph, N originNode, N destinationNode, PathCostBoundEvaluator<N> pathCostBoundEvaluator) {
		return getShortestPath(graph, originNode, destinationNode, null, pathCostBoundEvaluator);
	}
	
	private static class CostRecord<N, E> {
		
		@Override
        public String toString() {
	        return "CostRecord [node=" + node + ", cost=" + cost + ", minimumCostToDestination=" + minimumCostToDestination + ", lastEdge=" + lastEdge + "]";
        }

		private N node;
		
		private double cost;
		
		private double minimumCostToDestination;
		
		private E lastEdge;
		
	}
	
	private static class PathSolver<N, E> {
		
		private Map<N, CostRecord<N, E>> costMap = new LinkedHashMap<>();
		
		Comparator<CostRecord<N, E>> costComparator = new Comparator<CostRecord<N, E>>() {
			@Override
			public int compare(CostRecord<N, E> costRecord1, CostRecord<N, E> costRecord2) {
				
				double cost1 = costRecord1.cost + costRecord1.minimumCostToDestination;
				double cost2 = costRecord2.cost + costRecord2.minimumCostToDestination;
				
				if (cost1 < cost2) {
					return -1;
				}
				if (cost1 > cost2) {
					return 1;
				}
				return 0;
			}
		};
		
		private List<CostRecord<N, E>> visitList = new ArrayList<>();
		
		private Graph<N, E> graph;
		
		private N originNode;
		
		private N destinationNode;
		
		private Path<E> solution = Paths.newEmptyPath();
		
		private EdgeCostEvaluator<E> edgeCostEvaluator;
		
		private PathCostBoundEvaluator<N> pathCostBoundEvaluator;
		
		public PathSolver(Graph<N, E> graph, N originNode, N destinationNode, EdgeCostEvaluator<E> edgeCostEvaluator, PathCostBoundEvaluator<N> pathCostBoundEvaluator) {
			try {
				this.graph = graph;
				this.originNode = originNode;
				this.destinationNode = destinationNode;
				this.edgeCostEvaluator = edgeCostEvaluator;
				this.pathCostBoundEvaluator = pathCostBoundEvaluator;
				if (graph.containsNode(originNode) && graph.containsNode(destinationNode)) {
					initialze();
					solve();
					formPath();
				}
			} catch (Exception e) {
				throw new RuntimeException(originNode +"\t"+destinationNode);
			}
		}
		
		private void initialze() {
			addCostRecord(originNode, -1, null);
		}
		
		private double getMinimumCostToDestination(N node) {
			double result = 0;
			if (pathCostBoundEvaluator != null) {
				result = pathCostBoundEvaluator.getMinimumCost(node, destinationNode);
			}
			return result;
		}
		
		private CostRecord<N, E> getCostRecord(N node) {
			return costMap.get(node);
		}
		
		private void addCostRecord(N node, double cost, E edge) {
			CostRecord<N, E> costRecord = new CostRecord<>();
			costRecord.node = node;
			costRecord.cost = cost;
			costRecord.minimumCostToDestination = getMinimumCostToDestination(node);
			costRecord.lastEdge = edge;
			costMap.put(node, costRecord);
			visitList.add(costRecord);
		}
		
		private void push(E edge) {
			double edgeCost = 1;
			if (edgeCostEvaluator != null) {
				edgeCost = edgeCostEvaluator.getEdgeCost(edge);
			}
			
			if (!Double.isInfinite(edgeCost)) {
				
				edgeCost += getCostRecord(graph.getOriginNode(edge)).cost;
				
				N targetNode = graph.getDestinationNode(edge);
				
				CostRecord<N, E> costRecord = getCostRecord(targetNode);
				
				if (costRecord == null) {
					addCostRecord(targetNode, edgeCost, edge);
				} else {
					if ((costRecord.cost < 0) || (edgeCost < costRecord.cost)) {
						costRecord.cost = edgeCost;
						costRecord.lastEdge = edge;
					}
				}
			}
		}
		
		private CostRecord<N, E> pop() {
			CostRecord<N, E> result = null;
			if (visitList.size() > 0) {
				Collections.sort(visitList, costComparator);
				result = visitList.remove(0);
			}
			return result;
		}
		
		private boolean destinationVisitedForLowerCost(double cost) {
			boolean result = false;
			CostRecord<N, E> costRecord = getCostRecord(destinationNode);
			if (costRecord != null) {
				if (costRecord.cost >= 0) {
					if (cost >= costRecord.cost) {
						result = true;
					}
				}
			}
			return result;
		}
		
		private void solve() {
			CostRecord<N, E> costRecord = pop();
			while (costRecord != null) {
				if (destinationVisitedForLowerCost(costRecord.cost)) {
					costRecord = null;
				} else {
					for (E edge : graph.getOutboundEdges(costRecord.node)) {
						push(edge);
					}
					costRecord = pop();
				}
			}
		}
		
		private void formPath() {
			List<E> edges = new ArrayList<>();
			CostRecord<N, E> costRecord = getCostRecord(destinationNode);
			
			if ((costRecord != null) && (costRecord.cost >= 0)) {
				N node = destinationNode;
				while (node != null) {
					costRecord = getCostRecord(node);
					E edge = costRecord.lastEdge;
					edges.add(edge);
					node = graph.getOriginNode(edge);
					if (node.equals(originNode)) {
						node = null;
					}
				}
				Collections.reverse(edges);
			}
			solution = Paths.newPath(edges);
		}
		
	}
	
	public static <N, E> Path<E> getShortestPath(Graph<N, E> graph, N originNode, N destinationNode, EdgeCostEvaluator<E> edgeCostEvaluator, PathCostBoundEvaluator<N> pathCostBoundEvaluator) {
		return new PathSolver<>(graph, originNode, destinationNode, edgeCostEvaluator, pathCostBoundEvaluator).solution;
	}
	
}
