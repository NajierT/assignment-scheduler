package io.github.najiert.assignmentscheduler.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DateSelect {
	/** The ComboBox used to hold the options and selection for the month */
	private JComboBox<String> monthField;
	/** The ComboBox used to hold the options and selection for the day */
	private JComboBox<Integer> dayField;
	/** The ComboBox used to hold the options and selection for the year */
	private JComboBox<Integer> yearField;
	/** The JPanel that holds the date selection components */
	private JPanel dateSelectPanel;
	
	/**
	 * Creates a DateSelect object
	 */
	public DateSelect() {
		createDateSelect();
	}
	
	/**
	 * Creates a date selection panel with JComboBoxes for the month, day, and year fields
	 */
	private void createDateSelect() {
    	dateSelectPanel = new JPanel();
        dateSelectPanel.setLayout(new GridBagLayout());
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        
        GridBagConstraints gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth = 1;
	    gbc.weightx = 0.1;
	    gbc.weighty = 0.6;
	    
	    String[] monthsList = {"January", "February", "March", "April", "May", "June", "July",
	    						"August", "September", "October", "November", "December"};
	    
	    monthField = new JComboBox<>(monthsList);
	    monthField.setSelectedIndex(now.getMonthValue() - 1);
	    dateSelectPanel.add(monthField, gbc);
	    
	    
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.gridwidth = 1;
	    gbc.weightx = 0.1;
	    gbc.weighty = 0.6;
	    
	    Integer[] dayList = new Integer[31];
	    for (int i = 1; i <= dayList.length; i++) {
	    	dayList[i - 1] = i;
	    }
	    dayField = new JComboBox<>(dayList);
	    dayField.setSelectedIndex(now.getDayOfMonth( )- 1);
	    dateSelectPanel.add(dayField, gbc);
	    
	    
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridx = 2;
	    gbc.gridy = 0;
	    gbc.gridwidth = 1;
	    gbc.weightx = 0.1;
	    gbc.weighty = 0.6;
	    
	    Integer[] yearList = new Integer[2100-1970];
	    for (int i = 0; i < yearList.length; i++) {
	    	yearList[i] = 1970 + i;
	    }
	    yearField = new JComboBox<>(yearList);
	    yearField.setSelectedIndex(now.getYear() - 1970);
	    
	    dateSelectPanel.add(yearField, gbc);
	}
	
	public LocalDate parseDateFromFields() {
		LocalDate date = null;
		try {
			int year = (int) yearField.getSelectedItem();
			int month = monthField.getSelectedIndex() + 1;
			int day = (int) dayField.getSelectedItem();
			date = LocalDate.of(year, month, day);
		} catch(DateTimeException ex) {
			JOptionPane.showMessageDialog(dateSelectPanel, "Invalid date");
			dayField.setSelectedIndex(0);
		}
		return date;
	}

	/**
	 * @return the monthField
	 */
	public JComboBox<String> getMonthField() {
		return monthField;
	}

	/**
	 * @return the dayField
	 */
	public JComboBox<Integer> getDayField() {
		return dayField;
	}

	/**
	 * @return the yearField
	 */
	public JComboBox<Integer> getYearField() {
		return yearField;
	}

	/**
	 * @return the dateSelectPanel
	 */
	public JPanel getDateSelectPanel() {
		return dateSelectPanel;
	}
	
}
