package io.github.najiert.assignmentscheduler.ui;

import java.awt.BorderLayout;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TimeSelect {
	private JPanel timeSelectPanel;
	private JTextField editor;
	private JComboBox<String> timeSelect;
	
	public TimeSelect() {
		createTimeSelect();
	}
	
	private void createTimeSelect() {
    	timeSelectPanel = new JPanel();
        timeSelectPanel.setLayout(new BorderLayout());
        String[] timeList = new String[92];
        int hour = 1;
        int minute = 0;
        String meridiem = "am";
        String extraZero = "0";
        for (int i = 0; i < timeList.length; i++) {
	        if (minute == 0) {
	        	extraZero = "0";
	        }
	        else {
	        	extraZero = "";
	        }
	        if (hour == 12) {
	        	meridiem = "pm";
	        }
	        timeList[i] = hour + ":" + minute + extraZero + meridiem;
	        minute += 15;
	        if (minute > 45) {
	        	hour++;
	        	if (hour > 12) {
	        		hour = 1;
	        	}
	        	minute = 0;
	        }
        }
        
        timeSelect = new JComboBox<>(timeList);
        timeSelect.setEditable(true);
        editor = (JTextField) timeSelect.getEditor().getEditorComponent();
        timeSelectPanel.add(timeSelect);
    }
	
	public LocalTime parseTimeFromField(String default_time) {
		LocalTime time = null;
		try {
			DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
	                .parseCaseInsensitive()
	                .appendPattern("h:mma")
	                .toFormatter();
			time = LocalTime.parse(editor.getText(), timeFormatter);
		} catch(DateTimeException ex) {
			// try H:mm format
			try {
				DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
		                .parseCaseInsensitive()
		                .appendPattern("H:mm")
		                .toFormatter();;
				time = LocalTime.parse(editor.getText(), timeFormatter);
			} catch(DateTimeException ex2) {
				JOptionPane.showMessageDialog(timeSelectPanel, "Invalid time");
				timeSelect.setSelectedItem(default_time);
			}
		}
		return time;
	}

	/**
	 * @return the timeSelectPanel
	 */
	public JPanel getTimeSelectPanel() {
		return timeSelectPanel;
	}
	
	public JTextField getEditor() {
		return editor;
	}

	/**
	 * @return the timeSelect
	 */
	public JComboBox<String> getTimeSelect() {
		return timeSelect;
	}
}
