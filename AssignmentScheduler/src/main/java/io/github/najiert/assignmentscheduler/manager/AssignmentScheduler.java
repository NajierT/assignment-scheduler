package io.github.najiert.assignmentscheduler.manager;
import java.io.IOException;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import io.github.najiert.assignmentscheduler.assignment.Assignment;
import io.github.najiert.assignmentscheduler.schedule.ScheduleArray;


/**
 * Provides the methods used to create Google Calendar events based on a
 * user's calendar(s)and a list of Assignment objects
 * @author Najier Torrence
 *
 */
public class AssignmentScheduler {
	/** Holds the list of Assignment objects used to create each event */
	private ArrayList<Assignment> assignmentList;
	/** User's calendar service to collect calendars from*/
	private Calendar service;
	/** User's calendars to schedule the working blocks around */
	private ArrayList<CalendarListEntry> eventCalendars;
	/** Holds the times to start and end scheduling for each day */
	private Map<DayOfWeek, LocalTime[]> schedulingTimes;
	/** Holds the time slots for the free time from the User's calendar(s) */
	private ScheduleArray schedule;
	/** The minimum amount of time between one event and the next in minutes*/
	private int breakPeriod;
	/** Date and time to start scheduling at */
	private LocalDateTime startScheduling;
	/** Amount of free time from start scheduling to last deadline in minutes */
	private double totalFreeTime;
	/** Total amount of time required to complete all assigments */
	private double totalTimeRequired;
	/** Minimum amount of time to schedule a working period in minutes */
	private int minWorkPeriod;
	/** Maximum amount of time to schedule a working period in minutes */
	private int maxWorkPeriod;
	/** The number of minutes in a day */
	private final int MINUTES_IN_DAY = 24 * 60;
	/** The weight of the base priority score used in the priority scores calculation*/
	private final double BASE_WEIGHT = 0.3;
	/** the weight of the urgency score used in the priority scores calculation */
	private final double URGENCY_WEIGHT = 100;
	
	/**
	 * Creates an AssignmentScheduler object with a specified start point, minimum work period, and maximum
	 * work period
	 * @param eventCalendar the calendar service to create the events to
	 * @param breakPeriod the minimum amount of time desired between one event and the next
	 * @param startScheduling the DateTime to start the scheduling process at
	 * @param minWorkPeriod the minimum amount of time to schedule a working period in minutes
	 * @param maxWorkPeriod the maximum amount of time to schedule a working period in minutes
	 */
	public AssignmentScheduler(Calendar eventCalendar, int breakPeriod, LocalDateTime startScheduling, int minWorkPeriod, int maxWorkPeriod) {
		setEventCalendar(eventCalendar);
		schedulingTimes = new HashMap<>();
		assignmentList = new ArrayList<Assignment>();
		eventCalendars =  new ArrayList<CalendarListEntry>();
		setStartScheduling(startScheduling);
		schedule = new ScheduleArray(breakPeriod);
		totalFreeTime = 0;
		totalTimeRequired = 0;
		//TODO Make setter with error checking
		this.breakPeriod = breakPeriod;
		this.minWorkPeriod = minWorkPeriod;
		this.maxWorkPeriod = maxWorkPeriod;
	}
	
	/**
	 * Creates an AssignmentScheduler object with a specified start point, minimum work period
	 * @param eventCalendar the calendar service to create the events to
	 * @param breakPeriod the minimum amount of time desired between one event and the next
	 * @param startScheduling the DateTime to start the scheduling process at
	 * @param minWorkPeriod the minimum amount of time to schedule a working period in minutes
	 */
	public AssignmentScheduler(Calendar eventCalendar, int breakPeriod, LocalDateTime startScheduling, int minWorkPeriod) {
		this(eventCalendar, breakPeriod, startScheduling, minWorkPeriod, Integer.MAX_VALUE);
	}
	
	/**
	 * Creates an AssignmentScheduler object with the specified calendar and break period
	 * @param eventCalendar the calendar service to create the events to
	 * @param breakPeriod the minimum amount of time desired between one event and the next
	 */
	public AssignmentScheduler(Calendar eventCalendar, int breakPeriod) {
		this(eventCalendar, breakPeriod, LocalDateTime.now(), 0, Integer.MAX_VALUE);
	}
	
