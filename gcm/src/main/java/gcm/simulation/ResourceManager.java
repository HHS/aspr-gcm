package gcm.simulation;

import java.util.List;

import gcm.scenario.MaterialsProducerId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.scenario.TimeTrackingPolicy;
import gcm.util.annotations.Source;

/**
 * Manager for all resources.
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface ResourceManager extends Element {

	/**
	 * Reduces the resource for the particular person and resource by one.
	 * Negative resource values are allowed and it is up to the caller to
	 * validate resource levels.
	 * 
	 * @param resourceId
	 * @param personId
	 * @param resourceAmount
	 */
	public void decrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount);

	/**
	 * Returns a List of people who have a zero level of the resource
	 * 
	 * @param resourceId
	 */
	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId);
	/**
	 * Returns a List of people who have a non-zero level of the resource
	 * 
	 * @param resourceId
	 */
	public List<PersonId> getPeopleWithResource(final ResourceId resourceId);
	/**
	 * Returns the resource level for the given person and resource
	 * 
	 * @param resourceId
	 * @param personId
	 */
	public long getPersonResourceLevel(final ResourceId resourceId, final PersonId personId);

	/**
	 * 
	 * @param resourceId
	 * @param personId
	 */
	public double getPersonResourceTime(final ResourceId resourceId, final PersonId personId);

	/**
	 * Increments the resource for the particular person and resource by one. *
	 * 
	 * @param resourceId
	 * @param personId
	 * @param resourceAmount
	 */
	public void incrementPersonResourceLevel(final ResourceId resourceId, final PersonId personId, final long resourceAmount);
	
	/**
	 * Increments the region resource level by the given amount.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 * @param amount
	 *            cannot not be negative
	 */
	public void incrementRegionResourceLevel(RegionId regionId, ResourceId resourceId, long amount);
	/**
	 * Decrements the region resource level by the given amount.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 * @param amount
	 *            cannot not be negative
	 */
	public void decrementRegionResourceLevel(RegionId regionId, ResourceId resourceId, long amount);

	
	
	/**
	 * Returns the region resource level.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public long getRegionResourceLevel(RegionId regionId, ResourceId resourceId);

	/**
	 * Returns the simulation time when the region resource level was last
	 * assigned.
	 * 
	 * @param regionId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public double getRegionResourceTime(RegionId regionId, ResourceId resourceId);

	/**
	 * Increments the materials producer resource level by the given amount.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 * @param amount
	 *            cannot not be negative
	 */
	public void incrementMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount);
	/**
	 * Increments the materials producer resource level by the given amount.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 * @param amount
	 *            cannot not be negative
	 */
	public void decrementMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount);
	/**
	 * Returns the materials producer resource level.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public long getMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId);

	/**
	 * Returns the simulation time when the materials producer resource level
	 * was last assigned.
	 * 
	 * @param materialsProducerId
	 *            cannot be null
	 * @param resourceId
	 *            cannot be null
	 */
	public double getMaterialsProducerResourceTime(MaterialsProducerId materialsProducerId, ResourceId resourceId);
	/**
	 * Returns the {@link TimeTrackingPolicy} for the given resource
	 * 
	 * @param resourceId
	 *            cannot be null
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(ResourceId resourceId);

}
