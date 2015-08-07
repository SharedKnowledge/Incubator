package interfaces;

/**
 * This interface specifies a listener that can be registered on a Calendar 
 * instance and is called when a new Appointment was created (or received) 
 * or a Comment was added to an Appointment.
 * 
 * @see Calendar
 * @see Appointment
 *
 */
public interface ChangedAppointmentListener {
	
	/**
	 * This method handles a changed Appointment
	 * 
	 * @param app the Appointment that was changed (created) itself or for which 
	 * 						a new Comment was created
	 */
	public void handleUpdatedAppointment(Appointment app);
	
}
