/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.VersionKP;
import net.sharkfw.peer.SharkEngine;
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
    protected Knowledge getOffer()
    {
        try
        {
            L.d("#########" + kb.getOwner().getName() + " getOffer #########", this);
            final Knowledge knowledge = new InMemoKnowledge(kb);
            Enumeration<ContextPoint> cpEnumeration = kb.getAllContextPoints();
            while (cpEnumeration.hasMoreElements())
            {
                ContextPoint nextElement = cpEnumeration.nextElement();
                knowledge.addContextPoint(nextElement);
                
            }
            final String knowledgeString = L.knowledge2String(knowledge);
            L.d("+++++++++++++++++++++++++++++Offer Knowledge (CP: " + knowledge.getNumberOfContextPoints() + "):\n" + knowledgeString);
            return knowledge;
        } catch (SharkKBException ex)
        {
            Logger.getLogger(SubSpaceKP.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException(ex);
        }
    }
    
}
