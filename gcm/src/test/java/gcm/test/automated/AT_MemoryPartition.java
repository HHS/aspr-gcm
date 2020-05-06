package gcm.test.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import gcm.util.MemoryLink;
import gcm.util.MemoryPartition;
import gcm.util.annotations.UnitTest;

/**
 * Test for {@link MemoryPartition}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MemoryPartition.class)
public class AT_MemoryPartition {
	@Test
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

	@Test
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
