package gcm.manual.altpeople;

/**
 * Interface for abstracting the details of how people ids are stored as either
 * a Set or a Boolean container.
 * 
 * @author Shawn Hatch
 */
public interface AltPeopleContainer {

	/*
	 * Returns true if and only if the value was successfully added
	 */
	public boolean add(int value);

	/*
	 * Returns true if and only if the value was successfully removed
	 */
	public boolean remove(int value);

	/*
	 * Returns the number of values in this container
	 */
	public int size();

	/*
	 * Returns true if and only if the value is contained.
	 */
	public boolean contains(int value);

	/*
	 * Returns the value at the given index in the range [0,size()). Returns -1.
	 */
	public int getValue(int index);
}