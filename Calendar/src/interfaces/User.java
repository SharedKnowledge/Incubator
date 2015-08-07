package interfaces;

/**
 * This interface specifies a User that is known to a Calendar instance.
 * The user has a name, an unique email address and an URI which allows setting 
 * up a connection to him.
 * Please note that a User is managed by a Calendar instance, it can not be 
 * created or 
 * deleted without it; if it was removed from a Calendar, the User instance 
 * becomes invalid and should not be user anymore.
 * 
 */
public interface User {
	
	/**
	 * This method returns the name of this User.
	 * 
	 * @return the String representing the name of this User
	 */
	public String getName();

	/**
	 * This method returns the email address of this User.
	 * 
	 * @return the String representing the email address of this User
	 */
	public String getEmailAddress();

	/**
	 * This method returns the String representation of the host of this User.
	 * 
	 * @return the String representing the Host of this User
	 */
	public String getHost();
}
