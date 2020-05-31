package gcm.util.graph.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.util.graph.Graph;
import gcm.util.graph.Path;
import gcm.util.graph.Path.EdgeCostEvaluator;
import gcm.util.graph.Path.TravelCostEvaluator;

/**
 * Manages shortest path solutions for a given graph with reasonable efficiency.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 * @param <E>
 */
public final class ArrayPathManager<N, E> {

	private Graph<N, E> graph;

	private EdgeCostEvaluator<E> edgeCostEvaluator;

	private TravelCostEvaluator<N> travelCostEvaluator;

	public ArrayPathManager(Graph<N, E> graph, EdgeCostEvaluator<E> edgeCostEvaluator, TravelCostEvaluator<N> travelCostEvaluator) {
		this.graph = graph;
		this.edgeCostEvaluator = edgeCostEvaluator;
		this.travelCostEvaluator = travelCostEvaluator;
		int n = graph.nodeCount();
		navigationArray = new int[n + 1][n + 1];
		costArray = new double[n + 1][n + 1];
		for (N node : graph.getNodes()) {
			int index = nodeMap.size() + 1;
			nodeMap.put(node, index);
			indexMap.put(index, node);
		}
	}

	private int[][] navigationArray;
	private double[][] costArray;
	private Map<N, Integer> nodeMap = new LinkedHashMap<>();
	private Map<Integer, N> indexMap = new LinkedHashMap<>();

	public boolean pathExists(N origin, N destination) {
		int originIndex = nodeMap.get(origin);
		int destinationIndex = nodeMap.get(destination);
		int nextIndex = navigationArray[originIndex][destinationIndex];
		return nextIndex != 0;
	}

	public N getNextNode(N origin, N destination) {
		int originIndex = nodeMap.get(origin);
		int destinationIndex = nodeMap.get(destination);
		int nextIndex = navigationArray[originIndex][destinationIndex];
		if (nextIndex == 0) {
			solve(origin, destination);
		}
		nextIndex = navigationArray[originIndex][destinationIndex];
		if (nextIndex == 0) {
			return null;
		}
		return indexMap.get(nextIndex);
	}

	public double getCost(N origin, N destination) {
		int originIndex = nodeMap.get(origin);
		int destinationIndex = nodeMap.get(destination);
		int nextIndex = navigationArray[originIndex][destinationIndex];
		if (nextIndex == 0) {
			solve(origin, destination);
		}
		return costArray[originIndex][destinationIndex];
	}

	private void solve(N origin, N destination) {

		Path<E> path = PathSolver.getPath(graph, origin, destination, edgeCostEvaluator, travelCostEvaluator);

		if (path == null) {

			return;
		}

		List<N> originList = new ArrayList<>();
		List<N> destinationList = new ArrayList<>();
		List<Double> costList = new ArrayList<>();
		for (E edge : path.getEdges()) {
			originList.add(graph.getOriginNode(edge));
			destinationList.add(graph.getDestinationNode(edge));
			costList.add(edgeCostEvaluator.getEdgeCost(edge));
		}

		int n = originList.size();
		for (int i = 0; i < n; i++) {
			int sourceIndex = nodeMap.get(originList.get(i));
			int nextIndex = nodeMap.get(destinationList.get(i));
			double cost = 0;
			for (int j = i; j < n; j++) {
				cost += costList.get(j);
				int targetIndex = nodeMap.get(destinationList.get(j));
				navigationArray[sourceIndex][targetIndex] = nextIndex;
				costArray[sourceIndex][targetIndex] = cost;
			}
		}
		
	}

}
