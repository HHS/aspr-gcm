package gcm.manual.pp;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.scenario.PersonPropertyId;

public final class PopulationPartitionQuery {

	private Object compartmentLabel;
	private Object regionLabel;
	private Map<PersonPropertyId,Object> personPropertyLabels;

	public Object getCompartmentLabel() {
		return compartmentLabel;
	}
	
	public Object getPersonPropertyLabel(PersonPropertyId personPropertyId) {
		return personPropertyLabels.get(personPropertyId);
	}
	
	public Set<PersonPropertyId> getPersonPropertyIds(){
		return new LinkedHashSet<>(personPropertyLabels.keySet());
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
		
		public void setPersonPropertyLabel(PersonPropertyId personPropertyId, Object personPropertyLabel) {
			populationPartitionQuery.personPropertyLabels.put(personPropertyId, personPropertyLabel);
		}

	}

}
