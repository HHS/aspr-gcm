package gcm.simulation;

import java.util.List;

import gcm.scenario.BatchId;
import gcm.scenario.BatchPropertyId;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.MaterialId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.util.annotations.Source;

/**
 * Manager for all properties for the simulation. It managers various
 * sub-manager types for each property definition as well as handling mutations
 * to property values.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface PropertyManager extends Element {
	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */
	public <T> T getPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId);

	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */

	public double getPersonPropertyTime(PersonId personId, PersonPropertyId personPropertyId);

	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */

	public void setPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId, Object personPropertyValue);

	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */

	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue);
	
	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue);
	
	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */

	public void handlePersonAddition(final PersonId personId);

	/**
	 * Pass-through method. See PersonPropertyManager for details.
	 */

	public void handlePersonRemoval(final PersonId personId);

	/**
	 * Sets the value of the region property.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param regionPropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue);

	/**
	 * Returns the value of the region property.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param regionPropertyId
	 *            cannot be null
	 */
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId);

	/**
	 * Returns the time when the of the region property was last assigned.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param regionPropertyId
	 *            cannot be null
	 */

	public double getRegionPropertyTime(RegionId regionId, RegionPropertyId regionPropertyId);

	/**
	 * Sets the value of the compartment property.
	 * 
	 * @param compartmentId
	 *            cannot be null
	 * @param compartmentPropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, Object compartmentPropertyValue);

	/**
	 * Returns the value of the compartment property.
	 * 
	 * @param compartmentId
	 *            cannot be null
	 * @param compartmentPropertyId
	 *            cannot be null
	 */
	public <T> T getCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the time when the of the compartment property was last assigned.
	 * 
	 * @param compartmentId
	 *            cannot be null
	 * @param compartmentPropertyId
	 *            cannot be null
	 */

	public double getCompartmentPropertyTime(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId);

	/**
	 * Sets the value of the materials producer property.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param materialsProducerPropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue);

	/**
	 * Returns the value of the materials producer property.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param personPropertyId
	 *            cannot be null
	 */
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the time when the of the materials producer property was last
	 * assigned.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param materialsProducerPropertyId
	 *            cannot be null
	 */

	public double getMaterialsProducerPropertyTime(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Sets the value of the resource property.
	 * 
	 * @param resourceId
	 *            cannot be null
	 * @param resourcePropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object resourcPropertyValue);

	/**
	 * Returns the value of the resource property.
	 * 
	 * @param resourceId
	 *            cannot be null
	 * @param resourcePropertyId
	 *            cannot be null
	 */
	public <T> T getResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the time when the of the resource property was last assigned.
	 * 
	 * @param resourceId
	 *            cannot be null
	 * @param resourcePropertyId
	 *            cannot be null
	 */

	public double getResourcePropertyTime(ResourceId resourceId, ResourcePropertyId resourcePropertyId);

	/**
	 * Sets the value of the global property.
	 *
	 * @param globalPropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue);

	/**
	 * Returns the value of the global property.
	 * 
	 * @param globalPropertyId
	 *            cannot be null
	 */
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId);

	/**
	 * Returns the time when the of the global property was last assigned.
	 * 
	 * @param globalPropertyId
	 *            cannot be null
	 */

	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId);

	/**
	 * Returns the time when the of the batch property was last assigned. It is
	 * the caller's responsibility to validate the inputs.
	 * 
	 * @param batchId
	 *            cannot be null
	 * 
	 * @param batchPropertyId
	 *            cannot be null
	 */

	public double getBatchPropertyTime(BatchId batchId, BatchPropertyId batchPropertyId);

	/**
	 * Returns the value of the batch property. It is the caller's
	 * responsibility to validate the inputs.
	 * 
	 * @param batchId
	 *            cannot be null
	 * 
	 * @param batchPropertyId
	 *            cannot be null
	 */

	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId);

	/**
	 * Sets the value of the batch property. It is the caller's responsibility
	 * to validate the inputs.
	 *
	 * @param batchId
	 *            cannot be null
	 * @param batchPropertyId
	 *            cannot be null
	 * @param propertyValue
	 *            cannot be null
	 */
	public void setBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue);

	/**
	 * Adds the batch to property tracking, establishing the default values for
	 * each batch property. This should be invoked every time a batch is
	 * created.
	 * 
	 * @param batchId
	 *            cannot be null
	 * 
	 */

	public void handleBatchAddition(final BatchId batchId, MaterialId materialId);

	/**
	 * Removes the batch from property tracking. This should be invoked every
	 * time a batch is destroyed.
	 * 
	 * @param batchId
	 *            cannot be null
	 */

	public void handleBatchRemoval(final BatchId batchId);

	/**
	 * Removes the group from property tracking. This should be invoked every
	 * time a group is removed.
	 * 
	 * @param groupId
	 *            must be valid id of an existing group
	 */

	public void handleGroupRemoval(final GroupId groupId);

	/**
	 * Returns the value of the group property. It is the caller's
	 * responsibility to validate the inputs.
	 * 
	 */

	public <T> T getGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId);

	/**
	 * Returns the value of the group property. It is the caller's
	 * responsibility to validate the inputs.
	 * 
	 */
	public double getGroupPropertyTime(final GroupId groupId, GroupPropertyId groupPropertyId);

	/**
	 * Sets the value of the group property. It is the caller's responsibility
	 * to validate the inputs.
	 */
	public void setGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue);
}
