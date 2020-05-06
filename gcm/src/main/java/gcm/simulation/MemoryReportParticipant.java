package gcm.simulation;

import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;

/**
 * 
 * Interface defining all simulation objects that need to differentiate child
 * objects in the memory report. The memory partition is passed from parent to
 * child starting with the {@link Context} and thus any implementor of this
 * interface must be known to the natural owner of that implementor. This is
 * generally restricted to classes that are defined fully in GCM and not in
 * modeler contributed content.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface MemoryReportParticipant {

	/**
	 * Implementors agree to add memory links to the MemoryPartition that
	 * delineates their ownership of child objects.
	 */
	public void collectMemoryLinks(MemoryPartition memoryPartition);
}
