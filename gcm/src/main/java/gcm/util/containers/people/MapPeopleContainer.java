package gcm.util.containers.people;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.PersonId;
import gcm.simulation.EnvironmentImpl;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * PeopleContainer implementor that uses a LinkedHashMap to contain the people.
 * Since the LinkedHashMap uses significant overhead per person this container
 * is preferred to a BooleanContainer-based implementor when the number of
 * people in the set is less than 0.5% of the total population.
 * 
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class MapPeopleContainer implements PeopleContainer {

	public MapPeopleContainer() {
	}

	public MapPeopleContainer(PeopleContainer peopleContainer) {
		for (PersonId personId : peopleContainer.getPeople()) {
			add(personId);
		}
	}

	private Map<PersonId, Integer> map = new LinkedHashMap<>();

	private List<PersonId> list = new ArrayList<>();

	@Override
	public List<PersonId> getPeople() {
		return new ArrayList<>(map.keySet());
	}

	@Override
	public boolean add(PersonId personId) {
		boolean result = !map.containsKey(personId);
		if (result) {
			map.put(personId, list.size());
			list.add(personId);
		}
		return result;
	}

	@Override
	public boolean remove(PersonId personId) {
		boolean result = map.containsKey(personId);
		if (result) {
			Integer index = map.remove(personId);
			list.set(index, null);
			/*
			 * If the list is too big we will need to rebuild both the map and the list
			 * since the list has many null values and the map will no longer point to the
			 * correct indices in the list unless we rebuild the map as well.
			 */
			if (list.size() > 2 * map.size()) {
				list = new ArrayList<>(map.keySet());
				map = new LinkedHashMap<>();
				for (int i = 0; i < list.size(); i++) {
					map.put(list.get(i), i);
				}
			}
		}
		return result;
	}

	@Override
	public int size() {
		return map.keySet().size();
	}

	@Override
	public void addAll(Collection<PersonId> collection) {
		for (PersonId personId : collection) {
			add(personId);
		}
	}

	@Override
	public boolean contains(PersonId personId) {
		return map.containsKey(personId);
	}

	@Override
	public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
		/*
		 * We require that there be at least one person to select
		 */
		if (map.size() > 0) {
			/*
			 * We repeatedly select from the list until we get someone who is in the set.
			 */
			while (true) {
				PersonId personId = list.get(randomGenerator.nextInt(list.size()));
				if (personId != null) {
					return personId;
				}
			}
		}
		return null;
	}
}