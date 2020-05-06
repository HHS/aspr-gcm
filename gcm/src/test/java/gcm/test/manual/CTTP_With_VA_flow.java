package gcm.test.manual;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gcm.util.MultiKey;

public class CTTP_With_VA_flow {

	private static class Flow {
		// resolution,source_geoid,target_geoid,flow,moe
		final String sourceId;
		final String targetId;
		final int personCount;
		int VAPersonCount;

		public Flow(String sourceId, String targetId, int personCount) {
			super();
			this.sourceId = Objects.requireNonNull(sourceId);
			this.targetId = Objects.requireNonNull(targetId);
			this.personCount = personCount;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(sourceId);
			builder.append("\t");
			builder.append(targetId);
			builder.append("\t");
			builder.append(personCount);
			builder.append("\t");
			builder.append(VAPersonCount);
			return builder.toString();
		}

	}

	private static class Person {
		String homeId;
		String workId;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Person [homeId=");
			builder.append(homeId);
			builder.append(", workId=");
			builder.append(workId);
			builder.append("]");
			return builder.toString();
		}

	}

	private final Path cttpFilePath;

	private final Path populationFile;

	private CTTP_With_VA_flow(Path cttpFilePath, Path populationFile) {
		this.cttpFilePath = Objects.requireNonNull(cttpFilePath);
		this.populationFile = Objects.requireNonNull(populationFile);
	}

	public static void main(String[] args) throws IOException {
		Path cttpFilePath = Paths.get(args[0]);
		Path populationFile = Paths.get(args[1]);
		new CTTP_With_VA_flow(cttpFilePath, populationFile).execute();
	}

	private void execute() throws IOException {

		ArrayList<Flow> flows = Files	.readAllLines(cttpFilePath).stream()//
										.skip(1)//
										.filter(line -> {
											String[] strings = line.split(",", -1);
											return line.startsWith("tract") && strings[1].startsWith("51") && strings[2].startsWith("51");

										})//
										.map(line -> {
											// resolution,source_geoid,target_geoid,flow,moe
											String[] strings = line.split(",", -1);
											return new Flow(strings[1], strings[2], Integer.parseInt(strings[3]));
										})//
										.collect(Collectors.toCollection(ArrayList::new));

		System.out.println("flows collected: " + flows.size());

		// Load the people who both live and work in VA
		List<Person> people = Files.readAllLines(populationFile).stream().skip(1).filter(line -> {
			String[] strings = line.split(",", -1);
			return strings[3].startsWith("51");
		}).map(line -> {
			String[] strings = line.split(",", -1);
			Person person = new Person();
			person.homeId = strings[1].trim().substring(0, 11);
			person.workId = strings[3].trim().substring(0, 11);
			return person;
		}).collect(Collectors.toList());

		// System.out.println("People loaded " + people.size());

		Map<MultiKey, Flow> flowMap = new LinkedHashMap<>();
		for (Flow flow : flows) {
			MultiKey multiKey = new MultiKey(flow.sourceId, flow.targetId);
			flowMap.put(multiKey, flow);
		}

		List<Person> peopleWithoutFlow = new ArrayList<>();
		for (Person person : people) {
			MultiKey multiKey = new MultiKey(person.homeId, person.workId);
			Flow flow = flowMap.get(multiKey);
			if (flow == null) {
				peopleWithoutFlow.add(person);
			} else {
				flow.VAPersonCount++;
			}
		}
		System.out.println("people without matching cttp flow");
		System.out.println();
		System.out.println("homeId" + "\t" + "workId");
		for (Person person : peopleWithoutFlow) {
			System.out.println(person.homeId + "\t" + person.workId);
		}

		System.out.println();
		System.out.println("flows with cttp and VA population");

		System.out.println();
		System.out.println("sourceId" + "\t" + "targetId" + "\t" + "flow person count" + "\t" + "VA worker count");
		flows.forEach(flow -> System.out.println(flow));
	}

}
