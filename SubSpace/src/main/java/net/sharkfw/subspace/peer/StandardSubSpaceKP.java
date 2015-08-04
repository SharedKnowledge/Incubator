/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.peer;

import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.AbstractSyncKP;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class StandardSubSpaceKP extends AbstractSyncKP
{

    private final SubSpace subSpace;

    public StandardSubSpaceKP(final SharkEngine sharkEngine, final SubSpace subSpace, final SyncKB syncKB)
    {
        super(sharkEngine, syncKB, subSpace.getContext());
        this.subSpace = subSpace;
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        return subSpace.getKnowledge(kb);
    }

    @Override
    protected boolean isInterested(final SharkCS context)
    {
        return getIdentifier(context) != null;
    }

    @Override
    protected SemanticTag getIdentifier(final SharkCS context)
    {
        SemanticTag description;
        try
        {
            final SemanticTag subSpaceDescription = subSpace.getDescription();
            final STSet topics = context.getTopics();
            final String[] sis = subSpaceDescription.getSI();
            description = topics.getSemanticTag(sis);
        } catch (SharkKBException ex)
        {
            throw new IllegalStateException("Error while getting SemanticTa of given SharkCS topics.", ex);
        }
        return description;
    }
}
