package gcm.test.manual.demo.identifiers;

import java.nio.file.Path;

import gcm.scenario.GlobalPropertyId;
import gcm.scenario.PropertyDefinition;

public enum GlobalProperty implements GlobalPropertyId {
	POPULATION_PATH(PropertyDefinition.builder().setType(Path.class).build()),
	ALPHA(PropertyDefinition.builder().setType(Double.class).setDefaultValue(0).setPropertyValueMutability(true).build());
	
		
	private final PropertyDefinition propertyDefinition;

	private GlobalProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
