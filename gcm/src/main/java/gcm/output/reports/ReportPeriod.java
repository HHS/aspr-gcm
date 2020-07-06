package gcm.output.reports;

import gcm.output.reports.commonreports.PeriodicReport;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * An enumeration supporting {@link PeriodicReport} that represents the
 * periodicity of the report.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNREQUIRED)
public enum ReportPeriod {
	HOURLY, DAILY, END_OF_SIMULATION
}
