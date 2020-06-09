package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.containers.IntValueContainer;
import gcm.util.containers.IntValueContainer.IntValueType;

/**
 * Test class for {@link IntValueContainer}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = IntValueContainer.class)
public class AT_IntValueContainer {

	/**
	 * Test for {@link IntValueContainer construction}
	 */
	@Test
	public void testIntValueContainerConstruction() {
		long defaultValue = 3452352345L;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		assertNotNull(intValueContainer);
		assertEquals(defaultValue, intValueContainer.getDefaultValueAsLong());

		int capacity = 1200;
		intValueContainer = new IntValueContainer(defaultValue, capacity);
		assertNotNull(intValueContainer);
		assertEquals(defaultValue, intValueContainer.getDefaultValueAsLong());
		assertTrue(intValueContainer.getCapacity() >= capacity);

		// the default value is returned for all non-negative indices for which
		// an overriding assignment has not occured.
		assertEquals(defaultValue, intValueContainer.getValueAsLong(200));

		// precondition tests
		int badCapacity = -12;
		assertException(() -> new IntValueContainer(defaultValue, badCapacity), NegativeArraySizeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#getValueAsByte(int)}
	 */
	@Test
	public void testGetValueAsByte() {
		long defaultValue = 123;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		byte[] bytes = new byte[highIndex];
		for (int i = 0; i < bytes.length; i++) {
			byte b = (byte) (i % 256 - 128);
			bytes[i] = b;
		}
		for (int i = 0; i < bytes.length; i++) {
			intValueContainer.setByteValue(i, bytes[i]);
		}

		for (int i = 0; i < bytes.length; i++) {
			assertEquals(intValueContainer.getValueAsByte(i), bytes[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsByte(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> intValueContainer.getValueAsByte(-1), RuntimeException.class);

		// if the value to return is not compatible with byte
		intValueContainer.setIntValue(highIndex, 240);
		assertException(() -> intValueContainer.getValueAsByte(highIndex), RuntimeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#getValueAsInt(int)}
	 */
	@Test
	public void testGetValueAsInt() {
		long defaultValue = 9546754;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		int[] ints = new int[highIndex];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = i;
		}
		for (int i = 0; i < ints.length; i++) {
			intValueContainer.setIntValue(i, ints[i]);
		}

		for (int i = 0; i < ints.length; i++) {
			assertEquals(intValueContainer.getValueAsInt(i), ints[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsInt(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> intValueContainer.getValueAsInt(-1), RuntimeException.class);

		// if the value to return is not compatible with int
		intValueContainer.setLongValue(highIndex, 535445345543L);
		assertException(() -> intValueContainer.getValueAsInt(highIndex), RuntimeException.class);
	}

	/**
	 * Test for {@link IntValueContainer#getValueAsLong(int)}
	 */
	@Test
	public void testGetValueAsLong() {
		long defaultValue = 9546754;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		long[] longs = new long[highIndex];
		for (int i = 0; i < longs.length; i++) {
			longs[i] = 745644534457456L + i;
		}
		for (int i = 0; i < longs.length; i++) {
			intValueContainer.setLongValue(i, longs[i]);
		}

		for (int i = 0; i < longs.length; i++) {
			assertEquals(intValueContainer.getValueAsLong(i), longs[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsLong(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> intValueContainer.getValueAsLong(-1), RuntimeException.class);

		// if the value to return is not compatible with long -- can't fail
		// since all values are compatible with long.

	}

	/**
	 * Test for {@link IntValueContainer#getValueAsShort(int)}
	 */
	@Test
	public void testGetValueAsShort() {
		short defaultValue = 30467;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		short[] shorts = new short[highIndex];
		for (int i = 0; i < shorts.length; i++) {
			short s = (short) (i % (256 * 256) - 128 * 256);
			shorts[i] = s;
		}
		for (int i = 0; i < shorts.length; i++) {
			intValueContainer.setShortValue(i, shorts[i]);
		}

		for (int i = 0; i < shorts.length; i++) {
			assertEquals(intValueContainer.getValueAsShort(i), shorts[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsShort(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertException(() -> intValueContainer.getValueAsShort(-1), RuntimeException.class);

		// if the value to return is not compatible with short
		intValueContainer.setIntValue(highIndex, 40000);
		assertException(() -> intValueContainer.getValueAsShort(highIndex), RuntimeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#setCapacity(int)}
	 */
	@Test
	public void testSetCapacity() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		int expectedCapacity = 5;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

	}

	/**
	 * Test for {@link IntValueContainer#getCapacity()}
	 */
	@Test
	public void testGetCapacity() {

		IntValueContainer intValueContainer = new IntValueContainer(0);

		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

		intValueContainer.setIntValue(1, 1234);
		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

		intValueContainer.setIntValue(34, 364);
		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

		intValueContainer.setIntValue(10, 154);
		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

		intValueContainer.setIntValue(137, 2526);
		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

		intValueContainer.setLongValue(1000, 1234534234234234234L);
		assertTrue(intValueContainer.getCapacity() >= intValueContainer.size());

	}

	/**
	 * Test for {@link IntValueContainer#setLongValue(int, long)}
	 */
	@Test
	public void testSetLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l = 523423463534562345L;
		intValueContainer.setLongValue(0, l);
		assertEquals(l, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l2 = 1;
		assertException(() -> intValueContainer.setLongValue(-1, l2), RuntimeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#setIntValue(int, int)}
	 */
	@Test
	public void testSetIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i = 70000;
		intValueContainer.setIntValue(0, i);
		assertEquals(i, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i2 = 1;
		assertException(() -> intValueContainer.setIntValue(-1, i2), RuntimeException.class);
	}

	/**
	 * Test for {@link IntValueContainer#setShortValue(int, short)}
	 */

	@Test
	public void testSetShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s = 300;
		intValueContainer.setShortValue(0, s);
		assertEquals(s, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s2 = 1;
		assertException(() -> intValueContainer.setShortValue(-1, s2), RuntimeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#setByteValue(int, byte)}
	 */

	@Test
	public void testSetByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b = 5;
		intValueContainer.setByteValue(0, b);
		assertEquals(b, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b2 = 1;
		assertException(() -> intValueContainer.setByteValue(-1, b2), RuntimeException.class);

	}

	/**
	 * Test for {@link IntValueContainer#incrementIntValue(int, int)}
	 */

	@Test
	public void testIncrementIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i1 = 70000;
		intValueContainer.setIntValue(0, i1);
		int i2 = 2000;
		intValueContainer.incrementIntValue(0, i2);
		assertEquals(i1 + i2, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i3 = 1;
		assertException(() -> intValueContainer.incrementIntValue(-1, i3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertException(() -> intValueContainer.incrementIntValue(0, i3), ArithmeticException.class);
	}
	/**
	 * Test for {@link IntValueContainer#incrementLongValue(int, long)}
	 */
	@Test
	public void testIncrementLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l1 = 523423463534562345L;
		intValueContainer.setLongValue(0, l1);
		long l2 = 66457456456456456L;
		intValueContainer.incrementLongValue(0, l2);
		assertEquals(l1 + l2, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l3 = 1;
		assertException(() -> intValueContainer.incrementLongValue(-1, l3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertException(() -> intValueContainer.incrementLongValue(0, l3), ArithmeticException.class);
	}
	/**
	 * Test for {@link IntValueContainer#incrementShortValue(int, short)}
	 */
	@Test
	public void testIncrementShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s1 = 300;
		intValueContainer.setShortValue(0, s1);
		short s2 = 100;
		intValueContainer.incrementShortValue(0, s2);
		assertEquals(s1 + s2, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s3 = 1;
		assertException(() -> intValueContainer.incrementShortValue(-1, s3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertException(() -> intValueContainer.incrementShortValue(0, s3), ArithmeticException.class);
	}

	/**
	 * Test for {@link IntValueContainer#incrementByteValue(int, byte)}
	 */
	@Test
	public void testIncrementByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b1 = 5;
		intValueContainer.setByteValue(0, b1);
		byte b2 = 12;
		intValueContainer.incrementByteValue(0, b2);
		assertEquals(b1 + b2, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b3 = 1;
		assertException(() -> intValueContainer.incrementByteValue(-1, b3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertException(() -> intValueContainer.incrementByteValue(0, b3), ArithmeticException.class);
	}

	/**
	 * Test for {@link IntValueContainer#decrementByteValue(int, byte)}
	 */
	@Test
	public void testDecrementByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b1 = 5;
		intValueContainer.setByteValue(0, b1);

		byte b2 = 12;
		intValueContainer.decrementByteValue(0, b2);
		assertEquals(b1 - b2, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b3 = 1;
		assertException(() -> intValueContainer.decrementByteValue(-1, b3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertException(() -> intValueContainer.decrementByteValue(0, b3), ArithmeticException.class);
	}

	/**
	 * Test for {@link IntValueContainer#decrementShortValue(int, short)}
	 */
	@Test
	public void testDecrementShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s1 = 300;
		intValueContainer.setShortValue(0, s1);
		short s2 = 100;
		intValueContainer.decrementShortValue(0, s2);
		assertEquals(s1 - s2, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s3 = 1;
		assertException(() -> intValueContainer.decrementShortValue(-1, s3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertException(() -> intValueContainer.decrementShortValue(0, s3), ArithmeticException.class);
	}

	/**
	 * Test for {@link IntValueContainer#decrementIntValue(int, int)}
	 */
	@Test
	public void testDecrementIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i1 = 70000;
		intValueContainer.setIntValue(0, i1);
		int i2 = 2000;
		intValueContainer.decrementIntValue(0, i2);
		assertEquals(i1 - i2, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i3 = 1;
		assertException(() -> intValueContainer.decrementIntValue(-1, i3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertException(() -> intValueContainer.decrementIntValue(0, i3), ArithmeticException.class);
	}

	/**
	 * Test for {@link IntValueContainer#decrementLongValue(int, long)}
	 */
	@Test
	public void testDecrementLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l1 = 523423463534562345L;
		intValueContainer.setLongValue(0, l1);
		long l2 = 66457456456456456L;
		intValueContainer.decrementLongValue(0, l2);
		assertEquals(l1 - l2, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l3 = 1;
		assertException(() -> intValueContainer.decrementLongValue(-1, l3), RuntimeException.class);
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertException(() -> intValueContainer.decrementLongValue(0, l3), ArithmeticException.class);
	}

	
	/**
	 * Test for {@link IntValueContainer#size()}
	 */
	@Test
	public void testSize() {

		IntValueContainer intValueContainer = new IntValueContainer(0, 100);
		assertEquals(0, intValueContainer.size());

		intValueContainer.setIntValue(3, 352);
		assertEquals(4, intValueContainer.size());

		intValueContainer.setIntValue(1, 7456);
		assertEquals(4, intValueContainer.size());

		intValueContainer.setIntValue(15, 99);
		assertEquals(16, intValueContainer.size());

		intValueContainer.setIntValue(300, 247);
		assertEquals(301, intValueContainer.size());
	}
	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsByte()}
	 */
	@Test
	public void testGetDefaultValueAsByte() {
		byte expected = 120;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		byte actual = intValueContainer.getDefaultValueAsByte();
		assertEquals(expected, actual);

		// pre-condition tests

		// default short
		assertException(() -> new IntValueContainer(30000).getDefaultValueAsByte(), RuntimeException.class);

		// default int
		assertException(() -> new IntValueContainer(120000).getDefaultValueAsByte(), RuntimeException.class);

		// default long
		assertException(() -> new IntValueContainer(123124235123234234L).getDefaultValueAsByte(), RuntimeException.class);

	}
	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsShort()}
	 */
	@Test
	public void testGetDefaultValueAsShort() {
		short expected = 32000;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		short actual = intValueContainer.getDefaultValueAsShort();
		assertEquals(expected, actual);

		// pre-condition tests

		// default int
		assertException(() -> new IntValueContainer(120000).getDefaultValueAsShort(), RuntimeException.class);

		// default long
		assertException(() -> new IntValueContainer(123124235123234234L).getDefaultValueAsShort(), RuntimeException.class);
	}
	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsInt()}
	 */
	@Test
	public void testGetDefaultValueAsInt() {
		int expected = 52000;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		int actual = intValueContainer.getDefaultValueAsInt();
		assertEquals(expected, actual);

		// pre-condition tests

		// default long
		assertException(() -> new IntValueContainer(123124235123234234L).getDefaultValueAsInt(), RuntimeException.class);
	}
	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsLong()}
	 */
	@Test
	public void testGetDefaultValueAsLong() {
		long expected = 364534534534534345L;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		long actual = intValueContainer.getDefaultValueAsLong();
		assertEquals(expected, actual);

		// pre-condition tests -- none
	}
	/**
	 * Test for {@link IntValueContainer#getIntValueType()}
	 */
	@Test
	public void testGetIntValueType() {
		IntValueContainer intValueContainer = new IntValueContainer(0);
		assertEquals(IntValueType.BYTE, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(0, 1);
		assertEquals(1, intValueContainer.size());
		assertEquals(IntValueType.BYTE, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(1, 130);
		assertEquals(IntValueType.SHORT, intValueContainer.getIntValueType());
		assertEquals(2, intValueContainer.size());

		intValueContainer.setIntValue(2, 70000);
		assertEquals(IntValueType.INT, intValueContainer.getIntValueType());
		assertEquals(3, intValueContainer.size());

		intValueContainer.setLongValue(3, 123123123123123123L);
		assertEquals(IntValueType.LONG, intValueContainer.getIntValueType());
		assertEquals(4, intValueContainer.size());

		intValueContainer.setIntValue(4, 1);
		assertEquals(IntValueType.LONG, intValueContainer.getIntValueType());
		assertEquals(5, intValueContainer.size());

	}

}
