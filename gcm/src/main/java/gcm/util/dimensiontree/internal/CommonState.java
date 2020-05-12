package gcm.util.dimensiontree.internal;

import java.awt.Dimension;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * The common parameters shared by all nodes that takes up less memory than
 * storing these values on each node
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = Dimension.class)
public class CommonState {

	public final int leafSize;

	public final int dimension;

	public final int childCount;

	public CommonState(int leafSize, int dimension) {
		this.leafSize = leafSize;
		this.dimension = dimension;
		this.childCount = 1 << dimension;
	}

}
