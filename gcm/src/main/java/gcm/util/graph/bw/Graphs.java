package gcm.util.graph.bw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.util.MultiKey;



public class Graphs {
	
	private static class EdgeInfo<N> {
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("EdgeInfo");
			sb.append(" origin ");
			sb.append(originNode);
			sb.append(" destination ");
			sb.append(destinationNode);
			return sb.toString();
		}
		
		private final N originNode;
		
		private final N destinationNode;
		
		private EdgeInfo(N originNode, N destinationNode) {
			this.originNode = originNode;
			this.destinationNode = destinationNode;
		}
		
	}
	
	private static class ImmutableGraph<N, E> implements Graph<N, E> {
		
		private final Graph<N, E> graph;
		
		private ImmutableGraph(Graph<N, E> graph) {
			this.graph = graph;
		}
		
		@Override
		public boolean containsEdge(Object edge) {
			return graph.containsEdge(edge);
		}
		
		@Override
		public boolean containsNode(Object node) {
			return graph.containsNode(node);
		}
		
		@Override
		public int edgeCount() {
			return graph.edgeCount();
		}
		
		@Override
		public int edgeCount(N originNode, N destinationNode) {
			return graph.edgeCount(originNode, destinationNode);
		}
		
		@Override
		public N getDestinationNode(E edge) {
			return graph.getDestinationNode(edge);
		}
		
		@Override
		public Set<E> getEdges() {
			return graph.getEdges();
		}
		
		@Override
		public Set<E> getEdges(N originNode, N destinationNode) {
			return graph.getEdges(originNode, destinationNode);
		}
		
		@Override
		public int getInboundEdgeCount(N node) {
			return graph.getInboundEdgeCount(node);
		}
		
		@Override
		public Set<E> getInboundEdges(N node) {
			return graph.getInboundEdges(node);
		}
		
		@Override
		public Set<N> getNodes() {
			return graph.getNodes();
		}
		
		@Override
		public N getOriginNode(E edge) {
			return graph.getOriginNode(edge);
		}
		
		@Override
		public int getOutboundEdgeCount(N node) {
			return graph.getOutboundEdgeCount(node);
		}
		
		@Override
		public Set<E> getOutboundEdges(N node) {
			return graph.getOutboundEdges(node);
		}
		
		@Override
		public int inboundEdgeCount(N node) {
			return graph.inboundEdgeCount(node);
		}
		
		@Override
		public boolean isEmpty() {
			return graph.isEmpty();
		}
		
		@Override
		public int nodeCount() {
			return graph.nodeCount();
		}
		
		@Override
		public int outboundEdgeCount(N node) {
			return graph.outboundEdgeCount(node);
		}
		
		@Override
		public String toString() {
			return graph.toString();
		}
		
	}
	
	private static class MutableGraphImpl<N, E> implements MutableGraph<N, E> {
		
		private final Map<E, EdgeInfo<N>> edgeInfoMap = new LinkedHashMap<>();
		
		private final Map<N, NodeInfo<E>> nodeInfoMap = new LinkedHashMap<>();
		
		private final Map<MultiKey, NodePairInfo<E>> nodePairInfoMap = new LinkedHashMap<>();
		
		private boolean invariantsSatisfied() {
			
			assert edgeInfoMap != null;
			assert nodeInfoMap != null;
			assert nodePairInfoMap != null;
			
			for (E edge : edgeInfoMap.keySet()) {
				assert edge != null;
				EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
				assert edgeInfo != null;
				N originNode = edgeInfo.originNode;
				assert originNode != null;
				assert nodeInfoMap.containsKey(originNode);
				NodeInfo<E> nodeInfo = nodeInfoMap.get(originNode);
				assert nodeInfo != null;
				assert nodeInfo.exitingEdges != null;
				assert nodeInfo.exitingEdges.contains(edge);
				N destinationNode = edgeInfo.destinationNode;
				assert destinationNode != null;
				assert nodeInfoMap.containsKey(destinationNode);
				nodeInfo = nodeInfoMap.get(destinationNode);
				assert nodeInfo != null;
				assert nodeInfo.enteringEdges != null;
				assert nodeInfo.enteringEdges.contains(edge);
			}
			
			for (N node : nodeInfoMap.keySet()) {
				assert node != null;
				NodeInfo<E> nodeInfo = nodeInfoMap.get(node);
				assert nodeInfo != null;
				assert nodeInfo.enteringEdges != null;
				for (E edge : nodeInfo.enteringEdges) {
					assert edge != null;
					assert edgeInfoMap.containsKey(edge);
					EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
					assert edgeInfo != null;
					assert node.equals(edgeInfo.destinationNode);
				}
				for (E edge : nodeInfo.exitingEdges) {
					assert edge != null;
					assert edgeInfoMap.containsKey(edge);
					EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
					assert edgeInfo != null;
					assert node.equals(edgeInfo.originNode);
				}
			}
			
			
			int edgeCount = 0;
			for(NodePairInfo<E> nodePairInfo : nodePairInfoMap.values()){
				assert nodePairInfo != null;
				assert nodePairInfo.edges != null;
				edgeCount += nodePairInfo.edges.size();
			}
			assert edgeCount == edgeInfoMap.size();
			
			for (E edge : edgeInfoMap.keySet()) {
				EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
				MultiKey orderedKey = new MultiKey(edgeInfo.originNode, edgeInfo.destinationNode);
				NodePairInfo<E> nodePairInfo = nodePairInfoMap.get(orderedKey);
				assert nodePairInfo != null;
				assert nodePairInfo.edges != null;
				assert nodePairInfo.edges.contains(edge);
			}
			
			return true;
		}
		
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
			assert invariantsSatisfied();
		}
		
		@Override
		public boolean addEdge(E edge, N originNode, N destinationNode) {
			
			Set<E> edges = getInternalEdges(originNode, destinationNode);
			boolean result = (edges == null)||(!edges.contains(edge));
			
			if (result) {
				removeEdge(edge);
				addNode(originNode);
				addNode(destinationNode);
				
				NodeInfo<E> nodeInfo = nodeInfoMap.get(originNode);
				nodeInfo.exitingEdges.add(edge);
				
				nodeInfo = nodeInfoMap.get(destinationNode);
				nodeInfo.enteringEdges.add(edge);
				
				edgeInfoMap.put(edge, new EdgeInfo<>(originNode, destinationNode));
				
				MultiKey key = new MultiKey(originNode, destinationNode);
				NodePairInfo<E> nodePairInfo = nodePairInfoMap.get(key);
				if (nodePairInfo == null) {
					nodePairInfo = new NodePairInfo<>();
					nodePairInfoMap.put(key, nodePairInfo);
				}
				nodePairInfo.edges.add(edge);
				
			}
			
			assert invariantsSatisfied();
			
			assert containsEdge(edge);
			assert getOriginNode(edge).equals(originNode);
			assert getDestinationNode(edge).equals(destinationNode);
			
			return result;
		}
		
		@Override
		public boolean addNode(N node) {
			boolean result = false;
			if (!containsNode(node)) {
				result = true;
				nodeInfoMap.put(node, new NodeInfo<E>());
			}
			assert containsNode(node);
			assert invariantsSatisfied();
			return result;
		}
		
		@Override
		public boolean containsEdge(Object edge) {
			return edgeInfoMap.containsKey(edge);
		}
		
		@Override
		public boolean containsNode(Object node) {
			return nodeInfoMap.containsKey(node);
		}
		
		@Override
		public int edgeCount() {
			return edgeInfoMap.size();
		}
		
		@Override
		public int edgeCount(N originNode, N destinationNode) {
			int result = 0;
			Set<E> edgeSet = getInternalEdges(originNode, destinationNode);
			if (edgeSet != null) {
				result = edgeSet.size();
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
				
				if (!originNode.equals(other.getOriginNode(edge))) {
					return false;
				}
				
				N destinationNode = getDestinationNode(edge);
				
				if (!destinationNode.equals(other.getDestinationNode(edge))) {
					return false;
				}
				
			}
			
			return true;
		}
		
		@Override
		public N getDestinationNode(E edge) {
			N result = null;
			EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
			if (edgeInfo != null) {
				result = edgeInfo.destinationNode;
			}
			return result;
		}
		
		@Override
		public Set<E> getEdges() {
			return new LinkedHashSet<>(edgeInfoMap.keySet());
		}
		
		@Override
		public Set<E> getEdges(N originNode, N destinationNode) {
			Set<E> result = new LinkedHashSet<>();
			Set<E> edgeSet = getInternalEdges(originNode, destinationNode);
			if (edgeSet != null) {
				result.addAll(edgeSet);
			}
			return result;
		}
		
		@Override
		public int getInboundEdgeCount(N node) {
			int result = 0;
			if (containsNode(node)) {
				result = nodeInfoMap.get(node).enteringEdges.size();
			}
			return result;
		}
		
		@Override
		public Set<E> getInboundEdges(N node) {
			Set<E> result = new LinkedHashSet<>();
			if (containsNode(node)) {
				result.addAll(nodeInfoMap.get(node).enteringEdges);
			}
			return result;
		}
		
		private Set<E> getInternalEdges(N originNode, N destinationNode) {
			Set<E> result = null;
			NodePairInfo<E> nodePairInfo = nodePairInfoMap.get(new MultiKey(originNode, destinationNode));
			if (nodePairInfo != null) {
				result = nodePairInfo.edges;
			}
			return result;
		}
		
		@Override
		public Set<N> getNodes() {
			return new LinkedHashSet<>(nodeInfoMap.keySet());
		}
		
		@Override
		public N getOriginNode(E edge) {
			N result = null;
			EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
			if (edgeInfo != null) {
				result = edgeInfo.originNode;
			}
			return result;
		}
		
		@Override
		public int getOutboundEdgeCount(N node) {
			int result = 0;
			if (containsNode(node)) {
				result = nodeInfoMap.get(node).exitingEdges.size();
			}
			return result;
		}
		
		@Override
		public Set<E> getOutboundEdges(N node) {
			Set<E> result = new LinkedHashSet<>();
			if (containsNode(node)) {
				result.addAll(nodeInfoMap.get(node).exitingEdges);
			}
			return result;
		}
		
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
		public int inboundEdgeCount(N node) {
			int result = 0;
			if (containsNode(node)) {
				result = nodeInfoMap.get(node).enteringEdges.size();
			}
			return result;
		}
		
		@Override
		public boolean isEmpty() {
			return nodeCount() == 0;
		}
		
		@Override
		public int nodeCount() {
			return nodeInfoMap.keySet().size();
		}
		
		@Override
		public int outboundEdgeCount(N node) {
			int result = 0;
			if (containsNode(node)) {
				result = nodeInfoMap.get(node).exitingEdges.size();
			}
			return result;
		}
		
		@Override
		public boolean removeEdge(E edge) {
			boolean result = false;
			if (containsEdge(edge)) {
				result = true;
				EdgeInfo<N> edgeInfo = edgeInfoMap.remove(edge);
				NodeInfo<E> nodeInfo = nodeInfoMap.get(edgeInfo.originNode);
				nodeInfo.exitingEdges.remove(edge);
				nodeInfo = nodeInfoMap.get(edgeInfo.destinationNode);
				nodeInfo.enteringEdges.remove(edge);
				
				MultiKey key = new MultiKey(edgeInfo.originNode, edgeInfo.destinationNode);
				NodePairInfo<E> nodePairInfo = nodePairInfoMap.get(key);
				nodePairInfo.edges.remove(edge);
				if (nodePairInfo.edges.size() == 0) {
					nodePairInfoMap.remove(key);
				}
			}
			assert !containsEdge(edge);
			assert invariantsSatisfied();
			return result;
		}
		
		@Override
		public boolean removeNode(N node) {
			boolean result = false;
			if (containsNode(node)) {
				result = true;
				for (E edge : getInboundEdges(node)) {
					removeEdge(edge);
				}
				for (E edge : getOutboundEdges(node)) {
					removeEdge(edge);
				}
				nodeInfoMap.remove(node);
			}
			
			assert !containsNode(node);
			
			assert invariantsSatisfied();
			
			return result;
		}
		
		@Override
		public String toString() {
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("**********************************************");
			sb.append("\n");
			sb.append("Graph");
			sb.append("\n");
			sb.append("\n");
			sb.append("edgeInfoMap");
			sb.append("\n");
			sb.append("------------------");
			sb.append("\n");
			// private Map<E, EdgeInfo<N>> edgeInfoMap
			for (E edge : edgeInfoMap.keySet()) {
				EdgeInfo<N> edgeInfo = edgeInfoMap.get(edge);
				sb.append("edge " + edge + " --> " + edgeInfo);
				sb.append("\n");
			}
			
			sb.append("\n");
			sb.append("nodes");
			sb.append("\n");
			sb.append("------------------");
			sb.append("\n");
			for (N node : nodeInfoMap.keySet()) {
				sb.append("node " + node);
				sb.append("\n");
			}
			
			sb.append("\n");
			sb.append("edges");
			sb.append("\n");
			sb.append("------------------");
			sb.append("\n");
			for (E edge : edgeInfoMap.keySet()) {
				sb.append("edge " + edge);
				sb.append("\n");
			}
			
			sb.append("\n");
			sb.append("nodeInfoMap");
			sb.append("\n");
			sb.append("------------------");
			sb.append("\n");
			for (N node : nodeInfoMap.keySet()) {
				NodeInfo<E> nodeInfo = nodeInfoMap.get(node);
				sb.append("node " + node + " --> " + nodeInfo);
				sb.append("\n");
			}
			
			sb.append("\n");
			sb.append("nodePairInfoMap");
			sb.append("\n");
			sb.append("------------------");
			sb.append("\n");
			for (MultiKey orderedKey : nodePairInfoMap.keySet()) {
				NodePairInfo<E> nodePairInfo = nodePairInfoMap.get(orderedKey);
				sb.append(orderedKey + " --> " + nodePairInfo);
				sb.append("\n");
			}
			sb.append("**********************************************");
			return sb.toString();
			
		}

		
		
	}
	
	private static class NodeInfo<E> {
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(" entering edges ");
			for (E edge : enteringEdges) {
				sb.append(edge);
				sb.append(" ");
			}
			sb.append(" exiting edges ");
			for (E edge : exitingEdges) {
				sb.append(edge);
				sb.append(" ");
			}
			
			return sb.toString();
		}
		
		private final Set<E> enteringEdges = new LinkedHashSet<>();
		
		private final Set<E> exitingEdges = new LinkedHashSet<>();
	}
	
	private static class NodePairInfo<E> {
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("NodePairInfo ");
			for (E edge : edges) {
				sb.append(edge);
				sb.append(" ");
			}
			return sb.toString();
		}
		
		private final Set<E> edges = new LinkedHashSet<>();
		
	}
	
	public static <N, E> boolean isCompleteGraph(Graph<N, E> graph) {
		return false;
	}
	
	public static <N, E> boolean isAcyclicGraph(Graph<N, E> graph) {
		return newCyclicSubGraph(graph).isEmpty();
	}
	
	public static <N, E> boolean isCyclicGraph(Graph<N, E> graph) {
		return newCyclicSubGraph(graph).nodeCount() == graph.nodeCount();
	}
	
	public static <N, E> MutableGraph<N, E> newCyclicSubGraph(Graph<N, E> graph) {
		MutableGraphImpl<N, E> result = new MutableGraphImpl<>();
		result.addAll(graph);
		boolean nodeRemoved = true;
		while (nodeRemoved) {
			nodeRemoved = false;
			for (N node : result.getNodes()) {
				if ((result.getInboundEdgeCount(node) == 0) || (result.getOutboundEdgeCount(node) == 0)) {
					result.removeNode(node);
					nodeRemoved = true;
				}
			}
		}
		return result;
	}
	
	public static <N, E> MutableGraph<N, E> newGraph() {
		return new MutableGraphImpl<>();
	}
	
	public static <N, E> MutableGraph<N, E> newGraph(Graph<N, E> graph) {
		MutableGraphImpl<N, E> result = new MutableGraphImpl<>();
		result.addAll(graph);
		return result;
	}
	
	public static <N, E> Graph<N, E> newImmutableGraph(Graph<N, E> graph) {
		return new ImmutableGraph<>(graph);
	}
	
	public static <N, E> MutableGraph<N, E> newReversedGraph(Graph<N, E> graph) {
		
		MutableGraphImpl<N, E> result = new MutableGraphImpl<>();
		
		for (N node : graph.getNodes()) {
			result.addNode(node);
		}
		
		for (E edge : graph.getEdges()) {
			N destinationNode = graph.getDestinationNode(edge);
			N originNode = graph.getOriginNode(edge);
			result.addEdge(edge, destinationNode, originNode);
		}
		
		return result;
		
	}
	
	public static <N, E> boolean isConnectedGraph(Graph<N, E> graph) {
		return newConnectedGraphs(graph).size() == 1;
	}
	
	public static <N, E> boolean isStronlyConnectedGraph(Graph<N, E> graph) {
		return isConnectedGraph(graph) && isCyclicGraph(graph);
	}
	
	public static <N, E> List<MutableGraph<N, E>> newConnectedGraphs(Graph<N, E> graph) {
		// create a list to receive the graphs
		List<MutableGraph<N, E>> result = new ArrayList<>();
		
		// create a map to hold all node pairs without regard to directionality
		// of the underlying edges. Each node will now have a set of nodes
		// associated with it that are linked by edges.
		Map<N, Set<N>> connectionMap = new LinkedHashMap<>();
		
		for (E edge : graph.getEdges()) {
			
			N originNode = graph.getOriginNode(edge);
			N destinationNode = graph.getDestinationNode(edge);
			
			Set<N> set;
			
			set = connectionMap.get(originNode);
			if (set == null) {
				set = new LinkedHashSet<>();
				connectionMap.put(originNode, set);
			}
			set.add(destinationNode);
			
			set = connectionMap.get(destinationNode);
			if (set == null) {
				set = new LinkedHashSet<>();
				connectionMap.put(destinationNode, set);
			}
			set.add(originNode);
		}
		
		// We visit nodes in the graph and explore outward until we cannot find
		// any more nodes. Each time that we can no longer find a new node, we
		// construct a
		// resultant graph and move on to a new node from the main node iterator
		
		Set<N> visitedNodes = new LinkedHashSet<>();
		LinkedList<N> nodesToVisit = new LinkedList<>();
		N visitedNode;
		List<N> nodesForNextGraph;
		
		for (N node : graph.getNodes()) {
			// if we have already explored this node, then there is nothing to
			// do
			if (!visitedNodes.contains(node)) {
				// we have decided to explore the node
				nodesToVisit.add(node);
				// we create a list to hold all the nodes that result from this
				// exploration
				nodesForNextGraph = new ArrayList<>();
				// so long as there are more nodes to visit attached to the
				// original node, we keep exploring
				while (nodesToVisit.size() > 0) {
					// we take a node from those to visit and put it on the node
					// for the next graph
					visitedNode = nodesToVisit.remove();
					nodesForNextGraph.add(visitedNode);
					// we also put it on the set of nodes that we have visited
					// to keep us from exploring the node redundantly
					visitedNodes.add(visitedNode);
					// for each node that was connected, we add that node to the
					// nodes still left to visit if we have not already visited
					// that node
					Set<N> set = connectionMap.get(visitedNode);
					if (set != null) {
						for (N linkedNode : set) {
							if (!visitedNodes.contains(linkedNode)) {
								nodesToVisit.add(linkedNode);
							}
						}
					}
				}
				
				// we now construct a result graph from the nodesForNextGraph
				MutableGraph<N, E> resultGraph = new MutableGraphImpl<>();
				for (N node2 : nodesForNextGraph) {
					resultGraph.addNode(node2);
				}
				
				for (N originNode : nodesForNextGraph) {
					for (E edge : graph.getOutboundEdges(originNode)) {
						N destinationNode = graph.getDestinationNode(edge);
						resultGraph.addEdge(edge, originNode, destinationNode);
					}
				}
				// finally, we create an immutable graph to add to the outgoing
				// list of graphs
				result.add(resultGraph);
			}
		}
		return result;
	}
	
}
