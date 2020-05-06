package gcm.util.graph;


public class GenericGraph<N, E> extends AbstractGraph<N, E> {
	
	private MutableGraph<N, E> mutableGraph;
	
	private GenericGraph() {
	}
	
	public static <P, Q> GenericGraph<P, Q> getGenericGraph(Graph<P, Q> graph) {
		GenericGraph<P, Q> result = new GenericGraph<>();
		result.mutableGraph = new GenericMutableGraph<>();
		result.mutableGraph.addAll(graph);
		return result;
	}
	@Override
	public boolean containsNode(Object node) {
		return mutableGraph.containsNode(node);
	}
	@Override
	public boolean containsEdge(Object edge) {
		return mutableGraph.containsEdge(edge);
	}
	@Override
	public int edgeCount() {
		return mutableGraph.edgeCount();
	}
	@Override
	public N getDestinationNode(E edge) {
		return mutableGraph.getDestinationNode(edge);
	}
	@Override
	public N getOriginNode(E edge) {
		return mutableGraph.getOriginNode(edge);
	}
	@Override
	public int inboundEdgeCount(N node) {
		return mutableGraph.inboundEdgeCount(node);
	}
	@Override
	public int nodeCount() {
		return mutableGraph.nodeCount();
	}
	@Override
	public int outboundEdgeCount(N node) {
		return mutableGraph.outboundEdgeCount(node);
	}
	
	@Override
	public boolean isEmpty() {
		return mutableGraph.isEmpty();
	}
	
	@Override
	public int getInboundEdgeCount(N node) {
		return mutableGraph.getInboundEdgeCount(node);
	}
	
	@Override
	public int getOutboundEdgeCount(N node) {
		return mutableGraph.getOutboundEdgeCount(node);
	}
	
	@Override
	public boolean formsEdgeRelationship(Object edge, Object origin, Object destination) {
		return mutableGraph.formsEdgeRelationship(edge, origin, destination);
	}
	
	@Override
	public Iterable<N> getNodes() {
		return mutableGraph.getNodes();
	}
	
	@Override
	public Iterable<E> getEdges() {
		return mutableGraph.getEdges();
	}
	
	@Override
	public Iterable<E> getInboundEdges(N node) {
		return mutableGraph.getInboundEdges(node);
	}
	
	@Override
	public Iterable<E> getOutboundEdges(N node) {
		return mutableGraph.getOutboundEdges(node);
	}

	@Override
    public Iterable<E> getEdges(N originNode, N destinationNode) {
	    return mutableGraph.getEdges(originNode, destinationNode);
    }

	@Override
    public int edgeCount(N originNode, N destinationNode) {
	    return mutableGraph.edgeCount(originNode, destinationNode);
    }
	
}
