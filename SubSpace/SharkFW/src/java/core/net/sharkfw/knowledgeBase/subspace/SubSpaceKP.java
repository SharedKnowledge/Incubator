/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sharkfw.knowledgeBase.subspace;

import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.sync.SyncKB;
import net.sharkfw.knowledgeBase.sync.SyncKP;
import net.sharkfw.peer.SharkEngine;

/**
 *
 * @author NitrosRazril
 */
public class SubSpaceKP extends SyncKP
{

    public SubSpaceKP(SharkEngine engine, SyncKB kb, SharkCS interest, int retryTimeout) throws SharkKBException
    {
        super(engine, kb, retryTimeout);
    }
}
    
