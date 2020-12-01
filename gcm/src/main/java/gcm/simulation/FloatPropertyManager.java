package gcm.simulation;

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
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class FloatPropertyManager extends AbstractIndexedPropertyManager {

	

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
	public FloatPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition, initialSize);		
		if (propertyDefinition.getType() != Float.class) {
			throw new RuntimeException("Requires a property definition with float type");
		}
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		Float defaultValue = (Float) propertyDefinition.getDefaultValue().get();
		floatValueContainer = new FloatValueContainer(defaultValue, initialSize);
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		Float result = floatValueContainer.getValue(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		Float f = (Float) propertyValue;
		floatValueContainer.setValue(id, f);
	}

}
