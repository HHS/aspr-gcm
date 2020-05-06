package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
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
@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public final class IntPropertyManager extends AbstractPropertyManager {

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
	public IntPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId personPropertyId) {
		super(context, propertyDefinition, personPropertyId);
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
		
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		intValueContainer = new IntValueContainer(longDefaultValue, suggestedPopulationSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PersonId personId) {
		
		switch (intValueType) {
		case BYTE:
			Byte b = intValueContainer.getValueAsByte(personId.getValue());
			return (T) b;
		case INT:
			Integer i = intValueContainer.getValueAsInt(personId.getValue());
			return (T) i;
		case LONG:
			Long l = intValueContainer.getValueAsLong(personId.getValue());
			return (T) l;
		case SHORT:
			Short s = intValueContainer.getValueAsShort(personId.getValue());
			return (T) s;
		default:
			throw new RuntimeException("unhandled type");
		}
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		super.setPropertyValue(personId, personPropertyValue);

		switch (intValueType) {
		case BYTE:
			Byte b = (Byte) personPropertyValue;
			intValueContainer.setByteValue(personId.getValue(), b);
			break;
		case INT:
			Integer i = (Integer) personPropertyValue;
			intValueContainer.setIntValue(personId.getValue(), i);
			break;
		case LONG:
			Long l = (Long) personPropertyValue;
			intValueContainer.setLongValue(personId.getValue(), l);
			break;
		case SHORT:
			Short s = (Short) personPropertyValue;
			intValueContainer.setShortValue(personId.getValue(), s);
			break;
		default:
			throw new RuntimeException("unhandled type "+intValueType);
		}
	}

}
