package awesome.console.match;

public class FileLinkMatch {
	public final String link; // Full link match (with additional info, such as row and col)
	public final String prefix;
	public final String path; // Just path - no additional info
	public final int row;
	public final int col;
	public final int start;
	public final int end;

	public FileLinkMatch(
		final String link,
		final String prefix,
		final String path,
		final int start,
		final int end,
		final int row,
		final int col
	) {
		this.link = link;
		this.prefix = prefix;
		this.path = path;
		this.start = start;
		this.end = end;
		this.row = row;
		this.col = col;
	}
}
