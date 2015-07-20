/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import java.util.ArrayList;
import java.util.List;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.subspace.knowledgeBase.SubSpace;
import net.sharkfw.subspace.knowledgeBase.SubSpaceFactory;

/**
 *
 * @author jgrundma
 */
final class Converter
{

    private Converter()
    {
    }

    public static XmlSubSpaceDataHolder convertSubSpace(final SubSpace subspace)
    {
        final SemanticTag description = subspace.getDescription();
        final SharkCS context = subspace.getContext();
        final XmlSubSpaceDataHolder dataHolder = new XmlSubSpaceDataHolder();
        dataHolder.setContext(context);
        dataHolder.setDescription(description);
        return dataHolder;
    }

    public static SubSpace convertSubSpaceDataHolder(final XmlSubSpaceDataHolder dataHolder, final SubSpaceFactory factory)
    {
        final SemanticTag description = dataHolder.getDescription();
        final SharkCS context = dataHolder.getContext();
        return factory.createSubSpace(description, context);
    }

    public static List<XmlSubSpaceDataHolder> convertSubSpaceList(final List<SubSpace> subspaces)
    {
        final List<XmlSubSpaceDataHolder> list = new ArrayList<>();
        for (SubSpace subSpace : subspaces)
        {
            final XmlSubSpaceDataHolder dataHolder = convertSubSpace(subSpace);
            list.add(dataHolder);
        }
        return list;
    }

    public static List<SubSpace> convertSubSpaceDataHolderList(final List<XmlSubSpaceDataHolder> list, final SubSpaceFactory factory)
    {
        final List<SubSpace> subspaces = new ArrayList<>();
        for (XmlSubSpaceDataHolder dataHolder : list)
        {
            final SubSpace subspace = convertSubSpaceDataHolder(dataHolder, factory);
            subspaces.add(subspace);
        }
        return subspaces;
    }

}
