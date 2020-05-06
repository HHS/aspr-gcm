package gcm.scenario;

import gcm.util.annotations.Source;
import net.jcip.annotations.Immutable;

/**
 * Base class for int-based identifiers that are supplied by the GCM rather than
 * supplied by the modeler. This covers ids supplied by the modeler in the
 * scenario since they are replaced by a contiguous range of ids by the
 * simulation.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source
public class IntId implements Comparable<IntId> {

	private final int value;

	public IntId(final int id) {

		this.value = id;
	}

	
	/**
	 * To be equal, two IntId instances must have the same implementor type and have the same int value
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IntId other = (IntId) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}

	public final int getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public int compareTo(IntId personId) {
		return Integer.compare(value, personId.value);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
