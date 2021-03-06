package gcm.util.graph;


/**
 * A Path is an ordered a walk through a set of nodes by way of
 * edges without any breaks. 
 * 
 * 1) The pathEdges iterator returns the edges of the graph in a well defined
 * order 2) If two consecutive edges e1 and e2 are returned by the pathEdges
 * iterator, then e1's destination node will equal e2's origin node via
 * Object.equals(); 
 * 
 * Note that a path may be crossing or cyclic. The pathEdges() iterator may
 * repeat edges since it represents the walk through the graph. Contrast this
 * with the Graph.getEdgesIterator() iterator which merely iterates through the unique
 * edges in the graph.
 * 
 * A path may be a degenerate, having no edges. Degenerate paths are the
 * expected results for paths that do not exist.
 * 
 * @author Shawn Hatch
 * 
 */

public interface Path<E>  {
	
	/**
	 * Returns an iterable over the edges in the path walk. Note that the path
	 * may cross itself (revisit nodes) and even repeat edges. Thus this is NOT
	 * an iterator over the set of edges in the graph, but will include all
	 * edges in the graph at least once.
	 * 
	 * @return
	 */
	public Iterable<E> getEdges();
	
	/**
	 * Returns the number of edges in the path walk through the graph. Note that
	 * this is NOT necessarily the same value as returned by the edgeCount()
	 * method, but rather returns a value that may be higher due to revisited
	 * edges.
	 * 
	 * @return
	 */
	public int pathLength();
	
	/**
	 * Returns true if and only if the path contains no edges 
	 * @return
	 */
	public boolean isDegenerate(); 
	
}
