package utils;

import interfaces.Appointment;

import java.util.Comparator;

/**
 * This class implements a Comparator that compares Appointments by their 
 * start date
 *
 */
public class AppointmentStartComparator implements Comparator<Appointment> {

	/**
	 * This Method compares two Appointments by their start date.
	 * 
	 * @param a1 the Appointment compared to a2
	 * @param a2 the Appointment compared to a1
	 * @return 0 if the Appointments (their start dates) are considered equal, 
	 * 				 <0 if a1 is considered smaller, >0 if larger than a2
	 */
	@Override
	public int compare(Appointment a1, Appointment a2) {
		return a1.getStart().compareTo(a2.getStart());
	}
}
