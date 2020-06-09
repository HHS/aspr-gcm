package gcm.test.manual.demo.identifiers;

import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.PropertyDefinition;

public enum CompartmentProperty implements CompartmentPropertyId {
	WEIGHT_THRESHOLD(Compartment.INFECTED, PropertyDefinition.builder().setType(Double.class).setDefaultValue(0d).build());

	private final Compartment compartment;
	private final PropertyDefinition propertyDefinition;

	private CompartmentProperty(Compartment compartment, PropertyDefinition propertyDefinition) {
		this.compartment = compartment;
		this.propertyDefinition = propertyDefinition;
	}

	public Compartment getCompartment() {
		return compartment;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}
}
