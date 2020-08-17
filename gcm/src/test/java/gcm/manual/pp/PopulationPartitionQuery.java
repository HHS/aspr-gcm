package gcm.manual.pp;

public final class PopulationPartitionQuery {

	private Object compartmentLabel;
	private Object regionLabel;

	public Object getCompartmentLabel() {
		return compartmentLabel;
	}

	public Object getRegionLabel() {
		return regionLabel;
	}

	private PopulationPartitionQuery() {

	}

	public Builder builder() {
		return new Builder();

	}

	public final static class Builder {
		private PopulationPartitionQuery populationPartitionQuery = new PopulationPartitionQuery();

		public PopulationPartitionQuery build() {
			try {
				return populationPartitionQuery;
			} finally {
				populationPartitionQuery = new PopulationPartitionQuery();
			}

		}

		public void setCompartmentLabel(Object compartmentLabel) {
			populationPartitionQuery.compartmentLabel = compartmentLabel;
		}

		public void setRegionLabel(Object regionLabel) {
			populationPartitionQuery.regionLabel = regionLabel;
		}

	}

}
