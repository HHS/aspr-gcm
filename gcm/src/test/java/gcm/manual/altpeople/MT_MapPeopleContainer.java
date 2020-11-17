package gcm.manual.altpeople;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.manual.AltPersonIdManager;
import gcm.scenario.PersonId;
import gcm.util.MemSizer;
import gcm.util.TimeElapser;
import gcm.util.containers.people.IntSetPeopleContainer;
import gcm.util.containers.people.PeopleContainer;
import gcm.util.containers.people.TreeBitSetPeopleContainer;

public class MT_MapPeopleContainer {

	private static enum ContainerType {
		INTSET, //
		TREEBITSET;// 3_000_000 adds 124.6 milliseconds, 1.39 bits per person
	}

	@Test
	public void test() {
		RandomGenerator randomGenerator = EnvironmentSupport.getRandomGenerator(23408234723423423L);

		AltPersonIdManager altPersonIdManager = new AltPersonIdManager();

		int populationSize = 1_000_000;

		for (int i = 0; i < populationSize; i++) {
			altPersonIdManager.addPersonId();
		}

		PeopleContainer peopleContainer;
		ContainerType containerType = ContainerType.TREEBITSET;
		switch (containerType) {

		case INTSET:
			peopleContainer = new IntSetPeopleContainer();
			break;

		case TREEBITSET:
			peopleContainer = new TreeBitSetPeopleContainer(altPersonIdManager);
			break;
		default:
			throw new RuntimeException("unhandled case " + containerType);
		}

		System.out.println(containerType);
		System.out.println(populationSize);

		List<PersonId> people = altPersonIdManager.getPeople();
		TimeElapser timeElapser = new TimeElapser();
		for (PersonId personId : people) {
			peopleContainer.safeAdd(personId);
		}
		System.out.println("adds done " + timeElapser.getElapsedMilliSeconds());
		assertEquals(populationSize, peopleContainer.size());

		timeElapser.reset();
		for (int i = 0; i < populationSize; i++) {
			peopleContainer.getRandomPersonId(randomGenerator);
		}
		System.out.println("random draws " + timeElapser.getElapsedMilliSeconds());

		MemSizer memSizer = new MemSizer(false);
		memSizer.excludeClass(PersonId.class);
		memSizer.excludeClass(AltPersonIdManager.class);

		double bitsPerPerson = memSizer.getByteCount(peopleContainer);
		bitsPerPerson *= 8;
		bitsPerPerson /= populationSize;

		System.out.println("bits per person = " + bitsPerPerson);

	}

}
