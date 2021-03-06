package gcm.scenario;

import java.util.Optional;

import gcm.util.annotations.Source;
import net.jcip.annotations.ThreadSafe;

/**
 * A thread-safe, immutable class that defines a property, but does not indicate
 * the role that property is playing within GCM.
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source
public final class PropertyDefinition {

	public static Builder builder() {
		return new Builder();
	}

	private static class Scaffold {
		private Class<?> type = null;

		private MapOption mapOption = MapOption.NONE;

		private boolean propertyValuesAreMutable = true;

		private Object defaultValue = null;

		private TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
	}

	/**
	 * Builder class for {@linkplain PropertyDefinition}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Builds the property definition
		 * 
		 * @throws RuntimeException
		 *             if the class type of the definition is not assigned or
		 *             null
		 */
		public PropertyDefinition build() {
			try {				
				return new PropertyDefinition(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the class type. Value must be set by client.
		 */
		public Builder setType(final Class<?> type) {
			scaffold.type = type;
			return this;
		}

		/**
		 * Sets the {@linkplain MapOption}. Default value is
		 * {@link MapOption#NONE}
		 */
		public Builder setMapOption(final MapOption mapOption) {
			scaffold.mapOption = mapOption;
			return this;
		}

		/**
		 * Sets property value mutability during simulation run time. Default
		 * value is true.
		 */
		public Builder setPropertyValueMutability(boolean propertyValuesAreMutable) {
			scaffold.propertyValuesAreMutable = propertyValuesAreMutable;
			return this;
		}

		/**
		 * Sets the default value for property values of this definition. Value
		 * must be set(non-null) by client.
		 */
		public Builder setDefaultValue(Object defaultValue) {
			scaffold.defaultValue = defaultValue;
			return this;
		}

		/**
		 * Sets the {@linkplain TimeTrackingPolicy}. Default value is
		 * {@link TimeTrackingPolicy#DO_NOT_TRACK_TIME}
		 */
		public Builder setTimeTrackingPolicy(TimeTrackingPolicy timeTrackingPolicy) {
			scaffold.timeTrackingPolicy = timeTrackingPolicy;
			return this;
		}
	}

	private final Class<?> type;

	private final MapOption mapOption;

	private final boolean propertyValuesAreMutable;

	private final Object defaultValue;

	private final TimeTrackingPolicy timeTrackingPolicy;

	private PropertyDefinition(Scaffold scaffold) {
		if(scaffold.type == null) {
			throw new RuntimeException("property definition requires a non-null type");
		}
		this.type = scaffold.type;

		this.mapOption = scaffold.mapOption;

		this.propertyValuesAreMutable = scaffold.propertyValuesAreMutable;

		this.defaultValue = scaffold.defaultValue;

		this.timeTrackingPolicy = scaffold.timeTrackingPolicy;
	}

	/**
	 * Returns the Optional<Object> containing default value for the property
	 * definition. This exists so that modelers do not have to specify values
	 * for every property instance during scenario construction and so that the
	 * simulation can reduce memory by using default values. Any property
	 * definition that has a null default value must have corresponding property
	 * value assignments within the scenario or experiment that cover all cases.
	 * Since people, groups and batches can be created dynamically during the
	 * simulation execution, the corresponding property definitions MUST contain
	 * non-null default values.
	 *
	 */
	public Optional<Object> getDefaultValue() {
		if (defaultValue == null) {
			return Optional.empty();
		}
		return Optional.of(defaultValue);
	}
	
	

	/**
	 * Returns the non-null, default value for the property definition. This
	 * exists so that modelers do not have to specify values for every property
	 * instance during scenario construction and so that the simulation can
	 * reduce memory by using default values.
	 *
	 */
	public MapOption getMapOption() {
		return mapOption;
	}

	/**
	 * Returns that class type of this definition. It is used to ensure that all
	 * values assigned to properties have a predictable type from the modeler's
	 * perspective. Property assignments are descendant class tolerant. For
	 * example, a property having a defined type of Number, would accept values
	 * that are Double, Integer or any other descendant type.
	 *
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * The modeler may define a property such that all associated property
	 * values must be equal to the default value of this property definition.
	 * Any attempt to assign a value to a property so defined will result in an
	 * error. This can be used to ensure that some variables remain constant
	 * throughout the run of a simulation instance.
	 *
	 * Returns true if and only if the value of the property must remain
	 * constant.
	 */
	public boolean getPropertyValueAreMutability() {
		return propertyValuesAreMutable;
	}

	/**
	 * Returns the time tracking policy for the property.
	 */
	public TimeTrackingPolicy getTimeTrackingPolicy() {
		return timeTrackingPolicy;
	}

	/**
	 * Boilerplate implementation that uses all fields.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (propertyValuesAreMutable ? 1231 : 1237);
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((mapOption == null) ? 0 : mapOption.hashCode());
		result = prime * result + ((timeTrackingPolicy == null) ? 0 : timeTrackingPolicy.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Boilerplate implementation that uses all fields and type equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDefinition other = (PropertyDefinition) obj;
		if (propertyValuesAreMutable != other.propertyValuesAreMutable)
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (mapOption != other.mapOption)
			return false;
		if (timeTrackingPolicy != other.timeTrackingPolicy)
			return false;
		if (type == null) {
			return other.type == null;
		} else
			return type.equals(other.type);
	}

	/**
	 * Standard string representation in the form:
	 * 
	 * PropertyDefinition [type=someType,mapOption=mapOption,
	 * constantPropertyValues=true, defaultValue=someValue,
	 * timeTrackingPolicy=policy]
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyDefinition [type=");
		builder.append(type);
		builder.append(", mapOption=");
		builder.append(mapOption);
		builder.append(", propertyValuesAreMutable=");
		builder.append(propertyValuesAreMutable);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append(", timeTrackingPolicy=");
		builder.append(timeTrackingPolicy);
		builder.append("]");
		return builder.toString();
	}

}