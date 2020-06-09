package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.containers.DoubleValueContainer;
@UnitTest(target = DoubleValueContainer.class)
public class AT_DoubleValueContainer {

	@Test
	public void testDoubleValueContainerConstructor() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0);
		assertNotNull(doubleValueContainer);

		doubleValueContainer = new DoubleValueContainer(0, 1000);
		assertNotNull(doubleValueContainer);
		assertTrue(doubleValueContainer.getCapacity() >= 1000);

		// pre conditions
		assertException(() -> new DoubleValueContainer(0, -1), NegativeArraySizeException.class);

	}

	@Test
	public void testGetCapacity() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0);

		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());

		doubleValueContainer.setValue(1, 123.4);
		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());

		doubleValueContainer.setValue(34, 36.4);
		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());

		doubleValueContainer.setValue(10, 15.4);
		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());

		doubleValueContainer.setValue(137, 25.26);
		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());

		doubleValueContainer.setValue(1000, 123.6345);
		assertTrue(doubleValueContainer.getCapacity() >= doubleValueContainer.size());
	}

	@Test
	public void testGetDefaultValue() {
		double defaultValue = 0;
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(defaultValue);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(),0);

		defaultValue = -10;
		doubleValueContainer = new DoubleValueContainer(defaultValue);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(),0);

		defaultValue = 10;
		doubleValueContainer = new DoubleValueContainer(defaultValue);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(),0);

	}

	@Test
	public void testGetValue() {
		double defaultValue = -345.34;
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(defaultValue);
		int highIndex = 1000;
		double delta = 2.3452346;

		double[] doubles = new double[highIndex];
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = delta+i;
		}
		for (int i = 0; i < doubles.length; i++) {
			doubleValueContainer.setValue(i, doubles[i]);
		}

		for (int i = 0; i < doubles.length; i++) {
			assertEquals(doubles[i],doubleValueContainer.getValue(i) ,0);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(doubleValueContainer.getValue(i + highIndex), defaultValue,0);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> doubleValueContainer.getValue(-1), RuntimeException.class);

	}

	@Test
	public void testSetCapacity() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0);

		int expectedCapacity = 5;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);
	}

	@Test
	public void testSetValue() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0);

		// long value
		double value = 12123.234;
		doubleValueContainer.setValue(0, value);
		assertEquals(value, doubleValueContainer.getValue(0),0);
		
		//pre-condition tests		
		assertException(()->doubleValueContainer.setValue(-1, 234.63), RuntimeException.class);


	}

	@Test
	public void testSize() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0, 100);
		assertEquals(0, doubleValueContainer.size());

		doubleValueContainer.setValue(3, 352.2345);
		assertEquals(4, doubleValueContainer.size());

		doubleValueContainer.setValue(1, 7456.63);
		assertEquals(4, doubleValueContainer.size());

		doubleValueContainer.setValue(15, 99.1576);
		assertEquals(16, doubleValueContainer.size());

		doubleValueContainer.setValue(300, 247.989762);
		assertEquals(301, doubleValueContainer.size());
	}

	
}
