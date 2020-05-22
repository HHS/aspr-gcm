package gcm.output.reports;

import java.util.Set;

import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;

/**
 * An OutputItemHandler for handling all ReportItems that are released from the
 * simulation.
 * 
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface ReportItemHandler extends OutputItemHandler {

	/**
	 * Returns the set of thread-safe data objects that can be used to
	 * initialize the given report.
	 * 
	 * @throws RuntimeException
	 *             if the report is null
	 * @throws RuntimeException
	 *             if the report's class does not match one of the reports this
	 *             NIOReportItemHandler supplies
	 */
	public Set<Object> getInitializationData(Report report);

	/**
	 * Returns the set of Reports as fresh instances. The reports are designed
	 * to be in a thread-contained life cycle.
	 */
	public Set<Report> getReports();

}
