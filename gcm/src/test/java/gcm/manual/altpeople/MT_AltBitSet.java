package gcm.manual.altpeople;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.scenario.PersonId;
import gcm.util.TimeElapser;

public class MT_AltBitSet {
	private final static int POPULATION_SIZE = 10;

	private static enum BitSetType {
		BASE, //
		TREE_HOLDER, //
		BLOCK_SIZED, //
		ULLAGE;//
	}

	@Test
	public void test() {
		testBitSetType(BitSetType.ULLAGE, 5);
		
//		for (BitSetType bitSetType : BitSetType.values()) {
//			testBitSetType(bitSetType, 63);
//		}
	}

	private void testBitSetType(BitSetType bitSetType, int blockSize) {

		RandomGenerator randomGenerator = EnvironmentSupport.getRandomGenerator(235634545);

		// set the population size

		// create a person id manager
		AltPersonIdManager personIdManager = new AltPersonIdManager();

		// fill the person id manager with the population
		for (int i = 0; i < POPULATION_SIZE; i++) {
			personIdManager.addPersonId();
		}

		// get a list of all person id values
		List<PersonId> people = personIdManager.getPeople();

		// select about half of the people to be in the bit set
		List<PersonId> peopleInBitSet = new ArrayList<>();
		for (int i = 0; i < POPULATION_SIZE; i++) {
			if (randomGenerator.nextDouble() < 0.5) {
				peopleInBitSet.add(people.get(i));
			}
		}

		// place the people in the bit set
		AltPeopleContainer tbs;
		switch (bitSetType) {
		case BASE:
			tbs = new AltTreeBitSet1_Base(personIdManager);
			break;
		case TREE_HOLDER:
			tbs = new AltTreeBitSet2_TreeHolder(personIdManager);
			break;
		case BLOCK_SIZED:
			tbs = new AltTreeBitSet3_BlockSized(personIdManager, blockSize);
			break;
		case ULLAGE:
			tbs = new AltTreeBitSet4_Ullage(personIdManager, blockSize);
			break;
		default:
			throw new RuntimeException("unhandled bit set type");
		}

		for (PersonId personId : peopleInBitSet) {
			tbs.add(personId);
		}
		

		// show that the number of people in the bit set is correct
		assertTrue(tbs.size() == peopleInBitSet.size());

		TimeElapser timeElapser = new TimeElapser();

		for (int i = 0; i < peopleInBitSet.size(); i++) {
			assertEquals(peopleInBitSet.get(i), tbs.getPersonId(i));
			// tbs.getPersonId(i);
		}

		System.out.println(bitSetType + " " + timeElapser.getElapsedMilliSeconds());

	}

}
