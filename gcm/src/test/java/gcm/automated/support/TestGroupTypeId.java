package gcm.automated.support;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import gcm.scenario.CompartmentId;
import gcm.scenario.GroupPropertyId;
import gcm.scenario.GroupTypeId;

/**
 * Enumeration that identifies group types and group properties for all tests
 */
public enum TestGroupTypeId implements GroupTypeId {
	GROUP_TYPE_1("GTProp_1_1", "GTProp_1_2"),
	GROUP_TYPE_2("GTProp_2_1", "GTProp_2_2", "GTProp_2_3"),
	GROUP_TYPE_3("GTProp_3_1", "GTProp_3_2"),
	GROUP_TYPE_4("GTProp_4_1", "GTProp_4_2", "GTProp_4_3", "GTProp_4_4"),
	GROUP_TYPE_5("GTProp_5_1"),
	GROUP_TYPE_6("GTProp_6_1", "GTProp_6_2");

	private static class TestGroupPropertyId implements GroupPropertyId {
		private final String id;

		private TestGroupPropertyId(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestGroupPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private GroupPropertyId[] groupPropertyIds;

	private TestGroupTypeId(String... strings) {
		groupPropertyIds = new GroupPropertyId[strings.length];
		for (int i = 0; i < strings.length; i++) {
			groupPropertyIds[i] = new TestGroupPropertyId(strings[i]);
		}
	}

	public GroupPropertyId[] getGroupPropertyIds() {
		return Arrays.copyOf(groupPropertyIds, groupPropertyIds.length);
	}

	public static TestGroupTypeId getRandomGroupTypeId(final RandomGenerator randomGenerator) {
		return TestGroupTypeId.values()[randomGenerator.nextInt(TestGroupTypeId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestGroupTypeId next;

	public TestGroupTypeId next() {
		if (next == null) {
			next = TestGroupTypeId.values()[(ordinal() + 1) % TestGroupTypeId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link CompartmentId} instance.
	 */
	public static GroupTypeId getUnknownGroupTypeId() {
		return new GroupTypeId() {
		};
	}

	/**
	 * Returns a new {@link GroupPropertyId} instance.
	 */
	public static GroupPropertyId getUnknownGroupPropertyId() {
		return new GroupPropertyId() {
		};
	}
}
