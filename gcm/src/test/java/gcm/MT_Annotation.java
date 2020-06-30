package gcm;

import java.lang.reflect.Method;
import java.util.TreeSet;

public class MT_Annotation {
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		
		Class<?> c = Class.forName("gcm.util.vector.MutableVector2D");
		System.out.println(c.getSimpleName());		
		TreeSet<Integer> treeSet = new TreeSet<>();
		treeSet.iterator();
		
		
		Class<?> c1 = Class.forName("gcm.util.vector.MutableVector2D");
		Class<?> c2 = double.class;
		
		Method method = c.getMethod("rotateToward", c1, c2);
		System.out.println(method);

	}
}
