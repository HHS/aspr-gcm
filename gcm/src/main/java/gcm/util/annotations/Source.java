package gcm.util.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for JUnit test classes that are tested via a proxy test. Any class
 * marked with this annotation should not be part of the public GCM API and is
 * considered tested by the test of the class that is the proxy.
 * 
 * @author Shawn Hatch
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Source {
	Class<?> proxy() default Object.class;
	TestStatus status() default TestStatus.REQUIRED;
}