	/**
	 * Creates and adds an assignment scheduler to the assignmentList. This increases the
	 * totalTimeRequired field by the given timeRequired
	 * @param name the name of the assignment
	 * @param deadline the deadline of the assignment
	 * @param timeRequired the time required for the assignment in minutes
	 * @param colorID the color ID of the assignment
	 */
	public void addAssignmentToList(String name, LocalDateTime deadline, int timeRequired, String colorID) {
		assignmentList.add(new Assignment(name, deadline, timeRequired, colorID));
		totalTimeRequired += timeRequired;
	}
	
	/**
	 * Sets the time to start looking for free time
	 * @param day the day of the week to set the scheduling time for
	 * @param startScheduling the time to begin scheduling for the day
	 * @param endScheduling the time to end scheduling for the day
	 * @throws IllegalArgumentException if scheduling times intersect intersect
	 */
	public void setSchedulingPeriod(DayOfWeek day, LocalTime startScheduling, LocalTime endScheduling) {
		if (startScheduling.equals(endScheduling)){
			throw new IllegalArgumentException("Cannot start and end scheduling at the same time.");
		}
		if (startScheduling.isAfter(endScheduling)){
			throw new IllegalArgumentException("Cannot start Scheduling after end scheduling");
		}
		if (startScheduling == null || endScheduling == null) {
			throw new IllegalArgumentException("Start an end scheduling cannot be null");
		}
		
		LocalTime[] times = {startScheduling, endScheduling};
		schedulingTimes.put(day, times);
	}
	
	/**
	 * Get the startScheduling for the given day of week
	 * @param day the day of week to get the startScheduling for
	 * @return the startScheduling time for the given day of the week
	 */
	public LocalTime getStartScheduling(DayOfWeek day) {
		if (schedulingTimes.get(day) == null) {
			return LocalTime.MIDNIGHT;
		}
		return schedulingTimes.get(day)[0];
	}
	
	/**
	 * Get the endScheduling for the given day of week
	 * @param day the day of week to get the endScheduling for
	 * @return the endScheduling time for the given day of the week
	 */
	public LocalTime getEndScheduling(DayOfWeek day) {
		if (schedulingTimes.get(day) == null) {
			return LocalTime.MAX;
		}
		return schedulingTimes.get(day)[1];
	}
	
	/**
	 * Return the list of assignments used for creating the Google Calendar events
	 * @return the assignmentList
	 */
	public ArrayList<Assignment> getAssignmentList() {
		return assignmentList;
	}
	
	/**
	 * Returns the list of the user's calendars to schedule around
	 * @return the eventCaledendars
	 */
	public ArrayList<CalendarListEntry> getEventCalendars() {
		return eventCalendars;
	}
	
	/**
	 * Retrieves events from calendars at the specified start and end times
	 * @param start the date time to start searching for events
	 * @param end the date time to end searching for events
	 * @return the list of events from the eventCalendars list starting at start and ending at end
	 * @throws IOException 
	 */
	public ArrayList<Event> getEventList(LocalDateTime start, LocalDateTime end) throws IOException{
		ArrayList<Event> eventList = new ArrayList<Event>();
		// ensures break period
		start = start.minusMinutes(breakPeriod);
		end = end.plusMinutes(breakPeriod);
		for (int i = 0; i < eventCalendars.size(); i++) {
			Events events = service.events().list(eventCalendars.get(i).getId())
				      .setTimeMin(toDateTime(start))
				      .setTimeMax(toDateTime(end))
				      .setOrderBy("startTime")
				      .setSingleEvents(true)
				      .execute();
			eventList.addAll(events.getItems());
		}
		// filter out all day events
		for (Event e: eventList) {
			if (getEventDuration(e) >= MINUTES_IN_DAY) {
				eventList.remove(e);
			}
		}
		// Used to order events by start time
		eventList.sort(Comparator.comparing(event -> toLDT(event.getStart().getDateTime())));
		return eventList;
	}
	
