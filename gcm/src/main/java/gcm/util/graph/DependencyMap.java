package gcm.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DependencyMap is a convenience utility designed to work with acyclic graphs
 * that represent dependencies between nodes. Its principle use is to establish
 * an ordering of nodes such that nodes that have no dependencies on other nodes
 * are deemed as rank zero, nodes that depend only on rank zero nodes are deemed
 * rank 1 and so on.
 * 
 * Dependency in the graph is represented by edges. An edge directed from node A
 * to node B taken to mean that A depends on B.
 * 
 * The static constructor method creates the DependencyMap and fully solves the
 * dependency ranks at construction, i.e. the map is immutable. Changes to the
 * graph, nodes or edges after the map's construction will have no effect on the
 * map.
 * 
 * @author Shawn Hatch
 * 
 * @param <N>
 */
public final class DependencyMap<N> {
	
	private DependencyMap() {
		
	}
	
	private boolean isWellFormed;
	
	public boolean isWellFormed() {
		return isWellFormed;
	}
	
	private Map<N, Integer> levelMap = new LinkedHashMap<>();
	
	private List<N> dependencyList = new ArrayList<>();
	
	public int getDependencyRank(N node) {
		Integer result = levelMap.get(node);
		if (result == null) {
			return -1;
		}
		return result;
	}
	
	public List<N> getOrderedDependencies() {
		return new ArrayList<>(dependencyList);
	}
	
	private class DependencyLevelRec {		
		N node;		
		int level;		
	}
	
	private void formDependencyList() {
		List<DependencyLevelRec> list = new ArrayList<>();
		
		for (N key : levelMap.keySet()) {
			DependencyLevelRec rec = new DependencyLevelRec();
			rec.level = levelMap.get(key);
			rec.node = key;
			list.add(rec);
		}
		
		Collections.sort(list, new Comparator<DependencyLevelRec>() {
			@Override
			public int compare(DependencyLevelRec o1, DependencyLevelRec o2) {
				if (o1.level < o2.level) {
					return -1;
				}
				if (o1.level > o2.level) {
					return 1;
				}
				return 0;
			}
			
		});	
		for(DependencyLevelRec rec : list) {
			dependencyList.add(rec.node);	
		}
		
		
	}
	
	public static <N, E> DependencyMap<N> getDependencyMap(Graph<N, E> graph) {
		DependencyMap<N> result = new DependencyMap<>();
		
		MutableGraph<N, E> mutableGraph = new GenericMutableGraph<>();
		mutableGraph.addAll(graph);
		
		if (AcyclicGraphReducer.reduceGraph(mutableGraph).nodeCount() > 0) {
			result.isWellFormed = false;
			return result;
		}
		
		result.isWellFormed = true;
		
		
		for (N node : mutableGraph.getNodes()) {
			result.levelMap.put(node, 0);
		}
		
		while (true) {
			if (mutableGraph.nodeCount() == 0){
				break;
			}
			List<N> nodes = new ArrayList<>();
			for (N node : mutableGraph.getNodes()) {
				nodes.add(node);
			}
			for (N node : nodes) {
				if (mutableGraph.outboundEdgeCount(node) == 0) {					
					for (E edge : mutableGraph.getInboundEdges(node)) {						
						N originNode = mutableGraph.getOriginNode(edge);
						Integer rank = Math.max(result.levelMap.get(originNode), result.levelMap.get(node) + 1);
						result.levelMap.put(originNode, rank);
					}
					mutableGraph.removeNode(node);
				}
			}
		}
		result.formDependencyList();
		return result;
	}
	
}
