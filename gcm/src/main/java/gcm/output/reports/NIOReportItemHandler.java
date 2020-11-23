package gcm.output.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.experiment.Experiment;
import gcm.experiment.progress.ExperimentProgressLog;
import gcm.output.NIOHeaderedOutputItemHandler;
import gcm.output.OutputItem;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

@Source(status = TestStatus.UNEXPECTED)

/**
 * An implementor of ReportItemHandler that uses java.nio framework. Reports are
 * headered, tab-delimited files.
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOReportItemHandler implements ReportItemHandler {

	private static class LineWriter extends NIOHeaderedOutputItemHandler {

		private final String experimentHeader;
		private final List<String> experimentFields = new ArrayList<>();
		private final Experiment regularExperiment;
		private final boolean displayExperimentColumnsInReports;

		public LineWriter(final Path path, final Experiment regularExperiment,
				final boolean displayExperimentColumnsInReports, final ExperimentProgressLog experimentProgressLog) {
			super(path);

			this.displayExperimentColumnsInReports = displayExperimentColumnsInReports;

			this.regularExperiment = regularExperiment;

			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					experimentFields.add(regularExperiment.getExperimentFieldName(i));
				}
				final StringBuilder sb = new StringBuilder();
				for (final String experimentField : experimentFields) {
					sb.append("\t");
					sb.append(experimentField);
				}
				experimentHeader = sb.toString();
			} else {
				experimentHeader = "";
			}

		}

		@Override
		public Set<Class<? extends OutputItem>> getHandledClasses() {
			final Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
			result.add(ReportItem.class);
			return result;
		}

		@Override
		protected String getHeader(final OutputItem outputItem) {
			final ReportItem reportItem = (ReportItem) outputItem;
			final StringBuilder sb = new StringBuilder();
			sb.append("Scenario");
			sb.append("\t");
			sb.append("Replication");
			sb.append(experimentHeader);
			final List<String> headerStrings = reportItem.getReportHeader().getHeaderStrings();
			for (final String headerString : headerStrings) {
				sb.append("\t");
				sb.append(headerString);
			}
			return sb.toString();
		}

		@Override
		protected String getOutputLine(final OutputItem outputItem) {
			final ReportItem reportItem = (ReportItem) outputItem;
			final StringBuilder sb = new StringBuilder();
			sb.append(reportItem.getScenarioId());
			sb.append("\t");
			sb.append(reportItem.getReplicationId());

			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					sb.append("\t");
					final Object experimentFieldValue = regularExperiment
							.getExperimentFieldValue(reportItem.getScenarioId(), i);
					sb.append(experimentFieldValue);
				}
			}

			for (int i = 0; i < reportItem.size(); i++) {
				sb.append("\t");
				sb.append(reportItem.getValue(i));
			}
			return sb.toString();
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for NIOReportItemHandlerImpl
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Builder() {
		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Sets the path for the Experiment Column Report. Setting this path to null
		 * turns off the report. Default value is null.
		 */
		public Builder setExperimentColumnReport(final Path path) {
			scaffold.experimentColumnReportPath = path;
			return this;
		}

		/**
		 * Adds the experiment reference to the NIOReportItemHandler. Required if the
		 * Experiment Column Report is turned on. Default value is null.
		 */
		public Builder setRegularExperiment(final Experiment experiment) {
			scaffold.experiment = experiment;
			return this;
		}

		/**
		 * Add a report by class reference to the NIOReportItemHandler
		 * 
		 * @throws RuntimeException
		 *                          <li>if the path is null
		 *                          <li>if the report class is null
		 *                          <li>if the initialization data is null
		 *                          <li>if the initialization contains a null
		 */
		public Builder addReport(final Path path, final Class<? extends Report> reportClass,
				final Set<Object> initializationData) {
			if (path == null) {
				throw new RuntimeException("null path");
			}
			if (reportClass == null) {
				throw new RuntimeException("null report class");
			}
			if (initializationData == null) {
				throw new RuntimeException("null initialization data");
			}
			for (Object obj : initializationData) {
				if (obj == null) {
					throw new RuntimeException("null initialization data element");
				}
			}
			final ReportRec reportRec = new ReportRec();
			reportRec.path = path;
			reportRec.initializationData.addAll(initializationData);
			scaffold.reportMap.put(reportClass, reportRec);
			return this;
		}

		/**
		 * Builds the NIOReportItemHandlerImpl from the information gathered and resets
		 * the internal state of this builder.
		 */
		public NIOReportItemHandler build() {
			try {
				return new NIOReportItemHandler(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the display of experiment columns in all reports. Default value is true.
		 */
		public Builder setDisplayExperimentColumnsInReports(final boolean displayExperimentColumnsInReports) {
			scaffold.displayExperimentColumnsInReports = displayExperimentColumnsInReports;
			return this;
		}
	}

	private static class ReportRec {
		private Path path;
		private final Set<Object> initializationData = new LinkedHashSet<>();
	}

	private static class Scaffold {
		private final Map<Class<? extends Report>, ReportRec> reportMap = new LinkedHashMap<>();
		private Experiment experiment;
		private boolean displayExperimentColumnsInReports = DEFAULT_DISPLAY_EXPERIMENT_COLUMNS;
		private Path experimentColumnReportPath;

	}

	private final static boolean DEFAULT_DISPLAY_EXPERIMENT_COLUMNS = true;

	private static List<String> getLines(final Experiment experiment) {
		final List<String> result = new ArrayList<>();

		if (experiment != null) {

			/*
			 * Build the header line
			 */
			// final List<String> experimentFields =
			// experiment.getExperimentFields();
			StringBuilder sb = new StringBuilder();
			sb.append("Scenario");
			for (int i = 0; i < experiment.getExperimentFieldCount(); i++) {
				sb.append("\t");
				sb.append(experiment.getExperimentFieldName(i));
			}
			result.add(sb.toString());

			/*
			 * Build the scenario lines
			 */

			int scenarioCount = experiment.getScenarioCount();
			for (int i = 0; i < scenarioCount; i++) {
				final ScenarioId scenarioId = experiment.getScenarioId(i);
				sb = new StringBuilder();
				sb.append(scenarioId);

				for (int j = 0; j < experiment.getExperimentFieldCount(); j++) {
					sb.append("\t");
					final Object experimentFieldValue = experiment.getExperimentFieldValue(scenarioId, j);
					sb.append(experimentFieldValue);
				}
				result.add(sb.toString());
			}
		}
		return result;
	}

	private final Map<Object, LineWriter> lineWriterMap = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Path experimentColumnReportPath;

	private final Experiment experiment;

	private final Map<Class<? extends Report>, ReportRec> reportMap;

	private final boolean displayExperimentColumnsInReports;

	private NIOReportItemHandler(final Scaffold scaffold) {
		experimentColumnReportPath = scaffold.experimentColumnReportPath;
		experiment = scaffold.experiment;
		reportMap = scaffold.reportMap;
		displayExperimentColumnsInReports = scaffold.displayExperimentColumnsInReports;
	}

	@Override
	public void closeExperiment() {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.closeExperiment();
			}
		}
	}

	@Override
	public void closeSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.closeSimulation(scenarioId, replicationId);
			}
		}
	}

	@Override
	public Set<Class<? extends OutputItem>> getHandledClasses() {
		final Set<Class<? extends OutputItem>> result = new LinkedHashSet<>();
		result.add(ReportItem.class);
		return result;
	}

	@Override
	public Set<Object> getInitializationData(final Report report) {
		synchronized (lineWriterMap) {
			if (report == null) {
				throw new RuntimeException("null report instance");
			}
			final ReportRec reportRec = reportMap.get(report.getClass());
			if (reportRec == null) {
				throw new RuntimeException("Unknown report class " + report.getClass());
			}
			return new LinkedHashSet<>(reportRec.initializationData);
		}
	}

	@Override
	public Set<Report> getReports() {
		synchronized (lineWriterMap) {
			final Set<Report> result = new LinkedHashSet<>();
			for (final Class<? extends Report> reportClass : reportMap.keySet()) {
				try {
					result.add(reportClass.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
			return result;
		}
	}

	@Override
	public void handle(final OutputItem outputItem) {
		final ReportItem reportItem = (ReportItem) outputItem;
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportType());
		if (lineWriter != null) {
			lineWriter.handle(reportItem);
		}
	}

	@Override
	public void openExperiment(final ExperimentProgressLog experimentProgressLog) {
		synchronized (lineWriterMap) {
			if (experimentColumnReportPath != null) {
				writeExperimentScenarioReport(experiment);
			}
			/*
			 * Ensure that each path is associated with exactly one report id
			 */
			final Map<Path, Class<? extends Report>> pathMap = new LinkedHashMap<>();
			for (final Class<? extends Report> reportClass : reportMap.keySet()) {
				final ReportRec reportRec = reportMap.get(reportClass);
				final Path path = reportRec.path;
				if (pathMap.containsKey(path)) {
					throw new RuntimeException(path + " is selected for mutiple report id values");
				}
				pathMap.put(path, reportClass);
			}

			for (final Class<? extends Report> reportClass : reportMap.keySet()) {
				final ReportRec reportRec = reportMap.get(reportClass);
				final Path path = reportRec.path;
				final LineWriter lineWriter = new LineWriter(path, experiment, displayExperimentColumnsInReports,
						experimentProgressLog);
				lineWriter.openExperiment(experimentProgressLog);
				lineWriterMap.put(reportClass, lineWriter);
			}
		}
	}

	@Override
	public void openSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		// do nothing
	}

	private void writeExperimentScenarioReport(final Experiment experiment) {
		final List<String> lines = getLines(experiment);
		writeLines(lines);
	}

	private void writeLines(final List<String> lines) {
		try {
			Files.write(experimentColumnReportPath, lines, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