	/**
	 * Returns duration of the event in minutes
	 * @param e event to get duration of
	 * @return the duration of the event in minutes
	 * @throws NullPointerException if event is null
	 */
	private int getEventDuration(Event e) {
		if (e == null) {
			throw new NullPointerException("Event is null");
		}
		if (e.getEnd().getDateTime() == null) {
			return MINUTES_IN_DAY;
		}
		LocalDateTime startTime = toLDT(e.getStart().getDateTime());
		LocalDateTime endTime = toLDT(e.getEnd().getDateTime());
		
		return (int) startTime.until(endTime, ChronoUnit.MINUTES);
	}
	
	/**
	 * Returns the sum of time required for each assignment in the assignment list
	 * @return the total time required for each assignment in the assignment list
	 */
	public double getTotalTimeRequired() {
		return totalTimeRequired;
	}
	
	/**
	 * Adds the given calendar to the eventCalendars to schedule around
	 * @param calendar the calendar to add
	 * @throws IllegalArgumentException if the calendar is null
	 */
	public void addToEventCalendars(CalendarListEntry calendar) {
		if (calendar == null) {
			throw new IllegalArgumentException("Cannot add null calendar");
		}
		eventCalendars.add(calendar);
	}
	
	/**
	 * Returns a string array with each row containing each assingment's name in the first column 
	 * and it's deadline in the second column
	 * @return a string array containing each assingment's name and it's deadline
	 */
	public String[][] getAssignmentListString(){
		String[][] returnString = new String[assignmentList.size()][2];
		for (int i = 0; i < assignmentList.size(); i++) {
			returnString[i][0] = assignmentList.get(i).getName();
			returnString[i][1] = assignmentList.get(i).getDeadline().toString();
		}
		return returnString;
	}
	
	/**
	 * Gets the schedule used to store the free time and assignment time blocks
	 * @return the schedule used
	 */
	public ScheduleArray getSchedule() {
		return schedule;
	}
	
	/**
	 * Sorts the assignment list from greatest to least priority score;
	 */
	private void prioritizeAssignments(){
		Comparator <Assignment> assignmentComparator = Comparator.comparing(Assignment::getPriorityScore);
		assignmentList.sort(assignmentComparator.reversed());
	}
	
	/**
	 * Calculates and sets the priority score for each assignment in assignmentList based on
	 * the amount of free time remaining until its deadline
	 * @throws IllegalArgumentException if assignmentList is empty
	 */
	private void calculatePriorityScores() {
		if (assignmentList == null || assignmentList.size() == 0) {
			throw new IllegalArgumentException("Assignment list is empty");
		}
		for (Assignment assignment : assignmentList) {
			double newTimeRemaining = schedule.getFreeTimeUpTo(assignment.getDeadline());
			// prevent divide by 0, though the chance of 
			if (newTimeRemaining == 0) {
				newTimeRemaining = -100.0;
			}
			double basePriorityScore = assignment.getTimeRequired() / newTimeRemaining;
			// The lesser the time until the deadline, the higher the urgency score
			double urgencyScore = basePriorityScore != 0 ? 1.0 / (newTimeRemaining + 1) : 0;
			/** The weighting of the baseScore and urgencyScore were decided experimentally */
			double newPriorityScore = (BASE_WEIGHT * basePriorityScore) + (URGENCY_WEIGHT * urgencyScore);
			assignment.setTimeRemaining((int) newTimeRemaining);
			assignment.setPriorityScore(newPriorityScore);
		}
	}
	
	/**
	 * Reduces the time required for each assignment the total time required
	 * is greater than total free time. The new time required for an assignment is proportional
	 * to the ratio of the time required for it and the total time required for all assignments
	 * @return
	 */
	private boolean reduceTimeRequired() {
		if (totalTimeRequired <= totalFreeTime) {
			return false;
		}
		
		double totalTimeReduced = 0;
		
		// Reduces each assignment to the same proportion of total free time
		for (Assignment a: assignmentList) {
			double proportion = a.getTimeRequired() / totalTimeRequired;
			int newTimeReq = (int) (proportion * totalFreeTime);
			int timeReduced = a.getTimeRequired() - newTimeReq;
			a.setTimeRequired(newTimeReq);
			totalTimeReduced += timeReduced;
		}
		totalTimeRequired -= totalTimeReduced;
		return true;
	}
	
