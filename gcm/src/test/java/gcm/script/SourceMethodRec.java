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
public final class SourceMethodRec {
	
	public SourceMethodRec(SourceClassRec sourceClassRec) {		
		this.sourceClassRec = sourceClassRec;
	}
	
	private final SourceClassRec sourceClassRec;
	
	private final List<Method> methods = new ArrayList<>();
	
	public Method getMethod(int index) {
		return methods.get(index);
	}
	
	public void addMethod(Method method) {
		methods.add(method);
	}
	public String getName() {
		if(methods.size()==0) {
			return "unknown method name";
		}
		return methods.get(0).getName();
	}
	
	public int getMethodCount() {
		return methods.size();
	}
	public SourceClassRec getSourceClassRec() {
		return sourceClassRec;
	}
}