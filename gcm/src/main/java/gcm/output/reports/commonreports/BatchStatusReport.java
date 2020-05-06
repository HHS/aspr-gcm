package gcm.output.reports.commonreports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.BatchInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.StageInfo;
import gcm.output.reports.StateChange;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.ResourceId;
import gcm.scenario.StageId;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A Report that displays the state of batches over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when batch state was updated
 *
 * Batch -- the batch identifier
 *
 * Stage -- the stage associated with the batch
 *
 * MaterialsProducer -- the materials producer of the owner of the batch
 * 
 * Offered -- the offered state of the batch
 * 
 * Material -- the material of the batch
 * 
 * Amount -- the amount of material in the batch
 * 
 * Material.PropertyId -- multiple columns for the batch properties selected for
 * the report
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class BatchStatusReport extends AbstractReport {
	
	private Set<BatchId> updatedBatches = new LinkedHashSet<>();

	private double lastFlushTime = -1;

	private Map<MaterialId, Set<BatchPropertyId>> batchPropertyMap = new LinkedHashMap<>();

	/*
	 * Flushes the updated batches if the last flush time was in the past
	 */
	private void flushOnTimeChange(ObservableEnvironment observableEnvironment) {
		double currentTime = observableEnvironment.getTime();
		if (currentTime > lastFlushTime) {
			flush(observableEnvironment);
			lastFlushTime = currentTime;
		}
	}

	/*
	 * Releases a report item for each updated batch that still exists
	 */
	private void flush(ObservableEnvironment observableEnvironment) {
		for (BatchId batchId : updatedBatches) {

			if (observableEnvironment.batchExists(batchId)) {
				// report the batch - make sure batch exists

				final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
				reportItemBuilder.setReportHeader(getReportHeader(observableEnvironment));
				reportItemBuilder.setReportType(getClass());
				reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
				reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
				reportItemBuilder.addValue(lastFlushTime);
				reportItemBuilder.addValue(batchId);
				reportItemBuilder.addValue(observableEnvironment.getBatchProducer(batchId));
				StageId stageId = observableEnvironment.getBatchStageId(batchId).get();
				boolean offered = false;
				if (stageId != null) {
					offered = observableEnvironment.isStageOffered(stageId);
				}
				reportItemBuilder.addValue(offered);
				reportItemBuilder.addValue(observableEnvironment.getBatchMaterial(batchId));
				reportItemBuilder.addValue(observableEnvironment.getBatchAmount(batchId));

				for (MaterialId materialId : batchPropertyMap.keySet()) {
					boolean matchingMaterial = observableEnvironment.getBatchMaterial(batchId).equals(materialId);
					Set<BatchPropertyId> batchPropertyIds = batchPropertyMap.get(materialId);
					for (BatchPropertyId batchPropertyId : batchPropertyIds) {
						if (matchingMaterial) {
							reportItemBuilder.addValue(observableEnvironment.getBatchPropertyValue(batchId, batchPropertyId));
						} else {
							reportItemBuilder.addValue("");
						}
					}
				}
				observableEnvironment.releaseOutputItem(reportItemBuilder.build());
			}
		}
		updatedBatches.clear();

	}

	@Override
	public void close(ObservableEnvironment observableEnvironment) {
		flush(observableEnvironment);
	}

	private ReportHeader reportHeader;

	/*
	 * Returns the ReportHeader based on the batch properties selected by the
	 * client.
	 */
	private ReportHeader getReportHeader(ObservableEnvironment observableEnvironment) {
		if (reportHeader == null) {
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Batch");
			reportHeaderBuilder.add("Stage");
			reportHeaderBuilder.add("MaterialsProducer");
			reportHeaderBuilder.add("Offered");
			reportHeaderBuilder.add("Material");
			reportHeaderBuilder.add("Amount");
			Set<MaterialId> materialIds = observableEnvironment.getMaterialIds();
			for (MaterialId materialId : materialIds) {
				Set<BatchPropertyId> batchPropertyIds = observableEnvironment.getBatchPropertyIds(materialId);
				for (BatchPropertyId batchPropertyId : batchPropertyIds) {
					reportHeaderBuilder.add(materialId + "." + batchPropertyId);
				}
			}
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;

	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		result.add(StateChange.BATCH_CREATION);
		result.add(StateChange.BATCH_DESTRUCTION);
		result.add(StateChange.BATCH_SHIFT);
		result.add(StateChange.BATCH_STAGED);
		result.add(StateChange.BATCH_UNSTAGED);
		result.add(StateChange.STAGE_CONVERTED_TO_BATCH);
		result.add(StateChange.STAGE_CONVERTED_TO_RESOURCE);
		result.add(StateChange.STAGE_DESTRUCTION);
		result.add(StateChange.BATCH_PROPERTY_VALUE_ASSIGNMENT);
		result.add(StateChange.STAGE_OFFERED);
		result.add(StateChange.STAGE_TRANSFERRED);

		return result;
	}

	@Override
	public void handleBatchCreation(ObservableEnvironment observableEnvironment, final BatchId batchId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.add(batchId);
	}

	@Override
	public void handleBatchDestruction(ObservableEnvironment observableEnvironment, BatchInfo batchInfo) {
		flushOnTimeChange(observableEnvironment);
		writeBatchInfo(observableEnvironment, batchInfo, null);
	}

	@Override
	public void handleBatchShift(ObservableEnvironment observableEnvironment, final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.add(sourceBatchId);
		updatedBatches.add(destinationBatchId);
	}

	@Override
	public void handleStageConversionToBatch(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final BatchId batchId) {
		flushOnTimeChange(observableEnvironment);
		for (BatchInfo batchInfo : stageInfo.getBatchInfos()) {
			writeBatchInfo(observableEnvironment, batchInfo, stageInfo);
		}

	}

	@Override
	public void handleStageConversionToResource(ObservableEnvironment observableEnvironment, StageInfo stageInfo, final ResourceId resourceId, final long amount) {
		flushOnTimeChange(observableEnvironment);
		for (BatchInfo batchInfo : stageInfo.getBatchInfos()) {
			writeBatchInfo(observableEnvironment, batchInfo, stageInfo);
		}
	}

	@Override
	public void handleStageDestruction(ObservableEnvironment observableEnvironment, StageInfo stageInfo) {
		flushOnTimeChange(observableEnvironment);
		List<BatchInfo> batchInfos = stageInfo.getBatchInfos();
		for (BatchInfo batchInfo : batchInfos) {
			writeBatchInfo(observableEnvironment, batchInfo, stageInfo);
		}
	}

	@Override
	public void handleStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.add(batchId);
	}

	@Override
	public void handleUnStagedBatch(ObservableEnvironment observableEnvironment, final BatchId batchId, final StageId stageId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.add(batchId);
	}

	/*
	 * Releases a ReportItem for the batch info
	 */
	private void writeBatchInfo(ObservableEnvironment observableEnvironment, BatchInfo batchInfo, StageInfo stageInfo) {
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		reportItemBuilder.setReportHeader(getReportHeader(observableEnvironment));

		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());
		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(batchInfo.getBatchId());
		reportItemBuilder.addValue(batchInfo.getMaterialsProducerId());
		StageId stageId = batchInfo.getStageId();

		boolean offered = false;
		if (stageInfo != null) {
			offered = stageInfo.isStageOffered();
		} else {
			if (stageId != null) {
				offered = observableEnvironment.isStageOffered(stageId);
			}
		}
		reportItemBuilder.addValue(offered);
		reportItemBuilder.addValue(batchInfo.getMaterialId());
		reportItemBuilder.addValue(batchInfo.getAmount());

		Map<BatchPropertyId, Object> batchPropertyValueMap = batchInfo.getPropertyValueMap();
		for (MaterialId materialId : batchPropertyMap.keySet()) {
			boolean matchingMaterial = batchInfo.getMaterialId().equals(materialId);
			Set<BatchPropertyId> batchPropertyIds = batchPropertyMap.get(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				if (matchingMaterial) {
					reportItemBuilder.addValue(batchPropertyValueMap.get(batchPropertyId));
				} else {
					reportItemBuilder.addValue("");
				}
			}
		}
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

	@Override
	public void handleBatchPropertyValueAssignment(ObservableEnvironment observableEnvironment, final BatchId batchId, final BatchPropertyId batchPropertyId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.add(batchId);
	}

	@Override
	public void handleStageOfferChange(ObservableEnvironment observableEnvironment, final StageId stageId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.addAll(observableEnvironment.getStageBatches(stageId));
	}

	@Override
	public void handleStageTransfer(ObservableEnvironment observableEnvironment, final StageId stageId, final MaterialsProducerId materialsProducerId) {
		flushOnTimeChange(observableEnvironment);
		updatedBatches.addAll(observableEnvironment.getStageBatches(stageId));
	}
	
	@Override
	public void init(final ObservableEnvironment observableEnvironment,Set<Object> initialData) {
		for(MaterialsProducerId materialsProducerId : observableEnvironment.getMaterialsProducerIds()) {
			for(BatchId inventoryBatchId : observableEnvironment.getInventoryBatches(materialsProducerId)) {
				updatedBatches.add(inventoryBatchId);
			}
			for(StageId stageId : observableEnvironment.getStages(materialsProducerId)) {
				for(BatchId stageBatchId : observableEnvironment.getStageBatches(stageId)) {
					updatedBatches.add(stageBatchId);
				}	
			}						
		}
	}

}
