package awesome.console.config;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class AwesomeConsoleConfigForm {

	public JPanel mainPanel;
	public JCheckBox limitLineMatchingByCheckBox;
	public JFormattedTextField maxLengthTextField;
	public JCheckBox matchLinesLongerThanCheckBox;
	public JCheckBox searchForURLsFileCheckBox;
	public JLabel configTitleLabel;
	public JCheckBox matchNodeModulesPathCheckBox;

	public AwesomeConsoleConfigForm() {
		maxLengthTextField.setText(String.valueOf(DefaultConfig.DEFAULT_LINE_MAX_LENGTH));
		maxLengthTextField.setEnabled(true);
		maxLengthTextField.setEditable(true);
		limitLineMatchingByCheckBox.setSelected(DefaultConfig.DEFAULT_LIMIT_LINE_LENGTH);
		matchLinesLongerThanCheckBox.setEnabled(true);
		searchForURLsFileCheckBox.setSelected(DefaultConfig.DEFAULT_SEARCH_URLS);

		limitLineMatchingByCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean selected = limitLineMatchingByCheckBox.isSelected();
				maxLengthTextField.setEnabled(selected);
				maxLengthTextField.setEditable(selected);
				matchLinesLongerThanCheckBox.setEnabled(selected);
			}
		});
		searchForURLsFileCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean isSelected = searchForURLsFileCheckBox.isSelected();
				searchForURLsFileCheckBox.setSelected(isSelected);
			}
		});
		matchNodeModulesPathCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean isSelected = matchNodeModulesPathCheckBox.isSelected();
				matchNodeModulesPathCheckBox.setSelected(isSelected);
			}
		});
	}

	private void createUIComponents() {
		setupLineLimit();
		setupSplitLineIntoChunk();
		setupMatchURLs();
	}

	private void setupLineLimit() {
		limitLineMatchingByCheckBox = new JCheckBox("limitLineMatchingByCheckBox");
		limitLineMatchingByCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean selected = limitLineMatchingByCheckBox.isSelected();
				maxLengthTextField.setEnabled(selected);
				maxLengthTextField.setEditable(selected);
				matchLinesLongerThanCheckBox.setEnabled(selected);
			}
		});

		final DecimalFormat decimalFormat = new DecimalFormat("#####");
		final NumberFormatter formatter = new NumberFormatter(decimalFormat);
		formatter.setMinimum(0);
		formatter.setValueClass(Integer.class);
		maxLengthTextField = new JFormattedTextField(formatter);
		maxLengthTextField.setColumns(5);

		JPopupMenu popup = new JPopupMenu("Defaults");
		maxLengthTextField.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				maxLengthTextField.setText(String.valueOf(DefaultConfig.DEFAULT_LINE_MAX_LENGTH));
				maxLengthTextField.setEnabled(true);
				maxLengthTextField.setEditable(true);
				limitLineMatchingByCheckBox.setSelected(DefaultConfig.DEFAULT_LIMIT_LINE_LENGTH);
				matchLinesLongerThanCheckBox.setEnabled(true);
			}
		});
	}

	private void setupSplitLineIntoChunk() {
		matchLinesLongerThanCheckBox = new JCheckBox("matchLinesLongerThanCheckBox");
		matchLinesLongerThanCheckBox.setToolTipText("Check this to keep on matching the text of a line longer than the defined limit. Keep in mind: The text will be matched chunk by chunk, so it might miss some links.");
		JPopupMenu popup = new JPopupMenu("Defaults");
		matchLinesLongerThanCheckBox.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean isSelected = matchLinesLongerThanCheckBox.isSelected();
				matchLinesLongerThanCheckBox.setSelected(isSelected);
			}
		});
	}

	private void setupMatchURLs() {
		searchForURLsFileCheckBox = new JCheckBox("searchForURLsFileCheckBox");
		searchForURLsFileCheckBox.setSelected(DefaultConfig.DEFAULT_SEARCH_URLS);
		searchForURLsFileCheckBox.setToolTipText("Uncheck if you do not want URLs parsed from the console.");
		JPopupMenu popup = new JPopupMenu("Defaults");
		searchForURLsFileCheckBox.setComponentPopupMenu(popup);

		final JMenuItem itm = popup.add("Restore defaults");
		itm.setMnemonic(KeyEvent.VK_R);
		itm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				boolean isSelected = searchForURLsFileCheckBox.isSelected();
				searchForURLsFileCheckBox.setSelected(isSelected);
			}
		});
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		configTitleLabel = new JLabel();
		configTitleLabel.setEnabled(true);
		configTitleLabel.setText("Awesome Console Config");
		mainPanel.add(configTitleLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(5, 4, new Insets(0, 0, 0, 0), -1, -1));
		mainPanel.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		limitLineMatchingByCheckBox = new JCheckBox();
		limitLineMatchingByCheckBox.setSelected(false);
		limitLineMatchingByCheckBox.setText("Limit line matching by");
		panel1.add(limitLineMatchingByCheckBox, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("chars.");
		panel1.add(label1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		maxLengthTextField = new JFormattedTextField();
		maxLengthTextField.setText("0");
		panel1.add(maxLengthTextField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		matchLinesLongerThanCheckBox = new JCheckBox();
		matchLinesLongerThanCheckBox.setSelected(false);
		matchLinesLongerThanCheckBox.setText("Match lines longer than the limit chunk by chunk.");
		matchLinesLongerThanCheckBox.setToolTipText("Check this to keep on matching the text of a line longer than the defined limit. Keep in mind: The text will be matched chunk by chunk, so it might miss some links.");
		panel1.add(matchLinesLongerThanCheckBox, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		searchForURLsFileCheckBox = new JCheckBox();
		searchForURLsFileCheckBox.setText("Match URLs (file, ftp, http(s)) Protocol.");
		searchForURLsFileCheckBox.setToolTipText("Uncheck if you do not want URLs parsed from the console.");
		panel1.add(searchForURLsFileCheckBox, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		matchNodeModulesPathCheckBox = new JCheckBox();
		matchNodeModulesPathCheckBox.setText("Match node_modules paths.");
		panel1.add(matchNodeModulesPathCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return mainPanel;
	}

}
