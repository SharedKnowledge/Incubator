package interfaces;

import java.util.Date;

import exceptions.CalendarException;


/**
 * This interface specifies a Comment of a Appointment of a Calendar that can 
 * be added and retrieved from an Appointment.
 * A comment is always managed by a Calendar instance, therefore its 
 * creation always depends on it.
 * 
 * As comments are possibly transfered to other Calendar instances and 
 * therefore stored in a non-central way, deleting an existing comment is not 
 * possible.
 * The creator of a comment is the User that is registered as the owner of the 
 * Calendar instances it was created with.
 *
 */
public interface Comment{
	
	/**
	 * This method returns the email address of the creator of this comment
	 * 
	 * @return the String representing the email address of the creator of this 
	 * 				 comment
	 */
	public String getCreatorEmail();
	
	/**
	 * This method returns the User that created this Comment (if known).
	 * 
	 * @return the User that created this comment
	 * @throws CalendarException in case an error when retrieving the 
	 * 													 creator occurred
	 */
	public User getCreator() throws CalendarException;
	
	/**
	 * This method returns the content of this Comment
	 * 
	 * @return the String representing the content of this Comment
	 * @throws CalendarException in case an internal error occurred when trying to 
	 * 													 access the content of this Comment
	 */
	public String getContent() throws CalendarException;
	
	/**
	 * This method returns the creation date of this Comment.
	 * 
	 * @return the Date this Comment was created at
	 */
	public Date getCreationDate();
	
	/**
	 * This method returns the Appointment this comment was created for.
	 * 
	 * @see Appointment
	 * 
	 * @return the Appointment this Comment was created for
	 */
	public Appointment getAppointment();
}
