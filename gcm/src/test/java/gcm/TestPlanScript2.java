package gcm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.junit.runners.Suite.SuiteClasses;

import gcm.util.annotations.Source;
import gcm.util.annotations.SourceMethod;
import gcm.util.annotations.TestStatus;
import gcm.util.annotations.UnitTest;
import gcm.util.annotations.UnitTestMethod;

/**
 * A script covering the details of the GCM Test Plan. It produces a console
 * report that measures the completeness/status of the test classes. It does not
 * measure the correctness of any test, but rather shows which tests exist and
 * their status.
 *
 * @author Shawn Hatch
 *
 */
public class TestPlanScript2 {
	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static String getClassName(Path sourcePath, Path file) {
		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace(File.separator, ".");
	}

	/**
	 * Assumes that the source path and file are consistent
	 */
	private static Class<?> getClassFromFile(Path sourcePath, Path file) {
		try {
			String className = getClassName(sourcePath, file);
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private enum WarningType {

		TEST_ANNOTATION_WITHOUT_SOURCE_METHOD_ANNOTATION("Test method is marked with @Test but does not have a corresponding @UnitTestMethod"),

		SOURCE_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION("Test method is marked with @UnitTestMethod but does not have a corresponding @Test"),

		SOURCE_METHOD_CANNOT_BE_RESOLVED("The source method for a test method cannot be resolved"),

		PROXY_LEADS_OUTSIDE_SOURCE_FOLDER("Source class marked with proxy coverage leads to a class not in the source folder"),

		PROXY_HAS_LOWER_TEST_STATUS("Source class marked with proxy coverage leads to a class that has a lower test status"),

		CIRCULAR_PROXIES("Source class marked with proxy coverage leads to a circular proxy relationship"),

		TEST_CLASS_LINKED_OUTSIDE_SOURCE("Test class linked to source class that is not in the source folder"),

		TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD("Test method linked to unknown source method"),

		TEST_METHOD_LINKED_TO_PROXIED_SOURCE("Test method linked to source method that is proxied to another source class"),

		TEST_METHOD_TESTS_SOURCE_METHOD_THAT_DOES_NOT_REQUIRE_A_TEST("Test method tests source method that does not require a test"),

		SOURCE_METHOD_REQUIRES_TEST("Source method requires a test method but does not have one"),

		SUITE_CLASS_MISSING_TEST_CLASS("Test class not listed in the Suite Test"),

		SUITE_CLASS_CONTAINS_NON_TEST_CLASS("Suite Test contains non test class"),
		
		UNIT_TEST_ANNOTATION_LACKS_SOURCE_CLASS("Unit test annotation lacks source class reference");

		private final String description;

		private WarningType(String description) {
			this.description = description;
		}
	}

	private Map<WarningType, List<String>> warningMap = new LinkedHashMap<>();

	private void addWarning(WarningType warningType, Object details) {
		warningMap.get(warningType).add(details.toString());
	}

	private final static class SourceClassRec {

		private final Class<?> sourceClass;
		private final TestStatus testStatus;
		private final Class<?> proxyClass;

		public SourceClassRec(final Class<?> sourceClass, TestStatus testStatus, Class<?> proxyClass) {
			this.sourceClass = sourceClass;
			this.testStatus = testStatus;
			this.proxyClass = proxyClass;
		}

		public Class<?> getProxyClass() {
			return proxyClass;
		}

		public Class<?> getSourceClass() {
			return sourceClass;
		}

		public TestStatus getTestStatus() {
			return testStatus;
		}
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if (isJavaFile(file)) {
				final Class<?> c = getClassFromFile(sourcePath, file);
				
				if (!c.isAnnotation() && !c.isInterface()) {
					
					TestStatus testStatus;
					Class<?> proxyClass;
					final Source source = c.getAnnotation(Source.class);
					if (source != null) {
						testStatus = source.status();
						if (source.proxy() != Object.class) {
							proxyClass = source.proxy();
						} else {
							proxyClass = null;
						}
					} else {
						testStatus = TestStatus.REQUIRED;
						proxyClass = null;
					}

					final SourceClassRec sourceClassRec = new SourceClassRec(c, testStatus, proxyClass);
					sourceClassRecs.put(sourceClassRec.getSourceClass(), sourceClassRec);

					final Method[] methods = c.getMethods();
					for (final Method method : methods) {
						boolean addRec = method.getDeclaringClass().equals(c);
						addRec &= !method.isBridge();
						if (addRec) {
							TestStatus methodTestStatus = testStatus;
							SourceMethod sourceMethod = method.getAnnotation(SourceMethod.class);
							if (sourceMethod != null) {
								methodTestStatus = sourceMethod.status();
							}
							final SourceMethodRec sourceMethodRec = new SourceMethodRec(method, methodTestStatus, proxyClass != null);
							sourceMethodRecs.put(sourceMethodRec.getMethod(), sourceMethodRec);
						}
					}
//					Constructor<?>[] constructors = c.getConstructors();
//					for(final Constructor<?> constructor : constructors) {
//						boolean addRec = true;
//						addRec &= !constructor.getDeclaringClass().equals(Object.class);
//						addRec &= !constructor.getDeclaringClass().equals(Enum.class);
//						addRec &= !constructor.getDeclaringClass().equals(Throwable.class);
//						if (addRec) {
//							
//						}
//					}
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final static class SourceMethodRec {

		private final Method method;

		private final boolean isProxied;

		private final TestStatus testStatus;

		public SourceMethodRec(final Method method, TestStatus testStatus, boolean isProxied) {
			this.method = method;
			this.testStatus = testStatus;
			this.isProxied = isProxied;
		}

		public Method getMethod() {
			return method;
		}

		public TestStatus getTestStatus() {
			return testStatus;
		}

		public boolean isProxied() {
			return isProxied;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("SourceMethodRec [method=");
			builder.append(method);
			builder.append(", isProxied=");
			builder.append(isProxied);
			builder.append(", testStatus=");
			builder.append(testStatus);
			builder.append("]");
			return builder.toString();
		}
		

	}

	private final static class TestClassRec {

		private final Class<?> testClass;

		private final Class<?> sourceClass;

		public TestClassRec(final Class<?> testClass) {
			final UnitTest unitTest = testClass.getAnnotation(UnitTest.class);
			this.testClass = testClass;
			sourceClass = unitTest.target();
		}

		public Class<?> getSourceClass() {
			return sourceClass;
		}

		public Class<?> getTestClass() {
			return testClass;
		}

	}

	private final class TestFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			/*
			 * For a file to be a test file, it must be 1) a java file and 2) be
			 * annotated with a UnitTest annotation.
			 * 
			 * The UnitTest annotation must have a non-null source class
			 * reference. The validity of the source class reference is examined
			 * in the downstream process after all TestClassRecs have been
			 * loaded.
			 * 
			 * 
			 */

			if (isJavaFile(file)) {
				final Class<?> c = getClassFromFile(testPath, file);
				final UnitTest unitTest = c.getAnnotation(UnitTest.class);
				if (unitTest != null) {
					if (unitTest.target() == null) {
						addWarning(WarningType.UNIT_TEST_ANNOTATION_LACKS_SOURCE_CLASS, c.getCanonicalName());
					} else {
						final TestClassRec testClassRec = new TestClassRec(c);
						testClassRecs.put(testClassRec.getTestClass(), testClassRec);
						final Method[] methods = c.getMethods();
						for (final Method testMethod : methods) {
							final Test test = testMethod.getAnnotation(Test.class);
							final UnitTestMethod unitTestMethod = testMethod.getAnnotation(UnitTestMethod.class);
							if ((test != null) && (unitTestMethod != null)) {
								Method sourceMethod;
								try {
									sourceMethod = unitTest.target().getMethod(unitTestMethod.name(), unitTestMethod.args());
								} catch (NoSuchMethodException | SecurityException e) {
									sourceMethod = null;
								}
								if (sourceMethod != null) {
									final TestMethodRec testMethodRec = new TestMethodRec(testMethod, sourceMethod);
									testMethodRecs.put(testMethodRec.getSourceMethod(), testMethodRec);
								} else {
									addWarning(WarningType.SOURCE_METHOD_CANNOT_BE_RESOLVED, testMethod);
								}
							} else if ((test != null) && (unitTestMethod == null)) {
								addWarning(WarningType.TEST_ANNOTATION_WITHOUT_SOURCE_METHOD_ANNOTATION, testMethod);
							} else if ((test == null) && (unitTestMethod != null)) {
								addWarning(WarningType.SOURCE_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION, testMethod);
							}
						}
					}
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final static class TestMethodRec {

		private final Method testMethod;

		private final Method sourceMethod;

		public TestMethodRec(final Method testMethod, Method sourceMethod) {
			this.testMethod = testMethod;
			this.sourceMethod = sourceMethod;
		}

		public Method getSourceMethod() {
			return sourceMethod;
		}

		public Method getTestMethod() {
			return testMethod;
		}
	}

	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);

		final TestPlanScript2 testPlanScript = new TestPlanScript2(sourcePath, testPath);
		testPlanScript.execute();
	}

	private final Path sourcePath;

	private final Path testPath;

	private Map<Class<?>, SourceClassRec> sourceClassRecs = new TreeMap<>((c1, c2) -> c1.getCanonicalName().compareTo(c2.getCanonicalName()));

	private Map<Method, SourceMethodRec> sourceMethodRecs = new TreeMap<>((m1, m2) -> {
		int result = m1.getDeclaringClass().getCanonicalName().compareTo(m2.getDeclaringClass().getCanonicalName());
		if (result == 0) {
			result = m1.getName().compareTo(m2.getName());
			if (result == 0) {
				result = Integer.compare(m1.getParameterCount(), m2.getParameterCount());
			}
		}
		return result;
	});

	
	private Map<Class<?>, TestClassRec> testClassRecs = new TreeMap<>((c1, c2) -> c1.getCanonicalName().compareTo(c2.getCanonicalName()));

	private Map<Method, TestMethodRec> testMethodRecs = new TreeMap<>((m1, m2) -> {
		int result = m1.getDeclaringClass().getCanonicalName().compareTo(m2.getDeclaringClass().getCanonicalName());
		if (result == 0) {
			result = m1.getName().compareTo(m2.getName());
			if (result == 0) {
				result = Integer.compare(m1.getParameterCount(), m2.getParameterCount());
			}
		}
		return result;
	});

	private TestPlanScript2(final Path sourcePath, final Path testPath) {
		for (WarningType warningType : WarningType.values()) {
			warningMap.put(warningType, new ArrayList<String>());
		}
		this.sourcePath = sourcePath;
		this.testPath = testPath;
	}

	private void reportWarnings() {
		for (WarningType warningType : WarningType.values()) {
			List<String> warnings = warningMap.get(warningType);
			if (!warnings.isEmpty()) {
				System.out.println("("+warnings.size()+")"+warningType.description);
				for (int i = 0;i< FastMath.min(warnings.size(),10);i++) {
					String warning = warnings.get(i);
					System.out.println("\t" + warning);
				}
				System.out.println();
			}
		}
	}

	private void validateTestClassRecs() {
		// Show that every test class links to a source class
		for (TestClassRec testClassRec : testClassRecs.values()) {
			if (!sourceClassRecs.containsKey(testClassRec.getSourceClass())) {
				addWarning(WarningType.TEST_CLASS_LINKED_OUTSIDE_SOURCE, testClassRec.getTestClass().getCanonicalName());
			}
		}

		// Show that the test classes are in one to one correspondence with the
		// contents of the suite test file

		SuiteClasses suiteClasses = SuiteTest.class.getAnnotation(SuiteClasses.class);

		Set<Class<?>> automatedTestClasses = new LinkedHashSet<>();
		for (TestClassRec testClassRec : testClassRecs.values()) {
			automatedTestClasses.add(testClassRec.getTestClass());
		}

		Class<?>[] value = suiteClasses.value();
		Set<Class<?>> coveredClasses = new LinkedHashSet<>();
		for (Class<?> c : value) {
			coveredClasses.add(c);
		}

		for (Class<?> c : automatedTestClasses) {
			if (!coveredClasses.contains(c)) {
				addWarning(WarningType.SUITE_CLASS_MISSING_TEST_CLASS, c.getCanonicalName());
			}
		}

		for (Class<?> c : coveredClasses) {
			if (!automatedTestClasses.contains(c)) {
				addWarning(WarningType.SUITE_CLASS_CONTAINS_NON_TEST_CLASS, c.getCanonicalName());
			}
		}

	}

	private void validateSourceClassRecs() {
		// show that every proxied source class rec leads to through to other
		// source class recs, terminating in a non-proxied source class rec with
		// each succeeding parent record having a non-decreasing status

		for (SourceClassRec sourceClassRec : sourceClassRecs.values()) {
			TestStatus testStatus = sourceClassRec.getTestStatus();
			SourceClassRec s = sourceClassRec;
			Set<SourceClassRec> visitedSourceClassRecs = new LinkedHashSet<>();
			while (true) {
				if (s == null) {
					addWarning(WarningType.PROXY_LEADS_OUTSIDE_SOURCE_FOLDER, sourceClassRec.getSourceClass().getCanonicalName());
					break;
				}
				TestStatus nextTestStatus = s.getTestStatus();
				if (nextTestStatus.compareTo(testStatus) > 0) {
					addWarning(WarningType.PROXY_HAS_LOWER_TEST_STATUS, sourceClassRec.getSourceClass().getCanonicalName());
					break;
				}
				testStatus = nextTestStatus;
				if (!visitedSourceClassRecs.add(s)) {
					addWarning(WarningType.CIRCULAR_PROXIES, sourceClassRec.getSourceClass().getCanonicalName());
					break;
				}
				if (s.getProxyClass() == null) {
					// we have terminated in a non-proxy class
					break;
				}
				s = sourceClassRecs.get(s.getProxyClass());
			}
		}

	}

	private void validateTestMethodRecs() {
		// show that each test method links to a source method that is required
		// and non-proxied
		for (TestMethodRec testMethodRec : testMethodRecs.values()) {
			SourceMethodRec sourceMethodRec = sourceMethodRecs.get(testMethodRec.getSourceMethod());
			if (sourceMethodRec == null) {
				addWarning(WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD, testMethodRec.getTestMethod());
			} else {
				if (sourceMethodRec.isProxied()) {
					addWarning(WarningType.TEST_METHOD_LINKED_TO_PROXIED_SOURCE, testMethodRec.getTestMethod());
				} else {
					if (sourceMethodRec.getTestStatus() != TestStatus.REQUIRED) {
						addWarning(WarningType.TEST_METHOD_TESTS_SOURCE_METHOD_THAT_DOES_NOT_REQUIRE_A_TEST, testMethodRec.getTestMethod());
					}
				}
			}
		}
	}

	private void validateSourceMethodRecs() {
		for (SourceMethodRec sourceMethodRec : sourceMethodRecs.values()) {
			if (!sourceMethodRec.isProxied() && sourceMethodRec.testStatus == TestStatus.REQUIRED) {
				TestMethodRec testMethodRec = testMethodRecs.get(sourceMethodRec.getMethod());
				if (testMethodRec == null) {
					addWarning(WarningType.SOURCE_METHOD_REQUIRES_TEST, sourceMethodRec.getMethod());
				}
			}
		}
	}

	private void loadSourceClassRecs() {
		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		try {
			Files.walkFileTree(sourcePath, sourceFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadTestClassRecs() {
		final TestFileVisitor testFileVisitor = new TestFileVisitor();
		try {
			Files.walkFileTree(testPath, testFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void execute() {

		loadSourceClassRecs();

		loadTestClassRecs();

		validateSourceClassRecs();

		validateTestClassRecs();

		validateTestMethodRecs();

		validateSourceMethodRecs();

		reportWarnings();

	}

}
