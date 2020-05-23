package gcm.test.automated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import gcm.util.TimeElapser;
import gcm.util.annotations.UnitTest;
import gcm.util.dimensiontree.DimensionTree;
import gcm.util.dimensiontree.VolumetricDimensionTree;

/**
 * Test class for {@link VolumetricDimensionTree}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = VolumetricDimensionTree.class)

public class AT_VolumetricDimensionTree {

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

	/**
	 * Tests {@link VolumetricDimensionTree#getMembersInSphere(double, double[])
	 */
	@Test
	public void testGetMembersInSphere() {
		fail();
		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position, record);
		}

		double searchRadius = 10;

		for (int i = 0; i < n; i++) {
			double[] position = new double[2];
			position[0] = random.nextDouble() * 120 - 10;
			position[1] = random.nextDouble() * 120 - 10;

			List<Record> list = new ArrayList<>();

			for (Record record : records) {
				double deltaX = record.position[0] - position[0];
				double deltaY = record.position[1] - position[1];
				double distance = FastMath.sqrt(deltaX * deltaX + deltaY * deltaY);
				if (distance < searchRadius) {
					list.add(record);
				}
			}

			Set<Record> expectedRecords = new LinkedHashSet<>(list);

			list = tree.getMembersInSphere(searchRadius, position);

			Set<Record> actualRecords = new LinkedHashSet<>(list);

			assertEquals(expectedRecords, actualRecords);
		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#getNearestMember(double[])
	 */
	@Test
	public void testGetNearestMember() {
		fail();

		Random random = new Random(23412345234L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { -10, -10 })//
								.setUpperBounds(new double[] { -1, -1 })//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position, record);
		}

		for (int i = 0; i < n; i++) {
			double[] position = new double[2];
			position[0] = random.nextDouble() * 120 - 10;
			position[1] = random.nextDouble() * 120 - 10;

			Record expectedClosestRecord = null;
			double bestDistance = Double.POSITIVE_INFINITY;
			for (Record record : records) {
				double deltaX = record.position[0] - position[0];
				double deltaY = record.position[1] - position[1];
				double distance = FastMath.sqrt(deltaX * deltaX + deltaY * deltaY);
				if (expectedClosestRecord == null || distance < bestDistance) {
					expectedClosestRecord = record;
					bestDistance = distance;
				}
			}

			Optional<Record> actualClosestRecord = tree.getNearestMember(position);
			assertTrue(actualClosestRecord.isPresent());
			assertEquals(expectedClosestRecord, actualClosestRecord.get());

		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#getAll()
	 */
	@Test
	public void testGetAll() {
		fail();
		/*
		 * See test for add()
		 */
	}

	/**
	 * Tests {@link VolumetricDimensionTree#add(double[], double, Object)
	 */
	@Test
	public void testAdd() {
		fail();
		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 1, 1 })//
								.setLeafSize(15).build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
			tree.add(record.position, record);
		}

		Set<Record> expectedRecords = records.stream().collect(Collectors.toSet());
		Set<Record> actualRecords = tree.getAll().stream().collect(Collectors.toSet());

		assertEquals(expectedRecords, actualRecords);

		// We add the records again, this should result in each record being in
		// two places in the tree and the tree returning twice as many records.
		// Note that two records are equal if their id values match.
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			tree.add(record.position, record);
		}

		assertEquals(records.size() * 2, tree.getAll().size());

		expectedRecords = records.stream().collect(Collectors.toSet());
		actualRecords = tree.getAll().stream().collect(Collectors.toSet());

		assertEquals(expectedRecords, actualRecords);

	}

	/**
	 * Tests {@link VolumetricDimensionTree#contains(Object)
	 */
	@Test
	public void testContains() {
		fail();
		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 1, 1 })//
								.setLeafSize(15).build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
			if (i < n / 2) {
				tree.add(record.position, record);
			}
		}

		for (int i = 0; i < n; i++) {
			Record record = records.get(i);
			if (i < n / 2) {
				assertTrue(tree.contains(record));
			} else {
				assertFalse(tree.contains(record));
			}
		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#remove(Object) Tests
	 * {@link VolumetricDimensionTree#remove(double, Object)
	 */
	@Test
	public void testRemove() {
		fail();
		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.setFastRemovals(true)//
								.setLeafSize(50)//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
			if (i % 2 == 0) {
				tree.add(record.position, record);
				tree.add(record.position, record);
				tree.add(record.position, record);
				record.position[0] = random.nextDouble() * 100;
				record.position[1] = random.nextDouble() * 100;
				tree.add(record.position, record);
				tree.add(record.position, record);
			}
		}

		// System.out.println(tree);

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			boolean removed = tree.remove(record);
			assertEquals(i % 2 == 0, removed);
			// System.out.println(tree);
		}

	}

	// @Test
	public void testPerformance() {
		fail();
		Random random = new Random(3245233257245L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.setFastRemovals(true)//
								.setLeafSize(15)//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 600_000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, random.nextDouble() * 100, random.nextDouble() * 100);
			records.add(record);
		}
		TimeElapser timeElapser = new TimeElapser();

		for (Record record : records) {
			tree.add(record.position, record);
		}
		System.out.println("Add time = " + timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();
		for (Record record : records) {
			tree.remove(record);
		}
		System.out.println("Remove time = " + timeElapser.getElapsedMilliSeconds());
	}

}
