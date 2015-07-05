package addressExchanger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ArrayList; 
import java.util.Enumeration;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.*;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkSecurityException;

/**	
*	An AddressExchanger object represents an address exchanger that sends an address update
* interest to other peers in the knowledge base when the owner adds a new address. An 
*	AddressExchanger object has an ownerKB representing the knowledge base of the owner, an 
*	ownerSE representing the SharkEngine of the owner, an ownerKP representing the KnowledgePort
*	of the owner, an addressVersion representing a stamp that is incremented with each address 
*	change and a lastKnownOwner representing the last known owner of ownerKB. This class implements
*	KnowledgeBaseListener.
*   
*	@implements KnowledgeBaseListener
*/
public class AddressExchanger implements KnowledgeBaseListener
{
	/** ownerKB of this AddressExchanger */
	private SharkKB ownerKB;
	/** ownerSE of this AddressExchanger */
	private SharkEngine ownerSE;
	/** ownerKP of this AddressExchanger */
	private AddressExchangerKP ownerKP;
	/** addressVersion of this AddressExchanger */
	private int addressVersion;
	/** the owner of ownerKB */
	private PeerSemanticTag lastKnownOwner;

	/**	
	*	Class constructor initializing ownerKB, ownerSE, ownerKP to the given values, addressVersion 
	*	to 0 and lastKnownOwner to the owner of the given ownerKB.
	*
	*	@param	ownerSE		SharkEngine of the owner
	*	@param	ownerKB		KnowledgeBase of the owner
	*	@param	ownerKP		KnowledgePort of the owner
	*/
	public AddressExchanger(SharkEngine ownerSE, SharkKB ownerKB, AddressExchangerKP ownerKP)
	{
		this.ownerSE = ownerSE;
		this.ownerKB = ownerKB;
		this.ownerKP = ownerKP;
		this.addressVersion = 0;
		initialiseLastKnownOwner();
	}

	/**	
	*	Checks if the SemanticChange change is appropriate. Then updates lastKnownOwner, increases addressVersion 
	*	and sends an address update interest accordingly. 
	*
	*	@param	tag		SemanticTag that has been changed.
	*/
	@Override
	public void tagChanged(SemanticTag tag)
	{
		System.out.println("\nTag changed");
		L.d("\nTag changed");
 		if(isChangeAppropriate(tag))
 		{
 			// Increase addressVersion
			addressVersion++;
 			//If address has been changed create a new lastKnownOwner
			initialiseLastKnownOwner();
			// Send interest to all peers in the KnowledgeBase
			sendAddressUpdateInterest();
 		}
	}

	/**
	*	This method mocks the behaviour of tagChanged method. It can be deleted after implementation of tagChanged 
	*	method. Checks if the SemanticChange change is appropriate. Then updates lastKnownOwner, increases addressVersion 
	*	and sends an address update interest accordingly. 
	*
	*	@param	tag		SemanticTag that has been added.
	*/
	@Override
 	public void peerAdded(PeerSemanticTag tag)
 	{
 		System.out.println("\nNotice: peerAdded mocks the behaviour");
 		L.d("\nNotice: peerAdded mocks the behaviour");
 		if(isChangeAppropriate(tag))
 		{
 			// Increase addressVersion
			addressVersion++;
 			//If address has been changed create a new lastKnownOwner
			initialiseLastKnownOwner();
			// Send interest to all peers in the KnowledgeBase
			sendAddressUpdateInterest();
 		}	
 	}

	/**
	*	Initialises lastKnownOwner by assigning it the owner of ownerKB.
	*/
 	private void initialiseLastKnownOwner()
	{
		PeerSemanticTag tempOwner = ownerKB.getOwner();
		lastKnownOwner = InMemoSharkKB.createInMemoPeerSemanticTag(tempOwner.getName(), tempOwner.getSI(), tempOwner.getAddresses());
	}

