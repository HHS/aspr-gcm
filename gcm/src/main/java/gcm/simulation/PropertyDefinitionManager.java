package gcm.simulation;

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
import gcm.util.annotations.Source;

/**
 * Container for all property definitions and related property identifiers.
 * Property definitions are loaded from the sceanrio and stored here for
 * convenience.
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface PropertyDefinitionManager extends Element {

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId);

	/**
	 * Returns the {@link GlobalPropertyId} values
	 */
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds();

	/**
	 * Returns the property definition for the given {@link RegionPropertyId}
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId);

	/**
	 * Returns the {@link RegionPropertyId} values
	 */
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds();

	/**
	 * Returns the property definition for the given {@link CompartmentId} and
	 * {@link CompartmentPropertyId}
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the {@link CompartmentPropertyId} values for the given
	 * {@link CompartmentId}
	 */
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId);

	/**
	 * Returns the property definition for the given {@link ResourceId} and
	 * {@link ResourcePropertyId}
	 */
	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the {@link ResourcePropertyId} values for the given
	 * {@link ResourceId}
	 */
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId);

	/**
	 * Returns the {@link PersonPropertyId} values
	 */
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds();

	/**
	 * Returns the property definition for the given {@link PersonPropertyId}
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId);

	/**
	 * Returns the property definition for the given
	 * {@link MaterialsProducerPropertyId}
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the {@link MaterialsProducerPropertyId} values
	 */
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds();

	/**
	 * Returns the property definition for the given {@link MaterialId} and
	 * {@link BatchPropertyId}
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId);

	/**
	 * Returns the {@link BatchPropertyId} values for the given
	 * {@link MaterialId}
	 */
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId);

	/**
	 * Returns the property definition for the given {@link GroupTypeId} and
	 * {@link GroupPropertyId}
	 */
	public PropertyDefinition getGroupPropertyDefinition(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId);

	/**
	 * Returns the {@link GroupPropertyId} values for the given
	 * {@link GroupTypeId}
	 */
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(GroupTypeId groupTypeId);
}
