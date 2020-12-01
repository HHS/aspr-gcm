package gcm.simulation;

import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.BooleanContainer;

/**
 * Implementor of PropertyManager that compresses Boolean property values into a
 * bit-based data structure.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class BooleanPropertyManager extends AbstractIndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores boolean values as bits.
	 */
	private BooleanContainer boolContainer;

	/**
	 * Constructs this BooleanPropertyManager.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public BooleanPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition, initialSize);
		if (propertyDefinition.getType() != Boolean.class) {
			throw new RuntimeException("Requires a property definition with Boolean type ");
		}
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		boolean defaultValue = (Boolean) propertyDefinition.getDefaultValue().get();
		
		boolContainer = new BooleanContainer(defaultValue, initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		Boolean result = boolContainer.get(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id , Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		Boolean b = (Boolean) propertyValue;
		boolContainer.set(id, b.booleanValue());
	}
}
