package gcm.util.path;

import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
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
@Source(status = TestStatus.REQUIRED)
@Immutable
public final class Path<E> {
	// TODO rewrite class doc

	/**
	 * Returns a new instance of the Builder class
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	@Source(status = TestStatus.REQUIRED, proxy = Path.class)
	public static class Builder<T> {

		private Builder() {
		}

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
	 * Returns a list over the edges in the path walk. Note that the path may
	 * cross itself (revisit nodes) and even repeat edges.
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

	public int length() {
		return edges.size();
	}

	/**
	 * Returns true if and only if the path contains no edges
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return edges.size() == 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Path)) {
			return false;
		}
		Path other = (Path) obj;
		if (edges == null) {
			if (other.edges != null) {
				return false;
			}
		} else if (!edges.equals(other.edges)) {
			return false;
		}
		return true;
	}

}
