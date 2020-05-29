package gcm.util.graph.bw;

import java.util.Set;

/**
 * A graph represents a set of non-null nodes connected by a set number of
 * non-null directed edges. All node and edge members of a Graph must be fully
 * compliant with the equals contract of Object.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 * @param <E>
 */
public interface Graph<N, E> {
	
	/**
	 * Returns true if and only if the node is contained in this graph. More
	 * formally, for any node n, containsNode(n) is true if and only if there is
	 * a node x contained in the graph where x.equals(n) is true.
	 * 
	 * @param node
	 * @return
	 */
	public boolean containsNode(Object node);
	
	/**
	 * Returns true if and only if the edge is contained in this graph. More
	 * formally, for any edge e, containsEdge(e) is true if and only if there is
	 * an edge x contained in the graph where x.equals(e) is true.
	 * 
	 * @param edge
	 * @return
	 */
	public boolean containsEdge(Object edge);
	
	public Set<N> getNodes();
	
	public Set<E> getEdges();
	
	public Set<E> getInboundEdges(N node);
	
	public int getInboundEdgeCount(N node);
	
	public Set<E> getOutboundEdges(N node);
	
	public Set<E> getEdges(N originNode, N destinationNode);
	
	public int edgeCount(N originNode, N destinationNode);
	
	public int getOutboundEdgeCount(N node);
	
	public N getOriginNode(E edge);
	
	public N getDestinationNode(E edge);
	
	public int edgeCount();
	
	public int nodeCount();
	
	public int inboundEdgeCount(N node);
	
	public int outboundEdgeCount(N node);
	
	public boolean isEmpty();
	
	@Override
	public int hashCode();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public String toString();
	
}
