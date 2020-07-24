package gcm.manual.newagent;

import gcm.manual.newagent.ContextManager.Context;
import gcm.manual.newagent.events.Event;

public interface EventHandler<T extends Event> {

	public void handleEvent(Context context, T event);
}
