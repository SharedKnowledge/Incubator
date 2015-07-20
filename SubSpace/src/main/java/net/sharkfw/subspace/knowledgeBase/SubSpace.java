/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sharkfw.subspace.knowledgeBase;

import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public interface SubSpace
{    
    public Knowledge getKnowledge(SharkKB base);
    
    public SemanticTag getDescription();
    
    public SharkCS getContext();
}
