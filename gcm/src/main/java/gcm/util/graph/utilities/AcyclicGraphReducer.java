package gcm.util.graph.utilities;

import gcm.util.graph.Graph;
import gcm.util.graph.MutableGraph;

/**
 * A static class that reduces a graph to its cyclic components and analyzes
 * graphs for acyclicity.
 * 
 * @author Shawn Hatch
 * 
 */
public final class AcyclicGraphReducer {
	
	/**
	 * Returns the sub-graph of the input graph that has all acyclic parts
	 * removed. That is, the resultant graph will contain only cycles. This
	 * method is commonly used to determine whether a graph is acyclic, i.e. the
	 * returned graph is empty, with no nodes.
	 * 
	 * @param <N>
	 *            Node type
	 * @param <E>
	 *            Edge type
	 * @param graph
	 *            The graph to reduce
	 * @return The reduced graph
	 */
	
	public static <N, E> Graph<N, E> reduceGraph(Graph<N, E> graph) {
		
		MutableGraph<N, E> mutableGraph = new MutableGraph<>();
		mutableGraph.addAll(graph);
		boolean nodeRemoved = true;
		while (nodeRemoved) {
			nodeRemoved = false;
			for (N node : mutableGraph.getNodes()) {
				if ((mutableGraph.getInboundEdgeCount(node) == 0) || (mutableGraph.getOutboundEdgeCount(node) == 0)) {
					mutableGraph.removeNode(node);
					nodeRemoved = true;
				}
			}
		}
		return mutableGraph.asGraph();				
	}
	
	/**
	 * Returns true if and only if the given graph is acyclic. Note that
	 * empty graphs having no nodes are considered acyclic.
	 * 
	 */	
	public static <N, E> boolean isAcyclicGraph(Graph<N, E> graph) {
		return reduceGraph(graph).isEmpty();
	}

	/**
	 * Returns true if and only if the given graph is fully cyclic. Note that
	 * empty graphs having no nodes are considered acyclic.
	 * 
	 */
	public static <N, E> boolean isCyclicGraph(Graph<N, E> graph) {
		return reduceGraph(graph).nodeCount() == graph.nodeCount();
	}
	
	
}
