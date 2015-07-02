/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.VersionKP;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.security.utility.LoggingUtil;
import net.sharkfw.system.L;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public class SubSpaceKP extends VersionKP
{
    private final SubSpace subSpace;

    public SubSpaceKP(final SharkEngine sharkEngine, final SubSpace subSpace)
    {
        super(sharkEngine, subSpace.getBase(), subSpace.getContext());
        this.subSpace = subSpace;
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        return subSpace.getKnowledge();
    }

}
