package io.github.najiert.assignmentscheduler.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import io.github.najiert.assignmentscheduler.service.*;
import io.github.najiert.assignmentscheduler.manager.*;
import io.github.najiert.assignmentscheduler.assignment.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AssignmentSchedulerUI extends JFrame implements ActionListener {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
    private ArrayList<JCheckBox> calendarsSelectList;
    private JComboBox<String> addToCalendar;
    private JComboBox<String> timeZoneSelect;
	private DateSelect startDateSelect;
	private TimeSelect startTimeSelect;
	private SchedulePanel schedulePanel;
	private AssignmentsGridPanel assignmentsGridPanel;
	private DurationSelect breakPeriodSelect;
    private DurationSelect minDurationSelect;
    private JButton submitButton;
	private String DEFAULT_CALENDAR = "(None Selected)";
	private String DEFAULT_STARTTIME = "Enter or Select a Time:";
	private DurationSelect maxDurationSelect;
	private LocalDateTime startAt;
	private String timeZone;
	private Calendar c;
	private ArrayList<CalendarListEntry> eC;
	private JButton done;
	private JButton reset;
	private JDialog report;
	private AssignmentScheduler scheduler;
	private String calendarID;
	private JProgressBar progressBar;
	private JDialog progressFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new AssignmentSchedulerUI().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void initialize() throws GeneralSecurityException, IOException, InterruptedException {
        frame = new JFrame("Google Calendar App");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0.1;
        mainPanel.add(createTopPanel(), c);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0.7;
        mainPanel.add(createMiddlePanel(), c);
        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0.2;
        mainPanel.add(createBottomPanel(), c);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
        
    }
    

    private JPanel createTopPanel() throws GeneralSecurityException, IOException, InterruptedException {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 2, 10, 0));

        // Get calendar list data
        c = CalendarService.getUserCalendar();
        eC = CalendarService.getCalendarList(c);
        String[] calendarNames = new String[eC.size() + 1];
        
        //Adds section to choose which calendars to schedule around
        topPanel.add(createScheduleAroundPanel(calendarNames));
        
        // create section to set the remaining scheduler parameters
        JPanel schedulerParams = new JPanel();
        schedulerParams.setLayout(new GridLayout(6, 0));
        
        // allows user to select the calendar to add the created events to
        calendarNames[calendarNames.length - 1] = DEFAULT_CALENDAR;
        addToCalendar = new JComboBox<>(calendarNames);
        addToCalendar.setSelectedIndex(calendarNames.length - 1);
        
        schedulerParams.add(new JLabel("Select Calendar to Add to:"));
        schedulerParams.add(addToCalendar);
        
        // time zone selection
        schedulerParams.add(new JLabel("Select Time Zone:"));
        // Store Time Zone IDs into String array
        String[] timeZoneIds = new String[ZoneId.getAvailableZoneIds().size() + 1];
        int idx = 1;
        for (String id : ZoneId.getAvailableZoneIds()) {
        	timeZoneIds[idx] = id;
        	idx++;
        }
        // used to make the system's time zone as the default selection
        timeZoneIds[0]= "0";
        Arrays.sort(timeZoneIds);
        timeZoneIds[0] = "(Default) System Default";
        timeZoneSelect = new JComboBox<>(timeZoneIds);
        timeZoneSelect.setSelectedIndex(0);
        schedulerParams.add(timeZoneSelect);
        
        // section to choose the date an time to start scheduling at
        schedulerParams.add(new JLabel("Start Scheduling at:"));
        schedulerParams.add(createDateAndTimePanel());
        
        topPanel.add(schedulerParams);
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        return topPanel;
    }
    
    private JPanel createScheduleAroundPanel(String[] names) {
    	
    	//Create checkbox list of calendars to schedule around
        JPanel scheduleAroundSelect = new JPanel();
        scheduleAroundSelect.setLayout(new BorderLayout());
    	JPanel calendarsSelectOptions = new JPanel();
    	
    	// store each calendar name in a checkbox
    	calendarsSelectList = new ArrayList<JCheckBox>();
    	calendarsSelectOptions.setLayout(new GridLayout(eC.size(), 0));
        for (int i = 0; i < eC.size(); i++) {
        	String title = eC.get(i).getSummary();
        	names[i] = title;
        	JCheckBox checkbox = new JCheckBox(title);
        	calendarsSelectList.add(checkbox);
        	calendarsSelectOptions.add(checkbox);
        }
        JScrollPane calendarScrollPane = new JScrollPane(calendarsSelectOptions);

        scheduleAroundSelect.add(new JLabel("Select Calendars to Schedule Around:"), BorderLayout.NORTH);
        scheduleAroundSelect.add(calendarScrollPane, BorderLayout.CENTER);
        
        return scheduleAroundSelect;
    }
    
    private JPanel createDateAndTimePanel() {
    	
    	JPanel dateAndTimePanel = new JPanel();
        dateAndTimePanel.setLayout(new GridLayout(0, 2, 10, 0));
        // date selection
        startDateSelect = new DateSelect();
        dateAndTimePanel.add(startDateSelect.getDateSelectPanel());
        // time selection
        startTimeSelect = new TimeSelect();
        startTimeSelect.getTimeSelect().setSelectedItem(DEFAULT_STARTTIME);
        dateAndTimePanel.add(startTimeSelect.getTimeSelectPanel());
        return dateAndTimePanel;
    }

    private JPanel createMiddlePanel() {
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 1;
        // left panel
        schedulePanel = new SchedulePanel();
        middlePanel.add(schedulePanel.getSchedulePanel(), gbc);
        
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = .9;
        gbc.weighty = 1;
        
        // right panel
        assignmentsGridPanel = new AssignmentsGridPanel();
        middlePanel.add(assignmentsGridPanel.getPanel(), gbc);
        
        return middlePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(3, 2, 10, 0));

        // Assignment fields
        bottomPanel.add(new JLabel("Break Period:"));
        bottomPanel.add(new JLabel("Minimum Block Duration:"));
        bottomPanel.add(new JLabel("Maximum Block Duration:"));
        breakPeriodSelect = new DurationSelect();
        breakPeriodSelect.getPanel().setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(breakPeriodSelect.getPanel());

        
        minDurationSelect = new DurationSelect();
        minDurationSelect.getPanel().setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(minDurationSelect.getPanel());

        
        maxDurationSelect = new DurationSelect();
        maxDurationSelect.getPanel().setBorder(BorderFactory.createEtchedBorder());
        bottomPanel.add(maxDurationSelect.getPanel());
        // empty JLabel for spacing
        bottomPanel.add(new JLabel());

        submitButton = new JButton("Submit");
        submitButton.setFocusable(false);
        submitButton.addActionListener(this);
        bottomPanel.add(submitButton);

        return bottomPanel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == submitButton) {
	        try {
				handleSubmitButton();
			} catch (IOException e1) {
				e1.printStackTrace();
				unexpectedErrorExit();
			}
    	}
    	if (e.getSource() == done) {
    		report.setVisible(false);
    	}
    	if (e.getSource() == reset) {
    		int result = JOptionPane.showConfirmDialog(report, "Are you sure? This will delete any events that share a\n"
    				+ "name with your assignment(s) from your calendar.", "Confirm", JOptionPane.YES_NO_OPTION);
    		if (result == JOptionPane.YES_OPTION) {
    			try {
					scheduler.clearAssignments(calendarID);
					report.setVisible(false);
				} catch (IOException e1) {
					e1.printStackTrace();
					unexpectedErrorExit();
				}
    		}
    	}
    }
    
    /**
     * Checks if any of the calendar options are selected
     * @return
     */
    private boolean calendarsSelectIsFilled() {
    	for (int i = 0; i < calendarsSelectList.size(); i++) {
    		if (calendarsSelectList.get(i).isSelected()) {
    			return true;
    		}
    	}
    	JOptionPane.showMessageDialog(frame, "Must select calendar(s) to schedule around");
    	return false;
    }
    
    private boolean addToCalendarIsFilled() {
    	String selectedCalendar = (String) addToCalendar.getSelectedItem();
    	if (selectedCalendar.equals(DEFAULT_CALENDAR)) {
    		JOptionPane.showMessageDialog(frame, "Must select a calendar to add to");
    		return false;
    	}
    	return true;
    }
    
    private void getStartAt() {
    	LocalTime time = startTimeSelect.parseTimeFromField(DEFAULT_STARTTIME);
		LocalDate date = startDateSelect.parseDateFromFields();
		startAt = null;
		try {
			if (date != null && time != null) {
				startAt = LocalDateTime.of(date, time);
				timeZone = (String) timeZoneSelect.getSelectedItem();
				if (timeZoneSelect.getSelectedIndex() == 0) {
					timeZone = ZoneId.systemDefault().getId();
				}
				// create with time zone
				Instant instant = startAt.atZone(ZoneId.of(timeZone)).toInstant();
				startAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			}
		} catch(DateTimeException ex) {
			JOptionPane.showMessageDialog(startDateSelect.getDateSelectPanel(), "Invalid date");
		}
    }
    
    private boolean bottomPanelIsFilled() {
    	return breakPeriodSelect.getDurationInMins() != -1 && minDurationSelect.getDurationInMins() != -1
    			&& maxDurationSelect.getDurationInMins() != -1;
    }
    
    private void addAssignments() {
    	for (int i = 0; i < assignmentsGridPanel.getAssignmentPanels().size(); i++) {
    		AssignmentPanel ap = assignmentsGridPanel.getAssignmentPanels().get(i);
    		// adjust timeZone
    		Instant instant = ap.getDeadline().atZone(ZoneId.of(timeZone)).toInstant();
    		LocalDateTime deadline = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    		scheduler.addAssignmentToList(ap.getName(), deadline, ap.getTimeRequired(), ap.getColor());
    	}
    }
    
    private void setSchedulingPeriods() {
    	// monday - saturday
    	for (int i = 1; i < schedulePanel.getDayPanels().size(); i++) {
    		LocalTime start = schedulePanel.getDayPanels().get(i).getStart().parseTimeFromField(DEFAULT_STARTTIME);
    		Instant instant = startAt.with(start).atZone(ZoneId.of(timeZone)).toInstant();
    		LocalDateTime adjusted = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    		int offset = (int) startAt.with(start).until(adjusted, ChronoUnit.HOURS);
    		start = start.plusHours(offset);
    		
    		LocalTime end = schedulePanel.getDayPanels().get(i).getEnd().parseTimeFromField(DEFAULT_STARTTIME);
    		instant = startAt.with(end).atZone(ZoneId.of(timeZone)).toInstant();
    		adjusted = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    		offset = (int) startAt.with(end).until(adjusted, ChronoUnit.HOURS);
    		end = end.plusHours(offset);
    		scheduler.setSchedulingPeriod(DayOfWeek.of(i), start, end);
    	}
    	// for sunday
    	LocalTime start = schedulePanel.getDayPanels().get(0).getStart().parseTimeFromField(DEFAULT_STARTTIME);
		Instant instant = startAt.with(start).atZone(ZoneId.of(timeZone)).toInstant();
		LocalDateTime adjusted = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		int offset = (int) startAt.with(start).until(adjusted, ChronoUnit.HOURS);
		start = start.plusHours(offset);
		
		LocalTime end = schedulePanel.getDayPanels().get(0).getEnd().parseTimeFromField(DEFAULT_STARTTIME);
		instant = startAt.with(end).atZone(ZoneId.of(timeZone)).toInstant();
		adjusted = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		offset = (int) startAt.with(end).until(adjusted, ChronoUnit.HOURS);
		end = end.plusHours(offset);
		scheduler.setSchedulingPeriod(DayOfWeek.SUNDAY, start, end);
    }
    
    private void handleSubmitButton() throws IOException {
    	getStartAt();
    	if (schedulePanel.isFilled() && assignmentsGridPanel.isFilled() && calendarsSelectIsFilled()
    		&& addToCalendarIsFilled() && bottomPanelIsFilled() && startAt != null) {
    		scheduler = new AssignmentScheduler(c, breakPeriodSelect.getDurationInMins(),
    														 startAt, minDurationSelect.getDurationInMins(),
    														 maxDurationSelect.getDurationInMins());
    		addAssignments();
    		setSchedulingPeriods();
    		calendarID = eC.get(addToCalendar.getSelectedIndex()).getId();
    		for (int i = 0; i < eC.size(); i++) {
    			if (calendarsSelectList.get(i).isSelected()) {
    				scheduler.addToEventCalendars(eC.get(i));
    			}
    		}
    		createProgressBarPanel();
    		startThread();
    		
        }
    }
    
    private void createProgressBarPanel() {
    	progressFrame = new JDialog(frame, "Progress", false);
    	progressFrame.setSize(300, 75);
    	progressBar = new JProgressBar(0, 100);
		progressBar.setSize(300, 50);
		progressBar.setStringPainted(true);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BorderLayout());
		progressPanel.add(progressBar, BorderLayout.CENTER);
		
		progressFrame.add(progressPanel);
		progressFrame.setLocationRelativeTo(frame);
		
    }
    
    private JDialog createReportPanel() {
    	JDialog report = new JDialog(frame, "Report", true);
    	report.setSize(700, 300);
    	JPanel reportGrid = new JPanel();
    	
    	reportGrid.setLayout(new GridLayout(scheduler.getAssignmentList().size() + 1, 0));
    	reportGrid.add(new JLabel("Summary: ", JLabel.CENTER));
    	for (Assignment a : scheduler.getAssignmentList()) {
			double hoursAllocated = a.getTimeAllocated() / 60.0;
			double initHoursRequired = a.getInitTimeRequired() / 60.0;
			String reportString = String.format(a.getName() + ": %.2f / %.2f hours", hoursAllocated, initHoursRequired);
			reportGrid.add(new JLabel(reportString, JLabel.CENTER));
		}
    	JScrollPane reportScroller = new JScrollPane(reportGrid);
    	reportScroller.setBorder(BorderFactory.createEtchedBorder());
    	JPanel reportPanel = new JPanel();
    	reportPanel.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1;
    	gbc.weighty = 0.6;
    	
    	reportPanel.add(reportScroller, gbc);
    	JPanel messageGrid = new JPanel();
    	messageGrid.setLayout(new GridLayout(3, 0));
    	messageGrid.add(new JLabel("Please refresh your Google Calendar. If you are not satisfied with the results", JLabel.CENTER));
    	messageGrid.add(new JLabel("press \"Reset\" to clear the new events from your calendar and retry with new parameters", JLabel.CENTER));
    	messageGrid.add(new JLabel("Otherwise, click \"Done\".", JLabel.CENTER));
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1;
    	gbc.weighty = 0.3;
    	reportPanel.add(messageGrid, gbc);
    	
    	JPanel buttonPanel = new JPanel();
    	buttonPanel.setLayout(new GridLayout(0,2, 5, 0));
    	done = new JButton("Done");
    	done.setFocusable(false);
    	done.addActionListener(this);
    	reset = new JButton("Reset");
    	reset.setFocusable(false);
    	reset.addActionListener(this);
    	buttonPanel.add(done);
    	buttonPanel.add(reset);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 2;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1;
    	gbc.weighty = 0.1;
    	reportPanel.add(buttonPanel, gbc);
    	report.add(reportPanel);
    	report.setLocationRelativeTo(frame);
    	
    	return report;
    }
    
    private void unexpectedErrorExit() {
    	JOptionPane.showMessageDialog(frame, "Unexpected Error. Please try with new parameters. Exiting");
    	try {
			Thread.sleep(3000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    	frame.dispose();
    }
    
    private void startThread() {
    	SwingWorker<Void, Integer> sw = new SwingWorker<Void, Integer>() {
    		@Override
    		protected Void doInBackground() throws Exception {
    			progressFrame.setVisible(true);
    			Random random = new Random();
    	    	int upperLoopBound = 99;
    	    	int lowerLoopBound = 25;
    	    	int loopBound = random.nextInt(upperLoopBound - lowerLoopBound) + lowerLoopBound;
    	    	int i = 0;
    	    	while (i <= loopBound) {
    	    		// send to process method
    	    		publish(i);
    	    		int lowerBound = 2;
    	    		int upperBound = 16;
    	    		int rand = random.nextInt(upperBound - lowerBound) + lowerBound;
    	    		i += rand;
    	    		
    	    		try {
    	    			Thread.sleep(50);
    	    		} catch (Exception ex) {
    	    			ex.printStackTrace();
    	    			unexpectedErrorExit();
    	    		}
    	    	}
    	    	return null;
    		}
    		
    		@Override
    		protected void process(List<Integer> chunks) {
    			for (Integer i: chunks) {
    				progressBar.setValue(i);
    			}
    		}
    		
    		@Override
    	    protected void done() {
    			
    			try {
					scheduler.setAssignmentSchedules(calendarID);
				} catch (IOException e) {
					e.printStackTrace();
					unexpectedErrorExit();
				}
    			endThread();
    	    }
    	};
    	
    	sw.execute();
    }
    
    private void endThread() {
    	SwingWorker<Void, Integer> sw = new SwingWorker<Void, Integer>() {
    		@Override
    		protected Void doInBackground() throws Exception {
    			Random random = new Random();
    	    	int i = progressBar.getValue();
    	    	while (i <= 100) {
    	    		// send to process method
    	    		publish(i);
    	    		int lowerBound = 1;
    	    		int upperBound = 20;
    	    		int rand = random.nextInt(upperBound - lowerBound) + lowerBound;
    	    		i += rand;
    	    		try {
    	    			lowerBound = 25;
    	    			upperBound = 500;
    	    			rand = random.nextInt(upperBound - lowerBound) + lowerBound;
    	    			Thread.sleep(rand);
    	    		} catch (Exception ex) {
    	    			ex.printStackTrace();
    	    			unexpectedErrorExit();
    	    		}
    	    	}
    	    	progressBar.setValue(100);
    	    	return null;
    		}
    		
    		@Override
    		protected void process(List<Integer> chunks) {
    			for (Integer i: chunks) {
    				progressBar.setValue(i);
    			}
    		}
    		
    		@Override
    	    protected void done() {
    			progressFrame.setVisible(false);
    			report = createReportPanel();
        		report.setVisible(true);
    	    }
    	};
    	
    	sw.execute();
    }
}