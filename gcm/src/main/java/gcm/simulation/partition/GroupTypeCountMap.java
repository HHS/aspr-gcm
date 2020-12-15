package gcm.simulation.partition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gcm.scenario.GroupTypeId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

@Immutable
public final class GroupTypeCountMap {

	private GroupTypeCountMap() {

	}

	/**
	 * Follows the requirements of equals()
	 */
	@Override
	public int hashCode() {
		int result = 1;
		for (GroupTypeId groupTypeId : map.keySet()) {
			Integer value = map.get(groupTypeId);
			if (value.intValue() != 0) {
				result += value.hashCode();
			}
		}
		return result;
	}

	/**
	 * Returns an unmodifiable set of the {@link GroupTypeId} values contained
	 * in this {@link GroupTypeCountMap}
	 * 
	 */
	public Set<GroupTypeId> getGroupTypeIds() {
		return Collections.unmodifiableSet(map.keySet());
	}

	/**
	 * Two {@link GroupTypeCountMap} objects are considered equal if the
	 * POSITIVE values associated with their group type ids are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupTypeCountMap other = (GroupTypeCountMap) obj;
		for (GroupTypeId groupTypeId : map.keySet()) {
			int value = map.get(groupTypeId);
			if (value > 0) {
				int groupCount = other.getGroupCount(groupTypeId);
				if (value != groupCount) {
					return false;
				}
			}
		}
		for (GroupTypeId groupTypeId : other.map.keySet()) {
			int value = other.map.get(groupTypeId);
			if (value > 0) {
				int groupCount = getGroupCount(groupTypeId);
				if (value != groupCount) {
					return false;
				}
			}
		}
		return true;
	}

	private final Map<GroupTypeId, Integer> map = new LinkedHashMap<>();

	public int getGroupCount(GroupTypeId groupTypeId) {
		Integer result = map.get(groupTypeId);
		if (result == null) {
			result = 0;
		}
		return result;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Source(status = TestStatus.REQUIRED, proxy = GroupTypeCountMap.class)
	public static class Builder {
		private GroupTypeCountMap groupTypeCountMap = new GroupTypeCountMap();

		private Builder() {
		}

		public GroupTypeCountMap build() {
			try {
				return groupTypeCountMap;
			} finally {
				groupTypeCountMap = new GroupTypeCountMap();
			}
		}

		/**
		 * Sets the count for the given group type id
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if groupTypeId is null
		 *             <li>if the count is negative
		 * 
		 */
		public Builder setCount(GroupTypeId groupTypeId, int count) {
			if (groupTypeId == null) {
				throw new IllegalArgumentException("null group type id");
			}
			if (count < 0) {
				throw new IllegalArgumentException("negative count");
			}
			groupTypeCountMap.map.put(groupTypeId, count);
			return this;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GroupTypeCountMap [");
		boolean first = true;
		for(GroupTypeId groupTypeId : map.keySet()) {
			if(first) {
				first = false;
			}else {
				sb.append(", ");
			}
			sb.append(groupTypeId);
			sb.append("=");
			sb.append(map.get(groupTypeId));			
		}
		sb.append("]");
		return sb.toString();
	}

}
