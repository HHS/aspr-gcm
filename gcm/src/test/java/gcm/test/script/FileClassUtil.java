package gcm.test.script;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Static utility methods for working with paths and classes
 * 
 * @author Shawn Hatch
 *
 */
public final class FileClassUtil {

	public static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static String getClassName(Path sourcePath, Path file) {
		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace("\\", ".");
	}

	/**
	 * Assumes that the source path and file are consistent
	 */
	public static Class<?> getClassFromFile(Path sourcePath, Path file) {
		try {
			String className = getClassName(sourcePath, file);
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
