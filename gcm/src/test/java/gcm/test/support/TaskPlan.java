package gcm.test.support;

import gcm.simulation.Environment;
import gcm.simulation.Plan;

/**
 * Component implementor for all tasks.
 */
public class TaskPlan implements Plan {
	public interface Task {
		public void execute(Environment environment);
	}

	private final double scheduledTime;

	private final Object key;

	private boolean executed;

	private final Task task;

	public TaskPlan(final double scheduledTime, Object key, Task task) {
		this.scheduledTime = scheduledTime;
		this.key = key;
		this.task = task;
	}

	public boolean executed() {
		return executed;
	}

	public void executeTask(final Environment environment) {
		task.execute(environment);
		executed = true;
	}

	public Object getKey() {
		return key;
	}

	public double getScheduledTime() {
		return scheduledTime;
	}

	public boolean planExecuted() {
		return executed;
	}

}
