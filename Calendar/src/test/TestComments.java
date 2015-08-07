package test;

import static org.junit.Assert.*;
import implementations.CalendarImpl;
import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.Comment;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.Utils;
import exceptions.CalendarException;

/**
 * This class contains tests that check the retrieval and creation of comments.
 * 
 *
 */
public class TestComments {
	
	private final long duration = 60*60*1000;
	private final String location = "berlin";
	private Calendar cal;
	private Appointment app_1;
	private Appointment app_2;

	private final String mytag = "mytag";
	private final String creator_email = "alice@example.com";
	private Date start = new Date();
	private String alice_dir = "alice_test_folder";
	
	
	@Before
	public void setUp() throws Exception {
		cal = new CalendarImpl("alice", creator_email, 
				new URI("http://localhost:5555"), alice_dir, false);
		app_1 = cal.createAppointment(mytag, start, 
				duration, location, "mydescription");
		app_2 = cal.createAppointment(mytag + 2, start, 
				duration, location, "mydescription 2");
	}

	@After
	public void tearDown() throws Exception {
		Utils.deleteDirectory(new File(alice_dir));
	}

	/**
	 * This test checks whether comments created for two dates can be retrieved 
	 * completely, with full content (including creator and creation time) 
	 * and in association to the correct appointment.
	 * @throws CalendarException 
	 * @throws IllegalArgumentException 
	 * 
	 */
	@Test
	public void retrieveComments() throws IllegalArgumentException, CalendarException {
		LinkedList<Comment> app_1_comments = new LinkedList<Comment>();
		app_1_comments.add(app_1.createComment("my first comment "));
		app_1_comments.add(app_1.createComment("my second first comment "));
		app_1_comments.add(app_1.createComment("my second comment "));
		app_1_comments.add(app_1.createComment("my third comment "));
		app_1_comments.add(app_1.createComment("my fourth comment "));
		app_1_comments.add(app_1.createComment("my fifth comment "));
		
		LinkedList<Comment> app_2_comments = new LinkedList<Comment>();
		app_2_comments.add(app_2.createComment("my second comment "));
		app_2_comments.add(app_2.createComment("my third comment "));
		app_2_comments.add(app_2.createComment("my fourth comment "));
		app_2_comments.add(app_2.createComment("my fifth comment "));
		
		
		Collection<Comment> app_1_retrieved_comments = app_1.getComments(false);
		assertEquals(app_1_comments.size(), app_1_retrieved_comments.size());
		for ( Comment c : app_1_retrieved_comments ) {
			boolean equal_to_created_one = false;
			for ( Comment c_2 : app_1_comments ) {

				if (  c.toString().equals(c_2.toString()) ) {
					equal_to_created_one = true;
					break;
				}
			}
			assertTrue(equal_to_created_one);
		}

		Collection<Comment> app_2_retrieved_comments = app_2.getComments(false);
		assertEquals(app_2_comments.size(), app_2_retrieved_comments.size());
		for ( Comment c : app_2_retrieved_comments ) {
			boolean equal_to_created_one = false;
			for ( Comment c_2 : app_2_comments ) {
				if (  c.toString().equals(c_2.toString()) ) {
					equal_to_created_one = true;
					break;
				}
			}
			assertTrue(equal_to_created_one);
		}
	}
	
	
	/**
	 * This test checks whether the creation of a comment without or with 
	 * empty context does fail (as it should).
	 * @throws CalendarException 
	 * 
	 */
	@Test
	public void createEmptyComment() throws CalendarException {
		boolean ex_on_empty = false;
		boolean ex_on_null = false;
		try {
			app_1.createComment("");
		}
		catch ( IllegalArgumentException e) {
			ex_on_empty = true;
		}
		try {
			app_1.createComment(null);
		}
		catch ( IllegalArgumentException e) {
			ex_on_null = true;
		}
		assertTrue("No exception was thrown with empty content for comment.", 
							 ex_on_empty);
		assertTrue("No exception was thrown with null-content for comment.", 
				 ex_on_null);
	}

	/**
	 * This test checks whether an Appointment without comments returns an 
	 * empty collection of comments as expected.
	 * @throws CalendarException 
	 * 
	 */
	@Test
	public void getCommentsWhenEmpty() throws CalendarException {
		Collection<Comment> comments = app_1.getComments(false);
		assertEquals(0, comments.size());
	}
}
