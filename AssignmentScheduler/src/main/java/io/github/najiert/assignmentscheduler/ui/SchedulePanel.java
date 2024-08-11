package io.github.najiert.assignmentscheduler.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.najiert.assignmentscheduler.schedule.ScheduleArray;

public class SchedulePanel implements ActionListener, DocumentListener {
	
	public class DayPanel {
		private TimeSelect start;
		private TimeSelect end;
		private JComboBox<String> startSelect;
		private JComboBox<String> endSelect;
		private JTextField startField;
		private JTextField endField;
		
		private JPanel dayPanel;
		
		public DayPanel() {
			createDayPanel();
			
		}
		
		private void createDayPanel() {
			dayPanel = new JPanel();
			dayPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
        	
        	gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0.3;
            gbc.weighty = 0.1;
            start = new TimeSelect();
        	startSelect = start.getTimeSelect();
        	startField = start.getEditor();
        	startSelect.setSelectedItem(DEFAULT_START);
        	dayPanel.add(startSelect, gbc);
        	
        	gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0.7;
            gbc.weighty = 0.1;
            gbc.insets.left = 5;
            end = new TimeSelect();
        	endSelect = end.getTimeSelect();
        	endField = end.getEditor();
        	endSelect.setSelectedItem(DEFAULT_END);
        	dayPanel.add(endSelect, gbc);
		}

		/**
		 * @return the start
		 */
		public TimeSelect getStart() {
			return start;
		}

		/**
		 * @return the end
		 */
		public TimeSelect getEnd() {
			return end;
		}

		/**
		 * @return the startSelect
		 */
		public JComboBox<String> getStartSelect() {
			return startSelect;
		}

		/**
		 * @return the endSelect
		 */
		public JComboBox<String> getEndSelect() {
			return endSelect;
		}

		/**
		 * @return the startField
		 */
		public JTextField getStartField() {
			return startField;
		}

		/**
		 * @return the endField
		 */
		public JTextField getEndField() {
			return endField;
		}

		/**
		 * @return the dayPanel
		 */
		public JPanel getDayPanel() {
			return dayPanel;
		}
		
	}

	private JPanel schedulePanel;
	private ArrayList<DayPanel> dayPanels;
	private String DEFAULT_START = "7:00am";
	private String DEFAULT_END = "10:00pm";
	private JCheckBox sameSchedule;
	
	
	/**
	 * @return the dEFAULT_START
	 */
	public String getDEFAULT_START() {
		return DEFAULT_START;
	}

	/**
	 * @param dEFAULT_START the dEFAULT_START to set
	 */
	public void setDEFAULT_START(String dEFAULT_START) {
		DEFAULT_START = dEFAULT_START;
	}

	/**
	 * @return the dEFAULT_END
	 */
	public String getDEFAULT_END() {
		return DEFAULT_END;
	}

	/**
	 * @param dEFAULT_END the dEFAULT_END to set
	 */
	public void setDEFAULT_END(String dEFAULT_END) {
		DEFAULT_END = dEFAULT_END;
	}

	/**
	 * @return the schedulePanel
	 */
	public JPanel getSchedulePanel() {
		return schedulePanel;
	}

	/**
	 * @return the dayPanels
	 */
	public ArrayList<DayPanel> getDayPanels() {
		return dayPanels;
	}

	public SchedulePanel() {
		createSchedulePanel();
	}
	
