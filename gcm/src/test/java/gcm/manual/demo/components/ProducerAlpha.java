package gcm.test.manual.demo.components;

import gcm.components.AbstractComponent;
import gcm.scenario.BatchId;
import gcm.scenario.StageId;
import gcm.simulation.Environment;
import gcm.test.manual.demo.identifiers.Material;
import gcm.test.manual.demo.identifiers.MaterialsProducers;

public class ProducerAlpha extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		BatchId batchId = environment.createBatch(Material.MATERIAL_1, 15);
		StageId stageId = environment.createStage();
		environment.moveBatchToStage(batchId, stageId);
		environment.setStageOffer(stageId, true);
		environment.transferOfferedStageToMaterialsProducer(stageId, MaterialsProducers.PRODUCER_BETA);		
	}

}
