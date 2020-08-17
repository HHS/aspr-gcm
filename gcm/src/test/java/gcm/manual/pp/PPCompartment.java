package gcm.manual.pp;

import java.util.Random;

import gcm.scenario.CompartmentId;

public enum PPCompartment implements CompartmentId {
		COMP1, COMPARTMENT2, C3, COMP4;
		public static PPCompartment getRandomCompartment(Random random) {
			int ord = random.nextInt(values().length);
			return values()[ord];
		}
	}
