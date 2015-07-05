package addressExchanger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.*;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.StandardKP;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.L;


/**	
*	An Alice object represents the peer Alice. An Alice object has an aliceSE, aliceKB and
*	alice representing a SharkEngine, a Shark knowledge base and the owner of the knowledge
*	base respectively.
*/
public class Alice
{
	/** aliceSE of this Alice peer */
	private SharkEngine aliceSE;
	/** aliceKB of this Alice peer */
	private SharkKB aliceKB;
	/** the owner of aliceKB */
	private PeerSemanticTag alice;

	/**	
	*	Class constructor initializing aliceSE to a J2SEAndroidSharkEngine, aliceKB to an
	*	InMemoSharkKB, alice to the given name, SI and address and setting alice as the owner
	* of the knowledge base.
	*/
	public Alice()
	{
		// Create SharkEngine and KnowledgeBase
		aliceSE = new J2SEAndroidSharkEngine();
		aliceKB = new InMemoSharkKB();
		// Create a peer to describe Alice and set her as owner
		try
		{
			alice = aliceKB.createPeerSemanticTag("Alice", "http://www.sharksystem.net/alice.html", "tcp://localhost:7070");
			aliceKB.setOwner(alice);
		}
		catch(SharkKBException e)
	  	{
				L.w(getStackTrace(e), this);
	  	}
	}

	/**	
	*	Runs the peer.
	*/
	public void run() 
	{
		try
		{
		  // Add PeerSemanticTag bob
	   	PeerSemanticTag bob = aliceKB.createPeerSemanticTag("Bob", "http://www.sharksystem.net/bob.html", "tcp://localhost:7071");

		  // Create a AdressExchangerKP and add a AdressExchangerKPListener to the KnowledgePort
	    AddressExchangerKP aliceKP = new AddressExchangerKP(aliceSE, aliceKB);
	    AddressExchangerKPListener listener = new AddressExchangerKPListener();
	    aliceKP.addListener(listener);
		   	
		  // Add AddressExchanger as a KnowledgeBaseListener
		  KnowledgeBaseListener aliceAddressExchanger = new AddressExchanger(aliceSE, aliceKB, aliceKP);
		  aliceKB.addListener(aliceAddressExchanger);

	   //Start the TCP-networking component on the SharkEngine to send and receive traffic  		
			aliceSE.setConnectionTimeOut(10000);
	    aliceSE.startTCP(7070);
		  System.out.println("\n\nAlice is up and running...");
	  }
	  catch(SharkKBException e)
	  {
			L.w(getStackTrace(e), this);
	  }
	  catch(SharkProtocolNotSupportedException e)
	  {
	  	L.w(getStackTrace(e), this);		
	  }
	  catch(IOException e)
	  {
	  	L.w(getStackTrace(e), this);
	  }
	}

	/**	
	*	Adds the given address to the addresses of the knowledge base owner.
	*
	*	@param	address 	the given address
	*/
	public void addAddressToAlice(String address)
	{
		try
		{
			alice.addAddress(address);
			// Add PeerSemanticTag malice to trigger peerAdded method that mocks the behaviour of tagChanged method which is not implemented yet.
			PeerSemanticTag malice = aliceKB.createPeerSemanticTag("Malice", "http://www.sharksystem.net/alice.html", "malice@gmail.com");
		}
		catch(SharkKBException e)
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
}
