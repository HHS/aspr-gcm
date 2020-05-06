package gcm.util.dimensiontree.internal;

public interface Shape {
	
	public boolean containsPosition(double[] position);
	
	public<T> ShapeIntersectionType intersectsBox(Node<T> node);
}
