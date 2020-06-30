package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import gcm.scenario.IntId;
import gcm.scenario.PersonId;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;
import gcm.util.containers.ArrayIntSet;
import gcm.util.containers.IntSet;

/**
 * Test class for {@link ArrayIntSet}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ArrayIntSet.class)
public class AT_ArrayIntSet {

	private static Set<PersonId> getPersonIds(Integer... values) {
		Set<PersonId> result = new LinkedHashSet<>();
		for (Integer value : values) {
			result.add(new PersonId(value));
		}
		return result;
	}

	/**
	 * Test for {@link ArrayIntSet#add(T t)}
	 */
	@Test
	@UnitTestMethod(name = "add", args= {IntId.class})
	public void testAdd() {

		Set<PersonId> personIds = getPersonIds(45, 18, 23, 66);

		/*
		 * Test an IntSet that tolerates duplicates
		 */
		IntSet<PersonId> intSet = new ArrayIntSet<>(100, true);
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		intSet.add(new PersonId(45));
		assertEquals(5, intSet.size());
		assertEquals(personIds, new LinkedHashSet<>(intSet.getValues()));

		/*
		 * Test an IntSet that does not tolerate duplicates
		 */
		intSet = new ArrayIntSet<>(100, false);
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		intSet.add(new PersonId(45));
		assertEquals(4, intSet.size());
		assertEquals(personIds, new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#remove(T t)}
	 */
	@Test
	@UnitTestMethod(name = "remove", args= {IntId.class})
	public void testRemove() {
		IntSet<PersonId> intSet = new ArrayIntSet<>();
		intSet.add(new PersonId(300));
		intSet.add(new PersonId(-67));
		intSet.add(new PersonId(-4));
		intSet.add(new PersonId(687));
		intSet.add(new PersonId(213));
		assertEquals(getPersonIds(300, -67, -4, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		// nothing should change since 100 is not contained
		intSet.remove(new PersonId(100));
		assertEquals(getPersonIds(300, -67, -4, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new PersonId(-4));
		assertEquals(getPersonIds(300, -67, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new PersonId(213));
		assertEquals(getPersonIds(300, -67, 687), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new PersonId(300));
		assertEquals(getPersonIds(-67, 687), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new PersonId(687));
		assertEquals(getPersonIds(-67), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new PersonId(-67));
		assertEquals(getPersonIds(), new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#getValues()}
	 */
	@Test
	@UnitTestMethod(name = "getValues", args= {})
	public void testGetValues() {

		// Select 500 random values from 0..999
		Random random = new Random(3453763452345345L);
		List<PersonId> personIds = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			personIds.add(new PersonId(i));
		}
		Collections.shuffle(personIds, random);
		List<PersonId> selectedPersonIds = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			selectedPersonIds.add(personIds.get(i));
		}

		// Add the selected values to an IntSet
		IntSet<PersonId> intSet = new ArrayIntSet<>();
		for (PersonId selectedValue : selectedPersonIds) {

			intSet.add(selectedValue);
		}

		assertEquals(new LinkedHashSet<>(selectedPersonIds), new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args= {})
	public void testSize() {

		Random random = new Random(3453763452345345L);
		List<PersonId> personIds = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			personIds.add(new PersonId(i));
		}
		Collections.shuffle(personIds, random);

		// first test that when there are no duplicates that the size of the
		// IntSet matches the number of values.
		IntSet<PersonId> intSet = new ArrayIntSet<>();
		for (PersonId value : personIds) {
			intSet.add(value);
		}
		assertEquals(personIds.size(), intSet.size());

		// now test that when there are duplicates that the size of the
		// IntSet matches the number of entries
		intSet = new ArrayIntSet<>();
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		assertEquals(personIds.size() * 2, intSet.size());

		// finally test that when there are duplicates, but the IntSet does not
		// tolerate duplicates, that the size of the
		// IntSet matches the number of values
		intSet = new ArrayIntSet<>(false);
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		assertEquals(personIds.size(), intSet.size());

	}

	/**
	 * Test for {@link ArrayIntSet constructors}
	 */
	@Test
	public void testConstructors() {
		IntSet<PersonId> intSet = new ArrayIntSet<>();
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(false);
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(100);
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(100, true);
		assertEquals(0, intSet.size());

		/*
		 * Test pre-conditions
		 */
		assertException(() -> new ArrayIntSet<>(0), Exception.class);
		assertException(() -> new ArrayIntSet<>(0, true), Exception.class);
	}

	/**
	 * Test for {@link ArrayIntSet#contains(T t)}
	 */
	@Test
	@UnitTestMethod(name = "contains", args= {IntId.class})
	public void testContains() {

		Set<PersonId> personIds = getPersonIds(1, 4, 5, 7, 12, 14, 16, 17, 22, 23, 28);

		IntSet<PersonId> intSet = new ArrayIntSet<>(100, true);
		for (PersonId personId : personIds) {
			intSet.add(personId);
		}
		for (int i = 0; i < 30; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(intSet.contains(personId), personIds.contains(personId));
		}

	}

	/**
	 * Test for {@link ArrayIntSet#toString()}
	 */	
	@Test
	@UnitTestMethod(name = "toString", args= {})
	public void testToString() {

		IntSet<IntId> intSet = new ArrayIntSet<>();

		Set<Integer> expectedContents = new LinkedHashSet<>();
		expectedContents.add(45);
		expectedContents.add(37);
		expectedContents.add(66);
		expectedContents.add(99);
		expectedContents.add(32);
		expectedContents.add(14);

		for (Integer value : expectedContents) {
			intSet.add(new IntId(value));
		}
		String stringRepresentaion = intSet.toString();
		String commaSeparatedNumbers = stringRepresentaion.substring(7, stringRepresentaion.length() - 1);
		String[] strings = commaSeparatedNumbers.split(",");
		Set<Integer> actualContents = new LinkedHashSet<>();
		for (String string : strings) {
			int value = Integer.parseInt(string.trim());
			actualContents.add(value);
		}
		assertEquals(expectedContents, actualContents);

	}

}
