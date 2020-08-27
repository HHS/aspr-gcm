package gcm.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gcm.scenario.PersonPropertyId;
import gcm.scenario.ResourceId;
import gcm.simulation.LabelSet.CompartmentLabelSet;
import gcm.simulation.LabelSet.EmptyLabelSet;
import gcm.simulation.LabelSet.GroupLabelSet;
import gcm.simulation.LabelSet.PersonPropertyLabelSet;
import gcm.simulation.LabelSet.RegionLabelSet;
import gcm.simulation.LabelSet.ResourceLabelSet;
import gcm.simulation.LabelSet.WithLabelSet;
import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED)
public final class LabelSetInfo {

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
		LabelSetInfo other = (LabelSetInfo) obj;
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

	public Object getRegionLabel() {
		return regionLabel;
	}

	private void processLabelSet(LabelSet labelSet) {
		LabelType labelType = LabelType.getLabelType(labelSet);
		switch (labelType) {
		case COMPARTMENT:
			CompartmentLabelSet compartmentLabelSet = (CompartmentLabelSet) labelSet;
			compartmentLabel = compartmentLabelSet.compartmentLabel;
			break;
		case GROUP:
			GroupLabelSet groupLabelSet = (GroupLabelSet) labelSet;
			groupLabel = groupLabelSet.groupLabel;
			break;
		case PROPERTY:
			PersonPropertyLabelSet personPropertyLabelSet = (PersonPropertyLabelSet) labelSet;
			personPropertyLabels.put(personPropertyLabelSet.personPropertyId,
					personPropertyLabelSet.personPropertyLabel);
			break;
		case REGION:
			RegionLabelSet regionLabelSet = (RegionLabelSet) labelSet;
			regionLabel = regionLabelSet.regionLabel;
			break;
		case RESOURCE:
			ResourceLabelSet resourceLabelSet = (ResourceLabelSet) labelSet;
			this.personResourceLabels.put(resourceLabelSet.resourceId, resourceLabelSet.personResourceLabel);
			break;
		case WITH:
			WithLabelSet withLabelSet = (WithLabelSet) labelSet;
			processLabelSet(withLabelSet.a);
			processLabelSet(withLabelSet.b);
			break;
		case EMPTY:
			break;
		default:
			throw new RuntimeException("unhandled case");
		}

	}

	private static enum LabelType {

		WITH(WithLabelSet.class),

		COMPARTMENT(CompartmentLabelSet.class),

		REGION(RegionLabelSet.class),

		PROPERTY(PersonPropertyLabelSet.class),

		RESOURCE(ResourceLabelSet.class),

		GROUP(GroupLabelSet.class),

		EMPTY(EmptyLabelSet.class);

		private static Map<Class<? extends LabelSet>, LabelType> map = buildMap();

		private static Map<Class<? extends LabelSet>, LabelType> buildMap() {
			Map<Class<? extends LabelSet>, LabelType> result = new LinkedHashMap<>();
			for (LabelType filterType : LabelType.values()) {
				result.put(filterType.c, filterType);
			}
			return result;
		}

		private final Class<? extends LabelSet> c;

		private LabelType(Class<? extends LabelSet> c) {
			this.c = c;
		}

		static LabelType getLabelType(LabelSet labelSet) {
			LabelType result = map.get(labelSet.getClass());
			if (result == null) {
				throw new RuntimeException("unrecognized filter type for " + labelSet.getClass().getSimpleName());
			}
			return result;
		}
	}

	private LabelSetInfo(LabelSet labelSet) {
		processLabelSet(labelSet);
	}

	/**
	 * Builds a {@link LabelSetInfo} from the given {@link LabelSet}
	 *
	 */
	public static LabelSetInfo build(LabelSet labelSet) {
		return new LabelSetInfo(labelSet);
	}

}
