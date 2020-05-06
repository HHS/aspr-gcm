package gcm.util.graph;


/**
 * An extension of the Graph interface that provides for mutability.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 *            Node type
 * @param <E>
 *            Edge type
 */

public interface MutableGraph<N, E> extends Graph<N, E> {
	
	public void addNode(N node);
	
	public void removeNode(N node);
	
	public void addEdge(E edge, N originNode, N destinationNode);
	
	public void removeEdge(E edge);
	
	public void addAll(Graph<N, E> graph);
	
}
