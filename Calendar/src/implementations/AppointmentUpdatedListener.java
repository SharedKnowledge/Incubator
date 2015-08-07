package implementations;

import interfaces.Calendar;
import interfaces.ChangedAppointmentListener;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.KnowledgeBaseListener;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SNSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.TimeSemanticTag;


/**
 * THIS CLASS IS NOT USED BY CalendarImpl CURRENTLY.
 * (due to a problem when registering an ContextPointListener, see 
 * {@link TestContextPointListenerCallback})
 * 
 * This class adds the registered ChangedAppointmentListener to each new 
 * ContextPoint so new Appointments and Comments can be handled.
 */
public class AppointmentUpdatedListener implements KnowledgeBaseListener {
	private AppointmentCPListener info_listener;
	
	/**
	 * This constructor takes care that the passed ChangedAppointmentListener can 
	 * be called on new Appointments and Comments
	 * 
	 * @see AppointmentCPListener
	 * @see ChangedAppointmentListener
	 * 
	 * @param cal the Calendar this instances, <code>kb</code> and the 
	 * 						ChangedAppointmentListener are used by
	 * @param update_handler the ChangedAppointmentListener that is going to 
	 * 											 handle new Appointments
	 * @param kb						 the SharkKB used by <code>cal</code>
	 */
	public AppointmentUpdatedListener(Calendar cal, 
						ChangedAppointmentListener update_handler, SharkKB kb) {
		info_listener = new AppointmentCPListener(cal, kb, update_handler);
	}
	
	@Override
	public void contextPointAdded(ContextPoint cp) {
		cp.setListener(info_listener);
	}

	@Override
	public void contextPointRemoved(ContextPoint arg0) {
	}

	@Override
	public void cpChanged(ContextPoint cp) {
	}

	@Override
	public void locationAdded(SpatialSemanticTag arg0) {
	}

	@Override
	public void locationRemoved(SpatialSemanticTag arg0) {
	}

	@Override
	public void peerAdded(PeerSemanticTag arg0) {
	}

	@Override
	public void peerRemoved(PeerSemanticTag arg0) {
	}

	@Override
	public void predicateCreated(SNSemanticTag arg0, String arg1,
			SNSemanticTag arg2) {
	}

	@Override
	public void predicateRemoved(SNSemanticTag arg0, String arg1,
			SNSemanticTag arg2) {
	}

	@Override
	public void timespanAdded(TimeSemanticTag arg0) {
	}

	@Override
	public void timespanRemoved(TimeSemanticTag arg0) {
	}

	@Override
	public void topicAdded(SemanticTag arg0) {
	}

	@Override
	public void topicRemoved(SemanticTag arg0) {
	}
}
