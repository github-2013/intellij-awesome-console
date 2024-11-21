package awesome.console.util;

/**
 * @author anyesu
 */
public class SystemUtils {

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static boolean isWindows() {
        String osName = getOsName();
        return osName != null && osName.toLowerCase().startsWith("windows");
    }
}
