package gcm.test.manual.delaunay.geovisualizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import gcm.util.TimeElapser;
import gcm.util.delaunay.GeoDelaunaySolver;
import gcm.util.delaunay.SimpleGeoCoordinate;

public class GeoVisualizerDriver {

	private GeoVisualizerDriver() {

	}

	public static final class ClientGeoCoordinate extends SimpleGeoCoordinate {

		private final String id;

		private final int population;

		public ClientGeoCoordinate(String id, double latitude, double longitude, int population) {
			super(latitude, longitude);
			this.id = id;
			this.population = population;
		}

		public String getId() {
			return id;
		}

		public int getPopulation() {
			return population;
		}

	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {

		Path tractsFile = Paths.get(args[0]);

		List<ClientGeoCoordinate> geoCoordinates = new ArrayList<>();

		Files.readAllLines(tractsFile).stream().skip(1).forEach(line -> {
			String[] strings = line.split(",");
			String id = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int population = Integer.parseInt(strings[3]);
			ClientGeoCoordinate clientGeoCoordinate = new ClientGeoCoordinate(id, lat, lon, population);
			geoCoordinates.add(clientGeoCoordinate);
		});

		TimeElapser timeElapser = new TimeElapser();

		List<Pair<ClientGeoCoordinate, ClientGeoCoordinate>> pairs = GeoDelaunaySolver.solve(geoCoordinates);

		System.out.println("Solver time = " + timeElapser.getElapsedSeconds() + " seconds");

		new GeoVisualzerFrame(geoCoordinates,pairs);
	}

}
