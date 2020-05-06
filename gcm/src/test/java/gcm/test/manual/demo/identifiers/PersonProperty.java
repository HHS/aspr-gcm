package gcm.test.manual.demo.identifiers;

import gcm.scenario.MapOption;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.TimeTrackingPolicy;
import gcm.test.manual.demo.datatypes.Sex;

public enum PersonProperty implements PersonPropertyId {

	HEIGHT(PropertyDefinition.builder().setType(Double.class).setDefaultValue(0.0).build()),
	AGE(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).setMapOption(MapOption.NONE).build()),
	WEIGHT(PropertyDefinition.builder().setType(Float.class).setDefaultValue(0.0F).build()),
	SEX(PropertyDefinition.builder().setType(Sex.class).setDefaultValue(Sex.FEMALE).build()),
	IMMUNE(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).setMapOption(MapOption.NONE).build()),
	SHADY(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).setMapOption(MapOption.NONE).build()),
	UNCTUOUS(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).build()),
	WEIRD(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build());
	private final PropertyDefinition propertyDefinition;

	private PersonProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}
}
