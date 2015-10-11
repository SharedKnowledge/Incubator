package net.sharkfw.descriptor.peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.AbstractSyncKP;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 * This class allows partial synchronisation of a Knowledge Base based on a
 * {@link ContextSpaceDescriptor}. It also allows to synchronize the underlying
 * {@link ContextSpaceDescriptor} with other peers and notify all subclasses if
 * other peer changed its recipients. <br /><br/>
 *
 * It uses a artifical interest with the following structure:<br/><br/>
 * <table border ="1">
 * <tr>
 * <td>Topics</td>
 * <td>One {@link SemanticTag} which holds metadata as properties.</td>
 * <tr>
 * <td>Peers</td>
 * <td>Owner of the underlying schema.</td>
 * </tr>
 * <tr>
 * <td>Remote Peers</td>
 * <td>Recipients to sync with.</td>
 * </tr>
 * <tr>
 * <td>Direction</td>
 * <td>{@link SharkCS#DIRECTION_INOUT} per default.</td>
 * </tr>
 * </table>
 * <br/>
 * All other dimension are null.
 *
 * @author Nitros Razril (pseudonym)
 */
public abstract class AbstractDescriptorSyncKP extends AbstractSyncKP
{

    /**
     * Metadata key for the {@link ContextSpaceDescriptor} property.
     */
    private static final String DESCRIPTOR_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#DESCRIPTOR_PROPERTY";
    /**
     * Metadata key for a whole tree of {@link ContextSpaceDescriptor} when
     * sending it.
     */
    private static final String REMOTE_DESCRIPTOR_TREE_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#REMOTE_DESCRIPTOR_TREE_KEY";
    /**
     * SI of topic sued as a metadata tag.
     */
    private static final String METADATA_SI = "net.sharkfw.descriptor.peer.DescriptorSyncKP#METADATA_NAME";
    /**
     * Indicates, that the underlying {@link ContextSpaceDescriptor} should be
     * synced.
     */
    private static final String ACTION_DESCRIPTOR_SYNC = "net.sharkfw.descriptor.peer.DescriptorSyncKP#ACTION_DESCRIPTOR_SYNC";
    /**
     * Indicates, that peers of remote peer have changed.
     */
    private static final String ACTION_RECIPIENTS_CHANGED = "net.sharkfw.descriptor.peer.DescriptorSyncKP#ACTION_RECIPIENTS_CHANGED";

    /**
     * Listers that are called, when the underlying
     * {@link ContextSpaceDescriptor} changes.
     */
    private final Collection<DesriptorListener> desriptorListeners;

    /**
     * Underlying schema containing the {@link ContextSpaceDescriptor}.
     */
    private final SyncDescriptorSchema schema;

    /**
     * Initializes this class by building the artifical interest and passing
     * necessary to the superclass. Throws an exception, if the given descriptor
     * can not be found in the given shema.
     *
     * @param sharkEngine Engine to send data.
     * @param schema Schema that contains the descriptor.
     * @param descriptor Descriptor describing the space of data to sync.
     * @param recipients Recipients to sync with.
     *
     * @throws IllegalArgumentException If the given descriptor can not be found
     * in the given shema.
     */
    public AbstractDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor, final PeerSTSet recipients)
    {
        super(
                sharkEngine,
                schema.getSyncKB(),
                buildContext(descriptor, recipients, schema, SharkCS.DIRECTION_INOUT)
        );
        try
        {
            if (!schema.containsIdentical(descriptor))
            {
                throw new IllegalArgumentException("There is no identical descriptor in the schema.");
            }
            this.schema = schema;
            desriptorListeners = new ArrayList<>();
        } catch (DescriptorSchemaException ex)
        {
            throw new IllegalStateException("Error while testing descriptor.", ex);
        }
    }

    /**
     * Convenience constructor for {@link AbstractDescriptorSyncKP#AbstractDescriptorSyncKP(SharkEngine,
     * SyncDescriptorSchema, ContextSpaceDescriptor, PeerSTSet)}. The remote
     * peer dimension of the {@link ContextSpaceDescriptor} will be used for the
     * recipients.
     *
     * @param sharkEngine Engine to send data.
     * @param schema Schema that contains the descriptor.
     * @param descriptor Descriptor describing the space of data to sync.
     */
    public AbstractDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor)
    {
        this(sharkEngine, schema, descriptor, null);
    }

    /**
     * Gets the underlying schema.
     *
     * @return The underlying schema.
     */
    public SyncDescriptorSchema getSchema()
    {
        return schema;
    }

    /**
     * Gets the underlying descriptor. This is read from the metadata tag.
     * Changes to it will not be reflected in this class, as both hold two
     * different references.
     *
     * @return The underlying descriptor.
     */
    public ContextSpaceDescriptor getDescriptor()
    {
        final SharkCS localContext = getInterest();
        return deserialize(localContext);
    }

    /**
     * Sets a new descriptor.
     *
     * @param descriptor The new descriptor.
     *
     * @throws IllegalArgumentException If the given descriptor can not be found
     * in the given shema.
     */
    public void setDescriptor(final ContextSpaceDescriptor descriptor)
    {
        final SharkCS oldContext = getInterest();
        final PeerSTSet recipients = oldContext.getRemotePeers();
        final int direction = oldContext.getDirection();
        final SharkCS newContext = buildContext(descriptor, recipients, schema, direction);
        interest = newContext;
        notifyDesriptorListener();
    }

    /**
     * Sets the Direction dimension to {@link SharkCS.DIRECTION_INOUT} meaning,
     * that this port will communicate.
     */
    public void subscribe()
    {
        setDirection(SharkCS.DIRECTION_INOUT);
    }

    /**
     * Sets the Direction dimension to {@link SharkCS.DIRECTION_NOTHING}
     * meaning, that this port will not communicate.
     */
    public void unsubscribe()
    {
        setDirection(SharkCS.DIRECTION_NOTHING);
    }

    /**
     * Gets the remote peer dimension of the artifical interest.<br/><br/>
     *
     * NOTE: Changes to the set will not be communicated to other peers.
     *
     * @return
     */
    public PeerSTSet getRecipients()
    {
        return getInterest().getRemotePeers();
    }

    /**
     * Sends the underlying descriptor and its tree to all recipients. Those
     * will insert the tree in their schema and override all matching
     * descriptors meaning that they will contain the same tree afterwards.
     *
     * @throws SharkSecurityException see {@link SharkEngine#publishKP(KnowledgePort)
     * @throws IOException see {@link SharkEngine#publishKP(KnowledgePort)
     */
    public void sendDescriptor() throws SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }
        try
        {
            JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
            final ContextSpaceDescriptor descriptor = getDescriptor();
            final Set<ContextSpaceDescriptor> tree = schema.getTree(descriptor);
            final SharkCS context = getInterest();
            final SemanticTag metadataTag = getIdentifier(context);
            final String treeXml = serializer.serializeList(tree);
            metadataTag.setProperty(REMOTE_DESCRIPTOR_TREE_KEY, treeXml);
            setAction(ACTION_DESCRIPTOR_SYNC);
            se.publishKP(this);
            clearAction();
            metadataTag.setProperty(REMOTE_DESCRIPTOR_TREE_KEY, null);
        } catch (SharkKBException | DescriptorSchemaException | JAXBException ex)
        {
            throw new IllegalStateException("Exception thrown. Sending Descriptor failed.", ex);
        }
    }

    /**
     * Adds a listener to be notified if the descriptor changes.
     *
     * @param desriptorListener Listener to add.
     * @return If the process was successful.
     */
    public boolean addDesriptorListener(final DesriptorListener desriptorListener)
    {
        return desriptorListeners.add(desriptorListener);
    }

    /**
     * Removes a listener to no longer be notified if the descriptor changes.
     *
     * @param desriptorListener Listener to remove.
     * @return If the process was successful.
     */
    public boolean removeDesriptorListener(final DesriptorListener desriptorListener)
    {
        return desriptorListeners.remove(desriptorListener);
    }

    /**
     * Send the current remote peer dimension too all recipients calling their
     * {@link #recipientsChanged(PeerSemanticTag, PeerSTSet)} in the process.
     *
     * @param recipients Recipients to notify.
     * @throws SharkKBException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws IOException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     */
    public void sendRecipientsChange(final PeerSTSet recipients) throws SharkKBException, SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }

        setAction(ACTION_RECIPIENTS_CHANGED);
        final Enumeration<PeerSemanticTag> recipientsEnumeration = recipients.peerTags();
        while (recipientsEnumeration.hasMoreElements())
        {
            final PeerSemanticTag recipient = recipientsEnumeration.nextElement();
            se.publishKP(this, recipient);
        }
        clearAction();
    }

    /**
     * Adds a peer and calls {@link #sendRecipientsChange(PeerSTSet)} with the
     * new remote peer set.
     *
     * @param recipient Recipient to add.
     * @throws SharkKBException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws IOException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     */
    public void addRecipient(final PeerSemanticTag recipient) throws SharkKBException, SharkSecurityException, IOException
    {
        final PeerSTSet recipients = getRecipients();
        recipients.merge(recipient);
        sendRecipientsChange(recipients);
    }

    /**
     * Removes a peer and calls {@link #sendRecipientsChange(PeerSTSet)} with
     * the new remote peer set plus the removed one.
     *
     * @param recipient Recipient to remove.
     * @throws SharkKBException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws SharkSecurityException see
     * {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag)}
     * @throws IOException see null null null null null null null null null null null     {@link SharkEngine#publishKP(KnowledgePort, PeerSemanticTag
     */
    public void removeRecipient(final PeerSemanticTag recipient) throws SharkKBException, SharkSecurityException, IOException
    {
        final PeerSTSet recipients = getRecipients();
        final PeerSTSet peersToNotify = new InMemoPeerSTSet();
        peersToNotify.merge(recipients);
        recipients.removeSemanticTag(recipient);
        sendRecipientsChange(peersToNotify);
    }

    /**
     * Abstract method called when the remote per communicates it changes in the
     * remote peer dimension.
     *
     * @param sender The sender of the notification.
     * @param newPeers The senders new remote peer dimension.
     */
    protected abstract void recipientsChanged(final PeerSemanticTag sender, final PeerSTSet newPeers);

    /**
     * Notifies all listeners if the underlying descriptor changes.
     */
    protected void notifyDesriptorListener()
    {
        for (DesriptorListener desriptorListener : desriptorListeners)
        {
            desriptorListener.onDesriptorChange();
        }
    }

    /**
     * Additional action are processed here. If {@link AbstractSyncKP} can not
     * find a matching action this method is called. It handles the descriptor
     * sync and recipients changed action.
     *
     * @param action Action the superclass {@link AbstractSyncKP} received.
     * @param context Interest the superclass {@link AbstractSyncKP} received.
     * @param kepConnection Connection Object the superclass
     * {@link AbstractSyncKP} received.
     *
     * @see AbstractSyncKP#doAction(String, SharkCS, KEPConnection)
     * @see AbstractSyncKP#doExpose(SharkCS, KEPConnection)
     */
    @Override
    protected void doAction(final String action, final SharkCS context, final KEPConnection kepConnection)
    {
        try
        {
            switch (action)
            {
                case ACTION_DESCRIPTOR_SYNC:
                    syncDescribtor(context);
                    break;
                case ACTION_RECIPIENTS_CHANGED:
                    final PeerSemanticTag sender = kepConnection.getSender();
                    final PeerSTSet newPeers = context.getRemotePeers();
                    recipientsChanged(sender, newPeers);
                    break;
                default:
                    super.doAction(action, context, kepConnection);
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Could not handle action. Exception occurred.", ex);
        }
    }

    /**
     * Returns if this class is interested in received Interest. This is the
     * case, if it contains the artifical interest and the underlying
     * {@link ContextSpaceDescriptor} of it and this class are the same as in
     * {@link ContextSpaceDescriptor#equals(Object)}.
     *
     * @param context Interest the superclass {@link AbstractSyncKP} received.
     * @param kepConnection Connection Object the superclass
     * {@link AbstractSyncKP} received.
     * @return If above described conditions are met true, false otherwise.
     *
     * @see AbstractSyncKP#isInterested(SharkCS, KEPConnection)
     */
    @Override
    protected boolean isInterested(final SharkCS context, final KEPConnection kepConnection)
    {
        boolean interesed = false;
        try
        {
            final PeerSemanticTag orginator = schema.getSyncKB().getOwner();
            final PeerSemanticTag sender = kepConnection.getSender();
            interesed = !sender.identical(orginator);
            interesed |= isValidContext(context);
            if (interesed)
            {
                final ContextSpaceDescriptor localDescriptor = getDescriptor();
                final ContextSpaceDescriptor remoteDescriptor = deserialize(context);
                interesed |= localDescriptor.equals(remoteDescriptor);
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalArgumentException("Could not read sender.", ex);
        }
        return interesed;
    }

    /**
     * Gets the {@link SemanticTag} in the topic dimension with the si
     * {@link #METADATA_SI}.
     *
     * @param context The interest to get the tag from.
     * @return The {@link SemanticTag} in the topic dimension with the si
     * {@link #METADATA_SI}.
     */
    @Override
    protected SemanticTag getIdentifier(final SharkCS context)
    {
        SemanticTag identifier = null;
        try
        {
            identifier = context.getTopics().getSemanticTag(METADATA_SI);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Getting identifier failed.", ex);
        }
        return identifier;
    }

    /**
     * Serializes a {@link ContextSpaceDescriptor} to XML.
     *
     * @param descriptor ContextSpaceDescriptor to serialize.
     * @return The {@link ContextSpaceDescriptor} as XML.
     */
    protected static String serialize(final ContextSpaceDescriptor descriptor)
    {
        final String xml;
        try
        {
            final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
            xml = serializer.serialize(descriptor);
        } catch (JAXBException ex)
        {
            throw new IllegalStateException("Error while serialisation of ContextSpaceDescriptor.", ex);
        }
        return xml;
    }

    /**
     * Deserializes a {@link ContextSpaceDescriptor} from the metadata Tag. The
     * metadata tag is extracted from the given context which should be similar
     * to this class artificial interest.
     *
     * @param context Context to extract the ContextSpaceDescriptor from.
     * @return The extracted ContextSpaceDescriptor.
     *
     * @throws IllegalArgumentException If the ContextSpaceDescriptor could not
     * be found in the context.
     */
    protected static ContextSpaceDescriptor deserialize(final SharkCS context)
    {
        final ContextSpaceDescriptor descriptor;
        try
        {
            if (!isValidContext(context))
            {
                throw new IllegalArgumentException("Context has no topic dimension, "
                        + "no topic with an SI of " + METADATA_SI + " or "
                        + "the topic has no Prperty of key" + DESCRIPTOR_KEY + ".");
            }
            final String xml = context.getTopics().getSemanticTag(METADATA_SI).getProperty(DESCRIPTOR_KEY);
            if (xml.isEmpty())
            {
                throw new IllegalArgumentException("The Property withe the key" + DESCRIPTOR_KEY + " is the empty String.");
            }
            final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
            descriptor
                    = serializer.deserialize(xml, ContextSpaceDescriptor.class
                    );
        } catch (SharkKBException | JAXBException ex)
        {
            throw new IllegalStateException("Error while deserialisation of ContextSpaceDescriptor.", ex);
        }
        return descriptor;
    }

    /**
     * Test if a given context is valid. this is the case, the property
     * {@link #DESCRIPTOR_KEY} at the tag with the si {@link #METADATA_SI} in
     * the topic dimension is not null. This also means, the topic dimension
     * must not be null.
     *
     * @param context Context to validate.
     * @return If above described conditions are met true, false otherwise.
     */
    protected static boolean isValidContext(final SharkCS context)
    {
        boolean valid = (context.getTopics() != null);
        try
        {
            valid |= (context.getTopics().getSemanticTag(METADATA_SI) != null);
            valid |= (context.getTopics().getSemanticTag(METADATA_SI).getProperty(DESCRIPTOR_KEY) != null);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Error while validation of SharkCS.", ex);
        }
        return valid;
    }

    /**
     * Gets a list of {@link ContextSpaceDescriptor} form a send interest an
     * insert it in the schema via
     * {@link DescriptorSchema#overrideDescriptors(Collection)}.
     *
     * @param context The interest with the list of
     * {@link ContextSpaceDescriptor}.
     */
    private void syncDescribtor(final SharkCS context)
    {
        try
        {
            final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
            final SemanticTag metadataTag = getIdentifier(context);
            final ContextSpaceDescriptor remoteDescriptor = deserialize(context);
            final String treeProperty = metadataTag.getProperty(REMOTE_DESCRIPTOR_TREE_KEY);
            if (treeProperty == null)
            {
                throw new IllegalArgumentException("Metadata has no tree data.");
            }
            final List<ContextSpaceDescriptor> tree = serializer.deserializeList(treeProperty);
            if (!DescriptorSchema.containsIdentical(remoteDescriptor, tree))
            {
                throw new IllegalArgumentException("Tree does not contain descriptor.");
            }
            schema.overrideDescriptors(tree);
            setDescriptor(remoteDescriptor);
        } catch (SharkKBException | JAXBException | DescriptorSchemaException ex)
        {
            throw new IllegalArgumentException("Could not get Metadata from context.", ex);
        }
    }

    /**
     * Builds the artificial interest the following way:<br/><br/>
     * <table border ="1">
     * <tr>
     * <td>Topics</td>
     * <td>One {@link SemanticTag} which holds metadata as properties.</td>
     * <tr>
     * <td>Peers</td>
     * <td>Owner of the underlying schema.</td>
     * </tr>
     * <tr>
     * <td>Remote Peers</td>
     * <td>Recipients to sync with.</td>
     * </tr>
     * <tr>
     * <td>Direction</td>
     * <td>{@link SharkCS#DIRECTION_INOUT} per default.</td>
     * </tr>
     * </table>
     * <br/>
     * All other dimension are null.:<br/><br/>
     *
     * The descriptor is saved as a metadata property with the key
     * {@link #DESCRIPTOR_KEY}. If recipients is null, the descriptors remote
     * peer dimension is used instead.
     *
     * @param descriptor The descriptor to save in the metadata property.
     * @param recipients Recipients to sync with. If null the descriptors remote
     * peer dimension is used instead.
     * @param schema Schema the descriptor is in.
     * @param direction Initial Direction of the artifical interest.
     * @return
     *
     * @throws Of the descriptor is empty or not in eh given schema.
     */
    private static SharkCS buildContext(final ContextSpaceDescriptor descriptor, final PeerSTSet recipients, final SyncDescriptorSchema schema, final int direction)
    {
        if (descriptor.isEmpty())
        {
            throw new IllegalArgumentException("ContextSpaceDescriptor must not be empty.");
        }
        try
        {
            if (!schema.containsIdentical(descriptor))
            {
                throw new IllegalArgumentException("ContextSpaceDescriptor must exist identical in given be schema.");
            }
            final String descriptorProperty = serialize(descriptor);
            final STSet topicDimension = new InMemoSTSet();
            final SemanticTag descriptorTopic = topicDimension.createSemanticTag(METADATA_SI, METADATA_SI);
            final PeerSTSet peersDimension = new InMemoPeerSTSet();
            final PeerSemanticTag owner = schema.getSyncKB().getOwner();
            peersDimension.merge(owner);
            final PeerSTSet remotePeersDimension;
            if (recipients == null)
            {
                remotePeersDimension = descriptor.getContext().getRemotePeers();
            } else
            {
                remotePeersDimension = recipients;
            }
            final SharkCS context = new InMemoSharkKB().createInterest(
                    topicDimension,
                    null,
                    peersDimension,
                    remotePeersDimension,
                    null,
                    null,
                    direction
            );
            descriptorTopic.setProperty(DESCRIPTOR_KEY, descriptorProperty);
            return context;
        } catch (SharkKBException | DescriptorSchemaException ex)
        {
            throw new IllegalArgumentException("Given ContextSpaceDescriptor could not be prepaird for this Knowlegde Port.", ex);
        }
    }

    /**
     * Rebuilds the interest by and change it Direction to the given parameter.
     * Only {@link SharkCS.DIRECTION_INOUT} and
     * {@link SharkCS.DIRECTION_NOTHING} are allowed.
     *
     * @param direction {@link SharkCS.DIRECTION_INOUT} or
     * {@link SharkCS.DIRECTION_NOTHING}
     * @throws IllegalArgumentException if direction is not
     * {@link SharkCS.DIRECTION_INOUT} or {@link SharkCS.DIRECTION_NOTHING}
     */
    private void setDirection(int direction)
    {
        if (direction != SharkCS.DIRECTION_INOUT && direction != SharkCS.DIRECTION_NOTHING)
        {
            throw new IllegalArgumentException("Not a valid direction. Can only set SharkCS.DIRECTION_INOUT or SharkCS.DIRECTION_NOTHING");
        }
        final SharkCS oldContext = getInterest();
        final SharkCS newContext = new InMemoSharkKB().createInterest(
                oldContext.getTopics(),
                oldContext.getOriginator(),
                oldContext.getPeers(),
                oldContext.getRemotePeers(),
                oldContext.getTimes(),
                oldContext.getLocations(),
                direction
        );
        this.interest = newContext;
    }
}
