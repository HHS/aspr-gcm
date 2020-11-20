package gcm.simulation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gcm.output.simstate.ProfileItem;
import gcm.scenario.ComponentId;
import gcm.scenario.ReplicationId;
import gcm.scenario.ScenarioId;
import gcm.util.TimeElapser;
import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;
import gcm.util.stats.MutableStat;

/**
 * A Manager for the production of {@link ProfileItem} that detail the execution
 * statistics for each method in the simulation.
 *
 * This is accomplished by selectively wrapping the major elements of the
 * simulation in proxies that are controlled by
 * {@link java.lang.reflect.InvocationHandler} instances.
 *
 * The invocation handlers record nano time before and after the underlying
 * instance method is invoked. Time information is gathered via aggregation and
 * is kept in a tree structure that mirrors the loading of methods onto the
 * stack.
 *
 * @author Shawn Hatch
 *
 */
@Source(status = TestStatus.REQUIRED, proxy = EnvironmentImpl.class)
public final class ProfileManager extends BaseElement {
	/*
	 * A class representing a node in a tree that mirrors the execution tree for
	 * methods loaded on the stack. The same method may have multiple nodes
	 * depending on the object invoking that method. Each node is used to record
	 * aggregate time statistics for that particular instance of invocation.
	 *
	 *
	 */
	private static class MethodRecord {
		/*
		 * Unique identifier used to link parent to child in the report. A method will
		 * have a stable id in the context of the stack and the simulation instance that
		 * creates this MethodRecord.
		 */
		private int id;

		/*
		 * Depth in the stack relative to the Context of this method
		 */
		private int depth;

		/*
		 * The component that is invoking (perhaps indirectly) the method.
		 */
		private ComponentId componentId;

		/*
		 * The method being tracked. The same method may be tracked in multiple
		 * MethodRecords depending on the other
		 */
		private Method method;

		/*
		 * The class that is invoking the method
		 */
		private Class<?> invocationClass;

		/*
		 * The MethodRecord for the invoker of this MethodRecord's method. May be null.
		 */
		private MethodRecord parent;

		/*
		 * The MethodRecords that correspond to methods that are called from this
		 * MethodRecord's method.
		 */

		private final Map<MethodRecord, MethodRecord> children = new LinkedHashMap<>();
		/*
		 * The collected durations of this method's executions
		 */
		private final MutableStat mutableStat = new MutableStat();

