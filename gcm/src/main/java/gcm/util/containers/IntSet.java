package gcm.util.containers;

import java.util.List;

import gcm.scenario.IntId;
import gcm.util.annotations.Source;

/**
 * A set-like collection of Integers. Implementors are generally intended to
 * avoid the high memory overhead of Java core hash-based collections while
 * performing nearly as well in runtime.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface IntSet<T extends IntId> {

	/**
	 * Adds the value to the this IntSet. To improve efficiency IntSets, unlike
	 * Sets, do not guarantee intuitive behavior when duplicate values are
	 * added. For example the getValues() method may return a set that is
	 * smaller than the size() method would indicate. The implementing class
	 * will determine whether duplicates are rejected.
	 */
	public void add(T value);

	/**
	 * Removes the value from this IntSet. If duplicate values are allowed by
	 * the implementing class, only a single instance is guaranteed to be
	 * removed.
	 * 
	 * @param value
	 */
	public void remove(T value);

	/**
	 * Returns the values contained in this IntSet in a repeatable order when
	 * the resultant set's iteration. The specific order is unspecified but is
	 * repeatable relative to identical sequences of mutation's against this
	 * IntSet. May return duplicate values depending on the implementor's policy
	 * for handling duplicates.
	 */
	public List<T> getValues();

	/**
	 * Returns the number of values contained in this IntSet, including
	 * duplicates.
	 */
	public int size();

	/**
	 * Returns true if and only if the given value is contained in this IntSet
	 */
	public boolean contains(T value);

	/**
	 * Returns the contained integer values, including duplicates, in a
	 * repeatable but unspecified order. The returned String will be formatted
	 * as:
	 * 
	 * IntSet[, , ...] with a single space after each comma.
	 */
	@Override
	public String toString();

}
