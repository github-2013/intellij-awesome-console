package awesome.console.config;

import awesome.console.AwesomeLinkFilter;
import java.util.regex.Pattern;

/**
 * @author anyesu
 */
public interface AwesomeConsoleDefaults {

    int DEFAULT_GROUP_RETRIES = 5;

    String[] FILE_PATTERN_REQUIRED_GROUPS = "link,path,row,col".split(",");

    boolean DEFAULT_DEBUG_MODE = false;

    boolean DEFAULT_SPLIT_ON_LIMIT = false;

    boolean DEFAULT_LIMIT_LINE_LENGTH = true;

    int DEFAULT_LINE_MAX_LENGTH = 1024;

    boolean DEFAULT_SEARCH_URLS = true;

    boolean DEFAULT_SEARCH_FILES = true;

    boolean DEFAULT_SEARCH_CLASSES = true;

    boolean DEFAULT_USE_RESULT_LIMIT = true;

    int DEFAULT_RESULT_LIMIT = 100;

    int DEFAULT_MIN_RESULT_LIMIT = 1;

    boolean DEFAULT_USE_FILE_PATTERN = false;

    Pattern DEFAULT_FILE_PATTERN = AwesomeLinkFilter.FILE_PATTERN;

    String DEFAULT_FILE_PATTERN_TEXT = DEFAULT_FILE_PATTERN.pattern();

    boolean DEFAULT_USE_IGNORE_PATTERN = true;

    String DEFAULT_IGNORE_PATTERN_TEXT = "^(\"?)[.\\\\/]+\\1$|^node_modules/";

    Pattern DEFAULT_IGNORE_PATTERN = Pattern.compile(
            DEFAULT_IGNORE_PATTERN_TEXT,
            Pattern.UNICODE_CHARACTER_CLASS
    );

    boolean DEFAULT_USE_IGNORE_STYLE = false;

    boolean DEFAULT_FIX_CHOOSE_TARGET_FILE = true;

    boolean DEFAULT_USE_FILE_TYPES = true;

    String DEFAULT_FILE_TYPES = "bmp,gif,jpeg,jpg,png,webp,ttf";

    boolean DEFAULT_RESOLVE_SYMLINK = false;
}
