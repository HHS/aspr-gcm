package gcm.util.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.graph.Graph;
import gcm.util.path.Path.Builder;

/**
 * 
 * Solves a shortest path through a graph from an origin node to a destination
 * node.The solver uses two optional, auxiliary, client supplied objects: 1) an
 * EdgeCostEvaluator which returns the cost of an edge and 2) a
 * TravelCostEvaluator which determines the shortest possible cost across the
 * graph from one node to another.
 * 
 * The EdgeCostEvaluator should always return a non-negative value and should
 * return a stable value over the life-span of this utility. If the
 * EdgeCostEvaluator is null, the cost of an edge is set arbitrarily to 1.
 * 
 * The TravelCostEvaluator is also optional and exists to give the solver
 * insight into long distance costs. It should return a stable non-negative
 * value.
 * 
 * A typical TravelCostEvaluator example would be for nodes in a graph that
 * represent physical positions. The TravelCostEvaluator could return the
 * straight-line minimum distance between nodes. Note that the nodes do not have
 * to share an edge nor even be connected in the graph. By supplying a
 * TravelCostEvaluator, the client can greatly improve this solver's performance
 * in large networks where nodes represent positions in space. Essentially, the
 * TravelCostEvaluator allows the algorithm to expand its exploration of the
 * graph in an ellipse whose major axis is aligned to the origin and
 * destination, rather than doing a expanding sphere search.
 * 
 * If a path cannot be found then a degenerate, node-less path is returned.
 * 
 * @author Shawn Hatch
 * 
 */
@Source(status = TestStatus.REQUIRED)
public final class Paths {
	
	public interface EdgeCostEvaluator<E> {		
		public double getEdgeCost(E edge);
	}
	
	public static interface TravelCostEvaluator<N> {		
		public double getMinimumCost(N originNode, N destination);
	}
	
	
	private static class Node<E> {
		
		boolean visited;
		
		double cost;
		
		double auxillaryCost;
		
		E edge;
		
		double totalCost() {
			return cost + auxillaryCost;
		}
	}
	
	private Paths() {
		
	}
		
	public static <N,E> Optional<Path<E>> getPath(
			Graph<N, E> graph,
			N originNode,
			N destinationNode,
			EdgeCostEvaluator<E> edgeCostEvaluator,
			TravelCostEvaluator<N> travelCostEvaluator) {
		
		if (!graph.containsNode(originNode)) {
			return Optional.empty();
		}
		
		if (!graph.containsNode(destinationNode)) {
			return Optional.empty();
		}
		
		final Map<N, Node<E>> map = new LinkedHashMap<>();
		List<N> visitList = new ArrayList<>();
		
		visitList.add(originNode);
		Node<E> origin = new Node<>();
		
		origin.auxillaryCost = travelCostEvaluator.getMinimumCost(originNode, destinationNode);
		
		map.put(originNode, origin);
		
		Comparator<N> comp = new Comparator<N>() {
			@Override
			public int compare(N o1, N o2) {
				double cost1 = map.get(o1).totalCost();
				double cost2 = map.get(o2).totalCost();
				if (cost1 < cost2) {
					return -1;
				}
				if (cost2 < cost1) {
					return 1;
				}
				return 0;
			}
		};
		
		while (visitList.size() > 0) {
			// sort the visitList
			Collections.sort(visitList, comp);
			
			// pop off the first element
			N node = visitList.get(0);
			visitList.remove(0);
			Node<E> pushNode = map.get(node);
			
			Node<E> destination = map.get(destinationNode);
			if (destination != null) {
				if (destination.visited) {
					if (pushNode.cost >= destination.cost) {
						break;
					}
				}
			}
			
			for(E edge : graph.getOutboundEdges(node)) {				
				double edgeCost = edgeCostEvaluator.getEdgeCost(edge);
				
				if(Double.isInfinite(edgeCost)){
					continue;
				}
				edgeCost += pushNode.cost;
				N targetNode = graph.getDestinationNode(edge);
				Node<E> target = map.get(targetNode);
				if (target == null) {
					target = new Node<>();
					target.cost = edgeCost;
					if (travelCostEvaluator != null) {
						target.auxillaryCost = travelCostEvaluator.getMinimumCost(targetNode, destinationNode);
					}
					target.edge = edge;
					target.visited = true;
					map.put(targetNode, target);
					visitList.add(targetNode);
				} else {
					if ((!target.visited) || (edgeCost < target.cost)) {
						target.visited = true;
						target.cost = edgeCost;
						target.edge = edge;
					}
				}
			}
		}
		
		Node<E> destination = map.get(destinationNode);
		List<E> edges = new ArrayList<>();
		// assess whether we have a solution
		
		if ((destination != null) && (destination.visited)) {
			
			N node = destinationNode;
			
			while (true) {
				
				Node<E> visitedNode = map.get(node);
				E edge = visitedNode.edge;
				visitedNode.edge = null;
				if (edge == null) {
					break;
				}
				edges.add(edge);
				node = graph.getOriginNode(edge);
				if (node.equals(originNode)) {
					break;
				}
			}
			Collections.reverse(edges);
		}
		
		Builder<E> builder = Path.builder();
		for(E edge : edges) {
			builder.addEdge(edge);
		}
		
		return Optional.of(builder.build());
	}
	
	public static <E> double getCost(Path<E> path, EdgeCostEvaluator<E> edgeCostEvaluator) {
		double result = 0;
		for(E edge : path.getEdges()){
			result += edgeCostEvaluator.getEdgeCost(edge);
		}
		return result;		
	}
	
}