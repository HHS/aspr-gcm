package gcm.simulation;

import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A convenience implementor of Element that throws RuntimeExceptions
 * if init() is invoked more than once.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public class BaseElement implements Element {

	private boolean initialized;

	@Override
	public void init(Context context) {
		if (initialized) {
			throw new RuntimeException("previously initialized");
		}
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {

	}

}
