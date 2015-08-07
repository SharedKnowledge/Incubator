package implementations;

import interfaces.User;
import net.sharkfw.knowledgeBase.PeerSemanticTag;

/**
 * This class represents a known User of in a Calendar.
 * Users should only be created by the CalendarImpl itself and can not exist 
 * without its context.
 * 
 * If an UserImpl is removed from its calendar, it becomes invalid and should 
 * not be used anymore.
 */
public class UserImpl implements User {
	private PeerSemanticTag user;

	/**
	 * This constructor creates a UserImpl that represents the user stored with  
	 * the passed PeerSemanticTag in the Calendar that created it
	 * 
	 * @param user the PeerSemanticTag this UserImpl represents
	 */
	public UserImpl(PeerSemanticTag user) {
		this.user = user;
	}
	
	@Override
	public String getName() {
		return user.getName();
	}

	@Override
	public String getEmailAddress() {
		return user.getSI()[0];
	}

	@Override
	public String getHost() {
		return user.getAddresses()[0];
	}

	@Override
	public String toString() {
		return getName() + " \n " + getEmailAddress() + "\n" + getHost();
	}
}
