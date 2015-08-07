package interfaces;

import java.util.Collection;
import java.util.Date;

import exceptions.CalendarException;

/**
 * This interface specifies an appointment that can be stored in a Calendar.
 * An <code>Appointment</code> consists of start and end date, a creator 
 * (the owner of 
 * the Calendar client it was created with), a location, topic, description 
 * and possibly a number of comments.
 * Due to the decentralized storage of Appointments, their properties can not 
 * be changed.
 * 
 * @see Calendar
 *
 */
public interface Appointment {

	/**
	 * This method returns the Date at which this <code>Appointment</code> starts.
	 * 
	 * @return	the Date this <code>Appointment</code> starts
	 */
	public Date getStart();

	/**
	 * This method returns the Date at which this <code>Appointment</code> ends.
	 * 
	 * @return the Date at which this <code>Appointment</code> ends
	 */
	public Date getEnd();

	/**
	 * This Method returns the duration of this <code>Appointment</code> 
	 * in milliseconds.
	 * 
	 * 
	 * @return the duration of this Appointment in milliseconds
	 */
	public long getDuration();
	
	/**
	 * This method returns the User who created this <code>Appointment</code>. 
	 * 
	 * @return	the User who created this <code>Appointment</code>
	 */
	public User getCreator();
	
	/**
	 * This method removes this <code>Appointment</code> from the Calendar 
	 * it is attached to.
	 * After calling it, this <code>Appointment</code> instance should 
	 * not be used anymore.
	 * 
	 * @see User
	 */
	public void removeFromCalendar();
		
	/**
	 * This method returns the comments that were created for 
	 * this <code>Appointment</code>.
	 * 
	 * @see Comment
	 * 
	 * @param specifies whether the returned Comments should be sorted 
	 * 				(by their start date and time) or not
	 * @return a Collection representing the comments of this
	 * 				 <code>Appointment</code>
	 * @throws CalendarException if this Appointment instance is invalid (e.g. 
	 * 													 because it was removed from its Calendar before)
	 */
	public Collection<Comment> getComments(boolean sorted) throws CalendarException;
	
	/**
	 * This method creates a Comment for this <code>Appointment</code> and 
	 * returns it.
	 * The creator of this comment is the owner of the used 
	 * <code>Calendar</code> instance, 
	 * its creation time the one at its creation.
	 * 
	 * @see Comment
	 * 
	 * @param content	the content of the Comment that is going to be created
	 * @return	the Comment that was created
	 * @throws IllegalArgumentException	if the passed 
	 * 				 <code>content</code> is empty
	 * @throws CalendarException if this Appointment instance is invalid (e.g. 
	 * 													 because it was removed from its Calendar before)
	 */
	public Comment createComment(String content) throws IllegalArgumentException, 
																											CalendarException;
	
	/**
	 * This method return the String describing this <code>Appointment</code>.
	 * 
	 * 
	 * @return the String describing this <code>Appointment</code>
	 * @throws CalendarException
	 */
	public String getDescription() throws CalendarException;
	
	/**
	 * This method returns the String specifying the topic of this 
	 * <code>Appointment</code>.
	 * 
	 * @return the String describing the topic of this <code>Appointment</code>
	 */
	public String getTopic();
	
	/**
	 * This method returns the String specifying the location (e.g. city) of 
	 * this <code>Appointment</code>.
	 * 
	 * @return the String specifying the location of this 
	 * 				 <code>Appointment</code>
	 */
	public String getLocation();

}
