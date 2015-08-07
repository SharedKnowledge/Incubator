package sharkfw_tests;

import static org.junit.Assert.*;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.ContextPointListener;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

import org.junit.Test;

/**
 * This class tests whether the handler methods addedInformation() and 
 * removedInformation() of a ContextPointListener are called.
 * 
 * <code>sharkfw.2.12.1.jar</code> was used.
 *
 */
public class TestContextPointListenerCallback {

	/**
	 * This test sets a listener on a ContextPoint and checks whether its
	 * handler methods are called.
	 * 
	 * 
	 * 
	 * @throws SharkKBException
	 */
	@Test
	public void testCPListenerCalls() throws SharkKBException {
		SharkKB kb = new InMemoSharkKB();
		PeerSemanticTag alice = kb.createPeerSemanticTag("alice", "a@example.com", 
				"http://localhost:5555");
		SemanticTag topic = kb.createSemanticTag("mytopic", "http://example.com/topic");
		kb.setOwner(alice);
		ContextCoordinates cc = kb.createContextCoordinates(
				topic, alice, alice, null, null, null, SharkCS.DIRECTION_INOUT);
		ContextPoint kp = kb.createContextPoint(cc);
		MyKPListener listener = new MyKPListener();
		kp.setListener(listener);
		Information info_1 = kp.addInformation("new info");
		Information info_2 = kp.addInformation("new info2");
		assertTrue("Listener was not called when Information was added.", 
							 listener.was_called_on_added);
		kp.removeInformation(info_1);
		kp.removeInformation(info_2);
		assertTrue("Listener was not called when Information was removed", 
								listener.was_called_on_removed);
	}
	
	class MyKPListener implements ContextPointListener {
		public boolean was_called_on_added = false;
		public boolean was_called_on_removed = false;

		@Override
		public void addedInformation(Information arg0, ContextPoint arg1) {
			System.out.println("addedInformation called");
			was_called_on_added = true;
		}

		@Override
		public void removedInformation(Information arg0, ContextPoint arg1) {
			System.out.println("removedInformation called");
			was_called_on_removed = true;
		}
		
	}
}
