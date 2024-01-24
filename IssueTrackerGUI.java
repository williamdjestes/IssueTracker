import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatDarkLaf;

public class IssueTrackerGUI extends JFrame {
	private JTextField titleField;
	private JTextArea descriptionArea;
	private JButton addButton;
	private JTable issueTable;
	private DefaultTableModel tableModel;

	private static final String TEXT_FILE_PATH = "issues.txt";
	private JButton btnNewButton;

	public IssueTrackerGUI() {
		setTitle("Issue Tracker");
		setSize(500, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// Initialize components
		titleField = new JTextField();
		descriptionArea = new JTextArea(12, 20);
		descriptionArea.setLineWrap(true);
		addButton = new JButton("Add Issue");

		// Create table model and tables
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 3; // Allow all cells to be editable
			}
		};
		tableModel.addColumn("Title");
		tableModel.addColumn("Description");
		tableModel.addColumn("Status");

		// Add a TableModelListener to the table model
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int row = e.getFirstRow();
				int column = e.getColumn();

				// Check if a specific cell is edited
				if (column != TableModelEvent.ALL_COLUMNS) {
					saveIssues();
					System.out.println("Cell at row " + row + " and column " + column + " is edited.");
				}
			}
		});

		issueTable = new JTable(tableModel);
		issueTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
		issueTable.setAutoCreateRowSorter(true); // Enable automatic row sorting

		// Set layout manager to GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] { 0, 83, 38, 0 };
		getContentPane().setLayout(gridBagLayout);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		// Add components to the frame
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		getContentPane().add(new JLabel("Title:"), gbc);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.insets = new Insets(0, 0, 5, 0);
		gbc1.gridx = 1;
		gbc1.gridy = 0;
		gbc1.gridwidth = 2;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(titleField, gbc1);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.insets = new Insets(0, 0, 5, 5);
		gbc2.gridx = 0;
		gbc2.gridy = 1;
		gbc2.gridwidth = 1;
		gbc2.fill = GridBagConstraints.NONE;
		getContentPane().add(new JLabel("Description:"), gbc2);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.insets = new Insets(0, 0, 5, 0);
		gbc3.gridx = 1;
		gbc3.gridy = 1;
		gbc3.gridwidth = 2;
		gbc3.fill = GridBagConstraints.BOTH; // Change to BOTH for vertical and horizontal scrolling
		getContentPane().add(new JScrollPane(descriptionArea), gbc3);

		btnNewButton = new JButton("Guide");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String message = "-Issues are saved in issues.txt in the same directory as this JAR file is stored.\n\n"
						+ "-Set the status to 'open' if you want it highlighted green.\n\n"
						+ "-Set the status as 'delete' if you want to delete an issue.";
				JOptionPane.showMessageDialog(issueTable, message, "Guide", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 2;
		getContentPane().add(btnNewButton, gbc_btnNewButton);

		GridBagConstraints gbc4 = new GridBagConstraints();
		gbc4.insets = new Insets(0, 0, 5, 0);
		gbc4.gridx = 1;
		gbc4.gridy = 2;
		gbc4.gridwidth = 2;
		gbc4.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(addButton, gbc4);

		GridBagConstraints gbc5 = new GridBagConstraints();
		gbc5.gridx = 0;
		gbc5.gridy = 3;
		gbc5.gridwidth = 3;
		gbc5.fill = GridBagConstraints.BOTH;
		gbc5.weightx = 1.0;
		gbc5.weighty = 1.0;
		getContentPane().add(new JScrollPane(issueTable), gbc5);

		// Load data from text file at startup
		loadIssues();

		// Add ActionListener to the "Add Issue" button
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addIssue();
			}
		});

		// Save data to text file when the application is closed
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				saveIssues();
			}
		});
	}

	private void addIssue() {
		String title = titleField.getText();
		String description = descriptionArea.getText();

		if (!title.isEmpty() && !description.isEmpty()) {
			// Add the issue to the table
			tableModel.addRow(new Object[] { title, description, "open" });

			// Clear input fields
			titleField.setText("");
			descriptionArea.setText("");
		} else {
			JOptionPane.showMessageDialog(this, "Please enter both title and description.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
		}
		saveIssues(); // Save the issues after adding a new one
	}

	private void loadIssues() {

		try (BufferedReader reader = new BufferedReader(new FileReader(TEXT_FILE_PATH))) {
			String line;

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\t"); // Use tab as the separator
				if (parts.length == 3) {
					tableModel.addRow(parts);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveIssues() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEXT_FILE_PATH))) {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				String title = (String) tableModel.getValueAt(i, 0);
				String description = (String) tableModel.getValueAt(i, 1);
				String status = (String) tableModel.getValueAt(i, 2);

				// Check if the status is "delete" before writing to the file
				if (!status.equals("delete")) {
					writer.write(title + "\t" + description + "\t" + status); // Use tab as the separator
					writer.newLine();
					System.out.println("not delete: " + status);

				} else {
					System.out.println("yes delete:");
					tableModel.removeRow(i);
				}
			}

			// Do not clear the table and reload issues here
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatDarkLaf());
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}

				new IssueTrackerGUI().setVisible(true);

			}
		});
	}

	// Custom cell renderer for the third column
	private class StatusCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			// Check the cell value and set background color accordingly
			String status = (String) value;
			if ("open".equals(status)) {
				setBackground(new Color(0, 128, 0)); // Dark green
			} else {
				setBackground(Color.BLACK);
			}

			return this;
		}
	}
}
