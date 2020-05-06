package gcm.simulation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.OutputItem;
import gcm.output.OutputItemHandler;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.MemoryPartition;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Implementor of OutputItemManager.
 * 
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.PROXY, proxy = EnvironmentImpl.class)
public final class OutputItemManagerImpl extends BaseElement implements OutputItemManager {

	private final Map<Class<? extends OutputItem>, Set<OutputItemHandler>> handlerMap = new LinkedHashMap<>();
	private final List<OutputItemHandler> outputItemHandlers = new ArrayList<>();
	private ScenarioId scenarioId;
	private ReplicationId replicationId;

	@Override
	public void init(Context context) {
		super.init(context);
		scenarioId = context.getScenario().getScenarioId();
		replicationId = context.getReplication().getId();

		/*
		 * If the profile report is active, then wrap each output item handler
		 * in a proxy so that we can differentiate run time characteristics at
		 * the handler level
		 */
		if (context.produceProfileItems()) {
			ProfileManager profileManager = context.getProfileManager();
			for (OutputItemHandler outputItemHandler : context.getOutputItemHandlers()) {
				OutputItemHandler proxiedOutputItemHandler = profileManager.getProfiledProxy(outputItemHandler);
				outputItemHandlers.add(proxiedOutputItemHandler);
			}
		} else {
			for (OutputItemHandler outputItemHandler : context.getOutputItemHandlers()) {
				outputItemHandlers.add(outputItemHandler);
			}
		}

		/*
		 * Map the handlers to the output item sub-types they handle.
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			Set<Class<? extends OutputItem>> handledClasses = outputItemHandler.getHandledClasses();
			for (Class<? extends OutputItem> outputItemClass : handledClasses) {
				Set<OutputItemHandler> handlers = handlerMap.get(outputItemClass);
				if (handlers == null) {
					handlers = new LinkedHashSet<>();
					handlerMap.put(outputItemClass, handlers);
				}
				handlers.add(outputItemHandler);
			}
		}
		/*
		 * Inform each output item handler that the simulation has started.
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			outputItemHandler.openSimulation(context.getScenario().getScenarioId(), context.getReplication().getId());
		}
	}

	@Override
	public void releaseOutputItem(OutputItem outputItem) {
		Set<OutputItemHandler> handlers = handlerMap.get(outputItem.getClass());

		/*
		 * It may happen that the class of an output item do not explicitly
		 * match any handler, but that it compatible with that handler.
		 * 
		 * For example suppose handlerX lists OutputItemY class as a type it
		 * handles. When we encounter an instance of OutputItemZ that is a
		 * descendant of OutputItemY then we would want to extend the content of
		 * the handlerMap so that all OutputItemZ are mapped to handlerX.
		 */
		if (handlers == null) {
			handlers = new LinkedHashSet<>();
			for (Class<? extends OutputItem> outputItemClass : handlerMap.keySet()) {
				if (outputItemClass.isAssignableFrom(outputItem.getClass())) {
					handlers.addAll(handlerMap.get(outputItemClass));
				}
			}
			handlerMap.put(outputItem.getClass(), handlers);
		}

		/*
		 * It is possible that the handlers set is empty. In that case the
		 * output item will be ignored.
		 */
		for (OutputItemHandler outputItemHandler : handlers) {
			outputItemHandler.handle(outputItem);
		}
	}

	@Override
	public void close() {
		/*
		 * Let the output item handlers know that the simulation is closed
		 */
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			outputItemHandler.closeSimulation(scenarioId, replicationId);
		}

	}

	@Override
	public void collectMemoryLinks(MemoryPartition memoryPartition) {

		/*
		 * Organize the output item handlers by their implementation class types
		 * so that we may assign reasonable names in the memory links
		 */
		Map<Class<? extends OutputItemHandler>, List<OutputItemHandler>> map = new LinkedHashMap<>();
		for (OutputItemHandler outputItemHandler : outputItemHandlers) {
			List<OutputItemHandler> list = map.get(outputItemHandler.getClass());
			if (list == null) {
				list = new ArrayList<>();
				map.put(outputItemHandler.getClass(), list);
			}
			list.add(outputItemHandler);
		}

		for (Class<? extends OutputItemHandler> handlerClass : map.keySet()) {
			List<OutputItemHandler> list = map.get(handlerClass);
			int index = 1;
			for (OutputItemHandler outputItemHandler : list) {
				memoryPartition.addMemoryLink(this, outputItemHandler, handlerClass.getSimpleName() + " " + index);
				index++;
			}
		}

	}

}
