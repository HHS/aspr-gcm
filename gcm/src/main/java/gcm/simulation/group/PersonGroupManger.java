package gcm.simulation.group;

import java.util.List;

import gcm.scenario.GroupId;
import gcm.scenario.GroupTypeId;
import gcm.scenario.PersonId;
import gcm.simulation.Element;
import gcm.simulation.StochasticPersonSelection;
import gcm.util.annotations.Source;

/**
 * Manager for all group membership for the simulation.
 *
 * @author Shawn Hatch
 *
 */
@Source
public interface PersonGroupManger extends Element {

	/**
	 * Returns the group id for a newly defined group. The group type id must be
	 * valid.
	 */
	public GroupId addGroup(final GroupTypeId groupTypeId);

	/**
	 * Associates the person with the group. The group id must be valid. The person
	 * id must be valid. The person must not already be a member of the group.
	 */
	public void addPersonToGroup(final GroupId groupId, final PersonId personId);

	/**
	 * Returns the number of groups there are for a particular group type. The group
	 * type id must be valid.
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the number of groups associated with the given person where each
	 * group has the given group type. The person id must be non-null and
	 * non-negative. The group type id must be valid.
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the number of groups associated with the given person. The person id
	 * must be non-null and non-negative.
	 */
	public int getGroupCountForPerson(final PersonId personId);

	/**
	 * Returns the list of unique group identifiers.
	 */

	public List<GroupId> getGroupIds();

	/**
	 * Returns a list of unique groupIds associated with the given group type Id.
	 * Group type id must be valid.
	 *
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns a list of the unique group ids associated with the given person and
	 * group type. Group type id must be valid. Person id must be non-null and
	 * non-negative.
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId);

	/**
	 * Returns the list of unique groups associated the the given person. The person
	 * id must be non-null and non-negative.
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId);

	/**
	 * Returns the group type for the given group. The group id must be valid.
	 */
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId);

	/**
	 * Return the number of group types associated with the person via their group
	 * memberships. The person id must be non-null and non-negative.
	 */
	public int getGroupTypeCountForPersonId(final PersonId personId);

	/**
	 * Returns the list of unique group types associated with the person's groups.
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId);

	/**
	 * Returns a contacted person. The group should exist. The {@link GroupSampler}
	 * must be valid.
	 */
	public StochasticPersonSelection sampleGroup(final GroupId groupId, final GroupSampler groupSampler);

//	/**
//	 * Returns a contacted person. The group should exist. The
//	 * GroupWeightingFunction must not be null.
//	 */
//	public StochasticPersonSelection sampleGroup(final GroupId groupId,
//			final GroupWeightingFunction groupWeightingFunction);
//
//	/**
//	 * Returns a contacted person. The group should exist. The
//	 * GroupWeightingFunction must not be null. Uses the random generator associated
//	 * with the RandomNumberGeneratorId.
//	 */
//	public StochasticPersonSelection sampleGroup(final GroupId groupId,
//			final GroupWeightingFunction groupWeightingFunction, RandomNumberGeneratorId randomNumberGeneratorId);
//
//	/**
//	 * Returns a contacted person. The group should exist. The excludedPersonId may
//	 * be null.
//	 */
//	public StochasticPersonSelection sampleGroup(final GroupId groupId, final PersonId excludedPersonId);
//
//	/**
//	 * Returns a contacted person. The group should exist. The excludedPersonId may
//	 * be null. Uses the random generator associated with the given
//	 * RandomNumberGeneratorId.
//	 */
//	public StochasticPersonSelection sampleGroup(final GroupId groupId, RandomNumberGeneratorId randomNumberGeneratorId,
//			final PersonId excludedPersonId);

	/**
	 * Returns a list of unique people who are in the given group. Group id must be
	 * non-null and non-negative.
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId);

	/**
	 * Returns a list of unique person ids for the given group type(i.e. all people
	 * in groups having that type). Group type id must be valid.
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns the number of people in the given group. The group id must be valid.
	 */
	public int getPersonCountForGroup(final GroupId groupId);

	/**
	 * Returns the number of people(by unique count) who are associated with groups
	 * having the given group type. The group type id must be valid.
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId);

	/**
	 * Returns true if and only if the group exists.
	 */
	public boolean groupExists(final GroupId groupId);

	/**
	 * Returns true if and only if the person is in the group. Person id must be
	 * non-null and non-negative. Group id must be non-null.
	 */
	public boolean isGroupMember(final GroupId groupId, final PersonId personId);

	/**
	 * Removes the group from group management. The group id must be valid.
	 */
	public void removeGroup(final GroupId groupId);

	/**
	 * Removes the person from all group tracking.
	 *
	 * @param personId
	 */
	public void removePerson(final PersonId personId);

	/**
	 * Disassociates the person from the group. The group id must be valid. The
	 * person id must be valid.
	 */
	public void removePersonFromGroup(final GroupId groupId, final PersonId personId);
}
