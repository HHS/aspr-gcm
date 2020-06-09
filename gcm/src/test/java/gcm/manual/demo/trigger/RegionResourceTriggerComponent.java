package gcm.manual.demo.trigger;

import gcm.components.AbstractComponent;
import gcm.manual.demo.identifiers.RegionProperty;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.Environment;
import gcm.simulation.Plan;

public class RegionResourceTriggerComponent extends AbstractComponent {


	private static class FlagExaminationPlan implements Plan{
		
	}
	
	@Override
	public void executePlan(final Environment environment,final Plan plan) {
		examimeFlag(environment);
	}

	@Override
	public void init(Environment environment) {
		TriggerContainer triggerContainer = environment.getGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER);
		RegionResourceTrigger regionResourceTrigger = triggerContainer.getTrigger(environment.getCurrentComponentId());

		environment.observeRegionPersonArrival(true, regionResourceTrigger.getRegionId());
		environment.observeRegionPersonDeparture(true, regionResourceTrigger.getRegionId());
		environment.observeRegionResourceChange(true, regionResourceTrigger.getRegionId(), regionResourceTrigger.getResourceId());
		environment.addPlan(new FlagExaminationPlan(), 0);
		
	}

	private void examimeFlag(final Environment environment) {
		TriggerContainer triggerContainer = environment.getGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER);
		RegionResourceTrigger regionResourceTrigger = triggerContainer.getTrigger(environment.getCurrentComponentId());
		RegionId regionId = regionResourceTrigger.getRegionId();
		ResourceId resourceId = regionResourceTrigger.getResourceId();	
		double threshold = regionResourceTrigger.getThreshold();

		boolean flag = environment.getRegionPropertyValue(regionId, RegionProperty.FLAG);
		long regionResourceLevel = environment.getRegionResourceLevel(regionId, resourceId);
		int regionPopulationCount = environment.getRegionPopulationCount(regionId);
		
		if(flag) {
			if((regionResourceLevel/regionPopulationCount)>threshold) {
				environment.setRegionPropertyValue(regionId, RegionProperty.FLAG, false);
			}			
		}else {
			if((regionResourceLevel/regionPopulationCount)<threshold) {
				environment.setRegionPropertyValue(regionId, RegionProperty.FLAG, true);
			}			
		}
	}

	@Override
	public void observeRegionResourceChange(final Environment environment, final RegionId regionId, final ResourceId resourceId) {
		examimeFlag(environment);
	}

	@Override
	public void observeRegionPersonArrival(final Environment environment, final PersonId personId) {
		examimeFlag(environment);
	}

	@Override
	public void observeRegionPersonDeparture(final Environment environment, final RegionId regionId, final PersonId personId) {
		examimeFlag(environment);
	}

}
