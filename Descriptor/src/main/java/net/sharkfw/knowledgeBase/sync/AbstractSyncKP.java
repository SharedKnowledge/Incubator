package net.sharkfw.knowledgeBase.sync;

import java.io.IOException;
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
import net.sharkfw.system.LoggingUtils;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;
import net.sharkfw.system.SharkSecurityException;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractSyncKP extends KnowledgePort
{
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

    public void send() throws SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }
        se.publishKP(this);
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
        LoggingUtils.debugBox(ownerName + " received Knowledge for insert", knowledgeString, this);
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
                        LoggingUtils.debugBox("Insert/Replace ContextPoint in KB of " + ownerName + ".", contextPointString, this);
                        VersionUtils.replaceContextPoint(remoteContextPoint, kb);
                    }
                }
            } else
            {
                L.d("Skipped insert due to KP not intereded in receiving Information.", this);
            }
        } catch (SharkKBException ex)
        {
            Logger.getLogger(AbstractSyncKP.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Exception occurred in doInsert.", ex);
        }
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        LoggingUtils.debugBox("Received Interest " + kb.getOwner().getName() + " :", L.contextSpace2String(context));
        try
        {
            final SemanticTag identifier = getIdentifier(context);
            final boolean interested = isInterested(context);
            if (isOKP() && identifier != null && interested)
            {
                sendKnowledge(kepConnection);
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
            Logger.getLogger(AbstractSyncKP.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Exception occurred in doExpose.", ex);
        } catch (Exception ex)
        {
            Logger.getLogger(AbstractSyncKP.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void sendKnowledge(final KEPConnection kepConnection) throws SharkException
    {
        final PeerSemanticTag sender = kepConnection.getSender();
        final String[] senderAddresses = sender.getAddresses();
        final Date lastPeerMeeting = timestampList.getTimestamp(sender);
        final List<SyncContextPoint> syncContextPoints = getOffer(lastPeerMeeting);
        //build Knowledge
        final Knowledge knowledge = new InMemoSharkKB().asKnowledge();
        for (SyncContextPoint syncContextPoint : syncContextPoints)
        {
            if (syncContextPoint != null)
            {
                final ContextCoordinates contextCoordinates = syncContextPoint.getContextCoordinates();
                final SemanticTag topic = contextCoordinates.getTopic();
                final SharkVocabulary vocabulary = knowledge.getVocabulary();
                if (topic != null)
                {
                    STSet topics = kb.getTopicSTSet().fragment(topic, topicsFP);
                    vocabulary.getTopicSTSet().merge(topics);
                }
                final PeerSemanticTag peer = contextCoordinates.getPeer();
                if (peer != null)
                {
                    STSet peers = kb.getPeerSTSet().fragment(peer, peersFP);
                    vocabulary.getPeerSTSet().merge(peers);
                }
                knowledge.addContextPoint(syncContextPoint);
            }
        }
        final String knowledgeAsString = L.knowledge2String(knowledge);
        LoggingUtils.logBox("Sending knowledge", knowledgeAsString);
        System.out.println("Hello World 0");
        kepConnection.insert(knowledge, senderAddresses);
        timestampList.resetTimestamp(sender);
        this.notifyInsertSent(this, knowledge);
        System.out.println("Hello World 1");
    }

    private List<SyncContextPoint> getOffer(final Date lastPeerMeeting) throws SharkKBException
    {
        final List<SyncContextPoint> syncContextPoints = new ArrayList<>();
        final Knowledge offer = getOffer();
        LoggingUtils.debugBox("Knowledge from Offer.", L.knowledge2String(offer), this);
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
        LoggingUtils.debugBox("Filtered Knowledge.", offerString, this);
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
}
