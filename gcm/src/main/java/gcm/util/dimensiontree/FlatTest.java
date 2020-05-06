package gcm.util.dimensiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import gcm.util.TimeElapser;
import gcm.util.stats.MutableStat;

public class FlatTest {
	private static class Record {

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Record [id=");
			builder.append(id);
			builder.append(", position=");
			builder.append(Arrays.toString(position));
			builder.append("]");
			return builder.toString();
		}

		private final int id;
		private final double[] position = new double[2];

		public Record(int id, double x, double y) {
			this.id = id;
			position[0] = x;
			position[1] = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Record)) {
				return false;
			}
			Record other = (Record) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

	}

	public static void main(String[] args) {

		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.setFastRemovals(true)//
								.setLeafSize(15)//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1_000_000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
		}
		TimeElapser timeElapser = new TimeElapser();

		for (Record record : records) {
			tree.add(record.position, record);
		}

		MutableStat mutableStat = new MutableStat();
		for (int i = 0; i < 1000; i++) {
			
			timeElapser.reset();
			tree.getMembersInSphere(10, new double[] { 50, 50 });
			mutableStat.add(timeElapser.getElapsedMilliSeconds());
		}
		
		System.out.println(mutableStat);


	}
}
