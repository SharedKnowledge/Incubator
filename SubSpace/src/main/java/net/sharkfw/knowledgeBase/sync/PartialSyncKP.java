/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.knowledgeBase.sync;

import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.STSetListener;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author Nitros
 */
public class PartialSyncKP extends KnowledgePort
{

    private final static Logger LOGGER = Logger.getLogger(PartialSyncKP.class.getName());

    private final TimestampList timestampList;

    private final STSetListener remotePeerObserver = new STSetListener()
    {

        @Override
        public void semanticTagCreated(final SemanticTag tag, final STSet set)
        {
            if (tag instanceof PeerSemanticTag)
            {
                final PeerSemanticTag peer = (PeerSemanticTag) tag;
                timestampList.newPeer(peer);
            }
        }

        @Override
        public void semanticTagRemoved(final SemanticTag tag, final STSet set)
        {
            if (tag instanceof PeerSemanticTag)
            {
                final PeerSemanticTag peer = (PeerSemanticTag) tag;
                timestampList.removePeer(peer);
            }
        }

        @Override
        public void semanticTagChanged(final SemanticTag tag, final STSet stset)
        {
            //not interested in
        }
    };

    public PartialSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context)
    {
        super(sharkEngine, syncKB);
        this.interest = context;
        final PeerSTSet remotePeers = context.getRemotePeers();
        timestampList = new TimestampList(remotePeers, syncKB);
        remotePeers.addListener(remotePeerObserver);
    }

    @Override
    protected void doInsert(final Knowledge knowledge, final KEPConnection kepConnection)
    {
        try
        {
            final Enumeration<ContextPoint> contextPoints = knowledge.contextPoints();
            while (contextPoints.hasMoreElements())
            {
                final ContextPoint remoteContextPoint = contextPoints.nextElement();
                final ContextCoordinates remoteContextCoordinates = remoteContextPoint.getContextCoordinates();
                final ContextPoint localContextPoint = kb.getContextPoint(remoteContextCoordinates);

                final int localVersion = (localContextPoint == null) ? 0 : getVersion(localContextPoint);
                final int remoteVersion = getVersion(remoteContextPoint);
                if (remoteVersion > localVersion)
                {
                    getAsSyncKB().replaceContextPoint(remoteContextPoint);
                }
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Exception occurred in doInsert.", ex);
        }
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kePConnection)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int getVersion(final ContextPoint contextPoint) throws SharkKBException
    {
        final String versionProperty = contextPoint.getProperty(SyncContextPoint.VERSION_PROPERTY_NAME);
        return (versionProperty == null) ? 0 : Integer.parseInt(versionProperty);
    }

    public SyncKB getAsSyncKB()
    {
        if (!(kb instanceof SyncKB))
        {
            throw new IllegalStateException("Underlying knowldege base is not of type SyncKB.");
        }
        return (SyncKB) kb;
    }

}
