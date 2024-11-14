package awesome.console;

import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class AwesomeLinkFilterTest1 extends BasePlatformTestCase {
	@Test
	public void testFile() {
		assertPathDetection(".gitignore", ".gitignore");
	}

	private void assertPathDetection(final String line, final String expected) {
		assertPathDetection(line, expected, -1, -1);
	}

	private void assertPathDetection(final String line, final String expected, final int expectedRow) {
		assertPathDetection(line, expected, expectedRow, -1);
	}

	private void assertPathDetection(final String line, final String expected, final int expectedRow, final int expectedCol) {
		AwesomeLinkFilter filter = new AwesomeLinkFilter(getProject());

		// Test only detecting file paths - no file existence check
		List<FileLinkMatch> results = filter.detectPaths(line);

		assertEquals("No matches in line \"" + line + "\"", 1, results.size());
		FileLinkMatch info = results.get(0);
		assertEquals(String.format("Expected filter to detect \"%s\" link in \"%s\"", expected, line), expected, info.path);

		if (expectedRow >= 0)
			assertEquals("Expected to capture row number", expectedRow, info.row);

		if (expectedCol >= 0)
			assertEquals("Expected to capture column number", expectedCol, info.col);
	}


	private void assertURLDetection(final String line, final String expected) {
		AwesomeLinkFilter filter = new AwesomeLinkFilter(getProject());

		// Test only detecting file paths - no file existence check
		List<URLLinkMatch> results = filter.detectURLs(line);

		assertEquals("No matches in line \"" + line + "\"", 1, results.size());
		URLLinkMatch info = results.get(0);
		assertEquals(String.format("Expected filter to detect \"%s\" path in \"%s\"", expected, line), expected, info.path);
	}
}
