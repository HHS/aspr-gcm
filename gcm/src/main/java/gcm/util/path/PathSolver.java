package gcm.util.path;

import java.util.Optional;

public interface PathSolver<N,E> {

	public Optional<Path<E>> getPath(N originNode, N destinationNode);
	
}
