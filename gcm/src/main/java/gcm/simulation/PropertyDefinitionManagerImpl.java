package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of PropertyDefinitionManager
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class PropertyDefinitionManagerImpl extends BaseElement implements PropertyDefinitionManager {

	@Override
	public void init(Context context) {
		super.init(context);

		Scenario scenario = context.getScenario();
		loadGlobalPropertyDefinitions(scenario);
		loadRegionPropertyDefinitions(scenario);
		loadCompartmentPropertyDefinitions(scenario);
		loadResourcePropertyDefinitions(scenario);
		loadPersonPropertyDefinitions(scenario);
		loadMaterialsProducerPropertyDefinitions(scenario);
		loadBatchPropertyDefinitions(scenario);
		loadGroupPropertyDefinitions(scenario);
	}

	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

	private Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

	private Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> compartmentPropertyDefinitions = new LinkedHashMap<>();

	private Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions = new LinkedHashMap<>();

	private Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();

	private Map<MaterialsProducerPropertyId, PropertyDefinition> materialsProducerPropertyDefinitions = new LinkedHashMap<>();

	private Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> batchPropertyDefinitions = new LinkedHashMap<>();

	private Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();

	private void loadGlobalPropertyDefinitions(Scenario scenario) {
		for (final GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
			final PropertyDefinition propertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
			globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		}
	}

	@Override
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		return globalPropertyDefinitions.get(globalPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		Set<T> result = new LinkedHashSet<>(globalPropertyDefinitions.keySet().size());
		for (GlobalPropertyId globalPropertyId : globalPropertyDefinitions.keySet()) {
			result.add((T) globalPropertyId);
		}
		return result;
	}

	@Override
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		return regionPropertyDefinitions.get(regionPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		Set<T> result = new LinkedHashSet<>(regionPropertyDefinitions.keySet().size());
		for (RegionPropertyId regionPropertyId : regionPropertyDefinitions.keySet()) {
			result.add((T) regionPropertyId);
		}
		return result;
	}

	private void loadRegionPropertyDefinitions(Scenario scenario) {
		for (final RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
			final PropertyDefinition propertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
			regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		}
	}

	@Override
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId) {
		Map<CompartmentPropertyId, PropertyDefinition> map = compartmentPropertyDefinitions.get(compartmentId);
		Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (CompartmentPropertyId compartmentPropertyId : map.keySet()) {
			result.add((T) compartmentPropertyId);
		}
		return result;
	}

	private void loadCompartmentPropertyDefinitions(Scenario scenario) {
		for (final CompartmentId compartmentId : scenario.getCompartmentIds()) {
			for (final CompartmentPropertyId compartmentPropertyId : scenario.getCompartmentPropertyIds(compartmentId)) {
				final PropertyDefinition propertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				Map<CompartmentPropertyId, PropertyDefinition> map = compartmentPropertyDefinitions.get(compartmentId);
				if (map == null) {
					map = new LinkedHashMap<>();
					compartmentPropertyDefinitions.put(compartmentId, map);
				}
				map.put(compartmentPropertyId, propertyDefinition);
			}
		}
	}

	@Override
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourcePropertyDefinitions.get(resourceId).get(resourcePropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		Map<ResourcePropertyId, PropertyDefinition> defMap = resourcePropertyDefinitions.get(resourceId);
		Set<T> result = new LinkedHashSet<>(defMap.keySet().size());
		for (ResourcePropertyId resourcePropertyId : defMap.keySet()) {
			result.add((T) resourcePropertyId);
		}
		return result;
	}

	private void loadResourcePropertyDefinitions(Scenario scenario) {
		for (final ResourceId resourceId : scenario.getResourceIds()) {
			Map<ResourcePropertyId, PropertyDefinition> defMap = new LinkedHashMap<>();
			resourcePropertyDefinitions.put(resourceId, defMap);
			for (final ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
				final PropertyDefinition propertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				defMap.put(resourcePropertyId, propertyDefinition);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {

		Set<T> result = new LinkedHashSet<>(personPropertyDefinitions.keySet().size());
		for (PersonPropertyId personPropertyId : personPropertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}

		return result;
	}

	private void loadPersonPropertyDefinitions(Scenario scenario) {
		for (final PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
			final PropertyDefinition propertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
			personPropertyDefinitions.put(personPropertyId, propertyDefinition);
		}
	}

	@Override
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		return personPropertyDefinitions.get(personPropertyId);
	}

	@Override
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		Set<T> result = new LinkedHashSet<>(materialsProducerPropertyDefinitions.keySet().size());
		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
			result.add((T) materialsProducerPropertyId);
		}
		return result;
	}

	private void loadMaterialsProducerPropertyDefinitions(Scenario scenario) {
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
			final PropertyDefinition propertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
		}
	}

	@Override
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		return map.get(batchPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (BatchPropertyId batchPropertyId : map.keySet()) {
			result.add((T) batchPropertyId);
		}
		return result;
	}

	private void loadBatchPropertyDefinitions(Scenario scenario) {
		for (final MaterialId materialId : scenario.getMaterialIds()) {
			Map<BatchPropertyId, PropertyDefinition> map = new LinkedHashMap<>();
			batchPropertyDefinitions.put(materialId, map);
			final Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				final PropertyDefinition propertyDefinition = scenario.getBatchPropertyDefinition(materialId, batchPropertyId);
				map.put(batchPropertyId, propertyDefinition);
			}
		}
	}

	private void loadGroupPropertyDefinitions(Scenario scenario) {
		for (final GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			Map<GroupPropertyId, PropertyDefinition> map = new LinkedHashMap<>();
			groupPropertyDefinitions.put(groupTypeId, map);
			final Set<GroupPropertyId> propertyIds = scenario.getGroupPropertyIds(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIds) {
				final PropertyDefinition propertyDefinition = scenario.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				map.put(groupPropertyId, propertyDefinition);
			}
		}
	}

	@Override
	public PropertyDefinition getGroupPropertyDefinition(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		return map.get(groupPropertyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(GroupTypeId groupTypeId) {
		
		Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (GroupPropertyId groupPropertyId : map.keySet()) {
			result.add((T) groupPropertyId);
		}

		return result;
	}

}
