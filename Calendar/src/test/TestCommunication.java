package test;

import static org.junit.Assert.*;
import implementations.CalendarImpl;
import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.Comment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.Utils;
import exceptions.CalendarException;


/**
 * This class tests the communication between two Calendar-Clients.
 *  
 * @author n
 *
 */
public class TestCommunication {
	
	private final static long DURATION = 60*60*1000;
	private final static String LOCATION = "berlin";
	private final static String MYTAG = "mytag";
	private final static String ALICE_MAIL = "alice@example.com";
	private final static String ALICE_NAME = "alice";	
	private final static String ALICE_DIR = "alice_test_folder";
	private final static String BOB_DIR = "bob_test_folder";	
	
	private CalendarImpl alice_cal;
	private CalendarImpl bob_cal;
	private Appointment app;
	private Comment comment_from_alice;

	private URI alice_host;
	private Date start = new Date();
	private URI bob_host;
	
		
	/**
	 * Changing the port is often necessary for the test to succeed.
	 * 
	 */
	@Before
	public void setUp() throws Exception {
		bob_host = new URI("tcp://localhost:5780");
		alice_host = new URI("tcp://localhost:5813");
		alice_cal = new CalendarImpl("alice", ALICE_MAIL, alice_host, ALICE_DIR, true);
		alice_cal.createUser("bob", "bob@example.com", bob_host);
		
		app = alice_cal.createAppointment(MYTAG, start, 
				DURATION, LOCATION, "mydescription");
		comment_from_alice = app.createComment("I'm a comment");
		bob_cal = new CalendarImpl("bob", "bob@example.com", bob_host, BOB_DIR, true);
		bob_cal.createUser(ALICE_NAME, ALICE_MAIL, alice_host);

	}
	
	@After
	public void tearDown() throws InterruptedException {
		Utils.deleteDirectory(new File(ALICE_DIR));
		Utils.deleteDirectory(new File(BOB_DIR));

    alice_cal.closeConnection();
    bob_cal.closeConnection();
    alice_cal = null;
    bob_cal = null;
    Thread.sleep(10);
	
	}

	

	/**
	 * Tests whether an Appointment is can be transfered from Alice to Bob 
	 * using CalendarImpl.sendAndRecieve().
	 * 
	 * @throws IOException 
	 * @throws CalendarException 
	 * 
	 * 
	 */
	@Test
	public void testCalComm() 
							throws InterruptedException, IOException, CalendarException {
		Thread.sleep(50);
		alice_cal.sendAndReceiveAppointments();
		Thread.sleep(50);
		bob_cal.sendAndReceiveAppointments();  

		Thread.sleep(4000);
		alice_cal.closeConnection();
		Thread.sleep(10);
		bob_cal.closeConnection();
		Thread.sleep(10);

		assertTrue(bob_cal.hasAppointments());
		Collection<Appointment> bobs_apps = bob_cal.getAllAppointments(false);
		assertEquals(1, bobs_apps.size());
		Appointment retrieved_app = bobs_apps.iterator().next();
		assertEquals(app.toString(), retrieved_app.toString());
		assertEquals("Comment was not transfered correctly.", 
								comment_from_alice.toString(), 
								retrieved_app.getComments(false).iterator().next().toString());
	}
	
	class CalRunner implements Runnable
	{
		private Calendar cal;
		
		public CalRunner(Calendar cal) {
			this.cal = cal;
		}
	  @Override 
	  public void run()
	  {
	    try {
				cal.sendAndReceiveAppointments();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
	  }
	}

}
