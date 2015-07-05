package addressExchanger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.*;
import net.sharkfw.system.L;


/**	
*	An AddressExchangerKP object represents an address exchanger knowledge port. An AddressExchangerKP 
* object has kb representing a Shark KnowledgeBase. It updates the addresses of the peers in kb on 
*	incoming "Address update" interests depending on the addressVersions.This class extends KnowledgePort.
*
*	@extends KnowledgePort
*/
class AddressExchangerKP extends KnowledgePort
{
	/** kb of this AddressExchangerKP */
	private SharkKB kb;

	/**	
	*	Class constructor initializing the KnowledgePort and kb to the given variables.
	*
	*	@param	se		SharkEngine
	*	@param	kb		KnowledgeBase
	*/
	public AddressExchangerKP(SharkEngine se, SharkKB kb)
	{
		super(se);
		this.kb = kb;
	}

	/**	
	*	Updates the address of the corresponding peer in kb on incoming "Address update" interest depending on the 
	*	addressVersion. 
	*
	*	@param	interest 				the received interest
	*	@param	KEPConnection 	knowledge exchange protocol connection
	*/
	@Override
	protected void doExpose(SharkCS interest, KEPConnection kepConnection) 
	{
		try
		{
			//Topics are stored in semantic tag set.
			STSet topics = interest.getTopics();
			Enumeration<SemanticTag> enumerationTopics = topics.tags();
			if (enumerationTopics.hasMoreElements())
			{
				SemanticTag topicSematicTag = enumerationTopics.nextElement();
				// Validate the structure of address update. The topic should be "Address update" and there must be just one topic.
				if (topicSematicTag.getName() == "Address update" || !enumerationTopics.hasMoreElements())
				{
					// The address of the originator is the new address of the peer that is sending the address update
					PeerSemanticTag originator = interest.getOriginator();
					String newAddresses[] = originator.getAddresses();
					// Checks if names of peers in the KnowledgeBase matches with the name of the originator
					PeerSTSet allPeersSTSet = kb.getPeerSTSet();
					for (Enumeration<PeerSemanticTag> enumerationPeers = allPeersSTSet.peerTags(); enumerationPeers.hasMoreElements();)
					{
						PeerSemanticTag peer = enumerationPeers.nextElement();
						// Addresses of the matching peer are updated if incomingAddressVersion is higher than the availableAddressVersion
						if (SharkCSAlgebra.identical(peer, originator))
						{
							int availableAddressVersion = Integer.parseInt(peer.getProperty("addressVersion"));
							int incomingAddressVersion = Integer.parseInt(originator.getProperty("addressVersion"));
							printAddressVersions(availableAddressVersion, incomingAddressVersion);
							if (incomingAddressVersion > availableAddressVersion)
							{
								peer.setAddresses(newAddresses);
								peer.setProperty("addressVersion", Integer.toString(incomingAddressVersion));
							} 	
						}
					}
				}
			}
			printAllPeerSemanticTagsInKnowledgeBase(); 
		}
		catch (SharkKBException e)
		{
			L.w(getStackTrace(e), this);
		}
	}

	/**	
	*	Prints addressVersion that is available and that comes with the received interest. Method serves just for the 
	*	visualisation of the simulation. 
	*
	*	@param	availableAddressVersion		the addressVersion available in the kb
	*	@param	incomingAddressVersion 	knowledge exchange protocol connection
	*/
	private void printAddressVersions(int availableAddressVersion, int incomingAddressVersion)
	{
		System.out.printf("Available address version: %d\n", availableAddressVersion);
		System.out.printf("Incoming address version: %d\n", incomingAddressVersion);
	}

	/**	
	*	Prints all PeerSemanticTags in the KnowledgeBase. Method serves just for the visualisation of the simulation.
	*
	*	@throws SharkKBException
	*/
	private void printAllPeerSemanticTagsInKnowledgeBase() throws SharkKBException
	{
		System.out.println("PeerSemanticTags after update:");
		PeerSTSet allPeersSTSet = kb.getPeerSTSet();
		for (Enumeration<PeerSemanticTag> enumerationPeers = allPeersSTSet.peerTags(); enumerationPeers.hasMoreElements();)
		{
			PeerSemanticTag peer = enumerationPeers.nextElement();
			System.out.println(L.semanticTag2String(peer));
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
	public void doInsert(Knowledge knowledge, KEPConnection kepConnection) 
	{
	}

	/**	
	*	Notifys when an address expose is sent
	*
	*	@param	interest 	the sent interest
	*/
	void notifyAddressExposeSent(Interest interest)
	{
		this.notifyExposeSent(this, interest);
	}

}
