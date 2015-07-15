/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace.knowledgeBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;

/**
 *
 * @author jgrundma
 */
public final class SubSpaceAlgebra<T extends SharkKB>
{

    private static final String SUBSPACES_PROPERTY = "net.sharkfw.subspace.SubSpaceAlgebra#SUBSPACES_PROPERTY";

    public static boolean identical(final SubSpace firstSubSpace, final SubSpace secondSubSpace)
    {
        final SemanticTag firstSubSpaceDescription = firstSubSpace.getDescription();
        final SemanticTag secondSubSpaceDescription = secondSubSpace.getDescription();
        return firstSubSpaceDescription.identical(secondSubSpaceDescription);
    }

//    public List<SubSpace> extractSubSpaces(final SubSpaceFactory<T> factory, final T knowledgeBase) throws SharkKBException
//    {
//        final List<SubSpace> extractedSubSpaces = new ArrayList<>();
//        final STSet deserializedSubspaceDescriptions = extractSubspaceDescriptions(knowledgeBase);
//        final Enumeration<SemanticTag> subspaceDescriptions = deserializedSubspaceDescriptions.tags();
//        while (subspaceDescriptions.hasMoreElements())
//        {
//            final SemanticTag description = subspaceDescriptions.nextElement();
//            final SubSpace subSpace = factory.createSubSpace(description, knowledgeBase);
//            extractedSubSpaces.add(subSpace);
//        }
//        return extractedSubSpaces;
//    }
//
//    public void assimilateSubSpace(final Collection<SubSpace> subSpaces, final T knowledgeBase) throws SharkKBException
//    {
//        final STSet subspaceDescriptions = extractSubspaceDescriptions(knowledgeBase);
//        for (SubSpace subSpace : subSpaces)
//        {
//            final SemanticTag description = subSpace.getDescription();
//            subspaceDescriptions.merge(description);
//        }
//        setSubspaceDescriptions(subspaceDescriptions, knowledgeBase);
//    }
//
//    public void assimilateSubSpace(final SubSpace subSpaces, final T knowledgeBase) throws SharkKBException
//    {
//        final Set<SubSpace> singelton = Collections.singleton(subSpaces);
//        assimilateSubSpace(singelton, knowledgeBase);
//    }
//
//    public void removeSubSpace(final SemanticTag description, final T knowledgeBase) throws SharkKBException
//    {
//        STSet subspaceDescriptions = extractSubspaceDescriptions(knowledgeBase);
//        subspaceDescriptions.removeSemanticTag(description);
//        setSubspaceDescriptions(subspaceDescriptions, knowledgeBase);
//    }
//
//    public STSet extractSubspaceDescriptions(final T knowledgeBase) throws SharkKBException
//    {
//        final String subspacesProperty = knowledgeBase.getProperty(SUBSPACES_PROPERTY);
//        final XMLSerializer serializer = new XMLSerializer();
//        serializer.deserializeSharkCS(subspacesProperty)
//        final STSet deserializedSubspaceDescriptions = new InMemoSTSet();
//        serializer.deserializeSTSet(deserializedSubspaceDescriptions, subspacesProperty);
//        return deserializedSubspaceDescriptions;
//    }
//
//    private void setSubspaceDescriptions(final STSet descriptions, final T knowledgeBase) throws SharkKBException
//    {
//        final XMLSerializer serializer = new XMLSerializer();
//        final String serializedSubspaceDescriptions = serializer.serializeSTSet(descriptions);
//        knowledgeBase.setProperty(SUBSPACES_PROPERTY, serializedSubspaceDescriptions);
//    }
}
