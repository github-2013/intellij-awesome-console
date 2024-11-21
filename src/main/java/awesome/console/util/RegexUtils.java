package awesome.console.util;

import static awesome.console.config.AwesomeConsoleDefaults.DEFAULT_GROUP_RETRIES;

import com.intellij.openapi.util.text.StringUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author anyesu
 */
public class RegexUtils {

    /**
     * Note: The path in the {@code file:} URI has a leading slash which is added by the {@code slashify} method.
     *
     * @see java.io.File#toURI()
     * @see java.io.File#slashify(String, boolean)
     */
    @SuppressWarnings("JavadocReference")
    public static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^/?[A-Za-z]:[/\\\\]+.*");

    @NotNull
    public static String join(@NotNull final String... strings) {
        return StringUtil.join(List.of(strings), "|");
    }

    public static int[] tryGetGroupRange(final Matcher matcher, final String group) {
        return tryGetGroupRange(matcher, group, DEFAULT_GROUP_RETRIES);
    }

    public static int[] tryGetGroupRange(final Matcher matcher, final String group, final int retries) {
        int start = matcher.start(), end = matcher.end();
        for (int i = 0; i <= retries; i++) {
            String groupName = i > 0 ? group + i : group;
            try {
                start = matcher.start(groupName);
                end = matcher.end(groupName);
                break;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new int[]{start, end};
    }

    public static String tryMatchGroup(final Matcher matcher, final String group) {
        return tryMatchGroup(matcher, group, DEFAULT_GROUP_RETRIES);
    }

    public static String tryMatchGroup(final Matcher matcher, final String group, final int retries) {
        String[] groups = IntStream.range(0, retries + 1).mapToObj(i -> i > 0 ? group + i : group).toArray(String[]::new);
        return matchGroup(matcher, groups);
    }

    public static String matchGroup(final Matcher matcher, final String... groups) {
        for (String group : groups) {
            try {
                String match = matcher.group(group);
                if (null != match) {
                    return match;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static boolean isValidRegex(final String pattern) {
        try {
            if (null != pattern) {
                Pattern.compile(pattern);
                return true;
            }
        } catch (PatternSyntaxException ignored) {
        }
        return false;
    }
}
