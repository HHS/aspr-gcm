package gcm.simulation;

import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
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
@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public final class EnumPropertyManager extends AbstractPropertyManager {
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
	public EnumPropertyManager(Context context, PropertyDefinition propertyDefinition, PersonPropertyId propertyId) {
		super(context, propertyDefinition, propertyId);
		int suggestedPopulationSize = context.getScenario().getSuggestedPopulationSize();
		
		if(!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("default value is not present for "+propertyDefinition);
		}
		
		enumContainer = new EnumContainer(propertyDefinition.getType(), propertyDefinition.getDefaultValue().get(),suggestedPopulationSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(PersonId personId) {
		return (T) enumContainer.getValue(personId.getValue());
	}

	@Override
	public void setPropertyValue(PersonId personId, Object personPropertyValue) {
		super.setPropertyValue(personId, personPropertyValue);
		enumContainer.setValue(personId.getValue(), personPropertyValue);
	}

}
