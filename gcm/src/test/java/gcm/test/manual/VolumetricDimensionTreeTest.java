package gcm.test.manual;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import gcm.util.dimensiontree.VolumetricDimensionTree;
import gcm.util.vector.Vector2D;

public class VolumetricDimensionTreeTest {
	private static int masterId;

	private static class Record {
		private final int id = masterId++;
		private final double[] position;
		private final double radius;

		public Record(double x, double y, double radius) {

			position = new double[] { x, y };
			this.radius = radius;
		}

		

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Record [id=");
			builder.append(id);
			builder.append(", position=");
			builder.append(Arrays.toString(position));
			builder.append(", radius=");
			builder.append(radius);
			builder.append("]");
			return builder.toString();
		}

	}

	private VolumetricDimensionTreeTest() {
	}

	private void execute() {
		Random random = new Random();
		int n = 1;
		List<Record> records = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Record record = new Record(random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble()*3 + 0.01);
			records.add(record);
		}

		double[] lowerBounds = { 0, 0 };
		double[] upperBounds = { 100, 100 };

		VolumetricDimensionTree<Record> volumetricDimensionTree = VolumetricDimensionTree	.builder(Record.class)//
																							.setLowerBounds(lowerBounds)//
																							.setUpperBounds(upperBounds)//
																							.build();

		for (Record record : records) {
			volumetricDimensionTree.add(record.position, record.radius, record);
		}

		double[] searchPosition = new double[] { 45, 70 };
		double searchRadius = 15;

		Set<Record> expectedRecords = new LinkedHashSet<>();
		Vector2D v = new Vector2D(searchPosition[0], searchPosition[1]);
		for (Record record : records) {
			Vector2D u = new Vector2D(record.position[0], record.position[1]);
			if (v.distanceTo(u) <= (searchRadius+record.radius)) {
				expectedRecords.add(record);
			}
		}

		List<Record> foundMembers = volumetricDimensionTree.getMembersInSphere(searchRadius, searchPosition);
		foundMembers.sort(Comparator.comparing(Record::getId));
		Set<Record> actualRecords = new LinkedHashSet<>(foundMembers);

//		System.out.println("expected");
//		expectedRecords.forEach(rec -> System.out.println(rec));
//		System.out.println();
//		System.out.println("actual");
//		actualRecords.forEach(rec -> System.out.println(rec));

		assertEquals(expectedRecords, actualRecords);
		
		for (Record record : records) {
			assertTrue(volumetricDimensionTree.remove(record.radius, record));
			assertFalse(volumetricDimensionTree.remove(record.radius, record));			
		}
		
		System.out.println("VolumetricDimensionTreeTest.execute() done");

	}

	public static void main(String[] args) {
		new VolumetricDimensionTreeTest().execute();
	}
}
