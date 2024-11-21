package awesome.console;

import static awesome.console.IntegrationTest.JAVA_HOME;
import static awesome.console.IntegrationTest.TEST_DIR_UNIX;
import static awesome.console.IntegrationTest.TEST_DIR_WINDOWS;
import static awesome.console.IntegrationTest.TEST_DIR_WINDOWS2;
import static awesome.console.IntegrationTest.getFileProtocols;
import static awesome.console.IntegrationTest.getJarFileProtocols;
import static awesome.console.IntegrationTest.parseTemplate;

import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;


public class AwesomeLinkFilterTest extends BasePlatformTestCase {

	private AwesomeLinkFilter filter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		filter = new AwesomeLinkFilter(getProject());
	}

	@Test
	public void testFileWithoutDirectory() {
		assertPathDetection("Just a file: test.txt", "test.txt");
	}

	@Test
	public void testFileContainingSpecialCharsWithoutDirectory() {
		assertPathDetection("Another file: _test.txt", "_test.txt");
		assertPathDetection("Another file: test-me.txt", "test-me.txt");
	}

	@Test
	public void testSimpleFileWithLineNumberAndColumn() {
		assertPathDetection("With line: file1.java:5:5", "file1.java:5:5", 5, 5);
	}

	@Test
	public void testFileInHomeDirectory() {
		final String[] files = new String[]{"~", "~/.gradle", "~\\.gradle"};
		String desc = "Just a file in user's home directory: ";

		for (final String file : files) {
			assertSimplePathDetection(desc, file);
		}

		desc = "should not be highlighted: ";
		// `~~~~` is recognized as a path but actually checks if the file exists
		assertSimplePathDetection(desc, "~~~~");
		assertUrlNoMatches(desc, "~~~~");
	}

	@Test
	public void testFileContainingDotsWithoutDirectory() {
		assertPathDetection("Just a file: t.es.t.txt", "t.es.t.txt");
	}

	@Test
	public void testFileInRelativeDirectoryUnixStyle() {
		assertPathDetection("File in a dir (unix style): subdir/test.txt pewpew", "subdir/test.txt");
	}

	@Test
	public void testFileInRelativeDirectoryWindowsStyle() {
		assertPathDetection("File in a dir (Windows style): subdir\\test.txt pewpew", "subdir\\test.txt");
	}

	@Test
	public void testFileInAbsoluteDirectoryWindowsStyleWithDriveLetter() {
		assertPathDetection("File in a absolute dir (Windows style): D:\\subdir\\test.txt pewpew", "D:\\subdir\\test.txt");
	}

	@Test
	public void testFileInAbsoluteDirectoryMixedStyleWithDriveLetter() {
		assertPathDetection("Mixed slashes: D:\\test\\me/test.txt - happens stometimes", "D:\\test\\me/test.txt");
	}

	@Test
	public void testFileInRelativeDirectoryWithLineNumber() {
		assertPathDetection("With line: src/test.js:55", "src/test.js:55", 55);
	}

	@Test
	public void testFileInRelativeDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbers() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: src\\api\\service.ts(29,50)", "src\\api\\service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileInAbsoluteDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbers() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: D:\\src\\api\\service.ts(29,50)", "D:\\src\\api\\service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileInAbsoluteDirectoryWithWindowsTypeScriptStyleLineAndColumnNumbersAndMixedSlashes() {
		// Windows, exception from TypeScript compiler
		assertPathDetection("From stack trace: D:\\src\\api/service.ts(29,50)", "D:\\src\\api/service.ts(29,50)", 29, 50);
	}

	@Test
	public void testFileWithJavaExtensionInAbsoluteDirectoryAndLineNumbersWindowsStyle() {
		assertPathDetection("Windows: d:\\my\\file.java:150", "d:\\my\\file.java:150", 150);
	}


	@Test
	public void testFileWithJavaExtensionInAbsoluteDirectoryWithLineAndColumnNumbersInMaven() {
		assertPathDetection("/home/me/project/run.java:[245,15]", "/home/me/project/run.java:[245,15]", 245, 15);
	}

	@Test
	public void testFileWithJavaScriptExtensionInAbsoluteDirectoryWithLineNumbers() {
		// JS exception
		assertPathDetection("bla-bla /home/me/project/run.js:27 something", "/home/me/project/run.js:27", 27);
	}

	@Test
	public void testFileWithJavaStyleExceptionClassAndLineNumbers() {
		// Java exception stack trace
		assertPathDetection("bla-bla at (AwesomeLinkFilter.java:150) something", "AwesomeLinkFilter.java:150", 150);
	}

	@Test
	public void testFileWithRelativeDirectoryPythonExtensionAndLineNumberPlusColumn() {
		assertPathDetection("bla-bla at ./foobar/AwesomeConsole.py:1337:42 something", "./foobar/AwesomeConsole.py:1337:42", 1337, 42);
	}

	@Test
	public void testFileWithoutExtensionInRelativeDirectory() {
		// detect files without extension
		assertPathDetection("No extension: bin/script pewpew", "bin/script");
		assertPathDetection("No extension: testfile", "testfile");
	}

	@Test
	public void test_unicode_path_filename() {
		assertPathDetection("unicode 中.txt yay", "中.txt");
	}

	@Test
	public void testURLHTTP() {
		assertURLDetection("omfg something: http://xkcd.com/ yay", "http://xkcd.com/");
	}

	@Test
	public void testURLHTTPWithIP() {
		assertURLDetection("omfg something: http://8.8.8.8/ yay", "http://8.8.8.8/");
	}

	@Test
	public void testURLHTTPS() {
		assertURLDetection("omfg something: https://xkcd.com/ yay", "https://xkcd.com/");
	}

	@Test
	public void testURLHTTPWithoutPath() {
		assertURLDetection("omfg something: http://xkcd.com yay", "http://xkcd.com");
	}

	@Test
	public void testURLFTPWithPort() {
		assertURLDetection("omfg something: ftp://8.8.8.8:2424 yay", "ftp://8.8.8.8:2424");
	}

	@Test
	public void testURLGIT() {
		assertURLDetection("omfg something: git://8.8.8.8:2424 yay", "git://8.8.8.8:2424");
	}

	@Test
	public void testURLFILEWithoutSchemeUnixStyle() {
		assertPathDetection("omfg something: /root/something yay", "/root/something");
	}

	@Test
	public void testURLFILEWithoutSchemeWindowsStyle() {
		assertPathDetection("omfg something: C:\\root\\something.java yay", "C:\\root\\something.java");
		assertURLDetection("omfg something: C:\\root\\something.java yay", "C:\\root\\something.java");
	}

	@Test
	public void testURLFILEWithoutSchemeWindowsStyleWithMixedSlashes() {
		assertPathDetection("omfg something: C:\\root/something.java yay", "C:\\root/something.java");
		assertURLDetection("omfg something: C:\\root/something.java yay", "C:\\root/something.java");
	}

	@Test
	public void testURLFILE() {
		assertURLDetection("omfg something: file:///home/root yay", "file:///home/root");
		assertFilePathDetection("omfg something: {file:}/home/root yay", "{file:}/home/root");
		assertFilePathDetection("omfg something: {file:}C:/Windows/Temp yay", "{file:}C:/Windows/Temp");
		assertPathDetection(
				"WARNING: Illegal reflective access by com.intellij.util.ReflectionUtil (file:/H:/maven/com/jetbrains/intellij/idea/ideaIC/2021.2.1/ideaIC-2021.2.1/lib/util.jar) to field java.io.DeleteOnExitHook.files",
				"file:/H:/maven/com/jetbrains/intellij/idea/ideaIC/2021.2.1/ideaIC-2021.2.1/lib/util.jar"
		);
		assertPathDetection(
				"WARNING: Illegal reflective access by com.intellij.util.ReflectionUtil (file:/src/test/resources/file1.java) to field java.io.DeleteOnExitHook.files",
				"file:/src/test/resources/file1.java"
		);
	}

	@Test
	public void testURLFTPWithUsernameAndPath() {
		assertURLDetection("omfg something: ftp://user:password@xkcd.com:1337/some/path yay", "ftp://user:password@xkcd.com:1337/some/path");
	}

	@Test
	public void testURLInsideBrackets() {
		assertPathDetection("something (C:\\root\\something.java) blabla", "C:\\root\\something.java");
		assertURLDetection("something (C:\\root\\something.java) blabla", "C:\\root\\something.java");
	}

	@Test
	public void testWindowsDirectoryBackwardSlashes() {
		assertPathDetection("C:/Windows/Temp/test.tsx:5:3", "C:/Windows/Temp/test.tsx:5:3", 5, 3);
	}

	@Test
	public void testOverlyLongRowAndColumnNumbers() {
		assertPathDetection("test.tsx:123123123123123:12312312312312321", "test.tsx:123123123123123:12312312312312321", 0, 0);
	}

	@Test
	public void testTSCErrorMessages() {
		assertPathDetection("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:", "C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6", 1930, 6);
		assertURLDetection("C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:", "C:/project/node_modules/typescript/lib/lib.webworker.d.ts:1930:6:");
	}

	@Test
	public void testPythonTracebackWithQuotes() {
		assertPathDetection("File \"/Applications/plugins/python-ce/helpers/pycharm/teamcity/diff_tools.py\", line 38", "\"/Applications/plugins/python-ce/helpers/pycharm/teamcity/diff_tools.py\", line 38", 38);
	}

	@Test
	public void testAngularJSAtModule() {
		assertPathDetection("src/app/@app/app.module.ts:42:5", "src/app/@app/app.module.ts:42:5", 42, 5);
	}

	@Test
	public void testCsharpStacktrace() {
		assertPathDetection(
				"at Program.<Main>$(String[] args) in H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:line 4",
				"H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:line 4",
				4
		);
	}

	@Test
	public void testJavaStacktrace() {
		assertPathDetection("at Build_gradle.<init>(build.gradle.kts:9)", "build.gradle.kts:9", 9);
		assertPathDetection(
				"at awesome.console.AwesomeLinkFilterTest.testFileWithoutDirectory(AwesomeLinkFilterTest.java:14)",
				"awesome.console.AwesomeLinkFilterTest.testFileWithoutDirectory",
				"AwesomeLinkFilterTest.java:14"
		);
		assertPathDetection(
				"at redis.clients.jedis.util.Pool.getResource(Pool.java:59) ~[jedis-3.0.0.jar:?]",
				"redis.clients.jedis.util.Pool.getResource",
				"Pool.java:59"
		);
	}

	@Test
	public void testGradleStacktrace() {
		assertPathDetection("Gradle build task failed with an exception: Build file 'build.gradle' line: 14", "'build.gradle' line: 14", 14);
	}

	@Test
	public void testPathColonAtTheEnd() {
		assertPathDetection("colon at the end: resources/file1.java:5:1:", "resources/file1.java:5:1", 5, 1);
		assertSimplePathDetection("colon at the end: %s:", TEST_DIR_WINDOWS + "\\file1.java:5:4", 5, 4);
	}

	@Test
	public void testLineNumberAndColumnWithVariableWhitespace() {
		assertPathDetection("With line: file1.java: 5  :   5 ", "file1.java: 5  :   5", 5, 5);
		assertPathDetection("With line: src/test.js:  55   ", "src/test.js:  55", 55);
		assertPathDetection("From stack trace: src\\api\\service.ts( 29  ,   50 )  ", "src\\api\\service.ts( 29  ,   50 )", 29, 50);
		assertPathDetection("/home/me/project/run.java:[ 245  ,   15  ] ", "/home/me/project/run.java:[ 245  ,   15  ]", 245, 15);
		assertPathDetection("bla-bla at (AwesomeLinkFilter.java:  150) something", "AwesomeLinkFilter.java:  150", 150);
		assertPathDetection(
				"at Program.<Main>$(String[] args) in H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:    line    4    ",
				"H:\\test\\ConsoleApp\\ConsoleApp\\Program.cs:    line    4",
				4
		);
	}

	@Test
	public void testIllegalLineNumberAndColumn() {
		assertPathDetection("Vue2 build: static/css/app.b8050232.css (259 KiB)", "static/css/app.b8050232.css");
	}

	@Test
	public void testPathWithDots() {
		assertPathDetection("Path: . ", ".");
		assertPathDetection("Path: .. ", "..");
		assertPathDetection("Path: ./intellij-awesome-console/src ", "./intellij-awesome-console/src");
		assertPathDetection("Path: ../intellij-awesome-console/src ", "../intellij-awesome-console/src");
		assertPathDetection("Path: .../intellij-awesome-console/src ", ".../intellij-awesome-console/src");
		assertPathDetection("File: .gitignore ", ".gitignore");
		assertPathDetection("File ./src/test/resources/subdir/./file1.java", "./src/test/resources/subdir/./file1.java");
		assertPathDetection("File ./src/test/resources/subdir/../file1.java", "./src/test/resources/subdir/../file1.java");
	}

	@Test
	public void testUncPath() {
		assertPathDetection("UNC path: \\\\localhost\\c$", "\\\\localhost\\c$");
		assertPathDetection("UNC path: \\\\server\\share\\folder\\myfile.txt", "\\\\server\\share\\folder\\myfile.txt");
		assertPathDetection("UNC path: \\\\123.123.123.123\\share\\folder\\myfile.txt", "\\\\123.123.123.123\\share\\folder\\myfile.txt");
		assertPathDetection("UNC path: file://///localhost/c$", "file://///localhost/c$");
	}

	@Test
	public void testPathWithQuotes() {
		assertPathDetection("Path: src/test/resources/中文 空格.txt ", "空格.txt");
		assertPathDetection("Path: \"C:\\Program Files (x86)\\Windows NT\" ", "\"C:\\Program Files (x86)\\Windows NT\"");
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt\" ", "\"src/test/resources/中文 空格.txt\"");
		assertFilePathDetection("path: \"{file:}src/test/resources/中文 空格.txt\" ", "\"{file:}src/test/resources/中文 空格.txt\"");
		assertPathDetection("Path: \"  src/test/resources/中文 空格.txt  \" ", "空格.txt");
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt\":5:4 ", "\"src/test/resources/中文 空格.txt\":5:4", 5, 4);
		// TODO maybe row:col is enclosed in quotes?
		// assertPathDetection("Path: \"src/test/resources/中文 空格.txt:5:4\" ", "\"src/test/resources/中文 空格.txt:5:4\"", 5, 4);
		assertPathDetection("Path: \"src/test/resources/subdir/file1.java\" ", "\"src/test/resources/subdir/file1.java\"");
		assertPathDetection("Path: \"src/test/  resources/subdir/file1.java\" ", "src/test/", "resources/subdir/file1.java");
		assertPathDetection("Path: \"src/test/resources/subdir/file1.java \" ", "src/test/resources/subdir/file1.java");
		assertPathDetection("Path: \"src/test/resources/subdir/ file1.java\" ", "src/test/resources/subdir/", "file1.java");
		assertPathDetection("Path: \"src/test/resources/subdir /file1.java\" ", "src/test/resources/subdir", "/file1.java");
	}

	@Test
	public void testPathWithUnclosedQuotes() {
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt", "src/test/resources/中文", "空格.txt");
		assertPathDetection("Path: src/test/resources/中文 空格.txt\"", "src/test/resources/中文", "空格.txt");
		assertPathDetection("Path: \"src/test/resources/中文 空格.txt'", "src/test/resources/中文", "空格.txt");
		assertPathDetection("Path: src/test/resources/中文 空格.txt]", "src/test/resources/中文", "空格.txt");
		assertPathDetection(
				"Path: \"src/test/resources/中文 空格.txt   \"src/test/resources/中文 空格.txt\"",
				"src/test/resources/中文",
				"空格.txt",
				"\"src/test/resources/中文 空格.txt\""
		);
	}

	@Test
	public void testPathSeparatedByCommaOrSemicolon() {
		final String[] paths = new String[]{
				"%s\\file1.java,%s\\file2.java;%s\\file3.java".replace("%s", TEST_DIR_WINDOWS),
				"%s/file1.java,%s/file2.java;%s/file3.java".replace("%s", TEST_DIR_WINDOWS2),
				"%s/file1.java,%s/file2.java;%s/file3.java".replace("%s", TEST_DIR_UNIX),
				"src/test/resources/file1.java,src/test/resources/file1.py;src/test/resources/testfile"
		};
		final String desc = "Comma or semicolon separated paths: ";

		for (final String path : paths) {
			final String[] files = path.split("[,;]");
			assertPathDetection(desc + path, files);
			assertPathDetection(
					String.format(desc + "%s:20:1,%s:20:5;%s:20:10", (Object[]) files),
					files[0] + ":20:1", files[1] + ":20:5", files[2] + ":20:10"
			);
			assertFilePathDetection(desc + "{file:}" + path, "{file:}" + files[0], files[1], files[2]);
			assertFilePathDetection(
					String.format(desc + "{file:}%s,{file:}%s;{file:}%s", (Object[]) files),
					"{file:}" + files[0], "{file:}" + files[1], "{file:}" + files[2]
			);
		}
	}

	@Test
	public void testPathSurroundedBy() {
		final String[] files = new String[]{
				"file1.java",
				TEST_DIR_WINDOWS + "\\file1.java",
				TEST_DIR_WINDOWS2 + "/file1.java",
				TEST_DIR_UNIX + "/file1.java"
		};
		final String desc = "Path surrounded by: ";

		for (final String pair : new String[]{"()", "[]", "''", "\"\""}) {
			final String start = String.valueOf(pair.charAt(0));
			final String end = String.valueOf(pair.charAt(1));
			final boolean isDoubleQuote = "\"".equals(start);

			for (final String file : files) {
				final String line = start + file + end;
				assertPathDetection(desc + line, isDoubleQuote ? line : file);
			}

			assertPathDetection(desc + start + "awesome.console.IntegrationTest:2" + end, "awesome.console.IntegrationTest:2", 2);
			assertPathDetection(desc + start + "awesome.console.IntegrationTest:10:" + end, "awesome.console.IntegrationTest:10", 10);
			assertPathDetection(desc + start + "awesome.console.IntegrationTest:30", "awesome.console.IntegrationTest:30", 30);
			assertPathDetection(desc + "awesome.console.IntegrationTest:40" + end, "awesome.console.IntegrationTest:40", 40);
			assertPathDetection(
					desc + start + "awesome.console.IntegrationTest:45,awesome.console.IntegrationTest:50" + end,
					"awesome.console.IntegrationTest:45",
					"awesome.console.IntegrationTest:50"
			);

			assertURLDetection(String.format("something %sfile:///tmp%s blabla", start, end), "file:///tmp");
			final String expected = "{file:}/tmp";
			final String line = start + expected + end;
			assertFilePathDetection(desc + line, isDoubleQuote ? line : expected);
		}
	}

	@Test
	public void testTypeScriptCompiler() {
		assertPathDetection("error TS18003: No inputs were found in config file 'tsconfig.json'.", "tsconfig.json");

		assertPathDetection(
				"file1.ts:5:13 - error TS2475: 'const' enums can only be used in property or index access expressions or the right hand side of an import declaration or export assignment or type query.\n",
				"file1.ts:5:13",
				5, 13
		);
		assertPathDetection("5 console.log(Test);");
		assertUrlNoMatches("", "              ~~~~");
		assertPathDetection("\n\nFound 1 error in file1.ts:5", "file1.ts:5", 5);
	}

	@Test
	public void testPathBoundary() {
		assertPathDetection(".", ".");
		assertPathDetection("..", "..");
		assertPathDetection("Path end with a dot: file1.java.", "file1.java");
		assertPathDetection("Path end with a dot: \"file1.java\".", "\"file1.java\"");
		assertPathDetection("Path end with a dot: src/test/resources/subdir/.", "src/test/resources/subdir/.");
		assertPathDetection("Path end with a dot: src/test/resources/subdir/..", "src/test/resources/subdir/..");
		assertPathDetection("Path end with a dot: src/test/resources/subdir...", "src/test/resources/subdir");

		final String file = TEST_DIR_WINDOWS + "\\file1.java";
		assertSimplePathDetection("╭─[%s]", file + ":19:2", 19, 2);
		assertSimplePathDetection("╭─[%s]", file + ":19", 19);
		assertSimplePathDetection("╭─ %s", file + ":19:10", 19, 10);
		assertSimplePathDetection("--> [%s]", file + ":19:5", 19, 5);
		assertSimplePathDetection("--> %s", file + ":19:3", 19, 3);
	}

	@Test
	public void testIllegalChar() {
		assertPathDetection("Illegal char: \u0001file1.java", "file1.java");
		assertPathDetection("Illegal char: \u001ffile1.java", "file1.java");
		assertPathDetection("Illegal char: \u0021file1.java", "!file1.java");
		assertPathDetection("Illegal char: \u007ffile1.java", "file1.java");
	}

	@Test
	public void testWindowsDriveRoot() {
		final String desc = "Windows drive root: ";

		assertSimplePathDetection(desc, "C:\\");
		assertSimplePathDetection(desc, "C:/");
		assertSimplePathDetection(desc, "C:\\\\");
		assertSimplePathDetection(desc, "C:\\/");

		// `C:` without a slash is an invalid Windows drive
		assertPathDetection(desc + "C:", "C");
	}

	@Test
	public void testJarURL() {
		String desc = "File in JDK source: ";
		final String JdkFile = JAVA_HOME + "/lib/src.zip!/java.base/java/";

		assertSimplePathDetection(desc, JdkFile + "lang/Thread.java");

		for (final String protocol : getJarFileProtocols(JAVA_HOME)) {
			assertSimplePathDetection(desc, protocol + JdkFile + "io/File.java");
		}

		desc = "File in Jar: ";
		String file = "gradle/wrapper/gradle-wrapper.jar!/org/gradle/cli/CommandLineOption.class";
		assertSimplePathDetection(desc, file);
		assertSimplePathDetection(desc, file + ":31:26", 31, 26);

		file = "jar:file:/H:/maven/com/jetbrains/intellij/idea/ideaIC/2021.2.1/ideaIC-2021.2.1/lib/slf4j.jar!/org/slf4j/impl/StaticLoggerBinder.class";
		assertPathDetection(String.format("SLF4J: Found binding in [%s]", file), file);

		file = "jar:https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-common/1.9.23/kotlin-stdlib-common-1.9.23.jar";
		assertURLDetection("Remote Jar File: " + file, file);

		// `!/xxx` is an invalid Jar URL, but is a legal path. Check if the file exists.
		desc = "Invalid Jar URL: ";
		assertSimplePathDetection(desc, "gradle/wrapper/!/org/gradle/cli/CommandLineOption.class");
		assertSimplePathDetection(desc, "!/org/gradle/cli/CommandLineOption.class");
	}

	@Test
	public void testGit() {
		System.out.println("Git console log: ");
		assertPathDetection("warning: LF will be replaced by CRLF in README.md.", "README.md");
		assertPathDetection(
				"git update-index --cacheinfo 100644,5aaaff66f4b74af2f534be30b00020c93585f9d9,src/main/java/awesome/console/AwesomeLinkFilter.java --",
				"src/main/java/awesome/console/AwesomeLinkFilter.java"
		);
		assertURLDetection(
				"fatal: unable to access 'https://github.com/anthraxx/intellij-awesome-console.git/': schannel: failed to receive handshake, SSL/TLS connection failed",
				"https://github.com/anthraxx/intellij-awesome-console.git/"
		);
	}

	@Test
	public void testWindowsCommandLineShell() {
		// TODO support paths with spaces in the current working directory of Windows CMD and PowerShell

		System.out.println("Windows CMD console:");
		assertPathDetection("C:\\Windows\\Temp>", "C:\\Windows\\Temp");
		assertPathDetection("C:\\Windows\\Temp>echo hello", "C:\\Windows\\Temp");
		assertPathDetection("C:\\Windows\\Temp>..", "C:\\Windows\\Temp", "..");
		assertPathDetection("C:\\Windows\\Temp> ..", "C:\\Windows\\Temp", "..");
		assertPathDetection("C:\\Windows\\Temp>./build.gradle", "C:\\Windows\\Temp", "./build.gradle");
		assertPathDetection("C:\\Windows\\Temp>../intellij-awesome-console", "C:\\Windows\\Temp", "../intellij-awesome-console");
		// assertPathDetection("C:\\Program Files (x86)\\Windows NT>powershell", "C:\\Program Files (x86)\\Windows NT");

		System.out.println("Windows PowerShell console:");
		assertPathDetection("PS C:\\Windows\\Temp> ", "C:\\Windows\\Temp");
		assertPathDetection("PS C:\\Windows\\Temp> echo hello", "C:\\Windows\\Temp");
		assertPathDetection("PS C:\\Windows\\Temp> ..", "C:\\Windows\\Temp", "..");
		assertPathDetection("PS C:\\Windows\\Temp> ./build.gradle", "C:\\Windows\\Temp", "./build.gradle");
		assertPathDetection("PS C:\\Windows\\Temp> ../intellij-awesome-console", "C:\\Windows\\Temp", "../intellij-awesome-console");
		// assertPathDetection("PS C:\\Program Files (x86)\\Windows NT> echo hello", "C:\\Program Files (x86)\\Windows NT");
	}

	@Test
	public void testJavaClass() {
		assertSimplePathDetection("regular class name [%s]", "awesome.console.IntegrationTest:40", 40);
		assertSimplePathDetection("scala class name [%s]", "awesome.console.IntegrationTest$:4", 4);

		assertSimplePathDetection("class file: ", "build/classes/java/main/awesome/console/AwesomeLinkFilter.class:85:50", 85, 50);
	}

	private void assertFilePathDetection(@NotNull final String line, @NotNull final String... expected) {
		for (final String protocol : getFileProtocols(line)) {
			final String[] expected2 = Stream.of(expected).map(s -> parseTemplate(s, protocol)).toArray(String[]::new);
			assertPathDetection(parseTemplate(line, protocol), expected2);
		}
	}

	private void assertSimplePathDetection(@NotNull final String desc, @NotNull final String expected) {
		assertSimplePathDetection(desc, expected, -1, -1);
	}

	private void assertSimplePathDetection(@NotNull final String desc, @NotNull final String expected, final int expectedRow) {
		assertSimplePathDetection(desc, expected, expectedRow, -1);
	}

	private void assertSimplePathDetection(@NotNull final String desc, @NotNull final String expected, final int expectedRow, final int expectedCol) {
		final String line = desc.contains("%s") ? desc.replace("%s", expected) : desc + expected;
		assertPathDetection(line, expected, expectedRow, expectedCol);
	}

	private void assertPathNoMatches(@NotNull final String desc, @NotNull final String... lines) {
		for (final String line : lines) {
			System.out.println(desc + line);
			List<String> results = filter.detectPaths(line).stream().map(it -> it.match).collect(Collectors.toList());
			assertSameElements(results, Collections.emptyList());
		}
	}

	private void assertUrlNoMatches(@NotNull final String desc, @NotNull final String... lines) {
		for (final String line : lines) {
			System.out.println(desc + line);
			List<String> results = filter.detectURLs(line).stream().map(it -> it.match).collect(Collectors.toList());
			assertSameElements(results, Collections.emptyList());
		}
	}

	private List<FileLinkMatch> assertPathDetection(@NotNull final String line, @NotNull final String... expected) {
		System.out.println(line);

		// Test only detecting file paths - no file existence check
		List<FileLinkMatch> results = filter.detectPaths(line);

		assertFalse("No matches in line \"" + line + "\"", results.isEmpty());

		Set<String> expectedSet = Stream.of(expected).collect(Collectors.toSet());
		assertContainsElements(results.stream().map(it -> it.match).collect(Collectors.toList()), expectedSet);

		return results.stream().filter(i -> expectedSet.contains(i.match)).collect(Collectors.toList());
	}

	private void assertPathDetection(@NotNull final String line, @NotNull final String expected, final int expectedRow) {
		assertPathDetection(line, expected, expectedRow, -1);
	}

	private void assertPathDetection(@NotNull final String line, @NotNull final String expected, final int expectedRow, final int expectedCol) {
		FileLinkMatch info = assertPathDetection(line, expected).get(0);

		if (expectedRow >= 0) {
			assertEquals("Expected to capture row number", expectedRow, info.linkedRow);
		}

		if (expectedCol >= 0) {
			assertEquals("Expected to capture column number", expectedCol, info.linkedCol);
		}
	}


	private void assertURLDetection(final String line, final String expected) {
		System.out.println(line);

		// Test only detecting file paths - no file existence check
		List<URLLinkMatch> results = filter.detectURLs(line);

		assertEquals("No matches in line \"" + line + "\"", 1, results.size());
		URLLinkMatch info = results.get(0);
		assertEquals(String.format("Expected filter to detect \"%s\" link in \"%s\"", expected, line), expected, info.match);
	}
}
