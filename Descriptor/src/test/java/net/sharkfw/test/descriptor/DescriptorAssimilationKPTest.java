package net.sharkfw.test.descriptor;

import java.io.IOException;
import junit.framework.Assert;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.descriptor.peer.DescriptorAssimilationKP;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;
import static net.sharkfw.test.descriptor.AbstractDescriptorTest.ALICE_NAME;
import net.sharkfw.test.util.Dummy;
import net.sharkfw.test.util.DummyDataFactory;
import org.junit.Test;

/**
 *  A class to test the {@link DescriptorAssimilationKP}.
 * 
 * @author @author Nitros Razril (pseudonym)
 */
public class DescriptorAssimilationKPTest extends AbstractDescriptorTest
{

    /**
     * Send a {@link ContextSpaceDescriptor} from Alice to Bob. Bobs schema
     * {@link DescriptorSchema} should have the new @link ContextSpaceDescriptor}
     * afterwards.
     */
    @Test
    public void asssimilationTest() throws SharkKBException, DescriptorSchemaException, SharkProtocolNotSupportedException, IOException, SharkSecurityException, InterruptedException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final SyncDescriptorSchema aliceSchema = new SyncDescriptorSchema(aliceKB);
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        final SyncDescriptorSchema bobSchema = new SyncDescriptorSchema(bobKB);

        final ContextSpaceDescriptor descriptor = DummyDataFactory.createSimpleDescriptor(JAVA_NAME, JAVA_SI);
        aliceSchema.saveDescriptor(descriptor);

        final PeerSTSet aliceRecipients = new InMemoPeerSTSet();
        aliceRecipients.merge(bob.getPeer());

        final PeerSTSet bobRecipients = new InMemoPeerSTSet();
        bobRecipients.merge(alice.getPeer());

        final DescriptorAssimilationKP alicePort = new DescriptorAssimilationKP(aliceEngine, aliceSchema, aliceRecipients);
        final DescriptorAssimilationKP bobPort = new DescriptorAssimilationKP(bobEngine, bobSchema, bobRecipients);

        aliceEngine.startTCP(alice.getPort());
        bobEngine.startTCP(bob.getPort());

        Assert.assertFalse(bobSchema.containsIdentical(descriptor));

        alicePort.sendDescriptor(descriptor);
        Thread.sleep(1000);

        Assert.assertTrue(bobSchema.containsIdentical(descriptor));
        Assert.assertTrue(bobPort.getSchema().containsIdentical(descriptor));

        aliceEngine.stopTCP();
        bobEngine.stopTCP();
    }
}
