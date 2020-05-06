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

public class HospitalGeoLocatorTest {

	private HospitalGeoLocatorTest() {

	}

	public static void main(String[] args) throws IOException {
		Path tractsFile = Paths.get(args[0]);
		Path hospitalFile = Paths.get(args[1]);
		double searchRadiusKilometers = Double.parseDouble(args[2]);

		List<Hospital> hospitals = Files.readAllLines(hospitalFile).stream().skip(1).map(line -> {
			String[] strings = line.split("\t");
			String name = strings[0];
			double lat = Double.parseDouble(strings[1]);
			double lon = Double.parseDouble(strings[2]);
			return new Hospital(name, lat, lon);
		}).collect(Collectors.toList());

		List<Tract> tracts = Files.readAllLines(tractsFile).stream().skip(1).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int populationCount = Integer.parseInt(strings[3]);
			return new Tract(name, lat, lon, populationCount);
		}).collect(Collectors.toList());

		TimeElapser timeElapser = new TimeElapser();

		GeoLocator.Builder<Hospital> builder = GeoLocator.builder();
		hospitals.forEach(hospital -> builder.addLocation(hospital.getLat(), hospital.getLon(), hospital));
		GeoLocator<Hospital> geoLocator = builder.build();

		//Map<Tract, List<Pair<Hospital, Double>>> map =
				
		tracts//
			.parallelStream()//
			.collect(//
				Collectors.toMap(//
					Function.identity(), //
					tract -> geoLocator.getPrioritizedLocations(tract.getLatitude(), tract.getLongitude(), searchRadiusKilometers)//
				)//
			);//

//		map.entrySet().forEach(entry -> {
//			System.out.println(entry.getKey().getId() + " has " + entry.getValue().size() + " hospitals");
//		});

		System.out.println("Tracts file = " + tractsFile + "(" + tracts.size() + " tracts)");
		System.out.println("Hospitals file = " + hospitalFile + "(" + hospitals.size() + " hospitals)");
		System.out.println("Search Radius Kilometers = " + searchRadiusKilometers);
		System.out.println("Mapping of tracts to hospitals sorted by distance reqired " + timeElapser.getElapsedMilliSeconds() + " milliseconds");

	}
}
