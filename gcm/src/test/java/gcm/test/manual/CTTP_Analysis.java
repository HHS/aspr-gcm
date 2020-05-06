package gcm.test.manual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gcm.test.manual.BinContainer.Bin;
import gcm.util.earth.ECC;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLon;

public class CTTP_Analysis {
	private static class Tract {
		final String id;		
		final ECC ecc;

		public Tract(String id, double latitude, double longitude, Earth earth) {
			super();
			this.id = Objects.requireNonNull(id);			
			this.ecc = earth.getECCFromLatLon(new LatLon(latitude, longitude));
		}
	}

	private static class Flow {
		// resolution,source_geoid,target_geoid,flow,moe
		final Tract sourceTract;
		final Tract targetTract;
		final int personCount;

		public Flow(Tract sourceTract, Tract targetTract, int flow) {
			super();
			this.sourceTract = Objects.requireNonNull(sourceTract);
			this.targetTract = Objects.requireNonNull(targetTract);
			this.personCount = flow;
		}

	}

	private final Path cttpFilePath;

	private final Path tractFilePath;

	private CTTP_Analysis(Path tractFilePath, Path cttpFilePath) {
		this.tractFilePath = Objects.requireNonNull(tractFilePath);
		this.cttpFilePath = Objects.requireNonNull(cttpFilePath);
	}

	public static void main(String[] args) throws IOException {
		Path tractFilePath = Paths.get(args[0]);
		Path cttpFilePath = Paths.get(args[1]);
		new CTTP_Analysis(tractFilePath, cttpFilePath).execute();
	}

	private void execute() throws IOException {

		Earth earth = Earth.fromMeanRadius();

		Map<String, Tract> tractMap = Files.readAllLines(tractFilePath).stream().skip(1).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			return new Tract(name, lat, lon, earth);
		}).collect(Collectors.toMap(tract -> tract.id, tract -> tract));

		System.out.println("tracts collected: " + tractMap.size());

		
		ArrayList<Flow> flows = Files	.readAllLines(cttpFilePath).stream()//
										.skip(1)//
										.filter(line -> {
											String[] strings = line.split(",", -1);
											return 
											line.startsWith("tract")&&
											strings[1].startsWith("51")&&
											strings[2].startsWith("51");
											
										})//
										.map(line -> {
											// resolution,source_geoid,target_geoid,flow,moe
											String[] strings = line.split(",", -1);
											Tract sourceTract = tractMap.get(strings[1]);
											Tract targetTract = tractMap.get(strings[2]);
											return new Flow(sourceTract, targetTract, Integer.parseInt(strings[3]));
										})//
										.collect(Collectors.toCollection(ArrayList::new));

		System.out.println("flows collected: " + flows.size());

		BinContainer binContainer = new BinContainer(5000);
		flows.forEach(flow -> {
			double distance = earth.getGroundDistanceFromECC(flow.sourceTract.ecc, flow.targetTract.ecc);
			binContainer.addValue(distance, flow.personCount);
		});

		for (int i = 0; i < binContainer.binCount(); i++) {
			Bin bin = binContainer.getBin(i);
			System.out.println(bin.getLowerBound() + "\t" + bin.getUpperBound() + "\t" + bin.getCount());
		}
		
		
	}

}
