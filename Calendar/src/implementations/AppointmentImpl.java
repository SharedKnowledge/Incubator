package implementations;

import interfaces.Appointment;
import interfaces.Calendar;
import interfaces.Comment;
import interfaces.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import utils.CommentCreationDateComparator;
import utils.Utils;
import exceptions.CalendarException;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 * This class implements an Appointment that stores and retrieves its 
 * properties in a SharkKB and was created by {@link CalendarImpl}.
 * 
 * @see CalendarImpl
 */
public class AppointmentImpl implements Appointment {

	/**
	 * This string represents the name of the location property of an Appointment 
	 * to be used on an Information in SharkKB
	 */
	public static final String LOCATION_PROPERTY_NAME = "location";
	
	/**
	 * This string represents the name of the type property used by an Information 
	 * representing an Appointment in SharkKB
	 */
	public static final String TYPE_NAME = "appointment";
	
	private Calendar cal;
	private SharkKB kb;
	private ContextPoint cp;
	private Information i;
	
	/**
	 * This constructor creates an Appointment that represents an Information in 
	 * a SharkKB that was created by CalendarImpl.
	 * The constructor should not be used without the context of the associated 
	 * Calendar.
	 * If an Appointment was removed from its calendar, its Appointment instance 
	 * becomes invalid and should not be used anymore.
	 * 
	 * @param cal the Calendar the Appointment is stored in
	 * @param kb the SharkKB the Appointment is stored in 
	 * 					 (and used by <code>cal</code>)
	 * @param cp the ContextPoint of the Information representing this Appointment
	 * @param i the Information representing this Appointment
	 */
	public AppointmentImpl(Calendar cal, SharkKB kb, ContextPoint cp, 
												 Information i) {
		if ( kb == null || cp == null || i == null ) {
			throw new IllegalArgumentException("Null arguments");
		}
		this.kb = kb;
		this.cp = cp;
		this.i = i;
		this.cal = cal;
	}
	
	@Override
	public Date getStart() {
		return new Date(cp.getContextCoordinates().getTime().getFrom());
	}

	@Override
	public Date getEnd() {
		return new Date(cp.getContextCoordinates().getTime().getFrom() + 
				cp.getContextCoordinates().getTime().getDuration());
	}

	@Override
	public long getDuration() {
		return cp.getContextCoordinates().getTime().getDuration();
	}

	@Override
	public User getCreator() {
		return new UserImpl(cp.getContextCoordinates().getOriginator());
	}

	@Override
	public Collection<Comment> getComments(boolean sorted) throws CalendarException {
		if ( i == null ) {
			throw new CalendarException("This Appointment is invalid.");
		}
		ArrayList<Comment> comments = new ArrayList<Comment>();
		Iterator<Information> e = cp.getInformation();
		while ( e.hasNext() ) {
			Information curr_i = e.next();
			if (curr_i.getProperty(CalendarImpl.TYPE_KEY).equals(CommentImpl.TYPE_NAME) &&  
					curr_i.getProperty(CommentImpl.PARENT_APP).equals(this.getID()) ) {
				Comment c = new CommentImpl(cal, this, curr_i);
				comments.add(c);
			}
		}
		if ( sorted ) {
			Collections.sort(comments, new CommentCreationDateComparator());
		}
		return comments;
	}

	@Override
	public String getDescription() throws CalendarException {
		try {
			return i.getContentAsString();
		}
		catch (SharkKBException e) {
			throw new CalendarException("Could not retrieve description from "
					+ "SharkKB.", e);
		}
	}

	@Override
	public String getTopic() {
		return Utils.uriToString(cp.getContextCoordinates().getTopic().getSI()[0]);
	}

	@Override
	public Comment createComment(String content) throws IllegalArgumentException, 
																											CalendarException {
		if ( i == null ) {
			throw new CalendarException("Invalid Appointment");
		}
		if ( content == null || content.length() == 0 ) {
			throw new IllegalArgumentException("Comment's content has to be a "
					+ "non-empty String.");
		}
		Information i = cp.addInformation(content);
		Comment c = new CommentImpl(cal, this, i);
		i.setProperty(CalendarImpl.TYPE_KEY, CommentImpl.TYPE_NAME);
		i.setProperty(CommentImpl.PARENT_APP, this.getID() );
		i.setProperty(CommentImpl.CREATOR_PROPERTY_ID, kb.getOwner().getSI()[0]);
		return c;
	}

	@Override
	public void removeFromCalendar() {
		Iterator<Information> it = cp.getInformation();
		while ( it.hasNext() ) {
			Information i = it.next();
			if ( i.getProperty(CommentImpl.PARENT_APP) != null && 
					 i.getProperty(CommentImpl.PARENT_APP).equals(getID()) ) {
				cp.removeInformation(i);
			}
		}
		cp.removeInformation(i);
		this.i = null;
		this.cp = null;
		this.kb = null;
	}

	@Override
	public String getLocation() {
		String location = i.getProperty(LOCATION_PROPERTY_NAME);
		return location;
	}
	
	@Override
	public String toString() {
		String repr = "";
		try {
			repr = this.getTopic() + "\nCreator: " + this.getCreator().getEmailAddress() 
					+ "\n" + getDescription() + "\nDate: " + getStart() + "\nLocation: "
					+ getLocation();
		}
		catch (CalendarException e) {
			repr = "Invalid Appointment";
		}
		return repr;
	}

	private String getID() {
		return i.getUniqueID();
	}
	
	/**
	 * This method compares the values of this Appointment to the ones of the 
	 * other. Same properties means that the Appointments are considered equal.
	 * 
	 * @param other the Appointment that is compared to this
	 * @return true if the represented Appointments are considered equal, false 
	 * 				 otherwise
	 */
	public boolean equals(Appointment other) {
		return this.toString().equals(other.toString());
	}
}
