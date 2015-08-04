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
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.FragmentationParameter;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.SharkVocabulary;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.LoggingUtil;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractSyncKP extends KnowledgePort
{

    private static final String PROTOCOL_STATE = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#PROTOCOL_STATE";

    private static final String PROTOCOL_STATE_NONE = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#PROTOCOL_STATE_NONE";
    private static final String SERIALIZED_CONTEXT_POINTS = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#SERIALIZED_CONTEXT_POINTS";
    private static final String PROTOCOL_STATE_OFFER = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#PROTOCOL_STATE_OFFER";
    private static final String PROTOCOL_STATE_REQUEST = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#PROTOCOL_STATE_REQUEST";

    private final TimestampList timestampList;
    private final FragmentationParameter topicsFP;
    private final FragmentationParameter peersFP;

    public AbstractSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context, final FragmentationParameter topicFP, final FragmentationParameter peerFP)
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
            this.topicsFP = topicFP;
            this.peersFP = peerFP;
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not initialize Class " + getClass().getSimpleName() + " due to Exception occurring.", ex);
        }
    }

    public AbstractSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context)
    {
        this(sharkEngine, syncKB, context, FragmentationParameter.getZeroFP(), FragmentationParameter.getZeroFP());
    }

    protected abstract Knowledge getOffer() throws SharkKBException;

    protected abstract boolean isInterested(final SharkCS context);

    protected abstract SemanticTag getIdentifier(final SharkCS context);

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
                L.d("Skipped insert due to KP not intereded in receiving Information.", this);
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Exception occurred in doInsert.", ex);
        }
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        try
        {
            final SemanticTag identifier = getIdentifier(context);
            if (isOKP() && identifier != null && isInterested(context))
            {
                final String protocolState = getProtocoleState(identifier);
                switch (protocolState)
                {
                    case PROTOCOL_STATE_NONE:
                        offerContextPoints(context, kepConnection);
                        break;
                    case PROTOCOL_STATE_OFFER:
                        requestContextPoints(context, kepConnection);
                        break;
                    case PROTOCOL_STATE_REQUEST:
                        answerRequest(context, kepConnection);
                        break;
                    default:
                        throw new IllegalStateException("Could not find a vaild protocol state.");
                }
            } else
            {
                final StringBuilder builder = new StringBuilder();
                if (!isOKP())
                {
                    builder.append("This KnowledgePort does not send data. It is not a OKP.");
                } else if (identifier == null)
                {
                    builder.append("Cannot proceed as no identifier was provided.").append(" ");
                    builder.append("Method 'getIdentifier(SharkCS)' returns null.");
                } else
                {
                    final String contextAsString = L.contextSpace2String(context);
                    builder.append("Not interested in given Interest.").append(" ");
                    builder.append("Interest is:").append(" ").append("\n");
                    builder.append(contextAsString);
                }
                final String debugMessage = builder.toString();
                L.d(debugMessage, this);
            }
        } catch (SharkException ex)
        {
            throw new IllegalStateException("Exception occurred in doExpose.", ex);
        }
    }

    private void offerContextPoints(final SharkCS context, final KEPConnection kepConnection) throws SharkException
    {
        final PeerSemanticTag sender = kepConnection.getSender();
        final String[] senderAddresses = sender.getAddresses();
        final Date lastPeerMeeting = timestampList.getTimestamp(sender);
        final List<SyncContextPoint> syncContextPoints = getOffer(lastPeerMeeting);
        final String serializedContextPoints = ContextCoordinatesSerializer.serializeContextCoordinatesList(syncContextPoints);
        final SemanticTag identifier = getIdentifier(context);
        identifier.setProperty(SERIALIZED_CONTEXT_POINTS, serializedContextPoints);
        identifier.setProperty(PROTOCOL_STATE, PROTOCOL_STATE_OFFER);
        kepConnection.expose(context, senderAddresses);
        this.notifyExposeSent(this, context);
    }

    private void requestContextPoints(final SharkCS context, final KEPConnection kepConnection) throws SharkException
    {
        final List<SyncContextPoint> requestContextPoints = new ArrayList<>();
        final PeerSemanticTag sender = kepConnection.getSender();
        final String[] senderAddresses = sender.getAddresses();
        final SemanticTag identifier = getIdentifier(context);
        final String contextPointsProperty = identifier.getProperty(SERIALIZED_CONTEXT_POINTS);
        if (contextPointsProperty != null)
        {
            final List<SyncContextPoint> remoteContextPoints = ContextCoordinatesSerializer.deserializeContextCoordinatesList(contextPointsProperty);
            for (SyncContextPoint remoteContextPoint : remoteContextPoints)
            {
                final ContextCoordinates remoteContextCoordinates = remoteContextPoint.getContextCoordinates();
                final ContextPoint localContextPoint = kb.getContextPoint(remoteContextCoordinates);
                final int localVersion = (localContextPoint == null) ? 0 : VersionUtils.getVersion(localContextPoint);
                final int remoteVersion = VersionUtils.getVersion(remoteContextPoint);
                if (remoteVersion > localVersion)
                {
                    requestContextPoints.add(remoteContextPoint);
                }
            }
            final String serializedContextPoints = ContextCoordinatesSerializer.serializeContextCoordinatesList(requestContextPoints);
            identifier.setProperty(SERIALIZED_CONTEXT_POINTS, serializedContextPoints);
            identifier.setProperty(PROTOCOL_STATE, PROTOCOL_STATE_REQUEST);
            kepConnection.expose(context, senderAddresses);
            this.notifyExposeSent(this, context);
        } else
        {
            throw new IllegalStateException("No ContextPoint found. Property AbstractSyncKP#SERIALIZED_CONTEXT_POINTS on identifier is null.");
        }
    }

    private void answerRequest(final SharkCS context, final KEPConnection kepConnection) throws SharkException
    {
        final PeerSemanticTag sender = kepConnection.getSender();
        final String[] senderAddresses = sender.getAddresses();
        final SemanticTag identifier = getIdentifier(context);
        final String contextPointsProperty = identifier.getProperty(SERIALIZED_CONTEXT_POINTS);
        if (contextPointsProperty != null)
        {
            final List<SyncContextPoint> remoteSyncContextPoints = ContextCoordinatesSerializer.deserializeContextCoordinatesList(contextPointsProperty);
            final Knowledge knowledge = new InMemoSharkKB().asKnowledge();
            for (SyncContextPoint remoteSyncContextPoint : remoteSyncContextPoints)
            {
                final ContextCoordinates remoteContextCoordinates = remoteSyncContextPoint.getContextCoordinates();
                final ContextPoint localContextPoint = kb.getContextPoint(remoteContextCoordinates);
                if (localContextPoint != null)
                {
                    final ContextCoordinates localContextCoordinates = localContextPoint.getContextCoordinates();
                    final SemanticTag topic = localContextCoordinates.getTopic();
                    final SharkVocabulary vocabulary = knowledge.getVocabulary();
                    if (topic != null)
                    {
                        STSet topics = kb.getTopicSTSet().fragment(topic, topicsFP);
                        vocabulary.getTopicSTSet().merge(topics);
                    }
                    final PeerSemanticTag peer = localContextCoordinates.getPeer();
                    if (peer != null)
                    {
                        STSet peers = kb.getPeerSTSet().fragment(peer, peersFP);
                        vocabulary.getPeerSTSet().merge(peers);
                    }
                    knowledge.addContextPoint(localContextPoint);
                    kepConnection.insert(knowledge, senderAddresses);
                    timestampList.resetTimestamp(sender);
                    this.notifyInsertSent(this, knowledge);
                }
            }
        } else
        {
            throw new IllegalStateException("No ContextPoint found. Property AbstractSyncKP#SERIALIZED_CONTEXT_POINTS on identifier is null.");
        }
    }

    private List<SyncContextPoint> getOffer(final Date lastPeerMeeting) throws SharkKBException
    {
        final List<SyncContextPoint> syncContextPoints = new ArrayList<>();
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
                if (contextPointTimestamp.after(lastPeerMeeting))
                {
                    final SyncContextPoint syncContextPoint = new SyncContextPoint(element);
                    syncContextPoints.add(syncContextPoint);
                } else
                {
                    printTimestampNotAfterDebugMessage(contextPointTimestamp, lastPeerMeeting);
                }
            }
        }
        final String offerString = L.knowledge2String(offer);
        LoggingUtil.debugBox("Filtered Knowledge.", offerString, this);
        return syncContextPoints;
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

    private String getProtocoleState(final SemanticTag identifier) throws SharkKBException
    {
        String protocolState = identifier.getProperty(PROTOCOL_STATE);
        if (protocolState == null)
        {
            protocolState = PROTOCOL_STATE_NONE;
        }
        return protocolState;
    }
}
