package gcm.util.dimensiontree.internal;

import java.util.Arrays;

public class NearestMemberQuery<T> {
	
	public double bestSquareDistance = Double.POSITIVE_INFINITY;
	
	public T closestObject = null;
	
	public double[] position;
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NearestMemberQuery [bestSquareDistance=");
		builder.append(bestSquareDistance);
		builder.append(", closestObject=");
		builder.append(closestObject);
		builder.append(", position=");
		builder.append(Arrays.toString(position));
		builder.append("]");
		return builder.toString();
	}
	
	
}
