/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.knowledgeBase.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;

/**
 *
 * @author Nitros
 */
public abstract class VersionKP extends KnowledgePort
{

    private final static Logger LOGGER = Logger.getLogger(VersionKP.class.getName());

    private final TimestampList timestampList;

    public VersionKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context)
    {
        super(sharkEngine, syncKB);
        try
        {
            this.interest = InMemoSharkKB.createInMemoCopy(context);
            final PeerSTSet remotePeers = context.getRemotePeers();
            this.timestampList = new TimestampList(remotePeers, syncKB);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not initialize Class " + getClass().getSimpleName() + " due to Exception occurring.", ex);
        }
    }

    protected abstract Knowledge getOffer();

    /**
     * Inserts the received {@link Knowledge} into the local
     * {@link SharkKB}.<br/>
     * The List of {@link ContextPoint} is taken from the {@link Knowledge} and
     * the versions of those {@link ContextPoint} is compared against the
     * version of the local {@link ContextPoint} with the same
     * {@link ContextCoordinates}. If the version of the received
     * {@link ContextPoint} is higher than the local one or no local
     * {@link ContextPoint} for this {@link ContextCoordinates} exists, the
     * received {@link ContextPoint} will replace the local {@link ContextPoint}
     * or the received one is simply inserted respectively.
     *
     * @param knowledge
     * @param kepConnection
     *
     * @throws IllegalStateException If a SharkKBException occurs while
     * executing this method. That means, the KP can't work propertly.
     */
    @Override
    protected void doInsert(final Knowledge knowledge, final KEPConnection kepConnection)
    {
        System.out.println("Hello World!!!");
        L.d("****************************:::::::::::::::::::::::::::::::::::::::::::::Insert bekommen:\n" + L.knowledge2String(knowledge));
//        try
//        {
//            // Only do insert, wenn we are actually intereded in receiving Information.
//            if (isIKP())
//            {
//                final Enumeration<ContextPoint> contextPoints = knowledge.contextPoints();
//                while (contextPoints.hasMoreElements())
//                {
//                    final ContextPoint remoteContextPoint = contextPoints.nextElement();
//                    final ContextCoordinates remoteContextCoordinates = remoteContextPoint.getContextCoordinates();
//                    final ContextPoint localContextPoint = kb.getContextPoint(remoteContextCoordinates);
//                    // Compare version of the ContextPoint and ad to KnowledgeBase if necessary.
//                    final int localVersion = (localContextPoint == null) ? 0 : VersionUtils.getVersion(localContextPoint);
//                    final int remoteVersion = VersionUtils.getVersion(remoteContextPoint);
//                    if (remoteVersion > localVersion)
//                    {
//                        VersionUtils.replaceContextPoint(remoteContextPoint, kb);
//                    }
//                }
//            }
//        } catch (SharkKBException ex)
//        {
//            throw new IllegalStateException("Exception occurred in doInsert.", ex);
//        }
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        String contextString = L.contextSpace2String(context);
        L.d("************************************** " + kb.getOwner().getName() + " Received interest:\n" + contextString);
        try
        {
            kepConnection.insert(getOffer(), kepConnection.getSender().getAddresses());
        } catch (SharkException ex)
        {
            Logger.getLogger(VersionKP.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
        this.notifyExposeSent(this, this.getInterest());
        
    }

    private List<SyncContextPoint> collectSyncContextPoints(final Date lastPeerMeeting) throws SharkKBException
    {
        final List<SyncContextPoint> retrievedContextPoints = new ArrayList<SyncContextPoint>();
        final Knowledge offer = getOffer();
        final Enumeration<ContextPoint> offeredContextPoints = offer.contextPoints();
        while (offeredContextPoints.hasMoreElements())
        {
            final ContextPoint element = offeredContextPoints.nextElement();
            final String dateProperty = element.getProperty(SyncContextPoint.TIMESTAMP_PROPERTY_NAME);
            if (dateProperty != null)
            {
                final long time = Long.parseLong(dateProperty);
                final Date contextPointTimestamp = new Date(time);
                if (contextPointTimestamp.after(lastPeerMeeting))
                {
                    final SyncContextPoint syncContextPoint = VersionUtils.copy(element);
                    retrievedContextPoints.add(syncContextPoint);
                }
            }
        }
        return retrievedContextPoints;
    }
}
