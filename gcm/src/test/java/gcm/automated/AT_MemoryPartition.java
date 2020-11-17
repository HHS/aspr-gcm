package gcm.automated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gcm.util.MemoryLink;
import gcm.util.MemoryPartition;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test for {@link MemoryPartition}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MemoryPartition.class)
public class AT_MemoryPartition {
	/**
	 * Test {@link MemoryPartition#getMemoryLinks())}
	 */
	@Test
	@UnitTestMethod(name = "getMemoryLinks", args = {})
	public void testGetMemoryLinks() {
		Set<MemoryLink> expectedMemoryLinks = new LinkedHashSet<>();
		MemoryPartition memoryPartition = new MemoryPartition();
		for (int i = 0; i < 10; i++) {
			MemoryLink memoryLink = new MemoryLink("parent " + i, "child " + i, "descriptor " + i);
			expectedMemoryLinks.add(memoryLink);
		}
		for (MemoryLink memoryLink : expectedMemoryLinks) {
			memoryPartition.addMemoryLink(memoryLink.getParent(), memoryLink.getChild(), memoryLink.getDescriptor());
		}
		
		List<MemoryLink> actualMemoryLinks = memoryPartition.getMemoryLinks();
		
		assertEquals(expectedMemoryLinks.size(), actualMemoryLinks.size());
		assertTrue(expectedMemoryLinks.containsAll(actualMemoryLinks));
	}

	/**
	 * Tests {@link MemoryPartition#MemoryPartition()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MemoryPartition memoryPartition = new MemoryPartition();
		assertNotNull(memoryPartition);
	}
	
	/**
	 * Tests {@link MemoryPartition#addMemoryLink(Object, Object, String)}
	 */
	@Test
	@UnitTestMethod(name = "addMemoryLink", args = {Object.class, Object.class, String.class})
	public void testAddMemoryLink() {
		Set<MemoryLink> expectedMemoryLinks = new LinkedHashSet<>();
		MemoryPartition memoryPartition = new MemoryPartition();
		for (int i = 0; i < 10; i++) {
			MemoryLink memoryLink = new MemoryLink("parent " + i, "child " + i, "descriptor " + i);
			expectedMemoryLinks.add(memoryLink);
		}
		for (MemoryLink memoryLink : expectedMemoryLinks) {
			memoryPartition.addMemoryLink(memoryLink.getParent(), memoryLink.getChild(), memoryLink.getDescriptor());
		}
		
		List<MemoryLink> actualMemoryLinks = memoryPartition.getMemoryLinks();
		
		assertEquals(expectedMemoryLinks.size(), actualMemoryLinks.size());
		assertTrue(expectedMemoryLinks.containsAll(actualMemoryLinks));
	}
}
