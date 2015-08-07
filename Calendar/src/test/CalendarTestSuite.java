package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestAppointments.class, 
								TestCalendar.class, 
								TestComments.class,
								TestCommunication.class, 
								TestUsers.class })
public class CalendarTestSuite {
}
