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

	public static enum GraphCyclisity {
		/**
		 * The graph contains only cycles. There are no sources or sinks.
		 */
		CYCLIC,

		/**
		 * The graph contains at least one cycle and at least one source or
		 * sink.
		 */
		MIXED,

		/**
		 * The graph contains no cycles. Empty graphs are acyclic.
		 */
		ACYCLIC,

	}

	/**
	 * Returns the sub-graph of the input graph that has had all of its acyclic
	 * parts (sources and sinks) removed. That is, the resultant graph will
	 * contain only cycles. This method is commonly used to determine whether a
	 * graph is acyclic, i.e. the returned graph is empty, with no nodes.
	 */
	public static <N, E> Graph<N, E> getSourceSinkReducedGraph(Graph<N, E> graph) {
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
	 * Returns the GraphCyclisity of the given graph
	 * 
	 */
	public static <N, E> GraphCyclisity getGraphCyclisity(Graph<N, E> graph) {
		Graph<N, E> sourceSinkReducedGraph = getSourceSinkReducedGraph(graph);
		if(sourceSinkReducedGraph.isEmpty()) {
			return GraphCyclisity.ACYCLIC;
		}
		if(sourceSinkReducedGraph.nodeCount()==graph.nodeCount()) {
			return GraphCyclisity.CYCLIC;
		}
		return GraphCyclisity.MIXED;
	}


}
