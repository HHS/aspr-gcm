package gcm.manual.pp;

import gcm.scenario.CompartmentId;
import gcm.scenario.RegionId;

public class SampleLamdas {
	
	public static Integer getCompartmentLabel(CompartmentId compartmentId) {
		return compartmentId.toString().length();
	}

	public static String getRegionLabel(RegionId regionId) {
		PPRegion region = (PPRegion) regionId;
		return region.getCounty();
	}
}
