package test;

import static org.junit.Assert.*;
import implementations.CalendarImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import exceptions.CalendarException;

/**
 * This class tests functionality of the Calendar-Facade that are not handled 
 * elsewhere 
 * (like everything that has to do with Users, Appointments, Comments).
 * 
 *
 */
public class TestCalendar {
	
	private static final String ALICE_NAME = "alice";
	private static final String ALICE_EMAIL = "alice@example.com";
	private static final String KB_DIR = "alice_test_folder";
	private static URI host;
	
	@Before
	public void setup() throws URISyntaxException {
		host = new URI("http://localhost:5555");
	}

	/**
	 * This test checks whether null-arguments in the constructor correctly 
	 * result in exceptions.
	 * 
	 * 
	 * @throws CalendarException
	 * @throws IOException
	 */
	@Test
	public void testConstructorNullParams() throws CalendarException, IOException {
		boolean ex_thrown = false;
		try {
				new CalendarImpl(null, ALICE_EMAIL, host, KB_DIR, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on null-argument", ex_thrown);
		
		ex_thrown = false;
		try {
				new CalendarImpl(ALICE_NAME, null, host, KB_DIR, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on null-argument", ex_thrown);	
		
		ex_thrown = false;
		try {
				new CalendarImpl(ALICE_NAME, ALICE_EMAIL, null, KB_DIR, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on null-argument", ex_thrown);	
		
		ex_thrown = false;
		try {
				new CalendarImpl(ALICE_NAME, ALICE_EMAIL, host, null, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on null-argument", ex_thrown);	
	}

	/**
	 * This test checks whether empty arguments in the constructor correctly 
	 * result in exceptions.
	 * 
	 * 
	 * @throws CalendarException
	 * @throws IOException
	 */
	@Test
	public void testConstructorEmptyParams() throws CalendarException, IOException {
		boolean ex_thrown = false;
		try {
				new CalendarImpl("", ALICE_EMAIL, host, KB_DIR, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on empty argument", ex_thrown);
		
		ex_thrown = false;
		try {
				new CalendarImpl(ALICE_NAME, "", host, KB_DIR, false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on empty argument", ex_thrown);	
		
		ex_thrown = false;
		try {
				new CalendarImpl(ALICE_NAME, ALICE_EMAIL, host, "", false);
		}
		catch (IllegalArgumentException e) {
			ex_thrown = true;
		}
		assertTrue("No exception thrown on empty argument", ex_thrown);	
	}
}
