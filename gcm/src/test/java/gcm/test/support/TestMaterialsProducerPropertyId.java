package gcm.test.support;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.MaterialsProducerPropertyId;

/**
 * Enumeration that identifies region components for all tests
 */
public enum TestMaterialsProducerPropertyId implements MaterialsProducerPropertyId{
	MATERIALS_PRODUCER_PROPERTY_1,
	MATERIALS_PRODUCER_PROPERTY_2,
	MATERIALS_PRODUCER_PROPERTY_3,
	MATERIALS_PRODUCER_PROPERTY_4;
	
	public static TestMaterialsProducerPropertyId getRandomMaterialsProducerId(final RandomGenerator randomGenerator) {
		return TestMaterialsProducerPropertyId.values()[randomGenerator.nextInt(TestMaterialsProducerPropertyId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestMaterialsProducerPropertyId next;

	public TestMaterialsProducerPropertyId next() {
		if (next == null) {
			next = TestMaterialsProducerPropertyId.values()[(ordinal() + 1) % TestMaterialsProducerPropertyId.values().length];
		}
		return next;
	}
	/**
	 * Returns a new {@link MaterialsProducerPropertyId} instance.
	 */
	public static MaterialsProducerPropertyId getUnknownMaterialsProducerPropertyId() {
		return new MaterialsProducerPropertyId() {
		};
	}
}
