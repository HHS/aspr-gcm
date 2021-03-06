package gcm.simulation;

/**
 * Manager for warnings from the simulation to the user regarding various
 * settings that influence run time and memory usage.
 * 
 * @author Shawn Hatch
 *
 */
public interface SimulationWarningManager extends Element {

	public void processPopulationIndexEfficiencyWarning(PopulationIndexEfficiencyWarning populationIndexEfficiencyWarning);


	
}
