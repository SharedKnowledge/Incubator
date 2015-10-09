/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.peer;

import java.util.ArrayList;
import java.util.Collection;
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
 *
 * @author Nitros
 */
public class StandardDescriptorSyncKP extends AbstractDescriptorSyncKP
{

    final Collection<DesriptorListener> recipientsChangedListeners;

    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor, final PeerSTSet recipients)
    {
        super(sharkEngine, schema, descriptor, recipients);
        recipientsChangedListeners = new ArrayList<>();
    }

    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor)
    {
        this(sharkEngine, schema, descriptor, null);
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        final SharkCS localContext = getInterest();
        final SharkKB localKB = getKB();
        final ContextSpaceDescriptor localDescriptor = deserialize(localContext);
        return DescriptorAlgebra.extract(localKB, localDescriptor);
    }

    public boolean addRecipientsChangedListeners(final DesriptorListener recipientsChangedListener)
    {
        return recipientsChangedListeners.add(recipientsChangedListener);
    }

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
