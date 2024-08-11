package io.github.najiert.assignmentscheduler.schedule;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Custom list containing TimeSlots for each element
 * Sorted by the startTime of its TimeSlots
 * @author Najier Torrence
 *
 */
public class ScheduleArray{
	
	/**
	 * An object representing a period starting at startTime and ending at endTime
	 * @author Najier
	 *
	 */
	public class TimeSlot {
		/** The starting time for the timeSlot */
		private LocalDateTime startTime;
		/** The ending time for the timeSlot */
		private LocalDateTime endTime;
		/** True if the timeSlot is busy, false otherwise*/
		private boolean isFilled;

		/**
		 * Creates a timeslot with the specified startTime and endTime
		 * @param startTime the date and time this starts at
		 * @param endTime the date and time this ends at
		 */
		public TimeSlot(LocalDateTime startTime,LocalDateTime endTime) {
			setStartTime(startTime);
			setEndTime(endTime);
			isFilled = false;
		}
		
		/**
		 * @return the endTime
		 */
		public LocalDateTime getEndTime() {
			return endTime;
		}
		
		/**
		 * 
		 * @return the startTime
		 */
		public LocalDateTime getStartTime() {
			return startTime;
		}
		
		/**
		 * 
		 * @return if this isFilled
		 */
		public boolean isFilled() {
			return isFilled;
		}
		
		/**
		 * Sets the endTime to the given LocalDateTime
		 * @param endTime the endTime to set
		 * @throws IllegalArgumentException if the endTime/startTime is null or before the startTime
		 */
		private void setEndTime(LocalDateTime endTime) {
			if (endTime == null) {
				throw new IllegalArgumentException("End time cannot be null.");
			}
			if (startTime == null) {
				throw new IllegalArgumentException("Start time must be set");
			}
			if (endTime.isBefore(startTime)) {
				throw new IllegalArgumentException("End time cannot be before Start time");
			}

			this.endTime = endTime;
		}
		
		/**
		 * Sets isFilled to true or false
		 * @param isFilled whether this TimeSlot is filled or not
		 */
		public void setFilled(boolean isFilled) {
			this.isFilled = isFilled;
		}
		
		/**
		 * Sets the startTime to the given LocalDateTime
		 * @param startTime the startTime to set
		 * @throws IllegalArgumentException if the startTime is null
		 */
		private void setStartTime(LocalDateTime startTime) {
			if (startTime == null) {
				throw new IllegalArgumentException("Start time cannot be null.");
			}
			this.startTime = startTime;
		}
	}
	/** The initial size for the ScheduleArray*/
	private static final int INIT_SIZE = 10;
	/** Number of minutes before and after one time block */
	private static final int DEF_BREAK_PERIOD = 15;
	/** The list of time slots the ScheduleArray contains */
	private TimeSlot[] list;
	/** The size of the ScheduleArray*/
	private int size;
	/** The minimum amount of time between one time block's endTime and the next's startTime in minutes*/
	private int breakPeriod;

	/** 
	 * Creates a new schedule array with the breakPeriod set to default
	 */
	public ScheduleArray(){
		this(DEF_BREAK_PERIOD);
	}
	
	/** 
	 * Creates a new schedule array with the breakPeriod set to the given input
	 * @param breakPeriod the minimum amount of time between a TimeSlot's endTime and the next's startTime
	 */
	public ScheduleArray(int breakPeriod){
		list = new TimeSlot[INIT_SIZE];
		size = 0;
		setBreakPeriod(breakPeriod);
	}

