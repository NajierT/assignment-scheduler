package io.github.najiert.assignmentscheduler.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import io.github.najiert.assignmentscheduler.assignment.*;

/**
 * Handles the creation of a JPanel used to collect assignment information
 * @author Najier Torrence
 *
 */
public class AssignmentPanel{
	/** The JPanel used to hold the input fields for the assignment */
	private JPanel assignmentPanel;
	/** The DateSelect object used for the date selection */
	private DateSelect dateSelect;
	/** The TimeSelect object used for the time selection */
	private TimeSelect timeSelect;
	/** The DurationSelect object used for the duration selection */
	private DurationSelect durationSelect;
	/** The text field used to hold the name of the assignment */
	private JTextField nameField;
	/** The ComboBox used to hold the options and selection for the deadline year */
	private JComboBox<Integer> deadlineYear;
	/** The ComboBox used to hold the options and selection for the time of the deadline */
	private JComboBox<String> deadlineTime;
	/** The ComboBox use to hold the options for colors */
	private JComboBox<Icon> colors;
	/** The name value */
	private String name;
	/** The deadline value */
	private LocalDateTime deadline;
	/** The hour value */
	private int timeRequired;
	/** The color id */
	private String color;
	/** The default time to set the deadline to */
	private String DEFAULT_TIME = "11:55pm";
	/** The default color index to set */
	private int DEFAULT_COLOR = 4;
	
	
	public AssignmentPanel() {
		createAssignmentPanel();
		timeRequired = -1;
		name = null;
		deadline = null;
	}
	
	public JPanel getPanel() {
		return assignmentPanel;
	}
	
	/**
	 * Returns true if the name, hour, min, and dueDate fields are not null
	 * @return
	 */
	public boolean isFilled() {
		// validate inputs
		handleNameField();
		timeRequired = durationSelect.getDurationInMins();
		handleDeadlineFields();
		return (name != null) && (timeRequired >= 0) && (deadline != null);
	}
	
	
	
	/**
	 * @return the dEFAULT_COLOR
	 */
	public int getDEFAULT_COLOR() {
		return DEFAULT_COLOR;
	}

	/**
	 * @param dEFAULT_COLOR the dEFAULT_COLOR to set
	 */
	public void setDEFAULT_COLOR(int color) {
		DEFAULT_COLOR = color;
	}

	/**
	 * @return the deadlineYear
	 */
	public JComboBox<Integer> getDeadlineYear() {
		return deadlineYear;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the deadline
	 */
	public LocalDateTime getDeadline() {
		return deadline;
	}

	/**
	 * @return the hour
	 */
	public int getTimeRequired() {
		return timeRequired;
	}


	/**
	 * @return the color
	 */
	public String getColor() {
		int colorIdValue = colors.getSelectedIndex() + 1;
		color = colorIdValue + "";
		return color;
	}

	/**
	 * @return the dEFAULT_TIME
	 */
	public String getDEFAULT_TIME() {
		return DEFAULT_TIME;
	}
	
	public void setDEFAULT_TIME(String time) {
		DEFAULT_TIME = time;
		
	}

	private void createAssignmentPanel() {
    	assignmentPanel = new JPanel();
    	assignmentPanel.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
        
        // Name label and fields
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = .2;
        gbc.weighty = 0.5;
        
        JLabel nameLabel = new JLabel("Assignment Name:");
        assignmentPanel.add(nameLabel, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        
        nameField = new JTextField();
        assignmentPanel.add(nameField, gbc);
        
        // Deadline label and fields
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = .2;
        gbc.weighty = 0.5;
        
        
        JLabel deadlineLabel = new JLabel("Deadline:");
        assignmentPanel.add(deadlineLabel, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        
        dateSelect = new DateSelect();
        deadlineYear = dateSelect.getYearField();
        assignmentPanel.add(dateSelect.getDateSelectPanel(), gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        gbc.insets.left = 5;
        
        timeSelect = new TimeSelect();
        deadlineTime = timeSelect.getTimeSelect();
        deadlineTime.setSelectedItem(DEFAULT_TIME);
        assignmentPanel.add(timeSelect.getTimeSelectPanel(), gbc);
        
        // Time Required Label and Fields
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = .2;
        gbc.weighty = 0.5;
        gbc.insets.left = 0;
        
        JLabel timeRequiredLabel = new JLabel("Time Required:");
        assignmentPanel.add(timeRequiredLabel, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        gbc.insets.left = 5;
        
        durationSelect = new DurationSelect();
        JPanel timeRequiredPanel = durationSelect.getPanel();
        assignmentPanel.add(timeRequiredPanel, gbc);
        
        // Color label and fields
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.5;
        
        JLabel colorLabel = new JLabel("Color:");
        assignmentPanel.add(colorLabel, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        gbc.weighty = 0.5;
        
        colors = new JComboBox<Icon>();
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_1.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_2.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_3.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_4.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_5.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_6.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_7.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_8.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_9.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_10.png")));
        colors.addItem(new ImageIcon(getClass().getResource("/images/colorID_11.png")));
        
        colors.setSelectedIndex(DEFAULT_COLOR);
        assignmentPanel.add(colors, gbc);
        assignmentPanel.setBorder(BorderFactory.createEtchedBorder());
    }
	
	private void handleNameField() {
		name = nameField.getText();
		if (name == null || name.isBlank() || name.isEmpty()) {
			JOptionPane.showMessageDialog(assignmentPanel, "Must enter name");
		}
	}
	
	private void handleDeadlineFields() {
		LocalTime time = timeSelect.parseTimeFromField(DEFAULT_TIME);
		LocalDate date = dateSelect.parseDateFromFields();
		try {
			if (date != null && time != null) {
				deadline = LocalDateTime.of(date, time);
			}
		} catch(DateTimeException ex) {
			JOptionPane.showMessageDialog(assignmentPanel, "Invalid date");
		}
	}
}
