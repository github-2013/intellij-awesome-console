package awesome.console.match;

public class URLLinkMatch {
	public final String link;
	public final String prefix;
	public final String path;
	public final int start;
	public final int end;

	public URLLinkMatch(
		final String link,
		final String prefix,
		final String path,
		final int start,
		final int end
	) {
		this.link = link;
		this.prefix = prefix;
		this.path = path;
		this.start = start;
		this.end = end;
	}
}
