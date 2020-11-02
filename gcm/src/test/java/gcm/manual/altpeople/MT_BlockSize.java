package gcm.manual.altpeople;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.util.MemSizer;
import gcm.util.TimeElapser;

public class MT_BlockSize {

	@Test
	public void test() {
		StringBuilder sb = new StringBuilder();
		sb.append("blockSize");
		sb.append("\t");
		sb.append("addTime");
		sb.append("\t");
		sb.append("retrievalTime");
		sb.append("\t");
		sb.append("bitsPerPerson");

		System.out.println(sb);
		for (int i = 1; i < 512; i++) {			
			testBlockSize(i, 3_000_000);
		}
	}

	private void testBlockSize(int blockSize, int populationSize) {

		RandomGenerator randomGenerator = EnvironmentSupport.getRandomGenerator(235634545);

		// select about half of the values to be in the bit set
		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < populationSize; i++) {
			if (randomGenerator.nextDouble() < 0.5) {
				values.add(i);
			}
		}

		// place the people in the bit set
		AltPeopleContainer tbs = new AltTreeBitSet6_SplitArraySmooth(populationSize, blockSize);
		// int capacity = personIdManager.getPersonIdLimit();

		TimeElapser timeElapser = new TimeElapser();
		for (Integer i : values) {
			tbs.add(i);
		}
		double addTime = timeElapser.getElapsedMilliSeconds();

		// show that the number of people in the bit set is correct
		assertEquals(values.size(), tbs.size());

		timeElapser.reset();

		for (int i = 0; i < values.size(); i++) {
			assertEquals(values.get(i).intValue(), tbs.getValue(i));
			// tbs.getValue(i);
		}

		double retrievalTime = timeElapser.getElapsedMilliSeconds();

		MemSizer memSizer = new MemSizer(false);

		double bitsPerPerson = memSizer.getByteCount(tbs);
		bitsPerPerson *= 8;
		bitsPerPerson /= populationSize;

		StringBuilder sb = new StringBuilder();
		sb.append(blockSize);
		sb.append("\t");
		sb.append(addTime);
		sb.append("\t");
		sb.append(retrievalTime);
		sb.append("\t");
		sb.append(bitsPerPerson);

		System.out.println(sb);
	}

}
