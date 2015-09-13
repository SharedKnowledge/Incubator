/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.system;

import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 *
 * @author Nitros
 */
public final class TagUtils
{

    private TagUtils()
    {
    }
    
    public static boolean containsTag(final SemanticTag tag, final STSet set) throws SharkKBException{
        final String identifier[] = tag.getSI();
        return set.getSemanticTag(identifier) != null;
    }
}
