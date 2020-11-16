package gcm.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gcm.scenario.BatchPropertyId;
import gcm.scenario.MaterialId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;

/**
 * Represents the information to fully specify a batch, but not its relationship
 * to stages
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
@Immutable
public class BatchConstructionInfo {
	
	private BatchConstructionInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}
	
	private final Scaffold scaffold;

	private static class Scaffold {
		MaterialId materialId;
		double amount;
		Map<BatchPropertyId, Object> propertyValues = new LinkedHashMap<>();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		private Scaffold scaffold = new Scaffold();
		private Builder() {}
		public BatchConstructionInfo build() {
			try {
				return new BatchConstructionInfo(scaffold);
			}finally {
				scaffold = new Scaffold();
			}
		}
		
		/**
		 * Sets the amount;
		 * 
		 * @throws RuntimeException 
		 * <li> if the amount is negative
		 * <li> if the amount is not finite
		 */
		public Builder setAmount(double amount){
			if(amount<0) {
				throw new RuntimeException("negative amount");
			}
			if(!Double.isFinite(amount)) {
				throw new RuntimeException("infinite amount");
			}
			scaffold.amount = amount;			
			return this;
		}
		
		/**
		 * Sets the amount;
		 * 
		 * @throws RuntimeException 
		 * <li> if the materialId is null
		 */
		public Builder setMaterialId(MaterialId materialId){
			if(materialId == null) {
				throw new RuntimeException("null material id");
			}
			scaffold.materialId = materialId;			
			return this;
		}
		
		/**
		 * Sets the amount;
		 * 
		 * @throws RuntimeException 
		 * <li> if the batchPropertyId is null
		 * <li> if the propertyValue is null
		 */
		public Builder setPropertyValue(BatchPropertyId batchPropertyId, Object propertyValue){
			if(batchPropertyId == null) {
				throw new RuntimeException("null batch property id");
			}
			if(propertyValue == null) {
				throw new RuntimeException("null property value");
			}
			scaffold.propertyValues.put(batchPropertyId,propertyValue);			
			return this;
		}
	}

	
	/**
	 * Return the material id of the new batch
	 */
	public MaterialId getMaterialId() {
		return scaffold.materialId;
	}
	/**
	 * Returns the amount of the new batch
	 */
	public double getAmount() {
		return scaffold.amount;
	}
	
	/**
	 * Returns a map of the batch property values of the new batch
	 */
	public Map<BatchPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

}
