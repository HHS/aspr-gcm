package gcm.util.path;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * A Path is an ordered a walk through a set of nodes by way of edges without
 * any breaks.
 * 
 * 1) The pathEdges iterator returns the edges of the graph in a well defined
 * order 2) If two consecutive edges e1 and e2 are returned by the pathEdges
 * iterator, then e1's destination node will equal e2's origin node via
 * Object.equals();
 * 
 * Note that a path may be crossing or cyclic. The pathEdges() iterator may
 * repeat edges since it represents the walk through the graph. Contrast this
 * with the Graph.getEdgesIterator() iterator which merely iterates through the
 * unique edges in the graph.
 * 
 * A path may be a degenerate, having no edges. Degenerate paths are the
 * expected results for paths that do not exist.
 * 
 * @author Shawn Hatch
 * 
 */
@Immutable
public final class Path<E> {
	//TODO rewrite class doc
	
	public interface EdgeCostEvaluator<E> {		
		public double getEdgeCost(E edge);
	}
	
	public static interface TravelCostEvaluator<N> {		
		public double getMinimumCost(N originNode, N destination);
	}
	
	/**
	 * Returns a new instance of the Builder class
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static class Builder<T> {
		
		private Builder() {}

		private List<T> edges = new ArrayList<>();

		public Builder<T> addEdge(T edge) {
			edges.add(edge);
			return this;
		}

		public Path<T> build() {
			try {
				return new Path<>(edges);
			} finally {
				edges = new ArrayList<>();
			}
		}
	}

	private Path(List<E> edges) {
		this.edges = edges;
	}

	private final List<E> edges;

	/**
	 * Returns an iterable over the edges in the path walk. Note that the path
	 * may cross itself (revisit nodes) and even repeat edges. Thus this is NOT
	 * an iterator over the set of edges in the graph, but will include all
	 * edges in the graph at least once.
	 * 
	 */
	public List<E> getEdges() {
		return new ArrayList<>(edges);
	}

	/**
	 * Returns the number of edges in the path walk through the graph. Note that
	 * this is NOT necessarily the same value as returned by the edgeCount()
	 * method, but rather returns a value that may be higher due to revisited
	 * edges.
	 * 
	 */

	public int pathLength() {
		return edges.size();
	}

	public double getCost(EdgeCostEvaluator<E> edgeCostEvaluator) {
		double result = 0;
		for(E edge : edges){
			result += edgeCostEvaluator.getEdgeCost(edge);
		}
		return result;		
	}
	
	/**
	 * Returns true if and only if the path contains no edges
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return edges.size() == 0;
	}

}
