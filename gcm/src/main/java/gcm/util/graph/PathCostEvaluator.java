package gcm.util.graph;

import gcm.util.graph.GraphPathSolver.EdgeCostEvaluator;

public class PathCostEvaluator {
	
	public static <E> double getPathCost(Path< E> path, EdgeCostEvaluator<E> edgeCostEvaluator) {
		double result = 0;
		for(E edge : path.getEdges()){
			result += edgeCostEvaluator.getEdgeCost(edge);
		}
		return result;		
	}

}
