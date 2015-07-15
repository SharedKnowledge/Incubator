/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.subspace;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.LoggingUtil;
import net.sharkfw.subspace.knowledgeBase.StandardSubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.peer.SubSpaceKP;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.test.util.Dummy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class SubSpaceKPTests extends AbstractSubSpaceTest
{
    @Test
    public void simpleDataExchange() throws SharkKBException, SharkSecurityException, IOException, SharkProtocolNotSupportedException, InterruptedException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SharkEngine bobEngine = bob.getEngine();
        final SyncKB bobKB = bob.getKnowledgeBase();

        L.d("Alice: " + Arrays.toString(alice.getPeer().getAddresses()), this);
        L.d("Bob: " + Arrays.toString(bob.getPeer().getAddresses()), this);

        //Add teapot to alcie
        final SemanticTag teapot = SEMANTIC_TAG_FACTORY.createSemanticTag("teapot", TEAPOT_SI);
        final ContextCoordinates teapotCC = aliceKB.createContextCoordinates(
                teapot,
                null,
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint teapotCP = aliceKB.createContextPoint(teapotCC);
        teapotCP.addInformation("Teapots can be very pretty.");
        final SemanticTag java = SEMANTIC_TAG_FACTORY.createSemanticTag("java", JAVA_SI);
        //Bobs Java
        final ContextCoordinates bobJavaCC = bobKB.createContextCoordinates(
                java,
                null,
                bob.getPeer(),
                alice.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint bobJavaCP = bobKB.createContextPoint(bobJavaCC);
        bobJavaCP.addInformation("Java ist die beste Programmiersprache!");
        final SharkCS bobJavaCS = bobKB.createInterest(bobJavaCC);

        //Alices Java
        final ContextCoordinates aliceJavaCC = bobKB.createContextCoordinates(
                java,
                null,
                alice.getPeer(),
                bob.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final SharkCS aliceJavaCS = aliceKB.createInterest(aliceJavaCC);

        //KBs before sending
        //L.d(L.kb2String(aliceKB), this);
        //L.d(L.kb2String(bobKB), this);

        //SubSpace
        final SubSpace aliceJavaSubSpace = new StandardSubSpace(aliceKB, aliceJavaCS, java);
        final SubSpace bobJavaSubSpace = new StandardSubSpace(bobKB, bobJavaCS, java);
        //KnowledgePorts
        final SubSpaceKP aliceKP = new SubSpaceKP(aliceEngine, aliceJavaSubSpace);
        final SubSpaceKP bobKP = new SubSpaceKP(bobEngine, bobJavaSubSpace);

        final List<KnowledgePort> alicePorts = Collections.list(aliceEngine.getKPs());
        final List<KnowledgePort> bobPorts = Collections.list(bobEngine.getKPs());
        L.d("Alice KPs: " + alicePorts.size());
        L.d("Bob KPs: " + bobPorts.size());

        bobEngine.startTCP(bob.getPort());
        aliceEngine.startTCP(alice.getPort());
        aliceEngine.publishKP(aliceKP);
        //bobEngine.publishKP(subSpaceKP);
        Thread.sleep(2000);
        Assert.assertEquals(1, alicePorts.size());
        Assert.assertEquals(1, bobPorts.size());
        //bobEngine.stopTCP();
        //aliceEngine.stopTCP();

        //KBs after sending
        //L.d(L.kb2String(aliceKB), this);
        //L.d(L.kb2String(bobKB), this);

        List<ContextPoint> points = Collections.list(aliceKB.getAllContextPoints());
        Assert.assertEquals(2, points.size());

        
        LoggingUtil.debugBox("SubSpace Alice Knowledge", L.knowledge2String(aliceJavaSubSpace.getKnowledge()));
        

        //Assert.assertNotNull(contextPoint);
    }
}
