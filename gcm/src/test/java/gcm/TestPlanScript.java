package gcm;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.runners.Suite.SuiteClasses;

import gcm.script.SourceClassRec;
import gcm.script.SourceFileInvestigator;
import gcm.script.SourceMethodRec;
import gcm.script.TestClassRec;
import gcm.script.TestFileInvestigator;
import gcm.script.TestMethodRec;
import gcm.util.annotations.TestStatus;

/**
 * A script covering the details of the GCM Test Plan. It produces a console
 * report that measures the completeness/status of the test classes. It does not
 * measure the correctness of any test, but rather shows which tests exist and
 * their status.
 * 
 * @author Shawn Hatch
 *
 */
public class TestPlanScript {

	private static class InfoReport {
		private String title;
		private List<String> infos = new ArrayList<>();

		private InfoReport(String title) {
			this.title = title;
		}

		public void print() {
			System.out.println();

			System.out.println("Infomation: " + title);
			for (String info : infos) {
				String[] strings = info.split("\n");
				boolean first = true;
				for (String string : strings) {
					if (first) {
						first = false;
						System.out.println("\t" + string);
					} else {
						System.out.println("\t" + "\t" + string);
					}
				}
			}
		}

		public void addInfo(String info) {
			infos.add(info);
		}
	}

	private static class TestReport {
		private String question;
		private List<String> warnings = new ArrayList<>();
		private String affirmation;

		private TestReport(String question, String affirmation) {
			this.question = question;
			this.affirmation = affirmation;
		}

		public void print() {
			System.out.println();
			if (warnings.size() == 0) {
				System.out.println("Affirmation: " + affirmation);
			} else {
				System.out.println("Question: " + question + " -> " + warnings.size() + " warning(s)");
				for (String warning : warnings) {
					String[] strings = warning.split("\n");
					boolean first = true;
					for (String string : strings) {
						if (first) {
							first = false;
							System.out.println("\t" + "WARNING: " + string);
						} else {
							System.out.println("\t" + "\t" + string);
						}
					}

				}
			}
		}

		public void addWarning(String warning) {
			warnings.add(warning);
		}
	}

	private final Path sourcePath;

	private final Path testPath;

	private TestPlanScript(Path sourcePath, Path testPath) {
		this.sourcePath = sourcePath;
		this.testPath = testPath;
	}

	public static void main(String[] args) {
		
		// Should point to src/main/java
		Path sourcePath = Paths.get(args[0]);
		
		
		// Should point to src/test/java
		Path testPath = Paths.get(args[1]);

		TestPlanScript testPlanScript = new TestPlanScript(sourcePath, testPath);
		testPlanScript.execute();
	}

	private List<SourceClassRec> sourceClassRecs;
	private List<TestClassRec> testClassRecs;

	private void loadSourceClassRecs() {
		sourceClassRecs = SourceFileInvestigator.getSourceClassRecs(sourcePath);
		sourceClassRecs.sort((a, b) -> a.getSourceClass().getSimpleName().compareTo(b.getSourceClass().getSimpleName()));
	}

	private void loadTestClassRecs() {
		testClassRecs = TestFileInvestigator.getTestClassRecs(testPath);
	}

