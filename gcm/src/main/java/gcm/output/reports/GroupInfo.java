package gcm.output.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the primary information that was associated with a group just
 * prior to its removal from the simulation. It is designed to be used by
 * {@link Report} classes that need insight into a group after the group has
 * been removed. Construction is conducted via the contained Builder class.
 * 
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
@Immutable
public final class GroupInfo {
	private final Scaffold scaffold;

	private static class Scaffold {
		private GroupId groupId;
		private GroupTypeId groupTypeId;
		private Map<GroupPropertyId, Object> propertyValues = new LinkedHashMap<>();
		private Set<PersonId> people = new LinkedHashSet<>();
	}

	/**
	 * Returns the id of the removed group *
	 */
	public GroupId getGroupId() {
		return scaffold.groupId;
	}

	/**
	 * Returns the group Type of the group that was removed.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupTypeId() {
		return (T) scaffold.groupTypeId;
	}

	/**
	 * Returns the people who were associated with the group when it was removed
	 */
	public List<PersonId> getPeople() {
		return new ArrayList<>(scaffold.people);
	}

	/**
	 * Returns a map of the person property values of the person when they were
	 * removed from the simulation
	 * 
	 */
	public Map<GroupPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupInfo [groupId=");
		builder.append(scaffold.groupId);
		builder.append(", groupTypeId=");
		builder.append(scaffold.groupTypeId);
		builder.append(", propertyValues=");
		builder.append(scaffold.propertyValues);
		builder.append(", people count =");
		builder.append(scaffold.people.size());
		builder.append("]");
		return builder.toString();
	}

	/*
	 * Hidden constructor
	 */
	private GroupInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	@NotThreadSafe
	public static class GroupInfoBuilder {
		private Scaffold scaffold = new Scaffold();

		private void validate() {
			if (scaffold.groupId == null) {
				throw new RuntimeException("null group id");
			}
			if (scaffold.groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
		}

		/**
		 * Builds the {@link GroupInfo} from the collected data
		 * 
		 * @throws RuntimeException
		 *             <li>if no group id was collected
		 *             <li>if no group type id was collected
		 */
		public GroupInfo build() {
			try {
				validate();
				return new GroupInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the group id
		 * 
		 * @throws RuntimeException
		 *             if the group id is null
		 */
		public void setGroupId(GroupId groupId) {
			if (groupId == null) {
				throw new RuntimeException("null group id");
			}
			scaffold.groupId = groupId;
		}

		/**
		 * Sets the group type id
		 * 
		 * @throws RuntimeException
		 *             if the group type id is null
		 */
		public void setGroupTypeId(GroupTypeId groupTypeId) {
			if (groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
			scaffold.groupTypeId = groupTypeId;
		}

		/**
		 * Sets the group property value.
		 * 
		 * @throws RuntimeException
		 *             <li>if the group property id is null
		 *             <li>if the group property value is null
		 */
		public void setGroupPropertyValue(GroupPropertyId groupPropertyId, Object groupPropertyValue) {
			if (groupPropertyId == null) {
				throw new RuntimeException("null group property id");
			}
			if (groupPropertyValue == null) {
				throw new RuntimeException("null group property value");
			}
			scaffold.propertyValues.put(groupPropertyId, groupPropertyValue);
		}

		public void addPerson(PersonId personId) {
			if (personId == null) {
				throw new RuntimeException("null person id");
			}
			scaffold.people.add(personId);
		}

	}

}
