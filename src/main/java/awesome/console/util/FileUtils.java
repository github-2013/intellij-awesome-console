package awesome.console.util;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author anyesu
 */
public class FileUtils {

    public static final String JAR_PROTOCOL = "jar:";

    public static final String JAR_SEPARATOR = "!/";

    public static String normalizeSlashes(@NotNull final String path) {
        return path.replace('\\', '/');
    }

    /**
     * @see java.net.JarURLConnection
     */
    public static boolean isJarPath(@NotNull final String path) {
        return path.contains(JAR_SEPARATOR);
    }

    /**
     * E.g. "jar:file:///path/to/jar.jar!/resource.xml" is converted into ["/path/to/jar.jar", "resource.xml"].
     * <p>
     * ref: https://github.com/JetBrains/intellij-community/blob/212.5080/plugins/ide-features-trainer/src/training/project/FileUtils.kt#L119-L127
     * ref: https://github.com/JetBrains/intellij-community/blob/212.5080/platform/util/src/com/intellij/util/io/URLUtil.java#L138
     *
     * @see java.net.JarURLConnection
     * @see com.intellij.util.io.URLUtil#splitJarUrl(String)
     */
    @Nullable
    public static Pair<String, String> splitJarPath(@NotNull final String path) {
        int splitIdx = path.lastIndexOf(JAR_SEPARATOR);
        if (splitIdx == -1) {
            return null;
        }
        String filePath = path.substring(0, splitIdx);
        // remove "!/"
        String pathInsideJar = path.substring(splitIdx + 2);
        return new Pair<>(filePath, pathInsideJar);
    }

    public static boolean isAbsolutePath(@NotNull final String path) {
        return isUnixAbsolutePath(path) || isWindowsAbsolutePath(path);
    }

    public static boolean isUnixAbsolutePath(@NotNull String path) {
        return path.startsWith("/") || path.startsWith("\\");
    }

    public static boolean isWindowsAbsolutePath(@NotNull final String path) {
        return RegexUtils.WINDOWS_DRIVE_PATTERN.matcher(path).matches();
    }

    public static boolean isUncPath(@NotNull String path) {
        return SystemUtils.isWindows() &&
                (path.startsWith("//") || path.startsWith("\\\\"));
    }

    /**
     * Detect a junction/reparse point
     * <p>
     *
     * @see <a href="https://stackoverflow.com/a/74801717">Cross platform way to detect a symbolic link / junction point</a>
     * @see sun.nio.fs.WindowsFileAttributes#isReparsePoint(int)
     */
    @SuppressWarnings("JavadocReference")
    public static boolean isReparsePoint(@NotNull Path path) {
        try {
            Object attribute = Files.getAttribute(path, "dos:attributes", LinkOption.NOFOLLOW_LINKS);
            if (attribute instanceof Integer) {
                // is junction or symlink
                return ((Integer) attribute & 0x400) != 0;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isReparsePointOrSymlink(@NotNull String filePath) {
        try {
            Path path = Path.of(filePath);
            return Files.isSymbolicLink(path) || isReparsePoint(path);
        } catch (InvalidPathException e) {
            return false;
        }
    }

    /**
     * Tests whether the file or directory denoted by this abstract pathname
     * exists.
     *
     * @return <code>true</code> if and only if the pathname is not a UNC path and the file or directory denoted
     * by this abstract pathname exists; <code>false</code> otherwise
     * @see java.net.JarURLConnection
     */
    public static boolean quickExists(@NotNull String path) {
        // Finding the UNC path will access the network,
        // which takes a long time and causes the UI to freeze.
        // ref: https://stackoverflow.com/a/48554407
        if (isUncPath(path)) {
            return false;
        }

        Pair<String, String> paths = splitJarPath(normalizeSlashes(path));
        if (null != paths && new File(paths.first).isFile()) {
            // is jar file path
            return true;
        }

        return isReparsePointOrSymlink(path) || new File(path).exists();
    }

    /**
     * Get VirtualFile from path. Only for "file" and "jar" protocols under Unix and Windows
     *
     * @see VfsUtil#findFileByURL(URL)
     * @see com.intellij.openapi.vfs.VirtualFileManager#findFileByUrl(String)
     * @see java.net.JarURLConnection
     */
    @Nullable
    public static VirtualFile findFileByPath(@NotNull String path) {
        path = normalizeSlashes(path);
        if (isJarPath(path)) {
            return JarFileSystem.getInstance().findFileByPath(path);
        }
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    }

    public static String resolveSymlink(@NotNull final String filePath, final boolean resolveSymlink) {
        if (resolveSymlink) {
            try {
                // to avoid DisposalException: Editor is already disposed
                // caused by `IDEA Resolve Symlinks` plugin
                return Paths.get(filePath).toRealPath().toString();
            } catch (Throwable ignored) {
            }
        }
        return filePath;
    }

    public static List<VirtualFile> resolveSymlinks(@NotNull List<VirtualFile> files, final boolean resolveSymlink) {
        if (resolveSymlink) {
            try {
                return files.parallelStream()
                            .map(it -> resolveSymlink(it.getPath(), true))
                            .distinct()
                            .map(it -> VfsUtil.findFile(Paths.get(it), false))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            } catch (Throwable ignored) {
            }
        }
        return files;
    }
}
