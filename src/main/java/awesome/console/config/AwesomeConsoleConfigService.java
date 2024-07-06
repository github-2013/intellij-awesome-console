package awesome.console.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(
	name = "AwesomeConsoleConfig",
	storages = {
		@Storage(value = "AwesomeConsole.xml", roamingType = RoamingType.DISABLED)
	}
)
public class AwesomeConsoleConfigService implements PersistentStateComponent<AwesomeConsoleConfigService> {
	public boolean SPLIT_ON_LIMIT = DefaultConfig.DEFAULT_SPLIT_ON_LIMIT;
	public boolean LIMIT_LINE_LENGTH = DefaultConfig.DEFAULT_LIMIT_LINE_LENGTH;
	public int LINE_MAX_LENGTH = DefaultConfig.DEFAULT_LINE_MAX_LENGTH;
	public boolean SEARCH_URLS = DefaultConfig.DEFAULT_SEARCH_URLS;

	@Transient
	private AwesomeConsoleConfigForm form;

	@Nullable
	@Override
	public AwesomeConsoleConfigService getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull final AwesomeConsoleConfigService state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static AwesomeConsoleConfigService getInstance() {
		return ApplicationManager.getApplication()
				.getService(AwesomeConsoleConfigService.class);
	}
}
