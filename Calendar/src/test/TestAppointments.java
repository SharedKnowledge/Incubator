package test;

import static org.junit.Assert.*;
import implementations.CalendarImpl;
import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.User;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.Utils;
import exceptions.CalendarException;


/**
 * This class tests functionalities that involve the creation, removal and 
 * retrieval of Appointments.
 * 
 *
 */
public class TestAppointments {
	
	private final static String MYTAG = "mytag";
	private final static String ALICE_EMAIL = "alice@example.com";	
	private final static long DURATION = 60*60*1000;
	private final static String LOCATION = "berlin";
	private final static String ALICE_DIR = "alice_test_folder";
	private final static String BOB_DIR = "bob_test_folder";	
	
	private Calendar cal;
	private Appointment app;
	private Date start = new Date();
		
	@Before
	public void setUp() throws Exception {
		cal = new CalendarImpl("alice", ALICE_EMAIL, 
				new URI("http://localhost:5555"), ALICE_DIR, false);
		app = cal.createAppointment(MYTAG, start, 
				DURATION, LOCATION, "mydescription");
		System.out.println(app);
	}
	
	@After
	public void tearDown() {
		Utils.deleteDirectory(new File(ALICE_DIR));
		Utils.deleteDirectory(new File(BOB_DIR));

	}

	/**
	 * This test checks whether created appointments can be retrieved by their 
	 * tag-property.
	 */
	@Test
	public void testRetrieveAppointmentByTag() {
		Collection<Appointment> apps = cal.getAppointmentsByTopic(MYTAG, false);
		assertEquals(apps.size(), 1);
		for ( Appointment curr_app : apps ) {
			System.out.println(curr_app);
			assertEquals(app.toString(), curr_app.toString());
		}
	}
	
	/**
	 * This test checks whether a created appointment can be retrieved by its 
	 * creator-property.
	 * 
	 * @throws CalendarException
	 */
	@Test
	public void testRetrieveAppointmentByCreator() throws CalendarException {
		User u = cal.getUser(ALICE_EMAIL);
		Collection<Appointment> apps = cal.getAppointmentsByCreator(u, false);
		assertEquals(apps.size(), 1);
		for ( Appointment curr_app : apps ) {
			assertEquals(app.toString(), curr_app.toString());
		}
	}
	
	/**
	 * This test checks whether a created appointment can be retrieved by its 
	 * location-name-property.
	 */
	@Test
	public void testRetrieveAppointmentByLocation() {
		Collection<Appointment> apps = cal.getAppointmentsByLocation(LOCATION, 
																																 false);
		assertEquals(apps.size(), 1);
		for ( Appointment curr_app : apps ) {
			assertEquals(app.toString(), curr_app.toString());
		}		
	}
	
	/**
	 * This test checks whether a created appointment can be retrieved by its 
	 * starting date.
	 */
	@Test
	public void testRetrieveAppointmentByStartDate() {
		Collection<Appointment> apps = cal.getAppointments(null, null, 
																							start, DURATION, null, false);
		assertEquals(apps.size(), 1);
		for ( Appointment curr_app : apps ) {
			assertEquals(app.toString(), curr_app.toString());
		}
	}
	
	/**
	 * This test checks whether a created appointment can be retrieved by its 
	 * starting date.
	 */
	@Test
	public void testRetrieveAppointmentByStartDate2() {
		Collection<Appointment> apps = cal.getAppointmentsByTime(start, DURATION);
		assertEquals(apps.size(), 1);
		for ( Appointment curr_app : apps ) {
			assertEquals(app.toString(), curr_app.toString());
		}
	}
	
	@Test
	public void testGetAppointmentsWhenEmpty() throws URISyntaxException, CalendarException, IOException {
		Calendar cal = new CalendarImpl("bob", "bob@example.com",
				new URI("http://localhost:5556"), BOB_DIR, false);
		Collection<Appointment> apps = cal.getAllAppointments(false);
		assertEquals(apps.size(), 0);
	}
	
	/**
	 * This test checks whether a created appointment can be successfully deleted.
	 */
	@Test
	public void testDeleteAppointment() {
		cal.removeAppointment(app);
		System.out.println("number: " + cal.getAllAppointments(false).size());
		assertEquals(false, cal.hasAppointments());
	}
	
}
