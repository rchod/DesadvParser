import java.io.File;

/**
 * Utility class for resolving file paths in a cross-platform way.
 */
public class PathUtils {
    
    /**
     * Gets the base directory of the project.
     * Tries multiple strategies to find the project root.
     */
    public static String getBaseDir() {
        // Strategy 1: Check if we're running from project root
        File projectRoot = new File(".");
        File ediSrc = new File(projectRoot, "edi/src");
        if (ediSrc.exists() && ediSrc.isDirectory()) {
            return projectRoot.getAbsolutePath();
        }
        
        // Strategy 2: Try parent directory (if running from edi/src)
        File parent = projectRoot.getParentFile();
        if (parent != null) {
            File parentEdiSrc = new File(parent, "edi/src");
            if (parentEdiSrc.exists() && parentEdiSrc.isDirectory()) {
                return parent.getAbsolutePath();
            }
        }
        
        // Strategy 3: Use current directory as fallback
        return projectRoot.getAbsolutePath();
    }
    
    /**
     * Resolves a path relative to the project base directory.
     */
    public static File resolvePath(String relativePath) {
        String baseDir = getBaseDir();
        return new File(baseDir, relativePath);
    }
    
    /**
     * Resolves a path in edi/src directory.
     */
    public static File resolveEdiSrcPath(String filename) {
        return resolvePath("edi/src/" + filename);
    }
}