	/**
	 * Returns the latest deadline in the assignmentList
	 * @return the latest deadline in the assignmentList
	 */
	private LocalDateTime getLastDeadline() {
		if (assignmentList == null || assignmentList.size() == 0) {
			throw new IllegalArgumentException("Empty assignment list");
		}
		LocalDateTime lastDeadline = assignmentList.get(0).getDeadline();
		//searches for a later deadline
		for (int i = 1; i < assignmentList.size(); i++) {
			if (assignmentList.get(i).getDeadline().isAfter(lastDeadline)) {
				lastDeadline = assignmentList.get(i).getDeadline();
			}
		}
		return lastDeadline;
	}
	
	/**
	 * Returns the amount of time in minutes between the startPoint and eventStart.
	 * If eventStart is on a different day as startPoint, this returns the amount of time in 
	 * minutes between startPoint and endScheduling for the day of startPoint
	 * @param startPoint the date time to start searching for the time in between
	 * @param eventStart the date time to end searching for the time in between
	 * @return the amount of time between startPoint and eventStart in minutes
	 */
	private int getTimeBetween(LocalDateTime startPoint, LocalDateTime eventStart) {
		// used to check if start point and first event are on different days
		LocalTime timeToEnd = getEndScheduling(startPoint.getDayOfWeek());
		int timeBetween;
		
		// Checks if the eventStart is after timeToEnd for that day
		if (eventStart.isAfter(startPoint.with(timeToEnd))) {
			// adds any time remaining before the end scheduling time
			return (int) startPoint.until(timeToEnd.atDate(startPoint.toLocalDate()), ChronoUnit.MINUTES);
		}
		timeBetween = (int) startPoint.until(eventStart, ChronoUnit.MINUTES);
		
		// in case startPoint skipped ahead of eventStart
		if (timeBetween < 0) {
			return 0;
		}
		
		return timeBetween;
	}
	
	/**
	 * Returns a LocalDateTime starting at the startScheduling time of the next day from 
	 * the given startPoint
	 * @param startPoint the date time to move from
	 * @return a LocalDateTime with the next day's startTime
	 */
	private LocalDateTime startAtNextDay(LocalDateTime startPoint) {
		DayOfWeek nextDay = startPoint.getDayOfWeek().plus(1);
		LocalTime nextStartTime = getStartScheduling(nextDay);
		return startPoint.plusDays(1).with(nextStartTime);
	}
	
