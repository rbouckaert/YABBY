package yabby.app.beauti;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JSeparator;
import javax.swing.JButton;

import javax.swing.JLabel;
import javax.swing.JCheckBox;

import yabby.app.util.Utils;



public class GuessPatternDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public static final String EXAMPLE_FORMAT = "<html>A proper trait file is tab delimited. <br>"
			+ "The first row is always <font color=red>traits</font> followed by the keyword <br>"
			+ "(e.g. <font color=red>species</font> in *BEAST) in the second column and separated <br>"
			+ "by <font color=red>tab</font>. The rest rows are mapping taxa to species, which list <br>"
			+ "taxon name in the first column and species name in the second column separated by <br>"
			+ "<font color=red>tab</font>. For example: <br>" + "traits\tspecies<br>" + "taxon1\tspeciesA<br>"
			+ "taxon2\tspeciesA<br>" + "taxon3\tspeciesB<br>" + "... ...<br>"
			+ "Once mapping file is loaded, the trait named by keyword <font color=red>species</font> <br>"
			+ "is displayed in the main panel, and the message of using *BEAST is also displayed on <br>"
			+ "the bottom of main frame.<br>"
			+ "For multi-alignment, the default of *BEAST is unlinking all models: substitution model, <br>"
			+ "clock model, and tree models.</html>";

	public enum Status {
		canceled, pattern, trait
	};

	public String trait = null;

	public String getTrait() {
		return trait;
	}

	Component m_parent;
	JPanel guessPanel;
	ButtonGroup group;
	JRadioButton b1 = new JRadioButton("use everything");
	JRadioButton b2 = new JRadioButton("split on character");
	JRadioButton b3 = new JRadioButton("use regular expression");
	JRadioButton b4 = new JRadioButton("read from file");

	int m_location = 0;
	int m_splitlocation = 0;
	String m_sDelimiter = ".";
	JTextField textRegExp;
	JComboBox combo;
	JComboBox combo_1; 
	String pattern;
	
	public String getPattern() {
		return pattern;
	}

	private JTextField txtFile;
	private JTextField textSplitChar;
	private JTextField textSplitChar2;
	private JTextField textAddValue;
	private JTextField textUnlessLessThan;
	private JTextField textThenAdd;
	JCheckBox chckbxAddFixedValue;
	JCheckBox chckbxUnlessLessThan;
	JLabel lblThenAdd;
	JLabel lblAndTakeGroups;
	JButton btnBrowse;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JSeparator separator_4;
	private JSeparator separator_5;
	
	public GuessPatternDialog(Component parent, String pattern) {
		m_parent = parent;
		this.pattern = pattern;
		guessPanel = new JPanel();
		GridBagLayout gbl_guessPanel = new GridBagLayout();
		gbl_guessPanel.rowHeights = new int[]{0, 0, 0, 20, 0, 0, 20, 0, 0, 20, 0, 29, 0, 0, 0, 0};
		gbl_guessPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_guessPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_guessPanel.columnWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 1.0, 0.0 };
		guessPanel.setLayout(gbl_guessPanel);
		
		group = new ButtonGroup();
		group.add(b1);
		group.add(b2);
		group.add(b3);
		group.add(b4);
		group.setSelected(b1.getModel(), true);
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		b1.setName(b1.getText());
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		b2.setName(b2.getText());
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		b3.setName(b3.getText());
		b4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		b4.setName(b4.getText());

		createDelimiterBox(b1);
		createSplitBox(b2);
		createRegExtpBox(b3);
		
				textRegExp = new JTextField();
				textRegExp.setText(pattern);
				textRegExp.setColumns(10);
				textRegExp.setToolTipText("Enter regular expression to match taxa");
				textRegExp.setMaximumSize(new Dimension(1024, 25));
				GridBagConstraints gbc2 = new GridBagConstraints();
				gbc2.insets = new Insets(0, 0, 5, 5);
				gbc2.anchor = GridBagConstraints.WEST;
				gbc2.gridwidth = 4;
				gbc2.gridx = 1;
				gbc2.gridy = 7;
				guessPanel.add(textRegExp, gbc2);
				textRegExp.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void removeUpdate(DocumentEvent e) {
						b3.setSelected(true);
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						b3.setSelected(true);
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						b3.setSelected(true);
					}
				});
		
		separator_4 = new JSeparator();
		separator_4.setPreferredSize(new Dimension(5,1));
		GridBagConstraints gbc_separator_4 = new GridBagConstraints();
		gbc_separator_4.gridwidth = 5;
		gbc_separator_4.insets = new Insets(5, 0, 15, 5);
		gbc_separator_4.gridx = 0;
		gbc_separator_4.gridy = 8;
		gbc_separator_4.fill = GridBagConstraints.HORIZONTAL;
		guessPanel.add(separator_4, gbc_separator_4);

		GridBagConstraints gbc_rdbtnReadFromFile = new GridBagConstraints();
		gbc_rdbtnReadFromFile.anchor = GridBagConstraints.WEST;
		gbc_rdbtnReadFromFile.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnReadFromFile.gridx = 0;
		gbc_rdbtnReadFromFile.gridy = 10;
		guessPanel.add(b4, gbc_rdbtnReadFromFile);

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = Utils.getLoadFile("Load trait from file", new File(Beauti.g_sDir), "Select trait file", "dat","txt");
				if (file != null) {
					txtFile.setText(file.getPath());
					b4.setSelected(true);
					updateFields();
				}
			}
		});
		
				txtFile = new JTextField();
				txtFile.setText("File");
				GridBagConstraints gbc_txtFile = new GridBagConstraints();
				gbc_txtFile.gridwidth = 2;
				gbc_txtFile.insets = new Insets(0, 0, 5, 5);
				gbc_txtFile.fill = GridBagConstraints.HORIZONTAL;
				gbc_txtFile.gridx = 1;
				gbc_txtFile.gridy = 10;
				guessPanel.add(txtFile, gbc_txtFile);
				txtFile.setColumns(10);
		GridBagConstraints gbc_btnReadFromFile = new GridBagConstraints();
		gbc_btnReadFromFile.insets = new Insets(0, 0, 5, 5);
		gbc_btnReadFromFile.gridx = 3;
		gbc_btnReadFromFile.gridy = 10;
		guessPanel.add(btnBrowse, gbc_btnReadFromFile);

		JButton btnHelp = new JButton("?");
		btnHelp.setToolTipText("Show format of trait file");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(m_parent, EXAMPLE_FORMAT);
			}
		});
		GridBagConstraints gbc_btnHelp = new GridBagConstraints();
		gbc_btnHelp.insets = new Insets(0, 0, 5, 5);
		gbc_btnHelp.gridx = 4;
		gbc_btnHelp.gridy = 10;
		guessPanel.add(btnHelp, gbc_btnHelp);
		
		
		chckbxAddFixedValue = new JCheckBox("Add fixed value");
		chckbxAddFixedValue.setName("Add fixed value");
		chckbxAddFixedValue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		
		separator_5 = new JSeparator();
		separator_5.setPreferredSize(new Dimension(5,1));
		GridBagConstraints gbc_separator_5 = new GridBagConstraints();
		gbc_separator_5.gridwidth = 5;
		gbc_separator_5.insets = new Insets(5, 0, 15, 5);
		gbc_separator_5.gridx = 0;
		gbc_separator_5.gridy = 12;
		gbc_separator_5.fill = GridBagConstraints.HORIZONTAL;
		guessPanel.add(separator_5, gbc_separator_5);
		GridBagConstraints gbc_chckbxAddFixedValue = new GridBagConstraints();
		gbc_chckbxAddFixedValue.anchor = GridBagConstraints.WEST;
		gbc_chckbxAddFixedValue.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAddFixedValue.gridx = 0;
		gbc_chckbxAddFixedValue.gridy = 13;
		guessPanel.add(chckbxAddFixedValue, gbc_chckbxAddFixedValue);
		
		textAddValue = new JTextField("1900");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 13;
		guessPanel.add(textAddValue, gbc_textField);
		textAddValue.setColumns(10);
		
		chckbxUnlessLessThan = new JCheckBox("Unless less than...");
		chckbxUnlessLessThan.setName("Unless less than");
		chckbxUnlessLessThan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		GridBagConstraints gbc_chckbxUnlessLargerThan = new GridBagConstraints();
		gbc_chckbxUnlessLargerThan.anchor = GridBagConstraints.WEST;
		gbc_chckbxUnlessLargerThan.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUnlessLargerThan.gridx = 0;
		gbc_chckbxUnlessLargerThan.gridy = 14;
		guessPanel.add(chckbxUnlessLessThan, gbc_chckbxUnlessLargerThan);
		
		textUnlessLessThan = new JTextField("13");
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.gridwidth = 2;
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 14;
		guessPanel.add(textUnlessLessThan, gbc_textField_1);
		textUnlessLessThan.setColumns(10);
		
		lblThenAdd = new JLabel("...then add");
		GridBagConstraints gbc_lblThenAdd = new GridBagConstraints();
		gbc_lblThenAdd.anchor = GridBagConstraints.EAST;
		gbc_lblThenAdd.insets = new Insets(0, 0, 0, 5);
		gbc_lblThenAdd.gridx = 0;
		gbc_lblThenAdd.gridy = 15;
		guessPanel.add(lblThenAdd, gbc_lblThenAdd);
		
		textThenAdd = new JTextField("2000");
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.gridwidth = 2;
		gbc_textField_2.insets = new Insets(0, 0, 0, 5);
		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_2.gridx = 1;
		gbc_textField_2.gridy = 15;
		guessPanel.add(textThenAdd, gbc_textField_2);
		textThenAdd.setColumns(10);
		
		
		chckbxAddFixedValue.setVisible(false);
		textAddValue.setVisible(false);
		chckbxUnlessLessThan.setVisible(false);
		lblThenAdd.setVisible(false);
		chckbxUnlessLessThan.setVisible(false);
		textUnlessLessThan.setVisible(false);
		textThenAdd.setVisible(false);
	}
	
	public void allowAddingValues() {
		chckbxAddFixedValue.setVisible(true);
		textAddValue.setVisible(true);
		chckbxUnlessLessThan.setVisible(true);
		lblThenAdd.setVisible(true);
		chckbxUnlessLessThan.setVisible(true);
		textUnlessLessThan.setVisible(true);
		textThenAdd.setVisible(true);
	}

	protected void updateFields() {
		if (chckbxAddFixedValue.isSelected()) {
			textAddValue.setEnabled(true);
			chckbxUnlessLessThan.setEnabled(true);
			lblThenAdd.setEnabled(true);
			if (chckbxUnlessLessThan.isSelected()) {
				textUnlessLessThan.setEnabled(true);
				textThenAdd.setEnabled(true);
			} else {
				textUnlessLessThan.setEnabled(false);
				textThenAdd.setEnabled(false);
			}
		} else {
			textAddValue.setEnabled(false);
			chckbxUnlessLessThan.setEnabled(false);
			lblThenAdd.setEnabled(false);
			textUnlessLessThan.setEnabled(false);
			textThenAdd.setEnabled(false);
		}
		
		txtFile.setEnabled(false);
		textSplitChar.setEnabled(false);
		textSplitChar2.setEnabled(false);
		textRegExp.setEnabled(false);			
		combo.setEnabled(false);
		combo_1.setEnabled(false);
		lblAndTakeGroups.setEnabled(false);
		btnBrowse.setEnabled(false);
		if (b1.isSelected()) {
			textSplitChar.setEnabled(true);			
			combo.setEnabled(true);
		}
		if (b2.isSelected()) {
			textSplitChar2.setEnabled(true);			
			combo_1.setEnabled(true);
			lblAndTakeGroups.setEnabled(true);
		}
		if (b3.isSelected()) {
			textRegExp.setEnabled(true);			
		}
		if (b4.isSelected()) {
			btnBrowse.setEnabled(true);
			txtFile.setEnabled(true);			
		}
	}

	private void createDelimiterBox(JRadioButton b) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		guessPanel.add(b, gbc);

		combo = new JComboBox(new String[] { "after first", "after last", "before first", "before last" });
		combo.setName("delimiterCombo");
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.anchor = GridBagConstraints.WEST;
		gbc2.gridwidth = 2;
		gbc2.insets = new Insets(0, 0, 5, 5);
		gbc2.gridx = 1;
		gbc2.gridy = 1;
		guessPanel.add(combo, gbc2);
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox combo = (JComboBox) e.getSource();
				m_location = combo.getSelectedIndex();
				b1.setSelected(true);
				updateFields();
			}
		});
	}

	private void createSplitBox(JRadioButton b) {
		
		textSplitChar = new JTextField("_");
		textSplitChar.setName("SplitChar");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.WEST;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 1;
		guessPanel.add(textSplitChar, gbc_textField);
		textSplitChar.setColumns(2);
		
		separator_2 = new JSeparator();
		separator_2.setPreferredSize(new Dimension(5,1));
		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
		gbc_separator_2.gridwidth = 5;
		gbc_separator_2.insets = new Insets(5, 0, 15, 5);
		gbc_separator_2.gridx = 0;
		gbc_separator_2.gridy = 2;
		gbc_separator_2.fill = GridBagConstraints.HORIZONTAL;
		guessPanel.add(separator_2, gbc_separator_2);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 4;
		guessPanel.add(b, gbc);
	}
	public void createRegExtpBox(JRadioButton b) {
				
				textSplitChar2 = new JTextField("_");
				textSplitChar2.setName("SplitChar2");
				GridBagConstraints gbc_textField_1 = new GridBagConstraints();
				gbc_textField_1.anchor = GridBagConstraints.WEST;
				gbc_textField_1.insets = new Insets(0, 0, 5, 5);
				gbc_textField_1.gridx = 1;
				gbc_textField_1.gridy = 4;
				guessPanel.add(textSplitChar2, gbc_textField_1);
				textSplitChar2.setColumns(2);
				
				lblAndTakeGroups = new JLabel("and take group(s):");
				GridBagConstraints gbc_lblAndTakeGroups = new GridBagConstraints();
				gbc_lblAndTakeGroups.gridwidth = 2;
				gbc_lblAndTakeGroups.insets = new Insets(0, 0, 5, 5);
				gbc_lblAndTakeGroups.gridx = 2;
				gbc_lblAndTakeGroups.gridy = 4;
				guessPanel.add(lblAndTakeGroups, gbc_lblAndTakeGroups);
		
				combo_1 = new JComboBox(new String[] { "1", "2", "3", "4", "1-2", "2-3", "3-4", "1-3", "2-4" });
				combo_1.setName("splitCombo");
				GridBagConstraints gbc_combo_1 = new GridBagConstraints();
				gbc_combo_1.anchor = GridBagConstraints.WEST;
				gbc_combo_1.insets = new Insets(0, 0, 5, 5);
				gbc_combo_1.gridx = 4;
				gbc_combo_1.gridy = 4;
				guessPanel.add(combo_1, gbc_combo_1);
				combo_1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JComboBox combo = (JComboBox) e.getSource();
						m_splitlocation = combo.getSelectedIndex();
						b2.setSelected(true);
						updateFields();
					}
				});
		
		separator_3 = new JSeparator();
		separator_3.setPreferredSize(new Dimension(5,1));
		GridBagConstraints gbc_separator_3 = new GridBagConstraints();
		gbc_separator_3.gridwidth = 5;
		gbc_separator_3.insets = new Insets(5, 0, 15, 5);
		gbc_separator_3.gridx = 0;
		gbc_separator_3.gridy = 5;
		gbc_separator_3.fill = GridBagConstraints.HORIZONTAL;
		guessPanel.add(separator_3, gbc_separator_3);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 7;
		guessPanel.add(b, gbc);
	}

	public Status showDialog(String title) {

		JOptionPane optionPane = new JOptionPane(guessPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new String[] { "Cancel", "OK" }, "OK");
		optionPane.setBorder(new EmptyBorder(12, 12, 12, 12));

		final JDialog dialog = optionPane.createDialog(m_parent, title);
		dialog.setName("GuessTaxonSets");
		// dialog.setResizable(true);
		dialog.pack();
		updateFields();
		dialog.setVisible(true);
		
		if (optionPane.getValue() == null || !optionPane.getValue().equals("OK")) {
			return Status.canceled;
		}

		if (b1.getModel() == group.getSelection()) {
			String sDelimiter = normalise(textSplitChar.getText());
			switch (m_location) {
			case 0: // "after first",
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "(.*)$";
				break;
			case 1: // "after last",
				pattern = "^.*" + sDelimiter + "(.*)$";
				break;
			case 2: // "before first",
				pattern = "^([^" + sDelimiter + "]+)" + sDelimiter + ".*$";
				break;
			case 3: // "before last"
				pattern = "^(.*)" + sDelimiter + ".*$";
				break;
			}
		}
		if (b2.getModel() == group.getSelection()) {
			String sDelimiter = normalise(textSplitChar2.getText());
			switch (m_splitlocation) {
			case 0: // "1"
				pattern = "^([^" + sDelimiter + "]+)" + ".*$";
				break;
			case 1: // "2"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "([^" + sDelimiter + "]+)" + ".*$";
				break;
			case 2: // "3"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+" + sDelimiter + "([^"
						+ sDelimiter + "]+)" + ".*$";
				break;
			case 3: // "4"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+" + sDelimiter + "[^"
						+ sDelimiter + "]+" + sDelimiter + "([^" + sDelimiter + "]+)" + ".*$";
				break;
			case 4: // "1-2"
				pattern = "^([^" + sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+)" + ".*$";
				break;
			case 5: // "2-3"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "([^" + sDelimiter + "]+" + sDelimiter + "[^"
						+ sDelimiter + "]+)" + ".*$";
				break;
			case 6: // "3-4"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+" + sDelimiter + "([^"
						+ sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+)" + ".*$";
				break;
			case 7: // "1-3"
				pattern = "^([^" + sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+" + sDelimiter + "[^"
						+ sDelimiter + "]+)" + ".*$";
				break;
			case 8: // "2-4"
				pattern = "^[^" + sDelimiter + "]+" + sDelimiter + "([^" + sDelimiter + "]+" + sDelimiter + "[^"
						+ sDelimiter + "]+" + sDelimiter + "[^" + sDelimiter + "]+)" + ".*$";
			}
		}
		if (b3.getModel() == group.getSelection()) {
			pattern = textRegExp.getText();
		}
		if (b4.getModel() == group.getSelection()) {
			try {
				BufferedReader fin = new BufferedReader(new FileReader(txtFile.getText()));
				StringBuffer buf = new StringBuffer();
				// eat up header
				fin.readLine();
				// process data
				while (fin.ready()) {
					String str = fin.readLine();
					str = str.replaceFirst("\t", "=") + ",";
					// only add entries that are non-empty
					if (!str.matches("^\\s+=.*$")) {
						buf.append(str);
					}
				}
				fin.close();
				trait = buf.toString().trim();
				while (trait.endsWith(",")) {
					trait = trait.substring(0, trait.length() - 1).trim();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(m_parent, "Loading trait from file failed:" + e.getMessage());
				return Status.canceled;
			}
			return Status.trait;
		}

		// sanity check
		try {
			pattern.matches(pattern);
		} catch (PatternSyntaxException e) {
			JOptionPane.showMessageDialog(this, "This is not a valid regular expression");
			return Status.canceled;
		}

		if (optionPane.getValue() != null && optionPane.getValue().equals("OK")) {
			System.err.println("Pattern = " + pattern);
			return Status.pattern;
		} else {
			return Status.canceled;
		}
	}

	private String normalise(String sDelimiter) {
		if (sDelimiter.length() == 0) {
			return ".";
		}
		sDelimiter = sDelimiter.substring(0, 1);
		// insert escape chars for anything that might upset a regular expression
		if ("./\"[]()".indexOf(sDelimiter) > -1) {
			sDelimiter = "\\" + sDelimiter;
		}
		return sDelimiter;
	}
	
	public String match(String s) {
        Pattern _pattern = Pattern.compile(pattern);
        Matcher matcher = _pattern.matcher(s);
        if (matcher.find()) {
            String match = matcher.group(1);
            if (chckbxAddFixedValue.isSelected()) {
            	try {
					Double value = Double.parseDouble(match);
					Double addValue = Double.parseDouble(textAddValue.getText());
					if (chckbxUnlessLessThan.isSelected()) {
						Double threshold = Double.parseDouble(textUnlessLessThan.getText());
						Double addValue2 = Double.parseDouble(textThenAdd.getText());
						if (value < threshold) {
							value += addValue2;
						} else {
							value += addValue;
						}
					} else {
						value += addValue;
					}
					return value + "";
				} catch (Exception e) {
					// ignore
				}
            }
            return match;
        }
        return null;
	}
}
