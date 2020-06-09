package gcm.script;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.UnitTest;

/**
 * Static utility for generating the {@link SourceClassRec} values from the
 * source path of GCM.
 * 
 * @author Shawn Hatch
 *
 */
public final class TestFileInvestigator {

	private static class TestFileVisitor extends SimpleFileVisitor<Path> {
		private final Path testPath;

		private List<TestClassRec> testClassRecs = new ArrayList<>();

		public List<TestClassRec> getTestClassRecs() {
			return new ArrayList<>(testClassRecs);
		}

		public TestFileVisitor(Path testPath) {
			this.testPath = testPath;
		}
		
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (FileClassUtil.isJavaFile(file)) {				
				Class<?> c = FileClassUtil.getClassFromFile(testPath, file);
				UnitTest unitTest = c.getAnnotation(UnitTest.class);
				if (unitTest != null) {
					TestClassRec testClassRec = new TestClassRec(c, unitTest);
					testClassRecs.add(testClassRec);
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public static List<TestClassRec> getTestClassRecs(Path testPath) {
		TestFileVisitor testFileVisitor = new TestFileVisitor(testPath);
		try {
			Files.walkFileTree(testPath, testFileVisitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return testFileVisitor.getTestClassRecs();
	}
}