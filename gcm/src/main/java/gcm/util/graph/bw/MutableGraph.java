package gcm.util.graph.bw;

public interface MutableGraph<N, E> extends Graph<N, E> {
	
	public boolean addNode(N node);
	
	public boolean removeNode(N node);
	
	public boolean addEdge(E edge, N originNode, N destinationNode);
	
	public boolean removeEdge(E edge);
	
	public void addAll(Graph<N, E> graph);
		
}
