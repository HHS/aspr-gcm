package gcm.manual.altpeople;

import java.util.BitSet;

import gcm.simulation.EnvironmentImpl;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * PeopleContainer implementor that uses a BitSet to record a boolean value of
 * true for each person contained. Since the BitSet will tend to be the size of
 * the full population of the simulation at approximately three bits per person,
 * this container is preferred to a Set-based implementor when the number of
 * people in the set exceeds 0.67% of the total population.
 * 
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class AltTreeBitSet1_Base implements AltPeopleContainer {
	// MAX_POWER is the highest power of two in a positive integer
	private final int MAX_POWER = 30;
	// BLOCK_POWER is the power of two that is the block length -- i.e. 64
	// bits
	private final int BLOCK_POWER = 6;
	// BLOCK_LENGTH is the 64 bit length of a block
	private final int BLOCK_LENGTH = 1 << BLOCK_POWER;
	// power is the power of two that sets the length of the tree array
	private int power = 1;
	
	// bitSet holds the values for each person
	private BitSet bitSet;
	// the tree holds summation nodes in an array that is length two the
	// power
	private int[] tree = new int[1 << power];
	// maxPid is the maximum(exclusive) person id value(int) that can be
	// contained at the current power.
	int maxPid = 1 << (power + BLOCK_POWER - 1);

	public AltTreeBitSet1_Base(int capacity) {		
		// initialize the size of the bitSet to that of the full population,
		// including removed people		
		bitSet = new BitSet(capacity);
	}

	

	/*
	 * Returns the nearest(<=) integer that is a power of two. For example,
	 * getNearestPowerOfTwo(37) = 32. Requires a positive input.
	 */
	private int getNearestPowerOfTwo(int n) {
		if (n < 1) {
			throw new RuntimeException("Non-positive value");
		}
		int p = 1 << MAX_POWER;

		while (p > 0) {
			if (n >= p) {
				return p;
			}
			p >>= 1;
		}
		return 0;
	}

	/*
	 * Returns the nearest(<=) power of two. For example, getNearestPowerOfTwo(37) =
	 * 5. Requires a positive input.
	 */
	private int getPower(int n) {

		if (n < 1) {
			throw new RuntimeException("Non-positive value");
		}

		int power = MAX_POWER;
		int p = 1 << power;

		while (p > 0) {
			if ((n & p) != 0) {
				return power;
			}
			p >>= 1;
			power--;
		}

		return 0;
	}

	@Override
	public boolean add(int value) {
		
		// do we need to grow?
		if (value >= maxPid) {
			// determine the new size of the tree array
			int newTreeSize = getNearestPowerOfTwo(value) >> (BLOCK_POWER - 2);

			/*
			 * The tree array grows by powers of two. We determine how many power levels the
			 * new tree array is over the existing one to help us transport values from the
			 * old array into the new array rather than recalculate those values.
			 * Essentially, the old tree will slide down the left hand side of the new tree,
			 * while leaving a trail of the old tree's head value behind.
			 * 
			 */

			// moving the old tree's values into the new tree
			int powerShift = getPower(newTreeSize) - power;
			int[] newTree = new int[newTreeSize];
			int base = 1;
			int newBase = base << powerShift;
			for (int p = 0; p < power; p++) {
				for (int i = 0; i < base; i++) {
					newTree[newBase + i] = tree[i + base];
				}
				base <<= 1;
				newBase <<= 1;
			}
			/*
			 * The old tree's root value now has to propagate up the new tree to its root
			 */
			base = 1 << powerShift;
			while (base > 1) {
				base >>= 1;
				newTree[base] = tree[1];
			}
			// swap the tree
			tree = newTree;

			// update maxPid and power
			maxPid <<= powerShift;
			power += powerShift;
		}
		// add the value
		if (!bitSet.get(value)) {
			bitSet.set(value);
			// select the block(index) that will receive the bit flip.
			int block = value >> BLOCK_POWER;
			block += (tree.length >> 1);
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				tree[block] += 1;
				block >>= 1;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(int value) {
		/*
		 * If the person is not contained, then don't try to remove them. This protects
		 * us from removals that are >= maxPid.
		 */
		if (!contains(value)) {
			return false;
		}
		
		if (bitSet.get(value)) {
			bitSet.set(value, false);
			// select the block(index) that will receive the bit flip.
			int block = value >> BLOCK_POWER;
			block += (tree.length >> 1);
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				tree[block] -= 1;
				block >>= 1;
			}
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		return tree[1];
	}

	

	@Override
	public boolean contains(int value) {
		return bitSet.get(value);
	}

	
	@Override
	public int getValue(int index) {
		if(index>=size()) {
			return -1;
		}

		/*
		 * We need to use an integer that is at least one, so we add one to the randomly
		 * selected index. We will reduce this amount until it reaches zero.
		 */
		int targetCount = index + 1;

		/*
		 * Find the mid point of the tree. Think of the tree array as a triangle with a
		 * single root node at the top. This will be the first array element in the last
		 * row(last half) in the tree. This is the row that maps to the blocks in the
		 * bitset.
		 */
		int midTreeIndex = tree.length >> 1;
		int treeIndex = 1;

		/*
		 * Walk downward in the tree. If we move to the right, we have to reduce the
		 * target value.
		 */
		while (treeIndex < midTreeIndex) {
			// move to the left child
			treeIndex = treeIndex << 1;
			// if the left child is less than the target count, then reduce the target count
			// by the number in the left child and move to the right child
			if (tree[treeIndex] < targetCount) {
				targetCount -= tree[treeIndex];
				treeIndex++;
			}
		}
		/*
		 * We have arrived at the element of the tree array that corresponds to the
		 * desired block in the bitset. We will need to determine the positions to scan
		 * in the bitset
		 */
		int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
		int bitSetStopIndex = bitSetStartIndex + BLOCK_LENGTH;
		/*
		 * Finally, we scan the bits and reduce the target count until it reaches zero.
		 */
		for (int i = bitSetStartIndex; i < bitSetStopIndex; i++) {
			if (bitSet.get(i)) {
				targetCount--;
				if (targetCount == 0) {
					return i;
				}
			}
		}
		return -1;
	}

}
