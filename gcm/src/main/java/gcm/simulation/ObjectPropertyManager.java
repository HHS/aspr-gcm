package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.ObjectValueContainer;

/**
 * Implementor of PropertyManager that stores Object property values in an
 * Object array based data structure.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public final class ObjectPropertyManager extends AbstractPropertyManager {

	/*
	 * A container, indexed by person id, that stores Objects as an array.
	 */
	private ObjectValueContainer objectValueContainer;

	public ObjectPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId propertyId) {
		super(context, propertyDefinition, propertyId);	
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}

		
		Object defaultValue = propertyDefinition.getDefaultValue().get();		
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		objectValueContainer = new ObjectValueContainer(defaultValue, suggestedPopulationSize);
	}

	@Override
	public <T> T getPropertyValue(PersonId personId) {		
		return objectValueContainer.getValue(personId.getValue());
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		super.setPropertyValue(personId, personPropertyValue);
		objectValueContainer.setValue(personId.getValue(), personPropertyValue);
	}

}
