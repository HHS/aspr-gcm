package gcm.methodexamination;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gcm.util.containers.ArrayIntSet;

public class MethodOrganizationTest {

	public static void main(String[] args) {
		List<Method> methods = new ArrayList<>();
		methods.addAll(getMethods(A.class));
		methods.addAll(getMethods(B.class));
		methods.addAll(getMethods(C.class));
		methods.addAll(getMethods(P.class));
		methods.addAll(getMethods(Q.class));
		methods.addAll(getMethods(R.class));

		Set<Method> set = new LinkedHashSet<>(methods);

		assertEquals(methods.size(), set.size());
		
		
		Method[] methods2 = ArrayIntSet.class.getMethods();
		for(Method method : methods2) {
			System.out.println(method);
		}

	}

	private static List<Method> getMethods(Class<?> c) {
		List<Method> result = new ArrayList<>();
		Method[] methods = c.getMethods();
		for (Method method : methods) {
			ExampleAnnotation exampleAnnotation = method.getAnnotation(ExampleAnnotation.class);
			if (exampleAnnotation != null) {
				if (method.getDeclaringClass().equals(c)) {
					result.add(method);
				}
				// System.out.println(method);
				// System.out.println(method.getDeclaringClass().getSimpleName());
				// System.out.println(sketchyAnnotation.toString());
				// System.out.println();
			}
		}
		return result;
	}
}
