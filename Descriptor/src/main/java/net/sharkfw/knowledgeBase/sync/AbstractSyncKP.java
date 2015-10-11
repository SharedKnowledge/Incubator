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
 * This class is ab abstraction of {@link SyncKP}. It manages the actual
 * synchronisation. A peer A can either pull data from a peer B or create a pull
 * request so peer B pulls data from peer A. In case of a pull, peer A send an
 * interest to peer B. Peer B tests via
 * {@link #isInterested(SharkCS, KEPConnection)} is it will proceed. Than
 * extract data from it {@link SyncKB}, filters it to only send data changes
 * since last encountering peer A and send it to peer A. Peer A will then insert
 * all data of peer B, that does not exists in peer As {@link SyncKB} or has a
 * lower version in its Knowledge Base.
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractSyncKP extends KnowledgePort
{

    /**
     * Key to get the action metadata.
     */
    private static final String ACTION_KEY = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#ACTION_KEY";
    /**
     * Value for the pull action.
     */
    private static final String ACTION_PULL = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#ACTION_PULL";
    /**
     * Value fpr the pull request action.
     */
    private static final String ACTION_PULLREQUEST = "net.sharkfw.knowledgeBase.sync.AbstractSyncKP#ACTION_PULLREQUEST";
    /**
     * Timestamps when peers were last encountered.
     */
    private final TimestampList timestampList;
    /**
     * FragmentationParameter for extracting topics.
     */
    private final FragmentationParameter topicsFP;
    /**
     * FragmentationParameter for extracting peers.
     */
    private final FragmentationParameter peersFP;

    /**
     * Sets up this class, see {@link KnowledgePort#KnowledgePort(SharkEngine, SharkKB).
     * Form teh given interest the remote peer dimension is used to create
     * the {@link TimestampList}.
     *
     * @param sharkEngine Engine for communication.
     * @param syncKB SyncKB of the user of the peer using this port. Must have
     * an owner.
     * @param context Interest to use.
     * @param topicFP FragmentationParameter for extracting topics.
     * @param peerFP FragmentationParameter for extracting peers.
     *
     * @throws IllegalArgumentException If syncKBhas no owner.
     * @throws IllegalStateException Any error while initializing this class.
     */
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

    /**
     * Convenience constructor. Uses {@link FragmentationParameter#getZeroFP()}
     * for the FragmentationParameter parameters.
     *
     * @param sharkEngine Engine for communication.
     * @param syncKB SyncKB of the user of the peer using this port. Must have
     * an owner.
     * @param context Interest to use.
     *
     * @throws IllegalArgumentException If syncKBhas no owner.
     * @throws IllegalStateException Any error while initializing this class.
     */
    public AbstractSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final SharkCS context)
    {
        this(sharkEngine, syncKB, context, FragmentationParameter.getZeroFP(), FragmentationParameter.getZeroFP());
    }

    /**
     * Pull data from all remote peers. See class description for more.
     *
     * @throws SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort)}
     * @throws IOException {@link SharkEngine#publishKP(KnowledgePort)}
     * @throws IllegalStateException If this class engine {@link SharkEngine#isStarted() returns
     * null
     */
    public void pull() throws SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }
        se.publishKP(this);
    }

    /**
     * Requests all remote peers to pull data from this peer.It uses a Flag in
     * the Identifier, see {@link #getIdentifier(SharkCS), to do so.
     *
     * @throws SharkKBException Errors while setting the action.
     * @throws SharkSecurityException SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort)}
     * @throws IOException SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort)}
     * @throws IllegalStateException If this class engine {@link SharkEngine#isStarted() returns
     * null
     *
     * @see #doExpose(SharkCS, KEPConnection)
     */
    public void pullRequest() throws SharkKBException, SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }
        setAction(ACTION_PULLREQUEST);
        se.publishKP(this);
        clearAction();
    }

    /**
     * Subclasses of this class must provide how to extracted the Knowledge to
     * sync.
     *
     * @return Knowledge to sync.
     * @throws SharkKBException Errors while extracting the Knowledge
     */
    protected abstract Knowledge getOffer() throws SharkKBException;

    /**
     * Subclasses of this class must be able to find the identifier. This is a
     * SemanticTag in any of the dimension of context. The tag is used to add
     * metadata to it as a property.
     *
     * @param context Interest to extract the identifier from.
     * @return The SemanticTag that is the identifier.
     */
    protected abstract SemanticTag getIdentifier(final SharkCS context);

    /**
     * Subclasses of this class must tell if they are interested in processing
     * the received data. The two parameters are form the
     * KnowledgePort#doExpose(knowledgeBase.SharkCS, KEPConnection) method.
     *
     * @param context The send interest.
     * @param kepConnection A communication object containing data of the
     * communication
     * @return True, if this class should process the send data, false
     * otherwise.
     *
     * @see KnowledgePort#doExpose(knowledgeBase.SharkCS, KEPConnection)
     */
    protected abstract boolean isInterested(final SharkCS context, final KEPConnection kepConnection);

    /**
     * Gets an action metadata flag form the given context. The tag containing
     * the metadata is found via {@link #getIdentifier(SharkCS)}. If no action
     * was set, it always returns {@link #ACTION_PULL}
     *
     * @param context Context to get the data from.
     * @return The set action, or {@link #ACTION_PULL} if none was set.
     * @throws SharkKBException Any error while reading the action from the tag.
     */
    protected String getAction(final SharkCS context) throws SharkKBException
    {
        final SemanticTag identifier = getIdentifier(context);
        final String actionProperty = identifier.getProperty(ACTION_KEY);
        return (actionProperty == null) ? ACTION_PULL : actionProperty;
    }

    /**
     * Sets a action to the metadata tag. The tag containing the metadata is
     * found via {@link #getIdentifier(SharkCS)}.
     *
     * @param action Action to set.
     * @throws SharkKBException Any error while writing the action to the tag
     */
    protected void setAction(final String action) throws SharkKBException
    {
        final SemanticTag identifier = getIdentifier(interest);
        identifier.setProperty(ACTION_KEY, action);
    }

    /**
     * Clears action from the metadata tag. This is the same as calling
     * <code>setAction(null)</code>, which this method does.
     *
     * @throws SharkKBException Any error while writing the action to the tag
     */
    protected void clearAction() throws SharkKBException
    {
        setAction(null);
    }

    /**
     * Method to be overridden by subclass if they define own actions. Throws an
     * {@link  IllegalArgumentException} per default.
     *
     * @param action Action currently set.
     * @param context Interest received.
     * @param kepConnection A communication object containing data of the
     * communication
     *
     * @see KnowledgePort#doExpose(knowledgeBase.SharkCS, KEPConnection)
     */
    protected void doAction(final String action, final SharkCS context, final KEPConnection kepConnection)
    {
        throw new IllegalArgumentException("No valid action found in SharkCS.");
    }

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
     * @param knowledge Knowledge to insert.
     * @param kepConnection A communication object containing data of the
     * communication
     *
     * @throws IllegalStateException If a SharkKBException occurs while
     * executing this method. That means, the KP can't work properly.
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
                        notifyKnowledgeAssimilated(this, remoteContextPoint);
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

    /**
     * THis method via {@link #isInterested(nSharkCS, KEPConnection)} if it
     * should process the send data. Than it gets the metadata tag via
     * {@link #getIdentifier(SharkCS)} and test gets the action via
     * {@link #getAction(SharkCS)}.<br><br/>
     *
     * Depending on the action the following methods are called.<br/><br/>
     *
     * <table border ="1">
     * <tr>
     * <td>{@link #ACTION_PULL}</td>
     * <td>{@link #sendKnowledge(KEPConnection)}.</td>
     * <tr>
     * <td>{@link #ACTION_PULLREQUEST}</td>
     * <td>{@link #pull()}.</td>
     * </tr>
     * <tr>
     * <td>No matching action found.</td>
     * <td>{@link #doAction(String, SharkCS, KEPConnection)}</td>
     * </table>
     *
     * @param context Interest received.
     * @param kepConnection A communication object containing data of the
     * communication
     *
     * @throws IllegalStateException If any error occurs in the process of this
     * method.
     *
     */
    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        LoggingUtils.debugBox("Received Interest " + kb.getOwner().getName() + " :", L.contextSpace2String(context));
        try
        {
            final boolean interested = isInterested(context, kepConnection);
            if (isOKP() && interested)
            {
                final String action = getAction(context);
                switch (action)
                {
                    case ACTION_PULL:
                        sendKnowledge(kepConnection);
                        break;
                    case ACTION_PULLREQUEST:
                        pull();
                        break;
                    default:
                        doAction(action, context, kepConnection);
                }
            } else
            {
                final StringBuilder builder = new StringBuilder();
                if (!isOKP())
                {
                    builder.append("This KnowledgePort does not send data. It is not a OKP.");
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
        } catch (Exception ex)
        {
            System.out.println("error");
            Logger.getLogger(AbstractSyncKP.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Exception occurred in doExpose.", ex);
        }
    }

    /**
     * Gets the offer via {@link #getOffer()}. This is then filter so, that
     * only the data is send, that was changed this the last encounter with the
     * peer who requested the data. Finally, sends the data to 
     * {@link KEPConnection#getSender()}. Also, all containing peers and topic
     * in the extracted {@link ContextPoint} are added to the vocabulary of the
     * knowledge. Those are extracted via the {@link FragmentationParameter}
     * of this class.
     * 
     * @param kepConnection A communication object containing data of the
     * communication
     *
     * @throws SharkException Any error while extracting and filtering the data.
     */
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
        kepConnection.insert(knowledge, senderAddresses);
        timestampList.resetTimestamp(sender);
        this.notifyInsertSent(this, knowledge);
    }

    /**
     * Gets the actual offer via {@link #getOffer()} and filters it so, that
     * only the data is remains, that was changed this the last encounter with the
     * peer who requested the data.
     * 
     * @param lastPeerMeeting Last encounter with a peer.
     * @return A List of contextpoints that where changed since lastPeerMeeting.
     * This is filtered form {@link #getOffer()}.
     * 
     * @throws SharkKBException Any error while extracting and filtering the data.
     */
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

    /**
     * Output a log message message if a {@link ContextPoint} was skipped because
     * is has not changed since the last encounter.
     * 
     * @param contextPointTimestamp Timestamp of {@link ContextPoint}.
     * @param lastPeerMeeting Last encounter with a peer. 
     */
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
