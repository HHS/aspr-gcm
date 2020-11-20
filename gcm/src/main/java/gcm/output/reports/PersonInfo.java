package gcm.output.reports;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the primary information that was associated with a person just
 * prior to their removal from the simulation. It is designed to be used by
 * {@link Report} classes that need insight into a person after the person has
 * been removed. Construction is conducted via the contained Builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
@Immutable
public final class PersonInfo {

	/*
	 * Container class for the person's information just prior to removal from
	 * the simulation
	 *
	 */
	private static class Scaffold {
		private PersonId personId;
		private RegionId regionId;
		private CompartmentId compartmentId;
		private Map<PersonPropertyId, Object> propertyValues = new LinkedHashMap<>();
		private Map<ResourceId, Long> resourceValues = new LinkedHashMap<>();
	}

	private final Scaffold scaffold;

	/**
	 * Returns the id of the removed person *
	 */
	public PersonId getPersonId() {
		return scaffold.personId;
	}

	/**
	 * Returns the region of the person when they were removed from the
	 * simulation
	 * 
	 */
	public RegionId getRegionId() {
		return scaffold.regionId;
	}

	/**
	 * Returns the compartment of the person when they were removed from the
	 * simulation
	 * 
	 */
	public CompartmentId getCompartmentId() {
		return scaffold.compartmentId;
	}

	/**
	 * Returns a map of the person property values of the person when they were
	 * removed from the simulation
	 * 
	 */
	public Map<PersonPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

	/**
	 * Returns a map of the resource levels of the person when they were removed
	 * from the simulation
	 * 
	 */

	public Map<ResourceId, Long> getResourceValues() {
		return Collections.unmodifiableMap(scaffold.resourceValues);
	}

	/*
	 * Hidden constructor
	 */
	private PersonInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	@NotThreadSafe
	public static class Builder {
		
		private Builder() {
			
		}
		Scaffold scaffold = new Scaffold();

		private void validate() {
			if (scaffold.personId == null) {
				throw new RuntimeException("null person id");
			}
			if (scaffold.regionId == null) {
				throw new RuntimeException("null region id");
			}
			if (scaffold.compartmentId == null) {
				throw new RuntimeException("null compartment id");
			}
		}

		/**
		 * Builds the {@link PersonInfo} from the collected data
		 * 
		 * @throws RuntimeException
		 *             <li>if no person id was collected
		 *             <li>if no region id was collected
		 *             <li>if no compartment id was collected
		 * 
		 */
		public PersonInfo build() {
			try {
				validate();
				return new PersonInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the person id
		 * 
		 * @throws RuntimeException
		 *             if the person id is null
		 */
		public Builder setPersonId(PersonId personId) {
			if (personId == null) {
				throw new RuntimeException("null person id");
			}
			scaffold.personId = personId;
			return this;
		}

		/**
		 * Sets the region id
		 * 
		 * @throws RuntimeException
		 *             if the region id is null
		 */
		public Builder setPersonRegionId(RegionId regionId) {
			if (regionId == null) {
				throw new RuntimeException("null region id");
			}
			scaffold.regionId = regionId;
			return this;
		}

		/**
		 * Sets the compartment id
		 * 
		 * @throws RuntimeException
		 *             if the compartment id is null
		 */
		public Builder setPersonCompartmentId(CompartmentId compartmentId) {
			if (compartmentId == null) {
				throw new RuntimeException("null compartment id");
			}
			scaffold.compartmentId = compartmentId;
			return this;
		}

		/**
		 * Sets the person property value
		 * 
		 * @throws RuntimeException
		 *             <li>if the person property id is null
		 *             <li>if the person property value is null
		 */
		public Builder setPersonPropertyValue(PersonPropertyId personPropertyId, Object personPropertyValue) {
			if (personPropertyId == null) {
				throw new RuntimeException("null person property id");
			}

			if (personPropertyValue == null) {
				throw new RuntimeException("null person property value");
			}

			scaffold.propertyValues.put(personPropertyId, personPropertyValue);
			return this;
		}

		/**
		 * Sets the resource level
		 * 
		 * @throws RuntimeException
		 *             <li>if the resource id is null
		 *             <li>if the resource value is negative
		 */
		public Builder setPersonResourceValue(ResourceId resourceId, Long resourceValue) {
			if (resourceId == null) {
				throw new RuntimeException("null resource id");
			}

			if (resourceValue < 0) {
				throw new RuntimeException("negative resource value");
			}
			scaffold.resourceValues.put(resourceId, resourceValue);
			return this;
		}
	}

}
