package gcm.manual.newagent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gcm.manual.newagent.ContextManager.Context;
import gcm.manual.newagent.events.Event;

public class Sim {

	private ContextManager contextManager = new ContextManager();

	private Context context = contextManager.getContext();

	private class MetaEventHandler<T extends Event> {
		private final EventHandler<T> eventHandler;

		public MetaEventHandler(EventHandler<T> eventHandler) {
			this.eventHandler = eventHandler;
		}

		@SuppressWarnings("unchecked")
		public void handleEvent(Event event) {
			eventHandler.handleEvent(context, (T) event);
		}
	}

	private Map<Object, Object> agentToAgentTypeMap = new LinkedHashMap<>();

	private Map<Object, Map<Class<? extends Event>, MetaEventHandler<? extends Event>>> eventHandlerMap = new LinkedHashMap<>();

	private Map<Object, Map<Class<? extends Event>, MetaEventHandler<? extends Event>>> customEventHandlerMap = new LinkedHashMap<>();

	public <T extends Event> void registerCustomEventHandler(Object agentId, Class<T> eventClass, EventHandler<T> eventHandler) {

		if (agentId == null) {
			throw new IllegalArgumentException("null agent type id");
		}

		if (!agentToAgentTypeMap.containsKey(agentId)) {
			throw new IllegalArgumentException("unknown agent id " + agentId);
		}

		if (eventClass == null) {
			throw new IllegalArgumentException("null event class");
		}

		if (eventHandler == null) {
			throw new IllegalArgumentException("null event handler class");
		}

		Map<Class<? extends Event>, MetaEventHandler<? extends Event>> eventMap = customEventHandlerMap.get(agentId);
		if (eventMap == null) {
			eventMap = new LinkedHashMap<>();
			customEventHandlerMap.put(agentId, eventMap);
		}
		MetaEventHandler<? extends Event> metaEventHandler = eventMap.get(Event.class);
		if (metaEventHandler == null) {
			metaEventHandler = new MetaEventHandler<>(eventHandler);
			eventMap.put(eventClass, metaEventHandler);
		}
	}

	public <T extends Event> void registerEventHandler(Object agentTypeId, Class<T> eventClass, EventHandler<T> eventHandler) {
		if (agentTypeId == null) {
			throw new IllegalArgumentException("null agent type id");
		}

		if (eventClass == null) {
			throw new IllegalArgumentException("null event class");
		}

		if (eventHandler == null) {
			throw new IllegalArgumentException("null event handler class");
		}

		Map<Class<? extends Event>, MetaEventHandler<? extends Event>> eventMap = eventHandlerMap.get(agentTypeId);
		if (eventMap == null) {
			eventMap = new LinkedHashMap<>();
			eventHandlerMap.put(agentTypeId, eventMap);
		}
		MetaEventHandler<? extends Event> metaEventHandler = eventMap.get(Event.class);
		if (metaEventHandler == null) {
			metaEventHandler = new MetaEventHandler<>(eventHandler);
			eventMap.put(eventClass, metaEventHandler);
		}
	}

	public void sendEvent(Event event, Object agentId) {
		if (agentId == null) {
			throw new IllegalArgumentException("null agent id");
		}

		if (event == null) {
			throw new IllegalArgumentException("null event");
		}

		contextManager.setAgentId(agentId);
		Object agentTypeId = agentToAgentTypeMap.get(agentId);
		if (agentTypeId == null) {
			throw new RuntimeException("unknown agent id " + agentId);
		}
		MetaEventHandler<? extends Event> metaEventHandler;
		Optional<MetaEventHandler<? extends Event>> customOpt = getCustomMetaEventHandler(event, agentId);
		if (customOpt.isPresent()) {
			metaEventHandler = customOpt.get();
		} else {
			Optional<MetaEventHandler<? extends Event>> standardOpt = getMetaEventHandler(event, agentTypeId);
			if (standardOpt.isPresent()) {
				metaEventHandler = standardOpt.get();
			} else {
				throw new RuntimeException("cannot locate event handler for agent id = " + agentId + " and event = " + event);
			}
		}

		metaEventHandler.handleEvent(event);
	}

	private Optional<MetaEventHandler<? extends Event>> getMetaEventHandler(Event event, Object agentTypeId) {
		MetaEventHandler<? extends Event> metaEventHandler = null;
		Map<Class<? extends Event>, MetaEventHandler<? extends Event>> eventMap = eventHandlerMap.get(agentTypeId);

		if (eventMap != null) {

			metaEventHandler = eventMap.get(event.getClass());

			if (metaEventHandler == null) {
				Set<MetaEventHandler<? extends Event>> potentialHandlers = new LinkedHashSet<>();
				for (Class<? extends Event> classKey : eventMap.keySet()) {
					if (classKey.isAssignableFrom(event.getClass())) {
						potentialHandlers.add(eventMap.get(classKey));
					}
				}
				if (potentialHandlers.size() == 1) {
					metaEventHandler = potentialHandlers.iterator().next();
					eventMap.put(event.getClass(), metaEventHandler);
				}

			}
		}

		return Optional.ofNullable(metaEventHandler);

	}

	private Optional<MetaEventHandler<? extends Event>> getCustomMetaEventHandler(Event event, Object agentId) {
		MetaEventHandler<? extends Event> metaEventHandler = null;
		Map<Class<? extends Event>, MetaEventHandler<? extends Event>> eventMap = customEventHandlerMap.get(agentId);
		if (eventMap != null) {
			metaEventHandler = eventMap.get(event.getClass());
			if (metaEventHandler == null) {
				Set<MetaEventHandler<? extends Event>> potentialHandlers = new LinkedHashSet<>();
				for (Class<? extends Event> classKey : eventMap.keySet()) {
					if (classKey.isAssignableFrom(event.getClass())) {
						potentialHandlers.add(eventMap.get(classKey));
					}
				}
				if (potentialHandlers.size() == 1) {
					metaEventHandler = potentialHandlers.iterator().next();
					eventMap.put(event.getClass(), metaEventHandler);
				}
			}
		}
		return Optional.ofNullable(metaEventHandler);

	}

	public void addAgent(Object agentTypeId, Object agent) {
		agentToAgentTypeMap.put(agent, agentTypeId);
	}
}
