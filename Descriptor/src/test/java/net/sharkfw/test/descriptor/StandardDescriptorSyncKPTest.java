/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.descriptor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.descriptor.peer.StandardDescriptorSyncKP;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.L;
import net.sharkfw.system.LoggingUtils;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.test.util.Dummy;
import net.sharkfw.test.util.DummyDataFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class StandardDescriptorSyncKPTest extends AbstractDescriptorTest
{

    private static final String DESCRIPTOR_ID = "C_and_Java";

    private final SemanticTag javaTopic;
    private final SemanticTag cTopic;
    private final SemanticTag teapotTopic;
    private final SemanticTag deTopic;

    public StandardDescriptorSyncKPTest() throws SharkKBException
    {
        super();
        javaTopic = new InMemoSTSet().createSemanticTag(JAVA_NAME, JAVA_SI);
        cTopic = new InMemoSTSet().createSemanticTag(C_NAME, C_SI);
        teapotTopic = new InMemoSTSet().createSemanticTag(TEAPOT_NAME, TEAPOT_SI);
        deTopic = new InMemoSTSet().createSemanticTag(DEUTSCHLAND_NAME, DEUTSCHLAND_NAME);
    }

    @Test
    public void synchronisationTest() throws SharkKBException, DescriptorSchemaException, SharkProtocolNotSupportedException, SharkSecurityException, IOException, InterruptedException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncDescriptorSchema aliceSchema = new SyncDescriptorSchema(aliceKB);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        final SyncDescriptorSchema bobSchema = new SyncDescriptorSchema(bobKB);

        final ContextPoint aliceCCP = DummyDataFactory.createSimpleContextPoint(aliceKB, cTopic);
        final ContextPoint aliceDeCP = DummyDataFactory.createSimpleContextPoint(aliceKB, deTopic);
        final ContextPoint bobJavaCP = DummyDataFactory.createSimpleContextPoint(bobKB, javaTopic);
        final ContextPoint bobTeapotCP = DummyDataFactory.createSimpleContextPoint(bobKB, teapotTopic);

        final STSet multiTopics = new InMemoSTSet();
        multiTopics.merge(javaTopic);
        multiTopics.merge(cTopic);
        multiTopics.merge(deTopic);

        final SharkCS multiContext = new InMemoSharkKB().createInterest(
                multiTopics,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );

        final ContextSpaceDescriptor multiDescriptor = new ContextSpaceDescriptor(multiContext, DESCRIPTOR_ID);
        aliceSchema.saveDescriptor(multiDescriptor);
        bobSchema.saveDescriptor(multiDescriptor);

        final PeerSTSet aliceRecipients = new InMemoPeerSTSet();
        aliceRecipients.merge(bob.getPeer());
        final StandardDescriptorSyncKP alicePort = new StandardDescriptorSyncKP(aliceEngine, aliceSchema, multiDescriptor, aliceRecipients);
        LoggingUtils.logBox("alice interest", L.contextSpace2String(alicePort.getInterest()));

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, multiDescriptor, bobRecipients);
        LoggingUtils.logBox("Bob interest", L.contextSpace2String(bobPort.getInterest()));

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        final int aliceCPSizeBefore = Collections.list(aliceKB.getAllContextPoints()).size();
        final int bobCPSizeBefore = Collections.list(bobKB.getAllContextPoints()).size();
        Assert.assertEquals(2, aliceCPSizeBefore);
        Assert.assertEquals(2, bobCPSizeBefore);

        alicePort.send();
        Thread.sleep(1000);

        final List<ContextPoint> aliceContextPointsAliceSync = Collections.list(aliceKB.getAllContextPoints());
        final List<ContextPoint> bobContextPointsAliceSync = Collections.list(bobKB.getAllContextPoints());
        final int aliceCPSizeAfterAliceSync = aliceContextPointsAliceSync.size();
        final int bobCPSizeAfterAliceSync = bobContextPointsAliceSync.size();

        Assert.assertEquals(aliceCPSizeBefore + 1, aliceCPSizeAfterAliceSync);
        Assert.assertEquals(bobCPSizeBefore, bobCPSizeAfterAliceSync);

        Assert.assertTrue(aliceContextPointsAliceSync.contains(aliceCCP));
        Assert.assertTrue(aliceContextPointsAliceSync.contains(aliceDeCP));
        Assert.assertTrue(aliceContextPointsAliceSync.contains(bobJavaCP));
        Assert.assertFalse(aliceContextPointsAliceSync.contains(bobTeapotCP));

        Assert.assertTrue(bobContextPointsAliceSync.contains(bobJavaCP));
        Assert.assertTrue(bobContextPointsAliceSync.contains(bobTeapotCP));
        Assert.assertFalse(bobContextPointsAliceSync.contains(aliceCCP));
        Assert.assertFalse(bobContextPointsAliceSync.contains(aliceDeCP));

        bobPort.send();
        Thread.sleep(1000);

        final List<ContextPoint> aliceContextPointsBobSync = Collections.list(aliceKB.getAllContextPoints());
        final List<ContextPoint> bobContextPointsBobSync = Collections.list(bobKB.getAllContextPoints());
        final int aliceCPSizeAfterBobSync = aliceContextPointsBobSync.size();
        final int bobCPSizeAfterBobSync = bobContextPointsBobSync.size();
        
        Assert.assertEquals(aliceCPSizeAfterAliceSync, aliceCPSizeAfterBobSync);
        Assert.assertEquals(bobCPSizeBefore + 2, bobCPSizeAfterBobSync);

        Assert.assertTrue(bobContextPointsBobSync.contains(aliceCCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(aliceDeCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(bobJavaCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(bobTeapotCP));
             
        Assert.assertTrue(aliceContextPointsBobSync.contains(aliceCCP));
        Assert.assertTrue(aliceContextPointsBobSync.contains(aliceDeCP));
        Assert.assertTrue(aliceContextPointsBobSync.contains(bobJavaCP));
        Assert.assertFalse(aliceContextPointsBobSync.contains(bobTeapotCP));

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }
}