	/**
	 * Returns the amount of free time between a given starting and ending point
	 * in hours. If no start point specified, the default start point is now.
	 * Stores the free time blocks inside of the schedule field
	 * @param startPoint the date time to start searching for free time at
	 * @param endPoint the date time to end searching for free time at
	 * @return minutes of free time remaining until the endPoint
	 * @throws IllegalArgumentException if eventCalendars is empty or schedulingTimes is null
	 * @throws IOException 
	 */
	public int findFreeTimeFromCalendar(LocalDateTime startPoint, LocalDateTime endPoint) throws IOException {
		if (eventCalendars.size() == 0) {
			throw new IllegalArgumentException("No calendars to search in!");
		}
		if (schedulingTimes == null) {
			throw new IllegalArgumentException("Must enter scheduling boundaries");
		}
		if (startPoint == null) {
			startPoint = LocalDateTime.now();
		}
		
		LocalTime timeToStart = getStartScheduling(startPoint.getDayOfWeek());
		LocalTime timeToEnd = getEndScheduling(startPoint.getDayOfWeek());
		// Adjusting startPoint if needed 
		if (startPoint.toLocalTime().isBefore(timeToStart)){ // moves up start point
			startPoint = startPoint.with(timeToStart);
		}
		if (startPoint.toLocalTime().isAfter(timeToEnd)) { //moves start point to next day
			startPoint = startAtNextDay(startPoint);
			timeToStart= getStartScheduling(startPoint.getDayOfWeek());
			timeToEnd = getEndScheduling(startPoint.getDayOfWeek());
		}
		
		int freeTime = 0;
		//get list of events from calendars from startPoint to endPoint;
		ArrayList<Event> eventList = getEventList(startPoint, endPoint);
		
		// find free times between each event
		for (int i = 0; i < eventList.size(); i++) {
			// determine start scheduling time and end time for the day
			timeToStart = getStartScheduling(startPoint.getDayOfWeek());
			timeToEnd = getEndScheduling(startPoint.getDayOfWeek());
			
			// Adjusting startPoint if needed 
			if (startPoint.toLocalTime().isBefore(timeToStart)){ // moves up start point
				startPoint = startPoint.with(timeToStart);
			}
			if (startPoint.toLocalTime().isAfter(timeToEnd)) { //moves start point to next day
				startPoint = startAtNextDay(startPoint);
				timeToStart= getStartScheduling(startPoint.getDayOfWeek());
				timeToEnd = getEndScheduling(startPoint.getDayOfWeek());
			}
			
			// getting event details and converting them to more friendly LocalDate
			Event e = eventList.get(i);
			LocalDateTime eventStart = toLDT(e.getStart().getDateTime());
			LocalDateTime eventEnd = toLDT(e.getEnd().getDateTime());
			
			// get time until the next eventStart
			int timeBetween = getTimeBetween(startPoint, eventStart);
			int numSkips = 0;
			int partialTimeBetween = 0;
			//TODO maybe refactor for readability
			// move up startPoint until the days are the same
			while (startPoint.toLocalDate().isBefore(eventStart.toLocalDate())) {
				numSkips++;
				// adds the period from the last event to the timeToEnd to the schedule
				if (numSkips == 1) {
					if (i == 0 && timeBetween >= minWorkPeriod) {
						schedule.add(startPoint, startPoint.plusMinutes(timeBetween));
					}
					else if (i > 0 && timeBetween >= minWorkPeriod + breakPeriod) {
						schedule.add(startPoint.plusMinutes(breakPeriod), startPoint.plusMinutes(timeBetween));
					}
				}
				startPoint = startAtNextDay(startPoint);
				partialTimeBetween = getTimeBetween(startPoint, eventStart);
				if (partialTimeBetween >= minWorkPeriod + breakPeriod) {
					timeBetween += partialTimeBetween;
					// add time slot to schedule up until the day of the event
					if (startPoint.toLocalDate().isBefore(eventStart.toLocalDate())) {
						schedule.add(startPoint, startPoint.plusMinutes(partialTimeBetween));
					}
				}
				else { // to offset the break period that would have been subtracted if the remaining time before this event was free
					timeBetween += breakPeriod; 
				}
			}
			// need to subtract the break period after the previous event
			if (numSkips > 0 && i > 0) { 
				timeBetween -= breakPeriod;
			}
			
			// ensures that there is enough time between to account for break period and minWorkPeriod
			if ((startPoint.toLocalTime().equals(timeToStart) || i == 0) && timeBetween >= breakPeriod + minWorkPeriod) {
				freeTime += timeBetween - breakPeriod;
				if (numSkips > 0 && partialTimeBetween >= breakPeriod + minWorkPeriod) {
					schedule.add(startPoint, eventStart.minusMinutes(breakPeriod));
				}
				else if (numSkips == 0) {
					schedule.add(startPoint, eventStart.minusMinutes(breakPeriod));
				}
			}
			else if (timeBetween >= breakPeriod * 2 + minWorkPeriod) {
				freeTime += timeBetween - (breakPeriod * 2);
				//add leftover timeslot from skip to schedule
				if (numSkips > 0 && partialTimeBetween >= breakPeriod + minWorkPeriod) {
					schedule.add(startPoint, eventStart.minusMinutes(breakPeriod));
				}
				else if (numSkips == 0) {
					schedule.add(startPoint.plusMinutes(breakPeriod), eventStart.minusMinutes(breakPeriod));
				}
			}
			// moves to the end of the event 
			startPoint = eventEnd;
		}
		
		int timeBetween = getTimeBetween(startPoint, endPoint);
		int numSkips = 0;
		int partialTimeBetween = 0;
		// Checks for time between the last event and the endPoint
		while (startPoint.toLocalDate().isBefore(endPoint.toLocalDate())) {
			numSkips++;
			// adds the period from the last event to the timeToEnd to the schedule
			if (numSkips == 1 && eventList.size() > 1) {
				schedule.add(startPoint.plusMinutes(breakPeriod), startPoint.plusMinutes(timeBetween));
			}
			startPoint = startAtNextDay(startPoint);
			partialTimeBetween = getTimeBetween(startPoint, endPoint);
			if (partialTimeBetween >= minWorkPeriod) {
				timeBetween += partialTimeBetween;
				// add time slot to schedule up until the day of the event
				if (startPoint.toLocalDate().isBefore(endPoint.toLocalDate())) {
					schedule.add(startPoint, startPoint.plusMinutes(partialTimeBetween));
				}
			}
		}
		
		// considers break period after the end of the last event if endPoint is after it
		if (eventList.size() > 0 && timeBetween >= breakPeriod + minWorkPeriod) {
			freeTime += timeBetween - breakPeriod;
			if (numSkips > 0 && partialTimeBetween >= breakPeriod + minWorkPeriod) {
				schedule.add(startPoint, startPoint.plusMinutes(partialTimeBetween));
			}
			else if (numSkips == 0) {
				schedule.add(startPoint.plusMinutes(breakPeriod), startPoint.plusMinutes(timeBetween));
			}
		}
		else if (eventList.size() == 0 && timeBetween >= minWorkPeriod) {
			freeTime += timeBetween;
			schedule.add(startPoint, startPoint.plusMinutes(timeBetween));
		}
		
		
		return freeTime;
	}
	
	
	/**
	 * Creates events on the Google Calendar with the given calendar ID for each assignment 
	 * based on this AssignmentScheduler's fields
	 * @param calendarID the calendarID of the Calendar to add the created events to
	 * @throws IllegalArgumentException if assignmentList is empty
	 * @throws IOException 
	 * 
	 */
	public void setAssignmentSchedules(String calendarID) throws IOException {
		if (assignmentList.size() <= 0) {
			throw new IllegalArgumentException("No assignments");
		}
		// ensure that all assignments can fit on schedule
		totalFreeTime = findFreeTimeFromCalendar(startScheduling, getLastDeadline());
		calculatePriorityScores();
		prioritizeAssignments();
		reduceTimeRequired();
		
		// repeats for each day up to the last deadline
		while (totalTimeRequired > 0) {
			calculatePriorityScores();
			prioritizeAssignments();
			reduceTimeRequired();
			//Adds working event(s) to calendar for each assignment
			for (Assignment assignment : assignmentList) {
				addWorkTimesToSchedule(assignment, calendarID);
			}
		}
	}
	
