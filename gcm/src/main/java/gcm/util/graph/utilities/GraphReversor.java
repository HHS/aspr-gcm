package gcm.util.graph.utilities;

import gcm.util.graph.Graph;

/**
 * A static class for creating a Graph from a given graph that contains all the
 * edges and nodes of the original graph with all edges in reverse direction
 * from the original.
 * 
 * @author Shawn Hatch
 * 
 */

public final class GraphReversor {

	private GraphReversor() {

	}

	/**
	 * Returns a graph that has the same nodes and edges as the given graph,
	 * with each edge being reversed
	 */
	public static <N, E> Graph<N, E> getReverseGraph(Graph<N, E> graph) {

		Graph.Builder<N, E> builder = Graph.builder();

		for (N node : graph.getNodes()) {
			builder.addNode(node);
		}

		for (E edge : graph.getEdges()) {
			N destinationNode = graph.getDestinationNode(edge);
			N originNode = graph.getOriginNode(edge);
			builder.addEdge(edge, destinationNode, originNode);
		}
		return builder.build();
	}
}
