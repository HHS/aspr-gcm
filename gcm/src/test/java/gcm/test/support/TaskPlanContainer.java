package gcm.test.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gcm.scenario.ComponentId;
import gcm.test.support.TaskPlan.Task;

/**
 * A mutable container for holding TestPlans that are distributed to a test
 * simulation's component instances.
 * 
 * @author Shawn Hatch
 *
 */
public final class TaskPlanContainer {
	
	private int masterKey;

	private final Map<ComponentId, List<TaskPlan>> taskPlanMap = new LinkedHashMap<>();

	public TaskPlan addTaskPlan(final ComponentId componentId, final double scheduledTime, Task task) {
		if (componentId == null) {
			throw new RuntimeException("null component id");
		}
		if (task == null) {
			throw new RuntimeException("null test plan");
		}
		List<TaskPlan> list = taskPlanMap.get(componentId);
		if (list == null) {
			list = new ArrayList<>();
			taskPlanMap.put(componentId, list);
		}
		TaskPlan taskPlan = new TaskPlan(scheduledTime, masterKey++, task);
		list.add(taskPlan);
		return taskPlan;
	}

	public List<TaskPlan> getTaskPlans(final ComponentId componentId) {
		final List<TaskPlan> result = new ArrayList<>();
		final List<TaskPlan> list = taskPlanMap.get(componentId);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}
	
	public List<ComponentId> getComponentIds(){
		return new ArrayList<>(taskPlanMap.keySet());
	}

}
