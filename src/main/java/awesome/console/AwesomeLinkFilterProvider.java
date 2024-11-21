package awesome.console;

import com.intellij.execution.filters.ConsoleDependentFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.terminal.TerminalExecutionConsole;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AwesomeLinkFilterProvider extends ConsoleDependentFilterProvider {
	private static final Map<Project, Filter[]> cache = new ConcurrentHashMap<>();

	public AwesomeLinkFilterProvider() {
		ApplicationManager.getApplication().getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
			@Override
			public void projectClosed(@NotNull Project project) {
				cache.remove(project);
			}
		});
	}

	@NotNull
	@Override
	public Filter @NotNull [] getDefaultFilters(@NotNull final ConsoleView consoleView, @NotNull final Project project, @NotNull final GlobalSearchScope globalSearchScope) {
		boolean isTerminal = false;
		try {
			// TerminalExecutionConsole is used in JBTerminalWidget
			isTerminal = consoleView instanceof TerminalExecutionConsole;
		} catch (Throwable ignored) {
		}
		return getDefaultFilters(project, isTerminal);
	}

	@NotNull
	@Override
	public Filter @NotNull [] getDefaultFilters(@NotNull final Project project) {
		return getDefaultFilters(project, true);
	}

	@NotNull
	public Filter[] getDefaultFilters(@NotNull final Project project, final boolean isTerminal) {
		// TODO Hack: In the Terminal, Filter only belongs to one thread, but in ConsoleView,
		//     Filter will run in multiple threads, so set the default value of isTerminal to
		//     false. There's no better way determine whether a Filter is running in the Terminal,
		//     even if it's not a good way. Since Filter is cached as a singleton, everything has
		//     become more complicated.
		Filter[] filters = cache.computeIfAbsent(project, (key) -> new Filter[]{new AwesomeLinkFilter(project)});
		((AwesomeLinkFilter) filters[0]).isTerminal.set(isTerminal);
		return filters;
	}
}