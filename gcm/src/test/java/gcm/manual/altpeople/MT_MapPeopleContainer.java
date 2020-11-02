package gcm.manual.altpeople;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import gcm.automated.support.EnvironmentSupport;
import gcm.manual.AltPersonIdManager;
import gcm.scenario.PersonId;
import gcm.util.MemSizer;
import gcm.util.TimeElapser;
import gcm.util.containers.people.IntSetPeopleContainer;
import gcm.util.containers.people.MapPeopleContainer;
import gcm.util.containers.people.PeopleContainer;
import gcm.util.containers.people.TreeBitSetPeopleContainer;
import gcm.util.containers.people.TreeBitSetPeopleContainer_Fast;

public class MT_MapPeopleContainer {

	private static enum ContainerType {
		MAP, // 3_000_000 adds 135.8 milliseconds, 472.5 bits per person
		INTSET, //
		TREEBITSET1, // 3_000_000 adds 58.9 milliseconds, 2.40 bits per person
		TREEBITSET2;// 3_000_000 adds 124.6 milliseconds, 1.39 bits per person
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
		ContainerType containerType = ContainerType.TREEBITSET1;
		switch (containerType) {

		case INTSET:
			peopleContainer = new IntSetPeopleContainer();
			break;		
		case MAP:
			peopleContainer = new MapPeopleContainer();
			break;
		case TREEBITSET1:
			peopleContainer = new TreeBitSetPeopleContainer_Fast(altPersonIdManager);
			break;
		case TREEBITSET2:
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
			peopleContainer.add(personId);
		}
		System.out.println("adds done " + timeElapser.getElapsedMilliSeconds());
		assertEquals(populationSize, peopleContainer.size());

		timeElapser.reset();
		for (int i = 0; i < populationSize; i++) {
			peopleContainer.getRandomPersonId(randomGenerator);
		}
		System.out.println("random draws " + timeElapser.getElapsedMilliSeconds());

		if (containerType != ContainerType.MAP || populationSize <= 10_000) {
			MemSizer memSizer = new MemSizer(false);
			memSizer.excludeClass(PersonId.class);
			memSizer.excludeClass(AltPersonIdManager.class);

			double bitsPerPerson = memSizer.getByteCount(peopleContainer);
			bitsPerPerson *= 8;
			bitsPerPerson /= populationSize;

			System.out.println("bits per person = " + bitsPerPerson);
		}

	}

}
