package gcm.manual.geolocator;

public class Tract  {
	private final String id;
	private final double lat;
	private final double lon;
	private final int populationCount;

	public String getId() {
		return id;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lon;
	}

	public int getPopulationCount() {
		return populationCount;
	}

	public Tract(String id, double lat, double lon, int populationCount) {
		super();
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.populationCount = populationCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tract [id=");
		builder.append(id);
		builder.append(", lat=");
		builder.append(lat);
		builder.append(", lon=");
		builder.append(lon);
		builder.append(", populationCount=");
		builder.append(populationCount);
		builder.append("]");
		return builder.toString();
	}

}
