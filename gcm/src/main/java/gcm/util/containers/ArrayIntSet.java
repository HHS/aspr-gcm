package gcm.util.containers;

import java.util.ArrayList;
import java.util.List;

import gcm.scenario.IntId;
import gcm.util.annotations.Source;

/**
 * An IntSet implementor that significantly reduces memory overhead (~factor of
 * 10) versus a LinkedHashSet<T>
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class ArrayIntSet<T extends IntId> implements IntSet<T> {

	/**
	 * The general best practice bucket depth for ArrayIntSets containing millions
	 * of entries.
	 */
	public final static float DEFAULT_TARGET_DEPTH = 80;

	/*
	 * An array of ArrayLists that hold the values in the set.
	 */
	private List<T>[] buckets;

	/*
	 * An array that is the same length as the values array that tracks the maximum
	 * size each ArrayList instance in values has reached during its life-span. This
	 * value is used to trigger an occasional rebuild of these ArrayLists to reduce
	 * instance size.
	 */
	private int[] maxSizes;

	/*
	 * The number of Integers stored in this set
	 */
	private int size;

	/*
	 * The targeted list depth that is used to determine the number of buckets(i.e.
	 * the length of the values array) in this set.
	 */
	private final float targetDepth;

	/*
	 * Dictates whether duplicate values are allowed. If duplicates are allowed then
	 * performance on add increases significantly since we do not check for a value
	 * already existing in this IntSet. If the caller to this ArrayIntSet can
	 * guarantee that no duplicate values are added then tolerateDuplicates should
	 * be set to false(the default value).
	 */
	private boolean tolerateDuplicates;

	/**
	 * Constructs an ArrayIntSet having the given target depth and tolerance of
	 * duplicate values. Setting tolerateDuplicates to true will often significantly
	 * improve performance.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>the target depth is not positive
	 * @param targetDepth
	 */
	public ArrayIntSet(float targetDepth, boolean tolerateDuplicates) {
		if (targetDepth < 1) {
			throw new IllegalArgumentException("Non-positive target depth: " + targetDepth);
		}
		this.targetDepth = targetDepth;
		this.tolerateDuplicates = tolerateDuplicates;
	}

	/**
	 * Constructs an ArrayIntSet having the given target depth that will tolerate
	 * duplicate values.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>the target depth is not positive
	 * @param targetDepth
	 */
	public ArrayIntSet(float targetDepth) {
		if (targetDepth < 1) {
			throw new IllegalArgumentException("Non-positive target depth: " + targetDepth);
		}
		this.targetDepth = targetDepth;
		this.tolerateDuplicates = true;
	}

	/**
	 * Constructs an ArrayIntSet having the DEFAULT_TARGET_DEPTH and tolerance of
	 * duplicate values. Setting tolerateDuplicates to true will often significantly
	 * improve performance.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>the target depth is not positive
	 * @param tolerateDuplicates
	 */
	public ArrayIntSet(boolean tolerateDuplicates) {

		this.targetDepth = DEFAULT_TARGET_DEPTH;
		this.tolerateDuplicates = tolerateDuplicates;
	}

	/**
	 * Constructs an ArrayIntSet having the DEFAULT_TARGET_DEPTH and tolerance of
	 * duplicate values.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>the target depth is not positive
	 */
	public ArrayIntSet() {
		this.targetDepth = DEFAULT_TARGET_DEPTH;
		this.tolerateDuplicates = true;
	}

	/*
	 * Grows the values and maxSizes arrays to achieve an average bucket depth that
	 * closer to the target bucket depth.
	 */
	@SuppressWarnings("unchecked")
	private void grow() {
		if (buckets == null) {
			// establish a single bucket
			buckets = new List[1];
			maxSizes = new int[1];
		} else {
			// double the number of buckets
			rebuild(buckets.length << 1);
		}
	}

	/*
	 * Shrinks the values and maxSizes arrays to achieve an average bucket depth
	 * that closer to the target bucket depth.
	 */
	private void shrink() {
		if ( buckets.length > 1) {
			// halve the number of buckets
			rebuild(buckets.length >> 1);
		}
	}

	/*
	 * Rebuilds the buckets to the new size for the values and maxSizes arrays.
	 */
	private void rebuild(int newSize) {
		@SuppressWarnings("unchecked")
		/*
		 * create a new values array to the new size
		 */
		List<T>[] newValues = new List[newSize];
		/*
		 * Rebuild the maxSizes array to the correct length. The old values in the
		 * maxSizes array can be forgotten.
		 */
		maxSizes = new int[newValues.length];
		/*
		 * Place the values from the old values array into the new values array.
		 */
		for (int i = 0; i < buckets.length; i++) {
			List<T> list = buckets[i];
			if (list != null) {
				for (T value : list) {
					/*
					 * Since the length of the values array is always a power of 2, we can use a
					 * bit-wise math trick to calculate the modulus of value with values.length to
					 * derive the index of the bucket where the value should be stored.
					 */
					int index = value.getValue() & (newValues.length - 1);
					/*
					 * Get the list where the value should be stored or create it if it does not yet
					 * exist.
					 */
					List<T> newList = newValues[index];
					if (newList == null) {
						newList = new ArrayList<>();
						newValues[index] = newList;
					}
					/*
					 * Place the value in the list.
					 */
					newList.add(value);
				}
				/*
				 * Null out the ArrayList instance to encourage the GC to collect the list now.
				 * 
				 */
				buckets[i] = null;
			}
		}
		/*
		 * We no longer need the old values array, so we replace it with the new values
		 * array.
		 */
		buckets = newValues;
		/*
		 * Finally, we establish the maxSizes array values.
		 */
		for (int i = 0; i < buckets.length; i++) {
			List<T> list = buckets[i];
			if (list != null) {
				maxSizes[i] = list.size();
			}
		}
	}

	@Override
	public void add(T t) {
		if (!tolerateDuplicates && contains(t)) {
			return;
		}
		if (buckets == null) {
			grow();
		}
		/*
		 * The bucket index is value % values.length, but this is a bit faster since we
		 * know the values array length is a power of two.
		 */
		int index = t.getValue() & (buckets.length - 1);

		/*
		 * Add the value to list located at the index
		 */
		List<T> list = buckets[index];
		if (list == null) {
			list = new ArrayList<>();
			buckets[index] = list;
		}
		list.add(t);
		size++;

		/*
		 * Update the maxSizes
		 */
		if (maxSizes[index] < list.size()) {
			maxSizes[index] = list.size();
		}

		/*
		 * If the averageDepth exceeds the target depth then we should grow.
		 * (size/values.length)>targetDepth
		 * 
		 */
		if (size > targetDepth * buckets.length) {
			grow();
		}
	}

	@Override
	public void remove(T t) {
		if (buckets == null) {
			return;
		}
		/*
		 * The bucket index is value % values.length, but this is a bit faster since we
		 * know the values array length is a power of two.
		 */
		int index = t.getValue() & (buckets.length - 1);
		/*
		 * Remove the value from the list
		 */
		List<T> list = buckets[index];
		
		if (list.remove(t)) {
			size--;
		} else {
			/*
			 * This list did not contain the value, so we are done.
			 */
			return;
		}
		/*
		 * If the list is now less than half its maxSize in the past, then we should
		 * rebuild the list and record the new maxSize for the list.
		 */
		if (list.size() * 2 < maxSizes[index]) {
			if (list.size() > 0) {
				buckets[index] = new ArrayList<>(list);
				maxSizes[index] = list.size();
			} else {
				buckets[index] = null;
				maxSizes[index] = 0;
			}
		}

		/*
		 * If the averageDepth is less than half the target depth then we should shrink.
		 * (size/values.length)*2<targetDepth
		 * 
		 */
		if (size * 2 < targetDepth * buckets.length) {
			shrink();
		}

		
	}

	@Override
	public List<T> getValues() {
		List<T> result = new ArrayList<>(size);
		for (List<T> list : buckets) {
			if (list != null) {
				result.addAll(list);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntSet");
		builder.append(getValues());
		return builder.toString();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(T t) {
		if (buckets == null) {
			return false;
		}
		int index = t.getValue() & (buckets.length - 1);
		/*
		 * Add the value to list located at the index
		 */
		List<T> list = buckets[index];
		return list.contains(t);
	}

}
