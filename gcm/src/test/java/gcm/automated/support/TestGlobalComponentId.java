package gcm.automated.support;

import gcm.scenario.CompartmentId;
import gcm.scenario.GlobalComponentId;

/**
 * Enumeration that identifies global components for all tests
 */
public enum TestGlobalComponentId implements GlobalComponentId {
	GLOBAL_COMPONENT_1,
	GLOBAL_COMPONENT_2,
	GLOBAL_COMPONENT_3,
	GLOBAL_COMPONENT_4;
	
	/**
	 * Returns a new {@link CompartmentId} instance.
	 */
	public static GlobalComponentId getUnknownGlobalComponentId() {
		return new GlobalComponentId() {
		};
	}

}