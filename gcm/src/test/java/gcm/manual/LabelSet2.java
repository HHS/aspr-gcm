package gcm.manual;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.partition.LabelSetInfo;
import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED)
public class LabelSet2 {
	
	public static Builder builder() {
		
		return new Builder();
	}
	
	public static class Builder{
		
		private Builder() {}
		
		Scaffold scaffold = new Scaffold();
		
		public LabelSet2 build() {
			try {
				return new LabelSet2(scaffold);
			}finally {
				scaffold = new Scaffold();
			}
		}
		public Builder setCompartmentLabel(Object compartmentLabel) {
			scaffold.compartmentLabel = compartmentLabel;
			return this;
		}
		
		public Builder setRegionLabel(Object regionLabel) {
			scaffold.regionLabel = regionLabel;
			return this;
		}
		
		public Builder setGroupLabel(Object groupLabel) {
			scaffold.groupLabel = groupLabel;
			return this;
		}
		
		public Builder setResourceLabel(ResourceId resourceId, Object resourceLabel) {
			scaffold.personResourceLabels.put(resourceId, resourceLabel);
			return this;
		}

		public Builder setPropertyLabel(PersonPropertyId personPropertyId, Object propertyLabel) {
			scaffold.personPropertyLabels.put(personPropertyId, propertyLabel);
			return this;
		}

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

	/**
	 * Returns true if and only if the given {@link LabelSet2} is a subset of this
	 * {@link LabelSet2}. Only Non-null values in the input are compared to the
	 * corresponding values in this label set.
	 */
	public boolean isSubsetMatch(LabelSet2 labelSetInfo) {
		if (labelSetInfo.compartmentLabel != null) {
			if (compartmentLabel == null) {
				return false;
			} else {
				if (!compartmentLabel.equals(labelSetInfo.compartmentLabel)) {
					return false;
				}
			}
		}

		if (labelSetInfo.groupLabel != null) {
			if (groupLabel == null) {
				return false;
			} else {
				if (!groupLabel.equals(labelSetInfo.groupLabel)) {
					return false;
				}
			}
		}

		if (labelSetInfo.regionLabel == null) {
			if (regionLabel == null) {
				return false;
			}
		} else {
			if (!regionLabel.equals(labelSetInfo.regionLabel)) {
				return false;
			}
		}

		for (PersonPropertyId personPropertyId : labelSetInfo.personPropertyLabels.keySet()) {
			if (!personPropertyLabels.containsKey(personPropertyId)) {
				return false;
			}
			Object localPropertyLabel = personPropertyLabels.get(personPropertyId);
			Object propertyLabel = labelSetInfo.personPropertyLabels.get(personPropertyId);

			if (!localPropertyLabel.equals(propertyLabel)) {
				return false;
			}
		}

		for (ResourceId resourceId : labelSetInfo.personResourceLabels.keySet()) {
			if (!personResourceLabels.containsKey(resourceId)) {
				return false;
			}
			Object localResourceLabel = personResourceLabels.get(resourceId);
			Object resourceLabel = labelSetInfo.personResourceLabels.get(resourceId);
			if (!localResourceLabel.equals(resourceLabel)) {
				return false;
			}
		}
		return true;
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
		LabelSet2 other = (LabelSet2) obj;
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
		return "labelSet [compartmentLabel=" + compartmentLabel + ", regionLabel=" + regionLabel + ", groupLabel="
				+ groupLabel + ", personPropertyLabels=" + personPropertyLabels + ", personResourceLabels="
				+ personResourceLabels + "]";
	}

	private static class Scaffold {
		
		private Object compartmentLabel;

		private Object regionLabel;

		private Object groupLabel;

		private Map<PersonPropertyId, Object> personPropertyLabels = new LinkedHashMap<>();

		private Map<ResourceId, Object> personResourceLabels = new LinkedHashMap<>();
		
	}
	
	private final Object compartmentLabel;

	private final Object regionLabel;

	private final Object groupLabel;

	private final Map<PersonPropertyId, Object> personPropertyLabels;

	private final Map<ResourceId, Object> personResourceLabels;


	public Optional<Object> getCompartmentLabel() {
		return Optional.ofNullable(compartmentLabel);
	}

	public Optional<Object> getGroupLabel() {
		return Optional.ofNullable(groupLabel);
	}

	public Optional<Object> getPersonPropertyLabel(PersonPropertyId personPropertyId) {
		return Optional.ofNullable(personPropertyLabels.get(personPropertyId));
	}

	public Optional<Object> getPersonResourceLabel(ResourceId resourceId) {
		return Optional.ofNullable(personResourceLabels.get(resourceId));
	}

	/**
	 * Returns an unmodifiable set of {@link PersonPropertyId} values associated
	 * with this {@link LabelSetInfo}
	 * 
	 */
	public Set<PersonPropertyId> getPersonPropertyIds() {
		return Collections.unmodifiableSet(personPropertyLabels.keySet());
	}

	/**
	 * Returns an unmodifiable set of {@link ResourceId} values associated with this
	 * {@link LabelSetInfo}
	 * 
	 */
	public Set<ResourceId> getPersonResourceIds() {
		return Collections.unmodifiableSet(personResourceLabels.keySet());
	}

	public Optional<Object> getRegionLabel() {
		return Optional.ofNullable(regionLabel);
	}

	private LabelSet2(Scaffold scaffold) {
		
		this.compartmentLabel = scaffold.compartmentLabel;

		this.regionLabel = scaffold.regionLabel;

		this.groupLabel = scaffold.groupLabel;

		this.personPropertyLabels = scaffold.personPropertyLabels;

		this.personResourceLabels = scaffold.personResourceLabels;

	}

}
