package gcm.simulation;

import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.EnumContainer;

/**
 * Implementor of PropertyManager that compresses Enum property values into a
 * byte-based data structure of the various int-like primitives.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class EnumPropertyManager extends AbstractIndexedPropertyManager {
	/*
	 * The storage container.
	 */
	private EnumContainer enumContainer;

	/**
	 * Constructs this EnumPropertyManager.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public EnumPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition, initialSize);
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		
		enumContainer = new EnumContainer(propertyDefinition.getType(), propertyDefinition.getDefaultValue().get(),initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		return (T) enumContainer.getValue(id);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		enumContainer.setValue(id, propertyValue);
	}

}
