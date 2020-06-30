package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
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
public final class BooleanPropertyManager extends AbstractPropertyManager {

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
	public BooleanPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId propertyId) {
		super(context, propertyDefinition, propertyId);
		if (propertyDefinition.getType() != Boolean.class) {
			throw new RuntimeException("Requires a property definition with Boolean type ");
		}
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		boolean defaultValue = (Boolean) propertyDefinition.getDefaultValue().get();
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		boolContainer = new BooleanContainer(defaultValue, suggestedPopulationSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PersonId personId) {
		Boolean result = boolContainer.get(personId.getValue());
		return (T) result;
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		super.setPropertyValue(personId, personPropertyValue);
		Boolean b = (Boolean) personPropertyValue;
		boolContainer.set(personId.getValue(), b.booleanValue());
	}
}