	/**
	 * @param eventCalendar the eventCalendar to set
	 */
	private void setEventCalendar(Calendar eventCalendar) {
		if (eventCalendar == null) {
			throw new IllegalArgumentException("Invalid eventCalendar");
		}
		this.service = eventCalendar;
	}
	
	/**
	 * Sets the date and time to start the scheduling process at
	 * @param startScheduling the date and  time to start the scheduling process at
	 */
	private void setStartScheduling(LocalDateTime startScheduling) {
		if (startScheduling == null) {
			throw new IllegalArgumentException("Null startScheduling");
		}
		this.startScheduling = startScheduling;
	}
	
	/**
	 * Uses a greedy algorithm to determine how much time to schedule the given assignment for
	 * This will create and add as many events as necessary for the assignment until there is no more free time
	 * available or until the time required for the assignment is less than the minimum work period
	 * @param assignment the assignment to add the work events for
	 * @param calendarID the calendarID of the calendar to add to
	 * @throws IOException
	 */
	private void addWorkTimesToSchedule(Assignment assignment, String calendarID) throws IOException {
		int timePlanned = 0;
		timePlanned = (int) (assignment.getTimeRequired());
		int timeAllocated = 0;
		// inserts assignment blocks into calendar until sufficient time as calculated by timePlanned is allocated 
		while (timePlanned > 0 && (timePlanned >= minWorkPeriod)) {
			calculatePriorityScores();
			reduceTimeRequired();
			if (timePlanned > assignment.getTimeRequired()) {
				timePlanned = (int) assignment.getTimeRequired();
			}
			if (assignment.getTimeRemaining() <= timePlanned) {
				timePlanned = (int) assignment.getTimeRemaining();
			}
			if (timePlanned < minWorkPeriod && !(assignment.getInitTimeRequired() < minWorkPeriod)) {
				break;
			}
			// finds next free space between events in the day
			if (timePlanned > maxWorkPeriod) {
				timeAllocated = addToNextFreeSpace(assignment, maxWorkPeriod, calendarID);
			}
			else {
				timeAllocated = addToNextFreeSpace(assignment, timePlanned, calendarID);
			}
			timePlanned -= timeAllocated;
			// update assignment
			assignment.addTimeAllocated(timeAllocated);
			assignment.minusTimeRequired(timeAllocated);
			// update remaining free time in the day
			totalTimeRequired -= timeAllocated;
			if (totalTimeRequired > 0) {
				totalFreeTime = schedule.getTotalFreeTime();
			}
		}
		if (assignment.getTimeRequired() < minWorkPeriod || assignment.getTimeRemaining() < minWorkPeriod) {
			totalTimeRequired -= assignment.getTimeRequired();
			assignment.setTimeRequired(0);
		}
	}
	
