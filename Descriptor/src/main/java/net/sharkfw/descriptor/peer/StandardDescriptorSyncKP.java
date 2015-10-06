/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.peer;

import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.descriptor.knowledgeBase.DescriptorAlgebra;
import net.sharkfw.descriptor.knowledgeBase.SyncDescriptorSchema;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author Nitros
 */
public class StandardDescriptorSyncKP extends AbstractDescriptorSyncKP
{

    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor, final PeerSTSet recipients)
    {
        super(sharkEngine, schema, descriptor, recipients);
    }

    public StandardDescriptorSyncKP(final SharkEngine sharkEngine, final SyncDescriptorSchema schema, final ContextSpaceDescriptor descriptor)
    {
        super(sharkEngine, schema, descriptor);
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        final SharkCS localContext = getInterest();
        final SharkKB localKB = getKB();
        final ContextSpaceDescriptor localDescriptor = deserialize(localContext);
        return DescriptorAlgebra.extract(localKB, localDescriptor);
    }

}