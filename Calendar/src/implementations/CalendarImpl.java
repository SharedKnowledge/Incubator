package implementations;

import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.ChangedAppointmentListener;
import interfaces.User;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import utils.AppointmentStartComparator;
import utils.Utils;
import exceptions.CalendarException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TimeSemanticTag;
import net.sharkfw.knowledgeBase.filesystem.FSSharkKB;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.SyncKP;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.system.SharkSecurityException;

/**
 * This class implements a {@link Calendar} using a SharkKB to store and 
 * receive Appointments from and a SyncKP to transfer Appointments and 
 * Comments to other instances.
 * 
 * Please not that the SharkKB files that are stored on the file system 
 * (directory specified in the constructor) should not be used by other 
 * Applications using shark-fw to avoid conflicts.
 * 
 */
public class CalendarImpl implements Calendar {
	
	/** the connection timeout used by SharkEngine */
	public final static int CONNECTION_TIMEOUT = 2000;
	/** the name of the property used to identify Appointments and Comments */
	public final static String TYPE_KEY = "type";
	
	private final static String EXAMPLE_URL = "tcp://example.com";
	
	private SharkKB kb;
	private SyncKB skb;
	private SyncKP skp;
	private J2SEAndroidSharkEngine engine;
	private String host;
	private int port;
	private String sharkkb_dir;
	private boolean auto_connect = false;

	/**
	 * This constructor sets up a new Calendar by initializing the used 
	 * SharkKB, SharkEngine and SyncKP.
	 * 
	 * @param username	the name of the User that uses this instance (used as 
	 * 									creator of Appointments and Comments 
	 * 									created by this Calendar)
	 * @param email			the (unique)email address of the User that uses this 
	 * 									instance (used as 
	 * 									creator of Appointments and Comments 
	 * 									created by this Calendar)
	 * @param hosturi		the host-URI of the User that uses this 
	 * 									instance (used as 
	 * 									creator of Appointments and Comments 
	 * 									created by this Calendar)
	 * @param storage_directory the directory in which the SharkKB that is used 
	 * 													to store Appointments and Comments is persisted 
	 * 													by FSSharkKB
	 * @param connect		whether a connection of the used SharkEngine should be 
	 * 									set up on initialization 
	 * 									(otherwise {@link #startConnection()} can be used), if 
	 * 									this is the case, it can be closed again using 
	 * 									{@link #closeConnection()}
	 * @throws CalendarException in case initialization fails
	 * @throws IOException	in case setting up a connection fails
	 */
	public CalendarImpl(String username, 
											String email, 
											URI hosturi,
											String storage_directory, 
											boolean connect) 
				 throws CalendarException, IOException {
		
		if (  username == null || username.length() == 0 || 
				  email == null || email.length() == 0 ||
				  hosturi == null || storage_directory == null || 
				  storage_directory.length() == 0 ) {
			throw new IllegalArgumentException("No null- or empty arguments allowed");
		}
		this.host = hosturi.getHost();
		this.port = hosturi.getPort();
		this.sharkkb_dir = storage_directory;
		this.auto_connect = connect;
		engine = new J2SEAndroidSharkEngine();
		engine.setConnectionTimeOut(CONNECTION_TIMEOUT);
		try {
			kb = new FSSharkKB(sharkkb_dir);
		}
		catch (SharkKBException e) {
			throw new CalendarException(
					"Unable to setup storage in this directory: " + sharkkb_dir, e);
		}
		PeerSemanticTag owner = InMemoSharkKB.createInMemoPeerSemanticTag(username,
				new String[] { email }, new String[] { host + ":" + port });
		kb.setOwner(owner);
		try {
			skb = new SyncKB(kb);
			skp = new SyncKP(engine, skb, 2);
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to set up SyncKP", e);
		}
		if ( connect ) {
			startConnection();
		}
	}

	@Override
	public boolean hasAppointments() {
		return this.getAllAppointments(false).size() > 0;
	}

	@Override
	public void removeAppointment(Appointment appointment) {
		appointment.removeFromCalendar();
	}

	@Override
	public void addCommentToAppointment(String content, Appointment appointment) 
			throws IOException, IllegalArgumentException, CalendarException {
		appointment.createComment(content);
		if ( this.auto_connect ) {
			this.sendAndReceiveAppointments();
		}
	}

