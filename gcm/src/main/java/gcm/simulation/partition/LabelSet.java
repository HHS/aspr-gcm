package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;

/**
 * A {@linkplain LabelSet} is a set of labels that are used to specify a sub-set
 * of the cell space of a partition during sampling.
 * 
 * Partitions are composed of cells that are associated with combinations of
 * labels associated with the various attributes of people. The label set
 * specifies a subset of that space by value.
 * 
 * For example: Suppose a partition is formed by the regions and two person
 * properties. The regions are grouped together under state labels. The first
 * property is the Integer AGE and is grouped by PRESCHOOL, SCHOOL and ADULT.
 * The second property is the Integer VACCINE_DOSES_RECEIVED and ranges from 0
 * to 3 inclusive.
 * 
 * The {@link LabelSet} [REGION = VIRGINIA, AGE=PRESHOOL,
 * VACCINE_DOSES_RECEIVED=2] will match the single partition cell that
 * represents Virginia preschoolers who have received 2 doses of vaccine. The
 * {@link LabelSet} [REGION = VIRGINIA, AGE=PRESHOOL] will match all partition
 * cells that represent Virginia preschoolers, without regard to doses of
 * vaccine received.
 * 
 * 
 * Label Sets are built by the modeler via the supplied Builder class.
 *
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.REQUIRED)
public final class LabelSet {

	/**
	 * 
	 */
	@Override
	public int hashCode() {
		return scaffold.hashCode();
	}

	/**
	 * Two {@linkplain LabelSet} object are equal is their labels are equal.
	 */
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
		LabelSet other = (LabelSet) obj;
		return scaffold.equals(other.scaffold);
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Source(proxy = LabelSet.class)
	public static class Builder {

		private Builder() {
		}

		Scaffold scaffold = new Scaffold();

		public LabelSet build() {
			try {
				return new LabelSet(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the compartment label
		 */
		public Builder setCompartmentLabel(Object compartmentLabel) {
			scaffold.compartmentLabel = compartmentLabel;
			return this;
		}
		/**
		 * Sets the region label for the given {@link ResourceId}
		 */
		public Builder setRegionLabel(Object regionLabel) {
			scaffold.regionLabel = regionLabel;
			return this;
		}

		/**
		 * Sets the group label
		 */
		public Builder setGroupLabel(Object groupLabel) {
			scaffold.groupLabel = groupLabel;
			return this;
		}

		/**
		 * Sets the resource label for the given {@link ResourceId}
		 */
		public Builder setResourceLabel(ResourceId resourceId, Object resourceLabel) {
			if (resourceLabel != null) {
				scaffold.personResourceLabels.put(resourceId, resourceLabel);
			} else {
				scaffold.personResourceLabels.remove(resourceId);
			}
			return this;
		}

		/**
		 * Sets the property label for the given {@link PersonPropertyId}
		 */
		public Builder setPropertyLabel(PersonPropertyId personPropertyId, Object propertyLabel) {
			if (propertyLabel != null) {
				scaffold.personPropertyLabels.put(personPropertyId, propertyLabel);
			} else {
				scaffold.personPropertyLabels.remove(personPropertyId);
			}
			return this;
		}

	}

	/**
	 * Returns true if and only if the given {@link LabelSet} is a subset of
	 * this {@link LabelSet}. Only Non-null values in the input are compared to
	 * the corresponding values in this label set.
	 */
	public boolean isSubsetMatch(LabelSet labelSet) {
		if (labelSet.scaffold.compartmentLabel != null) {
			if (scaffold.compartmentLabel == null) {
				return false;
			} else {
				if (!scaffold.compartmentLabel.equals(labelSet.scaffold.compartmentLabel)) {
					return false;
				}
			}
		}

		if (labelSet.scaffold.groupLabel != null) {
			if (scaffold.groupLabel == null) {
				return false;
			} else {
				if (!scaffold.groupLabel.equals(labelSet.scaffold.groupLabel)) {
					return false;
				}
			}
		}

		if (labelSet.scaffold.regionLabel != null) {
			if (scaffold.regionLabel == null) {
				return false;
			} else {
				if (!scaffold.regionLabel.equals(labelSet.scaffold.regionLabel)) {
					return false;
				}
			}
		}

		for (PersonPropertyId personPropertyId : labelSet.scaffold.personPropertyLabels.keySet()) {
			if (!scaffold.personPropertyLabels.containsKey(personPropertyId)) {
				return false;
			}
			Object localPropertyLabel = scaffold.personPropertyLabels.get(personPropertyId);
			Object propertyLabel = labelSet.scaffold.personPropertyLabels.get(personPropertyId);

			if (!localPropertyLabel.equals(propertyLabel)) {
				return false;
			}
		}

		for (ResourceId resourceId : labelSet.scaffold.personResourceLabels.keySet()) {
			if (!scaffold.personResourceLabels.containsKey(resourceId)) {
				return false;
			}
			Object localResourceLabel = scaffold.personResourceLabels.get(resourceId);
			Object resourceLabel = labelSet.scaffold.personResourceLabels.get(resourceId);
			if (!localResourceLabel.equals(resourceLabel)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@SourceMethod(status = TestStatus.UNREQUIRED)
	public String toString() {
		return scaffold.toString();
	}

	private static class Scaffold {

		private Object compartmentLabel;

		private Object regionLabel;

		private Object groupLabel;

		private Map<PersonPropertyId, Object> personPropertyLabels = new LinkedHashMap<>();

		private Map<ResourceId, Object> personResourceLabels = new LinkedHashMap<>();

		@Override
		public String toString() {
			return "LabelSet [compartmentLabel=" + compartmentLabel + ", regionLabel=" + regionLabel + ", groupLabel=" + groupLabel + ", personPropertyLabels=" + personPropertyLabels
					+ ", personResourceLabels=" + personResourceLabels + "]";
		}

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
			Scaffold other = (Scaffold) obj;
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

	}

	private final Scaffold scaffold;
	private final boolean empty;

	/**
	 * Returns the compartment label for this {@link LabelSet}
	 */
	public Optional<Object> getCompartmentLabel() {
		return Optional.ofNullable(scaffold.compartmentLabel);
	}

	/**
	 * Returns the group label for this {@link LabelSet}
	 */
	public Optional<Object> getGroupLabel() {
		return Optional.ofNullable(scaffold.groupLabel);
	}

	/**
	 * Returns the property label associated to the given
	 * {@link PersonPropertyId} for this {@link LabelSet}
	 */
	public Optional<Object> getPersonPropertyLabel(PersonPropertyId personPropertyId) {
		return Optional.ofNullable(scaffold.personPropertyLabels.get(personPropertyId));
	}

	/**
	 * Returns the resource label associated to the given {@link ResourceId} for
	 * this {@link LabelSet}
	 */
	public Optional<Object> getPersonResourceLabel(ResourceId resourceId) {
		return Optional.ofNullable(scaffold.personResourceLabels.get(resourceId));
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link LabelSetInfo}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(scaffold.personPropertyLabels.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with
	 * this {@link LabelSetInfo}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(scaffold.personResourceLabels.keySet());
	}

	/**
	 * Returns the region label for this {@link LabelSet}
	 */
	public Optional<Object> getRegionLabel() {
		return Optional.ofNullable(scaffold.regionLabel);
	}

	/**
	 * Returns true if and only if this {@link LabelSet} has no label values
	 */
	public boolean isEmpty() {
		return empty;
	}

	private LabelSet(Scaffold scaffold) {
		this.scaffold = scaffold;

		empty = (scaffold.compartmentLabel == null) && (scaffold.regionLabel == null) && (scaffold.groupLabel == null) && (scaffold.personPropertyLabels.isEmpty())
				&& (scaffold.personResourceLabels.isEmpty());

	}
}
