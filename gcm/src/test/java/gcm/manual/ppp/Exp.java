package gcm.manual.ppp;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class Exp {

	private static class RunnableSim implements Runnable {
		private final Sim sim;

		public RunnableSim(Sim sim) {
			this.sim = sim;
		}

		@Override
		public void run() {
			sim.execute();
		}

	}

	public void execute() {
		Random random = new Random(4583458585613456L);

		// create the members
		Set<String> memberIds = new LinkedHashSet<>();
		memberIds.add("Alpha");
		memberIds.add("Beta");
		memberIds.add("Gamma");

		// create sims

		// create the runnables
		Exchange exchange = new Exchange(memberIds);

		for (String memberId : memberIds) {
			double maxProcessingDelay = 1000;
			if(memberId.equals("Gamma")) {
				maxProcessingDelay = 3000;
			}
			Sim sim = new Sim(memberId, exchange, random.nextLong(),maxProcessingDelay);
			RunnableSim runnableSim = new RunnableSim(sim);
			Thread thread = new Thread(runnableSim);
			thread.start();
		}
	}
}