	/**
	 * Adds a TimeSlot and sorts array by start times
	 * @param start the startTime for the TimeSlot
	 * @param end the endTime for the TimeSlot
	 * @throws NullPointerException if the start or end time is null
	 * @throws IllegalArgumentException if the end is before start
	 */
	public void add(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			throw new NullPointerException("Items cannot be null");
		}
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("Can't end an event before it starts");
		}
		for (int i = 0; i < size; i++) {
			// check for duplicate time block
			if (start.equals(list[i].startTime) || start.equals(list[i].endTime) ||
					end.equals(list[i].startTime) || end.equals(list[i].endTime)) {
				throw new IllegalArgumentException("Time block already exists");
			}
			// check if start and end is between already existing time block
			if (start.isAfter(list[i].startTime) && start.isBefore(list[i].endTime) ||
					end.isAfter(list[i].startTime) && end.isBefore(list[i].endTime)) {
				throw new IllegalArgumentException("Conflicts with existing time block");
			}
			// check for buffer period
			if (start.isAfter(list[i].endTime) && start.isBefore(list[i].endTime.plusMinutes(breakPeriod)) ||
					end.isBefore(list[i].startTime) && end.isAfter(list[i].startTime.minusMinutes(breakPeriod))) {
				throw new IllegalArgumentException("Not enough buffer time before/after event");
			}
		}
		if (size == list.length) {
			growArray();
		}
		list[size] = new TimeSlot(start, end);
		// Sort by start time, and puts null objects at the end
		Comparator<TimeSlot> timeSlotComparator = Comparator.nullsLast(Comparator.comparing(TimeSlot::getStartTime,
				Comparator.nullsLast(Comparator.naturalOrder())));
		Arrays.sort(list, timeSlotComparator);

		size +=1;
	}
	
	/**
	 * Removes every TimeSlot from this
	 */
	public void clear() {
		list = new TimeSlot[INIT_SIZE];
		size = 0;
	}

	/**
	 * Sets the isFilled field for the TimeSlot at the specified index to true if it is false.
	 * If the given duration is less than the duration of the time slot, then the time slot will be split if
	 * the duration would allow for another valid time slot to be created. If not, then the endTime for
	 * the TimeSlot will be truncated to startTime plus the duration.
	 * @param i the index of the TimeSlot to fill
	 * @param duration how much of the time slot to fill in minutes
	 * @param minWorkTime the minimum amount of time necessary for a valid TimeSlot
	 * @throws IllegalArgumentException if slot is filled or if the given duration is greater than the TimeSlot's
	 */
	public void fillSlot(int i, int duration, int minWorkTime) {
		if (i < 0 || i >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		LocalDateTime start = list[i].startTime;
		LocalDateTime end = list[i].endTime;
		LocalDateTime newEnd = start.plusMinutes(duration);
		// check if slot is already filled or if duration is too long
		if (list[i].isFilled() || newEnd.isAfter(end)){
			throw new IllegalArgumentException("Unable to fill this slot.");
		}
		list[i].endTime = newEnd;
		list[i].isFilled = true;

		// create new time slot if there is enough leftover time
		LocalDateTime newStart = newEnd.plusMinutes(breakPeriod);
		if (newStart.until(end, ChronoUnit.MINUTES) >= minWorkTime) {
			this.add(newStart, end);
		}
	}
	
	/**
	 * Returns true if filling the TimeSlot at the specified index would create a new valid TimeSlot
	 * @param i the index of the TimeSlot to check
	 * @param duration the duration to check
	 * @param minWorkTime the minimum duration for a valid TimeSlot
	 * @return true if filling the TimeSlot with the given duration would create a new valid TimeSlot
	 * @throws IllegalArgumentException if the given duration is greater than the TimeSlot's duration
	 */
	public boolean wouldSplit(int i, int duration, int minWorkTime) {
		if (i < 0 || i >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		LocalDateTime start = list[i].startTime;
		LocalDateTime end = list[i].endTime;
		LocalDateTime newEnd = start.plusMinutes(duration);
		// check if duration is too long long
		if (newEnd.isAfter(end)){
			throw new IllegalArgumentException("Duration is too long");
		}

		// Return true if there would be enough time for a new timeSlot
		LocalDateTime newStart = newEnd.plusMinutes(breakPeriod);
		return newStart.until(end, ChronoUnit.MINUTES) >= minWorkTime;
	}
	
	/**
	 * Gets the breakPeriod
	 * @return the breakPeriod
	 */
	public int getBreakPeriod() {
		return breakPeriod;
	}
	
	/**
	 * Gets the endTime for the TimeSlot at the given index
	 * @param row the index of the TimeSlot to get the end for
	 * @return the endTime for the specified TimeSlot
	 * @throws IllegalArgumentException if the row is out of bounds
	 */
	public LocalDateTime getEnd(int row) {
		if (row < 0 || row >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		return list[row].getEndTime();
	}

	/**
	 * Returns the amount of free time available up to the given point. Free time is considered the sum
	 * of the duration of TimeSlots with isFilled set to false.
	 * @param point the time to check the free time up to
	 * @return the amount of free time up to the point in minutes
	 */
	public int getFreeTimeUpTo(LocalDateTime point) {
		int freeTime = 0;
		for (int i = 0; i < size; i++) {
			// add time remaining from start of time block up until the point and finish
			if (point.isBefore(list[i].endTime) && !list[i].isFilled()) {
				int timeBetween = (int) list[i].startTime.until(point, ChronoUnit.MINUTES);
				if (timeBetween > 0) {
					freeTime += timeBetween;
				}
				break;
			}
			// add the duration of the TimeBlock to free time and move to the next TimeBlock
			else if (!list[i].isFilled()) {
				freeTime += list[i].startTime.until(list[i].endTime, ChronoUnit.MINUTES);
			}
		}
		return freeTime;
	}


	/**
	 * Returns time block start
	 * @param row the index of the TimeBlock to get the startTime for
	 * @return the starTime of the TimeBlock at the given index
	 */
	public LocalDateTime getStart(int row) {
		if (row < 0 || row >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		return list[row].getStartTime();
	}
	
	/**
	 * Returns the TimeSlot at the given index
	 * @param i the index of the timeSlot
	 * @return the TimeSlot at index i
	 */
	public TimeSlot getTimeSlot(int i) {
		if (i < 0 || i >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		return list[i];
	}
	
	/**
	 * Returns the total free time of this. Free time is considered the sum
	 * of the duration of TimeSlots with isFilled set to false.
	 * @return
	 */
	public int getTotalFreeTime() {
		int totalFreeTime = 0;
		for (int i = 0; i < size; i++) {
			if (!list[i].isFilled) {
				totalFreeTime += list[i].startTime.until(list[i].endTime, ChronoUnit.MINUTES);
			}
		}
		return totalFreeTime;
	}
	
	/**
	 * Used to grow the array if it is at capacity
	 */
	private void growArray() {
		TimeSlot[] newList = new TimeSlot[size * 2];

		for (int i = 0; i < size; i++) {
			newList[i] = list[i];
		}	
		list = newList;
	}
	
	/**
	 * Removes the TimeSlot at the given index
	 * @param row the index of the TimeSlot to remove
	 */
	public void remove(int row) {
		if (row < 0 || row >= size) {
			throw new IllegalArgumentException("Invalid index");
		}
		for (int i = row; i < size - 1; i++) {
			LocalDateTime nextStart = list[i+1].startTime;
			LocalDateTime nextEnd = list[i+1].endTime;
			list[i].startTime = nextStart;
			list[i].endTime = nextEnd;
		}
		size -= 1;
	}
	
	/**
	 * Sets the breakPeriod for this
	 * @param breakPeriod the breakPeriod to set
	 */
	public void setBreakPeriod(int breakPeriod) {
		if (breakPeriod < 0) {
			throw new IllegalArgumentException("Invalid break period");
		}
		this.breakPeriod = breakPeriod;
	}
	
	/**
	 * @return the size
	 */
	public int size() {
		return size;
	}
	
	/**
	 * @return a string with each TimeBlock's startTime and endTime, with each TimeBlock on a new
	 * line
	 */
	public String toString(){
		String rtnString = "";
		for (int i = 0; i < size; i++) {
			rtnString += list[i].startTime.toString() + " - " + list[i].endTime.toString()+ "\n";
		}
		return rtnString;
	}
	
}