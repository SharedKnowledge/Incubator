package implementations;

import java.util.Iterator;

import interfaces.Calendar;
import interfaces.ChangedAppointmentListener;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.ContextPointListener;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.SharkKB;

/**
 * THIS CLASS IS NOT TESTED AND CURRENTLY NOT USED BY {@link CalendarImpl}.
 * (due to a problem when registering a ContextPointListener, see 
 * {@link TestContextPointListenerCallback})
 * 
 * 
 * This class implements ContextPointListener and listens for 
 * added Information to a ContextPoint.
 * When Information is added that represents an Appointment or Comment of 
 * an Appointment, its {@link ChangedAppointmentListener} is called so that an 
 * {@link ChangedAppointmentListener#handleUpdatedAppointment(interfaces.Appointment)} 
 * implementation can handle it.
 * 
 * @see ChangedAppointmentListener
 * @see Calendar#registerAppointmentListener(ChangedAppointmentListener)
 */
public class AppointmentCPListener implements ContextPointListener {
	
	private ChangedAppointmentListener handler;
	private Calendar cal;
	private SharkKB kb;
	
	/**
	 * This constructor initializes this AppointmentCPListener so that the passed 
	 * {@link ChangedAppointmentListener} can handle a new Appointment or 
	 * Comment.
	 * 
	 * @param cal the Calendar that is used
	 * @param kb	the SharkKB that is used
	 * @param handler the ChangedAppointmentListener that handles new Appointments/
	 * 								Comments
	 */
	public AppointmentCPListener(Calendar cal, SharkKB kb,
																				ChangedAppointmentListener handler) {
		this.handler = handler;
		this.cal = cal;
		this.kb = kb;
	}

	@Override
	public void addedInformation(Information arg0, ContextPoint arg1) {
		if ( arg0.getProperty(CalendarImpl.TYPE_KEY).equalsIgnoreCase(
													AppointmentImpl.TYPE_NAME) ) {
			handler.handleUpdatedAppointment(
					new AppointmentImpl(cal, kb, arg1, arg0));
		}
		else if ( arg0.getProperty(CalendarImpl.TYPE_KEY).equalsIgnoreCase(
													CommentImpl.TYPE_NAME) ) {
			String app_id = arg0.getProperty(CommentImpl.PARENT_APP);
			Iterator<Information> e = arg1.getInformation();
			while ( e.hasNext() ) {
				Information curr_i = e.next();
				if ( curr_i.getUniqueID().equalsIgnoreCase(app_id) ) {
					handler.handleUpdatedAppointment(
							new AppointmentImpl(cal, kb, arg1, curr_i));					
				}
			}
		}
	}

	@Override
	public void removedInformation(Information arg0, ContextPoint arg1) {
	
	}
}