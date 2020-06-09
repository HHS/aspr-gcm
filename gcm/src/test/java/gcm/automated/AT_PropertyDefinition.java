package gcm.automated;

import static gcm.automated.support.ExceptionAssertion.assertException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.junit.Test;

import gcm.automated.support.BooleanType;
import gcm.automated.support.SeedProvider;
import gcm.scenario.MapOption;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.TimeTrackingPolicy;
import gcm.scenario.PropertyDefinition.Builder;
import gcm.util.annotations.UnitTest;

/**
 * Unit test for {@link PropertyDefinition}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PropertyDefinition.class)
public class AT_PropertyDefinition {

	private static final SeedProvider SEED_PROVIDER = new SeedProvider(3457645345234539076L);

	private static void refreshRandomGenerator(int seedCase) {
		randomGenerator = new Well44497b(SEED_PROVIDER.getSeedValue(seedCase));
	}

	private static RandomGenerator randomGenerator;

	private static final int TEST_COUNT = 1000;

	/*
	 * Generates a random property definition from the given Random instance
	 */
	private PropertyDefinition generateRandomPropertyDefinition() {
		Class<?> type;
		final int typeCase = randomGenerator.nextInt(5);
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = randomGenerator.nextBoolean();
			break;
		case 1:
			type = Integer.class;
			defaultValue = randomGenerator.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + randomGenerator.nextInt();
			break;
		case 3:
			type = Double.class;
			defaultValue = randomGenerator.nextDouble();
			break;
		default:
			type = Long.class;
			defaultValue = randomGenerator.nextLong();
			break;
		}

		boolean propertyValuesAreMutability = randomGenerator.nextBoolean();
		MapOption mapOption = MapOption.values()[randomGenerator.nextInt(MapOption.values().length)];
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[randomGenerator.nextInt(TimeTrackingPolicy.values().length)];

		return PropertyDefinition	.builder()//
									.setType(type)//
									.setDefaultValue(defaultValue)//
									.setPropertyValueMutability(propertyValuesAreMutability)//
									.setMapOption(mapOption)//
									.setTimeTrackingPolicy(timeTrackingPolicy)//
									.build();//

	}

	/*
	 * Generates a random property definition from the given Random instance
	 * that has at least one field value that does not match the given property
	 * definition.
	 */
	private PropertyDefinition generateNonMatchingRandomPropertyDefinition(PropertyDefinition propertyDefinition) {
		while (true) {
			PropertyDefinition result = generateRandomPropertyDefinition();
			boolean different = result.getPropertyValueAreMutability() != propertyDefinition.getPropertyValueAreMutability();

			different |= propertyDefinition.getDefaultValue().isPresent() != result.getDefaultValue().isPresent();
			if (propertyDefinition.getDefaultValue().isPresent() && result.getDefaultValue().isPresent()) {
				different |= !result.getDefaultValue().get().equals(propertyDefinition.getDefaultValue().get());
			}

			different |= !result.getMapOption().equals(propertyDefinition.getMapOption());
			different |= !result.getTimeTrackingPolicy().equals(propertyDefinition.getTimeTrackingPolicy());
			different |= !result.getType().equals(propertyDefinition.getType());
			if (different) {
				return result;
			}
		}
	}

	/*
	 * Generates a matching property definition created from the given property
	 * definition's field values
	 */
	private PropertyDefinition generateMatchingPropertyDefinition(PropertyDefinition propertyDefinition) {
		Builder builder = PropertyDefinition.builder();//
		if (propertyDefinition.getDefaultValue().isPresent()) {
			builder.setDefaultValue(propertyDefinition.getDefaultValue().get());//
		}

		return builder	.setType(propertyDefinition.getType())//
						.setPropertyValueMutability(propertyDefinition.getPropertyValueAreMutability())//
						.setMapOption(propertyDefinition.getMapOption())//
						.setTimeTrackingPolicy(propertyDefinition.getTimeTrackingPolicy())//
						.build();//
	}

	/**
	 * Tests {@link PropertyDefinition#equals(Object)} and
	 * {@link PropertyDefinition#hashCode()}. These are boiler plate
	 * implementations that do not specify behavior beyond the standard equals
	 * contract.
	 */
	@Test
	public void testEqualsContact() {
		refreshRandomGenerator(0);

		/*
		 * Show that two Property Definitions are equal if and only if their
		 * fields are equal
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def1 = generateRandomPropertyDefinition();
			PropertyDefinition def2 = generateMatchingPropertyDefinition(def1);
			assertEquals(def1, def2);
			PropertyDefinition def3 = generateNonMatchingRandomPropertyDefinition(def1);
			assertNotEquals(def1, def3);
		}

		/*
		 * Show that a property definition is not equal to null
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition();
			assertFalse(def.equals(null));
		}

		/*
		 * Show that a property definition is equal to itself
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition();
			assertTrue(def.equals(def));
		}

		/*
		 * Show that a property definition is not equal to an instance of
		 * another class
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition();
			assertFalse(def.equals(new Object()));
		}

		/*
		 * Show that equal objects have equal hash codes
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def1 = generateRandomPropertyDefinition();
			PropertyDefinition def2 = generateMatchingPropertyDefinition(def1);
			assertEquals(def1.hashCode(), def2.hashCode());
		}

	}

	/**
	 * Tests {@link PropertyDefinition#toString()}
	 */
	@Test
	public void testToString() {
		refreshRandomGenerator(1);

		/*
		 * Show that the toString returns a non-empty string. This is an
		 * otherwise boiler-plate implementation.
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition();
			String toString = propertyDefinition.toString();
			assertNotNull(toString);
			assertTrue(toString.length() > 0);
		}
	}

	/**
	 * test for {@link PropertyDefinition#getDefaultValue()}
	 */
	@Test
	public void testGetDefaultValue() {
		refreshRandomGenerator(2);

		// Show that a property definition that has a null default value (value
		// is set to null or was not set at all)
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.build();//
		assertFalse(propertyDefinition.getDefaultValue().isPresent());

		/*
		 * Show that the default value used to form the property definition is
		 * returned by the property definition
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			Boolean defaultValue = randomGenerator.nextBoolean();
			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Boolean.class)//
													.setDefaultValue(defaultValue)//
													.build();//
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());

		}
		for (int i = 0; i < TEST_COUNT; i++) {
			String defaultValue = "String " + randomGenerator.nextInt();
			propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Integer defaultValue = randomGenerator.nextInt();
			propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Double defaultValue = randomGenerator.nextDouble();
			propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Long defaultValue = randomGenerator.nextLong();
			propertyDefinition = PropertyDefinition.builder().setType(Long.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}

	}

	/**
	 * test for {@link PropertyDefinition#getMapOption()}
	 */
	@Test
	public void testGetMapOption() {
		refreshRandomGenerator(3);

		/*
		 * Show that the map option value used to form the property definition
		 * is returned by the property definition
		 */
		for (MapOption mapOption : MapOption.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(12)//
																		.setMapOption(mapOption)//
																		.build();//
			assertEquals(mapOption, propertyDefinition.getMapOption());
		}

	}

	/**
	 * test for {@link PropertyDefinition#getTimeTrackingPolicy()}
	 */
	@Test
	public void testGetTimeTrackingPolicy() {
		refreshRandomGenerator(4);

		/*
		 * Show that the TimeTrackingPolicy value used to form the property
		 * definition is returned by the property definition
		 */
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue("defaultValue")//
																		.setTimeTrackingPolicy(timeTrackingPolicy)//
																		.build();//
			assertEquals(timeTrackingPolicy, propertyDefinition.getTimeTrackingPolicy());
		}

	}

	/**
	 * test for {@link PropertyDefinition#getType()}
	 */
	@Test
	public void testGetType() {
		refreshRandomGenerator(5);

		/*
		 * Show that the class type value used to form the property definition
		 * is returned by the property definition
		 */

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("default value")//
																	.build();//
		assertEquals(String.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(5.6)//
												.build();//
		assertEquals(Double.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.build();//
		assertEquals(Boolean.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Long.class)//
												.setDefaultValue(3453453L)//
												.build();//
		assertEquals(Long.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(2234)//
												.build();//
		assertEquals(Integer.class, propertyDefinition.getType());

	}

	/**
	 * test for {@link PropertyDefinition#getPropertyValueAreMutability()}
	 */
	@Test
	public void testPropertyValuesAreMutable() {
		refreshRandomGenerator(6);

		/*
		 * Show that the hasConstantPropertyValues value used to form the
		 * property definition is returned by the property definition
		 */

		for (BooleanType booleanType : BooleanType.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue("default value")//
																		.setPropertyValueMutability(booleanType.value())//
																		.build();//
			assertEquals(booleanType.value(), propertyDefinition.getPropertyValueAreMutability());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Double.class)//
													.setDefaultValue(5.6)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.getPropertyValueAreMutability());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Boolean.class)//
													.setDefaultValue(false)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.getPropertyValueAreMutability());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Long.class)//
													.setDefaultValue(3453453L)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//

			assertEquals(booleanType.value(), propertyDefinition.getPropertyValueAreMutability());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Integer.class)//
													.setDefaultValue(2345)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.getPropertyValueAreMutability());
		}

	}

	/**
	 * test for {@link PropertyDefinition} constructors
	 */
	@Test
	public void testBuilderDefaults() {
		refreshRandomGenerator(7);

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//

		assertEquals(MapOption.NONE, propertyDefinition.getMapOption());
		assertEquals(TimeTrackingPolicy.DO_NOT_TRACK_TIME, propertyDefinition.getTimeTrackingPolicy());
		assertEquals(true, propertyDefinition.getPropertyValueAreMutability());

		assertException(() -> {
			PropertyDefinition	.builder()//
								.setDefaultValue(17)//
								.build();//
		}, RuntimeException.class);

	}

	@Test
	public void testEquals() {

		PropertyDefinition propertyDefinition1 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition2 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition3 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("xxx")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition4 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(false)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition5 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.HASH)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition6 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition7 = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(45)//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		assertEquals(propertyDefinition1, propertyDefinition1);
		assertEquals(propertyDefinition1, propertyDefinition2);
		assertEquals(propertyDefinition2, propertyDefinition1);

		assertNotEquals(propertyDefinition1, propertyDefinition3);
		assertNotEquals(propertyDefinition1, propertyDefinition4);
		assertNotEquals(propertyDefinition1, propertyDefinition5);
		assertNotEquals(propertyDefinition1, propertyDefinition6);
		assertNotEquals(propertyDefinition1, propertyDefinition7);

	}

	@Test
	public void testHashCode() {
		PropertyDefinition propertyDefinition1 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition2 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setMapOption(MapOption.ARRAY)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		assertEquals(propertyDefinition1.hashCode(), propertyDefinition2.hashCode());
	}

}
