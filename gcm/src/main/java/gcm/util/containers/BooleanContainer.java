package gcm.util.containers;

import java.util.BitSet;

import gcm.util.annotations.Source;

/**
 * A container that maps non-negative int index values to booleans by storing
 * each boolean as a single bit in a BitSet. Returns a default boolean value for
 * every non-negative int index value until the value is explicitly set by an
 * invocation to the set() method.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class BooleanContainer {	
	/*
	 * The default value to return for all indexes that are greater than or
	 * equal to the bounding index.
	 */
	private final boolean defaultValue;

	/*
	 * The lowest index value that has not been the subject of an explicit
	 * invocation of the set() method.
	 */
	private int boundingIndex;

	public BooleanContainer(boolean defaultValue) {
		this.defaultValue = defaultValue;
		bitSet = new BitSet();
	}
	
	public BooleanContainer(boolean defaultValue,int capacity) {
		this.defaultValue = defaultValue;
		boundingIndex = capacity;
		bitSet = new BitSet(boundingIndex);
		if(defaultValue) {
			bitSet.set(0, boundingIndex, defaultValue);
		}
	}

	/*
	 * The bitSet containing the bit level representation of the Booleans.
	 */
	private BitSet bitSet;

	/**
	 * Returns the boolean value associated with the given index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 */
	public boolean get(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("index = " + index);
		}
		/*
		 * If the index is beyond any we have had set, then return the default
		 * value.
		 */
		if (index >= boundingIndex) {
			return defaultValue;
		}

		return bitSet.get(index);
	}

	/**
	 * Set the boolean value associated with the given index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @param index
	 * @param value
	 */
	public void set(int index, boolean value) {		
		if (index < 0) {
			throw new IndexOutOfBoundsException("index = " + index);
		}
		// if the index is new to us, then fill the bitSet with the default from
		// the bounding index to the index.
		if (index >= boundingIndex) {
			int newBoundingIndex = index + 1;
			bitSet.set(boundingIndex, newBoundingIndex, defaultValue);
			boundingIndex = newBoundingIndex;
		}
		bitSet.set(index, value);
	}

}