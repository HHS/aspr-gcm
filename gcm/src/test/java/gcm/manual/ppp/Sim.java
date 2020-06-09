package gcm.manual.ppp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import gcm.util.TimeElapser;

public class Sim {
	private final Object memberId;
	private final Exchange exchange;
	private final List<Object> otherMembers;
	private final long seed;
	private int maxEventCount = 15;
	private final double maxProcessingDelay;

	public Sim(Object memberId, Exchange exchange, long seed, double maxProcessingDelay) {
		this.memberId = memberId;
		this.exchange = exchange;
		otherMembers = new ArrayList<>(exchange.getMemberIds());
		otherMembers.remove(memberId);
		this.seed = seed;
		this.maxProcessingDelay = maxProcessingDelay;
	}

	private void report(Object message) {
		
		System.out.println(exchange.getTime()+": "+memberId.toString() + ": " + message.toString());
	}

	public void execute() {
		Random random = new Random(seed);
		//report("initialized random");

		int minLoopCount = random.nextInt(10) + 2;
		//report("minLoopCount =  " + minLoopCount);
		// loop until some count is met and then start trying to terminate
		int loopCount = 0;
		int eventId = 0;
		while (true) {
			// wait some amount of time -- represents the activity of the sim
			double millisecondsToWait = random.nextDouble() * maxProcessingDelay;
			//report("established wait time to emulated sim activity " + millisecondsToWait);
			TimeElapser timeElapser = new TimeElapser();
			while (timeElapser.getElapsedMilliSeconds() < millisecondsToWait) {
				// do nothing
			}
			//report("completed waiting for emulated sim activity");

			/*
			 * Represent the publication of a few events. If we are past the min
			 * loop count, then begin reducing the number of events that are
			 * produced to zero over several loops.
			 */

			if (maxEventCount > 0) {
				if (loopCount >= minLoopCount) {
					maxEventCount = random.nextInt(maxEventCount);
				}
			}
			if (maxEventCount > 0) {
				int numberOfEvents = random.nextInt(maxEventCount);
				for (int i = 0; i < numberOfEvents; i++) {
					Object memId = otherMembers.get(random.nextInt(otherMembers.size()));
					StringBuilder sb = new StringBuilder();
					sb.append("sender ");
					sb.append(memberId);
					sb.append("-->");
					sb.append("receiver ");
					sb.append(memId);
					sb.append(" :");
					sb.append(eventId++);
					String event = sb.toString();
					report("sending event : " + event);
					exchange.putEvent(memId, event);
				}
			}

			/*
			 * Represents the eventPackage plan of the injected component
			 * waiting until the exchange has an event package
			 */
			loopCount++;
			//report("loopCount = " + loopCount);
			boolean readyForTermination = loopCount >= minLoopCount;

			exchange.signalReadyForProgress(memberId, readyForTermination);
			report("signalling ready to progress, readyForTermination = "+readyForTermination);

			EventPackage eventPackage = null;
			int attempts = 0;
			while (eventPackage == null) {
				Optional<EventPackage> events = exchange.getEvents(memberId);
				attempts++;
				if (events.isPresent()) {
					report("event package is present after "+attempts+" attempts");
					eventPackage = events.get();
				} else {
					try {
						//report("event package is not present -- starting wait");						
						Thread.sleep(1);
						//report("ending wait");
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// show the events received
			//eventPackage.getEvents().forEach(event -> report("received event " + event));

			if (exchange.terminationAchieved()) {
				report("achieved termination permission");
				break;
			}

		}

	}

}
