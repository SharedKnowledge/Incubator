package interfaces;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

import exceptions.CalendarException;

/**
 * This interface specifies a calendar that can create, store and exchange 
 * <code>Appointments</code> (which themselves can have <code>Comments</code>) 
 * with other instances using a network connection.
 * 
 * Once an <code>Appointment</code> was created, it can be send to all known 
 * <code>Users</code> (that were once added), likewise 
 * <code>Appointments</code> can be received from other Calendars.
 * This also applies to Appointments (and their Comments) that were just 
 * received from others and not created by the current instance.
 * Therefore, every Appointment is (potentially) public and, accordingly, 
 * Users should only be added when trusted.
 * 
 * The Calendar works as a facade to all other classes, instances of 
 * Appointment, User and Comment can not be created without it and are bound 
 * to the Calendar's persistence layer.
 * 
 * @see Appointment
 * @see Comment
 * @see User
 *
 */
public interface Calendar {

	/**
	 * This method returns a Collection containing all <code>Appointments</code> 
	 * with the passed <code>topic</code>, optionally sorted by their start date.
	 * 
	 * @see Appointment
	 * 
	 * @param topic	the topic the returned Appointments belong to, if set to null 
	 * 				the topic will not be used as a filter 
	 * @param sorted true if the returned Appointments should be sorted by their 
	 * 				start date, false otherwise
	 * @return the (optionally sorted) Collection of Appointments with the passed 
	 * 				 <code>topic</code>
	 */
	public Collection<Appointment> getAppointmentsByTopic(String topic, 
																							boolean sorted);
	
	/**
	 * This method returns this Calendars Appointments with the passed start  
	 * date.
	 * 
	 * @see Appointment
	 * 
	 * 
	 * @param start the start date the Appointments are requested have, 
	 * 				ignored as a filter when set to null
	 * @param duration_ms the duration of the Appointment, not relevant as a 
	 * 				filter
	 * @return the (unsorted) Collection of Appointments with the specified 
	 * 				 start date
	 */
	public Collection<Appointment> getAppointmentsByTime(Date start, 
																							long duration_ms);
	
	/**
	 * This method returns the Appointments of this Calendar with the specified 
	 * location, optionally sorted by their start date.
	 * 
	 * @see Appointment
	 * 
	 * @param location_name the String specifying the location of the returned 
	 * 				Appointments
	 * @param sorted true if the returned Appointments should be sorted by their 
	 * 				start date, false otherwise
	 * @return a Collection holding the Appointments with the specified location
	 */
	public Collection<Appointment> getAppointmentsByLocation(String location_name,
																													 boolean sorted);
	/**
	 * This method returns the Appointments of this Calendar were created by 
	 * the specifies User.
	 * 
	 * @see Appointment
	 * @see User
	 * 
	 * @param creator the User that created the Appointments that are returned, 
	 * 				if null the creator is ignored
	 * @param sorted true if the returned Appointments should be sorted by their 
	 * 				start date, false otherwise
	 * @return the Appointments created by the specified <code>creator</code>
	 */
	public Collection<Appointment> getAppointmentsByCreator(User creator, 
																													boolean sorted);
	
	/**
	 * This method returns all Appointments of this Calendar, optionally 
	 * sorted by their start date.
	 * 
	 * @see Appointment
	 * 
	 * @param sorted true if the returned Appointments should be sorted by 
	 * 				their start date, false otherwise
	 * @return	the Appointments stored by this Calendar instance
	 */
	public Collection<Appointment> getAllAppointments(boolean sorted);
	
	/**
	 * This method returns the Appointments of this Calendar, filtered by 
	 * the passed arguments.
	 * 
	 * @see Appointment
	 * 
	 * @param topic the topic of the returned Appointments, if null the topic 
	 * 				is ignored
	 * @param creator the User that created the returned Appointments, if null 
	 * 				the creator is ignored
	 * @param start the start date of the returned Appointments, if null the start 
	 * 				date is ignored
	 * @param duration the duration of the Appointments, ignored by the lookup 
	 * 				query
	 * @param location the location of the returned Appointments, if null the 
	 * 				location is ignored
	 * @param sorted true if the returned Appointments should be sorted by their 
	 * 				start date, false otherwise
	 * @return	the Appointments of this Calendar that match the passed arguments
	 */
	public Collection<Appointment> getAppointments(String topic, User creator,
			Date start, long duration, String location, boolean sorted);

	/**
	 * Returns whether one or more Appointments are stored in this Calendar
	 * 
	 * @return true if this Calendar stores one or more Appointments, false 
	 * 				 otherwise
	 */
	public boolean hasAppointments();
	
	/**
	 * This method removes the passed Appointment from this Calendar.
	 * Note that this does not cause removal of the Appointment from other 
	 * Calendar instances it was possibly transfered to and that the passed 
	 * Appointment is invalid after removal and should not be used anymore.
	 * 
	 * @see Appointment
	 * 
	 * @param appointment the Appointment that will be removed from this Calendar 
	 * 										and should not be used afterwards
	 * @throws IllegalArgumentException in case the passed Appointment is invalid
	 */
	public void removeAppointment(Appointment appointment) 
			throws IllegalArgumentException;
	
