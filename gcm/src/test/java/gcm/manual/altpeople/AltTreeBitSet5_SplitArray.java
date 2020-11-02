package gcm.manual.altpeople;

import java.util.BitSet;

import org.apache.commons.math3.util.FastMath;

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
 * 
 * The tree is now split across int, short and byte array structures
 * 
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class AltTreeBitSet5_SplitArray implements AltPeopleContainer {

	private static class TreeHolder {
		private final int blockStartingIndex;
		// maxPid is the maximum(exclusive) person id value(int) that can be
		int maxPid;

		public TreeHolder(int capacity, int blockSize, boolean fillUllage) {
			long baseLayerBlockCount = capacity / blockSize;
			if (capacity % blockSize != 0) {
				baseLayerBlockCount++;
			}
			baseLayerBlockCount = FastMath.max(baseLayerBlockCount, 1);

			long treePower = 0;
			long nodeCount = 1;
			while (nodeCount < baseLayerBlockCount) {
				treePower++;
				nodeCount *= 2;
			}
			blockStartingIndex = 1 << treePower;
			if (fillUllage) {
				baseLayerBlockCount = blockStartingIndex;
			}

			maxPid = (int) baseLayerBlockCount * blockSize;

			treePower++;

			long byteNodeCount = 0;
			long shortNodeCount = 0;
			long intNodeCount = 0;

			long maxNodeValue = blockSize;
			for (long power = treePower - 1; power >= 0; power--) {
				long nodesOnLayer;
				if (power == treePower - 1) {
					nodesOnLayer = baseLayerBlockCount;
				} else {
					nodesOnLayer = 1 << power;
				}
				if (maxNodeValue <= Byte.MAX_VALUE) {
					byteNodeCount += nodesOnLayer;
				} else if (maxNodeValue <= Short.MAX_VALUE) {
					shortNodeCount += nodesOnLayer;
				} else {
					intNodeCount += nodesOnLayer;
				}
				maxNodeValue *= 2;
			}
			if (intNodeCount > 0) {
				intNodeCount++;
			} else if (shortNodeCount > 0) {
				shortNodeCount++;
			} else {
				byteNodeCount++;
			}


			intNodes = new int[(int) intNodeCount];
			shortNodes = new short[(int) shortNodeCount];
			byteNodes = new byte[(int) byteNodeCount];

		}
		private int[] intNodes;
		private short[] shortNodes;
		private byte[] byteNodes;

		public void increment(int index) {

			if (index < intNodes.length) {
				intNodes[index]++;
			} else {
				index -= intNodes.length;
				if (index < shortNodes.length) {
					shortNodes[index]++;
				} else {
					index -= shortNodes.length;
					if (index < byteNodes.length) {
						byteNodes[index]++;
					}
				}
			}
		}

		public void decrement(int index) {
			if (index < intNodes.length) {
				intNodes[index]--;
			} else {
				index -= intNodes.length;
				if (index < shortNodes.length) {
					shortNodes[index]--;
				} else {
					index -= shortNodes.length;
					if (index < byteNodes.length) {
						byteNodes[index]--;
					}
				}
			}
		}

		public int get(int index) {
			if (index < intNodes.length) {
				return intNodes[index];
			}
			index -= intNodes.length;
			if (index < shortNodes.length) {
				return shortNodes[index];
			}
			index -= shortNodes.length;
			return byteNodes[index];
		}

	}

	private final int blockSize;

	// bitSet holds the values for each person
	private BitSet bitSet;
	// the tree holds summation nodes in an array that is length two the
	// power
	private TreeHolder treeHolder;

	public AltTreeBitSet5_SplitArray(int capacity, int blockSize) {
		this.blockSize = blockSize;
		bitSet = new BitSet(capacity);
		treeHolder = new TreeHolder(capacity, blockSize, false);
	}

	/*
	 * Grows the tree to allow the given pid to exist, filling the ullage in the
	 * base layer of the tree as required
	 */
	private void grow(int pid) {
		int capacity = pid + 1;
		BitSet oldBitSet = bitSet;
		size = 0;
		treeHolder = new TreeHolder(capacity, blockSize, true);
		bitSet = new BitSet(capacity);
		for (int i = 0; i < treeHolder.maxPid; i++) {
			if (oldBitSet.get(i)) {
				add(i);
			}
		}
	}

//	private void grow_smart(int pid) {		
//		// determine the new size of the tree array
//		// int newTreeSize = getNearestPowerOfTwo(pid) >> (BLOCK_POWER - 2);
//
//		int numberOfBlocks = pid / blockSize + 1;
//		int treeTop = getNextPowerOfTwo(numberOfBlocks);
//		int newTreeSize = treeTop * 2;
//
//		/*
//		 * The tree array grows by powers of two. We determine how many power levels the
//		 * new tree array is over the existing one to help us transport values from the
//		 * old array into the new array rather than recalculate those values.
//		 * Essentially, the old tree will slide down the left hand side of the new tree,
//		 * while leaving a trail of the old tree's head value behind.
//		 * 
//		 */
//
//		// moving the old tree's values into the new tree
//		int power = getPower2(treeHolder.length());
//		int powerShift = getPower2(newTreeSize) - power;
//		// int[] newTree = new int[newTreeSize];
//		TreeHolder newTreeHolder = new TreeHolder(pid+1,blockSize,true);
//		int base = 1;
//		int newBase = base << powerShift;
//		for (int p = 0; p < power; p++) {
//			for (int i = 0; i < base; i++) {
//				// newTree[newBase + i] = tree[i + base];
//				int index = i + base;
//				if (index < treeHolder.length()) {
//					newTreeHolder.set(newBase + i, treeHolder.get(index));
//				}
//			}
//			base <<= 1;
//			newBase <<= 1;
//		}
//		/*
//		 * The old tree's root value now has to propagate up the new tree to its root
//		 */
//		base = 1 << powerShift;
//		while (base > 1) {
//			base >>= 1;
//			// newTree[base] = tree[1];
//			newTreeHolder.set(base, treeHolder.get(1));
//		}
//		// swap the tree
//		// tree = newTree;
//		treeHolder = newTreeHolder;
//	}

	private int size;

	@Override
	public boolean add(int value) {

		// do we need to grow?
		if (value >= treeHolder.maxPid) {
			grow(value);
		}
		// add the value
		if (!bitSet.get(value)) {
			bitSet.set(value);
			// select the block(index) that will receive the bit flip.
			int block = value / blockSize;
			block += treeHolder.blockStartingIndex;
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				treeHolder.increment(block);
				block >>= 1;
			}
			size++;
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
			block += treeHolder.blockStartingIndex;// (treeHolder.length() >> 1);
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				// tree[block] -= 1;
				treeHolder.decrement(block);
				block >>= 1;
			}
			size--;
			return true;
		}
		return false;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(int value) {
		return bitSet.get(value);
	}

	@Override
	public int getValue(int index) {
		if (index >= size || index < 0) {
			return -1;
		}

		/*
		 * We need to use an integer that is at least one, so we add one to the selected
		 * index. We will reduce this amount until it reaches zero.
		 */
		int targetCount = index + 1;

		/*
		 * Find the mid point of the tree. Think of the tree array as a triangle with a
		 * single root node at the top. This will be the first array element in the last
		 * row(last half) in the tree. This is the row that maps to the blocks in the
		 * bitset.
		 */
		int treeIndex = 1;

		/*
		 * Walk downward in the tree. If we move to the right, we have to reduce the
		 * target value.
		 */
		while (treeIndex < treeHolder.blockStartingIndex) {
			// move to the left child
			treeIndex = treeIndex << 1;
			// if the left child is less than the target count, then reduce the target count
			// by the number in the left child and move to the right child
			int nodeValue = treeHolder.get(treeIndex);
			if (nodeValue < targetCount) {
				targetCount -= nodeValue;
				treeIndex++;
			}

		}
		/*
		 * We have arrived at the element of the tree array that corresponds to the
		 * desired block in the bitset. We will need to determine the positions to scan
		 * in the bitset
		 */
		// int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
		int bitSetStartIndex = (treeIndex - treeHolder.blockStartingIndex) * blockSize;
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
