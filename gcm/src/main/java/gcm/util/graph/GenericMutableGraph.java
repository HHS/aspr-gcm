package gcm.util.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 
 * A simple implementation of the MutableGraph. All iterators are immutable and
 * will throw UnsupportedOperationException runtime exception on remove().
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 *            Node type
 * @param <E>
 *            Edge type
 */
public final class GenericMutableGraph<N, E> extends AbstractGraph<N, E> implements MutableGraph<N, E> {
	
	private Set<N> nodes = new LinkedHashSet<>();
	
	private Set<E> edges = new LinkedHashSet<>();
	
	private Map<N, Set<E>> inEdges = new LinkedHashMap<>();
	
	private Map<N, Set<E>> outEdges = new LinkedHashMap<>();
	
	private Map<E, N> originNodeMap = new LinkedHashMap<>();
	
	private Map<E, N> destinationNodeMap = new LinkedHashMap<>();
	
	private Map<N, Map<N, Set<E>>> edgesMap = new LinkedHashMap<>();
	
	@Override
	public void addAll(Graph<N, E> graph) {
		
		for (N node : graph.getNodes()) {
			addNode(node);
		}
		
		for (E edge : graph.getEdges()) {
			N originNode = graph.getOriginNode(edge);
			N destinationNode = graph.getDestinationNode(edge);
			addEdge(edge, originNode, destinationNode);
		}
		
	}
	
	@Override
	public void addEdge(E edge, N originNode, N destinationNode) {
		removeEdge(edge);
		addNode(originNode);
		addNode(destinationNode);
		edges.add(edge);
		inEdges.get(destinationNode).add(edge);
		outEdges.get(originNode).add(edge);
		originNodeMap.put(edge, originNode);
		destinationNodeMap.put(edge, destinationNode);
		
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			map = new LinkedHashMap<>();
			edgesMap.put(originNode, map);
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			someEdges = new LinkedHashSet<>();
			map.put(destinationNode, someEdges);
		}
		someEdges.add(edge);
		
	}
	
	@Override
	public void addNode(N node) {
		if (!nodes.contains(node)) {
			nodes.add(node);
			inEdges.put(node, new LinkedHashSet<E>());
			outEdges.put(node, new LinkedHashSet<E>());
		}
	}
	
	@Override
	public boolean containsEdge(Object edge) {
		return edges.contains(edge);
	}
	
	@Override
	public boolean containsNode(Object node) {
		return nodes.contains(node);
	}
	
	@Override
	public int edgeCount() {
		return edges.size();
	}
	
	@Override
	public boolean formsEdgeRelationship(Object edge, Object origin, Object destination) {
		N originNode = originNodeMap.get(edge);
		if (originNode == null) {
			return false;
		}
		if (!originNode.equals(origin)) {
			return false;
		}
		N destinationNode = destinationNodeMap.get(edge);
		
		if (!destinationNode.equals(destination)) {
			return false;
		}
		return true;
	}
	
	@Override
	public N getDestinationNode(E edge) {
		return destinationNodeMap.get(edge);
	}
	
	@Override
	public Iterable<E> getEdges() {
		return new ImmutableIterable<>(edges);
	}
	
	@Override
	public Iterable<E> getEdges(N originNode, N destinationNode) {
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			return new ImmutableIterable<>(null);
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			return new ImmutableIterable<>(null);
		}
		return new ImmutableIterable<>(someEdges);
	}
	
	@Override
	public int getInboundEdgeCount(N node) {
		Set<E> set = inEdges.get(node);
		if (set == null) {
			return 0;
		}
		return set.size();
	}
	
	@Override
	public Iterable<E> getInboundEdges(N node) {
		Set<E> set = inEdges.get(node);
		if (set == null) {
			return new ImmutableIterable<>(null);
		}
		return new ImmutableIterable<>(inEdges.get(node));
	}
	
	@Override
	public Iterable<N> getNodes() {
		return new ImmutableIterable<>(nodes);
	}
	
	@Override
	public N getOriginNode(E edge) {
		return originNodeMap.get(edge);
	}
	
	@Override
	public int getOutboundEdgeCount(N node) {
		Set<E> set = outEdges.get(node);
		if (set == null) {
			return 0;
		}
		return set.size();
		
	}
	
	@Override
	public Iterable<E> getOutboundEdges(N node) {
		Set<E> set = outEdges.get(node);
		if (set == null) {
			return new ImmutableIterable<>(null);
		}
		return new ImmutableIterable<>(outEdges.get(node));
	}
	
	@Override
	public int inboundEdgeCount(N node) {
		Set<E> edges = inEdges.get(node);
		if (edges == null) {
			return 0;
		}
		return edges.size();
	}
	
	@Override
	public boolean isEmpty() {
		return nodeCount() == 0;
	}
	
	@Override
	public int nodeCount() {
		return nodes.size();
	}
	
	@Override
	public int outboundEdgeCount(N node) {
		Set<E> edges = outEdges.get(node);
		if (edges == null) {
			return 0;
		}
		return edges.size();
	}
	
	@Override
	public void removeEdge(E edge) {
		if (!edges.contains(edge)) {
			return;
		}
		
		N originNode = originNodeMap.get(edge);
		outEdges.get(originNode).remove(edge);
		
		N destinationNode = destinationNodeMap.get(edge);
		inEdges.get(destinationNode).remove(edge);
		
		edges.remove(edge);
		destinationNodeMap.remove(edge);
		originNodeMap.remove(edge);
		
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map != null) {
			Set<E> someEdges = map.get(destinationNode);
			if (someEdges != null) {
				someEdges.remove(edge);
				if (someEdges.size() == 0) {
					map.remove(destinationNode);
				}
			}
			if (map.size() == 0) {
				edgesMap.remove(originNode);
			}
		}
	}
	
	@Override
	public void removeNode(N node) {
		if (!nodes.contains(node)) {
			return;
		}
		List<E> list = new ArrayList<>();
		list.addAll(inEdges.get(node));
		list.addAll(outEdges.get(node));
		for (E edge : list) {
			removeEdge(edge);
		}
		inEdges.remove(node);
		outEdges.remove(node);
		nodes.remove(node);
		
	}
	
	@Override
	public int edgeCount(N originNode, N destinationNode) {
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			return 0;
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			return 0;
		}
		return someEdges.size();
		
	}
	
}
