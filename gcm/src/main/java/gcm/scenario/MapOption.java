package gcm.scenario;

import gcm.util.annotations.Source;

/**
 * Enumeration for the control of reverse mapping of properties and other
 * attributes to the sets of people who have those properties.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public enum MapOption {
	/**
	 * The reverse mapping is not supported
	 */
	NONE,

	/**
	 * Reverse mapping is supported using an array based methodology that is
	 * nearly as fast as the hash based methodology, but requires much less
	 * memory.
	 */
	ARRAY,

	/**
	 * Reverse mapping is supported and will be generally fast, but will require
	 * the most memory of these option.
	 */
	HASH
}
