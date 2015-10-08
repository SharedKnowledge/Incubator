package net.sharkfw.descriptor.peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.STSetListener;
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
 *
 * @author Nitros
 */
public abstract class AbstractDescriptorSyncKP extends AbstractSyncKP
{

    private static final String DESCRIPTOR_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#DESCRIPTOR_PROPERTY";
    private static final String REMOTE_DESCRIPTOR_TREE_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#REMOTE_DESCRIPTOR_TREE_KEY";
    private static final String METADATA_SI = "net.sharkfw.descriptor.peer.DescriptorSyncKP#METADATA_NAME";
    private static final String ACTION_DESCRIPTOR_SYNC = "net.sharkfw.descriptor.peer.DescriptorSyncKP#ACTION_DESCRIPTOR_SYNC";
    private static final String ACTION_RECIPIENTS_CHANGED = "net.sharkfw.descriptor.peer.DescriptorSyncKP#ACTION_RECIPIENTS_CHANGED";

    private final Collection<DesriptorListener> desriptorListeners;

    private final SyncDescriptorSchema schema;

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

    public AbstractDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor)
    {
        this(sharkEngine, schema, descriptor, null);
    }

    public SyncDescriptorSchema getSchema()
    {
        return schema;
    }

    public ContextSpaceDescriptor getDescriptor()
    {
        final SharkCS localContext = getInterest();
        return deserialize(localContext);
    }

    public void setDescriptor(final ContextSpaceDescriptor descriptor)
    {
        final SharkCS oldContext = getInterest();
        final PeerSTSet recipients = oldContext.getRemotePeers();
        final int direction = oldContext.getDirection();
        final SharkCS newContext = buildContext(descriptor, recipients, schema, direction);
        interest = newContext;
        notifyDesriptorListener();
    }

    public void subscribe()
    {
        setDirection(SharkCS.DIRECTION_INOUT);
    }

    public void unsubscribe()
    {
        setDirection(SharkCS.DIRECTION_NOTHING);
    }

    public PeerSTSet getRecipients()
    {
        return getInterest().getRemotePeers();
    }

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

    public boolean addDesriptorListener(final DesriptorListener desriptorListener)
    {
        return desriptorListeners.add(desriptorListener);
    }

    public boolean removeDesriptorListener(final DesriptorListener desriptorListener)
    {
        return desriptorListeners.remove(desriptorListener);
    }

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

    public void addRecipient(final PeerSemanticTag recipient) throws SharkKBException, SharkSecurityException, IOException
    {
        final PeerSTSet recipients = getRecipients();
        recipients.merge(recipient);
        sendRecipientsChange(recipients);
    }
    
    public void removeRecipient(final PeerSemanticTag recipient) throws SharkKBException, SharkSecurityException, IOException
    {
        final PeerSTSet recipients = getRecipients();
        final PeerSTSet peersToNotify = new InMemoPeerSTSet();
        peersToNotify.merge(recipients);
        recipients.removeSemanticTag(recipient);
        sendRecipientsChange(peersToNotify);
    }


    protected abstract void recipientsChanged(final PeerSemanticTag sender, final PeerSTSet newPeers);

    protected void notifyDesriptorListener()
    {
        for (DesriptorListener desriptorListener : desriptorListeners)
        {
            desriptorListener.onDesriptorChange();
        }
    }

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

    private void setDirection(int direction)
    {
        if (direction != SharkCS.DIRECTION_INOUT || direction != SharkCS.DIRECTION_NOTHING)
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