		/*
		 * Records the nano seconds for each invocation of the method used to fill the
		 * mutableStat
		 */
		private final TimeElapser timeElapser = new TimeElapser();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((componentId == null) ? 0 : componentId.hashCode());
			result = prime * result + ((invocationClass == null) ? 0 : invocationClass.hashCode());
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof MethodRecord)) {
				return false;
			}
			MethodRecord other = (MethodRecord) obj;
			if (componentId == null) {
				if (other.componentId != null) {
					return false;
				}
			} else if (!componentId.equals(other.componentId)) {
				return false;
			}
			if (invocationClass == null) {
				if (other.invocationClass != null) {
					return false;
				}
			} else if (!invocationClass.equals(other.invocationClass)) {
				return false;
			}
			if (method == null) {
				if (other.method != null) {
					return false;
				}
			} else if (!method.equals(other.method)) {
				return false;
			}
			return true;
		}

		/*
		 * Starts the recording of the method's duration for a single invocation of the
		 * method
		 */
		public void start() {
			timeElapser.reset();
		}

		/*
		 * Stops the recording of the method's duration for a single invocation of the
		 * method
		 */
		public void stop() {
			mutableStat.add(timeElapser.getElapsedMilliSeconds());
		}

		/*
		 * Boilerplate implementation
		 */
		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("MethodRecord [id=");
			builder.append(id);
			builder.append(", componentId=");
			builder.append(componentId);
			builder.append(", method=");
			builder.append(method.getName());
			builder.append(", invocationClass=");
			builder.append(invocationClass.getSimpleName());
			builder.append(", parent=");
			if (parent != null) {
				builder.append(parent.id);
			} else {
				builder.append(-1);
			}
			builder.append(", children=[");
			boolean first = true;
			for (final MethodRecord childMethodRecord : children.keySet()) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}
				builder.append(childMethodRecord.id);
			}
			builder.append("]");
			builder.append(", mutableStat=");
			builder.append(mutableStat);
			builder.append(", timeElapser=");
			builder.append(timeElapser.getElapsedMilliSeconds());
			builder.append("]");
			return builder.toString();
		}

	}

	private static class ProfileInvocationHandler implements InvocationHandler {

		private final ProfileManager profileManager;

		private final Object target;

		public ProfileInvocationHandler(final Object target, final ProfileManager profileManager) {
			this.target = target;
			this.profileManager = profileManager;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			profileManager.reportInvocationStart(target.getClass(), method);
			try {
				try {
					final Object result = method.invoke(target, args);
					return result;
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			} finally {
				profileManager.reportInvocationStop();
			}
		}

	}

	/*
	 * Returns the set of classes and interfaces that are implemented/extended by
	 * the given classRef
	 */
	private static Set<Class<?>> getClasses(final Class<?> classRef) {
		Class<?> c = classRef;
		final LinkedHashSet<Class<?>> set = new LinkedHashSet<>();
		while (c != null) {
			set.add(c);
			getInterfaces(c, set);
			c = c.getSuperclass();
		}
		return set;
	}

	/*
	 * Returns the interface(recursively determined) for the given class reference
	 */
	private static void getInterfaces(final Class<?> c, final Set<Class<?>> set) {
		final Class<?>[] interfaces = c.getInterfaces();
		for (final Class<?> d : interfaces) {
			set.add(d);
			getInterfaces(d, set);
		}
	}

	/*
	 * Returns an array of just the recursively determined interfaces implemented by
	 * the given object instance.
	 */
	private static Class<?>[] getInterfaces(final Object instance) {
		final Set<Class<?>> classes = getClasses(instance.getClass());
		final List<Class<?>> interfaces = new ArrayList<>();
		for (final Class<?> c : classes) {
			if (c.isInterface()) {
				interfaces.add(c);
			}
		}
		final Class<?>[] x = new Class<?>[0];
		return interfaces.toArray(x);
	}

	private int masterId;

	private OutputItemManager outputItemManager;

	private ScenarioId scenarioId;

	private ReplicationId replicationId;

	private ComponentManager componentManager;

	private MethodRecord focus;

	private boolean active;

	private final List<MethodRecord> methodRecords = new ArrayList<>();

	private final Map<Object, Object> proxyToOriginalInstanceMap = new LinkedHashMap<>();

	public ProfileManager() {
		focus = new MethodRecord();
		focus.id = masterId++;
		methodRecords.add(focus);
	}

	/**
	 * Causes this ProfileManager to disgorge its recorded aggregate time data into
	 * {@link ProfileItem} profile items that are distributed to the output item
	 * manager {@link OutputItemManager}. This should only be invoked once at the
	 * end of the simulation run.
	 */
	public void close() {
		if (!active) {
			return;
		}
		active = false;

		for (final MethodRecord methodRecord : methodRecords) {
			if (methodRecord.id == 0) {
				continue;
			}
			int parentId;
			if (methodRecord.parent != null) {
				parentId = methodRecord.parent.id;
			} else {
				parentId = -1;
			}
			final ProfileItem profileItem = ProfileItem.builder()//
					.setId(methodRecord.id)//
					.setDepth(methodRecord.depth)//
					.setParentId(parentId)//
					.setComponentId(methodRecord.componentId)//
					.setClassName(methodRecord.invocationClass.getSimpleName())//
					.setMethodName(methodRecord.method.getName())//
					.setReplicationId(replicationId)//
					.setScenarioId(scenarioId)//
					.setStat(methodRecord.mutableStat)//
					.build();//
			outputItemManager.releaseOutputItem(profileItem);
		}

	}

	/**
	 * Returns the original instance of an object if that object is a proxy created
	 * by this profile manager. Returns the object given if no such mapping exists.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOrginalInstance(final Object possibleProxyInstance) {
		Object result = proxyToOriginalInstanceMap.get(possibleProxyInstance);
		if (result == null) {
			result = possibleProxyInstance;
		}
		return (T) result;
	}

	/**
	 * If the profile report is active, this method returns a proxy instance of the
	 * given instance that implements all of that instance's interface
	 * implementations. Otherwise, it returns the given instance. The proxied
	 * methods will track profiling information for the replaced instance. Any
	 * non-interface defined methods on the original instance will throw an
	 * exception if invoked.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProfiledProxy(final T instance) {
		if (active) {
			final ProfileInvocationHandler profileInvocationHandler = new ProfileInvocationHandler(instance, this);
			ClassLoader classLoader = instance.getClass().getClassLoader();
			Class<?>[] interfaces = getInterfaces(instance);
			final T result = (T) Proxy.newProxyInstance(classLoader, interfaces, profileInvocationHandler);
			proxyToOriginalInstanceMap.put(result, instance);
			return result;
		} else {
			return instance;
		}
	}

	@Override
	public void init(final Context context) {
		super.init(context);
		componentManager = context.getComponentManager();
		outputItemManager = context.getOutputItemManager();
		scenarioId = context.getScenario().getScenarioId();
		replicationId = context.getReplication().getId();
		active = context.produceProfileItems();
		if (active) {
			componentManager.wrapComponentsInProxies();
		}
	}

	private void reportInvocationStart(final Class<?> c, final Method method) {
		if (active) {
			final ComponentId componentId = componentManager.getFocalComponentId();
			setFocus(componentId, c, method);
			focus.start();
		}
	}

	private void reportInvocationStop() {
		if (active) {
			focus.stop();
			focus = focus.parent;
		}
	}

	private void setFocus(final ComponentId componentId, final Class<?> invocationClass, final Method method) {
		final MethodRecord child = new MethodRecord();
		child.componentId = componentId;
		child.invocationClass = invocationClass;
		child.method = method;
		child.id = masterId++;
		child.parent = focus;
		child.depth = focus.depth + 1;

		MethodRecord currentChild = focus.children.get(child);
		if (currentChild != null) {
			focus = currentChild;
			return;
		}

		focus.children.put(child, child);
		focus = child;
		methodRecords.add(focus);
	}

}
