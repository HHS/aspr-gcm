package gcm.util.graph;

import java.util.ArrayList;
import java.util.List;

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
	 * returned graph is degenerate, with no nodes.
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
		MutableGraph<N, E> mutableGraph = new GenericMutableGraph<>();
		mutableGraph.addAll(graph);
		
		while (true) {
			boolean found = false;
			
			List<N> nodes = new ArrayList<>();
			for (N node : mutableGraph.getNodes()) {
				nodes.add(node);
			}
			
			for (N node : nodes) {
				if (mutableGraph.inboundEdgeCount(node) == 0) {
					mutableGraph.removeNode(node);
					found = true;
				}
			}
			if (!found) {
				break;
			}
		}
		return GenericGraph.getGenericGraph(mutableGraph);
	}
	
	/**
	 * Returns true if and only if the given graph is acyclic. Note that
	 * degenerate graphs having no nodes are considered acyclic.
	 * 
	 * @param <N>
	 * @param <E>
	 * @param graph
	 * @return
	 */
	
	public static <N, E> boolean isAcyclicGraph(Graph<N, E> graph) {
		return reduceGraph(graph).nodeCount() == 0;
	}
	
	// public static <N, E> Graph<N, E> reduceGraph(Graph<N, E> graph) {
	// MutableGraph<N, E> mutableGraph = new GenericMutableGraph<N, E>();
	// mutableGraph.addAll(graph);
	// boolean found = true;
	// while (found) {
	// found = false;
	// Iterator<N> nodeItr = mutableGraph.getNodesIterator();
	// List<N> nodes = new ArrayList<N>();
	// while (nodeItr.hasNext()) {
	// nodes.add(nodeItr.next());
	// }
	// for (N node : nodes) {
	// if (mutableGraph.inboundEdgeCount(node) == 0) {
	// found = true;
	// mutableGraph.removeNode(node);
	// }
	// }
	// }
	// return GenericGraph.getGenericGraph(mutableGraph);
	// }
}
