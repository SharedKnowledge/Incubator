package sharkfw_tests;

import static org.junit.Assert.*;

import java.io.IOException;

import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SpatialSemanticTag;
import net.sharkfw.knowledgeBase.geom.SharkGeometry;
import net.sharkfw.knowledgeBase.geom.inmemory.InMemoSharkGeometry;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.SyncKP;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;

import org.junit.Before;
import org.junit.Test;

/**
 * This test should show a potential error when exchanging a ContextPoint with a 
 * SpatialSemanticTag (using Geometry) with SyncKP.
 * If the SpatialSemanticTag is used, the CP is not transmitted, if it is set 
 * to null it is (here).
 * 
 * On my system, the communication itself does not always work, 
 * sometimes changing the 
 * ports is necessary.
 * 
 * <code>sharkfw.2.12.1.jar</code> was used.

 * 
 */

public class TestSyncKPWithSpatialST {
	private final String ALICE_IDENTIFIER = "aliceIdentifier";
	private final String BOB_IDENTIFIER = "bobIdentifier";
	private SyncKB _aliceKB, _bobKB;
	private SyncKP _aliceSyncKP, _bobSyncKP;
	private final long connectionTimeOut = 1000;
	private SemanticTag _teapotST;
	private SharkEngine _aliceEngine, _bobEngine;
	private SpatialSemanticTag location = null;
	private int _alicePort = 5039;
	private int _bobPort = 5288;
	private	PeerSemanticTag _alice, _bob;

	@Before
	public void setUp() throws Exception {
		_teapotST = InMemoSharkKB
				.createInMemoSemanticTag("teapot", "www.teapot.de");
		_aliceKB = new SyncKB(new InMemoSharkKB());
		_bobKB = new SyncKB(new InMemoSharkKB());
		_alice = _aliceKB.createPeerSemanticTag("Alice", ALICE_IDENTIFIER,
				"tcp://localhost:" + _alicePort);
		_bob = _bobKB.createPeerSemanticTag("Bob", BOB_IDENTIFIER,
				"tcp://localhost:" + _bobPort);
		_aliceKB.getPeerSTSet().merge(_bob);
		_bobKB.getPeerSTSet().merge(_alice);
		_aliceEngine = new J2SEAndroidSharkEngine();
		_bobEngine = new J2SEAndroidSharkEngine();
		_aliceKB.setOwner(_alice);
		_bobKB.setOwner(_bob);
		_aliceSyncKP = new SyncKP(_aliceEngine, _aliceKB, 2);
		_bobSyncKP = new SyncKP(_bobEngine, _bobKB, 2);
	}

	/**
	 * Location dimension is set to null, the test should succeed.
	 * 
	 * 
	 * @throws SharkKBException
	 * @throws SharkProtocolNotSupportedException
	 * @throws IOException
	 * @throws SharkSecurityException
	 * @throws InterruptedException
	 */
	@Test
	public void transferCPwithoutLocation() throws SharkKBException, SharkProtocolNotSupportedException, IOException, SharkSecurityException, InterruptedException {
		location = null;
		ContextCoordinates teapotCC = _aliceKB.createContextCoordinates(_teapotST,
				_alice, null, null, null, location, SharkCS.DIRECTION_INOUT);
		_aliceKB.createContextPoint(teapotCC);
		_aliceKB.getContextPoint(teapotCC).addInformation("Teapots freakin rock!");
		_aliceSyncKP.syncAllKnowledge();
		_bobSyncKP.syncAllKnowledge();
		_aliceEngine.setConnectionTimeOut(connectionTimeOut);
		_bobEngine.setConnectionTimeOut(connectionTimeOut);
		_aliceEngine.startTCP(_alicePort);
		_bobEngine.startTCP(_bobPort);
		_aliceEngine.publishAllKP(_bob);
		Thread.sleep(100);
		_bobEngine.publishAllKP(_alice);
		Thread.sleep(4000);
		_aliceEngine.stopTCP();
		Thread.sleep(10);
		_bobEngine.stopTCP();
		Thread.sleep(10);
		assertEquals("Teapots freakin rock!", _bobKB.getContextPoint(teapotCC)
				.getInformation().next().getContentAsString());
	}

	/**
	 * This test uses the location and does not (never) run here.
	 * 
	 * 
	 * @throws SharkProtocolNotSupportedException
	 * @throws SharkKBException
	 * @throws SharkSecurityException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void transferCPWithLocation() throws SharkProtocolNotSupportedException,
			SharkKBException, SharkSecurityException, IOException,
			InterruptedException {
		String wkt_example = "POINT (30 10)";
		SharkGeometry geo = InMemoSharkGeometry.createGeomByWKT(wkt_example);

		// only works if this line is commented out (and therefore no location used)
		location = _aliceKB.getSpatialSTSet().createSpatialSemanticTag(
				"location name", new String[] { "http://example.com/loc1" }, geo);
		
		ContextCoordinates teapotCC = _aliceKB.createContextCoordinates(_teapotST,
				_alice, null, null, null, location, SharkCS.DIRECTION_INOUT);
		_aliceKB.createContextPoint(teapotCC);
		_aliceKB.getContextPoint(teapotCC).addInformation("Teapots freakin rock!");
		_aliceSyncKP.syncAllKnowledge();
		_bobSyncKP.syncAllKnowledge();
		_aliceEngine.setConnectionTimeOut(connectionTimeOut);
		_bobEngine.setConnectionTimeOut(connectionTimeOut);
		_aliceEngine.startTCP(_alicePort);
		_bobEngine.startTCP(_bobPort);
		_aliceEngine.publishAllKP(_bob);
		Thread.sleep(100);
		_bobEngine.publishAllKP(_alice);
		Thread.sleep(4000);
		_aliceEngine.stopTCP();
		Thread.sleep(10);
		_bobEngine.stopTCP();
		Thread.sleep(10);
		assertEquals("Teapots freakin rock!", _bobKB.getContextPoint(teapotCC)
				.getInformation().next().getContentAsString());
	}
}
