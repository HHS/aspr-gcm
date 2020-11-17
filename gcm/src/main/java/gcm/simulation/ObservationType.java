package gcm.simulation;

import gcm.components.Component;
import gcm.output.reports.GroupInfo;
import gcm.output.reports.Report;
import gcm.output.reports.StateChange;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.PersonId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * An enumeration representing the data content changes that are observable by
 * components. Note that the focus here is not on the events that cause the
 * changes as in {@link StateChange} enumeration that supports {@link Report}
 * reports. Rather, the focus is on what changed and usually not why it changed.
 * 
 * Observations going to components are delayed until the current component has
 * completed its activation. This enumeration exists to facilitate the storage
 * of observed data for later release to the components as well as for the
 * registration of component interest in these observations.
 * 
 */
@Source(status = TestStatus.UNREQUIRED)
public enum ObservationType {
	/**
	 * Supports
	 * {@link Component#observeGlobalPropertyChange(gcm.scenario.GlobalPropertyId)}
	 */
	GLOBAL_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observeGlobalPersonArrival(gcm.scenario.PersonId)}
	 */
	GLOBAL_PERSON_ARRIVAL,

	/**
	 * Supports
	 * {@link Component#observeGlobalPersonDeparture(gcm.scenario.PersonId)}
	 */
	GLOBAL_PERSON_DEPARTURE,

	/**
	 * Supports
	 * {@link Component#observeRegionPropertyChange(gcm.scenario.RegionId, gcm.scenario.RegionPropertyId)}
	 */
	REGION_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observeRegionPersonArrival(gcm.scenario.PersonId)}
	 */
	REGION_PERSON_ARRIVAL,

	/**
	 * Supports
	 * {@link Component#observeRegionPersonDeparture(gcm.scenario.RegionId, gcm.scenario.PersonId)}
	 */
	REGION_PERSON_DEPARTURE,

	/**
	 * Supports
	 * {@link Component#observeRegionResourceChange(gcm.scenario.RegionId, gcm.scenario.ResourceId)}
	 */
	REGION_RESOURCE,

	/**
	 * Supports
	 * {@link Component#observeResourcePropertyChange(gcm.scenario.ResourceId, gcm.scenario.ResourcePropertyId)}
	 */
	RESOURCE_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observeCompartmentPropertyChange(gcm.scenario.CompartmentId, gcm.scenario.CompartmentPropertyId)}
	 */
	COMPARTMENT_PROPERTY,
	/**
	 * Supports
	 * {@link Component#observeCompartmentPersonArrival(gcm.scenario.PersonId)}
	 */
	COMPARTMENT_PERSON_ARRIVAL,

	/**
	 * Supports
	 * {@link Component#observeCompartmentPersonDeparture(gcm.scenario.CompartmentId, gcm.scenario.PersonId)}
	 */
	COMPARTMENT_PERSON_DEPARTURE,

	/**
	 * Supports
	 * {@link Component#observeMaterialsProducerPropertyChange(gcm.scenario.MaterialsProducerId, gcm.scenario.MaterialsProducerPropertyId)}
	 */
	MATERIALS_PRODUCER_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observeMaterialsProducerResourceChange(gcm.scenario.MaterialsProducerId, gcm.scenario.ResourceId)}
	 */
	MATERIALS_PRODUCER_RESOURCE,

	/**
	 * Supports
	 * {@link Component#observePersonPropertyChange(gcm.scenario.PersonId, gcm.scenario.PersonPropertyId)}
	 */
	PERSON_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observePersonRegionChange(gcm.scenario.PersonId)}
	 */
	PERSON_REGION,

	/**
	 * Supports
	 * {@link Component#observePersonCompartmentChange(gcm.scenario.PersonId)}
	 */
	PERSON_COMPARTMENT,

	/**
	 * Supports
	 * {@link Component#observePersonResourceChange(gcm.scenario.PersonId, gcm.scenario.ResourceId)}
	 */
	PERSON_RESOURCE,

	/**
	 * Supports
	 * {@link Component#observeStageTransfer(gcm.scenario.StageId)}
	 */
	STAGE_TRANSFER,

	/**
	 * Supports
	 * {@link Component#observeStageOfferChange(gcm.scenario.StageId)}
	 */
	STAGE_OFFER,

	/**
	 * Supports {@link Component#observeGroupConstruction(Environment, GroupId)}
	 */
	GROUP_CONSTRUCTION,

	/**
	 * Supports
	 * {@link Component#observeGroupDestruction(Environment, GroupInfo)}
	 */
	GROUP_DESTRUCTION,

	/**
	 * Supports
	 * {@link Component#observeGroupPropertyChange(Environment, GroupId, GroupPropertyId)}
	 */
	GROUP_PROPERTY,

	/**
	 * Supports
	 * {@link Component#observeGroupPersonArrival(Environment, GroupId, PersonId)}
	 */
	GROUP_PERSON_ARRIVAL,

	/**
	 * Supports
	 * {@link Component#observeGroupPersonDeparture(Environment, GroupId, PersonId)}
	 */
	GROUP_PERSON_DEPARTURE,
	
	/**
	 * Supports
	 * {@link Component#observePartitionPersonAddition(Environment, Object, PersonId)}
	 */
	PARTITION_PERSON_ADDITION,

	/**
	 * Supports
	 * {@link Component#observePartitionPersonRemoval(Environment, Object, PersonId)}
	 */
	PARTITION_PERSON_REMOVAL;

}
