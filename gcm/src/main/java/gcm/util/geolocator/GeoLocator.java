package gcm.util.geolocator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import gcm.util.dimensiontree.DimensionTree;
import gcm.util.earth.ECC;
import gcm.util.earth.Earth;
import gcm.util.earth.LatLonAlt;
import gcm.util.vector.Vector2D;
import gcm.util.vector.Vector3D;

/**
 * A generics-base utility class for managing point locations on a spherical
 * earth of WGS-84 mean radius. This utility specifically finds locations based
 * on a lat-lon coordinate and search radius.
 * 
 * @author Shawn Hatch
 * 
 */
public class GeoLocator<T> {

	/**
	 * Returns a new instance of the Builder class
	 */
	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	/*
	 * Record for holding the location data collected by the builder
	 */
	private static class LocationRecord<T> {
		private double latDegrees;
		private double lonDegrees;
		private T location;

		public LocationRecord(double lat, double lon, T location) {
			super();
			this.latDegrees = lat;
			this.lonDegrees = lon;
			this.location = location;
		}
	}

	/*
	 * Container for the location records collected by the builder
	 */
	private static class Scaffold<T> {
		List<LocationRecord<T>> locationRecords = new ArrayList<>();
	}

	/**
	 * Builder class for {@linkplain GeoLocator}. Builder instances return to an
	 * empty and ready state post build invocation.
	 * 
	 * @author Shawn Hatch
	 *
	 * @param <T>
	 */
	public static class Builder<T> {
		private Scaffold<T> scaffold = new Scaffold<>();

		private Builder() {
		}

		/**
		 * Adds a location at the given lat and lon degrees.
		 */
		public void addLocation(double latDegrees, double lonDegrees, T location) {
			LocationRecord<T> locationRecord = new LocationRecord<>(latDegrees, lonDegrees, location);
			scaffold.locationRecords.add(locationRecord);
		}

		/**
		 * Returns a {@linkplain GeoLocator} containing the locations presented
		 * to this builder.
		 */
		public GeoLocator<T> build() {
			try {
				return new GeoLocator<>(scaffold);
			} finally {
				scaffold = new Scaffold<>();
			}
		}
	}

	/*
	 * Record for holding the location data converted to Earth centered
	 * coordinates (in meters) as an ECC instance. Each such record corresponds
	 * to one of the location records recorded by the builder.
	 */
	private static class LocationEccRecord<T> {

		T location;
		ECC ecc;
		// Vector3D position;

		public LocationEccRecord(ECC ecc, T location) {

			this.location = location;
			this.ecc = ecc;
			// position = ecc.toVector3D();
		}

	}

	/*
	 * The tree containing the Location position records
	 */
	private final DimensionTree<LocationEccRecord<T>> dimensionTree;

	/*
	 * A utility object for converting to and from lat/lon and ecc coordinates.
	 * This is a spherical earth using a WGS84 mean earth radius.
	 */
	private final Earth earth = Earth.fromMeanRadius();

	private GeoLocator(Scaffold<T> scaffold) {

		/*
		 * Create the Location Position Records(ecc-vector based) from the
		 * Location Records(lat-lon based)
		 */
		List<LocationEccRecord<T>> locationEccRecords = scaffold.locationRecords.stream().map(rec -> {
			LatLonAlt latLonAlt = new LatLonAlt(rec.latDegrees, rec.lonDegrees, 0);
			ECC ecc = earth.getECCFromLatLonAlt(latLonAlt);
			return new LocationEccRecord<>(ecc, rec.location);
		}).collect(Collectors.toList());

		/*
		 * Initialize the bounding box for the tree. This is not strictly
		 * necessary, but it will improve the tree's performance.
		 */
		double[] lowerBounds = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		double[] upperBounds = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };

		/*
		 * Refine the bounding box using the Location Position Records
		 */
		if (locationEccRecords.isEmpty()) {
			lowerBounds = new double[] { -earth.getRadius(), -earth.getRadius(), -earth.getRadius() };
			upperBounds = new double[] { earth.getRadius(), earth.getRadius(), earth.getRadius() };

		} else {
			for (LocationEccRecord<T> rec : locationEccRecords) {
				double[] positionArray = rec.ecc.toArray();
				for (int i = 0; i < 3; i++) {
					lowerBounds[i] = FastMath.min(lowerBounds[i], positionArray[i]);
					upperBounds[i] = FastMath.max(upperBounds[i], positionArray[i]);
				}
			}
		}
		/*
		 * Build the tree
		 */
		dimensionTree = DimensionTree	.builder() //
										.setLowerBounds(lowerBounds)//
										.setUpperBounds(upperBounds)//
										.build();//

		/*
		 * Load the tree with the Location Position Records
		 */
		locationEccRecords.forEach(rec -> {
			dimensionTree.add(rec.ecc.toArray(), rec);
		});
	}

	private double getLinearSearchRangeMeters(double groundRangeInMeters) {
		double angle = groundRangeInMeters / earth.getRadius();
		Vector2D p = new Vector2D(FastMath.cos(angle), FastMath.sin(angle));
		Vector2D q = new Vector2D(1, 0);
		return p.distanceTo(q) * earth.getRadius();
	}

	/**
	 * Returns the list of locations that are with the given radiusKilometers
	 * from the given lat-lon position.
	 */
	public List<T> getLocations(double latDegrees, double lonDegrees, double radiusKilometers) {
		double linearSearchRangeMeters = getLinearSearchRangeMeters(radiusKilometers * 1000);
		double[] position = earth.getECCFromLatLonAlt(new LatLonAlt(latDegrees, lonDegrees, 0)).toVector3D().toArray();
		return dimensionTree.getMembersInSphere(linearSearchRangeMeters, position).stream()//
							.map(rec -> rec.location)//
							.collect(Collectors.toList());//
	}

	/**
	 * Returns the list of locations that are with the given radiusKilometers
	 * from the given lat-lon position as Pairs of location and distance(km).
	 * The resulting pairs are in ascending order by distance.
	 */
	public List<Pair<T, Double>> getPrioritizedLocations(double latDegrees, double lonDegrees, double radiusKilometers) {

		double linearSearchRangeMeters = getLinearSearchRangeMeters(radiusKilometers * 1000);

		LatLonAlt latLonAlt = new LatLonAlt(latDegrees, lonDegrees, 0);
		ECC ecc = earth.getECCFromLatLonAlt(latLonAlt);
		Vector3D position = ecc.toVector3D();
		double[] positionArray = position.toArray();

		return dimensionTree.getMembersInSphere(linearSearchRangeMeters, positionArray).stream()//
							.map(rec -> {
								double distance = earth.getGroundDistanceFromECC(ecc, rec.ecc);
								return new Pair<>(rec.location, distance / 1000);
							})//
							.sorted(Comparator.comparingDouble(Pair::getValue))//
							.collect(Collectors.toList());//
	}

	/**
	 * Returns the nearest location in this {@linkplain GeoLocator} to the given
	 * lat-lon position if any can be found. Ties for nearest location result in
	 * an arbitrary selection.
	 */
	public Optional<T> getNearestLocation(double latDegrees, double lonDegrees) {
		LatLonAlt latLonAlt = new LatLonAlt(latDegrees, lonDegrees, 0);
		ECC ecc = earth.getECCFromLatLonAlt(latLonAlt);
		Vector3D position = ecc.toVector3D();
		double[] positionArray = position.toArray();

		LocationEccRecord<T> locationEccRecord = dimensionTree.getNearestMember(positionArray);
		if (locationEccRecord != null) {
			return Optional.of(locationEccRecord.location);
		}
		return Optional.empty();
	}

}
