package gcm.simulation;

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
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class ObjectPropertyManager extends AbstractIndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores Objects as an array.
	 */
	private ObjectValueContainer objectValueContainer;
	private final Object defaultValue;

	public ObjectPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition,  initialSize);	
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		
		defaultValue = propertyDefinition.getDefaultValue().get();		
		objectValueContainer = new ObjectValueContainer(defaultValue, initialSize);
	}

	@Override
	public <T> T getPropertyValue(int id) {		
		return objectValueContainer.getValue(id);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		objectValueContainer.setValue(id, propertyValue);		
	}
	
	@Override
	public void removeId(int id) {
		//dropping reference to the currently stored value for potential garbage collection 
		objectValueContainer.setValue(id, defaultValue);
	}

}
