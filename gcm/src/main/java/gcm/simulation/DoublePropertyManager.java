package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
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
public final class DoublePropertyManager extends AbstractPropertyManager {
	
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
	public DoublePropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId propertyId) {
		super(context, propertyDefinition, propertyId);
		
		if (propertyDefinition.getType() != Double.class) {
			throw new RuntimeException("Requires a property definition with Double type");
		}
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		
		Double defaultValue = (Double)propertyDefinition.getDefaultValue().get();
		
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		doubleValueContainer = new DoubleValueContainer(defaultValue, suggestedPopulationSize);		
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PersonId personId) {
		Double result = doubleValueContainer.getValue(personId.getValue());
		return (T) result;
	}

	
	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {		
		super.setPropertyValue(personId, personPropertyValue);
		Double d = (Double) personPropertyValue;
		doubleValueContainer.setValue(personId.getValue(), d);
	}

}
