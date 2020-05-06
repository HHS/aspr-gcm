package gcm.simulation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.Scenario;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of {@link PropertyManager}
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class PropertyManagerImpl extends BaseElement implements PropertyManager {

	/*
	 * Record for holding the value and assignment time for a property. People
	 * property values, being numerous, are stored in specialized classes. All
	 * other property values are stored via maps to PropertyValueRecord.
	 */
	private static class PropertyValueRecord {

		private Object propertyValue;
		private double assignmentTime;
		private final EventManager eventManager;

		public PropertyValueRecord(EventManager eventManager) {
			this.eventManager = eventManager;
		}

		public Object getValue() {
			if (propertyValue == null) {
				throw new RuntimeException("uninitialized value");
			}
			return propertyValue;
		}

		public void setPropertyValue(Object propertyValue) {
			this.propertyValue = propertyValue;
			assignmentTime = eventManager.getTime();
		}

		public double getAssignmentTime() {
			return assignmentTime;
		}
	}

	private Map<RegionId, Map<RegionPropertyId, PropertyValueRecord>> regionPropertyMap = new LinkedHashMap<>();

	private Map<CompartmentId, Map<CompartmentPropertyId, PropertyValueRecord>> compartmentPropertyMap = new LinkedHashMap<>();

	private Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, PropertyValueRecord>> materialsProducerPropertyMap = new LinkedHashMap<>();

	private Map<ResourceId, Map<ResourcePropertyId, PropertyValueRecord>> resourcePropertyMap = new LinkedHashMap<>();

	private Map<GlobalPropertyId, PropertyValueRecord> globalPropertyMap = new LinkedHashMap<>();

	private Map<BatchId, Map<BatchPropertyId, PropertyValueRecord>> batchPropertyMap = new LinkedHashMap<>();

	private Map<MaterialId, Set<BatchPropertyId>> batchPropertyIdMap = new LinkedHashMap<>();

	private Map<GroupId, Map<GroupPropertyId, PropertyValueRecord>> groupPropertyMap = new LinkedHashMap<>();

	/*
	 * Stores for person property a PersonPropertyManager instance. Each
	 * PersonPropertyManager instance in turn manages all aspects of the
	 * particular property.
	 */
	private Map<PersonPropertyId, PersonPropertyManager> personPropertyManagerMap = new LinkedHashMap<>();

	private EventManager eventManager;

	private PropertyDefinitionManager propertyDefinitionManager;

	private PersonGroupManger personGroupManger;

	@Override
	public void init(Context context) {
		super.init(context);

		// initializing the region property values
		eventManager = context.getEventManager();
		propertyDefinitionManager = context.getPropertyDefinitionsManager();
		personGroupManger = context.getPersonGroupManger();
		Scenario scenario = context.getScenario();

		/*
		 * NOTE: For some of the property definitions contained in the scenario,
		 * there may be a default value of null. A null value will reside in the
		 * propertyValueRecord after this method executes, but the scenario
		 * guarantees that a non-null property value exists in the scenario that
		 * will be loaded before any component is initialized and so no
		 * component will be able to retrieve a null property value.
		 */

		for (RegionId regionId : scenario.getRegionIds()) {
			Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			regionPropertyMap.put(regionId, map);
			for (RegionPropertyId regionPropertyId : scenario.getRegionPropertyIds()) {
				PropertyDefinition regionPropertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
				if (regionPropertyDefinition.getDefaultValue().isPresent()) {
					propertyValueRecord.setPropertyValue(regionPropertyDefinition.getDefaultValue().get());
				} else {
					// see NOTE above
				}
				map.put(regionPropertyId, propertyValueRecord);
			}
		}

		// initializing the compartment property values
		for (CompartmentId compartmentId : scenario.getCompartmentIds()) {
			Map<CompartmentPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			compartmentPropertyMap.put(compartmentId, map);
			for (CompartmentPropertyId compartmentPropertyId : scenario.getCompartmentPropertyIds(compartmentId)) {
				PropertyDefinition compartmentPropertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
				if (compartmentPropertyDefinition.getDefaultValue().isPresent()) {
					propertyValueRecord.setPropertyValue(compartmentPropertyDefinition.getDefaultValue().get());
				} else {
					// see NOTE above
				}
				map.put(compartmentPropertyId, propertyValueRecord);
			}
		}

		// initializing the materials producer property values
		for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			Map<MaterialsProducerPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			materialsProducerPropertyMap.put(materialsProducerId, map);
			for (MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
				PropertyDefinition materialsProducerPropertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
				if (materialsProducerPropertyDefinition.getDefaultValue().isPresent()) {
					propertyValueRecord.setPropertyValue(materialsProducerPropertyDefinition.getDefaultValue().get());
				} else {
					// see NOTE above
				}
				map.put(materialsProducerPropertyId, propertyValueRecord);
			}
		}

		// initializing the resource property values
		for (ResourceId resourceId : scenario.getResourceIds()) {
			Map<ResourcePropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			resourcePropertyMap.put(resourceId, map);
			for (ResourcePropertyId resourcePropertyId : scenario.getResourcePropertyIds(resourceId)) {
				PropertyDefinition resourcePropertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
				if (resourcePropertyDefinition.getDefaultValue().isPresent()) {
					propertyValueRecord.setPropertyValue(resourcePropertyDefinition.getDefaultValue().get());
				} else {
					// see NOTE above
				}
				map.put(resourcePropertyId, propertyValueRecord);
			}
		}

		// initializing global property values

		for (GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
			if (globalPropertyDefinition.getDefaultValue().isPresent()) {
				propertyValueRecord.setPropertyValue(globalPropertyDefinition.getDefaultValue().get());
			} else {
				// see NOTE above
			}
			globalPropertyMap.put(globalPropertyId, propertyValueRecord);
		}

		// initializing batchPropertyIdMap
		for (MaterialId materialId : scenario.getMaterialIds()) {
			Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
			batchPropertyIdMap.put(materialId, batchPropertyIds);
		}

		/*
		 * Using the PropertyDefinition associated with each person property, we
		 * select an appropriate PropertyManager implementor and place it in the
		 * propertyManagerMap. The default for anything we don't understand such
		 * as modeler-defined types and Strings get handled with an
		 * ObjectPropertyManager.
		 */

		for (PersonPropertyId personPropertyId : scenario.getPersonPropertyIds()) {
			PropertyDefinition propertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
			PersonPropertyManager personPropertyManager;
			if (propertyDefinition.getType() == Boolean.class) {
				personPropertyManager = new BooleanPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Float.class) {
				personPropertyManager = new FloatPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Double.class) {
				personPropertyManager = new DoublePropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Byte.class) {
				personPropertyManager = new IntPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Short.class) {
				personPropertyManager = new IntPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Integer.class) {
				personPropertyManager = new IntPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (propertyDefinition.getType() == Long.class) {
				personPropertyManager = new IntPropertyManager(context, propertyDefinition, personPropertyId);
			} else if (Enum.class.isAssignableFrom(propertyDefinition.getType())) {
				personPropertyManager = new EnumPropertyManager(context, propertyDefinition, personPropertyId);
			} else {
				personPropertyManager = new ObjectPropertyManager(context, propertyDefinition, personPropertyId);
			}
			personPropertyManagerMap.put(personPropertyId, personPropertyManager);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId) {
		return (T) personPropertyManagerMap.get(personPropertyId).getPropertyValue(personId);
	}

	@Override
	public double getPersonPropertyTime(PersonId personId, PersonPropertyId personPropertyId) {
		return personPropertyManagerMap.get(personPropertyId).getPropertyTime(personId);
	}

	@Override
	public void setPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId, Object personPropertyValue) {
		personPropertyManagerMap.get(personPropertyId).setPropertyValue(personId, personPropertyValue);
	}

	@Override
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertyManagerMap.get(personPropertyId).getPeopleWithPropertyValue(personPropertyValue);
	}
	
	@Override
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertyManagerMap.get(personPropertyId).getPersonCountForPropertyValue(personPropertyValue);
	}

	@Override
	public void handlePersonAddition(final PersonId personId) {
		for (PersonPropertyManager propertyManager : personPropertyManagerMap.values()) {
			propertyManager.handlePersonAddition(personId);
		}
	}

	@Override
	public void handlePersonRemoval(final PersonId personId) {
		for (PersonPropertyManager propertyManager : personPropertyManagerMap.values()) {
			propertyManager.handlePersonRemoval(personId);
		}
	}

	@Override
	public void setRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue) {
		regionPropertyMap.get(regionId).get(regionPropertyId).setPropertyValue(regionPropertyValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId) {
		return (T) regionPropertyMap.get(regionId).get(regionPropertyId).getValue();
	}

	@Override
	public double getRegionPropertyTime(RegionId regionId, RegionPropertyId regionPropertyId) {
		return regionPropertyMap.get(regionId).get(regionPropertyId).getAssignmentTime();
	}

	@Override
	public void setCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, Object compartmentPropertyValue) {
		compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).setPropertyValue(compartmentPropertyValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		return (T) compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).getValue();
	}

	@Override
	public double getCompartmentPropertyTime(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		return compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).getAssignmentTime();
	}

	@Override
	public void setMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).setPropertyValue(materialsProducerPropertyValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		return (T) materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getValue();
	}

	@Override
	public double getMaterialsProducerPropertyTime(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getAssignmentTime();
	}

	@Override
	public void setResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object resourcPropertyValue) {
		resourcePropertyMap.get(resourceId).get(resourcePropertyId).setPropertyValue(resourcPropertyValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		return (T) resourcePropertyMap.get(resourceId).get(resourcePropertyId).getValue();
	}

	@Override
	public double getResourcePropertyTime(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		return resourcePropertyMap.get(resourceId).get(resourcePropertyId).getAssignmentTime();
	}

	@Override
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {
		globalPropertyMap.get(globalPropertyId).setPropertyValue(globalPropertyValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		return (T) globalPropertyMap.get(globalPropertyId).getValue();
	}

	@Override
	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		return globalPropertyMap.get(globalPropertyId).getAssignmentTime();
	}

	@Override
	public double getBatchPropertyTime(BatchId batchId, BatchPropertyId batchPropertyId) {
		Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		if (map == null) {
			throw new RuntimeException("batch not contained " + batchId);
		}
		PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		return propertyValueRecord.getAssignmentTime();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId) {
		Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		if (map == null) {
			throw new RuntimeException("batch not contained " + batchId);
		}
		PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		if (propertyValueRecord == null) {
			throw new RuntimeException("property not found " + batchPropertyId + " for batch " + batchId);
		}
		return (T) propertyValueRecord.getValue();
	}

	@Override
	public void setBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue) {
		Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		if (map == null) {
			throw new RuntimeException("batch not contained " + batchId);
		}
		PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		if (propertyValueRecord == null) {
			propertyValueRecord = new PropertyValueRecord(eventManager);
			map.put(batchPropertyId, propertyValueRecord);
		}
		propertyValueRecord.setPropertyValue(batchPropertyValue);
	}

	@Override
	public void handleBatchAddition(final BatchId batchId, MaterialId materialId) {
		if (batchPropertyMap.containsKey(batchId)) {
			throw new RuntimeException("batch already contained " + batchId);
		}
		Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();

		Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);

		for (BatchPropertyId batchPropertyId : batchPropertyIds) {
			PropertyDefinition propertyDefinition = propertyDefinitionManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(eventManager);
			if (propertyDefinition.getDefaultValue().isPresent()) {
				propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
			} else {
				throw new RuntimeException("Batch property id : " + batchPropertyId + " has a null default property value for its property definition: " + propertyDefinition);
			}

			map.put(batchPropertyId, propertyValueRecord);
		}

		batchPropertyMap.put(batchId, map);
	}

	@Override
	public void handleBatchRemoval(final BatchId batchId) {
		if (!batchPropertyMap.containsKey(batchId)) {
			throw new RuntimeException("batch not contained " + batchId);
		}
		batchPropertyMap.remove(batchId);
	}

	@Override
	public void handleGroupRemoval(final GroupId groupId) {
		groupPropertyMap.remove(groupId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, PropertyValueRecord> propertyMap = groupPropertyMap.get(groupId);
		/*
		 * If we cannot find a property value record we will use the default
		 * value associated with the property definition.
		 */
		PropertyValueRecord propertyValueRecord = null;
		if (propertyMap != null) {
			propertyValueRecord = propertyMap.get(groupPropertyId);
		}
		if (propertyValueRecord == null) {
			GroupTypeId groupTypeId = personGroupManger.getGroupType(groupId);
			PropertyDefinition propertyDefinition = propertyDefinitionManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
			if (propertyDefinition.getDefaultValue().isPresent()) {
				return (T) propertyDefinition.getDefaultValue().get();
			} else {
				throw new RuntimeException("Group property id : " + groupPropertyId + " has a null default property value for its property definition: " + propertyDefinition);
			}
		}
		return (T) propertyValueRecord.getValue();
	}

	@Override
	public double getGroupPropertyTime(final GroupId groupId, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, PropertyValueRecord> propertyMap = groupPropertyMap.get(groupId);
		/*
		 * If we cannot find a property assignment time, then we assume it is
		 * time zero.
		 */
		PropertyValueRecord propertyValueRecord = null;
		if (propertyMap != null) {
			propertyValueRecord = propertyMap.get(groupPropertyId);
		}
		if (propertyValueRecord == null) {
			return 0;
		}
		return propertyValueRecord.assignmentTime;
	}

	@Override
	public void setGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue) {
		Map<GroupPropertyId, PropertyValueRecord> propertyMap = groupPropertyMap.get(groupId);
		if (propertyMap == null) {
			propertyMap = new LinkedHashMap<>();
			groupPropertyMap.put(groupId, propertyMap);
		}

		PropertyValueRecord propertyValueRecord = propertyMap.get(groupPropertyId);
		if (propertyValueRecord == null) {
			propertyValueRecord = new PropertyValueRecord(eventManager);
			propertyMap.put(groupPropertyId, propertyValueRecord);
		}
		propertyValueRecord.setPropertyValue(groupPropertyValue);
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {
		memoryPartition.addMemoryLink(this, personPropertyManagerMap, "Person Property Manager Map");
		for (PersonPropertyId personPropertyId : personPropertyManagerMap.keySet()) {
			PersonPropertyManager personPropertyManager = personPropertyManagerMap.get(personPropertyId);
			memoryPartition.addMemoryLink(this, personPropertyManager, "Person Property: " + personPropertyId.toString());
		}

	}

}
