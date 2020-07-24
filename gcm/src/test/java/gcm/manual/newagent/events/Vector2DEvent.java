package gcm.manual.newagent.events;

public class Vector2DEvent implements Event {
	private final double x;
	private final double y;
	

	public Vector2DEvent(double x, double y) {
		this.x = x;
		this.y = y;		
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Vector2DEvent [x=");
		builder.append(x);
		builder.append(", y=");
		builder.append(y);
		builder.append("]");
		return builder.toString();
	}


	
	
}
