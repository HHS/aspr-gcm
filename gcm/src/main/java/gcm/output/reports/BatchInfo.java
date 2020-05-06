package gcm.output.reports;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the primary information that was associated with a batch just
 * prior to its removal from the simulation. It is designed to be used by
 * {@link Report} classes that need insight into a batch after the batch has
 * been removed. Construction is conducted via the contained Builder class.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
@Immutable
public final class BatchInfo {

	/**
	 * A container for the collected information on the deleted batch
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class Scaffold {
		private BatchId batchId;
		private StageId stageId;
		private MaterialId materialId;
		private MaterialsProducerId materialsProducerId;
		private double amount;
		private double creationTime;
		private Map<BatchPropertyId, Object> propertyValueMap = new LinkedHashMap<>();
	}

	/**
	 * A builder class for {@link BatchInfo}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	@NotThreadSafe
	public static class BatchInfoBuilder {

		/*
		 * Validate the collected data for the batch and throws
		 * RuntimeExceptions if invalid data found. Only the stage id is allowed
		 * to be null and both the amount and creation times must not be
		 * negative.
		 */
		private void validate() {
			if (scaffold.batchId == null) {
				throw new RuntimeException("null batch id");
			}
			if (scaffold.materialId == null) {
				throw new RuntimeException("null material id");
			}
			if (scaffold.materialsProducerId == null) {
				throw new RuntimeException("null materials producer id");
			}
			if (scaffold.amount < 0) {
				throw new RuntimeException("negative amount");
			}
			if (scaffold.creationTime < 0) {
				throw new RuntimeException("negative creation time");
			}

		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Builds the {@link BatchInfo} from the gathered data.
		 * 
		 * @throws RuntimeException
		 *             <li>if the batchId is null
		 *             <li>if the materialId is null
		 *             <li>if the materialsProducerId is null
		 *             <li>if the amount is negative
		 *             <li>if the creationTime is negative
		 */
		public BatchInfo build() {
			try {
				validate();
				return new BatchInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the batch id. Required and must not be null.
		 * 
		 * @param batchId
		 *            the batch id for the deleted batch
		 */
		public void setBatchId(BatchId batchId) {
			scaffold.batchId = batchId;
		}

		/**
		 * Sets the materials producer id. Required and must not be null.
		 * 
		 * @param materialsProducerId
		 *            the materials producer id for the deleted batch
		 */
		public void setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			scaffold.materialsProducerId = materialsProducerId;
		}

		/**
		 * Sets the stage id. Null is acceptable.
		 * 
		 * @param stageId
		 *            the stage id for the deleted batch
		 */
		public void setStageId(StageId stageId) {
			scaffold.stageId = stageId;
		}

		/**
		 * Sets the material id. Required and must not be null.
		 * 
		 * @param materialId
		 *            the material id for the deleted batch
		 */
		public void setMaterialId(MaterialId materialId) {
			scaffold.materialId = materialId;
		}

		/**
		 * Sets the amount. Must not be negative.
		 * 
		 * @param amount
		 *            the amount of material in the deleted batch
		 */
		public void setAmount(double amount) {
			scaffold.amount = amount;
		}

		/**
		 * Sets the creation time. Must not be negative.
		 * 
		 * @param creationTime
		 *            the creation time for the deleted batch
		 */
		public void setCreationTime(double creationTime) {
			scaffold.creationTime = creationTime;
		}

		/**
		 * Sets the property value for the given batch property id.
		 *
		 * @param batchPropertyId
		 *            a batch property id for the deleted batch
		 * @param batchPropertyValue
		 *            the batch property for the batch property id
		 * 
		 * @throws RuntimeException
		 *             <li>if the batchPropertyId is null
		 *             <li>if the batchPropertyValue is null
		 */
		public void setPropertyValue(BatchPropertyId batchPropertyId, Object batchPropertyValue) {
			if (batchPropertyId == null) {
				throw new RuntimeException("null batch property id");
			}

			if (batchPropertyValue == null) {
				throw new RuntimeException("null batch property value");
			}

			scaffold.propertyValueMap.put(batchPropertyId, batchPropertyValue);
		}

	}

	/**
	 * Returns the batch id for the batch
	 */
	public BatchId getBatchId() {
		return scaffold.batchId;
	}

	/**
	 * Returns the stage id for the batch
	 */
	public StageId getStageId() {
		return scaffold.stageId;
	}

	/**
	 * Returns the material id for the batch
	 */
	public MaterialId getMaterialId() {
		return scaffold.materialId;
	}

	/**
	 * Returns the materials producer id for the batch
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return scaffold.materialsProducerId;
	}

	/**
	 * Returns the material amount for the batch
	 */
	public double getAmount() {
		return scaffold.amount;
	}

	/**
	 * Returns the creation time for the batch
	 */
	public double getCreationTime() {
		return scaffold.creationTime;
	}

	/**
	 * Returns the property values for the batch
	 */
	public Map<BatchPropertyId, Object> getPropertyValueMap() {
		return Collections.unmodifiableMap(scaffold.propertyValueMap);
	}

	private final Scaffold scaffold;

	private BatchInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

}