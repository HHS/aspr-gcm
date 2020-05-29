package gcm.util.graph.bw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.util.MultiKey;
import gcm.util.graph.bw.Paths.EdgeCostEvaluator;
import gcm.util.graph.bw.Paths.PathCostBoundEvaluator;



public class FastPathSolver<N, E> {
	
	private static class SubPath<T> {
		
		Path<T> basePath;
		
		Path<T> solvedPath;
		
		int startIndex;
		
		int stopIndex;
	}
	
	private Map<MultiKey, Path<E>> pathMap;
	
	private Graph<N, E> graph;
	
	private Map<MultiKey, SubPath<E>> subPathMap = new LinkedHashMap<>();
	
	private EdgeCostEvaluator<E> edgeCostEvaluator;
	
	private PathCostBoundEvaluator<N> pathCostBoundEvaluator;
	
	public FastPathSolver(Graph<N, E> graph, EdgeCostEvaluator<E> edgeCostEvaluator, PathCostBoundEvaluator<N> pathCostBoundEvaluator) {
		if (graph == null) {
			throw new IllegalArgumentException("graph cannot be null");
		}
		this.graph = Graphs.newGraph(graph);
		this.edgeCostEvaluator = edgeCostEvaluator;
		this.pathCostBoundEvaluator = pathCostBoundEvaluator;
	}
	
	public int size() {
		return pathMap.size();
	}
	
	public Path<E> getPath(N originNode, N destinationNode) {
		Path<E> result = null;
		MultiKey key = new MultiKey(originNode, destinationNode);
		SubPath<E> subPath = subPathMap.get(key);
		if (subPath != null) {
			if (subPath.solvedPath == null) {
				List<E> edges = subPath.basePath.getEdges();
				List<E> subEdges = new ArrayList<>();
				for (int i = subPath.startIndex; i < subPath.stopIndex; i++) {
					subEdges.add(edges.get(i));
				}
				subPath.solvedPath = Paths.newPath(subEdges);
			}
			result = subPath.solvedPath;
		} else {
			result = Paths.getShortestPath(graph, originNode, destinationNode, edgeCostEvaluator, pathCostBoundEvaluator);
			List<E> edges = result.getEdges();
			for (int i = 0; i < edges.size(); i++) {
				N startNode = graph.getOriginNode(edges.get(i));
				for (int j = i; j < edges.size(); j++) {
					N endNode = graph.getDestinationNode(edges.get(j));
					key = new MultiKey(startNode, endNode);
					subPath = new SubPath<>();
					subPath.basePath = result;
					subPath.startIndex = i;
					subPath.stopIndex = j + 1;
					subPathMap.put(key, subPath);
				}
			}			
		}		
		pathMap.put(key, result);		
		return result;
	}
	
}
