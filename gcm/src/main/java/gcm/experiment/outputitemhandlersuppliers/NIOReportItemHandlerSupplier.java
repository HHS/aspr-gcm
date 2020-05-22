package gcm.experiment.outputitemhandlersuppliers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import gcm.experiment.Experiment;
import gcm.output.OutputItemHandler;
import gcm.output.reports.NIOReportItemHandler;
import gcm.output.reports.Report;
import gcm.output.reports.ReportPeriod;
import gcm.output.reports.commonreports.BatchStatusReport;
import gcm.output.reports.commonreports.CompartmentPopulationReport;
import gcm.output.reports.commonreports.CompartmentPropertyReport;
import gcm.output.reports.commonreports.CompartmentTransferReport;
import gcm.output.reports.commonreports.GlobalPropertyReport;
import gcm.output.reports.commonreports.GroupPopulationReport;
import gcm.output.reports.commonreports.GroupPropertyReport;
import gcm.output.reports.commonreports.GroupPropertyReport.GroupPropertyReportSettings;
import gcm.output.reports.commonreports.MaterialsProducerPropertyReport;
import gcm.output.reports.commonreports.MaterialsProducerResourceReport;
import gcm.output.reports.commonreports.PersonPropertyInteractionReport;
import gcm.output.reports.commonreports.PersonPropertyReport;
import gcm.output.reports.commonreports.PersonResourceReport;
import gcm.output.reports.commonreports.PersonResourceReport.PersonResourceReportOption;
import gcm.output.reports.commonreports.RegionPropertyReport;
import gcm.output.reports.commonreports.RegionTransferReport;
import gcm.output.reports.commonreports.ResourcePropertyReport;
import gcm.output.reports.commonreports.ResourceReport;
import gcm.output.reports.commonreports.StageReport;
import gcm.scenario.GlobalPropertyId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionPropertyId;
import gcm.scenario.ResourceId;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Multi-threaded executor of an experiment using replications, reports and
 * various settings that influence how the experiment is executed.
 * 
 * @author Shawn Hatch
 *
 */

@Source(status = TestStatus.UNEXPECTED)
public final class NIOReportItemHandlerSupplier implements Supplier<List<OutputItemHandler>> {
	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Scaffold {
		private Experiment experiment;
		private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
		private NIOReportItemHandler.Builder nioReportItemHandlerBuilder = NIOReportItemHandler.builder();
	}

	private final List<OutputItemHandler> outputItemHandlers;

	/*
	 * Hidden constructor
	 */
	private NIOReportItemHandlerSupplier(Scaffold scaffold) {
		this.outputItemHandlers = new ArrayList<>(scaffold.outputItemHandlers);

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {

		}

		
		public NIOReportItemHandlerSupplier build() {
			try {
			if (scaffold.experiment == null) {
				throw new RuntimeException("null experiment");
			}
			scaffold.nioReportItemHandlerBuilder.setRegularExperiment(scaffold.experiment);
			scaffold.outputItemHandlers.add(scaffold.nioReportItemHandlerBuilder.build());
			return new NIOReportItemHandlerSupplier(scaffold);
			}finally {
				scaffold = new Scaffold();
			}
		}

		private Scaffold scaffold = new Scaffold();

		

		/**
		 * Turns on or off the display of experiment columns in all reports.
		 * Default is true.
		 * 
		 * @param displayExperimentColumnsInReports
		 *            turns on/off the display of the experiment columns in all
		 *            reports
		 */
		public Builder setDisplayExperimentColumnsInReports(boolean displayExperimentColumnsInReports) {
			scaffold.nioReportItemHandlerBuilder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports);
			return this;
		}

		/**
		 * Adds a custom report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportClass
		 *            the class reference to a Report implementor that has an
		 *            empty constructor.
		 * 
		 * @param initializationData
		 *            the array of non-null,thread-safe objects that will be
		 *            used to initialize the report
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report class is null
		 *             <li>if the initialization data is null
		 *             <li>if any object in the initialization data is null
		 */
		public void addCustomReport(Path path, Class<? extends Report> reportClass, Object... initializationData) {
			Set<Object> initialData = new LinkedHashSet<>();
			for (Object initialDatum : initializationData) {
				initialData.add(initialDatum);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, reportClass, initialData);
		}

