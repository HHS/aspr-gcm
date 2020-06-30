package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.containers.FloatValueContainer;
/**
 * Test class for {@link FloatValueContainer}
 * @author Shawn Hatch
 *
 */
@UnitTest(target = FloatValueContainer.class)
public class AT_FloatValueContainer {

	/**
	 * Test {@link FloatValueContainer} contructor
	 */
	@Test	
	public void testFloatValueContainerConstructor() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);
		assertNotNull(floatValueContainer);

		floatValueContainer = new FloatValueContainer(0, 1000);
		assertNotNull(floatValueContainer);
		assertTrue(floatValueContainer.getCapacity() >= 1000);

		// pre conditions
		assertException(() -> new FloatValueContainer(0, -1), NegativeArraySizeException.class);

	}

	/**
	 * Tests {@link FloatValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(name = "getCapacity", args= {})
	public void testGetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(1, 123.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(34, 36.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(10, 15.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(137, 25.26f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(1000, 123.6345f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());
	}

	/**
	 * Tests {@link FloatValueContainer#getDefaultValue()}
	 */
	@Test
	@UnitTestMethod(name = "getDefaultValue", args= {})
	public void testGetDefaultValue() {
		float defaultValue = 0;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(),0);

		defaultValue = -10;
		floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(),0);

		defaultValue = 10;
		floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(),0);

	}

	/**
	 * Test {@link FloatValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args= {int.class})
	public void testGetValue() {		
		float defaultValue = -345.34f;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue);
		int highIndex = 1000;
		float delta = 2.3452346f;

		float[] floats = new float[highIndex];
		for (int i = 0; i < floats.length; i++) {
			floats[i] = delta+i;
		}
		for (int i = 0; i < floats.length; i++) {
			floatValueContainer.setValue(i, floats[i]);
		}

		for (int i = 0; i < floats.length; i++) {
			assertEquals(floats[i],floatValueContainer.getValue(i) ,0);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(floatValueContainer.getValue(i + highIndex), defaultValue,0);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> floatValueContainer.getValue(-1), RuntimeException.class);

	}

	/**
	 * Tests {@link FloatValueContainer#setCapacity(int)}
	 */
	@Test
	@UnitTestMethod(name = "setCapacity", args= {int.class})
	public void testSetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		int expectedCapacity = 5;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);
	}

	/**
	 * Test {@link FloatValueContainer#setValue(int, float)}
	 */
	@Test
	@UnitTestMethod(name = "setValue", args= {int.class, float.class})
	public void testSetValue() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		// long value
		float value = 12123.234f;
		floatValueContainer.setValue(0, value);
		assertEquals(value, floatValueContainer.getValue(0),0);
		
		//pre-condition tests		
		assertException(()->floatValueContainer.setValue(-1, 234.63f), RuntimeException.class);


	}

	/**
	 * Tests {@link FloatValueContainer#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args= {})
	public void testSize() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0, 100);
		assertEquals(0, floatValueContainer.size());

		floatValueContainer.setValue(3, 352.2345f);
		assertEquals(4, floatValueContainer.size());

		floatValueContainer.setValue(1, 7456.63f);
		assertEquals(4, floatValueContainer.size());

		floatValueContainer.setValue(15, 99.1576f);
		assertEquals(16, floatValueContainer.size());

		floatValueContainer.setValue(300, 247.989762f);
		assertEquals(301, floatValueContainer.size());
	}

	
}