	@Override
	public Collection<User> getAllUsers() {
		LinkedList<User> users = new LinkedList<User>();
		try {
			Enumeration<PeerSemanticTag> enu = kb.getPeerSTSet().peerTags();
			while (enu.hasMoreElements()) {
				PeerSemanticTag pst = enu.nextElement();
				users.add(new UserImpl(pst));
			}
		}
		catch (SharkKBException e) {
			throw new RuntimeException("Unable to retrieve users from SharkKB.", e);
		}
		return users;
	}

	@Override
	public Collection<Appointment> getAppointmentsByTopic(String tag, 
																											boolean sorted) {
		return getAppointments(tag, null, null, 0l, null, sorted);
	}

	@Override
	public Collection<Appointment> getAppointmentsByTime(Date start,
			long duration_ms) {
		return getAppointments(null, null, start, duration_ms, null, false);
	}

	@Override
	public Collection<Appointment> getAppointmentsByLocation(String location_tag, 
																													 boolean sorted) {
		return getAppointments(null, null, null, 0l, location_tag, sorted);
	}

	@Override
	public Collection<Appointment> getAppointmentsByCreator(User creator, 
																													boolean sorted) {
		return getAppointments(null, creator, null, 0l, null, sorted);
	}

	@Override
	public Collection<Appointment> getAppointments(String tag, User creator,
			Date start, long duration, String location_name, boolean sorted) {
		ArrayList<Appointment> appointments = new ArrayList<Appointment>();
		try {
			SemanticTag topic = tag == null ? null : kb.createSemanticTag(tag,
					Utils.stringToURI(tag));
			TimeSemanticTag time = start == null ? null : kb.createTimeSemanticTag(
					start.getTime(), duration);
			PeerSemanticTag origin = creator == null ? null : InMemoSharkKB
					.createInMemoPeerSemanticTag(creator.getName(),
							creator.getEmailAddress(), creator.getHost());
			ContextCoordinates cc = kb.createContextCoordinates(topic, origin, null,
					null, time, null, SharkCS.DIRECTION_INOUT);
			Enumeration<ContextPoint> e = kb.getContextPoints(cc);
			System.out.println("e: " + e);
			while (e != null && e.hasMoreElements()) {
				ContextPoint cp = e.nextElement();
				Iterator<Information> e_info = kb.getContextPoint(
						cp.getContextCoordinates()).getInformation();
				while (e_info.hasNext()) {
					// location-as-property workaround, no direct lookup because
					// using SyncKB and SpatialSemanticTags together seemed to
					// not really work out ...
					Information curr_i = e_info.next();
					if ( curr_i.getProperty(TYPE_KEY).equals(AppointmentImpl.TYPE_NAME)) {
						if (location_name == null
								|| curr_i.getProperty(AppointmentImpl.LOCATION_PROPERTY_NAME) == location_name) {
							appointments.add(new AppointmentImpl(this, kb, cp, curr_i));
						}
					}
				}
			}
		}
		catch (SharkKBException e) {
			throw new RuntimeException("Unable to access appointments from SharkKB", 
																	e);
		}
		if ( sorted ) {
			Collections.sort(appointments, new AppointmentStartComparator());
		}
		return appointments;
	}

	@Override
	public Collection<Appointment> getAllAppointments(boolean sorted) {
		return getAppointments(null, null, null, 0, null, sorted);
	}

	@Override
	public Appointment createAppointment(String tag, Date start, long duration,
			String location, String description) 
					throws CalendarException, IOException {
		if ( tag == null || tag.length() == 0 || start == null || location == null 
				 || location.length() == 0 || description == null ) {
			throw new IllegalArgumentException("No null or empty value allowed.");
		}
		try {
			TimeSemanticTag time = kb
					.createTimeSemanticTag(start.getTime(), duration);
			SemanticTag curr_tag = kb.createSemanticTag(tag, Utils.stringToURI(tag));
			PeerSemanticTag rec = null;
			ContextCoordinates cc = kb.createContextCoordinates(curr_tag,
					kb.getOwner(), null, rec, time, null, SharkCS.DIRECTION_INOUT);
			ContextPoint cp = kb.createContextPoint(cc);
			//cp.setListener(new AppointmentInformationListener());
			Information i = cp.addInformation(description);
			i.setProperty(AppointmentImpl.LOCATION_PROPERTY_NAME, location);
			i.setProperty(TYPE_KEY, AppointmentImpl.TYPE_NAME);
			AppointmentImpl a = new AppointmentImpl(this, kb, cp, i);
			if ( this.auto_connect ) {
				this.sendAndReceiveAppointments();
			}			
			return a;
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to create appointment in SharkKB", e);
		}

	}

