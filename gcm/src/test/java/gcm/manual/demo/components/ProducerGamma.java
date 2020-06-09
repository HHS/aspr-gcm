package gcm.manual.demo.components;

import gcm.components.AbstractComponent;
import gcm.manual.demo.identifiers.Material;
import gcm.manual.demo.identifiers.Resource;
import gcm.scenario.BatchId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.RegionId;
import gcm.scenario.StageId;
import gcm.simulation.Environment;
import gcm.simulation.Plan;

public class ProducerGamma extends AbstractComponent {

	private RegionId regionId;
	
	private static class ProductionPlan implements Plan {
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		BatchId batchId = environment.createBatch(Material.MATERIAL_1, 100);
		StageId stageId = environment.createStage();
		environment.moveBatchToStage(batchId, stageId);
		environment.convertStageToResource(stageId, Resource.RESOURCE_1, 10000);
		MaterialsProducerId componentId = (MaterialsProducerId)environment.getCurrentComponentId();
		long amount = environment.getMaterialsProducerResourceLevel(componentId, Resource.RESOURCE_1);
		environment.transferProducedResourceToRegion(componentId, Resource.RESOURCE_1, regionId, amount);
	}

	@Override
	public void init(Environment environment) {
		regionId = environment.getRegionIds().iterator().next();
		for (int i = 0; i < 14; i++) {
			environment.addPlan(new ProductionPlan(), i);
		}
	}

}