	/**
	 * Attempts to create and add an event for the given assignment with the given duration to the given calendar.
	 * If max duration is greater than the duration of the next free time block in schedule, then the duration will be set
	 * to the duration of the next free time block.
	 * @param a the assignment to add
	 * @param maxDuration the maximum duration the event will have if available
	 * @param calendarID the calendarID of the calendar to add the event to
	 * @return the duration of the created event
	 * @throws IOException
	 */
	private int addToNextFreeSpace(Assignment a, int maxDuration, String calendarID) throws IOException {
		int duration = 0;
		LocalDateTime assignmentEnd;
		LocalDateTime assignmentStart;
		// create event
		Event event = new Event();
		// more weird conversions for time formatting
		EventDateTime startTime = new EventDateTime();
		EventDateTime endTime = new EventDateTime();
		int i = 0;
		for (i = 0; i < schedule.size(); i++) {
			// find the next open time slot
			if (!schedule.getTimeSlot(i).isFilled()) {
				break;
			}
		}
		if (schedule.getTimeSlot(i).isFilled()) {
			throw new IllegalArgumentException("All slots are filled!");
		}
		LocalDateTime start = schedule.getTimeSlot(i).getStartTime();
		LocalDateTime end = schedule.getTimeSlot(i).getEndTime();
		duration = (int) start.until(end, ChronoUnit.MINUTES);
	
		if (maxDuration <  duration && (schedule.wouldSplit(i, maxDuration, minWorkPeriod)
										|| duration > a.getTimeRequired())) {
			duration = maxDuration;
		}
		
		// add to schedule
		schedule.fillSlot(i, duration, minWorkPeriod);
		
		assignmentStart = start;
		assignmentEnd = start.plusMinutes(duration);
		
		a.addTimeBlock(assignmentStart, assignmentEnd);
		
		// set title
		event.setSummary(a.getName());
		event.setColorId(a.getColorID());
		// set start and end date time, converts LocalDateTime to DateTime to EventDateTime
		event.setStart(startTime.setDateTime(toDateTime(assignmentStart)));
		event.setEnd(endTime.setDateTime(toDateTime(assignmentEnd)));
		
		// add event to calendar 
		service.events().insert(calendarID, event).execute();
		return duration;
	}

	/**
	 * Convert DateTime to LocalDateTime, provided by ChatGPT
	 * @param dateTime the dateTime to convert
	 * @return the LocalDateTime of dateTime
	 */
	public static LocalDateTime toLDT(DateTime dateTime) {
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
	
	/**
	 * Convert LocalDateTime to DateTime, provided by Chat GPT
	 * @param localDateTime the LocalDateTime to convert
	 * @return the DateTime of the localDateTime
	 */
	public static DateTime toDateTime(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        Date date = Date.from(instant);
        return new DateTime(date);
    }

	/**
	 * @return the service calendar
	 */
	public Calendar getServiceCalendar() {
		return service;
	}
	
	/**
	 * Deletes events with matching names of the assignments on the given calendar in the time range of
	 * startScheduling to the last deadline in the assingmentList
	 * @param calendarID the calendar ID of the calendar to remove the assignments from
	 * @throws IOException
	 */
	public void clearAssignments(String calendarID) throws IOException {
		for (Assignment a : assignmentList) {
			ArrayList<Event> eventList = this.getEventList(startScheduling, getLastDeadline());
			// delete matching event
			for (Event e: eventList) {
				if (e.getSummary().equals(a.getName())) {
					service.events().delete(calendarID, e.getId()).execute();
				}
			}
		}
	}
	
}