	private void createSchedulePanel() {
    	schedulePanel = new JPanel();
        schedulePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // create label
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        
        JLabel sectionLabel = new JLabel("Set Scheduling Periods:");
        schedulePanel.add(sectionLabel, gbc);
        
        // add same schedule each day option
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        
        sameSchedule = new JCheckBox("Same for Each Day");
        sameSchedule.addActionListener(this);
        schedulePanel.add(sameSchedule, gbc);
        
        // create table header
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.125;
        gbc.weighty = 0.1;
        
        JLabel dayLabel = new JLabel("Day of Week");
        dayLabel.setBorder(BorderFactory.createEtchedBorder());
        
        schedulePanel.add(dayLabel, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.75;
        gbc.weighty = 0.1;
        gbc.ipadx = 5;
        JLabel startScheduling = new JLabel("Start Scheduling");
        startScheduling.setBorder(BorderFactory.createEtchedBorder());
        schedulePanel.add(startScheduling, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.125;
        gbc.weighty = 0.1;
        gbc.insets.left = 2;
        JLabel endScheduling = new JLabel("End Scheduling");
        endScheduling.setBorder(BorderFactory.createEtchedBorder());
        schedulePanel.add(endScheduling, gbc);
        
        // create table
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        dayPanels = new ArrayList<DayPanel>();
        for (int i = 0; i < days.length; i++) {
        	gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = i + 2;
            gbc.gridwidth = 1;
            gbc.weightx = .125;
            gbc.weighty = 0.1;
            gbc.insets.left = 0;
        	JLabel day = new JLabel(days[i]);
        	day.getPreferredSize();
        	day.setBorder(BorderFactory.createEtchedBorder());
        	
        	schedulePanel.add(day, gbc);
        	gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 1;
            gbc.gridy = i + 2;
            gbc.gridwidth = 2;
            gbc.weightx = GridBagConstraints.REMAINDER;
            gbc.weighty = 0.1;
        	dayPanels.add(new DayPanel());
        	schedulePanel.add(dayPanels.get(i).getDayPanel(), gbc);
        }
        
        dayPanels.get(0).startField.getDocument().addDocumentListener(this);
        dayPanels.get(0).endField.getDocument().addDocumentListener(this);
        
        
        schedulePanel.setBorder(BorderFactory.createEtchedBorder());
    }
	
	public boolean isFilled() {
		LocalTime start = null;
		LocalTime end = null;
		for (int i = 0; i < dayPanels.size(); i++ ) {
			start = dayPanels.get(i).start.parseTimeFromField(DEFAULT_START);
			end = dayPanels.get(i).end.parseTimeFromField(DEFAULT_END);
			if (start == null || end == null) {
				return false;
			}
			if (start.isAfter(end) || start.equals(end)) {
				JOptionPane.showMessageDialog(dayPanels.get(i).getDayPanel(), "Start time must be before end time");
				dayPanels.get(i).startSelect.setSelectedItem(DEFAULT_START);
				dayPanels.get(i).endSelect.setSelectedItem(DEFAULT_END);
			}
			
			if (start == null || end == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sameSchedule) {
			handleCheckBox();
		}
		
	}
	
	private void handleCheckBox() {
		if (sameSchedule.isSelected()) {
			// Set each day equal to Sunday's schedule
			String sunStart = dayPanels.get(0).getStartField().getText();
			String sunEnd = dayPanels.get(0).getEndField().getText();
			for (int i = 1; i < dayPanels.size(); i++) {
				dayPanels.get(i).startSelect.setSelectedItem(sunStart);
				dayPanels.get(i).startSelect.setEnabled(false);
				dayPanels.get(i).endSelect.setSelectedItem(sunEnd);
				dayPanels.get(i).endSelect.setEnabled(false);
			}
		}
		else {
			for (int i = 1; i < dayPanels.size(); i++) {
				dayPanels.get(i).startSelect.setEnabled(true);
				dayPanels.get(i).endSelect.setEnabled(true);
			}
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == dayPanels.get(0).startField.getDocument()) {
			handleCheckBox();
		}
		if (e.getDocument() == dayPanels.get(0).endField.getDocument()) {
			handleCheckBox();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == dayPanels.get(0).startField.getDocument()) {
			handleCheckBox();
		}
		if (e.getDocument() == dayPanels.get(0).endField.getDocument()) {
			handleCheckBox();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}
}
