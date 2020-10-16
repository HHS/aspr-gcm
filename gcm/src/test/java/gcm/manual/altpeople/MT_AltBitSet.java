package gcm.manual.altpeople;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.util.TimeElapser;

public class MT_AltBitSet {
	private final static int POPULATION_SIZE = 30_000_000;

	private static enum BitSetType {
		BASE, //
		TREE_HOLDER, //
		BLOCK_SIZED, //
		ULLAGE,//
		SPLIT_ARRAY;//
	}

	@Test
	public void test() {
		//testBitSetType(BitSetType.BASE, 64);
		
		for (BitSetType bitSetType : BitSetType.values()) {
			testBitSetType(bitSetType, 63);
		}
	}

	private void testBitSetType(BitSetType bitSetType, int blockSize) {

		RandomGenerator randomGenerator = EnvironmentSupport.getRandomGenerator(235634545);
		
		
		// select about half of the values to be in the bit set
		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			if (randomGenerator.nextDouble() < 0.5) {
				values.add(i);
			}
		}

		// place the people in the bit set
		AltPeopleContainer tbs;
		//int capacity = personIdManager.getPersonIdLimit();
		int capacity = POPULATION_SIZE;
		switch (bitSetType) {
		case BASE:
			tbs = new AltTreeBitSet1_Base(capacity);
			break;
		case TREE_HOLDER:
			tbs = new AltTreeBitSet2_TreeHolder(capacity);
			break;
		case BLOCK_SIZED:
			tbs = new AltTreeBitSet3_BlockSized(capacity, blockSize);
			break;
		case ULLAGE:
			tbs = new AltTreeBitSet4_Ullage(capacity, blockSize);
			break;
		case SPLIT_ARRAY:
			tbs = new AltTreeBitSet5_SplitArray(capacity, blockSize);
			break;
		default:
			throw new RuntimeException("unhandled bit set type");
		}

		for (Integer i : values) {			
			tbs.add(i);
		}
		

		// show that the number of people in the bit set is correct
		assertEquals(values.size(),tbs.size());

		TimeElapser timeElapser = new TimeElapser();

		for (int i = 0; i < values.size(); i++) {
			assertEquals(values.get(i).intValue(), tbs.getValue(i));
			//tbs.getValue(i);
		}

		System.out.println(bitSetType + " " + timeElapser.getElapsedMilliSeconds());

	}

}
