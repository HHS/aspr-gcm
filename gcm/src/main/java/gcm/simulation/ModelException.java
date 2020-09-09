package gcm.simulation;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * A {@link RuntimeException} thrown during Simulation execution. It indicates
 * that the cause of the error is very likely due to invalid input from a
 * modeler contributed object and not an underlying error in GCM. It contains a
 * SimulationErrorType that indicates the general cause of the exception and may
 * also contain additional message text.
 * 
 * Although this is a RuntimeException, it functions like a checked exception,
 * leaving the object throwing the exception in a recoverable state. By making
 * such exceptions recoverable, tests that are forcing documented exceptions
 * will not corrupt the simulation instance being tested and thus the instance
 * can be reused for other tests.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.UNEXPECTED)
public final class ModelException extends RuntimeException {

	private static final long serialVersionUID = -2668978936990585390L;

	private final SimulationErrorType simulationErrorType;

	/*
	 * Package access constructor. Creates a ModelException having the given
	 * ErrorType and a message value of the given description
	 *
	 * @param simulationErrorType
	 */
	public ModelException(final SimulationErrorType simulationErrorType, final String description) {
		//TODO return this to package access?
		super(description);
		this.simulationErrorType = simulationErrorType;
	}

	/**
	 * Returns the SimulationErrorType that documents the general issue that
	 * caused the exception.
	 */
	public SimulationErrorType getErrorType() {
		return simulationErrorType;
	}

}