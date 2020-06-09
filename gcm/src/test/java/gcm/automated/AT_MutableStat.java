package gcm.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import gcm.util.annotations.UnitTest;
import gcm.util.stats.MutableStat;
import gcm.util.stats.Stat;

/**
 * Test class for {@link MutableStat}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableStat.class)
public class AT_MutableStat {

	private static final double TOLERANCE = 0.0001;

	private static void showSimilar(Stat stat1, Stat stat2) {
		assertEquals(stat1.size(), stat2.size());
		if (stat1.size() > 0) {
			showSimilarValues(stat1.getMin().get(), stat2.getMin().get());
			showSimilarValues(stat1.getMax().get(), stat2.getMax().get());
			showSimilarValues(stat1.getMean().get(), stat2.getMean().get());
			showSimilarValues(stat1.getStandardDeviation().get(), stat2.getStandardDeviation().get());
			showSimilarValues(stat1.getVariance().get(), stat2.getVariance().get());
		}
	}

	private static void showSimilarValues(double value1, double value2) {
		double mid = (value1 + value2) / 2;
		double portion = Math.abs((mid - value1) / mid);
		if (Double.isFinite(portion)) {
			assertTrue(portion < TOLERANCE);
		} else {
			double diff = Math.abs(value2 - value1);
			assertTrue(diff < TOLERANCE);
		}
	}

	@Test
	public void testCombineStats() {

		Random random = new Random(356787434527489L);

		for (int k = 0; k < 100; k++) {
			int n = random.nextInt(10);
			MutableStat stat1 = new MutableStat();
			List<Double> values1 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = random.nextDouble() * 100 + 50;
				values1.add(value);
				stat1.add(value);
			}

			// make sure that at least one of the stat objects is non-empty
			n = random.nextInt(10) + 1;
			MutableStat stat2 = new MutableStat();
			List<Double> values2 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = random.nextDouble() * 90 + 70;
				values2.add(value);
				stat2.add(value);
			}

			MutableStat expectedStat = new MutableStat();
			for (int i = 0; i < values1.size(); i++) {
				expectedStat.add(values1.get(i));
			}
			for (int i = 0; i < values2.size(); i++) {
				expectedStat.add(values2.get(i));
			}

			Stat combinedStat = MutableStat.combineStats(stat1, stat2);

			showSimilar(expectedStat, combinedStat);
		}
	}

}
