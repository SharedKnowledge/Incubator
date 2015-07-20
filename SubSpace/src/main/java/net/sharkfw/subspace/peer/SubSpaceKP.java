/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.peer;

import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.VersionKP;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class SubSpaceKP extends VersionKP
{
    private final SubSpace subSpace;

    public SubSpaceKP(final SharkEngine sharkEngine, final SubSpace subSpace, final SyncKB syncKB)
    {
        super(sharkEngine, syncKB, subSpace.getContext());
        this.subSpace = subSpace;
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        return subSpace.getKnowledge(kb);
    }

}
