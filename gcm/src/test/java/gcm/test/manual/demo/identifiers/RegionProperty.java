package gcm.test.manual.demo.identifiers;

import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionPropertyId;

public enum RegionProperty implements RegionPropertyId {

	LAT(PropertyDefinition.builder().setType(Double.class).build()),

	LON(PropertyDefinition.builder().setType(Double.class).build()),
	
	FLAG(PropertyDefinition.builder()
			.setType(Boolean.class)
			.setDefaultValue(false)
			.build());
	
	

	private final PropertyDefinition propertyDefinition;

	private RegionProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
