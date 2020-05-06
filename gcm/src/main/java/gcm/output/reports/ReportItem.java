package gcm.output.reports;

import java.util.ArrayList;
import java.util.List;

import gcm.output.OutputItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * A thread safe(immutable), container for data values having scenario and
 * replication ids. The values contained in a report item should be immutable
 * and support toString().
 *
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
@Source(status = TestStatus.UNEXPECTED)
public final class ReportItem implements OutputItem {

	@NotThreadSafe
	public final static class ReportItemBuilder {
		private Scaffold scaffold = new Scaffold();

		/**
		 * Adds a value to the report item. Order should follow the order in the
		 * {@link ReportHeader}
		 */
		public void addValue(final Object value) {
			scaffold.values.add(value.toString());
		}

		/*
		 * Null checks for the various fields.
		 */
		private void validateData() {
			if (scaffold.replicationId == null) {
				throw new RuntimeException("null replication id");
			}
			if (scaffold.reportHeader == null) {
				throw new RuntimeException("null report header");
			}
			if (scaffold.reportType == null) {
				throw new RuntimeException("null report type");
			}
			if (scaffold.scenarioId == null) {
				throw new RuntimeException("null scenario id");
			}
			if (scaffold.values == null) {
				throw new RuntimeException("null values");
			}
		}

		/**
		 * Builds the {@link ReportItem} from the colleced data.
		 */
		public ReportItem build() {
			try {
				validateData();
				return new ReportItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the associated {@link ReportHeader} for this {@link ReportItem}.
		 * The report header and the report item should have the same order of
		 * added fiels values.
		 */
		public void setReportHeader(ReportHeader reportHeader) {
			scaffold.reportHeader = reportHeader;
		}

		/**
		 * Sets the {@link ReplicationId}
		 */
		public void setReplicationId(final ReplicationId replicationId) {
			scaffold.replicationId = replicationId;
		}

		/**
		 * Sets the report type for this {@link ReportItem}. The report type
		 * should be the class type of the report that authors the report item.
		 */
		public void setReportType(final Class<? extends Report> reportType) {
			scaffold.reportType = reportType;
		}

		/**
		 * Sets the {@link ScenarioId}
		 */
		public void setScenarioId(final ScenarioId scenarioId) {
			scaffold.scenarioId = scenarioId;
		}
	}

	private static class Scaffold {
		private Class<? extends Report> reportType;
		private ReplicationId replicationId;
		private ScenarioId scenarioId;
		private ReportHeader reportHeader;
		private final List<String> values = new ArrayList<>();
	}

	private final ReplicationId replicationId;

	private final Class<? extends Report> reportType;

	private final ScenarioId scenarioId;

	private final List<String> values;

	private final ReportHeader reportHeader;

	private ReportItem(final Scaffold scaffold) {
		reportType = scaffold.reportType;
		replicationId = scaffold.replicationId;
		scenarioId = scaffold.scenarioId;
		reportHeader = scaffold.reportHeader;
		values = scaffold.values;
	}

	/**
	 * Returns the replication id of the simulation run that generated this
	 * ReportItem
	 *
	 * @return
	 */

	@Override
	public ReplicationId getReplicationId() {
		return replicationId;
	}

	/**
	 * Returns the report class type of the report that generated this
	 * ReportItem. This identifier aids in the mapping of this ReportItem to the
	 * appropriate file.
	 * 
	 * @return
	 */
	public Class<? extends Report> getReportType() {
		return reportType;
	}

	/**
	 * Returns the scenario id of the simulation run that generated this
	 * ReportItem
	 *
	 * @return
	 */
	@Override
	public ScenarioId getScenarioId() {
		return scenarioId;
	}

	public ReportHeader getReportHeader() {
		return reportHeader;
	}

	/**
	 * Returns the string value stored at the given index
	 *
	 * @param index
	 * @return
	 */
	public String getValue(final int index) {
		return values.get(index);
	}

	/**
	 * Returns the number of values stored in this report item
	 *
	 * @return
	 */
	public int size() {
		return values.size();
	}

	/**
	 * A string listing the values as added to this ReportItem delimited by
	 * commas in the form:
	 * 
	 * ReportItem
	 * [replicationId=replicationId,reportType=reportType,scenarioId=scenarioId,reportHeader=reportHeader,values=[value1,
	 * value2...]]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportItem [replicationId=");
		builder.append(replicationId);
		builder.append(", reportType=");
		builder.append(reportType);
		builder.append(", scenarioId=");
		builder.append(scenarioId);
		builder.append(", reportHeader=");
		builder.append(reportHeader);
		builder.append(", values=");
		builder.append(values);
		builder.append("]");
		return builder.toString();
	}

}
