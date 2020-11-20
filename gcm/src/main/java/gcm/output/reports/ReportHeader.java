package gcm.output.reports;

import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An immutable, ordered container for the string values in the header of a
 * report. Constructed via the contained builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
@Source(status = TestStatus.UNEXPECTED)
public final class ReportHeader {

	private final List<String> headerStrings;

	private ReportHeader(List<String> headerStrings) {
		this.headerStrings = new ArrayList<>(headerStrings);
	}
	
	public static Builder builder() {
		return new Builder();
	}

	@NotThreadSafe
	public final static class Builder {
		
		private Builder() {
			
		}

		private List<String> headerStrings = new ArrayList<>();

		public Builder add(String headerString) {
			this.headerStrings.add(headerString);
			return this;
		}

		public ReportHeader build() {
			try {
				return new ReportHeader(headerStrings);
			} finally {
				headerStrings = new ArrayList<>();
			}
		}
	}

	public List<String> getHeaderStrings() {
		return new ArrayList<>(headerStrings);
	}

	/**
	 * String representation that preserves the order of the added strings presented as:
	 * 
	 * ReportHeader [headerStrings=[string1, string2...]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportHeader [headerStrings=");
		builder.append(headerStrings);
		builder.append("]");
		return builder.toString();
	}
}
