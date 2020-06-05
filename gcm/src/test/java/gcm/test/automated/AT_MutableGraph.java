package gcm.test.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.graph.Graph;
import gcm.util.graph.MutableGraph;

/**
 * Test class for {@link MutableGraph}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableGraph.class)
public class AT_MutableGraph {

	private void testAddAllForMutableGraph() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addEdge("A->C", "A", "C");
		mutableGraph1.addEdge("B->C", "B", "C");
		mutableGraph1.addEdge("C->A", "C", "A");
		
		MutableGraph<String, String> mutableGraph2 = new MutableGraph<>();
		mutableGraph2.addEdge("A->B", "J", "K");//this will replace the existing edge
		mutableGraph2.addEdge("J->V", "J", "V");//this will be a new edge
		mutableGraph2.addEdge("B->A", "B", "A");//this will be a new edge
		mutableGraph2.addEdge("C->A", "C", "A");//this will the same as an existing edge
		
		mutableGraph1.addAll(mutableGraph2);
		
		assertEquals(6,mutableGraph1.edgeCount());
		assertEquals(6,mutableGraph1.nodeCount());
		
		assertTrue(mutableGraph1.containsNode("A"));
		assertTrue(mutableGraph1.containsNode("B"));
		assertTrue(mutableGraph1.containsNode("C"));
		assertTrue(mutableGraph1.containsNode("J"));
		assertTrue(mutableGraph1.containsNode("V"));
		assertTrue(mutableGraph1.containsNode("K"));
		

		
		assertTrue(mutableGraph1.containsEdge("A->C"));
		assertTrue(mutableGraph1.containsEdge("B->C"));
		assertTrue(mutableGraph1.containsEdge("C->A"));
		assertTrue(mutableGraph1.containsEdge("A->B"));
		assertTrue(mutableGraph1.containsEdge("J->V"));
		assertTrue(mutableGraph1.containsEdge("B->A"));
		
		assertEquals("A",mutableGraph1.getOriginNode("A->C"));
		assertEquals("B",mutableGraph1.getOriginNode("B->C"));
		assertEquals("C",mutableGraph1.getOriginNode("C->A"));
		assertEquals("J",mutableGraph1.getOriginNode("A->B"));
		assertEquals("J",mutableGraph1.getOriginNode("J->V"));
		assertEquals("B",mutableGraph1.getOriginNode("B->A"));

		assertEquals("C",mutableGraph1.getDestinationNode("A->C"));
		assertEquals("C",mutableGraph1.getDestinationNode("B->C"));
		assertEquals("A",mutableGraph1.getDestinationNode("C->A"));
		assertEquals("K",mutableGraph1.getDestinationNode("A->B"));
		assertEquals("V",mutableGraph1.getDestinationNode("J->V"));
		assertEquals("A",mutableGraph1.getDestinationNode("B->A"));
	}
	
	private void testAddAllForGraph() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addEdge("A->C", "A", "C");
		mutableGraph1.addEdge("B->C", "B", "C");
		mutableGraph1.addEdge("C->A", "C", "A");
		
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "J", "K");//this will replace the existing edge
		builder.addEdge("J->V", "J", "V");//this will be a new edge
		builder.addEdge("B->A", "B", "A");//this will be a new edge
		builder.addEdge("C->A", "C", "A");//this will the same as an existing edge
		
		mutableGraph1.addAll(builder.build());
		
		assertEquals(6,mutableGraph1.edgeCount());
		assertEquals(6,mutableGraph1.nodeCount());
		
		assertTrue(mutableGraph1.containsNode("A"));
		assertTrue(mutableGraph1.containsNode("B"));
		assertTrue(mutableGraph1.containsNode("C"));
		assertTrue(mutableGraph1.containsNode("J"));
		assertTrue(mutableGraph1.containsNode("V"));
		assertTrue(mutableGraph1.containsNode("K"));
		

		
		assertTrue(mutableGraph1.containsEdge("A->C"));
		assertTrue(mutableGraph1.containsEdge("B->C"));
		assertTrue(mutableGraph1.containsEdge("C->A"));
		assertTrue(mutableGraph1.containsEdge("A->B"));
		assertTrue(mutableGraph1.containsEdge("J->V"));
		assertTrue(mutableGraph1.containsEdge("B->A"));
		
		assertEquals("A",mutableGraph1.getOriginNode("A->C"));
		assertEquals("B",mutableGraph1.getOriginNode("B->C"));
		assertEquals("C",mutableGraph1.getOriginNode("C->A"));
		assertEquals("J",mutableGraph1.getOriginNode("A->B"));
		assertEquals("J",mutableGraph1.getOriginNode("J->V"));
		assertEquals("B",mutableGraph1.getOriginNode("B->A"));

		assertEquals("C",mutableGraph1.getDestinationNode("A->C"));
		assertEquals("C",mutableGraph1.getDestinationNode("B->C"));
		assertEquals("A",mutableGraph1.getDestinationNode("C->A"));
		assertEquals("K",mutableGraph1.getDestinationNode("A->B"));
		assertEquals("V",mutableGraph1.getDestinationNode("J->V"));
		assertEquals("A",mutableGraph1.getDestinationNode("B->A"));
	}
	
	/**
	 * Tests {@link MutableGraph#addAll(Graph)}
	 * 
	 * Tests {@link MutableGraph#addAll(MutableGraph)}
	 */
	@Test
	public void testAddAll() {		
		testAddAllForMutableGraph();
		testAddAllForGraph();
	}

	/**
	 * Tests {@link MutableGraph#addEdge(Object, Object, Object)}
	 */
	@Test
	public void testAddEdge() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		
		assertTrue(mutableGraph.containsNode("A"));
		assertTrue(mutableGraph.containsNode("B"));
		assertTrue(mutableGraph.containsEdge("A->B"));
		assertEquals("A",mutableGraph.getOriginNode("A->B"));
		assertEquals("B",mutableGraph.getDestinationNode("A->B"));
		
		//show that the edge can replace an existing edge 
		mutableGraph.addEdge("A->B", "B", "C");
		assertEquals("B",mutableGraph.getOriginNode("A->B"));
		assertEquals("C",mutableGraph.getDestinationNode("A->B"));

	}

	/**
	 * Tests {@link MutableGraph#addNode(Object)}
	 */
	@Test
	public void testAddNode() {
		MutableGraph<Integer, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode(1);
		mutableGraph.addNode(2);
		mutableGraph.addNode(3);
		mutableGraph.addNode(4);
		
		assertTrue(mutableGraph.containsNode(1));
		assertTrue(mutableGraph.containsNode(2));
		assertTrue(mutableGraph.containsNode(3));
		assertTrue(mutableGraph.containsNode(4));
		
		//re-adding should have no effect
		mutableGraph.addNode(4);
		assertTrue(mutableGraph.containsNode(4));
	}

	/**
	 * Tests {@link MutableGraph#containsEdge(Object)}
	 */
	@Test
	public void testContainsEdge() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->A", "B", "A");
				
		assertTrue(mutableGraph.containsEdge("A->B"));
		assertTrue(mutableGraph.containsEdge("A->C"));
		assertTrue(mutableGraph.containsEdge("B->A"));
		
	}

	/**
	 * Tests {@link MutableGraph#containsNode(Object)}
	 */
	@Test
	public void testContainsNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode("A");
		mutableGraph.addNode("B");
		mutableGraph.addNode("C");
				
		assertTrue(mutableGraph.containsNode("A"));
		assertTrue(mutableGraph.containsNode("B"));
		assertTrue(mutableGraph.containsNode("C"));
	}

	/**
	 * Tests {@link MutableGraph#edgeCount()}
	 * 
	 * Tests {@link MutableGraph#edgeCount(Object, Object)}
	 */
	@Test
	public void testEdgeCount() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B_1", "A", "B");
		mutableGraph.addEdge("A->B_2", "A", "B");
		mutableGraph.addEdge("A->B_3", "A", "B");
		mutableGraph.addEdge("A->C_1", "A", "C");
		mutableGraph.addEdge("A->C_2", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D_1", "B", "D");
		mutableGraph.addEdge("A->D_2", "A", "D");
		
		assertEquals(8,mutableGraph.edgeCount());		
		
		assertEquals(3,mutableGraph.edgeCount("A","B"));
		assertEquals(2,mutableGraph.edgeCount("A","C"));
		assertEquals(1,mutableGraph.edgeCount("B","C"));
		assertEquals(1,mutableGraph.edgeCount("B","D"));
		assertEquals(1,mutableGraph.edgeCount("A","D"));
		assertEquals(0,mutableGraph.edgeCount("C","D"));		
	}

	/**
	 * Tests {@link MutableGraph#equals(Object)}
	 */
	@Test
	public void testEquals() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addNode("C");
		
		MutableGraph<String, String> mutableGraph2 = new MutableGraph<>();
		mutableGraph2.addEdge("A->B", "A", "B");
		mutableGraph2.addNode("C");

		
		assertEquals(mutableGraph1, mutableGraph2);
		mutableGraph2.addNode("D");
		
		assertNotEquals(mutableGraph1, mutableGraph2);
		
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addNode("C");
		Graph<String, String> graph = builder.build();
		
		assertEquals(mutableGraph1, graph);
		
		mutableGraph1.addNode("D");
		assertNotEquals(mutableGraph1, graph);
		
	}

	/**
	 * Tests {@link MutableGraph#formsEdgeRelationship(Object, Object, Object)}
	 */
	@Test
	public void testFormsEdgeRelationship() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		
		assertTrue(mutableGraph.formsEdgeRelationship("A->B", "A", "B"));
		assertTrue(mutableGraph.formsEdgeRelationship("A->C", "A", "C"));
		assertTrue(mutableGraph.formsEdgeRelationship("B->C", "B", "C"));
		
		assertFalse(mutableGraph.formsEdgeRelationship("A->B", "A", "C"));
		assertFalse(mutableGraph.formsEdgeRelationship("A->C", "A", "B"));
		assertFalse(mutableGraph.formsEdgeRelationship("B->C", "B", "A"));
		
	}

	/**
	 * Tests {@link MutableGraph#getDestinationNode(Object)}
	 */
	@Test
	public void testGetDestinationNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		

		assertEquals("B",mutableGraph.getDestinationNode("A->B"));
		assertEquals("C",mutableGraph.getDestinationNode("A->C"));
		assertEquals("C",mutableGraph.getDestinationNode("B->C"));
		
		
	}

	/**
	 * Tests {@link MutableGraph#getEdges()}
	 * 
	 * Tests {@link MutableGraph#getEdges(Object, Object)}
	 */
	@Test
	public void testGetEdges() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		
		Set<String> expected = new LinkedHashSet<>();
		expected.add("A->B");
		expected.add("A->C");
		expected.add("B->C");

		Set<String> actual = mutableGraph.getEdges().stream().collect(Collectors.toSet());
		
		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link MutableGraph#getInboundEdgeCount(Object)}
	 */
	@Test
	public void testGetInboundEdgeCount() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#getInboundEdges(Object)}
	 */
	@Test
	public void testGetInboundEdges() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#getNodes()}
	 */
	@Test
	public void testGetNodes() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#getOriginNode(Object)}
	 */
	@Test
	public void testGetOriginNode() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#getOutboundEdgeCount(Object)}
	 */
	@Test
	public void testGetOutboundEdgeCount() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#getOutboundEdges(Object)}
	 */
	@Test
	public void testGetOutboundEdges() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#hashCode()}
	 */
	@Test
	public void testHashCode() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#isEmpty()}
	 */
	@Test
	public void testIsEmpty() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#nodeCount()}
	 */
	@Test
	public void testNodeCount() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#removeEdge(Object)}
	 */
	@Test
	public void testRemoveEdge() {
		fail();
	}

	/**
	 * Tests {@link MutableGraph#removeNode(Object)}
	 */
	@Test
	public void testRemoveNode() {
		fail();
	}

}
