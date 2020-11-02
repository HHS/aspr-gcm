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
 * The ullage of the last layer of the tree has been eliminated
 * 
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class AltTreeBitSet4_Ullage implements AltPeopleContainer {
	private static int getMidWay(int size) {
		if(size<2) {
			throw new RuntimeException("cannot be calculated");
		}
		int result = 1;
		while(true) {
			int nextResult= 2*result;
			if(nextResult>=size) {
				return result;
			}
			result = nextResult;
		}		
	}
	private static class TreeHolder {
		private final int blockStartingIndex;
		public int getBlockStartingIndex() {
			return blockStartingIndex;
		}

		public TreeHolder(int size) {
			tree = new int[size];			
			blockStartingIndex = getMidWay(size);
		}

		private int[] tree;

		public int size() {
			return tree[1];
		}

		public int length() {
			return tree.length;
		}

		public void increment(int index) {
			tree[index]++;
		}

		public void set(int index, int value) {
			tree[index] = value;
		}

		public void decrement(int index) {
			tree[index]--;
		}

		public int get(int index) {
			return tree[index];
		}

	}

	// BLOCK_POWER is the power of two that is the block length -- i.e. 64
	// bits
	// private final int BLOCK_POWER = 6;
	// blockSize is the 64 bit length of a block
	// private final int blockSize = 1 << BLOCK_POWER;
	private final int blockSize;

	// power is the power of two that sets the length of the tree array

	
	// bitSet holds the values for each person
	private BitSet bitSet;
	// the tree holds summation nodes in an array that is length two the
	// power
	private TreeHolder treeHolder = new TreeHolder(2);
	// private int[] tree = new int[1 << power];
	// maxPid is the maximum(exclusive) person id value(int) that can be
	// contained at the current power.
	// int maxPid = 1 << (power + BLOCK_POWER - 1);
	int maxPid;

	public AltTreeBitSet4_Ullage(int capacity, int blockSize) {
		this.blockSize = blockSize;
		this.maxPid = blockSize;
		
		// initialize the size of the bitSet to that of the full population,
		// including removed people
		
		bitSet = new BitSet(capacity);		
		grow(capacity-1, false);	
	}

	

	

	private int getPower2(int n) {

		if (n < 1) {
			throw new RuntimeException("Non-positive value");
		}

		int result = 0;
		int value = 1;
		while (value < n) {
			value *= 2;
			result++;
		}
		return result;
	}

	private int getNextPowerOfTwo(int value) {
		int v = value;
		int result = 1;
		while (v != 0 && result != value) {
			v /= 2;
			result *= 2;
		}
		return result;
	}

	/*
	 * Grows the tree to allow the given pid to exist, filling the ullage in the
	 * base layer of the tree as required
	 */
	private void grow(int pid, boolean fillUllage) {
		// determine the new size of the tree array
		// int newTreeSize = getNearestPowerOfTwo(pid) >> (BLOCK_POWER - 2);

		int numberOfBlocks = pid / blockSize + 1;
		int treeTop = getNextPowerOfTwo(numberOfBlocks);
		int newTreeSize;
		if (fillUllage) {
			newTreeSize = treeTop * 2;
			maxPid = treeTop*blockSize;
		} else {
			newTreeSize = treeTop + numberOfBlocks;
			maxPid = numberOfBlocks*blockSize;
		}
		/*
		 * The tree array grows by powers of two. We determine how many power levels the
		 * new tree array is over the existing one to help us transport values from the
		 * old array into the new array rather than recalculate those values.
		 * Essentially, the old tree will slide down the left hand side of the new tree,
		 * while leaving a trail of the old tree's head value behind.
		 * 
		 */

		// moving the old tree's values into the new tree
		int power = getPower2(treeHolder.length());
		int powerShift = getPower2(newTreeSize) - power;
		// int[] newTree = new int[newTreeSize];
		TreeHolder newTreeHolder = new TreeHolder(newTreeSize);
		int base = 1;
		int newBase = base << powerShift;
		for (int p = 0; p < power; p++) {
			for (int i = 0; i < base; i++) {
				// newTree[newBase + i] = tree[i + base];
				int index = i + base;
				if (index < treeHolder.length()) {
					newTreeHolder.set(newBase + i, treeHolder.get(index));
				}
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
			// newTree[base] = tree[1];
			newTreeHolder.set(base, treeHolder.get(1));
		}
		// swap the tree
		// tree = newTree;
		treeHolder = newTreeHolder;

		
		

	}

	@Override
	public boolean add(int value) {



		// do we need to grow?
		if (value >= maxPid) {
			grow(value, true);
		}
		// add the value
		if (!bitSet.get(value)) {
			bitSet.set(value);
			// select the block(index) that will receive the bit flip.
			// int block = pid >> BLOCK_POWER;
			int block = value / blockSize;
			// block += (tree.length >> 1);
			block += treeHolder.getBlockStartingIndex();
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				// tree[block] += 1;
				treeHolder.increment(block);
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
			// int block = pid >> BLOCK_POWER;
			int block = value / blockSize;
			// block += (tree.length >> 1);
			block += (treeHolder.length() >> 1);
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				// tree[block] -= 1;
				treeHolder.decrement(block);
				block >>= 1;
			}
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		// return tree[1];
		return treeHolder.size();
	}

	

	@Override
	public boolean contains(int value) {
		return bitSet.get(value);
	}

	@Override
	public int getValue(int index) {
		if (index >= size()) {
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
		// int midTreeIndex = tree.length >> 1;
		int midTreeIndex = treeHolder.getBlockStartingIndex();
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
//			if (tree[treeIndex] < targetCount) {
//				targetCount -= tree[treeIndex];
//				treeIndex++;
//			}

			if (treeHolder.get(treeIndex) < targetCount) {
				targetCount -= treeHolder.get(treeIndex);
				treeIndex++;
			}

		}
		/*
		 * We have arrived at the element of the tree array that corresponds to the
		 * desired block in the bitset. We will need to determine the positions to scan
		 * in the bitset
		 */
		// int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
		int bitSetStartIndex = (treeIndex - midTreeIndex) * blockSize;
		int bitSetStopIndex = bitSetStartIndex + blockSize;
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
