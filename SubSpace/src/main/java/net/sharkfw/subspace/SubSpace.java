/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sharkfw.subspace;

import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.sync.SyncKB;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public interface SubSpace
{
    public SyncKB getBase();
    
    public Knowledge getKnowledge();
    
    public SemanticTag getDescription();
    
    public SharkCS getContext();
    
}
