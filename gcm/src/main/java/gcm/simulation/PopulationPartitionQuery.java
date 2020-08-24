package gcm.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;

public final class PopulationPartitionQuery {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compartmentLabel == null) ? 0 : compartmentLabel.hashCode());
		result = prime * result + ((groupLabel == null) ? 0 : groupLabel.hashCode());
		result = prime * result + ((personPropertyLabels == null) ? 0 : personPropertyLabels.hashCode());
		result = prime * result + ((personResourceLabels == null) ? 0 : personResourceLabels.hashCode());
		result = prime * result + ((regionLabel == null) ? 0 : regionLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PopulationPartitionQuery other = (PopulationPartitionQuery) obj;
		if (compartmentLabel == null) {
			if (other.compartmentLabel != null) {
				return false;
			}
		} else if (!compartmentLabel.equals(other.compartmentLabel)) {
			return false;
		}
		if (groupLabel == null) {
			if (other.groupLabel != null) {
				return false;
			}
		} else if (!groupLabel.equals(other.groupLabel)) {
			return false;
		}
		if (personPropertyLabels == null) {
			if (other.personPropertyLabels != null) {
				return false;
			}
		} else if (!personPropertyLabels.equals(other.personPropertyLabels)) {
			return false;
		}
		if (personResourceLabels == null) {
			if (other.personResourceLabels != null) {
				return false;
			}
		} else if (!personResourceLabels.equals(other.personResourceLabels)) {
			return false;
		}
		if (regionLabel == null) {
			if (other.regionLabel != null) {
				return false;
			}
		} else if (!regionLabel.equals(other.regionLabel)) {
			return false;
		}
		return true;
	}

	@Override
	@SourceMethod(status = TestStatus.UNREQUIRED)
	public String toString() {
		return "PopulationPartitionQuery [compartmentLabel=" + compartmentLabel + ", regionLabel=" + regionLabel
				+ ", groupLabel=" + groupLabel + ", personPropertyLabels=" + personPropertyLabels
				+ ", personResourceLabels=" + personResourceLabels + "]";
	}

	private Object compartmentLabel;

	private Object regionLabel;

	private Object groupLabel;

	private Map<PersonPropertyId, Object> personPropertyLabels = new LinkedHashMap<>();

	private Map<ResourceId, Object> personResourceLabels = new LinkedHashMap<>();

	public Object getCompartmentLabel() {
		return compartmentLabel;
	}

	public Object getGroupLabel() {
		return groupLabel;
	}

	public Object getPersonPropertyLabel(PersonPropertyId personPropertyId) {
		return personPropertyLabels.get(personPropertyId);
	}

	public Object getPersonResourceLabel(ResourceId resourceId) {
		return personResourceLabels.get(resourceId);
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link PopulationPartitionQuery}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(personPropertyLabels.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with this
	 * {@link PopulationPartitionQuery}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(personResourceLabels.keySet());
	}

	public Object getRegionLabel() {
		return regionLabel;
	}

	private PopulationPartitionQuery() {

	}

	public static Builder builder() {
		return new Builder();
	}

	@Source(status = TestStatus.REQUIRED, proxy = PopulationPartitionQuery.class)
	public final static class Builder {

		private Builder() {
		}

		private PopulationPartitionQuery populationPartitionQuery = new PopulationPartitionQuery();

		public PopulationPartitionQuery build() {
			try {
				return populationPartitionQuery;
			} finally {
				populationPartitionQuery = new PopulationPartitionQuery();
			}
		}

		/**
		 * Sets the compartment label. Allows null labels.
		 */
		public Builder setCompartmentLabel(Object compartmentLabel) {
			populationPartitionQuery.compartmentLabel = compartmentLabel;
			return this;
		}

		/**
		 * Sets the group label. Allows null labels.
		 */
		public Builder setGroupLabel(Object groupLabel) {
			populationPartitionQuery.groupLabel = groupLabel;
			return this;
		}

		/**
		 * Sets the region label. Allows null labels.
		 */
		public Builder setRegionLabel(Object regionLabel) {
			populationPartitionQuery.regionLabel = regionLabel;
			return this;
		}

		/**
		 * Sets the person property label. Allows null labels.
		 * 
		 * @throws IllegalArgumentException
		 *                                  <li>if the personPropertyId is null
		 * 
		 */
		public Builder setPersonPropertyLabel(PersonPropertyId personPropertyId, Object personPropertyLabel) {
			if (personPropertyId == null) {
				throw new IllegalArgumentException("person property id is null");
			}

			if (personPropertyLabel != null) {
				populationPartitionQuery.personPropertyLabels.put(personPropertyId, personPropertyLabel);
			} else {
				populationPartitionQuery.personPropertyLabels.remove(personPropertyId);
			}
			return this;
		}

		/**
		 * Sets the person resource label. Allows null labels.
		 * 
		 * @throws IllegalArgumentException
		 *                                  <li>if the resourceId is null
		 * 
		 */
		public Builder setPersonResourceLabel(ResourceId resourceId, Object personResourceLabel) {
			if (resourceId == null) {
				throw new IllegalArgumentException("resource id is null");
			}
			if (personResourceLabel != null) {
				populationPartitionQuery.personResourceLabels.put(resourceId, personResourceLabel);
			} else {
				populationPartitionQuery.personResourceLabels.remove(resourceId);
			}
			return this;
		}

	}

}
