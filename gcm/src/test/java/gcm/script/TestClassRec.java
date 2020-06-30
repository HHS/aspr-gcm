package gcm.script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gcm.util.annotations.UnitTest;

/**
 * Container for all information collected on a GCM source file
 * 
 * @author Shawn Hatch
 *
 */
public final class TestClassRec {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestClassRec [testClass=");
		builder.append(testClass);
		builder.append(", sourceClass=");
		builder.append(sourceClass);
		builder.append(", automated=");
		builder.append(automated);
		builder.append("]");
		return builder.toString();
	}

	private SourceClassRec sourceClassRec;

	public SourceClassRec getSourceClassRec() {
		return sourceClassRec;
	}

	private final Class<?> testClass;

	private Class<?> sourceClass;

	private boolean automated;

	public Class<?> getTestClass() {
		return testClass;
	}

	public void setSourceClassRec(SourceClassRec sourceClassRec) {
		this.sourceClassRec = sourceClassRec;
	}

	public Class<?> getSourceClass() {
		return sourceClass;
	}

	public boolean isAutomated() {
		return automated;
	}

	private Map<String, TestMethodRec> methodMap = new LinkedHashMap<>();

	public List<String> getMethodNames() {
		return new ArrayList<>(methodMap.keySet());
	}

	public TestMethodRec getTestMethodRec(String methodName) {
		return methodMap.get(methodName);
	}

	public List<TestMethodRec> getTestMethodRecs() {
		return new ArrayList<>(methodMap.values());
	}

	public TestClassRec(Class<?> testClass, UnitTest unitTest) {
		this.testClass = testClass;
		this.sourceClass = unitTest.target();
		this.automated = unitTest.automated();
		Method[] methods = testClass.getMethods();
		for (Method method : methods) {
			Test test = method.getAnnotation(Test.class);
			if(test != null) {
				addMethod(method);			
			}
		}

	}

	private void addMethod(Method method) {
		TestMethodRec testMethodRec = methodMap.get(method.getName());
		if (testMethodRec == null) {
			testMethodRec = new TestMethodRec(this);
			methodMap.put(method.getName(), testMethodRec);
		}
		testMethodRec.addMethod(method);
	}

}
