package gcm.test.script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
/**
 * Container for all information collected on a GCM public source method
 * 
 * @author Shawn Hatch
 *
 */
public final class TestMethodRec {
	
	public TestMethodRec(TestClassRec testClassRec) {		
		this.testClassRec = testClassRec;
	}
	
	private final TestClassRec testClassRec;
	
	private final List<Method> methods = new ArrayList<>();
	
	public Method getMethod(int index) {
		return methods.get(index);
	}
	
	public void addMethod(Method method) {
		methods.add(method);
	}
	
	
	public int getMethodCount() {
		return methods.size();
	}
	public TestClassRec getTestClassRec() {
		return testClassRec;
	}
}