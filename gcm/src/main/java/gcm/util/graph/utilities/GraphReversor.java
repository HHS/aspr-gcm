package gcm.util.graph.utilities;

import gcm.util.graph.Graph;
import gcm.util.graph.MutableGraph;

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

	public static <N, E> Graph<N, E> reverseGraph(Graph<N, E> graph) {

		MutableGraph<N, E> mutableGraph = new MutableGraph<>();

		for (N node : graph.getNodes()) {
			mutableGraph.addNode(node);
		}

		for (E edge : graph.getEdges()) {
			N destinationNode = graph.getDestinationNode(edge);
			N originNode = graph.getOriginNode(edge);
			mutableGraph.addEdge(edge, destinationNode, originNode);
		}
		return mutableGraph.asGraph();
	}
}
