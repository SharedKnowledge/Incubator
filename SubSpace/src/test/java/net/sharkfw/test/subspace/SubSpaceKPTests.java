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
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.peer.StandardKP;
import net.sharkfw.subspace.SubSpaceKP;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.test.util.Dummy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nitros
 */
public class SubSpaceKPTests
{

    private static final String ALICE_NAME = "Alice";
    private static final String ALICE_SI = "http://www.sharksystem.net/alice.html";
    private static final String BOB_NAME = "Bob";
    private static final String BOB_SI = "http://www.sharksystem.net/bob.html";

    private static final String TEAPOT_SI = "https://en.wikipedia.org/?title=Teapot";
    private static final String JAVA_SI = "https://www.java.com";

    private static final STSet SEMANTIC_TAG_FACTORY = new InMemoPeerSTSet();

    @BeforeClass
    public static void setUpClass()
    {
        L.setLogLevel(L.LOGLEVEL_ALL);
    }

    @Test
    public void simpleDataExchange() throws SharkKBException, SharkSecurityException, IOException, SharkProtocolNotSupportedException
    {
        L.d("Start testing...");
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SharkEngine bobEngine = bob.getEngine();
        final SyncKB bobKB = bob.getKnowledgeBase();
        
        L.d("alice Adress: " + Arrays.toString(alice.getPeer().getAddresses()) + ", Bob Adresses: " + Arrays.toString(bob.getPeer().getAddresses()));

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
        
        final SemanticTag java = SEMANTIC_TAG_FACTORY.createSemanticTag("java", JAVA_SI);
        final ContextCoordinates javaCC = bobKB.createContextCoordinates(
                java,
                null,
                bob.getPeer(),
                alice.getPeer(),
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        final ContextPoint javaCP = bobKB.createContextPoint(javaCC);
        javaCP.addInformation("Bob ist toll!");
        L.d("################****************** Bob\n" + L.kb2String(bobKB));


        final SharkCS teapotCS = aliceKB.createInterest(teapotCC);
        final SubSpaceKP aliceKP = new SubSpaceKP(aliceEngine, teapotCS, aliceKB);
        final SubSpaceKP bobKP = new SubSpaceKP(bobEngine, teapotCS, bobKB);

        final List<KnowledgePort> alicePorts = Collections.list(aliceEngine.getKPs());
        final List<KnowledgePort> bobPorts = Collections.list(bobEngine.getKPs());
        L.d("Alice KPs: " + alicePorts.size());
        L.d("Bob KPs: " + bobPorts.size());

        bobEngine.startTCP(bob.getPort());
        aliceEngine.startTCP(alice.getPort());
        aliceEngine.publishKP(aliceKP);
        //bobEngine.publishKP(subSpaceKP);
        Assert.assertEquals(1, alicePorts.size());
        Assert.assertEquals(1, bobPorts.size());
        
        //bobEngine.stopTCP();
        //aliceEngine.stopTCP();
    }
}
