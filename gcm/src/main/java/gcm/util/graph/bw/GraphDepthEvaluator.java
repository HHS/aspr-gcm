package gcm.util.graph.bw;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GraphDepthEvaluator<N, E> {
	
	private Map<Integer, Set<N>> depthToNodeSetMap = new LinkedHashMap<>();
	
	private Map<N, Integer> nodeToDepthMap = new LinkedHashMap<>();
	
	private int maxDepth = -1;
	
	public GraphDepthEvaluator(Graph<N, E> graph) {
		
		MutableGraph<N, E> g = Graphs.newGraph(graph);
		
		while (true) {
			Set<N> nodes = new LinkedHashSet<>();
			
			for (N node : g.getNodes()) {
				if (g.getInboundEdgeCount(node) == 0) {
					
					g.removeNode(node);
					nodes.add(node);
				}
			}
			
			if (nodes.size() > 0) {
				maxDepth++;
				Integer depth = maxDepth;
				depthToNodeSetMap.put(depth, nodes);
				for (N node : nodes) {
					nodeToDepthMap.put(node, depth);
				}
			} else {
				break;
			}
		}
		
		Set<N> nodes = g.getNodes();
		if (nodes.size() > 0) {
			Integer depth = -1;
			depthToNodeSetMap.put(depth, nodes);
			for (N node : nodes) {
				nodeToDepthMap.put(node, depth);
			}
		}
		
	}
	
	public int getMaxDepth() {
		return maxDepth;
	}
	
	public int getDepth(N node) {
		Integer result = nodeToDepthMap.get(node);
		if (result == null) {
			result = -1;
		}
		return result;
	}
	
	public Set<N> getNodes(int depth) {
		int convertedDepth = Math.max(-1, depth);
		
		Set<N> result = new LinkedHashSet<>();
		
		Set<N> nodes = depthToNodeSetMap.get(convertedDepth);
		if (nodes != null) {
			result.addAll(nodes);
		}
		return result;
	}
	
}
