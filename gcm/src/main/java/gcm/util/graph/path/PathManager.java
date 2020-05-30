package gcm.util.graph.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.util.graph.Graph;
import gcm.util.graph.path.PathSolver.EdgeCostEvaluator;
import gcm.util.graph.path.PathSolver.TravelCostEvaluator;

/**
 * Manages shortest path solutions for a given graph with reasonable efficiency.
 * The PathManager class assumes that the underlying graph is not mutating and
 * that the paths that it derives can be reused in whole and in part to derive other
 * shortest paths and costs.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 * @param <E>
 */
public final class PathManager<N, E> {
	
	private Graph<N, E> graph;
	
	private EdgeCostEvaluator<E> edgeCostEvaluator;
	
	private TravelCostEvaluator<N> travelCostEvaluator;
	
	public PathManager(Graph<N, E> graph, EdgeCostEvaluator<E> edgeCostEvaluator, TravelCostEvaluator<N> travelCostEvaluator) {
		this.graph = graph;
		this.edgeCostEvaluator = edgeCostEvaluator;
		this.travelCostEvaluator = travelCostEvaluator;
	}
	
	private static class PathStep<K> {
		
		K next;
		
		double cost;
	}
	
	private Map<N, Map<N, PathStep<N>>> map = new LinkedHashMap<>();
	
	private PathStep<N> findPreSolvedPathStep(N origin, N destination) {
		Map<N, PathStep<N>> map2 = map.get(origin);
		if (map2 == null) {
			return null;
		}
		return map2.get(destination);
		
	}
	
	public boolean pathExists(N origin, N destination) {
		PathStep<N> pathStep = findPreSolvedPathStep(origin, destination);
		if (pathStep == null) {
			solve(origin, destination);
		}
		pathStep = findPreSolvedPathStep(origin, destination);
		return pathStep != null;
	}
	
	public N getNextNode(N origin, N destination) {
		PathStep<N> pathStep = findPreSolvedPathStep(origin, destination);
		if (pathStep == null) {
			solve(origin, destination);
		}
		pathStep = findPreSolvedPathStep(origin, destination);
		if (pathStep == null) {
			return null;
		}
		return pathStep.next;
	}
	
	public double getCost(N origin, N destination) {
		PathStep<N> pathStep = findPreSolvedPathStep(origin, destination);
		if (pathStep == null) {
			solve(origin, destination);
		}
		pathStep = findPreSolvedPathStep(origin, destination);
		if (pathStep == null) {
			return -1;
		}
		return pathStep.cost;
		
	}
	
	private int pathStepCount;
	
	private int solutionCount;
	
	public int getSolutionCount() {
		return solutionCount;
	}
	
	public int getPathStepCount() {
		return pathStepCount;
	}
	
	private void solve(N origin, N destination) {
		solutionCount++;
		Path< E> path = PathSolver.getPath(graph, origin, destination, edgeCostEvaluator, travelCostEvaluator);
		
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
			N next = destinationList.get(i);
			double cost = 0;
			for (int j = i; j < n; j++) {
				cost += costList.get(j);
				Map<N, PathStep<N>> innerMap = map.get(originList.get(i));
				if (innerMap == null) {
					innerMap = new LinkedHashMap<>();
					map.put(originList.get(i), innerMap);
				}
				PathStep<N> pathStep = innerMap.get(destinationList.get(j));
				
				if (pathStep == null) {
					pathStepCount++;
					pathStep = new PathStep<>();
					pathStep.next = next;
					pathStep.cost = cost;
					innerMap.put(destinationList.get(j), pathStep);
					
				}
			}
		}
		
	}
	
}
