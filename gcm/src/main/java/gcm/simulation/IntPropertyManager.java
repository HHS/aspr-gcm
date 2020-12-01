package gcm.simulation;

import gcm.scenario.PropertyDefinition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.containers.IntValueContainer;
import gcm.util.containers.IntValueContainer.IntValueType;

/**
 * Implementor of PropertyManager that compresses Byte, Short, Integer or Long
 * property values into a byte-based array data structure.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class IntPropertyManager extends AbstractIndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores the various Boxed integral
	 * types values as bytes.
	 */
	private IntValueContainer intValueContainer;

	/*
	 * The particular IntValueType for this property manager as determined by
	 * the class type associated with the corresponding property definition.
	 */
	private IntValueType intValueType;

	/**
	 * Constructs this IntPropertyManager.
	 * 
	 * @param environment
	 * @param propertyDefinition
	 * @param propertyId
	 */
	public IntPropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition, initialSize);
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}

		
		Object defaultValue = propertyDefinition.getDefaultValue().get();
		long longDefaultValue;
		if (propertyDefinition.getType() == Byte.class) {
			intValueType = IntValueType.BYTE;
		} else if (propertyDefinition.getType() == Short.class) {
			intValueType = IntValueType.SHORT;
		} else if (propertyDefinition.getType() == Integer.class) {
			intValueType = IntValueType.INT;
		} else if (propertyDefinition.getType() == Long.class) {
			intValueType = IntValueType.LONG;
		} else {
			throw new RuntimeException("Requires a property definition type of Byte, Short, Integer or Long");
		}
		
		switch (intValueType) {
		case BYTE:
			Byte b = (Byte) defaultValue;
			longDefaultValue = b.longValue();
			break;
		case INT:
			Integer i = (Integer) defaultValue;
			longDefaultValue = i.longValue();
			break;
		case LONG:
			Long l = (Long) defaultValue;
			longDefaultValue = l.longValue();
			break;
		case SHORT:
			Short s = (Short) defaultValue;
			longDefaultValue = s.longValue();
			break;
		default:
			throw new RuntimeException("unhandled type "+intValueType);
		}
		
		intValueContainer = new IntValueContainer(longDefaultValue, initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		
		switch (intValueType) {
		case BYTE:
			Byte b = intValueContainer.getValueAsByte(id);
			return (T) b;
		case INT:
			Integer i = intValueContainer.getValueAsInt(id);
			return (T) i;
		case LONG:
			Long l = intValueContainer.getValueAsLong(id);
			return (T) l;
		case SHORT:
			Short s = intValueContainer.getValueAsShort(id);
			return (T) s;
		default:
			throw new RuntimeException("unhandled type");
		}
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);

		switch (intValueType) {
		case BYTE:
			Byte b = (Byte) propertyValue;
			intValueContainer.setByteValue(id, b);
			break;
		case INT:
			Integer i = (Integer) propertyValue;
			intValueContainer.setIntValue(id, i);
			break;
		case LONG:
			Long l = (Long) propertyValue;
			intValueContainer.setLongValue(id, l);
			break;
		case SHORT:
			Short s = (Short) propertyValue;
			intValueContainer.setShortValue(id, s);
			break;
		default:
			throw new RuntimeException("unhandled type "+intValueType);
		}
	}

}
