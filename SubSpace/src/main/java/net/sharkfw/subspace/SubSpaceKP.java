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
 * @author Nitros
 */
public class SubSpaceKP extends VersionKP
{

    public SubSpaceKP(final SharkEngine sharkEngine, final SharkCS context, final SyncKB syncKB)
    {
        super(sharkEngine, syncKB, context);
    }

    @Override
    protected Knowledge getOffer() throws SharkKBException
    {
        final Knowledge knowledge = SharkCSAlgebra.extract(kb, interest);
        final String knowledgeString = L.knowledge2String(knowledge);
        LoggingUtil.debugBox("Collected Knowledge.", knowledgeString, this);
        return knowledge;
    }

}
