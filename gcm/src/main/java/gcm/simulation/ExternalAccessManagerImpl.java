package gcm.simulation;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * 
 * Implementor of {@link ExternalAccessManager}
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED,proxy = EnvironmentImpl.class)
public final class ExternalAccessManagerImpl extends BaseElement implements ExternalAccessManager{

	/*
	 * When true, all writes are blocked. This is used to turn off all writing
	 * of data from model contributed classes during the early stage of
	 * initialization and after all event are processed and the simulation is
	 * complete. Note that this does not represent the blocking due to an
	 * ongoing write.
	 */
	private boolean globalWriteAccessLocked;

	/*
	 * When true, all reads are blocked. This is used to turn off all reading of
	 * data from model contributed classes during the early stage of
	 * initialization and during most mutations. Mutations that require the
	 * participation of a model contributed class such as a filter may opt to
	 * not block reads in this way.
	 */
	private boolean globalReadAccessLocked;

	/*
	 * When true there is an ongoing write from the Environment.
	 */
	private boolean isWriting;
	/*
	 * The number of simultaneous nested reads. Can be any non-negative value.
	 */
	private int readDepth;

	/**
	 * Releases read access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if there are no ongoing reads
	 */
	@Override
	public void releaseReadAccess() {
		if (readDepth > 0) {
			readDepth--;
		} else {
			throw new RuntimeException("cannot decrement read depth");
		}
	}

	/**
	 * Releases write access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if there is no ongoing write
	 */
	@Override
	public void releaseWriteAccess() {
		if (isWriting) {
			isWriting = false;
		} else {
			throw new RuntimeException("cannot decrement write depth");
		}
	}

	/**
	 * Acquires read access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if global read access is locked
	 */
	@Override
	public void acquireReadAccess() {
		if (globalReadAccessLocked) {
			throw new RuntimeException("A contributed element is attempting to read data while read/write permission is blocked");
		}
		readDepth++;
	}

	/**
	 * Acquires write access for an environment method.
	 * 
	 * @throws RuntimeException
	 *             if either global write access is locked, or there is an
	 *             ongoing write or an ongoing read
	 */
	@Override
	public void acquireWriteAccess() {
		if (!globalWriteAccessLocked && (!isWriting) && (readDepth == 0)) {
			isWriting = true;
		} else {
			if (globalWriteAccessLocked) {
				throw new RuntimeException("A contributed element is attempting to write data while read/write permission is blocked");
			} else {
				throw new RuntimeException("A contributed element is attempting to write data while write permission is blocked due to an ongoing read or write");
			}
		}
	}

	/**
	 * Stops all writes coming through the Environment.
	 * 
	 * @throws RuntimeException
	 *             if global write access is already locked
	 */
	@Override
	public void acquireGlobalWriteAccessLock() {
		if (globalWriteAccessLocked) {
			throw new RuntimeException("global write access lock already acquired");
		}
		globalWriteAccessLocked = true;
	}

	/**
	 * Allows all writes coming through the Environment, unless blocked by an
	 * ongoing write or read.
	 * 
	 * @throws RuntimeException
	 *             if the global write access lock is not locked
	 */
	@Override
	public void releaseGlobalWriteAccessLock() {
		if (!globalWriteAccessLocked) {
			throw new RuntimeException("global write access lock already released");
		}

		globalWriteAccessLocked = false;
	}

	/**
	 * Stops all reads coming through the Environment.
	 * 
	 * @throws RuntimeException
	 *             if global read access is locked
	 */
	@Override
	public void acquireGlobalReadAccessLock() {
		if (globalReadAccessLocked) {
			throw new RuntimeException("global read access lock already acquired");
		}
		globalReadAccessLocked = true;
	}

	/**
	 * Allows all reads coming through the Environment, unless blocked by an
	 * ongoing write.
	 * 
	 * @throws RuntimeException
	 *             if global read access is not locked
	 */
	@Override
	public void releaseGlobalReadAccessLock() {
		if (!globalReadAccessLocked) {
			throw new RuntimeException("global read access lock already released");
		}
		globalReadAccessLocked = false;
	}


}
