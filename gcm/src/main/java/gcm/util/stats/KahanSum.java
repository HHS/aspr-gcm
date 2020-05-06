package gcm.util.stats;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implements the error reducing methodology for sums of floating point numbers
 * developed by mathematician William Kahan.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class KahanSum {

	private double sum;

	private double error;

	/**
	 * Adds the value to the error corrected sum
	 */
	public void add(double value) {
		double errorCorrectedValue = value - error;
		double tempSum = sum + errorCorrectedValue;
		error = (tempSum - sum) - errorCorrectedValue;
		sum = tempSum;
	}

	/**
	 * Returns the error corrected sum
	 */
	public double getSum() {
		return sum;
	}

}