	/**
	 * This method creates and returns an Appointment with the passed topic, 
	 * start date, 
	 * duration, location and description. The creator of this Appointment is 
	 * determined by this Calendar's owner.
	 * Note that the arguments apart from duration and description (empty String 
	 * allowed) must not be null or empty.
	 * 
	 * @see Appointment
	 * 
	 * @param topic the String specifies the topic of the Appointment
	 * @param start the Date specifying the start date of the Appointment
	 * @param duration the duration in milliseconds of the Appointment 
	 * @param location the location of the Appointment
	 * @param description the description of the Appointment
	 * @return the Appointment that was created
	 * @throws CalendarException in case of an error when internally storing the 
	 * 													 Appointment
	 * @throws IOException	in case of an connection error when trying to 
	 * 											transfer the Appointment
	 */
	public Appointment createAppointment(String topic, Date start, 
			long duration, String location, String description) 
					throws CalendarException, IOException;
	
	/**
	 * This method adds a new Comment with this Calendar's owner as creator to the 
	 * passed Appointment.
	 * 
	 * @see Comment
	 * @see Appointment
	 * 
	 * @param content the content of the Comment
	 * @param appointment the Appointment the created Comment is attached to
	 * @throws IllegalArgumentException in case the passed <code>content</code> 
	 * 																	is empty or null
	 * @throws IOException	in case of a connection error
	 * @throws CalendarException	in case of an internal error when trying to 
	 * 														store the Comment
	 */
	public void addCommentToAppointment(String content, Appointment appointment) 
			throws IllegalArgumentException, IOException, CalendarException; 
	
	/**
	 * This method creates a new known User for this Calendar.
	 * The user is identified by its email address which therefore has to be 
	 * unique for each user.
	 * Known users can automatically receive and transfer Appointments and 
	 * Comments to and from this Calendar so this function should be used 
	 * with care.
	 * 
	 * @see User
	 * 
	 * @param name the String specifying the name of the new User
	 * @param email	the (unique) email address of the new User
	 * @param host the URI at which a connection to the new User can be 
	 * 						 established
	 * @return the User that was created
	 * @throws CalendarException in case of an internal error when trying to store 
	 * 													 the User
	 * @throws IllegalArgumentException if one of the arguments is considered 
	 * 																	invalid (e.g. an already 
	 * 																	existing email address)
	 */
	public User createUser(String name, String email, URI host) throws CalendarException;
	
	/**
	 * This method removes the User that is identified with the passed email 
	 * address from this calendar.
	 * 
	 * @see User
	 * 
	 * @param email the email address that identifies the User that is going to 
	 * 				be removed
	 * @throws CalendarException in case of an internal error when trying to 
	 * 													 remove the user
	 * @throws IllegalArgumentException in case a user with the specified 
	 * 						<code>email</code> does not exist
	 */
	public void removeUser(String email) 
							throws CalendarException, IllegalArgumentException;

	/**
	 * This method removes the passed User from this calendar.
	 * 
	 * @see User
	 * 
	 * @param user the User that will be removed from this Calendar
	 * @throws CalendarException in case of an internal error when trying to 
	 * 													 remove the user
	 */	
	public void removeUser(User user) throws CalendarException;
	
	/**
	 * This method returns the User with the passed email address that is 
	 * stored as a known user in this Calendar.
	 * 
	 * @see User
	 * 
	 * @param email the String representing the email address of the user
	 * @return	the User having the specified <code>email</code> address
	 * @throws CalendarException in case of an internal error when trying to 
	 * 													 retrieve the User
	 * @throws IllegalArgumentException in case a User with the passed 
	 * 							<code>email</code> address does not exist
	 */
	public User getUser(String email) 
						throws CalendarException, IllegalArgumentException;

	/**
	 * This method returns all known users of this Calendar.
	 * 
	 * @see User
	 * 
	 * @return all known users of this Calendar
	 */
	public Collection<User> getAllUsers();
	
	/**
	 * This method checks whether a known user with the passed email address 
	 * exists in this Calendar.
	 * 
	 * @param email the email address specifying the User
	 * @return true if this Calendar contains a User with the passed 
	 * 				 email address, false otherwise
	 * @throws CalendarException in case an internal error when trying to 
	 * 													 check the User
	 */
	public boolean userExists(String email) throws CalendarException;
	
	/**
	 * A call to this method explicitly tries to send Appointments to and 
	 * receive Appointments from other Calendar instances that are owned by 
	 * known users.
	 * 
	 * @throws IOException in case of a connection error
	 */
	public void sendAndReceiveAppointments() throws IOException;
	
	/**
	 * This method registers a ChangedAppointmentListener that listens for an 
	 * Appointment that was created or one for which a Comment was added.
	 * 
	 * @see ChangedAppointmentListener
	 * 
	 * @param handler the ChangedAppointmentListener that is going to be called 
	 * 								when a new Appointment was created or a Comment was added 
	 * 								to an existing one
	 */
	public void registerAppointmentListener(ChangedAppointmentListener handler);
	
}