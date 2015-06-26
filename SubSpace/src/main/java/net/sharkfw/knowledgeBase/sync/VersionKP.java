/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.knowledgeBase.sync;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.security.utility.LoggingUtil;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;

/**
 *
 * @author Nitros
 */
public abstract class VersionKP extends KnowledgePort
{

    private final TimestampList timestampList;

    /**
     *
     * @param sharkEngine
     * @param syncKB
     * @param context
     *
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    public VersionKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context)
    {
        super(sharkEngine, syncKB);
        if (syncKB.getOwner() == null)
        {
            throw new IllegalArgumentException("Paratmeter of Type " + syncKB.getClass().getName() + " must have an owner set.");
        }
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

    protected abstract Knowledge getOffer() throws SharkKBException;

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
        final String knowledgeString = L.knowledge2String(knowledge);
        final String ownerName = kb.getOwner().getName();
        LoggingUtil.debugBox(ownerName + " received Knowledge for insert", knowledgeString, this);
        try
        {
            // Only do insert, wenn we are actually intereded in receiving Information.
            if (isIKP())
            {
                final Enumeration<ContextPoint> contextPoints = knowledge.contextPoints();
                while (contextPoints.hasMoreElements())
                {
                    final ContextPoint remoteContextPoint = contextPoints.nextElement();
                    final ContextCoordinates remoteContextCoordinates = remoteContextPoint.getContextCoordinates();
                    final ContextPoint localContextPoint = kb.getContextPoint(remoteContextCoordinates);
                    // Compare version of the ContextPoint and ad to KnowledgeBase if necessary.
                    final int localVersion = (localContextPoint == null) ? 0 : VersionUtils.getVersion(localContextPoint);
                    final int remoteVersion = VersionUtils.getVersion(remoteContextPoint);
                    if (remoteVersion > localVersion)
                    {
                        final String contextPointString = L.cp2String(remoteContextPoint);
                        LoggingUtil.debugBox("Insert/Replace ContextPoint in KB of " + ownerName + ".", contextPointString, this);
                        VersionUtils.replaceContextPoint(remoteContextPoint, kb);
                    }
                }
            } else
            {
                L.d("Skipped insert due to KP intereded in receiving Information.", this);
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Exception occurred in doInsert.", ex);
        }
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        final String ownerName = kb.getOwner().getName();
        final String contextSpaceString = L.contextSpace2String(context);
        LoggingUtil.debugBox(ownerName + " received interest and tries to collect Knowledge.", contextSpaceString, this);
        try
        {
            final PeerSemanticTag sender = kepConnection.getSender();
            final String[] peerAddresses = sender.getAddresses();
            final Date lastPeerMeeting = timestampList.getTimestamp(sender);
            final Knowledge knowledge = getOffer(lastPeerMeeting);
            kepConnection.insert(knowledge, peerAddresses);
            notifyInsertSent(this, knowledge);
            timestampList.resetTimestamp(sender);
        } catch (SharkException ex)
        {
            throw new IllegalStateException("Exception occurred in doExpose.", ex);
        }
    }

    private Knowledge getOffer(final Date lastPeerMeeting) throws SharkKBException
    {
        final Knowledge offer = getOffer();
        LoggingUtil.debugBox("Knowledge from Offer.", L.knowledge2String(offer), this);
        final Enumeration<ContextPoint> offeredContextPoints = offer.contextPoints();
        while (offeredContextPoints.hasMoreElements())
        {
            final ContextPoint element = offeredContextPoints.nextElement();
            final String dateProperty = element.getProperty(SyncContextPoint.TIMESTAMP_PROPERTY_NAME);
            if (dateProperty == null)
            {
                L.d("Skipping ContextPoint, date property was not set.", this);
            } else
            {
                final long time = Long.parseLong(dateProperty);
                final Date contextPointTimestamp = new Date(time);
                if (!contextPointTimestamp.after(lastPeerMeeting))
                {
                    offer.removeContextPoint(element);
                    printTimestampNotAfterDebugMessage(contextPointTimestamp, lastPeerMeeting);
                } 
            }
        }
        final String offerString = L.knowledge2String(offer);
        LoggingUtil.debugBox("Filtered Knowledge.", offerString, this);
        return offer;
    }

    private void printTimestampNotAfterDebugMessage(final Date contextPointTimestamp, final Date lastPeerMeeting)
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z", Locale.getDefault());
        final String contextPointTimestampString = dateFormat.format(contextPointTimestamp);
        final String lastPeerMeetingString = dateFormat.format(lastPeerMeeting);
        L.d(
                "Skipping ContextPoint, ContextPoint Timestamp ("
                + contextPointTimestampString
                + ") is not after last meeting of peer ("
                + lastPeerMeetingString
                + ").",
                this
        );
    }
}
