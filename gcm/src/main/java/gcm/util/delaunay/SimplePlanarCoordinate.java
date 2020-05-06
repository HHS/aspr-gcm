package gcm.util.delaunay;

public class SimplePlanarCoordinate implements PlanarCoordinate{
	
	private final double x;
	private final double y;
	public SimplePlanarCoordinate(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	@Override
	public double getX() {
		return x;
	}
	
	@Override
	public double getY() {
		return y;
	}
	
	

}
