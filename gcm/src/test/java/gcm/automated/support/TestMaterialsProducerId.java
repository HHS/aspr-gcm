package gcm.automated.support;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.MaterialsProducerId;

/**
 * Enumeration that identifies region components for all tests
 */
public enum TestMaterialsProducerId implements MaterialsProducerId {
	MATERIALS_PRODUCER_1, MATERIALS_PRODUCER_2, MATERIALS_PRODUCER_3;

	public static TestMaterialsProducerId getRandomMaterialsProducerId(final RandomGenerator randomGenerator) {
		return TestMaterialsProducerId.values()[randomGenerator.nextInt(TestMaterialsProducerId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestMaterialsProducerId next;

	public TestMaterialsProducerId next() {
		if (next == null) {
			next = TestMaterialsProducerId.values()[(ordinal() + 1) % TestMaterialsProducerId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link MaterialsProducerId} instance.
	 */
	public static MaterialsProducerId getUnknownMaterialsProducerId() {
		return new MaterialsProducerId() {
		};
	}
}
