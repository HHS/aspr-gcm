package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.containers.ObjectValueContainer;
/**
 * Test class for {@link ObjectValueContainer}
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ObjectValueContainer.class)
public class AT_ObjectValueContainer {
	
	@Test
	public void testConstructor() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
		assertNotNull(objectValueContainer);
		
		objectValueContainer = new ObjectValueContainer(null, 20);
		assertNotNull(objectValueContainer);
		
		//pre-condition tests
		assertException(()->new ObjectValueContainer(null, -4), IllegalArgumentException.class);
		
	}
	
	
	@Test
	public void testSetValue() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
		objectValueContainer.setValue(3, "dog");
		objectValueContainer.setValue(1, "cat");
		objectValueContainer.setValue(4, "pig");
		objectValueContainer.setValue(7, "cow");
		objectValueContainer.setValue(3, "bat");
		objectValueContainer.setValue(5, null);

		
		assertEquals(defaultValue,objectValueContainer.getValue(0));
		assertEquals("cat",objectValueContainer.getValue(1));
		assertEquals(defaultValue,objectValueContainer.getValue(2));
		assertEquals("bat",objectValueContainer.getValue(3));
		assertEquals("pig",objectValueContainer.getValue(4));
		assertNull(objectValueContainer.getValue(5));
		assertEquals(defaultValue,objectValueContainer.getValue(6));
		assertEquals("cow",objectValueContainer.getValue(7));
		assertEquals(defaultValue,objectValueContainer.getValue(8));
		assertEquals(defaultValue,objectValueContainer.getValue(9));
		
		
		//test pre-conditions
		assertException(()->objectValueContainer.setValue(-1, "frog"), IllegalArgumentException.class);
	}

	@Test
	public void testGetValue() {
		
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
		objectValueContainer.setValue(3, "dog");
		objectValueContainer.setValue(1, "cat");
		objectValueContainer.setValue(4, "pig");
		objectValueContainer.setValue(7, "cow");
		objectValueContainer.setValue(3, "bat");
		objectValueContainer.setValue(5, null);

		
		assertEquals(defaultValue,objectValueContainer.getValue(0));
		assertEquals("cat",objectValueContainer.getValue(1));
		assertEquals(defaultValue,objectValueContainer.getValue(2));
		assertEquals("bat",objectValueContainer.getValue(3));
		assertEquals("pig",objectValueContainer.getValue(4));
		assertNull(objectValueContainer.getValue(5));
		assertEquals(defaultValue,objectValueContainer.getValue(6));
		assertEquals("cow",objectValueContainer.getValue(7));
		assertEquals(defaultValue,objectValueContainer.getValue(8));
		assertEquals(defaultValue,objectValueContainer.getValue(9));
		
		
		//test pre-conditions
		assertException(()->objectValueContainer.getValue(-1), IllegalArgumentException.class);
	}
	
	

}
