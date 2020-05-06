package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.BatchId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.StageId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor for MaterialsManager
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class MaterialsManagerImpl extends BaseElement implements MaterialsManager {

	/*
	 * Represents the batch
	 */
	private static class BatchRecord {
		
		private final BatchId batchId;
		/*
		 * The stage on which this batch is staged -- may be null
		 */
		private StageRecord stageRecord;
		/*
		 * The non-negative amount of this batch
		 */
		private double amount;
		/*
		 * The time when this batch was created
		 */
		private double creationTime;
		/*
		 * The non-null material for this batch
		 */
		private MaterialId materialId;
		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;

		private BatchRecord(final int index) {
			batchId = new BatchId(index);
		}
	}

	/*
	 * Represents the materials producer
	 */
	private static class MaterialsProducerRecord {

		/*
		 * Identifier for the materials producer
		 */
		private MaterialsProducerId materialProducerId;

		/*
		 * Those batches owned by this materials producer that are not staged
		 */
		private final Set<BatchRecord> inventory = new LinkedHashSet<>();

		/*
		 * Those batches owned by this materials producer that are staged
		 */
		private final Set<StageRecord> stageRecords = new LinkedHashSet<>();

	}

	/*
	 * Represents the stage
	 */
	private static class StageRecord {
		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;

		private final StageId stageId;
		/*
		 * Flag marking that the stage has been offered up to other components.
		 * While true, this stage and its batches are immutable.
		 */
		private boolean offered;
		/*
		 * The set of batches that are staged on this stage
		 */
		private final Set<BatchRecord> batchRecords = new LinkedHashSet<>();

		private StageRecord(final int index) {
			stageId = new StageId(index);
		}
	}

	private EventManager eventManager;

	/*
	 * The identifier for the next created batch
	 */
	private int nextBatchRecordId;

	/*
	 * The identifier for the next created stage
	 */
	private int nextStageRecordId;

	/*
	 * <batch id, batch record>
	 */
	private final Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	/*
	 * <stage id, stage record>
	 */
	private final Map<StageId, StageRecord> stageRecords = new LinkedHashMap<>();

	/*
	 * <Materials Producer id, Materials Producer Record>
	 */
	private final Map<MaterialsProducerId, MaterialsProducerRecord> materialsProducerMap = new LinkedHashMap<>();

	@Override
	public boolean batchExists(final BatchId batchId) {
		return batchRecords.containsKey(batchId);
	}

	@Override
	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount) {

		final StageRecord stageRecord = stageRecords.get(stageId);

		/*
		 * Remove the stage and batches from our tracking system
		 */

		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			batchRecords.remove(batchRecord.batchId);
		}
		stageRecords.remove(stageId);
		stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);

		// create the new batch record
		if(!Double.isFinite(amount)){
			throw new RuntimeException("batch amount is not finite");
		}
		
		final BatchRecord batchRecord = new BatchRecord(nextBatchRecordId++);
		batchRecord.materialId = materialId;
		batchRecord.amount = amount;
		batchRecord.creationTime = eventManager.getTime();
		batchRecord.materialsProducerRecord = stageRecord.materialsProducerRecord;
		batchRecords.put(batchRecord.batchId, batchRecord);
		

		/*
		 * Clear the mutation type state for error reporting
		 */

		return batchRecord.batchId;
	}

	@Override
	public BatchId createBatch(final MaterialsProducerId materialsProducerId, final MaterialId materialId, final double amount) {

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);

		/*
		 * Create the batch and place it in the inventory of the materials
		 * producer.
		 */
		if(!Double.isFinite(amount)){
			throw new RuntimeException("batch amount is not finite");
		}

		
		final BatchRecord batchRecord = new BatchRecord(nextBatchRecordId++);
		batchRecord.amount = amount;
		batchRecord.creationTime = eventManager.getTime();
		batchRecord.materialId = materialId;
		batchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(batchRecord);
		batchRecords.put(batchRecord.batchId, batchRecord);

		return batchRecord.batchId;
	}

	@Override
	public StageId createStage(final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final StageRecord stageRecord = new StageRecord(nextStageRecordId++);
		stageRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.stageRecords.add(stageRecord);
		stageRecords.put(stageRecord.stageId, stageRecord);
		return stageRecord.stageId;
	}

	@Override
	public void destroyBatch(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);

		/*
		 * Remove the batch from its stage and the master batch tracking map
		 */
		if (batchRecord.stageRecord != null) {
			batchRecord.stageRecord.batchRecords.remove(batchRecord);
		}
		batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
		batchRecords.remove(batchId);

	}

	@Override
	public void destroyStage(final StageId stageId, final boolean destroyBatches) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		/*
		 * Remove the stage
		 */

		if (destroyBatches) {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchRecords.remove(batchRecord.batchId);
			}
		} else {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchRecord.stageRecord = null;
			}
		}
		stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
		stageRecords.remove(stageId);

	}

	@Override
	public double getBatchAmount(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.amount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return (T) batchRecord.materialId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MaterialsProducerId> T getBatchProducer(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);

		return (T) batchRecord.materialsProducerRecord.materialProducerId;
	}

	@Override
	public StageId getBatchStageId(final BatchId batchId) {

		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord == null) {
			return null;
		}
		return batchRecord.stageRecord.stageId;
	}

	@Override
	public double getBatchTime(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.creationTime;
	}

	@Override
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);

		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : materialsProducerRecord.inventory) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	@Override
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId) {

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : materialsProducerRecord.inventory) {
			if (batchRecord.materialId.equals(materialId)) {
				result.add(batchRecord.batchId);
			}
		}
		return result;
	}

	@Override
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {

		final List<StageId> result = new ArrayList<>();
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		for (final StageRecord stageRecord : materialsProducerRecord.stageRecords) {
			if (stageRecord.offered) {
				result.add(stageRecord.stageId);
			}
		}
		return result;
	}

	@Override
	public List<BatchId> getStageBatches(final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	@Override
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			if (batchRecord.materialId.equals(materialId)) {
				result.add(batchRecord.batchId);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		return (T) stageRecord.materialsProducerRecord.materialProducerId;
	}

	@Override
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		final List<StageId> result = new ArrayList<>();
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);

		for (final StageRecord stageRecord : materialsProducerRecord.stageRecords) {
			result.add(stageRecord.stageId);
		}
		return result;
	}

	@Override
	public void init(final Context context) {
		super.init(context);
		eventManager = context.getEventManager();

		/*
		 * Initialize the materials producer records
		 */
		for (final MaterialsProducerId materialsProducerId : context.getScenario().getMaterialsProducerIds()) {
			final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
			materialsProducerRecord.materialProducerId = materialsProducerId;
			materialsProducerMap.put(materialsProducerId, materialsProducerRecord);
		}
	}

	@Override
	public boolean isStageOffered(final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		return stageRecord.offered;
	}

	@Override
	public void moveBatchToInventory(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		final StageRecord stageRecord = batchRecord.stageRecord;
		if (stageRecord.offered) {
			throw new RuntimeException("batch cannot be moved to inventory when its stage is offered");
		}
		stageRecord.batchRecords.remove(batchRecord);
		batchRecord.stageRecord = null;
		batchRecord.materialsProducerRecord.inventory.add(batchRecord);
	}

	@Override
	public void moveBatchToStage(final BatchId batchId, final StageId stageId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		batchRecord.stageRecord = stageRecord;
		stageRecord.batchRecords.add(batchRecord);
		batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
	}

	@Override
	public void setStageOffer(final StageId stageId, final boolean offer) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.offered = offer;
	}

	@Override
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		final BatchRecord sourceBatchRecord = batchRecords.get(sourceBatchId);
		final BatchRecord destinationBatchRecord = batchRecords.get(destinationBatchId);
		if(!Double.isFinite(amount)){
			throw new RuntimeException("batch amount is not finite");
		}		
		sourceBatchRecord.amount -= amount;
		destinationBatchRecord.amount += amount;	
		
		if(!Double.isFinite(destinationBatchRecord.amount)){
			throw new RuntimeException("batch amount is not finite");
		}
	}

	@Override
	public boolean stageExists(final StageId stageId) {
		return stageRecords.containsKey(stageId);
	}

	@Override
	public void transferOfferedStageToMaterialsProducer(final MaterialsProducerId newMaterialsProducerId, final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);

		final MaterialsProducerRecord currentMaterialsProducerRecord = stageRecord.materialsProducerRecord;

		final MaterialsProducerRecord newMaterialsProducerRecord = materialsProducerMap.get(newMaterialsProducerId);

		currentMaterialsProducerRecord.stageRecords.remove(stageRecord);

		newMaterialsProducerRecord.stageRecords.add(stageRecord);

		stageRecord.materialsProducerRecord = newMaterialsProducerRecord;

		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			batchRecord.materialsProducerRecord = newMaterialsProducerRecord;
		}

		stageRecord.offered = false;
	}

}
