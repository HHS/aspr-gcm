package gcm.util.delaunay;

public class SimpleGeoCoordinate implements GeoCoordinate{

		private final double latitude;

		private final double longitude;

		@Override
		public double getLatitude() {
			return latitude;
		}

		@Override
		public double getLongitude() {
			return longitude;
		}

		public SimpleGeoCoordinate(double latitude, double longitude) {
			if(latitude>90) {
				throw new RuntimeException("Latitude > 90 degrees");
			}
			if(latitude<-90) {
				throw new RuntimeException("Latitude < -90 degrees");
			}
			if(longitude>180) {
				throw new RuntimeException("Longitude > 180 degrees");
			}
			if(longitude<-180) {
				throw new RuntimeException("Longitude < -180 degrees");
			}
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		

		@Override
		public String toString() {
			return "SimpleGeoCoordinate [latitude=" + latitude + ", longitude=" + longitude + "]";
		}

//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			long temp;
//			temp = Double.doubleToLongBits(latitude);
//			result = prime * result + (int) (temp ^ (temp >>> 32));
//			temp = Double.doubleToLongBits(longitude);
//			result = prime * result + (int) (temp ^ (temp >>> 32));
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (!(obj instanceof GeoCoordinate))
//				return false;
//			GeoCoordinate other = (GeoCoordinate) obj;
//			if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.getLatitude()))
//				return false;
//			if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.getLongitude()))
//				return false;
//			return true;
//		}

	}