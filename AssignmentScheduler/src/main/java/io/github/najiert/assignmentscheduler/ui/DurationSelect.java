package io.github.najiert.assignmentscheduler.ui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Creates and panel to collect duration in hours and minutes
 * @author Najier Torrence
 *
 */
public class DurationSelect {
	private JTextField hourSelect;
	private JTextField minSelect;
	private JPanel durationSelect;
	
	
	public DurationSelect() {
		createDurationSelect();
	}
	
	/**
	 * Creates the DurationSelect panel with text fields
	 * labled hr and min
	 */
	private void createDurationSelect() {
    	durationSelect = new JPanel();
    	durationSelect.setLayout(new GridLayout(0,4));
    	
    	hourSelect = new JTextField();
    	JLabel hourLabel = new JLabel("hrs");
    	
    	minSelect = new JTextField();
    	JLabel minLabel = new JLabel("mins");
    	
    	durationSelect.add(hourSelect);
    	durationSelect.add(hourLabel);
    	durationSelect.add(minSelect);
    	durationSelect.add(minLabel);
    }
	public int parseHourFromField() {
		int hour = -1;
		try {
			hour = Integer.parseInt(hourSelect.getText());
			if (hour < 0 ) {
				JOptionPane.showMessageDialog(durationSelect, "Hour cannot be negative");
				hourSelect.setText(null);
				hour = -1;
			}
			if (hour == 0 && this.parseMinuteFromField() <= 0) {
				JOptionPane.showMessageDialog(durationSelect, "Time must be at least 1 min");
				hourSelect.setText(null);
				hour = -1;
			}
		} catch(NumberFormatException ex) {
			if (!(hourSelect.getText().isBlank() || hourSelect.getText().isEmpty())){
				JOptionPane.showMessageDialog(durationSelect, "Must enter a number greater than or equal to 0 for hour");
				hourSelect.setText(null);
			}
			else if (minSelect.getText().isBlank() || minSelect.getText().isEmpty() || this.parseMinuteFromField() <= 0 ) {
				JOptionPane.showMessageDialog(durationSelect, "Time must be at least 1 min");
				hourSelect.setText(null);
				hour = -1;
			}
			else {
				hour = 0;
			}
		}
		return hour;
	}
	
	public int parseMinuteFromField() {
		int minute = -1;
		try {
			minute = Integer.parseInt(minSelect.getText());
			if (minute < 0 ) {
				JOptionPane.showMessageDialog(durationSelect, "Minute cannot be negative");
				minSelect.setText(null);
				minute = -1;
			}
			if (minute == 0 && this.parseHourFromField() <= 0) {
				JOptionPane.showMessageDialog(durationSelect, "Time must be at least 1 min");
				minSelect.setText(null);
				minute = -1;
			}
		} catch(NumberFormatException ex) {
			if (!(minSelect.getText().isBlank() || minSelect.getText().isEmpty())){
				JOptionPane.showMessageDialog(durationSelect, "Must enter a number greater than or equal to 0 for min");
				minSelect.setText(null);
				minute = -1;
			}
			else if (hourSelect.getText().isBlank() || hourSelect.getText().isEmpty() || this.parseHourFromField() <= 0) {
				JOptionPane.showMessageDialog(durationSelect, "Time must be at least 1 min");
				minSelect.setText(null);
				minute = -1;
			}
			else {
				minute = 0;
			}
		}
		return minute;
	}
	
	public int getDurationInMins() {
		int minute = this.parseMinuteFromField();
		int hour = this.parseHourFromField();
		
		if (minute == -1 || hour == -1) {
			return -1;
		}
		return minute + (hour * 60);
	}

	/**
	 * Returns the durationSelect panel
	 * @return the durationSelect JPanel
	 */
	public JPanel getPanel() {
		return durationSelect;
	}
	
	public JTextField getHourSelect() {
		return hourSelect;
	}
	
	public JTextField getMinSelect() {
		return minSelect;
	}

	
	
}
