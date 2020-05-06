package gcm.util.graph;

/**
 * 
 * An enumeration of the three types of graph connectedness.
 * 
 * Strongly connected graphs are ones in which for every pair (not necessarily
 * distinct) of nodes in the graph there exits a Path connecting those nodes.
 * Note that a node is not implicitly connected to itself.
 * 
 * Weakly connected graphs are ones where the nodes connect to one another if
 * edge directionality is ignored. Formally, let graph G exist with edges E and
 * nodes N. Construct a graph GPrime with all of N and E(i.e. a copy of G). For
 * each edge e in E, create edge ePrime that is oppositely directed from e and
 * add ePrime to GPrime. G is weakly connected graph if and only if GPrime is
 * strongly connected.
 * 
 * Disconnected graphs are those that are neither strongly nor weakly connected.
 * 
 * Note that strongly connected graphs are weakly connected. Empty graphs are
 * considered strongly connected.
 * 
 * @author Shawn Hatch
 * 
 */

public enum Connectedness {
	STRONGLYCONNECTED, WEAKLYCONNECTED, DISCONNECTED
}
