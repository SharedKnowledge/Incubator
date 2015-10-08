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
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.VersionUtils;
import net.sharkfw.peer.SharkEngine;
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
    public void pullTest() throws SharkKBException, DescriptorSchemaException, SharkProtocolNotSupportedException, SharkSecurityException, IOException, InterruptedException
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

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, multiDescriptor, bobRecipients);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        final int aliceCPSizeBefore = Collections.list(aliceKB.getAllContextPoints()).size();
        final int bobCPSizeBefore = Collections.list(bobKB.getAllContextPoints()).size();
        Assert.assertEquals(2, aliceCPSizeBefore);
        Assert.assertEquals(2, bobCPSizeBefore);

        alicePort.pull();
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

        bobPort.pull();
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

    @Test
    public void pullRequestTest() throws SharkKBException, DescriptorSchemaException, SharkProtocolNotSupportedException, IOException, SharkSecurityException, InterruptedException
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

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, multiDescriptor, bobRecipients);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        final int aliceCPSizeBefore = Collections.list(aliceKB.getAllContextPoints()).size();
        final int bobCPSizeBefore = Collections.list(bobKB.getAllContextPoints()).size();
        Assert.assertEquals(2, aliceCPSizeBefore);
        Assert.assertEquals(2, bobCPSizeBefore);

        alicePort.pullRequest();
        Thread.sleep(1000);

        final List<ContextPoint> aliceContextPointsAliceSync = Collections.list(aliceKB.getAllContextPoints());
        final List<ContextPoint> bobContextPointsAliceSync = Collections.list(bobKB.getAllContextPoints());
        final int aliceCPSizeAfterAliceSync = aliceContextPointsAliceSync.size();
        final int bobCPSizeAfterAliceSync = bobContextPointsAliceSync.size();

        Assert.assertEquals(aliceCPSizeBefore, aliceCPSizeAfterAliceSync);
        Assert.assertEquals(bobCPSizeBefore + 2, bobCPSizeAfterAliceSync);

        Assert.assertTrue(aliceContextPointsAliceSync.contains(aliceCCP));
        Assert.assertTrue(aliceContextPointsAliceSync.contains(aliceDeCP));
        Assert.assertFalse(aliceContextPointsAliceSync.contains(bobJavaCP));
        Assert.assertFalse(aliceContextPointsAliceSync.contains(bobTeapotCP));

        Assert.assertTrue(bobContextPointsAliceSync.contains(aliceCCP));
        Assert.assertTrue(bobContextPointsAliceSync.contains(aliceDeCP));
        Assert.assertTrue(bobContextPointsAliceSync.contains(bobJavaCP));
        Assert.assertTrue(bobContextPointsAliceSync.contains(bobTeapotCP));

        bobPort.pullRequest();
        Thread.sleep(1000);

        final List<ContextPoint> aliceContextPointsBobSync = Collections.list(aliceKB.getAllContextPoints());
        final List<ContextPoint> bobContextPointsBobSync = Collections.list(bobKB.getAllContextPoints());
        final int aliceCPSizeAfterBobSync = aliceContextPointsBobSync.size();
        final int bobCPSizeAfterBobSync = bobContextPointsBobSync.size();

        Assert.assertEquals(aliceCPSizeBefore + 1, aliceCPSizeAfterBobSync);
        Assert.assertEquals(bobCPSizeAfterAliceSync, bobCPSizeAfterBobSync);

        Assert.assertTrue(aliceContextPointsBobSync.contains(aliceCCP));
        Assert.assertTrue(aliceContextPointsBobSync.contains(aliceDeCP));
        Assert.assertTrue(aliceContextPointsBobSync.contains(bobJavaCP));
        Assert.assertFalse(aliceContextPointsBobSync.contains(bobTeapotCP));

        Assert.assertTrue(bobContextPointsBobSync.contains(aliceCCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(aliceDeCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(bobJavaCP));
        Assert.assertTrue(bobContextPointsBobSync.contains(bobTeapotCP));

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }

    @Test
    public void updateTest() throws SharkKBException, DescriptorSchemaException, SharkSecurityException, IOException, InterruptedException, SharkProtocolNotSupportedException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncDescriptorSchema aliceSchema = new SyncDescriptorSchema(aliceKB);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        final SyncDescriptorSchema bobSchema = new SyncDescriptorSchema(bobKB);

        final ContextCoordinates javaCC = new InMemoSharkKB().createContextCoordinates(
                javaTopic,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );

        aliceKB.createContextPoint(javaCC);
        final ContextPoint bobJavaCP = bobKB.createContextPoint(javaCC);
        bobJavaCP.addInformation(DummyDataFactory.TEST_INFORMATION);

        final STSet javaTopics = new InMemoSTSet();
        javaTopics.merge(javaTopic);

        final SharkCS javaContext = new InMemoSharkKB().createInterest(
                javaTopics,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );

        final ContextSpaceDescriptor javaDescriptor = new ContextSpaceDescriptor(javaContext, DESCRIPTOR_ID);
        aliceSchema.saveDescriptor(javaDescriptor);
        bobSchema.saveDescriptor(javaDescriptor);

        final PeerSTSet aliceRecipients = new InMemoPeerSTSet();
        aliceRecipients.merge(bob.getPeer());
        final StandardDescriptorSyncKP alicePort = new StandardDescriptorSyncKP(aliceEngine, aliceSchema, javaDescriptor, aliceRecipients);

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, javaDescriptor, bobRecipients);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        alicePort.pull();
        Thread.sleep(1000);

        final ContextPoint aliceJavaCPAliceSync = aliceKB.getContextPoint(javaCC);

        Assert.assertNotNull(aliceJavaCPAliceSync);
        Assert.assertTrue(aliceJavaCPAliceSync.equals(bobJavaCP));
        Assert.assertEquals(1, aliceJavaCPAliceSync.getNumberInformation());
        Assert.assertEquals(bobJavaCP.getNumberInformation(), aliceJavaCPAliceSync.getNumberInformation());
        final Information aliceInfoAliceSync = aliceJavaCPAliceSync.getInformation().next();
        final Information bobInfo = bobJavaCP.getInformation().next();
        Assert.assertNotNull(aliceInfoAliceSync);
        Assert.assertNotNull(bobInfo);
        Assert.assertTrue(bobInfo.getContentAsString().equals(aliceInfoAliceSync.getContentAsString()));
        Assert.assertEquals(VersionUtils.getVersion(bobJavaCP), VersionUtils.getVersion(aliceJavaCPAliceSync));

        bobPort.pull();
        Thread.sleep(1000);

        final ContextPoint bobJavaCPBobSync = bobKB.getContextPoint(javaCC);

        Assert.assertNotNull(bobJavaCPBobSync);
        Assert.assertTrue(bobJavaCPBobSync.equals(bobJavaCP));
        Assert.assertTrue(bobJavaCPBobSync.equals(aliceJavaCPAliceSync));
        Assert.assertEquals(1, bobJavaCPBobSync.getNumberInformation());
        Assert.assertEquals(bobJavaCP.getNumberInformation(), bobJavaCPBobSync.getNumberInformation());
        Assert.assertEquals(aliceJavaCPAliceSync.getNumberInformation(), bobJavaCPBobSync.getNumberInformation());
        final Information bobInfoBobSync = bobJavaCPBobSync.getInformation().next();
        Assert.assertTrue(bobInfo.getContentAsString().equals(bobInfoBobSync.getContentAsString()));
        Assert.assertTrue(aliceInfoAliceSync.getContentAsString().equals(bobInfoBobSync.getContentAsString()));
        Assert.assertEquals(VersionUtils.getVersion(bobJavaCP), VersionUtils.getVersion(bobJavaCPBobSync));
        Assert.assertEquals(VersionUtils.getVersion(aliceJavaCPAliceSync), VersionUtils.getVersion(bobJavaCPBobSync));

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }

    @Test
    public void syncDescriptorTest() throws SharkKBException, DescriptorSchemaException, SharkProtocolNotSupportedException, IOException, SharkSecurityException, InterruptedException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncDescriptorSchema aliceSchema = new SyncDescriptorSchema(aliceKB);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        final SyncDescriptorSchema bobSchema = new SyncDescriptorSchema(bobKB);

        final ContextSpaceDescriptor aliceDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        aliceSchema.saveDescriptor(aliceDescriptor);
        final ContextSpaceDescriptor bobDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        bobSchema.saveDescriptor(bobDescriptor);

        Assert.assertTrue(aliceDescriptor.identical(bobDescriptor));

        final PeerSTSet aliceRecipients = new InMemoPeerSTSet();
        aliceRecipients.merge(bob.getPeer());
        final StandardDescriptorSyncKP alicePort = new StandardDescriptorSyncKP(aliceEngine, aliceSchema, aliceDescriptor, aliceRecipients);

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, bobDescriptor, bobRecipients);

        final SemanticTag aliceJavaBefore = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag bobJavaBefore = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag aliceCBefore = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);
        final SemanticTag bobCBefore = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);

        Assert.assertNotNull(aliceJavaBefore);
        Assert.assertNotNull(bobJavaBefore);
        Assert.assertNull(aliceCBefore);
        Assert.assertNull(bobCBefore);

        final ContextSpaceDescriptor descriptorWithC = alicePort.getDescriptor();
        descriptorWithC.getContext().getTopics().createSemanticTag(C_NAME, C_SI);
        aliceSchema.overrideDescriptor(descriptorWithC);
        alicePort.setDescriptor(descriptorWithC);

        final SemanticTag aliceJavaBetween = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag bobJavaBetween = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag aliceCBetween = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);
        final SemanticTag bobCBetween = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);

        Assert.assertNotNull(aliceJavaBetween);
        Assert.assertNotNull(bobJavaBetween);
        Assert.assertNotNull(aliceCBetween);
        Assert.assertNull(bobCBetween);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        alicePort.sendDescriptor();
        Thread.sleep(1000);

        final SemanticTag aliceJavaAfter = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag bobJavaAfter = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(JAVA_SI);
        final SemanticTag aliceCAfter = alicePort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);
        final SemanticTag bobCAfter = bobPort.getDescriptor().getContext().getTopics().getSemanticTag(C_SI);

        Assert.assertNotNull(aliceJavaAfter);
        Assert.assertNotNull(bobJavaAfter);
        Assert.assertNotNull(aliceCAfter);
        Assert.assertNotNull(bobCAfter);

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }

    @Test
    public void recipientsChangeTest() throws SharkKBException, DescriptorSchemaException, SharkSecurityException, SharkProtocolNotSupportedException, IOException, InterruptedException
    {

        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncDescriptorSchema aliceSchema = new SyncDescriptorSchema(aliceKB);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        final SyncDescriptorSchema bobSchema = new SyncDescriptorSchema(bobKB);
        final Dummy eve = new Dummy(EVE_NAME, EVE_SI);

        final ContextSpaceDescriptor aliceDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        aliceSchema.saveDescriptor(aliceDescriptor);
        final ContextSpaceDescriptor bobDescriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        bobSchema.saveDescriptor(bobDescriptor);

        Assert.assertTrue(aliceDescriptor.identical(bobDescriptor));

        final PeerSTSet aliceRecipients = new InMemoPeerSTSet();
        aliceRecipients.merge(bob.getPeer());
        aliceRecipients.merge(eve.getPeer());
        final StandardDescriptorSyncKP alicePort = new StandardDescriptorSyncKP(aliceEngine, aliceSchema, aliceDescriptor, aliceRecipients);

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());
        bobRecipients.merge(eve.getPeer());
        final StandardDescriptorSyncKP bobPort = new StandardDescriptorSyncKP(bobEngine, bobSchema, bobDescriptor, bobRecipients);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        Assert.assertEquals(2, alicePort.getRecipients().size());
        Assert.assertEquals(2, bobPort.getRecipients().size());

        alicePort.removeRecipient(eve.getPeer());
        Thread.sleep(1000);

        Assert.assertEquals(1, alicePort.getRecipients().size());
        Assert.assertEquals(1, bobPort.getRecipients().size());

        final PeerSemanticTag aliceEveRemoved = alicePort.getRecipients().getSemanticTag(eve.getPeer().getSI());
        final PeerSemanticTag bobEveRemoved = bobPort.getRecipients().getSemanticTag(eve.getPeer().getSI());

        Assert.assertNull(aliceEveRemoved);
        Assert.assertNull(bobEveRemoved);

        bobPort.addRecipient(eve.getPeer());
        Thread.sleep(1000);

        Assert.assertEquals(2, alicePort.getRecipients().size());
        Assert.assertEquals(2, bobPort.getRecipients().size());

        final PeerSemanticTag aliceEveAdded = alicePort.getRecipients().getSemanticTag(eve.getPeer().getSI());
        final PeerSemanticTag bobEveAdded = bobPort.getRecipients().getSemanticTag(eve.getPeer().getSI());

        Assert.assertNotNull(aliceEveAdded);
        Assert.assertNotNull(bobEveAdded);

        bobPort.removeRecipient(alice.getPeer());
        Thread.sleep(1000);

        Assert.assertEquals(1, alicePort.getRecipients().size());
        Assert.assertEquals(1, bobPort.getRecipients().size());

        final PeerSemanticTag aliceAliceRemoved = alicePort.getRecipients().getSemanticTag(bob.getPeer().getSI());
        final PeerSemanticTag bobAliceRemoved = bobPort.getRecipients().getSemanticTag(alice.getPeer().getSI());

        Assert.assertNull(aliceAliceRemoved);
        Assert.assertNull(bobAliceRemoved);

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }
}
