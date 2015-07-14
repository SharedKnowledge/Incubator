/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.subspace;

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
public final class SubSpaceAlgebra
{

    private static final String SUBSPACES_PROPERTY = "net.sharkfw.subspace.SubSpaceAlgebra#SUBSPACES_PROPERTY";
    
    private SubSpaceAlgebra()
    {
    }

    public static boolean identical(final SubSpace firstSubSpace, final SubSpace secondSubSpace)
    {
        final SemanticTag firstSubSpaceDescription = firstSubSpace.getDescription();
        final SemanticTag secondSubSpaceDescription = secondSubSpace.getDescription();
        return firstSubSpaceDescription.identical(secondSubSpaceDescription);
    }

    public static List<SubSpace> extractAllSubSpaces(final SubSpaceFactory factory, final SharkKB knowledgeBase) throws SharkKBException
    {
        final List<SubSpace> extractedSubSpaces = new ArrayList<>();
        final STSet deserializedSubspaceDescriptions = getSubspaceDescriptions(knowledgeBase);
        final Enumeration<SemanticTag> subspaceDescriptions = deserializedSubspaceDescriptions.tags();
        while (subspaceDescriptions.hasMoreElements())
        {
            final SemanticTag description = subspaceDescriptions.nextElement();
            final SubSpace subSpace = factory.createSubSpace(description, knowledgeBase);
            extractedSubSpaces.add(subSpace);
        }
        return extractedSubSpaces;
    }

    public static void assimilateSubSpace(final Collection<SubSpace> subSpaces, final SharkKB knowledgeBase) throws SharkKBException
    {
        final STSet subspaceDescriptions = getSubspaceDescriptions(knowledgeBase);
        for (SubSpace subSpace : subSpaces)
        {
            final SemanticTag description = subSpace.getDescription();
            subspaceDescriptions.merge(description);
        }
        setSubspaceDescriptions(subspaceDescriptions, knowledgeBase);
    }

    public static void assimilateSubSpace(final SubSpace subSpaces, final SharkKB knowledgeBase) throws SharkKBException
    {
        final Set<SubSpace> singelton = Collections.singleton(subSpaces);
        assimilateSubSpace(singelton, knowledgeBase);
    }

    private static STSet getSubspaceDescriptions(final SharkKB knowledgeBase) throws SharkKBException
    {
        final String subspacesProperty = knowledgeBase.getProperty(SUBSPACES_PROPERTY);
        final XMLSerializer serializer = new XMLSerializer();
        final STSet deserializedSubspaceDescriptions = new InMemoSTSet();
        serializer.deserializeSTSet(deserializedSubspaceDescriptions, subspacesProperty);
        return deserializedSubspaceDescriptions;
    }

    private static void setSubspaceDescriptions(final STSet subspaceDescriptions, final SharkKB knowledgeBase) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        final String serializedSubspaceDescriptions = serializer.serializeSTSet(subspaceDescriptions);
        knowledgeBase.setProperty(SUBSPACES_PROPERTY, serializedSubspaceDescriptions);
    }
}
