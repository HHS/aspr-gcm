package gcm.test.automated;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.graph.Graph;
import gcm.util.graph.Path;
import gcm.util.graph.path.PathSolver;

@UnitTest(target = PathSolver.class)
public class AT_GraphPathSolver {
	
	private static class Edge{
		private final String name;
		private final Integer first;
		private final Integer second;
		public Edge(String name,Integer first, Integer second) {
			this.name = name;
			this.first = first;
			this.second = second;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Edge [name=");
			builder.append(name);
			builder.append(", first=");
			builder.append(first);
			builder.append(", second=");
			builder.append(second);
			builder.append("]");
			return builder.toString();
		}		
	}
	
	@Test
	public void test() {
		List<Edge> edges = new ArrayList<>();
		edges.add(new Edge("A",1,2));
		edges.add(new Edge("A*",2,1));
		edges.add(new Edge("B",2,3));
		edges.add(new Edge("B*",3,2));
		edges.add(new Edge("C",3,4));
		edges.add(new Edge("C*",4,3));

		Graph.Builder<Integer,Edge> builder = Graph.builder();
		edges.forEach(edge->builder.addEdge(edge, edge.first, edge.second));
		Graph<Integer,Edge> graph = builder.build();
		
		Path<Edge> path = PathSolver.getPath(graph, 2, 4, edge->1, (node1,node2)->0);
		
		Iterator<Edge> iterator = path.getEdges().iterator();
		while(iterator.hasNext()) {
			@SuppressWarnings("unused")
			Edge edge = iterator.next();
			//System.out.println(edge);
		}
	}
}
