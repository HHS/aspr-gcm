package gcm.manual.pp;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;

public final class PopulationPartitionDefinition {
	
	private PopulationPartitionDefinition() {}
	
	private Function<CompartmentId, Object> compartmentPartitionFunction;
	
	private Function<RegionId, Object> regionPartitionFunction;
	
	private Map<PersonPropertyId,Function<Object, Object>> personPropertyPartitionFunctions = new LinkedHashMap<>();
	
	public static Builder builder() {
		return new Builder();
	}
	
	public Function<CompartmentId, Object> getCompartmentPartitionFunction() {
		return compartmentPartitionFunction;
	}

	public Function<RegionId, Object> getRegionPartitionFunction() {
		return regionPartitionFunction;
	}
	
	public Function<Object, Object> getPersonPropertyPartitionFunction(PersonPropertyId personPropertyId) {
		return personPropertyPartitionFunctions.get(personPropertyId);
	}
	
	public Set<PersonPropertyId> getPersonPropertyIds(){
		return new LinkedHashSet<>(personPropertyPartitionFunctions.keySet());
	}

	public static final class Builder{
		
		private PopulationPartitionDefinition populationPartitionDefinition = new PopulationPartitionDefinition();
		
		//hidden constructor
		private Builder() {
			
		}
		
		public PopulationPartitionDefinition build() {
			try {
				return populationPartitionDefinition;
			}finally {
				populationPartitionDefinition = new PopulationPartitionDefinition();
			}
		}
		
		public Builder setCompartmentPartition(Function<CompartmentId, Object> compartmentPartitionFunction) {
			populationPartitionDefinition.compartmentPartitionFunction = compartmentPartitionFunction;
			return this;
		}

		public Builder setRegionPartition(Function<RegionId, Object> regionPartitionFunction) {
			populationPartitionDefinition.regionPartitionFunction = regionPartitionFunction;
			return this;
		}
		
		public Builder setPersonPropertyPartition(PersonPropertyId personPropertyId,Function<Object, Object> personPropertyPartitionFunction) {
			populationPartitionDefinition.personPropertyPartitionFunctions.put(personPropertyId,personPropertyPartitionFunction);
			return this;
		}
		
	}	
}