/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.descriptor;

import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.test.util.Dummy;
import org.junit.Test;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class StandardDescriptorSyncKPTest extends AbstractDescriptorTest
{

    @Test
    public void synchronisationTest() throws SharkKBException
    {
        final Dummy alice = new Dummy(ALICE_NAME, ALICE_SI);
        final SyncKB aliceKB = alice.getKnowledgeBase();
        final SharkEngine aliceEngine = alice.getEngine();
        final Dummy bob = new Dummy(BOB_NAME, BOB_SI);
        final SyncKB bobKB = bob.getKnowledgeBase();
        final SharkEngine bobEngine = bob.getEngine();
        
    }
}
