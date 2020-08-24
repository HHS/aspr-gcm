package gcm.util.containers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.PersonId;
import gcm.simulation.Context;
import gcm.simulation.PersonIdManager;

/*
	 * PeopleContainer implementor that uses a BitSet to record a boolean value
	 * of true for each person contained. Since the BitSet will tend to be the
	 * size of the full population of the simulation at approximately three
	 * bytes per person, this container is preferred to a Set-based implementor
	 * when the number of people in the set exceeds 0.67% of the total
	 * population.
	 */

	public class TreeBitSetPeopleContainer implements PeopleContainer {
		// MAX_POWER is the highest power of two in a positive integer
		private final int MAX_POWER = 30;
		// BLOCK_POWER is the power of two that is the block length -- i.e. 64
		// bits
		private final int BLOCK_POWER = 6;
		// BLOCK_LENGTH is the 64 bit length of a block
		private final int BLOCK_LENGTH = 1 << BLOCK_POWER;
		// power is the power of two that sets the length of the tree array
		private int power = 1;
		private final PersonIdManager personIdManager;
		// bitSet holds the values for each person
		private BitSet bitSet;
		// the tree holds summation nodes in an array that is length two the
		// power
		private int[] tree = new int[1 << power];
		// maxPid is the maximum(exclusive) person id value(int) that can be
		// contained at the current power.
		int maxPid = 1 << (power + BLOCK_POWER - 1);

		public TreeBitSetPeopleContainer(Context context, PeopleContainer peopleContainer) {
			personIdManager = context.getPersonIdManager();
			// initialize the size of the bitSet to that of the full population,
			// including removed people
			int capacity = personIdManager.getPersonIdLimit();
			bitSet = new BitSet(capacity);

			// take the people from the people container that is being replaced
			for (PersonId personId : peopleContainer.getPeople()) {
				add(personId);
			}
		}

		@Override
		public List<PersonId> getPeople() {
			List<PersonId> result = new ArrayList<>(size());
			int n = bitSet.size();
			for (int i = 0; i < n; i++) {
				if (bitSet.get(i)) {
					result.add(personIdManager.getBoxedPersonId(i));
				}
			}
			return result;
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
		 * Returns the nearest(<=) power of two. For example,
		 * getNearestPowerOfTwo(37) = 5. Requires a positive input.
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
		public boolean add(PersonId personId) {
			int pid = personId.getValue();
			// do we need to grow?
			if (pid >= maxPid) {
				// determine the new size of the tree array
				int newTreeSize = getNearestPowerOfTwo(pid) >> (BLOCK_POWER - 2);

				/*
				 * The tree array grows by powers of two. We determine how many
				 * power levels the new tree array is over the existing one to
				 * help us transport values from the old array into the new
				 * array rather than recalculate those values. Essentially, the
				 * old tree will slide down the left hand side of the new tree,
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
				 * The old tree's root value now has to propagate up the new
				 * tree to its root
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
			if (!bitSet.get(pid)) {
				bitSet.set(pid);
				// select the block(index) that will receive the bit flip.
				int block = pid >> BLOCK_POWER;
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
		public boolean remove(PersonId personId) {
			/*
			 * If the person is not contained, then don't try to remove them.
			 * This protects us from removals that are >= maxPid.
			 */
			if (!contains(personId)) {
				return false;
			}
			int pid = personId.getValue();
			if (bitSet.get(pid)) {
				bitSet.set(pid, false);
				// select the block(index) that will receive the bit flip.
				int block = pid >> BLOCK_POWER;
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
		public void addAll(Collection<PersonId> collection) {
			for (PersonId personId : collection) {
				add(personId);
			}
		}

		@Override
		public boolean contains(PersonId personId) {
			return bitSet.get(personId.getValue());
		}

		@Override
		public PersonId getRandomPersonId(RandomGenerator randomGenerator) {

			/*
			 * We need to use an integer that is at least one, so we add one to
			 * the randomly selected index. We will reduce this amount until it
			 * reaches zero.
			 */
			int targetCount = randomGenerator.nextInt(size()) + 1;

			/*
			 * Find the mid point of the tree. Think of the tree array as a
			 * triangle with a single root node at the top. This will be the
			 * first array element in the last row(last half) in the tree. This
			 * is the row that maps to the blocks in the bitset.
			 */
			int midTreeIndex = tree.length >> 1;
			int treeIndex = 1;

			/*
			 * Walk downward in the tree. If we move to the right, we have to
			 * reduce the target value.
			 */
			while (treeIndex < midTreeIndex) {
				treeIndex = treeIndex << 1;
				if (tree[treeIndex] < targetCount) {
					targetCount -= tree[treeIndex];
					treeIndex++;
				}
			}
			/*
			 * We have arrived element of the tree array that corresponds to the
			 * desired block in the bitset. We will need to determine the
			 * positions to scan in the bitset
			 */
			int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
			int bitSetStopIndex = bitSetStartIndex + BLOCK_LENGTH;
			/*
			 * Finally, we scan the bits and reduce the target count until it
			 * reaches zero.
			 */
			for (int i = bitSetStartIndex; i < bitSetStopIndex; i++) {
				if (bitSet.get(i)) {
					targetCount--;
					if (targetCount == 0) {
						return personIdManager.getBoxedPersonId(i);
					}
				}
			}
			return null;
		}
	}
