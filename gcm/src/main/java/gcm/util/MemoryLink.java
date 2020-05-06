package gcm.util;

import gcm.util.annotations.Source;
import net.jcip.annotations.ThreadSafe;

/**
 * Represents a parent child relationship in a memory usage report. The child is
 * named by the parent for purposes of reporting. MemoryLinks are created during
 * the collection of the {@link MemoryPartition} that forms the data for a
 * memory report.
 * 
 * @author Shawn Hatch
 *
 */

@Source
@ThreadSafe
public final class MemoryLink {
	private final Object parent;
	private final Object child;
	private final String descriptor;

	/**
	 * Boilerplate implementation using all fields
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	/**
	 * Boilerplate implementation that uses equality of all fields
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MemoryLink)) {
			return false;
		}
		MemoryLink other = (MemoryLink) obj;
		if (child == null) {
			if (other.child != null) {
				return false;
			}
		} else if (!child.equals(other.child)) {
			return false;
		}
		if (descriptor == null) {
			if (other.descriptor != null) {
				return false;
			}
		} else if (!descriptor.equals(other.descriptor)) {
			return false;
		}
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		return true;
	}

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MemoryLink [parent=");
		builder.append(parent);
		builder.append(", child=");
		builder.append(child);
		builder.append(", descriptor=");
		builder.append(descriptor);
		builder.append("]");
		return builder.toString();
	}

	public MemoryLink(Object parent, Object child, String descriptor) {
		super();
		this.parent = parent;
		this.child = child;
		this.descriptor = descriptor;
	}

	/**
	 * Returns the parent object
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Returns the child object
	 */
	public Object getChild() {
		return child;
	}

	/**
	 * Returns the child's descriptor
	 */
	public String getDescriptor() {
		return descriptor;
	}

}