	@Override
	public User createUser(String name, String email, URI host) 
						throws CalendarException, IllegalArgumentException {
		if (  name == null || name.length() == 0 || email == null || 
				  email.length() == 0 || host == null ) {
			throw new IllegalArgumentException("No null- or empty arguments allowed");
		}
		if ( this.userExists(email) ) {
			throw new IllegalArgumentException("User with this email already exists.");
			
		}
		User u = null;
		PeerSemanticTag pst = null;
		try {
			pst = kb.createPeerSemanticTag(name, email, host.toString());
			kb.getPeerSTSet().merge(pst);
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to create user in SharkKB", e);
		}
		u = new UserImpl(pst);
		return u;
	}
	
	@Override
	public void removeUser(User u) throws CalendarException {
		removeUser(u.getEmailAddress());
	}

	@Override
	public void removeUser(String email) 
								throws CalendarException, IllegalArgumentException {
		if ( !userExists(email) ) {
			throw new IllegalArgumentException("User with email: " + email + 
					" does not exist!");
		}
		try {
			kb.getPeerSTSet().removeSemanticTag(
					InMemoSharkKB.createInMemoPeerSemanticTag(email, email,
							EXAMPLE_URL));
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to remove user from SharkKB", e);

		}
	}

	@Override
	public boolean userExists(String email) throws CalendarException {
		boolean exists = false;
		PeerSemanticTag pst = InMemoSharkKB.createInMemoPeerSemanticTag(email,
				email, EXAMPLE_URL);
		try {
			exists = kb.getPeerSTSet().fragment(pst).peerTags().hasMoreElements();
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to retrieve users from SharkKB", e);
			
		}
		return exists;
	}

	/**
	 * This method start a new TCP-connection by
	 * the used Shark engine.
	 * 
	 * @throws IOException in case an error in setting up the connection occurs
	 */
	public void startConnection() throws IOException {
		engine.startTCP(port);
		engine.setConnectionTimeOut(2000);
	}

	/**
	 * This method closes a connection of the used Shark engine.
	 * 
	 */
	public void closeConnection() {
		engine.stopTCP();
	}

	@Override
	public void sendAndReceiveAppointments() throws IOException {
		skp.syncAllKnowledge();
		try {
			Enumeration<PeerSemanticTag> e = kb.getPeerSTSet().peerTags();
			while (e.hasMoreElements()) {
				PeerSemanticTag curr = e.nextElement();
				if (!SharkCSAlgebra.identical(curr, kb.getOwner())) {
					engine.publishAllKP(curr);
				}
			}
		}
		catch (IOException | SharkSecurityException | SharkKBException e) {
			throw new IOException("Error when trying to synchronize appointments", e);
		}
	}

	@Override
	public User getUser(String email) 
							throws CalendarException, IllegalArgumentException {
		if ( email == null || email.length() == 0 || !this.userExists(email) ) {
			throw new IllegalArgumentException("Non-existing email address"
					+ " was passed: "	+ email);
		}
		PeerSemanticTag user = null;
		try {
			user = kb.getPeerSTSet().getSemanticTag(new String[] { email });
		}
		catch (SharkKBException e) {
			throw new CalendarException("Unable to retrieve user from SharkKB", e);
		}
		return new UserImpl(user);
	}
	

	/**
	 * This method has no tested implementation yet, therefore it is not 
	 * supported due to a seemingly missing support of ContextPointListener.
	 * 
	 * @see TestContextPointListenerCallback
	 * 
	 */
	@Override
	public void registerAppointmentListener(ChangedAppointmentListener handler) {
		throw new UnsupportedOperationException("Function not implemented due "
				+ "to problems with InformationListener "
				+ "(see bug_tests/TestContextPointListenerCallback)"); 
/*		
		app_change_handler = new AppointmentUpdatedListener(this, handler, kb);
		kb.addListener(app_change_handler);
		AppointmentInformationListener kp_listener = new AppointmentInformationListener(this, kb, handler);
		try {
			Enumeration<ContextPoint> e = kb.getAllContextPoints();
			while ( e.hasMoreElements() ) {
				ContextPoint cp = e.nextElement();
				cp.setListener(kp_listener);
			}
		}
		catch (SharkKBException e) {
			e.printStackTrace();
		}
*/		
	}

}