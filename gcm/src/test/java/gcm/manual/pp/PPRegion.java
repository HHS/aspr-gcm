package gcm.manual.pp;

import java.util.Random;

import gcm.scenario.RegionId;

public enum PPRegion implements RegionId {
		REGION_1("Fairfax"), REGION_2("Fairfax"), REGION_3("Fairfax"), REGION_4("Loudoun"), REGION_5("Prince William"),
		REGION_6("Prince William");

		private final String county;

		private PPRegion(String county) {
			this.county = county;

		}

		public String getCounty() {
			return county;
		}

		public static PPRegion getRandomRegion(Random random) {
			int ord = random.nextInt(values().length);
			return values()[ord];
		}
	}
