
package gcmexample.gettingstarted;

import java.nio.file.Paths;

import gcm.experiment.Experiment;
import gcm.experiment.ExperimentExecutor;
import gcm.output.reports.ReportPeriod;
import gcm.scenario.ExperimentBuilder;
import gcm.scenario.PersonId;
import gcm.scenario.PropertyDefinition;
import gcm.scenario.TimeTrackingPolicy;

public class GettingStartedRunner {

	public static void main(String[] args) {
		// Create the experiment builder
		ExperimentBuilder experimentBuilder = new ExperimentBuilder();

		// Define person property - immune
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();//

		experimentBuilder.definePersonProperty(PersonProperty.IMMUNE, propertyDefinition);

		// Define person property - sick
		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build();//
		experimentBuilder.definePersonProperty(PersonProperty.SICK, propertyDefinition);

		// Add a single region
		experimentBuilder.addRegionId(Region.REGION_1, SimpleRegion.class);

		/*
		 * Add the exposed, treatment, recovered and terminal compartments. Note
		 * that we identify the compartment with an ID and then pass only a
		 * class reference, not an instance of the compartment class. More on
		 * that in later lessons.
		 */

		experimentBuilder.addCompartmentId(Compartment.TREATMENT, TreatmentCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.EXPOSED, ExposedCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.TERMINAL, TerminalCompartment.class);
		experimentBuilder.addCompartmentId(Compartment.RECOVERED, RecoveredCompartment.class);

		// Add a resource to the model and give REGION_1 300 doses of medication
		experimentBuilder.addResource(Resource.MEDICATION);
		experimentBuilder.addRegionResourceLevel(Region.REGION_1, Resource.MEDICATION, 300);

		// experimentBuilder.addMaterial(Material.THIMEROSAL);
		// experimentBuilder.addBatch(batchId, materialId, amount,
		// materialsProducerId);

		// Add a few people
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i);
			experimentBuilder.addPerson(personId, Region.REGION_1, Compartment.EXPOSED);
		}

		// Build the experiment
		Experiment experiment = experimentBuilder.build();

		// Prepare the experiment executor
		ExperimentExecutor experimentExecutor = new ExperimentExecutor();
		experimentExecutor.setExperiment(experiment);

		// Add a report
		experimentExecutor.addCompartmentTransferReport(Paths.get("c:\\temp\\gcm\\compartment report.txt"), ReportPeriod.DAILY);
		experimentExecutor.addPersonResourceReport(Paths.get("c:\\temp\\gcm\\person resource report.txt"), ReportPeriod.DAILY, false, false, Resource.MEDICATION);
		experimentExecutor.addResourceReport(Paths.get("c:\\temp\\gcm\\resource report.txt"), ReportPeriod.DAILY, Resource.MEDICATION);

		// Execute the experiment
		experimentExecutor.execute();
	}
}
