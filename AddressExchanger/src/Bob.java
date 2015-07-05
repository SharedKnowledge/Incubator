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
*	A Bob object represents the peer Bob. A Bob object has a bobSE representing 
*	a SharkEngine.
*/
public class Bob
{
	/** bobSE of this Bob peer */
	private SharkEngine bobSE;

	/**	
	*	Class constructor initializing the bobSE to a J2SEAndroidSharkEngine
	*/
	public Bob()
	{
		bobSE = new J2SEAndroidSharkEngine();
	}

	/**	
	*	Runs the peer.
	*/
	public void run()
	{
		try
		{
			SharkKB bobKB = new InMemoSharkKB();
				
			// Create a peer to describe Bob and set as owner
			PeerSemanticTag bob = bobKB.createPeerSemanticTag("Bob", "http://www.sharksystem.net/bob.html", "tcp://localhost:7071");
			bobKB.setOwner(bob);
				
			// Add PeerSemanticTag bob and set property addressVersion to 0. First incoming PeerSemanticTag will have addressVersion 1
			PeerSemanticTag alice = bobKB.createPeerSemanticTag("Alice", "http://www.sharksystem.net/alice.html", "tcp://localhost:7070");
			alice.setProperty("addressVersion", Integer.toString(0));

			// Create a AdressExchangerKP and add a AdressExchangerKPListener to the KnowledgePort
			AddressExchangerKP bobKP = new AddressExchangerKP(bobSE, bobKB);
			AddressExchangerKPListener listener = new AddressExchangerKPListener();
			bobKP.addListener(listener);
					
			// Add AdressExchanger as a KnowledgeBaseListener
			KnowledgeBaseListener bobAddressExchanger = new AddressExchanger(bobSE, bobKB, bobKP);
			bobKB.addListener(bobAddressExchanger);

			//Start the TCP-networking component on the SharkEngine to send and receive traffic  		
			bobSE.setConnectionTimeOut(10000);
			bobSE.startTCP(7071);
			System.out.println("\n\nBob is up and running...");	

			// Print all PeerSemanticTags in bobKB
			System.out.println("\nPeerSemanticTags:");
			PeerSTSet allPeersSTSet = bobKB.getPeerSTSet();
			for (Enumeration<PeerSemanticTag> enumerationPeers = allPeersSTSet.peerTags(); enumerationPeers.hasMoreElements();)
			{
				PeerSemanticTag peer = enumerationPeers.nextElement();
				System.out.println(L.semanticTag2String(peer));
			}
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
