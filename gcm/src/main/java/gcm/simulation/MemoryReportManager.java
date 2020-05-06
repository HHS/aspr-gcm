package gcm.simulation;

import gcm.output.simstate.MemoryReportItem;

/**
 * The manager for generating the {@link MemoryReportItem} objects that form the
 * memory report. It relies on the {@link ComponentManager} creating an instance
 * of the the {@link MemoryReportComponent} that in turn stimulates this manager
 * via repeated plan execution.
 * 
 * @author Shawn Hatch
 *
 */
public interface MemoryReportManager extends Element {

	/**
	 * Causes the generation of memory report items
	 */
	public void generateMemoryReport();

}