	/**
	*	Checks if the change of SemanticTag is regarding an address change of a PeerSemanticTag.
	*
	*	@param	tag		SemanticTag that has been changed.
	*
	*	@return	if the change is appropriate.
	*/
	private boolean isChangeAppropriate(SemanticTag tag)
	{
		boolean isAppropriate = false;
		//Check if changed tag is PeerSemanticTag
		if (tag instanceof PeerSemanticTag)
		{	
			//Gets the current owner form ownerKB
			PeerSemanticTag currentOwner = ownerKB.getOwner();
			//Compares the currentOwner with the lastKnownOwner
			if (SharkCSAlgebra.identical(currentOwner, lastKnownOwner))
			{
				//The ownership of the KnowledgeBase has not been changed. Compare to see if the owner has changed his address
				if (!Arrays.equals(currentOwner.getAddresses(), lastKnownOwner.getAddresses())) 
				{
					// Increase addressVersion
					//addressVersion++;
					// The change in the KnowledgeBase is an address change. Thus it concerns the AddressExchanger.
					isAppropriate = true;
				}
			}
		}
		return isAppropriate;
	}

	/**
	*	Sends an address update interest to the other peers in ownerKB. 
	*/
	private void sendAddressUpdateInterest() 
	{
		try
		{
			STSet interestTopics = InMemoSharkKB.createInMemoSTSet();
			//The topic is addressUpdate
			SemanticTag addressUpdate = ownerKB.createSemanticTag("Address update", "http://www.sharksystem.net/addressUpdate.html");
			interestTopics.merge(addressUpdate);
			// Set addressVersion property to the owner PeerSemanticTag
			lastKnownOwner.setProperty("addressVersion", Integer.toString(addressVersion));
    	// Create new interest with the topic addressUpdate
    	Interest interestAddressUpdate = InMemoSharkKB.createInMemoInterest(interestTopics, lastKnownOwner, null, null, null, null, SharkCS.DIRECTION_OUT);
    	// Get all peers in the KnowledgeBase
	    PeerSTSet allPeers = ownerKB.getPeerSTSet();
	    // Send the interest to all peers in the KnowledgeBase except the owner
	    for (Enumeration<SemanticTag> e = allPeers.tags(); e.hasMoreElements();)
	    {
	    	PeerSemanticTag recipient = (PeerSemanticTag)e.nextElement();
	    	if (!SharkCSAlgebra.identical(recipient, lastKnownOwner))
	    	{
	        ownerSE.sendInterest(interestAddressUpdate, recipient, ownerKP);
	        ownerKP.notifyAddressExposeSent(interestAddressUpdate);
	      }
	    }
		}
		catch(SharkKBException e)
		{
			L.w(getStackTrace(e), this);
		}
		catch(SharkSecurityException e)
		{
			L.w(getStackTrace(e), this);
		}
		catch(IOException e)
		{
			L.w(getStackTrace(e), this);		
		}
	}
	
	/**	
	*	Converts the stack trace to string.
	*
	*	@param	e 	the exception
	*
	*	@return the stack trace as string.
	*/
	private String getStackTrace(Throwable e)
	{
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		return stackTrace.toString();
	}

	/**	
	* Does nothing.
	*/
	@Override
	public void contextPointAdded(ContextPoint cp)
	{
	}

	/**	
	* Does nothing.
	*/
	@Override
 	public void contextPointRemoved(ContextPoint cp)
 	{
 	}
 	
 	/**	
	* Does nothing.
	*/
 	@Override
 	public void cpChanged(ContextPoint cp)
 	{
 	}

 	/**	
	* Does nothing.
	*/	
	@Override
 	public void locationAdded(SpatialSemanticTag location)
 	{
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
 	public void locationRemoved(SpatialSemanticTag tag)
 	{
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
 	public void peerRemoved(PeerSemanticTag tag)
 	{
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
 	public void predicateCreated(SNSemanticTag subject, java.lang.String type, SNSemanticTag object)
  {
 	}

 	/**	
	* Does nothing.
	*/
 	@Override	
  public void predicateRemoved(SNSemanticTag subject, java.lang.String type, SNSemanticTag object)
  {
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
  public void timespanAdded(TimeSemanticTag time)
  {
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
  public void timespanRemoved(TimeSemanticTag tag)
  {
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
  public void topicAdded(SemanticTag tag)
  {
 	}

 	/**	
	* Does nothing.
	*/
 	@Override
  public void topicRemoved(SemanticTag tag)
  {
 	}
}
