package gcm.manual.newagent;

import gcm.manual.newagent.ContextManager.Context;
import gcm.manual.newagent.events.Event;
import gcm.manual.newagent.events.MessageEvent;
import gcm.manual.newagent.events.ValueEvent;
import gcm.manual.newagent.events.Vector2DEvent;

public class AgentBehaviorProvider {

	private static void report(String methodName, Context context, Event event) {
		System.out.println(methodName + " is processing " + event + " for " + context.getAgentId());
	}

	public static void handleValueEvent_INFECTED(Context context, ValueEvent valueEvent) {
		report("AgentBehaviorProvider.handleValueEvent_INFECTED() ", context, valueEvent);
	}

	public static void handleValueEvent_EXPOSED(Context context, ValueEvent valueEvent) {
		report("AgentBehaviorProvider.handleValueEvent_EXPOSED()", context, valueEvent);
	}

	public static void handleVectorEvent_EXPOSED(Context context, Vector2DEvent vector2DEvent) {
		report("AgentBehaviorProvider.handleVectorEvent_EXPOSED()", context, vector2DEvent);
	}

	public static void handleMessageEvent_RECOVERED(Context context, MessageEvent messageEvent) {
		report("AgentBehaviorProvider.handleMessageEvent_RECOVERED()", context, messageEvent);
	}	
	
	public static void handleVectorEvent_EXPOSED_custom(Context context, Vector2DEvent vector2DEvent) {
		report("AgentBehaviorProvider.handleVectorEvent_EXPOSED_custom()", context, vector2DEvent);
	}
}
