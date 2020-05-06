package gcm.util.graph;

import java.util.ArrayList;
import java.util.List;


public class PathBuilder {
	
	private PathBuilder() {
	}
	
	public static <E>  Path<E> build(List<E> edges) {
		PathImpl<E> result = new PathImpl<>();
		result.edges.addAll(edges);
		return result;
	}
	
	public static <E> Path<E> buildEmptyPath() {
		return new PathImpl<>();
	}
	
	private static class PathImpl<E> implements Path<E> {
		
		private PathImpl() {
		}
		
		private List<E> edges = new ArrayList<>();
		
		@Override
		public Iterable<E> getEdges() {
			return new ImmutableIterable<>(edges);
		}
		
		@Override
		public int pathLength() {
			return edges.size();
		}
		
		@Override
		public boolean isDegenerate() {
			return edges.size() == 0;
		}
		
	}
	
}
