package gcm.test.manual.ppp;

import java.util.ArrayList;
import java.util.List;

public class EventPackage {

	private final List<Object> events = new ArrayList<>();

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private List<Object> events = new ArrayList<>();

		private Builder() {
		}

		public EventPackage build() {
			try {
				return new EventPackage(events);
			} finally {
				events = new ArrayList<>();
			}
		}
		public void addEvent(Object event) {
			events.add(event);
		}
	}
	
	public boolean isEmpty() {
		return events.size()==0;
	}
	
	public int size() {
		return events.size();
	}

	private EventPackage(List<Object> events) {
		this.events.addAll(events);
	}

	public List<Object> getEvents() {
		return new ArrayList<>(events);
	}
}
