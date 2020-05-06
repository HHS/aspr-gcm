package gcm.test.manual.geolocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;
import org.apache.commons.math3.util.Pair;

import gcm.util.geolocator.GeoLocator;

public final class RadiationFlowTest {
	private final Path tractsFile;
	private final double searchRadiusKilometers;

	private RadiationFlowTest(Path tractsFile, double searchRadiusKilometers) {
		this.tractsFile = tractsFile;
		this.searchRadiusKilometers = searchRadiusKilometers;
	}

	public static void main(String[] args) throws IOException {
		Path tractsFile = Paths.get(args[0]);
		double searchRadiusKilometers = Double.parseDouble(args[1]);
		new RadiationFlowTest(tractsFile, searchRadiusKilometers).execute();
	}

	@SuppressWarnings("unused")
	private void execute() throws IOException {
		RandomGenerator randomGenerator = new Well44497b(2423049230948L);
		List<Tract> tracts = Files.readAllLines(tractsFile).stream().skip(1).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int populationCount = Integer.parseInt(strings[3]);
			return new Tract(name, lat, lon, populationCount);
		}).collect(Collectors.toList());

		GeoLocator.Builder<Tract> builder = GeoLocator.builder();
		tracts.forEach(tract -> builder.addLocation(tract.getLatitude(), tract.getLongitude(), tract));
		GeoLocator<Tract> geoLocator = builder.build();

		int badTracts = 0;
		for (Tract tract : tracts) {
			List<Pair<Tract, Double>> prioritizedLocations = geoLocator.getPrioritizedLocations(tract.getLatitude(), tract.getLongitude(), searchRadiusKilometers);
			List<Pair<Tract, Double>> gcmRadiationFlowModel = getRadiationModelFlowData_GCM(tract, prioritizedLocations);
			if (validate(gcmRadiationFlowModel)) {				
				new EnumeratedDistribution<>(randomGenerator, gcmRadiationFlowModel);
			}else {
				badTracts++;				
			}
			
		}
		System.out.println(badTracts+"/"+tracts.size());
	}

	private boolean validate(List<Pair<Tract, Double>> list) {

		double sum = 0;
		for (Pair<Tract, Double> pair : list) {
			Double value = pair.getSecond();
			// if (!Double.isFinite(value)) {
			// return false;
			// }
			// if (value < 0) {
			// return false;
			// }
			sum += value;
		}
		return sum > 0;
	}

	private List<Pair<Tract, Double>> getRadiationModelFlowData_GCM(Tract sourceTract, List<Pair<Tract, Double>> prioritizedLocations) {

		List<Pair<Tract, Double>> result = new ArrayList<>();

		long sourceRegionPopulation = sourceTract.getPopulationCount();
		long cumulativePopulation = 0;
		for (Pair<Tract, Double> pair : prioritizedLocations) {
			Tract tract = pair.getFirst();
			long targetRegionPopulation = tract.getPopulationCount();
			if (cumulativePopulation > 0) {
				double radiationWeight = (double) sourceRegionPopulation * targetRegionPopulation / (cumulativePopulation * (double) (cumulativePopulation + targetRegionPopulation));
				result.add(new Pair<>(tract, radiationWeight));
			}
			cumulativePopulation += targetRegionPopulation;
		}
		// result = getNormalized(result);
		return result;
	}

	
	

}
