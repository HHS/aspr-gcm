package gcm.components;

import gcm.output.reports.GroupInfo;
import gcm.scenario.CompartmentId;
import gcm.scenario.CompartmentPropertyId;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.GroupId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.MaterialsProducerId;
import gcm.scenario.MaterialsProducerPropertyId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.scenario.ResourcePropertyId;
import gcm.scenario.StageId;
import gcm.simulation.Environment;
import gcm.simulation.Plan;
import gcm.util.annotations.Source;

/**
 * An abstract implementation of the Component interface that throws a
 * RunTimeException for all methods except for init() and close() methods. Init() remains
 * unimplemented here and close() is an empty implementation.
 *
 * Modelers who write descendant classes should override each method that
 * corresponds to a data change that their component registers to observe.
 *
 * @author Shawn Hatch
 *
 */
@Source
public abstract class AbstractComponent implements Component {
	/**
	 * Placeholder convenience implementation that does nothing if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void close(Environment environment) {
		
	}
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void executePlan(final Environment environment,final Plan plan) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeCompartmentPersonArrival(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeCompartmentPersonDeparture(final Environment environment,final CompartmentId compartmentId, final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeCompartmentPropertyChange(final Environment environment,final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGlobalPersonArrival(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGlobalPersonDeparture(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGlobalPropertyChange(final Environment environment,final GlobalPropertyId globalPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeMaterialsProducerPropertyChange(final Environment environment,final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeMaterialsProducerResourceChange(final Environment environment,final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observePersonCompartmentChange(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observePersonPropertyChange(final Environment environment,final PersonId personId, final PersonPropertyId personPropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observePersonRegionChange(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observePersonResourceChange(final Environment environment,final PersonId personId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeRegionPersonArrival(final Environment environment,final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeRegionPersonDeparture(final Environment environment,final RegionId regionId, final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeRegionPropertyChange(final Environment environment,final RegionId regionId, final RegionPropertyId regionPropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeRegionResourceChange(final Environment environment,final RegionId regionId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeResourcePropertyChange(final Environment environment,final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeStageOfferChange(final Environment environment,final StageId stageId) {
		throwNoConcreteImplementation();

	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeStageTransfer(final Environment environment,final StageId stageId,MaterialsProducerId sourceMaterialsProducerId,MaterialsProducerId destinationMaterialsProducerId) {
		throwNoConcreteImplementation();

	}

	private void throwNoConcreteImplementation() {
		throw new RuntimeException("No concrete implementation " + getClass().getName());
	}

	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGroupConstruction(Environment environment, GroupId groupId){
		throwNoConcreteImplementation();
	}
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGroupDestruction(Environment environment, GroupInfo groupInfo){
		throwNoConcreteImplementation();
	}
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGroupPropertyChange(Environment environment, GroupId groupId,GroupPropertyId groupPropertyId){
		throwNoConcreteImplementation();
	}
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGroupPersonArrival(Environment environment, GroupId groupId,PersonId personId){
		throwNoConcreteImplementation();
	}
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	@Override
	public void observeGroupPersonDeparture(Environment environment, GroupId groupId,PersonId personId){
		throwNoConcreteImplementation();
	}
	

	
	
	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	public void observePartitionPersonAddition(Environment environment, Object key, PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * Placeholder convenience implementation that throws an
	 * {@link RuntimeException} if invoked.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	public void observePartitionPersonRemoval(Environment environment, Object key, PersonId personId) {
		throwNoConcreteImplementation();
	}
	


}
