package gcm.manual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gcm.util.TimeElapser;

/**
 * An asynchronous, static utility for piping text lines from System.in to
 * registered listeners. Primarily used to delay progress of an application so
 * that inspections can be made of the stable state of the application by
 * profiling software.
 * 
 * @author Shawn Hatch
 *
 */

public class SystemInputManager {

	private long listeningIntervalMilliseconds = 3000;

	public static interface InputListener {
		public void handleInput(String input);
	}

	private final static Object mutex = new Object();

	private Set<InputListener> inputListeners = new LinkedHashSet<>();

	private ScheduledExecutorService scheduledExecutorService;

	private BufferedReader bufferReader;

	private final Runnable command = new Runnable() {

		@Override
		public void run() {
			synchronized (mutex) {

				TimeElapser timeElapser = new TimeElapser();
				try {
					boolean ready = bufferReader.ready();
					if (ready) {
						try {
							String line = bufferReader.readLine();
							List<InputListener> list = new ArrayList<>(inputListeners);
							for (InputListener inputListener : list) {
								inputListener.handleInput(line);
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					long elapsedTime = (long) timeElapser.getElapsedMilliSeconds();
					long delayTime = Math.max(0, listeningIntervalMilliseconds - elapsedTime);
					scheduledExecutorService.schedule(command, delayTime, TimeUnit.MILLISECONDS);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}

		}
	};

	private SystemInputManager() {

	}

	public static void addInputListener(InputListener inputListener) {
		synchronized (mutex) {
			if (inputListener != null) {
				prepInstance();
				instance.inputListeners.add(inputListener);
			}
		}
	}

	public static void removeInputListener(InputListener inputListener) {
		synchronized (mutex) {
			if (instance != null) {
				instance.inputListeners.remove(inputListener);
				if (instance.inputListeners.size() == 0) {
					instance._stop();
					instance = null;
				}
			}
		}
	}

	public static void removeAllInputListeners() {
		synchronized (mutex) {
			if (instance != null) {
				instance._stop();
				instance = null;
			}
		}
	}

	public void setListeningInterval(long listeningIntervalMilliseconds) {
		synchronized (mutex) {
			prepInstance();
			instance.listeningIntervalMilliseconds = Math.max(1, listeningIntervalMilliseconds);
		}
	}

	private static SystemInputManager instance;

	private static void prepInstance() {
		if (instance == null) {
			instance = new SystemInputManager();
			instance._start();
		}
	}

	private void _start() {
		bufferReader = new BufferedReader(new InputStreamReader(System.in));
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		command.run();
	}

	private void _stop() {
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdown();
			scheduledExecutorService = null;
		}
	}

}
