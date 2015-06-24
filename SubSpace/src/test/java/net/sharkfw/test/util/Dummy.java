/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.util;

import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author Nitros
 */
public class Dummy
{

    private static final String ADDRESS = "tcp://localhost";
    private static int currentPort = 5555;

    private final SyncKB knowledgeBase;
    private final PeerSemanticTag peer;
    private final SharkEngine engine;
    private final int port;

    public Dummy(final String name, final String subjectIdentifier) throws SharkKBException
    {
        knowledgeBase = new SyncKB(new InMemoSharkKB());
        final PeerSTSet peerFactory = new InMemoPeerSTSet();
        port = createPort();
        final String peerAddress = ADDRESS + ":" + port;
        peer = peerFactory.createPeerSemanticTag(name, subjectIdentifier, peerAddress);
        knowledgeBase.setOwner(peer);
        engine = new J2SEAndroidSharkEngine();
    }

    public SyncKB getKnowledgeBase()
    {
        return knowledgeBase;
    }

    public PeerSemanticTag getPeer()
    {
        return peer;
    }

    public SharkEngine getEngine()
    {
        return engine;
    }

    public int getPort()
    {
        return port;
    }

    private static int createPort()
    {
        currentPort++;
        return currentPort;
    }

}
