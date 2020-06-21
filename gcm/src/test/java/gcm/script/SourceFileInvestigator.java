package gcm.script;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import gcm.util.annotations.Source;

/**
 * Static utility for generating the {@link SourceClassRec} values from the
 * source path of GCM.
 * 
 * @author Shawn Hatch
 *
 */
public final class SourceFileInvestigator {

	private static class SourceFileVisitor extends SimpleFileVisitor<Path> {
		private final Path sourcePath;

		private List<SourceClassRec> sourceClassRecs = new ArrayList<>();

		public List<SourceClassRec> getSourceClassRecs() {
			return new ArrayList<>(sourceClassRecs);
		}

		public SourceFileVisitor(Path sourcePath) {
			this.sourcePath = sourcePath;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (FileClassUtil.isJavaFile(file)) {
				Class<?> c = FileClassUtil.getClassFromFile(sourcePath, file);
				if (!c.isAnnotation() && !c.isInterface()) {
					Source source = c.getAnnotation(Source.class);					
					SourceClassRec sourceClassRec = new SourceClassRec(c, source);
					sourceClassRecs.add(sourceClassRec);
				}
			}
			return FileVisitResult.CONTINUE;
		}

	}

	public static List<SourceClassRec> getSourceClassRecs(Path sourcePath) {
		SourceFileVisitor sourceFileVisitor = new SourceFileVisitor(sourcePath);
		try {
			Files.walkFileTree(sourcePath, sourceFileVisitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sourceFileVisitor.getSourceClassRecs();
	}
}