package test;

import static org.junit.Assert.*;
import implementations.CalendarImpl;
import interfaces.Calendar;
import interfaces.User;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.Utils;
import exceptions.CalendarException;

/**
 * This class contains tests for the creation, retrieval and deletion of users.
 * 
 *
 */
public class TestUsers {
	
	private final static String ALICE_EMAIL = "alice@example.com";
	private final static String ALICE_DIR = "alice_test_folder";
	private final static String BOB_NAME = "bob";
	private final static String BOB_EMAIL = "bob@example.com";
	
	private URI bob_host = null;
	private Calendar cal;
		
	
	@Before
	public void setUp() throws Exception {
		cal = new CalendarImpl("alice", ALICE_EMAIL, 
				new URI("http://localhost:5555"), ALICE_DIR, false);
		bob_host = new URI("http://localhost:5556");
	}

	@After
	public void tearDown() throws Exception {
		Utils.deleteDirectory(new File(ALICE_DIR));
	}	
	
	/**
	 * Tests whether passing null values in the Constructor throws exceptions 
	 * as it should.
	 * 
	 * @throws URISyntaxException if an invalid URI is passed
	 * @throws CalendarException 
	 * 
	 */
	@Test
	public void testCreateUserNullArguments() throws URISyntaxException, CalendarException {
		boolean exceptionthrown = false;
		try {
			cal.createUser(null, null, null);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);
		exceptionthrown = false;
		try {
			cal.createUser(BOB_NAME, BOB_EMAIL, null);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);		
		exceptionthrown = false;
		try {
			cal.createUser(BOB_NAME, null, bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);
		exceptionthrown = false;
		try {
			cal.createUser(null, BOB_EMAIL, bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);
	}
	
	/**
	 * Tests whether passing empty values in the Constructor throws exceptions 
	 * as it should.
	 * 
	 * @throws URISyntaxException if an invalid URI is passed
	 * @throws CalendarException 
	 * 
	 */
	@Test
	public void testEmptyArguments() 
												throws URISyntaxException, CalendarException {
		boolean exceptionthrown = false;
		try {
			cal.createUser("", "", bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);
		exceptionthrown = false;
		try {
			cal.createUser("", BOB_EMAIL, bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);		
		exceptionthrown = false;
		try {
			cal.createUser(BOB_NAME, "", bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No IllegalArgumentException was thrown.", exceptionthrown);
	}
	
	/**
	 * This test checks whether the properties of a created user 
	 * (name, location, host) can be retrieved correctly.	 * 
	 * 
	 * @throws URISyntaxException if the passed URI is invalid
	 * @throws CalendarException 
	 */
	@Test
	public void testUserProperties() throws URISyntaxException, CalendarException {
		User u = cal.createUser(BOB_NAME, BOB_EMAIL, bob_host);
		assertEquals(BOB_NAME, u.getName());
		assertEquals(BOB_EMAIL, u.getEmailAddress());
		assertEquals(bob_host.toString(), u.getHost());
	}
	
	/**
	 * This test checks whether a user can be successfully created and 
	 * deleted afterwards. 
	 * @throws CalendarException 
	 */
	@Test
	public void testDeleteUser() throws CalendarException {
		int size_before = cal.getAllUsers().size();
		User u = cal.createUser(BOB_NAME, 
				BOB_EMAIL, bob_host);
		assertEquals(size_before + 1, cal.getAllUsers().size());
		cal.removeUser(u.getEmailAddress());
		assertEquals(size_before, cal.getAllUsers().size());
	}
	
	/**
	 * This test checks whether Calendar.userExists() behaves correctly with 
	 * and without a matching user.
	 * @throws CalendarException 
	 */
	@Test
	public void testUserExists() throws CalendarException {
		assertFalse(cal.userExists(BOB_EMAIL));
		cal.createUser(BOB_NAME, BOB_EMAIL, bob_host);
		assertTrue(cal.userExists(BOB_EMAIL));
	}


	/**
	 * This test checks whether a user that was created has the same 
	 * properties after it is retrieved again.
	 * @throws CalendarException 
	 */
	@Test
	public void testRetrieveUsers() throws CalendarException {
		User created = cal.createUser(BOB_NAME, BOB_EMAIL, bob_host);
		User retrieved = cal.getUser(BOB_EMAIL);
		assertEquals(created.toString(), retrieved.toString());

	}
	
	/**
	 * This test checks whether creating a user with an email address that is 
	 * already used fails as it should.
	 * @throws CalendarException 
	 */
	@Test
	public void testCreateExistingUser() throws CalendarException {
		cal.createUser(BOB_NAME, BOB_EMAIL, bob_host);
		boolean exceptionthrown = false;
		try {
			cal.createUser(BOB_NAME + "_2", BOB_EMAIL, bob_host);
		}
		catch ( IllegalArgumentException e) {
			exceptionthrown = true;
		}
		assertTrue("No exception was thrown when creating a user with non-unique "
				+ "email address.", exceptionthrown);
		}
}