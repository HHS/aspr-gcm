package gcm.test.manual.demo.components;

import static gcm.simulation.Filter.groupsForPersonAndGroupType;
import static gcm.simulation.Filter.region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import gcm.components.AbstractComponent;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.scenario.RegionId;
import gcm.simulation.Environment;
import gcm.simulation.Equality;
import gcm.simulation.Filter;
import gcm.simulation.Plan;
import gcm.test.manual.demo.identifiers.Compartment;
import gcm.test.manual.demo.identifiers.GlobalProperty;
import gcm.test.manual.demo.identifiers.GroupType;
import gcm.test.manual.demo.identifiers.PersonProperty;
import gcm.util.TimeElapser;

public class PopulationLoader extends AbstractComponent {

	@Override
	public void executePlan(final Environment environment, final Plan plan) {

		TimeElapser timeElapser = new TimeElapser();

		Path populationPath = environment.getGlobalPropertyValue(GlobalProperty.POPULATION_PATH);
		Map<String, RegionId> regionMap = new LinkedHashMap<>();
		for (RegionId regionId : environment.getRegionIds()) {
			regionMap.put(regionId.toString(), regionId);
		}

//		System.out.println("region map preparation " + timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();

		Map<String, GroupId> homeIds = new LinkedHashMap<>();
		Map<String, GroupId> schoolIds = new LinkedHashMap<>();
		Map<String, GroupId> workPlaceIds = new LinkedHashMap<>();

		try {
			Files.readAllLines(populationPath).stream().skip(1).forEach(line -> {
				String[] strings = line.split(",", -1);
				int age = Integer.parseInt(strings[0]);
				String homeId = strings[1];
				String schoolId = strings[2];
				String workPlaceId = strings[3];
				String regionIdString = homeId.substring(0, 11);

				// determine the region and create the person
				RegionId regionId = regionMap.get(regionIdString);
				PersonId personId;
				if (environment.getRandomGenerator().nextDouble() < 0.001) {
					personId = environment.addPerson(regionId, Compartment.DEAD);
				} else {
					personId = environment.addPerson(regionId, Compartment.SUSCEPTIBLE);
				}
				environment.setPersonPropertyValue(personId, PersonProperty.AGE, age);
				boolean immune = environment.getRandomGenerator().nextDouble() < 0.05;
				environment.setPersonPropertyValue(personId, PersonProperty.IMMUNE, immune);

				// place the person in a home group
				GroupId groupId = homeIds.get(homeId);
				if (groupId == null) {
					groupId = environment.addGroup(GroupType.HOME);
					homeIds.put(homeId, groupId);
				}
				environment.addPersonToGroup(personId, groupId);

				// place the person in a school group

				if (!schoolId.isEmpty()) {
					groupId = schoolIds.get(schoolId);
					if (groupId == null) {
						groupId = environment.addGroup(GroupType.SCHOOL);
						schoolIds.put(schoolId, groupId);
					}
					environment.addPersonToGroup(personId, groupId);
				}

				// place the person in a work place group
				if (!workPlaceId.isEmpty()) {
					groupId = workPlaceIds.get(workPlaceId);
					if (groupId == null) {
						groupId = environment.addGroup(GroupType.WORK);
						workPlaceIds.put(workPlaceId, groupId);
					}
					// environment.setPersonPropertyValue(personId,
					// PersonProperty.IS_WORKING, true);
					environment.addPersonToGroup(personId, groupId);
				}

			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

//		System.out.println("population and group loading " + timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();

		environment.getRegionIds().stream().forEach(regionId -> {
			Filter filter = region(regionId).and(groupsForPersonAndGroupType(GroupType.WORK, Equality.GREATER_THAN, 0));
			environment.addPopulationIndex(filter, regionId);

		});

		double indexLoadingTime = timeElapser.getElapsedMilliSeconds();

//		System.out.println("index loading time " + indexLoadingTime);
		@SuppressWarnings("unused")
		double averageTimeToLoadIndex = indexLoadingTime;
		averageTimeToLoadIndex /= environment.getRegionIds().size();
//		System.out.println("time to load per index " + averageTimeToLoadIndex);

		timeElapser.reset();

		// Some more stats of interest
		//System.out.println("total population = " + environment.getPopulationCount());
		//System.out.println("total homes = " + environment.getGroupCountForGroupType(GroupType.HOME));
		//System.out.println("total schools = " + environment.getGroupCountForGroupType(GroupType.SCHOOL));
		int workPlaceCount = environment.getGroupCountForGroupType(GroupType.WORK);
		//System.out.println("total work places = " + workPlaceCount);
		//System.out.println("total regions = " + environment.getRegionIds().size());

		long workingPeople = environment.getPeople()//
										.stream()//
										.filter(personId -> environment.getGroupCountForGroupTypeAndPerson(GroupType.WORK, personId) > 0)//
										.count();//

		//System.out.println("number of people working = " + workingPeople);
		@SuppressWarnings("unused")
		double averageNumberofWorkersPerWorkPlace = workingPeople;
		averageNumberofWorkersPerWorkPlace /= workPlaceCount;
		//System.out.println("average number of workers per workplace = " + averageNumberofWorkersPerWorkPlace);

	}

	@Override
	public void init(Environment environment) {
		environment.addPlan(new Plan() {
		}, 0);
	}

	@Override
	public void close(Environment environment) {

	}
}