	private void linkSourceAndTestClasses() {
		Map<Class<?>, List<TestClassRec>> testClassMap = new LinkedHashMap<>();
		for (TestClassRec testClassRec : testClassRecs) {
			Class<?> sourceClass = testClassRec.getSourceClass();
			List<TestClassRec> list = testClassMap.get(sourceClass);
			if (list == null) {
				list = new ArrayList<>();
				testClassMap.put(sourceClass, list);
			}
			list.add(testClassRec);
		}

		Map<Class<?>, SourceClassRec> sourceClassMap = new LinkedHashMap<>();
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			sourceClassMap.put(sourceClassRec.getSourceClass(), sourceClassRec);
		}

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			List<TestClassRec> list = testClassMap.get(sourceClassRec.getSourceClass());
			if (list != null) {
				for (TestClassRec testClassRec : list) {
					sourceClassRec.addTestClassRec(testClassRec);
				}
			}
		}

		for (TestClassRec testClassRec : testClassRecs) {
			SourceClassRec sourceClassRec = sourceClassMap.get(testClassRec.getSourceClass());
			testClassRec.setSourceClassRec(sourceClassRec);
		}

	}

	private void demonstrateTestSuiteCompleteness() {
		TestReport testReport = new TestReport("Does the SuiteTest have all of the automated tests and only the automated tests?", "The SuiteTest matches the automated test classes");
		SuiteClasses suiteClasses = SuiteTest.class.getAnnotation(SuiteClasses.class);

		Set<Class<?>> automatedTestClasses = new LinkedHashSet<>();
		for (TestClassRec testClassRec : testClassRecs) {
			if (testClassRec.isAutomated()) {
				automatedTestClasses.add(testClassRec.getTestClass());
			}
		}

		Class<?>[] value = suiteClasses.value();
		Set<Class<?>> coveredClasses = new LinkedHashSet<>();
		for (Class<?> c : value) {
			coveredClasses.add(c);
		}

		for (Class<?> c : automatedTestClasses) {
			if (!coveredClasses.contains(c)) {
				testReport.addWarning("Automated test class " + c.getSimpleName() + " is not listed in the Suite Test");
			}
		}

		for (Class<?> c : coveredClasses) {
			if (!automatedTestClasses.contains(c)) {
				testReport.addWarning("Suite Test contains class " + c.getSimpleName() + " but that is not an automated test");
			}
		}
		testReport.print();
	}

	private void demonstrateSourceClassesHaveAtLeastOneTest() {
		TestReport testReport = new TestReport("Does each source class that requires a test class have one?", "All source class that require a test class have one");

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			if (sourceClassRec.getTestStatus() == TestStatus.REQUIRED) {
				if (sourceClassRec.getTestClassRecs().size() == 0) {
					testReport.addWarning(sourceClassRec.getSourceClass().getSimpleName() + " has no test classes");
				}
			}
		}

		testReport.print();
	}

	private void demonstrateSourceClassesHaveNoMoreThanOneTestClass() {
		TestReport testReport = new TestReport("Does any source class have a mix of automated and manual test classes?", "All source classes only automated or manual test class");
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			int automatedTestClassCount = 0;
			for (TestClassRec testClassRec : sourceClassRec.getTestClassRecs()) {
				if (testClassRec.isAutomated()) {
					automatedTestClassCount++;
				}
			}
			int manualTestClassCount = sourceClassRec.getTestClassRecs().size() - automatedTestClassCount;

			if (automatedTestClassCount > 0 && manualTestClassCount > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append(sourceClassRec.getSourceClass().toGenericString());
				sb.append(" has a mix of automated and manual tests");
				sb.append("\n");
				for (TestClassRec testClassRec : sourceClassRec.getTestClassRecs()) {
					sb.append(testClassRec.getTestClass().toGenericString());
					sb.append("\n");
				}
				testReport.addWarning(sb.toString());
			}

		}
		testReport.print();
	}

	private void demonstrateThatSourceClassesThatDontNeedTestsDontHaveTests() {
		TestReport testReport = new TestReport("Does any source class that should not have a test class have one?", "No source class that should not have a test class has one");
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			if (sourceClassRec.getTestStatus() != TestStatus.REQUIRED) {
				if (sourceClassRec.getTestClassRecs().size() > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(sourceClassRec.getSourceClass().toGenericString());
					sb.append(" has test(s) although the status for this class is ");
					sb.append(sourceClassRec.getTestStatus());
					sb.append("\n");
					for (TestClassRec testClassRec : sourceClassRec.getTestClassRecs()) {
						sb.append(testClassRec.getTestClass().toGenericString());
						sb.append("\n");
					}
					testReport.addWarning(sb.toString());
				}
			}
		}
		testReport.print();
	}

	private void demonstrateProxiedSourceClassesHaveAProxyStatus() {
		TestReport testReport = new TestReport("Does each source class that has a non-default proxy class have a PROXY status",
				"All source classes that have a non-default proxy class have their status as PROXY");
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			if (sourceClassRec.getProxyClass() != null) {
				if (sourceClassRec.getTestStatus() != TestStatus.PROXY) {
					testReport.addWarning(sourceClassRec.getSourceClass().toGenericString() + " has a non-default proxy class but is marked with status = " + sourceClassRec.getTestStatus());
				}
			}
		}
		testReport.print();
	}

	private void demonstrateEachProxiedSourceClassHasALegitimateProxyClass() {
		TestReport testReport = new TestReport("Does each proxied source class have a proxy class that corresponds to another known source class",
				"All proxied source classes have proxy classes that correspond to known source classes");

		Map<Class<?>, SourceClassRec> map = new LinkedHashMap<>();

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			map.put(sourceClassRec.getSourceClass(), sourceClassRec);
		}

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			if (sourceClassRec.getTestStatus() == TestStatus.PROXY) {
				if (!map.containsKey(sourceClassRec.getProxyClass())) {
					testReport.addWarning(sourceClassRec.getSourceClass().toGenericString() + " does not have a legitimate proxy class = " + sourceClassRec.getProxyClass().toGenericString());
				}
			}
		}

		testReport.print();
	}

	private void demonstrateEachProxiedSourceClassLinksToATestedSourceClass() {
		TestReport testReport = new TestReport("Does each proxied source class link to a tested source class", "All proxied source classes link to tested source classes");

		Map<Class<?>, SourceClassRec> map = new LinkedHashMap<>();

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			map.put(sourceClassRec.getSourceClass(), sourceClassRec);
		}

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			
			if (sourceClassRec.getTestStatus() == TestStatus.PROXY) {
				SourceClassRec s = sourceClassRec;
				Set<SourceClassRec> visitedSourceClassRecs = new LinkedHashSet<>();
				visitedSourceClassRecs.add(s);
				boolean circularProxies = false;
				while (s != null && s.getTestStatus() == TestStatus.PROXY && !circularProxies) {
					s = map.get(s.getProxyClass());
					if (s == null) {
						testReport.addWarning(sourceClassRec.getSourceClass().toGenericString() + " has unresolved proxy linkage ending in a non-source class");
					} else {
						circularProxies |= !visitedSourceClassRecs.add(s);
						if (circularProxies) {
							testReport.addWarning(sourceClassRec.getSourceClass().toGenericString() + " has circular proxy linkage");
						} else {
							switch (s.getTestStatus()) {
							case UNEXPECTED:
							case UNREQUIRED:
								testReport.addWarning(sourceClassRec.getSourceClass().toGenericString() + " has proxy linkage ending in a source class that does not require a test");
								break;
							default:
								// do nothing
								break;
							}
						}
					}
				}
			}
		}

		testReport.print();
	}

	private void demonstrateEachTestClassHasALegitimateSourceClass() {
		TestReport testReport = new TestReport("Does any test class not have a legitimate source class?", "All test classes have a known source class");

		for (TestClassRec testClassRec : testClassRecs) {
			if (testClassRec.getSourceClassRec() == null) {
				testReport.addWarning(testClassRec.getTestClass().toGenericString() + " does not correspond to a known source class");
			}
		}

		testReport.print();
	}

	private void informSourceClassStatus() {
		InfoReport infoReport = new InfoReport("Summary of source class status");
		Map<TestStatus, List<SourceClassRec>> map = new LinkedHashMap<>();

		for (TestStatus testStatus : TestStatus.values()) {
			map.put(testStatus, new ArrayList<>());
		}

		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			map.get(sourceClassRec.getTestStatus()).add(sourceClassRec);
		}

		for (TestStatus testStatus : TestStatus.values()) {
			List<SourceClassRec> list = map.get(testStatus);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Classes marked " + testStatus);
				sb.append("\n");
				for (SourceClassRec sourceClassRec : list) {
					sb.append(sourceClassRec.getSourceClass().toGenericString());
					sb.append("\n");
				}
				infoReport.addInfo(sb.toString());
			}
		}
		infoReport.print();
	}

	private void informOverloadedSourceMethods() {
		InfoReport infoReport = new InfoReport("Overloaded methods in the source classes");
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			for (SourceMethodRec sourceMethodRec : sourceClassRec.getSourceMethodRecs()) {
				if (sourceMethodRec.getMethodCount() > 1) {
					StringBuilder sb = new StringBuilder();
					sb.append(sourceMethodRec.getSourceClassRec().getSourceClass().getSimpleName());
					sb.append(".");
					sb.append(sourceMethodRec.getName());
					sb.append("\n");
					for (int i = 0; i < sourceMethodRec.getMethodCount(); i++) {
						Method method = sourceMethodRec.getMethod(i);
						sb.append(method.toGenericString());
						sb.append("\n");
					}
					infoReport.addInfo(sb.toString());
				}
			}

		}
		infoReport.print();
	}

	private void testSourceMethodCoverage(SourceClassRec sourceClassRec, TestReport testReport) {
		for (SourceMethodRec sourceMethodRec : sourceClassRec.getSourceMethodRecs()) {			
			String sourceMethodName = sourceMethodRec.getName();			
			String testMethodName = "test" + sourceMethodName.substring(0, 1).toUpperCase();
			testMethodName += sourceMethodName.substring(1, sourceMethodName.length());
			
			boolean testMethodFound = false;
			for (TestClassRec testClassRec : sourceClassRec.getTestClassRecs()) {
				TestMethodRec testMethodRec = testClassRec.getTestMethodRec(testMethodName);
				if (testMethodRec != null) {
					testMethodFound = true;
					break;
				}
			}

			if (!testMethodFound) {
				StringBuilder sb = new StringBuilder();
				sb.append(sourceMethodRec.getSourceClassRec().getSourceClass().getSimpleName());
				sb.append(".");
				sb.append(sourceMethodName);
				if (sourceClassRec.getTestClassRecs().size() > 0) {
					sb.append(" does not have a test method under ");
					boolean first = true;
					for (TestClassRec testClassRec : sourceClassRec.getTestClassRecs()) {
						if (first) {
							first = false;
						} else {
							sb.append(", ");
						}
						sb.append(testClassRec.getTestClass().getSimpleName());
					}
				} else {
					sb.append(" does not have a test method");
				}
				testReport.addWarning(sb.toString());
			}
		}
	}

	private void demonstrateEachSourceMethodHasATestMethod() {
		TestReport testReport = new TestReport("Does each source method of a source class that requires a test and has at least one test class have a test method?", "All source methods that are associated with at least one test class that require a test have test methods");
		for (SourceClassRec sourceClassRec : sourceClassRecs) {
			if (sourceClassRec.getTestStatus() == TestStatus.REQUIRED) {
				//if there are no test classes, then leave that to another test
				if(sourceClassRec.getTestClassRecs().size()>0) {				
					testSourceMethodCoverage(sourceClassRec, testReport);	
				}				
			}
		}
		testReport.print();
	}

	private void execute() {

		loadSourceClassRecs();

		loadTestClassRecs();

		linkSourceAndTestClasses();

		informSourceClassStatus();

		informOverloadedSourceMethods();

		demonstrateSourceClassesHaveAtLeastOneTest();

		demonstrateSourceClassesHaveNoMoreThanOneTestClass();

		demonstrateThatSourceClassesThatDontNeedTestsDontHaveTests();

		demonstrateProxiedSourceClassesHaveAProxyStatus();

		demonstrateEachProxiedSourceClassHasALegitimateProxyClass();

		demonstrateEachProxiedSourceClassLinksToATestedSourceClass();

		demonstrateEachTestClassHasALegitimateSourceClass();

		/*
		 * Does each method of a tested source class have a corresponding test
		 * method in each of its test classes?
		 */
		demonstrateEachSourceMethodHasATestMethod();

		/*
		 * Does each method of a test class test a source class method?
		 */

		demonstrateTestSuiteCompleteness();

	}
}
