package gcm.manual;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.OutputItemHandler;
import gcm.output.reports.ReportItemHandler;
import gcm.output.reports.NIOReportItemHandler;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportItem;
import gcm.output.reports.NIOReportItemHandler.Builder;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.output.reports.commonreports.CompartmentPopulationReport;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;

/**
 * A manual test that shows that the NIOReportItemHandler writes headers
 * properly under multiple threads depositing report items.
 * 
 * @author Shawn Hatch
 *
 */
public final class MT_NIOReportItemHandlerImpl {

	private MT_NIOReportItemHandlerImpl() {
	}

	public static void main(String[] args) {
		
		Path path = Paths.get(args[0]);		

		Builder nioReportItemHandlerBuilder = NIOReportItemHandler.builder();
		Set<Object> initialData = new LinkedHashSet<>();
		nioReportItemHandlerBuilder.addReport(path, CompartmentPopulationReport.class, initialData);
		ReportItemHandler nioReportItemHandler = nioReportItemHandlerBuilder.build();

		ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
		reportHeaderBuilder.add("Alpha");
		reportHeaderBuilder.add("Beta");

		ReportHeader reportHeader = reportHeaderBuilder.build();

		int jobCount = 10;
		JobCompletionCounter jobCompletionCounter = new JobCompletionCounter(jobCount, nioReportItemHandler);

		for (int i = 0; i < jobCount; i++) {
			new Thread(new Runner(reportHeader, i, nioReportItemHandler, jobCompletionCounter)).start();
		}

	}

	private static class JobCompletionCounter {
		private final OutputItemHandler outputItemHandler;

		public JobCompletionCounter(int jobs, OutputItemHandler outputItemHandler) {
			this.jobs = jobs;
			this.outputItemHandler = outputItemHandler;
		}

		private int jobs = 0;

		public synchronized void decrementJobs() {
			jobs--;
			if (jobs == 0) {
				outputItemHandler.closeExperiment();
			}
		}

	}

	private static class Runner implements Runnable {
		private final ReportHeader reportHeader;
		private final Integer index;
		private final ReportItemHandler nioReportItemHandler;
		private final JobCompletionCounter jobCompletionCounter;

		public Runner(ReportHeader reportHeader, Integer index, ReportItemHandler nioReportItemHandler, JobCompletionCounter jobCompletionCounter) {
			this.reportHeader = reportHeader;
			this.index = index;
			this.nioReportItemHandler = nioReportItemHandler;
			this.jobCompletionCounter = jobCompletionCounter;
		}

		@Override
		public void run() {
			for (int j = 0; j < 30; j++) {
				ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
				reportItemBuilder.setReplicationId(new ReplicationId(342));
				reportItemBuilder.setReportHeader(reportHeader);
				reportItemBuilder.setReportType(CompartmentPopulationReport.class);
				reportItemBuilder.setScenarioId(new ScenarioId(552));
				reportItemBuilder.addValue(index);
				reportItemBuilder.addValue(j);

				ReportItem reportItem = reportItemBuilder.build();
				nioReportItemHandler.handle(reportItem);
			}
			jobCompletionCounter.decrementJobs();
		}

	}
}
