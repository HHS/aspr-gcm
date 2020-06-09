package gcm.test.support;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.PersonPropertyId;

/**
 * Enumeration that identifies person property definitions
 */
public enum TestPersonPropertyId implements PersonPropertyId {
	PERSON_PROPERTY_1,
	PERSON_PROPERTY_2,
	PERSON_PROPERTY_3,
	PERSON_PROPERTY_4,
	PERSON_PROPERTY_5,
	PERSON_PROPERTY_6,
	PERSON_PROPERTY_7,
	PERSON_PROPERTY_8,
	PERSON_PROPERTY_9;
	public static TestPersonPropertyId getRandomPersonPropertyId(final RandomGenerator randomGenerator) {
		return TestPersonPropertyId.values()[randomGenerator.nextInt(TestPersonPropertyId.values().length)];
	}
	
	/**
	 * Returns a new {@link PersonPropertyId} instance.
	 */
	public static PersonPropertyId getUnknownPersonPropertyId() {
		return new PersonPropertyId() {
		};
	}
}