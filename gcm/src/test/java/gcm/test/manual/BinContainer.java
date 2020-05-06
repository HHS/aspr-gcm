package gcm.test.manual;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.math3.util.FastMath;

public class BinContainer {
	private final double binSize;
	public BinContainer(double binSize) {
		if(binSize<=0) {
			throw new RuntimeException("bin size must be positive");
		}
		this.binSize = binSize;
	}
	public static class Bin {
		private final double lowerBound;
		private final double upperBound;
		private int count;

		public int getCount() {
			return count;
		}

		public double getLowerBound() {
			return lowerBound;
		}

		public double getUpperBound() {
			return upperBound;
		}

		public Bin(double lowerBound, double upperBound) {
			super();
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Bin [lowerBound=");
			builder.append(lowerBound);
			builder.append(", upperBound=");
			builder.append(upperBound);
			builder.append(", count=");
			builder.append(count);
			builder.append("]");
			return builder.toString();
		}
	}
	
	private Map<Integer,Bin> map = new LinkedHashMap<>();
	private int lowIndex = Integer.MAX_VALUE;
	private int highIndex = Integer.MIN_VALUE;
	
	public void addValue(double value, int count) {
		if(count<0) {
			throw new RuntimeException("count cannot be negative");
		}
		int index = (int)FastMath.floor(value/binSize);
		Bin bin = map.get(index);
		if(bin==null) {
			lowIndex = FastMath.min(lowIndex, index);
			highIndex = FastMath.max(highIndex, index);
			bin = new Bin(index*binSize, (index+1)*binSize);
			map.put(index, bin);
		}
		bin.count+=count;
	}
	
	public int binCount() {
		return highIndex-lowIndex+1;
	}
	
	public Bin getBin(int index) {
		int adjustedIndex = lowIndex+index;
		Bin bin = map.get(adjustedIndex);
		if(bin == null) {
			bin = new Bin(adjustedIndex*binSize, (adjustedIndex+1)*binSize);
			map.put(adjustedIndex, bin);
		}
		return bin;
	}
	

}
