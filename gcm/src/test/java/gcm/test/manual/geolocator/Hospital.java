package gcm.test.manual.geolocator;

public class Hospital {
	private final String name;
	private final double lat;
	private final double lon;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Hospital [lat=");
		builder.append(lat);
		builder.append(", lon=");
		builder.append(lon);
		builder.append("]");
		return builder.toString();
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getName() {
		return name;
	}

	public Hospital(String name, double lat, double lon) {
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}

}