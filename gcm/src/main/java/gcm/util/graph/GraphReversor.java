package gcm.util.graph;


/**
 * Creates a Graph from a given graph that contains all the edges and nodes of
 * the original graph with all edges in reverse direction from the original.
 * 
 * @author Shawn Hatch
 * 
 */

public class GraphReversor {
	
	public static <N, E> Graph<N, E> reverseGraph(Graph<N, E> graph) {
		
		MutableGraph<N, E> mutableGraph = new GenericMutableGraph<>();
		
		
		for (N node : graph.getNodes()) {		
			mutableGraph.addNode(node);
		}
		
		for (E edge : graph.getEdges()) {		
			N destinationNode = graph.getDestinationNode(edge);
			N originNode = graph.getOriginNode(edge);			
			mutableGraph.addEdge(edge, destinationNode, originNode);
		}
		
		return GenericGraph.getGenericGraph(mutableGraph);
		
	}
}
