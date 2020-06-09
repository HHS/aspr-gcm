package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import gcm.util.MemoryLink;
import gcm.util.annotations.UnitTest;

/**
 * Test class for {@link MemoryLink}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MemoryLink.class)
public class AT_MemoryLink {

	@Test
	public void testGetChild() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(child, memoryLink.getChild());
	}

	@Test
	public void testGetDescriptor() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(descriptor, memoryLink.getDescriptor());
	}

	@Test
	public void testGetParent() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(parent, memoryLink.getParent());

	}

	@Test
	public void testToString() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		String expectedString = "MemoryLink [parent=parent, child=child, descriptor=mem link relationship]";
		assertEquals(expectedString, memoryLink.toString());
	}

	@Test
	public void testEquals() {
		MemoryLink memoryLink1 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		MemoryLink memoryLink2 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		
		MemoryLink memoryLink3 = new MemoryLink("parent "+2, "child "+1, "descriptor"+1);
		MemoryLink memoryLink4 = new MemoryLink("parent "+1, "child "+2, "descriptor"+1);
		MemoryLink memoryLink5 = new MemoryLink("parent "+1, "child "+1, "descriptor"+2);
		
		assertEquals(memoryLink1, memoryLink1);
		assertEquals(memoryLink1, memoryLink2);
		assertEquals(memoryLink2, memoryLink1);
		assertNotEquals(memoryLink1, memoryLink3);
		assertNotEquals(memoryLink1, memoryLink4);
		assertNotEquals(memoryLink1, memoryLink5);
		
		

	}

	@Test
	public void testHashCode() {
		MemoryLink memoryLink1 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		MemoryLink memoryLink2 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		assertEquals(memoryLink1.hashCode(), memoryLink2.hashCode());
	}
}
