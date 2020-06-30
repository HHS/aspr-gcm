package gcm.simulation;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.REQUIRED, proxy = IndexedPopulationManagerImpl.class)
public final class PopulationIndexEfficiencyWarning {

	private final Object populationIndexId;
	private final FilterInfo filterInfo;
	private final Set<Object> attributes;

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Builder() {
			
		}
		private Scaffold scaffold = new Scaffold();

		public Builder setPopulationIndexId(Object populationIndexId) {
			scaffold.populationIndexId = populationIndexId;
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

		public PopulationIndexEfficiencyWarning build() {
			try {
				return new PopulationIndexEfficiencyWarning(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	public Object getPopulationIndexId() {
		return populationIndexId;
	}

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}

	public Set<Object> getAttributes() {
		return new LinkedHashSet<>(attributes);
	}

	private static class Scaffold {
		private Object populationIndexId;
		private FilterInfo filterInfo;
		private Set<Object> attributes = new LinkedHashSet<>();

	}

	private PopulationIndexEfficiencyWarning(Scaffold scaffold) {
		this.populationIndexId = scaffold.populationIndexId;
		this.filterInfo = scaffold.filterInfo;
		this.attributes = new LinkedHashSet<>(scaffold.attributes);
	}

}
