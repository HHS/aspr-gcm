package gcm.test.manual.geolocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HospitalGenerator {
	private final Path tractsFile;
	private final Path hospitalFile;
	private final int hospitalCount;

	
	private HospitalGenerator(Path tractsFile, Path hospitalFile, int hospitalCount) {
		this.tractsFile = tractsFile;
		this.hospitalFile = hospitalFile;
		this.hospitalCount = hospitalCount;
	}
	
	
	private List<Tract> loadTracts() throws IOException {
		List<String> lines = Files.readAllLines(tractsFile);
		List<Tract> result = new ArrayList<>();
		boolean header = true;
		for (String line : lines) {
			if (header) {
				header = false;
			} else {
				String[] strings = line.split(",");
				Tract tract = new Tract(strings[0], Double.parseDouble(strings[2]), Double.parseDouble(strings[1]), Integer.parseInt(strings[3]));
				result.add(tract);
			}
		}
		return result;
	}

	
	private void execute() throws IOException {		
		List<Tract> tracts = loadTracts();
		List<Hospital> hospitals = createHospitals(tracts);
		writeHospitals(hospitals);		
	}

	private void writeHospitals(List<Hospital> hospitals) throws IOException {
		List<String> hospitalStrings = new ArrayList<>();
		hospitalStrings.add("Name"+"\t"+"Lat"+"\t"+"Lon");
		for(Hospital hospital : hospitals) {
			StringBuilder sb = new StringBuilder();
			sb.append(hospital.getName());
			sb.append("\t");
			sb.append(hospital.getLat());
			sb.append("\t");
			sb.append(hospital.getLon());
			hospitalStrings.add(sb.toString());			
		}
		Files.write(hospitalFile,hospitalStrings);
		
	}
	private List<Hospital> createHospitals(List<Tract> tracts){
		List<Hospital> result = new ArrayList<>();
		List<Integer> runningPopulation = new ArrayList<>();
		int popCount = 0;
		for(Tract tract : tracts) {
			popCount+=tract.getPopulationCount();
			runningPopulation.add(popCount);
		}
		Random random = new Random();
		for(int i = 0;i<hospitalCount;i++) {
			int pop = random.nextInt(popCount);
			int index = Collections.binarySearch(runningPopulation, pop);
			if(index<0) {
				index = -index-1;
			}
			Tract tract = tracts.get(index);
			Hospital hospital = new Hospital("Hospital_"+i,tract.getLatitude(),tract.getLongitude());
			result.add(hospital);
		}
		
		return result;
	}

	public static void main(String[] args) throws IOException {
		Path tractsFile = Paths.get(args[0]);
		Path hospitalFile = Paths.get(args[1]);
		int hospitalCount = Integer.parseInt(args[2]);
		
		new HospitalGenerator(tractsFile,hospitalFile,hospitalCount).execute();
	}
}
