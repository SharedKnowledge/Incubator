/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sharkfw.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.sharkfw.kep.format.XMLSerializer;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;

/**
 *
 * @author jgrundma
 */
class SharkCSAdapter extends XmlAdapter<String, SharkCS>
{
    @Override
    public SharkCS unmarshal(final String xml) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        return serializer.deserializeSharkCS(xml);
    }

    @Override
    public String marshal(final SharkCS context) throws SharkKBException
    {
        final XMLSerializer serializer = new XMLSerializer();
        return serializer.serializeSharkCS(context);
    }

}
