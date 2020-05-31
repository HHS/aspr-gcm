package gcm.util.graph.utilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.util.graph.Graph;
import gcm.util.graph.utilities.AcyclicGraphReducer.GraphCyclisity;

/**
 * 
 * A static utility class for separating a graph into multiple graphs such that
 * each resultant graph's nodes are connected via edges to one another.
 * Directionality of edges is ignored for the purposes of determining what nodes
 * connect to other nodes, thus the resultant graphs DO NOT guarantee that there
 * is necessarily a path from one node to another in the graph.
 * 
 * @author Shawn Hatch
 * 
 */
public final class ConnectionUtilities {

	/**
	 * 
	 * An enumeration of the three types of graph connectedness.
	 * 
	 * Strongly connected graphs are ones in which for every pair (not
	 * necessarily distinct) of nodes in the graph there exits a Path connecting
	 * those nodes. Note that a node is not implicitly connected to itself.
	 * 
	 * Weakly connected graphs are ones where the nodes connect to one another
	 * if edge directionality is ignored. Formally, let graph G exist with edges
	 * E and nodes N. Construct a graph GPrime with all of N and E(i.e. a copy
	 * of G). For each edge e in E, create edge ePrime that is oppositely
	 * directed from e and add ePrime to GPrime. G is weakly connected graph if
	 * and only if GPrime is strongly connected.
	 * 
	 * Disconnected graphs are those that are neither strongly nor weakly
	 * connected.
	 * 
	 * Note that strongly connected graphs are weakly connected. Empty graphs
	 * are considered strongly connected.
	 * 
	 * @author Shawn Hatch
	 * 
	 */
	public static enum GraphConnectedness {
		STRONGLYCONNECTED, WEAKLYCONNECTED, DISCONNECTED
	}

	private ConnectionUtilities() {

	}

	/**
	 * 
	 * Determines the connectedness of a graph
	 *	
	 */
	public static <N, E> GraphConnectedness getGraphConnectedness(Graph<N, E> graph) {
		if (cutGraph(graph).size() == 1) {
			if (AcyclicGraphReducer.getGraphCyclisity(graph)==GraphCyclisity.CYCLIC) {
				return GraphConnectedness.STRONGLYCONNECTED;
			} else {
				return GraphConnectedness.WEAKLYCONNECTED;
			}
		}
		return GraphConnectedness.DISCONNECTED;
	}

	/**
	 * 
	 * Separates a graph into a set of independent weakly connected and
	 * disconnected sub graphs. All disconnected subgraphs will consist of
	 * single nodes with no edges.
	 * 
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
				Graph.Builder<N, E> builder = Graph.builder(); 
				for (N node2 : nodesForNextGraph) {
					builder.addNode(node2);
				}
				for (N originNode : nodesForNextGraph) {
					for (E edge : graph.getOutboundEdges(originNode)) {
						N destinationNode = graph.getDestinationNode(edge);
						builder.addEdge(edge, originNode, destinationNode);
					}
				}
				// finally, we create a graph to add to the outgoing
				// list of graphs
				result.add(builder.build());
			}
		}
		return result;
	}

}
