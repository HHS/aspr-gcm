package gcm.output.reports;

import java.util.Set;

import gcm.output.OutputItemHandler;
import gcm.util.annotations.Source;

/**
 * An OutputItemHandler for handling all ReportItems that maps those items into
 * headered, tab-delimited files using the java.nio framework. The
 * NIOReportItemHandler supplies Report instances that are created in the
 * simulation's thread. It supplies the initialization data needed for each
 * report in the form of thread safe data objects.
 * 
 * @author Shawn Hatch
 *
 */

@Source
public interface NIOReportItemHandler extends OutputItemHandler {

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
