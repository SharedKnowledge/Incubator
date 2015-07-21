/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.test.util;

import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoPeerSTSet;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.subspace.knowledgeBase.StandardSubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class SubSpaceDummyFactory
{

    private static final STSet SEMANTIC_TAG_FACTORY = new InMemoPeerSTSet();
    private static final SharkKB CONTEX_FACTORY = new InMemoSharkKB();

    private static class StandardSubSpaceFactory implements SubSpaceFactory
    {

        @Override
        public SubSpace createSubSpace(final SemanticTag description, final SharkCS context)
        {
            return new StandardSubSpace(context, description);
        }
    }

    private SubSpaceDummyFactory()
    {
    }

    public static SubSpace createSimpleSubSpace(final String name, final String subjectIdentifier) throws SharkKBException
    {
        final SemanticTag topic = SEMANTIC_TAG_FACTORY.createSemanticTag(name, subjectIdentifier);
        final ContextCoordinates contextCoordinates = CONTEX_FACTORY.createContextCoordinates(
                topic,
                null,
                null,
                null,
                null,
                null,
                SharkCS.DIRECTION_NOTHING
        );
        return new StandardSubSpace(contextCoordinates, topic);
    }

    public static SubSpaceFactory getStandardSubSpaceFactory()
    {
        return new StandardSubSpaceFactory();
    }
}
