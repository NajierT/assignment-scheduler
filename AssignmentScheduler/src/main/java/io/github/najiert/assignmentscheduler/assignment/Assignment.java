package io.github.najiert.assignmentscheduler.assignment;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import io.github.najiert.assignmentscheduler.schedule.ScheduleArray;

/**
 * Represents a task with a specific deadline and required time for completion.
 * An assignment holds a schedule array that can be updated to represent working
 * times for the assignment. It also includes a priorityScore parameter than can
 * be set and used for comparison with other assignments.
 * @author Najier Torrence
 *
 */
public class Assignment {
	private String name;
	/** Deadline of the assignment */
	private LocalDateTime deadline;
	/** Time required to complete the assignment in minutes */
	private int timeRequired;
	/** The time required to complete the assignment in minutes when this object was made */
	private int initTimeRequired;
	/** Free time remaining until deadline in minutes */
	private int timeRemaining;
	/** The timeRequired divided by the timeRemaining */
	private double priorityScore;
	/** Amount of time in timeBlocks */
	private int timeAllocated;
	/** Google Calendar event Color ID */
	private String colorID;
	/** Holds the time blocks for the assignment */
	private ScheduleArray timeBlocks;

	/**
	 * Creates an assignment
	 * @param name		   name of assignment
	 * @param deadline	   deadline of assignment
	 * @param timeRequired time required to complete assignment in minutes
	 */
	public Assignment(String name, LocalDateTime deadline, int timeRequired) {
		this(name, deadline, timeRequired, null);
	}

	/**
	 * Creates an assignment with a specified color
	 * @param name 		   name of the assignment
	 * @param deadline 	   deadline of the assignment
	 * @param timeRequired time required to complete assignment in minutes
	 * @param colorID	   A google calendar event ColorID
	 */
	public Assignment(String name, LocalDateTime deadline, int timeRequired, String colorID) {
		setName(name);
		setTimeRequired(timeRequired);
		initTimeRequired = timeRequired;
		setDeadline(deadline);
		// initial value until updated with free time
		timeRemaining = (int) LocalDateTime.now().until(deadline, ChronoUnit.MINUTES);
		priorityScore = timeRequired / (double) timeRemaining;
		timeBlocks = new ScheduleArray();
		this.colorID = colorID;
		addTimeAllocated(0);
	}
	/**
	 * Adds the given amount of time to timeAllocated field
	 * @param  timeAllocated 			the amount of time to add
	 * @throws IllegalArgumentException if the time allocated is negative
	 */
	public void addTimeAllocated(int timeAllocated) {
		if (timeAllocated < 0) {
			throw new IllegalArgumentException("Invalid timeAllocated");
		}
		this.timeAllocated += timeAllocated;
	}

	/**
	 * Adds a time block with the specified start and end time to this Assignment's scheduleArray
	 * @param  start 					the LocalDateTime the block starts at
	 * @param  end						the LocalDateTime the block ends at
	 * @throws IllegalArgumentException if the end time is before the start time or after the deadline
	 */
	public void addTimeBlock(LocalDateTime start, LocalDateTime end) {
		if (start.isAfter(end) || end.isAfter(deadline)) {
			throw new IllegalArgumentException("Invalid time block");
		}
		timeBlocks.add(start, end);
	}

	/**
	 * Returns the colorID of this assignment
	 * @return the colorID of this assignment
	 */
	public String getColorID() {
		return colorID;
	}

	/**
	 * Returns the deadline of this assignment
	 * @return the deadline
	 */
	public LocalDateTime getDeadline() {
		return deadline;
	}

	/**
	 * Returns the initial time required by this assignment
	 * @return the initial time required by this assignment
	 */
	public double getInitTimeRequired() {
		return initTimeRequired;
	}

	/**
	 * Returns the name of the assignment
	 * @return the name of the assignment
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the priority score of the assignment
	 * @return the priority score of the assignment
	 */
	public double getPriorityScore() {
		return priorityScore;
	}

	/**
	 * Returns the timeAllocated to this assignment
	 * @return the timeAllocated to this assignment
	 */
	public int getTimeAllocated() {
		return timeAllocated;
	}

	/**
	 * Returns the scheduleArray for this assignment
	 * @return the scheduleArray for this assignment
	 */
	public ScheduleArray getTimeBlocks() {
		return timeBlocks;
	}

	/**
	 * Return the timeRemaining for this assignment
	 * @return the timeRemaining for this assignment
	 */
	public int getTimeRemaining() {
		return timeRemaining;
	}

	/**
	 * Return the timeRequired for this assignment
	 * @return the timeRequired for this assignment
	 */
	public int getTimeRequired() {
		return timeRequired;
	}

	/**
	 * Subtracts the given amount of time from timeRequired
	 * @param  time 					the amount of time to subtract
	 * @throws IllegalArgumentException if the time is <= 0 or if it would cause timeRequired to
	 * 									to be negative
	 */
	public void minusTimeRequired(int time) {
		if (time <= 0) {
			throw new IllegalArgumentException("Invalid time");
		}
		if (timeRequired - time < 0) {
			throw new IllegalArgumentException("Too much time added");
		}
		timeRequired -= time;
	}

	/**
	 * Sets the deadline for this assignment
	 * @param  deadline 			the deadline to set
	 * @throws NullPointerException if the deadline is null
	 */
	private void setDeadline(LocalDateTime deadline) {
		if (deadline == null) {
			throw new NullPointerException();
		}

		this.deadline = deadline;
	}

	/**
	 * Sets the name for this assignment
	 * @param name the name to set
	 * @throws IllegalArgumentException if name is empty or null
	 */
	private void setName(String name) {
		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("Invalid name");
		}
		this.name = name;
	}

	/**
	 * Sets the priority score for this assignment
	 * @param priorityScore the priority score to set
	 */
	public void setPriorityScore(double priorityScore) {
		this.priorityScore = priorityScore;
	}

	/**
	 * Sets the time remaining for this assignment
	 * @param timeRemaining the timeRemaining to set
	 */
	public void setTimeRemaining(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	/**
	 * Sets the time required for this assignment
	 * @param timeRequired the timeRequired to set
	 * @throws IllegalArgumentException if the timeRequired is less than 0
	 */
	public void setTimeRequired(int timeRequired) {
		if (timeRequired < 0) {
			throw new IllegalArgumentException("Invalid timeRequired");
		}
		this.timeRequired = timeRequired;
	}

}
