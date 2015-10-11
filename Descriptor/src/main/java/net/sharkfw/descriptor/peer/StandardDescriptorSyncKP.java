package net.sharkfw.descriptor.peer;

import java.util.Enumeration;
import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorAlgebra;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.peer.SharkEngine;

/**
 * A simple implementation of {@link AbstractDescriptorSyncKP}. Only extracts
 * the {@link Knowledge} of the given {@link ContextSpaceDescriptor} and copies
 * changes in the remote peers dimension if notified.
 *
 * @author @author Nitros Razril (pseudonym)
 */
public class StandardDescriptorSyncKP extends AbstractDescriptorSyncKP
{

    /**
     * Simply passes all arguments to {@link
     * AbstractDescriptorSyncKP#AbstractDescriptorSyncKP(SharkEngine,
     * SyncDescriptorSchema, ContextSpaceDescriptor, PeerSTSet)}
     *
     * @param sharkEngine Engine to send data.
     * @param schema Schema that contains the descriptor.
     * @param descriptor Descriptor describing the space of data to sync.
     * @param recipients Recipients to sync with.
     *
     * @throws IllegalArgumentException If the given descriptor can not be found
     * in the given shema.
     *
     */
    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor, final PeerSTSet recipients)
    {
        super(sharkEngine, schema, descriptor, recipients);
    }

    /**
     * Convenience constructor. Class {@link
     * StandardDescriptorSyncKP#StandardDescriptorSyncKP(SharkEngine,
     * SyncDescriptorSchema, ContextSpaceDescriptor, PeerSTSet)} with the
     * PeerSTSet being null resulting in {@link AbstractDescriptorSyncKP} taking
     * the descriptors remote per dimension as recipients.
     *
     * @param sharkEngine Engine to send data.
     * @param schema Schema that contains the descriptor.
     * @param descriptor Descriptor describing the space of data to sync.
     */
    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor)
    {
        this(sharkEngine, schema, descriptor, null);
    }

    /**
     * Creates an offer by simply extracting the Knowledge of the context of the
     * underlying {@link ContextSpaceDescriptor}.
     *
     * @return Knowledge of the context of the underlying
     * {@link ContextSpaceDescriptor}.
     *
     * @throws SharkKBException Any error while extracting the Knowledge
     */
    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        final SharkCS localContext = getInterest();
        final SharkKB localKB = getKB();
        final ContextSpaceDescriptor localDescriptor = deserialize(localContext);
        return DescriptorAlgebra.extract(localKB, localDescriptor);
    }

    /**
     * Copies the newPeers Set and sets it as the new remote peer dimension.
     * The owner of the underlying schema is switch with the sender in the 
     * process. If the owner does not exist in the newPeers set in means the
     * sender does not want to communicate with this peer anymore, so he
     * is removed form the remote peer dimension. If the owner is in newPeers,
     * it means the sender should be in the remote peer dimension of this peer,
     * so owner and sender are switched.
     * 
     * @param sender Sender of the notification.
     * @param newPeers New peers in the senders remote peer dimension.
     */
    @Override
    protected void recipientsChanged(final PeerSemanticTag sender, final PeerSTSet newPeers)
    {
        try
        {
            final PeerSemanticTag owner = getSchema().getSyncKB().getOwner();
            final PeerSTSet recipients = getRecipients();
            // put everything new int remote peers
            recipients.merge(newPeers);
            final Enumeration<PeerSemanticTag> recipientsEnumeration = recipients.peerTags();
            // remove everything no longer exisiting
            while (recipientsEnumeration.hasMoreElements())
            {
                final PeerSemanticTag peer = recipientsEnumeration.nextElement();
                final PeerSemanticTag recipientInNewPeers = newPeers.getSemanticTag(peer.getSI());
                if (recipientInNewPeers == null)
                {
                    recipients.removeSemanticTag(peer);
                }
            }
            // Owner and sender needs to be switched
            // If owner ist not in newPeers, than remove sender
            final PeerSemanticTag ownerInNewPeers = newPeers.getSemanticTag(owner.getSI());
            if (ownerInNewPeers == null)
            {
                recipients.removeSemanticTag(sender);
            } else
            {
                recipients.removeSemanticTag(owner);
                recipients.merge(sender);
            }
        } catch (SharkKBException ex)
        {
            throw new IllegalArgumentException("Exception while handling given arguments.", ex);
        }
    }

}
