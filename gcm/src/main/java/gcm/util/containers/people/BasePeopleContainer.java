package gcm.util.containers.people;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.PersonId;
import gcm.simulation.Context;
import gcm.simulation.EnvironmentImpl;
import gcm.simulation.PersonLocationManger;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of PeopleContainer that acts as a dynamic switching mechanism
 * between the two lower-level PeopleContainer implementors
 * 
 * @author Shawn Hatch
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class BasePeopleContainer implements PeopleContainer {
	/*
	 * Enumeration for the two ways that people are stored in an IndexedPopulation
	 */
	private static enum PeopleContainerMode {
		INTSET, TREE_BIT_SET_SLOW;
	}

	private static final int TREE_BIT_SET_SLOW_THRESHOLD = 28;

	private static final int INT_SET_THRESHOLD = 33;

	/*
	 * indexed populations start small and so we default to PeopleContainerMode.SET
	 */
	private PeopleContainerMode mode;

	private final Context context;

	private final PersonLocationManger personLocationManger;

	private PeopleContainer internalPeopleContainer;

	public BasePeopleContainer(Context context) {
		this.context = context;
		this.personLocationManger = context.getPersonLocationManger();
		mode = PeopleContainerMode.INTSET;
		internalPeopleContainer = new IntSetPeopleContainer();
	}

	/*
	 * Switches the internal container between BooleanPeopleContainer and
	 * SetPeopleContainer as needed whenever the appropriate threshold has been
	 * crossed. If the size of the container is less than 0.5% of the total world
	 * population, then the SetPeopleContainer should be chosen. If the size of the
	 * container is greater than 1% of the total world population, then the
	 * BooleanPeopleContainer should be chosen. By setting two separate thresholds,
	 * we avoid modality thrash.
	 */
	private void determineMode(int size) {

		switch (mode) {

		case TREE_BIT_SET_SLOW:
			if (size <= personLocationManger.getPopulationCount() / INT_SET_THRESHOLD) {
				mode = PeopleContainerMode.INTSET;
				List<PersonId> people = internalPeopleContainer.getPeople();
				internalPeopleContainer = new IntSetPeopleContainer();
				internalPeopleContainer.addAll(people);

			}
			break;
		case INTSET:
			if (size >= personLocationManger.getPopulationCount() / TREE_BIT_SET_SLOW_THRESHOLD) {
				mode = PeopleContainerMode.TREE_BIT_SET_SLOW;
				internalPeopleContainer = new TreeBitSetPeopleContainer(context.getPersonIdManager(),
						internalPeopleContainer);
			}
			break;
		default:
			throw new RuntimeException("unhandled mode " + mode);
		}
	}

	@Override
	public List<PersonId> getPeople() {
		return internalPeopleContainer.getPeople();
	}

	@Override
	public boolean add(PersonId personId) {
		boolean result = internalPeopleContainer.add(personId);
		if (result) {
			determineMode(size());
		}
		return result;
	}

	@Override
	public boolean remove(PersonId personId) {
		boolean result = internalPeopleContainer.remove(personId);
		determineMode(size());
		return result;
	}

	@Override
	public int size() {
		return internalPeopleContainer.size();
	}

	@Override
	public void addAll(Collection<PersonId> collection) {
		/*
		 * We are not sure if the collection and the current contents of the
		 * internalPeopleContainer overlap and we don't want to waste time combining the
		 * two sets. So we assume conservatively that we will be at least as large as
		 * the incoming collection.
		 */
		determineMode(collection.size());
		internalPeopleContainer.addAll(collection);
		/*
		 * now that all the adds are done, we finally determine the mode correctly
		 */
		determineMode(size());
	}

	@Override
	public boolean contains(PersonId personId) {
		return internalPeopleContainer.contains(personId);
	}

	/*
	 * Returns a randomly selected person if this container has any people. Returns
	 * null otherwise.
	 */
	@Override
	public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
		return internalPeopleContainer.getRandomPersonId(randomGenerator);
	}
}