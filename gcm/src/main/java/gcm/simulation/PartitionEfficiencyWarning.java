package gcm.simulation;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.simulation.partition.FilterInfo;
import gcm.simulation.partition.PartitionManagerImpl;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = PartitionManagerImpl.class)
public final class PartitionEfficiencyWarning {

	private final Object partitionId;
	private final FilterInfo filterInfo;
	private final Set<Object> attributes;

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Builder() {
			
		}
		private Scaffold scaffold = new Scaffold();

		public Builder setPartitionId(Object partitionId) {
			scaffold.partitionId = partitionId;
			return this;
		}

		public Builder setFilterInfo(FilterInfo filterInfo) {
			scaffold.filterInfo = filterInfo;
			return this;
		}

		public Builder addAttribute(Object attribute) {
			scaffold.attributes.add(attribute);
			return this;
		}

		public PartitionEfficiencyWarning build() {
			try {
				return new PartitionEfficiencyWarning(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	public Object getPartitionId() {
		return partitionId;
	}

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	public Set<Object> getAttributes() {
		return new LinkedHashSet<>(attributes);
	}

	private static class Scaffold {
		private Object partitionId;
		private FilterInfo filterInfo;
		private Set<Object> attributes = new LinkedHashSet<>();

	}

	private PartitionEfficiencyWarning(Scaffold scaffold) {
		this.partitionId = scaffold.partitionId;
		this.filterInfo = scaffold.filterInfo;
		this.attributes = new LinkedHashSet<>(scaffold.attributes);
	}

}
