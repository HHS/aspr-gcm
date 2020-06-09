package gcm.automated;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.graph.Graph;
import gcm.util.path.Path;
import gcm.util.path.Paths;

@UnitTest(target = Paths.class)
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
		
		Optional<Path<Edge>> solution = Paths.getPath(graph, 2, 4, edge->1, (node1,node2)->0);
		assertTrue(solution.isPresent());
		
		Path<Edge> path = solution.get();
		for(@SuppressWarnings("unused") Edge edge : path.getEdges()) {
		//while(iterator.hasNext()) {
			//@SuppressWarnings("unused")
			//Edge edge = iterator.next();
			//System.out.println(edge);
		}
	}
}