		/**
		 * Adds a custom report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportClass
		 *            the class reference to a Report implementor that has an
		 *            empty constructor.
		 * 
		 * @param initializationData
		 *            the collection of non-null,thread-safe objects that will
		 *            be used to initialize the report
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report class is null
		 *             <li>if the initialization data is null
		 *             <li>if any object in the initialization data is null
		 */
		public Builder addCustomReport(Path path, Class<? extends Report> reportClass, Collection<? extends Object> initializationData) {
			Set<Object> initialData = new LinkedHashSet<>(initializationData);
			scaffold.nioReportItemHandlerBuilder.addReport(path, reportClass, initialData);
			return this;
		}

		/**
		 * Adds an Experiment Column Report. This report is independent of the
		 * choice to include experiment columns in all other reports. A null
		 * path turns off the report. Default value is null.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 */
		public Builder addExperimentColumnReport(Path path) {
			scaffold.nioReportItemHandlerBuilder.setExperimentColumnReport(path);
			return this;
		}

		/**
		 * Adds a Batch Status Report.
		 *
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addBatchStatusReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();
			scaffold.nioReportItemHandlerBuilder.addReport(path, BatchStatusReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Compartment Population Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 */
		public Builder addCompartmentPopulationReport(Path path, ReportPeriod reportPeriod) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentPopulationReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Compartment Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addCompartmentPropertyReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();
			scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Compartment Transfer Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 */
		public Builder addCompartmentTransferReport(Path path, ReportPeriod reportPeriod) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			scaffold.nioReportItemHandlerBuilder.addReport(path, CompartmentTransferReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Global Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param globalPropertyIds
		 *            the array of {@link GlobalPropertyId} that will be
		 *            reported
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null *
		 *             <li>if the globalPropertyIds is null
		 *             <li>if member of the globalPropertyIds is null
		 * 
		 */
		public Builder addGlobalPropertyReport(Path path, GlobalPropertyId... globalPropertyIds) {
			Set<Object> initialData = new LinkedHashSet<>();

			if (globalPropertyIds == null) {
				throw new RuntimeException("null person Property Ids");
			}
			for (GlobalPropertyId globalPropertyId : globalPropertyIds) {
				if (globalPropertyId == null) {
					throw new RuntimeException("null global property");
				}
				initialData.add(globalPropertyId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, GlobalPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Global Population Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 */
		public Builder addGroupPopulationReport(Path path, ReportPeriod reportPeriod) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			scaffold.nioReportItemHandlerBuilder.addReport(path, GroupPopulationReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Group Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @param groupPropertyReportSettings
		 *            the {@link GroupPropertyReportSettings} for this report.
		 * 
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 *             <li>if the groupPropertyReportSettings is null
		 */
		public Builder addGroupPropertyReport(Path path, ReportPeriod reportPeriod, GroupPropertyReportSettings groupPropertyReportSettings) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			initialData.add(groupPropertyReportSettings);
			scaffold.nioReportItemHandlerBuilder.addReport(path, GroupPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Materials Producer Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addMaterialsProducerPropertyReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();			
			scaffold.nioReportItemHandlerBuilder.addReport(path, MaterialsProducerPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Materials Producer Resource Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addMaterialsProducerResourceReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();
			scaffold.nioReportItemHandlerBuilder.addReport(path, MaterialsProducerResourceReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Person Property Interaction Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @param personPropertyIds
		 *            the array of {@link PersonPropertyId} that will be
		 *            reported
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 *             <li>if the personPropertyIds is null
		 *             <li>if member of the personPropertyIds is null
		 */
		public Builder addPersonPropertyInteractionReport(Path path, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			if (personPropertyIds == null) {
				throw new RuntimeException("null person Property Ids");
			}
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				initialData.add(personPropertyId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, PersonPropertyInteractionReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Person Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @param personPropertyIds
		 *            the collection of {@link PersonPropertyId} that will be
		 *            reported. If none are specified, all person properties are
		 *            reported.
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 *             <li>if the personPropertyIds is null
		 *             <li>if member of the personPropertyIds is null
		 */
		public Builder addPersonPropertyReport(Path path, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			if (personPropertyIds == null) {
				throw new RuntimeException("null person Property Ids");
			}
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				if (personPropertyId == null) {
					throw new RuntimeException("null person property");
				}
				initialData.add(personPropertyId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, PersonPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Person Resource Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @param reportPeopleWithoutResources
		 *            turns on/off the reporting of people without a resource
		 * 
		 * @param reportZeroPopulations
		 *            turns on/off the reporting when the number of people
		 *            having a resource is zero
		 * 
		 * @param resourceIds
		 *            the array of {@link ResourceId} that will be reported. If
		 *            no resources are added, then all resources are assumed
		 *            active.
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 *             <li>if the resourceIds is null
		 *             <li>if member of the resourceIds is null
		 */
		public Builder addPersonResourceReport(Path path, ReportPeriod reportPeriod, boolean reportPeopleWithoutResources, boolean reportZeroPopulations, ResourceId... resourceIds) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			if (reportPeopleWithoutResources) {
				initialData.add(PersonResourceReportOption.REPORT_PEOPLE_WITHOUT_RESOURCES);
			}
			if (reportZeroPopulations) {
				initialData.add(PersonResourceReportOption.REPORT_ZERO_POPULATIONS);
			}
			if (resourceIds == null) {
				throw new RuntimeException("null resouce ids");
			}
			for (ResourceId resourceId : resourceIds) {
				initialData.add(resourceId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, PersonResourceReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Region Property Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param regionPropertyIds
		 *            the array of {@link RegionPropertyId} that will be
		 *            reported
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null *
		 *             <li>if the regionPropertyIds is null
		 *             <li>if member of the regionPropertyIds is null
		 * 
		 */
		public Builder addRegionPropertyReport(Path path, RegionPropertyId... regionPropertyIds) {
			Set<Object> initialData = new LinkedHashSet<>();

			if (regionPropertyIds == null) {
				throw new RuntimeException("null region Property Ids");
			}
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				if (regionPropertyId == null) {
					throw new RuntimeException("null region property");
				}
				initialData.add(regionPropertyId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, RegionPropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Region Transfer Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 */
		public Builder addRegionTransferReport(Path path, ReportPeriod reportPeriod) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			scaffold.nioReportItemHandlerBuilder.addReport(path, RegionTransferReport.class, initialData);
			return this;
		}
		

		/**
		 * Adds a Resource Property Report.
		 * 
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addResourcePropertyReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();
			scaffold.nioReportItemHandlerBuilder.addReport(path, ResourcePropertyReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Resource Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @param reportPeriod
		 *            the reporting interval for this report
		 * 
		 * 
		 * @param resourceIds
		 *            the array of {@link ResourceId} that will be reported
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report period is null
		 *             <li>if the resourceIds is null
		 *             <li>if member of the resourceIds is null
		 */
		public Builder addResourceReport(Path path, ReportPeriod reportPeriod, ResourceId... resourceIds) {
			Set<Object> initialData = new LinkedHashSet<>();
			initialData.add(reportPeriod);
			for (ResourceId resourceId : resourceIds) {
				initialData.add(resourceId);
			}
			scaffold.nioReportItemHandlerBuilder.addReport(path, ResourceReport.class, initialData);
			return this;
		}

		/**
		 * Adds a Stage Report.
		 * 
		 * @param path
		 *            the {@link Path} where the report will be recorded
		 * 
		 * @throws RuntimeException
		 *             <li>if the path is null
		 */
		public Builder addStageReport(Path path) {
			Set<Object> initialData = new LinkedHashSet<>();
			scaffold.nioReportItemHandlerBuilder.addReport(path, StageReport.class, initialData);
			return this;
		}

		/**
		 * Adds the given scenarios to the experiment
		 *
		 * @param experiment
		 *            the experiment to be executed
		 */
		public Builder setExperiment(final Experiment experiment) {
			if (experiment == null) {
				throw new RuntimeException("null experiment");
			}
			scaffold.experiment = experiment;
			return this;
		}
	}

	/**
	 * Supplies the {@link OutputItemHandler}s.
	 * 
	 */
	@Override
	public List<OutputItemHandler> get() {
		return new ArrayList<>(outputItemHandlers);
	}
}
