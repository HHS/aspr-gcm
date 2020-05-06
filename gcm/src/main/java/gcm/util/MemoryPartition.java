package gcm.util;

import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;

/**
 * MemoryPartition gathers the parent-child relationships used to partition
 * memory to a tree of objects that will in turn be used to form the memory
 * report.
 * 
 * @author Shawn Hatch
 *
 */
@Source
public final class MemoryPartition {

	private final List<MemoryLink> memoryLinks = new ArrayList<>();

	public List<MemoryLink> getMemoryLinks() {
		return new ArrayList<>(memoryLinks);
	}

	public void addMemoryLink(Object parent, Object child, String descriptor) {
		MemoryLink memoryLink = new MemoryLink(parent, child, descriptor);
		memoryLinks.add(memoryLink);
	}
}
