package awesome.console;

import awesome.console.match.FileLinkMatch;
import awesome.console.match.URLLinkMatch;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static awesome.console.IntegrationTest.*;


public class AwesomeLinkFilterTest1 extends BasePlatformTestCase {

	private AwesomeLinkFilter filter;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		filter = new AwesomeLinkFilter(getProject());
	}

	@Test
	public void testFileWithoutDirectory() {
		assertPathDetection("With line: file1.java:5:5", "file1.java:5:5", 5, 5);
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
