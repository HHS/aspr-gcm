package gcm.simulation;

/**
 * Enumeration used by various filters to perform equality comparisons
 *
 * @author Shawn Hatch
 *
 */
public enum Equality {
	LESS_THAN {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue < 0;
		}
	},

	LESS_THAN_EQUAL {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue <= 0;
		}
	},

	EQUAL {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue == 0;
		}
	},

	NOT_EQUAL {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue != 0;
		}
	},

	GREATER_THAN_EQUAL {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue >= 0;
		}
	},

	GREATER_THAN {
		@Override
		public boolean isCompatibleComparisonValue(final int comparisonValue) {
			return comparisonValue > 0;
		}
	};

	/**
	 * Returns true if and only if the given comparison value is compatible with
	 * this Equality. Comparison values are usually obtained via the
	 * {@link Comparable} interface.
	 *
	 * @param comparisonValue
	 * @return
	 */
	public abstract boolean isCompatibleComparisonValue(int comparisonValue);
	
	public static Equality getNegation(Equality equality) {
		switch(equality) {
		case EQUAL:
			return NOT_EQUAL;
		case GREATER_THAN:
			return Equality.LESS_THAN_EQUAL;
		case GREATER_THAN_EQUAL:
			return LESS_THAN;
		case LESS_THAN:
			return GREATER_THAN_EQUAL;
		case LESS_THAN_EQUAL:
			return GREATER_THAN;
		case NOT_EQUAL:
			return Equality.EQUAL;
		default:
			throw new RuntimeException("unhandled case "+equality);
		}
	}

}
