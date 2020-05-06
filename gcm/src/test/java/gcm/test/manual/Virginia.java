package gcm.test.manual;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gcm.test.manual.geolocator.Tract;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLon;
import gcm.util.stats.MutableStat;

public class Virginia {
	private static class Person {
		int age;
		String home;
		String school;
		String work;
		Tract homeTract;
		Tract workTract;		
		double distanceToWork;
		double distanceToSchool;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Person [age=");
			builder.append(age);
			builder.append(", home=");
			builder.append(home);
			builder.append(", school=");
			builder.append(school);
			builder.append(", work=");
			builder.append(work);
			builder.append(", distanceToWork=");
			builder.append(distanceToWork);
			builder.append(", distanceToSchool=");
			builder.append(distanceToSchool);
			builder.append("]");
			return builder.toString();
		}

	}

	

	public static void main(String[] args) throws IOException {
		getVirginaWorkPopulation();
	}
	public static Map<String,Integer> getVirginaWorkPopulation() throws IOException {

		Path tractsFile = Paths.get("C:\\hhs-io\\hhs-core-flu\\input\\population\\tracts\\tract-ids.csv");
		List<Tract> tracts = Files.readAllLines(tractsFile).stream().skip(1).map(line -> {
			String[] strings = line.split(",");
			String name = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int populationCount = Integer.parseInt(strings[3]);
			return new Tract(name, lat, lon, populationCount);
		}).collect(Collectors.toList());

		Map<String, Tract> tractMap = new LinkedHashMap<>();
		for (Tract tract : tracts) {
			tractMap.put(tract.getId(), tract);
		}

		Earth earth = Earth.fromMeanRadius();

		Path path = Paths.get("C:\\hhs-io\\hhs-core-flu\\input\\population\\all_states\\va.csv");
		List<Person> people = Files.readAllLines(path).stream().skip(1).map(line -> {
			String[] strings = line.split(",", -1);
			
			Person person = new Person();			
			person.age = Integer.parseInt(strings[0].trim());
			person.home = strings[1].trim();
			person.school = strings[2].trim();
			person.work = strings[3].trim();
			return person;
		}).collect(Collectors.toList());

		people.forEach(person -> {

			if (person.home.length() >= 11) {
				person.homeTract = tractMap.get(person.home.substring(0, 11));
			}

			if (person.work.length() >= 11) {				
				person.workTract = tractMap.get(person.work.substring(0, 11));
			}

			assertNotNull(person.home, person.homeTract);
			assertFalse(person.work, !person.work.isEmpty() && person.workTract == null);
			// assertFalse(person.school, !person.school.isEmpty() &&
			// schoolTract == null);

			if (person.workTract != null) {
				LatLon homeLatLon = new LatLon(person.homeTract.getLatitude(), person.homeTract.getLongitude());

				LatLon workLatLon = new LatLon(person.workTract.getLatitude(), person.workTract.getLongitude());

				person.distanceToWork = earth.getGroundDistanceFromLatLon(homeLatLon, workLatLon);

			}

		});

		Map<String, Integer> counterMap = new LinkedHashMap<>();

		MutableStat mutableStat = new MutableStat();
		people.forEach(person -> {
			if (person.workTract != null) {
				mutableStat.add(person.distanceToWork);
				Integer count = counterMap.get(person.workTract.getId());				
				if (count == null) {
					count = 0;					
				}
				counterMap.put(person.workTract.getId(), count+1);				
			}
		});
		
		int workerCount = 0;
		int inStateWorkerCount = 0;
		for(Person person : people) {
			if(person.workTract!=null) {
				workerCount++;
				if(person.work.substring(0,2).equals("51")) {
					inStateWorkerCount++;
				}
			}
		}
		
		System.out.println("workerCount = "+workerCount);
		System.out.println("inStateWorkerCount = "+inStateWorkerCount);
		double propotion = inStateWorkerCount;
		propotion/=workerCount;
		System.out.println("propotion = "+propotion);

		System.out.println(mutableStat);
		return counterMap;

		//people.stream().filter(person -> person.workTract != null && person.distanceToWork > 1000000).forEach(person -> System.out.println(person.line));

	}
}
