package gcm.util.dimensiontree.internal;

import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.dimensiontree.DimensionTree;

/**
 * Represent the members that are associated with a single position. Rather than
 * storing members in the leaf nodes, we store member groups. Each member group
 * can contain an unlimited number of members, but all such members have exactly
 * the same position. This is done to prevent infinite branching when there are
 * too many members at the same position.
 */
@Source(status = TestStatus.REQUIRED, proxy = DimensionTree.class)
public class Group<T> {

	public Node<T> node;

	public double position[];

	public List<T> members = new ArrayList<>();
	

	public boolean add(T t, double[] position) {
		if (members.size() == 0) {
			this.position = position.clone();
		}
		if (!members.contains(t)) {
			members.add(t);
			return true;
		}
		return false;
	}


	public boolean canContain(double[] p) {
		if (members.size() == 0) {
			return true;
		}
		for (int i = 0; i < position.length; i++) {
			if (p[i] != position[i]) {
				return false;
			}
		}
		return true;
	}

	public double squareDistanceTo(double[] p) {
		double result = 0;
		for (int i = 0; i < position.length; i++) {
			double value = position[i] - p[i];
			result += value * value;
		}
		return result;
	}

	

}