package io.github.najiert.assignmentscheduler.assignment;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.najiert.assignmentscheduler.assignment.Assignment;

import java.time.LocalDateTime;

/**
 * 
 */

/**
 * @author Najier
 *
 */
class AssignmentTest {

	@Test
	void test() {
		Assignment a1 = new Assignment("MAE 201", LocalDateTime.now().plusDays(3).plusHours(3), 7);
		assertEquals("MAE 201", a1.getName());
		assertEquals(75 * 60, a1.getTimeRemaining());
	}
	
	@Test
	void testInvalid() {
		//Test Invalid name
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("", LocalDateTime.now().plusYears(1), 7));
		
		//Test Invalid name
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment(null, LocalDateTime.now().plusYears(1), 7));
		//Day before today
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().minusDays(1), 7));
		//Hour before now
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().minusHours(1), 7));
		// minute before now
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().minusMinutes(1), 7));
		// not enough time
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().plusHours(1).plusMinutes(59), 2));
		//Invalid time to complete
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().plusYears(1), 0));
		assertThrows(IllegalArgumentException.class,
				()-> new Assignment("csc", LocalDateTime.now().plusYears(1), -1));
	}
}
