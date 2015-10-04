/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.util;

import net.sharkfw.descriptor.knowledgeBase.ContextSpaceDescriptor;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class DescriptorDummyFactory
{

    public static ContextSpaceDescriptor createSimpleDescriptor(final String name, final String subjectIdentifier) throws SharkKBException
    {
        final SemanticTag topic = new InMemoSTSet().createSemanticTag(name, subjectIdentifier);
        final ContextCoordinates contextCoordinates = new InMemoSharkKB().createContextCoordinates(
                topic,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_NOTHING
        );
        return new ContextSpaceDescriptor(contextCoordinates, subjectIdentifier);
    }
}
