package gcm.simulation;

import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.DoubleValueContainer;

/**
 * Implementor of PropertyManager that compresses Double property values into a
 * double[]-based data structure.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class DoublePropertyManager extends AbstractIndexedPropertyManager {
	
	/*
	 * A container, indexed by person id, that stores Double values as an array
	 * of double.
	 */
	private DoubleValueContainer doubleValueContainer;
	

	/**
	 * Constructs this DoublePropertyManager.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public DoublePropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition,  initialSize);
		
		if (propertyDefinition.getType() != Double.class) {
			throw new RuntimeException("Requires a property definition with Double type");
		}
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		
		Double defaultValue = (Double)propertyDefinition.getDefaultValue().get();
		
		doubleValueContainer = new DoubleValueContainer(defaultValue, initialSize);		
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		Double result = doubleValueContainer.getValue(id);
		return (T) result;
	}

	
	@Override
	public void setPropertyValue(int id, Object propertyValue) {		
		super.setPropertyValue(id, propertyValue);
		Double d = (Double) propertyValue;
		doubleValueContainer.setValue(id, d);
	}

}
