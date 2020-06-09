package gcm.automated.support;
/**
 * Enumeration over the two Boolean values
 * @author Shawn Hatch
 *
 */
public enum BooleanType {
	TRUE(Boolean.TRUE), FALSE(Boolean.FALSE);
	private final Boolean value;

	private BooleanType(Boolean value) {
		this.value = value;
	}

	public Boolean value() {
		return value;
	}
}
