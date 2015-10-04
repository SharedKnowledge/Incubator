/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;

/**
 *
 * @author Nitros Razril (pseudonym)
 */
public final class DescriptorAlgebra
{

    private DescriptorAlgebra()
    {
    }

    public static Knowledge extract(final SharkKB source, final ContextSpaceDescriptor descriptor) throws SharkKBException
    {
        final SharkCS context = descriptor.getContext();
        Knowledge knowledge = new InMemoKnowledge();
        if(!descriptor.isEmpty()){
            knowledge = SharkCSAlgebra.extract(source, context);
        }
        return knowledge;
    }

    public static Knowledge extract(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws SharkKBException
    {
        final SharkKB source = schema.getSharkKB();
        return extract(source, descriptor);
    }

    public static Knowledge extractWithSubtree(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException, SharkKBException
    {
        Knowledge finalKnowledge = new InMemoKnowledge();
        final Knowledge currentNodeKnowledge = extract(schema, descriptor);
        final Set<ContextSpaceDescriptor> children = schema.getChildren(descriptor);
        for (ContextSpaceDescriptor child : children)
        {
            final Knowledge childKnowledge = extractWithSubtree(schema, child);
            finalKnowledge = mergeKnowledge(finalKnowledge, childKnowledge);
        }
        finalKnowledge = mergeKnowledge(finalKnowledge, currentNodeKnowledge);
        return finalKnowledge;
    }
    
    public static Knowledge extractWholeTree(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException, SharkKBException{
        ContextSpaceDescriptor root = descriptor;
        while (root.hasParent())
        {
            root = schema.getParent(root);    
        }
        return extractWithSubtree(schema, root);
    }

    private static Knowledge mergeKnowledge(final Knowledge firstKnowledge, final Knowledge secondKnowledge)
    {
        final Knowledge mergedKnowledge = new InMemoKnowledge();
        final Set<ContextPoint> contextPointsToAdd = new HashSet<>();
        final Enumeration<ContextPoint> firstContextPoints = firstKnowledge.contextPoints();
        while (firstContextPoints.hasMoreElements())
        {
            final ContextPoint contextPoint = firstContextPoints.nextElement();
            contextPointsToAdd.add(contextPoint);
        }
        final Enumeration<ContextPoint> secondContextPoints = secondKnowledge.contextPoints();
        while (secondContextPoints.hasMoreElements())
        {
            final ContextPoint contextPoint = secondContextPoints.nextElement();
            contextPointsToAdd.add(contextPoint);
        }
        for (ContextPoint contextPoint : contextPointsToAdd)
        {
            mergedKnowledge.addContextPoint(contextPoint);
        }
        return mergedKnowledge;

    }

}
