package gcm.script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gcm.util.annotations.Source;
import gcm.util.annotations.TestStatus;

/**
 * Container for all information collected on a GCM source file
 * 
 * @author Shawn Hatch
 *
 */
public final class SourceClassRec {

	private final List<TestClassRec> testClassRecs = new ArrayList<>();
	private final Class<?> sourceClass;
	private final TestStatus testStatus;
	private final Class<?> proxyClass;
	private Map<String, SourceMethodRec> methodMap = new TreeMap<>();

	public void addTestClassRec(TestClassRec testClassRec) {
		testClassRecs.add(testClassRec);
	}

	public List<TestClassRec> getTestClassRecs() {
		return new ArrayList<>(testClassRecs);
	}

	public Class<?> getSourceClass() {
		return sourceClass;
	}

	public TestStatus getTestStatus() {
		return testStatus;
	}

	public Class<?> getProxyClass() {
		return proxyClass;
	}

	public List<String> getMethodNames() {
		return new ArrayList<>(methodMap.keySet());
	}

	public SourceMethodRec getSourceMethodRec(String methodName) {
		return methodMap.get(methodName);
	}

	public List<SourceMethodRec> getSourceMethodRecs() {
		return new ArrayList<>(methodMap.values());
	}

	public SourceClassRec(Class<?> sourceClass, Source source) {
		this.sourceClass = sourceClass;
		if (source != null) {
			testStatus = source.status();
			if (source.proxy() != Object.class) {
				proxyClass = source.proxy();
			}else {
				proxyClass = null;
			}
		} else {
			testStatus = TestStatus.REQUIRED;
			proxyClass = null;
		}

		Method[] methods = sourceClass.getMethods();
		for (Method method : methods) {
			boolean addRec = true;
			addRec &= !method.getDeclaringClass().equals(Object.class);
			addRec &= !method.getDeclaringClass().equals(Enum.class);
			addRec &= !method.getDeclaringClass().equals(Throwable.class);
			addRec &= !method.isBridge();
			if (addRec) {
				addMethod(method);
			}
		}

	}

	private void addMethod(Method method) {		
		SourceMethodRec sourceMethodRec = methodMap.get(method.getName());
		if (sourceMethodRec == null) {
			sourceMethodRec = new SourceMethodRec(this);
			methodMap.put(method.getName(), sourceMethodRec);
		}
		sourceMethodRec.addMethod(method);

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SourceClassRec [classRef=");
		builder.append(sourceClass);
		builder.append(", sourceMethodRecs=");
		builder.append(methodMap);
		builder.append("]");
		return builder.toString();
	}

}
