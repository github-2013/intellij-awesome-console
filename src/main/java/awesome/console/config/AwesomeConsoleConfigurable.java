// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package awesome.console.config;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * Provides controller functionality for application settings.
 */
final class AwesomeConsoleConfigurable implements Configurable {

    private AwesomeConsoleConfigForm form;

    // A default constructor with no arguments is required because
    // this implementation is registered as an applicationConfigurable

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Awesome Console X";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return form.mainPanel;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        form = new AwesomeConsoleConfigForm();
        return form.mainPanel;
    }

    @Override
    public boolean isModified() {
        AwesomeConsoleConfigService state =
                Objects.requireNonNull(AwesomeConsoleConfigService.getInstance().getState());
        int maxLength = getMaxLengthFromTextField();

        return form.limitLineMatchingByCheckBox.isSelected() != state.LIMIT_LINE_LENGTH
                || maxLength != state.LINE_MAX_LENGTH
                || form.matchLinesLongerThanCheckBox.isSelected() != state.SPLIT_ON_LIMIT
                || form.searchForURLsFileCheckBox.isSelected() != state.SEARCH_URLS
                || form.matchNodeModulesPathCheckBox.isSelected() != state.MATCH_NODE_MODULES_PATH;
    }

    @Override
    public void apply() {
        int maxLength = getMaxLengthFromTextField();
        if (maxLength < 1) {
            showErrorDialog();
            return;
        }

        AwesomeConsoleConfigService state =
                Objects.requireNonNull(AwesomeConsoleConfigService.getInstance().getState());
        state.LIMIT_LINE_LENGTH = form.limitLineMatchingByCheckBox.isSelected();
        state.LINE_MAX_LENGTH = maxLength;
        state.SPLIT_ON_LIMIT = form.matchLinesLongerThanCheckBox.isSelected();
        state.SEARCH_URLS = form.searchForURLsFileCheckBox.isSelected();
        state.MATCH_NODE_MODULES_PATH = form.matchNodeModulesPathCheckBox.isSelected();
    }

    @Override
    public void reset() {
        AwesomeConsoleConfigService state =
                Objects.requireNonNull(AwesomeConsoleConfigService.getInstance().getState());
        form.limitLineMatchingByCheckBox.setSelected(state.LIMIT_LINE_LENGTH);

        form.matchLinesLongerThanCheckBox.setEnabled(state.LIMIT_LINE_LENGTH);
        form.matchLinesLongerThanCheckBox.setSelected(state.SPLIT_ON_LIMIT);

        form.searchForURLsFileCheckBox.setSelected(state.SEARCH_URLS);

        form.maxLengthTextField.setText(String.valueOf(state.LINE_MAX_LENGTH));
        form.maxLengthTextField.setEnabled(state.LIMIT_LINE_LENGTH);
        form.maxLengthTextField.setEditable(state.LIMIT_LINE_LENGTH);

        form.matchNodeModulesPathCheckBox.setSelected(state.MATCH_NODE_MODULES_PATH);
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

    private void initFromConfig() {
        AwesomeConsoleConfigService state =
                Objects.requireNonNull(AwesomeConsoleConfigService.getInstance().getState());

        form.limitLineMatchingByCheckBox.setSelected(state.LIMIT_LINE_LENGTH);

        form.matchLinesLongerThanCheckBox.setEnabled(state.LIMIT_LINE_LENGTH);
        form.matchLinesLongerThanCheckBox.setSelected(state.SPLIT_ON_LIMIT);

        form.searchForURLsFileCheckBox.setSelected(state.SEARCH_URLS);

        form.maxLengthTextField.setText(String.valueOf(state.LINE_MAX_LENGTH));
        form.maxLengthTextField.setEnabled(state.LIMIT_LINE_LENGTH);
        form.maxLengthTextField.setEditable(state.LIMIT_LINE_LENGTH);
    }

    private void showErrorDialog() {
        JOptionPane.showMessageDialog(form.mainPanel, "Error: Please enter a positive number.", "Invalid value", JOptionPane.ERROR_MESSAGE);
    }

    private int getMaxLengthFromTextField() {
        int DefaultMaxLength = 1024;
        String text = form.maxLengthTextField.getText().trim();
        if (text.isEmpty()) {
            return DefaultMaxLength;
        }
        try {
            return Integer.parseInt(text);
        } catch (final NumberFormatException nfe) {
            return DefaultMaxLength;
        }
    }
}
