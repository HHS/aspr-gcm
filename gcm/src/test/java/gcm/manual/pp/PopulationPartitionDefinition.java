package gcm.manual.pp;

import java.util.function.Function;

import gcm.scenario.CompartmentId;
import gcm.scenario.RegionId;

public final class PopulationPartitionDefinition {
	
	private PopulationPartitionDefinition() {}
	
	private Function<CompartmentId, Object> compartmentPartitionFunction;
	
	private Function<RegionId, Object> regionPartitionFunction;
	
	public static Builder builder() {
		return new Builder();
	}
	
	public Function<CompartmentId, Object> getCompartmentPartitionFunction() {
		return compartmentPartitionFunction;
	}

	public Function<RegionId, Object> getRegionPartitionFunction() {
		return regionPartitionFunction;
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
		
	}	
}