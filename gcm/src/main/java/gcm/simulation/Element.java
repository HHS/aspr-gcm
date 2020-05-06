package gcm.simulation;

import gcm.util.annotations.Source;

/**
 * Elements represent all of the major, top level parts of the simulation and
 * are context aware, unlike components. They also participate in memory
 * reporting.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public interface Element extends MemoryReportParticipant {
	/**
	 * Invoked at the startup of the simulation by the Context. Elements
	 * typically establish connection to other elements and prepare to serve the
	 * needs of the components.
	 */
	public void init(Context context);
}
