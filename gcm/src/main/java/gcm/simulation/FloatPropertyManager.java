package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.FloatValueContainer;

/**
 * Implementor of PropertyManager that compresses Float property values into a
 * float[]-based data structure.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public final class FloatPropertyManager extends AbstractPropertyManager {

	

	/*
	 * A container, indexed by person id, that stores Double values as an array
	 * of float.
	 */
	private FloatValueContainer floatValueContainer;
	
	/**
	 * Constructs this FloatPropertyManager.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public FloatPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId propertyId) {
		super(context, propertyDefinition, propertyId);		
		if (propertyDefinition.getType() != Float.class) {
			throw new RuntimeException("Requires a property definition with float type");
		}
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		Float defaultValue = (Float) propertyDefinition.getDefaultValue().get();
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		floatValueContainer = new FloatValueContainer(defaultValue, suggestedPopulationSize);
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PersonId personId) {
		Float result = floatValueContainer.getValue(personId.getValue());
		return (T) result;
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		super.setPropertyValue(personId, personPropertyValue);
		Float f = (Float) personPropertyValue;
		floatValueContainer.setValue(personId.getValue(), f);
	}

}
