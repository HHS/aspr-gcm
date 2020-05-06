package gcm.util.containers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gcm.scenario.IntId;
import gcm.util.annotations.Source;

/**
 * An IntSet implementor based on LinkedHashSet. This implementation is designed
 * to have the best runtime performance, but requires significant memory
 * overhead per element.
 */
@Source
public final class HashIntSet<T extends IntId> implements IntSet<T> {

	/*
	 * Records the maximum size the values instance set. Used to rebuild the
	 * values set if the values set is less than half of its past max size. This
	 * is done to release unused record structures in the LinkedHashSetInstance.
	 */
	private int maxSize;

	private Set<T> values = new LinkedHashSet<>();

	@Override
	public void add(T t) {
		if (values.add(t)) {
			if (maxSize < values.size()) {
				maxSize = values.size();
			}
		}
	}

	@Override
	public void remove(T t) {
		if (values.remove(t)) {
			if (values.size() * 2 < maxSize) {
				values = new LinkedHashSet<>(values);
				maxSize = values.size();
			}
		}
	}

	@Override
	public List<T> getValues() {
		return new ArrayList<>(values);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public boolean contains(T t) {
		return values.contains(t);
	}

}
