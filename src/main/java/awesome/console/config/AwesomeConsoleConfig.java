package awesome.console.config;

import static awesome.console.config.AwesomeConsoleDefaults.DEFAULT_GROUP_RETRIES;
import static awesome.console.config.AwesomeConsoleDefaults.FILE_PATTERN_REQUIRED_GROUPS;

import awesome.console.util.RegexUtils;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.text.StringUtil;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * For a Configurable implementation correctly declared using an EP, the implementation's
 * constructor is not invoked by the IntelliJ Platform until a user chooses the corresponding
 * Settings displayName in the Settings Dialog menu.
 *
 * A Configurable instance's lifetime ends when OK or Cancel is selected in the Settings
 * Dialog. An instance's Configurable.disposeUIResources() is called when the Settings
 * Dialog is closing.
 *
 * ref: https://plugins.jetbrains.com/docs/intellij/settings-guide.html
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class AwesomeConsoleConfig implements Configurable {

	private AwesomeConsoleConfigForm form;

	private final AwesomeConsoleStorage storage;

	public AwesomeConsoleConfig() {
		this.storage = AwesomeConsoleStorage.getInstance();
	}

	private void initFromConfig() {
		form.debugModeCheckBox.setSelected(storage.DEBUG_MODE);

		form.limitLineMatchingByCheckBox.setSelected(storage.LIMIT_LINE_LENGTH);

		form.matchLinesLongerThanCheckBox.setEnabled(storage.LIMIT_LINE_LENGTH);
		form.matchLinesLongerThanCheckBox.setSelected(storage.SPLIT_ON_LIMIT);

		form.searchForURLsCheckBox.setSelected(storage.searchUrls);
		form.initMatchFiles(storage.searchFiles, storage.searchClasses, storage.useFilePattern, storage.getFilePatternText());
		form.initLimitResult(storage.useResultLimit, storage.getResultLimit());

		form.maxLengthTextField.setText(String.valueOf(storage.LINE_MAX_LENGTH));
		form.maxLengthTextField.setEnabled(storage.LIMIT_LINE_LENGTH);
		form.maxLengthTextField.setEditable(storage.LIMIT_LINE_LENGTH);

		form.initIgnorePattern(storage.useIgnorePattern, storage.getIgnorePatternText(), storage.useIgnoreStyle);

		form.fixChooseTargetFileCheckBox.setSelected(storage.fixChooseTargetFile);

		form.initFileTypes(storage.useFileTypes, storage.getFileTypes());

		form.resolveSymlinkCheckBox.setSelected(storage.resolveSymlink);
	}

	private void showErrorDialog() {
		JOptionPane.showMessageDialog(form.mainpanel, "Error: Please enter a positive number.", "Invalid value", JOptionPane.ERROR_MESSAGE);
	}

	private void showErrorDialog(String title, String message) {
		JOptionPane.showMessageDialog(form.mainpanel, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private boolean checkRegex(@NotNull final String pattern) {
		if (pattern.isEmpty() || !RegexUtils.isValidRegex(pattern)) {
			showErrorDialog("Invalid value", "Invalid pattern: " + StringUtil.trimMiddle(pattern, 150));
			return false;
		}
		return true;
	}

	private boolean checkRegexGroup(@NotNull final String pattern, @NotNull final String... groups) {
		if (pattern.isEmpty()) {
			return false;
		}
		boolean hasGroup;
		for (String group : groups) {
			try {
				Matcher matcher = Pattern.compile("\\(\\?<" + group + "([1-9][0-9]*)?>").matcher(pattern);
				if (matcher.find()) {
					String index = matcher.group(1);
					hasGroup = StringUtil.isEmpty(index) || Integer.parseInt(index) <= DEFAULT_GROUP_RETRIES;
				} else {
					hasGroup = false;
				}
			} catch (PatternSyntaxException | NumberFormatException e) {
				hasGroup = false;
			}
			if (!hasGroup) {
				showErrorDialog("Invalid value", String.format(
						"Missing required group \"%s\": %s",
						group, StringUtil.trimMiddle(pattern, 150)
				));
				return false;
			}
		}
		return true;
	}

	/**
	 * Configurable
	 */
	@Nls
	@Override
	public String getDisplayName() {
		return "Awesome Console";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return "help topic";
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		form = new AwesomeConsoleConfigForm();
		initFromConfig();
		return form.mainpanel;
	}

	@Override
	public boolean isModified() {
		final String text = form.maxLengthTextField.getText().trim();
		if (text.length() < 1) {
			return true;
		}
		final int len;
		try {
			len = Integer.parseInt(text);
		} catch (final NumberFormatException nfe) {
			return true;
		}
		return form.debugModeCheckBox.isSelected() != storage.DEBUG_MODE
				|| form.limitLineMatchingByCheckBox.isSelected() != storage.LIMIT_LINE_LENGTH
				|| len != storage.LINE_MAX_LENGTH
				|| form.matchLinesLongerThanCheckBox.isSelected() != storage.SPLIT_ON_LIMIT
				|| form.searchForURLsCheckBox.isSelected() != storage.searchUrls
				|| form.searchForFilesCheckBox.isSelected() != storage.searchFiles
				|| form.searchForClassesCheckBox.isSelected() != storage.searchClasses
				|| form.limitResultCheckBox.isSelected() != storage.useResultLimit
				|| !Objects.equals(form.limitResultSpinner.getValue(), storage.getResultLimit())
				|| form.filePatternCheckBox.isSelected() != storage.useFilePattern
				|| !form.filePatternTextArea.getText().trim().equals(storage.getFilePatternText())
				|| form.ignorePatternCheckBox.isSelected() != storage.useIgnorePattern
				|| !form.ignorePatternTextField.getText().trim().equals(storage.getIgnorePatternText())
				|| form.ignoreStyleCheckBox.isSelected() != storage.useIgnoreStyle
				|| form.fixChooseTargetFileCheckBox.isSelected() != storage.fixChooseTargetFile
				|| form.fileTypesCheckBox.isSelected() != storage.useFileTypes
				|| !form.fileTypesTextField.getText().trim().equals(storage.getFileTypes())
				|| form.resolveSymlinkCheckBox.isSelected() != storage.resolveSymlink
				;
	}

	@Override
	public void apply() {
		final String text = form.maxLengthTextField.getText().trim();
		if (text.length() < 1) {
			showErrorDialog();
			return;
		}
		final int maxLength;
		try {
			maxLength = Integer.parseInt(text);
		} catch (final NumberFormatException nfe) {
			showErrorDialog();
			return;
		}
		if (maxLength < 1) {
			showErrorDialog();
			return;
		}

		final boolean useFilePattern = form.filePatternCheckBox.isSelected();
		final String filePatternText = form.filePatternTextArea.getText().trim();

		if (!Objects.equals(filePatternText, storage.getFilePatternText()) &&
				!(checkRegex(filePatternText) && checkRegexGroup(filePatternText, FILE_PATTERN_REQUIRED_GROUPS))) {
			return;
		}

		final boolean useIgnorePattern = form.ignorePatternCheckBox.isSelected();
		final String ignorePatternText = form.ignorePatternTextField.getText().trim();

		if (!Objects.equals(ignorePatternText, storage.getIgnorePatternText()) &&
				!checkRegex(ignorePatternText)) {
			return;
		}

		storage.DEBUG_MODE = form.debugModeCheckBox.isSelected();
		storage.LIMIT_LINE_LENGTH = form.limitLineMatchingByCheckBox.isSelected();
		storage.LINE_MAX_LENGTH = maxLength;
		storage.SPLIT_ON_LIMIT = form.matchLinesLongerThanCheckBox.isSelected();

		storage.searchUrls = form.searchForURLsCheckBox.isSelected();
		storage.searchFiles = form.searchForFilesCheckBox.isSelected();
		storage.searchClasses = form.searchForClassesCheckBox.isSelected();

		storage.useResultLimit = form.limitResultCheckBox.isSelected();
		storage.setResultLimit((int) form.limitResultSpinner.getValue());

		storage.useFilePattern = useFilePattern;
		storage.setFilePatternText(filePatternText);
		form.filePatternTextArea.setText(filePatternText);

		storage.useIgnorePattern = useIgnorePattern;
		storage.setIgnorePatternText(ignorePatternText);
		form.ignorePatternTextField.setText(ignorePatternText);
		storage.useIgnoreStyle = form.ignoreStyleCheckBox.isSelected();

		storage.fixChooseTargetFile = form.fixChooseTargetFileCheckBox.isSelected();

		storage.useFileTypes = form.fileTypesCheckBox.isSelected();
		storage.setFileTypes(form.fileTypesTextField.getText().trim());
		form.fileTypesTextField.setText(storage.getFileTypes());

		storage.resolveSymlink = form.resolveSymlinkCheckBox.isSelected();
	}

	@Override
	public void reset() {
		initFromConfig();
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}
}
