package net.sharkfw.descriptor.peer;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorSchemaException;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.kep.format.SerializerFactroy;
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
 * This {@link KnowledgePort} acts as a way to push new
 * {@link ContextSpaceDescriptor} to an remote peer. It also handles the
 * assimilation process. As such is acts as an port to teach and learn
 * {@link ContextSpaceDescriptor}.
 *
 * @author @author Nitros Razril (pseudonym)
 */
public class DescriptorAssimilationKP extends KnowledgePort
{

    /**
     * SI of the{@link SemanticTag} containing the metadata.
     */
    private static final String METADATA_SI = "net.sharkfw.descriptor.peer#.escriptorAssimilationKP#METADATA_SI";
    /**
     * A tree of an {@link ContextSpaceDescriptor} as a list.
     */
    private static final String DESCRIPTOR_LIST = "net.sharkfw.descriptor.peer.DescriptorAssimilationKP#DESCRIPTOR_LIST";
    /**
     * The schema this {@link KnowledgePort} operates on.
     */
    private final SyncDescriptorSchema schema;

    /**
     * Construct this class. An artifical interest is build with the following
     * structure:<br/><br/>
     *
     * <table border ="1">
     * <tr>
     * <td>Topics</td>
     * <td>One {@link SemanticTag} which holds metadata as properties.</td>
     * <tr>
     * <td>Originator</td>
     * <td>Owner of the underlying schema.</td>
     * </tr>
     * <tr>
     * <td>Peers</td>
     * <td>Owner of the underlying schema.</td>
     * </tr>
     * <tr>
     * <td>Remote Peers</td>
     * <td>Recipients to communicate with.</td>
     * </tr>
     * <tr>
     * <td>Direction</td>
     * <td>{@link SharkCS#DIRECTION_INOUT}</td>
     * </tr>
     * </table>
     * <br/>
     *
     * @param engine Engine to handle handle communication.
     * @param schema Schema to operate on.
     * @param recipients Peers to communicate with.
     * @throws SharkKBException Errors while creating the artifical interest.
     */
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

    /**
     * Send a while tree to all remote peers. The tree is extracted base on the
     * passed descriptor. The remote peers will learn this tree.
     *
     * @param descriptor Descriptor to extract the tree for.
     * @throws SharkSecurityException see {@link SharkEngine#publishKP(KnowledgePort)
     * @throws IOException see {@link SharkEngine#publishKP(KnowledgePort)
     *
     * @see #doExpose(SharkCS, KEPConnection)
     */
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

    /**
     * This class underlying schema.
     *
     * @return Underlying schema of this class.
     */
    public SyncDescriptorSchema getSchema()
    {
        return schema;
    }

    /**
     * Test of this class is interested in the send interest. The method exists
     * to be overridden an returns true in it normal form.
     *
     * @param context Interest to test.
     * @return Always true.
     */
    protected boolean isInterested(final SharkCS context)
    {
        return true;
    }

    /**
     * Should never be called. Throws {@link UnsupportedOperationException}.
     */
    @Override
    protected void doInsert(final Knowledge knowledge, final KEPConnection kepConnection)
    {
        throw new UnsupportedOperationException("this method doInsert schould never be called.");
    }

    /**
     * Gets the metadata tag and test if it is not null and this class is
     * interested in the send interest. If so, it gets the list of
     * {@link ContextSpaceDescriptor} form the metadata tag an insert it into
     * the schema via {@link DescriptorSchema#overrideDescriptors(Collection).
     *
     * @param context see {@link KnowledgePort#doExpose(SharkCS, KEPConnection)}
     * @param kepConnection see {@link KnowledgePort#doExpose(SharkCS, KEPConnection)}
     */
    @Override
    protected void doExpose(final SharkCS context, final KEPConnection kepConnection)
    {
        try
        {
            final SemanticTag metadataTag = getMetadataTag(context);
            if (metadataTag != null && isInterested(context))
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

    /**
     * Gets the metadata tag or null, if it does nit exists.
     * 
     * @param context The interest to get the metadata tag from.
     * @return The metadata tag or null, if it does nit exists.
     * @throws SharkKBException Any error while reading form context.
     */
    protected SemanticTag getMetadataTag(final SharkCS context) throws SharkKBException
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
