package gcm.util.annotations;

/**
 * An enumeration supporting the marking of source classes with the expected
 * level of testing for that source class.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNREQUIRED)
/*
 * The order of the enum must be preserved 
 */
public enum TestStatus {

	/**
	 * A unit test is required.
	 */
	REQUIRED,

	/**
	 * A unit test is desired but not required.
	 */
	UNEXPECTED,
	
	/**
	 * No unit test is required or should be developed. Generally used for
	 * interfaces and simple enumerations.
	 */
	UNREQUIRED;
}
