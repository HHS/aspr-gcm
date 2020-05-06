package gcm.util.delaunay;

public interface GeoCoordinate {

		/**
		 * Returns a latitude value in range [-90,90] degrees.
		 */
		public double getLatitude();

		/**
		 * Returns a latitude value in range [-180,180] degrees.
		 */
		public double getLongitude();

	}