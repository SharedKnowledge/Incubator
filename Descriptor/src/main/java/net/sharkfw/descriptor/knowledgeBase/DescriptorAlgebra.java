/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

import example.descriptor.chat.javafx.ChatViewController;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoKnowledge;
import net.sharkfw.system.L;

/**
 * This class extract {@link Knowledge} form an {@link SharkKB} based on a
 * {@link ContextSpaceDescriptor}. There are 3 ways of extraction. Extraction of
 * the context of a descriptor, extraction od its subtree and extraction of it
 * whole tree.
 *
 * @author Nitros Razril (pseudonym)
 */
public final class DescriptorAlgebra
{

    /**
     * This class only has static methods.
     */
    private DescriptorAlgebra()
    {
    }

    /**
     * Gets the {@link SharkCS} of the descriptor and passes it to
     * {@link SharkCSAlgebra#extract(SharkKB, SharkCS)}. <br/>
     * If the descriptor is empty. A {@link Knowledge} with no
     * {@link ContextPoint} ist returned.
     *
     * @param source The SharkKB to extract from.
     * @param descriptor The descriptor describing the data.
     * @return The exctracted Knowledge.
     * @throws SharkKBException Any exception
     * {@link SharkCSAlgebra#extract(SharkKB, SharkCS)} throws.
     */
    public static Knowledge extract(final SharkKB source, final ContextSpaceDescriptor descriptor) throws SharkKBException
    {
        final SharkCS context = descriptor.getContext();
        Knowledge knowledge = new InMemoKnowledge();
        if (!descriptor.isEmpty())
        {
            try
            {
                // Bug in SyncKB: Can throw NullPointerException, when there are no
                // ContextPoints to extract.
                knowledge = SharkCSAlgebra.extract(source, context);
            } catch (NullPointerException ex)
            {
                final String message = "Workaround for Null Pointer Bug in SyncKB. Returning empty knowledge.";
                Logger.getLogger(DescriptorAlgebra.class.getName()).log(Level.INFO, message);
            }
        }
        return knowledge;
    }

    /**
     * Convenience method. Gets the {@link SharkKB} vom the schema and delegates
     * to {@link #extract(SharkKB, ContextSpaceDescriptor)}.
     *
     * @param schema Schema to get the {@link SharkKB} from.
     * @param descriptor The descriptor describing the data.
     * @return The exctracted Knowledge.
     * @throws SharkKBException SharkKBException Any exception
     * {@link #extract(SharkKB, ContextSpaceDescriptor)} throws.
     */
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

    public static Knowledge extractWholeTree(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException, SharkKBException
    {
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
