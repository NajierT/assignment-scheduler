package io.github.najiert.assignmentscheduler.manager;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.Test;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;

import io.github.najiert.assignmentscheduler.schedule.ScheduleArray;
import io.github.najiert.assignmentscheduler.service.*;
class AssignmentSchedulerTest {
	
	

	/**
	 * Tests that 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	void testSameSchedule() throws GeneralSecurityException, IOException, InterruptedException {
		Calendar c = CalendarService.getUserCalendar();
		AssignmentScheduler as = new AssignmentScheduler(c, 15);
		LocalTime startTime = LocalTime.of(7, 0);
		LocalTime endTime = LocalTime.of(20, 0);
		as.setSchedulingPeriod(DayOfWeek.MONDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.TUESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.WEDNESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.THURSDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.FRIDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SATURDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SUNDAY, startTime, endTime);
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.MONDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.TUESDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.WEDNESDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.THURSDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.FRIDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.SATURDAY));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.SUNDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.MONDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.TUESDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.WEDNESDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.THURSDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.FRIDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.SATURDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.SUNDAY));
	}
	
	@Test
	void testDifferentSchedule() throws GeneralSecurityException, IOException, InterruptedException {
		Calendar c = CalendarService.getUserCalendar();
		AssignmentScheduler as = new AssignmentScheduler(c, 15);
		LocalTime startTime = LocalTime.of(7, 0);
		LocalTime endTime = LocalTime.of(20, 0);
		as.setSchedulingPeriod(DayOfWeek.TUESDAY, startTime, endTime);
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.TUESDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.TUESDAY));
		as.setSchedulingPeriod(DayOfWeek.THURSDAY, LocalTime.of(9, 0),  LocalTime.of(19, 0));
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.TUESDAY));
		assertEquals(endTime, as.getEndScheduling(DayOfWeek.TUESDAY));
		assertEquals(LocalTime.of(9, 0), as.getStartScheduling(DayOfWeek.THURSDAY));
		assertEquals(LocalTime.of(19, 0), as.getEndScheduling(DayOfWeek.THURSDAY));
	}
	
	/**
	 * This test was for a specific calendar, so it won't work properly 
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	void testFreeTime() throws GeneralSecurityException, IOException, InterruptedException {
		Calendar c = CalendarService.getUserCalendar();
		// start scheduling
		// est is -5 hours from UTC
		
		LocalDateTime today = LocalDateTime.of(2023, 12, 26, 7, 0);
		Instant instant = today.atZone(ZoneId.of("US/Eastern")).toInstant();
		today = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		System.out.println(today.toString());
		// set 30 minute minimum work period
		AssignmentScheduler as = new AssignmentScheduler(c, 10, today, 30);
		ArrayList<CalendarListEntry> eC = CalendarService.getCalendarList(c);
		assertEquals(0, as.getEventCalendars().size());
		//Add google calendars to consider scheduling
		for (CalendarListEntry cLE : eC) {
			System.out.println(cLE.getSummary());
		}
		as.addToEventCalendars(eC.get(0));
		// check time zone
		ArrayList<Event> eventList = as.getEventList(today.plusHours(1), today.withHour(23 - 5));
		String firstEventName = eventList.get(0).getSummary();
		LocalDateTime firstEventStartTime = AssignmentScheduler.toLDT(eventList.get(0).getStart().getDateTime());
		System.out.printf("First event start Time: %s    %s\n", firstEventName, firstEventStartTime.toString());
		ScheduleArray s = as.getSchedule();
		
		// Add 5 assignments due 2 days from now
		LocalTime startTime = LocalTime.of(7 - 5, 0);
		LocalTime endTime = LocalTime.of(23 - 5, 00);
		as.setSchedulingPeriod(DayOfWeek.MONDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.MONDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.TUESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.WEDNESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.THURSDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.FRIDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SATURDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SUNDAY, startTime, endTime);
		as.addAssignmentToList("Status update 1", today.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(23 - 5).withMinute(55),260, "11");
		as.addAssignmentToList("3.1 Draft", today.truncatedTo(ChronoUnit.DAYS).plusDays(1).withHour(23 - 5).withMinute(55),260, "11");
		as.addAssignmentToList("ex3", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23 - 5).withMinute(55),260, "11");
		as.addAssignmentToList("quiz3", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23 - 5).withMinute(55),260, "11");
		as.addAssignmentToList("ex4", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23 - 5).withMinute(55),260, "11");
		
		assertEquals(1300,as.getTotalTimeRequired());
		as.clearAssignments(eC.get(0).getId());
		try {
		    Thread.sleep(1000 * 5); // pause for 10 seconds
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		int sameStartDifEnd = as.findFreeTimeFromCalendar(today, today.truncatedTo(ChronoUnit.DAYS).withHour(23 - 5).withMinute(55));
		assertEquals(420, sameStartDifEnd);
		assertEquals(4, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		// check free time with same startPoint and endPoint as startScheduling and EndScheduling
		int sameStartAndEnd = as.findFreeTimeFromCalendar(today, today.truncatedTo(ChronoUnit.DAYS).withHour(23 - 5));
		assertEquals(420, sameStartAndEnd);
		assertEquals(4, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		// check free time with different startPoint but same endPoint as EndScheduling
		int diffStartSameEnd = as.findFreeTimeFromCalendar(today.plusHours(1), today.truncatedTo(ChronoUnit.DAYS).withHour(23 - 5));
		assertEquals(370, diffStartSameEnd);
		assertEquals(3, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		int diffStartDifEnd = as.findFreeTimeFromCalendar(today.plusHours(1), today.truncatedTo(ChronoUnit.DAYS).withHour(23 - 6));
		assertEquals(310, diffStartDifEnd);
		assertEquals(3, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		// check free time with day of no events
		int dayNoEvents = as.findFreeTimeFromCalendar(today.plusDays(3), today.plusDays(3).truncatedTo(ChronoUnit.DAYS).withHour(23 - 5));
		assertEquals(960, dayNoEvents);
		assertEquals(1, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		// check free time with one event
		int dayOneEvent = as.findFreeTimeFromCalendar(today.minusDays(2), today.minusDays(1).plusMinutes(15));
		//System.out.println("Error Here");
		//System.out.println(s.toString());
		assertEquals(960, dayOneEvent);
		assertEquals(1, s.size());
		s.clear();
		
		//check free time with start and end times out of bounds 
		int outOfBounds = as.findFreeTimeFromCalendar(today.minusHours(3), today.truncatedTo(ChronoUnit.DAYS).withHour(23 - 5).withMinute(55));
		assertEquals(420, outOfBounds);
		assertEquals(4, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		//check next day
		int nextDay = as.findFreeTimeFromCalendar(today.plusDays(1), today.plusDays(1).truncatedTo(ChronoUnit.DAYS).withHour(23 - 5).withMinute(55));
		assertEquals(440, nextDay);
		assertEquals(3, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		int nextNextDay = as.findFreeTimeFromCalendar(today.plusDays(2), today.plusDays(2).truncatedTo(ChronoUnit.DAYS).withHour(23 - 5).withMinute(55));
		assertEquals(440, nextNextDay);
		assertEquals(3, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		
		// check free time across multiple days
		int multipleDays = as.findFreeTimeFromCalendar(today, today.plusDays(2).truncatedTo(ChronoUnit.DAYS).withHour(23 - 5).withMinute(55));
		assertEquals(1300, multipleDays);
		assertEquals(10, s.size());
		//System.out.println("");
		//System.out.println(s.toString());
		s.clear();
		as.setAssignmentSchedules(eC.get(0).getId());
	}
	
	//@Test
	void test25minWorkPeriod() throws GeneralSecurityException, IOException, InterruptedException {
		Calendar c = CalendarService.getUserCalendar();
		LocalDateTime today = LocalDateTime.of(2024, 5, 16, 10, 0);
		Instant instant = today.atZone(ZoneId.of("US/Eastern")).toInstant();
		today = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		// set 30 minute minimum work period
		AssignmentScheduler as = new AssignmentScheduler(c, 30, today, 60);
		ArrayList<CalendarListEntry> eC = CalendarService.getCalendarList(c);
		as.addToEventCalendars(eC.get(8));
		LocalTime startTime = LocalTime.of(10 - 6, 0);
		LocalTime endTime = LocalTime.of(23 - 6, 0);
		//System.out.println(eC.get(8).getSummary());
		as.setSchedulingPeriod(DayOfWeek.MONDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.TUESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.WEDNESDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.THURSDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.FRIDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SATURDAY, startTime, endTime);
		as.setSchedulingPeriod(DayOfWeek.SUNDAY, startTime, endTime);
		assertEquals(startTime, as.getStartScheduling(DayOfWeek.MONDAY));
		as.addAssignmentToList("quiz4", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23-6).withMinute(55),30, "11");
		as.addAssignmentToList("ex5", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("quiz5", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23-6).withMinute(55),30, "11");
		as.addAssignmentToList("3.2", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("td opt draft", today.truncatedTo(ChronoUnit.DAYS).plusDays(2).withHour(23-6).withMinute(55),120, "11");
		
		as.addAssignmentToList("td peer review", today.truncatedTo(ChronoUnit.DAYS).plusDays(3).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("td final", today.truncatedTo(ChronoUnit.DAYS).plusDays(5).withHour(23-6).withMinute(55),60, "11");
		as.addAssignmentToList("ec 4.1", today.truncatedTo(ChronoUnit.DAYS).plusDays(7).withHour(23-6).withMinute(55),60, "11");
		as.addAssignmentToList("4.2", today.truncatedTo(ChronoUnit.DAYS).plusDays(7).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("op email dft", today.truncatedTo(ChronoUnit.DAYS).plusDays(9).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("4.3 schd and costs", today.truncatedTo(ChronoUnit.DAYS).plusDays(10).withHour(23-6).withMinute(55),120, "11");
		as.addAssignmentToList("peer review em tr", today.truncatedTo(ChronoUnit.DAYS).plusDays(10).withHour(23-6).withMinute(55),60, "11");
		as.addAssignmentToList("Em tr final", today.truncatedTo(ChronoUnit.DAYS).plusDays(12).withHour(23-6).withMinute(55),60, "11");
		as.addAssignmentToList("CSC pj2", today.truncatedTo(ChronoUnit.DAYS).plusDays(13).withHour(23-6).withMinute(55),8*60, "11");
		
		
		as.clearAssignments("primary");
		try {
		    Thread.sleep(1000); // pause for 1 second
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		as.setAssignmentSchedules("primary");
	}
}
