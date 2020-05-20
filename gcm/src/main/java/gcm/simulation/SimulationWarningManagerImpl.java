package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gcm.output.simstate.SimulationWarningItem;
import gcm.scenario.CompartmentId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.PROXY,proxy = EnvironmentImpl.class)
public class SimulationWarningManagerImpl extends BaseElement implements SimulationWarningManager {

	private OutputItemManager outputItemManager;
	private ReplicationId replicationId;
	private ScenarioId scenarioId;
	private ComponentManager componentManager;

	private boolean regionMapOptionWarned;
	private boolean compartmentMapOptionWarned;
	private Set<PersonPropertyId> personPropertyIdsWarned = new LinkedHashSet<>();

	@Override
	public void init(Context context) {
		super.init(context);
		outputItemManager = context.getOutputItemManager();
		replicationId = context.getReplication().getId();
		componentManager = context.getComponentManager();
		scenarioId = context.getScenario().getScenarioId();

	}

	@Override
	public void processPopulationIndexEfficiencyWarning(PopulationIndexEfficiencyWarning populationIndexEfficiencyWarning) {

		/*
		 * Review the attributes
		 */
		boolean regionsNeedMapping = false;
		boolean compartmentsNeedMapping = false;
		Set<PersonPropertyId> personPropertyIds = new LinkedHashSet<>();
		for (Object attribute : populationIndexEfficiencyWarning.getAttributes()) {
			if (attribute instanceof PersonPropertyId) {
				personPropertyIds.add((PersonPropertyId) attribute);
			} else if (attribute instanceof RegionId) {
				regionsNeedMapping = true;
			} else if (attribute instanceof CompartmentId) {
				compartmentsNeedMapping = true;
			}
		}

		/*
		 * Decide to release the warning on the basis that it contains something
		 * not in a previous warning
		 */
		boolean releaseWarning = !regionMapOptionWarned && regionsNeedMapping;
		releaseWarning |= !compartmentMapOptionWarned && compartmentsNeedMapping;
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			releaseWarning |= !personPropertyIdsWarned.contains(personPropertyId);
		}

		/*
		 * Update the warned attributes
		 */

		regionMapOptionWarned |= regionsNeedMapping;
		compartmentMapOptionWarned |= compartmentsNeedMapping;
		personPropertyIdsWarned.addAll(personPropertyIds);

		if (releaseWarning) {

			List<String> attributesToList = new ArrayList<>();

			if (regionsNeedMapping) {
				attributesToList.add("regions");
			}

			if (compartmentsNeedMapping) {
				attributesToList.add("compartments");
			}

			for (PersonPropertyId personPropertyId : personPropertyIds) {
				attributesToList.add(personPropertyId.toString());
			}

			StringBuilder sb = new StringBuilder();
			sb.append("Population Index Efficiency for ");
			sb.append(componentManager.getFocalComponentId().toString());
			sb.append(" index id = ");
			sb.append(populationIndexEfficiencyWarning.getPopulationIndexId());
			sb.append(" may benefit from MapOptions being turned on for ");
			boolean first = true;

			for (String attribute : attributesToList) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");

				}
				sb.append(attribute);
			}

			sb.append("\n");
			sb.append("The index is using the following filter:");
			sb.append("\n");
			
			sb.append(FilterDisplay.getPrettyPrint(populationIndexEfficiencyWarning.getFilterInfo()));

			SimulationWarningItem simulationWarningItem = SimulationWarningItem	.builder()//
																				.setReplicationId(replicationId)//
																				.setScenarioId(scenarioId)//
																				.setWarning(sb.toString())//
																				.build();//

			outputItemManager.releaseOutputItem(simulationWarningItem);
		}

	}

}
