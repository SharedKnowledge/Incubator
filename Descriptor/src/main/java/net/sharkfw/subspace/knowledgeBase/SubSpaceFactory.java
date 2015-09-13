/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.knowledgeBase;

import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;

/**
 * 
 * @author Nitros Razril (pseudonym)
 */
@Deprecated
public interface SubSpaceFactory
{

    public SubSpace createSubSpace(SemanticTag description, SharkCS context);
}
