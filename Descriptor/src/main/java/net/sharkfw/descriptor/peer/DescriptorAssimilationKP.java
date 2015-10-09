package net.sharkfw.descriptor.peer;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Descriptor;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchema;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.kep.format.SerializerFactroy;
import net.sharkfw.knowledgeBase.Interest;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.SharkSecurityException;
import net.sharkfw.xml.jaxb.JAXBSerializer;

/**
 *
 * @author Nitros
 */
public class DescriptorAssimilationKP extends KnowledgePort
{

    private static final String METADATA_SI = "net.sharkfw.descriptor.peer#.escriptorAssimilationKP#METADATA_SI";
    private static final String DESCRIPTOR_LIST = "net.sharkfw.descriptor.peer.DescriptorAssimilationKP#DESCRIPTOR_LIST";

    private final SyncDescriptorSchema schema;

    public DescriptorAssimilationKP(final SharkEngine engine, final SyncDescriptorSchema schema, final PeerSTSet recipients) throws SharkKBException
    {
        super(engine);
        this.schema = schema;
        final PeerSemanticTag owner = schema.getSyncKB().getOwner();
        final STSet topics = new InMemoSTSet();
        topics.createSemanticTag(METADATA_SI, METADATA_SI);
        final PeerSTSet peers = new InMemoPeerSTSet();
        peers.merge(owner);
        final SharkCS context = new InMemoSharkKB().createInterest(
                topics,
                owner,
                peers,
                recipients,
                null,
                null,
                SharkCS.DIRECTION_INOUT
        );
        setInterest(context);
    }

    public void sendDescriptor(final ContextSpaceDescriptor descriptor) throws SharkSecurityException, IOException
    {
        if (!se.isStarted())
        {
            throw new IllegalStateException("Cannot send data without any open communication stubs on engine.");
        }
        try
        {
            if (!schema.containsIdentical(descriptor))
            {
                throw new IllegalArgumentException("Descriptor is not in Schema.");
            }
            final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
            final Set<ContextSpaceDescriptor> descriptors = schema.getTree(descriptor);
            final String xml = serializer.serializeList(descriptors);
            final SemanticTag metadataTag = getMetadataTag(interest);
            metadataTag.setProperty(DESCRIPTOR_LIST, xml);
            se.publishKP(this);
            metadataTag.setProperty(DESCRIPTOR_LIST, null);
        } catch (SharkKBException | JAXBException | DescriptorSchemaException ex)
        {
            throw new IllegalArgumentException("Faild to send descriptor.", ex);
        }
    }

    public SyncDescriptorSchema getSchema()
    {
        return schema;
    }

    @Override
    protected void doInsert(final Knowledge knowledge, final KEPConnection kepConnection)
    {
        throw new IllegalStateException("this method doInsert schould never be called.");
    }

    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        try
        {
            final SemanticTag metadataTag = getMetadataTag(context);
            if (metadataTag != null)
            {
                final String xml = metadataTag.getProperty(DESCRIPTOR_LIST);
                if (xml != null && !xml.isEmpty())
                {
                    final JAXBSerializer serializer = SerializerFactroy.INSTANCE.getDescriptorSerializer();
                    final List<ContextSpaceDescriptor> list = serializer.deserializeList(xml);
                    schema.overrideDescriptors(list);
                }
            }
        } catch (SharkKBException | JAXBException | DescriptorSchemaException ex)
        {
            throw new IllegalArgumentException("Faild to read metadata from interest or to insert them into schema.", ex);
        }
    }

    private SemanticTag getMetadataTag(final SharkCS context) throws SharkKBException
    {
        SemanticTag metadataTag = null;
        final STSet topics = context.getTopics();
        if (topics != null)
        {
            metadataTag = topics.getSemanticTag(METADATA_SI);
        }
        return metadataTag;
    }
}
