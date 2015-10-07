package net.sharkfw.descriptor.peer;

import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
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
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros
 */
public abstract class AbstractDescriptorSyncKP extends AbstractSyncKP
{

    protected static final String DESCRIPTOR_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#DESCRIPTOR_PROPERTY";
    protected static final String METADATA_SI = "net.sharkfw.descriptor.peer.DescriptorSyncKP#METADATA_NAME";

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

    @Override
    protected void doExpose(SharkCS context, KEPConnection kepConnection)
    {
        
        super.doExpose(context, kepConnection);
    }
    
    

    @Override
    protected boolean isInterested(final SharkCS context)
    {
        boolean interesed = isValidContext(context);
        if (interesed)
        {

            final ContextSpaceDescriptor localDescriptor = getDescriptor();
            final ContextSpaceDescriptor remoteDescriptor = deserialize(context);
            interesed |= localDescriptor.equals(remoteDescriptor);
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
            descriptor = serializer.deserialize(xml, ContextSpaceDescriptor.class);
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
