package gcm.util.graph;


public abstract class AbstractGraph<N, E> implements Graph<N, E> {
	
	@Override
	public int hashCode() {
		int result = 0;
		
		for (N node : getNodes()) {
			result += node.hashCode();
		}
		
		for (E edge : getEdges()) {
			result += edge.hashCode();
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof Graph)) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		Graph<N, E> other = (Graph<N, E>) obj;
		if (other.nodeCount() != nodeCount()) {
			return false;
		}
		if (other.edgeCount() != edgeCount()) {
			return false;
		}
		
		for (N node : getNodes()) {
			if (!other.containsNode(node)) {
				return false;
			}
		}
		
		for (E edge : getEdges()) {
			if (!other.containsEdge(edge)) {
				return false;
			}
			N originNode = getOriginNode(edge);
			N destinationNode = getDestinationNode(edge);
			other.formsEdgeRelationship(edge, originNode, destinationNode);
		}
		
		return true;
	}
	
}
