package gcm.manual.altpeople;

import java.util.ArrayList;
import java.util.List;

import gcm.scenario.PersonId;


public final class AltPersonIdManager {
	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */
	private List<PersonId> personIds = new ArrayList<>();
	
	public boolean personIndexExists(int personId) {
		boolean result = false;
		if ((personId >= 0) && (personId < personIds.size())) {
			result = personIds.get(personId) != null;
		}
		return result;

	}

	public int getPersonIdLimit() {
		return personIds.size();
	}

	public PersonId getBoxedPersonId(int personId) {
		PersonId result = null;
		if ((personId >= 0) && (personIds.size() > personId)) {
			result = personIds.get(personId);
		}

		if (result == null) {
			throw new RuntimeException("unknown person " + personId);
		}
		return result;
	}

	public PersonId addPersonId() {	
		PersonId personId = new PersonId(personIds.size());
		personIds.add(personId);
		return personId;
		
	}
	
	public PersonId getCleanedPersonId(final PersonId personId) {
		if (personId == null) {
			throw new RuntimeException("Personid is null");
		}

		if (!personExists(personId)) {
			throw new RuntimeException("Person does not exist "+personId);
		}
		return personIds.get(personId.getValue());
	}

	public boolean personExists(final PersonId personId) {

		boolean result = false;
		if ((personId != null) && (personId.getValue() >= 0) && (personId.getValue() < personIds.size())) {
			result = personIds.get(personId.getValue()) != null;
		}

		return result;
	}

	public List<PersonId> getPeople() {

		int count = 0;
		for (final PersonId boxedPersonId : personIds) {
			if (boxedPersonId != null) {
				count++;
			}
		}
		final List<PersonId> result = new ArrayList<>(count);

		for (final PersonId boxedPersonId : personIds) {
			if (boxedPersonId != null) {
				result.add(boxedPersonId);
			}
		}

		return result;
	}

	public void removePerson(PersonId personId) {
		if (!personExists(personId)) {
			throw new RuntimeException("Person does not exist "+personId);
		}
		personIds.set(personId.getValue(), null);
	}
	
	

}
