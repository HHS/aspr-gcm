package gcm.output.reports.commonreports;

import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.ReportPeriod;
import gcm.simulation.ObservableEnvironment;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * The abstract base class for reports that aggregate reporting aligned to
 * {@link ReportPeriod}
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public abstract class PeriodicReport extends AbstractReport {

	/*
	 * Assume a daily report period and let it be overridden
	 */
	private ReportPeriod reportPeriod = ReportPeriod.DAILY;

	/*
	 * The day value to be used in report lines
	 */
	private Integer reportingDay = 0;

	/*
	 * The hour value to be used in report lines
	 */
	private Integer reportingHour = 0;

	/*
	 * The report starts in an open state
	 */
	private boolean closed;

	/**
	 * Adds the time field column(s) to the given {@link ReportHeaderBuilder} as
	 * appropriate to the {@link ReportPeriod} specified during construction.
	 */
	protected ReportHeader.Builder addTimeFieldHeaders(ReportHeader.Builder reportHeaderBuilder) {
		switch (reportPeriod) {
		case DAILY:
			reportHeaderBuilder.add("Day");
			break;
		case END_OF_SIMULATION:
			// do nothing
			break;
		case HOURLY:
			reportHeaderBuilder.add("Day");
			reportHeaderBuilder.add("Hour");
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
		return reportHeaderBuilder;
	}

	/**
	 * Places the current reporting day and report hour on the report as
	 * appropriate to the {@link ReportPeriod} specified during construction.
	 *
	 */
	protected final void buildTimeFields(final ReportItem.Builder reportItemBuilder) {
		switch (reportPeriod) {
		case DAILY:
			reportItemBuilder.addValue(reportingDay);
			break;
		case END_OF_SIMULATION:
			// do nothing
			break;
		case HOURLY:
			reportItemBuilder.addValue(reportingDay);
			reportItemBuilder.addValue(reportingHour % 24);
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
	}

	@Override
	public void init(ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);
		for (Object initialDatum : initialData) {
			if (initialDatum instanceof ReportPeriod) {
				this.reportPeriod = (ReportPeriod) initialDatum;
			}
		}
	}

	@Override
	public void close(ObservableEnvironment observableEnvironment) {
		closed = true;
		setCurrentReportingPeriod(observableEnvironment);
	}

	/**
	 * Creates and releases report items from the data stored during the time
	 * since the last invocation of flush();
	 */
	protected abstract void flush(ObservableEnvironment observableEnvironment);

	/**
	 * Sets the current day and time by repeatedly incrementing these values
	 * until the reach the current simulation time. During each iteration,
	 * flush() is invoked as is appropriate to the ReportPeriod
	 */
	protected final void setCurrentReportingPeriod(ObservableEnvironment observableEnvironment) {

		double time = observableEnvironment.getTime();
		final int day = (int) time;
		final int hour = (int) (time * 24);
		boolean flushed = false;

		switch (reportPeriod) {
		case DAILY:
			while (day > reportingDay) {
				flush(observableEnvironment);
				flushed = true;
				reportingDay++;
			}
			break;
		case END_OF_SIMULATION:
			if (closed) {
				reportingDay = day;
				reportingHour = hour;
			}
			break;
		case HOURLY:
			while (hour > reportingHour) {
				flush(observableEnvironment);
				flushed = true;
				reportingHour++;
				if ((reportingHour % 24) == 0) {
					reportingDay++;
				}
			}
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
		if (closed && !flushed) {
			flush(observableEnvironment);
		}

	}

}
