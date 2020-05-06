package gcm.util.stats;

import java.util.Optional;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * A {@link Stat} implementor that is immutable and is constructed via the
 * contained builder class.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class ImmutableStat implements Stat {

	/**
	 * A container for collecting the five characteristics of a Stat
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class Scaffold {
		private double mean;
		private double variance;
		private double max;
		private double min;
		private int size;
	}

	/**
	 * A builder class for {@link ImmutableStat}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	@NotThreadSafe
	public static class ImmutableStatBuilder {
		private Scaffold scaffold = new Scaffold();

		/**
		 * Builds the ImmutableStat
		 * 
		 * @throws RuntimeException
		 *             <li>if the size is negative
		 *             <li>if the size value is one and the min mean and max are
		 *             not equal
		 *             <li>if the size value is one and the variance is not zero
		 *             <li>if the size value is greater than one and the min
		 *             exceeds the max
		 *             <li>if the size value is greater than one and the min
		 *             exceeds the mean
		 *             <li>if the size value is greater than one and the mean
		 *             exceeds the max
		 *             <li>if the size value is greater than one and the
		 *             variance is negative
		 */
		public ImmutableStat build() {
			try {
				validateScaffold();
				return new ImmutableStat(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the mean
		 */
		public void setMean(double mean) {
			scaffold.mean = mean;
		}

		/**
		 * Sets the variance
		 */
		public void setVariance(double variance) {
			scaffold.variance = variance;
		}

		/**
		 * Sets the max
		 */
		public void setMax(double max) {
			scaffold.max = max;
		}

		/**
		 * Sets the min
		 */
		public void setMin(double min) {
			scaffold.min = min;
		}

		/**
		 * Sets the size
		 */
		public void setSize(int size) {
			scaffold.size = size;
		}

		/*
		 * Validates the content of the Stat
		 * 
		 * @throws RuntimeException
		 * 
		 * <li>if the size is negative
		 * 
		 * <li>if the size value is one and the min mean and max are not equal
		 * 
		 * <li>if the size value is one and the variance is not zero
		 * 
		 * <li>if the size value is greater than one and the min exceeds the max
		 * 
		 * <li>if the size value is greater than one and the min exceeds the
		 * mean
		 * 
		 * <li>if the size value is greater than one and the mean exceeds the
		 * max
		 * 
		 * <li>if the size value is greater than one and the variance is
		 * negative
		 */
		private void validateScaffold() {
			if (scaffold.size < 0) {
				throw new RuntimeException("negative size");
			}
			if (scaffold.size == 1) {
				// min, max and mean must be equal and variance must be zero
				if (scaffold.min != scaffold.max) {
					throw new RuntimeException("size = 1 implies min=max");
				}
				if (scaffold.min != scaffold.mean) {
					throw new RuntimeException("size = 1 implies min=mean=max");
				}
				if (scaffold.variance != 0) {
					throw new RuntimeException("size = 1 implies variance = 0");
				}
			} else if (scaffold.size > 1) {
				if (scaffold.min > scaffold.max) {
					throw new RuntimeException("min exceeds max");
				}
				if (scaffold.min > scaffold.mean) {
					throw new RuntimeException("min exceeds mean");
				}

				if (scaffold.mean > scaffold.max) {
					throw new RuntimeException("mean exceeds max");
				}

				if (scaffold.variance < 0) {
					throw new RuntimeException("variance cannot be negative");
				}
			}
		}
	}

	private ImmutableStat(Scaffold scaffold) {
		this.mean = scaffold.mean;
		this.min = scaffold.min;
		this.max = scaffold.max;
		this.variance = scaffold.variance;
		this.standardDeviation = Math.sqrt(scaffold.variance);
		this.size = scaffold.size;
	}

	private final double mean;
	private final double variance;
	private final double standardDeviation;
	private final double max;
	private final double min;
	private final int size;

	@Override
	public Optional<Double> getMean() {
		if (size > 0) {
			return Optional.of(mean);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getVariance() {
		if (size > 0) {
			return Optional.of(variance);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getStandardDeviation() {
		if (size > 0) {
			return Optional.of(standardDeviation);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMax() {
		if (size > 0) {
			return Optional.of(max);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMin() {
		if (size > 0) {
			return Optional.of(min);
		}
		return Optional.empty();
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImmutableStat [mean=");
		builder.append(mean);
		builder.append(", variance=");
		builder.append(variance);
		builder.append(", standardDeviation=");
		builder.append(standardDeviation);
		builder.append(", max=");
		builder.append(max);
		builder.append(", min=");
		builder.append(min);
		builder.append(", size=");
		builder.append(size);
		builder.append("]");
		return builder.toString();
	}

}
