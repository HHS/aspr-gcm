package gcm.util.containers;

import gcm.util.annotations.Source;

/**
 * A container for retaining enum values from a single enumeration class indexed
 * by int index. It is designed to generally require approximately 1 byte per
 * index rather than a 4 or 8 byte object reference.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class EnumContainer {

	/*
	 * We store the enum values by their ord equivalents.
	 */
	private IntValueContainer intValueContainer;

	/*
	 * We convert an ord value for an enum into enum member instance via an
	 * ObjectValueContainer.
	 */
	private ObjectValueContainer objectValueContainer;

	/*
	 * The class reference for the enum type
	 */
	private final Class<?> enumClass;

	/**
	 * Constructs an instance of EnumContainer.
	 * 
	 * @throws IllegalArgumentException
	 *             <li>if the class is null
	 *             <li>if the class is not an enumeration
	 *             <li>if the default value is null
	 *             <li>if the default is not a member of the enumeration
	 */
	public EnumContainer(Class<?> c, Object defaultValue, int capacity) {
		if (c == null) {
			throw new IllegalArgumentException("null class reference");
		}
		if (!Enum.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException("cannot construct from class " + c.getName());
		}
		if (defaultValue == null) {
			throw new IllegalArgumentException("null default value");
		}
		if (defaultValue.getClass() != c) {
			throw new IllegalArgumentException("default value " + defaultValue + " does not match enum class " + c);
		}
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity " + capacity + " is less than zero");
		}

		enumClass = c;
		Enum<?> e = (Enum<?>) defaultValue;
		objectValueContainer = new ObjectValueContainer(null, 16);
		objectValueContainer.setValue(e.ordinal(), defaultValue);
		intValueContainer = new IntValueContainer(e.ordinal(),capacity);
	}
	
	public EnumContainer(Class<?> c, Object defaultValue) {
		this(c,defaultValue,0);
	}

	/**
	 * Set the value at the given index.
	 * 
	 * @throws IllegalArgumentException
	 *             <li>if the index is negative
	 *             <li>if the value is null
	 *             <li>if the value is not a member of the enumeration
	 */
	public void setValue(int index, Object value) {

		if (index < 0) {
			throw new IllegalArgumentException("negative index: " + index);
		}

		if (value == null) {
			throw new IllegalArgumentException("null value");
		}
		if (value.getClass() != enumClass) {
			throw new IllegalArgumentException("improper class type for value " + value + ", expected " + enumClass);
		}

		/*
		 * Convert the object value into an instance of Enum
		 */
		Enum<?> e = (Enum<?>) value;

		if (objectValueContainer.getValue(e.ordinal()) == null) {
			objectValueContainer.setValue(e.ordinal(), value);
		}

		intValueContainer.setIntValue(index, e.ordinal());

	}

	/**
	 * Set the value at the given index.
	 * 
	 * @throws IllegalArgumentException
	 *             <li>if the index is negative
	 */
	public Object getValue(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("negative index: " + index);
		}
		/*
		 * Retrieve the ordinal value from the index
		 */
		int ordinal = intValueContainer.getValueAsInt(index);
		/*
		 * Convert the ordinal value into the enum member instance.
		 */
		return objectValueContainer.getValue(ordinal);
	}

}