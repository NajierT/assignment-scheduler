package io.github.najiert.assignmentscheduler.schedule;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ScheduleArrayTest {
	
	private static final LocalDateTime TEST_DATE = LocalDateTime.of(LocalDate.of(2023, 1, 1), LocalTime.NOON);

	@Test
	void testScheduleArray() {
		ScheduleArray s = new ScheduleArray();
		assertEquals(0, s.size());
		assertEquals(15, s.getBreakPeriod());
		s.add(TEST_DATE, TEST_DATE.plusHours(1));
		
		assertEquals(TEST_DATE, s.getStart(0));
		assertEquals(TEST_DATE.plusHours(1), s.getEnd(0));
		assertEquals(1, s.size());
		
		s.add(TEST_DATE.minusHours(2), TEST_DATE.minusHours(1));
		System.out.println(s.toString());
		s.add(TEST_DATE.plusHours(3), TEST_DATE.plusHours(4));
		System.out.println(s.toString());
		assertEquals(TEST_DATE, s.getStart(1));
		assertEquals(TEST_DATE.plusHours(1), s.getEnd(1));
		assertEquals(TEST_DATE.minusHours(2), s.getStart(0));
		assertEquals(TEST_DATE.minusHours(1), s.getEnd(0));
		assertEquals(TEST_DATE.plusHours(3), s.getStart(2));
		assertEquals(TEST_DATE.plusHours(4), s.getEnd(2));
		assertEquals(3, s.size());
		
		// invalid tests
		assertThrows(IllegalArgumentException.class,
				()-> s.getStart(4));
		assertThrows(IllegalArgumentException.class,
				()-> s.remove(4));
		assertThrows(IllegalArgumentException.class,
				()-> s.remove(-1));
		assertThrows(IllegalArgumentException.class,
				()-> s.add(TEST_DATE, TEST_DATE.plusHours(2)));
		assertThrows(IllegalArgumentException.class,
				()-> s.add(TEST_DATE.plusHours(1), TEST_DATE.plusHours(2)));
		assertThrows(IllegalArgumentException.class,
				() -> s.add(TEST_DATE.plusHours(1).plusMinutes(14), TEST_DATE.plusHours(2)));
		assertThrows(IllegalArgumentException.class,
				()-> s.add(TEST_DATE.plusHours(1).plusMinutes(30), TEST_DATE.plusHours(2).plusMinutes(46)));
		assertThrows(IllegalArgumentException.class,
				()-> s.add(TEST_DATE.plusMinutes(30), TEST_DATE.plusHours(2)));
		
		// changing break period
		s.setBreakPeriod(10);
		s.add(TEST_DATE.plusHours(1).plusMinutes(14), TEST_DATE.plusHours(2));
		assertEquals(4, s.size());
		assertEquals(TEST_DATE.plusHours(1).plusMinutes(14), s.getStart(2));
		s.setBreakPeriod(15);
		assertEquals(4, s.size());
		System.out.println(s.toString());
		s.remove(2); // caused me so many problems, should probably test each method thoroughly...nah
		assertEquals(TEST_DATE.plusHours(3), s.getStart(2));
		assertEquals(TEST_DATE.plusHours(4), s.getEnd(2));
		assertEquals(3, s.size());
		System.out.println(s.toString());
		// testing different day but same time
		s.add(TEST_DATE.plusDays(1), TEST_DATE.plusDays(1).plusHours(1));
		assertEquals(4, s.size());
		assertEquals(TEST_DATE.plusDays(1), s.getStart(3));
		assertEquals(TEST_DATE.plusDays(1).plusHours(1), s.getEnd(3));
		s.add(TEST_DATE.plusDays(3), TEST_DATE.plusDays(3).plusHours(1));
		s.add(TEST_DATE.plusDays(4), TEST_DATE.plusDays(4).plusHours(1));
		// test that setting longer buffer doesn't affect previous times
		s.setBreakPeriod(70);
		s.add(TEST_DATE.plusHours(6), TEST_DATE.plusHours(7));
		assertEquals(7, s.size());
		
		System.out.println(s.toString());

	}                                                       
}
