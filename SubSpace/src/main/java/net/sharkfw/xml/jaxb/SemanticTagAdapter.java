/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSTSet;

/**
 *
 * @author jgrundma
 */
class SemanticTagAdapter extends XmlAdapter<String, SemanticTag>
{

    @Override
    public SemanticTag unmarshal(final String xml) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        final STSet set = new InMemoSTSet();
        serializer.deserializeSTSet(set, xml);
        if (set.size() != 1)
        {
            throw new IllegalArgumentException(
                    "Given xml string must be deserializable to a "
                    + STSet.class.getName()
                    + " which conatins excactly one " + SemanticTag.class.getName()
                    + ". The tag was not serializerd correctly."
            );
        }
        return set.stTags().next();
    }

    @Override
    public String marshal(final SemanticTag tag) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        final STSet set = new InMemoSTSet();
        set.merge(tag);
        return serializer.serializeSTSet(set);
    }

}
