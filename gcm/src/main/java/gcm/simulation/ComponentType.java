package gcm.simulation;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Enumeration representing the various types of Components. Each component must
 * be of a single type.
 */
@Source(status = TestStatus.UNREQUIRED)
public enum ComponentType {

	GLOBAL, // used for Global Components

	COMPARTMENT, // used for Compartment Components

	REGION, // used for Region Components

	MATERIALS_PRODUCER, // used for Materials Producer Components

	SIM, // used when no component has focus

	INTERNAL;// used for artificial components that are not contributed by the
				// modeler
}