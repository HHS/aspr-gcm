package gcm.test.manual.geolocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import gcm.util.TimeElapser;
import gcm.util.geolocator.GeoLocator;

public class TractGeoLocatorTest {

	private TractGeoLocatorTest() {

	}

	public static void main(String[] args) throws IOException {
		Path tractsFile = Paths.get(args[0]);
		double searchRadiusKilometers = Double.parseDouble(args[1]);

		List<Tract> tracts = Files.readAllLines(tractsFile).stream().skip(1).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int populationCount = Integer.parseInt(strings[3]);
			return new Tract(name, lat, lon, populationCount);
		}).collect(Collectors.toList());

		TimeElapser timeElapser = new TimeElapser();

		GeoLocator.Builder<Tract> builder = GeoLocator.builder();
		tracts.forEach(tract -> builder.addLocation(tract.getLatitude(), tract.getLongitude(), tract));
		GeoLocator<Tract> geoLocator = builder.build();

		tracts.parallelStream().collect(Collectors.toMap(//
				Function.identity(), //
				tract -> geoLocator.getLocations(tract.getLatitude(), tract.getLongitude(), searchRadiusKilometers)//
		));

		// tracts.parallelStream().collect(Collectors.toMap(//
		// Function.identity(), //
		// tract -> geoLocator.getPrioritizedLocations(tract.getLat(),
		// tract.getLon(), searchRadiusKilometers)//
		// ));

		System.out.println("Tracts file = " + tractsFile + "(" + tracts.size() + " tracts)");
		System.out.println("Search Radius Kilometers = " + searchRadiusKilometers);
		System.out.println("Mapping of tracts to candidate work locations sorted by distance reqired " + timeElapser.getElapsedMilliSeconds() + " milliseconds");

	}
}
