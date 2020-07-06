package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import gcm.util.MemoryLink;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestConstructor;
import gcm.util.annotations.UnitTestMethod;

/**
 * Test class for {@link MemoryLink}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MemoryLink.class)
public class AT_MemoryLink {

	/**
	 * Tests {@link MemoryLink#getChild()}
	 */
	@Test
	@UnitTestMethod(name = "getChild", args = {})
	public void testGetChild() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(child, memoryLink.getChild());
	}
	
	/**
	 * Tests {@link MemoryLink#MemoryLink(Object, Object, String)}
	 */
	@Test
	@UnitTestConstructor(args = {Object.class, Object.class, String.class})
	public void testConstructor() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertNotNull(memoryLink);
	}

	/**
	 * Tests {@link MemoryLink#getDescriptor()}
	 */
	@Test
	@UnitTestMethod(name = "getDescriptor", args = {})
	public void testGetDescriptor() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(descriptor, memoryLink.getDescriptor());
	}

	/**
	 * Test {@link MemoryLink#getParent()}
	 */
	@Test
	@UnitTestMethod(name = "getParent", args = {})
	public void testGetParent() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		assertEquals(parent, memoryLink.getParent());

	}

	/**
	 * Tests {@link MemoryLink#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		String parent = "parent";
		String child = "child";
		String descriptor = "mem link relationship";
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		String expectedString = "MemoryLink [parent=parent, child=child, descriptor=mem link relationship]";
		assertEquals(expectedString, memoryLink.toString());
	}

	
	/**
	 * Tests {@link MemoryLink#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = {Object.class})
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

	/**
	 * Tests {@link MemoryLink#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		MemoryLink memoryLink1 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		MemoryLink memoryLink2 = new MemoryLink("parent "+1, "child "+1, "descriptor"+1);
		assertEquals(memoryLink1.hashCode(), memoryLink2.hashCode());
	}
}
