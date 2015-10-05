package net.sharkfw.descriptor.peer;

import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.knowledgeBase.sync.AbstractSyncKP;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros
 */
public abstract class AbstractDescriptorSyncKP extends AbstractSyncKP
{

    private boolean lern;

    protected static final String DESCRIPTOR_KEY = "net.sharkfw.descriptor.peer.DescriptorSyncKP#DESCRIPTOR_PROPERTY";

    public AbstractDescriptorSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final ContextSpaceDescriptor descriptor, boolean lern)
    {
        super(sharkEngine, syncKB, buildContext(descriptor));
        this.lern = lern;
    }

    public AbstractDescriptorSyncKP(final SharkEngine sharkEngine, final SyncKB syncKB, final ContextSpaceDescriptor descriptor)
    {
        this(sharkEngine, syncKB, descriptor, false);
    }

    @Override
    protected boolean isInterested(final SharkCS context)
    {
        boolean interesed = isValidContext(context);
        interesed |= isIKP();
        if (interesed)
        {
            final SharkCS localContext = getInterest();
            final ContextSpaceDescriptor localDescriptor = deserialize(localContext);
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
            identifier = context.getTopics().getSemanticTag(DESCRIPTOR_KEY);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Getting identifier failed.", ex);
        }
        return identifier;
    }

    protected boolean isLern()
    {
        return lern;
    }

    protected void setLern(boolean lern)
    {
        this.lern = lern;
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
                        + "no topic with an SI of " + DESCRIPTOR_KEY + " or "
                        + "the topic has no Prperty of key" + DESCRIPTOR_KEY + ".");
            }
            final String xml = context.getTopics().getSemanticTag(DESCRIPTOR_KEY).getProperty(DESCRIPTOR_KEY);
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
            valid |= (context.getTopics().getSemanticTag(DESCRIPTOR_KEY) != null);
            valid |= (context.getTopics().getSemanticTag(DESCRIPTOR_KEY).getProperty(DESCRIPTOR_KEY) != null);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Error while Validation of SharkCS.", ex);
        }
        return valid;
    }

    private static SharkCS buildContext(final ContextSpaceDescriptor descriptor)
    {
        try
        {
            final SemanticTag descriptorTopic = new InMemoSTSet().createSemanticTag(DESCRIPTOR_KEY, DESCRIPTOR_KEY);
            final String descriptorProperty = serialize(descriptor);
            descriptorTopic.setProperty(DESCRIPTOR_KEY, descriptorProperty);
            final ContextCoordinates context = new InMemoSharkKB().createContextCoordinates(
                    descriptorTopic,
                    null,
                    null,
                    null,
                    null,
                    null,
                    SharkCS.DIRECTION_NOTHING
            );
            return context;
        } catch (SharkKBException ex)
        {
            throw new IllegalArgumentException("Given ContextSpaceDescriptor could not be prepaird for this Knowlegde Port.", ex);
        }
    }
}
