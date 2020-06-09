package gcm.manual.demo.identifiers;

import gcm.manual.demo.datatypes.PopulationDescription;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.PropertyDefinition;

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
