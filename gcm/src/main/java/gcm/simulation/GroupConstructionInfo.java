package gcm.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the information to fully specify a group, but not its relationship
 * to people.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
@Immutable
public final class GroupConstructionInfo {
	private final Scaffold scaffold;

	private static class Scaffold {		
		private GroupTypeId groupTypeId;
		private Map<GroupPropertyId, Object> propertyValues = new LinkedHashMap<>();
	}

	

	/**
	 * Returns the group Type of the group
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupTypeId() {
		return (T) scaffold.groupTypeId;
	}

	

	/**
	 * Returns a map of the person property values of the person
	 * 
	 */
	public Map<GroupPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupConstructionInfo [groupTypeId=");
		builder.append(scaffold.groupTypeId);
		builder.append(", propertyValues=");
		builder.append(scaffold.propertyValues);		
		builder.append("]");
		return builder.toString();
	}

	/*
	 * Hidden constructor
	 */
	private GroupConstructionInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	@Source(status = TestStatus.UNEXPECTED, proxy = GroupConstructionInfo.class)
	@NotThreadSafe
	public static class Builder {
		
		private Builder() {}
		
		private Scaffold scaffold = new Scaffold();

		private void validate() {			
			if (scaffold.groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
		}

		/**
		 * Builds the {@link GroupConstructionInfo} from the collected data
		 * 
		 * @throws RuntimeException                         
		 *                          <li>if no group type id was collected
		 */
		public GroupConstructionInfo build() {
			try {
				validate();
				return new GroupConstructionInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		

		/**
		 * Sets the group type id
		 * 
		 * @throws RuntimeException if the group type id is null
		 */
		public Builder setGroupTypeId(GroupTypeId groupTypeId) {
			if (groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
			scaffold.groupTypeId = groupTypeId;
			return this;
		}

		/**
		 * Sets the group property value.
		 * 
		 * @throws RuntimeException
		 *                          <li>if the group property id is null
		 *                          <li>if the group property value is null
		 */
		public Builder setGroupPropertyValue(GroupPropertyId groupPropertyId, Object groupPropertyValue) {
			if (groupPropertyId == null) {
				throw new RuntimeException("null group property id");
			}
			if (groupPropertyValue == null) {
				throw new RuntimeException("null group property value");
			}
			scaffold.propertyValues.put(groupPropertyId, groupPropertyValue);
			return this;
		}

		
	}

}
