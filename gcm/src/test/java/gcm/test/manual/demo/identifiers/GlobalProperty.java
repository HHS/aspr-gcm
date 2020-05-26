package gcm.test.manual.demo.identifiers;

import gcm.scenario.GlobalPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.test.manual.demo.datatypes.PopulationDescription;

public enum GlobalProperty implements GlobalPropertyId {
	
	POPULATION_DESCRIPTION(PropertyDefinition.builder().setType(PopulationDescription.class).build()),
	ALPHA(PropertyDefinition.builder().setType(Double.class).setDefaultValue(0).setPropertyValueMutability(true).build());
	
		
	private final PropertyDefinition propertyDefinition;

	private GlobalProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
