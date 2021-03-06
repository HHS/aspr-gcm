package gcm.util.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * A utility for separating a graph into multiple graphs such that each
 * resultant graph's nodes are connected via edges to one another.
 * Directionality of edges is ignored for the purposes of determining what nodes
 * connect to other nodes, thus the resultant graphs DO NOT guarantee that there
 * necessarily a path from one node to another in the graph.
 * 
 * @author Shawn Hatch
 * 
 */
public class ConnectionUtilities {
	
	/**
	 * 
	 * Determines the connectedness of a graph
	 * 
	 * @param <N>
	 * @param <E>
	 * @param graph
	 * @return
	 */
	public static <N, E> Connectedness determineConnectedness(Graph<N, E> graph) {
		if (isStronglyConnected(graph)) {
			return Connectedness.STRONGLYCONNECTED;
		}
		
		List<Graph<N, E>> list = cutGraph(graph);
		if ((list.size() == 1) && ((graph.nodeCount() > 1) || (graph.edgeCount() > 1))) {
			return Connectedness.WEAKLYCONNECTED;
		}
		
		return Connectedness.DISCONNECTED;
	}
	
	/**
	 * 
	 * Separates a graph into a set of independent weakly connected and
	 * disconnected sub graphs. All disconnected subgraphs will consist of
	 * single nodes with no edges.
	 * 
	 * @param <N>
	 * @param <E>
	 * @param graph
	 * @return
	 */
	public static <N, E> List<Graph<N, E>> cutGraph(Graph<N, E> graph) {
		// create a list to receive the graphs
		List<Graph<N, E>> result = new ArrayList<>();
		
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
				MutableGraph<N, E> resultGraph = new GenericMutableGraph<>();
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
				result.add(GenericGraph.getGenericGraph(resultGraph));
			}
		}
		return result;
	}
	
	private static <N, E> boolean isStronglyConnected(Graph<N, E> graph) {
		if (graph.nodeCount() == 0) {
			return true;
		}
		
		
		
		N primaryNode = graph.getNodes().iterator().next();
		
		Set<N> nodesToExpand = new LinkedHashSet<>();
		Set<N> expandedNodes = new LinkedHashSet<>();
		
		nodesToExpand.add(primaryNode);
		
		while (nodesToExpand.size() > 0) {			
			N node = nodesToExpand.iterator().next();
			nodesToExpand.remove(node);
			expandedNodes.add(node);			
			for (E edge : graph.getOutboundEdges(node)) {				
				N destinationNode = graph.getDestinationNode(edge);
				if (!expandedNodes.contains(destinationNode)) {
					nodesToExpand.add(destinationNode);
				}
			}
		}
		
		if (expandedNodes.size() != graph.nodeCount()) {
			return false;
		}
		
		expandedNodes.clear();
		nodesToExpand.clear();
		nodesToExpand.add(primaryNode);
		
		while (nodesToExpand.size() > 0) {			
			N node = nodesToExpand.iterator().next();
			nodesToExpand.remove(node);
			expandedNodes.add(node);			
			for (E edge : graph.getInboundEdges(node)) {				
				N originNode = graph.getOriginNode(edge);
				if (!expandedNodes.contains(originNode)) {
					nodesToExpand.add(originNode);
				}
			}
		}
		
		if (expandedNodes.size() != graph.nodeCount()) {
			return false;
		}
		return true;
		
	}
}
