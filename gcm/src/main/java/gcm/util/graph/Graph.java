package gcm.util.graph;

/**
 * Graph is a generics based interface for directed graphs. A graph is composed
 * of node objects and edge objects and acts as a non-null tolerant collection
 * over each of these types.
 * 
 * Each node in a graph is unique amongst the nodes. Formally, if nodes n1 and
 * n2 are two distinct returns from the getNodesIterator() iterator, then
 * n1.equals(n2) will always return false. Similarly, if edges e1 and e2 are two
 * distinct returns from the getEdgesIterator() iterator, then e1.equals(e2)
 * will always return false. While it may be rare to use the same class types
 * for both nodes and edges, it is permissible to do so. Uniqueness is not
 * enforced from nodes to edges, so it is possible and unambiguous to have an
 * object that is functioning as both a node and an edge.
 * 
 * Edges are uni-directional and are always associated with two nodes: an origin
 * node and a destination node, not necessarily distinct. Two nodes may be
 * connected by any number of distinct edges. Nodes may be associated with zero
 * to many edges.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 * @param <E>
 */
public interface Graph<N, E> {
	
	/**
	 * Returns true if and only if the node is contained in the graph.
	 * 
	 * @param node
	 * @return
	 */
	public boolean containsNode(Object node);
	
	/**
	 * Returns true if and only if the edge is contained in the graph.
	 * 
	 * @param edge
	 * @return
	 */
	public boolean containsEdge(Object edge);
	
	/**
	 * Returns true if and only if the given edge connects the given origin and
	 * destination.
	 * 
	 * @param edge
	 * @param origin
	 * @param destination
	 * @return
	 */
	public boolean formsEdgeRelationship(Object edge, Object origin, Object destination);
	
	/**
	 * Supplies an iterator over all nodes in the graph.
	 * 
	 * @return
	 */
	public Iterable<N> getNodes();
	
	/**
	 * Supplies an iterator over all edges in the graph.
	 * 
	 * @return
	 */
	public Iterable<E> getEdges();
	
	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * destination.
	 * 
	 * @param node
	 * @return
	 */
	public Iterable<E> getInboundEdges(N node);
	
	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getInboundEdgeCount(N node);
	
	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * origin.
	 * 
	 * @param node
	 * @return
	 */
	public Iterable<E> getOutboundEdges(N node);
	
	/**
	 * Supplies an iterable over the edges from the origin node to the
	 * destination node.
	 * 
	 * @param originNode
	 * @param destinationNode
	 * @return
	 */
	public Iterable<E> getEdges(N originNode, N destinationNode);
	/**
	 * Returns the number of edges in this graph from the origin node to the
	 * destination node.
	 * 
	 * @return
	 */
	public int edgeCount(N originNode, N destinationNode);
	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getOutboundEdgeCount(N node);
	
	/**
	 * Returns the origin node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getOriginNode(E edge);
	
	/**
	 * Returns the destination node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getDestinationNode(E edge);
	
	/**
	 * Returns the number of edges in this graph.
	 * 
	 * @return
	 */
	public int edgeCount();
	
	/**
	 * Returns the number of nodes in this graph.
	 * 
	 * @return
	 */
	public int nodeCount();
	
	/**
	 * Returns the number of edges that have the given node as their destination
	 * node.
	 * 
	 * @param node
	 * @return
	 */
	public int inboundEdgeCount(N node);
	
	/**
	 * Returns the number of edges that have the given node as their origin
	 * node.
	 * 
	 * @param node
	 * @return
	 */
	public int outboundEdgeCount(N node);
	
	/**
	 * Returns true if and only if the nodeCount() is zero
	 * 
	 * @return
	 */
	public boolean isEmpty();
	
}
