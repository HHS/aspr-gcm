package gcm.util.containers;

import java.util.Arrays;

import gcm.util.annotations.Source;

/**
 * A container that maps non-negative int index values to bytes, shorts, ints or
 * longs. Values are stored internally as whatever int-type that logically
 * represents the highest value in this container. For example, if this
 * container contains the values [1,0,14,-20,300] then these would be stored in
 * a short array since 300 exceeds the highest value of byte.
 * 
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class IntValueContainer {

	/**
	 * An enumeration representing the four int-based primitive data types in
	 * Java. It
	 *
	 */
	public static enum IntValueType {

		BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE), SHORT(Short.MIN_VALUE, Short.MAX_VALUE), INT(Integer.MIN_VALUE, Integer.MAX_VALUE), LONG(Long.MIN_VALUE, Long.MAX_VALUE);

		private final long min;

		private final long max;

		/*
		 * Constructs the IntValueType with its min and max values
		 */
		private IntValueType(long min, long max) {
			this.min = min;
			this.max = max;
		}

		/*
		 * Returns true if and only if this IntValueType represents an array
		 * whose min and max values inclusively bound the given value.
		 */
		private boolean isCompatibleValue(long value) {
			return value >= min && value <= max;
		}

	}

	/*
	 * Rebuilds the subTypeArray to be the most compact representation that is
	 * value-compatible with the given long.
	 * 
	 */
	private SubTypeArray rebuildSubTypeArray(long value) {
		if (IntValueType.SHORT.isCompatibleValue(value)) {
			return new ShortArray(subTypeArray);
		}
		if (IntValueType.INT.isCompatibleValue(value)) {
			return new IntArray(subTypeArray);
		}
		return new LongArray(subTypeArray);
	}

	/*
	 * Common interface for the four array wrapper classes.
	 */
	private static interface SubTypeArray {

		public IntValueType getIntValueType();

		public long getDefaultValue();

		public long getValue(int index);

		public void setValue(int index, long value);

		public int size();

		public void setCapacity(int capacity);

		public int getCapacity();

	}

	/*
	 * SubTypeArray implementor for longs
	 */
	private static class LongArray implements SubTypeArray {
		private long[] values;
		private long defaultValue;
		private int size;

		public LongArray(int capacity, long defaultValue) {
			values = new long[capacity];
			if (defaultValue != 0) {
				for (int i = 0; i < capacity; i++) {
					values[i] = defaultValue;
				}
			}
			size = values.length;
			this.defaultValue = defaultValue;
		}

		public LongArray(SubTypeArray subTypeArray) {
			this.defaultValue = subTypeArray.getDefaultValue();
			values = new long[subTypeArray.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (int) subTypeArray.getValue(i);
			}
			size = values.length;
		}

		private void grow(int capacity) {
			int oldCapacity = values.length;
			int newCapacity = Math.max(capacity, values.length + (values.length >> 2));
			values = Arrays.copyOf(values, newCapacity);
			if (defaultValue != 0) {
				for (int i = oldCapacity; i < newCapacity; i++) {
					values[i] = defaultValue;
				}
			}
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}
			if (index >= size) {
				size = index + 1;
			}

			values[index] = value;

		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.LONG;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

	}
	/*
	 * SubTypeArray implementor for ints
	 */

	private static class IntArray implements SubTypeArray {
		private int[] values;
		private int defaultValue;
		private int size;

		public IntArray(int capacity, int defaultValue) {
			values = new int[capacity];
			if (defaultValue != 0) {
				for (int i = 0; i < capacity; i++) {
					values[i] = defaultValue;
				}
			}
			size = values.length;
			this.defaultValue = defaultValue;
		}

		public IntArray(SubTypeArray subTypeArray) {
			this.defaultValue = (int) subTypeArray.getDefaultValue();
			values = new int[subTypeArray.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (int) subTypeArray.getValue(i);
			}
			size = values.length;
		}

		private void grow(int capacity) {
			int oldCapacity = values.length;
			int newCapacity = Math.max(capacity, values.length + (values.length >> 2));
			values = Arrays.copyOf(values, newCapacity);
			if (defaultValue != 0) {
				for (int i = oldCapacity; i < newCapacity; i++) {
					values[i] = defaultValue;
				}
			}
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}
			if (index >= size) {
				size = index + 1;
			}

			values[index] = (int) value;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.INT;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

	}

	/*
	 * SubTypeArray implementor for shorts
	 */
	private static class ShortArray implements SubTypeArray {
		private short[] values;
		private short defaultValue;
		private int size;

		public ShortArray(int capacity, short defaultValue) {
			values = new short[capacity];
			if (defaultValue != 0) {
				for (int i = 0; i < capacity; i++) {
					values[i] = defaultValue;
				}
			}
			size = values.length;
			this.defaultValue = defaultValue;
		}

		public ShortArray(SubTypeArray subTypeArray) {
			this.defaultValue = (short) subTypeArray.getDefaultValue();
			values = new short[subTypeArray.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (short) subTypeArray.getValue(i);
			}
			size = values.length;
		}

		private void grow(int capacity) {
			int oldCapacity = values.length;
			int newCapacity = Math.max(capacity, values.length + (values.length >> 2));
			values = Arrays.copyOf(values, newCapacity);
			if (defaultValue != 0) {
				for (int i = oldCapacity; i < newCapacity; i++) {
					values[i] = defaultValue;
				}
			}
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}
			if (index >= size) {
				size = index + 1;
			}

			values[index] = (short) value;
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.SHORT;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

	}

	/*
	 * SubTypeArray implementor for bytes
	 */
	private static class ByteArray implements SubTypeArray {
		private byte[] values;
		private byte defaultValue;
		private int size;

		public ByteArray(int capacity, byte defaultValue) {
			values = new byte[capacity];
			if (defaultValue != 0) {
				for (int i = 0; i < capacity; i++) {
					values[i] = defaultValue;
				}
			}
			this.defaultValue = defaultValue;
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}
			if (index >= size) {
				size = index + 1;
			}
			values[index] = (byte) value;
		}

		private void grow(int capacity) {
			int oldCapacity = values.length;
			int newCapacity = Math.max(capacity, values.length + (values.length >> 2));
			values = Arrays.copyOf(values, newCapacity);
			if (defaultValue != 0) {
				for (int i = oldCapacity; i < newCapacity; i++) {
					values[i] = defaultValue;
				}
			}
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.BYTE;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

	}

	/*
	 * The array holding instance.
	 */
	private SubTypeArray subTypeArray;

	/**
	 * Constructs the IntValueContainer with the given default value.
	 * 
	 * @param defaultValue
	 */
	public IntValueContainer(long defaultValue) {
		this(defaultValue, 16);
	}

	/**
	 * Returns the default value as a byte.
	 * 
	 * @throws RuntimeException
	 *             if the default value is not compatible with byte
	 * 
	 */
	public byte getDefaultValueAsByte() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.BYTE.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (byte) result;
	}

	/**
	 * Returns the default value as a short.
	 * 
	 * @throws RuntimeException
	 *             if the default value is not compatible with short
	 * 
	 */
	public short getDefaultValueAsShort() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.SHORT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (short) result;
	}

	/**
	 * Returns the default value as an int.
	 * 
	 * @throws RuntimeException
	 *             if the default value is not compatible with int
	 * 
	 */
	public int getDefaultValueAsInt() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.INT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (int) result;
	}

	/**
	 * Returns the default value as a long.
	 */
	public long getDefaultValueAsLong() {
		long result = subTypeArray.getDefaultValue();
		return result;
	}

	/**
	 * Constructs the IntValueContainer with the given default value and initial
	 * capacity
	 * 
	 * @param defaultValue
	 * @param capacity
	 * 
	 * @throws NegativeArraySizeException
	 *             if the capacity is negative
	 */
	public IntValueContainer(long defaultValue, int capacity) {

		if (IntValueType.BYTE.isCompatibleValue(defaultValue)) {
			subTypeArray = new ByteArray(capacity, (byte) defaultValue);
		} else if (IntValueType.SHORT.isCompatibleValue(defaultValue)) {
			subTypeArray = new ShortArray(capacity, (short) defaultValue);
		} else if (IntValueType.INT.isCompatibleValue(defaultValue)) {
			subTypeArray = new IntArray(capacity, (int) defaultValue);
		} else {
			subTypeArray = new LongArray(capacity, defaultValue);
		}
	}

	/**
	 * Sets the capacity to the given capacity if the current capacity is less
	 * than the one given.
	 */
	public void setCapacity(int capacity) {
		subTypeArray.setCapacity(capacity);
	}

	/**
	 * Returns the value at index as a byte.
	 * 
	 * @param index
	 * @return
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *             <li>if the value to return is not compatible with byte
	 * 
	 */
	public byte getValueAsByte(int index) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long result;
		if (index < subTypeArray.size()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}
		if (!IntValueType.BYTE.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (byte) result;
	}

	/**
	 * Returns the value at index as a short.
	 * 
	 * @param index
	 * @return
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *             <li>if the value to return is not compatible with short
	 * 
	 */
	public short getValueAsShort(int index) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long result;
		if (index < subTypeArray.size()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}
		if (!IntValueType.SHORT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (short) result;

	}

	/**
	 * Returns the value at index as a long.
	 * 
	 * @param index
	 * @return
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *             <li>if the value to return is not compatible with long
	 * 
	 */
	public int getValueAsInt(int index) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long result;
		if (index < subTypeArray.size()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}

		if (!IntValueType.INT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result + " at index " + index);
		}
		return (int) result;
	}

	/**
	 * Returns the value at index as a long.
	 * 
	 * @param index
	 * @return
	 * @throws RuntimeException
	 *             if index < 0
	 * 
	 */
	public long getValueAsLong(int index) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long result;
		if (index < subTypeArray.size()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}

		return result;
	}

	/**
	 * Sets the value at the index to the given byte
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void setByteValue(int index, byte value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given short
	 * 
	 * @throws RuntimeException
	 *             <li>if index < 0
	 * 
	 * 
	 */
	public void setShortValue(int index, short value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}
		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given int
	 * 
	 * @throws RuntimeException
	 *             <li>if index < 0
	 * 
	 * 
	 */
	public void setIntValue(int index, int value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}
		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given long
	 * 
	 * @throws RuntimeException
	 *             <li>if index < 0
	 * 
	 */
	public void setLongValue(int index, long value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}
		subTypeArray.setValue(index, value);
	}

	/**
	 * Returns the size of this container, determined by the highest index for
	 * which a value assignment has occurred.
	 */
	public int size() {
		return subTypeArray.size();
	}

	/**
	 * Returns the IntValueType for this container. Each IntValueType
	 * corresponds to the current implementation type of the underlying array of
	 * primitives.
	 */
	public IntValueType getIntValueType() {
		return subTypeArray.getIntValueType();
	}

	/**
	 * Returns the capacity of this container. Capacity is guaranteed to be
	 * greater than or equal to size.
	 */
	public int getCapacity() {
		return subTypeArray.getCapacity();
	}

	/**
	 * Increments the value at the index by the given byte
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void incrementByteValue(int index, byte value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}

		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given short
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void incrementShortValue(int index, short value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given int
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void incrementIntValue(int index, int value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given long
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void incrementLongValue(int index, long value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Decrements the value at the index by the given byte
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void decrementByteValue(int index, byte value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}

		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given short
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void decrementShortValue(int index, short value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given int
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void decrementIntValue(int index, int value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given long
	 *
	 * @throws RuntimeException
	 *             <li>if index < 0
	 *
	 */
	public void decrementLongValue(int index, long value) {
		if (index < 0) {
			throw new RuntimeException("index out of bounds " + index);
		}
		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

}
