/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.descriptor.knowledgeBase;

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

/**
 * This class extract {@link Knowledge} form an {@link SharkKB} based on a
 * {@link ContextSpaceDescriptor}. There are 3 ways of extraction. Extraction of
 * the context of a descriptor, extraction of its subtree and extraction of it
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
     * {@link ContextPoint} is returned.
     *
     * @param source The SharkKB to extract from.
     * @param descriptor The descriptor describing the data.
     * @return The extracted Knowledge.
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
     * Convenience method. Gets the {@link SharkKB} from the schema and delegates
     * to {@link #extract(SharkKB, ContextSpaceDescriptor)}.
     *
     * @param schema Schema to get the {@link SharkKB} from.
     * @param descriptor The descriptor describing the data.
     * @return The extracted Knowledge.
     * @throws SharkKBException SharkKBException Any exception
     * {@link #extract(SharkKB, ContextSpaceDescriptor)} throws.
     */
    public static Knowledge extract(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws SharkKBException
    {
        final SharkKB source = schema.getSharkKB();
        return extract(source, descriptor);
    }

    /**
     * Extracts a subtree from a schema. The resulting {@link Knowledge} will
     * contain the Knowledge of the context of the given descriptor as well as
     * all its children. It uses an recursively algorithm to do so.
     * 
     * @param schema The schema to extract from.
     * @param descriptor The descriptor to extract the subtree for.
     * @return Knowledge of the context of the given descriptor as well as
     * all its children.
     * @throws DescriptorSchemaException Any error while getting the children.
     * @throws SharkKBException Any error while extracting the Knowledge.
     */
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

    /**
     * Extracts the {@link Knowledge} of a whole tree. It will first search 
     * the root of the tree of the given descriptor and that extract the roots
     * subtree. This results in getting the Knowledge of the whole tree.
     * 
     * @param schema The schema to extract from.
     * @param descriptor The descriptor to extract the tree for. 
     * @return Knowledge of the whole tree of the descriptor.
     * @throws DescriptorSchemaException Any error while getting the children and parents.
     * @throws SharkKBException Any error while extracting the Knowledge.
     */
    public static Knowledge extractWholeTree(final DescriptorSchema schema, final ContextSpaceDescriptor descriptor) throws DescriptorSchemaException, SharkKBException
    {
        ContextSpaceDescriptor root = descriptor;
        while (root.hasParent())
        {
            root = schema.getParent(root);
        }
        return extractWithSubtree(schema, root);
    }

    /**
     * Merges to {@link Knowledge} into one with no identical {@link ContextPoint}.
     * 
     * @param firstKnowledge First Knowledge to merge.
     * @param secondKnowledge First Knowledge to merge.
     * @return Merged Knowledge with no identical ContextPoint.
     */
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
