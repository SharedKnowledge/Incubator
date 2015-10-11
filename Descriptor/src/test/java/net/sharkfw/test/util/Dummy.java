package net.sharkfw.test.util;

import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.SharkEngine;

/**
 * Creates a dummy with a peer, an engine, a port and a knowledge base.
 *
 * @author Nitros Razril (pseudonym)
 */
public class Dummy
{

    /**
     * Address prefix.
     */
    private static final String ADDRESS = "tcp://localhost";
    /**
     * Start number of port.
     */
    private static int currentPort = 5555;

    /**
     * The dummies knowledge base.
     */
    private final SyncKB knowledgeBase;
    /**
     * The dummy as peer.
     */
    private final PeerSemanticTag peer;
    /**
     * The dummies engine.
     */
    private final SharkEngine engine;
    /**
     * The dummies port.
     */
    private final int port;

    /**
     * Creates a new dummy with an name and SI.
     *
     * @param name Name of the new dummy.
     * @param subjectIdentifier SI of the new dummy.
     * @throws SharkKBException If creating the dummy fails.
     */
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

    /**
     * Gets {@link #knowledgeBase}
     *
     * @return {@link #knowledgeBase}
     */
    public SyncKB getKnowledgeBase()
    {
        return knowledgeBase;
    }

    /**
     * Gets {@link #peer}
     *
     * @return {@link #peer}
     */
    public PeerSemanticTag getPeer()
    {
        return peer;
    }

    /**
     * Gets {@link #engine}
     *
     * @return {@link #engine}
     */
    public SharkEngine getEngine()
    {
        return engine;
    }

    /**
     * Gets {@link #port}
     *
     * @return {@link #port}
     */
    public int getPort()
    {
        return port;
    }
    
    /**
     * Creates a new port. For that, {@link #currentPort} is increased by one
     * an its value is returned. This means every call of this method gets
     * a port one number high than the last one.
     * 
     * @return A new port.
     */
    private static int createPort()
    {
        currentPort++;
        return currentPort;
    }
}
