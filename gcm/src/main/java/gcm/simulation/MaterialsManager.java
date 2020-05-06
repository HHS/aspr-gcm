package gcm.simulation;

import java.util.List;

import gcm.scenario.BatchId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;

/**
 * General manager for all material activities.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface MaterialsManager extends Element {

	/**
	 * Returns true if and only if the batch exists.
	 * 
	 * @param batchId
	 *            cannot be null
	 */
	public boolean batchExists(final BatchId batchId);

	/**
	 * Returns the creation time for the batch.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 */
	public double getBatchTime(final BatchId batchId);

	/**
	 * Returns the amount of material in the batch.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 */
	public double getBatchAmount(final BatchId batchId);

	/**
	 * Returns the type of material in the batch.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 */
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId);

	/**
	 * Returns the stage id the batch. Returns null if the batch is not in a
	 * stage.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 */
	public StageId getBatchStageId(final BatchId batchId);

	/**
	 * Returns true if and only if the stage exists.
	 */
	public boolean stageExists(final StageId stageId);

	/**
	 * Returns true if and only if the stage is offered.
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 */
	public boolean isStageOffered(final StageId stageId);

	/**
	 * Returns as a list the set of stage ids owned by the material producer.
	 * 
	 * 
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 */
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns as a list the set of stage ids owned by the material producer
	 * where the stage is being offered.
	 * 
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 */
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId);

	/**
	 * Sets the offer state of the stage.
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 */
	public void setStageOffer(final StageId stageId, final boolean offer);

	/**
	 * Returns as a list the set of batch ids matching the stage and material
	 * type.
	 * 
	 *
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 * 
	 * @param materialId
	 *            <li>cannot be null
	 *            <li>must be a contained materialId
	 */
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId);

	/**
	 * Returns as a list the set of batch ids matching the stage .
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 */
	public List<BatchId> getStageBatches(final StageId stageId);

	/**
	 * Returns as a list the set of batch ids matching the materials producer
	 * and material id where the batches are not staged.
	 * 
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 * 
	 * @param materialId
	 *            <li>cannot be null
	 *            <li>must be a contained materialId
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId);

	/**
	 * Returns as a list the set of batch ids matching the materials producer
	 * where the batches are not staged.
	 * 
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 * 
	 */
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId);

	/**
	 * Returns the materials producer id for the stage.
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 * 
	 */
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId);

	/**
	 * Moves the stage to the materials producer and sets the offer state of the
	 * stage to false.
	 * 
	 * @param newMaterialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 * 
	 */
	public void transferOfferedStageToMaterialsProducer(final MaterialsProducerId newMaterialsProducerId, final StageId stageId);

	/**
	 * Moves the batch to its materials producer's inventory.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 */
	public void moveBatchToInventory(final BatchId batchId);

	/**
	 * Moves the batch to its materials producer's stage.
	 * 
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 * 
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 */
	public void moveBatchToStage(final BatchId batchId, final StageId stageId);

	/**
	 * Returns the materials producer identifier of the batch
	 *
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 *
	 */
	public <T extends MaterialsProducerId> T getBatchProducer(final BatchId batchId);

	/**
	 * Transfers the given amount from one batch to another
	 * 
	 * @param sourceBatchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 * 
	 * @param destinationBatchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 * 
	 * @param amount
	 *            <li>should be non-negative
	 */
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount);

	/**
	 * Returns the batch id of a new created batch that is stored in the
	 * inventory of the materials producer.
	 *
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 * 
	 * @param materialId
	 *            <li>cannot be null
	 *            <li>must be a contained materialId
	 * 
	 * @param amount
	 *            <li>should be non-negative
	 */
	public BatchId createBatch(MaterialsProducerId materialsProducerId, final MaterialId materialId, final double amount);

	/**
	 * Destroys the indicated batch that is owned by the invoking materials
	 * producer. The batch may not be part of an offered stage.
	 *
	 * @param batchId
	 *            <li>cannot be null
	 *            <li>must be a contained batchId
	 *
	 */
	public void destroyBatch(final BatchId batchId);

	/**
	 * Creates a new stage owned by the materials producer and returns the
	 * stage's id.
	 *
	 * @param materialsProducerId
	 *            <li>cannot be null
	 *            <li>must be a contained materialsProducerId
	 *
	 */

	public StageId createStage(MaterialsProducerId materialsProducerId);

	/**
	 * Destroys the stage owned by the materials producer component. If
	 * destroyBatches is set to true, then all batches associated with the stage
	 * are also destroyed, otherwise they are returned to inventory.
	 *
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 *
	 */

	public void destroyStage(final StageId stageId, final boolean destroyBatches);

	/**
	 * Converts a stage to a batch that will be held in the inventory of the
	 * invoking materials producer. The stage and its associated batches are
	 * destroyed. Returns the new created batch's id. The stage must be owned by
	 * the invoking materials producer and must not be in the offered state.
	 *
	 * @param stageId
	 *            <li>cannot be null
	 *            <li>must be a contained stageId
	 *
	 * @param materialId
	 *            <li>cannot be null
	 *            <li>must be a contained materialId
	 * 
	 * @param amount
	 *            <li>should be non-negative
	 */

	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount);

}
