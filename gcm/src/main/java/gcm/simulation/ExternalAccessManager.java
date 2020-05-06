package gcm.simulation;

import gcm.util.annotations.Source;

/**
 * A manager used by GCM to control the read and write access via the
 * Environment. The Environment is used exclusively by modeler contributed
 * classes. As GCM resolves mutation requests and data queries it could
 * potentially encounter temporary invariant condition violations and must guard
 * against access to its inconsistent state.
 * 
 * GCM has many modeler contributed items such as filters, components and a host
 * of identifiers. During a mutation, GCM may call back to these contributed
 * items resulting in an item querying or mutating the state of GCM while GCM is
 * in an inconsistent state. It can be difficult to reason out in detail when
 * this can occur since GCM uses many modeler-supplied identifiers that are used
 * as keys. Key classes have their hashcode() and equals() methods invoked
 * outside of GCM's control and thus can lead to callbacks.
 * 
 * A common solution is to create a guard variable for each invariant condition
 * and its associated data structures. In practice, this can be difficult to
 * maintain. Often invariant conditions might cross over natural boundaries
 * within GCM's various sub-management classes. Finally, these guards themselves
 * would likely have to be managed in data structures that are keyed with
 * modeler generated identifiers and are thus subject to callbacks, further
 * complicating the reasoning needed to assure that an inconsistent state cannot
 * be accessed.
 * 
 * This class helps GCM implement a global level solution to this problem by
 * creating a read and write access system in the {@link EnvironmentImpl}, the
 * {@link MutationResolver} and the {@link Context}
 * 
 * The general rules are:
 * 
 * GCM can block both read and write access from the Environment. This prevents
 * contributed components from 1) accessing data during the early phase of
 * simulation initialization, 2) writing data after simulation termination and
 * 3) reading and writing data during mutations WHEN THERE IS NO DOCUMENTED NEED
 * for such callbacks.
 * 
 * GCM will allow at most one ongoing write activity. Mutations must be fully
 * resolved before another mutation is attempted.
 * 
 * GCM will allow nested reads of data.  Any ongoing read activity will block all write attempts.
 * 
 * Violations of these rules result in {@link RuntimeException}s
 *   
 * @author Shawn Hatch
 *
 */
@Source
public interface ExternalAccessManager extends Element {

	/**
	 * Releases read access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if there are no ongoing reads
	 */
	public void releaseReadAccess();
	/**
	 * Releases write access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if there is no ongoing write
	 */
	public void releaseWriteAccess();
	/**
	 * Acquires read access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if global read access is locked
	 */
	public void acquireReadAccess();
	/**
	 * Acquires write access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if either global write access is locked, or there is an
	 *             ongoing write or an ongoing read
	 */
	public void acquireWriteAccess();
	/**
	 * Stops all writes coming through the Environment.
	 * 
	 * @throws RuntimeException
	 *             if global write access is already locked
	 */
	public void acquireGlobalWriteAccessLock();
	/**
	 * Allows all writes coming through the Environment, unless blocked by an
	 * ongoing write or read.
	 * 
	 * @throws RuntimeException
	 *             if the global write access lock is not locked
	 */
	public void releaseGlobalWriteAccessLock();
	/**
	 * Stops all reads coming through the Environment.
	 * 
	 * @throws RuntimeException
	 *             if global read access is locked
	 */
	public void acquireGlobalReadAccessLock();
	/**
	 * Allows all reads coming through the Environment, unless blocked by an
	 * ongoing write.
	 * 
	 * @throws RuntimeException
	 *             if global read access is not locked
	 */
	public void releaseGlobalReadAccessLock();

}
