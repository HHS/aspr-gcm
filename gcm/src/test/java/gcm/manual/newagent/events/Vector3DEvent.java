package gcm.manual.newagent.events;

public class Vector3DEvent extends Vector2DEvent{
	private final double z;

	public Vector3DEvent(double x, double y, double z) {
		super(x,y);
		this.z = z;
	}


	public double getZ() {
		return z;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Vector3DEvent [getX()=");
		builder.append(getX());
		builder.append(", getY()=");
		builder.append(getY());
		builder.append(", z=");
		builder.append(z);
		builder.append("]");
		return builder.toString();
	}


	

	
	
}
