package implementations;

import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.Comment;
import interfaces.User;

import java.util.Date;

import exceptions.CalendarException;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * This class represents a Comment that was created by a CalendarImpl instance 
 * and is stored in a SharkKB Information.
 * 
 * Please note that Comments itself can only by created by a Calendar.
 * 
 * @see CalendarImpl
 *
 */
public class CommentImpl implements Comment {
	
	public final static String CREATOR_PROPERTY_ID = "creator";
	public final static String COMMENT_SUFFIX = "_comment";
	public final static String PARENT_APP = "parentapp";
	public final static String TYPE_NAME = "comment";
	
	private Information i;
	private Appointment app;
	private Calendar cal;
	

	/**
	 * Constructor creates a representation of a Comment stored in the passed 
	 * Information and managed by the passed Calendar.
	 * 
	 * @param cal the Calendar that manages this Comment
	 * @param app	the Appointment this Comment belongs to
	 * @param i	the Information that contains the Comment this instance represents
	 */
	public CommentImpl(Calendar cal, Appointment app, Information i) {
		this.i = i;
		this.app = app;
		this.cal = cal;
	}
	
	@Override
	public String getCreatorEmail() {
		String name = i.getProperty(CREATOR_PROPERTY_ID);
		return name;
	}
	
	@Override
	public User getCreator() throws CalendarException {
		String email = getCreatorEmail();
		return cal.getUser(email);
	}

	@Override
	public String getContent() {
		String c = "";
		try {
			c = i.getContentAsString();
		}
		catch (SharkKBException e) {
			throw new RuntimeException("Unable to load comment description", e);
		}
		
		return c;
	}

	@Override
	public Date getCreationDate() {
		return new Date(i.creationTime());
	}
	
	/**
	 * This method returns a String representing this comments properties.
	 * 
	 * @return a String representation of this comment's content, creator and 
	 * 				 creation date
	 */
	@Override
	public String toString() {
		return getCreatorEmail() + " at " + getCreationDate() + 
				":\n" + getContent(); 
	}

	@Override
	public Appointment getAppointment() {
		return this.app;
	}
}