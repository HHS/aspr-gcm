package gcm.test.support;

import gcm.scenario.GlobalPropertyId;

/**
 * Enumeration that identifies global property definitions
 */
public enum TestGlobalPropertyId implements GlobalPropertyId {
	Global_Property_1, Global_Property_2, Global_Property_3, Global_Property_4, Global_Property_5, Global_Property_6, Global_Property_7;
	/**
	 * Returns a new {@link GlobalPropertyId} instance.
	 */
	public static GlobalPropertyId getUnknownGlobalPropertyId() {
		return new GlobalPropertyId() {
		};
	}
	
	public final static GlobalPropertyId TASK_PLAN_CONTAINER_PROPERTY_ID = new GlobalPropertyId() {
	};
	
	public final static GlobalPropertyId OBSERVATION_CONTAINER_PROPERTY_ID = new GlobalPropertyId() {
	};
	
		
}
